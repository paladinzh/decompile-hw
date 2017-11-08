package com.android.contacts.calllog;

import android.content.Context;
import android.database.Cursor;
import com.android.contacts.PhoneCallDetailsViews;

public class HwCustCallLogAdapter {
    protected static final int DEFAULT_CALL_FEATURES_VALUE = 0;
    Context mContext;

    public HwCustCallLogAdapter(Context context) {
        this.mContext = context;
    }

    public void getSdnNumbers() {
    }

    public String setSdnName(String number, String name) {
        return name;
    }

    public void addSdnNumbers() {
    }

    public boolean isSdnNumber(String number) {
        return false;
    }

    public void updateCallLogContactInfoCache() {
    }

    public long getCallFeaturesValue(Cursor c) {
        return 0;
    }

    public void updateCustSetting() {
    }

    public void getSdnNumbers41() {
    }

    public String setSdnName41(String number, String name, boolean isVoiceMail) {
        return name;
    }

    public String setSdnName41(String number, String name, boolean isVoiceMail, int subId) {
        return name;
    }

    public void addSdnNumbers41() {
    }

    public boolean isLongVoiceMailNumber(String number) {
        return false;
    }

    public boolean isCopyTextToEditText(boolean doubleSimCardEnabled) {
        return false;
    }

    public void updateEncryptCallView(PhoneCallDetailsViews phoneCallDetailsViews, Cursor c) {
    }

    public boolean isSupportCnap() {
        return false;
    }

    public String getCustomContextMenuTitle(Context context, CallLogListItemViews views, String toUse) {
        return toUse;
    }

    public boolean isNameUpdateRequried(ContactInfo updatedInfo, ContactInfo callLogInfo) {
        return true;
    }
}
