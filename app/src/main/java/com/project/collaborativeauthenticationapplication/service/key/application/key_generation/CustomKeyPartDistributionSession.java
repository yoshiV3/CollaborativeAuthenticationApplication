package com.project.collaborativeauthenticationapplication.service.key.application.key_generation;

import com.project.collaborativeauthenticationapplication.service.IdentifiedParticipant;
import com.project.collaborativeauthenticationapplication.service.Participant;
import com.project.collaborativeauthenticationapplication.service.crypto.BigNumber;
import com.project.collaborativeauthenticationapplication.service.crypto.Point;

import java.util.ArrayList;
import java.util.List;

public class CustomKeyPartDistributionSession implements KeyPartDistributionSession{



    private KeyGenerationSession parentSession;

    private ArrayList<BigNumber> keyParts = new ArrayList<>();

    Point publicKey;

    public CustomKeyPartDistributionSession(KeyGenerationSession session, List<BigNumber> generatedKeyParts, Point publicKey)
    {
        this.parentSession = session;
        keyParts.addAll(generatedKeyParts);
        this.publicKey = publicKey;

    }

    @Override
    public Participant getLocalParticipant() {
        return parentSession.getLocalParticipant();
    }

    @Override
    public int getThreshold() {
        return parentSession.getThreshold();
    }

    @Override
    public List<IdentifiedParticipant> getRemoteParticipantList() {
        return parentSession.getRemoteParticipantList();
    }

    @Override
    public BigNumber getKeyPartsFor(int identifier) {
        return keyParts.get(identifier-1);
    } //sum already done inside the c code

    @Override
    public Point getPublicKey() {
        return null;
    }

    @Override
    public String getApplicationName() {
        return parentSession.getApplicationName();
    }

    @Override
    public String getLogin() {
        return parentSession.getLogin();
    }
}
