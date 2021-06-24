package com.project.collaborativeauthenticationapplication.alternative.management.extend;

import android.content.Context;

import com.project.collaborativeauthenticationapplication.alternative.key.KeyManagementPresenter;
import com.project.collaborativeauthenticationapplication.alternative.management.PersistenceCoordinator;

import com.project.collaborativeauthenticationapplication.alternative.management.application.ThreadedPersistenceClient;
import com.project.collaborativeauthenticationapplication.logger.AndroidLogger;
import com.project.collaborativeauthenticationapplication.logger.Logger;
import com.project.collaborativeauthenticationapplication.service.concurrency.ThreadPoolSupplier;
import com.project.collaborativeauthenticationapplication.service.crypto.BigNumber;
import com.project.collaborativeauthenticationapplication.service.crypto.Point;

import java.util.ArrayList;
import java.util.List;

public abstract class ExtendingCoordinator implements PersistenceCoordinator {


    private Logger logger = new AndroidLogger();

    public static final String COMPONENT = "extending coordinator EX";

    private final KeyManagementPresenter presenter;

    private ThreadedPersistenceClient persistenceClient;



    private ArrayList<ExtendingClient> clients = new ArrayList<>();
    private LocalExtendingClient localExtendingClient;

    protected ExtendingCoordinator(KeyManagementPresenter presenter) {
        this.presenter = presenter;
    }


    protected KeyManagementPresenter getPresenter() {
        return presenter;
    }

    public void open(Context context){
        logger.logEvent(COMPONENT, "open ", "low");
        persistenceClient = new  ThreadedPersistenceClient(this);
        persistenceClient.open(context);
    }

    public String getApplicationName(){
        return getPresenter().getApplicationName();
    }



    public int getNumberOfSecretsFor(String device){
        return persistenceClient.getNumberOfSecretsFor(getApplicationName(), device);
    }

    public int getThreshold(){
        return  persistenceClient.getThresholdFor(getApplicationName());
    }

    public List<String> getAllDevices(){
        return persistenceClient.getAllRemotes(getApplicationName());
    }

    public int[] getIdentifiersFor(String device){
        return persistenceClient.getIdentifiers(getApplicationName(), device);
    }

    public int[] getLocalIdentifiers(){
        return persistenceClient.getLocalIdentifiers(getApplicationName());
    }


    private int newIdentifier;



    protected void setNewIdentifier(int newIdentifier) {
        logger.logEvent(COMPONENT, "setting new identifier", "low", String.valueOf(newIdentifier));
        this.newIdentifier = newIdentifier;
    }



    protected void confirm(){
        logger.logEvent(COMPONENT, "confirm ", "low");
        logger.logEvent(COMPONENT, "confirm ", "low", getApplicationName());
        logger.logEvent(COMPONENT, "confirm ", "low", getDevice());
        logger.logEvent(COMPONENT, "confirm ", "low", String.valueOf(newIdentifier));
        persistenceClient.persist(newIdentifier, getApplicationName(), getDevice());
    }



    public void start(){
        ThreadPoolSupplier.getSupplier().execute(new Runnable() {
            @Override
            public void run() {
                logger.logEvent(COMPONENT, "start run", "high");
                runExtend();
            }
        });
    }

    protected abstract void runExtend();


    private String device;

    public String getDevice() {
        return device;
    }

   public void setDevice(String device) {
        logger.logEvent(COMPONENT, "setting device", "low", device);
        this.device = device;
       logger.logEvent(COMPONENT, "setting device", "low", getDevice());
   }

    protected void buildAllClients(){
        logger.logEvent(COMPONENT, "building", "high");
        String devicePresenter = getPresenter().getDevice();
        String deviceNow       = getDevice();
        this.device = (devicePresenter==null)? deviceNow : devicePresenter;
        logger.logEvent(COMPONENT, "new device", "low", device);
        clients.clear();
        localExtendingClient = new LocalExtendingClient(this);
        List<String> list = getDeviceList();
        for (String participant : list){
                clients.add(buildRemoteClient(participant));
        }
        clients.add(localExtendingClient);

    }

    protected abstract List<String> getDeviceList();

    protected List<String> getRemoteList() {
        return persistenceClient.getAllParticipants(getPresenter().getApplicationName());
    }

    public ArrayList<ExtendingClient> getClients() {
        return clients;
    }

    protected abstract RemoteExtendingClient buildRemoteClient(String device);

    public synchronized void passToLocal(BigNumber slice){
        logger.logEvent(COMPONENT, "pass to local ", "low");
        localExtendingClient.receiveSlice(slice);
    }


    protected abstract ArrayList<ExtendingClient> getCalculatingClients();



    public  synchronized void distributeSlices(ArrayList<BigNumber> slices){
        logger.logEvent(COMPONENT, "distributing slices", "low");
        List<ExtendingClient> clients = getCalculatingClients();
        for (int i = 0;  i < clients.size(); i++){
            logger.logEvent(COMPONENT, "distributing slices to ", "low", clients.get(i).getDevice());
            clients.get(i).receiveSlice(slices.get(i));
            logger.logEvent(COMPONENT, "distributing slices to (done) ", "low", clients.get(i).getDevice());
        }
    }

    public  int getWeight(){
        return persistenceClient.getWeightFor(getApplicationName());
    }

    public  int getNumberOfRemotes(){
        return persistenceClient.getNumberOfRemotes(getApplicationName());
    }


    public int getNumberOfTotalSecrets(){
        return persistenceClient.getNumberOfTotalSecrets(getApplicationName());
    }

    public abstract void persisted();

    public  ArrayList<BigNumber> getLocalShares(){
        return persistenceClient.getAllLocalShares(getApplicationName());
    }

    public  Point getPublicKey(){
        return persistenceClient.getPublicKey(getApplicationName());
    }

    public  ArrayList<BigNumber> getAllLocalShares(){
        return persistenceClient.getAllLocalSecrets(getApplicationName());
    }

    public abstract void submitMessage(BigNumber message);

    public abstract String getMain();
}
