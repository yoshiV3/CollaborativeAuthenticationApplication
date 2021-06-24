package com.project.collaborativeauthenticationapplication.service.network.messages;

import com.project.collaborativeauthenticationapplication.service.crypto.BigNumber;

import java.util.ArrayList;

public class RefreshShareMessage extends AbstractMessage {

    private final ArrayList<BigNumber> shares;

    public RefreshShareMessage(ArrayList<BigNumber> shares) {
        this.shares = shares;
    }

    public ArrayList<BigNumber> getShares() {
        return shares;
    }
}
