package net.sharksystem.creditmoney;

import net.sharksystem.asap.ASAPSecurityException;
import net.sharksystem.asap.crypto.ASAPCryptoAlgorithms;
import net.sharksystem.asap.crypto.ASAPKeyStore;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

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
            bond.setDebtorSignature(signBond(ASAPKeyStore, bond, false));
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
            bond.setCreditorSignature(signBond(ASAPKeyStore, bond, true));
        }
    }

    /**
     * asked to allow transfer of this bonds' creditor to another peer. Transfer peer is named in bond.
     * @param bond
     * @throws SharkCreditMoneyException
     */
    static void acceptTransferCreditor(ASAPKeyStore asapKeyStore, SharkBond bond) throws SharkCreditMoneyException, ASAPSecurityException, IOException {
        // Test: if the current peer is the debtor of the bond
        if (!asapKeyStore.getOwner().equals(bond.getDebtorID())) {
            throw new SharkCreditMoneyException("The provided keyStore owner (" + asapKeyStore.getOwner() + ") doesn't match the debtor's id (" + bond.getDebtorID() + ")");
        } else if (!isSignedAsDebtor(bond, asapKeyStore)){
            throw new SharkCreditMoneyException("The provided SharkBond doesn't contain a correct debtor signature. The request will be rejected.");
        } else if (!isSignedAsCreditor(bond, asapKeyStore)) {
            throw new SharkCreditMoneyException("The provided SharkBond doesn't contain a correct creditor signature. The request will be rejected.");
        } else {
            // Set allowedToChangeCreditor to true
            bond.setAllowedToChangeCreditor(true);
            // then sign the bond again
            signAsDebtor(asapKeyStore, bond);
        }
    }

    /**
     * transfer request was accepted - maybe something has to be done. Has it?
     * @param bond
     * @throws SharkCreditMoneyException
     */
    static void acceptedTransferCreditor(ASAPKeyStore asapKeyStore, SharkBond bond) throws SharkCreditMoneyException, IOException, ASAPSecurityException {
        // First make sure that the bond transfer is allowed and the verify the debtor signature
        if (!bond.allowedToChangeCreditor()) {
            throw new SharkCreditMoneyException("The transfer of the shark bond wasn't allowed by the current debtor.");
        } else if (!isSignedAsDebtor(bond, asapKeyStore)) {
            throw new SharkCreditMoneyException("The provided SharkBond doesn't contain a correct debtor signature. The request will be rejected.");
        } else {
            // Save old creditor
            CharSequence oldCreditor = bond.getCreditorID();
            // Once the request to transfer a bond to a new creditor is accepted the new creditor will be recorded and the bond will be signed
            bond.setCreditorID(bond.getTempCreditorID());
            // Save old debtor as TempDebtor
            bond.setTempCreditorID(oldCreditor);
            // Sign the new bond as old creditor
            signAsCreditor(asapKeyStore, bond);
        }
    }

    /**
     * asked to allow transfer of this bonds' debtor to another peer
     * @param bond
     * @throws SharkCreditMoneyException
     */
    static void acceptTransferDebtor(ASAPKeyStore asapKeyStore, SharkBond bond) throws SharkCreditMoneyException, ASAPSecurityException, IOException {
        // Test: if the current peer is the creditor of the bond
        if (!asapKeyStore.getOwner().equals(bond.getCreditorID())) {
            throw new SharkCreditMoneyException("The provided keyStore owner (" + asapKeyStore.getOwner() + ") doesn't match the debtor's id (" + bond.getDebtorID() + ")");
        } else if (!isSignedAsCreditor(bond, asapKeyStore)) {
            throw new SharkCreditMoneyException("The provided SharkBond doesn't contain a correct creditor signature. The request will be rejected.");
        } else if (!isSignedAsDebtor(bond, asapKeyStore)) {
            throw new SharkCreditMoneyException("The provided SharkBond doesn't contain a correct debtor signature. The request will be rejected.");
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
    static void acceptedTransferDebtor(ASAPKeyStore asapKeyStore, SharkBond bond) throws SharkCreditMoneyException, IOException, ASAPSecurityException {
        // First make sure that the bond transfer is allowed and the verify the debtor signature
        if (!bond.allowedToChangeDebtor()) {
            throw new SharkCreditMoneyException("The transfer of the shark bond wasn't allowed by the current debtor.");
        } else if (!isSignedAsCreditor(bond, asapKeyStore)) {
            throw new SharkCreditMoneyException("The provided SharkBond doesn't contain a correct creditor signature. The request will be rejected.");
        } else {
            // Save old debtor
            CharSequence oldDebtor = bond.getDebtorID();
            // Once the request to transfer a bond to a new debtor is accepted the new debtor will be recorded and the bond will be signed
            bond.setDebtorID(bond.getTempDebtorID());
            // Save old debtor as TempDebtor
            bond.setTempDebtorID(oldDebtor);
            // Sign the new bond as old debtor
            signAsDebtor(asapKeyStore, bond);
        }
    }

    /**
     * A transfer bond was created (in the video: transfer bond would be Clara ows Alice). This transfer bond is
     * part of the original bond. Asked to sign as debtor (Clara in the video)
     * @param bond
     * @throws SharkCreditMoneyException
     */
    static void signTransferBondAsDebtor(ASAPKeyStore asapKeyStore, SharkBond bond) throws SharkCreditMoneyException, IOException, ASAPSecurityException {
        // Test: if the current peer is the debtor of the bond
        if (!asapKeyStore.getOwner().equals(bond.getDebtorID())) {
            throw new SharkCreditMoneyException("The provided keyStore owner (" + asapKeyStore.getOwner() + ") doesn't match the debtor's id (" + bond.getDebtorID() + ")");
        } else if (!isSignedAsDebtor(bond, (String) bond.getTempDebtorID(), asapKeyStore)) {
            // Test: if the old debtor signature is correct
            throw new SharkCreditMoneyException("The provided SharkBond doesn't contain a correct signature of the old debtor. The request will be rejected.");
        } else {
            bond.setDebtorSignature(signBond(asapKeyStore, bond, false));
        }
    }

    /**
     * A transfer bond was created (in the video: transfer bond would be Clara ows Alice). This transfer bond is
     * part of the original bond. Asked to sign it as creditor (Alice in the video)
     * @param bond
     * @throws SharkCreditMoneyException
     */
    static void signTransferBondAsCreditor(ASAPKeyStore asapKeyStore, SharkBond bond) throws SharkCreditMoneyException, IOException, ASAPSecurityException {
        // Test: if the current peer is the creditor of the bond
        if (!asapKeyStore.getOwner().equals(bond.getCreditorID())) {
            throw new SharkCreditMoneyException("The provided keyStore owner (" + asapKeyStore.getOwner() + ") doesn't match the creditor's id (" + bond.getCreditorID() + ")");
        } else if (!isSignedAsCreditor(bond, (String) bond.getTempCreditorID(), asapKeyStore)) {
            // Test: if the old creditor signature is correct
            throw new SharkCreditMoneyException("The provided SharkBond doesn't contain a correct signature of the old creditor. The request will be rejected.");
        } else {
            bond.setCreditorSignature(signBond(asapKeyStore, bond, true));
        }
    }

    static void annulBond(ASAPKeyStore asapKeyStore, SharkBond bond) throws SharkCreditMoneyException, IOException, ASAPSecurityException {
        // Test: if the current peer is the creditor of the bond
        if (!asapKeyStore.getOwner().equals(bond.getCreditorID()) && !asapKeyStore.getOwner().equals(bond.getDebtorID())) {
            throw new SharkCreditMoneyException("The provided keyStore owner (" + asapKeyStore.getOwner() + ") doesn't match the creditor/debtor's id (" + bond.getCreditorID() + "/" + bond.getDebtorID() + ")");
        } else if (bond.isAnnulled()) {
            throw new SharkCreditMoneyException("The provided bond was already annulled. The request will be rejected");
        } else {
            bond.setBondAsExpired();
            // Sign the bond
            if (asapKeyStore.getOwner().equals(bond.getCreditorID())) {
                bond.setBondIsAnnulledByCreditor();
                bond.setCreditorSignature(signBond(asapKeyStore, bond, true));
            } else if (asapKeyStore.getOwner().equals(bond.getDebtorID())) {
                bond.setBondIsAnnulledByDebtor();
                bond.setDebtorSignature(signBond(asapKeyStore, bond, false));
            }
        }
    }

    static boolean isSignedAsCreditor(SharkBond creditBond, ASAPKeyStore asapKeyStore) throws ASAPSecurityException, IOException {
        String signer = (String) creditBond.getCreditorID();
        return isSignedAsCreditor(creditBond, signer, asapKeyStore);
    }

    static boolean isSignedAsCreditor(SharkBond creditBond, String signer, ASAPKeyStore asapKeyStore) throws ASAPSecurityException, IOException {
        Set<CharSequence> receiver = new HashSet<>();
        receiver.add(creditBond.getDebtorID());
        byte[] signature = creditBond.getCreditorSignature();
        byte[] creditBondBytes = SharkBondSerializer.serializeCreditBond(creditBond, creditBond.getCreditorID(), receiver, true, true, asapKeyStore, true, 2);
        return signature != null && isSignatureCorrect(creditBondBytes, signature, signer, asapKeyStore);
    }

    static boolean isSignedAsDebtor(SharkBond creditBond, ASAPKeyStore asapKeyStore) throws ASAPSecurityException, IOException {
        String signer = (String) creditBond.getDebtorID();
        return isSignedAsDebtor(creditBond, signer, asapKeyStore);
    }

    static boolean isSignedAsDebtor(SharkBond creditBond, String signer, ASAPKeyStore asapKeyStore) throws ASAPSecurityException, IOException {
        Set<CharSequence> receiver = new HashSet<>();
        receiver.add(creditBond.getCreditorID());
        byte[] signature = creditBond.getDebtorSignature();
        byte[] creditBondBytes = SharkBondSerializer.serializeCreditBond(creditBond, creditBond.getDebtorID(), receiver, true, true, asapKeyStore, true, 2);
        return signature != null && isSignatureCorrect(creditBondBytes, signature, signer, asapKeyStore);
    }

    static boolean isSignatureCorrect(byte[] creditBond, byte[] signature, String signer, ASAPKeyStore asapKeyStore) throws ASAPSecurityException {
        return ASAPCryptoAlgorithms.verify(creditBond, signature, signer, asapKeyStore);
    }

    private static byte[] signBond(ASAPKeyStore asapKeyStore, SharkBond bond, boolean signAsCreditor) throws ASAPSecurityException, IOException {
        Set<CharSequence> receiver = new HashSet<>();
        CharSequence sender;
        if (signAsCreditor) {
            sender = bond.getCreditorID();
            receiver.add(bond.getDebtorID());
        } else {
            sender = bond.getDebtorID();
            receiver.add(bond.getCreditorID());
        }
        return SharkBondSerializer.serializeCreditBond(bond, sender, receiver, true, true, asapKeyStore, true, 1);
    }
}
