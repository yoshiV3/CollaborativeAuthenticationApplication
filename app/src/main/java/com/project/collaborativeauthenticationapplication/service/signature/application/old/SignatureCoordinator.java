package com.project.collaborativeauthenticationapplication.service.signature.application.old;

import com.project.collaborativeauthenticationapplication.service.general.ServiceHandler;
import com.project.collaborativeauthenticationapplication.service.general.Task;
import com.project.collaborativeauthenticationapplication.service.crypto.BigNumber;

public interface SignatureCoordinator extends ServiceHandler {

    void sign(Task task);

    BigNumber getHash();
    BigNumber getSignature();
    BigNumber getMessage();
}
