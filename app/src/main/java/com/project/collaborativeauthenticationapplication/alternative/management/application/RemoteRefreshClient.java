package com.project.collaborativeauthenticationapplication.alternative.management.application;

import com.project.collaborativeauthenticationapplication.alternative.network.AndroidConnection;
import com.project.collaborativeauthenticationapplication.alternative.network.Network;
import com.project.collaborativeauthenticationapplication.logger.AndroidLogger;
import com.project.collaborativeauthenticationapplication.logger.Logger;
import com.project.collaborativeauthenticationapplication.service.concurrency.ThreadPoolSupplier;
import com.project.collaborativeauthenticationapplication.service.crypto.BigNumber;
import com.project.collaborativeauthenticationapplication.service.network.messages.AbstractMessage;
import com.project.collaborativeauthenticationapplication.service.network.messages.MessageEncoder;
import com.project.collaborativeauthenticationapplication.service.network.messages.MessageParser;
import com.project.collaborativeauthenticationapplication.service.network.messages.RefreshShareMessage;
import com.project.collaborativeauthenticationapplication.service.network.messages.YesMessage;

import java.io.IOException;
import java.util.ArrayList;

public class RemoteRefreshClient extends RefreshClient{


    public static final String COMPONENT = "Remote refresh client RF";

    private static Logger logger = new AndroidLogger();



    private MessageEncoder encoder = new MessageEncoder();

    private MessageParser parser   = new MessageParser();

    private AndroidConnection connection;


    private final String remote;


    public RemoteRefreshClient(String remote, RefreshCoordinator coordinator){
        super(coordinator);
        this.remote = remote;
    }


    protected String getRemote() {
        return remote;
    }

    public static Logger getLogger() {
        return logger;
    }


    protected void setConnection(AndroidConnection connection) {
        this.connection = connection;
    }

    protected AndroidConnection getConnection() {
        return connection;
    }

    private boolean isInvited = false;


    protected void setInvited(boolean invited) {
        isInvited = invited;
    }



    private final Object inviteLock = new Object();

    protected Object getInviteLock() {
        return inviteLock;
    }

    @Override
    public String getDevice() {
        return remote;
    }

    @Override
    public void receiveRefreshShares(ArrayList<BigNumber> bigNumbers) {
        logger.logEvent(COMPONENT, "receive shares", "low");
        sendShares(bigNumbers);
        logger.logEvent(COMPONENT, "done shares", "low");
        connection.push();

    }

    protected void sendShares(ArrayList<BigNumber> bigNumbers) {
        logger.logEvent(COMPONENT, "sending shares", "low");
        synchronized (inviteLock) {
            logger.logEvent(COMPONENT, "locked", "low");
            while (!isInvited) {
                try {
                    logger.logEvent(COMPONENT, "waiting", "low");
                    inviteLock.wait();
                    logger.logEvent(COMPONENT, "notification", "low");
                } catch (InterruptedException interruptedException) {
                    interruptedException.printStackTrace();
                }
            }
            logger.logEvent(COMPONENT, "done waiting", "low");
            byte[] mes = encoder.makeRefreshShareMessage(bigNumbers);
            connection.writeToConnection(mes);
            logger.logEvent(COMPONENT, "done writing", "low");
        }
        logger.logEvent(COMPONENT, "done lock", "low");
    }

    @Override
    public void close() {
        logger.logEvent(COMPONENT, "close message", "low");
        byte[] mes = encoder.makeVoteYesMessage();
        connection.writeToConnection(mes);
        connection.pushForFinal();
    }

    @Override
    protected Runnable getRefreshCode(){
        return new Runnable() {
            @Override
            public void run() {
                Network instance = Network.getInstance();
                synchronized (inviteLock){
                    try {
                        logger.logEvent(COMPONENT, "join message", "low");
                        encoder.clear();
                        byte[] message = encoder.makeRefreshMessage(getRemove());
                        connection = instance.getConnectionWith(remote, AndroidConnection.MODE_CONTROLLER, false);
                        connection.writeToConnection(message);
                        isInvited = true;
                        inviteLock.notify();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                receiveRemoteRefreshShare();
                receiveVote();
            }
        };
    }

    private void receiveVote() {
        try {
            byte[] response       = connection.readFromConnection(); // wait till receive the correct parts
            YesMessage parsedMessage = (YesMessage) parser.parse(response);

            logger.logEvent(COMPONENT, "received vote", "low");

            ThreadPoolSupplier.getSupplier().execute(new Runnable() {
                @Override
                public void run() {
                    getCoordinator().persisted();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected void receiveRemoteRefreshShare(){
        logger.logEvent(COMPONENT, "started listening for refresh shares", "low");
        try {
            byte[] response       = connection.readFromConnection(); // wait till receive the correct parts
            RefreshShareMessage parsedMessage = (RefreshShareMessage) parser.parse(response);


            logger.logEvent(COMPONENT, "received refresh shares", "low");

            ThreadPoolSupplier.getSupplier().execute(new Runnable() {
                @Override
                public void run() {
                    getCoordinator().passToLocal(parsedMessage.getShares());
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
