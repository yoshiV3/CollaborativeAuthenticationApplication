package com.project.collaborativeauthenticationapplication.service.key.application;

import android.content.Context;

import com.project.collaborativeauthenticationapplication.service.Participant;
import com.project.collaborativeauthenticationapplication.service.key.KeyGenerationPresenter;

import java.util.List;

public class ThreadedKeyGenerationClient implements keyGenerationClient {


    private final KeyGenerationPresenter presenter;

    private keyGenerationClient client;


    public ThreadedKeyGenerationClient(KeyGenerationPresenter presenter)
    {
        this.presenter = presenter;
        this.client    = new CustomKeyGenerationClient(presenter);
    }
    @Override
    public void open(Context context) {
        client.open(context);
    }

    @Override
    public void close() {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                if (client != null){
                    client.close();
                    client = null;
                }
            }
        });
        thread.start();
    }

    @Override
    public void submitLoginDetails(String login, String application) {
        synchronized (client){
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try{
                        client.submitLoginDetails(login, application);
                    }
                    catch (IllegalArgumentException e){
                        presenter.submitLoginDetailsUnsuccessful();
                    }

                }
            });
            thread.start();
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
    public List<Participant> getOptions() {
        return client.getOptions();
    }

    @Override
    public void submitSelection(List<Participant> selection) {
        client.submitSelection(selection);
    }

    @Override
    public void submitThreshold(int threshold) {
        client.submitThreshold(threshold);
    }

    @Override
    public void run() {
        (new Thread(new ClientThread())).start();
    }


    private class ClientThread implements Runnable
    {
        @Override
        public void run() {
            client.run();
        }
    }
}
