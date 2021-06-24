package com.project.collaborativeauthenticationapplication.service.network.messages;

import com.project.collaborativeauthenticationapplication.service.crypto.BigNumber;

public class ExtendMessageMessage extends AbstractMessage{

    private final int weight;
    private final BigNumber message;


    public ExtendMessageMessage(int weight, BigNumber message) {
        this.weight = weight;
        this.message = message;
    }


    public int getWeight() {
        return weight;
    }

    public BigNumber getMessage() {
        return message;
    }
}
