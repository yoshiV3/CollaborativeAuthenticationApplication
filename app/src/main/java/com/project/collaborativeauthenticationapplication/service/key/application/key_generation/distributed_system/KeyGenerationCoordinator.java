package com.project.collaborativeauthenticationapplication.service.key.application.key_generation.distributed_system;

import com.project.collaborativeauthenticationapplication.service.Participant;
import com.project.collaborativeauthenticationapplication.service.ServiceHandler;

import java.util.List;

public interface KeyGenerationCoordinator extends ServiceHandler {



    void submitLoginDetails(String login, String application);


    List<Participant> getOptions();
    void submitSelection(List<Participant> selection);
    void  submitThreshold(int threshold);




    void run();


}
