package com.project.collaborativeauthenticationapplication.service.key.application.key_generation.distributed_system;

import com.project.collaborativeauthenticationapplication.service.general.IdentifiedParticipant;

public class CustomIdentifiedParticipant implements IdentifiedParticipant {

    private final int identifier;
    private final String address;
    private final int weight;
    private final boolean isLocal;

    public CustomIdentifiedParticipant(int identifier, String address, int weight, boolean isLocal){
        this.identifier = identifier;
        this.address = address;
        this.weight = weight;
        this.isLocal = isLocal;
    }

    @Override
    public int getIdentifier() {
        return identifier;
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public String getAddress() {
        return address;
    }

    @Override
    public void setWeight(int weight) {
    }

    @Override
    public int getWeight() {
        return weight;
    }

    @Override
    public boolean isLocal() {
        return isLocal;
    }
}
