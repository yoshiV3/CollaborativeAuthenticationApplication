package com.project.collaborativeauthenticationapplication.service.key;

import com.project.collaborativeauthenticationapplication.service.key.user.Requester;

public interface KeyManagementPresenter extends KeyPresenter{

    void onError(String message);

    void onUpDate();

    void openManagementSessionFor(String applicationName, String login);

    String retrieveMessage(String key);

    void onRemove(Requester requester);
}
