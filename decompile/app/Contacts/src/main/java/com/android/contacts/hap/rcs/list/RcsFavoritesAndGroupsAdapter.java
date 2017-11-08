package com.android.contacts.hap.rcs.list;

import android.content.Context;
import com.android.contacts.hap.EmuiFeatureManager;
import com.android.contacts.hap.rcs.RcsContactsUtils;
import com.autonavi.amap.mapcore.VTMCDataCache;
import com.huawei.rcs.capability.CapabilityService;

public class RcsFavoritesAndGroupsAdapter {
    private int iMatchNum = 7;
    private Context mContext;
    private CapabilityService mRcsCapability;

    public RcsFavoritesAndGroupsAdapter(Context context) {
        this.mContext = context;
    }

    public void initService(Context ctx) {
        if (EmuiFeatureManager.isRcsFeatureEnable() && this.mRcsCapability == null) {
            this.mRcsCapability = CapabilityService.getInstance("contacts");
        }
        if (this.mRcsCapability != null) {
            this.mRcsCapability.checkRcsServiceBind();
        }
    }

    public String getChildCursorSelection(int mFilterType, boolean mHasFavourites, int groupPosition) {
        if (mFilterType != VTMCDataCache.MAX_EXPIREDTIME || !EmuiFeatureManager.isRcsFeatureEnable()) {
            return null;
        }
        if (this.mRcsCapability != null) {
            this.iMatchNum = this.mRcsCapability.getMatchNum();
        }
        String selection;
        if (mHasFavourites && groupPosition == 0) {
            selection = "starred = 1 AND mimetype IN ( 'vnd.android.cursor.item/phone_v2','vnd.android.cursor.item/email_v2')";
        } else {
            selection = "mimetype = 'vnd.android.cursor.item/phone_v2' AND  raw_contact_id IN ( SELECT raw_contact_id FROM view_data WHERE mimetype = 'vnd.android.cursor.item/group_membership' AND data1 = ?)";
        }
        return " AND data._id in (select phone_lookup.data_id from phone_lookup where CASE WHEN length(phone_lookup.normalized_number)>" + this.iMatchNum + " THEN" + " substr(phone_lookup.normalized_number, length(phone_lookup.normalized_number)-" + this.iMatchNum + "+1, length(phone_lookup.normalized_number))" + " ELSE phone_lookup.normalized_number END in" + " (select rcs_capability.only_number from rcs_capability where rcs_capability.iRCSType != 255" + RcsContactsUtils.getExcludeExistedNumbersClause(this.mContext, this.iMatchNum) + "))";
    }

    public StringBuffer appendSbForRcs() {
        if (!EmuiFeatureManager.isRcsFeatureEnable()) {
            return null;
        }
        StringBuffer sb = new StringBuffer();
        sb.append("contact_id").append(", replace(replace(").append("data1").append(", '-', ''), ' ', '')");
        return sb;
    }
}
