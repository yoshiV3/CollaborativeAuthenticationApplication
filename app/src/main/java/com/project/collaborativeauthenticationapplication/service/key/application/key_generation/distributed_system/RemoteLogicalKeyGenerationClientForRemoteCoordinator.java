package com.project.collaborativeauthenticationapplication.service.key.application.key_generation.distributed_system;

import com.project.collaborativeauthenticationapplication.logger.AndroidLogger;
import com.project.collaborativeauthenticationapplication.logger.Logger;
import com.project.collaborativeauthenticationapplication.service.crypto.BigNumber;
import com.project.collaborativeauthenticationapplication.service.crypto.Point;
import com.project.collaborativeauthenticationapplication.service.general.IdentifiedParticipant;
import com.project.collaborativeauthenticationapplication.service.key.application.key_generation.local_system.control.protocol.KeyGenerationSession;
import com.project.collaborativeauthenticationapplication.service.network.AndroidCommunicationConnection;
import com.project.collaborativeauthenticationapplication.service.network.Communication;
import com.project.collaborativeauthenticationapplication.service.network.CustomCommunication;

import java.io.IOException;
import java.util.ArrayList;

public class RemoteLogicalKeyGenerationClientForRemoteCoordinator extends RemoteLogicalKeyGenerationClient{

    public RemoteLogicalKeyGenerationClientForRemoteCoordinator(IdentifiedParticipant participant, KeyGenerationSession session) {
        super(participant, session);
    }



    private static Logger logger = new AndroidLogger();


    @Override
    public void receiveParts(ArrayList<BigNumber> parts, Point publicKey, KeyGenerationCoordinator coordinator) {
        sendPartsToRemoteClient(parts, publicKey, coordinator);
    }

    @Override
    public void generateParts(KeyGenerationCoordinator coordinator) {
        Communication instance = CustomCommunication.getInstance();
        AndroidCommunicationConnection connection = null;
        logger.logEvent("Remote client", "invitation", "low");
        try {
            if (getSession().getLocalParticipant().getIdentifier() < getIdentifier()){
                setConnection(instance.getConnectionWith(getAddress()));
                setState(STATE_CONNECTED);
                connection = getConnection();
                getEncoder().clear();
                byte[] message = getEncoder().makeInvitationMessage(getSession(),getIdentifier());
                synchronized (getSync()){
                    setSending(true);

                    connection.createIOtStreams();

                    connection.writeToConnection(message);
                    setState(STATE_INVITED);
                    setSending(false);
                    getSync().notify();
                }
            } else {
                setConnection(instance.getConnectionWith(getAddress()));
                setState(STATE_CONNECTED);
                //TO DO
            }
            waitForAndHandleAnswer(coordinator);
        } catch (IOException e) {
            handleError(instance, coordinator, e, "IO exception during/before invitation could be send");
        }
    }
}
