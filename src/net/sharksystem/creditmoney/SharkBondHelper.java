package net.sharksystem.creditmoney;

import net.sharksystem.asap.ASAPSecurityException;
import net.sharksystem.asap.crypto.ASAPCryptoAlgorithms;
import net.sharksystem.asap.crypto.ASAPKeyStore;
import java.io.IOException;

class SharkBondHelper {
    /**
     * Sign this bond as debtor
     * @param bond
     * @throws SharkCreditMoneyException
     */
    static void signAsDebtor(ASAPKeyStore ASAPKeyStore, SharkBond bond) throws SharkCreditMoneyException, IOException, ASAPSecurityException {
        // test: allowed to sign as debtor - is this peer debtor - do we have key from creditor etc. pp.
        // if not throw new SharkCreditMoneyException("reasons");
        if (!ASAPKeyStore.getOwner().equals(bond.getDebtorID())) {
            throw new SharkCreditMoneyException("The provided keyStore owner (" + ASAPKeyStore.getOwner() + ") doesn't match the debtor's id (" + bond.getDebtorID() + ")");
        } else {
            bond.setDebtorSignature(signBond(ASAPKeyStore, bond));
        }
    }

    /**
     * Sign a bond as creditor
     * @param bond
     * @throws SharkCreditMoneyException
     */
    static void signAsCreditor(ASAPKeyStore ASAPKeyStore, SharkBond bond) throws SharkCreditMoneyException, IOException, ASAPSecurityException {
        // test: allowed to sign as creditor - is this peer creditor - do we have key from debtor etc. pp.
        // if not throw new SharkCreditMoneyException("reasons");
        if (!ASAPKeyStore.getOwner().equals(bond.getCreditorID())) {
            throw new SharkCreditMoneyException("The provided keyStore owner (" + ASAPKeyStore.getOwner() + ") doesn't match the creditor's id (" + bond.getDebtorID() + ")");
        } else {
            bond.setCreditorSignature(signBond(ASAPKeyStore, bond));
        }
    }

    /**
     * asked to allow transfer of this bonds' creditor to another peer. Transfer peer is named in bond.
     * @param bond
     * @throws SharkCreditMoneyException
     */
    static void acceptTransferCreditor(SharkBond bond) throws SharkCreditMoneyException {
    }

    /**
     * transfer request was accepted - maybe something has to be done. Has it?
     * @param bond
     * @throws SharkCreditMoneyException
     */
    static void acceptedTransferCreditor(SharkBond bond) throws SharkCreditMoneyException {
    }

    /**
     * asked to allow transfer of this bonds' debtor to another peer
     * @param bond
     * @throws SharkCreditMoneyException
     */
    static void acceptTransferDebtor(SharkBond bond) throws SharkCreditMoneyException {
    }

    /**
     * transfer request was accepted - maybe something has to be done. Has it?
     * @param bond
     * @throws SharkCreditMoneyException
     */
    static void acceptedTransferDebtor(SharkBond bond) throws SharkCreditMoneyException {
    }

    /**
     * A transfer bond was created (in the video: transfer bond would be Clara ows Alice). This transfer bond is
     * part of the original bond. Asked to sign as debtor (Clara in the video)
     * @param bond
     * @throws SharkCreditMoneyException
     */
    static void signTransferBondAsDebtor(SharkBond bond) throws SharkCreditMoneyException {
    }

    /**
     * transfer bond signed. do we need this method?
     * @param bond
     * @throws SharkCreditMoneyException
     */
    static void signedTransferBondAsDebtor(SharkBond bond) throws SharkCreditMoneyException {
    }

    /**
     * A transfer bond was created (in the video: transfer bond would be Clara ows Alice). This transfer bond is
     * part of the original bond. Asked to sign it as creditor (Alice in the video)
     * @param bond
     * @throws SharkCreditMoneyException
     */
    static void signTransferBondAsCreditor(SharkBond bond) throws SharkCreditMoneyException {
    }

    /**
     * In video: Bob received a signed transfer bond from Alice
      * @param bond
     * @throws SharkCreditMoneyException
     */
    static void signedTransferBondAsCreditor(SharkBond bond) throws SharkCreditMoneyException {
    }

    static void finalizeTransferAsCreditor(SharkBond bond) throws SharkCreditMoneyException {
    }

    static void finalizedTransferAsCreditor(SharkBond bond) throws SharkCreditMoneyException {
    }

    private static byte[] signBond(ASAPKeyStore ASAPKeyStore, SharkBond Bond) throws ASAPSecurityException, IOException {
        return ASAPCryptoAlgorithms.sign(SharkBondSerializer.serializeCreditBond(Bond), ASAPKeyStore);
    }
}
