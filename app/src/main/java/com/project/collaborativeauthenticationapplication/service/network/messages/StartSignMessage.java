package com.project.collaborativeauthenticationapplication.service.network.messages;

public class StartSignMessage extends AbstractMessage{
    private final String name;
    private final int number;

    private final String localAddress;
    public StartSignMessage(String name, int number, String localAddress){
        this.name = name;
        this.number = number;
        this.localAddress = localAddress;
    }

    public String getLocalAddress() {
        return localAddress;
    }


    public String getName() {
        return name;
    }

    public int getNumber() {
        return number;
    }


}
