package com.project.collaborativeauthenticationapplication.service.key.application.key_generation.distributed_system;

import com.project.collaborativeauthenticationapplication.service.crypto.BigNumber;
import com.project.collaborativeauthenticationapplication.service.crypto.Point;
import com.project.collaborativeauthenticationapplication.service.general.Participant;
import com.project.collaborativeauthenticationapplication.service.general.ServiceHandler;

import java.util.ArrayList;
import java.util.List;

public interface KeyGenerationCoordinator extends ServiceHandler {


    void abort();

    void submitGeneratedParts(ArrayList<BigNumber> parts, Point publicKey);

    void submitLoginDetails(String application);

    void submitSelection(List<Participant> selection);

    void  submitThreshold(int threshold);

    List<Participant> getOptions();

    void run();

    void persisted();


    void submitShares(ArrayList<BigNumber> shares, Point publicKey);


    void distributeParts(ArrayList<BigNumber> parts, Point publicKey);

    void done();

    void rollback();
}
