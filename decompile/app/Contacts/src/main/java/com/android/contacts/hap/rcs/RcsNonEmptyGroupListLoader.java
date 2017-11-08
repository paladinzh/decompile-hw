package com.android.contacts.hap.rcs;

import android.content.Context;
import com.android.contacts.hap.EmuiFeatureManager;
import com.autonavi.amap.mapcore.VTMCDataCache;
import com.huawei.rcs.capability.CapabilityService;

public class RcsNonEmptyGroupListLoader {
    private int iMatchNum = 7;
    private Context mContext;
    private int mFilterType = -1;
    private CapabilityService mRcsCapability = null;
    private boolean rcsSwitch = EmuiFeatureManager.isRcsFeatureEnable();

    public RcsNonEmptyGroupListLoader(Context context, int filterType) {
        this.mContext = context;
        this.mRcsCapability = CapabilityService.getInstance("contacts");
        if (this.mRcsCapability != null) {
            this.mRcsCapability.checkRcsServiceBind();
            this.iMatchNum = this.mRcsCapability.getMatchNum();
        }
        this.mFilterType = filterType;
    }

    public String appendCustomizationsFilterForDatasSelection(String selection) {
        if (!this.rcsSwitch) {
            return selection;
        }
        if (this.mFilterType == VTMCDataCache.MAX_EXPIREDTIME) {
            selection = " AND data._id in (select phone_lookup.data_id from phone_lookup where CASE WHEN length(phone_lookup.normalized_number)>" + this.iMatchNum + " THEN" + " substr(phone_lookup.normalized_number, length(phone_lookup.normalized_number)-" + this.iMatchNum + "+1, length(phone_lookup.normalized_number))" + " ELSE phone_lookup.normalized_number END in" + " (select rcs_capability.only_number from rcs_capability where rcs_capability.iRCSType != 255" + RcsContactsUtils.getExcludeExistedNumbersClause(this.mContext, this.iMatchNum) + "))";
        }
        return selection;
    }

    public String appendCustomizationsFilterForGroupsSelection(String selection) {
        if (!this.rcsSwitch) {
            return selection;
        }
        if (this.mFilterType == VTMCDataCache.MAX_EXPIREDTIME) {
            selection = " AND _id IN (SELECT data1 FROM data " + "WHERE mimetype_id=(select _id from mimetypes where mimetype='vnd.android.cursor.item/group_membership')" + " AND raw_contact_id IN (SELECT raw_contact_id FROM data WHERE mimetype_id=(select _id from mimetypes where mimetype='vnd.android.cursor.item/phone_v2')" + " AND data._id in (select phone_lookup.data_id from phone_lookup where CASE WHEN length(phone_lookup.normalized_number)>" + this.iMatchNum + " THEN" + " substr(phone_lookup.normalized_number, length(phone_lookup.normalized_number)-" + this.iMatchNum + "+1, length(phone_lookup.normalized_number))" + " ELSE phone_lookup.normalized_number END in" + " (select rcs_capability.only_number from rcs_capability where rcs_capability.iRCSType != 255" + RcsContactsUtils.getExcludeExistedNumbersClause(this.mContext, this.iMatchNum) + "))" + "))";
        }
        return selection;
    }
}
