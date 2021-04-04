package com.project.collaborativeauthenticationapplication.service.key;

import com.project.collaborativeauthenticationapplication.data.ApplicationLoginEntity;
import com.project.collaborativeauthenticationapplication.logger.AndroidLogger;
import com.project.collaborativeauthenticationapplication.logger.Logger;
import com.project.collaborativeauthenticationapplication.service.key.application.CustomKeyManagementViewManager;

import com.project.collaborativeauthenticationapplication.service.key.application.ThreadedKeyManagementClient;

import com.project.collaborativeauthenticationapplication.service.key.application.key_management.Task;
import com.project.collaborativeauthenticationapplication.service.key.user.KeyManagementView;
import com.project.collaborativeauthenticationapplication.R;
import com.project.collaborativeauthenticationapplication.service.key.user.Requester;

import java.util.HashMap;
import java.util.List;

public class CustomKeyManagementPresenter implements KeyManagementPresenter{

    private static CustomKeyManagementPresenter instance;

    public static final String  KEY_APPLICATION_NAME = "AP";
    public static final String  KEY_LOGIN            = "L";
    public static final String  KEY_NB_OF_REM_DEV    = "RD";
    public static final String  KEY_NB_OF_REM_KEYS   = "RK";
    public static final String  KEY_NB_OF_LOC_KEYS   = "LK";


    private Logger logger = new AndroidLogger();

    public static KeyManagementPresenter getInstance()
    {
        return instance;
    }


    public static void newInstance(KeyManagementView view) {

        instance = new CustomKeyManagementPresenter(view);
    }



    private KeyManagementView view;

    private CustomKeyManagementViewManager persistenceManager = new CustomKeyManagementViewManager();

    private ThreadedKeyManagementClient client;

    private HashMap<String, String> messages = new HashMap<>();



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
        messages.clear();
        view.clearAdapter();
        if (client !=null){
            client.close();
            client = null;
        }
        view.onDone();
    }

    @Override
    public void onStop() {
        messages.clear();
        view.clearAdapter();
        if (client !=null){
            client.close();
            client = null;
        }
    }

    @Override
    public void onPause() {
        messages.clear();
        view.clearAdapter();
        if (client !=null){
            client.close();
            client = null;
        }
        if (view.locate() == R.id.credentialManagementFragment){
            view.navigate(R.id.action_credentialManagementFragment_to_secretOverviewFragment);
        }
    }

    @Override
    public void onBackPressed() {
        messages.clear();
        if (view.locate() == R.id.credentialManagementFragment){
            if (client !=null){
                client.close();
                client = null;
            }
        }
        view.clearAdapter();
    }

    @Override
    public void onError(String message) {
        view.showTemporally(message);
        messages.clear();
        view.navigate(R.id.action_credentialManagementFragment_to_secretOverviewFragment);
    }

    @Override
    public void onUpDate() {
        String login            = messages.getOrDefault(KEY_LOGIN, null);
        String applicationName  = messages.getOrDefault(KEY_APPLICATION_NAME, null);
        if (applicationName != null && login != null){
            messages.put(KEY_NB_OF_LOC_KEYS, String.valueOf(persistenceManager.getNumberOfLocalKeys(applicationName, login)));
            messages.put(KEY_NB_OF_REM_KEYS, String.valueOf(persistenceManager.getNumberOfRemoteKeys(applicationName, login)));
            messages.put(KEY_NB_OF_REM_DEV, String.valueOf(persistenceManager.getNumberOfRemoteParticipants(applicationName, login)));
        }
    }

    @Override
    public void openManagementSessionFor(String applicationName, String login) {
        KeyManagementPresenter presenter = this;
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                if (client != null){
                    throw new IllegalStateException();
                }
                messages.put(KEY_LOGIN, login);
                messages.put(KEY_APPLICATION_NAME, applicationName);
                onUpDate();
                client = new ThreadedKeyManagementClient(presenter);
                client.open(view.getContext());
                view.navigate(R.id.action_secretOverviewFragment_to_credentialManagementFragment);
            }
        });
        thread.start();
    }

    @Override
    public String retrieveMessage(String key) {
        return messages.getOrDefault(key, "");
    }

    @Override
    public void onRemove(Requester requester) {
        String applicationName = messages.getOrDefault(KEY_APPLICATION_NAME, null);
        String login           = messages.getOrDefault(KEY_LOGIN, null);

        if (applicationName == null | login ==  null ){
            throw new  IllegalStateException();
        }
        Task task = new Task(applicationName, login, requester);
        client.remove(task);
    }
}