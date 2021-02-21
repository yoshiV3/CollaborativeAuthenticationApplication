package com.project.collaborativeauthenticationapplication.service.controller;

public abstract class StartStopAuthenticationServicePool implements AuthenticationServicePool{
    protected abstract void start();
    protected abstract void stop();
    protected abstract void sleep();



}
