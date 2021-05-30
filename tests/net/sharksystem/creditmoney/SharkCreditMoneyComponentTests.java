package net.sharksystem.creditmoney;

import net.sharksystem.*;
import net.sharksystem.asap.ASAPChannel;
import net.sharksystem.asap.ASAPException;
import net.sharksystem.asap.ASAPSecurityException;
import net.sharksystem.asap.ASAPStorage;
import net.sharksystem.asap.crypto.ASAPKeyStore;
import net.sharksystem.asap.pki.ASAPCertificate;
import net.sharksystem.asap.pki.CredentialMessageInMemo;
import net.sharksystem.pki.SharkPKIComponent;
import net.sharksystem.pki.SharkPKIComponentFactory;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

import static net.sharksystem.creditmoney.TestConstants.*;

public class SharkCreditMoneyComponentTests {
    private static final String THIS_ROOT_DIRECTORY = TestConstants.ROOT_DIRECTORY + SharkCreditMoneyComponentTests.class.getSimpleName() + "/";
    private static final String ALICE_FOLDER = THIS_ROOT_DIRECTORY + ALICE_NAME;
    public static final String BOB_FOLDER = THIS_ROOT_DIRECTORY + BOB_NAME;
    public static final String CLARA_FOLDER = THIS_ROOT_DIRECTORY + CLARA_NAME;
    public static final String DAVID_FOLDER = THIS_ROOT_DIRECTORY + DAVID_NAME;
    private SharkTestPeerFS alicePeer;
    private SharkTestPeerFS bobPeer;
    private SharkTestPeerFS claraPeer;

    private static int portNumber = 5000;
    private SharkCreditMoneyComponent aliceComponent;
    private SharkCreditMoneyComponent bobComponent;
    private SharkCreditMoneyComponent claraComponent;

    private SharkMoneyComponentImpl aliceComponentImpl;
    private SharkMoneyComponentImpl bobComponentImpl;
    private SharkMoneyComponentImpl claraComponentImpl;

    private int getPortNumber() {
        return SharkCreditMoneyComponentTests.portNumber++;
    }

    private SharkCreditMoneyComponent setupComponent(SharkPeer sharkPeer) throws SharkException {
        // create a component factory
        SharkPKIComponentFactory certificateComponentFactory = new SharkPKIComponentFactory();

        // register this component with shark peer - note: we use interface SharkPeer
        sharkPeer.addComponent(certificateComponentFactory, SharkPKIComponent.class);

        // get certificate component
        SharkPKIComponent certificateComponent =
                (SharkPKIComponent) sharkPeer.getComponent(SharkPKIComponent.class);

        // create money factory ;)
        SharkCreditMoneyComponentFactory componentFactory = new SharkCreditMoneyComponentFactory(certificateComponent);

        // shark money component required
        sharkPeer.addComponent(componentFactory, SharkCreditMoneyComponent.class);

        return (SharkCreditMoneyComponent) sharkPeer.getComponent(SharkCreditMoneyComponent.class);
    }

    private void setUpAliceBobExchangeScenario() throws SharkException, ASAPSecurityException, IOException {
        // Setup alice peer
        SharkTestPeerFS.removeFolder(ALICE_FOLDER);
        this.alicePeer = new SharkTestPeerFS(ALICE_ID, ALICE_FOLDER);
        this.setupComponent(this.alicePeer);

        // Setup bob peer
        SharkTestPeerFS.removeFolder(BOB_FOLDER);
        this.bobPeer = new SharkTestPeerFS(BOB_ID, BOB_FOLDER);
        this.setupComponent(this.bobPeer);

        // Start alice peer
        this.alicePeer.start();
        // Start bob peer
        this.bobPeer.start();

        // add some keys as described in scenario settings
        SharkPKIComponent alicePKI = (SharkPKIComponent) this.alicePeer.getComponent(SharkPKIComponent.class);
        SharkPKIComponent bobPKI = (SharkPKIComponent) this.bobPeer.getComponent(SharkPKIComponent.class);

        // let Bob accept ALice credentials and create a certificate
        CredentialMessageInMemo aliceCredentialMessage =
                new CredentialMessageInMemo(ALICE_ID, ALICE_NAME, System.currentTimeMillis(), alicePKI.getPublicKey());
        bobPKI.acceptAndSignCredential(aliceCredentialMessage);

        // Alice accepts Bob Public Key
        CredentialMessageInMemo bobCredentialMessage =
                new CredentialMessageInMemo(BOB_ID, BOB_NAME, System.currentTimeMillis(), bobPKI.getPublicKey());
        alicePKI.acceptAndSignCredential(bobCredentialMessage);

        this.aliceComponent = (SharkCreditMoneyComponent) this.alicePeer.getComponent(SharkCreditMoneyComponent.class);
        this.bobComponent = (SharkCreditMoneyComponent) this.bobPeer.getComponent(SharkCreditMoneyComponent.class);

        // Add SharkBondReceivedListener Implementation
        SharkBondsReceivedListener aliceListener = new DummySharkBondReceivedListener(this.aliceComponent);
        this.aliceComponent.subscribeBondReceivedListener(aliceListener);

        SharkBondsReceivedListener bobListener = new DummySharkBondReceivedListener(this.bobComponent);
        this.bobComponent.subscribeBondReceivedListener(bobListener);

        // set up backdoors
        this.aliceComponentImpl = (SharkMoneyComponentImpl) this.aliceComponent;
        this.bobComponentImpl = (SharkMoneyComponentImpl) this.bobComponent;
    }

    private class DummySharkBondReceivedListener implements SharkBondsReceivedListener {
        private final SharkCreditMoneyComponent sharkCreditMoneyComponent;
        private SharkBond creditBond;

        public DummySharkBondReceivedListener(SharkCreditMoneyComponent sharkCreditMoneyComponent) {
            this.sharkCreditMoneyComponent = sharkCreditMoneyComponent;
        }

        //////////////////////////////////////////////////////////////////////////////////////////////////////////
        //                                          act on received bonds                                       //
        //////////////////////////////////////////////////////////////////////////////////////////////////////////
        @Override
        public void sharkBondReceived(CharSequence uri) throws ASAPException, IOException, SharkCreditMoneyException {
            ASAPStorage asapStorage = this.sharkCreditMoneyComponent.getASAPStorage();
            byte[] asapMessage = asapStorage.getChannel(uri).getMessages(false).getMessage(0, true);

            this.creditBond = SharkBondSerializer.deserializeCreditBond(asapMessage, this.sharkCreditMoneyComponent.getSharkPKI());

            switch(uri.toString()) {
                case SharkCreditMoneyComponent.SHARK_CREDIT_MONEY_ASKED_TO_SIGN_AS_CREDITOR_URI:
                    SharkBondHelper.signAsCreditor(this.sharkCreditMoneyComponent.getSharkPKI(), this.creditBond);
                    this.sharkCreditMoneyComponent.getASAPPeer().sendASAPMessage(SharkCreditMoneyComponent.SHARK_CREDIT_MONEY_FORMAT, SharkCreditMoneyComponent.SHARK_CREDIT_MONEY_SIGNED_BOND_URI, SharkBondSerializer.serializeCreditBond(this.creditBond, this.sharkCreditMoneyComponent.getSharkPKI(), false));
                    break;
                case SharkCreditMoneyComponent.SHARK_CREDIT_MONEY_ASKED_TO_SIGN_AS_DEBTOR_URI:
                    try {
                        SharkBondHelper.signAsDebtor((SharkPKIComponent) bobPeer.getComponent(SharkPKIComponent.class), this.creditBond);
                    } catch (SharkException e) {
                        e.printStackTrace();
                    }
                    this.sharkCreditMoneyComponent.getASAPPeer().sendASAPMessage(SharkCreditMoneyComponent.SHARK_CREDIT_MONEY_FORMAT, SharkCreditMoneyComponent.SHARK_CREDIT_MONEY_SIGNED_BOND_URI, SharkBondSerializer.serializeCreditBond(this.creditBond, this.sharkCreditMoneyComponent.getSharkPKI(), false));
                    ///////////////////////////////// ASAP specific code - make an encounter Bob Alice
                    try {
                        runEncounter(bobPeer, alicePeer, true);
                    } catch (SharkException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    break;
                case SharkCreditMoneyComponent.SHARK_CREDIT_MONEY_ASKED_TO_CHANGE_CREDITOR_URI:
                    break;
                case SharkCreditMoneyComponent.SHARK_CREDIT_MONEY_ASKED_TO_CHANGE_DEBTOR_URI:
                    this.requestChangeDebtor(creditBond);
                    break;
                case SharkCreditMoneyComponent.SHARK_CREDIT_MONEY_SIGNED_BOND_URI:
                    /* TODO */
                    System.out.println("test");
                    break;
                case SharkCreditMoneyComponent.SHARK_CREDIT_MONEY_ANNUL_BOND_URI:
                    this.annulBond(creditBond);
                    break;
                default: // unknown URI
            }
        }


        @Override
        public void requestSignAsCreditor(SharkBond bond) throws ASAPException, IOException, SharkCreditMoneyException {

        }

        @Override
        public void requestSignAsDebtor(SharkBond bond) throws ASAPException, IOException, SharkCreditMoneyException {

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
            this.sharkCreditMoneyComponent.getASAPPeer().sendASAPMessage(SharkCreditMoneyComponent.SHARK_CREDIT_MONEY_FORMAT, SharkCreditMoneyComponent.SHARK_CREDIT_MONEY_ANNUL_BOND_URI, SharkBondSerializer.serializeCreditBond(creditBond, this.sharkCreditMoneyComponent.getSharkPKI()));
        }
    }

    public void runEncounter(SharkTestPeerFS leftPeer, SharkTestPeerFS rightPeer, boolean stop)
            throws SharkException, IOException, InterruptedException {
        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
        System.out.println("                       start encounter: "
                + leftPeer.getASAPPeer().getPeerID() + " <--> " + rightPeer.getASAPPeer().getPeerID());
        System.out.println("<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");

        leftPeer.getASAPTestPeerFS().startEncounter(this.getPortNumber(), rightPeer.getASAPTestPeerFS());
        // give them moment to exchange data
        Thread.sleep(1000);
        //Thread.sleep(Long.MAX_VALUE);
        System.out.println("slept a moment");

        if(stop) {
            System.out.println(">>>>>>>>>>>>>>>>>  stop encounter: "
                    + leftPeer.getASAPPeer().getPeerID() + " <--> " + rightPeer.getASAPPeer().getPeerID());
            leftPeer.getASAPTestPeerFS().stopEncounter(rightPeer.getASAPTestPeerFS());
        }
    }

    /**
     * Test full roundtrip to create an bond: Alice creates a bond as creditor with debtor Bob. Bob receives and
     * signs this bond which becomes complete.
     */
    @Test
    public void aliceCreatesBondAsCreditor() throws SharkException, ASAPException, IOException, InterruptedException {
        // Setup Scenario
        this.setUpAliceBobExchangeScenario();

        ////////////////////////////////// bond specific tests start here
        // Create a bond: Creditor Alice, debtor Bob of 100 "Euro"
        this.aliceComponent.createBond(ALICE_ID, BOB_ID, BOND_UNIT, BOND_AMOUNT);

        ///////////////////////////////// ASAP specific code - make an encounter Alice Bob
        this.runEncounter(this.alicePeer, this.bobPeer, true);

        // test results
        ASAPStorage bobAsapStorage = this.bobComponentImpl.getASAPStorage();
        List<CharSequence> bobSenderList = bobAsapStorage.getSender();

        // Sender list can't be null
        Assert.assertNotNull(bobSenderList);
        Assert.assertFalse(bobSenderList.isEmpty());
        CharSequence aliceSenderID = bobSenderList.get(0);
        Assert.assertTrue(this.alicePeer.samePeer(aliceSenderID));

        ASAPStorage senderIncomingStorageForBob = bobAsapStorage.getExistingIncomingStorage(ALICE_ID);
        ASAPChannel channelForBob = senderIncomingStorageForBob.getChannel(SharkCreditMoneyComponent.SHARK_CREDIT_MONEY_ASKED_TO_SIGN_AS_DEBTOR_URI);
        byte[] messageFromAlice = channelForBob.getMessages().getMessage(0, true);
        Assert.assertNotNull(messageFromAlice);

        SharkBond bondFromAlice = SharkBondSerializer.deserializeCreditBond(messageFromAlice, (SharkPKIComponent) this.bobPeer.getComponent(SharkPKIComponent.class));

        //List<SharkBond> bobBonds = (List<SharkBond>) this.bobComponentImpl.getBondsByDebtor(BOB_ID);
        // Bob must have a credit bond from Alice - he issued it by himself

        // Check if the correct bond was received
        Assert.assertNotNull(bondFromAlice);
        Assert.assertNotNull(bondFromAlice.getBondID());
        Assert.assertEquals(bondFromAlice.getAmount(), BOND_AMOUNT);
        Assert.assertEquals(bondFromAlice.getCreditorID(), ALICE_ID);
        Assert.assertEquals(bondFromAlice.getDebtorID(), BOB_ID);
        Assert.assertEquals(bondFromAlice.unitDescription(), BOND_UNIT);

        // After receiving creditBond from Alice, Bob signs it as debtor and send it back to Alice
        // test results
        ASAPStorage aliceAsapStorage = this.aliceComponentImpl.getASAPStorage();
        List<CharSequence> aliceSenderList = aliceAsapStorage.getSender();

        // Sender list can't be null
        Assert.assertNotNull(aliceSenderList);
        Assert.assertFalse(aliceSenderList.isEmpty());
        CharSequence bobSenderID = aliceSenderList.get(0);
        // Check if the message is coming from bob
        Assert.assertTrue(this.bobPeer.samePeer(bobSenderID));

        ASAPStorage senderIncomingStorageForAlice = aliceAsapStorage.getExistingIncomingStorage(BOB_ID);
        ASAPChannel channelForAlice = senderIncomingStorageForAlice.getChannel(SharkCreditMoneyComponent.SHARK_CREDIT_MONEY_SIGNED_BOND_URI);
        byte[] messageFromBob = channelForAlice.getMessages().getMessage(0, true);
        Assert.assertNotNull(messageFromBob);

        SharkBond bondFromBob = SharkBondSerializer.deserializeCreditBond(messageFromBob, (SharkPKIComponent) this.alicePeer.getComponent(SharkPKIComponent.class));
    }

    /**
     * Bob creates Bond as debtor. Alice accepts as creditor.
     */
    @Test
    public void bobCreatesBondAsDebtor() {
        // TODO
    }

    /**
     * Bond (Alice (creditor), bob (deptor). Alice wants to change creditor to Clara.
     */
    @Test
    public void aliceWantBondCreditorTransferToClara() {
        // TODO
    }

    /**
     * Bond (Alice (creditor), bob (deptor). Bob wants to change creditor to Clara.
     */
    @Test
    public void bobWantBondDebtorTransferToClara() {
        // TODO
    }

}
