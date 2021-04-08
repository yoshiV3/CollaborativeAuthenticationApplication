package com.project.collaborativeauthenticationapplication.service.signature.application;

import com.project.collaborativeauthenticationapplication.service.ServiceHandler;

public interface SignatureClient extends ServiceHandler {

    void sign(SignatureTask task);

}
