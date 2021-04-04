package com.project.collaborativeauthenticationapplication.service.key.application.key_generation;

import com.project.collaborativeauthenticationapplication.logger.AndroidLogger;
import com.project.collaborativeauthenticationapplication.logger.Logger;
import com.project.collaborativeauthenticationapplication.service.IllegalUseOfClosedTokenException;
import com.project.collaborativeauthenticationapplication.service.crypto.BigNumber;
import com.project.collaborativeauthenticationapplication.service.crypto.CryptoProcessor;
import com.project.collaborativeauthenticationapplication.service.crypto.Point;
import com.project.collaborativeauthenticationapplication.service.crypto.PolynomialGenerator;
import com.project.collaborativeauthenticationapplication.service.key.KeyToken;
import com.project.collaborativeauthenticationapplication.service.key.application.CustomTokenConsumer;

import java.util.ArrayList;

public class CustomLocalKeyPartGenerator extends CustomTokenConsumer<KeyToken> {


    private static final String COMPONENT_NAME                         = "KEY PART GENERATOR" ;
    private static final String EVENT_START_GENERATE_LOCAL_PARTS       = "Key part generation was initiated";
    private static final String EVENT_DONE_GENERATE_LOCAL_PARTS        = "Key part generation was finished" ;


    private final PolynomialGenerator polynomialGenerator;
    private final CryptoProcessor     keyPartGenerator;

    private Logger logger = new AndroidLogger();
            ;
    private KeyGenerationSession       session;

    private KeyPartDistributionSession keyPartDistributionSession;


    public CustomLocalKeyPartGenerator()
    {
         polynomialGenerator = new PolynomialGenerator();
         keyPartGenerator    = new CryptoProcessor();

    }

    public void generate(KeyToken token) throws IllegalUseOfClosedTokenException {
        consumeToken(token);

        int threshold      = session.getThreshold();
        int localWeight    = session.getLocalParticipant().getWeight();
        int totalWeight    = session.getTotalWeight();


        String extra = String.valueOf(threshold) + "," + String.valueOf(localWeight) + "," + String.valueOf(totalWeight);
        logger.logEvent(COMPONENT_NAME, EVENT_START_GENERATE_LOCAL_PARTS, "low", extra );

        ArrayList<BigNumber> keyParts               = new ArrayList<>();
        Point publicKey                             = new Point(BigNumber.getZero(), BigNumber.getZero(), true);
        ArrayList<ArrayList<BigNumber>> polynomials = new ArrayList<>();

        for (int virtualParticipant = 1; virtualParticipant <= localWeight; virtualParticipant++)
        {
            polynomials.add(polynomialGenerator.generatePoly(threshold));
        }

        keyPartGenerator.generateParts(totalWeight, polynomials, keyParts, publicKey );

        keyPartDistributionSession = new CustomKeyPartDistributionSession(session,keyParts, publicKey);
        session =null;
        logger.logEvent(COMPONENT_NAME, EVENT_DONE_GENERATE_LOCAL_PARTS, "low", extra );

    }

    public void receiveSession(KeyGenerationSession  session)
    {
        this.session = session;
    }


    public void passKeyPartDistributionSessionTo(CustomKeyPartDistributor distributor)
    {
        distributor.receiveKeyPartDistributionSession(keyPartDistributionSession);
        keyPartDistributionSession = null;

    }





}
