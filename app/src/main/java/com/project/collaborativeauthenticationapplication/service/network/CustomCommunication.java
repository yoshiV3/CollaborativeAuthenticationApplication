package com.project.collaborativeauthenticationapplication.service.network;

import android.bluetooth.BluetoothSocket;

import com.project.collaborativeauthenticationapplication.logger.AndroidLogger;
import com.project.collaborativeauthenticationapplication.logger.Logger;
import com.project.collaborativeauthenticationapplication.service.concurrency.ThreadPoolSupplier;
import com.project.collaborativeauthenticationapplication.service.controller.CustomAuthenticationPresenter;
import com.project.collaborativeauthenticationapplication.service.general.CustomParticipant;
import com.project.collaborativeauthenticationapplication.service.general.FeedbackRequester;
import com.project.collaborativeauthenticationapplication.service.general.IdentifiedParticipant;
import com.project.collaborativeauthenticationapplication.service.general.Participant;
import com.project.collaborativeauthenticationapplication.service.general.Requester;
import com.project.collaborativeauthenticationapplication.service.general.Task;
import com.project.collaborativeauthenticationapplication.service.key.application.key_generation.KeyGenerationClient;
import com.project.collaborativeauthenticationapplication.service.key.application.key_generation.ThreadedKeyGenerationClient;
import com.project.collaborativeauthenticationapplication.service.key.application.key_generation.distributed_system.CustomIdentifiedParticipant;
import com.project.collaborativeauthenticationapplication.service.key.application.key_generation.distributed_system.RemoteKeyGenerationCoordinator;
import com.project.collaborativeauthenticationapplication.service.key.application.key_generation.distributed_system.RemoteLogicalKeyGenerationClientForRemoteCoordinator;
import com.project.collaborativeauthenticationapplication.service.network.messages.AbstractMessage;
import com.project.collaborativeauthenticationapplication.service.network.messages.ConnectMessage;
import com.project.collaborativeauthenticationapplication.service.network.messages.InvitationMessage;
import com.project.collaborativeauthenticationapplication.service.network.messages.MessageEncoder;
import com.project.collaborativeauthenticationapplication.service.network.messages.MessageParser;
import com.project.collaborativeauthenticationapplication.service.network.messages.StartSignMessage;
import com.project.collaborativeauthenticationapplication.service.signature.application.distributed.RemoteSignatureCoordinator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;


public class CustomCommunication implements Communication{



    private static Communication instance = new CustomCommunication();
    private String localAddress;


    public static Communication getInstance()
    {
        return instance;
    }


    private static final String COMPONENT = "Custom Communication";


    private static Logger logger = new AndroidLogger();


    private MessageEncoder encoder = new MessageEncoder();

    private BluetoothMonitor bluetoothMonitor = new AndroidBluetoothMonitor();

    private AndroidCommunicationServer server;

    private boolean shouldStop = false;

    private MessageParser parser = new MessageParser();

    HashMap<String, AndroidBiDirectionalCommunicationConnection> cachedConnections = new HashMap<>();

    private CustomCommunication(){}


    @Override
    public ArrayList<Participant> getReachableParticipants() {
        ArrayList<Device> devices           = bluetoothMonitor.getPairedDevices();
        ArrayList<Participant> participants = new ArrayList<>();
        for (Device device: devices)
        {
            participants.add(CustomParticipant.fromDevice(device));
        }
        participants.add(new CustomParticipant("this", "here", true));
        return participants;
    }

    @Override
    public String getLocalAddress() {
        logger.logEvent(COMPONENT, "local address found ", "low", localAddress);
        return localAddress;
    }


    @Override
    public void  openServiceServer(FeedbackRequester controller){
        server     = new AndroidCommunicationServer();
        shouldStop = false;
        synchronized (server){
            if (server.getState() == AndroidCommunicationServer.STATE_IDLE){
                server.openServer(controller);
            }
        }
    }

    @Override
    public void handleIncomingRequests() {
        logger.logEvent(COMPONENT, "request ot handle incoming request is received", "low");
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    while (!shouldStop){
                        BluetoothSocket socket = server.listenForIncomingRequests();
                        handleIncomingConnection(socket); // functionality not yet done so n
                    }
                } catch (IOException e) {
                    e.printStackTrace(); //most likely cause: either the socket was closed or bluetooth was disabled
                } catch (Exception e){
                    e.printStackTrace();
                }
            }
        });
        thread.start();  //threaded listener
    }

    private void handleIncomingConnection(BluetoothSocket connection){
        AndroidBiDirectionalCommunicationConnection communicationConnection;
        String address = connection.getRemoteDevice().getAddress();
        synchronized (cachedConnections){
            communicationConnection = new AndroidBiDirectionalCommunicationConnection(connection);
            cachedConnections.put(address, communicationConnection);
        }
        logger.logEvent(COMPONENT, "new request is being processed", "normal");
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {

                      logger.logEvent(COMPONENT, "start IO streams", "low");
                      communicationConnection.createIOtStreams();
                      final CustomAuthenticationPresenter CUSTOM_AUTHENTICATION_PRESENTER = CustomAuthenticationPresenter.getInstance();
                      logger.logEvent(COMPONENT, "new request is being processed: reading", "normal");
                      byte[] message =  communicationConnection.readFromConnection();
                      logger.logEvent(COMPONENT, "new request is being processed: parsing", "normal");
                      AbstractMessage parsedMessage = parser.parse(message);
                      if (parsedMessage instanceof InvitationMessage){
                          /**
                          InvitationMessage mes = (InvitationMessage) parsedMessage;
                          RemoteKeyGenerationCoordinator coordinator = new RemoteKeyGenerationCoordinator(CUSTOM_AUTHENTICATION_PRESENTER);
                          coordinator.open(CUSTOM_AUTHENTICATION_PRESENTER.getServiceContext());
                          String extra =  mes.getLogin() + "," + mes.getApplicationName();
                          logger.logEvent(COMPONENT, "submitting credential details", "low", extra);
                          coordinator.submitLoginDetails(mes.getLogin(), mes.getApplicationName());
                          extra =  String.valueOf(mes.getThreshold());
                          logger.logEvent(COMPONENT, "submitting credential details: threshold", "low", extra);
                          coordinator.submitThreshold(mes.getThreshold());
                          ArrayList<Participant> participants = new ArrayList<>();
                          List<IdentifiedParticipant> list = mes.getParticipants();
                          int lowestIdentifier = mes.getTotalWeight();
                          for(IdentifiedParticipant part: list){
                              participants.add(part);
                              if (part.getIdentifier() < lowestIdentifier){
                                  lowestIdentifier = part.getIdentifier();
                              }
                          }
                          CustomIdentifiedParticipant main = new CustomIdentifiedParticipant(1, communicationConnection.getAddress(), lowestIdentifier - 1, false);
                          participants.add(main);
                          KeyGenerationClient client = new RemoteLogicalKeyGenerationClientForRemoteCoordinator(main, null);
                          ThreadedKeyGenerationClient threadedKeyGenerationClient = new ThreadedKeyGenerationClient(client);
                          coordinator.addMainClient(threadedKeyGenerationClient);
                          coordinator.addCoordinatorStub(main.getAddress());
                          coordinator.submitSelection(participants);
                          coordinator.run();
                        **/
                      } else if (parsedMessage instanceof ConnectMessage){
                          //TO DO
                          logger.logEvent("Custom communcation", "received connection request", "low");
                      } else if (parsedMessage instanceof StartSignMessage){
                          StartSignMessage parsedStartSignMessage  = (StartSignMessage) parsedMessage;
                          registerLocalAddress(parsedStartSignMessage.getLocalAddress());
                          String name = parsedStartSignMessage.getName();
                          //String login = parsedStartSignMessage.getLogin();
                          int number = parsedStartSignMessage.getNumber();
                          //Task task = new Task(name, login, new Requester() {
                            //  @Override
                              //public void signalJobDone() {
                                //  logger.logEvent(COMPONENT, "signature completed", "normal");
                              //}
                          //});
                          //String extra = name + "," + login + "," +  String.valueOf(number);
                          //logger.logEvent(COMPONENT, "signature started", "normal", extra);
                          RemoteSignatureCoordinator coordinator = new RemoteSignatureCoordinator(address);
                          coordinator.open(CUSTOM_AUTHENTICATION_PRESENTER.getServiceContext());
                          coordinator.setNumberToRequest(number);
                          //coordinator.sign(task);
                      }
                } catch (IOException e) {
                    e.printStackTrace(); //most likely cause: either the socket was closed or bluetooth was disabled
                    logger.logError(COMPONENT, "IO error", "critical", e.getMessage());
                } catch (Exception e){
                    e.printStackTrace();
                    logger.logError(COMPONENT, "unexpected error", "critical");
                }
            }
        });
        thread.start();
    }




    @Override
    public void closeServiceServer() {
        if (server != null){
            shouldStop = true;
            AndroidCommunicationServer cache = server;
            server = null;
            if (cache != null){
                cache.closeServer();
            }
        }
    }

    @Override
    public AndroidBiDirectionalCommunicationConnection getConnectionWith(String address) throws IOException {
        AndroidBiDirectionalCommunicationConnection connection = null;
        synchronized (cachedConnections) {
             connection = cachedConnections.getOrDefault(address, null);
        }
        if (connection == null || connection.getState() == AndroidBiDirectionalCommunicationConnection.STATE_IDLE){
            connection = new AndroidBiDirectionalCommunicationConnection(address);
            logger.logEvent(COMPONENT, "establishing connection", "low", address);
            connection.establishConnectionTo();
            logger.logEvent(COMPONENT, "established connection", "low", address);
            synchronized (cachedConnections){
                cachedConnections.put(address, connection);
            }
        }
        return connection;
    }

    @Override
    public void handleBrokenConnection(AndroidBiDirectionalCommunicationConnection connection) {
        if (connection != null){
            synchronized (cachedConnections){
                cachedConnections.remove(connection.getAddress());
                connection.closeConnection();
            }
        }
    }

    @Override
    public void closeConnection(AndroidBiDirectionalCommunicationConnection connection) {
        synchronized (cachedConnections){
            cachedConnections.remove(connection.getAddress());
            connection.closeConnection();
        }
    }

    @Override
    public void closeAllConnections() {
        synchronized (cachedConnections){
            for (AndroidBiDirectionalCommunicationConnection connection : cachedConnections.values()){
                connection.closeConnection();
            }
            cachedConnections.clear();
        }
    }

    @Override
    public void registerLocalAddress(String address) {
        logger.logEvent(COMPONENT, "new local address registered", "low", address);
        localAddress = address;
    }

    private class ConnectionCreator {
        private int numberOfAnswers;

        private final int numberOfRequiredAnswers;

        private final ConnectionRequester requester;

        public ConnectionCreator(int numberOfRequiredAnswers, ConnectionRequester requester) {
            this.numberOfRequiredAnswers = numberOfRequiredAnswers;
            this.requester = requester;
        }

        public synchronized void  establishedConnection(String address){
            requester.isAvailable(address);
            numberOfAnswers += 1;
            if (numberOfAnswers == numberOfRequiredAnswers){
                requester.signalJobDone();
            }
        }

        public synchronized void failedToEstablishConnection(){
            numberOfAnswers += 1;
            if (numberOfAnswers == numberOfRequiredAnswers){
                requester.signalJobDone();
            }
        }



    }

    @Override
    public void establishConnectionsWith(Set<String> addresses, ConnectionRequester requester) {
        logger.logEvent(COMPONENT, "establishing connection with a number of remotes", "normal");
        ConnectionCreator connectionCreator = new ConnectionCreator(addresses.size(), requester);
        for( String address : addresses){
            ThreadPoolSupplier.getSupplier().execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        getConnectionWith(address);
                        connectionCreator.establishedConnection(address);
                    } catch (IOException e) {
                        connectionCreator.failedToEstablishConnection();
                    }
                }
            });
        }


    }

    public void abortOnAllConnections(String applicationName, String login) {
        synchronized (cachedConnections){
            byte[] abortMessage = encoder.makeAbortMessage(applicationName, login);
            for (AndroidBiDirectionalCommunicationConnection communicationConnection: cachedConnections.values()){
                synchronized (communicationConnection){
                    try{
                        communicationConnection.createIOtStreams();
                        communicationConnection.writeToConnection(abortMessage);
                        communicationConnection.closeIOStreams();
                    } catch (Exception e){
                        e.printStackTrace();
                    } finally {
                        communicationConnection.closeConnection();
                    }
                }
            }
            cachedConnections.clear();
        }
    }

    @Override
    public boolean isBluetoothAvailable() {
        return bluetoothMonitor.isBluetoothAvailable();
    }

    @Override
    public boolean isBluetoothEnabled() {
        return bluetoothMonitor.isBluetoothEnabled();
    }

    @Override
    public ArrayList<Device> getPairedDevices() {
        return bluetoothMonitor.getPairedDevices();
    }

    public enum LOCATION {
        REMOTE,
        UNDEFINED,
        LOCAL;
    }
}
