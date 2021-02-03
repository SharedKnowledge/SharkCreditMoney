package net.sharksystem.creditmoney;

import net.sharksystem.SharkCertificateComponent;
import net.sharksystem.SharkException;
import net.sharksystem.SharkUnknownBehaviourException;
import net.sharksystem.asap.ASAPPeer;
import net.sharksystem.asap.persons.Person;

import java.util.Collection;

public class SharkCreditMoneyComponentImpl implements SharkCreditMoneyComponent {
    private final SharkCertificateComponent certificateComponent;

    public SharkCreditMoneyComponentImpl(SharkCertificateComponent certificateComponent) {
        this.certificateComponent = certificateComponent;
    }

    @Override
    public void createBond(Person creditor, Person debtor, CharSequence unit, int amount) throws SharkCreditMoneyException {

    }

    @Override
    public Collection<SharkCreditBond> getBondsByCreditor(Person creditor) {
        return null;
    }

    @Override
    public Collection<SharkCreditBond> getBondsByDebtor(Person debtor) {
        return null;
    }

    @Override
    public Collection<SharkCreditBond> getBondsByCreditorAndDebtor(Person creditor, Person debtor) {
        return null;
    }

    @Override
    public void replaceDebtor(SharkCreditBond bond, Person newDebtor) throws SharkCreditMoneyException {

    }

    @Override
    public void replaceCreditor(SharkCreditBond bond, Person newDebtor) throws SharkCreditMoneyException {

    }

    @Override
    public void subscribeSharkCreditBondReceivedListener(SharkCreditBondReceivedListener listener) {

    }

    @Override
    public void annulBond(SharkCreditBond bond) throws SharkCreditMoneyException {

    }

    @Override
    public void onStart(ASAPPeer asapPeer) throws SharkException {

    }

    @Override
    public void setBehaviour(String s, boolean b) throws SharkUnknownBehaviourException {

    }
}
