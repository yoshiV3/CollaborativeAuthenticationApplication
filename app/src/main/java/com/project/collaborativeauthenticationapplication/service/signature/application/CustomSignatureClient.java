package com.project.collaborativeauthenticationapplication.service.signature.application;

import android.content.Context;

import com.project.collaborativeauthenticationapplication.logger.AndroidLogger;
import com.project.collaborativeauthenticationapplication.service.IllegalNumberOfTokensException;
import com.project.collaborativeauthenticationapplication.service.ServiceStateException;
import com.project.collaborativeauthenticationapplication.service.SignatureToken;
import com.project.collaborativeauthenticationapplication.service.controller.CustomAuthenticationServicePool;
import com.project.collaborativeauthenticationapplication.service.controller.CustomServiceMonitor;
import com.project.collaborativeauthenticationapplication.service.crypto.AndroidSecretStorage;
import com.project.collaborativeauthenticationapplication.service.signature.SignaturePresenter;

public class CustomSignatureClient implements SignatureClient{

    public static final int STATE_INIT          = 0;
    public static final int STATE_CLOSED        = 1;
    public static final int STATE_START         = 2;
    private static final int STATE_ERROR        = 3 ;


    private SignaturePresenter  presenter;


    private SignatureToken token = null;
    private AndroidSecretStorage storage;

    private AndroidLogger logger = new AndroidLogger();

    int state = STATE_INIT;


    public CustomSignatureClient(SignaturePresenter signaturePresenter) {
        presenter = signaturePresenter;
    }

    @Override
    public void sign(SignatureTask task) {
        task.done();
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
                token = CustomAuthenticationServicePool.getInstance().getNewSignatureToken();
                state = STATE_START;
                this.storage =  new AndroidSecretStorage(context);

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
            logger.logError("CLIENT: signature ", "not properly managed states", "CRITICAL");
            close();
        }
        super.finalize();
    }
}
