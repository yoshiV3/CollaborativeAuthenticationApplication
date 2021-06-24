package com.project.collaborativeauthenticationapplication.service.signature.application.distributed;
import android.content.Context;

import com.project.collaborativeauthenticationapplication.alternative.network.AndroidConnection;
import com.project.collaborativeauthenticationapplication.alternative.network.Network;
import com.project.collaborativeauthenticationapplication.logger.AndroidLogger;
import com.project.collaborativeauthenticationapplication.service.controller.CustomServiceMonitor;
import com.project.collaborativeauthenticationapplication.service.crypto.BigNumber;
import com.project.collaborativeauthenticationapplication.service.crypto.Point;
import com.project.collaborativeauthenticationapplication.service.crypto.RandomnessGenerator;
import com.project.collaborativeauthenticationapplication.service.general.IllegalNumberOfTokensException;
import com.project.collaborativeauthenticationapplication.service.general.Requester;
import com.project.collaborativeauthenticationapplication.service.general.ServiceStateException;
import com.project.collaborativeauthenticationapplication.service.general.Task;
import com.project.collaborativeauthenticationapplication.service.network.ConnectionRequester;
import com.project.collaborativeauthenticationapplication.service.network.CustomCommunication;
import com.project.collaborativeauthenticationapplication.service.network.messages.MessageEncoder;
import com.project.collaborativeauthenticationapplication.service.signature.SignaturePresenter;
import com.project.collaborativeauthenticationapplication.service.signature.application.SignatureClient;
import com.project.collaborativeauthenticationapplication.service.signature.application.ThreadedSignatureClient;
import com.project.collaborativeauthenticationapplication.service.signature.application.local.DatabaseInformationRequester;
import com.project.collaborativeauthenticationapplication.service.signature.application.local.InformationSignatureClient;
import com.project.collaborativeauthenticationapplication.service.signature.application.local.LocalInformationSignatureClient;
import com.project.collaborativeauthenticationapplication.service.signature.application.local.SignatureRequester;
import com.project.collaborativeauthenticationapplication.service.signature.application.local.SignatureTask;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class LocalSignatureCoordinator extends AbstractSignatureCoordinator {

    public static final String COMPONENT = "Signature Coordinator";

    private interface SpecialRequester extends Requester {
        void setCode(Runnable code);
    }


    private final MessageEncoder encoder = new MessageEncoder();

    HashMap<String, Integer> remotes = new HashMap<>();


    private SignaturePresenter presenter;

    private ArrayList<SignatureClient> clients;



    private RandomnessGenerator randomnessGenerator;



    private int signThreshold         = 0;
    private int numberOfLocalKeys     = 0;
    private int numberToRequestRemote = 0;

    private Set<String> availableRemotes = new HashSet<>();


    private HashMap<String, Integer> requestedNumberOfShares;



    HashMap<String, ArrayList<Point>>  eCommitment = new HashMap<>();
    HashMap<String, ArrayList<Point>>  dCommitment = new HashMap<>();

    ArrayList<BigNumber> signatureParts = new ArrayList<>();



    boolean producingSignature = false;
    boolean haveAllCommitments = false;


    private AndroidLogger logger = new AndroidLogger();




    public LocalSignatureCoordinator(SignaturePresenter presenter){
        this.presenter      = presenter;
        randomnessGenerator = new RandomnessGenerator();
    }

    private void generateRandomness(){
        dCommitment.clear();
        eCommitment.clear();
        startRandomness();
        for (SignatureClient client : clients){
            requestSharesFromClient(client);
        }
    }

    private void produceSignatureShares(){
        logger.logEvent(COMPONENT, "producing signatures", "low");
        signatureParts.clear();
        for (SignatureClient client : clients){
            Task originalTask = getOriginalTask();
            Requester requester = new Requester() {
                @Override
                public void signalJobDone() {
                    boolean ok = false;
                    synchronized (signatureParts){
                        if (signatureParts.size() == clients.size()){
                            if (!producingSignature){
                                producingSignature = true;
                                ok = true;
                            }
                        }
                    }
                    if(ok){
                        produceSignature();
                    }

                }
            };
            SignatureTask task = new SignatureTask(originalTask.getApplicationName(), originalTask.getLogin(), requester, eCommitment, dCommitment, getMessage());
            client.sign(task);
        }
    }

    private void produceSignature() {
        logger.logEvent(COMPONENT, "producing signatures: final computation", "low");
        getClient().calculateFinalSignature(signatureParts, new SignatureRequester() {
            @Override
            public void submitSignature(BigNumber signature) {
                setSignature(signature);
            }

            @Override
            public void signalJobDone() {
                logger.logEvent(COMPONENT, "computations completed", "normal");
                getOriginalTask().done();
            }
        });
        producingSignature = false;
    }

    @Override
    public void sign(Task task) {
       setMessage(randomnessGenerator.generateRandomness());
        DatabaseInformationRequester databaseInformationRequester = new DatabaseInformationRequester() {
            @Override
            public void setThreshold(int threshold) {
                signThreshold = threshold;
            }

            @Override
            public void setNumberOfRemoteKeysForRemoteParticipant(String address, int remoteKeys) {
                synchronized (remotes){
                    remotes.put(address, new Integer(remoteKeys));
                }
                String extra = address + "," + String.valueOf(remoteKeys) ;
                logger.logEvent(COMPONENT, "added new possible remote", "low", extra);
            }

            @Override
            public void setNumberOfLocalKeys(int localKeys) {
                numberOfLocalKeys = localKeys;
            }

            @Override
            public void signalJobDone() {
                searchForTheRequiredShares();
            }
        };
        setOriginalTask(task);
        getClient().checkInformationAboutCredential(task.getApplicationName(), databaseInformationRequester);

    }

    @Override
    public void addSignaturePart(BigNumber signaturePart) {
        logger.logEvent(COMPONENT, "new signature part", "low");
        synchronized (signatureParts){
            signatureParts.add(signaturePart);
        }
    }

    private void searchForTheRequiredShares() {
        requestedNumberOfShares = new HashMap<>();

        if (numberOfLocalKeys >= signThreshold){
            requestedNumberOfShares.put("here", signThreshold);
            logger.logEvent(COMPONENT, "enough local shares to calculate alone", "low");
            generateRandomness();
        } else {
            requestedNumberOfShares.put("here", numberOfLocalKeys);
            numberToRequestRemote = signThreshold - numberOfLocalKeys;
            logger.logEvent(COMPONENT, "not enough local shares to calculate alone, looking for a number of shares", "low", String.valueOf(numberToRequestRemote));

            availableRemotes.clear();

            Network communication = Network.getInstance();

            SignatureCoordinator coordinator = this;


            ConnectionRequester requester = new ConnectionRequester() {
                @Override
                public void isAvailable(String address) {
                    availableRemotes.add(address);
                }

                @Override
                public void signalJobDone() {
                    logger.logEvent(COMPONENT, "Discovery of the remotes is done", "low", String.valueOf(availableRemotes.size()));
                    int stillNeeded = numberToRequestRemote;
                    try {
                        for (String address : availableRemotes){
                            if (stillNeeded != 0) {
                                Integer number = remotes.getOrDefault(address, 0);
                                SignatureClient client;
                                if (number >= stillNeeded) {
                                    client = new RemoteSignatureClient(coordinator, address);
                                    requestedNumberOfShares.put(address, Integer.valueOf(stillNeeded));
                                    stillNeeded = 0;

                                } else {
                                    stillNeeded = stillNeeded - number;
                                    client = new RemoteSignatureClient(coordinator, address);
                                    requestedNumberOfShares.put(address, Integer.valueOf(number));
                                }
                                clients.add(new ThreadedSignatureClient(client));
                            } else {
                               AndroidConnection con = Network.getInstance().getConnectionWith(address);
                               byte[] mes =  encoder.makeAbortMessage("computation does not need you", address);
                               con.writeToConnection(mes);
                               con.pushForFinal();
                            }
                        }
                        if (stillNeeded != 0){
                            logger.logError(COMPONENT, "not enough shares", "low");
                            for (String address : availableRemotes){
                                AndroidConnection con = Network.getInstance().getConnectionWith(address);
                                byte[] mes =  encoder.makeAbortMessage("computation does not need you", address);
                                con.writeToConnection(mes);
                                con.pushForFinal();
                            }
                            presenter.onErrorSignature("Not enough shares available");
                        } else {
                            logger.logEvent(COMPONENT, "enough shares: proceeding", "normal");
                            generateRandomness();
                        }
                    } catch (IOException e){
                        logger.logError(COMPONENT, "IOEXCEPTION DURING setuo", "normal");
                    }
                }
            };
            communication.establishConnectionsWithInTopologyOne(remotes.keySet(), requester);
        }
    }



    @Override
    public synchronized void abort() {
        logger.logEvent(COMPONENT, "abort", "high");
        if (getState() != STATE_ERROR){
            presenter.onErrorSignature("One of the devices aborted the computation");
            Network.getInstance().closeAllConnections();
            setState(STATE_ERROR);
        }
    }


    @Override
    public void open(Context context) {
        if (getState() != STATE_INIT)
        {
            throw  new IllegalStateException();
        }
        if (clients != null && clients.size() != 0){
            throw new IllegalStateException();
        }
        if(getClient() != null && getClient().getState() != STATE_INIT){
            throw new IllegalStateException();
        } else if (getClient() == null){
            InformationSignatureClient infoClient = new LocalInformationSignatureClient(this);
            setClient(infoClient);
        }
        clients = new ArrayList<>();
        getClient().open(context);
        clients.add(getClient());
        if (CustomServiceMonitor.getInstance().isServiceEnabled()) {
            try {
                getToken();
                setState(STATE_START);
            } catch (IllegalNumberOfTokensException | ServiceStateException e) {
                setState(STATE_ERROR);
                presenter.onErrorSignature("Could not open a client");
            }
        }
        else
        {
            presenter.onErrorSignature("Service is no longer working properly");
        }
    }

    @Override
    protected int getNumberOfRequestedSharesForClient(SignatureClient client) {
        return requestedNumberOfShares.getOrDefault(client.getAddress(), 0);
    }

    @Override
    protected void handleCommitmentE(List<Point> commitment, String address) {
        logger.logEvent(COMPONENT, "handle new commitment E", "low");
        synchronized (eCommitment){
            ArrayList<Point> com = new ArrayList<>(commitment);
            eCommitment.put(address, com);
        }
    }

    @Override
    protected void handleCommitmentD(List<Point> commitment, String address) {
        logger.logEvent(COMPONENT, "handle new commitment D", "low");
        synchronized (dCommitment){
            ArrayList<Point> com = new ArrayList<>(commitment);
            dCommitment.put(address, com);
        }
    }

    @Override
    protected void randomnessGenerationDone() {
        logger.logEvent(COMPONENT, "a client is done", "low");
        boolean ok = false;
        synchronized (dCommitment){
            if(dCommitment.keySet().size()== clients.size()){
                if (!haveAllCommitments){
                    haveAllCommitments = true;
                    ok = true;
                }
            }
        }
        if (ok){
            produceSignatureShares();
        }
    }

    @Override
    public void close() {
        if (clients != null){
            logger.logEvent(COMPONENT, "called close", "low");
            clients.clear();
            clients = null;
        }
        super.close();
    }
}
