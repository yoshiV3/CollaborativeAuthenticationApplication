package com.project.collaborativeauthenticationapplication.service.controller;

import com.project.collaborativeauthenticationapplication.alternative.key.KeyToken;
import com.project.collaborativeauthenticationapplication.service.general.ServiceStateException;
import com.project.collaborativeauthenticationapplication.service.general.IllegalNumberOfTokensException;
import com.project.collaborativeauthenticationapplication.service.general.SignatureToken;

public interface AuthenticationServiceController {

    boolean isActive();
    boolean isEnabled();

    boolean hasFreeKeyGenerationToken();
    boolean hasFreeSignatureTokens();


     KeyToken getNewKeyToken() throws IllegalNumberOfTokensException, ServiceStateException;

     SignatureToken getNewSignatureToken() throws ServiceStateException, IllegalNumberOfTokensException;


}