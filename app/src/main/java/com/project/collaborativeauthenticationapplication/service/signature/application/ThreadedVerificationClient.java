package com.project.collaborativeauthenticationapplication.service.signature.application;

import com.project.collaborativeauthenticationapplication.service.FeedbackRequester;
import com.project.collaborativeauthenticationapplication.service.crypto.BigNumber;

public class ThreadedVerificationClient implements VerificationClient{

    VerificationClient client;
    public ThreadedVerificationClient(){
        client = new CustomVerificationClient();
    }

    @Override
    public void verify(BigNumber signature, BigNumber hash, BigNumber message, String applicationName, String login, FeedbackRequester requester) {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                client.verify(signature, hash, message, applicationName, login, requester);
            }
        });
        thread.start();
    }
}
