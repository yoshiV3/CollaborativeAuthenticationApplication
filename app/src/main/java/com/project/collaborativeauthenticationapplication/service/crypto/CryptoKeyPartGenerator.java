package com.project.collaborativeauthenticationapplication.service.crypto;
import java.util.ArrayList;

public class CryptoKeyPartGenerator {

    static {

        System.loadLibrary("crypto-lib");
    }

    private ArrayList<BigNumber> polynomial;

    public ArrayList<BigNumber> generate(ArrayList<BigNumber> polynomial, int minWeight, int maxWeight)
    {
        this.polynomial = polynomial;
        ArrayList<BigNumber> result =  new ArrayList<>();
        getPartsForRange(result, minWeight, maxWeight);
        return result;
    }


    public ArrayList<BigNumber> generate( int minWeight, int maxWeight){
        ArrayList<BigNumber> result =  new ArrayList<>();
        getPartsForRange(result, minWeight, maxWeight);
        return result;
    }

    public void receivePolynomial(ArrayList<BigNumber> polynomial)
    {
        this.polynomial = polynomial;
    }

    private native void getPartsForRange(ArrayList<BigNumber> result, int minWeight, int maxWeight);

}
