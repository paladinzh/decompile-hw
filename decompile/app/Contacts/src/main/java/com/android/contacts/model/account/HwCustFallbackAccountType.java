package com.android.contacts.model.account;

import com.android.contacts.model.account.AccountType.DefinitionException;
import com.android.contacts.model.dataitem.DataKind;

public class HwCustFallbackAccountType {
    protected DataKind customizeAddDataKindStructuredPostal(FallbackAccountType fallBackAccountType) throws DefinitionException {
        return null;
    }

    public boolean isSupportMyInfoAddressFields() {
        return false;
    }
}
