package com.project.collaborativeauthenticationapplication.service.network.messages;

import java.util.ArrayList;

public class ExtendCalculateMessage extends AbstractMessage{

    ArrayList<String> remotes = new ArrayList<>();

    private final int newIdentifier;
    private final String address;
    private final int weight;

    private  int[] weights;

    private final String localAddress;

    public ExtendCalculateMessage(int newIdentifier, String address, int weight, String localAddress) {
        this.newIdentifier = newIdentifier;
        this.address = address;
        this.weight = weight;
        this.localAddress = localAddress;
    }

    public void addRemote(String remote){
        remotes.add(remote);
    }


    public String getLocalAddress() {
        return localAddress;
    }

    public int[] getWeights() {
        return weights;
    }

    public void setWeights(int[] weights) {
        this.weights = weights;
    }

    public int getWeight() {
        return weight;
    }

    public int getNewIdentifier() {
        return newIdentifier;
    }

    public String getAddress() {
        return address;
    }

    public ArrayList<String> getRemotes() {
        return remotes;
    }
}
