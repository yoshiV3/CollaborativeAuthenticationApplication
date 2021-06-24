package com.project.collaborativeauthenticationapplication.alternative.key.application;

import com.project.collaborativeauthenticationapplication.alternative.network.AndroidConnection;
import com.project.collaborativeauthenticationapplication.alternative.network.Network;
import com.project.collaborativeauthenticationapplication.logger.AndroidLogger;
import com.project.collaborativeauthenticationapplication.logger.Logger;
import com.project.collaborativeauthenticationapplication.service.concurrency.ThreadPoolSupplier;
import com.project.collaborativeauthenticationapplication.service.key.application.key_generation.distributed_system.RemoteKeyGenerationCoordinator;
import com.project.collaborativeauthenticationapplication.service.network.messages.MessageParser;

public abstract class  GuardLeader {

    private MessageParser parser = new MessageParser();


    private static final String COMPONENT = "Leader guard";


    private static Logger logger = new AndroidLogger();


    private AndroidConnection connection;
    public GuardLeader(){
        this.connection = Network.getInstance().getAnyConnection();

    }

    protected AndroidConnection getConnection() {
        return connection;
    }

    public void start(){
        logger.logEvent(COMPONENT, "about to start waiting", "low");
        ThreadPoolSupplier.getSupplier().execute(getListener());
    }



    public abstract Runnable getListener();
}
