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

public class SharkMoneyComponentImpl implements SharkCreditMoneyComponent, SharkBondReceivedListener, ASAPMessageReceivedListener {
    private static final String KEY_NAME_SHARK_MESSENGER_CHANNEL_NAME = "sharkMoneyChannelName";
    private final SharkPKIComponent certificateComponent;
    private ASAPPeer asapPeer;
    private SharkBondReceivedListener sharkBondReceivedListener;

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
    public void initDefaultChannels() throws IOException, SharkCreditMoneyException {
        this.createChannel(SharkCreditMoneyComponent.SHARK_CREDIT_MONEY_ASKED_TO_SIGN_AS_DEBTOR_URI, SharkCreditMoneyComponent.SHARK_CREDIT_MONEY_ASKED_TO_SIGN_AS_DEBTOR_NAME);
        this.createChannel(SharkCreditMoneyComponent.SHARK_CREDIT_MONEY_ASKED_TO_SIGN_AS_CREDITOR_URI, SharkCreditMoneyComponent.SHARK_CREDIT_MONEY_ASKED_TO_SIGN_AS_CREDITOR_NAME);
        this.createChannel(SharkCreditMoneyComponent.SHARK_CREDIT_MONEY_SIGNED_BOND_URI, SharkCreditMoneyComponent.SHARK_CREDIT_MONEY_SIGNED_BOND_NAME);
        this.createChannel(SharkCreditMoneyComponent.SHARK_CREDIT_MONEY_ANNUL_BOND_URI, SharkCreditMoneyComponent.SHARK_CREDIT_MONEY_ANNUL_BOND_NAME);
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
        this.asapPeer.sendASAPMessage(SHARK_CREDIT_MONEY_FORMAT, SHARK_CREDIT_MONEY_ASKED_TO_SIGN_AS_DEBTOR_URI, SharkBondSerializer.serializeCreditBond(creditBond, this.certificateComponent));
    }

    @Override
    public void replaceDebtor(SharkBond bond, CharSequence newDebtor) throws SharkCreditMoneyException, ASAPException, IOException {
        InMemoSharkBond creditBond = (InMemoSharkBond) bond;
        creditBond.setDebtorID(newDebtor);
        SharkBondHelper.signAsDebtor(this.certificateComponent, creditBond);
        this.asapPeer.sendASAPMessage(SHARK_CREDIT_MONEY_FORMAT, SHARK_CREDIT_MONEY_ASKED_TO_SIGN_AS_DEBTOR_URI, SharkBondSerializer.serializeCreditBond(creditBond, this.certificateComponent));
    }

    @Override
    public void replaceCreditor(SharkBond bond, CharSequence newCreditor) throws SharkCreditMoneyException, ASAPException, IOException {
        InMemoSharkBond creditBond = (InMemoSharkBond) bond;
        creditBond.setCreditorID(newCreditor);
        SharkBondHelper.signAsCreditor(this.certificateComponent, creditBond);
        this.asapPeer.sendASAPMessage(SHARK_CREDIT_MONEY_FORMAT, SHARK_CREDIT_MONEY_ASKED_TO_SIGN_AS_CREDITOR_URI, SharkBondSerializer.serializeCreditBond(creditBond, this.certificateComponent));
    }

    @Override
    public Collection<SharkBond> getBondsByCreditor(CharSequence creditorID) throws SharkCreditMoneyException {
        try {
            ASAPStorage asapStorage =this.getASAPStorage();

            ASAPMessages asapMessages = asapStorage.getChannel(SHARK_CREDIT_MONEY_ASKED_TO_SIGN_AS_CREDITOR_URI).getMessages();
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
            ASAPChunkStorage chunkStorage = asapStorage.getChunkStorage();
            int era = asapStorage.getEra(); // current era
            ASAPChunk asapChunk = chunkStorage.getChunk(SHARK_CREDIT_MONEY_ASKED_TO_SIGN_AS_DEBTOR_URI, era);
            int numberMsg = asapChunk.getNumberMessage(); // how many are there?
            // get messages
            Iterator<byte[]> bondIterator = asapChunk.getMessages();
            Collection<SharkBond> bonds = new ArrayList<>();
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
            ASAPMessages asapMessages = asapStorage.getChannel(SHARK_CREDIT_MONEY_SIGNED_BOND_URI).getMessages();
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
    public void subscribeBondReceivedListener(SharkBondReceivedListener listener) {
        // TODO just one listener. OK?
        this.sharkBondReceivedListener = listener;
    }

    @Override
    public void annulBond(SharkBond bond) throws ASAPException, IOException {
        InMemoSharkBond creditBond = (InMemoSharkBond) bond;
        creditBond.annulBond();
        this.asapPeer.sendASAPMessage(SHARK_CREDIT_MONEY_FORMAT, SHARK_CREDIT_MONEY_ASKED_TO_SIGN_AS_CREDITOR_URI, SharkBondSerializer.serializeCreditBond(creditBond, this.certificateComponent));
    }

    @Override
    public void asapMessagesReceived(ASAPMessages asapMessages) throws IOException {
        SharkBond bond = null;
        try {
            CharSequence uri = asapMessages.getURI();

            ASAPStorage asapStorage = this.getASAPStorage();
            byte[] asapMessage =
                    asapStorage.getChannel(uri).getMessages().getMessage(0, true);

            bond = SharkBondSerializer.deserializeCreditBond(asapMessage, this.certificateComponent);
            this.sharkBondReceivedListener.sharkBondReceived(bond, asapMessages.getURI());
        } catch (ASAPException | SharkCreditMoneyException | NullPointerException e) {
            e.printStackTrace();
        }
    }

    /////////////////////////////////////////////////////////////////////////////////////////////
    //                       backdoor - remove it when finished implementing                   //
    /////////////////////////////////////////////////////////////////////////////////////////////

    public ASAPStorage getASAPStorage() throws IOException, ASAPException {
        return this.asapPeer.getASAPStorage(SharkCreditMoneyComponent.SHARK_CREDIT_MONEY_FORMAT);
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
                this.asapPeer.sendASAPMessage(SHARK_CREDIT_MONEY_FORMAT, SHARK_CREDIT_MONEY_SIGNED_BOND_URI, SharkBondSerializer.serializeCreditBond(creditBond, this.certificateComponent));
                break;
            case SharkCreditMoneyComponent.SHARK_CREDIT_MONEY_ASKED_TO_SIGN_AS_DEBTOR_URI:
                SharkBondHelper.signAsDebtor(this.certificateComponent, creditBond);
                this.asapPeer.sendASAPMessage(SHARK_CREDIT_MONEY_FORMAT, SHARK_CREDIT_MONEY_SIGNED_BOND_URI, SharkBondSerializer.serializeCreditBond(creditBond, this.certificateComponent));
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
