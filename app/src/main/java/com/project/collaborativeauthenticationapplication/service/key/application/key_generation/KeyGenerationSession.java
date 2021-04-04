package com.project.collaborativeauthenticationapplication.service.key.application.key_generation;

import com.project.collaborativeauthenticationapplication.service.Participant;
import com.project.collaborativeauthenticationapplication.service.key.application.AuthenticationSession;

public interface KeyGenerationSession extends RemoteParticipantIterator, AuthenticationSession {

    Participant getLocalParticipant();

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
