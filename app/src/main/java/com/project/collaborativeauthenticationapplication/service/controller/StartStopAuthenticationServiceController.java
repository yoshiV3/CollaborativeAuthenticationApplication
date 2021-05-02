package com.project.collaborativeauthenticationapplication.service.controller;

public abstract class StartStopAuthenticationServiceController implements AuthenticationServiceController {
    protected abstract void start();
    protected abstract void stop();
    protected abstract void sleep();



}
