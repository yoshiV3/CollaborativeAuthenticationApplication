package com.project.collaborativeauthenticationapplication.service.crypto;

import java.util.ArrayList;

public class CryptoKeyShareGenerator {

    static {

        System.loadLibrary("crypto-lib");
    }



/*
*Parts: arrayList of (arrayLists with parts per identifier)
*
 */
    public ArrayList<BigNumber> generate(ArrayList<ArrayList<BigNumber>> parts){
        ArrayList<BigNumber> shares =  new ArrayList<>();
        if ((parts.size() == 0) || (parts.get(0).size() == 0) ){
            throw new IllegalArgumentException();
        }
        if ((parts.get(0).size() == 1) ){
            for (ArrayList<BigNumber> identifier: parts){
                shares.add(identifier.get(0));
            }
            return shares;
        }
        generateShares(shares, parts);
        return shares;
    }

    public Point generatePublicKey(ArrayList<Point> parts){
        Point result = new Point(BigNumber.getZero(), BigNumber.getZero(), true);
        generateFinalPublicKey(parts, result);
        return result;
    }


    private native void generateShares(ArrayList<BigNumber> shares, ArrayList<ArrayList<BigNumber>> keyParts);

    private native void generateFinalPublicKey(ArrayList<Point> parts, Point publicKey);

}
