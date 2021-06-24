package com.project.collaborativeauthenticationapplication.alternative.management.extend;

import android.content.Context;

import com.project.collaborativeauthenticationapplication.alternative.key.KeyManagementPresenter;
import com.project.collaborativeauthenticationapplication.alternative.key.application.GuardLeaderExtending;
import com.project.collaborativeauthenticationapplication.alternative.network.AndroidConnection;
import com.project.collaborativeauthenticationapplication.alternative.network.Network;
import com.project.collaborativeauthenticationapplication.logger.AndroidLogger;
import com.project.collaborativeauthenticationapplication.logger.Logger;
import com.project.collaborativeauthenticationapplication.service.crypto.BigNumber;
import com.project.collaborativeauthenticationapplication.service.network.messages.MessageEncoder;

import java.util.ArrayList;
import java.util.List;

public class RemoteExtendingCoordinator extends ExtendingCoordinator{


    private static final String COMPONENT = "Remote extending Coordinator  EX";

    private static Logger logger = new AndroidLogger();


    private String main;

    public RemoteExtendingCoordinator(KeyManagementPresenter presenter) {
        super(presenter);
    }


    AndroidConnection connectionWithTarget;

    GuardLeaderExtending guardLeaderExtending;

    @Override
    protected void runExtend() {
        if (isCalculating){
            logger.logEvent(COMPONENT, "run extend", "low");
            buildAllClients();

            for (ExtendingClient remote : getClients()){
                if (remote.isLocal()){
                    remote.calculate(weight);
                } else {
                    remote.calculate(0);
                }
            }

            connectionWithTarget = Network.getInstance().getConnectionWithInMode(targetAddress, AndroidConnection.MODE_SLAVE_MULTI);

            logger.logEvent(COMPONENT, "go clients extend", "low");
            for (ExtendingClient client : getClients()){
                client.go(remotes, newIdentifier,  getDevice(), weights);
            }
        } else {

        }
    }

    @Override
    protected List<String> getDeviceList() {
        ArrayList<String> result = new ArrayList<>();
        for (String device: remotes){
            if (!device.equals("here")){
                result.add(device);
            }
        }
        return result;
    }


    public void setMain(String main){
        this.main = main;
    }

    @Override
    public void open(Context context) {
        super.open(context);
        guardLeaderExtending = new GuardLeaderExtending(this);
        guardLeaderExtending.start();
    }

    @Override
    protected RemoteExtendingClient buildRemoteClient(String device) {
        return new RemoteRemoteExtendingClient(this, device);
    }

    @Override
    protected ArrayList<ExtendingClient> getCalculatingClients() {
        return getClients();
    }

    @Override
    public void persisted() {
        logger.logEvent(COMPONENT, "persisted extend", "low");
        confirm();
        getPresenter().onFinished();

    }

    @Override
    public void submitMessage(BigNumber message) {
        logger.logEvent(COMPONENT, "submit message extend", "low");
        logger.logEvent(COMPONENT, "submit message extend", "low", connectionWithTarget.getAddress());
        final MessageEncoder encoder = new MessageEncoder();
        connectionWithTarget.writeToConnection(encoder.makeExtendMessageMessage(message, weight));
        connectionWithTarget.pushForFinal();
    }

    @Override
    public String getMain() {
        return main;
    }

    private int weight;



    public void setWeight(int weight) {
        this.weight = weight;
    }




    private  int[] weights;

    public void setWeights(int[] weights) {
        logger.logEvent(COMPONENT, "weights", "low", String.valueOf(weights.length));
        this.weights = weights;
    }



    private  String targetAddress;

    public void setTargetAddress(String targetAddress) {
        this.targetAddress = targetAddress;
    }


    private boolean isCalculating = false;


    public void setCalculating(boolean calculating) {
        isCalculating = calculating;
    }

    private  int newIdentifier;

    public void setNewIdentifier(int newIdentifier) {
        this.newIdentifier = newIdentifier;
        super.setNewIdentifier(newIdentifier);
    }

    private ArrayList<String> remotes;


    public void setRemotes(ArrayList<String> remotes) {
        this.remotes = remotes;
    }

    public void ok() {
        logger.logEvent(COMPONENT, "ok extend", "low");
        getPresenter().isRunnable();
    }
}
