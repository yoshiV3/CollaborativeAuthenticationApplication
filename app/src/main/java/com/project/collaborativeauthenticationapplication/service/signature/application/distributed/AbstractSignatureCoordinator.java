package com.project.collaborativeauthenticationapplication.service.signature.application.distributed;

import com.project.collaborativeauthenticationapplication.logger.AndroidLogger;
import com.project.collaborativeauthenticationapplication.service.controller.CustomAuthenticationServiceController;
import com.project.collaborativeauthenticationapplication.service.crypto.BigNumber;
import com.project.collaborativeauthenticationapplication.service.crypto.Point;
import com.project.collaborativeauthenticationapplication.service.general.IllegalNumberOfTokensException;
import com.project.collaborativeauthenticationapplication.service.general.ServiceStateException;
import com.project.collaborativeauthenticationapplication.service.general.SignatureToken;
import com.project.collaborativeauthenticationapplication.service.general.Task;
import com.project.collaborativeauthenticationapplication.service.network.CustomCommunication;
import com.project.collaborativeauthenticationapplication.service.signature.application.SignatureClient;
import com.project.collaborativeauthenticationapplication.service.signature.application.local.InformationSignatureClient;
import com.project.collaborativeauthenticationapplication.service.signature.application.local.RandomnessRequester;
import com.project.collaborativeauthenticationapplication.service.signature.application.local.ThreadedInformationSignatureClient;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractSignatureCoordinator implements SignatureCoordinator {

    public static final int STATE_INIT          = 0;
    public static final int STATE_CLOSED        = 1;
    public static final int STATE_START         = 2;
    public static final int STATE_RANDOMNESS    = 3;
    public static final int STATE_ERROR         = 4;
    public static final String COMPONENT = "Abstract Coordinator";

    private AndroidLogger logger = new AndroidLogger();



    private SignatureToken token = null;


    public void getToken() throws ServiceStateException, IllegalNumberOfTokensException {
        token = CustomAuthenticationServiceController.getInstance().getNewSignatureToken();;
    }



    protected void startRandomness(){
        setState(STATE_RANDOMNESS);
    }



    protected  abstract int  getNumberOfRequestedSharesForClient(SignatureClient client);


    protected abstract  void handleCommitmentE(List<Point> commitment, String address);
    protected abstract  void handleCommitmentD(List<Point> commitment, String address);

    protected abstract void randomnessGenerationDone();


    protected void requestSharesFromClient(SignatureClient client){
        int numberToRequest = getNumberOfRequestedSharesForClient(client); //requestedNumberOfShares.getOrDefault(client.getAddress(), 0);
        RandomnessRequester requester = new RandomnessRequester() {
            @Override
            public String getApplicationName() {
                return getOriginalTask().getApplicationName();
            }

            @Override
            public String getLogin() {
                return getOriginalTask().getLogin();
            }

            @Override
            public int getNumberOfRequestedShares() {
                return numberToRequest;
            }


            @Override
            public void setCommitmentE(List<Point> commitment) {
                //synchronized (eCommitment){
                   // eCommitment.addAll(commitment);
                //}
                handleCommitmentE(commitment, client.getAddress());
                logger.logEvent("SIGVER", "set  comm e", "low");
            }

            @Override
            public void setCommitmentD(List<Point> commitment) {
                logger.logEvent("SIGVER", "set comm d", "low");
                //synchronized (dCommitment){
                    //dCommitment.addAll(commitment);
                //}
                handleCommitmentD(commitment, client.getAddress());
            }

            @Override
            public void signalJobDone() {
                //synchronized (dCommitment){
                    //if(dCommitment.size()== signThreshold){
                        //produceSignatureShares();
                    //}

                //}
                randomnessGenerationDone();

            }
        };
        logger.logEvent(COMPONENT, "request commitments from a client", "low", client.getAddress());
        client.generateRandomnessAndCalculateCommitments(requester);
    }

    @Override
    public void addHash(BigNumber hash) {
        this.hash = hash;
    }

    private int state = STATE_INIT;

    private BigNumber hash;
    private BigNumber signature;
    private BigNumber message;


    private InformationSignatureClient client = null;


    protected void setClient(InformationSignatureClient client) {
        this.client = new ThreadedInformationSignatureClient(client);
    }

    protected InformationSignatureClient getClient() {
        return client;
    }

    private Task originalTask;


    protected Task getOriginalTask() {
        return originalTask;
    }

    protected void setOriginalTask(Task originalTask) {
        this.originalTask = originalTask;
    }

    protected void setSignature(BigNumber signature) {
        this.signature = signature;
    }

    protected void setHash(BigNumber hash) {
        this.hash = hash;
    }

    protected void setMessage(BigNumber message) {
        this.message = message;
    }

    protected void setState(int state) {
        this.state = state;
    }


    @Override
    public BigNumber getHash() {
        return hash;
    }

    @Override
    public BigNumber getSignature() {
        return signature;
    }

    @Override
    public BigNumber getMessage() {
        return message;
    }

    @Override
    public int getState() {
        return state;
    }

    @Override
    public void close() {
        releaseToken();
        if (client != null){
            client.close();
            client = null;
        }
        CustomCommunication.getInstance().closeAllConnections();
        setState(STATE_CLOSED);
    }

    protected void releaseToken() {
        if (token != null) {
            token.close();
            token = null;
        }
    }
}
