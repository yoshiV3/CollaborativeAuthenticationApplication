package com.project.collaborativeauthenticationapplication.service.key.application;

import com.project.collaborativeauthenticationapplication.service.crypto.BigNumber;
import com.project.collaborativeauthenticationapplication.service.crypto.Point;


public interface KeyPartDistributionSession extends KeyGenerationSession {


    BigNumber getKeyPartsFor(int identifier);

    Point getPublicKey();
}