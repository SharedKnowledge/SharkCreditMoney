package net.sharksystem.creditmoney;

import net.sharksystem.asap.ASAPSecurityException;
import net.sharksystem.asap.crypto.ASAPKeyStore;
import net.sharksystem.asap.persons.Person;

public interface SharkCreditBond {
    /**
     * @return debtor of this bond
     */
    Person getDebtor();
    byte[] getDebtorSignature();
    boolean signedByDebtor();
    boolean allowedToChangeDebtor();

    /**
     * @return creditor of this bond
     */
    Person getCreditor();
    byte[] getCreditorSignature();
    boolean signedByCreditor();
    boolean allowedToChangeCreditor();

    /**
     * Set the creditor of this bond
     * @param creditor
     */
    void setCreditor(Person creditor) throws SharkCreditMoneyException;

    /**
     * Set the debtor of this bond
     * @param debtor
     */
    void setDebtor(Person debtor) throws SharkCreditMoneyException;

    void setCreditorSignature(byte[] signature);

    void setDebtorSignature(byte[] signature);


    /**
     * A bond defines a kind of depth. Each bond can have its own unit, e.g. bar of gold-pressed latinum, a favour,
     * a cigarette was a currency after WW2, etc. pp.
     * @return unit you defined
     */
    CharSequence unitDescription();

    /**
     *
     * @return amounts of whatever unit you defined.
     */
    int getAmount();

    long getExpirationDate();

    void extendCreditBondValidity();

    void annulBond();
}