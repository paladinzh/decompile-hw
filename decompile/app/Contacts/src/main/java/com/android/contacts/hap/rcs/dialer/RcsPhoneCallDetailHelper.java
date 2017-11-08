package com.android.contacts.hap.rcs.dialer;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewStub;
import android.widget.ImageView;
import android.widget.TextView;
import com.android.contacts.PhoneCallDetails;
import com.android.contacts.PhoneCallDetailsViews;
import com.android.contacts.hap.EmuiFeatureManager;
import com.google.android.gms.R;

public class RcsPhoneCallDetailHelper {
    public static void displayPreCallView(Context context, PhoneCallDetailsViews views, PhoneCallDetails details) {
        if (EmuiFeatureManager.isRcsFeatureEnable() && views != null && details != null && context != null) {
            if (isShowRcsPreCallView(details)) {
                if (views.mRcsPreCallView != null) {
                    if (views.mRcsPreCallView.getVisibility() == 8) {
                        views.mRcsPreCallView.setVisibility(0);
                    }
                    if (1 == details.mIsPrimary) {
                        views.mRcsImportantIcon.setVisibility(0);
                    } else {
                        views.mRcsImportantIcon.setVisibility(8);
                    }
                    if (TextUtils.isEmpty(details.mSubject)) {
                        views.mRcsSubject.setText(context.getResources().getString(R.string.rcs_pre_call_high_priority));
                    } else {
                        views.mRcsSubject.setText(details.mSubject);
                    }
                }
            } else if (views.mRcsPreCallView != null && views.mRcsPreCallView.getVisibility() == 0) {
                views.mRcsPreCallView.setVisibility(8);
            }
        }
    }

    private static boolean isShowRcsPreCallView(PhoneCallDetails details) {
        if (details.mIsPrimary == -1 || (details.mIsPrimary == 0 && details.mSubject == null)) {
            return false;
        }
        return true;
    }

    public static void fromView(PhoneCallDetailsViews views, View view) {
        views.mRcsPreCallView = view.findViewById(R.id.rcs_pre_call);
        views.mRcsImportantIcon = (ImageView) view.findViewById(R.id.rcs_call_importance);
        views.mRcsSubject = (TextView) view.findViewById(R.id.rcs_subject);
    }

    public static void fromView(PhoneCallDetailsViews views, View view, PhoneCallDetails details) {
        if (isShowRcsPreCallView(details)) {
            if (views.mRcsPreCallView == null) {
                fromView(views, ((ViewStub) view.findViewById(R.id.rcs_layout)).inflate());
            } else if (views.mRcsPreCallView.getVisibility() == 8) {
                views.mRcsPreCallView.setVisibility(0);
            }
        } else if (views.mRcsPreCallView != null) {
            views.mRcsPreCallView.setVisibility(8);
        }
    }
}
