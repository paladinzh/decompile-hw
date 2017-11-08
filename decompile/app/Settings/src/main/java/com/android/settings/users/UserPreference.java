package com.android.settings.users;

import android.app.ActivityManager;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.UserHandle;
import android.os.UserManager;
import android.support.v7.preference.PreferenceViewHolder;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import com.android.settings.deviceinfo.HwCustMSimSubscriptionStatusTabFragmentImpl;
import com.android.settingslib.RestrictedLockUtils;
import com.android.settingslib.RestrictedPreference;
import java.util.Comparator;

public class UserPreference extends RestrictedPreference {
    public static final Comparator<UserPreference> SERIAL_NUMBER_COMPARATOR = new Comparator<UserPreference>() {
        public int compare(UserPreference p1, UserPreference p2) {
            int sn1 = p1.getSerialNumber();
            int sn2 = p2.getSerialNumber();
            if (sn1 < sn2) {
                return -1;
            }
            if (sn1 > sn2) {
                return 1;
            }
            return 0;
        }
    };
    private OnClickListener mDeleteClickListener;
    private boolean mIsDividerEnabled = true;
    private int mSerialNumber = -1;
    private OnClickListener mSettingsClickListener;
    private int mUserId = -10;

    UserPreference(Context context, AttributeSet attrs, int userId, OnClickListener settingsListener, OnClickListener deleteListener) {
        super(context, attrs);
        setLayoutResource(2130968992);
        if (!(userId == ActivityManager.getCurrentUser() ? ((UserManager) context.getSystemService("user")).getUserInfo(userId).isGuest() : false)) {
            setWidgetLayoutResource(2130968998);
        }
        this.mDeleteClickListener = deleteListener;
        this.mSettingsClickListener = settingsListener;
        this.mUserId = userId;
        useAdminDisabledSummary(true);
    }

    private void dimIcon(boolean dimmed) {
        Drawable icon = getIcon();
        if (icon != null) {
            icon.mutate().setAlpha(dimmed ? 102 : 255);
            setIcon(icon);
        }
    }

    public void onBindViewHolder(PreferenceViewHolder view) {
        int i;
        super.onBindViewHolder(view);
        boolean disabledByAdmin = isDisabledByAdmin();
        dimIcon(disabledByAdmin);
        View userDeleteWidget = view.findViewById(2131887089);
        if (userDeleteWidget != null) {
            if (disabledByAdmin) {
                i = 8;
            } else {
                i = 0;
            }
            userDeleteWidget.setVisibility(i);
        }
        if (!disabledByAdmin) {
            UserManager um = (UserManager) getContext().getSystemService("user");
            View deleteDividerView = view.findViewById(2131886965);
            View manageDividerView = view.findViewById(2131886963);
            View deleteView = view.findViewById(2131886966);
            if (deleteView != null) {
                if (this.mDeleteClickListener == null || RestrictedLockUtils.hasBaseUserRestriction(getContext(), "no_remove_user", UserHandle.myUserId())) {
                    deleteView.setVisibility(8);
                    deleteDividerView.setVisibility(8);
                } else {
                    deleteView.setVisibility(0);
                    deleteDividerView.setVisibility(0);
                    deleteView.setOnClickListener(this.mDeleteClickListener);
                    deleteView.setTag(this);
                }
            }
            ImageView manageView = (ImageView) view.findViewById(2131886964);
            if (manageView != null) {
                if (this.mSettingsClickListener != null) {
                    manageView.setVisibility(0);
                    if (this.mDeleteClickListener == null) {
                        i = 0;
                    } else {
                        i = 8;
                    }
                    manageDividerView.setVisibility(i);
                    manageView.setOnClickListener(this.mSettingsClickListener);
                    manageView.setTag(this);
                } else {
                    manageView.setVisibility(8);
                    manageDividerView.setVisibility(8);
                }
            }
        }
        view.setDividerAllowedAbove(false);
        view.setDividerAllowedBelow(false);
        View divider = view.findViewById(2131886713);
        if (isDividerVisibile()) {
            divider.setVisibility(0);
        } else {
            divider.setVisibility(8);
        }
    }

    public boolean isDividerVisibile() {
        return this.mIsDividerEnabled;
    }

    public void setDividerVisible(boolean enabled) {
        this.mIsDividerEnabled = enabled;
    }

    private int getSerialNumber() {
        if (this.mUserId == UserHandle.myUserId()) {
            return Integer.MIN_VALUE;
        }
        if (this.mSerialNumber < 0) {
            if (this.mUserId == -10) {
                return HwCustMSimSubscriptionStatusTabFragmentImpl.INVALID;
            }
            if (this.mUserId == -11) {
                return 2147483646;
            }
            this.mSerialNumber = ((UserManager) getContext().getSystemService("user")).getUserSerialNumber(this.mUserId);
            if (this.mSerialNumber < 0) {
                return this.mUserId;
            }
        }
        return this.mSerialNumber;
    }

    public int getUserId() {
        return this.mUserId;
    }
}
