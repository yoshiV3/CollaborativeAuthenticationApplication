package com.project.collaborativeauthenticationapplication.service.key.application;

import com.project.collaborativeauthenticationapplication.service.Participant;
import com.project.collaborativeauthenticationapplication.service.key.KeyPresenter;

import java.util.List;

public interface Client {

    int getState();

    void open();
    void close();


    void submitLoginDetails(String login, String application);

    List<Participant> getOptions();
    void submitSelection(List<Participant> selection);
    void  submitThreshold(int threshold);


    void run();

}
