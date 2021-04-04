package com.project.collaborativeauthenticationapplication.service.crypto;


import java.util.List;

public class CryptoPartKeyRecovery {

    static {

        System.loadLibrary("crypto-lib");
    }

    public  BigNumber createLocalSecretSharePartsFromSharesForTarget(List<BigNumber> shares, int[] identifiers, int identifierTarget){
        return createLocalSecretSharePartsFromSharesForTargetNative(shares, identifiers, identifierTarget);
    }

    private native BigNumber createLocalSecretSharePartsFromSharesForTargetNative(List<BigNumber> shares, int[] identifiers, int identifierTarget);


}
