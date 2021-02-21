package com.project.collaborativeauthenticationapplication.service.key;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.project.collaborativeauthenticationapplication.CustomAuthenticationControllerActivity;
import com.project.collaborativeauthenticationapplication.R;
import com.project.collaborativeauthenticationapplication.logger.AndroidLogger;
import com.project.collaborativeauthenticationapplication.logger.Logger;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

public class DistributedKeyGenerationActivity extends CustomAuthenticationControllerActivity implements  KeyView{


    public static final  String KEY_LOGIN             = "Login";
    public static final  String KEY_APPLICATION_NAME  = "Application";
    public static final  String KEY_ERROR_MESSAGES    = "Error";

    public static String APPLICATION_NAME_FIELD_KEY = "com.project.collaborativeauthenticationapplication.APPLICATION_NAME";
    public static String LOGIN_NAME_FIELD_KEY       = "com.project.collaborativeauthenticationapplication.LOGIN_NAME";



    private static final String COMPONENT_NAME         = "Key";

    private static final String EVENT_START = "Key generation process started";



    private Logger logger = new AndroidLogger();

    private Navigator navigator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_distributed_key_generation);


        CustomKeyPresenter.newInstance(this);

        Intent intent = getIntent();

        String login       = intent.getStringExtra(LOGIN_NAME_FIELD_KEY);
        String application = intent.getStringExtra(APPLICATION_NAME_FIELD_KEY);

        KeyPresenter presenter = CustomKeyPresenter.getInstance();
        presenter.setMessage(KEY_APPLICATION_NAME, application);
        presenter.setMessage(KEY_LOGIN, login);

        logger.logEvent(COMPONENT_NAME, EVENT_START, getString(R.string.PRIORITY_HIGH), "(" + login + "," + application + ")");

        NavController controller = Navigation.findNavController(this, R.id.fragment);
        navigator = new Navigator() {
            NavController androidNavigator = controller;
            @Override
            public void navigate(int target) {
                androidNavigator.navigate(target);
            }

            @Override
            public int getLocation() {
                return controller.getCurrentDestination().getId();
            }
        };

    }


    @Override
    protected void onStart() {
        CustomKeyPresenter.getInstance().onStart();
        super.onStart();
    }

    @Override
    public void onBackPressed() {
        finish();
    }


    @Override
    protected void onPause() {
        CustomKeyPresenter.getInstance().onPause();
        super.onPause();
    }

    @Override
    protected void onStop() {
        CustomKeyPresenter.getInstance().onStop();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        CustomKeyPresenter.getInstance().onStop();
        super.onDestroy();
    }

    @Override
    public void navigate(int target) {
        navigator.navigate(target);
    }

    @Override
    public int locate() {
        return navigator.getLocation();

    }

    @Override
    public void showMetaData(String login, String application) {
        ((TextView) findViewById(R.id.textViewApplicationName)).setText(application);
        ((TextView) findViewById(R.id.textViewLogin)).setText(login);
    }


    private interface Navigator {
        void navigate(int target);
        int getLocation();
    }
}