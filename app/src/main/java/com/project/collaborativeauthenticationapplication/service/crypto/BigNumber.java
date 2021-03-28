package com.project.collaborativeauthenticationapplication.service.crypto;

import androidx.annotation.NonNull;

import java.util.Arrays;

public class BigNumber {




    private final byte[]  partOne;
    private final byte[]   partTwo;
    private final byte[]   partThree;
    private final byte[]   partFour;
    private final byte[]   partFive;
    private final byte[]   partSix;
    private final byte[]   partSeven;
    private final byte[]   partEight;

    public BigNumber(byte[]  partOne, byte[]  partTwo, byte[]  partThree, byte[]  partFour, byte[]  partFive, byte[]  partSix, byte[]  partSeven, byte[]  partEight) {
        this.partOne = partOne;
        this.partTwo = partTwo;
        this.partThree = partThree;
        this.partFour = partFour;
        this.partFive = partFive;
        this.partSix = partSix;
        this.partSeven = partSeven;
        this.partEight = partEight;
    }

    public  BigNumber(byte[] fullNumber)
    {
        this(
                Arrays.copyOfRange(fullNumber, 0, 4),
                Arrays.copyOfRange(fullNumber, 4, 8),
                Arrays.copyOfRange(fullNumber, 8, 12),
                Arrays.copyOfRange(fullNumber, 12, 16),
                Arrays.copyOfRange(fullNumber, 16, 20),
                Arrays.copyOfRange(fullNumber, 20, 24),
                Arrays.copyOfRange(fullNumber, 24, 28),
                Arrays.copyOfRange(fullNumber, 28, 32)
        );
        if (fullNumber.length != PolynomialGenerator.NUMBER_INTEGER_SIZE*PolynomialGenerator.INTEGER_BYTE_SIZE)
        {
            throw new IllegalArgumentException("number is not the correct size of 8 4 byte integers");
        }
    }


    public byte[]  getPart(int index)
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


    private String getStringFromPart(int part)
    {
        String result = " ";
        byte[] byteArrPart = getPart(part);
        for (byte element: byteArrPart)
        {
            result = result + String.valueOf(element) + " ";
        }
        return result + ".";
    }

    @NonNull
    @Override
    public String toString() {
        String result = "";
        for (int part = 0; part <8; part++)
        {
            result = result + getStringFromPart(part);
        }
        return result;
    }

    public static BigNumber getZero()
    {
        byte[] zero = {0,0,0,0};
        return new BigNumber(zero.clone(),zero.clone(),zero.clone(),zero.clone(),zero.clone(),zero.clone(),zero.clone(),zero.clone());
    }



}
