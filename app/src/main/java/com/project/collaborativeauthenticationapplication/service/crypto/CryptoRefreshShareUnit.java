package com.project.collaborativeauthenticationapplication.service.crypto;

import java.util.ArrayList;
import java.util.List;

public class CryptoRefreshShareUnit {

    public ArrayList<BigNumber> createShares(ArrayList<BigNumber> poly, int[] identifiers){
        ArrayList<BigNumber> shares = new ArrayList<>();
        createSharesNative(poly, identifiers, shares);
        return shares;
    }

    private native void createSharesNative(ArrayList<BigNumber> poly, int[] identifiers, ArrayList<BigNumber> shares);

    public ArrayList<BigNumber> calculateShares(ArrayList<ArrayList<BigNumber>> list) {
        ArrayList<BigNumber> shares = new ArrayList<>();
        calculateSharesNative(list, shares);
        return shares;
    }

    private native void calculateSharesNative(ArrayList<ArrayList<BigNumber>> list, ArrayList<BigNumber> shares);
}
