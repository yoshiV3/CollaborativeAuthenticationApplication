package com.project.collaborativeauthenticationapplication.alternative.management.application;

import com.project.collaborativeauthenticationapplication.alternative.network.AndroidConnection;
import com.project.collaborativeauthenticationapplication.alternative.network.Network;
import com.project.collaborativeauthenticationapplication.logger.AndroidLogger;
import com.project.collaborativeauthenticationapplication.logger.Logger;
import com.project.collaborativeauthenticationapplication.service.crypto.BigNumber;

import java.util.ArrayList;

public class RemoteRemoteRefreshClient extends RemoteRefreshClient {



    private Logger logger = new AndroidLogger();

    public static final String COMPONENT = "remote remote refresh client RF";



    public RemoteRemoteRefreshClient(String remote, RefreshCoordinator coordinator) {
        super(remote, coordinator);
    }


    @Override
    public void receiveRefreshShares(ArrayList<BigNumber> bigNumbers) {
        logger.logEvent(COMPONENT, "received shares", "low");
        sendShares(bigNumbers);
        getConnection().pushToCoordinator();
        logger.logEvent(COMPONENT, "done shares", "low");
    }

    @Override
    protected Runnable getRefreshCode() {
        return new Runnable() {
            @Override
            public void run() {

                Object lock = getInviteLock();

                synchronized (lock){
                    Network instance = Network.getInstance();
                    AndroidConnection connection = instance.getConnectionWithInMode(getRemote(), AndroidConnection.MODE_SLAVE_MULTI);
                    logger.logEvent(COMPONENT, "found connection", "low");
                    setConnection(connection);
                    logger.logEvent(COMPONENT, "generate parts", "low");
                    setInvited(true);
                    lock.notify();
                    logger.logEvent(COMPONENT, "release", "low");
                }
                receiveRemoteRefreshShare();
            }
        };
    }
}
