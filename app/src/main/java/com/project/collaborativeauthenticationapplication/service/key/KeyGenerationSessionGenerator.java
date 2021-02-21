package com.project.collaborativeauthenticationapplication.service.key;

import com.project.collaborativeauthenticationapplication.service.IllegalUseOfClosedTokenException;
import com.project.collaborativeauthenticationapplication.service.Participant;
import com.project.collaborativeauthenticationapplication.service.KeyToken;

import java.util.List;

public interface KeyGenerationSessionGenerator {

    void generateSession(List<Participant> participants, KeyToken token) throws IllegalUseOfClosedTokenException;

    void giveKeyGenerationSessionTo(KeyGenerationConnector connector);
}
