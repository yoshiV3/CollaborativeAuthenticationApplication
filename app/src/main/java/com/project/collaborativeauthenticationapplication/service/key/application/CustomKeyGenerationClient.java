package com.project.collaborativeauthenticationapplication.service.key.application;

import android.content.Context;

import com.project.collaborativeauthenticationapplication.logger.AndroidLogger;
import com.project.collaborativeauthenticationapplication.logger.Logger;
import com.project.collaborativeauthenticationapplication.service.IllegalNumberOfTokensException;
import com.project.collaborativeauthenticationapplication.service.IllegalUseOfClosedTokenException;
import com.project.collaborativeauthenticationapplication.service.Participant;
import com.project.collaborativeauthenticationapplication.service.ServiceStateException;
import com.project.collaborativeauthenticationapplication.service.controller.CustomAuthenticationServicePool;
import com.project.collaborativeauthenticationapplication.service.controller.CustomServiceMonitor;
import com.project.collaborativeauthenticationapplication.service.crypto.AndroidSecretStorage;
import com.project.collaborativeauthenticationapplication.service.crypto.SecureStorageException;
import com.project.collaborativeauthenticationapplication.service.key.KeyGenerationPresenter;
import com.project.collaborativeauthenticationapplication.service.key.KeyToken;
import com.project.collaborativeauthenticationapplication.service.key.user.DistributedKeyGenerationActivity;
import com.project.collaborativeauthenticationapplication.service.network.CustomCommunication;
import com.project.collaborativeauthenticationapplication.service.network.UnreachableParticipantException;

import java.util.ArrayList;
import java.util.List;

public class CustomKeyGenerationClient implements keyGenerationClient {




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

    private KeyGenerationPresenter presenter;

    public CustomKeyGenerationClient(KeyGenerationPresenter presenter)
    {
        this.presenter = presenter;
    }

    private CustomKeyGenerationPersistenceManager persistenceManager     = new CustomKeyGenerationPersistenceManager();


    private String[] details = {"", ""};

    private  final int    INDEX_APPLICATION_NAME = 0;
    private  final int    INDEX_LOGIN            = 1;



    private  int state = STATE_INIT;


    private AndroidSecretStorage storage;

    private KeyToken token = null;

    private int threshold =0;


    private ArrayList<Participant> selection;


    @Override
    public void open(Context context) {
        if (state != STATE_INIT)
        {
            throw  new IllegalStateException();
        }
        if (CustomServiceMonitor.getInstance().isServiceEnabled()) {
            try {
                token = CustomAuthenticationServicePool.getInstance().getNewKeyToken();
                state = STATE_START;
                presenter.SignalClientInNewState(state, STATE_CLOSED);
                this.storage =  new AndroidSecretStorage(context);

            } catch (IllegalNumberOfTokensException | ServiceStateException e) {
                presenter.setMessage(DistributedKeyGenerationActivity.KEY_ERROR_MESSAGES, e.getMessage());
                state = STATE_ERROR;
                presenter.SignalClientInNewState(state, STATE_CLOSED);
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
        if (previousState == STATE_PERSIST){
            persistenceManager.removeCredentials(details[INDEX_APPLICATION_NAME], details[INDEX_LOGIN], storage);
        }
        if (token != null)
        {
            token.close();
            token = null;
        }
        storage = null;
        presenter.SignalClientInNewState(state, previousState);
    }

    @Override
    public void submitLoginDetails(String login, String application) {
        if (state != STATE_START)
        {
            throw new IllegalStateException("Presenter should not call this method during this state");
        }
        if (persistenceManager.hasApplicationLoginWithGivenCredentials(application, login)){
           throw new IllegalArgumentException("Login is no longer available for this application");
        }
        if (token != null && !token.isClosed())
        {
            details[INDEX_LOGIN]            = login;
            details[INDEX_APPLICATION_NAME] = application;
            state = STATE_DETAILS;
        }
        else
        {
            state = STATE_ERROR;
        }
        presenter.SignalClientInNewState(state, STATE_START);
    }

    @Override
    public int getState() {
        return  state;
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
            state = STATE_ERROR;
        }
        presenter.SignalClientInNewState(state, STATE_DETAILS);
    }

    @Override
    public void submitThreshold(int threshold) {
        this.threshold = threshold;
    }


    @Override
    public void run() {
        if (state != STATE_SELECT)
        {
            throw new IllegalStateException("Cannot run during this state");
        }
        KeyGenerationSessionGenerator            generator              = new CustomKeyGenerationSessionGenerator();
        KeyGenerationDistributedInvitationSender invitationSender       = new CustomKeyGenerationDistributedInvitationSender();
        CustomLocalKeyPartGenerator              keyPartGenerator       = new CustomLocalKeyPartGenerator();
        CustomKeyPartDistributor                 keyPartDistributor     = new CustomKeyPartDistributor();
        CustomRemoteKeyPartHandler               remoteKeyPartHandler   = new CustomRemoteKeyPartHandler();
        CustomLocalKeyShareGenerator             shareGenerator         = new CustomLocalKeyShareGenerator();
        try {
            generateSession(generator, invitationSender);
            sendInvitations( invitationSender, generator);
            generatePartsForLocalParticipant(invitationSender, keyPartGenerator); //all parts and a part of the public key belonging to local participants
            distributeKeyParts(keyPartGenerator, keyPartDistributor, remoteKeyPartHandler, shareGenerator, persistenceManager);
            generateLocalShares(shareGenerator);
            persist(persistenceManager);
            int previous = state;
            changeState(STATE_FINISHED, previous);
        } catch (IllegalUseOfClosedTokenException | UnreachableParticipantException e) {
            int previousState = state;
            changeState(STATE_ERROR, previousState);
        }
    }

    private void generateSession(KeyGenerationSessionGenerator generator, KeyGenerationDistributedInvitationSender invitationSender) throws IllegalUseOfClosedTokenException {
        generator.generateSession(selection, threshold, details[INDEX_APPLICATION_NAME], details[INDEX_LOGIN], token);
        int previousState = state;
        changeState(STATE_SESSION, previousState);
    }


    private void sendInvitations(KeyGenerationDistributedInvitationSender sender, KeyGenerationSessionGenerator generator) throws IllegalUseOfClosedTokenException, UnreachableParticipantException {
        generator.giveKeyGenerationSessionTo(sender);
        sender.sendInvitations(token);
        int previousState = state;
        changeState(STATE_INVITATION, previousState);
    }

    private void generatePartsForLocalParticipant(KeyGenerationDistributedInvitationSender sender, CustomLocalKeyPartGenerator keyPartGenerator)
            throws IllegalUseOfClosedTokenException {
        sender.passSessionTo(keyPartGenerator);
        keyPartGenerator.generate(token);
        int previousState = state;
        changeState(STATE_KEYPART, previousState);
    }


    private void distributeKeyParts(CustomLocalKeyPartGenerator keyPartGenerator, CustomKeyPartDistributor distributor
            , CustomRemoteKeyPartHandler remoteKeyPartHandler, LocalKeyPartHandler localKeyPartHandler,
              CustomKeyGenerationPersistenceManager persistenceManager) throws IllegalUseOfClosedTokenException {
        keyPartGenerator.passKeyPartDistributionSessionTo(distributor);
        distributor.distribute(localKeyPartHandler, remoteKeyPartHandler, persistenceManager, token);
        int previousState = state;
        changeState(STATE_DISTRIBUTED, previousState);
    }

    private void generateLocalShares(CustomLocalKeyShareGenerator shareGenerator) throws IllegalUseOfClosedTokenException {
        shareGenerator.generate(persistenceManager, token);
        int previousState = state;
        changeState(STATE_SHARES, previousState);
    }


    private void persist(CustomKeyGenerationPersistenceManager persistenceManager) throws IllegalUseOfClosedTokenException {
        int previousState = state;
        try {
            persistenceManager.persist(token, storage);
        } catch (SecureStorageException e) {

            changeState(STATE_ERROR, previousState);
        }
        changeState(STATE_PERSIST, previousState);
    }



    private void changeState(int newState, int previousState)
    {
        state  = newState;
        presenter.SignalClientInNewState(state, previousState);
    }





    private boolean isWellFormedInput(List<Participant> selection) {
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

    @Override
    protected void finalize() throws Throwable {
        if (state != STATE_CLOSED)
        {
            Logger logger = new AndroidLogger();
            logger.logError("CLIENT", "not properly managed states", "CRITICAL");
            close();
        }
        super.finalize();
    }
}
