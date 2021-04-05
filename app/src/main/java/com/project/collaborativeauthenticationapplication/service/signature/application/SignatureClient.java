package com.project.collaborativeauthenticationapplication.service.signature.application;

import com.project.collaborativeauthenticationapplication.service.Client;

public interface SignatureClient extends Client {

    void sign(SignatureTask task);

}
