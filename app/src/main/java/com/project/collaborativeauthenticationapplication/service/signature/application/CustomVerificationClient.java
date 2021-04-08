package com.project.collaborativeauthenticationapplication.service.signature.application;

import com.project.collaborativeauthenticationapplication.logger.AndroidLogger;
import com.project.collaborativeauthenticationapplication.logger.Logger;
import com.project.collaborativeauthenticationapplication.service.CustomKeyViewManager;
import com.project.collaborativeauthenticationapplication.service.FeedbackRequester;
import com.project.collaborativeauthenticationapplication.service.crypto.BigNumber;
import com.project.collaborativeauthenticationapplication.service.crypto.CryptoVerificationProcessor;
import com.project.collaborativeauthenticationapplication.service.crypto.Point;

import java.util.ArrayList;

public class CustomVerificationClient implements VerificationClient{


    private static Logger logger = new AndroidLogger();
    CustomKeyViewManager keyViewManager = new CustomKeyViewManager();
    CryptoVerificationProcessor verificationProcessor =new CryptoVerificationProcessor();
    @Override
    public void verify(BigNumber signature, BigNumber hash, BigNumber message, String applicationName, String login, FeedbackRequester requester) {
        Point publicKey = keyViewManager.getPublicKeyForCredential(applicationName, login);
        ArrayList<BigNumber> signatureArr = new ArrayList<BigNumber>();
        signatureArr.add(signature);
        signatureArr.add(hash);
        Boolean correctness = verificationProcessor.verify(signatureArr, message, publicKey);
        requester.setResult(correctness);
        logger.logEvent("verification", "result", "high", String.valueOf(correctness));
        requester.signalJobDone();

    }
}
