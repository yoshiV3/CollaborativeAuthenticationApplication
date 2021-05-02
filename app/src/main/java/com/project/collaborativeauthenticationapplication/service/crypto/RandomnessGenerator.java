package com.project.collaborativeauthenticationapplication.service.crypto;



import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.ArrayList;

public class RandomnessGenerator {


    public static final int INTEGER_BYTE_SIZE   = 4;
    public static final int NUMBER_INTEGER_SIZE = 8; // is fixed if adapt: many consequences (c code, algorithm)

    //public static final BigInteger N = new BigInteger("FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFEBAAEDCE6AF48A03BBFD25E8CD0364141", 16);

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
        BigNumber current = new BigNumber(numberAsBytes);
        while (BigNumber.getN().compareTo(current) < 1 ){
            numberAsBytes = getNewNumberAsBytes();
            current       = new BigNumber(numberAsBytes);
        }
        return current;
    }

    public BigNumber generateRandomness(){
        return getNewNumberAsBigNumber(); 
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
