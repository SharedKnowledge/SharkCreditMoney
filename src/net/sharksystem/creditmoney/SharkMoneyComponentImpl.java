package net.sharksystem.creditmoney;

import net.sharksystem.SharkException;
import net.sharksystem.SharkUnknownBehaviourException;
import net.sharksystem.asap.*;
import net.sharksystem.pki.SharkPKIComponent;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

public class SharkMoneyComponentImpl implements SharkCreditMoneyComponent, SharkBondReceivedListener, ASAPMessageReceivedListener {
    private final SharkPKIComponent certificateComponent;private ASAPPeer asapPeer;
    private SharkBondReceivedListener sharkBondReceivedListener;

    private boolean allowTransfer = true;

    public SharkMoneyComponentImpl(SharkPKIComponent certificateComponent) {
        this.certificateComponent = certificateComponent;
    }

    @Override
    public void createBond(CharSequence creditorID, CharSequence debtorID, CharSequence unit, int amount) throws SharkCreditMoneyException, ASAPException, IOException {
        // Create creditBond and ask to sign by debtor
        InMemoSharkBond creditBond = new InMemoSharkBond(creditorID, debtorID, unit, amount, allowTransfer);
        SharkBondHelper.signAsCreditor(this.certificateComponent, creditBond);
        this.asapPeer.sendASAPMessage(SHARK_CREDIT_MONEY_FORMAT, SHARK_CREDIT_MONEY_ASKED_TO_SIGN_AS_DEBTOR_URI, SharkBondSerializer.serializeCreditBond(creditBond));
    }

    @Override
    public void replaceDebtor(SharkBond bond, CharSequence newDebtor) throws SharkCreditMoneyException, ASAPException, IOException {
        InMemoSharkBond creditBond = (InMemoSharkBond) bond;
        creditBond.setDebtorID(newDebtor);
        SharkBondHelper.signAsDebtor(this.certificateComponent, creditBond);
        this.asapPeer.sendASAPMessage(SHARK_CREDIT_MONEY_FORMAT, SHARK_CREDIT_MONEY_ASKED_TO_SIGN_AS_CREDITOR_URI, SharkBondSerializer.serializeCreditBond(creditBond));
    }

    @Override
    public void replaceCreditor(SharkBond bond, CharSequence newCreditor) throws SharkCreditMoneyException, ASAPException, IOException {
        InMemoSharkBond creditBond = (InMemoSharkBond) bond;
        creditBond.setCreditorID(newCreditor);
        SharkBondHelper.signAsCreditor(this.certificateComponent, creditBond);
        this.asapPeer.sendASAPMessage(SHARK_CREDIT_MONEY_FORMAT, SHARK_CREDIT_MONEY_ANNUL_BOND_URI, SharkBondSerializer.serializeCreditBond(creditBond));
    }

    @Override
    public Collection<SharkBond> getBondsByCreditor(CharSequence creditorID) throws SharkCreditMoneyException {
        try {
            ASAPStorage asapStorage =
                    this.asapPeer.getASAPStorage(SharkCreditMoneyComponent.SHARK_CREDIT_MONEY_FORMAT);

            ASAPMessages asapMessages = asapStorage.getChannel(SHARK_CREDIT_MONEY_SIGNED_BOND_URI).getMessages();
            Collection<SharkBond> bonds = new ArrayList<>();
            Iterator<byte[]> bondIterator = asapMessages.getMessages();
            while (bondIterator.hasNext()) {
                byte[] serializedBond = bondIterator.next();
                InMemoSharkBond creditBond = (InMemoSharkBond) SharkBondSerializer.deserializeCreditBond(serializedBond);
                if (creditBond.getCreditorID().equals(creditorID)) {
                    bonds.add(creditBond);
                }
            }
            return bonds;
        }
        catch(ASAPException | IOException asapException) {
            throw new SharkCreditMoneyException(asapException);
        }
    }

    @Override
    public Collection<SharkBond> getBondsByDebtor(CharSequence debtorID) throws SharkCreditMoneyException {
        try {
            ASAPStorage asapStorage =
                    this.asapPeer.getASAPStorage(SharkCreditMoneyComponent.SHARK_CREDIT_MONEY_FORMAT);

            ASAPMessages asapMessages = asapStorage.getChannel(SHARK_CREDIT_MONEY_SIGNED_BOND_URI).getMessages();
            Collection<SharkBond> bonds = new ArrayList<>();
            Iterator<byte[]> bondIterator = asapMessages.getMessages();
            while (bondIterator.hasNext()) {
                byte[] serializedBond = bondIterator.next();
                InMemoSharkBond creditBond = (InMemoSharkBond) SharkBondSerializer.deserializeCreditBond(serializedBond);
                if (creditBond.getDebtorID().equals(debtorID)) {
                    bonds.add(creditBond);
                }
            }
            return bonds;
        }
        catch(ASAPException | IOException asapException) {
            throw new SharkCreditMoneyException(asapException);
        }
    }

    @Override
    public Collection<SharkBond> getBondsByCreditorAndDebtor(CharSequence creditorID, CharSequence debtorID) throws SharkCreditMoneyException {
        try {
            ASAPStorage asapStorage =
                    this.asapPeer.getASAPStorage(SharkCreditMoneyComponent.SHARK_CREDIT_MONEY_FORMAT);

            ASAPMessages asapMessages = asapStorage.getChannel(SHARK_CREDIT_MONEY_SIGNED_BOND_URI).getMessages();
            Collection<SharkBond> bonds = new ArrayList<>();
            Iterator<byte[]> bondIterator = asapMessages.getMessages();
            while (bondIterator.hasNext()) {
                byte[] serializedBond = bondIterator.next();
                InMemoSharkBond creditBond = (InMemoSharkBond) SharkBondSerializer.deserializeCreditBond(serializedBond);
                if (creditBond.getCreditorID().equals(creditorID) && creditBond.getDebtorID().equals(debtorID)) {
                    bonds.add(creditBond);
                }
            }
            return bonds;
        }
        catch(ASAPException | IOException asapException) {
            throw new SharkCreditMoneyException(asapException);
        }
    }

    @Override
    public void subscribeBondReceivedListener(SharkBondReceivedListener listener) {
        // TODO just one listener. OK?
        this.sharkBondReceivedListener = listener;
    }

    @Override
    public void annulBond(SharkBond bond) throws SharkCreditMoneyException, ASAPException {
        InMemoSharkBond creditBond = (InMemoSharkBond) bond;
        creditBond.annulBond();
        this.asapPeer.sendASAPMessage(SHARK_CREDIT_MONEY_FORMAT, SHARK_CREDIT_MONEY_ASKED_TO_SIGN_AS_CREDITOR_URI, SharkBondSerializer.serializeCreditBond(creditBond));
    }

    @Override
    public void onStart(ASAPPeer asapPeer) throws SharkException {
        this.asapPeer = asapPeer;
        this.asapPeer.addASAPMessageReceivedListener(
                SharkCreditMoneyComponent.SHARK_CREDIT_MONEY_FORMAT, this);
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
    public void asapMessagesReceived(ASAPMessages asapMessages) throws IOException {
        SharkBond bond = null;

        try {
            bond = SharkBondSerializer.deserializeCreditBond(asapMessages.getMessage(0, true));
            this.sharkBondReceivedListener.sharkBondReceived(bond, asapMessages.getURI());
        } catch (ASAPException | SharkCreditMoneyException | NullPointerException e) {
            e.printStackTrace();
        }
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////////
    //                                          act on received bonds                                       //
    //////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void sharkBondReceived(SharkBond bond, CharSequence uri) throws ASAPException, IOException, SharkCreditMoneyException {
        InMemoSharkBond creditBond = (InMemoSharkBond) bond;
        switch(uri.toString()) {
            case SharkCreditMoneyComponent.SHARK_CREDIT_MONEY_ASKED_TO_SIGN_AS_CREDITOR_URI:
                SharkBondHelper.signAsCreditor(this.certificateComponent, creditBond);
                this.asapPeer.sendASAPMessage(SHARK_CREDIT_MONEY_FORMAT, SHARK_CREDIT_MONEY_SIGNED_BOND_URI, SharkBondSerializer.serializeCreditBond(creditBond));
                break;
            case SharkCreditMoneyComponent.SHARK_CREDIT_MONEY_ASKED_TO_SIGN_AS_DEBTOR_URI:
                SharkBondHelper.signAsDebtor(this.certificateComponent, creditBond);
                this.asapPeer.sendASAPMessage(SHARK_CREDIT_MONEY_FORMAT, SHARK_CREDIT_MONEY_SIGNED_BOND_URI, SharkBondSerializer.serializeCreditBond(creditBond));
                break;
            case SharkCreditMoneyComponent.SHARK_CREDIT_MONEY_SIGNED_BOND_URI:

                /* TODO */
                break;
            case SharkCreditMoneyComponent.SHARK_CREDIT_MONEY_ANNUL_BOND_URI:
                /* TODO */
                break;
            default: // unknown URI
        }
    }


    @Override
    public void requestSignAsCreditor(SharkBond bond) throws ASAPException {

    }

    @Override
    public void requestSignAsDebtor(SharkBond bond) throws ASAPException {

    }

    @Override
    public void requestChangeCreditor(SharkBond bond) throws ASAPException {

    }

    @Override
    public void requestChangeDebtor(SharkBond bond) throws ASAPException {

    }
}
