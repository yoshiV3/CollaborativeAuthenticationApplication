package com.project.collaborativeauthenticationapplication.service.signature.application.old;

import android.content.Context;

import com.project.collaborativeauthenticationapplication.logger.AndroidLogger;
import com.project.collaborativeauthenticationapplication.service.general.CustomKeyViewManager;
import com.project.collaborativeauthenticationapplication.service.crypto.AndroidSecretStorage;
import com.project.collaborativeauthenticationapplication.service.crypto.BigNumber;
import com.project.collaborativeauthenticationapplication.service.crypto.CryptoThresholdSignatureProcessor;
import com.project.collaborativeauthenticationapplication.service.crypto.Point;
import com.project.collaborativeauthenticationapplication.service.crypto.RandomnessGenerator;
import com.project.collaborativeauthenticationapplication.service.crypto.SecureStorageException;
import com.project.collaborativeauthenticationapplication.service.signature.application.local.DatabaseInformationRequester;
import com.project.collaborativeauthenticationapplication.service.signature.application.local.RandomnessRequester;

import java.util.ArrayList;
import java.util.List;

public class CustomSignatureClient {

    public static final int  STATE_INIT          = 0;
    public static final int  STATE_CLOSED        = 1;
    public static final int  STATE_START         = 2;
    private static final int STATE_ERROR        = 3 ;


    private SignatureCoordinator coordinator;
    private CustomKeyViewManager keyViewManager;

    RandomnessGenerator               randomnessGenerator = new RandomnessGenerator();
    CryptoThresholdSignatureProcessor signatureProcessor  = new CryptoThresholdSignatureProcessor();


    private AndroidSecretStorage storage;

    private AndroidLogger logger = new AndroidLogger();

    int state = STATE_INIT;


    public CustomSignatureClient(SignatureCoordinator coordinator) {
        this.coordinator    = coordinator;
        this.keyViewManager = new CustomKeyViewManager();
    }

    //@Override
    public void sign(SignatureTask task) {
        if (state == STATE_CLOSED){
            throw new IllegalStateException();
        }
        signatureProcessor.receiveAllRandomness(task.geteCommitment(), task.getdCommitment());
        String extra = String.valueOf(task.getNumberOfRequestShares());
        logger.logEvent("Client: signature", "request to sign", "low", extra);
        int[] identifiers = keyViewManager.getLocalIdentifiers(task.getApplicationName(), task.getLogin(), task.getNumberOfRequestShares());
        ArrayList<BigNumber> shares = new ArrayList<>();
        try {
            for (int identifier: identifiers){
                BigNumber secret = storage.getSecrets(task.getApplicationName(), task.getLogin(), identifier);
                shares.add(secret);
            }
            signatureProcessor.receiveShares(shares);
            signatureProcessor.produceSignatureShare(task.getMessage(), identifiers);
            List<BigNumber> signature = signatureProcessor.publishSignatureShare();
            task.setSignature(signature.get(0));
            task.setHash(signature.get(1));
        } catch (SecureStorageException e) {
                e.printStackTrace();
        }
        task.done();
    }

    //@Override
    public void checkInformationAboutCredential(String applicationName, String login, DatabaseInformationRequester requester) {
        if (state == STATE_CLOSED){
            throw new IllegalStateException();
        }
        List<String> participants = keyViewManager.getAllRemoteParticipantsFor(applicationName, login);
        for(String participant: participants){
            int number = keyViewManager.getNumberOfRemoteSecretsFor(participant, applicationName, login);
            requester.setNumberOfRemoteKeysForRemoteParticipant(participant, number);
        }
        requester.setNumberOfLocalKeys(keyViewManager.getNumberOfLocalKeys(applicationName, login));
        requester.setThreshold(keyViewManager.getThreshold(applicationName, login));
        requester.signalJobDone();
    }

    //@Override
    public void generateRandomnessAndCalculateCommitments(RandomnessRequester requester) {
        if (state == STATE_CLOSED){
            throw new IllegalStateException();
        }
        String extra = String.valueOf(requester.getNumberOfRequestedShares());
        logger.logEvent("Client: signature", "request for randomness", "low", extra);
        ArrayList<BigNumber> e = new ArrayList<>();
        ArrayList<BigNumber> d = new ArrayList<>();
        for(int i = 0; i < requester.getNumberOfRequestedShares(); i++){
            BigNumber r1 = randomnessGenerator.generateRandomness();
            BigNumber r2 = randomnessGenerator.generateRandomness();
            e.add(r1);
            d.add(r2);
        }
        signatureProcessor.receiveRandomness(e, d);
        signatureProcessor.calculateCommitmentsToRandomness();
        //requester.setE(e);
        //requester.setD(d);

        List<Point> commitmentE = signatureProcessor.publishCommitmentsE();
        requester.setCommitmentE(commitmentE);
        requester.setCommitmentD(signatureProcessor.publishCommitmentsD());
        requester.signalJobDone();
    }

    //@Override
    public int getState() {
        return state;
    }

    //@Override
    public void open(Context context) {
        if (state != STATE_INIT)
        {
            throw  new IllegalStateException();
        }
        state = STATE_START;
        this.storage =  new AndroidSecretStorage(context);
    }

    //@Override
    public void close() {
        state = STATE_CLOSED;
    }
}
