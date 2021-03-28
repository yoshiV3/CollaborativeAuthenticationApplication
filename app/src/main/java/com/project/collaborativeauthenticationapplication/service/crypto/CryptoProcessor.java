package com.project.collaborativeauthenticationapplication.service.crypto;

import java.util.ArrayList;

public class CryptoProcessor {
    static {

        System.loadLibrary("crypto-lib");
    }


    public void generateParts(int totalWeight, ArrayList<ArrayList<BigNumber>> polynomials, ArrayList<BigNumber> parts, Point publicKeyPart){
        generateKeyParts(totalWeight, polynomials, parts, publicKeyPart);
    }


    private native void generateKeyParts(int totalWeight, ArrayList<ArrayList<BigNumber>> polynomials, ArrayList<BigNumber> parts, Point publicKeyPart);


}
