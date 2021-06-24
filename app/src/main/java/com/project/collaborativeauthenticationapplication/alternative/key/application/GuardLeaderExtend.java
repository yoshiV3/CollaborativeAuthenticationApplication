package com.project.collaborativeauthenticationapplication.alternative.key.application;

import com.project.collaborativeauthenticationapplication.alternative.management.extend.ExtendCoordinator;
import com.project.collaborativeauthenticationapplication.logger.AndroidLogger;
import com.project.collaborativeauthenticationapplication.logger.Logger;
import com.project.collaborativeauthenticationapplication.service.network.messages.AbstractMessage;
import com.project.collaborativeauthenticationapplication.service.network.messages.ExtendStartMessage;
import com.project.collaborativeauthenticationapplication.service.network.messages.MessageParser;
import com.project.collaborativeauthenticationapplication.service.network.messages.RefreshMessage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class GuardLeaderExtend extends GuardLeader{


    private static final String COMPONENT =  "Guard leader extend EX";
    private final ExtendCoordinator coordinator;
    private MessageParser parser = new MessageParser();

    private static Logger logger = new AndroidLogger();

    public GuardLeaderExtend(ExtendCoordinator coordinator) {
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

                logger.logEvent(COMPONENT, "received request", "low");

                ExtendStartMessage extendMessage = (ExtendStartMessage) parsedMessage;

                coordinator.setMain(getConnection().getAddress());

                coordinator.setThreshold(extendMessage.getThreshold());
                logger.logEvent(COMPONENT, "new requested threshold", "low", String.valueOf(extendMessage.getThreshold()));

                coordinator.setApplicationName(extendMessage.getApplicationName());

                coordinator.setNewIdentifier(extendMessage.getNewIdentifier());


                logger.logEvent(COMPONENT, "new requested identifier", "low", String.valueOf(extendMessage.getNewIdentifier()));


                coordinator.setPublicKey(extendMessage.getPublicKey());


                ArrayList<String> calculatingRemotes = new ArrayList<>(extendMessage.getCalculatingRemotes());

                coordinator.setCalculatingRemotes(calculatingRemotes);


                coordinator.setMain(getConnection().getAddress());

                HashMap<String, int[]> remotes = extendMessage.getRemotes();
                for (String remote : remotes.keySet()){
                    logger.logEvent(COMPONENT, "adding remote", "low", remote);
                    if (remote.equals("here")){
                        String address = getConnection().getAddress();
                        logger.logEvent(COMPONENT, "adding remote", "low", address);
                        coordinator.setMain(address);
                        coordinator.addRemote(address, remotes.get(remote) );
                    } else {
                        coordinator.addRemote(remote, remotes.get(remote) );
                    }
                }

                coordinator.allSet();


            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
