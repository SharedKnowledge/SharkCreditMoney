package net.sharksystem.creditmoney;

import net.sharksystem.*;
import net.sharksystem.asap.ASAPException;
import net.sharksystem.asap.ASAPSecurityException;
import net.sharksystem.asap.pki.CredentialMessageInMemo;
import net.sharksystem.pki.SharkPKIComponent;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import static net.sharksystem.creditmoney.TestConstants.*;


public class SharkBondSerializerAndHelperTests {
    private static final String THIS_ROOT_DIRECTORY = ROOT_DIRECTORY + SharkCreditMoneyComponentTests.class.getSimpleName() + "/";
    private static final String ALICE_FOLDER = THIS_ROOT_DIRECTORY + ALICE_NAME;
    public static final String BOB_FOLDER = THIS_ROOT_DIRECTORY + BOB_NAME;
    private SharkTestPeerFS alicePeer;
    private SharkTestPeerFS bobPeer;
    private SharkCreditMoneyComponent aliceComponent;
    private SharkCreditMoneyComponent bobComponent;
    SharkPKIComponent alicePKI;
    SharkPKIComponent bobPKI;

    private void setUpSharkBondFunctionalitiesScenario() throws SharkException, IOException, ASAPSecurityException {
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

        // Get pki keys from peers
        this.alicePKI = (SharkPKIComponent) this.alicePeer.getComponent(SharkPKIComponent.class);
        this.bobPKI = (SharkPKIComponent) this.bobPeer.getComponent(SharkPKIComponent.class);
    }

    @Test
    public void bondTestUserSignaturesForCreditorAndDebtor() throws SharkException, IOException, ASAPSecurityException {
        this.setUpSharkBondFunctionalitiesScenario();
        SharkBond sharkBond = new InMemoSharkBond(ALICE_ID, BOB_ID, BOND_UNIT, BOND_AMOUNT, false);

        // Sign the bond as creditor
        SharkBondHelper.signAsCreditor(alicePKI, sharkBond, false);

        // Verify creditor signature
        Assert.assertNotNull(sharkBond.getCreditorSignature());
        Assert.assertTrue(SharkBondHelper.isSignedAsCreditor(sharkBond, alicePKI));

        // Sign the bond as debtor
        SharkBondHelper.signAsDebtor(bobPKI, sharkBond, false);

        // Verify debtor signature
        Assert.assertNotNull(sharkBond.getDebtorSignature());
        Assert.assertTrue(SharkBondHelper.isSignedAsDebtor(sharkBond, bobPKI));
    }

    @Test
    public void bondTestAnnulBondByCreditorAndDebtor() throws ASAPException, SharkException, IOException {
        this.setUpSharkBondFunctionalitiesScenario();
        SharkBond sharkBond = new InMemoSharkBond(ALICE_ID, BOB_ID, BOND_UNIT, BOND_AMOUNT, false);
        Set<CharSequence> receiver = new HashSet<>();
        receiver.add(sharkBond.getDebtorID());

        // Check the bond is not yet annulled
        Assert.assertFalse(sharkBond.isAnnulled());

        // Annul bond as creditor
        SharkBondHelper.annulBond(alicePKI, sharkBond);

        // The bond was annulled by creditor, but is still not completely annulled
        Assert.assertTrue(sharkBond.getBondIsAnnulledByCreditor());
        Assert.assertFalse(sharkBond.isAnnulled());

        // Now let us annul the bond as debtor
        SharkBondHelper.annulBond(bobPKI, sharkBond);

        // Now the bond is fully annulled
        Assert.assertTrue(sharkBond.getBondIsAnnulledByCreditor());
        Assert.assertTrue(sharkBond.isAnnulled());
    }

    @Test
    public void serializationTestBondPlain() throws SharkException, IOException, ASAPException {
        this.setUpSharkBondFunctionalitiesScenario();
        SharkBond sharkBond = new InMemoSharkBond(ALICE_ID, BOB_ID, BOND_UNIT, BOND_AMOUNT, false);
        Set<CharSequence> receiver = new HashSet<>();
        receiver.add(sharkBond.getDebtorID());

        // Check the bond data
        Assert.assertNotNull(sharkBond);
        Assert.assertNotNull(sharkBond.getBondID());
        Assert.assertEquals(sharkBond.getAmount(), BOND_AMOUNT);
        Assert.assertEquals(sharkBond.getCreditorID(), ALICE_ID);
        Assert.assertEquals(sharkBond.getDebtorID(), BOB_ID);
        Assert.assertEquals(sharkBond.unitDescription(), BOND_UNIT);

        // Serialize bond as plaintext
        byte[] sharkBondBytes = SharkBondSerializer.serializeCreditBond(sharkBond, sharkBond.getCreditorID(), receiver, false, false, alicePKI, false, 0);

        Assert.assertNotNull(sharkBondBytes);

        // Deserialize bondBytes
        SharkBond deserializedSharkBond = SharkBondSerializer.deserializeCreditBond(sharkBondBytes, bobPKI);

        // Check deserialized bond data
        Assert.assertNotNull(deserializedSharkBond);
        Assert.assertEquals(deserializedSharkBond.getBondID(), sharkBond.getBondID());
        Assert.assertEquals(deserializedSharkBond.getAmount(), BOND_AMOUNT);
        Assert.assertEquals(deserializedSharkBond.getCreditorID(), ALICE_ID);
        Assert.assertEquals(deserializedSharkBond.getDebtorID(), BOB_ID);
        Assert.assertEquals(deserializedSharkBond.unitDescription(), BOND_UNIT);
        Assert.assertEquals(deserializedSharkBond.getExpirationDate(), sharkBond.getExpirationDate());
    }

    @Test
    public void serializationTestBondSigned() throws SharkException, IOException, ASAPException {
        this.setUpSharkBondFunctionalitiesScenario();
        SharkBond sharkBond = new InMemoSharkBond(ALICE_ID, BOB_ID, BOND_UNIT, BOND_AMOUNT, false);
        Set<CharSequence> receiver = new HashSet<>();
        receiver.add(sharkBond.getDebtorID());

        // Check the bond data
        Assert.assertNotNull(sharkBond);
        Assert.assertNotNull(sharkBond.getBondID());
        Assert.assertEquals(sharkBond.getAmount(), BOND_AMOUNT);
        Assert.assertEquals(sharkBond.getCreditorID(), ALICE_ID);
        Assert.assertEquals(sharkBond.getDebtorID(), BOB_ID);
        Assert.assertEquals(sharkBond.unitDescription(), BOND_UNIT);

        // Serialize bond as signed bond
        byte[] sharkBondBytes = SharkBondSerializer.serializeCreditBond(sharkBond, sharkBond.getCreditorID(), receiver, true, false, alicePKI, false, 0);

        Assert.assertNotNull(sharkBondBytes);

        // Deserialize bondBytes
        SharkBond deserializedSharkBond = SharkBondSerializer.deserializeCreditBond(sharkBondBytes, bobPKI);

        // Check deserialized bond data
        Assert.assertNotNull(deserializedSharkBond);
        Assert.assertEquals(deserializedSharkBond.getBondID(), sharkBond.getBondID());
        Assert.assertEquals(deserializedSharkBond.getAmount(), BOND_AMOUNT);
        Assert.assertEquals(deserializedSharkBond.getCreditorID(), ALICE_ID);
        Assert.assertEquals(deserializedSharkBond.getDebtorID(), BOB_ID);
        Assert.assertEquals(deserializedSharkBond.unitDescription(), BOND_UNIT);
        Assert.assertEquals(deserializedSharkBond.getExpirationDate(), sharkBond.getExpirationDate());
    }

    @Test
    public void serializationTestBondEncrypted() throws SharkException, IOException, ASAPException {
        this.setUpSharkBondFunctionalitiesScenario();
        SharkBond sharkBond = new InMemoSharkBond(ALICE_ID, BOB_ID, BOND_UNIT, BOND_AMOUNT, false);
        Set<CharSequence> receiver = new HashSet<>();
        receiver.add(sharkBond.getDebtorID());

        // Check the bond data
        Assert.assertNotNull(sharkBond);
        Assert.assertNotNull(sharkBond.getBondID());
        Assert.assertEquals(sharkBond.getAmount(), BOND_AMOUNT);
        Assert.assertEquals(sharkBond.getCreditorID(), ALICE_ID);
        Assert.assertEquals(sharkBond.getDebtorID(), BOB_ID);
        Assert.assertEquals(sharkBond.unitDescription(), BOND_UNIT);

        // Serialize bond as signed bond
        byte[] sharkBondBytes = SharkBondSerializer.serializeCreditBond(sharkBond, sharkBond.getCreditorID(), receiver, false, true, alicePKI, false, 0);

        Assert.assertNotNull(sharkBondBytes);

        // Deserialize bondBytes
        SharkBond deserializedSharkBond = SharkBondSerializer.deserializeCreditBond(sharkBondBytes, bobPKI);

        // Check deserialized bond data
        Assert.assertNotNull(deserializedSharkBond);
        Assert.assertEquals(deserializedSharkBond.getBondID(), sharkBond.getBondID());
        Assert.assertEquals(deserializedSharkBond.getAmount(), BOND_AMOUNT);
        Assert.assertEquals(deserializedSharkBond.getCreditorID(), ALICE_ID);
        Assert.assertEquals(deserializedSharkBond.getDebtorID(), BOB_ID);
        Assert.assertEquals(deserializedSharkBond.unitDescription(), BOND_UNIT);
        Assert.assertEquals(deserializedSharkBond.getExpirationDate(), sharkBond.getExpirationDate());
    }

    @Test
    public void serializationTestBondEncryptedAndSigned() throws SharkException, IOException, ASAPException {
        this.setUpSharkBondFunctionalitiesScenario();
        SharkBond sharkBond = new InMemoSharkBond(ALICE_ID, BOB_ID, BOND_UNIT, BOND_AMOUNT, false);
        Set<CharSequence> receiver = new HashSet<>();
        receiver.add(sharkBond.getDebtorID());

        // Check the bond data
        Assert.assertNotNull(sharkBond);
        Assert.assertNotNull(sharkBond.getBondID());
        Assert.assertEquals(sharkBond.getAmount(), BOND_AMOUNT);
        Assert.assertEquals(sharkBond.getCreditorID(), ALICE_ID);
        Assert.assertEquals(sharkBond.getDebtorID(), BOB_ID);
        Assert.assertEquals(sharkBond.unitDescription(), BOND_UNIT);

        // Serialize bond as signed bond
        byte[] sharkBondBytes = SharkBondSerializer.serializeCreditBond(sharkBond, sharkBond.getCreditorID(), receiver, true, true, alicePKI, false, 0);

        Assert.assertNotNull(sharkBondBytes);

        // Deserialize bondBytes
        SharkBond deserializedSharkBond = SharkBondSerializer.deserializeCreditBond(sharkBondBytes, bobPKI);

        // Check deserialized bond data
        Assert.assertNotNull(deserializedSharkBond);
        Assert.assertEquals(deserializedSharkBond.getBondID(), sharkBond.getBondID());
        Assert.assertEquals(deserializedSharkBond.getAmount(), BOND_AMOUNT);
        Assert.assertEquals(deserializedSharkBond.getCreditorID(), ALICE_ID);
        Assert.assertEquals(deserializedSharkBond.getDebtorID(), BOB_ID);
        Assert.assertEquals(deserializedSharkBond.unitDescription(), BOND_UNIT);
        Assert.assertEquals(deserializedSharkBond.getExpirationDate(), sharkBond.getExpirationDate());
    }
}
