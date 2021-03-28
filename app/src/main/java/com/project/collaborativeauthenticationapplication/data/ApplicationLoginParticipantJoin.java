package com.project.collaborativeauthenticationapplication.data;


import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;

import static androidx.room.ForeignKey.CASCADE;


@Entity(primaryKeys = {"applicationLoginId",  "participantId"},
        foreignKeys =  {
            @ForeignKey(entity        =  ApplicationLoginEntity.class,
                        parentColumns =  "applicationLoginId",
                        childColumns  =  "applicationLoginId",
                        onDelete      =   CASCADE
                        ),
                @ForeignKey(entity        = ParticipantEntity.class,
                            parentColumns = "address",
                            childColumns  = "participantId",
                            onDelete      =   CASCADE
                )
        }
        )
public class ApplicationLoginParticipantJoin {
    @NonNull
    public long applicationLoginId;
    @NonNull
    public String participantId;

    public int weight;
    public int state;

    public ApplicationLoginParticipantJoin(long applicationLoginId, String participantId, int weight, int state){
        this.applicationLoginId    = applicationLoginId;
        this.participantId         = participantId;
        this.weight                = weight;
        this.state                 = state;
    }
}
