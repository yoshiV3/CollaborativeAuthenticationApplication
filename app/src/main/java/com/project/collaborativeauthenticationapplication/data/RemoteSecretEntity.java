package com.project.collaborativeauthenticationapplication.data;


import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

import static androidx.room.ForeignKey.CASCADE;

@Entity(foreignKeys = {
        @ForeignKey(
                entity        =  ApplicationLoginEntity.class,
                parentColumns =  "applicationLoginId",
                childColumns  =  "applicationLoginId",
                onDelete      =   CASCADE
        ),
        @ForeignKey(
                entity        = ParticipantEntity.class,
                parentColumns = "address",
                childColumns  = "participantId",
                onDelete      =   CASCADE
        )
})
public class RemoteSecretEntity {

    @PrimaryKey(autoGenerate = true)
    public long remoteSecretId;

    @NonNull
    public long applicationLoginId;
    @NonNull
    public String participantId;

    public int identifier;

    public RemoteSecretEntity(long applicationLoginId, String participantId, int identifier){
        this.applicationLoginId = applicationLoginId;
        this.participantId      = participantId;
        this.identifier         = identifier;
    }
}
