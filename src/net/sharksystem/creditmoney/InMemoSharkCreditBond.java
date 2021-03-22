package net.sharksystem.creditmoney;

import net.sharksystem.asap.ASAPSecurityException;
import net.sharksystem.asap.crypto.ASAPCryptoAlgorithms;
import net.sharksystem.asap.crypto.ASAPKeyStore;
import net.sharksystem.asap.persons.Person;
import net.sharksystem.asap.utils.ASAPSerialization;

import java.io.*;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;

public class InMemoSharkCreditBond implements SharkCreditBond, Serializable {

    /**
     * This constant is used to set the bond's expirationDate
     * It can also be set to the validity of the ASAP's certificate as follow:
     * ASAPCertificateImpl.DEFAULT_CERTIFICATE_VALIDITY_IN_YEARS
     */
    public static final int DEFAULT_CREDIT_BOND_VALIDITY_IN_YEARS = 1;
    private final CharSequence unitDescription;
    private final int amount;
    private final boolean allowedToChangeDebtor, allowedToChangeCreditor;
    private long expirationDate;
    private Person debtor, creditor;
    private byte[] debtorSignature, creditorSignature;


    public InMemoSharkCreditBond(CharSequence unitDescription, int amount) {
        this.unitDescription = unitDescription;
        this.amount = amount;
        this.setExpirationDate();
        this.debtorSignature = null;
        this.creditorSignature = null;
        this.allowedToChangeDebtor = true;
        this.allowedToChangeCreditor = true;
    }

    public InMemoSharkCreditBond(CharSequence creditorID, CharSequence debtorID, CharSequence unitDescription, int amount) {
        this(creditorID, debtorID, unitDescription, amount,true);
    }

    public InMemoSharkCreditBond(CharSequence creditorID, CharSequence debtorID, CharSequence unitDescription, int amount, boolean allowTransfer) {
        this.creditor = new PersonImpl(creditorID);
        this.debtor = new PersonImpl(debtorID);
        this.unitDescription = unitDescription;
        this.amount = amount;
        this.setExpirationDate();
        this.debtorSignature = null;
        this.creditorSignature = null;
        this.allowedToChangeDebtor = allowTransfer;
        this.allowedToChangeCreditor = allowTransfer;
    }

    /**
     * @return debtor of this bond
     */
    @Override
    public Person getDebtor() {
        return this.debtor;
    }

    @Override
    public boolean signedByDebtor() {
        if (this.debtorSignature == null) {
            return false;
        } else {
            return true;
        }
    }

    @Override
    public boolean allowedToChangeDebtor() {
        return this.allowedToChangeDebtor;
    }

    /**
     * Set the debtor of this bond
     * @param debtor
     */
    public void setDebtor(Person debtor) throws SharkCreditMoneyException {
        if (this.allowedToChangeDebtor) {
            this.debtor = debtor;
        } else {
            throw new SharkCreditMoneyException("Method not allowed. The current bond's debtor can't be changed");
        }
    }

    public boolean isDebtorSignatureCorrect(ASAPKeyStore ASAPKeyStore) throws ASAPSecurityException {
        return  this.isSignatureCorrect(this.debtor.getUUID().toString(), this.toString().getBytes(), this.debtorSignature, ASAPKeyStore);
    }

    /**
     * @return creditor of this bond
     */
    @Override
    public Person getCreditor() {
        return this.creditor;
    }

    @Override
    public boolean signedByCreditor() {
        if (this.creditorSignature == null) {
            return false;
        } else {
            return true;
        }
    }

    @Override
    public boolean allowedToChangeCreditor() {
        return this.allowedToChangeCreditor;
    }

    /**
     * Set the creditor of this bond
     * @param creditor
     */
    public void setCreditor(Person creditor) throws SharkCreditMoneyException {
        if (this.allowedToChangeCreditor) {
            this.creditor = creditor;
        } else {
            throw new SharkCreditMoneyException("Method not allowed. The current bond's creditor can't be changed");
        }
    }

    public boolean isCreditorSignatureCorrect(ASAPKeyStore ASAPKeyStore) throws ASAPSecurityException {
        return  this.isSignatureCorrect(this.creditor.getUUID().toString(), this.toString().getBytes(), this.creditorSignature, ASAPKeyStore);
    }

    /**
     * A bond defines a kind of depth. Each bond can have its own unit, e.g. bar of gold-pressed latinum, a favour,
     * a cigarette was a currency after WW2, etc. pp.
     *
     * @return unit you defined
     */
    @Override
    public CharSequence unitDescription() {
        return this.unitDescription;
    }

    /**
     * @return amounts of whatever unit you defined.
     */
    @Override
    public int getAmount() {
        return this.amount;
    }

    @Override
    public long getExpirationDate() {
        return this.expirationDate;
    }

    public void extendCreditBondValidity() {
        //TODO: extend bond's validity to one year.
        this.setExpirationDate(this.expirationDate);
    }

    public void annulBond() {
        this.setBondAsExpired();
    }

    public void signBondAsCreditor(ASAPKeyStore ASAPKeyStore) throws ASAPSecurityException {
        this.creditorSignature = ASAPCryptoAlgorithms.sign(this.toString().getBytes(), ASAPKeyStore);
    }

    public void signBondAsDebtor(ASAPKeyStore ASAPKeyStore) throws ASAPSecurityException {
        this.debtorSignature = ASAPCryptoAlgorithms.sign(this.toString().getBytes(), ASAPKeyStore);
    }

    @Override
    public String toString() {
        return "CreditBond{" +
                "creditorID=" + creditor.getUUID() +
                ", debtorID=" + debtor.getUUID() +
                ", unitDescription=" + unitDescription +
                ", amount=" + amount +
                ", expirationDate=" + expirationDate +
                ", creditorSignature=" + creditorSignature +
                ", debtorSignature=" + debtorSignature +
                '}';
    }

    public static byte [] serializeCreditBond(InMemoSharkCreditBond creditBond) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream out = null;
        byte[] serializedCreditBond = null;
        try {
            out = new ObjectOutputStream(bos);
            out.writeObject(creditBond);
            out.flush();
            serializedCreditBond = bos.toByteArray();
            ///// content
            ASAPSerialization.writeByteArray(serializedCreditBond, bos);
            ///// sender
            ASAPSerialization.writeCharSequenceParameter(creditBond.creditor.getUUID(), bos);
            ///// recipients
            Set<CharSequence> recipients = new HashSet<>();
            recipients.add(creditBond.debtor.getUUID());
            ASAPSerialization.writeCharSequenceSetParameter(recipients, bos);
            ///// timestamp
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

    public static InMemoSharkCreditBond deserializeCreditBond(byte[] serializedCreditBond) throws IOException {
        ByteArrayInputStream bis = new ByteArrayInputStream(serializedCreditBond);
        ObjectInput in = null;
        InMemoSharkCreditBond creditBond = null;
        try {
            in = new ObjectInputStream(bis);
            creditBond = (InMemoSharkCreditBond) in.readObject();
        } catch (ClassNotFoundException e) {
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

    private void setExpirationDate() {
        Calendar until = Calendar.getInstance();
        this.setExpirationDate(until.getTimeInMillis());
    }

    private void setExpirationDate(long creationDate) {
        Calendar until = Calendar.getInstance();
        until.setTimeInMillis(creationDate);
        until.add(Calendar.YEAR, DEFAULT_CREDIT_BOND_VALIDITY_IN_YEARS);
        this.expirationDate = until.getTimeInMillis();
    }

    private void setBondAsExpired() {
        Calendar until = Calendar.getInstance();
        this.expirationDate = until.getTimeInMillis();
    }

    private boolean isSignatureCorrect(String signer, byte[] message, byte[] signature, ASAPKeyStore ASAPKeyStore) throws ASAPSecurityException {
        return ASAPCryptoAlgorithms.verify(message, signature, signer, ASAPKeyStore);
    }
}
