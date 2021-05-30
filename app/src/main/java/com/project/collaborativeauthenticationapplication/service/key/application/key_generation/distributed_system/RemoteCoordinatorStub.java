package com.project.collaborativeauthenticationapplication.service.key.application.key_generation.distributed_system;


import com.project.collaborativeauthenticationapplication.alternative.network.AndroidConnection;
import com.project.collaborativeauthenticationapplication.alternative.network.Network;
import com.project.collaborativeauthenticationapplication.logger.AndroidLogger;
import com.project.collaborativeauthenticationapplication.logger.Logger;
import com.project.collaborativeauthenticationapplication.service.network.AndroidBiDirectionalCommunicationConnection;
import com.project.collaborativeauthenticationapplication.service.network.Communication;
import com.project.collaborativeauthenticationapplication.service.network.CustomCommunication;
import com.project.collaborativeauthenticationapplication.service.network.messages.AbstractMessage;
import com.project.collaborativeauthenticationapplication.service.network.messages.MessageEncoder;
import com.project.collaborativeauthenticationapplication.service.network.messages.MessageParser;
import com.project.collaborativeauthenticationapplication.service.network.messages.YesMessage;

import java.io.IOException;

public class RemoteCoordinatorStub implements RemoteCoordinator {

    private final AndroidConnection connection;
    private final KeyGenerationCoordinator       coordinator;



    private static Logger logger   = new AndroidLogger();

    private MessageEncoder encoder = new MessageEncoder();

    private MessageParser parser   = new MessageParser();

    public RemoteCoordinatorStub(String address, KeyGenerationCoordinator coordinator){
        connection = Network.getInstance().getConnectionWith(address);
        this.coordinator = coordinator;
    }
    @Override
    public void voteYes() {
            logger.logEvent("Stub", "voted YES", "normal");
            connection.writeToConnection(encoder.makeVoteYesMessage());
            connection.pushForFinal();
            listenForResponse();

    }

    @Override
    public void voteNo() {
            logger.logEvent("Stub", "voted NO", "normal");
            connection.writeToConnection(encoder.makeVoteNo());
            connection.pushForFinal();
    }

    private void listenForResponse(){
        try {
            logger.logEvent("Remote stub", "Listening for response", "normal");
            byte[] message = connection.readFromConnection();
            logger.logEvent("Remote stub", "received result", "normal");
            AbstractMessage result = parser.parse(message);
            if(result instanceof YesMessage){
                logger.logEvent("Remote stub", "result ok", "low");
                coordinator.done();
            } else {
                logger.logEvent("Remote stub", "result not ok", "low");
                coordinator.rollback();
            }
        } catch (IOException e) {
            e.printStackTrace();
            logger.logError("Remote stub", "error during voting after vote was cast", "normal");
            e.printStackTrace();
        }
    }
}
