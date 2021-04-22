package net.sharksystem.creditmoney;

import net.sharksystem.asap.ASAPException;

public interface SharkBondReceivedListener {
    // TODO

    void requestSignAsCreditor(SharkBond bond) throws ASAPException;
    void requestSignAsDebtor(SharkBond bond) throws ASAPException;
    void requestChangeCreditor(SharkBond bond) throws ASAPException;
    void requestChangeDebtor(SharkBond bond) throws ASAPException;
}
