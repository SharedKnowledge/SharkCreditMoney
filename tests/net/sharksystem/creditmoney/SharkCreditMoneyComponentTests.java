package net.sharksystem.creditmoney;

import net.sharksystem.*;
import net.sharksystem.asap.ASAPException;
import net.sharksystem.asap.apps.testsupport.ASAPTestPeerFS;
import org.junit.Assert;
import org.junit.jupiter.api.Test;

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
    public void createSignCreditBondAsCreditorAndSerializeTest() throws SharkException, ASAPException, IOException, InterruptedException {

        SharkTestPeerFS.removeFolder(THIS_ROOT_DIRECTORY);

        // Setup alice peer
        SharkTestPeerFS.removeFolder(ALICE_FOLDER);
        SharkTestPeerFS aliceSharkPeer = new SharkTestPeerFS(ALICE_ID, ALICE_FOLDER);
        SharkCreditMoneyComponent aliceComponent = this.setupComponent(aliceSharkPeer);

        // Start alice peer
        aliceSharkPeer.start();

        // Set alice component behavior
        aliceComponent.setBehaviour(SharkCreditMoneyComponent.BEHAVIOUR_SHARK_MONEY_ALLOW_TRANSFER, true);

        InMemoSharkCreditBond creditBond = new InMemoSharkCreditBond(ALICE_ID, BOB_ID, BOND_UNIT, BOND_AMOUNT, BOND_ALLOW_TRANSFER);
        creditBond.setCreditorSignature(aliceComponent.signBond((SharkCertificateComponent) aliceSharkPeer.getComponent(SharkCertificateComponent.class), creditBond));

        byte[] serializedSCreditBond = InMemoSharkCreditBond.serializeCreditBond(creditBond);

        Assert.assertEquals(BOND_UNIT, creditBond.unitDescription());
        Assert.assertEquals(BOND_AMOUNT, creditBond.getAmount());
        Assert.assertEquals(BOND_ALLOW_TRANSFER, creditBond.allowedToChangeCreditor());
        Assert.assertEquals(BOND_ALLOW_TRANSFER, creditBond.allowedToChangeDebtor());
    }
}
