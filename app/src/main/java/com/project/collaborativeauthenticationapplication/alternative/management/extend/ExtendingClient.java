package com.project.collaborativeauthenticationapplication.alternative.management.extend;

import com.project.collaborativeauthenticationapplication.logger.AndroidLogger;
import com.project.collaborativeauthenticationapplication.logger.Logger;
import com.project.collaborativeauthenticationapplication.service.concurrency.ThreadPoolSupplier;
import com.project.collaborativeauthenticationapplication.service.crypto.BigNumber;

import java.util.List;

public abstract class  ExtendingClient {


    private Logger logger = new AndroidLogger();

    public static final String COMPONENT = "extending client EX";

    private final ExtendingCoordinator coordinator;

    protected ExtendingClient(ExtendingCoordinator coordinator) {
        this.coordinator = coordinator;
    }

    public boolean isLocal(){
        return false;
    }

    public String getDevice(){
        return "here";
    }


    public ExtendingCoordinator getCoordinator() {
        return coordinator;
    }

    private boolean calculating = false;

    private int weight         = 0;


    protected void setWeight(int weight) {
        this.weight = weight;
    }

    protected int getWeight() {
        return weight;
    }

    public void go(List<String> remotes, int newIdentifier, String address, int[] weights){
        logger.logEvent(COMPONENT, "go ", "low", getDevice());
        Runnable code;
        if (calculating){
            logger.logEvent(COMPONENT, "cal ", "low");
            code = getCalculatingCode(remotes, newIdentifier, address, weight, weights);
        } else {
            logger.logEvent(COMPONENT, "wait ", "low");
            code = getWaitingCode(newIdentifier, address);
        }
        ThreadPoolSupplier.getSupplier().execute(code);
    }


    protected abstract Runnable getCalculatingCode(List<String> remotes, int newIdentifier, String address, int weight, int[] weights);

    protected abstract Runnable getWaitingCode(int newIdentifier, String address);




    protected void  setCalculating(boolean calculating){
        this.calculating = calculating;
    }

    public abstract void calculate(int weight);

    public abstract void waitTillCalculated();

    public abstract void receiveSlice(BigNumber bigNumber);

    public abstract void persist();
}
