package com.project.collaborativeauthenticationapplication.service.network;

import java.util.ArrayList;

public interface BluetoothMonitor {

    boolean isBluetoothAvailable();
    boolean isBluetoothEnabled();


    ArrayList<Device> getPairedDevices();
}
