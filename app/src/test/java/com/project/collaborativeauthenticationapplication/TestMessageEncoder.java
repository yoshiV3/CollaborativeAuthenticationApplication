package com.project.collaborativeauthenticationapplication;

import com.project.collaborativeauthenticationapplication.service.crypto.BigNumber;
import com.project.collaborativeauthenticationapplication.service.crypto.Point;
import com.project.collaborativeauthenticationapplication.service.general.IdentifiedParticipant;
import com.project.collaborativeauthenticationapplication.service.key.application.key_generation.local_system.control.protocol.KeyGenerationSession;
import com.project.collaborativeauthenticationapplication.service.network.messages.AbstractMessage;
import com.project.collaborativeauthenticationapplication.service.network.messages.InvitationMessage;
import com.project.collaborativeauthenticationapplication.service.network.messages.MessageEncoder;
import com.project.collaborativeauthenticationapplication.service.network.messages.MessageParser;
import com.project.collaborativeauthenticationapplication.service.network.messages.PartsMessage;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class TestMessageEncoder {


    private  class TestParticipant implements IdentifiedParticipant {


        private final int identifier;
        private final String address;
        private final int weight;
        private final boolean isLocal;

        public TestParticipant(int identifier, String address, int weight, boolean isLocal){
            this.identifier = identifier;
            this.address = address;
            this.weight = weight;
            this.isLocal = isLocal;
        }

        @Override
        public int getIdentifier() {
            return identifier;
        }

        @Override
        public String getName() {
            return null;
        }

        @Override
        public String getAddress() {
            return address;
        }

        @Override
        public void setWeight(int weight) {
        }

        @Override
        public int getWeight() {
            return weight;
        }

        @Override
        public boolean isLocal() {
            return isLocal;
        }
    }


    private class TestKeyGenerationSession implements KeyGenerationSession {

        private int    threshold;
        private String applicationName;
        private String login
                ;
        private IdentifiedParticipant local;

        private ArrayList<IdentifiedParticipant> remotes = new ArrayList<>();


        public void setApplicationName(String applicationName) {
            this.applicationName = applicationName;
        }

        public void setLogin(String login) {
            this.login = login;
        }

        public void setThreshold(int threshold) {
            this.threshold = threshold;
        }

        public void setLocal(IdentifiedParticipant local) {
            this.local = local;
        }

        public void addRemote(IdentifiedParticipant participant){
            remotes.add(participant);
        }

        @Override
        public IdentifiedParticipant getLocalParticipant() {
            return local;
        }

        @Override
        public int getThreshold() {
            return threshold;
        }

        @Override
        public String getApplicationName() {
            return applicationName;
        }

        @Override
        public String getLogin() {
            return login;
        }

        @Override
        public List<IdentifiedParticipant> getRemoteParticipantList() {
            return remotes;
        }
    }

    private MessageEncoder encoder = new MessageEncoder();
    private MessageParser parser   =  new MessageParser();

    @Before
    public void clear(){
        encoder.clear();
    }

    @Test
    public void testStringLessThan256(){
        String value = "I am Yoshi";
        encoder.addString(value);
        byte[] resultMessage = encoder.build();
        Assert.assertEquals(resultMessage[0], MessageEncoder.MAIN_STRING_TYPE);
        int length = (int) resultMessage[1];
        if (length < 0){
            length = length + 256;
        }
        length = length + 1;
        Assert.assertEquals(value.getBytes(StandardCharsets.ISO_8859_1).length, length);
        String result = new String(Arrays.copyOfRange(resultMessage, 2, resultMessage.length), StandardCharsets.ISO_8859_1);
        Assert.assertEquals(result, value);
    }

    @Test
    public void testStringMoreThan256Multiple(){
        String value = "";
        for (int i = 0; i <512; i++){
            value = value + "a";
        }
        encoder.addString(value);
        byte[] resultMessage = encoder.build();
        Assert.assertEquals(516, resultMessage.length);
        Assert.assertEquals(resultMessage[0], MessageEncoder.MAIN_STRING_TYPE);
        int length = (int) resultMessage[1];
        if (length < 0){
            length = length + 256;
        }
        length = length + 1;
        Assert.assertEquals(256, length);
        Assert.assertEquals(resultMessage[258], MessageEncoder.CON_STRING_TYPE);
        length = (int) resultMessage[259];
        if (length < 0){
            length = length + 256;
        }
        length = length + 1;
        Assert.assertEquals(256, length);
        String result = new String(Arrays.copyOfRange(resultMessage, 2, 258), StandardCharsets.ISO_8859_1);
        Assert.assertEquals(256, length);
        result = result + new String(Arrays.copyOfRange(resultMessage, 260, 516), StandardCharsets.ISO_8859_1);
        Assert.assertEquals(result, value);
    }
    @Test
    public void testStringMoreThan256NoMultiple(){
        String value = "";
        for (int i = 0; i <534; i++){
            value = value + "a";
        }
        encoder.addString(value);
        byte[] resultMessage = encoder.build();
        Assert.assertEquals(534+3*2, resultMessage.length);

        Assert.assertEquals(resultMessage[0], MessageEncoder.MAIN_STRING_TYPE);
        int length = (int) resultMessage[1];
        if (length < 0){
            length = length + 256;
        }
        length = length + 1;
        Assert.assertEquals(256, length);
        String result = new String(Arrays.copyOfRange(resultMessage, 2, 258), StandardCharsets.ISO_8859_1);

        Assert.assertEquals(resultMessage[258], MessageEncoder.CON_STRING_TYPE);
        length = (int) resultMessage[259];
        if (length < 0){
            length = length + 256;
        }
        length = length + 1;
        Assert.assertEquals(256, length);
        result = result + new String(Arrays.copyOfRange(resultMessage, 260, 516), StandardCharsets.ISO_8859_1);

        Assert.assertEquals(resultMessage[516], MessageEncoder.CON_STRING_TYPE);
        length = (int) resultMessage[517];
        if (length < 0){
            length = length + 256;
        }
        length = length + 1;
        Assert.assertEquals(22, length);
        result = result + new String(Arrays.copyOfRange(resultMessage, 518, 540), StandardCharsets.ISO_8859_1);


        Assert.assertEquals(result, value);
    }


    @Test
    public void testEncodeAndParseInvitation(){
        HashMap<Integer, Boolean> table =  new HashMap<>();

        final String address           = "aaa";
        final int    weight            = 2;
        final int    nbOfPart          = 5;
        final int    totalWeight       = (nbOfPart)*weight;
        final int    threshold         = 3;
        final String applicationName   = "application";
        final String login             = "log";
        final int     recipient        = 3;
        TestKeyGenerationSession session = new TestKeyGenerationSession();
        session.setApplicationName(applicationName);
        session.setLogin(login);
        session.setThreshold(threshold);
        TestParticipant participant = new TestParticipant(1, address, weight, true);
        session.setLocal(participant);
        for (int i = 3; i <= totalWeight; i = i + weight){
            session.addRemote(new TestParticipant(i, address, weight, false));
        }
        Assert.assertEquals(session.getRemoteParticipantList().size(), nbOfPart-1);

        byte[] message = encoder.makeInvitationMessage(session, recipient);
        InvitationMessage result = (InvitationMessage) parser.parse(message);

        Assert.assertEquals(result.getApplicationName(), applicationName);
        Assert.assertEquals(result.getLogin(), login);
        Assert.assertEquals(result.getThreshold(), threshold);

        List<IdentifiedParticipant> participants = result.getParticipants();
        Assert.assertEquals(result.getNumberOfParticipants(), nbOfPart);
        Assert.assertEquals(participants.size(), nbOfPart-1);

        int[] arr = new int[nbOfPart-1];

        for (int i = 0; i < nbOfPart-1; i++){
            arr[i] = participants.get(i).getIdentifier();
        }
        int[] exp = {3,5,7,9};
        Assert.assertArrayEquals(exp, arr);

        for (IdentifiedParticipant part : participants){
            Assert.assertTrue(2 < part.getIdentifier());
            Assert.assertTrue(part.getIdentifier() < totalWeight);
            Assert.assertNotEquals(table.getOrDefault(new Integer(part.getIdentifier()), new Boolean(false)), new Boolean(true));
            table.put(new Integer(part.getIdentifier()), new Boolean(true));
            Assert.assertEquals(part.getWeight(), weight);
            if (part.getIdentifier() == recipient){
                Assert.assertEquals(part.getAddress(), "here");
            } else {
                Assert.assertEquals(part.getAddress(), address);
            }

        }
    }


    @Test
    public void testMakePartsMessage(){
        final int NUMBER = 4;
        ArrayList<BigNumber> list = new ArrayList<>();
        for (int i = 0; i < NUMBER; i++){
            list.add(BigNumber.getZero());
        }
        Point point = new Point(BigNumber.getZero(), BigNumber.getN(), true);
        byte[] messsage = encoder.makePartsMessage(list, point);
        AbstractMessage response = parser.parse(messsage);
        Assert.assertTrue(response instanceof PartsMessage);
        PartsMessage p = (PartsMessage) response;
        ArrayList<BigNumber> result = p.getParts();
        Assert.assertEquals(result.size(), NUMBER);
        for (BigNumber number : result){
            byte[] representation = number.getBigNumberAsByteArray();
            for (byte b: representation){
                Assert.assertEquals(b, 0);
            }
        }
        byte[] x = p.getPublicKey().getX().getBigNumberAsByteArray();
        byte[] y = p.getPublicKey().getY().getBigNumberAsByteArray();
        for(int i =0; i <32; i++){
            Assert.assertEquals(x[i], 0);
            Assert.assertEquals(y[i], BigNumber.getN().getBigNumberAsByteArray()[i]);
        }
        Assert.assertTrue(p.getPublicKey().isZero());
    }
}
