package net.sharksystem.creditmoney;

import net.sharksystem.SharkCertificateComponent;
import net.sharksystem.SharkComponent;
import net.sharksystem.SharkComponentFactory;

public class SharkCreditMoneyComponentFactory implements SharkComponentFactory {
    private final SharkCertificateComponent certificateComponent;

    public SharkCreditMoneyComponentFactory(SharkCertificateComponent certificateComponent) {
        this.certificateComponent = certificateComponent;
    }

    @Override
    public SharkComponent getComponent() {
        return new SharkCreditMoneyComponentImpl(certificateComponent);
    }
}
