package net.sharksystem.creditmoney;

import net.sharksystem.SharkComponent;
import net.sharksystem.SharkComponentFactory;
import net.sharksystem.pki.SharkPKIComponent;

public class SharkCreditMoneyComponentFactory implements SharkComponentFactory {
    private final SharkPKIComponent pkiComponent;

    public SharkCreditMoneyComponentFactory(SharkPKIComponent pkiComponent) {
        this.pkiComponent = pkiComponent;
    }

    @Override
    public SharkComponent getComponent() {
        return new SharkMoneyComponentImpl(pkiComponent);
    }
}
