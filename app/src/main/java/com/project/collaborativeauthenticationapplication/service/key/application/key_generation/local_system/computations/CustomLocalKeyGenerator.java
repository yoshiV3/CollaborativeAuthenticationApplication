package com.project.collaborativeauthenticationapplication.service.key.application.key_generation.local_system.computations;

import com.project.collaborativeauthenticationapplication.logger.AndroidLogger;
import com.project.collaborativeauthenticationapplication.logger.Logger;
import com.project.collaborativeauthenticationapplication.service.crypto.BigNumber;
import com.project.collaborativeauthenticationapplication.service.crypto.CryptoKeyShareGenerator;
import com.project.collaborativeauthenticationapplication.service.crypto.CryptoProcessor;
import com.project.collaborativeauthenticationapplication.service.crypto.Point;
import com.project.collaborativeauthenticationapplication.service.crypto.RandomnessGenerator;
import com.project.collaborativeauthenticationapplication.service.key.application.key_generation.local_system.control.protocol.KeyGenerationSession;

import java.util.ArrayList;
import java.util.List;

public class CustomLocalKeyGenerator {

    private static final String COMPONENT_NAME                         = "KEY PART GENERATOR" ;
    private static final String EVENT_START_GENERATE_LOCAL_PARTS       = "Key part generation was initiated";
    private static final String EVENT_DONE_GENERATE_LOCAL_PARTS        = "Key part generation was finished" ;


    private final RandomnessGenerator     randomnessGenerator;
    private final CryptoProcessor         keyPartGenerator;
    private final CryptoKeyShareGenerator shareGenerator;



    private Logger logger = new AndroidLogger();

    public CustomLocalKeyGenerator()
    {
        randomnessGenerator = new RandomnessGenerator();
        keyPartGenerator    = new CryptoProcessor();
        shareGenerator      = new CryptoKeyShareGenerator();

    }

    public void  calculatePartsAndPublicKey(KeyGenerationSession session, List<BigNumber> parts, Point publicKey){
        int threshold      = session.getThreshold();
        int localWeight    = session.getLocalParticipant().getWeight();
        int totalWeight    = session.getTotalWeight();


        String extra = String.valueOf(threshold) + "," + String.valueOf(localWeight) + "," + String.valueOf(totalWeight);
        logger.logEvent(COMPONENT_NAME, EVENT_START_GENERATE_LOCAL_PARTS, "low", extra );

        ArrayList<BigNumber> keyParts               = new ArrayList<>();
        ArrayList<ArrayList<BigNumber>> polynomials = new ArrayList<>();

        ArrayList<BigNumber> poly = randomnessGenerator.generatePoly(threshold - 1);
        polynomials.add(poly); // only polynomials for the local participants
        logger.logEvent(COMPONENT_NAME, "New polynomial", "low", String.valueOf(poly.size()));

        keyPartGenerator.generateParts(totalWeight, polynomials, keyParts, publicKey ); // calculate the parts of all the other participants and a part of the public key
        logger.logEvent(COMPONENT_NAME, EVENT_DONE_GENERATE_LOCAL_PARTS+ " we have so many parts", "low", String.valueOf(keyParts.size()) );
        parts.addAll(keyParts);
        logger.logEvent(COMPONENT_NAME, EVENT_DONE_GENERATE_LOCAL_PARTS, "low", extra );
    }

    public void calculateShares(List<List<BigNumber>> parts, List<BigNumber> shares ){
        ArrayList<ArrayList<BigNumber>> partsArr = new ArrayList<ArrayList<BigNumber>>();
        for(List<BigNumber> list: parts){
            partsArr.add(new ArrayList<>(list));
        }
        shares.addAll(shareGenerator.generate(partsArr));
    }

    public Point calculatePublicKey(ArrayList<Point> parts){
        return shareGenerator.generatePublicKey(parts);
    }

}
