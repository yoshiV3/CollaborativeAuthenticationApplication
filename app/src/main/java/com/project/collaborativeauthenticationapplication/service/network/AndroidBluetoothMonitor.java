package com.project.collaborativeauthenticationapplication.service.network;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;

import java.util.ArrayList;

public class AndroidBluetoothMonitor implements BluetoothMonitor {

    private final BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    @Override
    public boolean isBluetoothAvailable() {
        return bluetoothAdapter != null;
    }

    @Override
    public boolean isBluetoothEnabled() {
        if (!isBluetoothAvailable())
        {
            return false;
        }
        return bluetoothAdapter.isEnabled();
    }



    @Override
    public ArrayList<Device> getPairedDevices() {
        ArrayList<Device> devices = new ArrayList<>();
        for (BluetoothDevice device :bluetoothAdapter.getBondedDevices())
        {
            devices.add(new Device() {
                @Override
                public String getName() {
                    return device.getName();
                }

                @Override
                public String getAddress() {
                    return device.getAddress();
                }
            });
        }
        return devices;
    }
}
