package com.project.collaborativeauthenticationapplication.service.network.messages;

import com.project.collaborativeauthenticationapplication.service.general.IdentifiedParticipant;

import java.util.ArrayList;

public class InvitationMessage extends AbstractMessage{

    private final String  applicationName;
    private final String login;

    private final int threshold;
    private final int totalWeight;

    private final int numberOfParticipants;

    private final ArrayList<IdentifiedParticipant> participants;

    public InvitationMessage(String applicationName, String login, int threshold, int totalWeight, int numberOfParticipants, ArrayList<IdentifiedParticipant> participants) {
        this.applicationName = applicationName;
        this.login = login;
        this.threshold = threshold;
        this.totalWeight = totalWeight;
        this.numberOfParticipants = numberOfParticipants;
        this.participants = participants;
    }


    public String getApplicationName() {
        return applicationName;
    }

    public String getLogin() {
        return login;
    }

    public ArrayList<IdentifiedParticipant> getParticipants() {
        return participants;
    }

    public int getThreshold() {
        return threshold;
    }

    public int getTotalWeight() {
        return totalWeight;
    }

    public int getNumberOfParticipants() {
        return numberOfParticipants;
    }
}
