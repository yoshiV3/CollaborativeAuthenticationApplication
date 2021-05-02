package com.project.collaborativeauthenticationapplication.service.network;

import com.project.collaborativeauthenticationapplication.service.general.Requester;

public interface ConnectionRequester extends Requester {

    void isAvailable(String address);
}
