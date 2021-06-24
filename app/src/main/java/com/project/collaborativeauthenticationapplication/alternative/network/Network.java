package com.project.collaborativeauthenticationapplication.alternative.network;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

import com.project.collaborativeauthenticationapplication.alternative.signature.SignatureStarter;
import com.project.collaborativeauthenticationapplication.logger.AndroidLogger;
import com.project.collaborativeauthenticationapplication.logger.Logger;

import com.project.collaborativeauthenticationapplication.service.concurrency.ThreadPoolSupplier;
import com.project.collaborativeauthenticationapplication.service.general.CustomParticipant;
import com.project.collaborativeauthenticationapplication.service.general.Participant;
import com.project.collaborativeauthenticationapplication.service.network.AndroidBluetoothMonitor;

import com.project.collaborativeauthenticationapplication.service.network.AndroidCommunicationServer;
import com.project.collaborativeauthenticationapplication.service.network.BluetoothMonitor;
import com.project.collaborativeauthenticationapplication.service.network.ConnectionRequester;

import com.project.collaborativeauthenticationapplication.service.network.Device;
import com.project.collaborativeauthenticationapplication.service.network.messages.MessageEncoder;
import com.project.collaborativeauthenticationapplication.service.network.messages.MessageParser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class Network {


    private static final int  STATE_IDLE   = 0;

    private static final int  STATE_ERROR  = 1;

    private static final int  STATE_BUSY    = 2;
    private static final int STATE_STARTING = 3;
    public static final String ANY = "any";


    private static final Network instance = new Network();

    private String localAddress = "";


    private final int numberOfConnections = 0;


    public static Network getInstance()
    {
        return instance;
    }


    private static final String COMPONENT = "ACN Network";


    private static final Logger logger = new AndroidLogger();

   private AndroidConnection defaultConnection;


    private Network(){}

    SignatureStarter signatureStarter;



    private final MessageEncoder encoder = new MessageEncoder();

    private final BluetoothMonitor bluetoothMonitor = new AndroidBluetoothMonitor();

    private AnonymousConnection  anonymousConnection;

    private boolean shouldStop = false;

    private MessageParser parser = new MessageParser();

    HashMap<String, AndroidConnectionState> cachedConnections = new HashMap<>();


    private int state = STATE_IDLE;


    private boolean mainControllerHere = false;


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


    public  void close(){
        logger.logEvent(COMPONENT, "close network", "low");
        closeAnonymousConnection();
        synchronized (cachedConnections){
            for (AndroidConnectionState con : cachedConnections.values()){
                con.getConnection().closeNoRelease();
            }
            cachedConnections.clear();
        }
        defaultConnection.closeNoRelease();
        state = STATE_IDLE;
    }



    public AndroidConnection getAnyConnection(){ //connected device is the main controller
        logger.logEvent(COMPONENT, "new any connection", "low");
        if (defaultConnection.isConnected()){
            throw new IllegalStateException();
        }
        AndroidConnection connection = new AndroidConnection();
        synchronized (cachedConnections){
            cachedConnections.put(ANY, new AndroidConnectionState(connection));
        }
        connection.open();
        return connection;
    }

    public void  open(){
        logger.logEvent(COMPONENT, "open network", "low");
        if (state == STATE_ERROR){
            logger.logError(COMPONENT, "Cannot start network", "critical");
        } else if (state == STATE_BUSY) {
            logger.logEvent(COMPONENT, "network already started", "high");
        } else if (state == STATE_STARTING){
            logger.logEvent(COMPONENT, "network already started", "high");
        } else {
            mainControllerHere = false;
            defaultConnection   = new AndroidConnection();
            anonymousConnection = new AnonymousConnection();
            //if network should be started, we should listen for incoming requests
            ThreadPoolSupplier.getSupplier().execute(new Runnable() {
                @Override
                public void run() {
                    synchronized (cachedConnections){
                        cachedConnections.put("any", new AndroidConnectionState(defaultConnection));
                        defaultConnection.open();
                    }
                }
            });
            signatureStarter  = new SignatureStarter(defaultConnection);
            signatureStarter.start();

            startAnonymousConnection();
        }
    }


    public  void closeAllConnections(){
        logger.logEvent(COMPONENT, "closing all connections", "low");
        closeAnonymousConnection();
        synchronized (cachedConnections){
            for (AndroidConnectionState con : cachedConnections.values()){
                con.getConnection().closeNoRelease();
            }
            cachedConnections.clear();
        }
        defaultConnection.close();
        mainControllerHere = false;
        signatureStarter = null;
        state = STATE_IDLE;
        anonymousConnection = new AnonymousConnection();
        logger.logEvent(COMPONENT, "reopening the anonymous connection", "low");
        open();
    }

    public String getLocalAddress() {
        logger.logEvent(COMPONENT, "local address found ", "low", localAddress);
        return localAddress;
    }


    public void registerLocalAddress(String address) {
        localAddress = address;
        logger.logEvent(COMPONENT, "new local address registered", "low", localAddress);
    }

    public boolean isBluetoothAvailable() {
        return bluetoothMonitor.isBluetoothAvailable();
    }

    public boolean isBluetoothEnabled() {
        return bluetoothMonitor.isBluetoothEnabled();
    }

    public ArrayList<Device> getPairedDevices() {
        return bluetoothMonitor.getPairedDevices();
    }

    public void establishConnectionsWithInTopologyOne(Set<String> addresses, ConnectionRequester requester) {
        closeAnonymousConnection();
        mainControllerHere = true;
        logger.logEvent(COMPONENT, "establishing connection with a number of remotes", "normal" );
        Network.ConnectionCreator connectionCreator = new Network.ConnectionCreator(addresses.size(), requester);
        for( String address : addresses) {
            ThreadPoolSupplier.getSupplier().execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        getConnectionWith(address, AndroidConnection.MODE_CONTROLLER, true);
                        connectionCreator.establishedConnection(address);
                    } catch (IOException e) {
                        connectionCreator.failedToEstablishConnection();
                    }
                }
            });

        }
    }

    public void establishConnectionsWithInTopologyTwo(){
        logger.logEvent(COMPONENT, "topology two", "low");
        mainControllerHere = true;
    }

    public AndroidConnection getConnectionWith(String address, int mode, boolean mainController) throws IOException {
        logger.logEvent(COMPONENT, "request connection with", "low");
        logger.logEvent(COMPONENT, "request another connection", "low", String.valueOf(cachedConnections.values().size()));
        AndroidConnection result = null;
        synchronized(cachedConnections){
            AndroidConnectionState connectionState = cachedConnections.getOrDefault(address, null);
            AndroidConnection connection;
            if (connectionState == null){
                connection = new AndroidConnection(address, mode, mainControllerHere, mainController);
                cachedConnections.put(address, new AndroidConnectionState(connection));
            } else {
                connection = connectionState.getConnection();
            }
            if (!connection.isConnected()){
                logger.logEvent(COMPONENT, "not connected: connecting", "low");
                BluetoothDevice device = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(address);
                BluetoothSocket con = device.createRfcommSocketToServiceRecord(AnonymousConnection.SERVICE_UUID);
                con.connect();
                logger.logEvent(COMPONENT, "connected", "low");
                connection.open();
                connection.setConnection(con);
            }
            result = connection;
        }
        result.open();
        return result;

    }

    public  AndroidConnection getConnectionWithInMode(String address, int mode){
        logger.logEvent(COMPONENT, "request connection in mode with", "low", address);
        logger.logEvent(COMPONENT, "request another connection", "low", String.valueOf(cachedConnections.values().size()));
        AndroidConnection result = null;
        AndroidConnectionState state;
        synchronized(cachedConnections){
            state = cachedConnections.getOrDefault(address, null);
        }
        if (state != null){
            logger.logEvent(COMPONENT, "cached connection requested", "low", address);
            result = state.getConnection();
            result.changeMode(mode);
        } else {
            logger.logEvent(COMPONENT, "new connection requested", "low", address);
            result = getNewConnectionWith(address);
            result.changeMode(mode);
        }
        logger.logEvent(COMPONENT, "opening connection", "low", address);
        result.open();
        logger.logEvent(COMPONENT, "opened connection", "low", address);
        return result;
    }

    private AndroidConnection getNewConnectionWith(String address) {
        logger.logEvent(COMPONENT, "request connection with", "low");
        logger.logEvent(COMPONENT, "request another connection", "low", String.valueOf(cachedConnections.values().size()));
        AndroidConnection connection = new AndroidConnection(address);
        synchronized (cachedConnections){
            cachedConnections.put(address, new AndroidConnectionState(connection));
        }
        return connection;
    }



    public AndroidConnection getConnectionWith(String address) { //assumes already cached
        logger.logEvent(COMPONENT, "request connection with", "low");
        logger.logEvent(COMPONENT, "request another connection", "low", String.valueOf(cachedConnections.values().size()));
        AndroidConnection result = null;
        AndroidConnectionState state;
        synchronized(cachedConnections){
            state = cachedConnections.getOrDefault(address, null);
        }
        if (state != null){
            result = state.getConnection();
            result.open();
        }
        return result;
    }

    public void release(String address) {
        logger.logEvent(COMPONENT, "release", "high", address);
        synchronized (cachedConnections){
            cachedConnections.remove(address);
            if (cachedConnections.values().size() == 0){
                logger.logEvent(COMPONENT, "all connections have been released", "high");
                defaultConnection.closeNoRelease();
                mainControllerHere = false;
                signatureStarter = null;
                state = STATE_IDLE;
                logger.logEvent(COMPONENT, "reopening the anonymous connection", "low");
                open();
            }
        }
    }

    protected void justBecameController() {
        logger.logEvent(COMPONENT, "just became the controller", "high");
        synchronized (cachedConnections){
                boolean active = false;
                AndroidConnection main = null;
                for (AndroidConnectionState state : cachedConnections.values()) {
                    final AndroidConnection connection = state.getConnection();
                    if (connection.isConnectedWithMainController()) {
                        main = connection;
                    } else {
                        if (connection.isConnected()){
                            logger.logEvent(COMPONENT, "disconnecting from other connections", "low");
                            connection.disconnect();
                        }
                    }
                    active = active || !state.isInactive();
                }
                for (AndroidConnectionState state : cachedConnections.values()) {
                    final AndroidConnection connection = state.getConnection();
                    if (!state.isInactive()) {
                        logger.logEvent(COMPONENT, "(re)connect", "high");
                        ThreadPoolSupplier.getSupplier().execute(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    logger.logEvent(COMPONENT, "(re)connect: threat", "high");
                                    connection.changeMode(AndroidConnection.MODE_CONTROLLER);
                                    connection.open();
                                    connection.connect();
                                } catch (IOException e) {
                                    logger.logError(COMPONENT, "connection failed", "low");
                                    e.printStackTrace();
                                    closeAllConnections();
                                }
                            }
                        });
                    }
                }
                if (!active){
                    //get back to main controller
                    logger.logEvent(COMPONENT, "all inactive: reconnecting", "high");
                    try {
                        try {
                            TimeUnit.MICROSECONDS.sleep(500);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        main.reconnect();
                    } catch (IOException e) {
                        e.printStackTrace();
                        closeAllConnections();
                    }
                } else {
                    if (main.isConnected()){
                        logger.logEvent(COMPONENT, "disconnecting from main", "low");
                        main.disconnect();
                    }
                }
        }
    }

    public void handleDisconnect() {
        if (mainControllerHere){
            if (anonymousConnection == null || !isRunning ){
                anonymousConnection = new AnonymousConnection();
                startAnonymousConnection();
            }
        } else {
            synchronized (cachedConnections){
                for (AndroidConnectionState stat : cachedConnections.values()){
                    AndroidConnection con = stat.getConnection();
                    if (con.isConnectedWithMainController()){
                        try {
                            con.connect();
                        } catch (IOException e) {
                            e.printStackTrace();
                            closeAllConnections();
                        }
                    }
                }
            }
        }
    }



    public enum LOCATION {
        REMOTE,
        UNDEFINED,
        LOCAL;
    }



    private Object multiLock = new Object();

    private boolean hasPickedNextController = false;


    protected void makeActive(String address){
        synchronized (cachedConnections) {
            AndroidConnectionState state = cachedConnections.getOrDefault(address, null);
            state.setInactive_multi(false);
            state.setInactive_uni(false);
        }
    }

    protected void  makeInactive(String address){
        synchronized (cachedConnections) {
            logger.logEvent(COMPONENT, "make inactive uni", "low", address);
            AndroidConnectionState state = cachedConnections.getOrDefault(address, null);
            state.getConnection().changeMode(AndroidConnection.MODE_SLAVE_UNI);
            state.setInactive_multi(true);
            state.setInactive_uni(true);
        }
    }

    protected void makeInactiveMulti(String address){
        ThreadPoolSupplier.getSupplier().execute(new Runnable() {
            @Override
            public void run() {
                logger.logEvent(COMPONENT, "multi connection has become inactive", "high", address);
                synchronized (cachedConnections) {
                    AndroidConnectionState th = cachedConnections.get(address);
                    th.setInactive_multi(true);
                    th.getConnection().changeMode(AndroidConnection.MODE_SLAVE_UNI);
                    logger.logEvent(COMPONENT, "make inactive", "low", address);
                    synchronized (multiLock) {
                        if (!hasPickedNextController) {
                            th.getConnection().makeNextController();
                            hasPickedNextController = true;
                        }
                        boolean active = false;
                        for (AndroidConnectionState state : cachedConnections.values()) {
                            logger.logEvent(COMPONENT, "is inactive " + state.getConnection().getAddress() , "low", String.valueOf(state.isInactive()));
                            active = active || (!state.isInactive() && !state.getConnection().isInterruptBlocked());
                        }
                        AndroidConnection nextLeader = null;//at least one should be the next leader
                        AndroidConnection main = null;
                        if (!active) {
                            logger.logEvent(COMPONENT, "all connections have become inactive", "high");
                            for (AndroidConnectionState state : cachedConnections.values()) {
                                logger.logEvent(COMPONENT, "check if next controller", "high");
                                AndroidConnection con = state.getConnection();
                                if (con.isNextController()) {
                                    nextLeader = con;
                                    logger.logEvent(COMPONENT, "found next controller", "high");
                                } else {
                                    if (!con.isInterruptBlocked()){
                                        con.continueToNextStage();
                                    }
                                }
                                if (con.isConnectedWithMainController()){
                                    main = con;
                                    logger.logEvent(COMPONENT, "MESSAGE: main found", "low");
                                }
                            }
                            hasPickedNextController = false;
                            try {
                                TimeUnit.MICROSECONDS.sleep(500);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            nextLeader.continueToNextStage();
                            try {
                                TimeUnit.MICROSECONDS.sleep(500);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            if (!mainControllerHere) {
                                main.changeMode(AndroidConnection.MODE_SLAVE_UNI);
                                try {
                                    main.reconnect();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                    logger.logError(COMPONENT, "could not connect with main", "critical");
                                }
                            }
                        }
                    }
                }
            }
        });
   }

    protected void startAnonymousConnection() {
        ThreadPoolSupplier.getSupplier().execute(new StartAnonymousConnectionCode());
    }


    private synchronized void setStateStarting(){
        if (state != STATE_IDLE){
            throw new IllegalStateException();
        }
        state = STATE_STARTING;
    }

    private synchronized void openAnonymousConnection(){
        try {
            anonymousConnection.open();
        } catch (IOException e) {
            e.printStackTrace();
            logger.logError(COMPONENT, "could not open server", "critical");
        }
    }

    private synchronized void closeAnonymousConnection(){
        logger.logEvent(COMPONENT, "close anonymous connection", "low");
        if ( anonymousConnection != null){
            anonymousConnection.close();
        }
        isRunning = false;
    }


    private static boolean isRunning = false;

    private class StartAnonymousConnectionCode implements Runnable {


        @Override
        public void run() {
            try {
                boolean startEnable = true;
                if (!isRunning){
                    isRunning = true;
                    while (startEnable){
                        openAnonymousConnection();
                        logger.logEvent(COMPONENT, " ABC waiting for new connection", "high");
                        BluetoothSocket socket = anonymousConnection.listenForIncomingRequests();
                        logger.logEvent(COMPONENT, " ABC receive new connection", "high", socket.getRemoteDevice().getAddress());
                        String remoteAddress = socket.getRemoteDevice().getAddress();
                        synchronized (cachedConnections){
                            if (cachedConnections.containsKey(remoteAddress)){
                                AndroidConnection connection = cachedConnections.get(remoteAddress).getConnection();
                                connection.setConnection(socket);
                                if (mainControllerHere){
                                    logger.logEvent(COMPONENT, "ABC reconnected with main controller", "low", remoteAddress);
                                    connection.changeMode(AndroidConnection.MODE_CONTROLLER);
                                } else {
                                    logger.logEvent(COMPONENT, "connected with  a new controller", "low", remoteAddress);
                                }
                                connection.waitForIO();
                                logger.logEvent(COMPONENT, "ABC done", "low", remoteAddress);
                            } else {
                                if (cachedConnections.values().size() >1){
                                    logger.logEvent(COMPONENT, "ABC closing socket due to too many connections", "error");
                                    socket.close();
                                    anonymousConnection.open();
                                } else {
                                    AndroidConnectionState con = cachedConnections.remove(ANY);
                                    logger.logEvent(COMPONENT, "ABC give socket to connection", "low");
                                    con.getConnection().setConnection(socket);
                                    con.getConnection().waitForIO();
                                    cachedConnections.put(remoteAddress, con);
                                    startEnable = false;
                                    isRunning = false;
                                }
                            }
                        }
                    }
                } else {
                    logger.logEvent(COMPONENT, "ABC request to start anonymous server twice", "low");
                }
            } catch (IllegalStateException e){
                logger.logError(COMPONENT, "ABC tried to start during wrong state, probably requested twice", "critical");
                isRunning = false;
            } catch (IOException e) {
                e.printStackTrace();
                logger.logError(COMPONENT, "ABC IO exception during anonymous launch", "critical");
                isRunning = false;
            }
        }
    }


    private class ConnectionCreator {
        private int numberOfAnswers;

        private final int numberOfRequiredAnswers;

        private final ConnectionRequester requester;

        public ConnectionCreator(int numberOfRequiredAnswers, ConnectionRequester requester) {
            this.numberOfRequiredAnswers = numberOfRequiredAnswers;
            this.requester = requester;
        }

        public synchronized void  establishedConnection(String address){
            requester.isAvailable(address);
            numberOfAnswers += 1;
            if (numberOfAnswers == numberOfRequiredAnswers){
                requester.signalJobDone();
            }
        }

        public synchronized void failedToEstablishConnection(){
            numberOfAnswers += 1;
            if (numberOfAnswers == numberOfRequiredAnswers){
                requester.signalJobDone();
            }
        }
    }


    private class AndroidConnectionState {

        private AndroidConnection connection;

        private boolean inactive_uni;

        private boolean Inactive_multi;


        public AndroidConnectionState(AndroidConnection connection, boolean inactive_uni, boolean inactive_multi){
            this.connection       = connection;
            this.inactive_uni     = inactive_uni;
            this.Inactive_multi = inactive_multi;
        }


        public AndroidConnectionState(AndroidConnection connection){
            this(connection, false, false);
        }

        public boolean isInactive_multi() {
            return Inactive_multi;
        }

        public boolean isInactive_uni() {
            return inactive_uni;
        }

        public AndroidConnection getConnection() {
            return connection;
        }

        public void setInactive_multi(boolean inactive_multi) {
            Inactive_multi = inactive_multi;
        }

        public void setInactive_uni(boolean inactive_uni) {
            this.inactive_uni = inactive_uni;
        }

        public boolean isInactive(){
            return isInactive_multi()|| isInactive_uni();
        }
    }


}
