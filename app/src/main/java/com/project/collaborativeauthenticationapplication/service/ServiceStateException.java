package com.project.collaborativeauthenticationapplication.service;

public class ServiceStateException extends Exception{

    public ServiceStateException(String currentState, String requiredState)
    {
        super("Service is not in the required state: in state: "+ currentState + " instead of state: " + requiredState);
    }
}
