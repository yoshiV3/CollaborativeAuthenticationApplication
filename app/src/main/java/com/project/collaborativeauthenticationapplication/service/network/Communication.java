package com.project.collaborativeauthenticationapplication.service.network;

import com.project.collaborativeauthenticationapplication.service.general.FeedbackRequester;
import com.project.collaborativeauthenticationapplication.service.general.Participant;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;

public interface Communication extends BluetoothMonitor {


    ArrayList<Participant> getReachableParticipants();

    String getLocalAddress();

    void  openServiceServer(FeedbackRequester requester);

    void handleIncomingRequests();

    void closeServiceServer();

    AndroidBiDirectionalCommunicationConnection getConnectionWith(String address) throws IOException;

    void handleBrokenConnection(AndroidBiDirectionalCommunicationConnection connection);

    void closeConnection(AndroidBiDirectionalCommunicationConnection connection);

    void closeAllConnections();

    void registerLocalAddress(String address);


    void establishConnectionsWith(Set<String> addresses, ConnectionRequester requester);


}
