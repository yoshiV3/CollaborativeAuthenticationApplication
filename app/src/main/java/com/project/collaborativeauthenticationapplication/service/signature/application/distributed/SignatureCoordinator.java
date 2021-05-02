package com.project.collaborativeauthenticationapplication.service.signature.application.distributed;

import com.project.collaborativeauthenticationapplication.service.crypto.BigNumber;
import com.project.collaborativeauthenticationapplication.service.general.ServiceHandler;
import com.project.collaborativeauthenticationapplication.service.general.Task;

public interface SignatureCoordinator extends ServiceHandler {

    void sign(Task task);

    BigNumber getHash();
    BigNumber getSignature();
    BigNumber getMessage();


    void addHash(BigNumber hash);

    void addSignaturePart(BigNumber signaturePart);


    void abort();
}

