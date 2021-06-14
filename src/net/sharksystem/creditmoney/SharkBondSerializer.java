package net.sharksystem.creditmoney;

import net.sharksystem.asap.ASAPException;
import net.sharksystem.asap.ASAPSecurityException;
import net.sharksystem.asap.crypto.ASAPCryptoAlgorithms;
import net.sharksystem.asap.crypto.ASAPKeyStore;
import net.sharksystem.asap.utils.ASAPSerialization;

import java.io.*;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.json.*;

class SharkBondSerializer {

    static byte [] serializeCreditBond(SharkBond creditBond, ASAPKeyStore asapKeyStore) throws IOException, ASAPSecurityException {
        return serializeCreditBond(creditBond, null, null, true, true, asapKeyStore, true);
    }

    static byte [] serializeCreditBond(SharkBond creditBond, CharSequence sender, Set<CharSequence> receiver,
                                       boolean sign, boolean encrypt, ASAPKeyStore asapKeyStore, boolean excludeSignature) throws ASAPSecurityException, IOException {
        return serializeCreditBond(creditBond, sender, receiver, sign, encrypt, asapKeyStore, excludeSignature, 0);
    }


    static byte [] serializeCreditBond(SharkBond creditBond, CharSequence sender, Set<CharSequence> receiver,
                                       boolean sign, boolean encrypt, ASAPKeyStore asapKeyStore, boolean excludeSignature, int usedFor) throws ASAPSecurityException, IOException {

        if( (receiver != null && receiver.size() > 1) && encrypt) {
            throw new ASAPSecurityException("cannot (yet) encrypt one message for more than one recipient - split it into more messages");
        }

        if(receiver == null || receiver.isEmpty()) {
            if(encrypt) throw new ASAPSecurityException("impossible to encrypt a message without a receiver");
            // else
            receiver = new HashSet<>();
            receiver.add(creditBond.getDebtorID());
        }

        if(sender == null) {
            sender = creditBond.getCreditorID();
        }

        // Convert bond to byteArray
        // use excludeSignature to exclude or include previous signatures (that can be helpful for a late verification of the the bond signatures)
        byte[] content = sharkBondToByteArray(creditBond, excludeSignature);
        /////////// produce serialized structure

        // merge content, sender and recipient
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ///// content
        ASAPSerialization.writeByteArray(content, baos);
        ///// sender is the creditor
        ASAPSerialization.writeCharSequenceParameter(sender, baos);
        ///// recipients
        ASAPSerialization.writeCharSequenceSetParameter(receiver, baos);

        ///// timestamp
        // Timestamp creationTime = new Timestamp(System.currentTimeMillis());
        // String timestampString = creationTime.toString();
        // ASAPSerialization.writeCharSequenceParameter(timestampString, baos);

        content = baos.toByteArray();

        byte flags = 0;
        // Sign SC Bond Message
        if(sign) {
            byte[] signature = ASAPCryptoAlgorithms.sign(content, asapKeyStore);
            // usedFor == 1 function is used for signature purpose
            if (usedFor == 1) {
                return signature;
            }

            baos = new ByteArrayOutputStream();
            ASAPSerialization.writeByteArray(content, baos); // message has three parts: content, sender, receiver

            // usedFor == 2 function is used for verification purpose
            if (usedFor == 2) {
                return content;
            }

            // append signature
            ASAPSerialization.writeByteArray(signature, baos);
            // attach signature to message
            content = baos.toByteArray();
            flags += SharkBond.SIGNED_MASK;
        }

        if(encrypt) {
            // Encrypt SN Message
            content = ASAPCryptoAlgorithms.produceEncryptedMessagePackage(
                    content,
                    receiver.iterator().next(), // already checked if one and only one is recipient
                    asapKeyStore);
            flags += SharkBond.ENCRYPTED_MASK;
        }

        // serialize SN message
        baos = new ByteArrayOutputStream();
        ASAPSerialization.writeByteParameter(flags, baos);
        ASAPSerialization.writeByteArray(content, baos);

        return baos.toByteArray();
    }

    static SharkBond deserializeCreditBond(byte[] serializedCreditBond, ASAPKeyStore asapKeyStore) throws IOException, ASAPException {
        ByteArrayInputStream bais = new ByteArrayInputStream(serializedCreditBond);
        byte flags = ASAPSerialization.readByte(bais);
        byte[] tmpMessage = ASAPSerialization.readByteArray(bais);

        boolean signed = (flags & SharkBond.SIGNED_MASK) != 0;
        boolean encrypted = (flags & SharkBond.ENCRYPTED_MASK) != 0;

        if (encrypted) {
            // decrypt
            bais = new ByteArrayInputStream(tmpMessage);
            ASAPCryptoAlgorithms.EncryptedMessagePackage
                    encryptedMessagePackage = ASAPCryptoAlgorithms.parseEncryptedMessagePackage(bais);

            // for me?
            if (!asapKeyStore.isOwner(encryptedMessagePackage.getRecipient())) {
                throw new ASAPException("SharkBond Message: message not for me");
            }
            // replace message with decrypted message
            tmpMessage = ASAPCryptoAlgorithms.decryptPackage(
                    encryptedMessagePackage, asapKeyStore);
        }

        byte[] signature = null;
        byte[] signedMessage = null;
        if (signed) {
            // split message from signature
            bais = new ByteArrayInputStream(tmpMessage);
            tmpMessage = ASAPSerialization.readByteArray(bais);
            signedMessage = tmpMessage;
            signature = ASAPSerialization.readByteArray(bais);
        }

        ///////////////// produce object form serialized bytes
        bais = new ByteArrayInputStream(tmpMessage);

        ////// content
        byte[] snMessage = ASAPSerialization.readByteArray(bais);
        ////// sender
        String snSender = ASAPSerialization.readCharSequenceParameter(bais);
        ////// recipients
        Set<CharSequence> snReceivers = ASAPSerialization.readCharSequenceSetParameter(bais);
        ///// timestamp
        // String timestampString = ASAPSerialization.readCharSequenceParameter(bais);
        // Timestamp creationTime = Timestamp.valueOf(timestampString);

        boolean verified = false; // initialize
        if (signature != null) {
            try {
                verified = ASAPCryptoAlgorithms.verify(
                        signedMessage, signature, snSender, asapKeyStore);
            } catch (ASAPSecurityException e) {
                // verified definitely false
                verified = false;
            }
        }

        System.out.println("Verified: " + verified);

        // replace special sn symbols
        return byteArrayToSharkBond(snMessage);
    }

    static byte[] deserializeCreditBond(byte[] serializedCreditBond, ASAPKeyStore asapKeyStore, int usedFor) throws IOException, ASAPException {
        ByteArrayInputStream bais = new ByteArrayInputStream(serializedCreditBond);
        byte flags = ASAPSerialization.readByte(bais);
        byte[] tmpMessage = ASAPSerialization.readByteArray(bais);

        boolean signed = (flags & SharkBond.SIGNED_MASK) != 0;
        boolean encrypted = (flags & SharkBond.ENCRYPTED_MASK) != 0;

        if (encrypted) {
            // decrypt
            bais = new ByteArrayInputStream(tmpMessage);
            ASAPCryptoAlgorithms.EncryptedMessagePackage
                    encryptedMessagePackage = ASAPCryptoAlgorithms.parseEncryptedMessagePackage(bais);

            // for me?
            if (!asapKeyStore.isOwner(encryptedMessagePackage.getRecipient())) {
                throw new ASAPException("SharkBond Message: message not for me");
            }
            // replace message with decrypted message
            tmpMessage = ASAPCryptoAlgorithms.decryptPackage(
                    encryptedMessagePackage, asapKeyStore);
        }

        byte[] signature = null;
        byte[] signedMessage = null;
        if (signed) {
            // split message from signature
            bais = new ByteArrayInputStream(tmpMessage);
            tmpMessage = ASAPSerialization.readByteArray(bais);
            signedMessage = tmpMessage;
            signature = ASAPSerialization.readByteArray(bais);

            // usedFor == 1 function is used for signature purpose
            if (usedFor == 1) {
                return signature;
            }

            // usedFor == 2 function is used for verification purpose
            if (usedFor == 2) {
                return signedMessage;
            }
        }

        ///////////////// produce object form serialized bytes
        bais = new ByteArrayInputStream(tmpMessage);

        ////// content
        byte[] snMessage = ASAPSerialization.readByteArray(bais);
        ////// sender
        String snSender = ASAPSerialization.readCharSequenceParameter(bais);
        ////// recipients
        Set<CharSequence> snReceivers = ASAPSerialization.readCharSequenceSetParameter(bais);
        ///// timestamp
        // String timestampString = ASAPSerialization.readCharSequenceParameter(bais);
        // Timestamp creationTime = Timestamp.valueOf(timestampString);

        boolean verified = false; // initialize
        if (signature != null) {
            try {
                verified = ASAPCryptoAlgorithms.verify(
                        signedMessage, signature, snSender, asapKeyStore);
            } catch (ASAPSecurityException e) {
                // verified definitely false
                verified = false;
            }
        }

        System.out.println("Verified: " + verified);

        // replace special sn symbols
        return snMessage;
    }

    static byte [] sharkBondToByteArray(String creditBond) {
        byte[] byteArray = null;

        try {
            byteArray = creditBond.getBytes();
        } catch (NullPointerException e) {
            e.printStackTrace();
        }

        return byteArray;
    }

    static byte [] sharkBondToByteArray(SharkBond creditBond, boolean excludeSignature) {

        // if excludeSignature is true set creditor and debtor signature to null before signing
        if (excludeSignature) {
            SharkBond creditBondCopy = new InMemoSharkBond(creditBond);
            creditBondCopy.setCreditorSignature(null);
            creditBondCopy.setDebtorSignature(null);
            return sharkBondToByteArray(objectToJsonString(creditBondCopy));
        }

        return sharkBondToByteArray(objectToJsonString(creditBond));
    }

    static SharkBond byteArrayToSharkBond(byte [] byteArray) {
        SharkBond bond = null;
        String bondString;
        try {
            bondString = new String(byteArray);
            bond = jsonStringToSharkBond(bondString);
        } catch (NullPointerException e) {
            e.printStackTrace();
        }

        return bond;
    }

    static String objectToJsonString(SharkBond creditBond) {
        List<String> debtorSignature = new ArrayList<>();
        List<String> creditorSignature = new ArrayList<>();
        if (creditBond.getDebtorSignature() != null) {
            for (byte debtorSignatureByte: creditBond.getDebtorSignature()) {
                debtorSignature.add("" + debtorSignatureByte);
            }
        }

        if (creditBond.getCreditorSignature() != null) {
            for (byte creditorSignatureByte: creditBond.getCreditorSignature()) {
                creditorSignature.add("" + creditorSignatureByte);
            }
        }

        JSONArray debtorJsonArray = new JSONArray(debtorSignature);
        JSONArray creditorJsonArray = new JSONArray(creditorSignature);

        return "{" +
                "\"bondID\":\"" + creditBond.getBondID() + "\""+
                ", \"creditorID\":\"" + creditBond.getCreditorID() + "\""+
                ", \"debtorID\":\"" + creditBond.getDebtorID() + "\""+
                ", \"unitDescription\":\"" + creditBond.unitDescription() + "\""+
                ", \"amount\":" + creditBond.getAmount() +
                ",\"expirationDate\":" + creditBond.getExpirationDate() +
                ", \"debtorSignature\":" + debtorJsonArray.toString() +
                ", \"creditorSignature\":" + creditorJsonArray.toString() +
                ", \"allowedToChangeDebtor\":" + creditBond.allowedToChangeDebtor() +
                ", \"allowedToChangeCreditor\":" + creditBond.allowedToChangeCreditor() +
                ", \"bondIsAnnulledByCreditor\":" + creditBond.getBondIsAnnulledByCreditor() +
                ", \"bondIsAnnulledByCreditor\":" + creditBond.getBondIsAnnulledByDebtor() +
                ", \"tempCreditorID\":\"" + creditBond.getTempCreditorID() + "\""+
                ", \"tempDebtorID\":\"" + creditBond.getTempDebtorID() + "\""+
                '}';
    }

    static SharkBond jsonStringToSharkBond(String jsonString) {
        JSONObject jsonObject = new JSONObject(jsonString);
        JSONArray jArrayDebtorSignature = jsonObject.getJSONArray("debtorSignature");
        byte[] debtorSignature = new byte[jArrayDebtorSignature.length()];

        for (int i=0;i<jArrayDebtorSignature.length();i++){
            debtorSignature[i] = (byte) jArrayDebtorSignature.getInt(i);
        }
        JSONArray jArrayCreditorSignature = jsonObject.getJSONArray("creditorSignature");
        byte[] creditorSignature = new byte[jArrayCreditorSignature.length()];
        for (int i=0;i<jArrayCreditorSignature.length();i++){
            creditorSignature[i] = Byte.parseByte(jArrayCreditorSignature.getString(i));
        }
        return new InMemoSharkBond(
                jsonObject.getString("bondID"),
                jsonObject.getString("creditorID"),
                jsonObject.getString("debtorID"),
                jsonObject.getString("unitDescription"),
                jsonObject.getInt("amount"),
                jsonObject.getLong("expirationDate"),
                debtorSignature.length == 0 ? null : debtorSignature,
                creditorSignature.length == 0 ? null : creditorSignature,
                jsonObject.getBoolean("allowedToChangeCreditor"),
                jsonObject.getBoolean("allowedToChangeDebtor"),
                jsonObject.getBoolean("bondIsAnnulledByCreditor"),
                jsonObject.getBoolean("bondIsAnnulledByCreditor"),
                jsonObject.getString("tempCreditorID"),
                jsonObject.getString("tempDebtorID")
        );
    }
}
