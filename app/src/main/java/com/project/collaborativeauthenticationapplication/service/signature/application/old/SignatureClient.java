package com.project.collaborativeauthenticationapplication.service.signature.application.old;

import com.project.collaborativeauthenticationapplication.service.general.ServiceHandler;
import com.project.collaborativeauthenticationapplication.service.signature.application.local.DatabaseInformationRequester;
import com.project.collaborativeauthenticationapplication.service.signature.application.local.RandomnessRequester;

public interface SignatureClient extends ServiceHandler {

    void sign(SignatureTask task);

    void checkInformationAboutCredential(String applicationName, DatabaseInformationRequester requester);

    void generateRandomnessAndCalculateCommitments(RandomnessRequester requester);



}
