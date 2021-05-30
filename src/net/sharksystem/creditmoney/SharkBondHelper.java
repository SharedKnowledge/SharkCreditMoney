package net.sharksystem.creditmoney;

import net.sharksystem.asap.ASAPSecurityException;
import net.sharksystem.asap.crypto.ASAPCryptoAlgorithms;
import net.sharksystem.asap.crypto.ASAPKeyStore;
import net.sharksystem.asap.pki.ASAPKeyStorage;

import java.io.IOException;
import java.util.Calendar;

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
            throw new SharkCreditMoneyException("The provided keyStore owner (" + ASAPKeyStore.getOwner() + ") doesn't match the creditor's id (" + bond.getCreditorID() + ")");
        } else {
            bond.setCreditorSignature(signBond(ASAPKeyStore, bond));
        }
    }

    /**
     * asked to allow transfer of this bonds' creditor to another peer. Transfer peer is named in bond.
     * @param bond
     * @throws SharkCreditMoneyException
     */
    static void acceptTransferCreditor(ASAPKeyStore asapKeyStore, SharkBond bond) throws SharkCreditMoneyException, ASAPSecurityException, IOException {
        // Test: if the current peer is the creditor of the bond
        if (!asapKeyStore.getOwner().equals(bond.getCreditorID())) {
            throw new SharkCreditMoneyException("The provided keyStore owner (" + asapKeyStore.getOwner() + ") doesn't match the creditor's id (" + bond.getCreditorID() + ")");
        } else if (!isSignedAsCreditor(bond, asapKeyStore)){
            throw new SharkCreditMoneyException("The provided SharkBond doesn't contain a correct signature. The request will be rejected.");
        } else {
            // Set allowedToChangeCreditor to true
            bond.setAllowedToChangeCreditor(true);
            // then sign the bond again
            signAsCreditor(asapKeyStore, bond);
        }
    }

    /**
     * transfer request was accepted - maybe something has to be done. Has it?
     * @param bond
     * @throws SharkCreditMoneyException
     */
    static void acceptedTransferCreditor(ASAPKeyStore ASAPKeyStore, SharkBond bond) throws SharkCreditMoneyException {
        // Once the request to transfer a bond to a new creditor is accepted the new creditor will be recorded and the bond will be signed
        
    }

    /**
     * asked to allow transfer of this bonds' debtor to another peer
     * @param bond
     * @throws SharkCreditMoneyException
     */
    static void acceptTransferDebtor(ASAPKeyStore asapKeyStore, SharkBond bond) throws SharkCreditMoneyException, ASAPSecurityException, IOException {
        // Test: if the current peer is the debtor of the bond
        if (!asapKeyStore.getOwner().equals(bond.getDebtorID())) {
            throw new SharkCreditMoneyException("The provided keyStore owner (" + asapKeyStore.getOwner() + ") doesn't match the debtor's id (" + bond.getDebtorID() + ")");
        } else if (!isSignedAsDebtor(bond, asapKeyStore)){
            throw new SharkCreditMoneyException("The provided SharkBond doesn't contain a correct signature. The request will be rejected.");
        } else {
            // Set allowedToChangeDebtor to true
            bond.setAllowedToChangeDebtor(true);
            // then sign the bond again
            signAsCreditor(asapKeyStore, bond);
        }
    }

    /**
     * transfer request was accepted - maybe something has to be done. Has it?
     * @param bond
     * @throws SharkCreditMoneyException
     */
    static void acceptedTransferDebtor(ASAPKeyStore ASAPKeyStore, SharkBond bond) throws SharkCreditMoneyException {
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

    static void annulBond(SharkBond bond) {
        bond.setBondAsExpired();
    }

    static boolean isSignedAsCreditor(SharkBond creditBond, ASAPKeyStore asapKeyStore) throws ASAPSecurityException {
        return creditBond.getCreditorSignature() != null && isSignatureCorrect(SharkBondSerializer.sharkBondToByteArray(creditBond, true), creditBond.getCreditorSignature(), asapKeyStore);
    }

    static boolean isSignedAsDebtor(SharkBond creditBond, ASAPKeyStore asapKeyStore) throws ASAPSecurityException {
        return creditBond.getDebtorSignature() != null && isSignatureCorrect(SharkBondSerializer.sharkBondToByteArray(creditBond, true), creditBond.getDebtorSignature(), asapKeyStore);
    }

    static boolean isSignatureCorrect(byte[] creditBond, byte[] signature, ASAPKeyStore asapKeyStore) throws ASAPSecurityException {
        return ASAPCryptoAlgorithms.verify(creditBond, signature, asapKeyStore.getOwner().toString(), asapKeyStore);
    }

    private static byte[] signBond(ASAPKeyStore ASAPKeyStore, SharkBond Bond) throws ASAPSecurityException, IOException {
        return ASAPCryptoAlgorithms.sign(SharkBondSerializer.serializeCreditBond(Bond, ASAPKeyStore), ASAPKeyStore);
    }
}
