package net.sharksystem.creditmoney;

import net.sharksystem.SharkCertificateComponent;
import net.sharksystem.SharkException;
import net.sharksystem.SharkUnknownBehaviourException;
import net.sharksystem.asap.ASAPException;
import net.sharksystem.asap.ASAPMessageReceivedListener;
import net.sharksystem.asap.ASAPMessages;
import net.sharksystem.asap.ASAPPeer;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Collection;

public class SharkCreditMoneyComponentImpl implements
        SharkCreditMoneyComponent, SharkCreditBondReceivedListener, ASAPMessageReceivedListener {
    private final SharkCertificateComponent certificateComponent;
    private static CharSequence DefaultURI = "sn2//all";
    private ASAPPeer asapPeer;
    private SharkCreditBondReceivedListener sharkCreditBondReceivedListener;

    private boolean allowTransfer = true;

    public SharkCreditMoneyComponentImpl(SharkCertificateComponent certificateComponent) {
        this.certificateComponent = certificateComponent;
    }

    @Override
    public void createBond(CharSequence creditorID, CharSequence debtorID, CharSequence unit, int amount) throws SharkCreditMoneyException {
        //this.asapPeer.sendASAPMessage(SHARK_CREDIT_MONEY_FORMAT, DefaultURI, new InMemoSharkCreditBond());
    }

    @Override
    public Collection<SharkCreditBond> getBondsByCreditor(CharSequence creditorID) {


        return null;
    }

    @Override
    public Collection<SharkCreditBond> getBondsByDebtor(CharSequence debtorID) {
        return null;
    }

    @Override
    public Collection<SharkCreditBond> getBondsByCreditorAndDebtor(CharSequence creditorID, CharSequence debtorID) {
        return null;
    }

    @Override
    public void replaceDebtor(SharkCreditBond bond, CharSequence newDebtor) throws SharkCreditMoneyException {

    }

    @Override
    public void replaceCreditor(SharkCreditBond bond, CharSequence newDebtor) throws SharkCreditMoneyException {

    }

    @Override
    public void subscribeSharkCreditBondReceivedListener(SharkCreditBondReceivedListener listener) {
        // TODO just one listener. OK?
        this.sharkCreditBondReceivedListener = listener;
    }

    @Override
    public void annulBond(SharkCreditBond bond) throws SharkCreditMoneyException {

    }

    @Override
    public void onStart(ASAPPeer asapPeer) throws SharkException {
        this.asapPeer = asapPeer;
        this.asapPeer.addASAPMessageReceivedListener(
                SharkCreditMoneyComponent.SHARK_CREDIT_MONEY_FORMAT, this);
    }

    @Override
    public void setBehaviour(String behaviour, boolean on) throws SharkUnknownBehaviourException {
        switch (behaviour) {
            case SharkCreditMoneyComponent.BEHAVIOUR_SHARK_MONEY_ALLOW_TRANSFER:
                this.allowTransfer = on; break;

            default: throw new SharkUnknownBehaviourException(behaviour);
        }
    }


    @Override
    public void asapMessagesReceived(ASAPMessages asapMessages) throws IOException {
        SharkCreditBond bond = null;
        // TODO: deserialize Bond.

        try {
            ByteArrayInputStream bais = new ByteArrayInputStream(asapMessages.getMessage(0, true));
        } catch (ASAPException e) {
            e.printStackTrace();
        }

        this.sharkCreditBondReceivedListener.sharkCreditBondReceived(bond, asapMessages.getURI());
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////////
    //                                          act on received bonds                                       //
    //////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void sharkCreditBondReceived(SharkCreditBond bond, CharSequence uri) {
        switch(uri.toString()) {
            case SharkCreditMoneyComponent.SHARK_CREDIT_MONEY_ASKED_TO_SIGN_AS_CREDITOR_URI: /* TODO */ break;
            case SharkCreditMoneyComponent.SHARK_CREDIT_MONEY_ASKED_TO_SIGN_AS_DEBTOR_URI: /* TODO */ break;
            case SharkCreditMoneyComponent.SHARK_CREDIT_MONEY_SIGNED_BOND_URI: /* TODO */ break;
            case SharkCreditMoneyComponent.SHARK_CREDIT_MONEY_ANNUL_BOND_URI: /* TODO */ break;
            default: // unknown URI
        }
    }
}
