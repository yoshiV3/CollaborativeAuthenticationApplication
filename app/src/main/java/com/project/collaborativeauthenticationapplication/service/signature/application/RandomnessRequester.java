package com.project.collaborativeauthenticationapplication.service.signature.application;

import com.project.collaborativeauthenticationapplication.service.Requester;
import com.project.collaborativeauthenticationapplication.service.crypto.BigNumber;
import com.project.collaborativeauthenticationapplication.service.crypto.Point;


import java.util.List;

public interface RandomnessRequester extends Requester {

    int getNumberOfRequestedShares();

    void setE(List<BigNumber> e);

    void setD(List<BigNumber> d);

    void setCommitmentE(List<Point> commitment);
    void setCommitmentD(List<Point> commitment);

}
