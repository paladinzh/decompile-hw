package com.android.mms.transaction;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.os.SystemProperties;
import android.text.TextUtils;
import android.util.Log;
import com.android.mms.ui.HwCustPreferenceUtilsImpl;
import com.huawei.cspcommon.ex.SqliteWrapper;
import com.huawei.sprint.chameleon.provider.ChameleonContract;
import java.util.ArrayList;

public class HwCustTransactionSettingsImpl extends HwCustTransactionSettings {
    private static final int ROW_MMSPROXY = 1;
    private static final String TAG = "HwCustTransactionSettingsImpl";

    public boolean isChameleonEnabled() {
        return HwCustPreferenceUtilsImpl.IS_SPRINT;
    }

    public HwCustMMSProxyDetails getCustomizedMmsProxyDetails(Context context, HwCustMMSProxyDetails aHwCustMMSProxyDetails) {
        if (!isChameleonEnabled()) {
            return aHwCustMMSProxyDetails;
        }
        ArrayList<String> customizedMmsProxyDetails = queryChameleonProviderForMmsProxy(context);
        if (customizedMmsProxyDetails == null || customizedMmsProxyDetails.size() == 0) {
            return aHwCustMMSProxyDetails;
        }
        HwCustMMSProxyDetails custMMSProxyDetails = new HwCustMMSProxyDetails();
        custMMSProxyDetails.mServiceCenter = (String) customizedMmsProxyDetails.get(0);
        custMMSProxyDetails.mProxyAddress = (String) customizedMmsProxyDetails.get(1);
        custMMSProxyDetails.mProxyPort = Integer.parseInt((String) customizedMmsProxyDetails.get(2));
        return custMMSProxyDetails;
    }

    private ArrayList<String> queryChameleonProviderForMmsProxy(Context context) {
        SQLException e;
        Throwable th;
        String[] PROJECTION = new String[]{HwCustHttpUtilsImpl.CHAMELEON_COLUMNS_VALUE};
        ArrayList<String> arrayList = null;
        Context context2 = context;
        Cursor cursor = SqliteWrapper.query(context2, context.getContentResolver(), ChameleonContract.CONTENT_URI, PROJECTION, "_index in (589,590)", null, null);
        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    ArrayList<String> proxyValuesFromCursor = new ArrayList();
                    int cursorCounter = 0;
                    do {
                        try {
                            String cursorValue = cursor.getString(0);
                            if (!TextUtils.isEmpty(cursorValue)) {
                                if (1 == cursorCounter) {
                                    String[] mmsProxy = cursorValue.split(":");
                                    proxyValuesFromCursor.add(mmsProxy[0]);
                                    proxyValuesFromCursor.add(mmsProxy[1]);
                                } else {
                                    proxyValuesFromCursor.add(cursorValue);
                                }
                            }
                            cursorCounter++;
                        } catch (SQLException e2) {
                            e = e2;
                            arrayList = proxyValuesFromCursor;
                        } catch (Throwable th2) {
                            th = th2;
                        }
                    } while (cursor.moveToNext());
                    arrayList = proxyValuesFromCursor;
                }
                cursor.close();
            } catch (SQLException e3) {
                e = e3;
                try {
                    Log.e(TAG, "queryChameleonProviderForMmsProxy Error is thrown-- Stop querying the db " + e);
                    cursor.close();
                    return arrayList;
                } catch (Throwable th3) {
                    th = th3;
                    cursor.close();
                    throw th;
                }
            }
        }
        return arrayList;
    }

    public String getCustSortOrder() {
        if (SystemProperties.getBoolean("ro.config.reverse_mms_apn", false)) {
            return "_id DESC";
        }
        return null;
    }
}
