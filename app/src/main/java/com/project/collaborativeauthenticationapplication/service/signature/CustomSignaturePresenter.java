package com.project.collaborativeauthenticationapplication.service.signature;



import com.project.collaborativeauthenticationapplication.R;
import com.project.collaborativeauthenticationapplication.data.ApplicationLoginEntity;
import com.project.collaborativeauthenticationapplication.logger.AndroidLogger;
import com.project.collaborativeauthenticationapplication.logger.Logger;
import com.project.collaborativeauthenticationapplication.service.general.CustomKeyViewManager;
import com.project.collaborativeauthenticationapplication.service.general.FeedbackRequester;
import com.project.collaborativeauthenticationapplication.service.general.Requester;
import com.project.collaborativeauthenticationapplication.service.general.Task;
import com.project.collaborativeauthenticationapplication.service.crypto.BigNumber;
import com.project.collaborativeauthenticationapplication.service.signature.application.distributed.LocalSignatureCoordinator;
import com.project.collaborativeauthenticationapplication.service.signature.application.distributed.SignatureCoordinator;
import com.project.collaborativeauthenticationapplication.service.signature.application.old.CustomSignatureCoordinator;
import com.project.collaborativeauthenticationapplication.service.signature.application.local.ThreadedVerificationClient;
import com.project.collaborativeauthenticationapplication.service.signature.application.local.VerificationClient;
import com.project.collaborativeauthenticationapplication.service.signature.user.SignatureView;

import java.util.List;

public class CustomSignaturePresenter implements SignaturePresenter{


    private static final String COMPONENT_NAME         = "signature presenter ";

    private final Logger logger                         = new AndroidLogger();

    private static SignaturePresenter instance;

    private CustomKeyViewManager persistenceManager = new CustomKeyViewManager();


    private SignatureView view;

    private SignatureCoordinator coordinator;

    private String applicationName;

    private BigNumber hash;
    private BigNumber signature;
    private BigNumber message;

    private VerificationClient verificationClient = new ThreadedVerificationClient();


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

    public synchronized void closeCoordinator(){
            if (coordinator !=null){
                synchronized (coordinator){
                    coordinator.close();
                    coordinator = null;
            }
        }
    }

    @Override
    public void onErrorSignature(String message) {
        view.showTemporally(message);
        view.navigate(R.id.action_signatureFragment_to_errorSignatureFragment);
        closeCoordinator();
    }

    @Override
    public String getApplicationName() {
        return applicationName;
    }



    @Override
    public void onFinishSignature() {
        message   = coordinator.getMessage();
        hash      = coordinator.getHash();
        signature = coordinator.getSignature();
        closeCoordinator();
        view.navigate(R.id.action_signatureFragment_to_verifySignatureFragment);
    }

    @Override
    public void onFinishVerification() {
        view.onDone();
    }

    @Override
    public void onCredentialSelectedForSignature(String applicationName){
        logger.logEvent(COMPONENT_NAME, "Received request for signature", "low");
        this.applicationName = applicationName;
        view.navigate(R.id.action_secretOverviewSignatureFragment_to_signatureFragment);
        coordinator = new LocalSignatureCoordinator(this);
        coordinator.open(view.getContext());
    }

    @Override
    public void onComputeSignature(Requester requester ) {
            logger.logEvent(COMPONENT_NAME, "Start signature", "low");
            if (coordinator == null || coordinator.getState() != CustomSignatureCoordinator.STATE_START){
                view.navigate(R.id.action_signatureFragment_to_secretOverviewSignatureFragment);
            }
            else {
                synchronized (coordinator){
                    Requester inner = new Requester() {
                        @Override
                        public void signalJobDone() {
                            view.showTemporally("Signature done");
                            requester.signalJobDone();
                        }
                    };
                    Task task = new Task(applicationName,null,  inner);
                    coordinator.sign(task);
                }
            }
    }

    @Override
    public void onStart() {
        onStartOverview();
    }

    @Override
    public void onBackPressed() {
        closeCoordinator();
    }

    @Override
    public void onPause() {
        closeCoordinator();
    }

    @Override
    public void onStop() {
        if (coordinator != null){
            coordinator.close();
            coordinator = null;
        }
    }

    @Override
    public void onPauseSignature() {
        closeCoordinator();
    }

    @Override
    public void onVerify(FeedbackRequester requester) {
        verificationClient.verify(signature, hash, message, applicationName, requester);
    }


}
