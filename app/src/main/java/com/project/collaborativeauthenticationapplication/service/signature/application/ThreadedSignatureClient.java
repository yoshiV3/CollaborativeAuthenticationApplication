package com.project.collaborativeauthenticationapplication.service.signature.application;

import android.content.Context;

import com.project.collaborativeauthenticationapplication.service.signature.SignaturePresenter;

public class ThreadedSignatureClient implements SignatureClient{

    SignatureClient client;

    public ThreadedSignatureClient(SignaturePresenter presenter){
        client = new CustomSignatureClient(presenter);
    }


    @Override
    public void sign(SignatureTask task) {
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
    public int getState() {
        return client.getState();
    }

    @Override
    public void open(Context context) {
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
