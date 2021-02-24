package com.project.collaborativeauthenticationapplication.service.key.application;

import com.project.collaborativeauthenticationapplication.service.Participant;
import com.project.collaborativeauthenticationapplication.service.RemoteParticipantIterator;

public interface KeyGenerationSession extends RemoteParticipantIterator {

    Participant getLocalParticipant();
}
