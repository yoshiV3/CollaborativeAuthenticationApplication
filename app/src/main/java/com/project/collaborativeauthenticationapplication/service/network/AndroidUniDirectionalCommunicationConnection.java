package com.project.collaborativeauthenticationapplication.service.network;

import android.bluetooth.BluetoothSocket;

import java.io.InputStream;

public class AndroidUniDirectionalCommunicationConnection {

    private final String address;


    private InputStream inputStream;


    private BluetoothSocket connection;

    public AndroidUniDirectionalCommunicationConnection(String address) {
        this.address = address;
    }


}
