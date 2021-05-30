package com.project.collaborativeauthenticationapplication.alternative.key.user;

import com.project.collaborativeauthenticationapplication.NavigationEnabledAuthenticationControllerActivity;
import com.project.collaborativeauthenticationapplication.R;
import com.project.collaborativeauthenticationapplication.logger.AndroidLogger;
import com.project.collaborativeauthenticationapplication.logger.Logger;
import com.project.collaborativeauthenticationapplication.alternative.key.CustomKeyGenerationPresenter;
import com.project.collaborativeauthenticationapplication.alternative.key.KeyGenerationPresenter;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

public class DistributedKeyGenerationActivity extends NavigationEnabledAuthenticationControllerActivity implements KeyGenerationView {


    public static final  String KEY_LOGIN             = "Login";
    public static final  String KEY_APPLICATION_NAME  = "Application";
    public static final  String KEY_ERROR_MESSAGES    = "Error";

    public static String APPLICATION_NAME_FIELD_KEY = "com.project.collaborativeauthenticationapplication.APPLICATION_NAME";
    public static String LOGIN_NAME_FIELD_KEY       = "com.project.collaborativeauthenticationapplication.LOGIN_NAME";



    private static final String COMPONENT_NAME         = "Key";

    private static final String EVENT_START = "Key generation process started";



    private Logger logger = new AndroidLogger();


    private KeyGenerationPresenter keyGenerationPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_distributed_key_generation);

        Intent intent = getIntent();

        String login       = intent.getStringExtra(LOGIN_NAME_FIELD_KEY);
        String application = intent.getStringExtra(APPLICATION_NAME_FIELD_KEY);


        CustomKeyGenerationPresenter.newInstance(this);
        keyGenerationPresenter =  CustomKeyGenerationPresenter.getInstance();


        keyGenerationPresenter.setMessage(KEY_APPLICATION_NAME, application);
        keyGenerationPresenter.setMessage(KEY_LOGIN, login);

        buildNavigator(R.id.fragment);


        logger.logEvent(COMPONENT_NAME, EVENT_START, getString(R.string.PRIORITY_HIGH), "(" + login + "," + application + ")");



    }


    @Override
    protected void onStart() {
        keyGenerationPresenter.onStart();
        super.onStart();
    }

    @Override
    public void onBackPressed() {
        keyGenerationPresenter.onBackPressed();
    }

    @Override
    protected void onPause() {

        keyGenerationPresenter.onPause();
        super.onPause();
    }

    @Override
    protected void onStop() {
        keyGenerationPresenter.onStop();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        keyGenerationPresenter.onStop();
        super.onDestroy();
    }

    @Override
    public void onDone() {
        finish();
    }

    @Override
    public void navigate(int target) {
        getNavigator().navigate(target);
    }

    @Override
    public int locate() {
        return getNavigator().getLocation();

    }

    @Override
    public void showMetaData(String login, String application) {
        ((TextView) findViewById(R.id.textViewApplicationName)).setText(application);
        ((TextView) findViewById(R.id.textViewLogin)).setText(login);
    }

    @Override
    public Context getContext() {
        return this;
    }


}