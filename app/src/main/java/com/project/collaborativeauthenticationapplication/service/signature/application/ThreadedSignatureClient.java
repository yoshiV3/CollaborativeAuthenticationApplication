package com.project.collaborativeauthenticationapplication.service.signature.application;

import com.project.collaborativeauthenticationapplication.service.concurrency.ThreadPoolSupplier;
import com.project.collaborativeauthenticationapplication.service.signature.application.local.RandomnessRequester;
import com.project.collaborativeauthenticationapplication.service.signature.application.local.SignatureTask;


public class ThreadedSignatureClient implements SignatureClient {

    private SignatureClient client;

    public ThreadedSignatureClient(SignatureClient client){
        this.client = client;
    }


    @Override
    public String getAddress() {
        return client.getAddress();
    }

    @Override
    public void sign(SignatureTask task) {
        ThreadPoolSupplier.getSupplier().execute(new Runnable() {
            @Override
            public void run() {
                client.sign(task);
            }
        });

    }

    @Override
    public void generateRandomnessAndCalculateCommitments(RandomnessRequester requester) {
        ThreadPoolSupplier.getSupplier().execute(new Runnable() {
            @Override
            public void run() {
                client.generateRandomnessAndCalculateCommitments(requester);
            }
        });
    }
}
