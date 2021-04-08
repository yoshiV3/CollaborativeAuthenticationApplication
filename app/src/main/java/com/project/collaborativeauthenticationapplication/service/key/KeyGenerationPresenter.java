package com.project.collaborativeauthenticationapplication.service.key;

import com.project.collaborativeauthenticationapplication.service.Participant;

import java.util.ArrayList;
import java.util.List;

public interface KeyGenerationPresenter extends KeyPresenter {



    void onJobDone();


    void onRun();


    boolean isCurrentlyActive();

    void setMessage(String key, String message );
    String getMessage(String key);

    void SignalCoordinatorInNewState(int clientState, int oldState);

    List<Participant> getInitialOptions();



    void submitLoginDetails();
    void submitLoginDetailsUnsuccessful();
    void submitSelectedParticipants(List<Participant> participants);
    void submitThreshold(int threshold);


}
