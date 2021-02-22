package com.project.collaborativeauthenticationapplication.service.network;

import com.project.collaborativeauthenticationapplication.service.CustomParticipant;
import com.project.collaborativeauthenticationapplication.service.Participant;

import java.util.ArrayList;

public class CustomCommunication implements Communication{


    private static Communication instance = new CustomCommunication();


    public static Communication getInstance()
    {
        return instance;
    }


    private BluetoothMonitor bluetoothMonitor = new AndroidBluetoothMonitor();

    private CustomCommunication(){}


    @Override
    public ArrayList<Participant> getReachableParticipants() {
        ArrayList<Device> devices           = bluetoothMonitor.getPairedDevices();
        ArrayList<Participant> participants = new ArrayList<>();
        for (Device device: devices)
        {
            participants.add(CustomParticipant.fromDevice(device));
        }
        participants.add(new CustomParticipant("this", "here", true));
        return participants;
    }

    public enum LOCATION {
        REMOTE,
        UNDEFINED,
        LOCAL;
    }
}
