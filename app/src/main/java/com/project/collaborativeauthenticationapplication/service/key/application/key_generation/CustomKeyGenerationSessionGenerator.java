package com.project.collaborativeauthenticationapplication.service.key.application.key_generation;

import com.project.collaborativeauthenticationapplication.service.CustomIdentifiedParticipant;
import com.project.collaborativeauthenticationapplication.service.IdentifiedParticipant;
import com.project.collaborativeauthenticationapplication.service.IllegalUseOfClosedTokenException;
import com.project.collaborativeauthenticationapplication.service.Participant;
import com.project.collaborativeauthenticationapplication.service.key.KeyToken;
import com.project.collaborativeauthenticationapplication.service.key.application.CustomTokenConsumer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CustomKeyGenerationSessionGenerator extends CustomTokenConsumer<KeyToken> implements KeyGenerationSessionGenerator{



    private KeyGenerationSession session;

    public CustomKeyGenerationSessionGenerator() {}
    @Override
    public void generateSession(List<Participant> participants, int threshold, String applicationName, String login, KeyToken token) throws IllegalUseOfClosedTokenException {
        class ConfigurableParticipant implements IdentifiedParticipant
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

            @Override
            public int getIdentifier() {
                return 1;
            }
        }
        consumeToken(token);
        ArrayList<Participant> remoteParticipants             = new ArrayList<>();
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
        ArrayList<IdentifiedParticipant> remoteIdentifiedParticipants = new ArrayList<>();
        int currentIdentifier = localParticipant.getWeight() +1;
        for (Participant participant: remoteParticipants )
        {
            remoteIdentifiedParticipants.add(new CustomIdentifiedParticipant(participant, currentIdentifier));
            currentIdentifier  = currentIdentifier + participant.getWeight();
        }
        session = new KeyGenerationSession() {

            @Override
            public String getApplicationName() {
                return applicationName;
            }

            @Override
            public String getLogin() {
                return login;
            }

            @Override
            public IdentifiedParticipant getLocalParticipant() {
                return localParticipant;
            }

            @Override
            public int getThreshold() {
                return threshold;
            }

            @Override
            public List<IdentifiedParticipant> getRemoteParticipantList() {
                return Collections.unmodifiableList(remoteIdentifiedParticipants);
            }
        };
    }

    @Override
    public void giveKeyGenerationSessionTo(KeyGenerationDistributedInvitationSender invitationSender) {
        invitationSender.receiveKeyGenerationSession(session);
        session = null;
    }
}
