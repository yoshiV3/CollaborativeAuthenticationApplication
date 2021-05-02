package com.project.collaborativeauthenticationapplication.service.network;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

import com.project.collaborativeauthenticationapplication.logger.AndroidLogger;
import com.project.collaborativeauthenticationapplication.logger.Logger;
import com.project.collaborativeauthenticationapplication.service.concurrency.ThreadPoolSupplier;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.LinkedList;

public class AndroidCommunicationConnection {


    private static final String COMPONENT_NAME    = "Android connection";
    private static final Logger logger            = new AndroidLogger();

    public static final int STATE_IDLE      = 0;
    public static final int STATE_ERROR     = 1;
    public static final int STATE_CONNECTED = 2;
    public static final int STATE_OPENED    = 3;

    LinkedList<byte[]> buffer = new LinkedList<>();



    private  int state = STATE_IDLE;

    private final String address;

    private OutputStream outputStream;
    private InputStream  inputStream;


    private BluetoothSocket connection;

    public AndroidCommunicationConnection(String address) {
        this.address = address;
    }


    public AndroidCommunicationConnection(BluetoothSocket connection){
        this.address    = connection.getRemoteDevice().getAddress();
        this.connection = connection;
        state           = STATE_CONNECTED;
    }


    public int getState() {
        return state;
    }

    public String getAddress() {
        return address;
    }

    public void establishConnectionTo() throws IOException {
        if ( state != STATE_IDLE){
            throw new IllegalStateException();
        }
        BluetoothDevice device = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(address);
        connection = device.createRfcommSocketToServiceRecord(AndroidCommunicationServer.SERVICE_UUID);
        connection.connect();
        state = STATE_CONNECTED;
    }

    public void createIOtStreams() throws IOException {
        synchronized (connection){
            if ( state < STATE_CONNECTED){
                logger.logError(COMPONENT_NAME, "illegal state", "high", String.valueOf(state));
                throw new IllegalStateException();
            }
            if (state == STATE_CONNECTED){
                outputStream = connection.getOutputStream();
                inputStream  = connection.getInputStream();
                startListening();
                state        = STATE_OPENED;
            }
        }
    }

    private void startListening() {
        ThreadPoolSupplier.getSupplier().execute(new Runnable() {
            @Override
            public void run() {
                try{
                    while(true){
                        logger.logEvent(COMPONENT_NAME, "start listening", "low");
                        byte[] buf = new byte[4096];
                        int bytes = inputStream.read(buf);
                        byte[] result = new byte[bytes];
                        System.arraycopy(buf, 0, result, 0, bytes);
                        logger.logEvent(COMPONENT_NAME, "received message", "low");
                        synchronized (buffer){
                            logger.logEvent(COMPONENT_NAME, "add message to buffer", "low");
                            buffer.add(result);
                            buffer.notify();
                        }
                    }
                } catch(IOException e){
                    e.printStackTrace();
                    logger.logError(COMPONENT_NAME, "IO exception", "high", e.getMessage());
                    state = STATE_ERROR;
                    synchronized (buffer){
                        buffer.notify();
                    }
                    logger.logEvent(COMPONENT_NAME, "stopped listening", "low");
                }
            }
        });
    }


    public void closeConnection(){
        logger.logEvent(COMPONENT_NAME, "Closed", "low");
        try {
            BluetoothSocket conn = connection;
            if (conn != null){
                connection.close();
                connection = null;
                state = STATE_IDLE;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void writeToConnection(byte[] message) throws IOException {
        logger.logEvent(COMPONENT_NAME, "write", "normal");
        if ( state != STATE_OPENED){
            throw new IllegalStateException();
        }
        outputStream.write(message);
        outputStream.flush();
    }

    public synchronized void closeIOStreams() throws IOException {
        if ( state != STATE_OPENED){
            throw new IllegalStateException();
        }
        try {
            if (outputStream != null){
                outputStream.close();
                outputStream = null;
            }
            if (inputStream != null){
                inputStream.close();
                inputStream = null;
            }
        } catch (IOException e) {
                state = STATE_ERROR;
                throw  e;
        }
        state = STATE_CONNECTED;
    }
    //blocking call
    public byte[] readFromConnection() throws IOException {
        if (state != STATE_OPENED){
            throw new IllegalStateException();
        }
        synchronized (buffer){
            try{
                logger.logEvent(COMPONENT_NAME, "state of buffer", "low", String.valueOf(buffer.size()));
                while (buffer.size() == 0){
                    if (state == STATE_ERROR && buffer.size() == 0){
                        throw new IOException();
                    }
                    buffer.wait();
                }
                byte[] result = buffer.pop();
                return result;
            } catch (InterruptedException e) {
                e.printStackTrace();
                throw new IOException();
            }

        }

    }


}
