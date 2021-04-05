package com.project.collaborativeauthenticationapplication.service.key;

import com.project.collaborativeauthenticationapplication.data.ApplicationLoginEntity;
import com.project.collaborativeauthenticationapplication.logger.AndroidLogger;
import com.project.collaborativeauthenticationapplication.logger.Logger;
import com.project.collaborativeauthenticationapplication.service.CustomKeyViewManager;

import com.project.collaborativeauthenticationapplication.service.key.application.key_management.FeedbackTask;
import com.project.collaborativeauthenticationapplication.service.key.application.key_management.KeyManagementClient;
import com.project.collaborativeauthenticationapplication.service.key.application.key_management.ThreadedKeyManagementClient;

import com.project.collaborativeauthenticationapplication.service.Task;
import com.project.collaborativeauthenticationapplication.service.key.user.key_management.KeyManagementView;
import com.project.collaborativeauthenticationapplication.R;
import com.project.collaborativeauthenticationapplication.service.Requester;
import com.project.collaborativeauthenticationapplication.service.key.user.key_management.RequesterOfFeedbackTask;

import java.util.HashMap;
import java.util.List;

public class CustomKeyManagementPresenter implements KeyManagementPresenter{

    private static CustomKeyManagementPresenter instance;

    public static final String  KEY_APPLICATION_NAME = "AP";
    public static final String  KEY_LOGIN            = "L";
    public static final String  KEY_NB_OF_REM_DEV    = "RD";
    public static final String  KEY_NB_OF_REM_KEYS   = "RK";
    public static final String  KEY_NB_OF_LOC_KEYS   = "LK";

    public static final String  KEY_MESS_REC         = "MR";


    private Logger logger = new AndroidLogger();

    public static KeyManagementPresenter getInstance()
    {
        return instance;
    }


    public static void newInstance(KeyManagementView view) {

        instance = new CustomKeyManagementPresenter(view);
    }



    private KeyManagementView view;

    private CustomKeyViewManager persistenceManager = new CustomKeyViewManager();

    private KeyManagementClient client;

    private HashMap<String, String> messages = new HashMap<>();



    private CustomKeyManagementPresenter(KeyManagementView view) {
        this.view = view;
    }


    @Override
    public void onStart() {
        onStartOverview();
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
    public void onExtendSecret() {
        view.navigate(R.id.action_credentialManagementFragment_to_keyRecoveryFragment);
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
        Task task = new Task(applicationName, login,
                new Requester() {
                    @Override
                    public void signalJobDone() {
                        view.showTemporally("Delete operation complete");
                        requester.signalJobDone();
                    }
                });
        client.remove(task);
    }

    @Override
    public void onExtend(Requester requester) {
        String applicationName = messages.getOrDefault(KEY_APPLICATION_NAME, null);
        String login           = messages.getOrDefault(KEY_LOGIN, null);

        if (applicationName == null | login ==  null ){
            throw new  IllegalStateException();
        }
        RequesterOfFeedbackTask requesterInternal = new RequesterOfFeedbackTask() {
            private FeedbackTask task = null;
            @Override
            public void setTask(FeedbackTask task) {
                this.task = task;
            }

            @Override
            public FeedbackTask getTask() {
                return task;
            }

            @Override
            public void signalJobDone() {
                if (getTask().hasBeenSuccessful()){
                    view.showTemporally("Added new secret");
                    messages.put(KEY_MESS_REC, "Complete");
                }
                else {
                    view.showTemporally("Failed");
                    messages.put(KEY_MESS_REC, "Failed");
                    view.showTemporally(task.getMessage());

                }
                requester.signalJobDone();
            }
        };

        FeedbackTask task = new FeedbackTask(applicationName, login, requesterInternal);

        requesterInternal.setTask(task);

        client.extend(task);
    }

    @Override
    public void onFinishedRecovery() {
        view.navigate(R.id.action_keyRecoveryFragment_to_credentialManagementFragment);
    }

    @Override
    public void onStartOverview() {
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
}
