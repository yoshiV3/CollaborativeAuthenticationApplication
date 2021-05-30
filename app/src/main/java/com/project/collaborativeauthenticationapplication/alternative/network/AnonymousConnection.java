package com.project.collaborativeauthenticationapplication.alternative.network;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;

import com.project.collaborativeauthenticationapplication.logger.AndroidLogger;
import com.project.collaborativeauthenticationapplication.logger.Logger;


import java.io.IOException;
import java.util.UUID;

public class AnonymousConnection {


    private static final String COMPONENT_NAME    = "Anonymous connection server";
    private static final Logger logger            = new AndroidLogger();



    private static final BluetoothAdapter BLUETOOTH_ADAPTER = BluetoothAdapter.getDefaultAdapter();


    public static final  String SERVICE_NAME  = "Mobile collaborative authentication service"; // the name of the service
    public static final UUID SERVICE_UUID  = UUID.fromString("af0298c6-1b4b-49fc-b1ec-5fb3a4109707"); //randomly generated UUID



    public static final int STATE_IDLE         = 0;
    public static final int STATE_SERVER       = 1;

    private int state = STATE_IDLE;

    private BluetoothServerSocket bluetoothServerSocket = null;

    public AnonymousConnection(){
    }

    public int getState() {
        return state;
    }

    public synchronized void  open() throws IOException {
        logger.logEvent(COMPONENT_NAME, "request open a new bluetooth server", "high");
        if (state == STATE_IDLE){ // check if server is idle, otherwise we cannot open a new server
            try{
                this.bluetoothServerSocket = BLUETOOTH_ADAPTER.listenUsingRfcommWithServiceRecord(SERVICE_NAME, SERVICE_UUID);
                state = STATE_SERVER; // if server is successfully opened, change the state
            } catch(IOException e){
                logger.logError(COMPONENT_NAME, "IO exception during launch of new bluetooth server", "CRITICAL", e.getMessage());
                throw  e;
            }
        } else {
           bluetoothServerSocket.close();
           this.bluetoothServerSocket = BLUETOOTH_ADAPTER.listenUsingRfcommWithServiceRecord(SERVICE_NAME, SERVICE_UUID);
           state = STATE_SERVER; // if server is successfully opened, change the state
        }
    }

    public BluetoothSocket listenForIncomingRequests() throws IOException { // can throw this exception if other thread close the server
        logger.logEvent(COMPONENT_NAME, "listening for incoming requests", "low");
        if(state != STATE_SERVER){
            throw new IllegalStateException("Server is not in the proper state to listen for new requests");
        }
        if (bluetoothServerSocket == null ){
            logger.logError(COMPONENT_NAME, "is in an inconsistent state", "FATAL");
            throw new NullPointerException("cannot listen on a null server socket");
        }
        BluetoothSocket socket =  bluetoothServerSocket.accept();
        logger.logEvent(COMPONENT_NAME, "accepted a new connection", "low", socket.getRemoteDevice().getAddress());
        close();
        return socket;
    }


    public synchronized void close(){
        if(state == STATE_SERVER){
            try {
                bluetoothServerSocket.close();
                bluetoothServerSocket = null;
                state = STATE_IDLE;
                logger.logEvent(COMPONENT_NAME, "server is closed", "low");
            } catch (IOException e) {
                logger.logError(COMPONENT_NAME, "unexpected IO exception during close operation of the server", "unexpected", e.getMessage() );
                bluetoothServerSocket = null;
                state = STATE_IDLE;
            }
        }
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        if (state != STATE_IDLE){
            logger.logError(COMPONENT_NAME, "Unexpected state while removing object: potential resource leakage", "critical", String.valueOf(state));
        }
    }


}
