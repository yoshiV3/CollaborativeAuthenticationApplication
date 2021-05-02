package com.project.collaborativeauthenticationapplication.service.signature.application.distributed;

import android.content.Context;
import android.text.format.Time;


import com.project.collaborativeauthenticationapplication.logger.AndroidLogger;
import com.project.collaborativeauthenticationapplication.service.controller.CustomServiceMonitor;
import com.project.collaborativeauthenticationapplication.service.crypto.BigNumber;
import com.project.collaborativeauthenticationapplication.service.crypto.Point;
import com.project.collaborativeauthenticationapplication.service.general.FeedbackRequester;
import com.project.collaborativeauthenticationapplication.service.general.IllegalNumberOfTokensException;
import com.project.collaborativeauthenticationapplication.service.general.Requester;
import com.project.collaborativeauthenticationapplication.service.general.ServiceStateException;
import com.project.collaborativeauthenticationapplication.service.general.Task;
import com.project.collaborativeauthenticationapplication.service.network.AndroidCommunicationConnection;
import com.project.collaborativeauthenticationapplication.service.network.CustomCommunication;
import com.project.collaborativeauthenticationapplication.service.network.messages.AbstractMessage;
import com.project.collaborativeauthenticationapplication.service.network.messages.MessageEncoder;
import com.project.collaborativeauthenticationapplication.service.network.messages.MessageParser;
import com.project.collaborativeauthenticationapplication.service.network.messages.SignPublishMessage;
import com.project.collaborativeauthenticationapplication.service.network.messages.SignatureMessage;
import com.project.collaborativeauthenticationapplication.service.signature.application.SignatureClient;
import com.project.collaborativeauthenticationapplication.service.signature.application.local.InformationSignatureClient;
import com.project.collaborativeauthenticationapplication.service.signature.application.local.LocalInformationSignatureClient;
import com.project.collaborativeauthenticationapplication.service.signature.application.local.SignatureTask;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;


public class RemoteSignatureCoordinator extends AbstractSignatureCoordinator {

    public static final String COMPONENT = "Remote Coordinator";
    private int numberToRequest;


    private  String addressLeader;

    private AndroidLogger logger = new AndroidLogger();

    private final MessageEncoder encoder = new MessageEncoder();
    private final MessageParser  parser  = new MessageParser();

    public RemoteSignatureCoordinator(String addressLeader) {
        this.addressLeader = addressLeader;
    }

    @Override
    public void sign(Task task) {

        setOriginalTask(task);
        logger.logEvent(COMPONENT, "start signature", "normal");

        getClient().checkIfEnoughLocalShares(numberToRequest, task.getApplicationName(), task.getLogin(), new FeedbackRequester() {
            boolean result;
            @Override
            public void setResult(boolean result) {
                this.result = result;
            }

            @Override
            public void signalJobDone() {
                if (result) {
                    logger.logEvent(COMPONENT, "enough local shares", "low");
                    requestSharesFromClient(getClient());
                } else {
                    abort();
                    task.done();
                    logger.logError(COMPONENT, "not enough local shares", "high");
                }

            }
        });
    }

    @Override
    public void addSignaturePart(BigNumber signaturePart) {
        logger.logEvent(COMPONENT, " sending signature part", "low");
        byte[] mes = encoder.makeSignatureMessage(signaturePart);
        try {
            AndroidCommunicationConnection connection = CustomCommunication.getInstance().getConnectionWith(addressLeader);
            connection.createIOtStreams();
            connection.writeToConnection(mes);
            String extra = String.valueOf(mes[0]);
            logger.logEvent(COMPONENT, " done signature ", "low", extra);


        } catch (IOException ioException) {
            ioException.printStackTrace();
            abort();
            getOriginalTask().done();
        }
    }


    public void setNumberToRequest(int numberToRequest) {
        this.numberToRequest = numberToRequest;
    }

    @Override
    public void abort() {
        if (getState() != STATE_ERROR){
            CustomCommunication.getInstance().closeAllConnections();
            setState(STATE_ERROR);
        }
    }

    @Override
    public void open(Context context) {
        if (getState() != STATE_INIT)
        {
            throw  new IllegalStateException();
        }
        if(getClient() != null && getClient().getState() != STATE_INIT){
            throw new IllegalStateException();
        } else if (getClient() == null){
            InformationSignatureClient infoClient = new LocalInformationSignatureClient(this);
            setClient(infoClient);
        }
        getClient().open(context);
        if (CustomServiceMonitor.getInstance().isServiceEnabled()) {
            try {
                getToken();
                setState(STATE_START);
            } catch (IllegalNumberOfTokensException | ServiceStateException e) {
                setState(STATE_ERROR);
                abort();
            }
        }
        else
        {
            abort();
        }
    }

    @Override
    protected int getNumberOfRequestedSharesForClient(SignatureClient client) {
        return numberToRequest;
    }

    @Override
    protected void handleCommitmentE(List<Point> commitment, String address) {
        encoder.clear();
        encoder.makePartOneCommitmentResponse(commitment, addressLeader);
        logger.logEvent(COMPONENT, " e commitments received", "low");
    }

    @Override
    protected void handleCommitmentD(List<Point> commitment, String address) {
        encoder.makePartTwoCommitmentResponse(commitment);
        logger.logEvent(COMPONENT, " d commitments received", "low");
    }

    @Override
    protected void randomnessGenerationDone() {
        logger.logEvent(COMPONENT, " sending commitments", "low");
        byte[] mes = encoder.build();
        try {
            AndroidCommunicationConnection connection = CustomCommunication.getInstance().getConnectionWith(addressLeader);
            connection.createIOtStreams();
            connection.writeToConnection(mes);
            String extra = String.valueOf(mes[0]);
            logger.logEvent(COMPONENT, " done sending commitments", "low", extra);
            listenForResponse(connection);
        } catch (IOException ioException) {
            ioException.printStackTrace();
            abort();
            getOriginalTask().done();
        }
    }

    private void listenForResponse(AndroidCommunicationConnection connection) {
        logger.logEvent(COMPONENT, "started listening for a response", "normal");
        try {
            byte[] response = connection.readFromConnection();
            logger.logEvent(COMPONENT, "received a response", "normal");
            AbstractMessage parsedResponse = parser.parse(response);
            if (parsedResponse == null){
                logger.logError(COMPONENT, "received message that cannot be parsed", "low");
            }
            if (!(parsedResponse instanceof SignPublishMessage) ){
                logger.logError(COMPONENT, "received illegal response", "high", String.valueOf(parsedResponse.getClass()));
            } else{
                SignPublishMessage signPublishMessage = (SignPublishMessage) parsedResponse;
                logger.logEvent(COMPONENT, "received all commitments", "low");
                Task originalTask = getOriginalTask();
                Requester requester = new Requester() {
                    @Override
                    public void signalJobDone() {
                        try {
                            TimeUnit.MILLISECONDS.sleep(10);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        CustomCommunication.getInstance().closeAllConnections();

                        addressLeader = null;

                        setState(STATE_INIT);

                        logger.logEvent(COMPONENT, "releasing token", "low");
                        releaseToken();

                        getOriginalTask().done();
                    }
                };
                SignatureTask task = new SignatureTask(originalTask.getApplicationName(), originalTask.getLogin(), requester,
                        signPublishMessage.getCommitmentsE(), signPublishMessage.getCommitmentsD(), signPublishMessage.getMessage());
                getClient().sign(task);
            }
        } catch (IOException ioException) {
            ioException.printStackTrace();
            abort();
            getOriginalTask().done();
        }
    }
}

