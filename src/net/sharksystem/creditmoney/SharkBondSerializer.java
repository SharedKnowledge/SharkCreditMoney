package net.sharksystem.creditmoney;

import net.sharksystem.asap.ASAPException;
import net.sharksystem.asap.utils.ASAPSerialization;

import java.io.*;
import java.sql.Timestamp;
import java.util.HashSet;
import java.util.Set;

class SharkBondSerializer {
    static byte [] serializeCreditBond(SharkBond creditBond) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream out = null;
        byte[] serializedCreditBond = null;
        try {
            out = new ObjectOutputStream(bos);
            out.writeObject(creditBond);
            out.flush();
            serializedCreditBond = bos.toByteArray();
            // content
            ASAPSerialization.writeByteArray(serializedCreditBond, bos);
            // sender
            ASAPSerialization.writeCharSequenceParameter(creditBond.getCreditorID(), bos);
            // recipients
            Set<CharSequence> recipients = new HashSet<>();
            recipients.add(creditBond.getDebtorID());
            ASAPSerialization.writeCharSequenceSetParameter(recipients, bos);
            // timestamp
            Timestamp creationTime = new Timestamp(System.currentTimeMillis());
            String timestampString = creationTime.toString();
            ASAPSerialization.writeCharSequenceParameter(timestampString, bos);

            serializedCreditBond = bos.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                bos.close();
            } catch (IOException ex) {
                // ignore close exception
                serializedCreditBond = null;
            }
        }

        return serializedCreditBond;
    }

    static SharkBond deserializeCreditBond(byte[] serializedCreditBond) throws IOException {
        ByteArrayInputStream bis = new ByteArrayInputStream(serializedCreditBond);
        ObjectInput in = null;
        InMemoSharkBond creditBond = null;
        try {
            //// content
            byte[] snMessage = ASAPSerialization.readByteArray(bis);
            //// sender
            String snSender = ASAPSerialization.readCharSequenceParameter(bis);
            //// recipients
            Set<CharSequence> snReceivers = ASAPSerialization.readCharSequenceSetParameter(bis);
            //// timestamp
            String timestampString = ASAPSerialization.readCharSequenceParameter(bis);
            Timestamp creationTime = Timestamp.valueOf(timestampString);
            in = new ObjectInputStream(bis);
            creditBond = (InMemoSharkBond) in.readObject();
        } catch (ClassNotFoundException | ASAPException e) {
            e.printStackTrace();
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (IOException ex) {
                // ignore close exception
                creditBond = null;
            }
        }

        return creditBond;
    }
}
