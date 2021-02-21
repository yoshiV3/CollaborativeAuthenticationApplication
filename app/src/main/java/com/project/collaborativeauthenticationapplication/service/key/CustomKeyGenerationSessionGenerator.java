package com.project.collaborativeauthenticationapplication.service.key;

import com.project.collaborativeauthenticationapplication.service.IllegalUseOfClosedTokenException;
import com.project.collaborativeauthenticationapplication.service.Participant;
import com.project.collaborativeauthenticationapplication.service.KeyToken;

import java.util.Iterator;
import java.util.List;

public class CustomKeyGenerationSessionGenerator implements KeyGenerationSessionGenerator{


    private static KeyGenerationSessionGenerator instance = new CustomKeyGenerationSessionGenerator();

    public static KeyGenerationSessionGenerator getInstance()
    {
        return instance;
    }

    private KeyGenerationSession session;

    private CustomKeyGenerationSessionGenerator() {}
    @Override
    public void generateSession(List<Participant> participants, KeyToken token) throws IllegalUseOfClosedTokenException {
        if (token.isClosed())
        {
            throw new IllegalUseOfClosedTokenException( "generate session");
        }
        session = new KeyGenerationSession() {
            @Override
            public Iterator<Participant> getParticipantIterator() {
                return participants.iterator();
            }
        };
    }

    @Override
    public void giveKeyGenerationSessionTo(KeyGenerationConnector connector) {
        session = null;
    }
}
