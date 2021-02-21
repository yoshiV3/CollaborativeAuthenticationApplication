package com.project.collaborativeauthenticationapplication.main;

import com.project.collaborativeauthenticationapplication.service.controller.CustomServiceMonitor;
import com.project.collaborativeauthenticationapplication.service.controller.ServiceMonitor;
import com.project.collaborativeauthenticationapplication.service.network.AndroidBluetoothMonitor;
import com.project.collaborativeauthenticationapplication.service.network.BluetoothMonitor;

public class CustomMainMenuPresenter implements MainMenuPresenter {

    private final BluetoothMonitor bluetoothMonitor = new AndroidBluetoothMonitor();

    private final ServiceMonitor serviceMonitor      = CustomServiceMonitor.getInstance();

    @Override
    public boolean getVisibilityEnableBluetooth() {
        return bluetoothMonitor.isBluetoothAvailable() && !bluetoothMonitor.isBluetoothEnabled();
    }

    @Override
    public boolean getVisibilityEnableService() {
        return !serviceMonitor.isServiceEnabled() && serviceMonitor.isServiceAvailable();
    }

    @Override
    public boolean getVisibilityDisableService() {
        return serviceMonitor.isServiceEnabled() ;
    }
}
