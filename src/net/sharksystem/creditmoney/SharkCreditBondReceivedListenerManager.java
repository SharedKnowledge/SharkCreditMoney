package net.sharksystem.creditmoney;

import net.sharksystem.asap.ASAPException;
import net.sharksystem.asap.listenermanager.GenericListenerImplementation;
import net.sharksystem.asap.listenermanager.GenericNotifier;

public class SharkCreditBondReceivedListenerManager extends GenericListenerImplementation<SharkCreditBondReceivedListener> {
    public void addSharkCreditBondReceivedListener(SharkCreditBondReceivedListener listener) {
        this.addListener(listener);
    }

    public void removeSharkCreditBondReceivedListener(SharkCreditBondReceivedListener listener) {
        this.removeListener(listener);
    }

    protected void notifySharkCreditBondReceivedListener(
            SharkCreditBond bond,
            CharSequence uri) {

        SharkCreditBondReceivedNotifier sharkMessagesReceivedNotifier =
                new SharkCreditBondReceivedNotifier(bond, uri);

        this.notifyAll(sharkMessagesReceivedNotifier, false);
    }

    private class SharkCreditBondReceivedNotifier implements GenericNotifier<SharkCreditBondReceivedListener> {
        private final SharkCreditBond bond;
        private final CharSequence uri;

        public SharkCreditBondReceivedNotifier(SharkCreditBond bond, CharSequence uri) {
            this.bond = bond;
            this.uri = uri;
        }

        @Override
        public void doNotify(SharkCreditBondReceivedListener sharkCreditBondReceivedListener) {
            try {
                sharkCreditBondReceivedListener.sharkCreditBondReceived(this.bond, this.uri);
            } catch (ASAPException e) {
                e.printStackTrace();
            }
        }
    }
}
