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
        client.close();
        client = null;
    }

    @Override
    public void submitLoginDetails(String login, String application) {
        client.submitLoginDetails(login, application);
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
