package com.project.collaborativeauthenticationapplication.service.controller;

import com.project.collaborativeauthenticationapplication.logger.AndroidLogger;
import com.project.collaborativeauthenticationapplication.logger.Logger;
import com.project.collaborativeauthenticationapplication.service.general.FeedbackRequester;
import com.project.collaborativeauthenticationapplication.service.key.KeyToken;
import com.project.collaborativeauthenticationapplication.service.general.ServiceStateException;
import com.project.collaborativeauthenticationapplication.service.general.IllegalNumberOfTokensException;
import com.project.collaborativeauthenticationapplication.service.general.SignatureToken;
import com.project.collaborativeauthenticationapplication.service.network.Communication;
import com.project.collaborativeauthenticationapplication.service.network.CustomCommunication;

public class CustomAuthenticationServiceController extends StartStopAuthenticationServiceController {


    private static final CustomAuthenticationServiceController instance = new CustomAuthenticationServiceController();


    private static final String COMPONENT_NAME         = "Service Pool";


    private static final String EVENT_CREATE_TOKEN_WRONG_STATE  = "New token request during a state other than the active state";

    private static final String PRIORITY_CRITICAL       = "Highly critical: subtle bug";
    private static final String PRIORITY_NORMAL         = "normal";


    public static CustomAuthenticationServiceController getInstance()
    {
        return instance;
    }


    Communication communication = CustomCommunication.getInstance();

    private boolean hasBeenEnabled = false;

    private static Logger logger = new AndroidLogger();

    private CustomAuthenticationServiceController(){}

    @Override
    protected void start() {
        CustomServiceMonitor.getInstance().serviceActive();
        communication.openServiceServer(new FeedbackRequester() {
            private boolean result;
            @Override
            public void setResult(boolean result) {
                this.result = result;
            }

            @Override
            public void signalJobDone() {
                if (!result){
                    stop();
                } else{
                    communication.handleIncomingRequests();
                }
            }
        });
        this.hasBeenEnabled = true;
    }

    @Override
    protected void stop() {
        CustomToken.closeAll();
        communication.closeServiceServer();
        CustomServiceMonitor.getInstance().serviceDisabled();
        this.hasBeenEnabled = false;
    }

    @Override
    protected void sleep() {
        CustomToken.closeAll();
        communication.closeServiceServer();
        CustomServiceMonitor.getInstance().serviceSleeps();
        this.hasBeenEnabled = true;
    }

    @Override
    public boolean isActive() {
        return hasBeenEnabled && communication.isBluetoothEnabled();
    }

    @Override
    public boolean isEnabled() {
        return hasBeenEnabled;
    }

    @Override
    public boolean hasFreeKeyGenerationToken() {
        return (CustomToken.getNumberOfActiveTokens() != 0) && isActive();
    }

    @Override
    public boolean hasFreeSignatureTokens() {
        return false;
    }

    @Override
    public synchronized KeyToken getNewKeyToken() throws IllegalNumberOfTokensException, ServiceStateException {
        if (! isActive())
        {
            String currentState;
            if (isEnabled())
            {
                currentState = "INACTIVE";
            }
            else
            {
                currentState = "DISABLED";
            }
            logger.logEvent(COMPONENT_NAME, EVENT_CREATE_TOKEN_WRONG_STATE, PRIORITY_NORMAL, currentState);
            throw  new ServiceStateException(currentState, "Active");
        }
        return new CustomKeyToken();
    }

    @Override
    public synchronized SignatureToken getNewSignatureToken() throws ServiceStateException, IllegalNumberOfTokensException {
        if (! isActive())
        {
            String currentState;
            if (isEnabled())
            {
                currentState = "INACTIVE";
            }
            else
            {
                currentState = "DISABLED";
            }
            logger.logEvent(COMPONENT_NAME, EVENT_CREATE_TOKEN_WRONG_STATE, PRIORITY_NORMAL, currentState);
            throw  new ServiceStateException(currentState, "Active");
        }
        return new CustomSignatureToken();
    }


    private static class CustomSignatureToken extends CustomToken implements SignatureToken {
        private static final String COMPONENT_NAME_INNER   = COMPONENT_NAME + ":signatureToken";

        private static final String ERROR_CLOSE                     = "Token not closed before finalized, potential  resource leakage";
        private static final String ERROR_CREATE                    = "Too many active tokens";
        private static final String EVENT_CREATE                    = "New Key token";

        private static final int number_of_concurrent_signatures    = 1;

        public CustomSignatureToken() throws IllegalNumberOfTokensException {
            super();
            if (getNumberOfActiveTokens() > number_of_concurrent_signatures)
            {
                logger.logError(COMPONENT_NAME_INNER, ERROR_CREATE, PRIORITY_NORMAL);
                close();
                throw new IllegalNumberOfTokensException("Too many active tokens");
            }
            logger.logEvent(COMPONENT_NAME_INNER, EVENT_CREATE, PRIORITY_NORMAL, String.valueOf(getIdentifier()));
        }


        @Override
        protected void finalize() throws Throwable {
            if (!isClosed())
            {
                logger.logError(COMPONENT_NAME_INNER, ERROR_CLOSE, PRIORITY_CRITICAL, String.valueOf(getIdentifier()));
                close();
            }
            super.finalize();
        }
    }



    private static class CustomKeyToken extends CustomToken implements KeyToken
    {
        private static final String COMPONENT_NAME_INNER   = COMPONENT_NAME + ":KeyToken";

        private static final String ERROR_CLOSE             = "Token not closed before finalized, potential  resource leakage";
        private static final String ERROR_CREATE            = "Too many active tokens";
        private static final String EVENT_CREATE            = "New Key token";

        public CustomKeyToken() throws IllegalNumberOfTokensException {
            super();
            if (getNumberOfActiveTokens() > 1)
            {
                logger.logError(COMPONENT_NAME_INNER, ERROR_CREATE, PRIORITY_NORMAL);
                close();
                throw new IllegalNumberOfTokensException("Too many active tokens");
            }
            logger.logEvent(COMPONENT_NAME_INNER, EVENT_CREATE, PRIORITY_NORMAL, String.valueOf(getIdentifier()));
        }


        @Override
        protected void finalize() throws Throwable {
            if (!isClosed())
            {
                logger.logError(COMPONENT_NAME_INNER, ERROR_CLOSE, PRIORITY_CRITICAL, String.valueOf(getIdentifier()));
                close();
            }
            super.finalize();
        }
    }
}
