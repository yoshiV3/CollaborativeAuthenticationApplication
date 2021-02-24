package com.project.collaborativeauthenticationapplication.service.key.application;

import com.project.collaborativeauthenticationapplication.service.IllegalUseOfClosedTokenException;
import com.project.collaborativeauthenticationapplication.service.key.KeyToken;
import com.project.collaborativeauthenticationapplication.service.network.UnreachableParticipantException;

public interface KeyGenerationDistributedInvitationSender {


    void receiveKeyGenerationSession(KeyGenerationSession session);
    void sendInvitations(KeyToken token) throws IllegalUseOfClosedTokenException, UnreachableParticipantException;
}
