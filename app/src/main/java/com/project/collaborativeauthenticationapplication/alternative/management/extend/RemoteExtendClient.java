package com.project.collaborativeauthenticationapplication.alternative.management.extend;

import com.project.collaborativeauthenticationapplication.alternative.network.AndroidConnection;
import com.project.collaborativeauthenticationapplication.alternative.network.Network;
import com.project.collaborativeauthenticationapplication.logger.AndroidLogger;
import com.project.collaborativeauthenticationapplication.logger.Logger;
import com.project.collaborativeauthenticationapplication.service.crypto.BigNumber;
import com.project.collaborativeauthenticationapplication.service.network.messages.MessageEncoder;
import com.project.collaborativeauthenticationapplication.service.network.messages.MessageParser;
import com.project.collaborativeauthenticationapplication.service.network.messages.YesMessage;

import java.io.IOException;
import java.util.List;

public class RemoteExtendClient {


    private Logger logger = new AndroidLogger();

    public static final String COMPONENT = "remote extend client EX";


    private final String remote;

    private AndroidConnection connection;

    private final MessageEncoder encoder = new MessageEncoder();

    private final MessageParser parser   = new MessageParser();

    private final LocalExtendingCoordinator localExtendingCoordinator;

    public RemoteExtendClient(String remote, LocalExtendingCoordinator localExtendingCoordinator){
        this.remote = remote;
        this.localExtendingCoordinator = localExtendingCoordinator;
    }

    public void go(List<String> remotes, int newIdentifier, String applicationName, List<String> chosen) {
        logger.logEvent(COMPONENT, "go ", "low", applicationName);
        logger.logEvent(COMPONENT, "go ", "low", String.valueOf(newIdentifier));
        logger.logEvent(COMPONENT, "go: chosen ", "low", String.valueOf(chosen.size()));
        logger.logEvent(COMPONENT, "go: remotes ", "low", String.valueOf(remotes.size()));
        encoder.clear();
        encoder.beginExtendCode(localExtendingCoordinator.getPublicKey(), localExtendingCoordinator.getThreshold(), newIdentifier, applicationName, remotes.size()+1);
        for (String remote : remotes){
            boolean cal = chosen.contains(remote);
            logger.logEvent(COMPONENT, "add: remotes ", "low", remote);
            logger.logEvent(COMPONENT, "add: remotes ", "low", String.valueOf(cal));
            encoder.addRemote(remote, localExtendingCoordinator.getIdentifiersFor(remote), cal);
        }
        encoder.addRemote("here", localExtendingCoordinator.getLocalIdentifiers(), true);

        byte[] mes = encoder.build();

        Network instance = Network.getInstance();

        try {
            logger.logEvent(COMPONENT, "write", "low");
            connection = instance.getConnectionWith(remote, AndroidConnection.MODE_CONTROLLER, false);
            connection.writeToConnection(mes);
            YesMessage message = (YesMessage) parser.parse(connection.readFromConnection());
            logger.logEvent(COMPONENT, "yes", "low", remote);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void send(BigNumber message, int weight) {
        logger.logEvent(COMPONENT, "send", "low", remote);
        connection.writeToConnection(encoder.makeExtendMessageMessage(message, weight ));
        connection.push();
        try {
            YesMessage yesMessage = (YesMessage) parser.parse(connection.readFromConnection());
            logger.logEvent(COMPONENT, "yes", "low", remote);
            localExtendingCoordinator.persisted();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void ok() {
        logger.logEvent(COMPONENT, "ok", "low", remote);
        connection.writeToConnection(encoder.makeVoteYesMessage());
        connection.pushForFinal();
    }
}
