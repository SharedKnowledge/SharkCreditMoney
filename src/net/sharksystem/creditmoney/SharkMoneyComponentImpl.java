package net.sharksystem.creditmoney;

import net.sharksystem.SharkException;
import net.sharksystem.SharkUnknownBehaviourException;
import net.sharksystem.asap.*;
import net.sharksystem.pki.SharkPKIComponent;
import net.sharksystem.utils.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

public class SharkMoneyComponentImpl extends SharkBondReceivedListenerManager implements SharkCreditMoneyComponent, ASAPMessageReceivedListener {
    private static final String KEY_NAME_SHARK_MESSENGER_CHANNEL_NAME = "sharkMoneyChannelName";
    private final SharkPKIComponent certificateComponent;
    private ASAPPeer asapPeer;
    private SharkBondsReceivedListener sharkBondReceivedListener;

    private boolean allowTransfer = true;

    public SharkMoneyComponentImpl(SharkPKIComponent certificateComponent) {
        this.certificateComponent = certificateComponent;
    }

    @Override
    public void onStart(ASAPPeer asapPeer) throws SharkException {
        this.asapPeer = asapPeer;
        Log.writeLog(this, "MAKE URI LISTENER PUBLIC AGAIN. Thank you :)");
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
    public void createChannel(CharSequence uri, CharSequence name) throws IOException, SharkCreditMoneyException {
        try {
            ASAPStorage asapStorage =
                    this.asapPeer.getASAPStorage(SharkCreditMoneyComponent.SHARK_CREDIT_MONEY_FORMAT);

            asapStorage.createChannel(uri);
            asapStorage.putExtra(uri, KEY_NAME_SHARK_MESSENGER_CHANNEL_NAME, name.toString());
        }
        catch(ASAPException asapException) {
            throw new SharkCreditMoneyException(asapException);
        }
    }

    @Override
    public void removeChannel(CharSequence uri) throws SharkCreditMoneyException {
        Log.writeLog(this, "removeChannel", "not yet implemented");
        throw new SharkCreditMoneyException("not yet implemented");
    }

    @Override
    public void removeAllChannels() throws SharkCreditMoneyException {
        Log.writeLog(this, "removeChannel", "not yet implemented");
        throw new SharkCreditMoneyException("not yet implemented");
    }

    @Override
    public void createBond(CharSequence creditorID, CharSequence debtorID, CharSequence unit, int amount) throws SharkCreditMoneyException, ASAPException, IOException {
        // Create creditBond and ask to sign by debtor
        InMemoSharkBond creditBond = new InMemoSharkBond(creditorID, debtorID, unit, amount, allowTransfer);
        SharkBondHelper.signAsCreditor(this.certificateComponent, creditBond);
        this.asapPeer.sendASAPMessage(SHARK_CREDIT_MONEY_FORMAT, SHARK_CREDIT_MONEY_ASKED_TO_SIGN_AS_DEBTOR_URI, SharkBondSerializer.serializeCreditBond(creditBond, this.certificateComponent, false));
    }

    @Override
    public void replaceDebtor(SharkBond bond, CharSequence newDebtor) throws SharkCreditMoneyException, ASAPException, IOException {
        InMemoSharkBond creditBond = (InMemoSharkBond) bond;
        creditBond.setDebtorID(newDebtor);
        SharkBondHelper.signAsDebtor(this.certificateComponent, creditBond);
        this.asapPeer.sendASAPMessage(SHARK_CREDIT_MONEY_FORMAT, SHARK_CREDIT_MONEY_ASKED_TO_SIGN_AS_DEBTOR_URI, SharkBondSerializer.serializeCreditBond(creditBond, this.certificateComponent, false));
    }

    @Override
    public void replaceCreditor(SharkBond bond, CharSequence newCreditor) throws SharkCreditMoneyException, ASAPException, IOException {
        InMemoSharkBond creditBond = (InMemoSharkBond) bond;
        creditBond.setCreditorID(newCreditor);
        SharkBondHelper.signAsCreditor(this.certificateComponent, creditBond);
        this.asapPeer.sendASAPMessage(SHARK_CREDIT_MONEY_FORMAT, SHARK_CREDIT_MONEY_ASKED_TO_SIGN_AS_CREDITOR_URI, SharkBondSerializer.serializeCreditBond(creditBond, this.certificateComponent, false));
    }

    @Override
    public Collection<SharkBond> getBondsByCreditor(CharSequence creditorID) throws SharkCreditMoneyException {
        try {
            ASAPStorage asapStorage = this.getASAPStorage();
            ASAPMessages asapMessages = asapStorage.getChannel(SHARK_CREDIT_MONEY_SIGNED_BOND_URI).getMessages(false);
            Collection<SharkBond> bonds = new ArrayList<>();
            Iterator<byte[]> bondIterator = asapMessages.getMessages();
            while (bondIterator.hasNext()) {
                byte[] serializedBond = bondIterator.next();
                InMemoSharkBond creditBond = (InMemoSharkBond) SharkBondSerializer.deserializeCreditBond(serializedBond, this.certificateComponent);
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
            ASAPStorage asapStorage = this.getASAPStorage();
            ASAPMessages asapMessages = asapStorage.getChannel(SHARK_CREDIT_MONEY_SIGNED_BOND_URI).getMessages(false);
            Collection<SharkBond> bonds = new ArrayList<>();
            Iterator<byte[]> bondIterator = asapMessages.getMessages();
            while (bondIterator.hasNext()) {
                byte[] serializedBond = bondIterator.next();
                InMemoSharkBond creditBond = (InMemoSharkBond) SharkBondSerializer.deserializeCreditBond(serializedBond, this.certificateComponent);
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
            ASAPStorage asapStorage = this.getASAPStorage();
            ASAPMessages asapMessages = asapStorage.getChannel(SHARK_CREDIT_MONEY_SIGNED_BOND_URI).getMessages(false);
            Collection<SharkBond> bonds = new ArrayList<>();
            Iterator<byte[]> bondIterator = asapMessages.getMessages();
            while (bondIterator.hasNext()) {
                byte[] serializedBond = bondIterator.next();
                InMemoSharkBond creditBond = (InMemoSharkBond) SharkBondSerializer.deserializeCreditBond(serializedBond, this.certificateComponent);
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
    public void subscribeBondReceivedListener(SharkBondsReceivedListener listener) {
        // TODO just one listener. OK?
        this.sharkBondReceivedListener = listener;
        this.addSharkBondReceivedListener(listener);
    }

    @Override
    public ASAPPeer getASAPPeer() {
        return this.asapPeer;
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
    public SharkPKIComponent getSharkPKI() {
        return this.certificateComponent;
    }

    /////////////////////////////////////////////////////////////////////////////////////////////
    //                       backdoor - remove it when finished implementing                   //
    /////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public ASAPStorage getASAPStorage() throws IOException, ASAPException {
        return this.asapPeer.getASAPStorage(SharkCreditMoneyComponent.SHARK_CREDIT_MONEY_FORMAT);
    }
}
