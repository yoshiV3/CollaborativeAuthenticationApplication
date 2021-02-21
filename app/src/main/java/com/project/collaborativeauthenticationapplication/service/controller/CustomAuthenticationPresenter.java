package com.project.collaborativeauthenticationapplication.service.controller;

import com.project.collaborativeauthenticationapplication.service.network.AndroidBluetoothMonitor;
import com.project.collaborativeauthenticationapplication.service.network.BluetoothMonitor;

public class CustomAuthenticationPresenter implements AuthenticationPresenter{



    private static final CustomAuthenticationPresenter instance = new CustomAuthenticationPresenter();



    private BluetoothMonitor bluetoothMonitor = new AndroidBluetoothMonitor();

    public static CustomAuthenticationPresenter getInstance()
    {
        return instance;
    }


    private CustomAuthenticationPresenter(){}

    @Override
    public void onStartCommand() {
        if (bluetoothMonitor.isBluetoothEnabled())
        {
            CustomAuthenticationServicePool.getInstance().start();
        }
        else
        {
            AuthenticationForegroundService.getInstance().serviceSleeps();
            CustomAuthenticationServicePool.getInstance().sleep();
        }
    }

    @Override
    public void onStopCommand() {
        CustomAuthenticationServicePool.getInstance().stop();
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
}
