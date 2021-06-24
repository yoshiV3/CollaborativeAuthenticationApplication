package com.project.collaborativeauthenticationapplication.alternative.management.extend;

import com.project.collaborativeauthenticationapplication.alternative.network.AndroidConnection;
import com.project.collaborativeauthenticationapplication.alternative.network.Network;
import com.project.collaborativeauthenticationapplication.logger.AndroidLogger;
import com.project.collaborativeauthenticationapplication.logger.Logger;
import com.project.collaborativeauthenticationapplication.service.concurrency.ThreadPoolSupplier;
import com.project.collaborativeauthenticationapplication.service.crypto.BigNumber;
import com.project.collaborativeauthenticationapplication.service.network.messages.MessageEncoder;
import com.project.collaborativeauthenticationapplication.service.network.messages.MessageParser;
import com.project.collaborativeauthenticationapplication.service.network.messages.SliceMessage;

import java.io.IOException;
import java.util.List;

public class RemoteExtendingClient extends ExtendingClient{

    private final String device;


    private AndroidConnection connection;


    public static final String COMPONENT = "Remote extend client EX";

    private static Logger logger = new AndroidLogger();

    private MessageEncoder encoder = new MessageEncoder();

    private MessageParser parser   = new MessageParser();


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


    public RemoteExtendingClient(ExtendingCoordinator coordinator, String device) {
        super(coordinator);
        this.device = device;
    }

    @Override
    public String getDevice() {
        return device;
    }

    @Override
    protected Runnable getCalculatingCode(List<String> remotes, int newIdentifier, String address, int weight, int[] weights) {
        return new Runnable() {
            @Override
            public void run() {
                Network instance = Network.getInstance();
                synchronized (inviteLock){
                    try {
                        logger.logEvent(COMPONENT, "join message", "low", device);
                        logger.logEvent(COMPONENT, "join message: address", "low", address);
                        logger.logEvent(COMPONENT, "join message: newIdentifier", "low", String.valueOf(newIdentifier));
                        logger.logEvent(COMPONENT, "join message: newIdentifier", "low", String.valueOf(weight));
                        logger.logEvent(COMPONENT, "join message: remotes", "low", String.valueOf(remotes.size()));
                        encoder.clear();
                        byte[] message = encoder.makeCalculateMessage(remotes, newIdentifier, address, weight, weights, device);
                        connection = instance.getConnectionWith(getDevice(), AndroidConnection.MODE_CONTROLLER, false);
                        connection.writeToConnection(message);
                        isInvited = true;
                        inviteLock.notify();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                receiveRemoteSlices();
            }
        };
    }

    protected void receiveRemoteSlices() {
        logger.logEvent(COMPONENT, "started listening for slices", "low");
        try {
            byte[] response       = connection.readFromConnection(); // wait till receive the correct parts
            SliceMessage parsedMessage = (SliceMessage) parser.parse(response);


            logger.logEvent(COMPONENT, "received refresh shares", "low");

            ThreadPoolSupplier.getSupplier().execute(new Runnable() {
                @Override
                public void run() {
                    getCoordinator().passToLocal(parsedMessage.getSlice());
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected Runnable getWaitingCode(int newIdentifier, String address) {
        return null;
    }

    @Override
    public void calculate(int weight) {
        logger.logEvent(COMPONENT, "calculating", "low", String.valueOf(weight));
        setCalculating(true);
        setWeight(weight);
    }

    @Override
    public void waitTillCalculated() {
        setCalculating(false);
    }

    @Override
    public void receiveSlice(BigNumber bigNumber) {
        logger.logEvent(COMPONENT, "receive slices", "low");
        sendSlice(bigNumber);
        connection.push();
    }

    protected void sendSlice(BigNumber bigNumber) {
        logger.logEvent(COMPONENT, "sending shares", "low", device);
        synchronized (inviteLock) {
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
            byte[] mes = encoder.makeExtendSliceMessage(bigNumber);
            connection.writeToConnection(mes);
        }
    }

    @Override
    public void persist() {
        logger.logEvent(COMPONENT, "persisted message", "low", device);
        byte[] mes = encoder.makeVoteYesMessage();
        connection.writeToConnection(mes);
        connection.pushForFinal();

    }
}
