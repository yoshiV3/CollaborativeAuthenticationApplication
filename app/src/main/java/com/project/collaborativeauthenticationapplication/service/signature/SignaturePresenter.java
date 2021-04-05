package com.project.collaborativeauthenticationapplication.service.signature;

import com.project.collaborativeauthenticationapplication.service.Requester;
import com.project.collaborativeauthenticationapplication.service.SecretOverviewAdapterPresenter;

public interface SignaturePresenter extends SecretOverviewAdapterPresenter {

    void onErrorSignature(String message);

    String  getApplicationName();
    String  getLogin();

    void onFinishSignature();
    void onFinishVerification();

    void onCredentialSelectedForSignature(String message, String applicationName, String login);

    void onComputeSignature(Requester requester);

    void onStart();

    void onBackPressed();

    void onPause();

    void onStop();

    void onPauseSignature();

}
