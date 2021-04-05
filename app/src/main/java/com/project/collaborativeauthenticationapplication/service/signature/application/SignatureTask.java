package com.project.collaborativeauthenticationapplication.service.signature.application;

import com.project.collaborativeauthenticationapplication.service.Requester;
import com.project.collaborativeauthenticationapplication.service.Task;

public class SignatureTask extends Task {

    private final String message;


    public SignatureTask(String message, String applicationName, String login, Requester requester) {
        super(applicationName, login, requester);
        this.message = message;
    }

    public String getMessage(){
        return message;
    }
}
