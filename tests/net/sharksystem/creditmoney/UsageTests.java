package net.sharksystem.creditmoney;

import net.sharksystem.SharkTestPeerFS;
import org.junit.jupiter.api.Test;

import static net.sharksystem.creditmoney.TestConstants.*;

public class UsageTests {
    private static final String THIS_ROOT_DIRECTORY = TestConstants.ROOT_DIRECTORY + UsageTests.class.getSimpleName() + "/";
    private static final String ALICE_FOLDER = THIS_ROOT_DIRECTORY + ALICE_NAME;

    @Test
    public void test1() {
        // nothing yet
        SharkTestPeerFS.removeFolder(THIS_ROOT_DIRECTORY);
        SharkTestPeerFS aliceSharkPeer = new SharkTestPeerFS(ALICE_ID, ALICE_FOLDER);
    }
}
