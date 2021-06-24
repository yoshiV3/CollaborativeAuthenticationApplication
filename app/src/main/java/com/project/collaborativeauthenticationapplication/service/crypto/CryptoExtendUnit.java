package com.project.collaborativeauthenticationapplication.service.crypto;

import java.util.ArrayList;

public class CryptoExtendUnit {


    static {

        System.loadLibrary("crypto-lib");
    }


    public ArrayList<BigNumber> getSlices(int[] localIdentifiers, ArrayList<BigNumber> shares, int weight, int newIdentifier, ArrayList<BigNumber> randomness) {
        ArrayList<BigNumber> result = new ArrayList<>();
        getSlicesNative(localIdentifiers, shares, weight, newIdentifier, randomness, result);
        return result;
    }


    private native void getSlicesNative(int[] localIdentifiers, ArrayList<BigNumber> shares, int weight, int newIdentifier, ArrayList<BigNumber> randomness,
                                        ArrayList<BigNumber> slices);

    public BigNumber calculateMessage(ArrayList<BigNumber> slices) {
        return calculateMessageNative(slices);
    }


    private native BigNumber calculateMessageNative(ArrayList<BigNumber> slices);
}
