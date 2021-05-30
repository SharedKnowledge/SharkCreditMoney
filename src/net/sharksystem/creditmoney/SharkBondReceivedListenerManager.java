package net.sharksystem.creditmoney;

import net.sharksystem.asap.ASAPException;
import net.sharksystem.asap.listenermanager.GenericListenerImplementation;
import net.sharksystem.asap.listenermanager.GenericNotifier;

import java.io.IOException;

public class SharkBondReceivedListenerManager extends GenericListenerImplementation<SharkBondReceivedListener> {
    public void addSharkBondReceivedListener(SharkBondReceivedListener listener) {
        this.addListener(listener);
    }

    public void removeSharkBondReceivedListener(SharkBondReceivedListener listener) {
        this.removeListener(listener);
    }

    protected void notifySharkBondReceivedListener(
            CharSequence uri) {

        SharkBondReceivedNotifier sharkBondReceivedNotifier =
                new SharkBondReceivedNotifier(uri);

        this.notifyAll(sharkBondReceivedNotifier, false);
    }

    private class SharkBondReceivedNotifier implements GenericNotifier<SharkBondReceivedListener> {
        private final CharSequence uri;

        public SharkBondReceivedNotifier(CharSequence uri) {
            this.uri = uri;
        }

        @Override
        public void doNotify(SharkBondReceivedListener sharkMessagesReceivedListener) {
            try {
                sharkMessagesReceivedListener.sharkBondReceived(this.uri);
            } catch (ASAPException | IOException | SharkCreditMoneyException e) {
                e.printStackTrace();
            }
        }
    }
}
