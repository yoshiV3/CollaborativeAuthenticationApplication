package com.project.collaborativeauthenticationapplication.service.general;

public class CustomIdentifiedParticipant implements IdentifiedParticipant{




    private  final int identifier;


    private final  Participant unIdentifiedParticipant;
    public CustomIdentifiedParticipant(Participant participant, int identifier)
    {
        this.identifier         = identifier;
        unIdentifiedParticipant = participant;
    }
    @Override
    public int getIdentifier() {
        return identifier;
    } //returns the first index

    @Override
    public String getName() {
        return unIdentifiedParticipant.getName();
    }

    @Override
    public String getAddress() {
        return unIdentifiedParticipant.getAddress();
    }

    @Override
    public void setWeight(int weight) {
        unIdentifiedParticipant.setWeight(weight);
    }

    @Override
    public int getWeight() {
        return unIdentifiedParticipant.getWeight();
    }

    @Override
    public boolean isLocal() {
        return unIdentifiedParticipant.isLocal();
    }
}
