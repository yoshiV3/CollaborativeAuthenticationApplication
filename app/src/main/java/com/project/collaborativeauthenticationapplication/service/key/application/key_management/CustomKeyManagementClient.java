package com.project.collaborativeauthenticationapplication.service.key.application.key_management;

import android.content.Context;

import com.project.collaborativeauthenticationapplication.logger.AndroidLogger;
import com.project.collaborativeauthenticationapplication.service.IllegalNumberOfTokensException;
import com.project.collaborativeauthenticationapplication.service.IllegalUseOfClosedTokenException;
import com.project.collaborativeauthenticationapplication.service.ServiceStateException;
import com.project.collaborativeauthenticationapplication.service.controller.CustomAuthenticationServicePool;
import com.project.collaborativeauthenticationapplication.service.controller.CustomServiceMonitor;
import com.project.collaborativeauthenticationapplication.service.crypto.AndroidSecretStorage;
import com.project.collaborativeauthenticationapplication.service.crypto.BigNumber;
import com.project.collaborativeauthenticationapplication.service.crypto.CryptoPartKeyRecovery;
import com.project.collaborativeauthenticationapplication.service.crypto.SecureStorageException;
import com.project.collaborativeauthenticationapplication.service.key.KeyManagementPresenter;
import com.project.collaborativeauthenticationapplication.service.key.KeyToken;

import java.util.ArrayList;


public class CustomKeyManagementClient implements KeyManagementClient{

    public static final int STATE_INIT          = 0;
    public static final int STATE_CLOSED        = 1;
    public static final int STATE_START         = 2;
    private static final int STATE_ERROR        = 3 ;

    private int state                           = STATE_INIT;

    KeyManagementPresenter presenter;

    private AndroidSecretStorage storage;

    private AndroidLogger  logger = new AndroidLogger();

    private CustomKeyManagementPersistenceManager persistenceManager = new CustomKeyManagementPersistenceManager();

    private KeyToken token = null;

    private CryptoPartKeyRecovery recovery = new CryptoPartKeyRecovery();

    public CustomKeyManagementClient(KeyManagementPresenter keyManagementPresenter) {
        presenter = keyManagementPresenter;
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
        if (CustomServiceMonitor.getInstance().isServiceEnabled()) {
            try {
                token = CustomAuthenticationServicePool.getInstance().getNewKeyToken();
                state = STATE_START;
                this.storage =  new AndroidSecretStorage(context);

            } catch (IllegalNumberOfTokensException | ServiceStateException e) {
                state = STATE_ERROR;
                presenter.onError("Could not get a token (not available");
            }
        }
        else
        {
            presenter.onError("Service is no longer working properly");
        }
    }

    @Override
    public void close() {
        if (token != null) {
            token.close();
            token = null;
        }
        state = STATE_CLOSED;
        storage = null;
    }

    @Override
    protected void finalize() throws Throwable {
        if (state != STATE_CLOSED)
        {
            logger.logError("CLIENT: key management", "not properly managed states", "CRITICAL");
            close();
        }
        super.finalize();
    }

    @Override
    public void remove(Task task)  {
        logger.logError("CLIENT: key management", "not properly managed states", "CRITICAL");
        persistenceManager.removeCredentials(task.getApplicationName(), task.getLogin(), storage);
        task.done();

    }

    @Override
    public void extend(FeedbackTask task) {
        logger.logEvent("CLIENT: key management", "request to extend a secret to a nex share", "high");
        ArrayList<BigNumber> shares = new ArrayList<>();

        try {
            int[] identifiers = persistenceManager.getSharesAndIdentifiersForRecovery(token, task.getApplicationName(), task.getLogin(), storage, shares);
            int newIdentifier = persistenceManager.getTotalNumberOfKeys(task.getApplicationName(), task.getLogin());
            BigNumber share   = recovery.createLocalSecretSharePartsFromSharesForTarget(shares, identifiers,
                    newIdentifier);
            persistenceManager.persist(token, task.getApplicationName(), task.getLogin(), share, newIdentifier, storage);
            task.giveFeedback("Successfully generated anew share", true);
            task.done();
        } catch (IllegalUseOfClosedTokenException e) {
            task.giveFeedback("Failed due to an illegal state", false);
        } catch (SecureStorageException e) {
            task.giveFeedback("Failed due to the secret storage", false);
        } catch (IllegalArgumentException e){
            task.giveFeedback("Failed due to bad credentials", false);
        }
    }
}
