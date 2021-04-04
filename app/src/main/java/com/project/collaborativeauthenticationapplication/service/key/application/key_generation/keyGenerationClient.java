package com.project.collaborativeauthenticationapplication.service.key.application.key_generation;



import com.project.collaborativeauthenticationapplication.service.Participant;
import com.project.collaborativeauthenticationapplication.service.key.application.Client;

import java.util.List;

public interface keyGenerationClient extends Client {




    void submitLoginDetails(String login, String application);

    List<Participant> getOptions();
    void submitSelection(List<Participant> selection);
    void  submitThreshold(int threshold);


    void run();

}
