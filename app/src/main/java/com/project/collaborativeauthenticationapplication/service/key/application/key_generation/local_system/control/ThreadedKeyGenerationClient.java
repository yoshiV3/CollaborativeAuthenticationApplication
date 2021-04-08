package com.project.collaborativeauthenticationapplication.service.key.application.key_generation.local_system.control;

import android.content.Context;

import com.project.collaborativeauthenticationapplication.service.Requester;
import com.project.collaborativeauthenticationapplication.service.crypto.BigNumber;
import com.project.collaborativeauthenticationapplication.service.crypto.Point;
import com.project.collaborativeauthenticationapplication.service.key.application.key_generation.distributed_system.KeyGenerationCoordinator;
import com.project.collaborativeauthenticationapplication.service.FeedbackRequester;

import java.util.List;

public class ThreadedKeyGenerationClient implements KeyGenerationClient {



    private KeyGenerationClient client;
    private KeyGenerationCoordinator coordinator;


    public ThreadedKeyGenerationClient(KeyGenerationCoordinator coordinator)
    {
       this.coordinator = coordinator;
    }


    @Override
    public void receiveKeyGenerationSession(Requester requester, KeyGenerationSession session) {
        if (client == null) {
            throw new IllegalStateException();
        }
        client.receiveKeyGenerationSession(requester, session);
    }

    @Override
    public void calculatePartsAndPublicKey(Requester requester, List<BigNumber> parts, Point publicKey) {
        if(client != null){
            synchronized (client){
                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        client.calculatePartsAndPublicKey(requester, parts, publicKey);
                    }
                });
                thread.start();
            }
        }
        else {
            throw  new IllegalStateException();
        }
    }

    @Override
    public void calculateShares(Requester requester, List<List<BigNumber>> parts) {
        if(client != null){
            synchronized (client){
                    Thread thread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            client.calculateShares(requester, parts);
                        }
                    });
                    thread.start();
                }
        }
        else {
            throw  new IllegalStateException();
        }
    }

    @Override
    public void checkCredentials(FeedbackRequester requester, String applicationName, String login) {
        if(client != null){
            synchronized (client){
                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        client.checkCredentials(requester, applicationName, login);
                    }
                });
                thread.start();
            }
        }
        else {
            throw  new IllegalStateException();
        }
    }

    @Override
    public void receiveFinalPublicKey(Requester requester, Point publicKey) {
        client.receiveFinalPublicKey(requester, publicKey);
    }

    @Override
    public void persist(FeedbackRequester requester) {
        if (client != null){
            synchronized (client){
                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        client.persist(requester);
                    }
                });
                thread.start();
            }
        }
        else {
            throw  new IllegalStateException();
        }
    }

    @Override
    public int getState() {
        if (client == null){
            return CustomKeyGenerationClient.STATE_CLOSED;
        }
        return client.getState();
    }

    @Override
    public void open(Context context) {
        if (client != null){
            throw  new IllegalStateException();
        }
        client = new CustomKeyGenerationClient(coordinator);
        client.open(context);
    }

    @Override
    public void close() {
        if( client != null){
            synchronized (client){
                Thread thread = new Thread(new Runnable() {
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
