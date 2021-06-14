package net.sharksystem.creditmoney;

import net.sharksystem.SharkException;
import net.sharksystem.SharkPeer;
import net.sharksystem.pki.SharkPKIComponent;
import net.sharksystem.pki.SharkPKIComponentFactory;
import static net.sharksystem.creditmoney.TestConstants.*;

public class TestHelper {

    static SharkCreditMoneyComponent setupComponent(SharkPeer sharkPeer) throws SharkException {
        // create a component factory
        SharkPKIComponentFactory certificateComponentFactory = new SharkPKIComponentFactory();

        // register this component with shark peer - note: we use interface SharkPeer
        sharkPeer.addComponent(certificateComponentFactory, SharkPKIComponent.class);

        // get certificate component
        SharkPKIComponent certificateComponent =
                (SharkPKIComponent) sharkPeer.getComponent(SharkPKIComponent.class);

        // create money factory ;)
        SharkCreditMoneyComponentFactory componentFactory = new SharkCreditMoneyComponentFactory(certificateComponent);

        // shark money component required
        sharkPeer.addComponent(componentFactory, SharkCreditMoneyComponent.class);

        return (SharkCreditMoneyComponent) sharkPeer.getComponent(SharkCreditMoneyComponent.class);
    }
}
