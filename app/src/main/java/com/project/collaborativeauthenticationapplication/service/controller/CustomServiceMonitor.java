package com.project.collaborativeauthenticationapplication.service.controller;

import com.project.collaborativeauthenticationapplication.logger.AndroidLogger;
import com.project.collaborativeauthenticationapplication.logger.Logger;
import com.project.collaborativeauthenticationapplication.service.network.AndroidBluetoothMonitor;
import com.project.collaborativeauthenticationapplication.service.network.BluetoothMonitor;

import java.util.HashSet;
import java.util.Set;

public class CustomServiceMonitor implements ServiceMonitor{


    private Logger logger = new AndroidLogger();

    private static final CustomServiceMonitor instance = new CustomServiceMonitor();



    public static CustomServiceMonitor getInstance()
    {
        return instance;
    }


    Set<Notifiable> subscribers = new HashSet<>();

    private final BluetoothMonitor bluetoothMonitor = new AndroidBluetoothMonitor();

    private CustomServiceMonitor(){ }


    @Override
    public boolean isServiceAvailable() {
        return bluetoothMonitor.isBluetoothAvailable();
    }

    @Override
    public boolean isServiceEnabled() {
        return CustomAuthenticationServicePool.getInstance().isEnabled();
    }

    @Override
    public boolean isServiceActive() {
        return CustomAuthenticationServicePool.getInstance().isActive();
    }

    @Override
    public void subscribeMe(Notifiable me) {
        subscribers.add(me);
    }

    @Override
    public void unsubscribeMe(Notifiable me) {
        subscribers.remove(me);
    }


    protected void serviceSleeps()
    {
        logger.logEvent("Service Monitor", "service sleeps", "low");
        for (Notifiable notifiable: subscribers)
        {
            notifiable.serviceSleeps();
        }
    }

    protected void serviceActive()
    {
        logger.logEvent("Service Monitor", "service active", "low");
        for (Notifiable notifiable: subscribers)
        {
            notifiable.serviceActive();
        }
    }

    protected void serviceDisabled()
    {
        logger.logEvent("Service Monitor", "service disabled", "low");
        for (Notifiable notifiable: subscribers)
        {
            notifiable.serviceDisabled();
        }
    }




}
