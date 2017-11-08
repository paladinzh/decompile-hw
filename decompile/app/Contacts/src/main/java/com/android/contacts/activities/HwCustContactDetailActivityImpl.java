package com.android.contacts.activities;

import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.os.Handler;
import android.provider.ContactsContract.RawContacts;
import android.provider.Settings.System;
import android.telephony.MSimTelephonyManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import com.android.contacts.ContactSaveService;
import com.android.contacts.model.Contact;
import com.android.contacts.util.HwCustContactFeatureUtils;
import com.google.android.gms.R;
import com.huawei.android.provider.SettingsEx.Systemex;
import com.huawei.android.telephony.TelephonyManagerEx;
import com.huawei.android.util.NoExtAPIException;
import java.util.List;

public class HwCustContactDetailActivityImpl extends HwCustContactDetailActivity {
    private static final String EXTRA_AUTO_DISMISS = "EXTRA_AUTO_DISMISS";
    private static final String TAG = "HwCustContactDetailActivityImpl";
    private Runnable mAutoDismiss = new Runnable() {
        public void run() {
            if (HwCustContactDetailActivityImpl.this.mContext != null && (HwCustContactDetailActivityImpl.this.mContext instanceof ContactDetailActivity)) {
                ContactDetailActivity activity = (ContactDetailActivity) HwCustContactDetailActivityImpl.this.mContext;
                if (!activity.isFinishing()) {
                    activity.finish();
                }
            }
        }
    };
    private Context mContext;
    private Handler mHandler = new Handler();

    public HwCustContactDetailActivityImpl(Context mContext) {
        super(mContext);
        this.mContext = mContext;
    }

    public boolean isCustHideGeoInfo() {
        boolean result = false;
        String mcc_ncc = TelephonyManager.getDefault().getSimOperator();
        if (TelephonyManager.getDefault().isMultiSimEnabled()) {
            int mSwitchDualCardSlot = 0;
            try {
                mSwitchDualCardSlot = TelephonyManagerEx.getDefault4GSlotId();
            } catch (NoExtAPIException e) {
                Log.v(TAG, "TelephonyManagerEx.getDefault4GSlotId()->NoExtAPIException!");
            }
            mcc_ncc = MSimTelephonyManager.getDefault().getSimOperator(mSwitchDualCardSlot);
        }
        String configString = System.getString(this.mContext.getContentResolver(), "hw_hide_call_geo_info");
        if ("true".equals(configString)) {
            return true;
        }
        if (!TextUtils.isEmpty(configString) && !TextUtils.isEmpty(mcc_ncc)) {
            String[] custValues = configString.trim().split(";");
            int size = custValues.length;
            int i = 0;
            while (i < size) {
                if (mcc_ncc.startsWith(custValues[i]) || mcc_ncc.equalsIgnoreCase(custValues[i])) {
                    result = true;
                    break;
                }
                i++;
            }
        }
        Log.d(TAG, "mcc_ncc =" + mcc_ncc + ", configString=" + configString);
        return result;
    }

    public void custOnCreate() {
        custAutoDismiss();
    }

    private void custAutoDismiss() {
        if (this.mContext instanceof ContactDetailActivity) {
            ContactDetailActivity activity = this.mContext;
            Intent intent = activity.getIntent();
            if (intent == null || !intent.getBooleanExtra(EXTRA_AUTO_DISMISS, false)) {
                Log.i(TAG, "Not supposed to be auto dismissed");
                return;
            }
            activity.setRequestedOrientation(14);
            this.mHandler.postDelayed(this.mAutoDismiss, Systemex.getLong(this.mContext.getContentResolver(), "hw_auto_dismiss_dur", 3000));
        }
    }

    public boolean supportReadOnly() {
        return HwCustContactFeatureUtils.isSupportPredefinedReadOnlyFeature();
    }

    public boolean isReadOnlyContact(Uri contactUri) {
        boolean readonly = false;
        Cursor cursor = null;
        try {
            cursor = this.mContext.getContentResolver().query(RawContacts.CONTENT_URI, new String[]{"_id"}, "_id = ? AND raw_contact_is_read_only = 1", new String[]{String.valueOf(getRawContactID(contactUri))}, null);
            if (cursor == null) {
                if (cursor != null) {
                    cursor.close();
                }
                return false;
            }
            readonly = cursor.getCount() >= 1;
            if (cursor != null) {
                cursor.close();
            }
            return readonly;
        } catch (SQLiteException e) {
            Log.w(TAG, "=====Error is thrown-- Stop querying the db=====" + e);
            if (cursor != null) {
                cursor.close();
            }
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    private long getRawContactID(Uri lookupUri) {
        if (lookupUri == null) {
            return -1;
        }
        List<String> pathSegments = lookupUri.getPathSegments();
        long rawContactId = -1;
        if (pathSegments.size() == 4) {
            String lookupKey = (String) pathSegments.get(2);
            int start = lookupKey.lastIndexOf("r") + 1;
            int end = 0;
            if (start > 1 && start < lookupKey.length()) {
                int temp = lookupKey.substring(start).indexOf("-");
                if (temp > 0) {
                    end = temp + start;
                }
            }
            if (start > 1 && start < lookupKey.length() && end > 0 && end < lookupKey.length() && start < end) {
                try {
                    rawContactId = Long.parseLong(lookupKey.substring(start, end));
                } catch (Exception e) {
                    rawContactId = -1;
                }
            }
        }
        return rawContactId;
    }

    public boolean joinContactsRequired() {
        return HwCustContactFeatureUtils.isJoinFeatureEnabled();
    }

    public void joinAggregate(Intent aData) {
        Log.i(TAG, "ContactLoaderFragment:REQUEST_CODE_JOIN");
        long contactId = ContentUris.parseId(aData.getData());
        this.mContext.getApplicationContext().startService(ContactSaveService.createJoinContactsIntent(this.mContext.getApplicationContext(), aData.getLongExtra("com.android.contacts.action.CONTACT_ID", -1), contactId, true, JoinContactActivity.class, "joinCompleted"));
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void checkAndAddIntentExtra(Intent aIntent, Contact aContactData, Context aContext) {
        if (!(aIntent == null || aContactData == null || aContext == null || !aContactData.isUserProfile() || !HwCustContactFeatureUtils.isShowMyInfoForMyProfile() || 40 == aContactData.getDisplayNameSource())) {
            aIntent.putExtra("name", aContext.getResources().getString(R.string.string_aab_my_info));
            aIntent.putExtra("requestid", -1);
        }
    }

    public void addCustomIntentExtrasForCnap(Intent receivedIntent, Intent targetIntent) {
        if (HwCustContactFeatureUtils.isCNAPFeatureSupported(this.mContext) && receivedIntent != null && targetIntent != null) {
            String name = receivedIntent.getStringExtra("contact_display_name");
            if (!TextUtils.isEmpty(name)) {
                targetIntent.putExtra("name", name);
            }
        }
    }

    public String getCnapNameExtraFromIntent(Intent receivedIntent, String defaultName) {
        if (!HwCustContactFeatureUtils.isCNAPFeatureSupported(this.mContext) || receivedIntent == null) {
            return defaultName;
        }
        String callLogName = receivedIntent.getStringExtra("contact_display_name");
        if (TextUtils.isEmpty(callLogName)) {
            return defaultName;
        }
        return callLogName;
    }

    public void putNameExtraToIntent(Intent aIntent, Cursor cursor) {
        if (HwCustContactFeatureUtils.isCNAPFeatureSupported(this.mContext) && aIntent != null && cursor != null && !cursor.isClosed()) {
            String name = cursor.getString(8);
            if (!TextUtils.isEmpty(name)) {
                aIntent.putExtra("contact_display_name", name);
            }
        }
    }
}
