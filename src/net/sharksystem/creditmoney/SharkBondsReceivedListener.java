package net.sharksystem.creditmoney;

import net.sharksystem.asap.ASAPException;

import java.io.IOException;

public interface SharkBondsReceivedListener {
    void sharkBondReceived(CharSequence uri) throws ASAPException, IOException, SharkCreditMoneyException;
    void requestSignAsCreditor(SharkBond bond) throws ASAPException, IOException, SharkCreditMoneyException;
    void requestSignAsDebtor(SharkBond bond) throws ASAPException, IOException, SharkCreditMoneyException;
    void requestChangeCreditor(SharkBond bond) throws ASAPException;
    void requestChangeDebtor(SharkBond bond) throws ASAPException;
    void annulBond(SharkBond bond) throws SharkCreditMoneyException, ASAPException, IOException;
}
