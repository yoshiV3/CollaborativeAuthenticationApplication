package com.project.collaborativeauthenticationapplication.service.network.messages;

public class StartSignMessage extends AbstractMessage{
    private final String name;
    private final String login;
    private final int number;

    private final String localAddress;
    public StartSignMessage(String name, String login, int number, String localAddress){
        this.name = name;
        this.login = login;
        this.number = number;
        this.localAddress = localAddress;
    }

    public String getLocalAddress() {
        return localAddress;
    }

    public String getLogin() {
        return login;
    }

    public String getName() {
        return name;
    }

    public int getNumber() {
        return number;
    }


}
