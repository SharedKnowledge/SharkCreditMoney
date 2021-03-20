package net.sharksystem.creditmoney;

import net.sharksystem.asap.persons.Person;

public class PersonImpl implements Person {
    private final CharSequence personID;
    private CharSequence displayName;

    public PersonImpl(CharSequence personID) {
        this.personID = personID;
    }

    public PersonImpl(CharSequence personID, CharSequence displayName) {
        this.personID = personID;
        this.displayName = displayName;
    }

    @Override
    public CharSequence getDisplayName() {
        return this.displayName;
    }

    @Override
    public void setDisplayName(CharSequence displayName) {
        this.displayName = displayName;
    }

    @Override
    public CharSequence getUUID() {
        return this.personID;
    }
}
