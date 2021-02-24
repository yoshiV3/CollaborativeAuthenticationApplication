package com.project.collaborativeauthenticationapplication.service.crypto;

import androidx.annotation.NonNull;

public class BigNumber {

    private final int  partOne;
    private final int  partTwo;
    private final int  partThree;
    private final int  partFour;
    private final int  partFive;
    private final int  partSix;
    private final int  partSeven;
    private final int  partEight;

    public BigNumber(int partOne, int partTwo, int partThree, int partFour, int partFive, int partSix, int partSeven, int partEight) {
        this.partOne = partOne;
        this.partTwo = partTwo;
        this.partThree = partThree;
        this.partFour = partFour;
        this.partFive = partFive;
        this.partSix = partSix;
        this.partSeven = partSeven;
        this.partEight = partEight;
    }


    public int getPart(int index)
    {
        switch (index)
        {
            case 0:
                return partOne;
            case 1:
                return partTwo;
            case 2:
                return partThree;
            case 3:
                return partFour;
            case 4:
                return partFive;
            case 5:
                return partSix;
            case 6:
                return partSeven;
            default:
                return partEight;
        }
    }

    @NonNull
    @Override
    public String toString() {
        return String.valueOf(partOne) + " " + String.valueOf(partTwo) + " " + String.valueOf(partThree) + "  ... " + String.valueOf(partEight);
    }

    public static BigNumber getZero()
    {
        return new BigNumber(0,0,0,0,0,0,0,0);
    }

}
