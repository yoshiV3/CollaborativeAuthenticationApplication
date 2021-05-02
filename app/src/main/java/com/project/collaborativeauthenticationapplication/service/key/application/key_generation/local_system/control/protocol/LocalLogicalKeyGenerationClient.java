package com.project.collaborativeauthenticationapplication.service.key.application.key_generation.local_system.control.protocol;

import com.project.collaborativeauthenticationapplication.service.crypto.BigNumber;
import com.project.collaborativeauthenticationapplication.service.crypto.Point;
import com.project.collaborativeauthenticationapplication.service.general.IdentifiedParticipant;
import com.project.collaborativeauthenticationapplication.service.key.application.key_generation.KeyGenerationClient;
import com.project.collaborativeauthenticationapplication.service.key.application.key_generation.distributed_system.KeyGenerationCoordinator;
import com.project.collaborativeauthenticationapplication.service.key.application.key_generation.local_system.computations.CustomLocalKeyGenerator;

import java.util.ArrayList;
import java.util.List;

public class LocalLogicalKeyGenerationClient implements KeyGenerationClient {

    public static int STATE_INIT        = 0;
    public static int STATE_ERROR       = 1;
    public static int STATE_CLOSED      = 3;
    private  int numberOfParticipants = 0;

    private int state = STATE_INIT;


    private KeyGenerationSession session;


    private final IdentifiedParticipant participant;



    ArrayList<Point> publicKeyParts;

    ArrayList<ArrayList<BigNumber>> shareParts = null;


    private CustomLocalKeyGenerator keyGenerator          = new CustomLocalKeyGenerator();
    private int submittedParticipants;


    public LocalLogicalKeyGenerationClient(IdentifiedParticipant participant,KeyGenerationSession session){
        this.session              = session;
        this.participant          = participant;
        this.numberOfParticipants  = session.getRemoteParticipantList().size() +1;
    }

    @Override
    public int getState() {
        return state;
    }

    @Override
    public int getWeight() {
        return participant.getWeight();
    }

    @Override
    public int getIdentifier() {
        return participant.getIdentifier();
    }

    @Override
    public void generateParts(KeyGenerationCoordinator coordinator) {
        Point publicKey            = new Point(BigNumber.getZero(), BigNumber.getZero(), true);
        ArrayList<BigNumber> parts = new ArrayList<>();
        keyGenerator.calculatePartsAndPublicKey(session, parts, publicKey);
        coordinator.distributeParts(parts, publicKey);
    }

    @Override
    public void receiveParts(ArrayList<BigNumber> parts, Point publicKey, KeyGenerationCoordinator coordinator) {
        if (shareParts == null){
            shareParts      = new ArrayList<>();
            publicKeyParts  = new ArrayList<>();;
            for (int i = 0; i < session.getLocalParticipant().getWeight(); i++){
                ArrayList<BigNumber> part = new ArrayList<>();
                part.add(parts.get(i));
                shareParts.add(part);
            }
            submittedParticipants = 1;
        } else {
            for (int i = 0; i < session.getLocalParticipant().getWeight(); i++){
                shareParts.get(i).add(parts.get(i));

            }
            submittedParticipants += 1;
        }
        publicKeyParts.add(publicKey);
        if (submittedParticipants == numberOfParticipants){
            Point pb = keyGenerator.calculatePublicKey(publicKeyParts);
            ArrayList<BigNumber> shares = new ArrayList<>();
            ArrayList<List<BigNumber>> list = new ArrayList<>();
            list.addAll(shareParts);
            keyGenerator.calculateShares(list, shares);
            coordinator.submitShares(shares, pb);
        }
    }

    @Override
    public void close(boolean success) {
        state = STATE_CLOSED;
    }

    @Override
    public void abort() {
        state = STATE_CLOSED;
    }
}
