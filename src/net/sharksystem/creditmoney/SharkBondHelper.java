package net.sharksystem.creditmoney;

class SharkBondHelper {
    /**
     * Sign this bond as debtor
     * @param bond
     * @throws SharkCreditMoneyException
     */
    static void signAsDebtor(AdminSharkBond bond) throws SharkCreditMoneyException {
        // test: allowed to sign as debtor - is this peer debtor - do we have key from creditor etc. pp.
        // if not throw new SharkCreditMoneyException("reasons");

        // else sign
    }

    /**
     * Sign a bond as creditor
     * @param bond
     * @throws SharkCreditMoneyException
     */
    static void signAsCreditor(AdminSharkBond bond) throws SharkCreditMoneyException {
    }

    /**
     * asked to allow transfer of this bonds' creditor to another peer. Transfer peer is named in bond.
     * @param bond
     * @throws SharkCreditMoneyException
     */
    static void acceptTransferCreditor(AdminSharkBond bond) throws SharkCreditMoneyException {
    }

    /**
     * transfer request was accepted - maybe something has to be done. Has it?
     * @param bond
     * @throws SharkCreditMoneyException
     */
    static void acceptedTransferCreditor(AdminSharkBond bond) throws SharkCreditMoneyException {
    }

    /**
     * asked to allow transfer of this bonds' debtor to another peer
     * @param bond
     * @throws SharkCreditMoneyException
     */
    static void acceptTransferDebtor(AdminSharkBond bond) throws SharkCreditMoneyException {
    }

    /**
     * transfer request was accepted - maybe something has to be done. Has it?
     * @param bond
     * @throws SharkCreditMoneyException
     */
    static void acceptedTransferDebtor(AdminSharkBond bond) throws SharkCreditMoneyException {
    }

    /**
     * A transfer bond was created (in the video: transfer bond would be Clara ows Alice). This transfer bond is
     * part of the original bond. Asked to sign as debtor (Clara in the video)
     * @param bond
     * @throws SharkCreditMoneyException
     */
    static void signTransferBondAsDebtor(AdminSharkBond bond) throws SharkCreditMoneyException {
    }

    /**
     * transfer bond signed. do we need this method?
     * @param bond
     * @throws SharkCreditMoneyException
     */
    static void signedTransferBondAsDebtor(AdminSharkBond bond) throws SharkCreditMoneyException {
    }

    /**
     * A transfer bond was created (in the video: transfer bond would be Clara ows Alice). This transfer bond is
     * part of the original bond. Asked to sign it as creditor (Alice in the video)
     * @param bond
     * @throws SharkCreditMoneyException
     */
    static void signTransferBondAsCreditor(AdminSharkBond bond) throws SharkCreditMoneyException {
    }

    /**
     * In video: Bob received a signed transfer bond from Alice
      * @param bond
     * @throws SharkCreditMoneyException
     */
    static void signedTransferBondAsCreditor(AdminSharkBond bond) throws SharkCreditMoneyException {
    }

    static void finalizeTransferAsCreditor(AdminSharkBond bond) throws SharkCreditMoneyException {
    }

    static void finalizedTransferAsCreditor(AdminSharkBond bond) throws SharkCreditMoneyException {
    }
}
