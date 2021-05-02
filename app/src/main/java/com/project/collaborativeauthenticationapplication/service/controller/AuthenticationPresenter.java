package com.project.collaborativeauthenticationapplication.service.controller;

import android.content.Context;

public interface AuthenticationPresenter {

    void onStartCommand();
    void onStopCommand();


    void statusActive();
    void statusInactive();

    Context getServiceContext();

    void onReceivedNewInvitation();

}
