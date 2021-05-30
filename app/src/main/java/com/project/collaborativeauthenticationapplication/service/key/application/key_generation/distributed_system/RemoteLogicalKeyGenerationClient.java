package com.project.collaborativeauthenticationapplication.service.key.application.key_generation.distributed_system;

import com.project.collaborativeauthenticationapplication.alternative.network.AndroidConnection;
import com.project.collaborativeauthenticationapplication.alternative.network.Network;
import com.project.collaborativeauthenticationapplication.logger.AndroidLogger;
import com.project.collaborativeauthenticationapplication.logger.Logger;
import com.project.collaborativeauthenticationapplication.service.crypto.BigNumber;
import com.project.collaborativeauthenticationapplication.service.crypto.Point;
import com.project.collaborativeauthenticationapplication.service.general.IdentifiedParticipant;
import com.project.collaborativeauthenticationapplication.service.key.application.key_generation.KeyGenerationClient;
import com.project.collaborativeauthenticationapplication.service.key.application.key_generation.local_system.control.protocol.KeyGenerationSession;
import com.project.collaborativeauthenticationapplication.service.network.messages.AbstractMessage;
import com.project.collaborativeauthenticationapplication.service.network.messages.MessageEncoder;
import com.project.collaborativeauthenticationapplication.service.network.messages.MessageParser;
import com.project.collaborativeauthenticationapplication.service.network.messages.PartsMessage;
import com.project.collaborativeauthenticationapplication.service.network.messages.YesMessage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class RemoteLogicalKeyGenerationClient implements KeyGenerationClient {


    public static final String COMPONENT = "Remote client";
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
    private AndroidConnection connection;

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

    protected void setConnection(AndroidConnection connection) {
        this.connection = connection;
    }

    protected AndroidConnection getConnection() {
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
        Network instance = Network.getInstance();
        logger.logEvent("Remote client", "invitation", "low");
        try {
            connection = instance.getConnectionWith(participant.getAddress(), AndroidConnection.MODE_CONTROLLER, false);
            state = STATE_CONNECTED;
            encoder.clear();
            byte[] message = encoder.makeInvitationMessage(session, participant.getIdentifier());
            synchronized (sync){
                sending = true;
                logger.logEvent("Remote client", "sending invitation", "high");
                connection.writeToConnection(message);
                state = STATE_INVITED;
                sending = false;
                sync.notify();
            }
            logger.logEvent(COMPONENT, "state:", "low", String.valueOf(state));
            waitForAndHandleAnswer(coordinator);
        } catch (IOException e) {
            e.printStackTrace();
            logger.logError("Remote client", "error", "low");
            handleError(coordinator, e, "IO exception during/before invitation could be send"+ String.valueOf(getIdentifier()));
        }
    }

    protected void handleError(KeyGenerationCoordinator coordinator, IOException e, String s) {
        e.printStackTrace();
        logger.logError("Remote client", s, "normal");
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
        connection.push();
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
            logger.logError("Remote client", "error during voting after vote was cast", "normal");
        }
    }

    protected void sendPartsToRemoteClient(ArrayList<BigNumber> parts, Point publicKey, KeyGenerationCoordinator coordinator) {
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
        }  catch(InterruptedException e){
            e.printStackTrace();
            logger.logError("remote client", "interrupt", "low");
        }
    }

    @Override
    public void close(boolean success) {
        logger.logEvent(COMPONENT, "close", "high", String.valueOf(success));
        if (success){
            connection.writeToConnection(encoder.makeVoteYesMessage());
            connection.pushForFinal();
        } else {
                connection.writeToConnection(encoder.makeVoteNo());
                connection.pushForFinal();
        }
        state = STATE_CLOSED;
    }

    @Override
    public synchronized void abort() {
        logger.logEvent(COMPONENT, "abort", "low");
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
                connection.writeToConnection(abortMessage);
                connection.close();
            } catch (Exception e){
                e.printStackTrace();
            } finally {
                connection = null;
                state = STATE_CLOSED;
            }
        }

    }
}
