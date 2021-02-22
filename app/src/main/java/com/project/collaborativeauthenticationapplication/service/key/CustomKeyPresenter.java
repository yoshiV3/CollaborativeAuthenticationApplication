package com.project.collaborativeauthenticationapplication.service.key;



import com.project.collaborativeauthenticationapplication.R;
import com.project.collaborativeauthenticationapplication.logger.AndroidLogger;
import com.project.collaborativeauthenticationapplication.logger.Logger;
import com.project.collaborativeauthenticationapplication.service.IllegalUseOfClosedTokenException;
import com.project.collaborativeauthenticationapplication.service.Participant;
import com.project.collaborativeauthenticationapplication.service.ServiceStateException;
import com.project.collaborativeauthenticationapplication.service.IllegalNumberOfTokensException;
import com.project.collaborativeauthenticationapplication.service.controller.CustomAuthenticationServicePool;
import com.project.collaborativeauthenticationapplication.service.controller.CustomServiceMonitor;
import com.project.collaborativeauthenticationapplication.service.KeyToken;
import com.project.collaborativeauthenticationapplication.service.network.CustomCommunication;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class CustomKeyPresenter implements KeyPresenter{

    private static final String COMPONENT_NAME         = "Key Presenter";

    private static final String EVENT_START                      = "Key generation process started";
    private static final String EVENT_RECEIVED_TOKEN             = "Token received";
    private static final String EVENT_NOT_RECEIVED_TOKEN         =  "Token not received";
    private static final String EVENT_SERVICE_DISABLED           =  "Service disabled";
    private static final String EVENT_DETAILS_SUBMITTED          =  "Details were submitted";
    private static final String EVENT_DETAILS_NOT_SUBMITTED      =  "Details were not submitted (token revoked)";
    private static final String EVENT_PARTICIPANTS_NOT_SUBMITTED =  "Participants not submitted (token revoked)";
    private static final String EVENT_PARTICIPANTS_SUBMITTED     =  "Participants were submitted";


    private Logger logger = new AndroidLogger();

    private static KeyPresenter instance;


    public static KeyPresenter getInstance()
    {
        return instance;
    }


    protected static void newInstance(KeyView view) {

        instance = new CustomKeyPresenter(view);
    }

    private final KeyView view;


    private HashMap<String, String> messages = new HashMap<>();

    private KeyToken token = null;


    private CustomKeyPresenter(KeyView view)
    {
        this.view = view;
    }

    @Override
    public void onStart() {
        logger.logEvent(COMPONENT_NAME, EVENT_START, "low");
        if (CustomServiceMonitor.getInstance().isServiceEnabled())
        {
            try {
                token  = CustomAuthenticationServicePool.getInstance().getNewKeyToken();
                logger.logEvent(COMPONENT_NAME, EVENT_RECEIVED_TOKEN, "low", String.valueOf(token.getIdentifier()));
            } catch (IllegalNumberOfTokensException e) {
                logger.logEvent(COMPONENT_NAME, EVENT_NOT_RECEIVED_TOKEN, "low");
                setMessage(DistributedKeyGenerationActivity.KEY_ERROR_MESSAGES, e.getMessage());
                view.navigate(R.id.error_home);
            } catch (ServiceStateException e) {
                logger.logEvent(COMPONENT_NAME, EVENT_NOT_RECEIVED_TOKEN, "low");
                setMessage(DistributedKeyGenerationActivity.KEY_ERROR_MESSAGES, e.getMessage());
                view.navigate(R.id.error_home);

            }
        }
        else
        {
            logger.logEvent(COMPONENT_NAME, EVENT_SERVICE_DISABLED, "low");
            setMessage(DistributedKeyGenerationActivity.KEY_ERROR_MESSAGES, "Disabled");
            view.navigate(R.id.error_home);
        }
    }

    @Override
    public void close() {
        if (token != null)
        {
            token.close();
            token = null;
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
    public void setMessage(String key, String message) {
        messages.put(key, message);
    }

    @Override
    public String getMessage(String key) {
        return messages.getOrDefault(key, "");
    }

    @Override
    public ArrayList<Participant> getInitialOptions() {
        return CustomCommunication.getInstance().getReachableParticipants();
    }


    @Override
    public void submitLoginDetails() {
        if (isTokenValid())
        {
            view.navigate(R.id.select);
            logger.logEvent(COMPONENT_NAME, EVENT_DETAILS_SUBMITTED, "low");
            view.showMetaData(getMessage(DistributedKeyGenerationActivity.KEY_LOGIN), getMessage(DistributedKeyGenerationActivity.KEY_APPLICATION_NAME));
        }
        else
        {
            logger.logEvent(COMPONENT_NAME, EVENT_DETAILS_NOT_SUBMITTED, "low");
            setMessage(DistributedKeyGenerationActivity.KEY_ERROR_MESSAGES, "Token revocation");
            view.navigate(R.id.error_home);
        }

    }

    @Override
    public void submitSelectedParticipants(List<Participant> participants) {
        if (isTokenValid())
        {
            try {
                KeyGenerationSessionGenerator generator = CustomKeyGenerationSessionGenerator.getInstance();
                generator.generateSession(participants, token);
                generator.giveKeyGenerationSessionTo(CustomKeyGenerationConnector.getInstance());
                logger.logEvent(COMPONENT_NAME, EVENT_PARTICIPANTS_SUBMITTED, "low");
                view.navigate(R.id.run);
            } catch (IllegalUseOfClosedTokenException e) {
                logger.logEvent(COMPONENT_NAME, EVENT_PARTICIPANTS_NOT_SUBMITTED, "high");
                setMessage(DistributedKeyGenerationActivity.KEY_ERROR_MESSAGES, "Token revocation");
                view.navigate(R.id.error_select);
            }
        }
        else
        {
            logger.logEvent(COMPONENT_NAME, EVENT_PARTICIPANTS_NOT_SUBMITTED, "low");
            setMessage(DistributedKeyGenerationActivity.KEY_ERROR_MESSAGES, "Token revocation");
            view.navigate(R.id.error_select);
        }
    }


    private boolean isTokenValid()
    {
        if (token == null)
        {
            return  false;
        }
        return  !token.isClosed();

    }

}
