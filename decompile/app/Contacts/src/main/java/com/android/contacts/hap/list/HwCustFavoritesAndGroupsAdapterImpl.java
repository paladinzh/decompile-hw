package com.android.contacts.hap.list;

import android.content.Context;
import com.android.contacts.util.HwCustContactFeatureUtils;
import com.huawei.android.provider.SettingsEx.Systemex;

public class HwCustFavoritesAndGroupsAdapterImpl extends HwCustFavoritesAndGroupsAdapter {
    public HwCustFavoritesAndGroupsAdapterImpl(Context context) {
        super(context);
    }

    public boolean getEnableEmailContactInMms() {
        if ("true".equals(Systemex.getString(this.mContext.getContentResolver(), "enable_email_contact_in_mms"))) {
            return HwCustContactFeatureUtils.isBindOnlyNumberSwitch(this.mContext);
        }
        return false;
    }

    public String getChildSelection() {
        return "mimetype IN ('vnd.android.cursor.item/phone_v2','vnd.android.cursor.item/email_v2') AND  raw_contact_id IN ( SELECT raw_contact_id FROM view_data WHERE mimetype = 'vnd.android.cursor.item/group_membership' AND data1 = ?)";
    }
}
