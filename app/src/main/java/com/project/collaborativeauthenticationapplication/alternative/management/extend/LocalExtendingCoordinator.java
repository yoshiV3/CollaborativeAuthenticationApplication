package com.project.collaborativeauthenticationapplication.alternative.management.extend;

import com.project.collaborativeauthenticationapplication.alternative.key.KeyManagementPresenter;
import com.project.collaborativeauthenticationapplication.alternative.network.Network;
import com.project.collaborativeauthenticationapplication.logger.AndroidLogger;
import com.project.collaborativeauthenticationapplication.logger.Logger;
import com.project.collaborativeauthenticationapplication.service.crypto.BigNumber;

import java.util.ArrayList;
import java.util.List;

public class LocalExtendingCoordinator extends ExtendingCoordinator{


    private static final String COMPONENT = "local extending coordinator  EX";

    private Logger logger = new AndroidLogger();

    public LocalExtendingCoordinator(KeyManagementPresenter presenter) {
        super(presenter);
    }


    private RemoteExtendClient remoteExtendClient;

    ArrayList<ExtendingClient> calculatingClients = new ArrayList<>();

    @Override
    protected void runExtend() {

        calculatingClients.clear();

        logger.logEvent(COMPONENT, "start local extend phase", "low");

        buildAllClients();

        Network.getInstance().establishConnectionsWithInTopologyTwo();

        this.remoteExtendClient = new RemoteExtendClient(getDevice(), this);

        int total = getNumberOfTotalSecrets();

        int newIdentifier = total + 1;

        setNewIdentifier(newIdentifier);

        logger.logEvent(COMPONENT, "newIdentifier", "low", String.valueOf(newIdentifier));

        int threshold = getThreshold();

        logger.logEvent(COMPONENT, "threshold", "low", String.valueOf(threshold));

        List<String> remotes = getAllDevices();



        List<String> rem = new ArrayList<>();

        int[] weights = new int[remotes.size()+1];

        int index = 0;


        int localWeight = getWeight();
        int alreadyWeight = localWeight;
        for (ExtendingClient remote : getClients()){
            if (remote.isLocal()){
                logger.logEvent(COMPONENT, "local weight", "low", String.valueOf(localWeight));
                calculatingClients.add(remote);
                remote.calculate(localWeight);
                rem.add("here");
                weights[index] = localWeight;
            } else {
                String device = remote.getDevice();
                int weight = getNumberOfSecretsFor(device);
                logger.logEvent(COMPONENT, "remote ", "low", device);
                logger.logEvent(COMPONENT, "remote weight", "low", String.valueOf(weight));
                if (weight + alreadyWeight <= threshold){
                    logger.logEvent(COMPONENT, "add ", "low", device);
                    remote.calculate(weight);
                    rem.add(device);
                    calculatingClients.add(remote);
                    weights[index] = weight;
                } else {
                    int weight1 = threshold - alreadyWeight;
                    if (weight1 > 0 ){
                        logger.logEvent(COMPONENT, "add ", "low", device);
                        logger.logEvent(COMPONENT, "add ", "low", String.valueOf(weight1));
                        calculatingClients.add(remote);
                        remote.calculate(weight1);
                        rem.add(device);
                        weights[index] = weight1;
                    } else {
                        logger.logEvent(COMPONENT, "wait ", "low", device);
                        remote.waitTillCalculated();
                    }
                }
            }
            index += 1;
        }

        logger.logEvent(COMPONENT, "go extend client ", "low");
        remoteExtendClient.go(remotes, newIdentifier, getApplicationName(), rem);

        logger.logEvent(COMPONENT, "go extending clients ", "low");
        for (ExtendingClient client : getClients()){
            client.go(rem, newIdentifier,  getDevice(), weights);
        }
    }

    @Override
    protected List<String>  getDeviceList() {
        return getRemoteList();
    }

    @Override
    protected RemoteExtendingClient buildRemoteClient(String device) {
        return new RemoteExtendingClient(this, device);
    }

    @Override
    protected ArrayList<ExtendingClient> getCalculatingClients() {
        return calculatingClients;
    }

    @Override
    public void persisted() {
        logger.logEvent(COMPONENT, "persisted", "low");
        for(ExtendingClient client : getClients()){
            client.persist();
        }
        logger.logEvent(COMPONENT, "confirm", "low");
        confirm();
        remoteExtendClient.ok();
        getPresenter().onFinished();

    }

    @Override
    public void submitMessage(BigNumber message) {
        logger.logEvent(COMPONENT, "submit message ", "low");
        remoteExtendClient.send(message, getWeight() );
    }

    @Override
    public String getMain() {
        return "here";
    }
}
