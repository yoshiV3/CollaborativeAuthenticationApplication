package com.project.collaborativeauthenticationapplication.alternative.signature;

import android.bluetooth.BluetoothSocket;

import com.project.collaborativeauthenticationapplication.alternative.network.AndroidConnection;
import com.project.collaborativeauthenticationapplication.alternative.network.Network;
import com.project.collaborativeauthenticationapplication.logger.AndroidLogger;
import com.project.collaborativeauthenticationapplication.logger.Logger;
import com.project.collaborativeauthenticationapplication.service.concurrency.ThreadPoolSupplier;
import com.project.collaborativeauthenticationapplication.service.controller.CustomAuthenticationPresenter;
import com.project.collaborativeauthenticationapplication.service.general.Requester;
import com.project.collaborativeauthenticationapplication.service.general.Task;
import com.project.collaborativeauthenticationapplication.service.network.messages.AbortMessage;
import com.project.collaborativeauthenticationapplication.service.network.messages.AbstractMessage;
import com.project.collaborativeauthenticationapplication.service.network.messages.MessageParser;
import com.project.collaborativeauthenticationapplication.service.network.messages.StartSignMessage;
import com.project.collaborativeauthenticationapplication.service.signature.application.distributed.RemoteSignatureCoordinator;

import java.io.IOException;

public class SignatureStarter {

    private MessageParser parser = new MessageParser();


    private static final String COMPONENT = "Signature Starter";


    private static Logger logger = new AndroidLogger();


    AndroidConnection connection;
    public SignatureStarter(AndroidConnection connection){
        this.connection = connection;
    }

    public void start(){
        ThreadPoolSupplier.getSupplier().execute(new SignatureStartCode());
    }

    private class SignatureStartCode implements Runnable {

        @Override
        public void run() {
            try {
                final CustomAuthenticationPresenter CUSTOM_AUTHENTICATION_PRESENTER = CustomAuthenticationPresenter.getInstance();
                byte[] message = connection.readFromConnection();
                AbstractMessage parsedMessage = parser.parse(message);
                if (parsedMessage instanceof StartSignMessage){
                    CUSTOM_AUTHENTICATION_PRESENTER.onNewSignature(connection.getNickName());
                    StartSignMessage parsedStartSignMessage  = (StartSignMessage) parsedMessage;
                    Network.getInstance().registerLocalAddress(parsedStartSignMessage.getLocalAddress());
                    String name = parsedStartSignMessage.getName();
                    int number = parsedStartSignMessage.getNumber();
                    Task task = new Task(name, null, new Requester() {
                        @Override
                        public void signalJobDone() {
                            logger.logEvent(COMPONENT, "signature completed", "normal");
                        }
                    });
                    String extra = name;
                    logger.logEvent(COMPONENT, "signature started", "normal", extra);
                    RemoteSignatureCoordinator coordinator = new RemoteSignatureCoordinator(connection.getAddress());
                    coordinator.open(CUSTOM_AUTHENTICATION_PRESENTER.getServiceContext());
                    coordinator.setNumberToRequest(number);
                    coordinator.sign(task);
                } else if (parsedMessage instanceof AbortMessage) {
                    logger.logEvent(COMPONENT, "should not join in the computation", "high");
                    connection.pushForFinal();
                } else {
                    logger.logEvent(COMPONENT, "invalid request received (ignored)", "high", String.valueOf(parsedMessage.getClass()));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
