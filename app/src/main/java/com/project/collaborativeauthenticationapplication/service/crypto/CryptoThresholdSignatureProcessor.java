package com.project.collaborativeauthenticationapplication.service.crypto;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CryptoThresholdSignatureProcessor {
    static {

        System.loadLibrary("crypto-lib");
    }


    private ArrayList<BigNumber> shares;

    private ArrayList<Point> E;
    private ArrayList<Point> D;

    private ArrayList<Point> commitmentE;
    private ArrayList<Point> commitmentD;

    private int localWeight = 0;
    private int totalWeight = 0;

    private ArrayList<BigNumber> eList;
    private ArrayList<BigNumber> dList;

    private ArrayList<BigNumber> signatureShare;

    private ArrayList<BigNumber> geteList() {
        return eList;
    }

    private ArrayList<BigNumber> getdList() {
        return dList;
    }

    public int getLocalWeight(){
        return localWeight;
    }

    public int getTotalWeight(){
        return totalWeight;
    }


    private ArrayList<Point> getCommitmentE() {
        return commitmentE;
    }

    private ArrayList<Point> getCommitmentD() {
        return commitmentD;
    }

    private ArrayList<Point> getE() {
        return E;
    }

    private ArrayList<Point> getD() {
        return D;
    }

    public void receiveRandomness(ArrayList<BigNumber> e, ArrayList<BigNumber> d){
        if (e == null || d == null){
            throw new IllegalStateException("the randomness was not properly set");
        }
        if(e.size() == 0 || d.size() == 0 || e.size() != d.size()){
            throw new IllegalStateException("Randomness was not correctly  initialized");
        }
        this.eList = new ArrayList<>();
        this.eList.addAll(e);

        this.dList = new ArrayList<>();
        this.dList.addAll(d);
    }


    public void receiveShares(List<BigNumber> shares){
        this.shares = new ArrayList<>();
        this.shares.addAll(shares);
    }

    private ArrayList<BigNumber> getShares() {
        return shares;
    }

    public List<Point> publishCommitmentsE(){
        return Collections.unmodifiableList(commitmentE);
    }
    public List<Point> publishCommitmentsD(){
        return Collections.unmodifiableList(commitmentD);
    }

    public void receiveAllRandomness(List<Point> E, List<Point> D){
        if (E == null || D == null){
            throw new IllegalStateException("the randomness was not properly set");
        }
        if(E.size() < getLocalWeight() || D.size() < getLocalWeight() || E.size() != D.size()){
            throw new IllegalStateException("Randomness was not correctly  initialized");
        }
        this.E = new ArrayList<>();
        this.E.addAll(E);

        this.D = new ArrayList<>();
        this.D.addAll(D);
    }

    public void calculateCommitmentsToRandomness(){
        // set localWeight to the size of the commitments
        if (eList == null || dList == null){
            throw new IllegalStateException("the randomness was not properly set");
        }
        if(eList.size() == 0 || dList.size() == 0 || eList.size() != dList.size()){
            throw new IllegalStateException("Randomness was not correctly  initialized");
        }
        localWeight = eList.size();
        commitmentE = new ArrayList<>();
        commitmentD = new ArrayList<>();
        calculateCommitmentsToRandomnessNative();
    }

    public List<BigNumber> publishSignatureShare(){
        return Collections.unmodifiableList(signatureShare);
    }

    private ArrayList<BigNumber> getSignatureShare() {
        return signatureShare;
    }

    public void produceSignatureShare(BigNumber message, int[] identifiers){
        if (message == null){
            throw new IllegalArgumentException("cannot calculate a signature on an null message");
        }
        if (eList == null || dList == null){
            throw new IllegalStateException("the randomness was not properly set");
        }
        if(eList.size() != getLocalWeight() || dList.size() != getLocalWeight()){
            throw new IllegalStateException("Randomness was not correctly  initialized");
        }
        if (commitmentE == null || commitmentD == null){
            throw new IllegalStateException("the randomness was not properly set");
        }
        if (E == null || D == null){
            throw new IllegalStateException("the randomness was not properly set");
        }
        if(E.size() < getLocalWeight() || D.size() < getLocalWeight()){
            throw new IllegalStateException("Randomness was not correctly  initialized");
        }
        if(shares.size() != getLocalWeight()){
            throw new IllegalStateException("Shares were not correctly  initialized");
        }
        totalWeight = E.size();
        signatureShare = new ArrayList<>();
        calculateSignatureShare(message, identifiers);
    }


    private native void calculateCommitmentsToRandomnessNative();

    private native void calculateSignatureShare(BigNumber message, int[] identifiers);






}
