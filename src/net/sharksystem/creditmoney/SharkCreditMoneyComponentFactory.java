package net.sharksystem.creditmoney;

import net.sharksystem.SharkComponent;
import net.sharksystem.SharkComponentFactory;
import net.sharksystem.contactinformation.SharkContactInformationComponent;
import net.sharksystem.pki.SharkPKIComponent;

public class SharkCreditMoneyComponentFactory implements SharkComponentFactory {
    private final SharkPKIComponent pkiComponent;
    private final SharkContactInformationComponent contactsComponent;

    public SharkCreditMoneyComponentFactory(SharkPKIComponent pkiComponent, SharkContactInformationComponent contactsComponent) {
        this.pkiComponent = pkiComponent;
        this.contactsComponent = contactsComponent;
    }

    public SharkCreditMoneyComponentFactory(SharkPKIComponent pkiComponent) {
        this(pkiComponent, null);
    }

    @Override
    public SharkComponent getComponent() {
        return new SharkCreditMoneyComponentImpl(pkiComponent);
    }
}
