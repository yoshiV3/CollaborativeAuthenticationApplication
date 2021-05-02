package com.project.collaborativeauthenticationapplication.service.network.messages;

public class ConnectMessage extends AbstractMessage {

    private final String applicationName;
    private final String login;

    public ConnectMessage(String applicationName, String login) {
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
