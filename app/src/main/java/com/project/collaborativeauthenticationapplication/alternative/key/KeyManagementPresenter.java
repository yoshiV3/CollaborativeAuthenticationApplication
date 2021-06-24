package com.project.collaborativeauthenticationapplication.alternative.key;

import android.content.Context;

import com.project.collaborativeauthenticationapplication.alternative.management.DataFiller;
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

    void onRefreshSecret();

    void makeLeader();

    void makeFollower();

    void selectedDevice(String remove);

    void getDeviceList(DataFiller dataFiller);

    void openCoordinator(Context context);

    String getDevice();



    void startRefresh();

    String getApplicationName();

    void isRunnable();

    void onFinished();

    void endRefresh();

    void startExtend();


}
