package com.project.collaborativeauthenticationapplication.service.signature.application.local;

import com.project.collaborativeauthenticationapplication.service.general.Requester;
import com.project.collaborativeauthenticationapplication.service.crypto.BigNumber;
import com.project.collaborativeauthenticationapplication.service.crypto.Point;


import java.util.List;

public interface RandomnessRequester extends Requester {


    String getApplicationName();

    String getLogin();

    int getNumberOfRequestedShares();

    void setCommitmentE(List<Point> commitment);
    void setCommitmentD(List<Point> commitment);

}
