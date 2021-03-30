package net.sharksystem.creditmoney;

import net.sharksystem.SharkCertificateComponent;
import net.sharksystem.SharkException;
import net.sharksystem.SharkUnknownBehaviourException;
import net.sharksystem.asap.*;
import net.sharksystem.asap.crypto.ASAPCryptoAlgorithms;
import net.sharksystem.asap.crypto.ASAPKeyStore;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

public class SharkCreditMoneyComponentImpl extends SharkCreditBondReceivedListenerManager implements
        SharkCreditMoneyComponent, SharkCreditBondReceivedListener, ASAPMessageReceivedListener {
    private final SharkCertificateComponent certificateComponent;
    // private static final String KEY_NAME_SHARK_CREDIT_BOND_CHANNEL_NAME = "sharkCreditBondChannelName";
    private ASAPPeer asapPeer;
    private SharkCreditBondReceivedListener sharkCreditBondReceivedListener;

    private boolean allowTransfer = true;

    public SharkCreditMoneyComponentImpl(SharkCertificateComponent certificateComponent) {
        this.certificateComponent = certificateComponent;
    }

    @Override
    public void createBond(CharSequence creditorID, CharSequence debtorID, CharSequence unit, int amount) throws ASAPException {
        // Create creditBond and ask to sign by debtor
        InMemoSharkCreditBond creditBond = new InMemoSharkCreditBond(creditorID, debtorID, unit, amount, allowTransfer);
        creditBond.setCreditorSignature(this.signBond(this.certificateComponent, creditBond));
        this.asapPeer.sendASAPMessage(SHARK_CREDIT_MONEY_FORMAT, SHARK_CREDIT_MONEY_ASKED_TO_SIGN_AS_DEBTOR_URI, InMemoSharkCreditBond.serializeCreditBond(creditBond));
    }

    @Override
    public void createChannel(CharSequence uri/*, CharSequence name*/) throws IOException, SharkCreditMoneyException {

        try {
            ASAPStorage asapStorage =
                    this.asapPeer.getASAPStorage(SharkCreditMoneyComponent.SHARK_CREDIT_MONEY_FORMAT);

            asapStorage.createChannel(uri);
            //asapStorage.putExtra(uri, KEY_NAME_SHARK_CREDIT_BOND_CHANNEL_NAME, name.toString());
        }
        catch(ASAPException asapException) {
            throw new SharkCreditMoneyException(asapException);
        }
    }

    @Override
    public Collection<SharkCreditBond> getBondsByCreditor(CharSequence creditorID) throws SharkCreditMoneyException {
        try {
            Collection<SharkCreditBond> bonds = new ArrayList<>();
            ASAPStorage asapStorage =
                    this.asapPeer.getASAPStorage(SharkCreditMoneyComponent.SHARK_CREDIT_MONEY_FORMAT);

            if (asapStorage.channelExists(SHARK_CREDIT_MONEY_SIGNED_BOND_URI)) {
                ASAPMessages asapMessages = asapStorage.getChannel(SHARK_CREDIT_MONEY_SIGNED_BOND_URI).getMessages();

                Iterator<byte[]> bondIterator = asapMessages.getMessages();
                while (bondIterator.hasNext()) {
                    byte[] serializedBond = bondIterator.next();
                    InMemoSharkCreditBond creditBond = InMemoSharkCreditBond.deserializeCreditBond(serializedBond);
                    if (creditBond.getCreditor().getUUID().equals(creditorID)) {
                        bonds.add(creditBond);
                    }
                }
            }

            return bonds;
        }
        catch(ASAPException | IOException asapException) {
            throw new SharkCreditMoneyException(asapException);
        }
    }

    @Override
    public Collection<SharkCreditBond> getBondsByDebtor(CharSequence debtorID) throws SharkCreditMoneyException {
        try {
            ASAPStorage asapStorage =
                    this.asapPeer.getASAPStorage(SharkCreditMoneyComponent.SHARK_CREDIT_MONEY_FORMAT);

            ASAPMessages asapMessages = asapStorage.getChannel(SHARK_CREDIT_MONEY_SIGNED_BOND_URI).getMessages();
            Collection<SharkCreditBond> bonds = new ArrayList<>();
            Iterator<byte[]> bondIterator = asapMessages.getMessages();
            while (bondIterator.hasNext()) {
                byte[] serializedBond = bondIterator.next();
                InMemoSharkCreditBond creditBond = InMemoSharkCreditBond.deserializeCreditBond(serializedBond);
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
    public Collection<SharkCreditBond> getBondsByCreditorAndDebtor(CharSequence creditorID, CharSequence debtorID) throws SharkCreditMoneyException {
        try {
            ASAPStorage asapStorage =
                    this.asapPeer.getASAPStorage(SharkCreditMoneyComponent.SHARK_CREDIT_MONEY_FORMAT);

            ASAPMessages asapMessages = asapStorage.getChannel(SHARK_CREDIT_MONEY_SIGNED_BOND_URI).getMessages();
            Collection<SharkCreditBond> bonds = new ArrayList<>();
            Iterator<byte[]> bondIterator = asapMessages.getMessages();
            while (bondIterator.hasNext()) {
                byte[] serializedBond = bondIterator.next();
                InMemoSharkCreditBond creditBond = InMemoSharkCreditBond.deserializeCreditBond(serializedBond);
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
    public void replaceDebtor(SharkCreditBond bond, CharSequence newDebtor) throws SharkCreditMoneyException, ASAPException {
        InMemoSharkCreditBond creditBond = (InMemoSharkCreditBond) bond;
        creditBond.setDebtor(new PersonImpl(newDebtor));
        creditBond.setDebtorSignature(this.signBond(this.certificateComponent, creditBond));
        this.asapPeer.sendASAPMessage(SHARK_CREDIT_MONEY_FORMAT, SHARK_CREDIT_MONEY_ASKED_TO_SIGN_AS_CREDITOR_URI, InMemoSharkCreditBond.serializeCreditBond(creditBond));
    }

    @Override
    public void replaceCreditor(SharkCreditBond bond, CharSequence newCreditor) throws SharkCreditMoneyException, ASAPException {
        InMemoSharkCreditBond creditBond = (InMemoSharkCreditBond) bond;
        creditBond.setCreditor(new PersonImpl(newCreditor));
        creditBond.setCreditorSignature(this.signBond(this.certificateComponent, creditBond));
        this.asapPeer.sendASAPMessage(SHARK_CREDIT_MONEY_FORMAT, SHARK_CREDIT_MONEY_ANNUL_BOND_URI, InMemoSharkCreditBond.serializeCreditBond(creditBond));
    }

    @Override
    public void subscribeSharkCreditBondReceivedListener(SharkCreditBondReceivedListener listener) {
        // TODO just one listener. OK?
        this.sharkCreditBondReceivedListener = listener;
    }

    @Override
    public void annulBond(SharkCreditBond bond) throws SharkCreditMoneyException, ASAPException {
        InMemoSharkCreditBond creditBond = (InMemoSharkCreditBond) bond;
        creditBond.annulBond();
        this.asapPeer.sendASAPMessage(SHARK_CREDIT_MONEY_FORMAT, SHARK_CREDIT_MONEY_ASKED_TO_SIGN_AS_CREDITOR_URI, InMemoSharkCreditBond.serializeCreditBond(creditBond));
    }

    @Override
    public void onStart(ASAPPeer asapPeer) throws SharkException {
        this.asapPeer = asapPeer;
        this.asapPeer.addASAPMessageReceivedListener(
                SharkCreditMoneyComponent.SHARK_CREDIT_MONEY_FORMAT, this);

        // Create channels
        /*
        try {
            this.createChannel(SHARK_CREDIT_MONEY_SIGNED_BOND_URI);
            this.createChannel(SHARK_CREDIT_MONEY_ASKED_TO_SIGN_AS_CREDITOR_URI);
            this.createChannel(SHARK_CREDIT_MONEY_SIGNED_BOND_URI);
            this.createChannel(SHARK_CREDIT_MONEY_ANNUL_BOND_URI);
        } catch (IOException e) {
            e.printStackTrace();
        }
        */
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

        ASAPStorage asapStorage = null;
        ASAPChunkStorage chunkStorage = null;
        try {
            asapStorage = this.asapPeer.getASAPStorage(SharkCreditMoneyComponent.SHARK_CREDIT_MONEY_FORMAT);
            chunkStorage = asapStorage.getChunkStorage();
        } catch (ASAPException e) {
            e.printStackTrace();
        }

        if (asapMessages != null) {
            try {
                int era = asapStorage.getEra(); // current era
                ASAPChunk asapChunk = chunkStorage.getChunk(asapMessages.getURI(), era);
                int numberMsg = asapChunk.getNumberMessage(); // how many are there?
                if (numberMsg > 0) {
                    bond = InMemoSharkCreditBond.deserializeCreditBond(asapMessages.getMessage(0, true));
                    this.sharkCreditBondReceivedListener.sharkCreditBondReceived(bond, asapMessages.getURI());
                }
            } catch (ASAPException e) {
                e.printStackTrace();
            }
        }
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////////
    //                                          act on received bonds                                       //
    //////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void sharkCreditBondReceived(SharkCreditBond bond, CharSequence uri) throws ASAPException {
        InMemoSharkCreditBond creditBond = (InMemoSharkCreditBond) bond;
        switch(uri.toString()) {
            case SharkCreditMoneyComponent.SHARK_CREDIT_MONEY_ASKED_TO_SIGN_AS_CREDITOR_URI:
                creditBond.setCreditorSignature(this.signBond(certificateComponent, creditBond));
                this.asapPeer.sendASAPMessage(SHARK_CREDIT_MONEY_FORMAT, SHARK_CREDIT_MONEY_SIGNED_BOND_URI, InMemoSharkCreditBond.serializeCreditBond(creditBond));
                break;
            case SharkCreditMoneyComponent.SHARK_CREDIT_MONEY_ASKED_TO_SIGN_AS_DEBTOR_URI:
                creditBond.setDebtorSignature(this.signBond(certificateComponent, creditBond));
                this.asapPeer.sendASAPMessage(SHARK_CREDIT_MONEY_FORMAT, SHARK_CREDIT_MONEY_SIGNED_BOND_URI, InMemoSharkCreditBond.serializeCreditBond(creditBond));
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
    public boolean isCreditorSignatureCorrect(SharkCreditBond creditBond, ASAPKeyStore ASAPKeyStore) throws ASAPSecurityException {
        return  this.isSignatureCorrect(creditBond.getCreditor().getUUID().toString(), creditBond.toString().getBytes(), creditBond.getCreditorSignature(), ASAPKeyStore);
    }

    @Override
    public boolean isDebtorSignatureCorrect(SharkCreditBond CreditBond, ASAPKeyStore ASAPKeyStore) throws ASAPSecurityException {
        return  this.isSignatureCorrect(CreditBond.getDebtor().getUUID().toString(), CreditBond.toString().getBytes(), CreditBond.getDebtorSignature(), ASAPKeyStore);
    }

    @Override
    public byte[] signBond(ASAPKeyStore ASAPKeyStore, SharkCreditBond Bond) throws ASAPSecurityException {
        return ASAPCryptoAlgorithms.sign(Bond.toString().getBytes(), ASAPKeyStore);
    }

    private boolean isSignatureCorrect(String signer, byte[] message, byte[] signature, ASAPKeyStore ASAPKeyStore) throws ASAPSecurityException {
        return ASAPCryptoAlgorithms.verify(message, signature, signer, ASAPKeyStore);
    }
}
