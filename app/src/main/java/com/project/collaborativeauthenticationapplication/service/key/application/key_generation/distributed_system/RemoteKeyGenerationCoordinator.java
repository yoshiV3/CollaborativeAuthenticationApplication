package com.project.collaborativeauthenticationapplication.service.key.application.key_generation.distributed_system;

import android.content.Context;

import com.project.collaborativeauthenticationapplication.alternative.key.KeyGenerationPresenter;
import com.project.collaborativeauthenticationapplication.alternative.key.application.GuardLeader;
import com.project.collaborativeauthenticationapplication.alternative.network.Network;
import com.project.collaborativeauthenticationapplication.logger.AndroidLogger;
import com.project.collaborativeauthenticationapplication.logger.Logger;
import com.project.collaborativeauthenticationapplication.service.crypto.BigNumber;
import com.project.collaborativeauthenticationapplication.service.crypto.Point;
import com.project.collaborativeauthenticationapplication.service.general.FeedbackRequester;
import com.project.collaborativeauthenticationapplication.service.general.IdentifiedParticipant;
import com.project.collaborativeauthenticationapplication.service.general.IllegalUseOfClosedTokenException;
import com.project.collaborativeauthenticationapplication.service.general.Participant;

import com.project.collaborativeauthenticationapplication.service.key.application.key_generation.KeyGenerationClient;


import java.util.ArrayList;
import java.util.List;

public class RemoteKeyGenerationCoordinator extends LocalKeyGenerationCoordinator{


    private static final String COMPONENT = "Remote Coordinator";


    private RemoteCoordinatorStub stub;


    private static Logger logger = new AndroidLogger();




    private GuardLeader guardLeader;

    public RemoteKeyGenerationCoordinator(KeyGenerationPresenter presenter) {
        super(presenter);
    }


    public void  addMainClient(KeyGenerationClient client){
        getClients().put(new Integer(1), client);
    }

    public void addCoordinatorStub(String stub){
            logger.logEvent(COMPONENT, "added a remote coordinator stub", "normal");
            this.stub = new RemoteCoordinatorStub(stub, this);
    }

    @Override
    public void done() {
        basisClose();
        closeToken();
    }

    @Override
    public synchronized void persisted() {
        basisPersisted();
        stub.voteYes();
    }

    @Override
    public void open(Context context) {
        super.open(context);
        logger.logEvent(COMPONENT, "new open request: create listener", "low");
        guardLeader = new GuardLeader(this);
        guardLeader.start();
        /**
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
         **/


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
        //Network.getInstance().closeAllConnections();
        closeResourcesEarly();
    }


    @Override
    protected int getLocalIdentifier() {
        return getSession().getLocalParticipant().getIdentifier();
    }

    @Override
    public void submitLoginDetails( String application) {
        logger.logEvent(COMPONENT, "new details submitted", "low");
        if (getState() == STATE_START && getToken() != null && !getToken().isClosed())
        {
            setApplicationName(application);
            setState(STATE_DETAILS);
            getPresenter().foundLeader();
        }
        else
        {
            setState(STATE_TOKEN_REVOKED);
            logger.logEvent(COMPONENT, "Problem has occurred before the submission of the credential details", "low");
            error();
        }
    }

    private void error() {
        getPresenter().error();
        Network.getInstance().closeAllConnections();
    }


    @Override
    public void abort() {
        basisAbort();
    }

    @Override
    public void submitSelection(List<Participant> selection) {
        logger.logEvent(COMPONENT, "received new participants", "low", String.valueOf(selection.size()));
        if (getState() == STATE_DETAILS && getToken() != null && !getToken().isClosed())
        {
            if (isWellFormedInput(selection))
            {
               commitToSelection(selection);
               setState(STATE_SELECT);
               getPresenter().runAsRemote();
            }
            else
            {
                setState(STATE_ERROR);
                error();
            }
        }
        else
        {
            setState(STATE_TOKEN_REVOKED);
            logger.logEvent(COMPONENT, "Problem has occurred before the submission of the selection", "low");
            error();

        }
    }


    @Override
    public void run() {
        logger.logEvent(COMPONENT, "run called during state", "low", String.valueOf(getState()));
        if (getState() == STATE_SELECT){
            //presenter.onReceivedNewInvitation();
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
                            error();
                        }
                    } else {
                        logger.logEvent(COMPONENT, "participant aborted the computation", "low");
                        error();
                    }
                }
            };
            getPersistenceClient().checkCredentials(requester, getApplicationName());
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
