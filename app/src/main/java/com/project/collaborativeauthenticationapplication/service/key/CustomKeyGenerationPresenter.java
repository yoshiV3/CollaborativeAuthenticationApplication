package com.project.collaborativeauthenticationapplication.service.key;



import com.project.collaborativeauthenticationapplication.R;
import com.project.collaborativeauthenticationapplication.logger.AndroidLogger;
import com.project.collaborativeauthenticationapplication.logger.Logger;
import com.project.collaborativeauthenticationapplication.service.Participant;
import com.project.collaborativeauthenticationapplication.service.key.application.keyGenerationClient;
import com.project.collaborativeauthenticationapplication.service.key.application.CustomKeyGenerationClient;
import com.project.collaborativeauthenticationapplication.service.key.application.ThreadedKeyGenerationClient;
import com.project.collaborativeauthenticationapplication.service.key.user.DistributedKeyGenerationActivity;
import com.project.collaborativeauthenticationapplication.service.key.user.KeyGenerationView;
import com.project.collaborativeauthenticationapplication.service.key.user.ProgressNotifier;
import com.project.collaborativeauthenticationapplication.service.key.user.ProgressView;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;


public class CustomKeyGenerationPresenter implements KeyGenerationPresenter, ProgressNotifier {

    private static final String COMPONENT_NAME         = "Key Presenter";


    private static final String MESSAGE_STATE_SESSION            =  "Session successfully generated";
    private static final String MESSAGE_STATE_INVITATIONS        =  "Remote devices were invited";
    private static final String MESSAGE_STATE_KEYPART            =  "Local parts are generated";
    private static final String MESSAGE_BAD_LOGIN                =  "Credentials are not available";

    private static final String EVENT_START                         = "Key generation process started";
    private static final String EVENT_RECEIVED_TOKEN                = "Token received";
    private static final String EVENT_NOT_RECEIVED_TOKEN            =  "Token not received";
    private static final String EVENT_SERVICE_DISABLED              =  "Service disabled";
    private static final String EVENT_DETAILS_SUBMITTED             =  "Details were submitted";
    private static final String EVENT_DETAILS_NOT_SUBMITTED         =  "Details were not submitted (token revoked)";
    private static final String EVENT_PARTICIPANTS_NOT_SUBMITTED    =  "Participants not submitted (token revoked)";
    private static final String EVENT_PARTICIPANTS_SUBMITTED        =  "Participants were submitted";
    private static final String EVENT_SESSION_FAILED                =  "Could not create session";
    private static final String EVENT_NEW_SUBSCRIBER                =  "New subscriber is added";
    private static final String EVENT_OLD_SUBSCRIBER                =  "Old subscriber issued new request: no change";
    private static final String EVENT_REMOVED_SUBSCRIBER            =  "A subscriber was removed";
    private static final String EVENT_NEW_REMOVED_SUBSCRIBER        =  "Removed:Subscriber was not/ no longer available: no change";
    private static final String EVENT_NOTIFY_SUBSCRIBER             =  "Notification send";
    private static final String EVENT_THRESHOLD_SUBMITTED           =  "Threshold was correctly submitted";
    private static final String EVENT_KEY_PARTS_LOCALLY_AVAILABLE   =  "Key parts are locally available";
    private static final String MESSAGE_STATE_DISTRIBUTED           =  "Key parts are distributed to the local and remote key part handlers";
    private static final String MESSAGE_STATE_SHARES                =  "Local key shares were calculated";
    private static final String MESSAGE_STATE_PERSISTED             =  "Local data was persisted";
    private static final String EVENT_UNAVAILABLE_LOGIN             =  "Could not submit the requested login";
    private static final String EVENT_NEW_MESSAGE                   =  "New message received" ;
    private static final String EVENT_KEY_DONE                      =  "Finished the key generation process";
    private static final String EVENT_RUN_FAILED                    =  "Something went wrong during the generation process";


    private Logger logger = new AndroidLogger();

    private static KeyGenerationPresenter instance;


    private HashSet<ProgressView> subscribers = new HashSet<>();


    public static KeyGenerationPresenter getInstance()
    {
        return instance;
    }


    public static void newInstance(KeyGenerationView view) {

        instance = new CustomKeyGenerationPresenter(view);
    }

    private final KeyGenerationView view;


    private keyGenerationClient client;

    private HashMap<String, String> messages = new HashMap<>();




    private CustomKeyGenerationPresenter(KeyGenerationView view)
    {
        this.view = view;
    }

    @Override
    public void onStart() {
        logger.logEvent(COMPONENT_NAME, EVENT_START, "low");
        if (client != null){
            throw new IllegalStateException();
        }
        client = new ThreadedKeyGenerationClient(this);
        client.open(view.getContext());
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
    public void onBackPressed() {
        if (isCurrentlyActive()){
            close();
        }
        view.onDone();
    }

    @Override
    public void onJobDone() {
        if (isCurrentlyActive()){
            close();
        }
        view.onDone();
    }

    @Override
    public void onRun() {
        client.run();
    }

    @Override
    public boolean isCurrentlyActive() {
        return client != null && client.getState() != CustomKeyGenerationClient.STATE_FINISHED && client.getState() != CustomKeyGenerationClient.STATE_CLOSED;
    }

    @Override
    public void setMessage(String key, String message) {
        messages.put(key, message);
        logger.logEvent(COMPONENT_NAME, EVENT_NEW_MESSAGE, "low", message);
    }

    @Override
    public String getMessage(String key) {
        return messages.getOrDefault(key, "");
    }

    @Override
    public void SignalClientInNewState(int clientState, int oldState) {
        switch (clientState)
        {
            case CustomKeyGenerationClient.STATE_START:
                logger.logEvent(COMPONENT_NAME, EVENT_RECEIVED_TOKEN, "low");
                break;
            case CustomKeyGenerationClient.STATE_DETAILS:
                view.navigate(R.id.select);
                logger.logEvent(COMPONENT_NAME, EVENT_DETAILS_SUBMITTED, "low");
                view.showMetaData(getMessage(DistributedKeyGenerationActivity.KEY_LOGIN), getMessage(DistributedKeyGenerationActivity.KEY_APPLICATION_NAME));
                break;
            case CustomKeyGenerationClient.STATE_SELECT:
                logger.logEvent(COMPONENT_NAME, EVENT_PARTICIPANTS_SUBMITTED, "low");
                view.navigate(R.id.run);
                break;
            case CustomKeyGenerationClient.STATE_BAD_INP_SEL:
                view.showTemporally("Incorrect input");
                break;
            case CustomKeyGenerationClient.STATE_SESSION:
                notifySubscribers(MESSAGE_STATE_SESSION);
                break;
            case CustomKeyGenerationClient.STATE_INVITATION:
                notifySubscribers(MESSAGE_STATE_INVITATIONS);
                break;
            case CustomKeyGenerationClient.STATE_KEYPART:
                logger.logEvent(COMPONENT_NAME, EVENT_KEY_PARTS_LOCALLY_AVAILABLE, "low");
                notifySubscribers(MESSAGE_STATE_KEYPART);
                break;
            case CustomKeyGenerationClient.STATE_DISTRIBUTED:
                notifySubscribers(MESSAGE_STATE_DISTRIBUTED);
                break;
            case CustomKeyGenerationClient.STATE_SHARES:
                notifySubscribers(MESSAGE_STATE_SHARES);
                break;
            case CustomKeyGenerationClient.STATE_PERSIST:
                notifySubscribers(MESSAGE_STATE_PERSISTED);
                break;
            case CustomKeyGenerationClient.STATE_ERROR:
                handleClientErrors(oldState);
                break;
            case CustomKeyGenerationClient.STATE_CLOSED:
                break;
            case CustomKeyGenerationClient.STATE_FINISHED:
                logger.logEvent(COMPONENT_NAME, EVENT_KEY_DONE, "low");
                client.close();
                view.navigate(R.id.success);
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
            case CustomKeyGenerationClient.STATE_INIT:
                logger.logEvent(COMPONENT_NAME, EVENT_NOT_RECEIVED_TOKEN, "low");
                view.navigate(R.id.error_home);
                break;
            case CustomKeyGenerationClient.STATE_START:
                logger.logEvent(COMPONENT_NAME, EVENT_DETAILS_NOT_SUBMITTED, "low");
                setMessage(DistributedKeyGenerationActivity.KEY_ERROR_MESSAGES, "Token revocation");
                view.navigate(R.id.error_home);
                break;
            case CustomKeyGenerationClient.STATE_DETAILS:
                logger.logEvent(COMPONENT_NAME, EVENT_PARTICIPANTS_NOT_SUBMITTED, "high");
                setMessage(DistributedKeyGenerationActivity.KEY_ERROR_MESSAGES, "Token revocation");
                view.navigate(R.id.error_select);
                break;
            case CustomKeyGenerationClient.STATE_BAD_INP_SEL:
                logger.logEvent(COMPONENT_NAME, EVENT_SESSION_FAILED, "high");
                setMessage(DistributedKeyGenerationActivity.KEY_ERROR_MESSAGES, "Token revocation");
                view.navigate(R.id.error_select);
                break;
            case CustomKeyGenerationClient.STATE_SELECT:
            case CustomKeyGenerationClient.STATE_SESSION:
            case CustomKeyGenerationClient.STATE_INVITATION:
            case CustomKeyGenerationClient.STATE_KEYPART:
            case CustomKeyGenerationClient.STATE_DISTRIBUTED:
            case CustomKeyGenerationClient.STATE_SHARES:
            case CustomKeyGenerationClient.STATE_PERSIST:
                logger.logEvent(COMPONENT_NAME, EVENT_RUN_FAILED, "high", String.valueOf(oldState));
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
    public void submitLoginDetailsUnsuccessful() {
        logger.logEvent(COMPONENT_NAME, EVENT_UNAVAILABLE_LOGIN,"low");
        view.showTemporally(MESSAGE_BAD_LOGIN);
    }

    @Override
    public void submitSelectedParticipants(List<Participant> participants) {
        client.submitSelection(participants);
    }

    @Override
    public void submitThreshold(int threshold) {
        client.submitThreshold(threshold);
        logger.logEvent(COMPONENT_NAME, EVENT_THRESHOLD_SUBMITTED, "low", String.valueOf(threshold));
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
