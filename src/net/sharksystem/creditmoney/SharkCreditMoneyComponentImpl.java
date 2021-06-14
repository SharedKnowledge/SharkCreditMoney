package net.sharksystem.creditmoney;

import net.sharksystem.SharkException;
import net.sharksystem.SharkUnknownBehaviourException;
import net.sharksystem.asap.*;
import net.sharksystem.pki.SharkPKIComponent;
import net.sharksystem.utils.Log;

import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.*;

public class SharkCreditMoneyComponentImpl extends SharkBondReceivedListenerManager implements SharkCreditMoneyComponent, ASAPMessageReceivedListener {
    private final SharkPKIComponent certificateComponent;
    private ASAPPeer asapPeer;
    private SharkBondsReceivedListener sharkBondReceivedListener;
    private SharkBondStorage sharkBondStorage;
    private boolean allowTransfer = true;


    public SharkCreditMoneyComponentImpl(SharkPKIComponent certificateComponent) {
        this.certificateComponent = certificateComponent;
    }

    @Override
    public void onStart(ASAPPeer asapPeer) throws SharkException {
        this.sharkBondStorage = new SharkBondStorageImpl();
        this.asapPeer = asapPeer;
        Log.writeLog(this, "MAKE URI LISTENER PUBLIC AGAIN. Thank you :)");
        this.asapPeer.addASAPMessageReceivedListener(SharkCreditMoneyComponent.SHARK_CREDIT_MONEY_FORMAT, this);
    }

    @Override
    public void setBehaviour(String behaviour, boolean on) throws SharkUnknownBehaviourException {
        /* no component specific behaviour here. */
        switch (behaviour) {
            case SharkCreditMoneyComponent.BEHAVIOUR_SHARK_MONEY_ALLOW_TRANSFER:
                this.allowTransfer = on; break;

            default: throw new SharkUnknownBehaviourException(behaviour);
        }
    }

    @Override
    public void subscribeBondReceivedListener(SharkBondsReceivedListener listener) {
        // TODO just one listener. OK?
        this.sharkBondReceivedListener = listener;
        this.addSharkBondReceivedListener(listener);
    }

    @Override
    public void asapMessagesReceived(ASAPMessages asapMessages) throws IOException {
        try {
            CharSequence uri = asapMessages.getURI();
            this.notifySharkBondReceivedListener(uri);
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void createBond(CharSequence creditorID, CharSequence debtorID, CharSequence unit, int amount, boolean asCreditor) throws SharkCreditMoneyException, ASAPException, IOException {
        // Create creditBond and ask to sign by debtor
        InMemoSharkBond creditBond = new InMemoSharkBond(creditorID, debtorID, unit, amount, allowTransfer);
        Set<CharSequence> receiver = new HashSet<>();
        if (asCreditor) {
            SharkBondHelper.signAsCreditor(this.certificateComponent, creditBond);
            receiver.add(creditBond.getDebtorID());
            this.sendBond(creditBond, creditorID, receiver, true, true, SHARK_CREDIT_MONEY_ASKED_TO_SIGN_AS_DEBTOR_URI);
        } else {
            SharkBondHelper.signAsDebtor(this.certificateComponent, creditBond);
            receiver.add(creditBond.getCreditorID());
            this.sendBond(creditBond, debtorID, receiver, true, true, SHARK_CREDIT_MONEY_ASKED_TO_SIGN_AS_CREDITOR_URI);
        }
    }

    @Override
    public void replaceDebtor(SharkBond bond) throws SharkCreditMoneyException, ASAPException, IOException {
        InMemoSharkBond creditBond = (InMemoSharkBond) bond;
        creditBond.setDebtorID(this.asapPeer.getPeerID());
        SharkBondHelper.signAsDebtor(this.certificateComponent, creditBond);
        Set<CharSequence> receiver = new HashSet<>();
        receiver.add(creditBond.getCreditorID());
        this.sendBond(creditBond, creditBond.getDebtorID(), receiver, true, true, SHARK_CREDIT_MONEY_ASKED_TO_SIGN_AS_DEBTOR_URI);
    }

    @Override
    public void replaceCreditor(SharkBond bond) throws SharkCreditMoneyException, ASAPException, IOException {
        InMemoSharkBond creditBond = (InMemoSharkBond) bond;
        creditBond.setCreditorID(this.asapPeer.getPeerID());
        SharkBondHelper.signAsCreditor(this.certificateComponent, creditBond);
        Set<CharSequence> receiver = new HashSet<>();
        receiver.add(creditBond.getDebtorID());
        this.sendBond(creditBond, creditBond.getCreditorID(), receiver, true, true, SHARK_CREDIT_MONEY_ASKED_TO_SIGN_AS_CREDITOR_URI);
    }

    @Override
    public void sendBond(SharkBond bond, CharSequence sender, Set<CharSequence> receiver, boolean sign, boolean encrypt, CharSequence uri) throws IOException, ASAPException, SharkCreditMoneyException {
        byte[] serializedBond = SharkBondSerializer.serializeCreditBond(bond, sender, receiver, sign, encrypt, this.certificateComponent, false);
        this.asapPeer.sendASAPMessage(SharkCreditMoneyComponent.SHARK_CREDIT_MONEY_FORMAT, uri, serializedBond);
        this.sharkBondStorage.addOrUpdateSharkBond(bond);
    }

    @Override
    public void saveBond(SharkBond bond) throws SharkCreditMoneyException {
        this.sharkBondStorage.addOrUpdateSharkBond(bond);
    }

    @Override
    public Collection<SharkBond> getBondsByCreditor(CharSequence creditorID) throws SharkCreditMoneyException {
        return this.sharkBondStorage.getSharkBondsByCreditor(creditorID);
    }

    @Override
    public Collection<SharkBond> getBondsByDebtor(CharSequence debtorID) throws SharkCreditMoneyException {
        return this.sharkBondStorage.getSharkBondsByDebtor(debtorID);
    }

    @Override
    public Collection<SharkBond> getBondsByCreditorAndDebtor(CharSequence creditorID, CharSequence debtorID) throws SharkCreditMoneyException {
        return this.sharkBondStorage.getSharkBondsByCreditorAndDebtor(creditorID, debtorID);
    }

    /////////////////////////////////////////////////////////////////////////////////////////////
    //                       backdoor - remove it when finished implementing                   //
    /////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public SharkPKIComponent getSharkPKI() {
        return this.certificateComponent;
    }

    @Override
    public ASAPStorage getASAPStorage() throws IOException, ASAPException {
        return this.asapPeer.getASAPStorage(SharkCreditMoneyComponent.SHARK_CREDIT_MONEY_FORMAT);
    }
}
