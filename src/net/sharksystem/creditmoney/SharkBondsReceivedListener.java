package net.sharksystem.creditmoney;

import net.sharksystem.asap.ASAPException;

import java.io.IOException;

public interface SharkBondsReceivedListener {
    void sharkBondReceived(CharSequence uri) throws ASAPException, IOException, SharkCreditMoneyException;
}
