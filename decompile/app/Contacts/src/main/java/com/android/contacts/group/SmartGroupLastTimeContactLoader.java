package com.android.contacts.group;

import android.content.Context;
import android.content.CursorLoader;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.MatrixCursor.RowBuilder;
import com.android.contacts.util.HwLog;
import com.google.android.gms.R;

public class SmartGroupLastTimeContactLoader extends CursorLoader {
    private static String CONTACT_IN_THREE_MONTHS = "3";
    private static String CONTACT_MONTHLY = CallInterceptDetails.UNBRANDED_STATE;
    private static String CONTACT_OVER_THREE_MONTHS = "4";
    private static final String[] CONTACT_TYPE = new String[]{CONTACT_WEEKLY, CONTACT_MONTHLY, CONTACT_IN_THREE_MONTHS, CONTACT_OVER_THREE_MONTHS};
    private static String CONTACT_WEEKLY = CallInterceptDetails.BRANDED_STATE;
    private static final String[] PROJECTION = new String[]{"frequent_type", "count"};
    private String[] mContactType = new String[4];
    private Context mContext;
    private Resources mResources;

    public SmartGroupLastTimeContactLoader(Context context) {
        super(context);
        this.mContext = context;
        this.mResources = this.mContext.getResources();
        initContactType(this.mContext);
    }

    private void initContactType(Context context) {
        this.mContactType[0] = this.mResources.getString(R.string.contacts_within_one_week, new Object[]{Integer.valueOf(1)});
        this.mContactType[1] = this.mResources.getString(R.string.contacts_within_one_month, new Object[]{Integer.valueOf(1), Integer.valueOf(1)});
        this.mContactType[2] = this.mResources.getString(R.string.contacts_within_three_months, new Object[]{Integer.valueOf(3), Integer.valueOf(1)});
        this.mContactType[3] = this.mResources.getString(R.string.contacts_contact_over_three_month, new Object[]{Integer.valueOf(3)});
    }

    public Cursor loadInBackground() {
        Throwable th;
        Cursor cursor = null;
        Cursor cursor2 = null;
        try {
            cursor = this.mContext.getContentResolver().query(SmartGroupUtil.LAST_TIME_CONTACT_URI, null, null, null, null);
            int i = 0;
            MatrixCursor lastContactTimeCursor = new MatrixCursor(PROJECTION);
            try {
                int count = cursor.getCount();
                RowBuilder builder1;
                if (count > 0) {
                    while (cursor.moveToNext() && i < CONTACT_TYPE.length) {
                        if (CONTACT_TYPE[i].equals(cursor.getString(1))) {
                            builder1 = lastContactTimeCursor.newRow();
                            builder1.add(this.mContactType[i]);
                            builder1.add(Integer.valueOf(cursor.getInt(0)));
                            if (cursor.getPosition() == count - 1) {
                                cursor.moveToPrevious();
                            }
                        } else {
                            builder1 = lastContactTimeCursor.newRow();
                            builder1.add(this.mContactType[i]);
                            builder1.add(Integer.valueOf(0));
                            cursor.moveToPrevious();
                        }
                        i++;
                    }
                } else {
                    while (i < CONTACT_TYPE.length) {
                        builder1 = lastContactTimeCursor.newRow();
                        builder1.add(this.mContactType[i]);
                        builder1.add(Integer.valueOf(0));
                        i++;
                    }
                }
                if (cursor != null) {
                    cursor.close();
                }
                return lastContactTimeCursor;
            } catch (RuntimeException e) {
                cursor2 = lastContactTimeCursor;
            } catch (Throwable th2) {
                th = th2;
            }
        } catch (RuntimeException e2) {
            try {
                HwLog.e("SmartGroupLastTimeContactLoader", "query error!");
                if (cursor == null) {
                    return cursor2;
                }
                cursor.close();
                return cursor2;
            } catch (Throwable th3) {
                th = th3;
                if (cursor != null) {
                    cursor.close();
                }
                throw th;
            }
        }
    }
}
