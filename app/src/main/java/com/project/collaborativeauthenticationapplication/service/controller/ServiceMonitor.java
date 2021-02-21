package com.project.collaborativeauthenticationapplication.service.controller;

public interface ServiceMonitor {

    boolean isServiceAvailable();
    boolean isServiceEnabled();
    boolean isServiceActive();

    void subscribeMe(Notifiable me);
    void unsubscribeMe(Notifiable me);
}
