package com.project.collaborativeauthenticationapplication.alternative.network;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

import com.project.collaborativeauthenticationapplication.logger.AndroidLogger;
import com.project.collaborativeauthenticationapplication.logger.Logger;
import com.project.collaborativeauthenticationapplication.service.concurrency.ThreadPoolSupplier;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.util.LinkedList;
import java.util.concurrent.TimeUnit;

public class AndroidConnection {


    private static final byte REQ_STATE_ENDING                = 0;
    private static final byte REQ_STATE_FINALISING            = 1;
    private static final byte REQ_STATE_FINALISING_SYNC       = 2;


    private static final int STATE_DISCONNECTED_UNNAMED           = -2;  //disconnected and no known address
    private static final int STATE_DISCONNECTED                   = -1;  // disconnected with address fixed
    private static final int STATE_DISCONNECTED_UNNAMED_OPENED    = 0;  //disconnected and no known address
    private static final int STATE_DISCONNECTED_OPENED            = 1;  // disconnected with address fixed
    private static final int STATE_CONNECTED                      = 2;  // connected (currently active connection)
    private static final int STATE_ERROR                          = 3;  //something went wrong
    private static final int STATE_CONNECTED_END_READ             = 4;  //controller is ready for this round
    private static final int STATE_CONNECTED_END_READ_UNI         = 5;  // no more data from connection, target does not connect to someone else
    private static final int STATE_CONNECTED_END_READ_MULTI       = 6;  // no more data from connection, target connects to someone else
    private static final int STATE_CONNECTED_END_READ_FINAL       = 7;  // no more data from connection, is done
    private static final int STATE_CONNECTED_END_WRITE            = 8;  // does not have to write in this session, but can write/read in next session
    private static final int STATE_CONNECTED_END_WRITE_REQ        = 9;  // does not have to write in this session, but can write/read in next session, plus request
    private static final int STATE_CONNECTED_END_WRITE_FINAL      = 10;  // does not have to write in this session, but can write/read in next session
    private static final int STATE_CONNECTED_END_WRITE_REQ_FINAL  = 11;  // does not have to write in this session, but can write/read in next session
    private static final int STATE_CONNECTED_INACTIVE_UNI         = 12;  //inactive connection, but targeted not connected with other devices
    private static final int STATE_CONNECTED_INACTIVE_MULTI       = 13; //inactive connection and targeted can be connected with other device(s)


    private static final int STATE_CONNECTED_INACTIVE_UNI_FINAL    = 14;  //inactive connection, but targeted not connected with other devices
    private static final int STATE_CONNECTED_INACTIVE_MULTI_FINAL  = 15;

    private static final int STATE_CONNECTED_INACTIVE_UNI_DISC    = 16;  //inactive connection, but targeted not connected with other devices
    private static final int STATE_CONNECTED_INACTIVE_MULTI_DISC  = 17; //inactive connection and targeted can be connected with other device(s)

    private static final int STATE_END                            = 18; //connection has been terminated


    private static final byte PREAMBLE_NET = 0;
    private static final byte PREAMBLE_MES = 1;

    public static final int MODE_CONTROLLER  = 0;
    public static final int MODE_SLAVE_UNI   = 1;
    public static final int MODE_SLAVE_MULTI = 2;


    private static final byte MESSAGE_END_WRITE             = -1;
    private static final byte MESSAGE_END_WRITE_REQ         = -2;
    private static final byte MESSAGE_END_WRITE_REQ_FINAL   = -3;
    private static final byte MESSAGE_END_WRITE_FINAL       = -4;
    private static final byte MESSAGE_REQ_GRANTED           = -5;
    private static final byte MESSAGE_REQ_REFUSED           = -6;


    private static final byte MESSAGE_REQ_GRANTED_FINAL     = -7;
    private static final byte MESSAGE_REQ_REFUSED_FINAL     = -8;

    private static final byte MESSAGE_DISCONNECT            = -9;
    private static final byte MESSAGE_CONTINUE_CONTROLLER   = -10;
    private static final byte MESSAGE_ACCEPTED              = -11;



    private int state = STATE_DISCONNECTED_UNNAMED;





    private int mode;


    private static final String COMPONENT_NAME    = "ACN Android connection";
    private static final Logger logger            = new AndroidLogger();

    private  String address;

    private BluetoothDevice device;

    private OutputStream outputStream;
    private InputStream inputStream;


    private BluetoothSocket connection;

    private boolean stopped = false;


    private boolean nextController = false;

    public AndroidConnection(String address) {
        this(address, MODE_SLAVE_UNI, false, false);
        logger.logEvent(COMPONENT_NAME, "created new android connection (" +  address +",slave uni, not there, not here", "low");

    }


    protected boolean isNextController() {
        return nextController;
    }

    private boolean mainControllerHere;
    private boolean mainControllerThere;

    private final Object  connectionLOCk = new Object();


    private final Object  writeLock = new Object();
    private Object  readLock  = new Object();
    private final Object  stateLock = new Object();


    LinkedList<byte[]> readBuffer          = new LinkedList<>();
    LinkedList<byte[]> writeBufferMessages = new LinkedList<>();
    LinkedList<byte[]> requestBuffer  = new LinkedList<>();


    public void changeMode(int newMode){
        logger.logEvent(COMPONENT_NAME, "new mode", "low", getAddress());
        logger.logEvent(COMPONENT_NAME, "new mode", "low", String.valueOf(newMode));
        mode = newMode;
    }


    public AndroidConnection(){
        logger.logEvent(COMPONENT_NAME, "created new android connection (any, slave uni, there, not here", "low");
        this.mode = MODE_SLAVE_UNI;
        this.address = "any";
        mainControllerThere = true;
        mainControllerHere  = false;
    }

    private boolean canDisturb = true;

    public  void doNotDisturb(){
        canDisturb = false;
    }

    public void canBeDisturbed(){
        canDisturb = true;
    }

    public boolean isInterruptBlocked(){
        return ! canDisturb;
    }

    public boolean isConnectedWithMainController(){
        return mainControllerThere;
    }


    public AndroidConnection(String address, int mode, boolean mainControllerHere, boolean mainControllerThere){
        logger.logEvent(COMPONENT_NAME, "new connection", "low");
        state = STATE_DISCONNECTED;
        this.address = address;
        this.mode   = mode;
        this.mainControllerThere = mainControllerThere;
        this.mainControllerHere  = mainControllerHere;
    }



    protected void  makeNextController(){
        logger.logEvent(COMPONENT_NAME, "has been picked to act as the controller next", "low", getAddress());
        nextController = true;
    }


    protected void continueToNextStage(){
        synchronized (stateLock) {
            logger.logEvent(COMPONENT_NAME, "continue to next state " + "( "+ getAddress() + " ) ", "high", String.valueOf(state));
            byte[] out = new byte[20];
            switch (state) {
                case STATE_CONNECTED_INACTIVE_MULTI:
                    if (nextController) {
                            logger.logEvent(COMPONENT_NAME, "continue to next state " + "("+ getAddress() + ")", "high", "controller granted");
                            try {
                                changeMode(MODE_SLAVE_MULTI);
                                out[1] = MESSAGE_REQ_GRANTED;
                                outputStream.write(out);
                            } catch (IOException e) {
                                e.printStackTrace();
                                logger.logError(COMPONENT_NAME, e.getMessage(), "critical", getAddress());
                                Network.getInstance().closeAllConnections();
                            }
                    } else {
                        logger.logEvent(COMPONENT_NAME, "continue to next state " + "("+ getAddress() + ")", "high", "controller refused");
                        state = STATE_CONNECTED_INACTIVE_MULTI_DISC;
                        disconnect(MESSAGE_REQ_REFUSED);
                    }
                    break;
                case  STATE_CONNECTED_INACTIVE_MULTI_FINAL:
                    try {
                        if (nextController){
                            logger.logEvent(COMPONENT_NAME, "is next controller (final)", "low", getAddress());
                            out[1] = MESSAGE_REQ_GRANTED_FINAL;
                        } else {
                            out[1] = MESSAGE_REQ_REFUSED_FINAL;
                        }
                        outputStream.write(out);
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        logger.logEvent(COMPONENT_NAME, "connection ended", "low", getAddress());
                        state = STATE_END;
                        closeStream();
                        Network.getInstance().release(getAddress());
                    }
                    break;
                case STATE_CONNECTED_INACTIVE_UNI:
                    logger.logEvent(COMPONENT_NAME, "go in disconnected state", "low", getAddress());
                    state = STATE_CONNECTED_INACTIVE_UNI_DISC;
                    disconnect(MESSAGE_DISCONNECT);
                    break;
                case STATE_CONNECTED_INACTIVE_MULTI_DISC:
                case STATE_CONNECTED_INACTIVE_UNI_DISC:
                    logger.logEvent(COMPONENT_NAME, "continue in disconnected state", "low", getAddress());
                    break;
                case STATE_END:
                    logger.logEvent(COMPONENT_NAME, "connection has already ended", "low", getAddress());
                    break;
                default:
                    logger.logError(COMPONENT_NAME, "illegal state", "high", String.valueOf(state));
                    throw new IllegalStateException();
            }
        }
    }


    protected void disconnect(){
        logger.logEvent(COMPONENT_NAME, "disconnecting (multi disc", "low", getAddress());
        synchronized (stateLock){
            state = STATE_CONNECTED_INACTIVE_MULTI_DISC;
        }
        disconnect(AndroidConnection.MESSAGE_DISCONNECT);
    }

    protected void disconnect(byte message){
        byte[] out = new byte[20];
        logger.logEvent(COMPONENT_NAME, "disconnected from connection", "high", getAddress());
        BluetoothSocket sock = connection;
        try {
            out[1] = message;
            outputStream.write(out);
        } catch (IOException e) {
            e.printStackTrace();
            logger.logError(COMPONENT_NAME, e.getMessage(), "critical", getAddress());
            Network.getInstance().closeAllConnections();
        }
        try {
            if (sock != null){
                connection.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            connection = null;
        }
    }

    protected void reconnect() throws IOException {
        if (state == STATE_CONNECTED_INACTIVE_MULTI ){
            logger.logEvent(COMPONENT_NAME, "reconnecting to controller", "low", getAddress());
            byte[] out = new byte[20];
            out[1] = MESSAGE_CONTINUE_CONTROLLER;
            outputStream.write(out);

            state = STATE_CONNECTED;

            waitForIO();
        }
        else if (! (state == STATE_CONNECTED_INACTIVE_UNI_DISC  || state == STATE_CONNECTED_INACTIVE_MULTI_DISC)){
            logger.logError(COMPONENT_NAME, "illegal state:", "high", String.valueOf(state));
            throw new IllegalStateException();
        } else {
            logger.logEvent(COMPONENT_NAME, "reconnection with connection", "high", getAddress());
            int times = 0;
            boolean success = false;
            BluetoothSocket con = null;
            while (! success) {
                try {
                    con = device.createRfcommSocketToServiceRecord(AnonymousConnection.SERVICE_UUID);
                    con.connect();
                    success = true;
                    logger.logEvent(COMPONENT_NAME, "could connect with device", "low", getAddress());
                } catch (IOException e) {
                    logger.logError(COMPONENT_NAME, "could not connect with device", "low", getAddress());
                    times += 1;
                    if (times > 4) {
                        throw e;
                    }
                    try {
                        TimeUnit.MICROSECONDS.sleep(500);
                    } catch (InterruptedException interruptedException) {
                        e.printStackTrace();
                    }
                }
            }
            if (mainControllerHere) {
                logger.logEvent(COMPONENT_NAME, "main controller", getAddress());
                mode = MODE_CONTROLLER;
            } else if (mainControllerThere) {
                logger.logEvent(COMPONENT_NAME, "temp slave uni", getAddress());
                mode = MODE_SLAVE_UNI;
            } else {
                logger.logEvent(COMPONENT_NAME, "temp controller", getAddress());
                mode = MODE_CONTROLLER;
            }
            setConnection(con);
            waitForIO();
        }
    }





    public void open(){
        logger.logEvent(COMPONENT_NAME, "open android connection request", "low", getAddress());
        synchronized (stateLock){
            logger.logEvent(COMPONENT_NAME, "lock granted in open", "low", getAddress());
            if (state == STATE_DISCONNECTED_UNNAMED || state == STATE_DISCONNECTED){ //only in those states, otherwise ignore
                logger.logEvent(COMPONENT_NAME, "opened connection: waiting", "low", getAddress());
                state = state +2;
                waitForIO();
            }
        }
    }

    protected void waitForIO(){
        logger.logEvent(COMPONENT_NAME, "launch waiting for a connection: ", "low", getAddress());
        ThreadPoolSupplier.getSupplier().execute(new Runnable() {
            @Override
            public void run() {
                synchronized (connectionLOCk){
                    logger.logEvent(COMPONENT_NAME, "start waiting for a connection: ", "low", getAddress());
                    while (state != STATE_CONNECTED && state != STATE_ERROR && state != STATE_END){
                        try {
                            logger.logEvent(COMPONENT_NAME, "waiting for a connection: ", "low", getAddress());
                            connectionLOCk.wait();
                            logger.logEvent(COMPONENT_NAME, "received notifications on connection lock", "low", getAddress());
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                            logger.logError(COMPONENT_NAME, "wait time out", "low");
                            throw new UnsupportedOperationException();

                        }
                    }
                }
                if (state == STATE_CONNECTED){
                    logger.logEvent(COMPONENT_NAME, "opened connection: start reading and writing", "low", getAddress());
                    startListening();
                    startWriting();
                } else {
                    logger.logEvent(COMPONENT_NAME, "stopped waiting for IO", "low", getAddress());
                }

            }
        });
    }


    Object pushLock = new Object();

    public void push(){
        logger.logEvent(COMPONENT_NAME, "push", "high", getAddress());
        synchronized (pushLock){
            requestBuffer.add(new byte[] {REQ_STATE_ENDING});
        }
        pushEnd();
    }

    private void pushEnd() {
        logger.logEvent(COMPONENT_NAME, "push to end", "high", getAddress());
        synchronized (writeLock){
            writeBufferMessages.add(null);
            writeLock.notify();
        }
    }

    public void pushForFinal(){
        logger.logEvent(COMPONENT_NAME, "push for final", "high", getAddress());
        synchronized (pushLock){
            requestBuffer.add(new byte[] {REQ_STATE_FINALISING});
        }
        pushEnd();
    }

    final Object syncLock = new Object();
    public void pushForFinalSynchronous() {
        logger.logEvent(COMPONENT_NAME, "final end synchronous", "low", getAddress());
        synchronized (pushLock){
            requestBuffer.add(new byte[] {REQ_STATE_FINALISING_SYNC});
        }
        pushEnd();
        synchronized (syncLock){
            try {
                syncLock.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }


    public void pushToCoordinator(){
        logger.logEvent(COMPONENT_NAME, "push to coordinator", "high", getAddress());
        if (mainControllerThere){
            logger.logEvent(COMPONENT_NAME, "ending connection session", "low", getAddress());
            synchronized (pushLock){
                requestBuffer.add(new byte[] {REQ_STATE_ENDING});
            }

        } else {
            logger.logEvent(COMPONENT_NAME, "final connection session", "low", getAddress());
            synchronized (pushLock){
                requestBuffer.add(new byte[] {REQ_STATE_FINALISING});
            }
        }
        pushEnd();
    }


    public void closeNoRelease(){
        logger.logEvent(COMPONENT_NAME, "close connection (no release)", "low", getAddress());
        synchronized (stateLock){
            state = STATE_END;
        }
        try {
            connection.close();
            synchronized (connectionLOCk){
                connectionLOCk.notify();
                logger.logEvent(COMPONENT_NAME, "notification send: close", "low", getAddress());
            }
        } catch (IOException | NullPointerException e) {
            e.printStackTrace();
        } finally {
            synchronized (connectionLOCk){
                connectionLOCk.notify();
                logger.logEvent(COMPONENT_NAME, "notification send: close", "low", getAddress());
            }
            pushEnd();
            connection = null;
        }
    }

    public void close(){
        logger.logEvent(COMPONENT_NAME, "close connection", "low", getAddress());
        synchronized (stateLock){
            state = STATE_END;
        }
        try {
            connection.close();
            synchronized (connectionLOCk){
                connectionLOCk.notify();
            }
        } catch (IOException | NullPointerException e) {
            e.printStackTrace();
        } finally {
            pushEnd();
            connection = null;
            Network.getInstance().release(getAddress());
        }

        /**
        synchronized (lock){
            if (connection != null){
                try {
                    connection.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                connection = null;
            } else {
                stopped = true;
            }
            lock.notify();
        }
         **/
    }

    protected void setConnection(BluetoothSocket connection){
        logger.logEvent(COMPONENT_NAME, "set connection", "low", getAddress());
        synchronized (connectionLOCk) {
            this.connection = connection;
            try {
                if (state == STATE_DISCONNECTED_OPENED || state == STATE_DISCONNECTED_UNNAMED_OPENED ){
                    device       = connection.getRemoteDevice();
                    address      = device.getAddress();
                } else {
                    if (!address.equals(connection.getRemoteDevice().getAddress())){
                        logger.logEvent(COMPONENT_NAME, "wrong name", "high", getAddress() + ", " + connection.getRemoteDevice().getAddress());
                        throw new IllegalArgumentException();
                    }
                }
                outputStream = connection.getOutputStream();
                inputStream  = connection.getInputStream();
                synchronized (stateLock){
                    state = STATE_CONNECTED;
                    logger.logEvent(COMPONENT_NAME, "connection connected", "low", getAddress());
                }
                nextController = false;
            } catch (IOException e) {
                e.printStackTrace();
                state = STATE_ERROR;
            }
            logger.logEvent(COMPONENT_NAME, "notify that connection has been created", "low", getAddress());
            connectionLOCk.notify();
        }
        logger.logEvent(COMPONENT_NAME, "done set connection", "low", getAddress());
    }

    public boolean isConnected(){
        if (connection != null ){
            return connection.isConnected();
        }
        return false;
    }






    private final Object  startLock = new Object();



    boolean isWriting  = false;

    protected void startWriting(){
        logger.logEvent(COMPONENT_NAME, "start writing"+ getAddress(), "low");
        ThreadPoolSupplier.getSupplier().execute(new Runnable() {
            @Override
            public void run() {
                boolean strt = false;
                synchronized (startLock){
                    strt = !isWriting;
                }
                if (strt) {
                    isWriting = true;
                    boolean writeEnabled = true;
                    try {
                        while (writeEnabled) {
                            byte[] out = new byte[20];
                            logger.logEvent(COMPONENT_NAME, "waiting before clear to send again", "low", getAddress());
                            try {
                                TimeUnit.MICROSECONDS.sleep(10);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            synchronized (writeLock) {
                                while (writeBufferMessages.size() == 0) {
                                    logger.logEvent(COMPONENT_NAME, "no messages to send: waiting", "low", getAddress());
                                    try {
                                        writeLock.wait();
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                }
                                logger.logEvent(COMPONENT_NAME, "sending message", "low", getAddress());
                                byte[] mes = writeBufferMessages.pop();
                                if (mes == null) {
                                    byte req = -1;
                                    synchronized (pushLock) {
                                        req = requestBuffer.pop()[0];
                                    }
                                    synchronized (stateLock) {
                                        switch (state) {
                                            case STATE_CONNECTED:
                                                switch (req) {
                                                    case REQ_STATE_ENDING:
                                                        if (mode == MODE_SLAVE_MULTI) {
                                                            logger.logEvent(COMPONENT_NAME, "requesting to be controller (ending, connected)", "high", getAddress());
                                                            out[1] = MESSAGE_END_WRITE_REQ;
                                                            outputStream.write(out);
                                                            outputStream.flush();
                                                            state = STATE_CONNECTED_END_WRITE_REQ;
                                                        } else {
                                                            logger.logEvent(COMPONENT_NAME, "end of write session: send message", "high", getAddress());
                                                            out[1] = MESSAGE_END_WRITE;
                                                            outputStream.write(out);
                                                            outputStream.flush();
                                                            state = STATE_CONNECTED_END_WRITE;
                                                        }
                                                        break;
                                                    case REQ_STATE_FINALISING:
                                                        if (mode == MODE_SLAVE_MULTI) {
                                                            logger.logEvent(COMPONENT_NAME, "requesting to be controller", "high", getAddress());
                                                            out[1] = MESSAGE_END_WRITE_REQ_FINAL;
                                                            outputStream.write(out);
                                                            outputStream.flush();
                                                            state = STATE_CONNECTED_END_WRITE_REQ_FINAL;
                                                        } else {
                                                            logger.logEvent(COMPONENT_NAME, "finalising writing", "low", getAddress());
                                                            out[1] = MESSAGE_END_WRITE_FINAL;
                                                            outputStream.write(out);
                                                            outputStream.flush();
                                                            state = STATE_CONNECTED_END_WRITE_FINAL;
                                                        }
                                                        break;
                                                    case REQ_STATE_FINALISING_SYNC:
                                                        logger.logEvent(COMPONENT_NAME, "finalising writing (synchronous)", "low", getAddress());
                                                        out[1] = MESSAGE_END_WRITE_FINAL;
                                                        outputStream.write(out);
                                                        outputStream.flush();
                                                        state = STATE_CONNECTED_END_WRITE_FINAL;
                                                        synchronized (syncLock) {
                                                            syncLock.notify();
                                                        }
                                                        break;
                                                }
                                                writeEnabled = false;
                                                logger.logEvent(COMPONENT_NAME, "write disabled", "low", "from connected");
                                                break;
                                            case STATE_CONNECTED_END_READ_FINAL:
                                                switch (req) {
                                                    case REQ_STATE_ENDING:
                                                        //TODO
                                                        throw new UnsupportedOperationException();
                                                    case REQ_STATE_FINALISING:
                                                        logger.logEvent(COMPONENT_NAME, "finalising connection (end read final)", "low", getAddress());
                                                        if (mode == MODE_SLAVE_MULTI){
                                                            out[1] = MESSAGE_END_WRITE_REQ_FINAL;
                                                            outputStream.write(out); //should already have stopped reading
                                                            outputStream.flush();
                                                            writeEnabled = false;
                                                        } else {
                                                            out[1] = MESSAGE_END_WRITE_FINAL;
                                                            outputStream.write(out); //should already have stopped reading
                                                            outputStream.flush();
                                                            state = STATE_END;
                                                            inputStream.close();
                                                            outputStream.close();
                                                            connection.close();
                                                            Network.getInstance().release(getAddress());
                                                            writeEnabled = false;
                                                        }

                                                        break;
                                                    case REQ_STATE_FINALISING_SYNC:
                                                        writeEnabled = false;
                                                        logger.logEvent(COMPONENT_NAME, "finalising connection (end read final, sync)", "low", getAddress());
                                                        out[1] = MESSAGE_END_WRITE_FINAL;
                                                        outputStream.write(out); //should already have stopped reading
                                                        outputStream.flush();
                                                        state = STATE_END;
                                                        Network.getInstance().release(getAddress());
                                                        inputStream.close();
                                                        outputStream.close();
                                                        connection.close();
                                                        Network.getInstance().release(getAddress());
                                                        synchronized (syncLock) {
                                                            syncLock.notify();
                                                        }

                                                }
                                                break;
                                            case STATE_CONNECTED_END_READ_UNI:
                                                //TODO
                                                logger.logError(COMPONENT_NAME, "STATE_CONNECTED_END_READ_UNI", "critical");
                                                throw new UnsupportedOperationException();
                                            case STATE_CONNECTED_END_READ_MULTI:
                                                if (mode != MODE_CONTROLLER) {
                                                    throw new IllegalStateException();
                                                }
                                                switch (req) {
                                                    case REQ_STATE_ENDING:
                                                        logger.logEvent(COMPONENT_NAME, "become inactive (not final)", "low", getAddress());
                                                        state = STATE_CONNECTED_INACTIVE_MULTI;
                                                        break;
                                                    case REQ_STATE_FINALISING:
                                                        logger.logEvent(COMPONENT_NAME, "become inactive (final)", "low", getAddress());
                                                        state = STATE_CONNECTED_INACTIVE_MULTI_FINAL;
                                                    case REQ_STATE_FINALISING_SYNC:
                                                        logger.logEvent(COMPONENT_NAME, "become inactive (final) (sync)", "low", getAddress());
                                                        state = STATE_CONNECTED_INACTIVE_MULTI_FINAL;
                                                        synchronized (syncLock) {
                                                            syncLock.notify();
                                                        }
                                                }
                                                logger.logEvent(COMPONENT_NAME, "making inactive connection", "low", getAddress());
                                                Network.getInstance().makeInactiveMulti(getAddress());
                                                writeEnabled = false;
                                                logger.logEvent(COMPONENT_NAME, "write disabled", "low", "from STATE_CONNECTED_END_READ_MULTI");
                                                break;
                                            case STATE_CONNECTED_END_READ:
                                                logger.logEvent(COMPONENT_NAME, "requesting to be controller", "high", getAddress());
                                                out[1] = MESSAGE_END_WRITE_REQ;
                                                outputStream.write(out);
                                                outputStream.flush();
                                                state = STATE_CONNECTED_INACTIVE_MULTI;
                                                writeEnabled = false;
                                                break;
                                            case STATE_ERROR:
                                                logger.logEvent(COMPONENT_NAME, "state error: stop writing", "low", getAddress());
                                                break;
                                            default:
                                                logger.logEvent(COMPONENT_NAME, "state error: stop writing", "low", String.valueOf(state));
                                                throw new IllegalStateException();
                                        }
                                    }
                                } else {
                                    logger.logEvent(COMPONENT_NAME, "output", "low", getAddress());
                                    logger.logEvent(COMPONENT_NAME, "output", "low", String.valueOf(mes.length));
                                    outputStream.write(mes);
                                    outputStream.flush();
                                }
                            }
                        }

                    } catch (IOException e) {
                        Network.getInstance().release(getAddress());
                        switch (state) {
                            case STATE_END:
                                logger.logEvent(COMPONENT_NAME, "stopped writing after closure of the connection", "high");
                                break;
                            case STATE_ERROR:
                                logger.logError(COMPONENT_NAME, "stopped writing due to error", "critical");
                                break;
                            case STATE_CONNECTED_END_WRITE:
                                logger.logEvent(COMPONENT_NAME, "do not know whether error occurred", "high");
                                break;
                            case STATE_CONNECTED_INACTIVE_UNI:
                                logger.logEvent(COMPONENT_NAME, "stopped writing due to inactivity ", "low");
                                break;
                            default:
                                logger.logError(COMPONENT_NAME, "connection closed in bad state ", "critical", getAddress());
                                state = STATE_ERROR;
                        }
                    }
                    synchronized (startLock){
                        isWriting = false;
                    }
                }
            }
        });

            /**
                @Override
                public void run() {
                    try{
                        while(!stopped){
                            synchronized (lock){
                                while (connection == null){
                                    lock.wait();
                                    if (stopped){
                                        throw new IOException();
                                    }
                                    lock.notify();
                                }
                            }
                            logger.logEvent(COMPONENT_NAME, "start writing", "low");
                            byte[] mes = null;
                            synchronized (writeBuffer) {
                                while (writeBuffer.size() == 0) {
                                    writeBuffer.wait();
                                }
                                mes = writeBuffer.getFirst();
                            }
                            logger.logEvent(COMPONENT_NAME, "write message", "low");
                            synchronized (con){
                                outputStream.write(mes);
                            }
                        }
                    } catch(IOException e){
                        e.printStackTrace();
                        logger.logError(COMPONENT_NAME, "IO exception (could be normal behavior)", "high", e.getMessage());
                        connection = null;
                        synchronized (readBuffer){
                            readBuffer.notify();
                        }
                        logger.logEvent(COMPONENT_NAME, "stopped listening", "low");
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            });
              **/
    }



    private void  handleNetwork(){
        ThreadPoolSupplier.getSupplier().execute(new Runnable() {
            @Override
            public void run() {
                logger.logEvent(COMPONENT_NAME, "handling network after end read", "low", getAddress());
                byte[] buf = new byte[40];
                try {
                    boolean readEnabled = true;

                    while (readEnabled){
                        logger.logEvent(COMPONENT_NAME, "loop started again (handling network) ", "low", getAddress());
                        int bytes = inputStream.read(buf);
                        logger.logEvent(COMPONENT_NAME, "received message to handle", "high", getAddress());
                        if (buf[0] != PREAMBLE_NET){
                            logger.logError(COMPONENT_NAME, "received message in network state", "high", getAddress());
                            logger.logError(COMPONENT_NAME, "received message in network state", "high", String.valueOf(buf[0]));
                            throw new IllegalArgumentException();
                        }
                        switch (buf[1]){
                            case MESSAGE_ACCEPTED:
                                readEnabled = false;
                                logger.logEvent(COMPONENT_NAME, "stopped handling network (other controller)", "high", getAddress());
                                break;
                            case MESSAGE_CONTINUE_CONTROLLER:
                                logger.logEvent(COMPONENT_NAME, "got request to continue: switching back to controller", "high", getAddress());
                                changeMode(MODE_CONTROLLER);
                                synchronized (stateLock){
                                    state = STATE_CONNECTED;
                                }
                                byte[] out =  new byte[20];
                                logger.logEvent(COMPONENT_NAME, "accepting offer", "low", getAddress());
                                out[1] = MESSAGE_ACCEPTED;
                                outputStream.write(out);
                                readEnabled = false;
                                logger.logEvent(COMPONENT_NAME, "stopped handling network", "high", getAddress());
                                Network.getInstance().makeActive(getAddress());
                                waitForIO();
                                break;
                            case MESSAGE_DISCONNECT:
                                synchronized (stateLock){
                                    state = STATE_CONNECTED_INACTIVE_MULTI_DISC;
                                }
                                logger.logEvent(COMPONENT_NAME, "disconnected", "high", getAddress());
                                readEnabled = false;
                                logger.logEvent(COMPONENT_NAME, "stopped handling network", "high", getAddress());
                                closeStream();
                                Network.getInstance().makeInactive(getAddress());
                                Network.getInstance().handleDisconnect();
                                break;
                            case MESSAGE_REQ_GRANTED:
                                logger.logEvent(COMPONENT_NAME, "been granted controller", "high", getAddress());
                                changeMode(MODE_CONTROLLER);
                                Network.getInstance().makeInactive(getAddress());
                                Network.getInstance().justBecameController();
                                break;
                            case MESSAGE_REQ_GRANTED_FINAL:
                                logger.logEvent(COMPONENT_NAME, "granted controller: final", "high", getAddress());
                                changeMode(MODE_CONTROLLER);
                                synchronized (stateLock){
                                    state= STATE_END;
                                }
                                readEnabled = false;
                                logger.logEvent(COMPONENT_NAME, "stopped handling network", "high", getAddress());
                                logger.logEvent(COMPONENT_NAME, "closing connection", "high", getAddress());
                                closeStream();
                                Network.getInstance().release(getAddress());
                                Network.getInstance().justBecameController();
                                break;
                            case MESSAGE_REQ_REFUSED:
                                logger.logEvent(COMPONENT_NAME, "refused controller", "high", getAddress());
                                synchronized (stateLock){
                                    state = STATE_CONNECTED_INACTIVE_MULTI_DISC;
                                }
                                logger.logEvent(COMPONENT_NAME, "disconnected", "high", getAddress());
                                readEnabled = false;
                                Network.getInstance().makeInactive(getAddress());
                                logger.logEvent(COMPONENT_NAME, "stopped handling network", "high", getAddress());
                                closeStream();
                                Network.getInstance().startAnonymousConnection();
                                break;
                            case MESSAGE_REQ_REFUSED_FINAL:
                                logger.logEvent(COMPONENT_NAME, "refused controller: final", "high", getAddress());
                                changeMode(MODE_CONTROLLER);
                                synchronized (stateLock){
                                    state= STATE_END;
                                }
                                readEnabled = false;
                                logger.logEvent(COMPONENT_NAME, "stopped handling network", "high", getAddress());
                                logger.logEvent(COMPONENT_NAME, "closing connection", "high", getAddress());
                                closeStream();
                                Network.getInstance().release(getAddress());
                                Network.getInstance().startAnonymousConnection();
                                break;
                            default:
                                logger.logError(COMPONENT_NAME, "handle network cannot handle this request", "critical", getAddress());
                                logger.logError(COMPONENT_NAME, "handle network cannot handle this request", "critical", String.valueOf(buf[1]));
                                throw new IllegalStateException();

                        }
                    }
                } catch (IOException e) {
                    synchronized (stateLock){
                        if (state != STATE_CONNECTED_INACTIVE_UNI_DISC && state != STATE_CONNECTED_INACTIVE_MULTI_DISC && state != STATE_END){
                            logger.logError(COMPONENT_NAME, "IO exception during network maintenance", "low", getAddress());
                            logger.logError(COMPONENT_NAME, "IO exception during network maintenance", "low", String.valueOf(state));
                            e.printStackTrace();
                            state = STATE_ERROR;
                            Network.getInstance().closeAllConnections();
                        }
                    }
                }
            }
        });

    }

    private void closeStream() {
        try {
            outputStream.close();
            inputStream.close();
            connection.close();
        } catch (IOException io){
            logger.logError(COMPONENT_NAME, "error during close"+ io.getMessage(), "low", getAddress());
        }
    }

    private LinkedList<byte []> parsedMessageBuffer = new LinkedList<>();

    private int byteToInt(byte b){
        int result = (int) b;
        if (result < 0){
            result = result +256;
        }
        return result;
    }

    private void networkParseMessage(byte[] buffer, int bytes){
        logger.logEvent(COMPONENT_NAME, "parsing", "low");
        parsedMessageBuffer.clear();
        int unhandled = bytes;
        int index     = 0; //position in original buffer
        while (unhandled != 0){
            logger.logEvent(COMPONENT_NAME, "unhandled", "low", String.valueOf(unhandled));
            if (buffer[index] == PREAMBLE_NET){
                logger.logEvent(COMPONENT_NAME, "parsed network", "low");
                byte[] buf = new byte[20];
                index = index + 1;
                buf[1] = buffer[index];
                index  = index + 1;
                unhandled = unhandled -20;
                index = index + 20;
                parsedMessageBuffer.add(buf);
            } else if (buffer[index] ==  PREAMBLE_MES){
                logger.logEvent(COMPONENT_NAME, "parse message", "low");
                byte[] buf       = new byte[4096];
                buf[0]           = PREAMBLE_MES;
                int internalBuf  = 1;
                boolean end = false;
                while (!end){
                    index            = index + 1;
                    logger.logEvent(COMPONENT_NAME, "parse loop", "low");
                    int l = byteToInt(buffer[index]) +1;
                    logger.logEvent(COMPONENT_NAME, "length part", "low", String.valueOf(l));
                    index += 1;
                    if ((l != 256) || (bytes < index + 256) || (buffer[index + 256] != 2) ){
                        end = true; //stop
                    }
                    System.arraycopy(buffer,  index, buf, internalBuf, l);
                    internalBuf = internalBuf +l;
                    index = index + l ;
                    unhandled = unhandled - l - 2;
                }
                parsedMessageBuffer.add(buf);
            }
        }
    }


    boolean isListening = false;
    protected void startListening() {
        boolean strt = false;
        synchronized (startLock){
            strt = !isListening;
        }
        if (strt) {
            isListening = true;
            ThreadPoolSupplier.getSupplier().execute(new Runnable() {
                @Override
                public void run() {
                    boolean readEnabled = true;
                    while (readEnabled) {
                        logger.logEvent(COMPONENT_NAME, "start listening (loop)", "low", getAddress());
                        byte[] buff = new byte[4096];
                        try {
                            int bytes = inputStream.read(buff);
                            networkParseMessage(buff, bytes);
                            for (byte[] buf : parsedMessageBuffer) {
                                logger.logEvent(COMPONENT_NAME, "new message", "low", getAddress());
                                logger.logEvent(COMPONENT_NAME, "new message", "low", String.valueOf(bytes));
                                if (buf[0] == PREAMBLE_NET) {
                                    switch (buf[1]) {
                                        case MESSAGE_END_WRITE_FINAL:
                                            synchronized (stateLock) {
                                                switch (state) {
                                                    case STATE_CONNECTED:
                                                        logger.logEvent(COMPONENT_NAME, "received request to end this connection", "low", getAddress());
                                                        state = STATE_CONNECTED_END_READ_FINAL;
                                                        readEnabled = false;
                                                        handleNetwork();
                                                        break;
                                                    case STATE_CONNECTED_END_WRITE_FINAL:
                                                        readEnabled = false;
                                                        logger.logEvent(COMPONENT_NAME, "finalising connection, (final write)", "low", getAddress());
                                                        state = STATE_END;
                                                        try {
                                                            logger.logEvent(COMPONENT_NAME, "ending connection", "low", getAddress());
                                                            inputStream.close();
                                                            outputStream.close();
                                                            connection.close();
                                                            Network.getInstance().release(getAddress());
                                                        } catch (IOException e) {
                                                            logger.logError(COMPONENT_NAME, "IO exception while closing", "high", getAddress());
                                                            Network.getInstance().release(getAddress());
                                                        }
                                                        break;
                                                    case STATE_CONNECTED_END_WRITE_REQ_FINAL:
                                                        logger.logEvent(COMPONENT_NAME, "end read", "low", getAddress());
                                                        state = STATE_CONNECTED_INACTIVE_MULTI_FINAL;
                                                        handleNetwork();
                                                        logger.logEvent(COMPONENT_NAME, "read disabled", "low", "from STATE_CONNECTED_END_WRITE_REQ_FINAL," + getAddress());
                                                        readEnabled = false;
                                                        break;
                                                    default:
                                                        logger.logError(COMPONENT_NAME, "illegal state (read)", "low", getAddress());
                                                        logger.logError(COMPONENT_NAME, "illegal state (read)", "low", String.valueOf(state));
                                                        throw new IllegalStateException();
                                                }
                                            }
                                            break;
                                        case MESSAGE_END_WRITE_REQ:
                                            synchronized (stateLock) {
                                                switch (state) {
                                                    case STATE_CONNECTED:
                                                        state = STATE_CONNECTED_END_READ_MULTI;
                                                        readEnabled = false;
                                                        logger.logEvent(COMPONENT_NAME, "read disabled", "low", "from STATE_CONNECTED_END_READ_MULTI");
                                                        handleNetwork(); //wait for reaction
                                                        break;
                                                    case STATE_CONNECTED_END_WRITE:
                                                        if (mode == MODE_CONTROLLER) {
                                                            logger.logEvent(COMPONENT_NAME, "requested to be controller", "high",
                                                                    "STATE_CONNECTED_END_WRITE," + getAddress());
                                                            state = STATE_CONNECTED_INACTIVE_MULTI;
                                                            handleNetwork();
                                                            Network.getInstance().makeInactiveMulti(getAddress());
                                                            readEnabled = false;
                                                        } else {
                                                            logger.logError(COMPONENT_NAME, "cannot request if not controller", "high", getAddress());
                                                            throw new IllegalStateException();
                                                        }
                                                        break;
                                                    case STATE_CONNECTED_END_WRITE_REQ:
                                                    case STATE_CONNECTED_END_WRITE_REQ_FINAL:
                                                        logger.logError(COMPONENT_NAME, "cannot both request to be controller", "critical", getAddress());
                                                        throw new IllegalStateException();
                                                    default:
                                                        logger.logError(COMPONENT_NAME, "request to be controller from a weird state", "low", getAddress());
                                                        throw new IllegalStateException();
                                                }
                                            }
                                            break;
                                        case MESSAGE_END_WRITE_REQ_FINAL:
                                            synchronized (stateLock) {
                                                switch (state) {
                                                    case STATE_CONNECTED:
                                                        state = STATE_CONNECTED_END_READ_MULTI;
                                                        readEnabled = false;
                                                        logger.logEvent(COMPONENT_NAME, "read disabled", "low", "from STATE_CONNECTED");
                                                        handleNetwork(); //wait for reaction
                                                        break;
                                                    case STATE_CONNECTED_END_WRITE_FINAL:
                                                        if (mode == MODE_CONTROLLER) {
                                                            logger.logEvent(COMPONENT_NAME, "requested to be controller (final)", "high",
                                                                    "STATE_CONNECTED_END_WRITE," + getAddress());
                                                            state = STATE_CONNECTED_INACTIVE_MULTI_FINAL;
                                                            handleNetwork();
                                                            Network.getInstance().makeInactiveMulti(getAddress());
                                                            readEnabled = false;
                                                        } else {
                                                            logger.logError(COMPONENT_NAME, "cannot request if not controller (final)", "high", getAddress());
                                                            throw new IllegalStateException();
                                                        }
                                                        break;
                                                    case STATE_CONNECTED_END_WRITE_REQ:
                                                    case STATE_CONNECTED_END_WRITE_REQ_FINAL:
                                                        logger.logError(COMPONENT_NAME, "cannot both request to be controller", "critical", getAddress());
                                                        throw new IllegalStateException();

                                                    default:
                                                        logger.logError(COMPONENT_NAME, "request to be controller from a weird state", "low", getAddress());
                                                        throw new IllegalStateException();
                                                }
                                            }
                                            break;
                                        case MESSAGE_END_WRITE:
                                            synchronized (stateLock) {
                                                switch (state) {
                                                    case STATE_CONNECTED:
                                                        if (mode == MODE_CONTROLLER) {
                                                            throw new UnsupportedOperationException();
                                                        } else {
                                                            logger.logEvent(COMPONENT_NAME, "end read", "low", getAddress());
                                                            state = STATE_CONNECTED_END_READ;
                                                            handleNetwork();
                                                        }
                                                        readEnabled = false;
                                                        logger.logEvent(COMPONENT_NAME, "read disabled", "low", "from STATE_CONNECTED," + getAddress());
                                                        // handleNetwork(); //wait for reaction
                                                        break;
                                                    case STATE_CONNECTED_END_WRITE:
                                                        if (mode == MODE_CONTROLLER) {
                                                            logger.logEvent(COMPONENT_NAME, "requested to be controller", "high",
                                                                    "STATE_CONNECTED_END_WRITE," + getAddress());
                                                            handleNetwork();
                                                            Network.getInstance().makeInactiveMulti(getAddress());
                                                            readEnabled = false;
                                                        } else {
                                                            logger.logError(COMPONENT_NAME, "cannot request if not controller", "high", getAddress());
                                                            throw new IllegalStateException();
                                                        }
                                                        break;
                                                    case STATE_CONNECTED_END_WRITE_REQ:
                                                        logger.logEvent(COMPONENT_NAME, "end read", "low", getAddress());
                                                        state = STATE_CONNECTED_INACTIVE_MULTI;
                                                        handleNetwork();
                                                        logger.logEvent(COMPONENT_NAME, "read disabled", "low", "from STATE_CONNECTED_END_WRITE_REQ," + getAddress());
                                                        readEnabled = false;
                                                        break;
                                                    case STATE_CONNECTED_END_WRITE_REQ_FINAL:
                                                        logger.logError(COMPONENT_NAME, "cannot both request to be controller", "critical", getAddress());
                                                        throw new IllegalStateException();
                                                }
                                            }
                                            break;
                                        case MESSAGE_REQ_GRANTED:
                                            logger.logEvent(COMPONENT_NAME, "been granted controller", "high", getAddress());
                                            changeMode(MODE_CONTROLLER);
                                            Network.getInstance().makeInactive(getAddress());
                                            synchronized (stateLock){
                                                state= STATE_CONNECTED_INACTIVE_MULTI;
                                            }
                                            readEnabled = false;
                                            Network.getInstance().justBecameController();
                                            break;
                                        case MESSAGE_REQ_GRANTED_FINAL:
                                            logger.logEvent(COMPONENT_NAME, "granted controller: final", "high", getAddress());
                                            changeMode(MODE_CONTROLLER);
                                            synchronized (stateLock){
                                                state= STATE_END;
                                            }
                                            readEnabled = false;
                                            logger.logEvent(COMPONENT_NAME, "stopped handling network", "high", getAddress());
                                            logger.logEvent(COMPONENT_NAME, "closing connection", "high", getAddress());
                                            closeStream();
                                            Network.getInstance().release(getAddress());
                                            Network.getInstance().justBecameController();
                                            break;
                                        case MESSAGE_REQ_REFUSED:
                                            logger.logEvent(COMPONENT_NAME, "refused controller", "high", getAddress());
                                            synchronized (stateLock){
                                                state = STATE_CONNECTED_INACTIVE_MULTI_DISC;
                                            }
                                            logger.logEvent(COMPONENT_NAME, "disconnected", "high", getAddress());
                                            readEnabled = false;
                                            Network.getInstance().makeInactive(getAddress());
                                            logger.logEvent(COMPONENT_NAME, "stopped handling network", "high", getAddress());
                                            closeStream();
                                            Network.getInstance().startAnonymousConnection();
                                            break;
                                        case MESSAGE_REQ_REFUSED_FINAL:
                                            logger.logEvent(COMPONENT_NAME, "refused controller: final", "high", getAddress());
                                            changeMode(MODE_CONTROLLER);
                                            synchronized (stateLock){
                                                state= STATE_END;
                                            }
                                            readEnabled = false;
                                            logger.logEvent(COMPONENT_NAME, "stopped handling network", "high", getAddress());
                                            logger.logEvent(COMPONENT_NAME, "closing connection", "high", getAddress());
                                            closeStream();
                                            Network.getInstance().release(getAddress());
                                            Network.getInstance().startAnonymousConnection();
                                            break;
                                        default:
                                            logger.logError(COMPONENT_NAME, "received unknown message", "high", getAddress());
                                            logger.logError(COMPONENT_NAME, "received unknown message", "high", String.valueOf(buf[1]));

                                    }
                                } else {
                                    byte[] result = new byte[bytes];
                                    System.arraycopy(buf, 0, result, 0, bytes);
                                    logger.logEvent(COMPONENT_NAME, "received message", "low");
                                    synchronized (readLock) {
                                        readBuffer.add(result);
                                        readLock.notify();
                                    }
                                }
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                            logger.logError(COMPONENT_NAME, "IO EXCEPTION DURING READ", "high", getAddress());
                            readEnabled = false;
                            synchronized (stateLock) {
                                state = STATE_ERROR;
                            }
                            synchronized (readLock) {
                                readBuffer.add(null);
                                readLock.notify();
                            }
                        }
                    }
                    synchronized (startLock){
                        isListening = false;
                    }
                    logger.logEvent(COMPONENT_NAME, "no longer listening", "low", getAddress());
                }
            });
        }
        /**
        ThreadPoolSupplier.getSupplier().execute(new Runnable() {
            @Override
            public void run() {
                try{
                    while(!stopped){
                        synchronized (lock){
                            while (connection == null){
                                lock.wait();
                                if (stopped){
                                    throw new IOException();
                                }
                                lock.notify();
                            }
                        }
                        logger.logEvent(COMPONENT_NAME, "start listening", "low");
                        byte[] buf = new byte[4096];
                        int bytes = 0;
                        synchronized (con){
                            bytes = inputStream.read(buf);
                        }
                        byte[] result = new byte[bytes];
                        System.arraycopy(buf, 0, result, 0, bytes);
                        logger.logEvent(COMPONENT_NAME, "received message", "low");
                        synchronized (readBuffer){
                            logger.logEvent(COMPONENT_NAME, "add message to buffer", "low");
                            readBuffer.add(result);
                            readBuffer.notify();
                        }
                    }
                } catch(IOException e){
                    e.printStackTrace();
                    logger.logError(COMPONENT_NAME, "IO exception (could be normal behavior)", "high", e.getMessage());
                    connection = null;
                    synchronized (readBuffer){
                        readBuffer.notify();
                    }
                    logger.logEvent(COMPONENT_NAME, "stopped listening", "low");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        **/
    }


    //blocking call
    public byte[] readFromConnection() throws IOException {
        byte[] result = null;
        synchronized (readLock){
            while (readBuffer.size() == 0){
                try {
                    readLock.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            byte[] mes = readBuffer.pop();
            if (mes == null){
                throw new IOException();
            }
            result = mes;
        }
        /**
        synchronized (readBuffer){
            try{
                while (readBuffer.size() == 0 ){
                    readBuffer.wait();
                    if (stopped || connection ==  null ){
                        throw new IOException();
                    }
                }
                byte[] result = readBuffer.pop();
                return result;
            } catch (InterruptedException e) {
                e.printStackTrace();
                throw new IOException();
            }

        }
         **/
        return result;
    }

    public String getAddress() {
        return address;
    }

    public String getNickName(){
        return device.getAddress();
    }

    //asynchronous write
    public void writeToConnection(byte[] message) {
        logger.logEvent(COMPONENT_NAME, "adding message to write buffer", "low", getAddress());
        synchronized (writeLock){
            writeBufferMessages.add(message);
            writeLock.notify();
        }
    }

    public void connect() throws IOException {
        logger.logEvent(COMPONENT_NAME, "connecting with remote", "low", getAddress());
        synchronized (stateLock){
            if (state == STATE_CONNECTED_INACTIVE_MULTI_DISC || state == STATE_CONNECTED_INACTIVE_UNI_DISC
                    ||  state == STATE_DISCONNECTED_OPENED || state == STATE_DISCONNECTED_UNNAMED_OPENED){
                if (device == null) {
                    device = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(getAddress());
                }
                logger.logEvent(COMPONENT_NAME, "starting connection", "high", getAddress());
                BluetoothSocket con = device.createRfcommSocketToServiceRecord(AnonymousConnection.SERVICE_UUID);
                con.connect();
                setConnection(con);
                logger.logEvent(COMPONENT_NAME, "connected", "low", getAddress());
            } else  if (state == STATE_END || state == STATE_ERROR){
                throw new IllegalStateException();
            } else {
                logger.logError(COMPONENT_NAME, "undefined state action", "low", String.valueOf(state) );
            }
            state = STATE_CONNECTED;
        }
        waitForIO();
    }


}
