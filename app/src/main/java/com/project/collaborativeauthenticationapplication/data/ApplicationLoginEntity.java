package com.project.collaborativeauthenticationapplication.data;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class ApplicationLoginEntity {
    @PrimaryKey(autoGenerate = true)
    public long   applicationLoginId;
    public final String applicationName;
    public final String login;
    public final int    threshold;


    public ApplicationLoginEntity(String applicationName, String login, int threshold){

        this.applicationName = applicationName;
        this.login           = login;
        this.threshold       = threshold;
    }
}
