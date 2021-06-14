package net.sharksystem.creditmoney;

import java.io.*;
import java.util.Calendar;
import java.util.UUID;

public class InMemoSharkBond implements SharkBond, Serializable {
    /**
     * This constant is used to set the bond's expirationDate
     * It can also be set to the validity of the ASAP's certificate as follow:
     * ASAPCertificateImpl.DEFAULT_CERTIFICATE_VALIDITY_IN_YEARS
     */
    public static final int DEFAULT_CREDIT_BOND_VALIDITY_IN_YEARS = 1;
    private final CharSequence unitDescription;
    private final int amount;
    private boolean allowedToChangeDebtor, allowedToChangeCreditor;
    private long expirationDate;
    private CharSequence bondID;
    private CharSequence debtorID, creditorID;
    private CharSequence tempDebtorID, tempCreditorID;
    private byte[] debtorSignature, creditorSignature;
    private boolean bondIsAnnulledByCreditor, bondIsAnnulledByDebtor;

    public InMemoSharkBond(SharkBond bond) {
        this(bond.getBondID(), bond.getCreditorID(), bond.getDebtorID(),
                bond.unitDescription(), bond.getAmount(), bond.getExpirationDate(),
                bond.getDebtorSignature(), bond.getCreditorSignature(), bond.allowedToChangeCreditor(),
                bond.allowedToChangeDebtor(), bond.getBondIsAnnulledByCreditor(), bond.getBondIsAnnulledByDebtor(),
                bond.getTempCreditorID(), bond.getTempDebtorID());
    }

    public InMemoSharkBond(CharSequence creditorID, CharSequence debtorID, CharSequence unitDescription, int amount, boolean allowTransfer) {
        this(creditorID, debtorID, unitDescription, amount, allowTransfer, allowTransfer);
    }

    public InMemoSharkBond(CharSequence creditorID, CharSequence debtorID, CharSequence unitDescription, int amount, boolean allowedToChangeCreditor, boolean allowedToChangeDebtor) {
        this(generateBondID(), creditorID, debtorID, unitDescription, amount, getDefaultExpirationDate(), null, null, allowedToChangeCreditor, allowedToChangeDebtor, false, false, null, null);
    }

    public InMemoSharkBond(CharSequence bondID, CharSequence creditorID, CharSequence debtorID,
                           CharSequence unitDescription, int amount, long expirationDate,
                           byte[] debtorSignature, byte[] creditorSignature, boolean allowedToChangeCreditor,
                           boolean allowedToChangeDebtor, boolean bondIsAnnulledByCreditor, boolean bondIsAnnulledByDebtor,
                           CharSequence tempCreditorID, CharSequence tempDebtorID) {
        this.bondID = bondID;
        this.creditorID = creditorID;
        this.debtorID = debtorID;
        this.unitDescription = unitDescription;
        this.amount = amount;
        this.expirationDate = expirationDate;
        this.debtorSignature = debtorSignature;
        this.creditorSignature = creditorSignature;
        this.allowedToChangeCreditor = allowedToChangeCreditor;
        this.allowedToChangeDebtor = allowedToChangeDebtor;
        this.bondIsAnnulledByCreditor = bondIsAnnulledByCreditor;
        this.bondIsAnnulledByDebtor = bondIsAnnulledByDebtor;
        this.tempCreditorID = tempCreditorID;
        this.tempDebtorID = tempDebtorID;
    }

    @Override
    public CharSequence getBondID() {
        return this.bondID;
    }

    /**
     * @return debtor of this bond
     */
    @Override
    public CharSequence getDebtorID() {
        return this.debtorID;
    }

    @Override
    public byte[] getDebtorSignature() {
        return this.debtorSignature;
    }

    @Override
    public boolean allowedToChangeDebtor() {
        return this.allowedToChangeDebtor;
    }

    @Override
    public void setAllowedToChangeDebtor(boolean on) throws SharkCreditMoneyException {
        this.allowedToChangeDebtor = on;
    }

    /**
     * Set the debtor of this bond
     * @param debtorID
     */
    @Override
    public void setDebtorID(CharSequence debtorID) throws SharkCreditMoneyException {
        if (this.allowedToChangeDebtor) {
            this.debtorID = debtorID;
        } else {
            throw new SharkCreditMoneyException("Method not allowed. The current bond's debtor can't be changed");
        }
    }

    /**
     * @return creditor of this bond
     */
    @Override
    public CharSequence getCreditorID() {
        return this.creditorID;
    }

    @Override
    public CharSequence getTempDebtorID() {
        return this.tempDebtorID;
    }

    @Override
    public CharSequence getTempCreditorID() {
        return this.tempCreditorID;
    }

    @Override
    public byte[] getCreditorSignature() {
        return this.creditorSignature;
    }

    @Override
    public boolean getBondIsAnnulledByCreditor() {
        return this.bondIsAnnulledByCreditor;
    }

    @Override
    public boolean getBondIsAnnulledByDebtor() {
        return this.bondIsAnnulledByDebtor;
    }

    @Override
    public boolean allowedToChangeCreditor() {
        return this.allowedToChangeCreditor;
    }

    @Override
    public void setAllowedToChangeCreditor(boolean on) throws SharkCreditMoneyException {
        this.allowedToChangeCreditor = on;
    }

    /**
     * Set the creditor of this bond
     * @param creditorID
     */
    @Override
    public void setCreditorID(CharSequence creditorID) throws SharkCreditMoneyException {
        if (this.allowedToChangeCreditor) {
            this.creditorID = creditorID;
        } else {
            throw new SharkCreditMoneyException("Method not allowed. The current bond's creditor can't be changed");
        }
    }

    @Override
    public void setTempDebtorID(CharSequence debtorID) {
        this.tempDebtorID = debtorID;
    }

    @Override
    public void setTempCreditorID(CharSequence creditorID) {
        this.tempCreditorID = creditorID;
    }

    @Override
    public void setCreditorSignature(byte[] signature) {
        this.creditorSignature = signature;
    }

    @Override
    public void setDebtorSignature(byte[] signature) {
        this.debtorSignature = signature;
    }

    @Override
    public void setBondIsAnnulledByCreditor() {
        this.bondIsAnnulledByCreditor = true;
    }

    @Override
    public void setBondIsAnnulledByDebtor() {
        this.bondIsAnnulledByDebtor = true;
    }

    @Override
    public void resetBondState() {
        this.bondIsAnnulledByCreditor = false;
        this.bondIsAnnulledByDebtor = false;
        this.allowedToChangeCreditor = false;
        this.allowedToChangeDebtor = false;
        this.tempCreditorID = null;
        this.tempDebtorID = null;
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

    @Override
    public boolean isAnnulled() {
        return this.bondIsAnnulledByCreditor && this.bondIsAnnulledByDebtor;
    }

    @Override
    public boolean bondIsExpired() {
        Calendar until = Calendar.getInstance();
        return this.expirationDate <= until.getTimeInMillis();
    }

    @Override
    public void extendCreditBondValidity() {
        //TODO: extend bond's validity to one year.
        this.setExpirationDate(this.expirationDate);
    }

    @Override
    public String toString() {
        return "{" +
                ", \"bondID\"=\"" + bondID + "\""+
                ", \"creditorID\"=\"" + creditorID + "\""+
                ", \"debtorID\"=\"" + debtorID + "\""+
                ", \"unitDescription\"=\"" + unitDescription + "\""+
                ", \"amount\"=" + amount +
                ",\"expirationDate\"=" + expirationDate +
                ", \"allowedToChangeDebtor\"=" + allowedToChangeDebtor +
                ", \"allowedToChangeCreditor\"=" + allowedToChangeCreditor +
                '}';
    }

    @Override
    public void setBondAsExpired() {
        Calendar until = Calendar.getInstance();
        this.expirationDate = until.getTimeInMillis();
    }

    private void setExpirationDate(long creationDate) {
        Calendar until = Calendar.getInstance();
        until.setTimeInMillis(creationDate);
        until.add(Calendar.YEAR, DEFAULT_CREDIT_BOND_VALIDITY_IN_YEARS);
        this.expirationDate = until.getTimeInMillis();
    }

    private static long getDefaultExpirationDate() {
        Calendar until = Calendar.getInstance();
        return until.getTimeInMillis();
    }

    private static CharSequence generateBondID() {
        UUID uuid = UUID.randomUUID();
        return uuid.toString();
    }
}
