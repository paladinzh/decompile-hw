package com.android.contacts;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteException;
import android.provider.ContactsContract.RawContacts;
import android.text.TextUtils;
import android.util.Log;
import com.android.contacts.hap.HwCustCommonConstants;
import com.android.contacts.util.HwCustContactFeatureUtils;

public class HwCustContactSaveServiceImpl extends HwCustContactSaveService {
    private static final String TAG = "HwCustContactSaveServiceImpl";

    public void updateVibrationPatternIntoDatabase(Context context, Intent intent, long rawContactId) {
        if (HwCustContactFeatureUtils.isVibrationPatternRequired()) {
            String pattern = intent.getStringExtra(HwCustCommonConstants.VIBRATION_PATTERN_KEY);
            if (!TextUtils.isEmpty(pattern)) {
                ContentValues contentValues = new ContentValues();
                contentValues.put("vibration_type", pattern);
                StringBuilder whereClause = new StringBuilder();
                whereClause.append("_id=").append(rawContactId);
                try {
                    context.getContentResolver().update(RawContacts.CONTENT_URI, contentValues, whereClause.toString(), null);
                } catch (IllegalArgumentException e) {
                    Log.e(TAG, e.getMessage());
                } catch (SQLiteException e2) {
                    Log.e(TAG, e2.getMessage());
                }
            }
        }
    }
}
