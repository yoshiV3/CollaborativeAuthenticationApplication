package com.project.collaborativeauthenticationapplication;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;



public abstract class CustomAuthenticationControllerActivity extends CustomShowActivity {



    protected void onEnableBluetooth()
    {
        Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(enableIntent, getResources().getInteger(R.integer.BluetoothRequestCode));
    }
}
