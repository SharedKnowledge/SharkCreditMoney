package net.sharksystem.creditmoney;

import java.io.IOException;

/**
 * Each bond is about a depth. It is assumed that this depth can be described as integer value with a unit.
 * A deptor owes the creditor this depth. Each bond is in one of those statuses:
 *
 * <ul>
 *     <li>created, not signed</li>>
 *     <li>signed by deptor but not creditor</li>>
 *     <li>signed by creditor but not deptor</li>>
 *     <li>signed by both</li>>
 *     <li>annulled</li>>
 * </ul>
 *
 * @author Thomas Schwotzer
 *
 *
 */
public interface SharkBond {
    int SIGNED_MASK = 0x1;
    int ENCRYPTED_MASK = 0x2;

    /**
     * There is an id that makes any bond unique.
     * @return unique permanent id of this bond.
     */
    CharSequence getBondID();

    /**
     * @return debtor of this bond
     */
    CharSequence getDebtorID();

    byte[] getDebtorSignature();

    boolean signedByDebtor();
    boolean allowedToChangeDebtor();
    void setAllowedToChangeDebtor(boolean on) throws SharkCreditMoneyException;

    void setDebtorID(CharSequence debtorID) throws SharkCreditMoneyException;

    /**
     * @return creditor of this bond
     */
    CharSequence getCreditorID();

    byte[] getCreditorSignature();

    boolean signedByCreditor();
    boolean allowedToChangeCreditor();
    void setAllowedToChangeCreditor(boolean on) throws SharkCreditMoneyException;

    void setCreditorID(CharSequence creditorID) throws SharkCreditMoneyException;

    void setCreditorSignature(byte[] signature);

    void setDebtorSignature(byte[] signature);

    /**
     * A bond defines a kind of depth. Each bond can have its own unit, e.g. bar of gold-pressed latinum, a favour,
     * a cigarette was a currency after WW2, etc. pp.
     * @return unit you defined
     */
    CharSequence unitDescription();
    void setUnitDescription(CharSequence description);

    /**
     *
     * @return amounts of whatever unit you defined.
     */
    int getAmount();

    long getExpirationDate();
    boolean isAnnulled();

    void extendCreditBondValidity();

    void annulBond();
}