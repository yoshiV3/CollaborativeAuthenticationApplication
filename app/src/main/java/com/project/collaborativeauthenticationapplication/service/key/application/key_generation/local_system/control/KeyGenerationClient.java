package com.project.collaborativeauthenticationapplication.service.key.application.key_generation.local_system.control;




import com.project.collaborativeauthenticationapplication.service.Requester;
import com.project.collaborativeauthenticationapplication.service.ServiceHandler;
import com.project.collaborativeauthenticationapplication.service.crypto.BigNumber;
import com.project.collaborativeauthenticationapplication.service.crypto.Point;
import com.project.collaborativeauthenticationapplication.service.key.application.key_generation.local_system.FeedbackRequester;

import java.util.List;

public interface KeyGenerationClient extends ServiceHandler {

    void receiveKeyGenerationSession(Requester requester, KeyGenerationSession session);
    void calculatePartsAndPublicKey(Requester requester, List<BigNumber> parts, Point publicKey);
    void calculateShares(Requester requester, List<List<BigNumber>> parts);
    void checkCredentials(FeedbackRequester requester, String applicationName, String login);
    void receiveFinalPublicKey(Requester requester, Point publicKey);
    void persist(FeedbackRequester requester);
}
