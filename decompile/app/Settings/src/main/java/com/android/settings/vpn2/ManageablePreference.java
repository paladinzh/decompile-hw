package com.android.settings.vpn2;

import android.content.Context;
import android.content.res.Resources;
import android.os.UserHandle;
import android.text.TextUtils;
import android.util.AttributeSet;
import com.android.internal.net.VpnProfile;
import com.android.settingslib.RestrictedPreference;
import com.huawei.cust.HwCustUtils;

public abstract class ManageablePreference extends RestrictedPreference {
    public static int STATE_NONE = -1;
    private HwCustVpnSettings mHwCustVpnSettings = ((HwCustVpnSettings) HwCustUtils.createObj(HwCustVpnSettings.class, new Object[0]));
    boolean mIsAlwaysOn = false;
    protected VpnProfile mProfile;
    int mState = STATE_NONE;
    int mUserId;

    public ManageablePreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        setPersistent(false);
        setOrder(0);
        setUserId(UserHandle.myUserId());
        setWidgetLayoutResource(2130968998);
    }

    public int getUserId() {
        return this.mUserId;
    }

    public void setUserId(int userId) {
        this.mUserId = userId;
        checkRestrictionAndSetDisabled("no_config_vpn", userId);
    }

    public int getState() {
        return this.mState;
    }

    public void setState(int state) {
        if (this.mState != state) {
            this.mState = state;
            updateSummary();
            notifyHierarchyChanged();
        }
    }

    public void setAlwaysOn(boolean isEnabled) {
        if (this.mIsAlwaysOn != isEnabled) {
            this.mIsAlwaysOn = isEnabled;
            updateSummary();
        }
    }

    protected void updateSummary() {
        Resources res = getContext().getResources();
        String[] states = res.getStringArray(2131361907);
        String summary = "";
        if (this.mState <= STATE_NONE) {
            String[] types = getContext().getResources().getStringArray(2131361906);
            if (this.mHwCustVpnSettings != null && this.mHwCustVpnSettings.isShowVpnL2TP()) {
                String[] custTypes = this.mHwCustVpnSettings.getTypes(getContext());
                if (custTypes.length > 0) {
                    types = custTypes;
                }
            }
            if (this.mProfile != null) {
                summary = types[this.mProfile.type];
            }
        } else {
            summary = states[this.mState];
        }
        if (this.mIsAlwaysOn) {
            String alwaysOnString = res.getString(2131626400);
            if (TextUtils.isEmpty(summary)) {
                summary = alwaysOnString;
            } else {
                summary = res.getString(2131625666, new Object[]{summary, alwaysOnString});
            }
        }
        setSummary((CharSequence) summary);
    }
}
