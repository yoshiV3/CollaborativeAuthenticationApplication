package com.project.collaborativeauthenticationapplication.service.key.application;

import com.project.collaborativeauthenticationapplication.service.Participant;
import com.project.collaborativeauthenticationapplication.service.key.KeyPresenter;

import java.util.List;

public class ThreadedClient implements Client{


    private final KeyPresenter presenter;

    private Client client;


    public ThreadedClient(KeyPresenter presenter)
    {
        this.presenter = presenter;
        this.client    = new CustomClient(presenter);
    }
    @Override
    public void open() {
        client.open();
    }

    @Override
    public void close() {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                client.close();
                client = null;
            }
        });
        thread.start();
    }

    @Override
    public void submitLoginDetails(String login, String application) {
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

    @Override
    public int getState() {
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
