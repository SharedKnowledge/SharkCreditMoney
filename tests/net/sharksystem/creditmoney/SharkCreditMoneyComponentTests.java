package net.sharksystem.creditmoney;

import net.sharksystem.*;
import net.sharksystem.asap.ASAPException;
import net.sharksystem.asap.ASAPSecurityException;
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
        SharkCertificateComponent aliceCertificationComponent = (SharkCertificateComponent) aliceSharkPeer.getComponent(SharkCertificateComponent.class);

        // Start alice peer
        aliceSharkPeer.start();

        // Set alice component behavior
        aliceComponent.setBehaviour(SharkCreditMoneyComponent.BEHAVIOUR_SHARK_MONEY_ALLOW_TRANSFER, true);

        InMemoSharkCreditBond creditBond = new InMemoSharkCreditBond(ALICE_ID, BOB_ID, BOND_UNIT, BOND_AMOUNT, BOND_ALLOW_TRANSFER);
        creditBond.setCreditorSignature(aliceComponent.signBond(aliceCertificationComponent, creditBond));

        byte[] serializedSCreditBond = InMemoSharkCreditBond.serializeCreditBond(creditBond);

        Assert.assertEquals(ALICE_ID, creditBond.getCreditor());
        Assert.assertEquals(BOB_ID, creditBond.getDebtor());
        Assert.assertEquals(BOND_UNIT, creditBond.unitDescription());
        Assert.assertEquals(BOND_AMOUNT, creditBond.getAmount());
        Assert.assertEquals(BOND_ALLOW_TRANSFER, creditBond.allowedToChangeCreditor());
        Assert.assertEquals(BOND_ALLOW_TRANSFER, creditBond.allowedToChangeDebtor());

        // Check if serialized bond is not null
        Assert.assertNotNull(serializedSCreditBond);

        // Check creditor signature
        Assert.assertTrue(aliceComponent.isCreditorSignatureCorrect(creditBond, aliceCertificationComponent));
    }

    @Test
    public void createSignCreditBondSerializeDeserializeTest() throws SharkException, ASAPSecurityException, IOException {
        SharkTestPeerFS.removeFolder(THIS_ROOT_DIRECTORY);

        // Setup alice peer
        SharkTestPeerFS.removeFolder(ALICE_FOLDER);
        SharkTestPeerFS aliceSharkPeer = new SharkTestPeerFS(ALICE_ID, ALICE_FOLDER);
        SharkCreditMoneyComponent aliceComponent = this.setupComponent(aliceSharkPeer);
        SharkCertificateComponent aliceCertificationComponent = (SharkCertificateComponent) aliceSharkPeer.getComponent(SharkCertificateComponent.class);

        // Start alice peer
        aliceSharkPeer.start();

        // Set alice component behavior
        aliceComponent.setBehaviour(SharkCreditMoneyComponent.BEHAVIOUR_SHARK_MONEY_ALLOW_TRANSFER, true);

        InMemoSharkCreditBond creditBond = new InMemoSharkCreditBond(ALICE_ID, BOB_ID, BOND_UNIT, BOND_AMOUNT, BOND_ALLOW_TRANSFER);
        creditBond.setCreditorSignature(aliceComponent.signBond(aliceCertificationComponent, creditBond));

        byte[] serializedSCreditBond = InMemoSharkCreditBond.serializeCreditBond(creditBond);

        // Deserialize bond and check if all infos are still correct
        InMemoSharkCreditBond deserializedCreditBond = InMemoSharkCreditBond.deserializeCreditBond(serializedSCreditBond);

        Assert.assertEquals(deserializedCreditBond.unitDescription(), creditBond.unitDescription());
        Assert.assertEquals(deserializedCreditBond.getAmount(), creditBond.getAmount());
        Assert.assertEquals(deserializedCreditBond.allowedToChangeCreditor(), creditBond.allowedToChangeCreditor());
        Assert.assertEquals(deserializedCreditBond.allowedToChangeDebtor(), creditBond.allowedToChangeDebtor());
    }

    @Test
    public void createSignCreditBondAsCreditorAndSignAsDebtorTest() throws SharkException, ASAPSecurityException {
        SharkTestPeerFS.removeFolder(THIS_ROOT_DIRECTORY);

        // Setup alice peer
        SharkTestPeerFS.removeFolder(ALICE_FOLDER);
        SharkTestPeerFS aliceSharkPeer = new SharkTestPeerFS(ALICE_ID, ALICE_FOLDER);
        SharkCreditMoneyComponent aliceComponent = this.setupComponent(aliceSharkPeer);
        SharkCertificateComponent aliceCertificationComponent = (SharkCertificateComponent) aliceSharkPeer.getComponent(SharkCertificateComponent.class);

        // Start alice peer
        aliceSharkPeer.start();

        // Set alice component behavior
        aliceComponent.setBehaviour(SharkCreditMoneyComponent.BEHAVIOUR_SHARK_MONEY_ALLOW_TRANSFER, true);

        // Setup bob peer
        SharkTestPeerFS.removeFolder(BOB_FOLDER);
        SharkTestPeerFS bobSharkPeer = new SharkTestPeerFS(BOB_ID, BOB_FOLDER);
        SharkCreditMoneyComponent bobComponent = this.setupComponent(bobSharkPeer);
        SharkCertificateComponent bobCertificationComponent = (SharkCertificateComponent) bobSharkPeer.getComponent(SharkCertificateComponent.class);

        // Start bob peer
        bobSharkPeer.start();

        // Set bob component behavior
        bobComponent.setBehaviour(SharkCreditMoneyComponent.BEHAVIOUR_SHARK_MONEY_ALLOW_TRANSFER, true);


        InMemoSharkCreditBond creditBond = new InMemoSharkCreditBond(ALICE_ID, BOB_ID, BOND_UNIT, BOND_AMOUNT, BOND_ALLOW_TRANSFER);
        creditBond.setCreditorSignature(aliceComponent.signBond(aliceCertificationComponent, creditBond));
        creditBond.setDebtorSignature(bobComponent.signBond(bobCertificationComponent, creditBond));

        byte[] serializedSCreditBond = InMemoSharkCreditBond.serializeCreditBond(creditBond);

        Assert.assertEquals(ALICE_ID, creditBond.getCreditor());
        Assert.assertEquals(BOB_ID, creditBond.getDebtor());
        Assert.assertEquals(BOND_UNIT, creditBond.unitDescription());
        Assert.assertEquals(BOND_AMOUNT, creditBond.getAmount());
        Assert.assertEquals(BOND_ALLOW_TRANSFER, creditBond.allowedToChangeCreditor());
        Assert.assertEquals(BOND_ALLOW_TRANSFER, creditBond.allowedToChangeDebtor());

        // Check if serialized bond is not null
        Assert.assertNotNull(serializedSCreditBond);

        // Check creditor signature
        Assert.assertTrue(aliceComponent.isCreditorSignatureCorrect(creditBond, aliceCertificationComponent));
        Assert.assertTrue(bobComponent.isDebtorSignatureCorrect(creditBond, bobCertificationComponent));
    }

    @Test
    public void transferCreditBondToNewCreditor() throws SharkException, ASAPSecurityException {
        SharkTestPeerFS.removeFolder(THIS_ROOT_DIRECTORY);

        // Setup alice peer
        SharkTestPeerFS.removeFolder(ALICE_FOLDER);
        SharkTestPeerFS aliceSharkPeer = new SharkTestPeerFS(ALICE_ID, ALICE_FOLDER);
        SharkCreditMoneyComponent aliceComponent = this.setupComponent(aliceSharkPeer);
        SharkCertificateComponent aliceCertificationComponent = (SharkCertificateComponent) aliceSharkPeer.getComponent(SharkCertificateComponent.class);

        // Start alice peer
        aliceSharkPeer.start();

        // Set alice component behavior
        aliceComponent.setBehaviour(SharkCreditMoneyComponent.BEHAVIOUR_SHARK_MONEY_ALLOW_TRANSFER, true);

        // Setup bob peer
        SharkTestPeerFS.removeFolder(BOB_FOLDER);
        SharkTestPeerFS bobSharkPeer = new SharkTestPeerFS(BOB_ID, BOB_FOLDER);
        SharkCreditMoneyComponent bobComponent = this.setupComponent(bobSharkPeer);
        SharkCertificateComponent bobCertificationComponent = (SharkCertificateComponent) bobSharkPeer.getComponent(SharkCertificateComponent.class);

        // Start bob peer
        bobSharkPeer.start();

        // Set bob component behavior
        bobComponent.setBehaviour(SharkCreditMoneyComponent.BEHAVIOUR_SHARK_MONEY_ALLOW_TRANSFER, true);

        // Setup bob peer
        SharkTestPeerFS.removeFolder(CLARA_ID);
        SharkTestPeerFS claraSharkPeer = new SharkTestPeerFS(CLARA_ID, CLARA_FOLDER);
        SharkCreditMoneyComponent claraComponent = this.setupComponent(bobSharkPeer);
        SharkCertificateComponent claraCertificationComponent = (SharkCertificateComponent) claraSharkPeer.getComponent(SharkCertificateComponent.class);

        // Start clara peer
        claraSharkPeer.start();

        // Set clara component behavior
        claraComponent.setBehaviour(SharkCreditMoneyComponent.BEHAVIOUR_SHARK_MONEY_ALLOW_TRANSFER, true);

        InMemoSharkCreditBond creditBond = new InMemoSharkCreditBond(ALICE_ID, BOB_ID, BOND_UNIT, BOND_AMOUNT, BOND_ALLOW_TRANSFER);
        creditBond.setCreditorSignature(aliceComponent.signBond(aliceCertificationComponent, creditBond));
        creditBond.setDebtorSignature(bobComponent.signBond(bobCertificationComponent, creditBond));

        Assert.assertEquals(ALICE_ID, creditBond.getCreditor());
        Assert.assertEquals(BOB_ID, creditBond.getDebtor());

        // Check creditor & debtor signature
        Assert.assertTrue(aliceComponent.isCreditorSignatureCorrect(creditBond, aliceCertificationComponent));
        Assert.assertTrue(bobComponent.isDebtorSignatureCorrect(creditBond, bobCertificationComponent));

        // transfer credit bond to new creditor
        creditBond.setDebtor(new PersonImpl(CLARA_ID));
        creditBond.setDebtorSignature(claraComponent.signBond(claraCertificationComponent, creditBond));

        Assert.assertEquals(CLARA_ID, creditBond.getDebtor());

        // Check debtor signature
        Assert.assertTrue(claraComponent.isDebtorSignatureCorrect(creditBond, claraCertificationComponent));
    }

    @Test
    public void transferCreditBondToNewDebtor() {

    }
}
