package com.android.contacts;

import android.content.Context;
import android.net.Uri;
import android.provider.ContactsContract.Groups;
import com.android.contacts.hap.EmuiFeatureManager;
import com.android.contacts.hap.group.AbstractGroupsLoader;
import com.huawei.cust.HwCustUtils;

public final class GroupListLoader extends AbstractGroupsLoader {
    private static final String[] COLUMNS = new String[]{"account_name", "account_type", "data_set", "_id", "title", "summ_count", "group_is_read_only", "title_res", "res_package", "sync4", "sync1"};
    public static final String GROUP_LIST_SORT_ORDER = ("account_type" + " , " + "account_name" + " , " + "data_set" + " , " + "title" + " COLLATE LOCALIZED ASC");
    private static final Uri GROUP_LIST_URI = Groups.CONTENT_SUMMARY_URI.buildUpon().appendQueryParameter("count_distinct_contact", "true").build();
    private static HwCustGroupListLoader mCust = ((HwCustGroupListLoader) HwCustUtils.createObj(HwCustGroupListLoader.class, new Object[0]));

    static {
        if (EmuiFeatureManager.isProductCustFeatureEnable()) {
        }
    }

    public static String[] getGroupsProjection() {
        String[] lProjection = new String[COLUMNS.length];
        System.arraycopy(COLUMNS, 0, lProjection, 0, COLUMNS.length);
        return lProjection;
    }

    public GroupListLoader(Context context) {
        String addCustomProjectionCondition;
        Uri uri = GROUP_LIST_URI;
        String[] strArr = COLUMNS;
        StringBuilder append = new StringBuilder().append("account_type NOT NULL AND account_type != 'com.android.huawei.sim' AND account_type != 'com.android.huawei.secondsim' AND title != 'PREDEFINED_HUAWEI_GROUP_CCARD' AND account_name NOT NULL AND favorites=0 AND deleted=0");
        if (mCust != null) {
            addCustomProjectionCondition = mCust.addCustomProjectionCondition();
        } else {
            addCustomProjectionCondition = "";
        }
        super(context, uri, strArr, append.append(addCustomProjectionCondition).toString(), null, GROUP_LIST_SORT_ORDER);
    }

    protected int getSummaryCountIndex() {
        return 5;
    }

    protected int getGroupIdIndex() {
        return 3;
    }
}
