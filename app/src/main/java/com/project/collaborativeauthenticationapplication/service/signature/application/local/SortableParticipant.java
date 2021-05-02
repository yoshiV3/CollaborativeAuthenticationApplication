package com.project.collaborativeauthenticationapplication.service.signature.application.local;

import java.util.Arrays;

public class SortableParticipant implements Comparable<SortableParticipant> {

    private int[] identifiers;

    private String address;

    private boolean isLocal;

    public SortableParticipant(String address, int[] identifiers, boolean isLocal){
        this.address     = address;
        this.isLocal = isLocal;
        Arrays.sort(identifiers);
        this.identifiers = identifiers;
    }


    public boolean isLocal() {
        return isLocal;
    }

    public int getLowestIdentifier(){
        return identifiers[0];
    }

    public int[] getIdentifiers() {
        return identifiers;
    }

    public String getAddress() {
        return address;
    }

    public int[] getSubsetOfIdentifiers(int size){
        int[] result = new int[size];
        System.arraycopy(identifiers, 0, result, 0, size);
        return  result;
    }

    @Override
    public int compareTo(SortableParticipant o) {
        int lowestThis  = getLowestIdentifier();
        int lowestOther = o.getLowestIdentifier();
        if (lowestOther > lowestThis){
            return -1;
        } else if (lowestThis > lowestOther){
            return 1;
        }
        return 0;
    }
}
