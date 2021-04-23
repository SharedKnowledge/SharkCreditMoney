package net.sharksystem.creditmoney;

import net.sharksystem.AbstractSharkComponent;
import net.sharksystem.SharkCertificateComponent;
import net.sharksystem.SharkException;
import net.sharksystem.SharkUnknownBehaviourException;
import net.sharksystem.asap.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

public class SharkMoneyComponentImpl extends AbstractSharkComponent implements
        SharkCreditMoneyComponent, /*SharkBondReceivedListener,*/ ASAPMessageReceivedListener {
    private final SharkCertificateComponent certificateComponent;
    private ASAPPeer asapPeer;
    private SharkBondReceivedListener sharkBondReceivedListener;

    private boolean allowTransfer = true;

    public SharkMoneyComponentImpl(SharkCertificateComponent certificateComponent) {
        this.certificateComponent = certificateComponent;
    }

    @Override
    public SharkBond createBond(CharSequence creditorID, CharSequence debtorID, CharSequence unit, int amount) throws SharkCreditMoneyException, ASAPException {
        // Create creditBond and ask to sign by debtor
        InMemoSharkBond creditBond = new InMemoSharkBond(creditorID, debtorID, unit, amount, allowTransfer);
        creditBond.signBondAsCreditor(this.certificateComponent);
        this.asapPeer.sendASAPMessage(SHARK_CREDIT_MONEY_FORMAT, SHARK_CREDIT_MONEY_ASKED_TO_SIGN_AS_DEBTOR_URI, InMemoSharkBond.serializeCreditBond(creditBond));
        return null;
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
                InMemoSharkBond creditBond = InMemoSharkBond.deserializeCreditBond(serializedBond);
                if (creditBond.getCreditor().getUUID().equals(creditorID)) {
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
                InMemoSharkBond creditBond = InMemoSharkBond.deserializeCreditBond(serializedBond);
                if (creditBond.getDebtor().getUUID().equals(debtorID)) {
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
                InMemoSharkBond creditBond = InMemoSharkBond.deserializeCreditBond(serializedBond);
                if (creditBond.getCreditor().getUUID().equals(creditorID) && creditBond.getDebtor().getUUID().equals(debtorID)) {
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
    public void replaceDebtor(SharkBond bond, CharSequence newDebtor) throws SharkCreditMoneyException, ASAPException {
        InMemoSharkBond creditBond = (InMemoSharkBond) bond;
        creditBond.setDebtor(new PersonImpl(newDebtor));
        creditBond.signBondAsDebtor(this.certificateComponent);
        this.asapPeer.sendASAPMessage(SHARK_CREDIT_MONEY_FORMAT, SHARK_CREDIT_MONEY_ASKED_TO_SIGN_AS_CREDITOR_URI, InMemoSharkBond.serializeCreditBond(creditBond));
    }

    @Override
    public void replaceCreditor(SharkBond bond, CharSequence newCreditor) throws SharkCreditMoneyException, ASAPException {
        InMemoSharkBond creditBond = (InMemoSharkBond) bond;
        creditBond.setCreditor(new PersonImpl(newCreditor));
        creditBond.signBondAsCreditor(this.certificateComponent);
        this.asapPeer.sendASAPMessage(SHARK_CREDIT_MONEY_FORMAT, SHARK_CREDIT_MONEY_ANNUL_BOND_URI, InMemoSharkBond.serializeCreditBond(creditBond));
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
        this.asapPeer.sendASAPMessage(SHARK_CREDIT_MONEY_FORMAT, SHARK_CREDIT_MONEY_ASKED_TO_SIGN_AS_CREDITOR_URI, InMemoSharkBond.serializeCreditBond(creditBond));
    }

    @Override
    public void onStart(ASAPPeer asapPeer) throws SharkException {
        this.asapPeer = asapPeer;
        this.asapPeer.addASAPMessageReceivedListener(
                SharkCreditMoneyComponent.SHARK_CREDIT_MONEY_FORMAT, this);
    }

    @Override
    public void setBehaviour(String behaviour, boolean on) throws SharkUnknownBehaviourException {
        /* no component specific behaviour here.
        switch (behaviour) {
            case SharkCreditMoneyComponent.BEHAVIOUR_SHARK_MONEY_ALLOW_TRANSFER:
                this.allowTransfer = on; break;

            default: throw new SharkUnknownBehaviourException(behaviour);
        }*/

        super.setBehaviour(behaviour, on);
    }

    @Override
    public void asapMessagesReceived(ASAPMessages asapMessages) throws IOException {
        SharkBond bond = null;

        try {
            bond = InMemoSharkBond.deserializeCreditBond(asapMessages.getMessage(0, true));
        } catch (ASAPException e) {
            e.printStackTrace();
        }

        /*
        try {
            this.sharkBondReceivedListener.sharkCreditBondReceived(bond, asapMessages.getURI());
        } catch (ASAPException e) {
            e.printStackTrace();
        }
         */
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////////
    //                                          act on received bonds                                       //
    //////////////////////////////////////////////////////////////////////////////////////////////////////////

    //@Override
    public void sharkCreditBondReceived(SharkBond bond, CharSequence uri) throws ASAPException {
        InMemoSharkBond creditBond = (InMemoSharkBond) bond;
        switch(uri.toString()) {
            case SharkCreditMoneyComponent.SHARK_CREDIT_MONEY_ASKED_TO_SIGN_AS_CREDITOR_URI:
                creditBond.signBondAsCreditor(this.certificateComponent);
                this.asapPeer.sendASAPMessage(SHARK_CREDIT_MONEY_FORMAT, SHARK_CREDIT_MONEY_SIGNED_BOND_URI, InMemoSharkBond.serializeCreditBond(creditBond));
                break;
            case SharkCreditMoneyComponent.SHARK_CREDIT_MONEY_ASKED_TO_SIGN_AS_DEBTOR_URI:
                creditBond.signBondAsDebtor(this.certificateComponent);
                this.asapPeer.sendASAPMessage(SHARK_CREDIT_MONEY_FORMAT, SHARK_CREDIT_MONEY_SIGNED_BOND_URI, InMemoSharkBond.serializeCreditBond(creditBond));
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
}
