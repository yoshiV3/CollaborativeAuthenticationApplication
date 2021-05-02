package com.project.collaborativeauthenticationapplication.service.key;

import com.project.collaborativeauthenticationapplication.service.general.SecretOverviewAdapterPresenter;
import com.project.collaborativeauthenticationapplication.service.general.Requester;

public interface KeyManagementPresenter extends KeyPresenter, SecretOverviewAdapterPresenter {

    void onError(String message);

    void onUpDate();

    void onExtendSecret();

    void openManagementSessionFor(String applicationName, String login);

    String retrieveMessage(String key);

    void onRemove(Requester requester);
    void onExtend(Requester requester);

    void onFinishedRecovery();
}
