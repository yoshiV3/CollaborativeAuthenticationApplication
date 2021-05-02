package com.project.collaborativeauthenticationapplication.service.signature.application;


import com.project.collaborativeauthenticationapplication.service.signature.application.local.RandomnessRequester;
import com.project.collaborativeauthenticationapplication.service.signature.application.local.SignatureTask;


public interface SignatureClient {


    String getAddress();

    void sign(SignatureTask task);

    void generateRandomnessAndCalculateCommitments(RandomnessRequester requester);



}