package com.project.collaborativeauthenticationapplication.alternative.management.extend;

import com.project.collaborativeauthenticationapplication.logger.AndroidLogger;
import com.project.collaborativeauthenticationapplication.logger.Logger;
import com.project.collaborativeauthenticationapplication.service.crypto.BigNumber;
import com.project.collaborativeauthenticationapplication.service.crypto.CryptoExtendUnit;
import com.project.collaborativeauthenticationapplication.service.crypto.RandomnessGenerator;

import java.util.ArrayList;
import java.util.List;

public class LocalExtendingClient extends ExtendingClient{



    private Logger logger = new AndroidLogger();

    public static final String COMPONENT = "local extending client EX";


    private final CryptoExtendUnit unit = new CryptoExtendUnit();
    private final RandomnessGenerator randomnessGenerator = new RandomnessGenerator();
    private int submittedParticipants = 0;


    private int expected;

    public LocalExtendingClient(ExtendingCoordinator coordinator) {
        super(coordinator);
    }

    @Override
    public boolean isLocal() {
        return true;
    }

    @Override
    protected Runnable getCalculatingCode(List<String> remotes, int newIdentifier, String address, int weight, int[] weights) {
        return new Runnable() {
            @Override
            public void run() {
                logger.logEvent(COMPONENT, "calculating code", "low");
                final  int size =  remotes.size();
                expected = size;
                logger.logEvent(COMPONENT, "calculating code: size", "low", String.valueOf(expected));
                ArrayList<BigNumber> randomness = new ArrayList<>();
                for (int i = 1; i < size; i++){
                    randomness.add(randomnessGenerator.generateRandomness());
                }
                ExtendingCoordinator coordinator = getCoordinator();
                ArrayList<BigNumber> shares = coordinator.getAllLocalShares();

                int[] identifiers = new int[coordinator.getThreshold()];

                logger.logEvent(COMPONENT, "calculating code: local weight", "low", String.valueOf(weight));

                System.arraycopy(coordinator.getLocalIdentifiers(), 0, identifiers, 0, weight);

                int currentIndex = weight;

                for(int i = 0; i < remotes.size(); i++){
                    String remote = remotes.get(i);
                    if (!remote.equals("here")){
                        int current = weights[i];
                        logger.logEvent(COMPONENT, "calculating code: remote ", "low", remote);
                        logger.logEvent(COMPONENT, "calculating code: remote weight", "low", String.valueOf(current));
                        System.arraycopy(coordinator.getIdentifiersFor(remote), 0, identifiers, currentIndex, current);
                        currentIndex = current + currentIndex;
                    }
                }
                ArrayList<BigNumber> slices = unit.getSlices(identifiers, shares, weight, newIdentifier, randomness);
                coordinator.distributeSlices(slices);
            }
        };
    }

    @Override
    protected Runnable getWaitingCode(int newIdentifier, String address) {
        return null;
    }

    @Override
    public void calculate(int weight) {
        setCalculating(true);
        setWeight(weight);
    }

    @Override
    public void waitTillCalculated() {
        throw new IllegalStateException();
    }

    ArrayList<BigNumber> slices;

    @Override
    public void receiveSlice(BigNumber bigNumber) {
        logger.logEvent(COMPONENT, "receive slices", "low");
        if (slices == null){
            logger.logEvent(COMPONENT, "receive slices first", "low");
            slices      = new ArrayList<>();
            slices.add(bigNumber);
            submittedParticipants = 1;
        } else {
            logger.logEvent(COMPONENT, "receive slices later", "low");
            slices.add(bigNumber);
            submittedParticipants += 1;
        }


        logger.logEvent(COMPONENT, "expected", "low", String.valueOf(expected));

        logger.logEvent(COMPONENT, "submitted", "low", String.valueOf(submittedParticipants));

        if (submittedParticipants == expected ){
            logger.logEvent(COMPONENT, "final", "low");
            BigNumber message = unit.calculateMessage(slices);
            slices = null;
            getCoordinator().submitMessage(message);
        }
    }

    @Override
    public void persist() {
        logger.logEvent(COMPONENT, "persist", "low");
    }
}
