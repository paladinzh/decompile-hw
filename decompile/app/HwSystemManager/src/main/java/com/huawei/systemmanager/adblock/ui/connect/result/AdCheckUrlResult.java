package com.huawei.systemmanager.adblock.ui.connect.result;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.SparseArray;
import com.huawei.systemmanager.adblock.comm.AdConst.CloudResult;
import com.huawei.systemmanager.adblock.ui.apkdlcheck.DlBlockManager;
import com.huawei.systemmanager.adblock.ui.apkdlcheck.DlBlockManager.Record;
import com.huawei.systemmanager.comm.misc.Closeables;
import com.huawei.systemmanager.optimize.smcs.SMCSDatabaseConstant.DlBlockColumns;
import com.huawei.systemmanager.util.HwLog;
import com.huawei.systemmanager.util.app.HsmPackageManager;
import com.huawei.systemmanager.util.app.HsmPkgInfo;
import com.huawei.systemmanager.util.app.PackageManagerWrapper;
import java.io.Closeable;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONException;
import org.json.JSONObject;

public class AdCheckUrlResult implements DlBlockColumns {
    public static final int CONTENT_DOWNLOADER_PKGNAME = 1;
    public static final int CONTENT_DOWNLOAD_APK_APPNAME = 3;
    public static final int CONTENT_DOWNLOAD_APK_PKGNAME = 2;
    public static final int CONTENT_OPT_POLICY = 4;
    static final String[] CONTENT_PROJECTION = new String[]{DlBlockColumns.COLUMN_UID_PKGNAME, DlBlockColumns.COLUMN_DOWNLOADER_PKGNAME, DlBlockColumns.COLUMN_DOWNLOAD_APK_PKG_NAME, DlBlockColumns.COLUMN_DOWNLOAD_APK_APPNAME, DlBlockColumns.COLUMN_OPT_POLICY, DlBlockColumns.COLUMN_TIMESTAMP};
    public static final int CONTENT_TIMESTAMP = 5;
    public static final int CONTENT_UID_PKGNAME = 0;
    public static final Uri CONTENT_URI = Uri.parse("content://smcs/dl_block_table");
    public static final String DL_BLOCK_CHANGE_ACTION = "com.huawei.systemmanager.action.dlblock.datachange";
    private static final int MAX_RECORD_SIZE = 1000;
    private static final String TAG = "DlBlock";
    private String mApkAppName = "";
    private String mApkIcon = "";
    private String mApkPkgName = "";
    private long mApkSize = 0;
    private String mCancelBtnText = "";
    private String mContinueBtnText = "";
    private String mDetailId = "";
    private String mDownloaderAppName = "";
    private Drawable mDownloaderIcon;
    private String mDownloaderPkgName = "";
    private String mOfficalBtnText = "";
    private int mOptPolicy = 0;
    private long mTimestamp = 0;
    private String mTips = "";
    private String mUidApkPkgName = "";

    public static class UpdateRecordOptRunnable implements Runnable {
        Context mContext;
        boolean mNotifyChange;
        int mOptPolicy;
        String mPkg;
        String mUidPkg;

        public UpdateRecordOptRunnable(Context context, String uidPkg, String pkg, int optPolicy, boolean notifyChange) {
            this.mContext = context;
            this.mUidPkg = uidPkg;
            this.mPkg = pkg;
            this.mOptPolicy = optPolicy;
            this.mNotifyChange = notifyChange;
        }

        public void run() {
            AdCheckUrlResult.updateOptPolicy(this.mContext, this.mUidPkg, this.mPkg, this.mOptPolicy, this.mNotifyChange);
        }
    }

    public static class UpdateTimestampRunnable implements Runnable {
        Context mContext;
        String mPkg;
        int mUid;

        public UpdateTimestampRunnable(Context context, int uid, String pkg) {
            this.mContext = context;
            this.mUid = uid;
            this.mPkg = pkg;
        }

        public void run() {
            AdCheckUrlResult.updateTimestamp(this.mContext, this.mUid, this.mPkg);
        }
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[result:");
        sb.append(", mUidApkPkgName=").append(this.mUidApkPkgName);
        sb.append(", mDownloaderPkgName=").append(this.mDownloaderPkgName);
        sb.append(", mDetailId=").append(this.mDetailId);
        sb.append(", mApkPkgName=").append(this.mApkPkgName);
        sb.append(", mApkIcon=").append(this.mApkIcon);
        sb.append(", mApkAppName=").append(this.mApkAppName);
        sb.append(", mApkSize=").append(this.mApkSize);
        sb.append(", mTips=").append(this.mTips);
        sb.append(", mOfficalBtnText=").append(this.mOfficalBtnText);
        sb.append(", mCancelBtnText=").append(this.mCancelBtnText);
        sb.append(", mContinueBtnText=").append(this.mContinueBtnText);
        sb.append(", mOptPolicy=").append(this.mOptPolicy);
        sb.append("]");
        return sb.toString();
    }

    public ContentValues toContentValues() {
        ContentValues values = new ContentValues();
        values.put(DlBlockColumns.COLUMN_UID_PKGNAME, this.mUidApkPkgName);
        values.put(DlBlockColumns.COLUMN_DOWNLOADER_PKGNAME, this.mDownloaderPkgName);
        values.put(DlBlockColumns.COLUMN_DOWNLOAD_APK_PKG_NAME, this.mApkPkgName);
        values.put(DlBlockColumns.COLUMN_DOWNLOAD_APK_APPNAME, this.mApkAppName);
        values.put(DlBlockColumns.COLUMN_OPT_POLICY, Integer.valueOf(this.mOptPolicy));
        values.put(DlBlockColumns.COLUMN_TIMESTAMP, Long.valueOf(this.mTimestamp));
        return values;
    }

    private void restore(Cursor cursor) {
        this.mUidApkPkgName = cursor.getString(0);
        this.mDownloaderPkgName = cursor.getString(1);
        this.mApkPkgName = cursor.getString(2);
        this.mApkAppName = cursor.getString(3);
        this.mOptPolicy = cursor.getInt(4);
    }

    public void saveOrUpdate(Context context) {
        this.mTimestamp = System.currentTimeMillis();
        if (getRecordSize(context) > 1000) {
            deleteRecordOverTime(context);
        }
        if (update(context, toContentValues()) <= 0) {
            save(context);
        }
        notifyDataChange(context);
    }

    private static void notifyDataChange(Context context) {
        LocalBroadcastManager.getInstance(context.getApplicationContext()).sendBroadcast(new Intent(DL_BLOCK_CHANGE_ACTION));
    }

    private Uri save(Context context) {
        HwLog.i(TAG, "save->insert uid&download=" + this.mUidApkPkgName);
        return context.getContentResolver().insert(CONTENT_URI, toContentValues());
    }

    private int update(Context context, ContentValues contentValues) {
        HwLog.i(TAG, "update uid&download=" + this.mUidApkPkgName);
        return context.getContentResolver().update(CONTENT_URI, contentValues, getWhere(this.mUidApkPkgName), null);
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static List<AdCheckUrlResult> getAllDlBlockList(Context context) {
        ArrayList<AdCheckUrlResult> dlBlockList = new ArrayList();
        try {
            Cursor cursor = context.getContentResolver().query(CONTENT_URI, CONTENT_PROJECTION, null, null, "timestamp DESC");
            while (cursor != null && cursor.moveToNext()) {
                AdCheckUrlResult dlBlock = new AdCheckUrlResult();
                dlBlock.restore(cursor);
                dlBlockList.add(dlBlock);
            }
            Closeables.close(cursor);
        } catch (Exception e) {
            HwLog.w(TAG, "getAllAdBlocks Exception", e);
        } catch (Throwable th) {
            Closeables.close(null);
        }
        return dlBlockList;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static SparseArray<Record> getAllDlBlockArray(Context context, String selection, String[] selectionArgs, String sortOrder) {
        SparseArray<Record> dlBlockList = new SparseArray();
        try {
            Cursor cursor = context.getContentResolver().query(CONTENT_URI, CONTENT_PROJECTION, selection, selectionArgs, sortOrder);
            while (cursor != null && cursor.moveToNext()) {
                String uidDownloadUrl = cursor.getString(0);
                Record record = new Record();
                record.mApkName = cursor.getString(3);
                record.mPkgName = cursor.getString(2);
                record.mOptPolicy = cursor.getInt(4);
                dlBlockList.put(uidDownloadUrl.hashCode(), record);
            }
            Closeables.close(cursor);
        } catch (Exception e) {
            HwLog.w(TAG, "getAllAdBlocks Exception", e);
        } catch (Throwable th) {
            Closeables.close(null);
        }
        return dlBlockList;
    }

    public static Record getDlBlockByUIDPkg(Context context, String uidPkg) {
        Exception e;
        Throwable th;
        Record record = null;
        Closeable closeable = null;
        try {
            closeable = context.getContentResolver().query(CONTENT_URI, CONTENT_PROJECTION, getWhere(uidPkg), null, null);
            if (closeable != null && closeable.moveToNext()) {
                Record record2 = new Record();
                try {
                    record2.mApkName = closeable.getString(3);
                    record2.mPkgName = closeable.getString(2);
                    record2.mOptPolicy = closeable.getInt(4);
                    record = record2;
                } catch (Exception e2) {
                    e = e2;
                    record = record2;
                    try {
                        HwLog.w(TAG, "getAllAdBlocks Exception", e);
                        Closeables.close(closeable);
                        return record;
                    } catch (Throwable th2) {
                        th = th2;
                        Closeables.close(closeable);
                        throw th;
                    }
                } catch (Throwable th3) {
                    th = th3;
                    Closeables.close(closeable);
                    throw th;
                }
            }
            Closeables.close(closeable);
        } catch (Exception e3) {
            e = e3;
            HwLog.w(TAG, "getAllAdBlocks Exception", e);
            Closeables.close(closeable);
            return record;
        }
        return record;
    }

    private static String getWhere(String uid_pkgName) {
        return "uid_pkg='" + uid_pkgName + "'";
    }

    public static int deleteRecordOverTime(Context context) {
        long overTimer = System.currentTimeMillis() - 604800000;
        int returnCode = context.getContentResolver().delete(CONTENT_URI, "timestamp < ?", new String[]{String.valueOf(overTimer)});
        if (returnCode > 0) {
            notifyDataChange(context);
        }
        return returnCode;
    }

    public static int getBlockRecordSize(Context context) {
        Closeable closeable = null;
        try {
            closeable = context.getContentResolver().query(CONTENT_URI, new String[]{DlBlockColumns.COLUMN_OPT_POLICY}, "opt_policy = ?", new String[]{String.valueOf(4)}, null);
            if (closeable == null) {
                HwLog.e(TAG, "getBlackListCount cursor is null!");
                return 0;
            }
            int count = closeable.getCount();
            Closeables.close(closeable);
            return count;
        } catch (Exception e) {
            HwLog.e(TAG, "getWhiteListCount", e);
            return 0;
        } finally {
            Closeables.close(closeable);
        }
    }

    public static int getRecordSize(Context context) {
        Closeable closeable = null;
        try {
            closeable = context.getContentResolver().query(CONTENT_URI, new String[]{DlBlockColumns.COLUMN_OPT_POLICY}, null, null, null);
            if (closeable == null) {
                HwLog.e(TAG, "getBlackListCount cursor is null!");
                return 0;
            }
            int count = closeable.getCount();
            Closeables.close(closeable);
            return count;
        } catch (Exception e) {
            HwLog.e(TAG, "getWhiteListCount", e);
            return 0;
        } finally {
            Closeables.close(closeable);
        }
    }

    public static int updateOptPolicy(Context context, String uidPkg, String pkg, int optPolicy, boolean notifyChange) {
        HwLog.i(TAG, "updateOptPolicy uidPkg=" + uidPkg);
        ContentValues values = new ContentValues();
        values.put(DlBlockColumns.COLUMN_OPT_POLICY, Integer.valueOf(optPolicy));
        DlBlockManager.getInstance().clearTempRecord(pkg);
        int returnCode = context.getContentResolver().update(CONTENT_URI, values, getWhere(uidPkg), null);
        if (returnCode > 0 && notifyChange) {
            notifyDataChange(context);
        }
        return returnCode;
    }

    public static int updateTimestamp(Context context, int uid, String pkg) {
        long nowTime = System.currentTimeMillis();
        ContentValues values = new ContentValues();
        values.put(DlBlockColumns.COLUMN_TIMESTAMP, Long.valueOf(nowTime));
        int returnCode = context.getContentResolver().update(CONTENT_URI, values, getWhere(uid + pkg), null);
        if (returnCode > 0) {
            notifyDataChange(context);
        }
        return returnCode;
    }

    public void init(JSONObject json, String downloaderPkgName, String downloaderAppName, String uid, String url) throws JSONException {
        this.mDownloaderPkgName = downloaderPkgName;
        this.mDownloaderAppName = downloaderAppName;
        if (json.has(CloudResult.AD_RESULT_OPT_POLICY)) {
            this.mOptPolicy = json.getInt(CloudResult.AD_RESULT_OPT_POLICY);
        }
        if (this.mOptPolicy == 0) {
            Record record = new Record();
            record.mOptPolicy = this.mOptPolicy;
            if (json.has("packageName")) {
                record.mPkgName = json.getString("packageName");
            }
            if (json.has(CloudResult.AD_RESULT_APPNAME)) {
                record.mApkName = json.getString(CloudResult.AD_RESULT_APPNAME);
            }
            DlBlockManager.getInstance().setTempRecord(Integer.parseInt(uid), url, record);
            return;
        }
        if (json.has(CloudResult.AD_RESULT_DETAILID)) {
            this.mDetailId = json.getString(CloudResult.AD_RESULT_DETAILID);
        }
        if (json.has("packageName")) {
            this.mApkPkgName = json.getString("packageName");
        }
        if (json.has("icon")) {
            this.mApkIcon = json.getString("icon");
        }
        if (json.has(CloudResult.AD_RESULT_APPNAME)) {
            this.mApkAppName = json.getString(CloudResult.AD_RESULT_APPNAME);
        }
        if (json.has("size")) {
            this.mApkSize = json.getLong("size");
        }
        if (json.has(CloudResult.AD_RESULT_TIPS)) {
            this.mTips = json.getString(CloudResult.AD_RESULT_TIPS);
        }
        if (json.has(CloudResult.AD_RESULT_OFFICIAL_BTN_TEXT)) {
            this.mOfficalBtnText = json.getString(CloudResult.AD_RESULT_OFFICIAL_BTN_TEXT);
        }
        if (json.has(CloudResult.AD_RESULT_CANCEL_BTN_TEXT)) {
            this.mCancelBtnText = json.getString(CloudResult.AD_RESULT_CANCEL_BTN_TEXT);
        }
        if (json.has(CloudResult.AD_RESULT_CONTINUE_BTN_TEXT)) {
            this.mContinueBtnText = json.getString(CloudResult.AD_RESULT_CONTINUE_BTN_TEXT);
        }
        this.mUidApkPkgName = uid + this.mApkPkgName;
        if (!isValid()) {
            throw new JSONException("result is not valid");
        }
    }

    public boolean isValid() {
        if (TextUtils.isEmpty(this.mApkPkgName) && TextUtils.isEmpty(this.mApkAppName)) {
            HwLog.w(TAG, "pkg and app name are empty");
            return false;
        } else if (this.mApkSize <= 0) {
            HwLog.w(TAG, "apk size is <= 0");
            return false;
        } else if (TextUtils.isEmpty(this.mOfficalBtnText)) {
            HwLog.w(TAG, "offical text is empty");
            return false;
        } else if (TextUtils.isEmpty(this.mCancelBtnText)) {
            HwLog.w(TAG, "cancel text is empty");
            return false;
        } else if (this.mOptPolicy == 1 || !TextUtils.isEmpty(this.mContinueBtnText)) {
            return true;
        } else {
            HwLog.w(TAG, "continue text is empty and policy is " + this.mOptPolicy);
            return false;
        }
    }

    public boolean isPackageInstalled(Context context) {
        boolean z = false;
        try {
            if (PackageManagerWrapper.getPackageInfo(context.getPackageManager(), this.mDownloaderPkgName, 8192) != null) {
                z = true;
            }
            return z;
        } catch (NameNotFoundException e) {
            HwLog.i(TAG, "isPackageExist package not found pkg=" + this.mDownloaderPkgName);
            return false;
        }
    }

    public void loadLabelAndIcon(Context context) {
        HsmPkgInfo hsmPkgInfo = null;
        try {
            hsmPkgInfo = HsmPackageManager.getInstance().getPkgInfo(this.mDownloaderPkgName, 8192);
        } catch (NameNotFoundException e) {
            try {
                PackageManager pm = context.getPackageManager();
                hsmPkgInfo = new HsmPkgInfo(PackageManagerWrapper.getPackageInfo(pm, this.mDownloaderPkgName, 8192), pm, true);
            } catch (NameNotFoundException e1) {
                HwLog.w(TAG, "loadLabelAndIcon NameNotFoundException", e1);
            }
        }
        if (hsmPkgInfo != null) {
            this.mDownloaderAppName = hsmPkgInfo.label();
            this.mDownloaderIcon = hsmPkgInfo.icon();
            return;
        }
        this.mDownloaderAppName = this.mDownloaderPkgName;
        this.mDownloaderIcon = HsmPackageManager.getInstance().getDefaultIcon();
    }

    public String getDownloaderPkgName() {
        return this.mDownloaderPkgName;
    }

    public long getSize() {
        return this.mApkSize;
    }

    public String getIcon() {
        return this.mApkIcon;
    }

    public String getTips() {
        return this.mTips;
    }

    public String getOfficalBtnText() {
        return this.mOfficalBtnText;
    }

    public String getCancelBtnText() {
        return this.mCancelBtnText;
    }

    public String getContinueBtnText() {
        return this.mContinueBtnText;
    }

    public int getOptPolicy() {
        return this.mOptPolicy;
    }

    public String getDetailId() {
        return this.mDetailId;
    }

    public void setOptPolicy(int mOptPolicy) {
        this.mOptPolicy = mOptPolicy;
    }

    public String getApkAppName() {
        if (TextUtils.isEmpty(this.mApkAppName)) {
            return this.mApkPkgName;
        }
        return this.mApkAppName;
    }

    public String getUidPkgName() {
        return this.mUidApkPkgName;
    }

    public Drawable getDownloaderIcon() {
        return this.mDownloaderIcon;
    }

    public String getDownloaderAppName() {
        return this.mDownloaderAppName;
    }

    public String getApkPkgName() {
        return this.mApkPkgName;
    }
}
