package com.project.collaborativeauthenticationapplication.service.key.application.key_generation;

import com.project.collaborativeauthenticationapplication.logger.AndroidLogger;
import com.project.collaborativeauthenticationapplication.logger.Logger;
import com.project.collaborativeauthenticationapplication.service.IllegalUseOfClosedTokenException;
import com.project.collaborativeauthenticationapplication.service.crypto.BigNumber;
import com.project.collaborativeauthenticationapplication.service.crypto.CryptoKeyShareGenerator;
import com.project.collaborativeauthenticationapplication.service.key.KeyToken;
import com.project.collaborativeauthenticationapplication.service.key.application.CustomTokenConsumer;

import java.util.ArrayList;
import java.util.List;

public class CustomLocalKeyShareGenerator extends CustomTokenConsumer implements LocalKeyPartHandler{
    
    private final CryptoKeyShareGenerator generator;
    private ArrayList<ArrayList<BigNumber>> keyParts = new ArrayList<>();
    private static Logger logger = new AndroidLogger();

    public CustomLocalKeyShareGenerator()
    {

        generator = new CryptoKeyShareGenerator();
    }

    public void generate(CustomKeyGenerationPersistenceManager persistenceManager, KeyToken token) throws IllegalUseOfClosedTokenException {
        ArrayList<BigNumber> shares;
        consumeToken(token);
        shares = generator.generate(keyParts);
        persistenceManager.receiveShares(shares);
        logger.logEvent("LOCAL SHARE GENERATOR", "generated new shates", "low", String.valueOf(shares.size()));
    }



    @Override
    public void receiveLocalKeyPartsFromLocalSource(List<List<BigNumber>> keyParts) {
        for (List<BigNumber> keyPart: keyParts)
        {
            this.keyParts.add(new ArrayList<BigNumber>(keyPart));
        }
        logger.logEvent("LOCAL SHARE GENERATOR", "Received new key parts", "low", String.valueOf(keyParts.size()));
    }
}
