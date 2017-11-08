package com.android.contacts.hap;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.SystemProperties;
import android.provider.CallLog.Calls;
import android.telephony.PhoneNumberUtils;
import android.text.TextUtils;
import android.util.Log;
import android.widget.TextView;
import com.android.contacts.util.HwCustContactFeatureUtils;
import java.util.Locale;

public class HwCustCommonUtilMethodsImpl extends HwCustCommonUtilMethods {
    private static final String TAG = "HwCustCommonUtilMethodsImpl";
    private static final boolean isNeedAllLeftDirection = SystemProperties.getBoolean("ro.config.all_left_direction", false);

    public boolean isEnableSimAddPlus(boolean charAdded, boolean isDialable, boolean isCLIR) {
        if (!SystemProperties.getBoolean("ro.config.sim_store_plus", false) || isCLIR) {
            return charAdded;
        }
        return isDialable;
    }

    public boolean isHebrewLanForDialpad() {
        String specialLanguages = "iw";
        if (!"iw".equals(Locale.getDefault().getLanguage())) {
            return false;
        }
        Log.d(TAG, "isSpecialLanguageForDialpad Hebrew true");
        return true;
    }

    public boolean isAllowSprintRedialInEmergencyMode(String number, Context context) {
        if (HwCustContactFeatureUtils.isSupportSprintEmergencyModeRedial()) {
            return HwCustContactFeatureUtils.allowRedialEmergencyMode(number, context);
        }
        return true;
    }

    public int queryLastCallNumberFromCust(String number, Context context) {
        int slotId = -1;
        if (TextUtils.isEmpty(number) || context == null) {
            return -1;
        }
        String[] projection = new String[]{"number", "subscription_id", "encrypt_call"};
        String selection = "_id IN ( SELECT _id FROM Calls WHERE features<> 32 AND PHONE_NUMBERS_EQUAL(number,?) GROUP BY number )";
        Cursor cursor = null;
        int encrypt = -1;
        try {
            cursor = context.getContentResolver().query(Calls.CONTENT_URI, projection, selection, new String[]{number}, "date DESC");
            if (cursor != null && cursor.getCount() > 0 && cursor.moveToFirst()) {
                while (!CommonUtilMethods.compareNumsHw(number, cursor.getString(cursor.getColumnIndex("number")))) {
                    if (!cursor.moveToNext()) {
                        break;
                    }
                }
                slotId = cursor.getInt(cursor.getColumnIndex("subscription_id"));
                encrypt = cursor.getInt(cursor.getColumnIndex("encrypt_call"));
            }
            if (cursor != null) {
                cursor.close();
            }
        } catch (RuntimeException e) {
            Log.d(TAG, "can't get slot id!");
            if (cursor != null) {
                cursor.close();
            }
        } catch (Exception e2) {
            Log.d(TAG, "can't get slot id!");
            if (cursor != null) {
                cursor.close();
            }
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
        }
        if (encrypt == 1) {
            if (slotId == 0) {
                slotId = 2;
            } else if (slotId == 1) {
                slotId = 3;
            }
        }
        return slotId;
    }

    public boolean checkAndInitCall(Context aContext, Intent aIntent) {
        if (aIntent == null || aContext == null) {
            return false;
        }
        return HwCustContactFeatureUtils.checkAndInitCall(aContext, PhoneNumberUtils.getNumberFromIntent(aIntent, aContext));
    }

    public void setNameViewDirection(TextView view) {
        if (isNeedAllLeftDirection && view != null && !CommonUtilMethods.isLayoutRTL()) {
            view.setTextDirection(5);
        }
    }
}
