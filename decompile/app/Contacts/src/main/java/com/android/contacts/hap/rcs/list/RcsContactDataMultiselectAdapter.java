package com.android.contacts.hap.rcs.list;

import android.content.Context;
import android.net.Uri;
import android.net.Uri.Builder;
import android.provider.ContactsContract;
import android.text.TextUtils;
import com.android.contacts.hap.EmuiFeatureManager;
import com.android.contacts.hap.rcs.RcsContactsUtils;
import com.huawei.rcs.capability.CapabilityService;
import java.util.List;

public class RcsContactDataMultiselectAdapter {
    private boolean isRcsOn = EmuiFeatureManager.isRcsFeatureEnable();
    private Context mContext;
    private CapabilityService mRcsCapability;

    public RcsContactDataMultiselectAdapter(Context context) {
        this.mContext = context;
    }

    public void initService() {
        if (this.mRcsCapability == null && this.mContext != null && EmuiFeatureManager.isRcsFeatureEnable()) {
            this.mRcsCapability = CapabilityService.getInstance("contacts");
        }
        if (this.mRcsCapability != null) {
            this.mRcsCapability.checkRcsServiceBind();
        }
    }

    public void setSelectionAndSelectionArgsForCustomizations(int filterType, StringBuilder selection, List<String> selectionArgs) {
        if (filterType == -50 && EmuiFeatureManager.isRcsFeatureEnable()) {
            setSelectionAndSelectionArgsToGetOnlyRCSContactsForMessaging(selection, selectionArgs, filterType);
        }
    }

    private void setSelectionAndSelectionArgsToGetOnlyRCSContactsForMessaging(StringBuilder aSelectionBuilder, List<String> aSelectionArgs, int filterType) {
        if (filterType == -4) {
            aSelectionBuilder.append("mimetype IN (?)");
            aSelectionArgs.add("vnd.android.cursor.item/email_v2");
        } else {
            aSelectionBuilder.append("mimetype IN (?)");
            aSelectionArgs.add("vnd.android.cursor.item/phone_v2");
        }
        int iMatchNum = 0;
        if (this.mRcsCapability != null) {
            iMatchNum = this.mRcsCapability.getMatchNum();
        }
        RcsContactsUtils.configSelectionToGetOnlyRCSContactsExcludeExistedNumbers(aSelectionBuilder, iMatchNum, RcsContactsUtils.getExcludeExistedNumbersClause(this.mContext, iMatchNum));
    }

    public Uri configRCSSearchUri(String searchType, String searchValue, String keywords, int filter) {
        if (this.isRcsOn && -50 == filter) {
            return getSearchUri(Uri.withAppendedPath(ContactsContract.AUTHORITY_URI, "rcs/group_message_contacts"), keywords, searchType, searchValue);
        }
        return null;
    }

    public void configRCSBuilder(String parameter, Builder builder) {
        int iMatchNum = 0;
        if (this.mRcsCapability != null) {
            iMatchNum = this.mRcsCapability.getMatchNum();
        }
        builder.appendQueryParameter(parameter, String.valueOf(true));
        builder.appendQueryParameter("matchNumber", String.valueOf(iMatchNum));
        String excludeClause = RcsContactsUtils.getExcludeExistedNumbersClause(this.mContext, iMatchNum);
        if (!TextUtils.isEmpty(excludeClause)) {
            builder.appendQueryParameter("excludeClause", excludeClause);
        }
    }

    private Uri getSearchUri(Uri baseUri, String keywords, String searchType, String searchValue) {
        return Uri.withAppendedPath(baseUri, Uri.encode(keywords)).buildUpon().appendQueryParameter(searchType, searchValue).build();
    }
}
