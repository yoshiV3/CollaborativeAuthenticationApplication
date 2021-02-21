package com.project.collaborativeauthenticationapplication.service.controller;

import com.project.collaborativeauthenticationapplication.service.KeyToken;
import com.project.collaborativeauthenticationapplication.service.ServiceStateException;
import com.project.collaborativeauthenticationapplication.service.IllegalNumberOfTokensException;
import com.project.collaborativeauthenticationapplication.service.SignatureToken;

public interface AuthenticationServicePool {

    boolean isActive();
    boolean isEnabled();

    boolean hasFreeKeyGenerationToken();
    boolean hasFreeSignatureTokens();


     KeyToken getNewKeyToken() throws IllegalNumberOfTokensException, ServiceStateException;

     SignatureToken getNewSignatureToken();


}