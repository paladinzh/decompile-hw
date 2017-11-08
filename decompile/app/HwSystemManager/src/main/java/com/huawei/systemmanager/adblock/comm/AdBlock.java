package com.huawei.systemmanager.adblock.comm;

import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.text.TextUtils;
import com.huawei.systemmanager.comm.misc.Closeables;
import com.huawei.systemmanager.optimize.smcs.SMCSDatabaseConstant.AdBlockColumns;
import com.huawei.systemmanager.util.HwLog;
import com.huawei.systemmanager.util.app.HsmPackageManager;
import com.huawei.systemmanager.util.app.HsmPkgInfo;
import com.huawei.systemmanager.util.app.PackageManagerWrapper;
import java.io.Closeable;
import java.io.Serializable;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;

public class AdBlock implements AdBlockColumns {
    public static final int CONTENT_DIRTY_COLUMN = 4;
    public static final int CONTENT_DL_CHECK_COLUMN = 7;
    public static final int CONTENT_ENABLE_COLUMN = 3;
    public static final int CONTENT_PKGNAME_COLUMN = 0;
    static final String[] CONTENT_PROJECTION = new String[]{"pkg_name", AdBlockColumns.COLUMN_VERSION_CODE, AdBlockColumns.COLUMN_VERSION_NAME, AdBlockColumns.COLUMN_ENABLE, AdBlockColumns.COLUMN_DIRTY, "views", AdBlockColumns.COLUMN_VIEW_IDS, AdBlockColumns.COLUMN_DL_CHECK, AdBlockColumns.COLUMN_URLS, AdBlockColumns.COLUMN_TX_URLS, AdBlockColumns.COLUMN_USER_TENCENT};
    public static final int CONTENT_TX_URLS_COLUMN = 9;
    public static final Uri CONTENT_URI = Uri.parse("content://smcs/ad_block_table");
    public static final int CONTENT_URLS_COLUMN = 8;
    public static final int CONTENT_USER_TENCENT = 10;
    public static final int CONTENT_VERSION_CODE_COLUMN = 1;
    public static final int CONTENT_VERSION_NAME_COLUMN = 2;
    public static final int CONTENT_VIEWS_COLUMN = 5;
    public static final int CONTENT_VIEW_IDS_COLUMN = 6;
    private static final String TAG = "AdBlock";
    private boolean mDirty = true;
    private boolean mDlCheck = false;
    private boolean mEnable = false;
    private Drawable mIcon;
    private String mLabel = "";
    public String mPkgName = "";
    private String mTxUrls = "";
    private String mUrls = "";
    private int mUseTencent = 0;
    private int mVersionCode;
    private String mVersionName = "";
    private String mViewIds = "";
    private String mViews = "";

    public static class Cmp implements Comparator<AdBlock>, Serializable {
        private static final long serialVersionUID = 1;

        public int compare(AdBlock lhs, AdBlock rhs) {
            if (lhs.mEnable == rhs.mEnable) {
                return Collator.getInstance().compare(lhs.mLabel, rhs.mLabel);
            }
            return lhs.mEnable ? -1 : 1;
        }
    }

    private AdBlock() {
    }

    public AdBlock(String pkgName, int versionCode, String versionName) {
        this.mPkgName = pkgName;
        this.mVersionCode = versionCode;
        this.mVersionName = versionName;
    }

    public ContentValues toContentValues() {
        return toContentValues(false);
    }

    public ContentValues toContentValues(boolean updateOnline) {
        int i;
        int i2 = 1;
        ContentValues values = new ContentValues();
        values.put("pkg_name", this.mPkgName);
        values.put(AdBlockColumns.COLUMN_VERSION_CODE, Integer.valueOf(this.mVersionCode));
        values.put(AdBlockColumns.COLUMN_VERSION_NAME, this.mVersionName);
        values.put(AdBlockColumns.COLUMN_DIRTY, Integer.valueOf(this.mDirty ? 1 : 0));
        values.put("views", this.mViews);
        values.put(AdBlockColumns.COLUMN_VIEW_IDS, this.mViewIds);
        String str = AdBlockColumns.COLUMN_DL_CHECK;
        if (this.mDlCheck) {
            i = 1;
        } else {
            i = 0;
        }
        values.put(str, Integer.valueOf(i));
        values.put(AdBlockColumns.COLUMN_URLS, this.mUrls);
        values.put(AdBlockColumns.COLUMN_USER_TENCENT, Integer.valueOf(this.mUseTencent));
        if (!updateOnline) {
            String str2 = AdBlockColumns.COLUMN_ENABLE;
            if (!this.mEnable) {
                i2 = 0;
            }
            values.put(str2, Integer.valueOf(i2));
            values.put(AdBlockColumns.COLUMN_TX_URLS, this.mTxUrls);
        }
        return values;
    }

    private void restore(Cursor cursor) {
        boolean z;
        boolean z2 = true;
        this.mPkgName = cursor.getString(0);
        this.mVersionCode = cursor.getInt(1);
        this.mVersionName = cursor.getString(2);
        if (cursor.getInt(3) == 1) {
            z = true;
        } else {
            z = false;
        }
        this.mEnable = z;
        if (cursor.getInt(4) == 1) {
            z = true;
        } else {
            z = false;
        }
        this.mDirty = z;
        this.mViews = cursor.getString(5);
        this.mViewIds = cursor.getString(6);
        if (cursor.getInt(7) != 1) {
            z2 = false;
        }
        this.mDlCheck = z2;
        this.mUrls = cursor.getString(8);
        this.mTxUrls = cursor.getString(9);
        this.mUseTencent = cursor.getInt(10);
    }

    public String getPkgName() {
        return this.mPkgName;
    }

    public int getVersionCode() {
        return this.mVersionCode;
    }

    public void setVersionCode(int versionCode) {
        this.mVersionCode = versionCode;
    }

    public String getVersionName() {
        return this.mVersionName;
    }

    public void setVersionName(String versionName) {
        this.mVersionName = versionName;
    }

    public void setUseTencent(int opt) {
        this.mUseTencent = opt;
    }

    public boolean isUseTencent() {
        return this.mUseTencent != 0;
    }

    public boolean isDirty() {
        return this.mDirty;
    }

    public boolean isEnable() {
        return this.mEnable;
    }

    public void setEnable(boolean enable) {
        this.mEnable = enable;
    }

    public void setDirty(boolean dirty) {
        this.mDirty = dirty;
    }

    public String getViews() {
        return this.mViews;
    }

    public List<String> getViewList() {
        return getListFromJson(this.mViews);
    }

    public void setViews(String views) {
        this.mViews = views;
    }

    public String getViewIds() {
        return this.mViewIds;
    }

    public List<String> getViewIdList() {
        return getListFromJson(this.mViewIds);
    }

    public void setViewIds(String viewIds) {
        this.mViewIds = viewIds;
    }

    public boolean isDlCheck() {
        return this.mDlCheck;
    }

    public void setDlCheck(boolean dlCheck) {
        this.mDlCheck = dlCheck;
    }

    public List<String> getAllUrlList() {
        List<String> list = new ArrayList();
        list.addAll(getListFromJson(this.mUrls));
        if (isUseTencent()) {
            list.addAll(getListFromJson(this.mTxUrls));
        }
        return list;
    }

    public List<String> getUrlList() {
        return getListFromJson(this.mUrls);
    }

    public String getUrls() {
        return this.mUrls;
    }

    public void setUrls(String urls) {
        this.mUrls = urls;
    }

    public String getTxUrls() {
        return this.mTxUrls;
    }

    public void setTxUrls(String txUrls) {
        this.mTxUrls = txUrls;
    }

    public boolean isValid() {
        return hasAd() || this.mDlCheck || isUseTencent() || !TextUtils.isEmpty(this.mTxUrls);
    }

    public boolean hasAd() {
        if (TextUtils.isEmpty(this.mViews) && TextUtils.isEmpty(this.mViewIds) && TextUtils.isEmpty(this.mUrls)) {
            return !TextUtils.isEmpty(this.mTxUrls) ? isUseTencent() : false;
        } else {
            return true;
        }
    }

    private static List<String> getListFromJson(String json) {
        List<String> list = new ArrayList();
        if (TextUtils.isEmpty(json)) {
            return list;
        }
        try {
            JSONArray array = new JSONArray(json);
            for (int i = 0; i < array.length(); i++) {
                list.add(array.getString(i));
            }
        } catch (JSONException e) {
            HwLog.w(TAG, "getListFromJson JSONException", e);
        }
        return list;
    }

    public boolean isPackageInstalled(Context context) {
        boolean z = false;
        try {
            if (PackageManagerWrapper.getPackageInfo(context.getPackageManager(), this.mPkgName, 8192) != null) {
                z = true;
            }
            return z;
        } catch (NameNotFoundException e) {
            HwLog.i(TAG, "isPackageExist package not found pkg=" + this.mPkgName);
            return false;
        }
    }

    public String getWhere() {
        return "pkg_name='" + this.mPkgName + "'";
    }

    public static AdBlock restoreAdBlockWithPkg(Context context, String pkg) {
        Closeable closeable = null;
        try {
            closeable = context.getContentResolver().query(CONTENT_URI, CONTENT_PROJECTION, "pkg_name=?", new String[]{pkg}, null);
            if (closeable == null || !closeable.moveToFirst()) {
                Closeables.close(closeable);
                return null;
            }
            AdBlock adBlock = new AdBlock();
            adBlock.restore(closeable);
            return adBlock;
        } catch (Exception e) {
            HwLog.w(TAG, "restoreAdBlockWithPkg Exception", e);
        } finally {
            Closeables.close(closeable);
        }
    }

    public void saveOrUpdate(Context context, boolean updateOnline) {
        if (update(context, toContentValues(updateOnline)) <= 0) {
            save(context);
        }
    }

    public Uri save(Context context) {
        HwLog.i(TAG, "save->insert pkg=" + this.mPkgName);
        return context.getContentResolver().insert(CONTENT_URI, toContentValues());
    }

    public int update(Context context, ContentValues contentValues) {
        HwLog.i(TAG, "update pkg=" + this.mPkgName);
        return context.getContentResolver().update(CONTENT_URI, contentValues, getWhere(), null);
    }

    public int delete(Context context) {
        HwLog.i(TAG, "delete pkg=" + this.mPkgName);
        return context.getContentResolver().delete(CONTENT_URI, getWhere(), null);
    }

    public static int deleteByPackages(Context context, List<String> packages) {
        if (packages == null || packages.isEmpty()) {
            return -1;
        }
        StringBuilder where = new StringBuilder();
        where.append("pkg_name").append(" in ('").append((String) packages.get(0));
        for (int i = 1; i < packages.size(); i++) {
            where.append("','").append((String) packages.get(i));
        }
        where.append("')");
        return context.getContentResolver().delete(CONTENT_URI, where.toString(), null);
    }

    public static List<AdBlock> getAllAdBlocks(Context context) {
        return getAdBlocks(context, null, null, null);
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static List<AdBlock> getAdBlocks(Context context, String selection, String[] selectionArgs, String sortOrder) {
        ArrayList<AdBlock> adBlockList = new ArrayList();
        try {
            Cursor cursor = context.getContentResolver().query(CONTENT_URI, CONTENT_PROJECTION, selection, selectionArgs, sortOrder);
            while (cursor != null && cursor.moveToNext()) {
                AdBlock adBlock = new AdBlock();
                adBlock.restore(cursor);
                adBlockList.add(adBlock);
            }
            Closeables.close(cursor);
        } catch (Exception e) {
            HwLog.w(TAG, "getAllAdBlocks Exception", e);
        } catch (Throwable th) {
            Closeables.close(null);
        }
        return adBlockList;
    }

    public static AdBlock getByPkgName(List<AdBlock> adBlocks, String pkgName) {
        for (AdBlock adBlock : adBlocks) {
            if (TextUtils.equals(pkgName, adBlock.getPkgName())) {
                return adBlock;
            }
        }
        return null;
    }

    public void loadLabelAndIcon(Context context) {
        HsmPkgInfo hsmPkgInfo = null;
        try {
            hsmPkgInfo = HsmPackageManager.getInstance().getPkgInfo(this.mPkgName, 8192);
        } catch (NameNotFoundException e) {
            try {
                PackageManager pm = context.getPackageManager();
                hsmPkgInfo = new HsmPkgInfo(PackageManagerWrapper.getPackageInfo(pm, this.mPkgName, 8192), pm, true);
            } catch (NameNotFoundException e1) {
                HwLog.w(TAG, "loadLabelAndIcon NameNotFoundException", e1);
            }
        }
        if (hsmPkgInfo != null) {
            this.mLabel = hsmPkgInfo.label();
            this.mIcon = hsmPkgInfo.icon();
            return;
        }
        this.mLabel = this.mPkgName;
        this.mIcon = HsmPackageManager.getInstance().getDefaultIcon();
    }

    public String getLabel() {
        return this.mLabel;
    }

    public Drawable getIcon() {
        return this.mIcon;
    }
}
