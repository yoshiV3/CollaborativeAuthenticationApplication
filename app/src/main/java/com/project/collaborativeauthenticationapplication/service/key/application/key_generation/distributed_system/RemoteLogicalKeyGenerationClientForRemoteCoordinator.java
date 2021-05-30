package com.project.collaborativeauthenticationapplication.service.key.application.key_generation.distributed_system;

import android.text.format.Time;

import com.project.collaborativeauthenticationapplication.alternative.network.AndroidConnection;
import com.project.collaborativeauthenticationapplication.alternative.network.Network;
import com.project.collaborativeauthenticationapplication.logger.AndroidLogger;
import com.project.collaborativeauthenticationapplication.logger.Logger;
import com.project.collaborativeauthenticationapplication.service.crypto.BigNumber;
import com.project.collaborativeauthenticationapplication.service.crypto.Point;
import com.project.collaborativeauthenticationapplication.service.general.IdentifiedParticipant;
import com.project.collaborativeauthenticationapplication.service.key.application.key_generation.local_system.control.protocol.KeyGenerationSession;
import com.project.collaborativeauthenticationapplication.service.network.AndroidBiDirectionalCommunicationConnection;
import com.project.collaborativeauthenticationapplication.service.network.Communication;
import com.project.collaborativeauthenticationapplication.service.network.CustomCommunication;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class RemoteLogicalKeyGenerationClientForRemoteCoordinator extends RemoteLogicalKeyGenerationClient{

    public RemoteLogicalKeyGenerationClientForRemoteCoordinator(IdentifiedParticipant participant, KeyGenerationSession session) {
        super(participant, session);
    }



    private static Logger logger = new AndroidLogger();


    @Override
    public void receiveParts(ArrayList<BigNumber> parts, Point publicKey, KeyGenerationCoordinator coordinator) {
        sendPartsToRemoteClient(parts, publicKey, coordinator);
        getConnection().pushToCoordinator();
    }

    @Override
    public void generateParts(KeyGenerationCoordinator coordinator) {
        Network instance = Network.getInstance();
        AndroidConnection connection = instance.getConnectionWithInMode(getAddress(), AndroidConnection.MODE_SLAVE_MULTI);
        logger.logEvent(COMPONENT, "found connection", "low");
        setConnection(connection);
        logger.logEvent("Remote client", "generate parts", "low");
        try {

            /**
            if (getSession().getLocalParticipant().getIdentifier() < getIdentifier()){
                TimeUnit.SECONDS.sleep(4);
                logger.logEvent("Remote client", "connecting", "low");
                setConnection(instance.getConnectionWith(getAddress()));
                setState(STATE_CONNECTED);
                connection = getConnection();
                getEncoder().clear();
                byte[] message = getEncoder().makeConnectMessage(getSession().getApplicationName(), getSession().getLogin());
                synchronized (getSync()){
                    setSending(true);

                    connection.createIOtStreams();

                    connection.writeToConnection(message);
                    setState(STATE_INVITED);
                    setSending(false);
                    getSync().notify();
                }
            } else if (getIdentifier() == 1) {
                setConnection(instance.getConnectionWith(getAddress()));
                setState(STATE_CONNECTED);
            } else {
                logger.logEvent("Remote Client", "waiting", "low");
                TimeUnit.MINUTES.sleep(1);
            }
             **/
            waitForAndHandleAnswer(coordinator);
        } catch (IOException e) {
            handleError(coordinator, e, "IO exception during/before invitation could be send");
        }
    }
}
