package com.project.collaborativeauthenticationapplication.alternative.key;



import com.project.collaborativeauthenticationapplication.R;
import com.project.collaborativeauthenticationapplication.alternative.network.Network;
import com.project.collaborativeauthenticationapplication.logger.AndroidLogger;
import com.project.collaborativeauthenticationapplication.logger.Logger;
import com.project.collaborativeauthenticationapplication.service.general.Participant;
import com.project.collaborativeauthenticationapplication.service.key.application.key_generation.distributed_system.KeyGenerationCoordinator;
import com.project.collaborativeauthenticationapplication.service.key.application.key_generation.distributed_system.LocalKeyGenerationCoordinator;
import com.project.collaborativeauthenticationapplication.alternative.key.user.DistributedKeyGenerationActivity;
import com.project.collaborativeauthenticationapplication.alternative.key.user.KeyGenerationView;
import com.project.collaborativeauthenticationapplication.alternative.key.user.ProgressNotifier;
import com.project.collaborativeauthenticationapplication.alternative.key.user.ProgressView;
import com.project.collaborativeauthenticationapplication.service.key.application.key_generation.distributed_system.RemoteKeyGenerationCoordinator;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;


public class CustomKeyGenerationPresenter implements KeyGenerationPresenter {

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


    public static final int MODE_LEADER   = 0;

    public static final int MODE_FOLLOWER = 1;



    private Logger logger = new AndroidLogger();

    private static KeyGenerationPresenter instance;


    private HashSet<ProgressView> subscribers = new HashSet<>();


    private int mode = 0;


    public static KeyGenerationPresenter getInstance()
    {
        return instance;
    }


    public static void newInstance(KeyGenerationView view) {

        instance = new CustomKeyGenerationPresenter(view);
    }

    private final KeyGenerationView view;


    private KeyGenerationCoordinator localCoordinator;
    private KeyGenerationCoordinator remoteCoordinator;

    private HashMap<String, String> messages = new HashMap<>();




    private CustomKeyGenerationPresenter(KeyGenerationView view)
    {
        this.view = view;
    }

    @Override
    public void onStart() {
        logger.logEvent(COMPONENT_NAME, EVENT_START, "low");
        if (localCoordinator != null){
            throw new IllegalStateException();
        }
        localCoordinator  = new LocalKeyGenerationCoordinator(this);
        remoteCoordinator = new RemoteKeyGenerationCoordinator(this);
        //localCoordinator.open(view.getContext());
    }

    @Override
    public void close() {
        if (localCoordinator != null)
        {
            localCoordinator.close();
            localCoordinator = null;
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
        if (mode == MODE_LEADER){
            localCoordinator.run();
        } else {
            remoteCoordinator.run();
        }

    }

    @Override
    public void setMode(int mode) {
        this.mode = mode;
    }

    @Override
    public void switchToModel() {
        switch (mode){
            case MODE_LEADER:
                view.navigate(R.id.action_modeSelection_to_homeFragment);
                break;
            case MODE_FOLLOWER:
                view.navigate(R.id.action_modeSelection_to_waitingForLeaderFragment);
                break;
            default:
                throw new IllegalStateException("wrong mode (switch to mode)");
        }
    }

    @Override
    public void openCoordinator() {
        switch (mode){
            case MODE_LEADER:
                logger.logEvent(COMPONENT_NAME, "opening new coordinator", "high");
                Network.getInstance().close();
                localCoordinator.open(view.getContext());
                break;
            case MODE_FOLLOWER:
                logger.logEvent(COMPONENT_NAME, "opening new coordinator", "high");
                remoteCoordinator.open(view.getContext());
                break;
            default:
                throw new IllegalStateException("wrong mode (open coordinator)");
        }

    }

    @Override
    public boolean isCurrentlyActive() {
        return localCoordinator != null && localCoordinator.getState() != LocalKeyGenerationCoordinator.STATE_FINISHED && localCoordinator.getState() != LocalKeyGenerationCoordinator.STATE_CLOSED;
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
    public void signalOpened() {
        logger.logEvent(COMPONENT_NAME, "opened coordinator", "low");

    }

    private Object errorLock = new Object();

    @Override
    public void error() {
        logger.logEvent(COMPONENT_NAME, "error registered", "high");
        synchronized (errorLock){
            int location = view.locate();
            switch (location){
                case R.id.homeFragment:
                    view.navigate(R.id.error_home);
                    break;
                case R.id.deviceSelectionFragment:
                    view.navigate(R.id.error_select);
                    break;
                case R.id.generationFragment:
                    view.navigate(R.id.error_generation);
                    break;
                case R.id.waitingForLeaderFragment:
                    view.navigate(R.id.action_waitingForLeaderFragment_to_errorFragment);
                    break;
                case R.id.leaderFoundFragment:
                    view.navigate(R.id.action_leaderFoundFragment_to_errorFragment);
                    break;
                default:
                    logger.logEvent(COMPONENT_NAME, "error registered, but no change", "high");
            }
        }
        Network.getInstance().closeAllConnections();
    }


    public void SignalCoordinatorInNewState(int clientState, int oldState) {
        switch (clientState)
        {
            case LocalKeyGenerationCoordinator.STATE_START:
                logger.logEvent(COMPONENT_NAME, EVENT_RECEIVED_TOKEN, "low");
                break;
            case LocalKeyGenerationCoordinator.STATE_DETAILS:
                view.navigate(R.id.select);
                logger.logEvent(COMPONENT_NAME, EVENT_DETAILS_SUBMITTED, "low");
                view.showMetaData(getMessage(DistributedKeyGenerationActivity.KEY_LOGIN), getMessage(DistributedKeyGenerationActivity.KEY_APPLICATION_NAME));
                break;
            case LocalKeyGenerationCoordinator.STATE_SELECT:
                logger.logEvent(COMPONENT_NAME, EVENT_PARTICIPANTS_SUBMITTED, "low");
                view.navigate(R.id.run);
                break;
            case LocalKeyGenerationCoordinator.STATE_BAD_INP_SEL:
                view.showTemporally("Incorrect input");
                break;
            case LocalKeyGenerationCoordinator.STATE_SESSION:
                //notifySubscribers(MESSAGE_STATE_SESSION);
                break;
            case LocalKeyGenerationCoordinator.STATE_INVITATION:
               // notifySubscribers(MESSAGE_STATE_INVITATIONS);
                break;
            case LocalKeyGenerationCoordinator.STATE_DISTRIBUTED:
               // notifySubscribers(MESSAGE_STATE_DISTRIBUTED);
                break;
            case LocalKeyGenerationCoordinator.STATE_SHARES:
               // notifySubscribers(MESSAGE_STATE_SHARES);
                break;
            case LocalKeyGenerationCoordinator.STATE_PERSIST:
               // notifySubscribers(MESSAGE_STATE_PERSISTED);
                break;
            case LocalKeyGenerationCoordinator.STATE_ERROR:
                handleClientErrors(oldState);
                break;
            case LocalKeyGenerationCoordinator.STATE_CLOSED:
                break;
            case LocalKeyGenerationCoordinator.STATE_FINISHED:
                logger.logEvent(COMPONENT_NAME, EVENT_KEY_DONE, "low");
                localCoordinator.close();
                view.navigate(R.id.success);
                break;
            default:
                logger.logError(COMPONENT_NAME, "impossible state of the client", "Critical");
                throw new IllegalStateException("Impossible return state");

        }
    }

    private void handleClientErrors(int oldState) {
        if (localCoordinator != null){
            localCoordinator.close();
        }
        switch (oldState)
        {
            case LocalKeyGenerationCoordinator.STATE_INIT:
                logger.logEvent(COMPONENT_NAME, EVENT_NOT_RECEIVED_TOKEN, "low");
                view.navigate(R.id.error_home);
                break;
            case LocalKeyGenerationCoordinator.STATE_START:
                logger.logEvent(COMPONENT_NAME, EVENT_DETAILS_NOT_SUBMITTED, "low");
                setMessage(DistributedKeyGenerationActivity.KEY_ERROR_MESSAGES, "Token revocation");
                view.navigate(R.id.error_home);
                break;
            case LocalKeyGenerationCoordinator.STATE_DETAILS:
                logger.logEvent(COMPONENT_NAME, EVENT_PARTICIPANTS_NOT_SUBMITTED, "high");
                setMessage(DistributedKeyGenerationActivity.KEY_ERROR_MESSAGES, "Token revocation");
                view.navigate(R.id.error_select);
                break;
            case LocalKeyGenerationCoordinator.STATE_BAD_INP_SEL:
                logger.logEvent(COMPONENT_NAME, EVENT_SESSION_FAILED, "high");
                setMessage(DistributedKeyGenerationActivity.KEY_ERROR_MESSAGES, "Token revocation");
                view.navigate(R.id.error_select);
                break;
            case LocalKeyGenerationCoordinator.STATE_SELECT:
            case LocalKeyGenerationCoordinator.STATE_SESSION:
            case LocalKeyGenerationCoordinator.STATE_INVITATION:
            case LocalKeyGenerationCoordinator.STATE_DISTRIBUTED:
            case LocalKeyGenerationCoordinator.STATE_SHARES:
            case LocalKeyGenerationCoordinator.STATE_PERSIST:
                logger.logEvent(COMPONENT_NAME, EVENT_RUN_FAILED, "high", String.valueOf(oldState));
                setMessage(DistributedKeyGenerationActivity.KEY_ERROR_MESSAGES, "Session failed");
                view.navigate(R.id.error_generation);
                break;
            case LocalKeyGenerationCoordinator.STATE_TOKEN_REVOKED:
                logger.logEvent(COMPONENT_NAME, EVENT_RUN_FAILED, "high", "Token revoked");
                break;
            default:
                logger.logError(COMPONENT_NAME, "impossible state of the client", "Critical");
                throw new IllegalStateException("Impossible return state");
        }
    }

    @Override
    public List<Participant> getInitialOptions() {
        return localCoordinator.getOptions();
    }


    @Override
    public void submitLoginDetails() {
            localCoordinator.submitLoginDetails(getMessage(DistributedKeyGenerationActivity.KEY_APPLICATION_NAME));
    }

    @Override
    public void submitLoginDetailsUnsuccessful() {
        logger.logEvent(COMPONENT_NAME, EVENT_UNAVAILABLE_LOGIN,"low");
        view.showTemporally(MESSAGE_BAD_LOGIN);
    }

    @Override
    public void submitSelectedParticipants(List<Participant> participants) {
        localCoordinator.submitSelection(participants);
    }

    @Override
    public void submitThreshold(int threshold) {
        localCoordinator.submitThreshold(threshold);
        logger.logEvent(COMPONENT_NAME, EVENT_THRESHOLD_SUBMITTED, "low", String.valueOf(threshold));
    }

    @Override
    public void successfulSubmission() {
        logger.logEvent(COMPONENT_NAME, "authentication name registered", "low");
        view.navigate(R.id.select);
    }

    @Override
    public void successfulSubmissionOfParameters() {
        logger.logEvent(COMPONENT_NAME, "parameters registered", "low");
        view.navigate(R.id.run);
    }

    private Object leaderLock = new Object();

    @Override
    public void foundLeader() {
        synchronized (leaderLock){
            logger.logEvent(COMPONENT_NAME, "found leader", "low");
            view.navigate(R.id.action_waitingForLeaderFragment_to_leaderFoundFragment);
        }
    }

    @Override
    public void runAsRemote() {
        synchronized (leaderLock){
            logger.logEvent(COMPONENT_NAME, "parameters registered", "low");
            view.navigate(R.id.action_leaderFoundFragment_to_generationFragment);
        }
    }

    @Override
    public void ok() {
        view.navigate(R.id.success);
       // Network.getInstance().closeAllConnections();
    }

}
