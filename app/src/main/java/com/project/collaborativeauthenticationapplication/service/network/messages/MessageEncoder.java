package com.project.collaborativeauthenticationapplication.service.network.messages;

import com.project.collaborativeauthenticationapplication.logger.AndroidLogger;
import com.project.collaborativeauthenticationapplication.logger.Logger;
import com.project.collaborativeauthenticationapplication.service.crypto.BigNumber;
import com.project.collaborativeauthenticationapplication.service.crypto.Point;
import com.project.collaborativeauthenticationapplication.service.general.IdentifiedParticipant;
import com.project.collaborativeauthenticationapplication.service.key.application.key_generation.local_system.control.protocol.KeyGenerationSession;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

public class MessageEncoder {





    private static final String COMPONENT_NAME    = "encoder";
    private static final Logger logger            = new AndroidLogger();


    public static final byte MAIN_STRING_TYPE  = 1;
    public static final byte CON_STRING_TYPE   = 2;
    public static final byte BIG_NUMBER_TYPE   = 3;
    public static final byte INT_PARAM_TYPE    = 4;
    public static final byte POINT_TYPE        = 5;
    public static final byte COMMITMENTS       = 6;
    public static final byte NAMED_COMMITMENTS = 6;

    public static final byte PREAMBLE_BEGIN        = 1;

    public static final byte PREAMBLE_CON          = 2;



    HashMap<Integer, byte[]> table = new HashMap<>();

    int totalLength = 0;


    public void addHeaderField(byte type){
        byte[] value = {type};
        table.put(new Integer(totalLength), value );
        totalLength += 1;
    }

    public void addString(String string){
        byte[] value = string.getBytes(StandardCharsets.ISO_8859_1);
        int length   =  value.length;
        if (length == 0){
            throw new IllegalArgumentException("Illegal use of empty strings");
        }
        byte nextType = MAIN_STRING_TYPE;
        int numberOfBlocks = length/256;
        int remainder      = length%256;
        for(int block = 0; block < numberOfBlocks; block++){ //number of full blocks
            byte[] message = new byte[256+2];
            message[0] = nextType;
            message[1] = -1;
            System.arraycopy(value,   block*256, message, 2, 256);
            nextType = CON_STRING_TYPE;
            table.put(new Integer(totalLength), message);
            totalLength += 1;
        }
        if (remainder != 0){
            byte[] message = new byte[remainder+2];
            message[0]     = nextType;
            int remainderB = remainder -1; //need for compatibility
            if (remainderB > 127){
                remainderB = remainder- 256;
            }
            message[1] =  (byte) remainderB;
            System.arraycopy(value,   numberOfBlocks*256, message, 2, remainder);
            table.put(new Integer(totalLength), message);
            totalLength += 1;
        }
    }


    public void addBoolean(boolean bool){
        byte[] message = new byte[1];
        if (bool){
            message[0] = 1;
        } else {
            message[0] = 0;
        }
        table.put(new Integer(totalLength), message);
        totalLength += 1;
    }

    public void addBigNumber(BigNumber number){
        byte[] message = new byte[33]; //32 bytes for number, 1 for type
        message[0] = BIG_NUMBER_TYPE;
        System.arraycopy(number.getBigNumberAsByteArray(),   0, message, 1, 32);
        table.put(new Integer(totalLength), message);
        totalLength += 1;
    }


    public void addZero(){
        byte[] message = {0};
        table.put(new Integer(totalLength), message);
        totalLength += 1;
    }

    public void addPoint(Point point){
        addHeaderField(POINT_TYPE);
        addBigNumber(point.getX());
        addBigNumber(point.getY());
        addBoolean(point.isZero());

    }


    public void addNonZeroByteParameter(int identifier){
        if (identifier > 256){
            throw  new UnsupportedOperationException("Application does not consider");
        }
        byte[] message = new byte[2]; //four bytes for integer, one for type
        message[0]     = INT_PARAM_TYPE;
        byte identifierB = getByteFromInt(identifier);
        message[1] = identifierB;
        table.put(new Integer(totalLength), message);
        totalLength += 1;
    }

    private byte getByteFromInt(int identifier) {
        int identifierB    = identifier -1;
        if (identifierB > 127){
            identifierB = identifierB - 256;
        }
        return (byte) identifierB;
    }

    public byte[] build(){
        int length = 0;
        for(byte[] arr : table.values()){
            length += arr.length;
        }
        logger.logEvent(COMPONENT_NAME, "encode bytes (length)", "low", String.valueOf(length));
        logger.logEvent(COMPONENT_NAME, "encode parts (total length)", "low", String.valueOf(totalLength));
        int numberOfParts = (int) Math.ceil(((double)length)/(256.0)) ;
        logger.logEvent(COMPONENT_NAME, "encode pieces", "low", String.valueOf(numberOfParts));
        byte[] message = new byte[length+2*numberOfParts];
        message[0] = PREAMBLE_BEGIN;
        int index = 2;
        int currentSizeIndex = 1;
        int doneLength = 0;
        for(int i= 0; i < totalLength; i++){
            byte[] part = table.getOrDefault(new Integer(i), null);
            int l = part.length;
            logger.logEvent(COMPONENT_NAME, "encode part length", "low", String.valueOf(l));
            logger.logEvent(COMPONENT_NAME, "encode part index", "low", String.valueOf(index));
            logger.logEvent(COMPONENT_NAME, "done before", "low", String.valueOf(doneLength));
            int doneLengthNew = doneLength + l;
            logger.logEvent(COMPONENT_NAME, "new done length", "low", String.valueOf(doneLengthNew));
            int internal = 0;
            while (doneLengthNew > 256){
                logger.logEvent(COMPONENT_NAME, "loop: higher than 256", "low", String.valueOf(l));
                message[currentSizeIndex] = getByteFromInt(256);
                System.arraycopy(part,   internal, message, index, 256-doneLength);
                int basis = index + 256 - doneLength;
                internal = 256-doneLength;
                logger.logEvent(COMPONENT_NAME, "internal", "low", String.valueOf(internal));
                index = basis +2;
                message[basis] = PREAMBLE_CON;
                currentSizeIndex = basis+1;
                doneLengthNew = doneLengthNew - 256;
                doneLength    = 0;
            }
            int size = doneLengthNew-doneLength;
            logger.logEvent(COMPONENT_NAME, "size", "low", String.valueOf(size));
            message[currentSizeIndex] = getByteFromInt(doneLengthNew);
            logger.logEvent(COMPONENT_NAME, "current size index", "low", String.valueOf(currentSizeIndex));
            System.arraycopy(part,   internal, message, index, size);
            logger.logEvent(COMPONENT_NAME, "internal", "low", String.valueOf(internal));
            index += size;
            doneLength = doneLengthNew;
        }
        table.clear();
        totalLength = 0;
        return  message;
    }


    public byte[] makeAbortMessage(String applicationName, String login){
        addHeaderField(MessageParser.ABORT_MESSAGE_CODE);
        addString(applicationName);
        addString(login);
        return build();
    }

    public byte[] makeConnectMessage(String applicationName, String login){
        addHeaderField(MessageParser.CONN_MESSAGE_CODE);
        addString(applicationName);
        addString(login);
        return build();
    }


    public byte[] makeInvitationMessage(KeyGenerationSession session, int recipientIdentifier){
        addHeaderField(MessageParser.INVITATION_MESSAGE_CODE);
        addString(session.getApplicationName());
        addNonZeroByteParameter(session.getThreshold());
        addNonZeroByteParameter(session.getTotalWeight());
        List<IdentifiedParticipant> participants = session.getRemoteParticipantList();
        addNonZeroByteParameter(participants.size()+1); //remote plus a local participant
        for (IdentifiedParticipant participant : participants){
            addNonZeroByteParameter(participant.getIdentifier());
            if (participant.getIdentifier() == recipientIdentifier){
                addZero();
            } else {
                addString(participant.getAddress());
            }
        }
        return build();
    }

    public byte[] makePartsMessage(ArrayList<BigNumber> parts, Point publicKey){
        addHeaderField(MessageParser.PARTS_MESSAGE_CODE);
        addNonZeroByteParameter(parts.size());
        for (BigNumber part: parts){
            addBigNumber(part);
        }
        addPoint(publicKey);
        return build();
    }


    public byte[] makeVoteYesMessage(){
        addHeaderField(MessageParser.COMMIT_MESSAGE_CODE);
        addBoolean(true);
        return build();
    }

    public byte[] makeStartSignMessage(String applicationName,  int numberToRequest, String localAddress){
        addHeaderField(MessageParser.START_SIGN_CODE);
        addString(localAddress);
        addString(applicationName);
        addNonZeroByteParameter(numberToRequest);
        return build();
    }

    public byte[] makeVoteNo(){
        addHeaderField(MessageParser.COMMIT_MESSAGE_CODE);
        addBoolean(false);
        return build();
    }

    public void makePartOneCommitmentResponse(List<Point> commitments, String address){
        addHeaderField(MessageParser.SIGN_COMMIT_CODE);
        addString(address);
        addCommitments(commitments);
    }

    public void makePartTwoCommitmentResponse(List<Point> commitments){
        addCommitments(commitments);
    }

    protected void addCommitments(List<Point> commitments) {
        addHeaderField(COMMITMENTS);
        addNonZeroByteParameter(commitments.size());
        for (Point commitment : commitments) {
            addPoint(commitment);
        }
    }

    public byte[] makeSignatureMessage(BigNumber signature){
        addHeaderField(MessageParser.SIGN_SIGNATURE_CODE);
        addBigNumber(signature);
        return build();
    }


    public byte[] makePublishMessage(HashMap<String, ArrayList<Point>> commitmentsE, HashMap<String,
            ArrayList<Point>> commitmentsD, BigNumber message, String localAddress){
        addHeaderField(MessageParser.SIGN_PUBLISH_CODE);
        addBigNumber(message);
        Set<String> remotes = commitmentsE.keySet();
        addNonZeroByteParameter(remotes.size());
        for (String remote: remotes){
            addHeaderField(NAMED_COMMITMENTS);
            if (remote.equals("here")){
                addString(localAddress);
            } else {
                addString(remote);
            }
            addCommitments(commitmentsE.get(remote));
            addCommitments(commitmentsD.get(remote));
        }
        return build();
    }

    public void clear(){
        table.clear();
    }
}
