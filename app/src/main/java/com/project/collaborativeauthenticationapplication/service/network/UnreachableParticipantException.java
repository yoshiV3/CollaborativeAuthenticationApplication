package com.project.collaborativeauthenticationapplication.service.network;

import com.project.collaborativeauthenticationapplication.service.Participant;

public class UnreachableParticipantException extends Exception{

    public UnreachableParticipantException(String text, Participant participant)
    {
        super(String.format("Cannot reach %s (%s)", participant.getName(), text));
    }
}
