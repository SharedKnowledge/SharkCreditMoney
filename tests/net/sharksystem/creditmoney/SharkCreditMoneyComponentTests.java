package net.sharksystem.creditmoney;

import net.sharksystem.*;
import net.sharksystem.asap.ASAPChannel;
import net.sharksystem.asap.ASAPException;
import net.sharksystem.asap.ASAPSecurityException;
import net.sharksystem.asap.ASAPStorage;
import net.sharksystem.asap.crypto.ASAPKeyStore;
import net.sharksystem.asap.pki.CredentialMessageInMemo;
import net.sharksystem.pki.SharkPKIComponent;
import net.sharksystem.pki.SharkPKIComponentFactory;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

    private SharkCreditMoneyComponentImpl aliceComponentImpl;
    private SharkCreditMoneyComponentImpl bobComponentImpl;
    private SharkCreditMoneyComponentImpl claraComponentImpl;

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
        this.aliceComponentImpl = (SharkCreditMoneyComponentImpl) this.aliceComponent;
        this.bobComponentImpl = (SharkCreditMoneyComponentImpl) this.bobComponent;
    }

    private class DummySharkBondReceivedListener implements SharkBondsReceivedListener {
        private final SharkCreditMoneyComponentImpl sharkCreditMoneyComponent;
        private SharkBond creditBond;

        public DummySharkBondReceivedListener(SharkCreditMoneyComponent sharkCreditMoneyComponent) {
            this.sharkCreditMoneyComponent = (SharkCreditMoneyComponentImpl) sharkCreditMoneyComponent;
        }

        //////////////////////////////////////////////////////////////////////////////////////////////////////////
        //                                          act on received bonds                                       //
        //////////////////////////////////////////////////////////////////////////////////////////////////////////
        @Override
        public void sharkBondReceived(CharSequence uri) throws ASAPException, IOException, SharkCreditMoneyException {
            System.out.println("### Inside Counter ####");
            CharSequence sender;
            Set<CharSequence> receiver;
            ASAPStorage asapStorage = this.sharkCreditMoneyComponent.getASAPStorage();
            byte[] asapMessage = asapStorage.getChannel(uri).getMessages(false).getMessage(0, true);
            SharkPKIComponent pkiStore = this.sharkCreditMoneyComponent.getSharkPKI();
            this.creditBond = SharkBondSerializer.deserializeCreditBond(asapMessage, pkiStore);

            switch(uri.toString()) {
                case SharkCreditMoneyComponent.SHARK_CREDIT_MONEY_ASKED_TO_SIGN_AS_CREDITOR_URI:
                    SharkBondHelper.signAsCreditor(pkiStore, this.creditBond);
                    receiver = new HashSet<>();
                    receiver.add(creditBond.getDebtorID());
                    this.sharkCreditMoneyComponent.sendBond(creditBond, creditBond.getCreditorID(), receiver, true, true, SharkCreditMoneyComponent.SHARK_CREDIT_MONEY_SIGNED_BOND_URI);
                    break;
                case SharkCreditMoneyComponent.SHARK_CREDIT_MONEY_ASKED_TO_SIGN_AS_DEBTOR_URI:
                    SharkBondHelper.signAsDebtor(pkiStore, this.creditBond);
                    receiver = new HashSet<>();
                    receiver.add(creditBond.getCreditorID());
                    this.sharkCreditMoneyComponent.sendBond(creditBond, creditBond.getDebtorID(), receiver, true, true, SharkCreditMoneyComponent.SHARK_CREDIT_MONEY_SIGNED_BOND_URI);
                    break;
                case SharkCreditMoneyComponent.SHARK_CREDIT_MONEY_ASKED_TO_CHANGE_CREDITOR_URI:
                    this.sharkCreditMoneyComponent.replaceCreditor(this.creditBond);
                    break;
                case SharkCreditMoneyComponent.SHARK_CREDIT_MONEY_ASKED_TO_CHANGE_DEBTOR_URI:
                    this.sharkCreditMoneyComponent.replaceDebtor(this.creditBond);
                    break;
                case SharkCreditMoneyComponent.SHARK_CREDIT_MONEY_SIGNED_BOND_URI:
                    // Finalise exchange process and save sharkBond
                    this.sharkCreditMoneyComponent.saveBond(this.creditBond);
                    break;
                case SharkCreditMoneyComponent.SHARK_CREDIT_MONEY_ANNUL_BOND_URI:
                    SharkBondHelper.annulBond(pkiStore, this.creditBond);
                    receiver = new HashSet<>();
                    if (pkiStore.getOwner().equals(this.creditBond.getCreditorID())) {
                        sender = this.creditBond.getCreditorID();
                        receiver.add(this.creditBond.getDebtorID());
                    } else {
                        sender = this.creditBond.getDebtorID();
                        receiver.add(this.creditBond.getCreditorID());
                    }
                    this.sharkCreditMoneyComponent.sendBond(this.creditBond, sender, receiver, true, true, SharkCreditMoneyComponent.SHARK_CREDIT_MONEY_ANNULLED_BOND_URI);
                    break;
                case SharkCreditMoneyComponent.SHARK_CREDIT_MONEY_ANNULLED_BOND_URI:
                    SharkBondHelper.annulBond(pkiStore, this.creditBond);
                    receiver = new HashSet<>();
                    if (pkiStore.getOwner().equals(this.creditBond.getCreditorID())) {
                        sender = this.creditBond.getCreditorID();
                        receiver.add(this.creditBond.getDebtorID());
                    } else {
                        sender = this.creditBond.getDebtorID();
                        receiver.add(this.creditBond.getCreditorID());
                    }
                    this.sharkCreditMoneyComponent.sendBond(this.creditBond, sender, receiver, true, true, SharkCreditMoneyComponent.SHARK_CREDIT_MONEY_SIGNED_BOND_URI);
                    break;
                case SharkCreditMoneyComponent.SHARK_CREDIT_MONEY_ASKED_TO_ACCEPT_TRANSFER_DEBTOR_URI:
                    SharkBondHelper.acceptTransferDebtor(pkiStore, this.creditBond);
                    receiver = new HashSet<>();
                    receiver.add(this.creditBond.getDebtorID());
                    this.sharkCreditMoneyComponent.sendBond(this.creditBond, this.creditBond.getCreditorID(), receiver, true, true, SharkCreditMoneyComponent.SHARK_CREDIT_MONEY_ACCEPTED_TRANSFER_DEBTOR_URI);
                    break;
                case SharkCreditMoneyComponent.SHARK_CREDIT_MONEY_ASKED_TO_ACCEPT_TRANSFER_CREDITOR_URI:
                    SharkBondHelper.acceptTransferCreditor(pkiStore, this.creditBond);
                    receiver = new HashSet<>();
                    receiver.add(this.creditBond.getCreditorID());
                    this.sharkCreditMoneyComponent.sendBond(this.creditBond, this.creditBond.getDebtorID(), receiver, true, true, SharkCreditMoneyComponent.SHARK_CREDIT_MONEY_ACCEPTED_TRANSFER_CREDITOR_URI);
                    break;
                case SharkCreditMoneyComponent.SHARK_CREDIT_MONEY_ACCEPTED_TRANSFER_DEBTOR_URI:
                    SharkBondHelper.acceptedTransferDebtor(pkiStore, this.creditBond);
                    receiver = new HashSet<>();
                    receiver.add(this.creditBond.getDebtorID());
                    this.sharkCreditMoneyComponent.sendBond(this.creditBond, this.creditBond.getTempDebtorID(), receiver, true, true, SharkCreditMoneyComponent.SHARK_CREDIT_MONEY_ASKED_TO_SIGN_TRANSFER_BOND_AS_DEBTOR_URI);
                    break;
                case SharkCreditMoneyComponent.SHARK_CREDIT_MONEY_ACCEPTED_TRANSFER_CREDITOR_URI:
                    SharkBondHelper.acceptedTransferCreditor(pkiStore, this.creditBond);
                    receiver = new HashSet<>();
                    receiver.add(this.creditBond.getCreditorID());
                    this.sharkCreditMoneyComponent.sendBond(this.creditBond, this.creditBond.getTempCreditorID(), receiver, true, true, SharkCreditMoneyComponent.SHARK_CREDIT_MONEY_ASKED_TO_SIGN_TRANSFER_BOND_AS_CREDITOR_URI);
                    break;
                case SharkCreditMoneyComponent.SHARK_CREDIT_MONEY_ASKED_TO_SIGN_TRANSFER_BOND_AS_DEBTOR_URI:
                    SharkBondHelper.signTransferBondAsDebtor(pkiStore, this.creditBond);
                    receiver = new HashSet<>();
                    receiver.add(this.creditBond.getCreditorID());
                    this.sharkCreditMoneyComponent.sendBond(this.creditBond, this.creditBond.getDebtorID(), receiver, true, true, SharkCreditMoneyComponent.SHARK_CREDIT_MONEY_SIGNED_TRANSFER_BOND_AS_DEBTOR_URI);
                    break;
                case SharkCreditMoneyComponent.SHARK_CREDIT_MONEY_ASKED_TO_SIGN_TRANSFER_BOND_AS_CREDITOR_URI:
                    SharkBondHelper.signTransferBondAsCreditor(pkiStore, this.creditBond);
                    receiver = new HashSet<>();
                    receiver.add(this.creditBond.getDebtorID());
                    this.sharkCreditMoneyComponent.sendBond(this.creditBond, this.creditBond.getCreditorID(), receiver, true, true, SharkCreditMoneyComponent.SHARK_CREDIT_MONEY_SIGNED_TRANSFER_BOND_AS_CREDITOR_URI);
                    break;
                case SharkCreditMoneyComponent.SHARK_CREDIT_MONEY_SIGNED_TRANSFER_BOND_AS_DEBTOR_URI:
                    receiver = new HashSet<>();
                    receiver.add(this.creditBond.getCreditorID());
                    receiver.add(this.creditBond.getTempDebtorID());
                    this.sharkCreditMoneyComponent.sendBond(this.creditBond, this.creditBond.getDebtorID(), receiver, true, true, SharkCreditMoneyComponent.SHARK_CREDIT_MONEY_SIGNED_BOND_URI);
                    break;
                case SharkCreditMoneyComponent.SHARK_CREDIT_MONEY_SIGNED_TRANSFER_BOND_AS_CREDITOR_URI:
                    receiver = new HashSet<>();
                    receiver.add(this.creditBond.getDebtorID());
                    receiver.add(this.creditBond.getTempCreditorID());
                    this.sharkCreditMoneyComponent.sendBond(this.creditBond, this.creditBond.getCreditorID(), receiver, true, true, SharkCreditMoneyComponent.SHARK_CREDIT_MONEY_SIGNED_BOND_URI);
                    break;
                default:
                    throw new ASAPException("Shark Bond received on unknown uri");
            }
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
        this.aliceComponent.createBond(ALICE_ID, BOB_ID, BOND_UNIT, BOND_AMOUNT, true);

        ///////////////////////////////// ASAP specific code - make an encounter Alice Bob
        this.runEncounter(this.alicePeer, this.bobPeer, true);

        Collection<SharkBond> bobBondList = this.bobComponent.getBondsByCreditor(ALICE_ID);

        // Bond list can't be null
        Assert.assertNotNull(bobBondList);
        Assert.assertFalse(bobBondList.isEmpty());
        SharkBond bondFromAlice = (SharkBond) bobBondList.toArray()[0];

        // Bob must have a credit bond from Alice - he issued it by himself

        // Check if the correct bond was received
        Assert.assertNotNull(bondFromAlice);
        Assert.assertNotNull(bondFromAlice.getBondID());
        Assert.assertEquals(bondFromAlice.getAmount(), BOND_AMOUNT);
        Assert.assertEquals(bondFromAlice.getCreditorID(), ALICE_ID);
        Assert.assertEquals(bondFromAlice.getDebtorID(), BOB_ID);
        Assert.assertEquals(bondFromAlice.unitDescription(), BOND_UNIT);

        // Verify creditor signature
        Assert.assertTrue(SharkBondHelper.isSignedAsCreditor(bondFromAlice, (SharkPKIComponent) this.bobPeer.getComponent(SharkPKIComponent.class)));

        // After receiving creditBond from Alice, Bob signs it as debtor and send it back to Alice
        // test results
        Collection<SharkBond> aliceBondList = this.bobComponent.getBondsByCreditor(ALICE_ID);

        // Bond list can't be null
        Assert.assertNotNull(aliceBondList);
        Assert.assertFalse(aliceBondList.isEmpty());
        SharkBond bondFromBob = (SharkBond) aliceBondList.toArray()[0];

        Assert.assertNotNull(bondFromBob);
        Assert.assertNotNull(bondFromBob.getBondID());
        Assert.assertEquals(bondFromBob.getAmount(), BOND_AMOUNT);
        Assert.assertEquals(bondFromBob.getCreditorID(), ALICE_ID);
        Assert.assertEquals(bondFromBob.getDebtorID(), BOB_ID);
        Assert.assertEquals(bondFromBob.unitDescription(), BOND_UNIT);

        // Verify debtor signature
        Assert.assertTrue(SharkBondHelper.isSignedAsDebtor(bondFromBob, (SharkPKIComponent) this.alicePeer.getComponent(SharkPKIComponent.class)));
    }

    /**
     * Bob creates Bond as debtor. Alice accepts as creditor.
     */
    @Test
    public void bobCreatesBondAsDebtor() throws ASAPException, SharkException, IOException, InterruptedException {
        // Setup Scenario
        this.setUpAliceBobExchangeScenario();

        ////////////////////////////////// bond specific tests start here
        // Create a bond: Creditor Alice, debtor Bob of 100 "Euro"
        this.bobComponent.createBond(ALICE_ID, BOB_ID, BOND_UNIT, BOND_AMOUNT, false);

        ///////////////////////////////// ASAP specific code - make an encounter Alice Bob
        this.runEncounter(this.bobPeer, this.alicePeer, true);

        Collection<SharkBond> aliceBondList = this.aliceComponent.getBondsByDebtor(BOB_ID);

        // Bond list can't be null
        Assert.assertNotNull(aliceBondList);
        Assert.assertFalse(aliceBondList.isEmpty());
        SharkBond bondFromBob = (SharkBond) aliceBondList.toArray()[0];

        // Bob must have a credit bond from Alice - he issued it by himself

        // Check if the correct bond was received
        Assert.assertNotNull(bondFromBob);
        Assert.assertNotNull(bondFromBob.getBondID());
        Assert.assertEquals(bondFromBob.getAmount(), BOND_AMOUNT);
        Assert.assertEquals(bondFromBob.getCreditorID(), ALICE_ID);
        Assert.assertEquals(bondFromBob.getDebtorID(), BOB_ID);
        Assert.assertEquals(bondFromBob.unitDescription(), BOND_UNIT);

        // Verify creditor signature
        Assert.assertTrue(SharkBondHelper.isSignedAsDebtor(bondFromBob, (SharkPKIComponent) this.alicePeer.getComponent(SharkPKIComponent.class)));

        // After receiving creditBond from Alice, Bob signs it as debtor and send it back to Alice
        // test results
        Collection<SharkBond> bobBondList = this.bobComponent.getBondsByCreditor(ALICE_ID);

        // Bond list can't be null
        Assert.assertNotNull(bobBondList);
        Assert.assertFalse(bobBondList.isEmpty());
        SharkBond bondFromAlice = (SharkBond) bobBondList.toArray()[0];

        Assert.assertNotNull(bondFromAlice);
        Assert.assertNotNull(bondFromAlice.getBondID());
        Assert.assertEquals(bondFromAlice.getAmount(), BOND_AMOUNT);
        Assert.assertEquals(bondFromAlice.getCreditorID(), ALICE_ID);
        Assert.assertEquals(bondFromAlice.getDebtorID(), BOB_ID);
        Assert.assertEquals(bondFromAlice.unitDescription(), BOND_UNIT);

        // Verify debtor signature
        Assert.assertTrue(SharkBondHelper.isSignedAsCreditor(bondFromAlice, (SharkPKIComponent) this.bobPeer.getComponent(SharkPKIComponent.class)));
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
