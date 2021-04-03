package com.project.collaborativeauthenticationapplication.service.crypto;

import androidx.annotation.NonNull;

import java.util.Arrays;

public class BigNumber {




    private final byte[]  representation = new byte[PolynomialGenerator.NUMBER_INTEGER_SIZE*PolynomialGenerator.INTEGER_BYTE_SIZE];

    public BigNumber(byte[]  partOne, byte[]  partTwo, byte[]  partThree, byte[]  partFour, byte[]  partFive, byte[]  partSix, byte[]  partSeven, byte[]  partEight) {
        System.arraycopy(partOne,   0, representation, 0, 4);
        System.arraycopy(partTwo,   0, representation, 4, 4);
        System.arraycopy(partThree, 0, representation, 8, 4);
        System.arraycopy(partFour,  0, representation, 12,4);
        System.arraycopy(partFive,  0, representation, 16,4);
        System.arraycopy(partSix,   0, representation, 20,4);
        System.arraycopy(partSeven, 0, representation, 24,4);
        System.arraycopy(partEight, 0, representation, 28,4);

    }

    public  BigNumber(byte[] fullNumber)
    {
        int length = PolynomialGenerator.NUMBER_INTEGER_SIZE*PolynomialGenerator.INTEGER_BYTE_SIZE;
        if (fullNumber.length != length )
        {
            throw new IllegalArgumentException("number is not the correct size of 8 4 byte integers");
        }
        System.arraycopy(fullNumber, 0, representation, 0, length);
    }


    public byte[] getBigNumberAsByteArray(){
        return representation.clone();
    }

    public byte[]  getPart(int index)
    {
        byte result[] = new byte[4];
        System.arraycopy(representation, index*4, result, 0, 4);
        return result;
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
