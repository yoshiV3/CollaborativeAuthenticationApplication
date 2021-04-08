package com.project.collaborativeauthenticationapplication.service.key.application.key_generation.distributed_system;

import android.content.Context;

import com.project.collaborativeauthenticationapplication.logger.AndroidLogger;
import com.project.collaborativeauthenticationapplication.logger.Logger;
import com.project.collaborativeauthenticationapplication.service.IllegalNumberOfTokensException;
import com.project.collaborativeauthenticationapplication.service.IllegalUseOfClosedTokenException;
import com.project.collaborativeauthenticationapplication.service.Participant;
import com.project.collaborativeauthenticationapplication.service.Requester;
import com.project.collaborativeauthenticationapplication.service.ServiceStateException;
import com.project.collaborativeauthenticationapplication.service.controller.CustomAuthenticationServicePool;
import com.project.collaborativeauthenticationapplication.service.controller.CustomServiceMonitor;
import com.project.collaborativeauthenticationapplication.service.crypto.BigNumber;
import com.project.collaborativeauthenticationapplication.service.crypto.Point;
import com.project.collaborativeauthenticationapplication.service.key.KeyGenerationPresenter;
import com.project.collaborativeauthenticationapplication.service.key.KeyToken;
import com.project.collaborativeauthenticationapplication.service.key.application.CustomTokenConsumer;
import com.project.collaborativeauthenticationapplication.service.key.application.key_generation.local_system.FeedbackRequester;
import com.project.collaborativeauthenticationapplication.service.key.application.key_generation.local_system.control.CustomKeyGenerationClient;
import com.project.collaborativeauthenticationapplication.service.key.application.key_generation.local_system.control.CustomKeyGenerationSessionGenerator;
import com.project.collaborativeauthenticationapplication.service.key.application.key_generation.local_system.control.KeyGenerationClient;
import com.project.collaborativeauthenticationapplication.service.key.application.key_generation.local_system.control.KeyGenerationSession;
import com.project.collaborativeauthenticationapplication.service.key.application.key_generation.local_system.control.KeyGenerationSessionGenerator;
import com.project.collaborativeauthenticationapplication.service.key.application.key_generation.local_system.control.ThreadedKeyGenerationClient;
import com.project.collaborativeauthenticationapplication.service.key.user.key_generation.DistributedKeyGenerationActivity;
import com.project.collaborativeauthenticationapplication.service.network.CustomCommunication;

import java.util.ArrayList;
import java.util.List;

public class CustomLocalKeyGenerationCoordinator extends CustomTokenConsumer implements KeyGenerationCoordinator {


    private static final String COMPONENT = "Local Coordinator";


    private static Logger logger = new AndroidLogger();


    public static final int STATE_INIT          = 0;
    public static final int STATE_CLOSED        = 1;
    public static final int STATE_START         = 2;
    public static final int STATE_DETAILS       = 3;
    public static final int STATE_SELECT        = 4;
    public static final int STATE_FINISHED      = 5;
    public static final int STATE_ERROR         = 6;
    public static final int STATE_BAD_INP_SEL   = 7;
    public static final int STATE_SESSION       = 8;
    public static final int STATE_INVITATION    = 9 ;
    public static final int STATE_KEYPART       = 10;
    public static final int STATE_DISTRIBUTED   = 11;
    public static final int STATE_SHARES        = 12;
    public static final int STATE_PERSIST       = 13;
    public static final int STATE_TOKEN_REVOKED = 14;

    private KeyGenerationPresenter presenter;


    private  final int    INDEX_APPLICATION_NAME = 0;
    private  final int    INDEX_LOGIN            = 1;

    private String[] details = {"", ""};
    private int threshold =0;
    private ArrayList<Participant> selection;



    private  int state = STATE_INIT;

    private KeyToken token = null;

    private KeyGenerationClient                   client;
    private KeyGenerationSessionGenerator         sessionGenerator       = new CustomKeyGenerationSessionGenerator();
    private KeyGenerationSession                  session;
    ArrayList<BigNumber>                          parts;
    Point                                         publicKey;


    public CustomLocalKeyGenerationCoordinator(KeyGenerationPresenter presenter)
    {
        this.presenter = presenter;
    }


    @Override
    public void open(Context context) {
        logger.logEvent(COMPONENT, "new open request", "low");
        if (state != STATE_INIT)
        {
            throw  new IllegalStateException();
        }
        if (client != null && client.getState() != CustomKeyGenerationClient.STATE_INIT){
            throw  new IllegalStateException();
        }
        if (client == null){
            client = new ThreadedKeyGenerationClient(this);
        }
        client.open(context);
        if (CustomServiceMonitor.getInstance().isServiceEnabled()) {
            try {
                token = CustomAuthenticationServicePool.getInstance().getNewKeyToken();
                state = STATE_START;

            } catch ( ServiceStateException e) {
                presenter.setMessage(DistributedKeyGenerationActivity.KEY_ERROR_MESSAGES, e.getMessage());
                state = STATE_ERROR;
                client.close();
                client = null;
            } catch (IllegalNumberOfTokensException e){
                state = STATE_TOKEN_REVOKED;
                client.close();
                client = null;
            }finally {
                presenter.SignalCoordinatorInNewState(state, STATE_INIT);
            }
        }
        else
        {
            presenter.setMessage(DistributedKeyGenerationActivity.KEY_ERROR_MESSAGES, "Disabled");

        }
    }

    @Override
    public void close() {
        int previousState = state;
        state             = STATE_CLOSED;
        if (client != null){
            client.close();
            client = null;
        }
        if (token != null)
        {
            token.close();
            token = null;
        }
        presenter.SignalCoordinatorInNewState(state, previousState);
    }

    @Override
    public void submitLoginDetails(String login, String application) {
        logger.logEvent("Coordinator", "new details submitted", "low");
        if (state != STATE_START)
        {
            throw new IllegalStateException("Presenter should not call this method during this state");
        }
        if (token != null && !token.isClosed())
        {
            details[INDEX_LOGIN]            = login;
            details[INDEX_APPLICATION_NAME] = application;
            FeedbackRequester requester = new FeedbackRequester() {
                private boolean result;
                @Override
                public void setResult(boolean result) {
                    this.result = result;
                }

                @Override
                public void signalJobDone() {
                    logger.logEvent("Coordinator", "received results for credential check", "low", String.valueOf(result));
                    if (result){
                        state = STATE_DETAILS;
                        presenter.SignalCoordinatorInNewState(state, STATE_START);
                    } else {
                        presenter.submitLoginDetailsUnsuccessful();
                    }
                }
            };
            client.checkCredentials(requester, application, login);
        }
        else
        {
            state = STATE_TOKEN_REVOKED;
            presenter.SignalCoordinatorInNewState(state, STATE_START);
            logger.logEvent("Coordinator", "No active token", "low");
        }
    }


    @Override
    public List<Participant> getOptions() {
        return CustomCommunication.getInstance().getReachableParticipants();
    }

    @Override
    public void submitSelection(List<Participant> selection) {
        if (!(state == STATE_DETAILS || state == STATE_BAD_INP_SEL))
        {
            throw new IllegalStateException("Presenter should not call this method during this state");
        }
        if (token != null && !token.isClosed())
        {
            if (isWellFormedInput(selection))
            {
                this.selection =  new ArrayList<>(selection);
                state = STATE_SELECT;
            }
            else
            {
                state = STATE_BAD_INP_SEL;
            }
        }
        else
        {
            state = STATE_TOKEN_REVOKED;
        }
        presenter.SignalCoordinatorInNewState(state, STATE_DETAILS);
    }

    @Override
    public void submitThreshold(int threshold) {
        this.threshold = threshold;
    }




    private void transferSessionToClient(){
        Requester requester = new Requester() {
            @Override
            public void signalJobDone() {
                int previousState = state;
                changeState(STATE_SESSION, previousState);
            }
        };
        client.receiveKeyGenerationSession(requester, session);
        inviteParticipants();
    }


    private void inviteParticipants(){
        int previousState = state;
        changeState(STATE_INVITATION, previousState);
        generateKeyParts();
    }


    private void generateKeyParts(){
        int previousState = state;
        try{
            consumeToken(token);
            parts      = new ArrayList<>(); //shares for the local participants
            publicKey  = new Point(BigNumber.getZero(), BigNumber.getZero(), true);
            Requester requester = new Requester() {
                @Override
                public void signalJobDone() {
                    logger.logEvent("Coordinator", "received parts from the client", "low", String.valueOf(parts.size()));
                    changeState(STATE_KEYPART, previousState);
                    distribute();
                }
            };
            client.calculatePartsAndPublicKey(requester, parts, publicKey);
        } catch (IllegalUseOfClosedTokenException e){
                    state = STATE_TOKEN_REVOKED;
        } finally {
            changeState(state, previousState);
        }
    }

    private void distribute(){
        int  previousState = state;
        changeState(STATE_DISTRIBUTED, previousState);
        calculateShares();
    }

    private void calculateShares(){
        int previousState = state;
        try {
            consumeToken(token);
            //Calculate the shares from the received and calculated parts
            ArrayList<List<BigNumber> >partsForShares = new ArrayList<>();
            for (BigNumber secret : parts){
                ArrayList<BigNumber> p = new ArrayList<>();
                p.add(secret);
                partsForShares.add(p);
            }
            logger.logEvent("Coordinator", "Requesting a number of shares with parts", "low", String.valueOf(parts.size()));
            logger.logEvent("Coordinator", "Requesting a number of shares ", "low", String.valueOf(partsForShares.size()));
            Requester requester = new Requester() {
                @Override
                public void signalJobDone() {
                    changeState(STATE_SHARES, previousState);
                    addPublicKey();
                }
            };
            client.calculateShares(requester, partsForShares);
        } catch (IllegalUseOfClosedTokenException e){
            state = STATE_TOKEN_REVOKED;
        } finally {
            changeState(state, previousState);
        }
    }

    private void addPublicKey(){
        Requester requester  = new Requester() {
            @Override
            public void signalJobDone() {
                persist();
            }
        };
        client.receiveFinalPublicKey(requester, publicKey);
    }


    private void persist(){
        int previousState = state;
        try {
            consumeToken(token);
            FeedbackRequester requester = new FeedbackRequester() {
                private boolean result;
                @Override
                public void setResult(boolean result) {
                    this.result = result;
                }

                @Override
                public void signalJobDone() {
                    int previousState = state;
                    if(!result){
                        state = STATE_ERROR;
                        logger.logError("coordinator", "Error occurred during run", "Critical");

                    } else{
                        changeState(STATE_PERSIST, previousState);
                    }
                    presenter.SignalCoordinatorInNewState(state, previousState);
                    finish();
                }
            };
            client.persist(requester);
        } catch (IllegalUseOfClosedTokenException e){
            state = STATE_TOKEN_REVOKED;
        } finally {
            changeState(state, previousState);
        }
    }

    private void finish(){
        changeState(STATE_FINISHED, state);
    }
    @Override
    public void run() {
        logger.logEvent(COMPONENT, "request to run", "low");
        int previousState = state;

        if (state != STATE_SELECT)
        {
            throw new IllegalStateException("Cannot run during this state");
        }
        try{
            consumeToken(token);
            session = sessionGenerator.generateSession(selection, threshold, details[INDEX_APPLICATION_NAME], details[INDEX_LOGIN]); // generate session


            transferSessionToClient();

        } catch (IllegalUseOfClosedTokenException e){
                state = STATE_TOKEN_REVOKED;
        } finally {
            changeState(state, previousState);
        }
    }

    @Override
    public int getState() {
        return  state;
    }


    @Override
    protected void finalize() throws Throwable {
        if (state != STATE_CLOSED)
        {
            Logger logger = new AndroidLogger();
            logger.logError("Local coordinator", "not properly managed states", "CRITICAL");
            close();
        }
        super.finalize();
    }



    private boolean isWellFormedInput(List<Participant> selection) { //threshold is always ok due to the options given to in the UI
        boolean inputCorrect;
        inputCorrect= selection.size()>=1;

        int totalWeight = 0;
        for (Participant participant : selection)
        {
            totalWeight = totalWeight + participant.getWeight();
        }

        inputCorrect = inputCorrect && (totalWeight>=2);
        return  inputCorrect;
    }

    private void changeState(int newState, int previousState)
    {
        state  = newState;
        presenter.SignalCoordinatorInNewState(state, previousState);
    }
}
