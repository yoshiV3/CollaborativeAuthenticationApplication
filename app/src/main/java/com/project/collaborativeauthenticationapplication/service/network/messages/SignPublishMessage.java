package com.project.collaborativeauthenticationapplication.service.network.messages;

import com.project.collaborativeauthenticationapplication.service.crypto.BigNumber;
import com.project.collaborativeauthenticationapplication.service.crypto.Point;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

public class SignPublishMessage extends AbstractMessage{

    private HashMap<String, ArrayList<Point>> commitmentsE = new HashMap<>();
    private HashMap<String, ArrayList<Point>> commitmentsD = new HashMap<>();
    private BigNumber message;


    public SignPublishMessage(HashMap<String, ArrayList<Point>> commitmentsE, HashMap<String, ArrayList<Point>> commitmentsD, BigNumber message){
        this.message = message;
        this.commitmentsE.putAll(commitmentsE);
        this.commitmentsD.putAll(commitmentsD);
    }

    public Set<String> getParticipants(){
        return commitmentsD.keySet();
    }

    public HashMap<String, ArrayList<Point>> getCommitmentsE() {
        return commitmentsE;
    }

    public HashMap<String, ArrayList<Point>> getCommitmentsD() {
        return commitmentsD;
    }

    public BigNumber getMessage() {
        return message;
    }
}
