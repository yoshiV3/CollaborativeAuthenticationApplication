package com.project.collaborativeauthenticationapplication.service.signature.application;

import com.project.collaborativeauthenticationapplication.service.Requester;

public interface DatabaseInformationRequester  extends Requester {

    void setThreshold(int threshold);
    void setNumberOfRemoteKeysForRemoteParticipant(String address, int remoteKeys);
    void setNumberOfLocalKeys(int localKeys);


}
