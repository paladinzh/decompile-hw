package com.android.contacts.model.account;

import com.android.contacts.hap.HwCustCommonConstants;
import com.android.contacts.model.account.AccountType.DefinitionException;
import com.android.contacts.model.account.AccountType.EditField;
import com.android.contacts.model.account.BaseAccountType.PostalActionInflater;
import com.android.contacts.model.account.BaseAccountType.SimpleInflater;
import com.android.contacts.model.dataitem.DataKind;
import com.android.contacts.util.HwCustContactFeatureUtils;
import com.google.android.collect.Lists;
import com.google.android.gms.R;

public class HwCustFallbackAccountTypeImpl extends HwCustFallbackAccountType {
    protected static final int MAX_LINES_FOR_POSTAL_ADDRESS = 10;

    protected DataKind customizeAddDataKindStructuredPostal(FallbackAccountType fallBackAccountType) throws DefinitionException {
        DataKind kind = fallBackAccountType.addKind(new DataKind("vnd.android.cursor.item/postal-address_v2", R.string.contact_postalLabelsGroup, 25, true));
        kind.actionHeader = new PostalActionInflater();
        kind.actionBody = new SimpleInflater("data1");
        kind.typeColumn = "data2";
        kind.typeList = Lists.newArrayList();
        kind.typeList.add(BaseAccountType.buildPostalType(1));
        kind.typeList.add(BaseAccountType.buildPostalType(2));
        kind.typeList.add(BaseAccountType.buildPostalType(3));
        kind.typeList.add(BaseAccountType.buildPostalType(0).setSecondary(true).setCustomColumn("data3"));
        kind.fieldList = Lists.newArrayList();
        kind.fieldList.add(new EditField("data4", R.string.postal_street, 139377));
        kind.fieldList.add(new EditField("data7", R.string.postal_city, 139377).setOptional(true));
        kind.fieldList.add(new EditField("data8", R.string.postal_region, 139377).setOptional(true));
        kind.fieldList.add(new EditField("data9", R.string.postal_postcode, 3).setOptional(true));
        kind.fieldList.add(new EditField("data10", R.string.postal_country, 139377).setOptional(true));
        kind.maxLinesForDisplay = 10;
        return kind;
    }

    public boolean isSupportMyInfoAddressFields() {
        return !HwCustCommonConstants.IS_AAB_ATT ? HwCustContactFeatureUtils.isSupportMyInfoAddressFields() : true;
    }
}
