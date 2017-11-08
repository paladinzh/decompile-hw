package com.android.contacts.hap.rcs.list;

import android.content.Context;
import com.android.contacts.hap.EmuiFeatureManager;
import com.android.contacts.hap.rcs.RcsContactsUtils;
import com.huawei.rcs.capability.CapabilityService;
import java.util.List;

public class RcsFrequentContactMultiselectListLoader {
    private CapabilityService mCapabilityService;
    private int mMatchNum = 7;
    private Context mParentContext;

    public void setParentContext(Context context) {
        this.mParentContext = context;
    }

    public void initService(Context context) {
        if (context != null && this.mCapabilityService == null && EmuiFeatureManager.isRcsFeatureEnable()) {
            this.mCapabilityService = CapabilityService.getInstance("contacts");
        }
        if (this.mCapabilityService != null) {
            this.mCapabilityService.checkRcsServiceBind();
            this.mMatchNum = this.mCapabilityService.getMatchNum();
        }
    }

    public void setSelectionAndSelectionArgsForCustomizations(int filterType, StringBuilder aSelectionBuilder, List<String> aSelectionArgs) {
        if (filterType == -50 && EmuiFeatureManager.isRcsFeatureEnable()) {
            setSelectionAndSelectionArgsToGetOnlyRCSContactsForMessaging(filterType, aSelectionBuilder, aSelectionArgs);
        }
    }

    private void setSelectionAndSelectionArgsToGetOnlyRCSContactsForMessaging(int filterType, StringBuilder aSelectionBuilder, List<String> aSelectionArgs) {
        if (filterType == -4) {
            aSelectionBuilder.append("mimetype IN (?)");
            aSelectionArgs.add("vnd.android.cursor.item/email_v2");
        } else {
            aSelectionBuilder.append("mimetype IN (?)");
            aSelectionArgs.add("vnd.android.cursor.item/phone_v2");
        }
        setSelectionToGetOnlyRCSContactsForMessaging(aSelectionBuilder);
    }

    public void setSelectionToGetOnlyRCSContactsForMessaging(StringBuilder aSelectionBuilder) {
        int iMatchNum = 0;
        if (this.mCapabilityService != null) {
            iMatchNum = this.mCapabilityService.getMatchNum();
        }
        RcsContactsUtils.configSelectionToGetOnlyRCSContactsExcludeExistedNumbers(aSelectionBuilder, iMatchNum, RcsContactsUtils.getExcludeExistedNumbersClause(this.mParentContext, iMatchNum));
    }
}
