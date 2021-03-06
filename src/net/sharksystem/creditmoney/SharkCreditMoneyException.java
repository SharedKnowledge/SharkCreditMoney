package net.sharksystem.creditmoney;

import net.sharksystem.SharkException;

public class SharkCreditMoneyException extends SharkException {
    public SharkCreditMoneyException() {
        super();
    }
    public SharkCreditMoneyException(String message) {
        super(message);
    }
    public SharkCreditMoneyException(String message, Throwable cause) {
        super(message, cause);
    }
    public SharkCreditMoneyException(Throwable cause) {
        super(cause);
    }

}
