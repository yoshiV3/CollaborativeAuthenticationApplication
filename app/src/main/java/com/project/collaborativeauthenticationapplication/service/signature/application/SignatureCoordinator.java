package com.project.collaborativeauthenticationapplication.service.signature.application;

import com.project.collaborativeauthenticationapplication.service.ServiceHandler;
import com.project.collaborativeauthenticationapplication.service.Task;
import com.project.collaborativeauthenticationapplication.service.crypto.BigNumber;

public interface SignatureCoordinator extends ServiceHandler {

    void sign(Task task);

    BigNumber getHash();
    BigNumber getSignature();
    BigNumber getMessage();
}
