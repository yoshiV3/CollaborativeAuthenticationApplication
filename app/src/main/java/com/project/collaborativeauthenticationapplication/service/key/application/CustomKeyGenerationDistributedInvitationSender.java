package com.project.collaborativeauthenticationapplication.service.key.application;

import com.project.collaborativeauthenticationapplication.service.IllegalUseOfClosedTokenException;
import com.project.collaborativeauthenticationapplication.service.Participant;
import com.project.collaborativeauthenticationapplication.service.key.KeyToken;
import com.project.collaborativeauthenticationapplication.service.network.UnreachableParticipantException;

public class CustomKeyGenerationDistributedInvitationSender extends CustomTokenConsumer<KeyToken> implements KeyGenerationDistributedInvitationSender {




    private KeyGenerationSession session;


    public CustomKeyGenerationDistributedInvitationSender() {}


    @Override
    public void receiveKeyGenerationSession(KeyGenerationSession session) {
        this.session = session;
    }

    @Override
    public void sendInvitations(KeyToken token) throws IllegalUseOfClosedTokenException,UnreachableParticipantException {
        consumeToken(token);
        for (Participant participant: session.getRemoteParticipantList())
        {
            throw new UnreachableParticipantException("Temporally unsupported functionality", participant);
        }
    }


}
