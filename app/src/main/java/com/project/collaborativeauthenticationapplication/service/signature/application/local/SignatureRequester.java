package com.project.collaborativeauthenticationapplication.service.signature.application.local;

import com.project.collaborativeauthenticationapplication.service.crypto.BigNumber;
import com.project.collaborativeauthenticationapplication.service.general.Requester;

public interface SignatureRequester extends Requester {

    void submitSignature(BigNumber signature);
}
