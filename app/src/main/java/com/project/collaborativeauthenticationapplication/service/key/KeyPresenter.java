package com.project.collaborativeauthenticationapplication.service.key;

import com.project.collaborativeauthenticationapplication.service.Participant;

import java.util.ArrayList;
import java.util.List;

public interface KeyPresenter {


    void onStart();
    void close();
    void onStop();
    void onPause();
    void onBackPressed();


    void onRun();


    boolean isCurrentlyActive();

    void setMessage(String key, String message );
    String getMessage(String key);

    void SignalClientInNewState(int clientState, int oldState);

    List<Participant> getInitialOptions();



    void submitLoginDetails();
    void submitLoginDetailsUnsuccessful();
    void submitSelectedParticipants(List<Participant> participants);
    void submitThreshold(int threshold);


}
