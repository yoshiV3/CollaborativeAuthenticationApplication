package com.project.collaborativeauthenticationapplication.service.key.application.key_generation;

import com.project.collaborativeauthenticationapplication.service.IllegalUseOfClosedTokenException;
import com.project.collaborativeauthenticationapplication.service.key.KeyToken;
import com.project.collaborativeauthenticationapplication.service.network.UnreachableParticipantException;

public interface KeyGenerationDistributedInvitationSender {


    void receiveKeyGenerationSession(KeyGenerationSession session);
    void sendInvitations(KeyToken token) throws IllegalUseOfClosedTokenException, UnreachableParticipantException;

    void passSessionTo(CustomLocalKeyPartGenerator keyPartGenerator);
}
