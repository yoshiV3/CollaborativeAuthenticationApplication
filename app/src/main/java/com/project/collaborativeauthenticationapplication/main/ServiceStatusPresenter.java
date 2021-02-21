package com.project.collaborativeauthenticationapplication.main;

public interface ServiceStatusPresenter {


    boolean isServiceEnabled();

    void stop();
    void start();
}
