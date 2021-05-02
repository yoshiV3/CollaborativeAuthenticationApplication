package com.project.collaborativeauthenticationapplication.service.key.application.key_generation.distributed_system;


import com.project.collaborativeauthenticationapplication.logger.AndroidLogger;
import com.project.collaborativeauthenticationapplication.logger.Logger;
import com.project.collaborativeauthenticationapplication.service.network.AndroidCommunicationConnection;
import com.project.collaborativeauthenticationapplication.service.network.Communication;
import com.project.collaborativeauthenticationapplication.service.network.CustomCommunication;
import com.project.collaborativeauthenticationapplication.service.network.messages.AbstractMessage;
import com.project.collaborativeauthenticationapplication.service.network.messages.MessageEncoder;
import com.project.collaborativeauthenticationapplication.service.network.messages.MessageParser;
import com.project.collaborativeauthenticationapplication.service.network.messages.YesMessage;

import java.io.IOException;

public class RemoteCoordinatorStub implements RemoteCoordinator {

    private final AndroidCommunicationConnection connection;
    private final KeyGenerationCoordinator       coordinator;



    private static Logger logger   = new AndroidLogger();

    private MessageEncoder encoder = new MessageEncoder();

    private MessageParser parser   = new MessageParser();

    public RemoteCoordinatorStub(String address, KeyGenerationCoordinator coordinator) throws IOException {
        connection = CustomCommunication.getInstance().getConnectionWith(address);
        this.coordinator = coordinator;
    }
    @Override
    public void voteYes() {
        try {
            logger.logEvent("Stub", "voted YES", "normal");
            connection.writeToConnection(encoder.makeVoteYesMessage());
            listenForResponse();
        } catch (IOException e) {
            handleError(CustomCommunication.getInstance(), coordinator, e, "Exception while voting");
        }

    }

    @Override
    public void voteNo() {
        try {
            logger.logEvent("Stub", "voted NO", "normal");
            connection.writeToConnection(encoder.makeVoteNo());
        } catch (IOException e) {
            handleError(CustomCommunication.getInstance(), coordinator, e, "Exception while voting");
        }
    }

    private void listenForResponse(){
        try {
            logger.logEvent("Remote stub", "Listening for response", "normal");
            byte[] message = connection.readFromConnection();
            logger.logEvent("Remote stub", "received result", "normal");
            AbstractMessage result = parser.parse(message);
            if(result instanceof YesMessage){
                coordinator.done();
            } else {
                coordinator.rollback();
            }
        } catch (IOException e) {
            e.printStackTrace();
            Communication instance = CustomCommunication.getInstance();
            logger.logError("Remote stub", "error during voting after vote was cast", "normal");
            instance.handleBrokenConnection(connection);
            e.printStackTrace();
        }
    }


    protected void handleError(Communication instance, KeyGenerationCoordinator coordinator, IOException e, String s) {
        e.printStackTrace();
        logger.logError("Remote client", s, "normal");
        instance.handleBrokenConnection(connection);
        coordinator.abort();
    }
}
