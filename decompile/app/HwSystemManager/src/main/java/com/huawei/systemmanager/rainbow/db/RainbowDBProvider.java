package com.huawei.systemmanager.rainbow.db;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.os.Bundle;
import com.huawei.systemmanager.rainbow.comm.base.CloudProviderConst.ProviderSubSegment;
import com.huawei.systemmanager.rainbow.comm.base.CloudProviderConst.RealFeatureCallMethod;
import com.huawei.systemmanager.rainbow.comm.base.CloudProviderConst.RecommendCallMethod;
import com.huawei.systemmanager.rainbow.comm.base.CloudProviderUtils;
import com.huawei.systemmanager.rainbow.db.assist.CommonTableViewAssist;
import com.huawei.systemmanager.rainbow.db.assist.DefaultLogicDbAssist;
import com.huawei.systemmanager.rainbow.db.assist.RealFeatureTableAssist;
import com.huawei.systemmanager.rainbow.db.base.CloudConst.AddViewValues;
import com.huawei.systemmanager.rainbow.db.base.CloudConst.BackgroundValues;
import com.huawei.systemmanager.rainbow.db.base.CloudConst.BootstartupValues;
import com.huawei.systemmanager.rainbow.db.base.CloudConst.CloudCommonValue;
import com.huawei.systemmanager.rainbow.db.base.CloudConst.CloudVagueValues;
import com.huawei.systemmanager.rainbow.db.base.CloudConst.CompetitorConfigFile;
import com.huawei.systemmanager.rainbow.db.base.CloudConst.ControlRangeBlackList;
import com.huawei.systemmanager.rainbow.db.base.CloudConst.ControlRangeWhiteList;
import com.huawei.systemmanager.rainbow.db.base.CloudConst.GetapplistValues;
import com.huawei.systemmanager.rainbow.db.base.CloudConst.MessageSafeConfigFile;
import com.huawei.systemmanager.rainbow.db.base.CloudConst.NetworkValues;
import com.huawei.systemmanager.rainbow.db.base.CloudConst.NotificationConfigFile;
import com.huawei.systemmanager.rainbow.db.base.CloudConst.NotificationExValues;
import com.huawei.systemmanager.rainbow.db.base.CloudConst.NotificationTip;
import com.huawei.systemmanager.rainbow.db.base.CloudConst.NotificationValues;
import com.huawei.systemmanager.rainbow.db.base.CloudConst.PermissionValues;
import com.huawei.systemmanager.rainbow.db.base.CloudConst.PushBlackList;
import com.huawei.systemmanager.rainbow.db.base.CloudConst.StartupConfigFile;
import com.huawei.systemmanager.rainbow.db.base.CloudConst.UnifiedPowerAppsConfigConfigFile;
import com.huawei.systemmanager.rainbow.db.recommend.RecommendCallAssist;
import com.huawei.systemmanager.util.HwLog;

public class RainbowDBProvider extends ContentProvider {
    private static final int APPLIST_REAL_TABLE_MAINTAIN_INDICATOR = 100;
    private static final int EXTEND_VIEW_MAINTAIN_INDICATOR = 103;
    private static final int GFEATURE_REAL_TABLE_MAINTAIN_INDICATOR = 101;
    private static final int NEW_CLOUD_INDICATOR_BASE = 100;
    private static final String TAG = "RainbowDBProvider";
    private static final int VIEWCOPY_REAL_TABLE_MAINTAIN_INDICATOR = 102;
    private static final UriMatcher mUriMatcher = new UriMatcher(-1);
    private CloudDBHelper mDatabaseHelper = null;

    public Bundle call(String method, String arg, Bundle extras) {
        super.call(method, arg, extras);
        if (RealFeatureCallMethod.CALL_METHOD_DELETE_GFEATURE.equals(method)) {
            return RealFeatureTableAssist.callDelete(this.mDatabaseHelper, arg, extras);
        }
        if (RecommendCallMethod.CALL_METHOD_QUERY_RECOMMEND.equals(method)) {
            return RecommendCallAssist.callQuery(this.mDatabaseHelper, arg, extras);
        }
        return null;
    }

    static {
        mUriMatcher.addURI("com.huawei.systemmanager.rainbow.rainbowprovider", NotificationTip.OUTERTABLE_NAME, 12);
        mUriMatcher.addURI("com.huawei.systemmanager.rainbow.rainbowprovider", "CloudPermission", 13);
        mUriMatcher.addURI("com.huawei.systemmanager.rainbow.rainbowprovider", "CloudVaguePermission", 14);
        mUriMatcher.addURI("com.huawei.systemmanager.rainbow.rainbowprovider", PermissionValues.PERMISSION_OUTER_VIEW_NAME, 15);
        mUriMatcher.addURI("com.huawei.systemmanager.rainbow.rainbowprovider", PermissionValues.PERMISSION_OUTER_TABLE_NAME, 16);
        mUriMatcher.addURI("com.huawei.systemmanager.rainbow.rainbowprovider", CloudVagueValues.PERMISSION_OUTER_VIEW_NAME, 17);
        mUriMatcher.addURI("com.huawei.systemmanager.rainbow.rainbowprovider", CloudCommonValue.BOOTSTARTUP_OUTERVIEW_NAME, 18);
        mUriMatcher.addURI("com.huawei.systemmanager.rainbow.rainbowprovider", BootstartupValues.OUTERTABLE_NAME, 19);
        mUriMatcher.addURI("com.huawei.systemmanager.rainbow.rainbowprovider", CloudCommonValue.ADDVIEW_OUTERVIEW_NAME, 20);
        mUriMatcher.addURI("com.huawei.systemmanager.rainbow.rainbowprovider", AddViewValues.OUTERTABLE_NAME, 21);
        mUriMatcher.addURI("com.huawei.systemmanager.rainbow.rainbowprovider", CloudCommonValue.SEND_NOTIFICATION_OUTERVIEW_NAME, 22);
        mUriMatcher.addURI("com.huawei.systemmanager.rainbow.rainbowprovider", NotificationValues.OUTERTABLE_NAME, 23);
        mUriMatcher.addURI("com.huawei.systemmanager.rainbow.rainbowprovider", CloudCommonValue.NOTIFICATION_SIGNAL_OUTERVIEW_NAME, 24);
        mUriMatcher.addURI("com.huawei.systemmanager.rainbow.rainbowprovider", NotificationExValues.OUTERTABLE_NAME, 25);
        mUriMatcher.addURI("com.huawei.systemmanager.rainbow.rainbowprovider", CloudCommonValue.GET_APPLIST_OUTERVIEW_NAME, 26);
        mUriMatcher.addURI("com.huawei.systemmanager.rainbow.rainbowprovider", GetapplistValues.OUTERTABLE_NAME, 27);
        mUriMatcher.addURI("com.huawei.systemmanager.rainbow.rainbowprovider", ControlRangeWhiteList.OUTERTABLE_NAME, 28);
        mUriMatcher.addURI("com.huawei.systemmanager.rainbow.rainbowprovider", ControlRangeBlackList.OUTERTABLE_NAME, 29);
        mUriMatcher.addURI("com.huawei.systemmanager.rainbow.rainbowprovider", BackgroundValues.OUTERTABLE_NAME, 30);
        mUriMatcher.addURI("com.huawei.systemmanager.rainbow.rainbowprovider", PushBlackList.OUTERTABLE_NAME, 31);
        mUriMatcher.addURI("com.huawei.systemmanager.rainbow.rainbowprovider", "phoneNumberTable", 32);
        mUriMatcher.addURI("com.huawei.systemmanager.rainbow.rainbowprovider", NetworkValues.OUTER_VIEW_NAME, 33);
        mUriMatcher.addURI("com.huawei.systemmanager.rainbow.rainbowprovider", NetworkValues.OUTER_TABLE_NAME, 34);
        mUriMatcher.addURI("com.huawei.systemmanager.rainbow.rainbowprovider", NotificationConfigFile.OUTERTABLE_NAME, 35);
        mUriMatcher.addURI("com.huawei.systemmanager.rainbow.rainbowprovider", StartupConfigFile.OUTERTABLE_NAME, 36);
        mUriMatcher.addURI("com.huawei.systemmanager.rainbow.rainbowprovider", UnifiedPowerAppsConfigConfigFile.OUTERTABLE_NAME, 37);
        mUriMatcher.addURI("com.huawei.systemmanager.rainbow.rainbowprovider", CompetitorConfigFile.OUTERTABLE_NAME, 38);
        mUriMatcher.addURI("com.huawei.systemmanager.rainbow.rainbowprovider", MessageSafeConfigFile.NUMBER_OUTERTABLE_NAME, 39);
        mUriMatcher.addURI("com.huawei.systemmanager.rainbow.rainbowprovider", MessageSafeConfigFile.LINK_OUTERTABLE_NAME, 40);
        mUriMatcher.addURI("com.huawei.systemmanager.rainbow.rainbowprovider", MessageSafeConfigFile.OUTERVIEW_NAME, 41);
        mUriMatcher.addURI("com.huawei.systemmanager.rainbow.rainbowprovider", CloudProviderUtils.wildcardSegment(ProviderSubSegment.APPLIST_PROVIDER_SEGMENT), 100);
        mUriMatcher.addURI("com.huawei.systemmanager.rainbow.rainbowprovider", CloudProviderUtils.wildcardSegment(ProviderSubSegment.GFEATURE_PROVIDER_SEGMENT), 101);
        mUriMatcher.addURI("com.huawei.systemmanager.rainbow.rainbowprovider", CloudProviderUtils.wildcardSegment(ProviderSubSegment.VIEWCOPY_PROVIDER_SEGMENT), 102);
        mUriMatcher.addURI("com.huawei.systemmanager.rainbow.rainbowprovider", CloudProviderUtils.wildcardSegment(ProviderSubSegment.EXTEND_VIEW_PROVIDER_SEGMENT), 103);
    }

    public String getType(Uri arg0) {
        return null;
    }

    public boolean onCreate() {
        this.mDatabaseHelper = CloudDBHelper.getInstance(getContext());
        try {
            return this.mDatabaseHelper.getWritableDatabase() != null;
        } catch (SQLiteException e) {
            HwLog.e(TAG, "Fail to get database: " + e.getMessage());
            return false;
        }
    }

    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int effectCount;
        int code = mUriMatcher.match(uri);
        switch (code) {
            case 100:
            case 102:
                effectCount = CommonTableViewAssist.delete(this.mDatabaseHelper, uri, selection, selectionArgs);
                break;
            default:
                effectCount = DefaultLogicDbAssist.defaultDelete(this.mDatabaseHelper, code, selection, selectionArgs);
                break;
        }
        if (effectCount > 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return effectCount;
    }

    public Uri insert(Uri uri, ContentValues values) {
        long rowId;
        int code = mUriMatcher.match(uri);
        switch (code) {
            case 100:
            case 102:
                rowId = CommonTableViewAssist.insert(this.mDatabaseHelper, uri, values);
                break;
            default:
                rowId = DefaultLogicDbAssist.defaultInsert(this.mDatabaseHelper, code, values);
                break;
        }
        if (0 < rowId) {
            Uri retUri = Uri.withAppendedPath(uri, String.valueOf(rowId));
            getContext().getContentResolver().notifyChange(retUri, null);
            return retUri;
        }
        HwLog.e(TAG, "insert failed, uri: " + uri);
        return null;
    }

    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        int code = mUriMatcher.match(uri);
        switch (code) {
            case 100:
            case 102:
            case 103:
                return CommonTableViewAssist.query(this.mDatabaseHelper, uri, projection, selection, selectionArgs);
            case 101:
                return RealFeatureTableAssist.query(this.mDatabaseHelper, uri, projection, selection, selectionArgs);
            default:
                return DefaultLogicDbAssist.defaultQuery(this.mDatabaseHelper, code, projection, selection, selectionArgs, sortOrder);
        }
    }

    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        int effectCount = DefaultLogicDbAssist.defaultUpdate(this.mDatabaseHelper, mUriMatcher.match(uri), values, selection, selectionArgs);
        if (effectCount > 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return effectCount;
    }

    public int bulkInsert(Uri uri, ContentValues[] values) {
        int effectCount;
        int code = mUriMatcher.match(uri);
        switch (code) {
            case 30:
            case 35:
            case 36:
            case 37:
            case 38:
                effectCount = DefaultLogicDbAssist.defaultBulkReplace(this.mDatabaseHelper, code, values);
                break;
            case 100:
            case 102:
                effectCount = CommonTableViewAssist.bulkInsert(this.mDatabaseHelper, uri, values);
                break;
            case 101:
                effectCount = RealFeatureTableAssist.bulkInsert(this.mDatabaseHelper, uri, values);
                break;
            default:
                effectCount = DefaultLogicDbAssist.defaultBulkInsert(this.mDatabaseHelper, code, values);
                break;
        }
        if (effectCount > 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return effectCount;
    }
}
