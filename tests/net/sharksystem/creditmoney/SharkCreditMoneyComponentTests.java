package net.sharksystem.creditmoney;

import net.sharksystem.*;
import net.sharksystem.asap.ASAPException;
import net.sharksystem.asap.ASAPSecurityException;
import net.sharksystem.asap.pki.CredentialMessageInMemo;
import net.sharksystem.pki.SharkPKIComponent;
import org.junit.Assert;
import org.junit.Test;
import java.io.IOException;
import java.util.Collection;
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

    private void setUpAliceBobExchangeScenario() throws SharkException, ASAPSecurityException, IOException {
        // Setup alice peer
        SharkTestPeerFS.removeFolder(ALICE_FOLDER);
        this.alicePeer = new SharkTestPeerFS(ALICE_ID, ALICE_FOLDER);
        TestHelper.setupComponent(this.alicePeer);

        // Setup bob peer
        SharkTestPeerFS.removeFolder(BOB_FOLDER);
        this.bobPeer = new SharkTestPeerFS(BOB_ID, BOB_FOLDER);
        TestHelper.setupComponent(this.bobPeer);

        // Start alice peer
        this.alicePeer.start();
        // Start bob peer
        this.bobPeer.start();

        // add some keys as described in scenario settings
        SharkPKIComponent alicePKI = (SharkPKIComponent) this.alicePeer.getComponent(SharkPKIComponent.class);
        SharkPKIComponent bobPKI = (SharkPKIComponent) this.bobPeer.getComponent(SharkPKIComponent.class);

        // let Bob accept ALice credentials and create a certificate
        CredentialMessageInMemo aliceCredentialMessage = new CredentialMessageInMemo(ALICE_ID, ALICE_NAME, System.currentTimeMillis(), alicePKI.getPublicKey());
        bobPKI.acceptAndSignCredential(aliceCredentialMessage);

        // Alice accepts Bob Public Key
        CredentialMessageInMemo bobCredentialMessage = new CredentialMessageInMemo(BOB_ID, BOB_NAME, System.currentTimeMillis(), bobPKI.getPublicKey());
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

    private void setUpAliceBobClaraExchangeScenario() throws SharkException, ASAPSecurityException, IOException {
        // Setup alice peer
        SharkTestPeerFS.removeFolder(ALICE_FOLDER);
        this.alicePeer = new SharkTestPeerFS(ALICE_ID, ALICE_FOLDER);
        TestHelper.setupComponent(this.alicePeer);

        // Setup bob peer
        SharkTestPeerFS.removeFolder(BOB_FOLDER);
        this.bobPeer = new SharkTestPeerFS(BOB_ID, BOB_FOLDER);
        TestHelper.setupComponent(this.bobPeer);

        // Setup clara peer
        SharkTestPeerFS.removeFolder(CLARA_FOLDER);
        this.claraPeer = new SharkTestPeerFS(CLARA_ID, CLARA_FOLDER);
        TestHelper.setupComponent(this.claraPeer);

        // Start alice peer
        this.alicePeer.start();
        // Start bob peer
        this.bobPeer.start();
        // Start clara peer
        this.claraPeer.start();

        // add some keys as described in scenario settings
        SharkPKIComponent alicePKI = (SharkPKIComponent) this.alicePeer.getComponent(SharkPKIComponent.class);
        SharkPKIComponent bobPKI = (SharkPKIComponent) this.bobPeer.getComponent(SharkPKIComponent.class);
        SharkPKIComponent claraPKI = (SharkPKIComponent) this.claraPeer.getComponent(SharkPKIComponent.class);

        // create credential messages
        CredentialMessageInMemo aliceCredentialMessage = new CredentialMessageInMemo(ALICE_ID, ALICE_NAME, System.currentTimeMillis(), alicePKI.getPublicKey());
        CredentialMessageInMemo bobCredentialMessage = new CredentialMessageInMemo(BOB_ID, BOB_NAME, System.currentTimeMillis(), bobPKI.getPublicKey());
        CredentialMessageInMemo claraCredentialMessage = new CredentialMessageInMemo(CLARA_ID, CLARA_NAME, System.currentTimeMillis(), claraPKI.getPublicKey());

        // a) Alice, Bob and clara exchange and accept credential messages and issue certificates
        alicePKI.acceptAndSignCredential(bobCredentialMessage);
        alicePKI.acceptAndSignCredential(claraCredentialMessage);


        // b) Bob and Clara meet, accept credential messages and issue certificates

        bobPKI.acceptAndSignCredential(aliceCredentialMessage);
        bobPKI.acceptAndSignCredential(claraCredentialMessage);

        // c) Clara, Bob and Alice exchange and accept credential
        claraPKI.acceptAndSignCredential(aliceCredentialMessage);
        claraPKI.acceptAndSignCredential(bobCredentialMessage);

        ///////////// check stability of SharkPKI - just in case - it is a copy of a test in this project:
        // check identity assurance
        int iaAliceSideBob = alicePKI.getIdentityAssurance(BOB_ID);
        int iaAliceSideClara = alicePKI.getIdentityAssurance(CLARA_ID);

        int iaBobSideAlice = bobPKI.getIdentityAssurance(ALICE_ID);
        int iaBobSideClara = bobPKI.getIdentityAssurance(CLARA_ID);

        int iaClaraSideAlice = claraPKI.getIdentityAssurance(ALICE_ID);
        int iaClaraSideBob = claraPKI.getIdentityAssurance(BOB_ID);

        Assert.assertEquals(10, iaAliceSideBob); // met
        System.out.println("10 - okay, Alice met Bob");
        Assert.assertEquals(10, iaAliceSideClara); // met
        System.out.println("10 - okay, Alice knows Clara");
        Assert.assertEquals(10, iaBobSideAlice); // met
        System.out.println("10 - okay, Bob met Alice");
        Assert.assertEquals(10, iaBobSideClara); // met
        System.out.println("10 - okay, Bob met Clara");
        Assert.assertEquals(10, iaClaraSideAlice); // met
        System.out.println("10 - okay, Clara met Alice");
        Assert.assertEquals(10, iaClaraSideBob); // met
        System.out.println("10 - okay, Clara met Bob");

        System.out.println("********************************************************************");
        System.out.println("**                          PKI works                             **");
        System.out.println("********************************************************************");

        this.aliceComponent = (SharkCreditMoneyComponent) this.alicePeer.getComponent(SharkCreditMoneyComponent.class);
        this.bobComponent = (SharkCreditMoneyComponent) this.bobPeer.getComponent(SharkCreditMoneyComponent.class);
        this.claraComponent = (SharkCreditMoneyComponent) this.claraPeer.getComponent(SharkCreditMoneyComponent.class);


        // Add SharkBondReceivedListener Implementation
        SharkBondsReceivedListener aliceListener = new DummySharkBondReceivedListener(this.aliceComponent);
        this.aliceComponent.subscribeBondReceivedListener(aliceListener);

        SharkBondsReceivedListener bobListener = new DummySharkBondReceivedListener(this.bobComponent);
        this.bobComponent.subscribeBondReceivedListener(bobListener);

        SharkBondsReceivedListener claraListener = new DummySharkBondReceivedListener(this.claraComponent);
        this.claraComponent.subscribeBondReceivedListener(claraListener);

        // set up backdoors
        this.aliceComponentImpl = (SharkCreditMoneyComponentImpl) this.aliceComponent;
        this.bobComponentImpl = (SharkCreditMoneyComponentImpl) this.bobComponent;
        this.claraComponentImpl = (SharkCreditMoneyComponentImpl) this.claraComponent;
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
    public void aliceTestCreatesBondAsCreditor() throws SharkException, ASAPException, IOException, InterruptedException {
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

        // Stop alice peer
        this.alicePeer.stop();
        // Stop bob peer
        this.bobPeer.stop();
    }

    /**
     * Bob creates Bond as debtor. Alice accepts as creditor.
     */
    @Test
    public void bobTestCreatesBondAsDebtor() throws ASAPException, SharkException, IOException, InterruptedException {
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

        // Stop alice peer
        this.alicePeer.stop();
        // Stop bob peer
        this.bobPeer.stop();
    }

    /**
     * Bond (Alice (creditor), bob (debtor). Alice wants to change creditor to Clara.
     */
    @Test
    public void aliceWantBondCreditorTestTransferToClara() throws ASAPException, SharkException, IOException, InterruptedException {
        // Setup Scenario
        this.setUpAliceBobClaraExchangeScenario();

        // Create a bond: Creditor Alice, debtor Bob of 100 "Euro"
        this.aliceComponent.createBond(ALICE_ID, BOB_ID, BOND_UNIT, BOND_AMOUNT, true);

        ///////////////////////////////// ASAP specific code - make an encounter Alice Bob
        this.runEncounter(this.alicePeer, this.bobPeer, true);

        // Once the created bond was exchanged and saved by alice and bob, we can now start a bond transfer

        Collection<SharkBond> aliceBondList = this.aliceComponent.getBondsByCreditor(ALICE_ID);
        SharkBond aliceBond = (SharkBond) aliceBondList.toArray()[0];

        // transfer bond creditor to clara
        aliceBond.setTempCreditorID(CLARA_ID);
        this.aliceComponent.transferBond(aliceBond, true);

        ///////////////////////////////// ASAP specific code - make an encounter Alice Bob
        this.runEncounter(this.alicePeer, this.bobPeer, true);
        this.runEncounter(this.alicePeer, this.claraPeer, true);

        // The transfer of the bond should now be fulfill
        // Check if bob and clara have the correct bond
        Collection<SharkBond> bobBondList = this.bobComponent.getBondsByCreditor(CLARA_ID);

        // bobBond's list can't be null
        Assert.assertNotNull(bobBondList);
        Assert.assertFalse(bobBondList.isEmpty());
        SharkBond bobBond = (SharkBond) bobBondList.toArray()[0];

        Assert.assertNotNull(bobBond);
        Assert.assertNotNull(bobBond.getBondID());
        Assert.assertEquals(bobBond.getBondID(), aliceBond.getBondID());
        Assert.assertEquals(bobBond.getAmount(), aliceBond.getAmount());
        Assert.assertEquals(bobBond.unitDescription(), aliceBond.unitDescription());
        Assert.assertEquals(bobBond.getCreditorID(), CLARA_ID);
        Assert.assertEquals(bobBond.getDebtorID(), BOB_ID);

        // Verify debtor signature
        Assert.assertTrue(SharkBondHelper.isSignedAsDebtor(bobBond, (SharkPKIComponent) this.bobPeer.getComponent(SharkPKIComponent.class)));

        // The transfer of the bond should now be fulfill
        // Check if bob and clara have the correct bond
        Collection<SharkBond> claraBondList = this.claraComponent.getBondsByCreditor(CLARA_ID);

        // claraBond's list can't be null
        Assert.assertNotNull(claraBondList);
        Assert.assertFalse(claraBondList.isEmpty());
        SharkBond claraBond = (SharkBond) claraBondList.toArray()[0];

        Assert.assertNotNull(claraBond);
        Assert.assertNotNull(claraBond.getBondID());
        Assert.assertEquals(claraBond.getBondID(), aliceBond.getBondID());
        Assert.assertEquals(claraBond.getAmount(), aliceBond.getAmount());
        Assert.assertEquals(claraBond.unitDescription(), aliceBond.unitDescription());
        Assert.assertEquals(claraBond.getCreditorID(), CLARA_ID);
        Assert.assertEquals(claraBond.getDebtorID(), aliceBond.getDebtorID());

        // Verify creditor signature
        Assert.assertTrue(SharkBondHelper.isSignedAsCreditor(claraBond, (SharkPKIComponent) this.claraPeer.getComponent(SharkPKIComponent.class)));
    }

    /**
     * Bond (Alice (creditor), bob (deptor). Bob wants to change creditor to Clara.
     */
    @Test
    public void bobWantBondDebtorTestTransferToClara() throws SharkException, IOException, ASAPException, InterruptedException {
        // Setup Scenario
        this.setUpAliceBobClaraExchangeScenario();

        // Create a bond: Creditor Alice, debtor Bob of 100 "Euro"
        this.aliceComponent.createBond(ALICE_ID, BOB_ID, BOND_UNIT, BOND_AMOUNT, true);

        ///////////////////////////////// ASAP specific code - make an encounter Alice Bob
        this.runEncounter(this.alicePeer, this.bobPeer, true);

        // Once the created bond was exchanged and saved by alice and bob, we can now start a bond transfer

        Collection<SharkBond> bobBondList = this.aliceComponent.getBondsByDebtor(BOB_ID);
        SharkBond bobBond = (SharkBond) bobBondList.toArray()[0];

        // transfer bond creditor to clara
        bobBond.setTempDebtorID(CLARA_ID);
        this.bobComponent.transferBond(bobBond, false);

        ///////////////////////////////// ASAP specific code - make an encounter Alice Bob
        this.runEncounter(this.claraPeer, this.bobPeer, true);
        this.runEncounter(this.alicePeer, this.claraPeer, true);

        // The transfer of the bond should now be fulfill
        // Check if bob and clara have the correct bond
        Collection<SharkBond> aliceBondList = this.aliceComponent.getBondsByDebtor(CLARA_ID);

        // bobBond's list can't be null
        Assert.assertNotNull(aliceBondList);
        Assert.assertFalse(aliceBondList.isEmpty());
        SharkBond aliceBond = (SharkBond) aliceBondList.toArray()[0];

        Assert.assertNotNull(aliceBond);
        Assert.assertNotNull(aliceBond.getBondID());
        Assert.assertEquals(aliceBond.getBondID(), bobBond.getBondID());
        Assert.assertEquals(aliceBond.getAmount(), bobBond.getAmount());
        Assert.assertEquals(aliceBond.unitDescription(), bobBond.unitDescription());
        Assert.assertEquals(aliceBond.getCreditorID(), bobBond.getCreditorID());
        Assert.assertEquals(aliceBond.getDebtorID(), CLARA_ID);

        // Verify creditor signature
        Assert.assertTrue(SharkBondHelper.isSignedAsCreditor(aliceBond, (SharkPKIComponent) this.alicePeer.getComponent(SharkPKIComponent.class)));

        // The transfer of the bond should now be fulfill
        // Check if bob and clara have the correct bond
        Collection<SharkBond> claraBondList = this.claraComponent.getBondsByDebtor(CLARA_ID);

        // claraBond's list can't be null
        Assert.assertNotNull(claraBondList);
        Assert.assertFalse(claraBondList.isEmpty());
        SharkBond claraBond = (SharkBond) claraBondList.toArray()[0];

        Assert.assertNotNull(claraBond);
        Assert.assertNotNull(claraBond.getBondID());
        Assert.assertEquals(claraBond.getBondID(), aliceBond.getBondID());
        Assert.assertEquals(claraBond.getAmount(), aliceBond.getAmount());
        Assert.assertEquals(claraBond.unitDescription(), aliceBond.unitDescription());
        Assert.assertEquals(claraBond.getCreditorID(), aliceBond.getCreditorID());
        Assert.assertEquals(claraBond.getDebtorID(), CLARA_ID);

        // Verify debtor signature
        Assert.assertTrue(SharkBondHelper.isSignedAsDebtor(claraBond, (SharkPKIComponent) this.claraPeer.getComponent(SharkPKIComponent.class)));

    }
}
