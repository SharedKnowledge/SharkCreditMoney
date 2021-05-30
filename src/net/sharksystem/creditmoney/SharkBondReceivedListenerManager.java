package net.sharksystem.creditmoney;

import net.sharksystem.asap.ASAPException;
import net.sharksystem.asap.listenermanager.GenericListenerImplementation;
import net.sharksystem.asap.listenermanager.GenericNotifier;

import java.io.IOException;

public class SharkBondReceivedListenerManager extends GenericListenerImplementation<SharkBondsReceivedListener> {
    public void addSharkBondReceivedListener(SharkBondsReceivedListener listener) {
        this.addListener(listener);
    }

    public void removeSharkBondReceivedListener(SharkBondsReceivedListener listener) {
        this.removeListener(listener);
    }

    protected void notifySharkBondReceivedListener(
            CharSequence uri) {

        SharkBondsReceivedNotifier sharkBondsReceivedNotifier =
                new SharkBondsReceivedNotifier(uri);

        this.notifyAll(sharkBondsReceivedNotifier, false);
    }

    private class SharkBondsReceivedNotifier implements GenericNotifier<SharkBondsReceivedListener> {
        private final CharSequence uri;

        public SharkBondsReceivedNotifier(CharSequence uri) {
            this.uri = uri;
        }

        @Override
        public void doNotify(SharkBondsReceivedListener sharkMessagesReceivedListener) {
            try {
                sharkMessagesReceivedListener.sharkBondReceived(this.uri);
            } catch (ASAPException | IOException | SharkCreditMoneyException e) {
                e.printStackTrace();
            }
        }
    }
}
