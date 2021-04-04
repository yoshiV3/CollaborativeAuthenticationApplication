package com.project.collaborativeauthenticationapplication;

import com.project.collaborativeauthenticationapplication.service.crypto.BigNumber;
import com.project.collaborativeauthenticationapplication.service.crypto.CryptoKeyPartGenerator;
import com.project.collaborativeauthenticationapplication.service.crypto.CryptoKeyShareGenerator;
import com.project.collaborativeauthenticationapplication.service.crypto.CryptoPartKeyRecovery;
import com.project.collaborativeauthenticationapplication.service.crypto.CryptoProcessor;
import com.project.collaborativeauthenticationapplication.service.crypto.Point;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;

public class AndroidCodeCTest {



    private   CryptoKeyPartGenerator partGenerator;
    private   CryptoKeyShareGenerator shareGenerator;
    private   CryptoProcessor processor;
    private   CryptoPartKeyRecovery recovery;


    @Before
    public void setup(){
            partGenerator            = new CryptoKeyPartGenerator();
            shareGenerator           = new CryptoKeyShareGenerator();
            processor                = new CryptoProcessor();
            recovery                 = new CryptoPartKeyRecovery();
    }
    @Test
    public void testPartGenerator(){
        ArrayList<BigNumber> poly = new ArrayList<>();
        int degree = 4;

        byte[] one = new byte[32];
        one[0] = 1;
        for (int index = 1; index <32; index++)
        {
            one[index] = 0;
        }
        BigNumber oneBnN = new BigNumber(one);

        for (int exp =0; exp <degree; exp++)
        {
            poly.add(BigNumber.getZero());
        }

        poly.add(oneBnN);
        ArrayList<BigNumber> result = partGenerator.generate(poly, 1,1);
        Assert.assertEquals(result.size(), 1 );
        Assert.assertEquals(result.get(0).getPart(0)[0], 1 );
        Assert.assertEquals(result.get(0).getPart(0)[2], 0 );
    }


    @Test
    public void testPartGeneratorHighDegree(){
        ArrayList<BigNumber> poly = new ArrayList<>();
        int degree = 4;
        byte[] one = new byte[32];
        one[0] = 2;
        for (int index = 1; index <32; index++)
        {
            one[index] = 0;
        }
        BigNumber oneBnN = new BigNumber(one);
        for (int exp =0; exp <degree; exp++)
        {
            poly.add(BigNumber.getZero());
        }
        poly.add(3,oneBnN);
        ArrayList<BigNumber> result = partGenerator.generate(poly, 1,1);

        Assert.assertEquals(result.size(), 1 );
        Assert.assertEquals(result.get(0).getPart(0)[0], 2);
        Assert.assertEquals(result.get(0).getPart(0)[1], 0);
    }

    @Test
    public void testPartGeneratorSecondTermOnly(){
        ArrayList<BigNumber> poly = new ArrayList<>();
        int degree = 4;
        byte[] one = new byte[32];
        for (int index = 0; index <32; index++)
        {
            one[index] = 0;
        }
        one[1] = 1;
        BigNumber oneBnN = new BigNumber(one);
        for (int exp =0; exp <degree; exp++)
        {
            poly.add(BigNumber.getZero());
        }
        poly.add(oneBnN);
        ArrayList<BigNumber> result = partGenerator.generate(poly, 1,1);
        Assert.assertEquals(result.size(), 1 );
        Assert.assertEquals(result.get(0).getPart(0)[1], 1);
        Assert.assertEquals(result.get(0).getPart(0)[0], 0);
    }

    @Test
    public void testPartGeneratorDifferentNumbers(){
        ArrayList<BigNumber> poly = new ArrayList<>();
        int degree = 4;
        byte[] one = new byte[32];
        for (int index = 0; index <32; index++)
        {
            one[index] = 1;
        }
        one[5] = 3;
        BigNumber oneBnN = new BigNumber(one);
        for (int exp =0; exp <degree; exp++)
        {
            poly.add(BigNumber.getZero());
        }
        poly.add(oneBnN);
        ArrayList<BigNumber> result = partGenerator.generate(poly, 1,1);
        Assert.assertEquals(result.size(), 1 );
        Assert.assertEquals(result.get(0).getPart(1)[1], 3);
        Assert.assertEquals(result.get(0).getPart(1)[0], 1);
    }





    @Test
    public void testShareGeneratorTwoShares(){
        ArrayList<ArrayList<BigNumber>> parts  = new ArrayList<>();
        ArrayList<BigNumber>            shares;

        byte[] one = new byte[32];
        for (int index = 0; index <32; index++)
        {
            one[index] = 0;
        }
        one[6] = 1;
        BigNumber oneBnNOne = new BigNumber(one);
        one[0] = 1;
        BigNumber oneBnNTwo = new BigNumber(one);

        ArrayList<BigNumber> first = new ArrayList<>();
        ArrayList<BigNumber> second = new ArrayList<>();

        first.add(oneBnNOne);
        second.add(oneBnNTwo);


        int NB_PARTS = 20;
        for (int i = 0; i < NB_PARTS; i++ )
        {
            first.add(BigNumber.getZero());
            second.add(BigNumber.getZero());
        }
        parts.add(first);
        parts.add(second);
        shares = shareGenerator.generate(parts);

        Assert.assertEquals(shares.size(), 2 );
        Assert.assertEquals(shares.get(0).getPart(1)[2], 1 );
        Assert.assertEquals(shares.get(0).getPart(0)[0], 0 );
        Assert.assertEquals(shares.get(1).getPart(0)[0], 1 );
        Assert.assertEquals(shares.get(1).getPart(1)[2], 1 );
    }

    @Test
    public void testShareGeneratorOneShare(){
        ArrayList<ArrayList<BigNumber>> parts  = new ArrayList<>();
        ArrayList<BigNumber>            shares;

        byte[] one = new byte[32];
        for (int index = 0; index <32; index++)
        {
            one[index] = 0;
        }
        one[0] = 1;

        ArrayList<BigNumber> first = new ArrayList<>();

        first.add(new BigNumber(one));

        int NB_PARTS = 20;
        for (int i = 0; i < NB_PARTS; i++ )
        {
            first.add(BigNumber.getZero());
        }
        parts.add(first);
        shares = shareGenerator.generate(parts);
        Assert.assertEquals(shares.size(), 1);
        Assert.assertEquals(shares.get(0).getPart(0)[0] , 1 );
    }


    @Test
    public void testPublicKeyAndPrivateSecretGenerator(){
        ArrayList<ArrayList<BigNumber>> polynomials = new ArrayList<>();
        ArrayList<BigNumber> poly = new ArrayList<>();
        int degree = 4;

        byte[] one = new byte[32];
        one[0] = 1;
        for (int index = 1; index <32; index++)
        {
            one[index] = 0;
        }
        BigNumber oneBnN = new BigNumber(one);

        for (int exp =0; exp <degree; exp++)
        {
            poly.add(BigNumber.getZero());
        }

        poly.add(oneBnN);
        polynomials.add(poly);

        ArrayList<BigNumber> result_secrets = new ArrayList<>();
        Point result_public = new Point(BigNumber.getZero(), BigNumber.getZero(), true);

        processor.generateParts(6, polynomials, result_secrets, result_public);
        Assert.assertEquals(result_secrets.size(), 6);
        Assert.assertEquals(result_secrets.get(0).getPart(0)[0],1);
        Assert.assertEquals(result_secrets.get(0).getPart(1)[0], 0);
        Assert.assertEquals(result_public.getX().getPart(0)[0], -104);
    }


    @Test
    public void testPublicKeyAndPrivateSecretGeneratorLongerExponent(){
        ArrayList<ArrayList<BigNumber>> polynomials = new ArrayList<>();
        ArrayList<BigNumber> poly = new ArrayList<>();
        int degree = 4;

        byte[] one = new byte[32];
        one[0] = 1;
        for (int index = 1; index <32; index++)
        {
            one[index] = 0;
        }
        BigNumber oneBnN = new BigNumber(one);

        for (int exp =0; exp <degree; exp++)
        {
            poly.add(BigNumber.getZero());
        }

        poly.add(oneBnN);
        polynomials.add(poly);

        poly = new ArrayList<>();

        byte[] oneBis = new byte[32];
        oneBis[0] = 1;
        for (int index = 1; index <32; index++)
        {
            oneBis[index] = 0;
        }
        oneBnN = new BigNumber(oneBis);

        for (int exp =0; exp <degree; exp++)
        {
            poly.add(BigNumber.getZero());
        }

        poly.add(oneBnN);
        polynomials.add(poly);


        ArrayList<BigNumber> result_secrets = new ArrayList<>();
        Point result_public = new Point(BigNumber.getZero(), BigNumber.getZero(), true);

        processor.generateParts(6, polynomials, result_secrets, result_public);
        Assert.assertEquals(result_secrets.size(), 6);
        Assert.assertEquals(result_secrets.get(0).getPart(0)[0], 2);
        Assert.assertEquals(result_secrets.get(0).getPart(1)[0],0);
        Assert.assertEquals(result_public.getX().getPart(0)[0],-27);
    }


    @Test
    public void testKeyRecovery(){
        ArrayList<BigNumber> shares = new ArrayList<>();
        byte[] one = new byte[32];
        one[0] = 1;
        for (int index = 1; index <32; index++)
        {
            one[index] = 0;
        }
        shares.add(new BigNumber(one));

        byte[] two = new byte[32];
        two[0] = 4;
        for (int index = 1; index <32; index++)
        {
            two[index] = 0;
        }
        shares.add(new BigNumber(two));


        byte[] three = new byte[32];
        three[0] = 9;
        for (int index = 1; index <32; index++)
        {
            three[index] = 0;
        }
        shares.add(new BigNumber(three));

        int[] identifiers = {1,2,3};
        BigNumber result = recovery.createLocalSecretSharePartsFromSharesForTarget(shares, identifiers, 4);
        Assert.assertEquals(result.getPart(0)[0], 16);
    }

}
