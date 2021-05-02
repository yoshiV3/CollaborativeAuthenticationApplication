package com.project.collaborativeauthenticationapplication.service.network.messages;

import com.project.collaborativeauthenticationapplication.service.crypto.BigNumber;

public class SignatureMessage extends AbstractMessage{

    private BigNumber signature;

    public SignatureMessage(BigNumber signature){

        this.signature = signature;
    }

    public BigNumber getSignature() {
        return signature;
    }
}
