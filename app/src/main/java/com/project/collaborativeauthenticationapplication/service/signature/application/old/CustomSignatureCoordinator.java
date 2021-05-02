package com.project.collaborativeauthenticationapplication.service.signature.application.old;

import android.content.Context;

import com.project.collaborativeauthenticationapplication.logger.AndroidLogger;
import com.project.collaborativeauthenticationapplication.service.general.IllegalNumberOfTokensException;
import com.project.collaborativeauthenticationapplication.service.general.Requester;
import com.project.collaborativeauthenticationapplication.service.general.ServiceStateException;
import com.project.collaborativeauthenticationapplication.service.general.SignatureToken;
import com.project.collaborativeauthenticationapplication.service.general.Task;
import com.project.collaborativeauthenticationapplication.service.controller.CustomAuthenticationServiceController;
import com.project.collaborativeauthenticationapplication.service.controller.CustomServiceMonitor;
import com.project.collaborativeauthenticationapplication.service.crypto.BigNumber;
import com.project.collaborativeauthenticationapplication.service.crypto.Point;
import com.project.collaborativeauthenticationapplication.service.crypto.RandomnessGenerator;
import com.project.collaborativeauthenticationapplication.service.signature.SignaturePresenter;
import com.project.collaborativeauthenticationapplication.service.signature.application.local.DatabaseInformationRequester;
import com.project.collaborativeauthenticationapplication.service.signature.application.local.RandomnessRequester;

import java.util.ArrayList;
import java.util.List;

public class CustomSignatureCoordinator implements SignatureCoordinator {


    public static final int STATE_INIT          = 0;
    public static final int STATE_CLOSED        = 1;
    public static final int STATE_START         = 2;
    private static final int STATE_ERROR        = 3;


    private SignaturePresenter presenter;

    private SignatureClient client;

    private RandomnessGenerator randomnessGenerator;


    private BigNumber hash;
    private BigNumber signature;
    private BigNumber message;


    private int signThreshold     = 0;
    private int numberOfLocalKeys = 0;
    private int numberToRequest   = 0;

    ArrayList<BigNumber> e = new ArrayList<>();
    ArrayList<BigNumber> d = new ArrayList<>();

    ArrayList<Point> eCommitment = new ArrayList<>();
    ArrayList<Point> dCommitment = new ArrayList<>();



    private SignatureToken token = null;
    private AndroidLogger logger = new AndroidLogger();

    int state = STATE_INIT;

    public CustomSignatureCoordinator(SignaturePresenter presenter){
        this.presenter      = presenter;
        randomnessGenerator = new RandomnessGenerator();
    }

    private interface SpecialRequester extends Requester {
        void setCode(Runnable code);
    }


    private void generateRandomness(Task originalTask, SignatureTask task, SpecialRequester specialRequester){
        RandomnessRequester requester = new RandomnessRequester() {
            @Override
            public String getApplicationName() {
                return task.getApplicationName();
            }

            @Override
            public String getLogin() {
                return task.getLogin();
            }

            @Override
            public int getNumberOfRequestedShares() {
                return numberToRequest;
            }



            @Override
            public void setCommitmentE(List<Point> commitment) {
                logger.logEvent("SIGVER", "set  comm e", "low");
                eCommitment.clear();
                eCommitment.addAll(commitment);
            }

            @Override
            public void setCommitmentD(List<Point> commitment) {
                logger.logEvent("SIGVER", "set comm d", "low");
                dCommitment.clear();
                dCommitment.addAll(commitment);
            }

            @Override
            public void signalJobDone() {
                produceSignatureShares(originalTask, task, specialRequester);
            }
        };
        client.generateRandomnessAndCalculateCommitments(requester);
    }

    private void produceSignatureShares(Task originalTask, SignatureTask task, SpecialRequester specialRequester){
        task.setNumberOfRequestShares(numberToRequest);
        Runnable code = new Runnable() {
            @Override
            public void run() {
                hash       = task.getHash();
                signature  = task.getSignature();
                message    = task.getMessage();
                originalTask.done();
            }
        };
        specialRequester.setCode(code);
        client.sign(task);
    }

    @Override
    public void sign(Task task) {
        message = randomnessGenerator.generateRandomness();
        SpecialRequester requester = new SpecialRequester() {
            private  Runnable code;

            public void setCode(Runnable code) {
                this.code = code;
            }

            @Override
            public void signalJobDone() {
                code.run();
            }
        };
        SignatureTask  innerTask   = new SignatureTask(task, message, e,d, eCommitment, dCommitment, requester);
        DatabaseInformationRequester databaseInformationRequester = new DatabaseInformationRequester() {
            @Override
            public void setThreshold(int threshold) {
                signThreshold = threshold;
            }

            @Override
            public void setNumberOfRemoteKeysForRemoteParticipant(String address, int remoteKeys) {

            }

            @Override
            public void setNumberOfLocalKeys(int localKeys) {
                numberOfLocalKeys = localKeys;
            }

            @Override
            public void signalJobDone() {
                String extra = "(" + String.valueOf(numberOfLocalKeys) +"," + String.valueOf(signThreshold) + ")";
                logger.logEvent("Coordinator: signature", "request to sign", "low", extra);

                if (numberOfLocalKeys > signThreshold){
                    numberToRequest = signThreshold;
                }
                else {
                    numberToRequest = numberOfLocalKeys;
                }
                generateRandomness(task, innerTask, requester);
            }
        };
        client.checkInformationAboutCredential(task.getApplicationName(), task.getLogin(), databaseInformationRequester);
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
    public void open(Context context) {
        if (state != STATE_INIT)
        {
            throw  new IllegalStateException();
        }
        if(client != null && client.getState() != STATE_INIT){
            throw new IllegalStateException();
        } else if (client == null){
            client = new ThreadedSignatureClient(this);
        }
        client.open(context);
        if (CustomServiceMonitor.getInstance().isServiceEnabled()) {
            try {
                token = CustomAuthenticationServiceController.getInstance().getNewSignatureToken();
                state = STATE_START;
            } catch (IllegalNumberOfTokensException | ServiceStateException e) {
                state = STATE_ERROR;
                presenter.onErrorSignature("Could not open a client");
            }
        }
        else
        {
            presenter.onErrorSignature("Service is no longer working properly");
        }
    }

    @Override
    public void close() {
        if (token != null) {
            token.close();
            token = null;
        }
        state = STATE_CLOSED;
    }


    @Override
    protected void finalize() throws Throwable {
        if (state != STATE_CLOSED)
        {
            logger.logError("coordinator: signature ", "not properly managed states", "CRITICAL");
            close();
        }
        super.finalize();
    }
}
