package com.project.collaborativeauthenticationapplication.alternative.management.application;

import com.project.collaborativeauthenticationapplication.alternative.key.KeyManagementPresenter;
import com.project.collaborativeauthenticationapplication.alternative.network.Network;
import com.project.collaborativeauthenticationapplication.logger.AndroidLogger;
import com.project.collaborativeauthenticationapplication.logger.Logger;

public class LocalRefreshCoordinator extends RefreshCoordinator{

    private Logger logger = new AndroidLogger();


    public LocalRefreshCoordinator(KeyManagementPresenter presenter) {
        super(presenter);
    }

    @Override
    public void runRefresh() {
        logger.logEvent(" refresh coordinator", "refresh", "low");
        final String remove = getPresenter().getDevice();

        logger.logEvent(" refresh coordinator", "network", "low");


        Network.getInstance().establishConnectionsWithInTopologyTwo();


        logger.logEvent(" refresh coordinator", "build", "low");

        buildAllClients(remove);

    }

    @Override
    protected RemoteRefreshClient buildRemoteClient(String device, RefreshCoordinator coordinator) {
        return new RemoteRefreshClient(device, coordinator);
    }

    private int numberOfVotes = 0;

    @Override
    public synchronized void persisted() {
        numberOfVotes += 1;
        int requiredNumberOfVotes = getNumberOfRemotes() +1;
        if (getRemove() != null){
            requiredNumberOfVotes = requiredNumberOfVotes -1;
        }
        if(numberOfVotes == requiredNumberOfVotes){
            for (RefreshClient client : getClients()){
                client.close();
            }
            getPresenter().onFinished();
        }
    }
}
