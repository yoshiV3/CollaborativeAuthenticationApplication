package com.project.collaborativeauthenticationapplication.service;



public class Task {

    private final String    applicationName;
    private final String    login;
    private final Requester requester;


    public Task(String applicationName, String login, Requester requester) {
        this.applicationName = applicationName;
        this.login = login;
        this.requester = requester;
    }



    public String getLogin() {
        return login;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public void done(){
        requester.signalJobDone();
    }
}
