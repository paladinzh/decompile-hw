package com.android.contacts.model.account;

import com.android.contacts.hap.HwCustCommonConstants;
import com.android.contacts.model.account.AccountType.EditField;
import com.android.contacts.model.account.AccountType.EditType;
import com.android.contacts.model.dataitem.DataKind;
import com.android.contacts.util.HwCustContactFeatureUtils;
import com.google.android.gms.R;

public class HwCustAccountModelCustomizationImpl extends HwCustAccountModelCustomization {
    public boolean handleAddressFieldCustomization(DataKind aKind) {
        if (!HwCustCommonConstants.IS_AAB_ATT && !HwCustContactFeatureUtils.isSupportPostalExtendedFields()) {
            return false;
        }
        aKind.fieldList.add(new EditField("data4", R.string.postal_street, 139377));
        aKind.fieldList.add(new EditField("data7", R.string.postal_city, 139377).setOptional(true));
        aKind.fieldList.add(new EditField("data8", R.string.postal_region, 139377).setOptional(true));
        aKind.fieldList.add(new EditField("data9", R.string.postal_postcode, 3).setOptional(true));
        aKind.fieldList.add(new EditField("data10", R.string.postal_country, 139377).setOptional(true));
        return true;
    }

    public EditType handleRadioIDCustomization() {
        if (HwCustContactFeatureUtils.isSupportRadioIDLabelCustomization()) {
            return new EditType(14, R.string.radio_id).setSecondary(true);
        }
        return null;
    }

    public EditType handleHomeFaxCustomization() {
        if (HwCustContactFeatureUtils.isSupportHomeFaxLabelCustomization()) {
            return new EditType(5, R.string.fax).setSecondary(true);
        }
        return null;
    }
}
