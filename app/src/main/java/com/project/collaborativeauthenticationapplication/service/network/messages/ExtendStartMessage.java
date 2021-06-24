package com.project.collaborativeauthenticationapplication.service.network.messages;

import com.project.collaborativeauthenticationapplication.service.crypto.Point;

import java.util.ArrayList;
import java.util.HashMap;

public class ExtendStartMessage  extends AbstractMessage{


    private HashMap<String, int[]> remotes = new HashMap<>();

    private ArrayList<String> calculatingRemotes = new ArrayList<>();

    private final int threshold;
    private final Point publicKey;
    private final int newIdentifier;
    private final String applicationName;

    public ExtendStartMessage(int threshold, Point publicKey, int newIdentifier, String applicationName) {
        this.threshold = threshold;
        this.publicKey = publicKey;
        this.newIdentifier = newIdentifier;
        this.applicationName = applicationName;
    }


    public void addToRemote(String remote, int[] identifier, boolean calculating){
        remotes.put(remote, identifier);
        if (calculating){
            calculatingRemotes.add(remote);
        }
    }


    public Point getPublicKey() {
        return publicKey;
    }

    public int getThreshold() {
        return threshold;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public HashMap<String, int[]> getRemotes() {
        return remotes;
    }

    public int getNewIdentifier() {
        return newIdentifier;
    }

    public ArrayList<String> getCalculatingRemotes() {
        return calculatingRemotes;
    }
}
