package com.project.collaborativeauthenticationapplication.service.controller;

import android.content.Context;

import com.project.collaborativeauthenticationapplication.service.network.AndroidBluetoothMonitor;
import com.project.collaborativeauthenticationapplication.service.network.BluetoothMonitor;

public class CustomAuthenticationPresenter implements AuthenticationPresenter{



    private static final CustomAuthenticationPresenter instance = new CustomAuthenticationPresenter();



    private final BluetoothMonitor bluetoothMonitor = new AndroidBluetoothMonitor();

    public static CustomAuthenticationPresenter getInstance()
    {
        return instance;
    }


    private CustomAuthenticationPresenter(){
    }

    @Override
    public void onStartCommand() {
        if (bluetoothMonitor.isBluetoothEnabled())
        {
            CustomAuthenticationServiceController.getInstance().start();
        }
        else
        {
            AuthenticationForegroundService.getInstance().serviceSleeps();
            CustomAuthenticationServiceController.getInstance().sleep();
        }
    }

    @Override
    public void onStopCommand() {
        CustomAuthenticationServiceController.getInstance().stop();
        AuthenticationForegroundService.getInstance().serviceDisabled();
    }

    @Override
    public void statusActive() {
        AuthenticationForegroundService.getInstance().serviceActive();
    }

    @Override
    public void statusInactive() {
        AuthenticationForegroundService.getInstance().serviceSleeps();
    }

    @Override
    public Context getServiceContext() {
        return AuthenticationForegroundService.getInstance().getContext();
    }

    @Override
    public void onNewSignature(String requester) {
        AuthenticationForegroundService.getInstance().notify("New signature request from "+ requester);
    }
}
