package com.project.collaborativeauthenticationapplication.alternative.management.application;

import android.content.Context;

import com.project.collaborativeauthenticationapplication.alternative.key.KeyManagementPresenter;

import com.project.collaborativeauthenticationapplication.alternative.key.application.GuardLeaderRefresh;
import com.project.collaborativeauthenticationapplication.alternative.network.AndroidConnection;
import com.project.collaborativeauthenticationapplication.alternative.network.Network;
import com.project.collaborativeauthenticationapplication.logger.AndroidLogger;
import com.project.collaborativeauthenticationapplication.logger.Logger;
import com.project.collaborativeauthenticationapplication.service.network.messages.MessageEncoder;
import com.project.collaborativeauthenticationapplication.service.network.messages.MessageParser;
import com.project.collaborativeauthenticationapplication.service.network.messages.YesMessage;

import java.io.IOException;


public class RemoteRefreshCoordinator  extends RefreshCoordinator{


    private MessageEncoder encoder = new MessageEncoder();

    private MessageParser parser   = new MessageParser();



    private static final String COMPONENT = "Remote Refresh Coordinator  RF";




    private static Logger logger = new AndroidLogger();



    private String main;


    public RemoteRefreshCoordinator(KeyManagementPresenter presenter) {
        super(presenter);
    }

    private GuardLeaderRefresh guardLeaderRefresh;

    @Override
    public void open(Context context) {
        super.open(context);
        logger.logEvent(COMPONENT, "new open request: create listener", "low");
        guardLeaderRefresh = new GuardLeaderRefresh(this);
        guardLeaderRefresh.start();
    }


    @Override
    public void setRemove(String remove) {
        logger.logEvent(COMPONENT, "set remove", "low", remove);
        super.setRemove(remove);
        getPresenter().isRunnable();
    }




    public void setMain(String main)
    {
        logger.logEvent(COMPONENT, "set main", "low");
        this.main = main;
    }

    @Override
    public void runRefresh() {
        logger.logEvent(COMPONENT, "run refresh", "low");
        buildAllClients(getRemove());

    }

    @Override
    protected RemoteRefreshClient buildRemoteClient(String device, RefreshCoordinator coordinator) {
        return new RemoteRemoteRefreshClient(device, coordinator);
    }

    @Override
    public void persisted() {
        logger.logEvent(COMPONENT, "persisted", "low");
        AndroidConnection connection = Network.getInstance().getConnectionWith(main);
        connection.writeToConnection(encoder.makeVoteYesMessage());

        connection.pushForFinal();

        try {
            byte[] response = connection.readFromConnection();
            logger.logEvent(COMPONENT, "persisted response", "low");
            YesMessage parsedMessage = (YesMessage) parser.parse(response);
            getPresenter().onFinished();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
