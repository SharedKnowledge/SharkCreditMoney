package net.sharksystem.creditmoney;

import net.sharksystem.*;
import net.sharksystem.asap.ASAPException;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static net.sharksystem.creditmoney.TestConstants.*;

public class SharkCreditMoneyComponentTests {
    private static final String THIS_ROOT_DIRECTORY = TestConstants.ROOT_DIRECTORY + SharkCreditMoneyComponent.class.getSimpleName() + "/";
    private static final String ALICE_FOLDER = THIS_ROOT_DIRECTORY + ALICE_NAME;
    public static final String BOB_FOLDER = THIS_ROOT_DIRECTORY + BOB_NAME;
    public static final String CLARA_FOLDER = THIS_ROOT_DIRECTORY + CLARA_NAME;
    public static final String DAVID_FOLDER = THIS_ROOT_DIRECTORY + DAVID_NAME;

    private SharkCreditMoneyComponent setupComponent(SharkPeer sharkPeer) throws SharkException {

        // certificate component required
        sharkPeer.addComponent(new SharkCertificateComponentFactory(), SharkCertificateComponent.class);

        // get certificate component
        SharkCertificateComponent certificateComponent =
                (SharkCertificateComponent) sharkPeer.getComponent(SharkCertificateComponent.class);

        // create money factory ;)
        SharkCreditMoneyComponentFactory scmcf = new SharkCreditMoneyComponentFactory(certificateComponent);

        // shark money component required
        sharkPeer.addComponent(scmcf, SharkCreditMoneyComponent.class);

        SharkComponent component = sharkPeer.getComponent(SharkCreditMoneyComponent.class);

        SharkCreditMoneyComponent sharkCreditMoneyComponent = (SharkCreditMoneyComponent) component;

        return sharkCreditMoneyComponent;
    }

    @Test
    public void sendReceiveCreditBondSignAndResend() throws SharkException, ASAPException, IOException, InterruptedException {
        SharkTestPeerFS.removeFolder(THIS_ROOT_DIRECTORY);

        // Setup alice peer
        SharkTestPeerFS aliceSharkPeer = new SharkTestPeerFS(ALICE_ID, ALICE_FOLDER);
        SharkCreditMoneyComponent aliceComponent = this.setupComponent(aliceSharkPeer);

        // Start alice peer
        aliceSharkPeer.start();

        // Set alice component behavior
        aliceComponent.setBehaviour(SharkCreditMoneyComponent.BEHAVIOUR_SHARK_MONEY_ALLOW_TRANSFER, true);

        // Setup bob peer
        SharkTestPeerFS.removeFolder(BOB_FOLDER);
        SharkTestPeerFS bobSharkPeer = new SharkTestPeerFS(BOB_NAME, BOB_FOLDER);
        SharkCreditMoneyComponent bobComponent = this.setupComponent(bobSharkPeer);

        // Start bob peer
        bobSharkPeer.start();

        // Create, sign and send bond to bob
        aliceComponent.createBond(ALICE_ID, BOB_ID, "EURO", 20);

        aliceSharkPeer.getASAPTestPeerFS().startEncounter(7777, bobSharkPeer.getASAPTestPeerFS());

        // give them moment to exchange data
        Thread.sleep(1000);
        //Thread.sleep(Long.MAX_VALUE);
        System.out.println("slept a moment");

        // Bob must have a credit bond from Alice - he issued it by himself

    }
}
