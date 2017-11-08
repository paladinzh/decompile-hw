package com.android.mms.transaction;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.Telephony.Carriers;
import android.text.TextUtils;
import cn.com.xy.sms.sdk.db.entity.NumberInfo;
import com.android.mms.ui.MessageUtils;
import com.huawei.cspcommon.MLog;
import com.huawei.cspcommon.ex.ErrorMonitor;
import com.huawei.cspcommon.ex.SqliteWrapper;
import com.huawei.cust.HwCustUtils;
import com.huawei.mms.util.MmsCommon;

public class TransactionSettings {
    private static final String[] APN_PROJECTION = new String[]{NumberInfo.TYPE_KEY, "mmsc", "mmsproxy", "mmsport"};
    private HwCustTransactionSettings mHwCustTransactionSettings;
    private String mProxyAddress;
    private int mProxyPort;
    private String mServiceCenter;

    public TransactionSettings(Context context, String apnName) {
        this(context, apnName, 0);
    }

    public TransactionSettings(String mmscUrl, String proxyAddr, int proxyPort) {
        String trim;
        this.mProxyPort = -1;
        this.mHwCustTransactionSettings = (HwCustTransactionSettings) HwCustUtils.createObj(HwCustTransactionSettings.class, new Object[0]);
        if (mmscUrl != null) {
            trim = mmscUrl.trim();
        } else {
            trim = null;
        }
        this.mServiceCenter = trim;
        this.mProxyAddress = proxyAddr;
        this.mProxyPort = proxyPort;
    }

    public String getMmscUrl() {
        return this.mServiceCenter;
    }

    public String getProxyAddress() {
        return this.mProxyAddress;
    }

    public int getProxyPort() {
        return this.mProxyPort;
    }

    public boolean isProxySet() {
        return (this.mProxyAddress == null || this.mProxyAddress.trim().length() == 0) ? false : true;
    }

    private static boolean isValidApnType(String types, String requestType) {
        if (TextUtils.isEmpty(types)) {
            return true;
        }
        for (String t : types.split(",")) {
            if (t.equals(requestType) || t.equals("*")) {
                return true;
            }
        }
        return false;
    }

    public TransactionSettings(Context context, String apnName, int slotId) {
        this.mProxyPort = -1;
        this.mHwCustTransactionSettings = (HwCustTransactionSettings) HwCustUtils.createObj(HwCustTransactionSettings.class, new Object[0]);
        MLog.i("Mms_TXM_Settings", "TransactionSettings slot id:" + slotId);
        Cursor cursor = null;
        String str = "";
        if (!MmsCommon.PLATFORM_MTK || !MessageUtils.isMultiSimEnabled()) {
            String[] strArr = null;
            str = "current IS NOT NULL";
            if (!TextUtils.isEmpty(apnName)) {
                str = str + " AND apn=?";
                strArr = new String[]{apnName.trim()};
            }
            Uri uri = Carriers.CONTENT_URI;
            if (MessageUtils.isMultiSimEnabled()) {
                uri = Uri.withAppendedPath(Uri.parse("content://telephony/carriers/subId"), String.valueOf(slotId));
            }
            String sortOrder = null;
            if (this.mHwCustTransactionSettings != null) {
                sortOrder = this.mHwCustTransactionSettings.getCustSortOrder();
            }
            cursor = SqliteWrapper.query(context, context.getContentResolver(), uri, APN_PROJECTION, str, strArr, sortOrder);
        } else if (slotId >= 0) {
            str = apnName != null ? "apn='" + apnName.trim() + "'" : null;
            cursor = SqliteWrapper.query(context, context.getContentResolver(), Uri.withAppendedPath(Uri.parse("content://telephony/carriers_sim" + (slotId + 1)), "current"), APN_PROJECTION, str, null, null);
        } else {
            MLog.e("Mms_TXM_Settings", "Invalide slot id:" + slotId);
        }
        if (MLog.isLoggable("Mms_TXN", 2)) {
            MLog.v("Mms_TXM_Settings", "TransactionSettings looking for apn: " + str + " returned: " + (cursor == null ? "null cursor" : cursor.getCount() + " hits"));
        }
        if (cursor == null) {
            MLog.e("Mms_TXM_Settings", "Apn is not found in Database!");
            return;
        }
        boolean sawValidApn = false;
        while (cursor.moveToNext() && TextUtils.isEmpty(this.mServiceCenter)) {
            if (isValidApnType(cursor.getString(0), "mms")) {
                sawValidApn = true;
                this.mServiceCenter = cursor.getString(1) != null ? cursor.getString(1).trim() : null;
                this.mProxyAddress = cursor.getString(2);
                if (isProxySet()) {
                    portString = cursor.getString(3);
                    try {
                        this.mProxyPort = Integer.parseInt(portString);
                    } catch (NumberFormatException e) {
                        String portString;
                        if (TextUtils.isEmpty(portString)) {
                            MLog.w("Mms_TXM_Settings", "mms port not set!");
                        } else {
                            MLog.e("Mms_TXM_Settings", "apn " + apnName + " slot=" + slotId, (Throwable) e);
                        }
                    } catch (Throwable th) {
                        cursor.close();
                    }
                } else {
                    continue;
                }
            }
        }
        cursor.close();
        HwCustTransactionSettings hwCustTransactionSettings = (HwCustTransactionSettings) HwCustUtils.createObj(HwCustTransactionSettings.class, new Object[0]);
        if (hwCustTransactionSettings != null) {
            HwCustMMSProxyDetails hwCustMMSProxyDetails = hwCustTransactionSettings.getCustomizedMmsProxyDetails(context, new HwCustMMSProxyDetails(this.mServiceCenter, this.mProxyAddress, this.mProxyPort));
            this.mServiceCenter = hwCustMMSProxyDetails.mServiceCenter;
            this.mProxyAddress = hwCustMMSProxyDetails.mProxyAddress;
            this.mProxyPort = hwCustMMSProxyDetails.mProxyPort;
        }
        MLog.v("Mms_TXM_Settings", "APN setting: MMSC: " + this.mServiceCenter + " looked for: " + str);
        if (sawValidApn && TextUtils.isEmpty(this.mServiceCenter)) {
            ErrorMonitor.reportRadar(907000016, 2, 0, "Invalid APN setting: MMSC is empty" + apnName + " slot=" + slotId, "");
        }
    }
}
