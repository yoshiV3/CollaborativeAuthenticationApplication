package com.project.collaborativeauthenticationapplication.data;


import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class ParticipantEntity {

    @PrimaryKey
    @NonNull
    public String address;

    public ParticipantEntity(String address){
        this.address = address;
    }
}
