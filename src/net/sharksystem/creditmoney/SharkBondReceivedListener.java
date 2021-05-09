package net.sharksystem.creditmoney;

import net.sharksystem.asap.ASAPException;

import java.io.IOException;

public interface SharkBondReceivedListener {
    void sharkBondReceived(SharkBond bond, CharSequence uri) throws ASAPException, IOException, SharkCreditMoneyException;
    void requestSignAsCreditor(SharkBond bond) throws ASAPException;
    void requestSignAsDebtor(SharkBond bond) throws ASAPException;
    void requestChangeCreditor(SharkBond bond) throws ASAPException;
    void requestChangeDebtor(SharkBond bond) throws ASAPException;
}
