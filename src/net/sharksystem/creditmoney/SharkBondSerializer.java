package net.sharksystem.creditmoney;

import net.sharksystem.asap.ASAPException;
import net.sharksystem.asap.ASAPSecurityException;
import net.sharksystem.asap.crypto.ASAPCryptoAlgorithms;
import net.sharksystem.asap.crypto.ASAPKeyStore;
import net.sharksystem.asap.utils.ASAPSerialization;

import java.io.*;
import java.sql.Timestamp;
import java.util.HashSet;
import java.util.Set;

class SharkBondSerializer {

    static byte [] serializeCreditBond(SharkBond creditBond, ASAPKeyStore asapKeyStore) throws ASAPSecurityException, IOException {
        // recipients
        Set<CharSequence> recipients = new HashSet<>();
        recipients.add(creditBond.getDebtorID());

        // Convert bond to byteArray
        // use excludeSignature to exclude or include previous signatures (that can be helpful for a late verification of the the bond signatures)
        byte[] content = sharkBondToByteArray(creditBond, true);
        /////////// produce serialized structure

        // merge content, sender and recipient
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ///// content
        ASAPSerialization.writeByteArray(content, baos);
        ///// sender is the creditor
        ASAPSerialization.writeCharSequenceParameter(creditBond.getCreditorID(), baos);
        ///// recipients
        ASAPSerialization.writeCharSequenceSetParameter(recipients, baos);
        ///// timestamp
        Timestamp creationTime = new Timestamp(System.currentTimeMillis());
        String timestampString = creationTime.toString();
        ASAPSerialization.writeCharSequenceParameter(timestampString, baos);

        content = baos.toByteArray();

        byte flags = 0;
        // Sign SN Message
        byte[] signature = ASAPCryptoAlgorithms.sign(content, asapKeyStore);
        baos = new ByteArrayOutputStream();
        ASAPSerialization.writeByteArray(content, baos); // message has three parts: content, sender, receiver
        // append signature
        ASAPSerialization.writeByteArray(signature, baos);
        // attach signature to message
        content = baos.toByteArray();
        flags += SharkBond.SIGNED_MASK;

        // Encrypt SN Message
        content = ASAPCryptoAlgorithms.produceEncryptedMessagePackage(
                content,
                recipients.iterator().next(), // already checked if one and only one is recipient
                asapKeyStore);
        flags += SharkBond.ENCRYPTED_MASK;

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

            // decrypt
        bais = new ByteArrayInputStream(tmpMessage);
        ASAPCryptoAlgorithms.EncryptedMessagePackage
                encryptedMessagePackage = ASAPCryptoAlgorithms.parseEncryptedMessagePackage(bais);

        // replace message with decrypted message
        tmpMessage = ASAPCryptoAlgorithms.decryptPackage(
                encryptedMessagePackage, asapKeyStore);

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
        String timestampString = ASAPSerialization.readCharSequenceParameter(bais);
        Timestamp creationTime = Timestamp.valueOf(timestampString);

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

        // replace special sn symbols
        return byteArrayToSharkBond(snMessage);
    }

    static byte [] sharkBondToByteArray(SharkBond creditBond) {
        byte[] byteArray = null;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream out = null;
        try {
            out = new ObjectOutputStream(bos);
            out.writeObject(creditBond);
            out.flush();
            byteArray = bos.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                bos.close();
            } catch (IOException ex) {
                // ignore close exception
            }
        }

        return byteArray;
    }

    static byte [] sharkBondToByteArray(SharkBond creditBond, boolean excludeSignature) {

        // if excludeSignature is true set creditor and debtor signature to null before signing
        if (excludeSignature) {
            SharkBond creditBondCopy = new InMemoSharkBond(creditBond);
            creditBondCopy.setCreditorSignature(null);
            creditBondCopy.setDebtorSignature(null);
            return sharkBondToByteArray(creditBondCopy);
        }

        return sharkBondToByteArray(creditBond);
    }

    static SharkBond byteArrayToSharkBond(byte [] byteArray) {
        SharkBond bond = null;
        ByteArrayInputStream bis = new ByteArrayInputStream(byteArray);
        ObjectInput in = null;
        try {
            in = new ObjectInputStream(bis);
            bond = (SharkBond) in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (IOException ex) {
                // ignore close exception
            }
        }

        return bond;
    }

}
