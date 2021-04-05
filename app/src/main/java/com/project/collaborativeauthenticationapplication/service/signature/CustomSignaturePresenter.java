package com.project.collaborativeauthenticationapplication.service.signature;



import com.project.collaborativeauthenticationapplication.R;
import com.project.collaborativeauthenticationapplication.data.ApplicationLoginEntity;
import com.project.collaborativeauthenticationapplication.logger.AndroidLogger;
import com.project.collaborativeauthenticationapplication.logger.Logger;
import com.project.collaborativeauthenticationapplication.service.CustomKeyViewManager;
import com.project.collaborativeauthenticationapplication.service.Requester;
import com.project.collaborativeauthenticationapplication.service.signature.application.CustomSignatureClient;
import com.project.collaborativeauthenticationapplication.service.signature.application.SignatureClient;
import com.project.collaborativeauthenticationapplication.service.signature.application.SignatureTask;
import com.project.collaborativeauthenticationapplication.service.signature.application.ThreadedSignatureClient;
import com.project.collaborativeauthenticationapplication.service.signature.user.SignatureView;

import java.util.List;

public class CustomSignaturePresenter implements SignaturePresenter{


    private static final String COMPONENT_NAME         = "signature presenter ";

    private final Logger logger                         = new AndroidLogger();

    private static SignaturePresenter instance;

    private CustomKeyViewManager persistenceManager = new CustomKeyViewManager();


    private SignatureView view;

    private SignatureClient client;

    private String applicationName;
    private String login;
    private String message;


    public static void newInstance(SignatureView view) {

        instance = new CustomSignaturePresenter(view);
    }


    public static SignaturePresenter getInstance()
    {
        return instance;
    }

    protected CustomSignaturePresenter(SignatureView view){
        this.view = view;
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

    public synchronized void closeClient(){
            if (client !=null){
                synchronized (client){
                    client.close();
                    client = null;
            }
        }
    }

    @Override
    public void onErrorSignature(String message) {
        view.showTemporally(message);
        view.navigate(R.id.action_signatureFragment_to_errorSignatureFragment);
        closeClient();
    }

    @Override
    public String getApplicationName() {
        return applicationName;
    }

    @Override
    public String getLogin() {
        return login;
    }

    @Override
    public void onFinishSignature() {
        closeClient();
        view.navigate(R.id.action_signatureFragment_to_verifySignatureFragment);
    }

    @Override
    public void onFinishVerification() {
        view.onDone();
    }

    @Override
    public void onCredentialSelectedForSignature(String message, String applicationName, String login){
        logger.logEvent(COMPONENT_NAME, "Received request for signature", "low");
        this.message         = message;
        this.applicationName = applicationName;
        this.login           = login;
        view.navigate(R.id.action_secretOverviewSignatureFragment_to_signatureFragment);
        client = new ThreadedSignatureClient(this);
        client.open(view.getContext());
    }

    @Override
    public void onComputeSignature(Requester requester ) {
            logger.logEvent(COMPONENT_NAME, "Start signature", "low");
            if (client == null || client.getState() != CustomSignatureClient.STATE_START){
                view.navigate(R.id.action_signatureFragment_to_secretOverviewSignatureFragment);
            }
            else {
                synchronized (client){
                    Requester inner = new Requester() {
                        @Override
                        public void signalJobDone() {
                            view.showTemporally("Signature done");
                            requester.signalJobDone();
                        }
                    };
                    SignatureTask task = new SignatureTask(message, applicationName, login, inner);
                    client.sign(task);
                }
            }
    }

    @Override
    public void onStart() {
        onStartOverview();
    }

    @Override
    public void onBackPressed() {
        closeClient();
    }

    @Override
    public void onPause() {
        closeClient();
    }

    @Override
    public void onStop() {
        if (client != null){
            client.close();
            client = null;
        }
    }

    @Override
    public void onPauseSignature() {
        closeClient();
    }


}
