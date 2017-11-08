package com.android.contacts.util;

import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.text.TextUtils;
import android.text.format.DateUtils;
import com.google.android.gms.R;

public class ContactBadgeUtil {
    public static CharSequence getSocialDate(StreamItemEntry streamItem, Context context) {
        CharSequence timestampDisplayValue = DateUtils.getRelativeTimeSpanString(streamItem.getTimestamp(), System.currentTimeMillis(), 60000, 262144);
        String labelDisplayValue = null;
        String statusLabelRes = streamItem.getLabelRes();
        String statusResPackage = streamItem.getResPackage();
        String identiferPackage = statusResPackage;
        if (statusLabelRes != null) {
            Resources resources;
            if (TextUtils.isEmpty(statusResPackage)) {
                resources = context.getResources();
                identiferPackage = "android";
            } else {
                try {
                    resources = context.getPackageManager().getResourcesForApplication(statusResPackage);
                } catch (NameNotFoundException e) {
                    HwLog.w("ContactBadgeUtil", "Contact status update resource package not found: " + statusResPackage);
                    resources = null;
                }
            }
            if (resources != null) {
                int resId = resources.getIdentifier(statusLabelRes, "string", identiferPackage);
                if (resId == 0) {
                    HwLog.w("ContactBadgeUtil", "Contact status update resource not found: " + statusLabelRes + " in " + statusResPackage);
                } else {
                    labelDisplayValue = resources.getString(resId);
                }
            }
        }
        if (timestampDisplayValue != null && labelDisplayValue != null) {
            return context.getString(R.string.contact_status_update_attribution_with_date, new Object[]{timestampDisplayValue, labelDisplayValue});
        } else if (timestampDisplayValue == null && labelDisplayValue != null) {
            return context.getString(R.string.contact_status_update_attribution, new Object[]{labelDisplayValue});
        } else if (timestampDisplayValue != null) {
            return timestampDisplayValue;
        } else {
            return null;
        }
    }
}
