package com.project.collaborativeauthenticationapplication;

import com.project.collaborativeauthenticationapplication.service.crypto.BigNumber;
import com.project.collaborativeauthenticationapplication.service.crypto.CryptoKeyPartGenerator;
import com.project.collaborativeauthenticationapplication.service.crypto.CryptoKeyShareGenerator;
import com.project.collaborativeauthenticationapplication.service.crypto.CryptoPartKeyRecovery;
import com.project.collaborativeauthenticationapplication.service.crypto.CryptoProcessor;
import com.project.collaborativeauthenticationapplication.service.crypto.CryptoThresholdSignatureProcessor;
import com.project.collaborativeauthenticationapplication.service.crypto.CryptoVerificationProcessor;
import com.project.collaborativeauthenticationapplication.service.crypto.Point;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class AndroidCodeCTest {



    private   CryptoKeyPartGenerator partGenerator;
    private   CryptoKeyShareGenerator shareGenerator;
    private   CryptoProcessor processor;
    private   CryptoPartKeyRecovery recovery;
    private   CryptoThresholdSignatureProcessor signatureProcessor;
    private   CryptoVerificationProcessor verificationProcessor;


    @Before
    public void setup(){
            partGenerator            = new CryptoKeyPartGenerator();
            shareGenerator           = new CryptoKeyShareGenerator();
            processor                = new CryptoProcessor();
            recovery                 = new CryptoPartKeyRecovery();
            signatureProcessor       = new CryptoThresholdSignatureProcessor();
            verificationProcessor    = new CryptoVerificationProcessor();
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


    @Test
    public void testCommitmentToRandomness(){
        ArrayList<BigNumber> e = new ArrayList<>();
        ArrayList<BigNumber> d = new ArrayList<>();

        byte[] one = new byte[32];
        one[0] = 1;
        for (int index = 1; index <32; index++)
        {
            one[index] = 0;
        }
        byte[] two = new byte[32];
        two[0] = 2;
        for (int index = 1; index <32; index++)
        {
            two[index] = 0;
        }

        e.add(new BigNumber(one));
        e.add(new BigNumber(two));

        d.add(new BigNumber(two));
        d.add(new BigNumber(one));

        signatureProcessor.receiveRandomness(e, d);

        signatureProcessor.calculateCommitmentsToRandomness();

        List<Point> E = signatureProcessor.publishCommitmentsE();
        List<Point> D = signatureProcessor.publishCommitmentsD();

        Assert.assertEquals(E.size(), 2);
        Assert.assertEquals(D.size(), 2);

        Assert.assertEquals(E.get(0).getX().getPart(0)[0], -104);
        Assert.assertEquals(D.get(1).getX().getPart(0)[0], -104);

        Assert.assertEquals(E.get(0).getX().getPart(0)[1], 23);
        Assert.assertEquals(D.get(1).getX().getPart(0)[1], 23);


        Assert.assertEquals(E.get(1).getX().getPart(0)[0], -27);
        Assert.assertEquals(D.get(0).getX().getPart(0)[0], -27);
    }



    @Test
    public void testSignatureProcedure(){
        ArrayList<ArrayList<BigNumber>> polynomials = new ArrayList<>();
        ArrayList<BigNumber> poly = new ArrayList<>();
        int degree = 1;

        int identifiers[] = {1, 2};

        byte[] onePoly = new byte[32];
        onePoly[0] = 1;
        for (int index = 1; index <32; index++)
        {
            onePoly[index] = 0;
        }
        BigNumber oneBnN = new BigNumber(onePoly);

        for (int exp =0; exp <degree; exp++)
        {
            poly.add(BigNumber.getZero());
        }
        poly.add(oneBnN);
        polynomials.add(poly); // one polynomial of degree 4 so we need at least 5 shares to successfully sign

        ArrayList<BigNumber> result_secrets = new ArrayList<>();
        Point result_public = new Point(BigNumber.getZero(), BigNumber.getZero(), true);
        processor.generateParts(degree+1, polynomials, result_secrets, result_public);

        BigNumber message = new BigNumber(onePoly);

        ArrayList<BigNumber> e = new ArrayList<>();
        ArrayList<BigNumber> d = new ArrayList<>();

        byte[] one = new byte[32];
        one[0] = 1;
        for (int index = 1; index <32; index++)
        {
            one[index] = 0;
        }
        byte[] two = new byte[32];
        two[0] = 2;
        for (int index = 1; index <32; index++)
        {
            two[index] = 0;
        }


        for (int i = 0; i < degree +1; i++){
            e.add(new BigNumber(one));
            d.add(new BigNumber(two));
        }

        Assert.assertEquals(result_secrets.size(), degree+1);

        Assert.assertEquals(result_secrets.get(0).getPart(0)[0], 1);
        Assert.assertEquals(result_secrets.get(0).getPart(0)[1], 0);
        Assert.assertEquals(result_secrets.get(1).getPart(0)[0], 1);
        Assert.assertEquals(result_secrets.get(1).getPart(0)[1], 0);



        signatureProcessor.receiveRandomness(e, d);

        signatureProcessor.calculateCommitmentsToRandomness();


        List<Point> commE = signatureProcessor.publishCommitmentsE();
        List<Point> commD = signatureProcessor.publishCommitmentsD();

        Assert.assertEquals(commE.size(), degree+1);
        Assert.assertEquals(commD.size(), degree+1);
        Assert.assertEquals(commE.get(0).getX().getPart(0)[0], -104);
        Assert.assertEquals(commE.get(1).getX().getPart(0)[0], -104);
        Assert.assertEquals(commD.get(0).getX().getPart(0)[0], -27);
        Assert.assertEquals(commD.get(1).getX().getPart(0)[0], -27);

        for(int i = 0; i < degree+1; i++){
                for (int j = 0;  j <32; j++) {
                    Assert.assertEquals(commE.get(i).getX().getBigNumberAsByteArray()[j], commE.get(0).getX().getBigNumberAsByteArray()[j]);
                }
        }
        signatureProcessor.receiveAllRandomness(commE, commD);

        signatureProcessor.receiveShares(result_secrets);

        signatureProcessor.produceSignatureShare(message, identifiers);


        List<BigNumber> signatureShares = signatureProcessor.publishSignatureShare();

        Assert.assertEquals(signatureShares.size(), 2);

        //Assert.assertEquals(signatureShares.get(1).getBigNumberAsByteArray()[0], 20);
        //Assert.assertEquals(signatureShares.get(1).getBigNumberAsByteArray()[1], 76);
        Assert.assertNotEquals(signatureShares.get(1).getPart(0)[0], 0);


        boolean result = verificationProcessor.verify( signatureShares, message, result_public);

        Assert.assertTrue(result);
    }

}
