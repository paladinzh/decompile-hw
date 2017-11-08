package com.android.contacts.model.account;

import com.android.contacts.model.account.AccountType.EditType;
import com.android.contacts.util.HwCustContactFeatureUtils;
import com.google.android.gms.R;
import java.util.List;

public class HwCustExchangeAccountTypeImpl extends HwCustExchangeAccountType {
    public static final int TYPE_BUSINESS = 51;
    public static final int TYPE_BUSINESS2 = 52;
    public static final int TYPE_CAR_PHONE = 53;
    public static final int TYPE_HOME2 = 54;
    public static final int TYPE_PRIMARY = 55;

    public boolean isAdditionalSprintFieldsRequired() {
        return HwCustContactFeatureUtils.isSupportAdditionalExchangePhoneFields();
    }

    public void addCustomDataKindPhone(List<EditType> typeList) {
        if (isAdditionalSprintFieldsRequired()) {
            addPhoneTypeBusiness(typeList);
            addPhoneTypeBusiness2(typeList);
            addPhoneTypeHome2(typeList);
            addPhoneTypeCarPhone(typeList);
            addPhoneTypePrimary(typeList);
        }
    }

    public void addPhoneTypeBusiness(List<EditType> typeList) {
        typeList.add(new EditType(51, R.string.phoneTypeBusiness).setSpecificMax(2));
    }

    public void addPhoneTypeBusiness2(List<EditType> typeList) {
        typeList.add(new EditType(52, R.string.phoneTypeBusiness2).setSpecificMax(1));
    }

    public void addPhoneTypeHome2(List<EditType> typeList) {
        typeList.add(new EditType(54, R.string.phoneTypeHome2).setSpecificMax(1));
    }

    public void addPhoneTypeCarPhone(List<EditType> typeList) {
        typeList.add(new EditType(53, R.string.phoneTypeCarPhone).setSpecificMax(1));
    }

    public void addPhoneTypePrimary(List<EditType> typeList) {
        typeList.add(new EditType(55, R.string.phoneTypePrimary).setSpecificMax(1));
    }
}
