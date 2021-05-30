package com.project.collaborativeauthenticationapplication.service.signature.application.distributed;

import com.project.collaborativeauthenticationapplication.alternative.network.AndroidConnection;
import com.project.collaborativeauthenticationapplication.alternative.network.Network;
import com.project.collaborativeauthenticationapplication.logger.AndroidLogger;
import com.project.collaborativeauthenticationapplication.service.network.AndroidBiDirectionalCommunicationConnection;
import com.project.collaborativeauthenticationapplication.service.network.Communication;
import com.project.collaborativeauthenticationapplication.service.network.CustomCommunication;
import com.project.collaborativeauthenticationapplication.service.network.messages.AbstractMessage;
import com.project.collaborativeauthenticationapplication.service.network.messages.MessageEncoder;
import com.project.collaborativeauthenticationapplication.service.network.messages.MessageParser;
import com.project.collaborativeauthenticationapplication.service.network.messages.SignCommitmentMessage;
import com.project.collaborativeauthenticationapplication.service.network.messages.SignatureMessage;
import com.project.collaborativeauthenticationapplication.service.signature.application.SignatureClient;
import com.project.collaborativeauthenticationapplication.service.signature.application.local.RandomnessRequester;
import com.project.collaborativeauthenticationapplication.service.signature.application.local.SignatureTask;


import java.io.IOException;

public class RemoteSignatureClient implements SignatureClient {

    private MessageEncoder encoder = new MessageEncoder();
    private MessageParser  parser  = new MessageParser();


    public static final String COMPONENT = "Signature remote client";

    private AndroidLogger logger = new AndroidLogger();


    private final SignatureCoordinator coordinator;
    private final String address;

    private AndroidConnection connection;

    public RemoteSignatureClient(SignatureCoordinator coordinator, String address) throws IOException {
        this.coordinator           = coordinator;
        this.address               = address;
        Network communication = Network.getInstance();
        try{
            connection = communication.getConnectionWith(address, AndroidConnection.MODE_SLAVE_UNI, false);
        } catch (IOException e) {
            e.printStackTrace();
            coordinator.abort();
            throw e;
        }
    }


    @Override
    public String getAddress() {
        return address;
    }

    @Override
    public void sign(SignatureTask task) {
        byte[] message = encoder.makePublishMessage(task.getCommitmentsE(), task.getCommitmentsD(), task.getMessage(), Network.getInstance().getLocalAddress());
        try{
            connection.writeToConnection(message);
            connection.pushForFinal();
            byte[] response = connection.readFromConnection();
            logger.logEvent("received response from remote", "low", getAddress());
            AbstractMessage parsedResponse = parser.parse(response);
            if (parsedResponse == null){
                logger.logError(COMPONENT, "received message that cannot be parsed", "low");
            }
            if (!(parsedResponse instanceof SignatureMessage) ){
                coordinator.abort();
                logger.logError(COMPONENT, "received illegal response", "high", String.valueOf(parsedResponse.getClass()));
            } else{
                SignatureMessage signatureMessage = (SignatureMessage) parsedResponse;
                logger.logEvent(COMPONENT, "received signature part", "low", getAddress());

                coordinator.addSignaturePart(signatureMessage.getSignature());
                task.done();
            }
        } catch (IOException e) {
            e.printStackTrace();
            coordinator.abort();
        }

    }

    @Override
    public void generateRandomnessAndCalculateCommitments(RandomnessRequester requester) {
        try {
            String address = connection.getAddress();
            byte[] message = encoder.makeStartSignMessage(requester.getApplicationName(), requester.getNumberOfRequestedShares(), address);
            connection.writeToConnection(message);
            byte[] response = connection.readFromConnection();
            logger.logEvent("received response from remote", "low", getAddress());
            AbstractMessage parsedResponse = parser.parse(response);
            if (parsedResponse == null){
                logger.logError(COMPONENT, "received message that cannot be parsed", "low");
            }
            if (!(parsedResponse instanceof SignCommitmentMessage) ){
                coordinator.abort();
                logger.logError(COMPONENT, "received illegal response", "high", String.valueOf(parsedResponse.getClass()));
            } else{
                SignCommitmentMessage commitmentMessage = (SignCommitmentMessage) parsedResponse;
                logger.logEvent(COMPONENT, "received commitments", "low", getAddress());
                Network.getInstance().registerLocalAddress(commitmentMessage.getLocalAddress());
                requester.setCommitmentE(commitmentMessage.getE());
                requester.setCommitmentD(commitmentMessage.getD());
                requester.signalJobDone();
            }
        } catch (IOException e) {
            coordinator.abort();
        }
    }
}
