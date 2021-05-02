package com.project.collaborativeauthenticationapplication.service.key.application.key_generation.distributed_system;

import com.project.collaborativeauthenticationapplication.logger.AndroidLogger;
import com.project.collaborativeauthenticationapplication.logger.Logger;
import com.project.collaborativeauthenticationapplication.service.crypto.BigNumber;
import com.project.collaborativeauthenticationapplication.service.crypto.Point;
import com.project.collaborativeauthenticationapplication.service.general.IdentifiedParticipant;
import com.project.collaborativeauthenticationapplication.service.key.application.key_generation.KeyGenerationClient;
import com.project.collaborativeauthenticationapplication.service.key.application.key_generation.local_system.control.protocol.KeyGenerationSession;
import com.project.collaborativeauthenticationapplication.service.network.AndroidCommunicationConnection;
import com.project.collaborativeauthenticationapplication.service.network.Communication;
import com.project.collaborativeauthenticationapplication.service.network.CustomCommunication;
import com.project.collaborativeauthenticationapplication.service.network.messages.AbstractMessage;
import com.project.collaborativeauthenticationapplication.service.network.messages.MessageEncoder;
import com.project.collaborativeauthenticationapplication.service.network.messages.MessageParser;
import com.project.collaborativeauthenticationapplication.service.network.messages.PartsMessage;
import com.project.collaborativeauthenticationapplication.service.network.messages.YesMessage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class RemoteLogicalKeyGenerationClient implements KeyGenerationClient {


    public static int STATE_INIT        = 0;
    public static int STATE_ERROR       = 1;
    public static int STATE_CLOSED      = 2;
    public static int STATE_CONNECTED   = 3;
    public static int STATE_INVITED     = 4;
    public static int STATE_REACHABLE   = 5;


    private boolean sending = false;

    private final Object sync = new Object();

    private int state = STATE_INIT;

    private final IdentifiedParticipant participant;


    private static Logger logger = new AndroidLogger();

    private MessageEncoder encoder = new MessageEncoder();

    private MessageParser parser   = new MessageParser();


    private KeyGenerationSession session;
    private AndroidCommunicationConnection connection;

    public RemoteLogicalKeyGenerationClient(IdentifiedParticipant participant, KeyGenerationSession session){

        this.participant = participant;
        this.session     = session;
    }


    protected Object getSync() {
        return sync;
    }

    protected void setSending(boolean sending) {
        this.sending = sending;
    }

    protected String getAddress(){
        return participant.getAddress();
    }

    protected void setConnection(AndroidCommunicationConnection connection) {
        this.connection = connection;
    }

    protected AndroidCommunicationConnection getConnection() {
        return connection;
    }

    protected MessageEncoder getEncoder() {
        return encoder;
    }

    protected KeyGenerationSession getSession() {
        return session;
    }

    @Override
    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    @Override
    public int getWeight() {
        return participant.getWeight();
    }

    @Override
    public int getIdentifier() {
        return participant.getIdentifier();
    }

    @Override
    public void generateParts(KeyGenerationCoordinator coordinator) {
        Communication instance = CustomCommunication.getInstance();
        logger.logEvent("Remote client", "invitation", "low");
        try {
            connection = instance.getConnectionWith(participant.getAddress());
            state = STATE_CONNECTED;
            connection.createIOtStreams();
            encoder.clear();
            byte[] message = encoder.makeInvitationMessage(session, participant.getIdentifier());
            synchronized (sync){
                sending = true;
                connection.writeToConnection(message);
                state = STATE_INVITED;
                sending = false;
                sync.notify();
            }
            waitForAndHandleAnswer(coordinator);
        } catch (IOException e) {
            handleError(instance, coordinator, e, "IO exception during/before invitation could be send");
        }
    }

    protected void handleError(Communication instance, KeyGenerationCoordinator coordinator, IOException e, String s) {
        e.printStackTrace();
        logger.logError("Remote client", s, "normal");
        instance.handleBrokenConnection(connection);
        if (state != STATE_CLOSED) {
            state = STATE_ERROR;
            connection = null;
        }
        coordinator.abort();
    }



    protected void waitForAndHandleAnswer(KeyGenerationCoordinator coordinator) throws IOException {
        state = STATE_REACHABLE;
        logger.logEvent("Remote client", "started listening", "low");
        byte[] response       = connection.readFromConnection(); // wait till receive the correct parts
        AbstractMessage resp = parser.parse(response);
        if (resp instanceof PartsMessage){
            logger.logEvent("Remote client", "received parts", "low");
            PartsMessage partsMessage = (PartsMessage) resp;
            coordinator.submitGeneratedParts(partsMessage.getParts(), partsMessage.getPublicKey());
        } else {
            coordinator.abort();
        }
    }

    @Override
    public void receiveParts(ArrayList<BigNumber> parts, Point publicKey, KeyGenerationCoordinator coordinator) {
        sendPartsToRemoteClient(parts, publicKey, coordinator);
        try {
            byte[] message = connection.readFromConnection();
            AbstractMessage result = parser.parse(message);
            if(result instanceof YesMessage){
                coordinator.persisted();
            } else {
                coordinator.rollback();
            }
        } catch (IOException e) {
            e.printStackTrace();
            Communication instance = CustomCommunication.getInstance();
            logger.logError("Remote client", "error during voting after vote was cast", "normal");
            instance.handleBrokenConnection(connection);
            e.printStackTrace();
        }
    }

    protected void sendPartsToRemoteClient(ArrayList<BigNumber> parts, Point publicKey, KeyGenerationCoordinator coordinator) {
        Communication instance = CustomCommunication.getInstance();
        logger.logEvent("Remote client", "sending parts", "low");
        try {
            synchronized (sync){
                while (sending || state < STATE_INVITED){
                    logger.logEvent("Remote client", "sending", "low", String.valueOf(sending));
                    logger.logEvent("Remote client", "state", "low", String.valueOf(state));
                    sync.wait();
                    TimeUnit.MILLISECONDS.sleep(100);
                }
                logger.logEvent("Remote client", "sending parts: locked sync", "low");
                sending = true;
                logger.logEvent("Remote client", "sending parts: go output stream", "low");
                connection.createIOtStreams();
                encoder.clear();
                byte[] message = encoder.makePartsMessage(parts, publicKey);
                logger.logEvent("Remote client", "sending parts: go output message", "low");
                connection.writeToConnection(message);
                sending = false;
            }


           // byte[] response       = connection.readFromConnection(); // wait till receive the correct parts
            //AbstractMessage  resp = parser.parse(response);
            //if (resp instanceof PartsMessage){
                //PartsMessage partsMessage = (PartsMessage) resp;
                //coordinator.submitGeneratedParts(partsMessage.getParts(), partsMessage.getPublicKey());
            //}else {
                //coordinator.abort();
            //}
        } catch (IOException e) {
            handleError(instance, coordinator, e, "IO exception before parts could be send");
        } catch(InterruptedException e){
            e.printStackTrace();
            logger.logError("remote client", "interrupt", "low");
        }
    }

    @Override
    public void close(boolean success) {
        if (success){
            try {
                connection.writeToConnection(encoder.makeVoteYesMessage());
            } catch (IOException e) {
                e.printStackTrace();
            }
            CustomCommunication.getInstance().closeConnection(connection);
        } else {
            try {
                connection.writeToConnection(encoder.makeVoteNo());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        state = STATE_CLOSED;
    }

    @Override
    public synchronized void abort() {
        if  (state >= STATE_CONNECTED){
            state = STATE_CLOSED;
            String name;
            String login;
            if (session == null){
                name = "";
                login= "";
            } else {
                name  = session.getApplicationName();
                login = session.getLogin();
            }
            byte[] abortMessage = encoder.makeAbortMessage(name, login);
            try{
                connection.createIOtStreams();
                connection.writeToConnection(abortMessage);
                connection.closeIOStreams();
            } catch (Exception e){
                e.printStackTrace();
            } finally {
                connection.closeConnection();
                connection = null;
                state = STATE_CLOSED;
            }
        }

    }
}
