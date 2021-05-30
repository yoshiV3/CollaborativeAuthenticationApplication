package com.project.collaborativeauthenticationapplication.service.signature.application.local;

import android.content.Context;

import com.project.collaborativeauthenticationapplication.alternative.network.Network;
import com.project.collaborativeauthenticationapplication.logger.AndroidLogger;
import com.project.collaborativeauthenticationapplication.service.crypto.AndroidSecretStorage;
import com.project.collaborativeauthenticationapplication.service.crypto.BigNumber;
import com.project.collaborativeauthenticationapplication.service.crypto.CryptoThresholdSignatureProcessor;
import com.project.collaborativeauthenticationapplication.service.crypto.Point;
import com.project.collaborativeauthenticationapplication.service.crypto.RandomnessGenerator;
import com.project.collaborativeauthenticationapplication.service.crypto.SecureStorageException;
import com.project.collaborativeauthenticationapplication.service.general.CustomKeyViewManager;
import com.project.collaborativeauthenticationapplication.service.general.FeedbackRequester;
import com.project.collaborativeauthenticationapplication.service.network.CustomCommunication;
import com.project.collaborativeauthenticationapplication.service.signature.application.distributed.SignatureCoordinator;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;


public class LocalInformationSignatureClient implements InformationSignatureClient {

    public static final int  STATE_INIT          = 0;
    public static final int  STATE_CLOSED        = 1;
    public static final int  STATE_START         = 2;
    private static final int STATE_ERROR        = 3 ;


    private SignatureCoordinator coordinator;
    private CustomKeyViewManager keyViewManager;

    RandomnessGenerator randomnessGenerator = new RandomnessGenerator();
    CryptoThresholdSignatureProcessor signatureProcessor  = new CryptoThresholdSignatureProcessor();


    private AndroidSecretStorage storage;

    private AndroidLogger logger = new AndroidLogger();

    int state = STATE_INIT;
    private ArrayList<BigNumber> e;
    private ArrayList<BigNumber> d;

    public LocalInformationSignatureClient(SignatureCoordinator coordinator) {
        this.coordinator    = coordinator;
        this.keyViewManager = new CustomKeyViewManager();
    }

    @Override
    public void checkInformationAboutCredential(String applicationName, DatabaseInformationRequester requester) {
        if (state == STATE_CLOSED){
            throw new IllegalStateException();
        }
        List<String> participants = keyViewManager.getAllRemoteParticipantsFor(applicationName);
        for(String participant: participants){
            int number = keyViewManager.getNumberOfRemoteSecretsFor(participant, applicationName);
            requester.setNumberOfRemoteKeysForRemoteParticipant(participant, number);
        }
        requester.setNumberOfLocalKeys(keyViewManager.getNumberOfLocalKeys(applicationName));
        requester.setThreshold(keyViewManager.getThreshold(applicationName));
        requester.signalJobDone();
    }

    @Override
    public void checkIfEnoughLocalShares(int numberOfShares, String applicationName, FeedbackRequester requester) {
        boolean result =  keyViewManager.getNumberOfLocalKeys(applicationName) >= numberOfShares;
        requester.setResult(result);
        requester.signalJobDone();
    }

    @Override
    public void calculateFinalSignature(ArrayList<BigNumber> parts, SignatureRequester requester) {
        BigNumber signature = signatureProcessor.calculateFinalSignature(parts);
        requester.submitSignature(signature);
        requester.signalJobDone();

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
        state = STATE_START;
        this.storage =  new AndroidSecretStorage(context);
    }

    @Override
    public void close() {
        state = STATE_CLOSED;
    }

    @Override
    public String getAddress() {
        return "here";
    }

    @Override
    public void sign(SignatureTask task) {
        String localAddress = Network.getInstance().getLocalAddress();
        ArrayList<SortableParticipant> list = new ArrayList<>();
        HashMap<String, ArrayList<Point>>  eCommitment = task.getCommitmentsE();
        HashMap<String, ArrayList<Point>>  dCommitment = task.getCommitmentsD();
        String applicationName = task.getApplicationName();
        String extra = applicationName ;
        logger.logEvent("Local client", "looking for identifiers of ", "normal",  extra);
        int localNumber = keyViewManager.getNumberOfLocalKeys(applicationName);
        logger.logEvent("Local client", "has a number of local keys ", "normal", String.valueOf(localNumber));
        for (String address : dCommitment.keySet()){
            boolean isLocal;
            int[] identifiers;
            logger.logEvent("Local client", "looking for identifiers of ", "normal", address);
            if (address.equals(localAddress) || address.equals("here")){
                isLocal = true;
                identifiers = keyViewManager.getAllLocalIdentifiers(applicationName);
            } else {
                isLocal = false;
                identifiers = keyViewManager.getAllRemoteIdentifiers(applicationName, address);
            }
             extra = address + "," + identifiers.length;
            logger.logEvent("Local client", "found a number of identifiers", "normal",  extra);
            list.add(new SortableParticipant(address, identifiers, isLocal));
        }
        Collections.sort(list);

        ArrayList<Point> E =  new ArrayList<>();
        ArrayList<Point> D =  new ArrayList<>();

        int[] identifiers = new int[keyViewManager.getThreshold(applicationName)];

        int firstLocalIndex = 0;
        int localWeight     = 0;

        SortableParticipant localParticipant = null;

        for (SortableParticipant participant : list){
            int index = E.size();
            ArrayList<Point> eList = eCommitment.get(participant.getAddress());
            ArrayList<Point> dList = dCommitment.get(participant.getAddress());
            int weight = eList.size();
            if (participant.isLocal()){
                firstLocalIndex = index;
                localWeight     = weight;
                localParticipant = participant;
            }
            E.addAll(eList);
            D.addAll(dList);
            System.arraycopy(participant.getIdentifiers(), 0, identifiers, index, weight);
        }

        ArrayList<BigNumber> shares = new ArrayList<>();

        try{
            signatureProcessor.receiveAllRandomness(E, D);
            for (int identifier: localParticipant.getSubsetOfIdentifiers(localWeight)){
                BigNumber secret = storage.getSecrets(applicationName, identifier);
                shares.add(secret);
            }
            signatureProcessor.receiveShares(shares);
            signatureProcessor.produceSignatureShare(task.getMessage(), identifiers, firstLocalIndex);
            List<BigNumber> signature = signatureProcessor.publishSignatureShare();
            coordinator.addSignaturePart(signature.get(0));
            coordinator.addHash(signature.get(1));
            task.done();
        } catch (SecureStorageException secureStorageException) {
            secureStorageException.printStackTrace();
            coordinator.abort();
        }

    }

    @Override
    public void generateRandomnessAndCalculateCommitments(RandomnessRequester requester) {
        if (state == STATE_CLOSED){
            throw new IllegalStateException();
        }
        String extra = String.valueOf(requester.getNumberOfRequestedShares());
        logger.logEvent("Client: signature", "request for randomness", "low", extra);
        e = new ArrayList<>();
        d = new ArrayList<>();
        for(int i = 0; i < requester.getNumberOfRequestedShares(); i++){
            BigNumber r1 = randomnessGenerator.generateRandomness();
            BigNumber r2 = randomnessGenerator.generateRandomness();
            e.add(r1);
            d.add(r2);
        }
        signatureProcessor.receiveRandomness(e, d);
        signatureProcessor.calculateCommitmentsToRandomness();
        List<Point> commitmentE = signatureProcessor.publishCommitmentsE();
        requester.setCommitmentE(commitmentE);
        requester.setCommitmentD(signatureProcessor.publishCommitmentsD());
        requester.signalJobDone();
    }
}
