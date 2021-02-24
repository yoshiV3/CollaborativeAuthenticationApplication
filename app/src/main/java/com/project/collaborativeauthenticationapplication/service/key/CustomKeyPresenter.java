package com.project.collaborativeauthenticationapplication.service.key;



import com.project.collaborativeauthenticationapplication.R;
import com.project.collaborativeauthenticationapplication.logger.AndroidLogger;
import com.project.collaborativeauthenticationapplication.logger.Logger;
import com.project.collaborativeauthenticationapplication.service.Participant;
import com.project.collaborativeauthenticationapplication.service.key.application.Client;
import com.project.collaborativeauthenticationapplication.service.key.application.CustomClient;
import com.project.collaborativeauthenticationapplication.service.key.application.ThreadedClient;
import com.project.collaborativeauthenticationapplication.service.key.user.DistributedKeyGenerationActivity;
import com.project.collaborativeauthenticationapplication.service.key.user.KeyView;
import com.project.collaborativeauthenticationapplication.service.key.user.ProgressNotifier;
import com.project.collaborativeauthenticationapplication.service.key.user.ProgressView;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;


public class CustomKeyPresenter implements KeyPresenter, ProgressNotifier {

    private static final String COMPONENT_NAME         = "Key Presenter";


    private static final String MESSAGE_STATE_SESSION            =  "Session successfully generated";
    private static final String MESSAGE_STATE_INVITATIONS        =  "Remote devices were invited";

    private static final String EVENT_START                      = "Key generation process started";
    private static final String EVENT_RECEIVED_TOKEN             = "Token received";
    private static final String EVENT_NOT_RECEIVED_TOKEN         =  "Token not received";
    private static final String EVENT_SERVICE_DISABLED           =  "Service disabled";
    private static final String EVENT_DETAILS_SUBMITTED          =  "Details were submitted";
    private static final String EVENT_DETAILS_NOT_SUBMITTED      =  "Details were not submitted (token revoked)";
    private static final String EVENT_PARTICIPANTS_NOT_SUBMITTED =  "Participants not submitted (token revoked)";
    private static final String EVENT_PARTICIPANTS_SUBMITTED     =  "Participants were submitted";
    private static final String EVENT_SESSION_FAILED             =  "Could not create session";
    private static final String EVENT_NEW_SUBSCRIBER             =  "New subscriber is added";
    private static final String EVENT_OLD_SUBSCRIBER             =  "Old subscriber issued new request: no change";
    private static final String EVENT_REMOVED_SUBSCRIBER         =  "A subscriber was removed";
    private static final String EVENT_NEW_REMOVED_SUBSCRIBER     =  "Removed:Subscriber was not/ no longer available: no change";
    private static final String EVENT_NOTIFY_SUBSCRIBER          =  "Notification send";



    private Logger logger = new AndroidLogger();

    private static KeyPresenter instance;


    private HashSet<ProgressView> subscribers = new HashSet<>();


    public static KeyPresenter getInstance()
    {
        return instance;
    }


    public static void newInstance(KeyView view) {

        instance = new CustomKeyPresenter(view);
    }

    private final KeyView view;


    private Client client;

    private HashMap<String, String> messages = new HashMap<>();




    private CustomKeyPresenter(KeyView view)
    {
        this.view = view;
        client = new ThreadedClient(this);
    }

    @Override
    public void onStart() {
        logger.logEvent(COMPONENT_NAME, EVENT_START, "low");
        client.open();
    }

    @Override
    public void close() {
        if (client != null)
        {
            client.close();
            client = null;
        }
    }


    @Override
    public void onStop() {
        close();
    }

    @Override
    public void onPause() {
        close();
        int destination = view.locate();
        switch (destination)
        {
            case R.id.deviceSelectionFragment:
                view.navigate(R.id.action_deviceSelectionFragment_to_homeFragment);
                break;
            case R.id.generationFragment:
                view.navigate(R.id.action_generationFragment_to_homeFragment);
                break;
        }
    }

    @Override
    public void onRun() {
        client.run();
    }

    @Override
    public boolean isCurrentlyActive() {
        return client != null && client.getState() != CustomClient.STATE_FINISHED;
    }

    @Override
    public void setMessage(String key, String message) {
        messages.put(key, message);
    }

    @Override
    public String getMessage(String key) {
        return messages.getOrDefault(key, "");
    }

    @Override
    public void SignalClientInNewState(int clientState, int oldState) {
        switch (clientState)
        {
            case CustomClient.STATE_START:
                logger.logEvent(COMPONENT_NAME, EVENT_RECEIVED_TOKEN, "low");
                break;
            case CustomClient.STATE_DETAILS:
                view.navigate(R.id.select);
                logger.logEvent(COMPONENT_NAME, EVENT_DETAILS_SUBMITTED, "low");
                view.showMetaData(getMessage(DistributedKeyGenerationActivity.KEY_LOGIN), getMessage(DistributedKeyGenerationActivity.KEY_APPLICATION_NAME));
                break;
            case CustomClient.STATE_SELECT:
                logger.logEvent(COMPONENT_NAME, EVENT_PARTICIPANTS_SUBMITTED, "low");
                view.navigate(R.id.run);
                break;
            case CustomClient.STATE_BAD_INP_SEL:
                view.showTemporally("Incorrect input");
                break;
            case CustomClient.STATE_SESSION:
                notifySubscribers(MESSAGE_STATE_SESSION);
                break;
            case CustomClient.STATE_INVITATION:
                notifySubscribers(MESSAGE_STATE_INVITATIONS);
                break;
            case CustomClient.STATE_ERROR:
                handleClientErrors(oldState);
                break;
            case CustomClient.STATE_CLOSED:
                break;
            case CustomClient.STATE_FINISHED:
                client = null;
                break;
            default:
                logger.logError(COMPONENT_NAME, "impossible state of the client", "Critical");
                throw new IllegalStateException("Impossible return state");

        }
    }

    private void handleClientErrors(int oldState) {
        client.close();
        switch (oldState)
        {
            case CustomClient.STATE_CLOSED:
                logger.logEvent(COMPONENT_NAME, EVENT_NOT_RECEIVED_TOKEN, "low");
                view.navigate(R.id.error_home);
                break;
            case CustomClient.STATE_START:
                logger.logEvent(COMPONENT_NAME, EVENT_DETAILS_NOT_SUBMITTED, "low");
                setMessage(DistributedKeyGenerationActivity.KEY_ERROR_MESSAGES, "Token revocation");
                view.navigate(R.id.error_home);
                break;
            case CustomClient.STATE_DETAILS:
                logger.logEvent(COMPONENT_NAME, EVENT_PARTICIPANTS_NOT_SUBMITTED, "high");
                setMessage(DistributedKeyGenerationActivity.KEY_ERROR_MESSAGES, "Token revocation");
                view.navigate(R.id.error_select);
                break;
            case CustomClient.STATE_BAD_INP_SEL:
                logger.logEvent(COMPONENT_NAME, EVENT_SESSION_FAILED, "high");
                setMessage(DistributedKeyGenerationActivity.KEY_ERROR_MESSAGES, "Token revocation");
                view.navigate(R.id.error_select);
                break;
            case CustomClient.STATE_SELECT:
            case CustomClient.STATE_SESSION:
            case CustomClient.STATE_INVITATION:
                logger.logEvent(COMPONENT_NAME, EVENT_SESSION_FAILED, "high");
                setMessage(DistributedKeyGenerationActivity.KEY_ERROR_MESSAGES, "Session failed");
                view.navigate(R.id.error_generation);
                break;
            default:
                logger.logError(COMPONENT_NAME, "impossible state of the client", "Critical");
                throw new IllegalStateException("Impossible return state");
        }
    }

    @Override
    public List<Participant> getInitialOptions() {
        return client.getOptions();
    }


    @Override
    public void submitLoginDetails() {
        client.submitLoginDetails(getMessage(DistributedKeyGenerationActivity.KEY_LOGIN), getMessage(DistributedKeyGenerationActivity.KEY_APPLICATION_NAME));
    }

    @Override
    public void submitSelectedParticipants(List<Participant> participants) {
        client.submitSelection(participants);
    }


    @Override
    public void subscribe(ProgressView view) {
        boolean added = subscribers.add(view);
        if (added)
        {
            logger.logEvent(COMPONENT_NAME, EVENT_NEW_SUBSCRIBER, "low");
        }
        else
        {
            logger.logEvent(COMPONENT_NAME, EVENT_OLD_SUBSCRIBER, "low");
        }
    }

    @Override
    public void unSubScribe(ProgressView view) {
        boolean removed = subscribers.remove(view);
        if (removed)
        {
            logger.logEvent(COMPONENT_NAME, EVENT_REMOVED_SUBSCRIBER, "low");
        }
        else
        {
            logger.logEvent(COMPONENT_NAME, EVENT_NEW_REMOVED_SUBSCRIBER, "low");
        }
    }

    private void notifySubscribers(String text)
    {
        logger.logEvent(COMPONENT_NAME, EVENT_NOTIFY_SUBSCRIBER, "low", text);
        for (ProgressView subscriber: subscribers)
        {
            subscriber.pushNewMessage(text);
        }
    }
}
