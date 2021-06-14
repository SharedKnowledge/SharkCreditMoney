package net.sharksystem.creditmoney;

import java.util.Collection;

public interface SharkBondStorage {
    void loadBondsFromStorage();
    void addSharkBond(SharkBond sharkBond) throws SharkCreditMoneyException;
    void updateSharkBond(SharkBond sharkBond) throws SharkCreditMoneyException;
    void addOrUpdateSharkBond(SharkBond sharkBond) throws SharkCreditMoneyException;
    void deleteSharkBondById(CharSequence bondId) throws SharkCreditMoneyException;
    void deleteAll();
    int getStorageSize();
    int getIndexOfSharkBond(CharSequence bondId);
    Collection<SharkBond> getAllSharkBonds();
    SharkBond getSharkBondAtIndex(int index);
    SharkBond getSharkBondByBondId(CharSequence bondId);
    Collection<SharkBond> getSharkBondsByCreditor(CharSequence creditorId);
    Collection<SharkBond> getSharkBondsByDebtor(CharSequence debtorId);
    Collection<SharkBond> getSharkBondsByCreditorAndDebtor(CharSequence creditorId, CharSequence debtorId);
}
