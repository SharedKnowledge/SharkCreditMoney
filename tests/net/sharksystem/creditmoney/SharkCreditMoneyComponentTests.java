package net.sharksystem.creditmoney;

import net.sharksystem.*;
import net.sharksystem.asap.ASAPException;
import net.sharksystem.pki.SharkPKIComponent;
import net.sharksystem.pki.SharkPKIComponentFactory;
import org.junit.Test;

import java.io.IOException;
import java.util.Collection;

import static net.sharksystem.creditmoney.TestConstants.*;

public class SharkCreditMoneyComponentTests {
    private static final String THIS_ROOT_DIRECTORY = TestConstants.ROOT_DIRECTORY + SharkCreditMoneyComponent.class.getSimpleName() + "/";
    private static final String ALICE_FOLDER = THIS_ROOT_DIRECTORY + ALICE_NAME;
    public static final String BOB_FOLDER = THIS_ROOT_DIRECTORY + BOB_NAME;
    public static final String CLARA_FOLDER = THIS_ROOT_DIRECTORY + CLARA_NAME;
    public static final String DAVID_FOLDER = THIS_ROOT_DIRECTORY + DAVID_NAME;

    private SharkCreditMoneyComponent setupComponent(SharkPeer sharkPeer) throws SharkException {
    // certificate component required
        sharkPeer.addComponent(new SharkPKIComponentFactory(), SharkPKIComponent.class);

        // get certificate component
        SharkPKIComponent certificateComponent =
                (SharkPKIComponent) sharkPeer.getComponent(SharkPKIComponent.class);

        // create money factory ;)
        SharkCreditMoneyComponentFactory scmcf = new SharkCreditMoneyComponentFactory(certificateComponent);

        // shark money component required
        sharkPeer.addComponent(scmcf, SharkCreditMoneyComponent.class);

        SharkComponent component = sharkPeer.getComponent(SharkCreditMoneyComponent.class);

        SharkCreditMoneyComponent sharkCreditMoneyComponent = (SharkCreditMoneyComponent) component;

        return sharkCreditMoneyComponent;
    }

    /**
     * Test full roundtrip to create an bond: Alice creates a bond as creditor with debtor Bob. Bob receives and
     * signs this bond which becomes complete.
     */
    @Test
    public void aliceCreatesBondAsCreditor() throws SharkException, ASAPException, IOException, InterruptedException {
        SharkTestPeerFS.removeFolder(THIS_ROOT_DIRECTORY);

        // Setup alice peer
        SharkTestPeerFS aliceSharkPeer = new SharkTestPeerFS(ALICE_ID, ALICE_FOLDER);
        SharkCreditMoneyComponent aliceComponent = this.setupComponent(aliceSharkPeer);
        SharkBondReceivedListener aliceListener = new DummySharkBondReceivedListener();
        aliceComponent.subscribeBondReceivedListener(aliceListener);

        // Start alice peer
        aliceSharkPeer.start();

        // Setup bob peer
        SharkTestPeerFS.removeFolder(BOB_FOLDER);
        SharkTestPeerFS bobSharkPeer = new SharkTestPeerFS(BOB_NAME, BOB_FOLDER);
        SharkCreditMoneyComponent bobComponent = this.setupComponent(bobSharkPeer);
        SharkBondReceivedListener bobListener = new DummySharkBondReceivedListener();
        bobComponent.subscribeBondReceivedListener(bobListener);

        // Start bob peer
        bobSharkPeer.start();

        ///////////////////////////////// ASAP specific code - make an encounter Alice Bob
        aliceSharkPeer.getASAPTestPeerFS().startEncounter(7777, bobSharkPeer.getASAPTestPeerFS());

        // give them moment to exchange data
        Thread.sleep(2000);
        //Thread.sleep(Long.MAX_VALUE);
        System.out.println("slept a moment");

        ////////////////////////////////// bond specific tests start here
        // Create a bond: Creditor Alice, debtor Bob of 100 "Euro"
        aliceComponent.createBond(ALICE_ID, BOB_ID, BOND_UNIT, BOND_AMOUNT);
        
        // give them moment to exchange data
        Thread.sleep(2000);
        //Thread.sleep(Long.MAX_VALUE);
        System.out.println("slept a moment");

        Collection<SharkBond> bonds =  bobComponent.getBondsByDebtor(BOB_ID);
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
        @Override
        public void sharkBondReceived(SharkBond bond, CharSequence uri) throws ASAPException, IOException, SharkCreditMoneyException {
            System.out.println("Message received: " + bond);
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
    }

    /**
     * Bond (Alice (creditor), bob (deptor). Alice wants to change creditor to Clara.
     */

    /**
     * Bond (Alice (creditor), bob (deptor). Bob wants to change creditor to Clara.
     */

}
