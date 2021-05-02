package com.project.collaborativeauthenticationapplication.service.signature.application.local;

import com.project.collaborativeauthenticationapplication.service.crypto.BigNumber;
import com.project.collaborativeauthenticationapplication.service.crypto.Point;
import com.project.collaborativeauthenticationapplication.service.general.Requester;
import com.project.collaborativeauthenticationapplication.service.general.Task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

public class SignatureTask extends Task {


    private  HashMap<String, ArrayList<Point>> commitmentsE;
    private  HashMap<String, ArrayList<Point>> commitmentsD;
    private BigNumber message;

    public SignatureTask(String applicationName, String login, Requester requester, HashMap<String, ArrayList<Point>> commitmentsE
            , HashMap<String, ArrayList<Point>> commitmentsD, BigNumber message) {
        super(applicationName, login, requester);
        this.commitmentsE = commitmentsE;
        this.commitmentsD = commitmentsD;
        this.message = message;
    }

    public Set<String> getParticipants(){
        return commitmentsD.keySet();
    }

    public ArrayList<Point> getCommitmentEFor(String address){
        return commitmentsE.getOrDefault(address, null);
    }

    public ArrayList<Point> getCommitmentDFor(String address){
        return commitmentsD.getOrDefault(address, null);
    }

    public BigNumber getMessage() {
        return message;
    }

    public HashMap<String, ArrayList<Point>> getCommitmentsD() {
        return commitmentsD;
    }

    public HashMap<String, ArrayList<Point>> getCommitmentsE() {
        return commitmentsE;
    }
}
