package com.project.collaborativeauthenticationapplication.service.crypto;

import java.util.ArrayList;
import java.util.List;

public class CryptoVerificationProcessor {

    public boolean verify(List<BigNumber> signature, BigNumber message, Point publicKey){

        ArrayList<BigNumber> signatureARR = new ArrayList<>(signature);
        return verifyNative(signatureARR, message, publicKey);
    }

    private native boolean verifyNative(ArrayList<BigNumber> signature, BigNumber message, Point public_key);
}
