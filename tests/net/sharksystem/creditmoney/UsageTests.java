package net.sharksystem.creditmoney;

import net.sharksystem.*;
import net.sharksystem.pki.SharkPKIComponent;
import net.sharksystem.pki.SharkPKIComponentFactory;
import org.junit.Test;

import static net.sharksystem.creditmoney.TestConstants.*;

public class UsageTests {
    private static final String THIS_ROOT_DIRECTORY = TestConstants.ROOT_DIRECTORY + UsageTests.class.getSimpleName() + "/";
    private static final String ALICE_FOLDER = THIS_ROOT_DIRECTORY + ALICE_NAME;

    @Test
    public void test1() throws SharkException {
        // nothing yet
        SharkTestPeerFS.removeFolder(THIS_ROOT_DIRECTORY);
        SharkTestPeerFS aliceSharkPeer = new SharkTestPeerFS(ALICE_ID, ALICE_FOLDER);

        // certificate component required
        aliceSharkPeer.addComponent(new SharkPKIComponentFactory(), SharkPKIComponent.class);

        // get certificate component
        SharkPKIComponent certificateComponent =
                (SharkPKIComponent) aliceSharkPeer.getComponent(SharkPKIComponent.class);


        // create money factory ;)
        SharkCreditMoneyComponentFactory scmcf = new SharkCreditMoneyComponentFactory(certificateComponent);

        // shark money component required
        aliceSharkPeer.addComponent(scmcf, SharkCreditMoneyComponent.class);

        // add tests
    }
}
