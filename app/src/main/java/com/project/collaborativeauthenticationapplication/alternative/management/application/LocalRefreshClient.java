package com.project.collaborativeauthenticationapplication.alternative.management.application;

import com.project.collaborativeauthenticationapplication.logger.AndroidLogger;
import com.project.collaborativeauthenticationapplication.logger.Logger;
import com.project.collaborativeauthenticationapplication.service.crypto.BigNumber;
import com.project.collaborativeauthenticationapplication.service.crypto.CryptoRefreshShareUnit;
import com.project.collaborativeauthenticationapplication.service.crypto.RandomnessGenerator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class LocalRefreshClient extends RefreshClient {

    CryptoRefreshShareUnit unit = new CryptoRefreshShareUnit();
    private ArrayList<ArrayList<BigNumber>> shareParts;
    private int submittedParticipants = 0;

    protected LocalRefreshClient(RefreshCoordinator coordinator) {
        super(coordinator);
    }



    private Logger logger = new AndroidLogger();

    public static final String COMPONENT = "local refresh client RF";


    RandomnessGenerator randomnessGenerator = new RandomnessGenerator();

    @Override
    protected Runnable getRefreshCode() {
        return new Runnable() {
            @Override
            public void run() {
                final int threshold       =  getCoordinator().getThreshold();
                ArrayList<BigNumber> poly = new ArrayList<>();
                for (int i = 1; i < threshold; i++){
                    poly.add(randomnessGenerator.generateRandomness());
                }
                poly.add(BigNumber.getZero());
                logger.logEvent(COMPONENT, "poly generated", "low");

                HashMap<String, ArrayList<BigNumber>> refreshShares= new HashMap<>();

                final List<String> devices = getCoordinator().getAllDevices();

                logger.logEvent(COMPONENT, "poly evaluation", "low");

                for (String device: devices){
                    if (!devices.equals(getRemove())){
                        logger.logEvent(COMPONENT, "poly evaluation", "low", device);
                        int[] identifier = getCoordinator().getIdentifiersFor(device);
                        logger.logEvent(COMPONENT, "poly evaluation", "low", String.valueOf(identifier));
                        ArrayList<BigNumber> shares = unit.createShares(poly, identifier);
                        refreshShares.put(device, shares);
                    }
                }
                ArrayList<BigNumber> shares = unit.createShares(poly, getCoordinator().getLocalIdentifiers());
                refreshShares.put(getDevice(), shares);

                logger.logEvent(COMPONENT, "distributed", "low");

                getCoordinator().distributeShares(refreshShares);
            }
        };
    }



    @Override
    public void receiveRefreshShares(ArrayList<BigNumber> bigNumbers) {
        logger.logEvent(COMPONENT, "receive shares", "low");
        if (shareParts == null){
            logger.logEvent(COMPONENT, "receive shares first", "low");
            shareParts      = new ArrayList<>();
            for (int i = 0; i < getCoordinator().getWeight(); i++){
                ArrayList<BigNumber> part = new ArrayList<>();
                part.add(bigNumbers.get(i));
                shareParts.add(part);
            }
            submittedParticipants = 1;
        } else {
            logger.logEvent(COMPONENT, "receive shares later", "low");
            for (int i = 0; i < getCoordinator().getWeight(); i++){
                shareParts.get(i).add(bigNumbers.get(i));

            }
            submittedParticipants += 1;
        }
        int expected = getCoordinator().getNumberOfRemotes()+1;
        if (getRemove() != null){
            expected  = expected -1;
        }
        logger.logEvent(COMPONENT, "expected", "low", String.valueOf(expected));

        logger.logEvent(COMPONENT, "submitted", "low", String.valueOf(submittedParticipants));

        if (submittedParticipants == expected ){
            logger.logEvent(COMPONENT, "final", "low");
            ArrayList<BigNumber> locals = getCoordinator().getLocalShares();
            for (int i = 0; i < getCoordinator().getWeight(); i++){
                shareParts.get(i).add(locals.get(i));
            }
            ArrayList<BigNumber> shares;
            shares = unit.calculateShares(shareParts);
            shareParts = null;
            getCoordinator().submitFinalRefreshedShares(shares);
        }
    }

    @Override
    public void close() {}

}
