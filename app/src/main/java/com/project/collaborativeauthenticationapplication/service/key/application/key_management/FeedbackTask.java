package com.project.collaborativeauthenticationapplication.service.key.application.key_management;

import com.project.collaborativeauthenticationapplication.service.Requester;
import com.project.collaborativeauthenticationapplication.service.Task;

public class FeedbackTask  extends Task {

    private boolean wasSuccessful = false;
    private String  message       = "No feedback was set";

    public FeedbackTask(String applicationName, String login, Requester requester) {
        super(applicationName, login, requester);
    }

    public String getMessage() {
        return message;
    }

    public boolean hasBeenSuccessful(){
        return wasSuccessful;
    }

    public void giveFeedback(String message, boolean success){
        this.message         = message;
        this.wasSuccessful   = success;
    }
}
