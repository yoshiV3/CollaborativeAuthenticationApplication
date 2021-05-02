package com.project.collaborativeauthenticationapplication.service.key.application.key_generation.local_system.control.protocol;

import com.project.collaborativeauthenticationapplication.service.general.CustomIdentifiedParticipant;
import com.project.collaborativeauthenticationapplication.service.general.IdentifiedParticipant;
import com.project.collaborativeauthenticationapplication.service.general.Participant;
import com.project.collaborativeauthenticationapplication.service.key.KeyToken;
import com.project.collaborativeauthenticationapplication.service.key.application.CustomTokenConsumer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CustomKeyGenerationSessionGenerator extends CustomTokenConsumer<KeyToken> implements KeyGenerationSessionGenerator {




    public CustomKeyGenerationSessionGenerator() {}
    @Override
    public KeyGenerationSession  generateSession(List<Participant> participants, int threshold, String applicationName, String login )  {
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

        KeyGenerationSession session;
        ArrayList<Participant> remoteParticipants             = new ArrayList<>();
        IdentifiedParticipant localParticipant                = null;
        final ConfigurableParticipant      defaultLocalParticipant   = new ConfigurableParticipant();

        boolean skip = false;
        ArrayList<IdentifiedParticipant> remoteIdentifiedParticipants = new ArrayList<>();

        for (Participant participant: participants)
        {
            if (participant.isLocal())
            {
                if (participant instanceof  IdentifiedParticipant){
                    localParticipant = (IdentifiedParticipant) participant;
                    skip = true;
                } else {
                    defaultLocalParticipant.setWeight(participant.getWeight());
                    defaultLocalParticipant.setName(participant.getName());
                    defaultLocalParticipant.setAddress(participant.getAddress());
                    localParticipant = defaultLocalParticipant;
                }
            }
            else
            {
                if (participant instanceof  IdentifiedParticipant){
                    remoteIdentifiedParticipants.add((IdentifiedParticipant) participant);
                }
                remoteParticipants.add(participant);
            }
        } if (!skip){
            int currentIdentifier = localParticipant.getWeight() +1;
            for (Participant participant: remoteParticipants )
            {
                remoteIdentifiedParticipants.add(new CustomIdentifiedParticipant(participant, currentIdentifier));
                currentIdentifier  = currentIdentifier + participant.getWeight();
            }
        }

        final IdentifiedParticipant local = localParticipant;
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
                return local;
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
        return session;
    }
}
