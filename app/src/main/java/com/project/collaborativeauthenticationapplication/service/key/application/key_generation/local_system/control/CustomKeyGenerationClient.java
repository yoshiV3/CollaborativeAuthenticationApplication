package com.project.collaborativeauthenticationapplication.service.key.application.key_generation.local_system.control;

import android.content.Context;

import com.project.collaborativeauthenticationapplication.logger.AndroidLogger;
import com.project.collaborativeauthenticationapplication.logger.Logger;
import com.project.collaborativeauthenticationapplication.service.Requester;
import com.project.collaborativeauthenticationapplication.service.crypto.AndroidSecretStorage;
import com.project.collaborativeauthenticationapplication.service.crypto.BigNumber;
import com.project.collaborativeauthenticationapplication.service.crypto.Point;
import com.project.collaborativeauthenticationapplication.service.crypto.SecureStorageException;
import com.project.collaborativeauthenticationapplication.service.key.application.key_generation.distributed_system.KeyGenerationCoordinator;
import com.project.collaborativeauthenticationapplication.service.key.application.key_generation.local_system.FeedbackRequester;
import com.project.collaborativeauthenticationapplication.service.key.application.key_generation.local_system.computations.CustomLocalKeyGenerator;
import com.project.collaborativeauthenticationapplication.service.key.application.key_management.FeedbackTask;


import java.util.ArrayList;
import java.util.List;

public class CustomKeyGenerationClient implements KeyGenerationClient {



    public static final int STATE_INIT    = 0;
    public static final int STATE_OPEN    = 1;
    public static final int STATE_SESSION = 2;
    public static final int STATE_SHARES  = 3;
    public static final int STATE_CLOSED  = 4;


    private static Logger logger = new AndroidLogger();



    private KeyGenerationCoordinator coordinator;

    private int state = STATE_INIT;

    private CustomLocalKeyGenerator                keyGenerator          = new CustomLocalKeyGenerator();
    private CustomKeyGenerationPersistenceManager  persistenceManager    = new CustomKeyGenerationPersistenceManager();

    private KeyGenerationSession session;

    private ArrayList<BigNumber> shares;
    private Point                finalPubicKey;
    private AndroidSecretStorage storage;


    public CustomKeyGenerationClient(KeyGenerationCoordinator coordinator){
        this.coordinator = coordinator;
    }


    @Override
    public void receiveKeyGenerationSession(Requester requester, KeyGenerationSession session) {
        if (state != STATE_OPEN){
            throw new IllegalStateException();
        }
        this.session = session;
        state = STATE_SESSION;
        requester.signalJobDone();
    }

    @Override
    public void calculatePartsAndPublicKey(Requester requester, List<BigNumber> parts, Point publicKey) {
        if (state != STATE_SESSION){
            throw new IllegalStateException();
        }
        if (session == null){
            throw new IllegalStateException();
        }
        keyGenerator.calculatePartsAndPublicKey(session, parts, publicKey);
        requester.signalJobDone();
    }

    @Override
    public void calculateShares(Requester requester, List<List<BigNumber>> parts) {
        if (state != STATE_SESSION){
            throw new IllegalStateException();
        }
        shares = new ArrayList<>();
        keyGenerator.calculateShares(parts, shares);

        String extra = "(" + String.valueOf(parts.size()) + "," + String.valueOf(shares.size()) + ")";
        logger.logEvent("Client", "Request to calculate shares", "low", extra);

        state = STATE_SHARES;
        requester.signalJobDone();
    }

    @Override
    public void checkCredentials(FeedbackRequester requester, String applicationName, String login) {
        boolean check = !persistenceManager.hasApplicationLoginWithGivenCredentials(applicationName, login);
        requester.setResult(check);
        requester.signalJobDone();
    }

    @Override
    public void receiveFinalPublicKey(Requester requester, Point publicKey) {
        this.finalPubicKey = publicKey;
        requester.signalJobDone();
    }

    @Override
    public void persist(FeedbackRequester requester) {
        if (state != STATE_SHARES){
            throw new IllegalStateException();
        }
        try{
            persistenceManager.persist(storage, shares, finalPubicKey, session );
            requester.setResult(true);
        } catch (SecureStorageException e) {
            e.printStackTrace();
            requester.setResult(false);
        }
        requester.signalJobDone();
    }

    @Override
    public int getState() {
        return state;
    }

    @Override
    public void open(Context context) {
        logger.logEvent("Client", "new open request", "low");
        if (state != STATE_INIT)
        {
            throw  new IllegalStateException();
        }
        state = STATE_OPEN;
        this.storage =  new AndroidSecretStorage(context);
    }

    @Override
    public void close() {
        state = STATE_CLOSED;
        session = null;
        shares  = null;
        storage = null;

    }
}
