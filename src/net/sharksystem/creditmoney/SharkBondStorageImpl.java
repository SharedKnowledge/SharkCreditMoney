package net.sharksystem.creditmoney;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Collection;

public class SharkBondStorageImpl implements SharkBondStorage {
    private ArrayList<SharkBond> sharkBondStore;

    public SharkBondStorageImpl() {
        this.sharkBondStore = new ArrayList<>();
        this.loadBondsFromStorage();
    }

    @Override
    public void loadBondsFromStorage() {
        this.sharkBondStore = new ArrayList<>();
        // TODO
    }

    @Override
    public void addSharkBond(SharkBond sharkBond) throws SharkCreditMoneyException {
        if (getIndexOfSharkBond(sharkBond.getBondID()) != -1) {
            this.sharkBondStore.add(sharkBond);
        } else {
            throw new SharkCreditMoneyException("The provided bond already exist in the sharkBondStorage. Use updateSharkBond to edit an existing bond");
        }
    }

    @Override
    public void updateSharkBond(SharkBond sharkBond) throws SharkCreditMoneyException {
        int index = getIndexOfSharkBond(sharkBond.getBondID());
        if (index == -1) {
            throw new SharkCreditMoneyException("A bond matching The provided bondId couldn't be found in the sharkBondStorage");
        } else {
            this.sharkBondStore.set(index, sharkBond);
        }
    }

    @Override
    public void addOrUpdateSharkBond(SharkBond sharkBond) throws SharkCreditMoneyException {
        int index = getIndexOfSharkBond(sharkBond.getBondID());
        if (index == -1) {
            this.sharkBondStore.add(sharkBond);
        } else {
            this.sharkBondStore.set(index, sharkBond);
        }
    }

    @Override
    public void deleteSharkBondById(CharSequence bondId) throws SharkCreditMoneyException {
        int index = getIndexOfSharkBond(bondId);
        if (index == -1) {
            throw new SharkCreditMoneyException("A bond matching the provided bondId couldn't be found in the sharkBondStorage");
        } else {
            this.sharkBondStore.remove(index);
        }
    }

    @Override
    public void deleteAll() {
        this.sharkBondStore.clear();
    }

    @Override
    public int getStorageSize() {
        return this.sharkBondStore.size();
    }

    @Override
    public int getIndexOfSharkBond(CharSequence bondId) {
        if (bondId != null) {
            int index = 0;
            for (SharkBond sharkBond : this.sharkBondStore) {
                if (sharkBond.getBondID() != null && sharkBond.getBondID().equals(bondId)) {
                    return index;
                }
                index++;
            }
        }

        return -1;
    }

    @Override
    public Collection<SharkBond> getAllSharkBonds() {
        return this.sharkBondStore;
    }

    @Override
    public SharkBond getSharkBondAtIndex(int index) {
        if (index >= this.sharkBondStore.size() || index < 0) {
            throw new InvalidParameterException("The provided index is out of the bounds. Make sure that the index match the condition below: 0 <= index < getStorageSize()");
        }

        return  this.sharkBondStore.get(index);
    }

    @Override
    public SharkBond getSharkBondByBondId(CharSequence bondId) {
        int index = getIndexOfSharkBond(bondId);
        if (index >= this.sharkBondStore.size() || index < 0) {
            throw new InvalidParameterException("A bond matching the provided bondId couldn't be found in the sharkBondStorage");
        }

        return this.sharkBondStore.get(index);
    }

    @Override
    public Collection<SharkBond> getSharkBondsByCreditor(CharSequence creditorId) {
        Collection<SharkBond> bondsAsCreditor = new ArrayList<>();
        if (creditorId != null) {
            for (SharkBond sharkBond : this.sharkBondStore) {
                if (sharkBond.getCreditorID().equals(creditorId)) {
                    bondsAsCreditor.add(sharkBond);
                }
            }
        }

        return bondsAsCreditor;
    }

    @Override
    public Collection<SharkBond> getSharkBondsByDebtor(CharSequence debtorId) {
        Collection<SharkBond> bondsAsDebtor = new ArrayList<>();
        if (debtorId != null) {
            for (SharkBond sharkBond : this.sharkBondStore) {
                if (sharkBond.getDebtorID().equals(debtorId)) {
                    bondsAsDebtor.add(sharkBond);
                }
            }
        }

        return bondsAsDebtor;
    }

    @Override
    public Collection<SharkBond> getSharkBondsByCreditorAndDebtor(CharSequence creditorId, CharSequence debtorId) {
        Collection<SharkBond> bondsAsCreditorAndDebtor = new ArrayList<>();
        if (creditorId != null && debtorId != null) {
            for (SharkBond sharkBond : this.sharkBondStore) {
                if (sharkBond.getCreditorID().equals(creditorId) && sharkBond.getDebtorID().equals(debtorId)) {
                    bondsAsCreditorAndDebtor.add(sharkBond);
                }
            }
        }

        return bondsAsCreditorAndDebtor;
    }
}
