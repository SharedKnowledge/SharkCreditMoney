package net.sharksystem.creditmoney;

import net.sharksystem.asap.ASAPException;
import net.sharksystem.asap.ASAPStorage;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class SharkBondsReceivedListenerImpl implements SharkBondsReceivedListener {
    private final SharkCreditMoneyComponent sharkCreditMoneyComponent;

    public SharkBondsReceivedListenerImpl(SharkCreditMoneyComponent sharkCreditMoneyComponent) {
        this.sharkCreditMoneyComponent = sharkCreditMoneyComponent;
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////////
    //                                          act on received bonds                                       //
    //////////////////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    public void sharkBondReceived(CharSequence uri) throws ASAPException, IOException, SharkCreditMoneyException {
        ASAPStorage asapStorage = this.sharkCreditMoneyComponent.getASAPStorage();
        byte[] asapMessage = asapStorage.getChannel(uri).getMessages(false).getMessage(0, true);

        InMemoSharkBond creditBond = (InMemoSharkBond) SharkBondSerializer.deserializeCreditBond(asapMessage, this.sharkCreditMoneyComponent.getSharkPKI());

        switch(uri.toString()) {
            case SharkCreditMoneyComponent.SHARK_CREDIT_MONEY_ASKED_TO_SIGN_AS_CREDITOR_URI:
                this.requestSignAsCreditor(creditBond);
                break;
            case SharkCreditMoneyComponent.SHARK_CREDIT_MONEY_ASKED_TO_SIGN_AS_DEBTOR_URI:
                this.requestSignAsDebtor(creditBond);
                break;
            case SharkCreditMoneyComponent.SHARK_CREDIT_MONEY_ASKED_TO_CHANGE_CREDITOR_URI:
                this.requestChangeCreditor(creditBond);
                break;
            case SharkCreditMoneyComponent.SHARK_CREDIT_MONEY_ASKED_TO_CHANGE_DEBTOR_URI:
                this.requestChangeDebtor(creditBond);
                break;
            case SharkCreditMoneyComponent.SHARK_CREDIT_MONEY_SIGNED_BOND_URI:
                /* TODO */
                break;
            case SharkCreditMoneyComponent.SHARK_CREDIT_MONEY_ANNUL_BOND_URI:
                this.annulBond(creditBond);
                break;
            default: // unknown URI
        }
    }


    @Override
    public void requestSignAsCreditor(SharkBond bond) throws ASAPException, IOException, SharkCreditMoneyException {
        SharkBondHelper.signAsCreditor(this.sharkCreditMoneyComponent.getSharkPKI(), bond);
        Set<CharSequence> receiver = new HashSet<>();
        receiver.add(bond.getCreditorID());
        byte[] serializedBond = SharkBondSerializer.serializeCreditBond(bond, bond.getDebtorID(), receiver, true, true, this.sharkCreditMoneyComponent.getSharkPKI(), false);

        this.sharkCreditMoneyComponent.getASAPPeer().sendASAPMessage(SharkCreditMoneyComponent.SHARK_CREDIT_MONEY_FORMAT, SharkCreditMoneyComponent.SHARK_CREDIT_MONEY_SIGNED_BOND_URI, serializedBond);
    }

    @Override
    public void requestSignAsDebtor(SharkBond bond) throws ASAPException, IOException, SharkCreditMoneyException {
        SharkBondHelper.signAsDebtor(this.sharkCreditMoneyComponent.getSharkPKI(), bond);
        Set<CharSequence> receiver = new HashSet<>();
        receiver.add(bond.getDebtorID());
        byte[] serializedBond = SharkBondSerializer.serializeCreditBond(bond, bond.getCreditorID(), receiver, true, true, this.sharkCreditMoneyComponent.getSharkPKI(), false);

        this.sharkCreditMoneyComponent.getASAPPeer().sendASAPMessage(SharkCreditMoneyComponent.SHARK_CREDIT_MONEY_FORMAT, SharkCreditMoneyComponent.SHARK_CREDIT_MONEY_SIGNED_BOND_URI, serializedBond);
    }

    @Override
    public void requestChangeCreditor(SharkBond bond) throws ASAPException {

    }

    @Override
    public void requestChangeDebtor(SharkBond bond) throws ASAPException {

    }

    @Override
    public void annulBond(SharkBond bond) throws ASAPException, IOException {
        InMemoSharkBond creditBond = (InMemoSharkBond) bond;
        SharkBondHelper.annulBond(creditBond);
        Set<CharSequence> receiver = new HashSet<>();
        if (this.sharkCreditMoneyComponent.getASAPPeer().samePeer(bond.getCreditorID())) {
            receiver.add(bond.getDebtorID());
        } else {
            receiver.add(bond.getCreditorID());
        }

        byte[] serializedBond = SharkBondSerializer.serializeCreditBond(bond, this.sharkCreditMoneyComponent.getASAPPeer().getPeerID(), receiver, true, true, this.sharkCreditMoneyComponent.getSharkPKI(), false);

        this.sharkCreditMoneyComponent.getASAPPeer().sendASAPMessage(SharkCreditMoneyComponent.SHARK_CREDIT_MONEY_FORMAT, SharkCreditMoneyComponent.SHARK_CREDIT_MONEY_ANNUL_BOND_URI, serializedBond);
    }
}
