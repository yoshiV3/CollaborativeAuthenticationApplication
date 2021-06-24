package com.project.collaborativeauthenticationapplication.service.network.messages;

import com.project.collaborativeauthenticationapplication.logger.AndroidLogger;
import com.project.collaborativeauthenticationapplication.logger.Logger;
import com.project.collaborativeauthenticationapplication.service.crypto.BigNumber;
import com.project.collaborativeauthenticationapplication.service.crypto.Point;
import com.project.collaborativeauthenticationapplication.service.general.IdentifiedParticipant;


import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;


public class MessageParser {



    private int index = 0;


    private static final String COMPONENT_NAME    = "Parser";
    private static final Logger logger            = new AndroidLogger();


    public static final byte INVITATION_MESSAGE_CODE = 1;
    public static final byte ABORT_MESSAGE_CODE      = 2;
    public static final byte CONN_MESSAGE_CODE       = 3;
    public static final byte PARTS_MESSAGE_CODE      = 4;
    public static final byte COMMIT_MESSAGE_CODE     = 5;
    public static final byte START_SIGN_CODE         = 6;
    public static final byte SIGN_COMMIT_CODE        = 7;
    public static final byte SIGN_PUBLISH_CODE       = 8;
    public static final byte SIGN_SIGNATURE_CODE     = 9;
    public static final byte REFRESH_CODE            = 10;
    public static final byte REFRESH_SHARES_CODE     = 11;
    public static final byte EXTEND_NEW_CODE         = 12;
    public static final byte EXTEND_CALCULATE_CODE   = 13;
    public static final byte EXTEND_SLICE_CODE       = 14;
    public static final byte EXTEND_MESSAGE_CODE     = 15;


    public AbstractMessage parse(byte[] message){
        logger.logEvent(COMPONENT_NAME, "received message", "low", String.valueOf(message.length));
        byte type = message[1];
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
            case REFRESH_CODE:
                return parseRefreshCode(message);
            case REFRESH_SHARES_CODE:
                return parseRefreshShares(message);
            case EXTEND_NEW_CODE:
                return parseNewExtend(message);
            case EXTEND_CALCULATE_CODE:
                return parseExtendCalculateMessage(message);
            case EXTEND_SLICE_CODE:
                return parseExtendSliceMessage(message);
            case EXTEND_MESSAGE_CODE:
                return parseExtendMessageMessage(message);
            default:
                return null;
        }
    }

    private AbstractMessage parseExtendMessageMessage(byte[] message) {
        index = 2;
        final BigNumber mes = parseBigNumber(message);
        final int weight    = parseNonZeroIntegerParameter(message);
        return new ExtendMessageMessage(weight, mes);
    }

    private AbstractMessage parseExtendSliceMessage(byte[] message) {
        index = 2;
        return new SliceMessage(parseBigNumber(message));
    }

    private AbstractMessage parseExtendCalculateMessage(byte[] message) {
        index = 2;
        final String localAddress = parseString(message);
        final int newIdentifier = parseNonZeroIntegerParameter(message);
        final String address = parseString(message);
        final int weight = parseNonZeroIntegerParameter(message);
        ExtendCalculateMessage extendCalculateMessage = new ExtendCalculateMessage(newIdentifier, address, weight, localAddress);
        index = index + 1;
        final int numberOfRemotes = parseNonZeroIntegerParameter(message);
        int[] weights = new int[numberOfRemotes];
        for (int i = 0; i < numberOfRemotes; i++){
            String remote = parseString(message);
            extendCalculateMessage.addRemote(remote);
            weights[i] = parseNonZeroIntegerParameter(message);
        }
        extendCalculateMessage.setWeights(weights);
        return extendCalculateMessage;
    }

    private AbstractMessage parseNewExtend(byte[] message) {
        index = 2;
        final String applicationName = parseString(message);
        final int threshold = parseNonZeroIntegerParameter(message);
        final int newIdentifier = parseNonZeroIntegerParameter(message);
        final Point publicKey   = parsePoint(message);

        ExtendStartMessage startMessage = new ExtendStartMessage(threshold, publicKey, newIdentifier, applicationName);

        index = index + 1;
        final int numberOfRemotes = parseNonZeroIntegerParameter(message);
        for (int i = 0; i < numberOfRemotes; i++){
            String remote = parseString(message);
            boolean cal   = parseBoolean(message);
            index = index + 1;
            int length = parseNonZeroIntegerParameter(message);
            int[] identifier = new int[length];
            for (int j = 0; j < length; j++){
                identifier[j] = parseNonZeroIntegerParameter(message);
            }
            startMessage.addToRemote(remote, identifier, cal );
        }
        return startMessage;
    }

    private AbstractMessage parseRefreshShares(byte[] message) {
        index = 2;
        int  weight = parseNonZeroIntegerParameter(message);
        ArrayList<BigNumber> parts = parseBigNumberList(message, weight);
        logger.logEvent(COMPONENT_NAME, "received message", "low", String.valueOf(index));
        index = 0;
        return new RefreshShareMessage(parts);
    }

    private AbstractMessage parseRefreshCode(byte[] message) {
        index = 2;
        String remove = parseString(message);
        return new RefreshMessage(remove);
    }

    private AbstractMessage parseSignatureMessage(byte[] message) {
        index = 2;
        BigNumber sig = parseBigNumber(message);
        logger.logEvent(COMPONENT_NAME, "received message", "low", String.valueOf(index));
        index = 0;
        return new SignatureMessage(sig);
    }

    private AbstractMessage parseSignPublishMessage(byte[] message) {
        index = 2;
        HashMap<String, ArrayList<Point>> commitmentsE = new HashMap<>();
        HashMap<String, ArrayList<Point>> commitmentsD = new HashMap<>();
        BigNumber mes = parseBigNumber(message);
        int length = parseNonZeroIntegerParameter(message);
        for (int i = 0; i < length; i++){
            parseNamedCommitment(message, commitmentsE, commitmentsD);
        }
        logger.logEvent(COMPONENT_NAME, "received message", "low", String.valueOf(index));
        index = 0;
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
        index = 2;
        String address = parseString(message);
        ArrayList<Point> e = parseCommitments(message);
        ArrayList<Point> d = parseCommitments(message);
        logger.logEvent(COMPONENT_NAME, "received message", "low", String.valueOf(index));
        index = 0;
        return new SignCommitmentMessage(e, d, address);
    }

    private AbstractMessage parseStartSignMessage(byte[] message) {
        index = 2;
        String address = parseString(message);
        String name   = parseString(message);
        int   number  = parseNonZeroIntegerParameter(message);
        logger.logEvent(COMPONENT_NAME, "received message", "low", String.valueOf(index));
        index = 0;
        return new StartSignMessage(name, number, address);
    }

    private AbstractMessage parseCommitMessage(byte[] message) {
        index = 2;
        if(parseBoolean(message)){
            return new YesMessage();
        } else {
            return new NoMessage();
        }

    }

    private AbstractMessage parsePartsMessage(byte[] message) {
        index = 2;
        int  weight = parseNonZeroIntegerParameter(message);
        ArrayList<BigNumber> parts = parseBigNumberList(message, weight);
        Point publicKey  = parsePoint(message);
        logger.logEvent(COMPONENT_NAME, "received message", "low", String.valueOf(index));
        index = 0;
        return new PartsMessage(parts, publicKey);
    }

    private AbstractMessage parseConnectMessage(byte[] message) {
        index = 2;
        String applicationName = parseString(message);
        String login           = parseString(message);
        logger.logEvent(COMPONENT_NAME, "received message", "low", String.valueOf(index));
        index = 0;
        return new ConnectMessage(applicationName, login);
    }

    private AbstractMessage parseAbortMessage(byte[] message) {
        index = 2;
        String applicationName = parseString(message);
        String login           = parseString(message);
        logger.logEvent(COMPONENT_NAME, "received message", "low", String.valueOf(index));
        index = 0;
        return new AbortMessage(applicationName, login);

    }

    private AbstractMessage parseInvitationMessage(byte[] message) {
        index = 2;
        String applicationName = parseString(message);
        int    threshold       = parseNonZeroIntegerParameter(message);
        int    totalWeight     = parseNonZeroIntegerParameter(message);
        int    numberOfParticipants = parseNonZeroIntegerParameter(message);
        ArrayList<IdentifiedParticipant> participants = parseParticipants(totalWeight, numberOfParticipants, message);
        logger.logEvent(COMPONENT_NAME, "received message", "low", String.valueOf(index));
        index = 0;
        return new InvitationMessage(applicationName, threshold, totalWeight, numberOfParticipants, participants);
    }

    private int byteToInt(byte b){
        int result = (int) b;
        if (result < 0){
            result = result +256;
        }
        return result;
    }

    private String parseString(byte[] message){
        if (!(message[index] == MessageEncoder.MAIN_STRING_TYPE || message[index] == MessageEncoder.MAIN_STRING_TYPE_NULL)){
            throw new IllegalArgumentException("Malformed message");
        }
        String string;
        if (message[index] == MessageEncoder.MAIN_STRING_TYPE) {
            index += 1;
            int length = byteToInt(message[index])+1;
            index += 1;
            string = new String(Arrays.copyOfRange(message, index, index + length), StandardCharsets.ISO_8859_1);
            index = index + length;
            while (length == 256) {//if previous length equals 256 then full word
                if (message[index] != MessageEncoder.CON_STRING_TYPE) {
                    throw new IllegalArgumentException("Malformed message");
                }
                index += 1;
                length = byteToInt(message[index]) + 1;
                index += 1;
                string = string + new String(Arrays.copyOfRange(message, index, index + length), StandardCharsets.ISO_8859_1);
                index += length;
            }
        } else {
            string = null;
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
