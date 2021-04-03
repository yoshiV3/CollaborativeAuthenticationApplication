package com.project.collaborativeauthenticationapplication.service.key;

import com.project.collaborativeauthenticationapplication.data.ApplicationLoginEntity;
import com.project.collaborativeauthenticationapplication.service.key.application.CustomKeyManagementPersistenceManager;
import com.project.collaborativeauthenticationapplication.service.key.user.KeyGenerationView;
import com.project.collaborativeauthenticationapplication.service.key.user.KeyManagementView;

import java.util.List;

public class CustomKeyManagementPresenter implements KeyManagementPresenter{

    private static CustomKeyManagementPresenter instance;




    public static KeyManagementPresenter getInstance()
    {
        return instance;
    }


    public static void newInstance(KeyManagementView view) {

        instance = new CustomKeyManagementPresenter(view);
    }



    private KeyManagementView view;

    private CustomKeyManagementPersistenceManager persistenceManager = new CustomKeyManagementPersistenceManager();


    private CustomKeyManagementPresenter(KeyManagementView view) {
        this.view = view;
    }


    @Override
    public void onStart() {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                view.clearAdapter();
                List<ApplicationLoginEntity> items = persistenceManager.getAllCredentials();
                view.fillAdapter(items);
            }
        });
        thread.start();
    }

    @Override
    public void close() {

    }

    @Override
    public void onStop() {
        view.clearAdapter();
    }

    @Override
    public void onPause() {
        view.clearAdapter();
    }

    @Override
    public void onBackPressed() {
        view.clearAdapter();
    }
}
