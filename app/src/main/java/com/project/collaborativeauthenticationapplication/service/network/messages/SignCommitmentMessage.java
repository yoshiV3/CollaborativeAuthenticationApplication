package com.project.collaborativeauthenticationapplication.service.network.messages;

import com.project.collaborativeauthenticationapplication.service.crypto.Point;

import java.util.ArrayList;

public class SignCommitmentMessage extends AbstractMessage{

    private final ArrayList<Point> e;
    private final ArrayList<Point> d;

    private final String localAddress;

    public SignCommitmentMessage(ArrayList<Point> e, ArrayList<Point> d, String localAddress){
        this.e = e;
        this.d = d;
        this.localAddress = localAddress;
    }

    public ArrayList<Point> getD() {
        return d;
    }

    public ArrayList<Point> getE() {
        return e;
    }

    public String getLocalAddress() {
        return localAddress;
    }
}
