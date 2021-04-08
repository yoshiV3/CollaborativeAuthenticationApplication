package com.project.collaborativeauthenticationapplication.service.signature.application;

import com.project.collaborativeauthenticationapplication.service.FeedbackRequester;
import com.project.collaborativeauthenticationapplication.service.crypto.BigNumber;

public interface VerificationClient {

    void verify(BigNumber signature, BigNumber hash, BigNumber message, String applicationName, String login, FeedbackRequester requester);
}
