package com.android.contacts.model.account;

import com.android.contacts.model.account.AccountType.EditType;
import com.android.contacts.model.dataitem.DataKind;

public class HwCustAccountModelCustomization {
    public boolean handleAddressFieldCustomization(DataKind aKind) {
        return false;
    }

    public EditType handleRadioIDCustomization() {
        return null;
    }
}
