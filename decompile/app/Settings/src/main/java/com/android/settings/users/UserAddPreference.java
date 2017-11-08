package com.android.settings.users;

import android.content.Context;
import android.os.UserHandle;
import android.support.v7.preference.PreferenceViewHolder;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import com.android.settingslib.RestrictedLockUtils;
import com.android.settingslib.RestrictedPreference;

public class UserAddPreference extends RestrictedPreference {
    private OnClickListener mDeleteClickListener;
    private boolean mIsDividerEnabled = true;
    private OnClickListener mSettingsClickListener;

    UserAddPreference(Context context, AttributeSet attrs, OnClickListener settingsListener, OnClickListener deleteListener) {
        super(context, attrs);
        setLayoutResource(2130968993);
        this.mDeleteClickListener = deleteListener;
        this.mSettingsClickListener = settingsListener;
    }

    public void onBindViewHolder(PreferenceViewHolder view) {
        int i;
        super.onBindViewHolder(view);
        boolean disabledByAdmin = isDisabledByAdmin();
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
        view.setDividerAllowedAbove(true);
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
}
