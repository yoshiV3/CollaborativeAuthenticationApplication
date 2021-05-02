package com.project.collaborativeauthenticationapplication.service.network.messages;

public class AbortMessage extends AbstractMessage {

    private final String applicationName;
    private final String login;

    public AbortMessage(String applicationName, String login) {
        this.applicationName = applicationName;
        this.login = login;
    }

    public String getLogin() {
        return login;
    }

    public String getApplicationName() {
        return applicationName;
    }
}
