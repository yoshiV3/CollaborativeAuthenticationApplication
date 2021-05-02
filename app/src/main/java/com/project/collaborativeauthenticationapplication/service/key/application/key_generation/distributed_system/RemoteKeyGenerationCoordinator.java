package com.project.collaborativeauthenticationapplication.service.key.application.key_generation.distributed_system;

import android.content.Context;

import com.project.collaborativeauthenticationapplication.logger.AndroidLogger;
import com.project.collaborativeauthenticationapplication.logger.Logger;
import com.project.collaborativeauthenticationapplication.service.controller.AuthenticationPresenter;
import com.project.collaborativeauthenticationapplication.service.controller.CustomServiceMonitor;
import com.project.collaborativeauthenticationapplication.service.crypto.BigNumber;
import com.project.collaborativeauthenticationapplication.service.crypto.Point;
import com.project.collaborativeauthenticationapplication.service.general.FeedbackRequester;
import com.project.collaborativeauthenticationapplication.service.general.IdentifiedParticipant;
import com.project.collaborativeauthenticationapplication.service.general.IllegalNumberOfTokensException;
import com.project.collaborativeauthenticationapplication.service.general.IllegalUseOfClosedTokenException;
import com.project.collaborativeauthenticationapplication.service.general.Participant;
import com.project.collaborativeauthenticationapplication.service.general.ServiceStateException;

import com.project.collaborativeauthenticationapplication.service.key.application.key_generation.KeyGenerationClient;
import com.project.collaborativeauthenticationapplication.service.key.application.key_generation.local_system.control.protocol.LocalLogicalKeyGenerationClient;


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class RemoteKeyGenerationCoordinator extends LocalKeyGenerationCoordinator{


    private static final String COMPONENT = "Remote Coordinator";


    private RemoteCoordinatorStub stub;


    private static Logger logger = new AndroidLogger();


    private AuthenticationPresenter presenter;


    public RemoteKeyGenerationCoordinator(AuthenticationPresenter presenter) {
        super(null);
        this.presenter = presenter;
    }


    public void  addMainClient(KeyGenerationClient client){
        getClients().put(new Integer(1), client);
    }

    public void addCoordinatorStub(String stub){
        try {
            logger.logEvent(COMPONENT, "added a remote coordinator stub", "normal");
            this.stub = new RemoteCoordinatorStub(stub, this);
        } catch (IOException e) {
            abort();
            e.printStackTrace();
        }
    }

    @Override
    public void done() {
        basisClose();
        closeToken();
    }

    @Override
    public synchronized void persisted() {
        basisPeristed();
        stub.voteYes();
    }

    @Override
    public void open(Context context) {
        logger.logEvent(COMPONENT, "new open request", "low");
        if (getState() != STATE_INIT)
        {
            throw  new IllegalStateException();
        }
        if (getClients() != null && getClients().size() != 0 && getClients().get(0) != null && getClients().get(0).getState() != LocalLogicalKeyGenerationClient.STATE_INIT){
            throw  new IllegalStateException();
        }
        if (getClients() == null){
            initializeClients();
        }
        buildPersistenceClient(context);
        if (CustomServiceMonitor.getInstance().isServiceEnabled()) {
            try {
                retrieveToken();

            } catch ( ServiceStateException e) {
                handleServiceError();
            } catch (IllegalNumberOfTokensException e){
                handleTokenError();
            }
        }
    }

    @Override
    protected void changeState(int newState, int previousState) {
        setState(newState);
    }

    @Override
    public void submitShares(ArrayList<BigNumber> shares, Point publicKey) {
        persist(shares,  publicKey);
    }

    @Override
    public void close() {
        closeResourcesEarly();
    }


    @Override
    protected int getLocalIdentifier() {
        return getSession().getLocalParticipant().getIdentifier();
    }

    @Override
    public void submitLoginDetails(String login, String application) {
        logger.logEvent(COMPONENT, "new details submitted", "low");
        if (getState() == STATE_START && getToken() != null && !getToken().isClosed())
        {
            setLogin(login);
            setApplicationName(application);
            setState(STATE_DETAILS);
        }
        else
        {
            setState(STATE_TOKEN_REVOKED);
            logger.logEvent(COMPONENT, "Problem has occurred before the submission of the credential details", "low");
        }
    }


    @Override
    public void abort() {
        basisAbort();
    }

    @Override
    public void submitSelection(List<Participant> selection) {
        if (getState() == STATE_DETAILS && getToken() != null && !getToken().isClosed())
        {
            if (isWellFormedInput(selection))
            {
               commitToSelection(selection);
               setState(STATE_SELECT);
            }
            else
            {
                setState(STATE_ERROR);
            }
        }
        else
        {
            setState(STATE_TOKEN_REVOKED);
            logger.logEvent(COMPONENT, "Problem has occurred before the submission of the selection", "low");
        }
    }


    @Override
    public void run() {
        logger.logEvent(COMPONENT, "run called during state", "low", String.valueOf(getState()));
        if (getState() == STATE_SELECT){
            presenter.onReceivedNewInvitation();
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
                        try{
                            consumeToken(getToken());
                            generateSession();; // generate session
                            connectWithOtherClients();
                        } catch (IllegalUseOfClosedTokenException e){
                            abort();
                        }
                    } else {
                        logger.logEvent(COMPONENT, "participant aborted the computation", "low");
                        abort();
                    }
                }
            };
            getPersistenceClient().checkCredentials(requester, getApplicationName(), getLogin());
        } else {
            abort();
        }
    }

    @Override
    protected KeyGenerationClient buildRemoteClient(IdentifiedParticipant participant) {
        return new RemoteLogicalKeyGenerationClientForRemoteCoordinator(participant, getSession());
    }

    private void connectWithOtherClients() {
        buildAllClients();
    }
}
