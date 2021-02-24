package com.project.collaborativeauthenticationapplication.service.crypto;

import java.util.ArrayList;

public class CryptoKeyPartGenerator {

    static {

        System.loadLibrary("crypto-lib");
    }

    private ArrayList<BigNumber> polynomial = new ArrayList<>();

    public CryptoKeyPartGenerator(ArrayList<BigNumber> polynomial)
    {
        this.polynomial.addAll(polynomial);
    }

    public ArrayList<BigNumber> generate(int minWeight, int maxWeight)
    {
        ArrayList<BigNumber> result =  new ArrayList<>();
        getPartsForRange(result, minWeight, maxWeight);
        return result;
    }

    private native void getPartsForRange(ArrayList<BigNumber> result, int minWeight, int maxWeight);

}
