package com.project.collaborativeauthenticationapplication.service.key.application.key_generation.distributed_system;

import android.content.Context;

import com.project.collaborativeauthenticationapplication.alternative.network.Network;
import com.project.collaborativeauthenticationapplication.logger.AndroidLogger;
import com.project.collaborativeauthenticationapplication.logger.Logger;
import com.project.collaborativeauthenticationapplication.service.concurrency.ThreadPoolSupplier;
import com.project.collaborativeauthenticationapplication.service.controller.CustomAuthenticationServiceController;
import com.project.collaborativeauthenticationapplication.service.controller.CustomServiceMonitor;
import com.project.collaborativeauthenticationapplication.service.crypto.BigNumber;
import com.project.collaborativeauthenticationapplication.service.crypto.Point;
import com.project.collaborativeauthenticationapplication.service.general.FeedbackRequester;
import com.project.collaborativeauthenticationapplication.service.general.IdentifiedParticipant;
import com.project.collaborativeauthenticationapplication.service.general.IllegalNumberOfTokensException;
import com.project.collaborativeauthenticationapplication.service.general.IllegalUseOfClosedTokenException;
import com.project.collaborativeauthenticationapplication.service.general.Participant;
import com.project.collaborativeauthenticationapplication.service.general.ServiceStateException;
import com.project.collaborativeauthenticationapplication.alternative.key.KeyGenerationPresenter;
import com.project.collaborativeauthenticationapplication.alternative.key.KeyToken;
import com.project.collaborativeauthenticationapplication.service.key.application.CustomTokenConsumer;
import com.project.collaborativeauthenticationapplication.service.key.application.key_generation.KeyGenerationClient;
import com.project.collaborativeauthenticationapplication.service.key.application.key_generation.ThreadedKeyGenerationClient;
import com.project.collaborativeauthenticationapplication.service.key.application.key_generation.local_system.control.protocol.CustomKeyGenerationSessionGenerator;
import com.project.collaborativeauthenticationapplication.service.key.application.key_generation.local_system.control.protocol.KeyGenerationSession;
import com.project.collaborativeauthenticationapplication.service.key.application.key_generation.local_system.control.persistance.KeyGenerationPersistenceClient;
import com.project.collaborativeauthenticationapplication.service.key.application.key_generation.local_system.control.persistance.ThreadedKeyGenerationPersistenceClient;
import com.project.collaborativeauthenticationapplication.service.key.application.key_generation.local_system.control.protocol.KeyGenerationSessionGenerator;
import com.project.collaborativeauthenticationapplication.service.key.application.key_generation.local_system.control.protocol.LocalLogicalKeyGenerationClient;
import com.project.collaborativeauthenticationapplication.alternative.key.user.DistributedKeyGenerationActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class LocalKeyGenerationCoordinator extends CustomTokenConsumer implements KeyGenerationCoordinator {



    private static final String COMPONENT = "Local Coordinator";

    private static Logger logger = new AndroidLogger();


    public static final int STATE_TOKEN_REVOKED = -1;
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
    public static final int STATE_DISTRIBUTED   = 11;
    public static final int STATE_SHARES        = 12;
    public static final int STATE_PERSIST       = 13;


    private KeyGenerationPresenter presenter;


    private  final int    INDEX_APPLICATION_NAME = 0;
    private  final int    INDEX_LOGIN            = 1;

    private String[] details = {"", ""};
    private int threshold =0;
    private ArrayList<Participant> selection;


    private  int state = STATE_INIT;

    private KeyToken token = null;

    private  HashMap<Integer, KeyGenerationClient> clients = new HashMap<>();

    KeyGenerationPersistenceClient persistenceClient;
            ;
    private KeyGenerationSessionGenerator sessionGenerator       = new CustomKeyGenerationSessionGenerator();
    private KeyGenerationSession session;





    private int numberOfVotes =    0;
    private boolean result        = true;


    private boolean allClientsBuild   = false;
    private int requiredNumberOfVotes;


    protected int getLocalIdentifier(){
        return 1;
    }

    public KeyGenerationSession getSession() {
        return session;
    }


    public LocalKeyGenerationCoordinator(KeyGenerationPresenter presenter)
    {
        this.presenter = presenter;
    }


    protected KeyGenerationPresenter getPresenter() {
        return presenter;
    }

    protected KeyGenerationPersistenceClient getPersistenceClient() {
        return persistenceClient;
    }

    protected HashMap<Integer, KeyGenerationClient> getClients() {
        return clients;
    }

    @Override
    public int getState() {
        return state;
    }

    protected void setState(int state) {
        this.state = state;
    }

    protected KeyToken getToken() {
        return token;
    }

    @Override
    public void open(Context context) {


        logger.logEvent(COMPONENT, "new open request", "low");
        if (state != STATE_INIT)
        {
            throw  new IllegalStateException();
        }
        if (clients != null && clients.size() != 0 && clients.get(0) != null && clients.get(0).getState() != LocalLogicalKeyGenerationClient.STATE_INIT){
            throw  new IllegalStateException();
        }
        if (clients == null){
            initializeClients();
        }
        buildPersistenceClient(context);
        if (CustomServiceMonitor.getInstance().isServiceEnabled()) {
            try {
                retrieveToken();
            } catch ( ServiceStateException e) {
                presenter.setMessage(DistributedKeyGenerationActivity.KEY_ERROR_MESSAGES, e.getMessage());
                handleServiceError();
            } catch (IllegalNumberOfTokensException e){
                handleTokenError();
            }
        }
        else
        {
            throw new IllegalStateException();
        }
    }

    protected void handleTokenError() {
        state = STATE_ERROR;
        persistenceClient = null;
    }

    protected void handleServiceError() {
        state = STATE_ERROR;
        persistenceClient = null;
    }

    protected void retrieveToken() throws IllegalNumberOfTokensException, ServiceStateException {
        token = CustomAuthenticationServiceController.getInstance().getNewKeyToken();
        state = STATE_START;
    }

    protected void initializeClients() {
        clients = new HashMap<>();
    }

    protected void buildPersistenceClient(Context context) {
        persistenceClient = new ThreadedKeyGenerationPersistenceClient();
        persistenceClient.open(context);
    }



    @Override
    public void close() {
        int previousState = state;
        closeResourcesAtEnd();
    }

    protected void closeResourcesAtEnd(){
        state             = STATE_CLOSED;
        if (clients != null && clients.size() != 0){
            clients.clear();
        }
        clients           = null;
        persistenceClient = null;
        //Network.getInstance().closeAllConnections();
        closeToken();
    }

    protected void closeResourcesEarly() {
        state             = STATE_CLOSED;
        if (clients != null && clients.size() != 0){
            for (KeyGenerationClient client: clients.values()){
                client.abort();
            }
            clients.clear();
        }
        clients           = null;
        persistenceClient = null;
        closeToken();
    }

    protected void closeToken() {
        if (token != null)
        {
            token.close();
            token = null;
        }
    }



    @Override
    public void abort() {
        basisAbort();
    }

    protected void basisAbort() {
        logger.logEvent(COMPONENT, "aborting key generation", "medium high");
        Network.getInstance().closeAllConnections();
        closeToken();
        if (state >= STATE_PERSIST){
            persistenceClient.rollback();
            presenter.error();
            changeState(STATE_ERROR, state);
        }
    }

    @Override
    public synchronized void submitGeneratedParts(ArrayList<BigNumber> parts, Point publicKey) {
        synchronized (clients){
            while(!allClientsBuild){
                try {
                    clients.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            logger.logEvent(COMPONENT, "Generated parts for:", "low", String.valueOf(getLocalIdentifier()));
            clients.getOrDefault(new Integer(getLocalIdentifier()), null).receiveParts(parts, publicKey, this);
        }
    }


    @Override
    public void submitLoginDetails(String application) {
        logger.logEvent("Coordinator", "new details submitted", "low");
        if (state != STATE_START)
        {
            throw new IllegalStateException("Presenter should not call this method during this state");
        }
        if (token != null && !token.isClosed())
        {
            setApplicationName(application);
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
                        presenter.successfulSubmission();
                    } else {
                        presenter.submitLoginDetailsUnsuccessful();
                    }
                }
            };
            logger.logEvent(COMPONENT, "checking credentials", "low");
            persistenceClient.checkCredentials(requester, application);
        }
        else
        {
            state = STATE_TOKEN_REVOKED;
            presenter.error();
            logger.logEvent("Coordinator", "No active token", "low");
        }
    }

    protected void setApplicationName(String application) {
        details[INDEX_APPLICATION_NAME] = application;
    }


    @Override
    public List<Participant> getOptions() {
        return Network.getInstance().getReachableParticipants();
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
                commitToSelection(selection);
                state = STATE_SELECT;
                presenter.successfulSubmissionOfParameters();
            }
            else
            {
                presenter.error();
                state = STATE_BAD_INP_SEL;
            }
        }
        else
        {
            presenter.error();
            state = STATE_TOKEN_REVOKED;
        }
    }

    protected void commitToSelection(List<Participant> selection) {
        this.selection =  new ArrayList<>(selection);
    }

    protected String getApplicationName(){
        return details[INDEX_APPLICATION_NAME];
    }


    protected String getLogin(){
        return details[INDEX_LOGIN];
    }


    @Override
    public void submitThreshold(int threshold) {
        this.threshold = threshold;
    }

    @Override
    public void run() {
        logger.logEvent(COMPONENT, "request to run", "low");
        if (state != STATE_SELECT)
        {
            throw new IllegalStateException("Cannot run during this state");
        }
        try{
            consumeToken(token);
            generateSession();
            changeState(STATE_SESSION, state);
            inviteAllParticipants();
        } catch (IllegalUseOfClosedTokenException e){
            changeState(STATE_TOKEN_REVOKED, state);
        }
    }

    @Override
    public synchronized  void persisted() {
        basisPersisted();
        numberOfVotes += 1;
            if(result && numberOfVotes == requiredNumberOfVotes){
                done();
            } else if (!result && numberOfVotes == requiredNumberOfVotes){
                rollback();
            }
    }

    protected void basisPersisted() {
        changeState(STATE_PERSIST, state);
        logger.logEvent(COMPONENT, "temporally persisted the data at a participant", "low");
    }

    @Override
    public void submitShares(ArrayList<BigNumber> shares, Point publicKey) {
        changeState(STATE_SHARES, state);
        persist(shares, publicKey);
    }

    protected void persist(ArrayList<BigNumber> shares, Point publicKey) {
        logger.logEvent(COMPONENT, "received final shares and public key", "low");
        persistenceClient.persist(shares, publicKey, session, this);
    }

    protected void generateSession() {
        session  = sessionGenerator.generateSession(selection, threshold, details[INDEX_APPLICATION_NAME], details[INDEX_LOGIN]); // generate session
        requiredNumberOfVotes = 1 + session.getRemoteParticipantList().size();
        logger.logEvent("coordinator", "identifier:"+String.valueOf(session.getLocalParticipant().getIdentifier()), "low");
    }



    @Override
    public synchronized void distributeParts(ArrayList<BigNumber> parts, Point publicKey) {
        changeState(STATE_DISTRIBUTED, state);
        synchronized (clients){
            while (!allClientsBuild){
                try {
                    clients.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            for(KeyGenerationClient client: clients.values()){
                ArrayList<BigNumber> p = new ArrayList<>();
                for(int i =  client.getIdentifier() -1; i < client.getIdentifier() -1 + client.getWeight(); i++){
                    p.add(parts.get(i));
                }
                client.receiveParts(p, publicKey, this);
                logger.logEvent(COMPONENT, "distributed to:", "low", String.valueOf(client.getIdentifier()));
            }
            clients.notify();
        }
    }

    @Override
    public void done() {
        basisClose();

    }

    protected void basisClose() {
        ThreadPoolSupplier.getSupplier().execute(new Runnable() {
            @Override
            public void run() {
                logger.logEvent(COMPONENT, "closing", "normal");
                for (KeyGenerationClient client: clients.values()){
                    client.close(true);
                }
                //Network.getInstance().closeAllConnections();
                logger.logEvent(COMPONENT, "event confirm success of key generation", "normal");
                persistenceClient.confirm();
                changeState(STATE_FINISHED, state);
                presenter.ok();
            }
        });
    }

    @Override
    public void rollback() {
        for (KeyGenerationClient client: clients.values()){
            client.close(true);
        }
        persistenceClient.rollback();
        presenter.error();
    }

    private void inviteAllParticipants(){
        Network.getInstance().establishConnectionsWithInTopologyTwo();
        buildAllClients();
        changeState(STATE_INVITATION, state);
    }

    protected void buildAllClients() {
        synchronized (clients){
            KeyGenerationClient client;
            ThreadedKeyGenerationClient threadedKeyGenerationClient;
            List<IdentifiedParticipant> remoteParticipantList = session.getRemoteParticipantList();
            for (IdentifiedParticipant participant: remoteParticipantList){
                logger.logEvent(COMPONENT, "registered new client", "low", String.valueOf(participant.getIdentifier()));
                client = buildRemoteClient(participant);
                threadedKeyGenerationClient = new ThreadedKeyGenerationClient(client);
                clients.put(new Integer(participant.getIdentifier()), threadedKeyGenerationClient);
                threadedKeyGenerationClient.generateParts(this);
            }
            logger.logEvent(COMPONENT, "registered new client", "low", String.valueOf(session.getLocalParticipant().getIdentifier()));
            client = new LocalLogicalKeyGenerationClient(session.getLocalParticipant(), session);
            threadedKeyGenerationClient = new ThreadedKeyGenerationClient(client);
            logger.logEvent(COMPONENT, "registered new client (check)", "low", String.valueOf(getLocalIdentifier()));
            clients.put(Integer.valueOf(getLocalIdentifier()), threadedKeyGenerationClient);
            threadedKeyGenerationClient.generateParts(this);
            allClientsBuild = true;
            clients.notify();
        }
    }

    protected KeyGenerationClient buildRemoteClient(IdentifiedParticipant participant) {
        KeyGenerationClient client;
        client = new RemoteLogicalKeyGenerationClient(participant, session);
        return client;
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



    protected boolean isWellFormedInput(List<Participant> selection) { //threshold is always ok due to the options given to in the UI
        boolean inputCorrect;
        if (selection == null){
            return false;
        }
        inputCorrect= selection.size()>=1;

        int totalWeight = 0;
        for (Participant participant : selection)
        {
            totalWeight = totalWeight + participant.getWeight();
            logger.logEvent(COMPONENT, "participant added", "low", participant.getAddress());
        }

        inputCorrect = inputCorrect && (totalWeight>=2);
        return  inputCorrect;
    }

    protected void changeState(int newState, int previousState)
    {
        state  = newState;
    }
}
