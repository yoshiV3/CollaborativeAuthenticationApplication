package com.project.collaborativeauthenticationapplication.service.controller;

public interface AuthenticationPresenter {

    void onStartCommand();
    void onStopCommand();


    void statusActive();
    void statusInactive();

}
