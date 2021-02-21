package com.project.collaborativeauthenticationapplication.service;

import com.project.collaborativeauthenticationapplication.service.network.CustomCommunication;
import com.project.collaborativeauthenticationapplication.service.network.Device;

public class CustomParticipant implements Participant {


    private final String name;
    private final String address;

    public static Participant fromDevice(Device device)
    {
        return new CustomParticipant(device.getName(), device.getAddress(), false);
    }

    private CustomCommunication.LOCATION location = CustomCommunication.LOCATION.UNDEFINED;

    private int weight = 1;

    public CustomParticipant(String name, String address, boolean isLocal)
    {
        this.name     =  name;
        this.address  =  address;
        this.location =  isLocal ? CustomCommunication.LOCATION.LOCAL: CustomCommunication.LOCATION.REMOTE;

    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getAddress() {
        return address;
    }

    @Override
    public void setWeight(int weight) {
        this.weight = weight;
    }

    @Override
    public int getWeight() {
        return weight;
    }

    @Override
    public boolean isLocal() {
        return location == CustomCommunication.LOCATION.LOCAL;
    }
}
