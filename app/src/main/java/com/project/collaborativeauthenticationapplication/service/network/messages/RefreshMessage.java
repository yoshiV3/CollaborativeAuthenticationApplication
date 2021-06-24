package com.project.collaborativeauthenticationapplication.service.network.messages;

public class RefreshMessage extends AbstractMessage {

    private final String remove;

    public RefreshMessage(String remove){
        this.remove = remove;
    }

    public String getRemove() {
        return remove;
    }
}
