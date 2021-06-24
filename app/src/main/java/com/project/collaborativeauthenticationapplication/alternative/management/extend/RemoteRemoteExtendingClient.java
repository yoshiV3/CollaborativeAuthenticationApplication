package com.project.collaborativeauthenticationapplication.alternative.management.extend;

import com.project.collaborativeauthenticationapplication.alternative.network.AndroidConnection;
import com.project.collaborativeauthenticationapplication.alternative.network.Network;
import com.project.collaborativeauthenticationapplication.logger.AndroidLogger;
import com.project.collaborativeauthenticationapplication.logger.Logger;
import com.project.collaborativeauthenticationapplication.service.concurrency.ThreadPoolSupplier;
import com.project.collaborativeauthenticationapplication.service.crypto.BigNumber;
import com.project.collaborativeauthenticationapplication.service.network.messages.MessageParser;
import com.project.collaborativeauthenticationapplication.service.network.messages.YesMessage;

import java.io.IOException;
import java.util.List;

public class RemoteRemoteExtendingClient extends RemoteExtendingClient {


    public static final String COMPONENT = "Remote remote extend client EX";

    private static Logger logger = new AndroidLogger();

    private MessageParser parser   = new MessageParser();


    public RemoteRemoteExtendingClient(ExtendingCoordinator coordinator, String device) {
        super(coordinator, device);
    }


    @Override
    public void receiveSlice(BigNumber bigNumber) {
        logger.logEvent(COMPONENT, "receive slices", "low");
        sendSlice(bigNumber);
        getConnection().pushToCoordinator();


        String main = getCoordinator().getMain();
        logger.logEvent(COMPONENT, "main", "low", main);
        if (getConnection().getAddress().equals(main)){
            ThreadPoolSupplier.getSupplier().execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        logger.logEvent(COMPONENT, "main: waiting", "low", main);
                        YesMessage yesMessage = (YesMessage) parser.parse(getConnection().readFromConnection());
                        getConnection().pushForFinal();
                        logger.logEvent(COMPONENT, "main: done", "low", main);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    logger.logEvent(COMPONENT, "yes", "low");
                    getCoordinator().persisted();
                }
            });
        }
    }

    @Override
    protected Runnable getCalculatingCode(List<String> remotes, int newIdentifier, String address, int weight, int[] weights) {
        return new Runnable() {
            @Override
            public void run() {
                Object lock = getInviteLock();

                synchronized (lock){
                    Network instance = Network.getInstance();
                    AndroidConnection connection = instance.getConnectionWithInMode(getDevice(), AndroidConnection.MODE_SLAVE_MULTI);
                    logger.logEvent(COMPONENT, "found connection", "low");
                    setConnection(connection);
                    logger.logEvent(COMPONENT, "generate parts", "low");
                    setInvited(true);
                    lock.notify();
                }
                receiveRemoteSlices();
            }
        };
    }
}
