package com.project.collaborativeauthenticationapplication.service.signature.application;

import com.project.collaborativeauthenticationapplication.service.Requester;
import com.project.collaborativeauthenticationapplication.service.Task;
import com.project.collaborativeauthenticationapplication.service.crypto.BigNumber;
import com.project.collaborativeauthenticationapplication.service.crypto.Point;

import java.util.ArrayList;

public class SignatureTask extends Task {


    private final  BigNumber message;
    private BigNumber hash;
    private BigNumber signature;

    ArrayList<BigNumber> e;
    ArrayList<BigNumber> d;

    ArrayList<Point> eCommitment;
    ArrayList<Point> dCommitment;

    private int numberOfRequestShares;

    public SignatureTask(String applicationName, String login, BigNumber message, ArrayList<BigNumber> e, ArrayList<BigNumber> d,
                         ArrayList<Point> eCommitment,  ArrayList<Point> dCommitment,       Requester requester) {
        super(applicationName, login, requester);
        this.message               = message;
        this.numberOfRequestShares = 0;
        this.e =e;
        this.d =d;
        this.eCommitment = eCommitment;
        this.dCommitment = dCommitment;
    }

    public SignatureTask(Task task, BigNumber message, ArrayList<BigNumber> e, ArrayList<BigNumber> d,
                         ArrayList<Point> eCommitment, ArrayList<Point> dCommitment, Requester requester){
        this(task.getApplicationName(), task.getLogin(), message, e,d, eCommitment, dCommitment, requester);
    }

    public void setNumberOfRequestShares(int numberOfRequestShares) {
        this.numberOfRequestShares = numberOfRequestShares;
    }

    public int getNumberOfRequestShares() {
        return numberOfRequestShares;
    }

    public BigNumber getMessage() {
        return message;
    }

    public void setHash(BigNumber hash) {
        this.hash = hash;
    }

    public void setSignature(BigNumber signature) {
        this.signature = signature;
    }

    public BigNumber getHash() {
        return hash;
    }

    public BigNumber getSignature() {
        return signature;
    }

    public ArrayList<BigNumber> getE() {
        return e;
    }

    public ArrayList<BigNumber> getD() {
        return d;
    }

    public ArrayList<Point> getdCommitment() {
        return dCommitment;
    }

    public ArrayList<Point> geteCommitment() {
        return eCommitment;
    }
}


