package com.project.collaborativeauthenticationapplication.service.crypto;

import com.project.collaborativeauthenticationapplication.service.IllegalUseOfClosedTokenException;
import com.project.collaborativeauthenticationapplication.service.Token;
import com.project.collaborativeauthenticationapplication.service.key.KeyToken;
import com.project.collaborativeauthenticationapplication.service.key.application.CustomTokenConsumer;

import java.security.SecureRandom;
import java.util.ArrayList;

public class PolynomialGenerator  {


    public static final int INTEGER_BYTE_SIZE   = 4;
    public static final int NUMBER_INTEGER_SIZE = 8; // is fixed if adapt: many consequences (c code, algorithm)

    private ArrayList<BigNumber> polynomial;

    private SecureRandom secureRandom = new SecureRandom();

    private byte[] getNewNumberAsBytes()
    {
        byte[] number = new byte[ NUMBER_INTEGER_SIZE*INTEGER_BYTE_SIZE];
        secureRandom.nextBytes(number);
        return number;
    }

    private BigNumber getNewNumberAsBigNumber()
    {
        byte[] numberAsBytes = getNewNumberAsBytes();
        return new BigNumber(numberAsBytes);
    }

    public ArrayList<BigNumber>  generatePoly(int degree) {
        ArrayList<BigNumber> poly = new ArrayList<>();
        for (int exp = 0; exp <= degree; exp++)
        {
            poly.add(getNewNumberAsBigNumber());
        }
        polynomial=  poly;
        return poly;
    }

    public void passPolynomialToKeyPartGenerator(CryptoKeyPartGenerator generator)
    {
        generator.receivePolynomial(polynomial);
    }
}
