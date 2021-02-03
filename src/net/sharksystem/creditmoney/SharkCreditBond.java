package net.sharksystem.creditmoney;

import net.sharksystem.asap.persons.Person;

public interface SharkCreditBond {
    /**
     * @return debtor of this bond
     */
    Person getDebtor();

    boolean signedByDebtor();
    boolean allowedToChangeDebtor();

    /**
     * @return creditor of this bond
     */
    Person getCreditor();

    boolean signedByCreditor();
    boolean allowedToChangeCreditor();

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
}
