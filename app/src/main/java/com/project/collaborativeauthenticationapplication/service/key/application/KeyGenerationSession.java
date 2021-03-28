package com.project.collaborativeauthenticationapplication.service.key.application;

import com.project.collaborativeauthenticationapplication.service.Participant;

public interface KeyGenerationSession extends RemoteParticipantIterator, AuthenticationSession{

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
