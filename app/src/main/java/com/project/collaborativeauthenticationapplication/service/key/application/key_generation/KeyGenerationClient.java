package com.project.collaborativeauthenticationapplication.service.key.application.key_generation;


import com.project.collaborativeauthenticationapplication.service.crypto.BigNumber;
import com.project.collaborativeauthenticationapplication.service.crypto.Point;
import com.project.collaborativeauthenticationapplication.service.key.application.key_generation.distributed_system.KeyGenerationCoordinator;

import java.util.ArrayList;

public interface KeyGenerationClient {

    int getState();

    int getWeight();
    int getIdentifier();

    void generateParts(KeyGenerationCoordinator coordinator);

    void receiveParts(ArrayList<BigNumber> parts, Point publicKey, KeyGenerationCoordinator coordinator);


    void close(boolean success);

    void abort();
}
