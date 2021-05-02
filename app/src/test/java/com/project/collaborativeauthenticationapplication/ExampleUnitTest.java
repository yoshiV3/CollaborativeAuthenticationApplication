package com.project.collaborativeauthenticationapplication;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() {
        assertEquals(4, 2 + 2);
    }

    @Test
    public void test_byte_conversion() {
        for (int value = 0; value < 256; value++){
            int temp  = value;
            if (temp > 127){
                temp  = temp - 256;
            }
            byte tempB = (byte) temp;

            int tempI = (int) tempB;
            if (tempI < 0){
                tempI += 256;
            }
            assertEquals(tempI, value);
        }
    }
}