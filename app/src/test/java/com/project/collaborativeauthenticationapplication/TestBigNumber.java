package com.project.collaborativeauthenticationapplication;

import com.project.collaborativeauthenticationapplication.service.crypto.BigNumber;

import org.junit.Assert;
import org.junit.Test;

import java.nio.charset.StandardCharsets;

public class TestBigNumber {

    @Test
    public void testSmallerSimple(){
        int result = BigNumber.getN().compareTo(BigNumber.getZero());
        Assert.assertTrue(result>0);
    }

    @Test
    public void testLargerSimple(){
        byte number[] = new byte[32];
        for (int i =0; i< 32 ; i++){
            number[i] = -1;
        }
        int result = BigNumber.getN().compareTo(new BigNumber(number));
        Assert.assertTrue(result<0);
    }

    @Test
    public void testEqual(){
            int result = BigNumber.getN().compareTo(BigNumber.getN());
            Assert.assertEquals(result, 0);
    }

    @Test
    public void testLargerHarder(){
        for(int i = 0; i < 17; i++){
            byte number[] = BigNumber.getN().getBigNumberAsByteArray();
            number[i] += 1;
            int result = BigNumber.getN().compareTo(new BigNumber(number));
            Assert.assertEquals(result, -1);;
        }
    }

    @Test
    public void testSmallerHarder(){
        for(int i = 0; i < 32; i++){
            byte number[] = BigNumber.getN().getBigNumberAsByteArray();
            number[i] += -1;
            int result = BigNumber.getN().compareTo(new BigNumber(number));
            Assert.assertEquals(result, 1);;
        }
    }

    @Test
    public void encoding(){
        String encodedVersionN = new String(BigNumber.getN().getBigNumberAsByteArray(), StandardCharsets.ISO_8859_1);
        byte   decode[] = encodedVersionN.getBytes(StandardCharsets.ISO_8859_1);
        for(int i = 0; i <32;i++){
            Assert.assertEquals(decode[i], BigNumber.getN().getBigNumberAsByteArray()[i]);
        }
    }
}
