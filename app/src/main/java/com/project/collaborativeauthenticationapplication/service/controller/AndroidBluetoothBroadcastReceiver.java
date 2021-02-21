package com.project.collaborativeauthenticationapplication.service.controller;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;



public class AndroidBluetoothBroadcastReceiver extends BroadcastReceiver {


    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent != null)
        {
            String action = intent.getAction();
            if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED))
            {
                if(intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1)
                        == BluetoothAdapter.STATE_OFF)
                {
                    CustomAuthenticationServicePool.getInstance().sleep();
                    CustomAuthenticationPresenter.getInstance().statusInactive();
                }
                else if (intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1)
                        == BluetoothAdapter.STATE_ON)
                {
                    CustomAuthenticationServicePool.getInstance().start();
                    CustomAuthenticationPresenter.getInstance().statusActive();
                }
            }

        }
    }
}
