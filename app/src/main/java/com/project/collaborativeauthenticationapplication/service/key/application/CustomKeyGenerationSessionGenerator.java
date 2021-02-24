package com.project.collaborativeauthenticationapplication.service.key.application;

import com.project.collaborativeauthenticationapplication.service.IllegalUseOfClosedTokenException;
import com.project.collaborativeauthenticationapplication.service.Participant;
import com.project.collaborativeauthenticationapplication.service.key.KeyToken;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class CustomKeyGenerationSessionGenerator extends CustomTokenConsumer<KeyToken> implements KeyGenerationSessionGenerator{



    private KeyGenerationSession session;

    public CustomKeyGenerationSessionGenerator() {}
    @Override
    public void generateSession(List<Participant> participants, KeyToken token) throws IllegalUseOfClosedTokenException {
        class ConfigurableParticipant implements Participant
        {
            String name;
            String address;
            int    weight;

            @Override
            public String getName() {
                return name;
            }

            public void setName(String name)
            {
                this.name = name;
            }

            @Override
            public String getAddress() {
                return address;
            }

            public void setAddress(String address) {
                this.address = address;
            }

            @Override
            public void setWeight(int weight) {
                this.weight = weight;
            }

            @Override
            public int getWeight() {
                return weight;
            }

            @Override
            public boolean isLocal() {
                return true;
            }
        }
        consumeToken(token);
        ArrayList<Participant> remoteParticipants = new ArrayList<>();
        final ConfigurableParticipant      localParticipant   = new ConfigurableParticipant();

        for (Participant participant: participants)
        {
            if (participant.isLocal())
            {
                localParticipant.setWeight(participant.getWeight());
                localParticipant.setName(participant.getName());
                localParticipant.setAddress(participant.getAddress());
            }
            else
            {
                remoteParticipants.add(participant);
            }
        }
        session = new KeyGenerationSession() {

            @Override
            public Participant getLocalParticipant() {
                return localParticipant;
            }

            @Override
            public List<Participant> getRemoteParticipantList() {
                return remoteParticipants;
            }
        };
    }

    @Override
    public void giveKeyGenerationSessionTo(KeyGenerationDistributedInvitationSender invitationSender) {
        invitationSender.receiveKeyGenerationSession(session);
        session = null;
    }
}
