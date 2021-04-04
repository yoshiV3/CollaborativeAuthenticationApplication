package com.project.collaborativeauthenticationapplication.service.key.application;

import android.content.Context;

import com.project.collaborativeauthenticationapplication.logger.AndroidLogger;
import com.project.collaborativeauthenticationapplication.logger.Logger;
import com.project.collaborativeauthenticationapplication.service.IllegalNumberOfTokensException;
import com.project.collaborativeauthenticationapplication.service.ServiceStateException;
import com.project.collaborativeauthenticationapplication.service.controller.CustomAuthenticationServicePool;
import com.project.collaborativeauthenticationapplication.service.controller.CustomServiceMonitor;
import com.project.collaborativeauthenticationapplication.service.crypto.AndroidSecretStorage;
import com.project.collaborativeauthenticationapplication.service.key.KeyManagementPresenter;
import com.project.collaborativeauthenticationapplication.service.key.KeyToken;
import com.project.collaborativeauthenticationapplication.service.key.application.key_management.Task;


public class CustomKeyManagementClient implements KeyManagementClient{

    public static final int STATE_INIT          = 0;
    public static final int STATE_CLOSED        = 1;
    public static final int STATE_START         = 2;
    private static final int STATE_ERROR        = 3 ;

    private int state                           = STATE_INIT;

    KeyManagementPresenter presenter;

    private AndroidSecretStorage storage;

    private CustomKeyManagementPersistenceManager persistenceManager = new CustomKeyManagementPersistenceManager();

    private KeyToken token = null;

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
            Logger logger = new AndroidLogger();
            logger.logError("CLIENT: key management", "not properly managed states", "CRITICAL");
            close();
        }
        super.finalize();
    }

    @Override
    public void remove(Task task)  {
        persistenceManager.removeCredentials(task.getApplicationName(), task.getLogin(), storage);
        task.done();

    }
}
