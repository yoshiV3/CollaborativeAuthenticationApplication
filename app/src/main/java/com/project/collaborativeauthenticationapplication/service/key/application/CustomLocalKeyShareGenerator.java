package com.project.collaborativeauthenticationapplication.service.key.application;

import com.project.collaborativeauthenticationapplication.service.IllegalUseOfClosedTokenException;
import com.project.collaborativeauthenticationapplication.service.crypto.BigNumber;
import com.project.collaborativeauthenticationapplication.service.crypto.CryptoKeyShareGenerator;
import com.project.collaborativeauthenticationapplication.service.key.KeyToken;

import java.util.ArrayList;
import java.util.List;

public class CustomLocalKeyShareGenerator extends  CustomTokenConsumer implements LocalKeyPartHandler{
    
    private final CryptoKeyShareGenerator generator;
    private ArrayList<ArrayList<BigNumber>> keyParts = new ArrayList<>();
    ArrayList<BigNumber> shares;

    public CustomLocalKeyShareGenerator()
    {

        generator = new CryptoKeyShareGenerator();
    }

    public void generate(KeyToken token) throws IllegalUseOfClosedTokenException {
        consumeToken(token);
        shares = generator.generate(keyParts);

    }

    @Override
    public void receiveLocalKeyPartsFromLocalSource(List<List<BigNumber>> keyParts) {
        for (List<BigNumber> keyPart: keyParts)
        {
            this.keyParts.add(new ArrayList<BigNumber>(keyPart));
        }

    }
}
