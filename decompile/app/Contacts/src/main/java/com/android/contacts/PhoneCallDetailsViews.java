package com.android.contacts;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.android.contacts.hap.CommonUtilMethods;
import com.android.contacts.hap.EmuiFeatureManager;
import com.android.contacts.hap.rcs.dialer.RcsPhoneCallDetailHelper;
import com.android.contacts.util.Constants;
import com.android.contacts.util.EmuiVersion;
import com.google.android.gms.R;
import com.huawei.cust.HwCustUtils;
import huawei.android.widget.TimeAxisWidget;

public final class PhoneCallDetailsViews {
    public final TextView callCount;
    public final ImageView cardType;
    public final TextView dateView;
    public ImageView hdcallIcon;
    private HwCustPhoneCallDetailsViews mCust;
    private PhoneCallDetails mDetails;
    private EncryptPhoneCallDetailsViews mEncryptPhoneCallDetailsViews;
    public TextView mEspaceView;
    public TextView mLocationView;
    public ImageView mRcsImportantIcon;
    public View mRcsPreCallView;
    public TextView mRcsSubject;
    public TextView mRingTimes;
    public ImageView mWorkIcon;
    public final TextView missedCallCount;
    public final TextView nameView;
    public TextView numberView;
    public final ImageView outgoingIcon;
    public View timeAxisWidget;
    public final ImageView voicemailIcon;

    public void setPhoneCallDetail(PhoneCallDetails details) {
        this.mDetails = details;
    }

    public PhoneCallDetails getPhoneCallDetails() {
        return this.mDetails;
    }

    public HwCustPhoneCallDetailsViews getHwCust() {
        return this.mCust;
    }

    public EncryptPhoneCallDetailsViews getEncryptPhoneCallDetailsView() {
        return this.mEncryptPhoneCallDetailsViews;
    }

    private PhoneCallDetailsViews(TextView nameView, ImageView cardType, TextView missedCallCount, ImageView outgoingIcon, ImageView voicemailIcon, TextView callCount, TextView dateView, TextView numberView) {
        this.mDetails = null;
        this.mCust = (HwCustPhoneCallDetailsViews) HwCustUtils.createObj(HwCustPhoneCallDetailsViews.class, new Object[0]);
        this.mEncryptPhoneCallDetailsViews = new EncryptPhoneCallDetailsViews();
        this.nameView = nameView;
        this.cardType = cardType;
        this.missedCallCount = missedCallCount;
        this.outgoingIcon = outgoingIcon;
        this.voicemailIcon = voicemailIcon;
        this.callCount = callCount;
        this.dateView = dateView;
        this.numberView = numberView;
        if (Constants.isEXTRA_HUGE() && this.nameView != null) {
            this.nameView.setTextSize(1, 28.0f);
        }
        CommonUtilMethods.setNameViewDirection(this.nameView);
        this.mRingTimes = null;
        this.mLocationView = null;
        this.timeAxisWidget = null;
    }

    private PhoneCallDetailsViews(TextView nameView, ImageView cardType, TextView missedCallCount, ImageView outgoingIcon, ImageView voicemailIcon, TextView callCount, TextView dateView, TextView numberView, TextView aRingTimes, TextView aLocation, TimeAxisWidget timeAxisWidget) {
        this.mDetails = null;
        this.mCust = (HwCustPhoneCallDetailsViews) HwCustUtils.createObj(HwCustPhoneCallDetailsViews.class, new Object[0]);
        this.mEncryptPhoneCallDetailsViews = new EncryptPhoneCallDetailsViews();
        this.nameView = nameView;
        if (Constants.isEXTRA_HUGE() && this.nameView != null) {
            this.nameView.setTextSize(1, 28.0f);
        }
        CommonUtilMethods.setNameViewDirection(this.nameView);
        this.cardType = cardType;
        this.missedCallCount = missedCallCount;
        this.outgoingIcon = outgoingIcon;
        this.voicemailIcon = voicemailIcon;
        this.callCount = callCount;
        this.dateView = dateView;
        this.numberView = numberView;
        this.mRingTimes = aRingTimes;
        this.mLocationView = aLocation;
        this.timeAxisWidget = timeAxisWidget;
    }

    public static PhoneCallDetailsViews fromView(View view) {
        View lLocationView = view.findViewById(R.id.location);
        PhoneCallDetailsViews views;
        if (EmuiVersion.isSupportEmui3()) {
            TextView textView;
            TextView textView2 = (TextView) view.findViewById(R.id.name);
            ImageView imageView = (ImageView) view.findViewById(R.id.card_type);
            TextView textView3 = (TextView) view.findViewById(R.id.missed_call);
            ImageView imageView2 = (ImageView) view.findViewById(R.id.outgoing_call);
            ImageView imageView3 = (ImageView) view.findViewById(R.id.voicemail);
            TextView textView4 = (TextView) view.findViewById(R.id.call_count);
            TextView textView5 = (TextView) view.findViewById(R.id.contact_date);
            TextView textView6 = (TextView) view.findViewById(R.id.number);
            if (lLocationView != null) {
                textView = (TextView) lLocationView;
            } else {
                textView = null;
            }
            views = new PhoneCallDetailsViews(textView2, imageView, textView3, imageView2, imageView3, textView4, textView5, textView6, null, textView, null);
            views.hdcallIcon = (ImageView) view.findViewById(R.id.call_log_hd_icon);
            if (EmuiFeatureManager.isRcsFeatureEnable()) {
                RcsPhoneCallDetailHelper.fromView(views, view);
            }
            views.mEspaceView = (TextView) view.findViewById(R.id.espace_call);
            views.mWorkIcon = (ImageView) view.findViewById(R.id.work_profile_icon);
            if (EmuiFeatureManager.isProductCustFeatureEnable()) {
                HwCustPhoneCallDetailsViews viewCust = views.getHwCust();
                if (viewCust != null) {
                    viewCust.initEncryptCallView(view);
                }
            }
            EncryptPhoneCallDetailsViews encryptPhoneCallDetailsView = views.getEncryptPhoneCallDetailsView();
            if (encryptPhoneCallDetailsView != null) {
                encryptPhoneCallDetailsView.initEncryptCallView(view);
            }
            return views;
        }
        views = new PhoneCallDetailsViews((TextView) view.findViewById(R.id.name), (ImageView) view.findViewById(R.id.card_type), (TextView) view.findViewById(R.id.missed_call), (ImageView) view.findViewById(R.id.outgoing_call), (ImageView) view.findViewById(R.id.voicemail), (TextView) view.findViewById(R.id.call_count), (TextView) view.findViewById(R.id.contact_date), (TextView) view.findViewById(R.id.number));
        if (EmuiFeatureManager.isRcsFeatureEnable()) {
            RcsPhoneCallDetailHelper.fromView(views, view);
        }
        if (lLocationView != null) {
            lLocationView = (TextView) lLocationView;
        } else {
            lLocationView = null;
        }
        views.mLocationView = lLocationView;
        views.hdcallIcon = (ImageView) view.findViewById(R.id.call_log_hd_icon);
        views.mEspaceView = (TextView) view.findViewById(R.id.espace_call);
        views.mWorkIcon = (ImageView) view.findViewById(R.id.work_profile_icon);
        return views;
    }

    public static PhoneCallDetailsViews fromView(View view, View interView) {
        View lLocationView = interView.findViewById(R.id.location);
        View lTimeWidget = null;
        if (EmuiVersion.isSupportEmui3()) {
            lTimeWidget = view.findViewById(R.id.layout_time_axis);
        }
        PhoneCallDetailsViews views = new PhoneCallDetailsViews((TextView) interView.findViewById(R.id.name), (ImageView) interView.findViewById(R.id.card_type), (TextView) interView.findViewById(R.id.missed_call), (ImageView) interView.findViewById(R.id.outgoing_call), (ImageView) interView.findViewById(R.id.voicemail), (TextView) interView.findViewById(R.id.call_count), null, (TextView) interView.findViewById(R.id.number));
        if (lLocationView != null) {
            lLocationView = (TextView) lLocationView;
        } else {
            lLocationView = null;
        }
        views.mLocationView = lLocationView;
        views.mEspaceView = (TextView) interView.findViewById(R.id.espace_call);
        views.timeAxisWidget = lTimeWidget;
        views.hdcallIcon = (ImageView) view.findViewById(R.id.call_log_hd_icon);
        views.mWorkIcon = (ImageView) interView.findViewById(R.id.work_profile_icon);
        if (EmuiFeatureManager.isProductCustFeatureEnable()) {
            HwCustPhoneCallDetailsViews viewCust = views.getHwCust();
            if (viewCust != null) {
                viewCust.initEncryptCallView(view);
            }
        }
        EncryptPhoneCallDetailsViews encryptPhoneCallDetailsView = views.getEncryptPhoneCallDetailsView();
        if (encryptPhoneCallDetailsView != null) {
            encryptPhoneCallDetailsView.initEncryptCallView(view);
        }
        return views;
    }
}
