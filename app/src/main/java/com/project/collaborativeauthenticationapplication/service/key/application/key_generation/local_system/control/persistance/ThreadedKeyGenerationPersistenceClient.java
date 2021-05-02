package com.project.collaborativeauthenticationapplication.service.key.application.key_generation.local_system.control.persistance;

import android.content.Context;

import com.project.collaborativeauthenticationapplication.service.concurrency.ThreadPoolSupplier;
import com.project.collaborativeauthenticationapplication.service.crypto.AndroidSecretStorage;
import com.project.collaborativeauthenticationapplication.service.crypto.BigNumber;
import com.project.collaborativeauthenticationapplication.service.crypto.Point;
import com.project.collaborativeauthenticationapplication.service.crypto.SecureStorageException;
import com.project.collaborativeauthenticationapplication.service.general.FeedbackRequester;
import com.project.collaborativeauthenticationapplication.service.key.application.key_generation.distributed_system.KeyGenerationCoordinator;
import com.project.collaborativeauthenticationapplication.service.key.application.key_generation.local_system.control.protocol.KeyGenerationSession;

import java.util.List;


public class ThreadedKeyGenerationPersistenceClient  implements KeyGenerationPersistenceClient{


    private CustomKeyGenerationPersistenceManager persistenceManager    = new CustomKeyGenerationPersistenceManager();

    private AndroidSecretStorage storage;

    private KeyGenerationSession session;

    @Override
    public void open(Context context){
        storage =  new AndroidSecretStorage(context);
    }

    @Override
    public void persist(List<BigNumber> shares, Point publicKey, KeyGenerationSession session, KeyGenerationCoordinator coordinator) {
        this.session = session;
        ThreadPoolSupplier.getSupplier().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    persistenceManager.persist(storage, shares, publicKey, session);
                    coordinator.persisted();
                } catch (SecureStorageException e){
                    coordinator.abort();
                }
            }
        });
    }

    @Override
    public void rollback() {
        ThreadPoolSupplier.getSupplier().execute(new Runnable() {
            @Override
            public void run() {
                persistenceManager.removeCredentials(session.getApplicationName(), session.getLogin(), storage);
            }
        });
    }

    @Override
    public void confirm() {
        ThreadPoolSupplier.getSupplier().execute(new Runnable() {
            @Override
            public void run() {
                persistenceManager.confirm(session.getApplicationName(), session.getLogin());
            }
        });
    }

    @Override
    public void checkCredentials(FeedbackRequester requester, String applicationName, String login) {
        ThreadPoolSupplier.getSupplier().execute(new Runnable() {
            @Override
            public void run() {
                boolean result = !persistenceManager.hasApplicationLoginWithGivenCredentials(applicationName, login);
                requester.setResult(result);
                requester.signalJobDone();
            }
        });
    }
}
