package com.project.collaborativeauthenticationapplication.service.network.messages;

import com.project.collaborativeauthenticationapplication.service.crypto.BigNumber;
import com.project.collaborativeauthenticationapplication.service.crypto.Point;
import com.project.collaborativeauthenticationapplication.service.general.IdentifiedParticipant;


import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;


public class MessageParser {

    private int index = 0;


    public static final byte INVITATION_MESSAGE_CODE = 1;
    public static final byte ABORT_MESSAGE_CODE      = 2;
    public static final byte CONN_MESSAGE_CODE       = 3;
    public static final byte PARTS_MESSAGE_CODE      = 4;
    public static final byte COMMIT_MESSAGE_CODE     = 5;
    public static final byte START_SIGN_CODE         = 6;
    public static final byte SIGN_COMMIT_CODE        = 7;
    public static final byte SIGN_PUBLISH_CODE       = 8;
    public static final byte SIGN_SIGNATURE_CODE     = 9;


    public AbstractMessage parse(byte[] message){
        byte type = message[0];
        switch (type){
            case INVITATION_MESSAGE_CODE:
                return parseInvitationMessage(message);
            case ABORT_MESSAGE_CODE:
                return parseAbortMessage(message);
            case CONN_MESSAGE_CODE:
                return parseConnectMessage(message);
            case PARTS_MESSAGE_CODE:
                return parsePartsMessage(message);
            case COMMIT_MESSAGE_CODE:
                return parseCommitMessage(message);
            case START_SIGN_CODE:
                return parseStartSignMessage(message);
            case SIGN_COMMIT_CODE:
                return parseSignCommitMessage(message);
            case SIGN_PUBLISH_CODE:
                return parseSignPublishMessage(message);
            case SIGN_SIGNATURE_CODE:
                return parseSignatureMessage(message);
            default:
                return null;
        }
    }

    private AbstractMessage parseSignatureMessage(byte[] message) {
        index = 1;
        BigNumber sig = parseBigNumber(message);
        return new SignatureMessage(sig);
    }

    private AbstractMessage parseSignPublishMessage(byte[] message) {
        index = 1;
        HashMap<String, ArrayList<Point>> commitmentsE = new HashMap<>();
        HashMap<String, ArrayList<Point>> commitmentsD = new HashMap<>();
        BigNumber mes = parseBigNumber(message);
        int length = parseNonZeroIntegerParameter(message);
        for (int i = 0; i < length; i++){
            parseNamedCommitment(message, commitmentsE, commitmentsD);
        }
        return new SignPublishMessage(commitmentsE, commitmentsD, mes);
    }

    private void parseNamedCommitment(byte[] message, HashMap<String, ArrayList<Point>> commitmentsE, HashMap<String, ArrayList<Point>> commitmentsD) {
        if (message[index] != MessageEncoder.NAMED_COMMITMENTS){
            throw new IllegalArgumentException("Malformed message");
        }
        index += 1;
        String remote = parseString(message);
        commitmentsE.put(remote, parseCommitments(message));
        commitmentsD.put(remote, parseCommitments(message));
    }

    private AbstractMessage parseSignCommitMessage(byte[] message) {
        index = 1;
        String address = parseString(message);
        ArrayList<Point> e = parseCommitments(message);
        ArrayList<Point> d = parseCommitments(message);
        return new SignCommitmentMessage(e, d, address);
    }

    private AbstractMessage parseStartSignMessage(byte[] message) {
        index = 1;
        String address = parseString(message);
        String name   = parseString(message);
        String login  = parseString(message);
        int   number  = parseNonZeroIntegerParameter(message);
        return new StartSignMessage(name, login, number, address);
    }

    private AbstractMessage parseCommitMessage(byte[] message) {
        index  = 1;
        if(parseBoolean(message)){
            return new YesMessage();
        } else {
            return new NoMessage();
        }

    }

    private AbstractMessage parsePartsMessage(byte[] message) {
        index       = 1;
        int  weight = parseNonZeroIntegerParameter(message);
        ArrayList<BigNumber> parts = parseBigNumberList(message, weight);
        Point publicKey  = parsePoint(message);
        index = 0;
        return new PartsMessage(parts, publicKey);
    }

    private AbstractMessage parseConnectMessage(byte[] message) {
        index = 1;
        String applicationName = parseString(message);
        String login           = parseString(message);
        index = 0;
        return new AbortMessage(applicationName, login);
    }

    private AbstractMessage parseAbortMessage(byte[] message) {
        index = 1;
        String applicationName = parseString(message);
        String login           = parseString(message);
        index = 0;
        return new ConnectMessage(applicationName, login);

    }

    private AbstractMessage parseInvitationMessage(byte[] message) {
        index = 1;
        String applicationName = parseString(message);
        String login           = parseString(message);
        int    threshold       = parseNonZeroIntegerParameter(message);
        int    totalWeight     = parseNonZeroIntegerParameter(message);
        int    numberOfParticipants = parseNonZeroIntegerParameter(message);
        ArrayList<IdentifiedParticipant> participants = parseParticipants(totalWeight, numberOfParticipants, message);
        index = 0;
        return new InvitationMessage(applicationName, login, threshold, totalWeight, numberOfParticipants, participants);
    }

    private int byteToInt(byte b){
        int result = (int) b;
        if (result < 0){
            result = result +256;
        }
        return result;
    }

    private String parseString(byte[] message){
        if (message[index] != MessageEncoder.MAIN_STRING_TYPE){
            throw new IllegalArgumentException("Malformed message");
        }
        index += 1;
        int length = byteToInt(message[index])+1;
        index += 1;
        String string =  new String(Arrays.copyOfRange(message, index, index+length), StandardCharsets.ISO_8859_1);
        index = index + length;
        while (length == 256){//if previous length equals 256 then full word
            if (message[index] != MessageEncoder.CON_STRING_TYPE){
                throw new IllegalArgumentException("Malformed message");
            }
            index +=  1;
            length = byteToInt(message[index])+1;
            index += 1;
            string = string + new String(Arrays.copyOfRange(message, index, index+length), StandardCharsets.ISO_8859_1);
            index += length;
        }
        return string;
    }



    private BigNumber parseBigNumber(byte[] message){
        if (message[index] != MessageEncoder.BIG_NUMBER_TYPE){
            throw new IllegalArgumentException("Malformed message");
        }
        index += 1;
        byte[] value = new byte[32];
        System.arraycopy(message,   index, value, 0, 32);
        index += 32;
        return new BigNumber(value);
    }

    private ArrayList<BigNumber> parseBigNumberList(byte[] message, int length){
        ArrayList<BigNumber> result = new ArrayList<>();
        for(int i = 0; i < length; i++){
            result.add(parseBigNumber(message));
        }
        return result;
    }

    private Point parsePoint(byte[]  message){
        if (message[index] != MessageEncoder.POINT_TYPE){
            throw new IllegalArgumentException("Malformed message");
        }
        index += 1;
        BigNumber x    = parseBigNumber(message);
        BigNumber y    = parseBigNumber(message);
        boolean isZero = parseBoolean(message);
        return new Point(x, y, isZero);
    }

    ArrayList<Point> parseCommitments(byte [] message){
        if (message[index] != MessageEncoder.COMMITMENTS){
            throw new IllegalArgumentException("Malformed message");
        }
        ArrayList<Point> result = new ArrayList<>();
        index += 1;
        int length = parseNonZeroIntegerParameter(message);
        for (int i = 0; i < length; i++){
            result.add(parsePoint(message));
        }
        return result;
    }

    private boolean parseBoolean(byte[] message) {
        boolean result = message[index] != 0;
        index += 1;
        return  result;
    }


    private int parseNonZeroIntegerParameter(byte[] message){
        if (message[index] != MessageEncoder.INT_PARAM_TYPE){
            throw new IllegalArgumentException("Malformed message");
        }
        index += 1;
        int result = (int) message[index];
        if (result < 0){
            result = result + 256;
        }
        result += 1;
        index  += 1;
        return  result;
    }

    private ArrayList<IdentifiedParticipant> parseParticipants(int totalWeight,  int numberOfParticipants, byte[] message){
        ArrayList<IdentifiedParticipant> participants = new ArrayList<>();
        for (int participant = 0; participant<numberOfParticipants-1; participant++){ //first participant is implicit
            int identifier = parseNonZeroIntegerParameter(message);
            if (participant != 0){
                IdentifiedParticipant previous = participants.get(participant-1);
                previous.setWeight(identifier-previous.getIdentifier());
            }
            if (message[index] == 0){
                index += 1;
                IdentifiedParticipant p = new IdentifiedParticipant() {
                    int identifierParticipant = identifier;
                    int weight;
                    @Override
                    public int getIdentifier() {
                        return identifierParticipant;
                    }

                    @Override
                    public String getName() {
                        return null;
                    }

                    @Override
                    public String getAddress() {
                        return "here";
                    }

                    @Override
                    public void setWeight(int weight) {
                        this.weight = weight;
                    }

                    @Override
                    public int getWeight() {
                        return weight;
                    }

                    @Override
                    public boolean isLocal() {
                        return true;
                    }
                };
                participants.add(p);
            } else {
                String address = parseString(message);
                IdentifiedParticipant p = new IdentifiedParticipant() {
                    int identifierParticipant = identifier;
                    int weight;
                    String addressParticipant  = address;
                    @Override
                    public int getIdentifier() {
                        return identifierParticipant;
                    }

                    @Override
                    public String getName() {
                        return null;
                    }

                    @Override
                    public String getAddress() {
                        return addressParticipant;
                    }

                    @Override
                    public void setWeight(int weight) {
                        this.weight = weight;
                    }

                    @Override
                    public int getWeight() {
                        return weight;
                    }

                    @Override
                    public boolean isLocal() {
                        return false;
                    }
                };
                participants.add(p);
            }
        }
        IdentifiedParticipant participant = participants.get(participants.size()-1); // there are only number - 1 participants (remotes only)
        participant.setWeight(totalWeight - participant.getIdentifier() +1);
        return participants;
    }



}
