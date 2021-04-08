package com.project.collaborativeauthenticationapplication.data;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class ApplicationLoginEntity {
    @PrimaryKey(autoGenerate = true)
    public long          applicationLoginId;
    public final String  applicationName;
    public final String  login;
    public final int     threshold;
    public final String  publicKeyX;
    public final String  publicKeyY;
    public final Boolean publicKeyIsZero;


    public ApplicationLoginEntity(String applicationName, String login, int threshold, String publicKeyX, String publicKeyY, Boolean publicKeyIsZero){

        this.applicationName = applicationName;
        this.login           = login;
        this.threshold       = threshold;
        this.publicKeyX      = publicKeyX;
        this.publicKeyY      = publicKeyY;
        this.publicKeyIsZero = publicKeyIsZero;
    }
}
