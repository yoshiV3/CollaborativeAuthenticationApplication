package com.project.collaborativeauthenticationapplication.service.key.application.key_generation.local_system.control.protocol;

import com.project.collaborativeauthenticationapplication.service.general.IdentifiedParticipant;
import com.project.collaborativeauthenticationapplication.service.general.Participant;
import com.project.collaborativeauthenticationapplication.service.key.application.AuthenticationSession;

public interface KeyGenerationSession extends RemoteParticipantIterator, AuthenticationSession {

    IdentifiedParticipant getLocalParticipant();

    int getThreshold();

    default int getTotalWeight()
    {
        int total = getLocalParticipant().getWeight();
        for (Participant participant : getRemoteParticipantList())
        {
            total += participant.getWeight();
        }
        return total;
    }
}
