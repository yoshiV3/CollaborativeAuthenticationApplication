package com.project.collaborativeauthenticationapplication.alternative.key.application;

import com.project.collaborativeauthenticationapplication.alternative.management.extend.RemoteExtendingCoordinator;
import com.project.collaborativeauthenticationapplication.logger.AndroidLogger;
import com.project.collaborativeauthenticationapplication.logger.Logger;
import com.project.collaborativeauthenticationapplication.service.network.messages.AbstractMessage;
import com.project.collaborativeauthenticationapplication.service.network.messages.ExtendCalculateMessage;
import com.project.collaborativeauthenticationapplication.service.network.messages.MessageParser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GuardLeaderExtending extends GuardLeader{



    private static final String COMPONENT =  "Guard leader extend";
    private final RemoteExtendingCoordinator coordinator;
    private MessageParser parser = new MessageParser();

    private static Logger logger = new AndroidLogger();

    public GuardLeaderExtending(RemoteExtendingCoordinator coordinator) {
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
                AbstractMessage parsedMessage =  parser.parse(message);
                String main = getConnection().getAddress();
                coordinator.setMain(main);
                if (parsedMessage instanceof ExtendCalculateMessage){
                    ExtendCalculateMessage extendCalculateMessage = (ExtendCalculateMessage) parsedMessage;
                    coordinator.setWeight(extendCalculateMessage.getWeight());
                    coordinator.setNewIdentifier(extendCalculateMessage.getNewIdentifier());
                    logger.logEvent(COMPONENT, "new ", "low", String.valueOf(extendCalculateMessage.getNewIdentifier()));
                    coordinator.setTargetAddress(extendCalculateMessage.getAddress());
                    coordinator.setDevice(extendCalculateMessage.getAddress());
                    logger.logEvent(COMPONENT, "address set ", "low", extendCalculateMessage.getAddress());
                    final String localAddress = extendCalculateMessage.getLocalAddress();
                    List<String> remotes = extendCalculateMessage.getRemotes();
                    final int[] weights = extendCalculateMessage.getWeights();
                    coordinator.setWeights(weights);
                    ArrayList<String> list = new ArrayList<>();
                    for (String device : remotes){
                        if (device.equals(localAddress)){
                            list.add("here");
                        } else if (device.equals("here")){
                            list.add(main);
                        } else {
                            list.add(device);
                        }
                    }
                    coordinator.setRemotes(list);

                    coordinator.setCalculating(true);

                    coordinator.ok();
                }


            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
