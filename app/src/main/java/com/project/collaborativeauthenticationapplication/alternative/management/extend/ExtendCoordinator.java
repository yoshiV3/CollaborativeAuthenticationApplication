package com.project.collaborativeauthenticationapplication.alternative.management.extend;

import android.content.Context;

import com.project.collaborativeauthenticationapplication.alternative.key.application.GuardLeaderExtend;
import com.project.collaborativeauthenticationapplication.alternative.management.PersistenceCoordinator;
import com.project.collaborativeauthenticationapplication.alternative.management.application.ThreadedPersistenceClient;
import com.project.collaborativeauthenticationapplication.alternative.network.AndroidConnection;
import com.project.collaborativeauthenticationapplication.alternative.network.Network;
import com.project.collaborativeauthenticationapplication.logger.AndroidLogger;
import com.project.collaborativeauthenticationapplication.logger.Logger;
import com.project.collaborativeauthenticationapplication.service.concurrency.ThreadPoolSupplier;
import com.project.collaborativeauthenticationapplication.service.crypto.BigNumber;
import com.project.collaborativeauthenticationapplication.service.crypto.CryptoExtendUnit;
import com.project.collaborativeauthenticationapplication.service.crypto.Point;
import com.project.collaborativeauthenticationapplication.service.network.messages.AbstractMessage;
import com.project.collaborativeauthenticationapplication.service.network.messages.ExtendCalculateMessage;
import com.project.collaborativeauthenticationapplication.service.network.messages.ExtendMessageMessage;
import com.project.collaborativeauthenticationapplication.service.network.messages.MessageEncoder;
import com.project.collaborativeauthenticationapplication.service.network.messages.MessageParser;
import com.project.collaborativeauthenticationapplication.service.network.messages.YesMessage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class ExtendCoordinator implements PersistenceCoordinator {


    private Logger logger = new AndroidLogger();

    public static final String COMPONENT = "extend coordinator EX";

    private MessageEncoder encoder = new MessageEncoder();

    private MessageParser parser   = new MessageParser();

    private final ExtendPresenter presenter;
    private ThreadedPersistenceClient persistenceClient;


    public ExtendCoordinator(ExtendPresenter presenter){
        this.presenter = presenter;
    }


    private GuardLeaderExtend guardLeaderExtend;


    private AndroidConnection connection;

    public void open(Context context){
        logger.logEvent(COMPONENT, "opening", "low");
        persistenceClient = new ThreadedPersistenceClient(this);
        persistenceClient.open(context);
        guardLeaderExtend = new GuardLeaderExtend(this);
        guardLeaderExtend.start();

    }


    private int threshold;

    public void setThreshold(int threshold) {
        this.threshold = threshold;
    }

    private String applicationName;
    public void setApplicationName(String applicationName) {
        logger.logEvent(COMPONENT, "applicationName", "low", applicationName);
        this.applicationName = applicationName;
    }

    private int newIdentifier;
    public void setNewIdentifier(int newIdentifier) {
        logger.logEvent(COMPONENT, "newIdentifier", "low", String.valueOf(newIdentifier));
        this.newIdentifier = newIdentifier;
    }


    private Point publicKey;
    public void setPublicKey(Point publicKey) {
        this.publicKey = publicKey;
    }

    HashMap<String, int[]> remotes = new HashMap<>();

    public void addRemote(String address, int[] ints) {
        logger.logEvent(COMPONENT, "remote address", "low", address);
        logger.logEvent(COMPONENT, "remote Identifier", "low", String.valueOf(ints[0]));
        remotes.put(address, ints);
    }


    private ArrayList<String> calculatingRemotes;

    public void setCalculatingRemotes(List<String> calculatingRemotes) {
        logger.logEvent(COMPONENT, "set calculating remotes", "low", String.valueOf(calculatingRemotes.size()));
        this.calculatingRemotes =  new ArrayList<>();
        this.calculatingRemotes.addAll(calculatingRemotes);
    }

    public void allSet() {
        logger.logEvent(COMPONENT, "found leader", "low", applicationName);
        presenter.foundLeader(applicationName);
    }

    public void continueWithOperation() {
        logger.logEvent(COMPONENT, "continue", "low", applicationName);
        ThreadPoolSupplier.getSupplier().execute(new Runnable() {
            @Override
            public void run() {

                Network instance = Network.getInstance();
                connection = instance.getConnectionWithInMode( main, AndroidConnection.MODE_SLAVE_MULTI);
                encoder.clear();
                byte[] mes = encoder.makeVoteYesMessage();
                logger.logEvent(COMPONENT, "write yes", "low", applicationName);
                connection.writeToConnection(mes);

                for (String remote: calculatingRemotes){
                    logger.logEvent(COMPONENT, "waiting for", "low", remote);
                    ThreadPoolSupplier.getSupplier().execute(new listenForMessageCode(remote));
                }
            }
        });
    }

    private String main;

    public void setMain(String address) {
        logger.logEvent(COMPONENT, "main ", "low", main);
        main = address;
    }


    private int submittedMessages = 0;

    ArrayList<BigNumber> messages = new ArrayList<>();

    private synchronized void submit(BigNumber message){
        logger.logEvent(COMPONENT, "submit message ", "low");
        messages.add(message);
        logger.logEvent(COMPONENT, "submit message ", "low", String.valueOf(messages.size()));
        logger.logEvent(COMPONENT, "submit message expected ", "low", String.valueOf(calculatingRemotes.size()));
        if (messages.size() == calculatingRemotes.size()){
            logger.logEvent(COMPONENT, "expected ", "low");
            CryptoExtendUnit unit = new CryptoExtendUnit();
            BigNumber share = unit.calculateMessage(messages);
            logger.logEvent(COMPONENT, "persist ", "low");
            logger.logEvent(COMPONENT, "persist: applicationName ", "low", applicationName);
            logger.logEvent(COMPONENT, "persist:  threshold ", "low", String.valueOf(threshold));
            logger.logEvent(COMPONENT, "persist: newIdentifier ", "low", String.valueOf(newIdentifier));
            persistenceClient.persist(remotes, publicKey, applicationName, threshold, newIdentifier, share);
            encoder.clear();
            logger.logEvent(COMPONENT, "yes ", "low");
            byte[] mes = encoder.makeVoteYesMessage();
            connection.writeToConnection(mes);
            connection.pushForFinal();
            try {
                logger.logEvent(COMPONENT, "final ", "low");
                YesMessage yesMessage = (YesMessage) parser.parse(connection.readFromConnection());
                persistenceClient.confirm(applicationName);
                presenter.finish();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void persisted() {

    }


    private class listenForMessageCode implements Runnable {

        private final String remote;

        public listenForMessageCode(String remote){
            if (remote.equals("here")){
                this.remote = main;
            } else {
                this.remote = remote;
            }
        }

        @Override
        public void run() {
            Network instance = Network.getInstance();
            AndroidConnection connection = instance.getConnectionWithInMode( remote, AndroidConnection.MODE_SLAVE_MULTI);
            try {
                logger.logEvent(COMPONENT, "waiting for message", "low", connection.getAddress());
                AbstractMessage parsedMessage = parser.parse(connection.readFromConnection());
                connection.pushToCoordinator();
                logger.logEvent(COMPONENT, "received message", "low", connection.getAddress());

                if (parsedMessage instanceof ExtendMessageMessage){
                    ExtendMessageMessage message = (ExtendMessageMessage) parsedMessage;
                    submit(message.getMessage());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }
}
