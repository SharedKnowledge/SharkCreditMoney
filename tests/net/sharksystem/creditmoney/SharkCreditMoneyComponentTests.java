package net.sharksystem.creditmoney;

import net.sharksystem.*;
import net.sharksystem.asap.ASAPChannel;
import net.sharksystem.asap.ASAPException;
import net.sharksystem.asap.ASAPSecurityException;
import net.sharksystem.asap.ASAPStorage;
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

        // set up backdoors
        this.aliceComponentImpl = (SharkMoneyComponentImpl) this.aliceComponent;
        this.bobComponentImpl = (SharkMoneyComponentImpl) this.bobComponent;
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
        CharSequence senderID = bobSenderList.get(0);
        Assert.assertTrue(alicePeer.samePeer(senderID));

        ASAPStorage senderIncomingStorage = bobAsapStorage.getExistingIncomingStorage(ALICE_ID);
        ASAPChannel channel = senderIncomingStorage.getChannel(SharkCreditMoneyComponent.SHARK_CREDIT_MONEY_ASKED_TO_SIGN_AS_DEBTOR_URI);
        byte[] message = channel.getMessages().getMessage(0, true);
        Assert.assertNotNull(message);

        SharkBond bond = SharkBondSerializer.deserializeCreditBond(message, (SharkPKIComponent) this.bobPeer.getComponent(SharkPKIComponent.class));
        Collection<SharkBond> bonds =  this.bobComponentImpl.getBondsByDebtor(BOB_ID);
        // Bob must have a credit bond from Alice - he issued it by himself
    }

    /**
     * Bob creates Bond as debtor. Alice accepts as creditor.
     */
    @Test
    public void bobCreatesBondAsDebtor() {
        // TODO
    }

    private class DummySharkBondReceivedListener implements SharkBondReceivedListener {
        private final SharkCreditMoneyComponent sharkCreditMoneyComponent;

        public DummySharkBondReceivedListener(SharkCreditMoneyComponent sharkCreditMoneyComponent) {
            this.sharkCreditMoneyComponent = sharkCreditMoneyComponent;
        }

        @Override
        public void sharkBondReceived(CharSequence uri) throws ASAPException, IOException, SharkCreditMoneyException {

        }

        @Override
        public void requestSignAsCreditor(SharkBond bond) throws ASAPException {
            System.out.println("Message received: " + bond);
        }

        @Override
        public void requestSignAsDebtor(SharkBond bond) throws ASAPException {
            System.out.println("Message received: " + bond);
        }

        @Override
        public void requestChangeCreditor(SharkBond bond) throws ASAPException {
            System.out.println("Message received: " + bond);
        }

        @Override
        public void requestChangeDebtor(SharkBond bond) throws ASAPException {
            System.out.println("Message received: " + bond);
        }

        @Override
        public void annulBond(SharkBond bond) throws SharkCreditMoneyException, ASAPException, IOException {

        }
    }

    /**
     * Bond (Alice (creditor), bob (deptor). Alice wants to change creditor to Clara.
     */

    /**
     * Bond (Alice (creditor), bob (deptor). Bob wants to change creditor to Clara.
     */

}
