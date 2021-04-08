package com.project.collaborativeauthenticationapplication.service.signature.application;

import android.content.Context;

import com.project.collaborativeauthenticationapplication.service.signature.SignaturePresenter;

public class ThreadedSignatureClient implements SignatureClient{

    SignatureClient client;
    SignatureCoordinator coordinator;

    public ThreadedSignatureClient(SignatureCoordinator coordinator){
        this.coordinator = coordinator;
    }


    @Override
    public void sign(SignatureTask task) {
        if (client == null){
            throw new  IllegalStateException();
        }
        synchronized (client){
            Thread thread  = new Thread(new Runnable() {
                @Override
                public void run() {
                    client.sign(task);
                }
            });
            thread.start();
        }
    }

    @Override
    public void checkInformationAboutCredential(String applicationLoginName, String login, DatabaseInformationRequester requester) {
        if (client == null){
            throw new  IllegalStateException();
        }
        synchronized (client){
            Thread thread  = new Thread(new Runnable() {
                @Override
                public void run() {
                    client.checkInformationAboutCredential(applicationLoginName, login, requester);
                }
            });
            thread.start();
        }
    }

    @Override
    public void generateRandomnessAndCalculateCommitments(RandomnessRequester requester) {
        if (client == null){
            throw new  IllegalStateException();
        }
        synchronized (client){
            Thread thread  = new Thread(new Runnable() {
                @Override
                public void run() {
                    client.generateRandomnessAndCalculateCommitments(requester);
                }
            });
            thread.start();
        }
    }


    @Override
    public int getState() {
        return client.getState();
    }

    @Override
    public void open(Context context) {
        if (client != null){
            throw new IllegalStateException();
        }
        client = new CustomSignatureClient(coordinator);
        synchronized (client){
            Thread thread  = new Thread(new Runnable() {
                @Override
                public void run() {
                    client.open(context);
                }
            });
            thread.start();
        }
    }

    @Override
    public void close() {
        synchronized (client){
            if (client != null){
                Thread thread  = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        client.close();
                        client = null;
                    }
                });
                thread.start();
            }
        }
    }
}
