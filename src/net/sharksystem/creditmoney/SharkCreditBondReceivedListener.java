package net.sharksystem.creditmoney;

import net.sharksystem.asap.ASAPException;
import net.sharksystem.asap.ASAPSecurityException;

public interface SharkCreditBondReceivedListener {
    void sharkCreditBondReceived(SharkCreditBond bond, CharSequence uri) throws ASAPException;
}
