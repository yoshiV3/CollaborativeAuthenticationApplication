package com.project.collaborativeauthenticationapplication.service.key.application;

import android.content.Context;

import com.project.collaborativeauthenticationapplication.service.key.CustomKeyManagementPresenter;
import com.project.collaborativeauthenticationapplication.service.key.KeyManagementPresenter;
import com.project.collaborativeauthenticationapplication.service.key.application.key_management.Task;

public class ThreadedKeyManagementClient implements KeyManagementClient {


    private CustomKeyManagementClient client;

    public ThreadedKeyManagementClient(KeyManagementPresenter keyManagementPresenter) {
        client = new CustomKeyManagementClient(keyManagementPresenter);
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

    @Override
    public void remove(Task task) {
        synchronized (client){
            Thread thread  = new Thread(new Runnable() {
                @Override
                public void run() {
                    client.remove(task) ;
                }
            });
            thread.start();
        }

    }
}
