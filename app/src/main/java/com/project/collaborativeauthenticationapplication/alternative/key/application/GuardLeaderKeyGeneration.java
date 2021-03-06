package com.project.collaborativeauthenticationapplication.alternative.key.application;

import com.project.collaborativeauthenticationapplication.logger.AndroidLogger;
import com.project.collaborativeauthenticationapplication.logger.Logger;



import com.project.collaborativeauthenticationapplication.service.general.IdentifiedParticipant;
import com.project.collaborativeauthenticationapplication.service.general.Participant;
import com.project.collaborativeauthenticationapplication.service.key.application.key_generation.KeyGenerationClient;
import com.project.collaborativeauthenticationapplication.service.key.application.key_generation.ThreadedKeyGenerationClient;
import com.project.collaborativeauthenticationapplication.service.key.application.key_generation.distributed_system.CustomIdentifiedParticipant;
import com.project.collaborativeauthenticationapplication.service.key.application.key_generation.distributed_system.RemoteKeyGenerationCoordinator;
import com.project.collaborativeauthenticationapplication.service.key.application.key_generation.distributed_system.RemoteLogicalKeyGenerationClientForRemoteCoordinator;
import com.project.collaborativeauthenticationapplication.service.network.messages.AbstractMessage;
import com.project.collaborativeauthenticationapplication.service.network.messages.InvitationMessage;
import com.project.collaborativeauthenticationapplication.service.network.messages.MessageParser;


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GuardLeaderKeyGeneration extends GuardLeader {

    private final RemoteKeyGenerationCoordinator coordinator;
    private MessageParser parser = new MessageParser();


    private static final String COMPONENT = "Leader guard";


    private static Logger logger = new AndroidLogger();



    public GuardLeaderKeyGeneration(RemoteKeyGenerationCoordinator coordinator){
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
                if (parsedMessage instanceof InvitationMessage) {
                    InvitationMessage mes = (InvitationMessage) parsedMessage;
                    String extra = mes.getApplicationName();
                    logger.logEvent(COMPONENT, "submitting credential details", "low", extra);
                    coordinator.submitLoginDetails(mes.getApplicationName());
                    extra = String.valueOf(mes.getThreshold());
                    logger.logEvent(COMPONENT, "submitting credential details: threshold", "low", extra);
                    coordinator.submitThreshold(mes.getThreshold());
                    ArrayList<Participant> participants = new ArrayList<>();
                    List<IdentifiedParticipant> list = mes.getParticipants();
                    int lowestIdentifier = mes.getTotalWeight();
                    for (IdentifiedParticipant part : list) {
                        participants.add(part);
                        if (part.getIdentifier() < lowestIdentifier) {
                            lowestIdentifier = part.getIdentifier();
                        }
                    }
                    CustomIdentifiedParticipant main = new CustomIdentifiedParticipant(1, getConnection().getAddress(), lowestIdentifier - 1, false);
                    participants.add(main);
                    KeyGenerationClient client = new RemoteLogicalKeyGenerationClientForRemoteCoordinator(main, null);
                    ThreadedKeyGenerationClient threadedKeyGenerationClient = new ThreadedKeyGenerationClient(client);
                    coordinator.addMainClient(threadedKeyGenerationClient);
                    coordinator.addCoordinatorStub(main.getAddress());
                    coordinator.submitSelection(participants);
                   // coordinator.run();
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
