package com.project.collaborativeauthenticationapplication.alternative.key.application;

import com.project.collaborativeauthenticationapplication.alternative.management.application.RemoteRefreshCoordinator;
import com.project.collaborativeauthenticationapplication.logger.AndroidLogger;
import com.project.collaborativeauthenticationapplication.logger.Logger;
import com.project.collaborativeauthenticationapplication.service.network.messages.AbstractMessage;

import com.project.collaborativeauthenticationapplication.service.network.messages.MessageParser;
import com.project.collaborativeauthenticationapplication.service.network.messages.RefreshMessage;

import java.io.IOException;


public class GuardLeaderRefresh extends GuardLeader{


    private final RemoteRefreshCoordinator coordinator;
    private MessageParser parser = new MessageParser();


    private static final String COMPONENT = "Leader guard";


    private static Logger logger = new AndroidLogger();



    public GuardLeaderRefresh(RemoteRefreshCoordinator coordinator){
        super();
        this.coordinator = coordinator;
    }

    @Override
    public Runnable getListener() {
        return new ListenerStartCode();
    }

    private class ListenerStartCode implements Runnable {

        @Override
        public void run() {
            try {
                byte[] message = getConnection().readFromConnection();
                AbstractMessage parsedMessage = parser.parse(message);
                if (parsedMessage instanceof RefreshMessage) {
                    logger.logEvent(COMPONENT, "refresh request", "high");
                    RefreshMessage mes = (RefreshMessage) parsedMessage;

                    String remove = mes.getRemove();
                    String extra = (remove == null) ? "null" : remove;
                    logger.logEvent(COMPONENT, "received request to refresh", "low", extra);

                    coordinator.setMain(getConnection().getAddress());

                    coordinator.setRemove(remove);







                }
                else{
                    logger.logEvent(COMPONENT, "invalid request received (ignored)", "high");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
