package com.project.collaborativeauthenticationapplication.data;


import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

import static androidx.room.ForeignKey.CASCADE;


@Entity(foreignKeys = {
                @ForeignKey(
                        entity        =  ApplicationLoginEntity.class,
                        parentColumns =  "applicationLoginId",
                        childColumns  =  "applicationId",
                        onDelete      = CASCADE
                )
            }
        )
public class SecretEntity {
    @PrimaryKey(autoGenerate = true)
    public long SecretId;
    public long  applicationId;

    public SecretEntity(long applicationId){
        this.applicationId = applicationId;
    }
}
