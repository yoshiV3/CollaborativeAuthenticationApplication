package com.project.collaborativeauthenticationapplication.service.key.application;

import com.project.collaborativeauthenticationapplication.logger.AndroidLogger;
import com.project.collaborativeauthenticationapplication.logger.Logger;
import com.project.collaborativeauthenticationapplication.service.IllegalUseOfClosedTokenException;
import com.project.collaborativeauthenticationapplication.service.crypto.BigNumber;
import com.project.collaborativeauthenticationapplication.service.key.KeyToken;

import java.util.ArrayList;
import java.util.List;

public class CustomKeyPartDistributor extends CustomTokenConsumer<KeyToken> {
    private static Logger logger = new AndroidLogger();


    private KeyPartDistributionSession session;

    public void receiveKeyPartDistributionSession(KeyPartDistributionSession session)
    {
        this.session = session;
    }


    public void distribute(LocalKeyPartHandler local, CustomRemoteKeyPartHandler remote, CustomKeyGenerationPersistenceManager persistenceManager, KeyToken token) throws IllegalUseOfClosedTokenException {
        consumeToken(token);
        persistenceManager.receiveKeyDistributionSession(session);
        int localWeight = session.getLocalParticipant().getWeight();
        //local
        ArrayList<List<BigNumber>> localKeyParts = new ArrayList<>();
        for(int identifier=1; identifier <=localWeight; identifier++)
        {
            ArrayList<BigNumber> partsForParticipant= new ArrayList<>();
            partsForParticipant.add(session.getKeyPartsFor(identifier));
            localKeyParts.add(partsForParticipant);
        }
        logger.logEvent("Distributor", "request to distribute", "low");
        local.receiveLocalKeyPartsFromLocalSource(localKeyParts);
        //Remote: currently impossible

    }
}
