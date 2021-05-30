package com.project.collaborativeauthenticationapplication.service.signature.application.local;

import com.project.collaborativeauthenticationapplication.service.crypto.BigNumber;
import com.project.collaborativeauthenticationapplication.service.general.FeedbackRequester;
import com.project.collaborativeauthenticationapplication.service.general.ServiceHandler;
import com.project.collaborativeauthenticationapplication.service.signature.application.SignatureClient;

import java.util.ArrayList;


public interface InformationSignatureClient extends SignatureClient, ServiceHandler {

    void checkInformationAboutCredential(String applicationName, DatabaseInformationRequester requester);

    void checkIfEnoughLocalShares(int numberOfShares, String applicationName, FeedbackRequester requester);

    void calculateFinalSignature(ArrayList<BigNumber> parts, SignatureRequester requester);
}
