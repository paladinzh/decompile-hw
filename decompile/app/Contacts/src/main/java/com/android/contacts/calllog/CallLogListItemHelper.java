package com.android.contacts.calllog;

import android.content.Context;
import android.content.res.Resources;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.widget.TextView;
import com.amap.api.services.core.AMapException;
import com.android.contacts.PhoneCallDetails;
import com.android.contacts.PhoneCallDetailsHelper;
import com.android.contacts.hap.CommonUtilMethods;
import com.android.contacts.util.DateUtils;
import com.google.android.gms.R;
import com.huawei.cspcommon.util.CustomStateListDrawable;
import com.huawei.cspcommon.util.ViewUtil;
import java.util.Calendar;
import java.util.Date;

class CallLogListItemHelper {
    private float mCallLogDurationTextSize;
    private boolean mIsFling;
    private final PhoneCallDetailsHelper mPhoneCallDetailsHelper;
    private final PhoneNumberHelper mPhoneNumberHelper;
    private final Resources mResources;

    public void setFlingMode(boolean aIsFling) {
        this.mIsFling = aIsFling;
    }

    public CallLogListItemHelper(PhoneCallDetailsHelper phoneCallDetailsHelper, PhoneNumberHelper phoneNumberHelper, Resources resources) {
        float dimension;
        this.mPhoneCallDetailsHelper = phoneCallDetailsHelper;
        this.mPhoneNumberHelper = phoneNumberHelper;
        this.mResources = resources;
        if (this.mResources != null) {
            dimension = this.mResources.getDimension(R.dimen.contact_calllog_item_duration_font);
        } else {
            dimension = 0.0f;
        }
        this.mCallLogDurationTextSize = dimension;
    }

    public void setPhoneCallDetails(CallLogListItemViews views, PhoneCallDetails details, boolean isHighlighted, String callId, boolean aFling, Context context) {
        this.mPhoneCallDetailsHelper.setPhoneCallDetails(views.phoneCallDetailsViews, details, isHighlighted, callId, aFling, false);
        if (views.durationTextView != null) {
            setDurationTextViewContext(views.durationTextView, details);
        }
        if (views.dateTextView != null) {
            setDateTextViewContext(views.dateTextView, details, context);
        }
        boolean canPlay = details.callTypes[0] == 4;
        if (!TextUtils.isEmpty(details.number) || details.getPresentation() == 3 || details.getPresentation() == 2 || details.getPresentation() == 4 || canPlay) {
            configureCallSecondaryAction(views, details);
        } else {
            views.secondaryActionViewLayout.setVisibility(8);
        }
    }

    private void setDurationTextViewContext(TextView durationTextView, PhoneCallDetails details) {
        if (durationTextView != null && details != null) {
            int callType = details.callTypes[0];
            durationTextView.setTextSize(0, this.mCallLogDurationTextSize);
            if (callType == 2 && details.duration == 0) {
                durationTextView.setText(R.string.miss_outgoing);
            } else if (CommonUtilMethods.isMissedType(details.callTypes[0])) {
                String result = "";
                if (5 == details.callTypes[0]) {
                    result = this.mResources.getString(R.string.call_reject);
                } else {
                    result = String.format(this.mResources.getQuantityText(R.plurals.contacts_ring_times, details.mRingTimes).toString(), new Object[]{Integer.valueOf(details.mRingTimes)});
                }
                durationTextView.setText(result);
            } else {
                durationTextView.setText(formatDuration(details.duration));
            }
        }
    }

    private void setDateTextViewContext(TextView dateTextView, PhoneCallDetails details, Context context) {
        String callDateTime = "";
        Calendar instance = Calendar.getInstance();
        long duration = Math.abs(instance.getTimeInMillis() - details.date);
        if (duration <= 60000) {
            callDateTime = context.getResources().getString(R.string.call_log_date_in_one_minute);
        } else if (duration <= 3600000) {
            callDateTime = String.format(context.getResources().getQuantityText(R.plurals.call_log_date_in_one_hour, ((int) duration) / 60000).toString(), new Object[]{Integer.valueOf(passMinute)});
        } else {
            int flagsWithYear = DateUtils.getYearTimeFormat();
            int flagsHourTime = DateUtils.getHourTimeFormat();
            int flagsMonthDate = DateUtils.getMonthDateFormat();
            int nowDate = instance.get(5);
            int nowMonth = instance.get(2);
            int nowYear = instance.get(1);
            Date date2 = new Date(details.date);
            int callDate = date2.getDate();
            int callMonth = date2.getMonth();
            int callYear = date2.getYear() + AMapException.CODE_AMAP_CLIENT_UNKNOWN_ERROR;
            boolean is24HourFormat = DateFormat.is24HourFormat(context);
            if (nowYear != callYear) {
                if (is24HourFormat) {
                    callDateTime = android.text.format.DateUtils.formatDateTime(context, details.date, flagsWithYear);
                } else {
                    callDateTime = CommonUtilMethods.convertTimeToBeDetailer(context, details.date);
                }
            } else if (nowDate - callDate == 0 && nowMonth == callMonth) {
                if (is24HourFormat) {
                    callDateTime = android.text.format.DateUtils.formatDateTime(context, details.date, flagsHourTime);
                } else {
                    callDateTime = CommonUtilMethods.convertTimeToBeDetailer(context, details.date);
                }
            } else if (nowDate - callDate == 1 && nowMonth == callMonth) {
                callDateTime = context.getResources().getString(R.string.str_yesterday);
            } else if (is24HourFormat) {
                callDateTime = android.text.format.DateUtils.formatDateTime(context, details.date, flagsMonthDate);
            } else {
                callDateTime = CommonUtilMethods.convertTimeToBeDetailer(context, details.date);
            }
        }
        dateTextView.setText(callDateTime);
    }

    private String formatDuration(long elapsedSeconds) {
        long minutes = 0;
        long hours = 0;
        if (elapsedSeconds >= 60) {
            minutes = elapsedSeconds / 60;
            if (minutes >= 60) {
                hours = minutes / 60;
                minutes -= 60 * hours;
                elapsedSeconds -= (60 * hours) * 60;
            }
            elapsedSeconds -= 60 * minutes;
        }
        long seconds = elapsedSeconds;
        String min = this.mResources.getQuantityString(R.plurals.callDetailsDurationFormatHours_Minutes, (int) minutes, new Object[]{Long.valueOf(minutes)});
        String sec = this.mResources.getQuantityString(R.plurals.callDetailsDurationFormatHours_Seconds, (int) seconds, new Object[]{Long.valueOf(seconds)});
        if (hours >= 1) {
            String h = this.mResources.getQuantityString(R.plurals.callDetailsDurationFormatHours_Hours, (int) hours, new Object[]{Long.valueOf(hours)});
            return this.mResources.getString(R.string.callDetailsDurationFormat_Merge, new Object[]{h, min, ""});
        } else if (minutes < 1) {
            return this.mResources.getString(R.string.callDetailsDurationFormat_Merge, new Object[]{"", "", sec});
        } else {
            return this.mResources.getString(R.string.callDetailsDurationFormat_Merge, new Object[]{"", min, sec});
        }
    }

    private void configureCallSecondaryAction(CallLogListItemViews views, PhoneCallDetails details) {
        views.secondaryActionViewLayout.setVisibility(0);
        if (!(views.secondaryActionView.getDrawable() instanceof CustomStateListDrawable)) {
            views.secondaryActionView.setImageResource(R.drawable.ic_information_normal);
            ViewUtil.setStateListIcon(views.secondaryActionView.getContext(), views.secondaryActionView, false);
        }
        if (!this.mIsFling && details != null) {
            views.secondaryActionView.setContentDescription(getCallActionDescription(details));
        }
    }

    private CharSequence getCallActionDescription(PhoneCallDetails details) {
        if (details == null) {
            return "";
        }
        CharSequence recipient;
        if (TextUtils.isEmpty(details.name)) {
            recipient = this.mPhoneNumberHelper.getDisplayNumber(details.number, details.getPresentation(), details.formattedNumber, details.postDialDigits, details.isVoicemailNumber);
        } else {
            recipient = details.name;
        }
        return this.mResources.getString(R.string.viewContactTitle, new Object[]{recipient});
    }
}
