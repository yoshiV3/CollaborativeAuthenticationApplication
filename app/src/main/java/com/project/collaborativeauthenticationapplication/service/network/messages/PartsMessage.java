package com.project.collaborativeauthenticationapplication.service.network.messages;

import com.project.collaborativeauthenticationapplication.service.crypto.BigNumber;
import com.project.collaborativeauthenticationapplication.service.crypto.Point;

import java.util.ArrayList;

public class PartsMessage extends AbstractMessage {


    private final ArrayList<BigNumber> parts;
    private final Point publicKey;

    public PartsMessage(ArrayList<BigNumber> parts, Point publicKey){
        this.parts = parts;
        this.publicKey = publicKey;
    }

    public ArrayList<BigNumber> getParts() {
        return parts;
    }

    public Point getPublicKey() {
        return publicKey;
    }
}
