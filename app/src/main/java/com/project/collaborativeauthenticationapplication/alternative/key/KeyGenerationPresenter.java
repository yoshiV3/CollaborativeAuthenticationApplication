package com.project.collaborativeauthenticationapplication.alternative.key;

import com.project.collaborativeauthenticationapplication.service.general.Participant;

import java.util.List;

public interface KeyGenerationPresenter extends KeyPresenter {



    void onJobDone();


    void onRun();

    void setMode(int mode);

    void switchToModel();


    void openCoordinator();


    boolean isCurrentlyActive();

    void setMessage(String key, String message );
    String getMessage(String key);


    void signalOpened();

    void error();

    List<Participant> getInitialOptions();



    void submitLoginDetails();
    void submitLoginDetailsUnsuccessful();
    void submitSelectedParticipants(List<Participant> participants);
    void submitThreshold(int threshold);


    void successfulSubmission();

    void successfulSubmissionOfParameters();

    void foundLeader();

    void runAsRemote();

    void ok();
}
