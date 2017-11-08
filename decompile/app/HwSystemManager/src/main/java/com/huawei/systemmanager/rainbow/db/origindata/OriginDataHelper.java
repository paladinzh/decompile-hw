package com.huawei.systemmanager.rainbow.db.origindata;

import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import com.google.common.collect.Lists;
import com.huawei.systemmanager.comm.database.DbOpWrapper;
import com.huawei.systemmanager.comm.database.gfeature.AbsFeatureView;
import com.huawei.systemmanager.rainbow.db.base.CloudConst.AddViewValues;
import com.huawei.systemmanager.rainbow.db.base.CloudConst.BackgroundValues;
import com.huawei.systemmanager.rainbow.db.base.CloudConst.BackstartupValues;
import com.huawei.systemmanager.rainbow.db.base.CloudConst.BootstartupValues;
import com.huawei.systemmanager.rainbow.db.base.CloudConst.CloudVagueValues;
import com.huawei.systemmanager.rainbow.db.base.CloudConst.CloudValues;
import com.huawei.systemmanager.rainbow.db.base.CloudConst.CompetitorConfigFile;
import com.huawei.systemmanager.rainbow.db.base.CloudConst.DefaultConfigureValue;
import com.huawei.systemmanager.rainbow.db.base.CloudConst.GetapplistValues;
import com.huawei.systemmanager.rainbow.db.base.CloudConst.MessageSafeConfigFile;
import com.huawei.systemmanager.rainbow.db.base.CloudConst.NetworkValues;
import com.huawei.systemmanager.rainbow.db.base.CloudConst.NotificationConfigFile;
import com.huawei.systemmanager.rainbow.db.base.CloudConst.NotificationValues;
import com.huawei.systemmanager.rainbow.db.base.CloudConst.PermissionValues;
import com.huawei.systemmanager.rainbow.db.base.CloudConst.SecurityBlackList;
import com.huawei.systemmanager.rainbow.db.base.CloudConst.StartupConfigFile;
import com.huawei.systemmanager.rainbow.db.base.CloudConst.UnifiedPowerAppsConfigConfigFile;
import com.huawei.systemmanager.rainbow.db.featureview.CreateOuterViewHelper;
import com.huawei.systemmanager.rainbow.db.featureview.PermissionFeatureView;
import com.huawei.systemmanager.rainbow.db.featureview.VaguePermissionFeatureView;
import java.util.List;

public class OriginDataHelper {
    private static final String ANDROID_METADATA_TABLE = "android_metadata";
    private static final String NAME_COLUMN_NAME = "name";
    private static final String SQL_CREATE_MESSAGE_SAFE_LINK_TABLE = "create table if not exists messageSafeLinkConfigTable ( partner text primary key, secureLink text );";
    private static final String SQL_CREATE_MESSAGE_SAFE_NUMBER_TABLE = "create table if not exists messageSafeNumberConfigTable ( messageNo text primary key, partner text );";
    private static final String SQL_CREATE_MESSAGE_SAFE_VIEW = "CREATE VIEW IF NOT EXISTS vMessageSafe as select n.messageNo, l.secureLink from messageSafeNumberConfigTable n, messageSafeLinkConfigTable l WHERE n.partner = l.partner";
    private static final String TABLE_NAME = "table";
    private static final String TYPE_COLUMN_NAME = "type";
    private static final String VIEW_NAME = "view";

    public static List<AbsFeatureView> getOriginFeatureViews() {
        List<AbsFeatureView> list = Lists.newArrayList();
        list.add(new PermissionFeatureView());
        list.add(new VaguePermissionFeatureView());
        return list;
    }

    public static void createOriginConfigTables(SQLiteDatabase db) {
        db.execSQL("create table if not exists PermissionOuterTable ( packageName text primary key, permissionCode int  DEFAULT (0), permissionCfg int  DEFAULT (0), trust text );");
        db.execSQL("create unique index if not exists permission_outer_index on PermissionOuterTable ( packageName )");
        db.execSQL("create table if not exists notificationOuterTable ( packageName text primary key, permissionCfg int  DEFAULT (2) );");
        db.execSQL("create unique index if not exists notification_outer_index on notificationOuterTable ( packageName )");
        db.execSQL("create table if not exists notificationExOuterTable ( packageName text primary key, permissionCfg int  DEFAULT (2) );");
        db.execSQL("create unique index if not exists notificationEx_outer_index on notificationExOuterTable ( packageName )");
        db.execSQL("create table if not exists getapplistOuterTable ( packageName text primary key, permissionCfg int  DEFAULT (2) );");
        db.execSQL("create unique index if not exists getapplist_outer_index on getapplistOuterTable ( packageName )");
        db.execSQL("create table if not exists addviewOuterTable ( packageName text primary key, permissionCfg int  DEFAULT (1) );");
        db.execSQL("create unique index if not exists addview_outer_index on addviewOuterTable ( packageName )");
        db.execSQL("create table if not exists bootstartupOuterTable ( packageName text primary key, permissionCfg int  DEFAULT (1) );");
        db.execSQL("create unique index if not exists bootstartup_outer_index on bootstartupOuterTable ( packageName )");
        db.execSQL("create table if not exists controlRangeBlackTable ( packageName text primary key );");
        db.execSQL("create unique index if not exists rangeblack_outer_index on controlRangeBlackTable ( packageName )");
        db.execSQL("create table if not exists controlRangeWhiteTable ( packageName text primary key );");
        db.execSQL("create unique index if not exists rangewhite_outer_index on controlRangeWhiteTable ( packageName )");
        db.execSQL("create table if not exists backgroundTable ( packageName text primary key, isControlled text, isProtected text, isKeyTask text );");
        db.execSQL("create unique index if not exists background_outer_index on backgroundTable ( packageName )");
        db.execSQL("create table if not exists pushBlackTable ( packageName text primary key );");
        db.execSQL("create unique index if not exists push_outer_index on pushBlackTable ( packageName )");
        db.execSQL("create table if not exists phoneNumberTable ( packageName text primary key );");
        db.execSQL("create unique index if not exists phoneNumber_outer_index on phoneNumberTable ( packageName )");
        db.execSQL("create table if not exists notificationTipTable ( packageName text primary key, notificationTipStatus int  DEFAULT (0) );");
        db.execSQL("create unique index if not exists recommand_outer_index on notificationTipTable ( packageName )");
        db.execSQL("create table if not exists networkOuterTable ( packageName text primary key, netDataPermission text, netWifiPermission text );");
        db.execSQL("create unique index if not exists network_outer_index on networkOuterTable ( packageName )");
        db.execSQL("create table if not exists notificationConfigTable ( packageName text primary key, isControlled text, notificationCfg text, statusbarCfg text, lockscreenCfg text, headsubCfg text, canForbidden text );");
        db.execSQL("create unique index if not exists notification_outer_index on notificationConfigTable ( packageName )");
        db.execSQL("create table if not exists unifiedPowerAppsConfigTable ( packageName text primary key, isShow text, isProtected text );");
        db.execSQL("create unique index if not exists notification_outer_index on unifiedPowerAppsConfigTable ( packageName )");
        db.execSQL("create table if not exists startupConfigTable ( packageName text primary key, isControlled text, receiver text, serviceProvider text );");
        db.execSQL("create unique index if not exists notification_outer_index on startupConfigTable ( packageName )");
        db.execSQL("create table if not exists competitorConfigTable ( packageName text primary key );");
        db.execSQL("create unique index if not exists notification_outer_index on competitorConfigTable ( packageName )");
        db.execSQL(SQL_CREATE_MESSAGE_SAFE_LINK_TABLE);
        db.execSQL(SQL_CREATE_MESSAGE_SAFE_NUMBER_TABLE);
    }

    public static void createOriginConfigViews(SQLiteDatabase db) {
        CreateOuterViewHelper.getInstance().genPermissionView(db, PermissionValues.PERMISSION_OUTER_VIEW_NAME, PermissionValues.PERMISSION_INNER_VIEW_NAME);
        CreateOuterViewHelper.getInstance().genVaguePermissionView(db, CloudVagueValues.PERMISSION_OUTER_VIEW_NAME, CloudVagueValues.PERMISSION_INNER_VIEW_NAME);
        CreateOuterViewHelper.getInstance().genCommonFeatureView(db, PermissionValues.PERMISSION_INNER_VIEW_NAME);
        CreateOuterViewHelper.getInstance().genNetworkFeatureView(db, NetworkValues.OUTER_VIEW_NAME, PermissionValues.PERMISSION_INNER_VIEW_NAME);
        db.execSQL(SQL_CREATE_MESSAGE_SAFE_VIEW);
    }

    public static void dropCloudTablesAndViews(SQLiteDatabase db) {
        DbOpWrapper.dropView(db, GetapplistValues.VIEW_NAME);
        DbOpWrapper.dropView(db, NotificationValues.VIEW_NAME);
        DbOpWrapper.dropView(db, NetworkValues.VIEW_NAME);
        DbOpWrapper.dropView(db, PermissionValues.VIEW_NAME);
        DbOpWrapper.dropTable(db, AddViewValues.TABLE_NAME);
        DbOpWrapper.dropTable(db, BackgroundValues.OUTERTABLE_NAME);
        DbOpWrapper.dropTable(db, BackstartupValues.TABLE_NAME);
        DbOpWrapper.dropTable(db, BootstartupValues.TABLE_NAME);
        DbOpWrapper.dropTable(db, CloudValues.CLOUD_SETTINGS_TABLE);
        DbOpWrapper.dropTable(db, DefaultConfigureValue.TABLE_NAME);
        DbOpWrapper.dropTable(db, SecurityBlackList.TABLE_NAME);
        DbOpWrapper.dropTable(db, NotificationConfigFile.OUTERTABLE_NAME);
        DbOpWrapper.dropTable(db, UnifiedPowerAppsConfigConfigFile.OUTERTABLE_NAME);
        DbOpWrapper.dropTable(db, StartupConfigFile.OUTERTABLE_NAME);
        DbOpWrapper.dropTable(db, CompetitorConfigFile.OUTERTABLE_NAME);
        DbOpWrapper.dropTable(db, MessageSafeConfigFile.LINK_OUTERTABLE_NAME);
        DbOpWrapper.dropTable(db, MessageSafeConfigFile.NUMBER_OUTERTABLE_NAME);
    }

    public static void dropCloudTablesAndViewsWhenDowngrade(SQLiteDatabase db) {
        try {
            getSqliteMasterViewsAndDelete(db);
            getSqliteMasterTablesAndDelete(db);
        } catch (SQLException ex) {
            ex.printStackTrace();
        } catch (Exception ex2) {
            ex2.printStackTrace();
        }
    }

    public static void getSqliteMasterViewsAndDelete(SQLiteDatabase db) {
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("SELECT * FROM sqlite_master WHERE type= \"view\" ", null);
        } catch (SQLException ex) {
            ex.printStackTrace();
        } catch (Exception ex2) {
            ex2.printStackTrace();
        }
        if (cursor != null) {
            if (cursor.getCount() > 0) {
                int nameIndex = cursor.getColumnIndex("name");
                while (cursor.moveToNext()) {
                    dropView(db, cursor.getString(nameIndex));
                }
            }
            cursor.close();
        }
    }

    public static void getSqliteMasterTablesAndDelete(SQLiteDatabase db) {
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("SELECT * FROM sqlite_master WHERE type= \"table\" ", null);
        } catch (SQLException ex) {
            ex.printStackTrace();
        } catch (Exception ex2) {
            ex2.printStackTrace();
        }
        if (cursor != null) {
            if (cursor.getCount() > 0) {
                int nameIndex = cursor.getColumnIndex("name");
                while (cursor.moveToNext()) {
                    String tableName = cursor.getString(nameIndex);
                    if (!ANDROID_METADATA_TABLE.equals(tableName)) {
                        dropTable(db, tableName);
                    }
                }
            }
            cursor.close();
        }
    }

    private static void dropTable(SQLiteDatabase db, String tableName) {
        StringBuffer strBuf = new StringBuffer();
        strBuf.append("DROP TABLE IF EXISTS ").append(tableName);
        runSingleSqlSentence(db, strBuf.toString());
    }

    private static void dropView(SQLiteDatabase db, String viewName) {
        StringBuffer strBuf = new StringBuffer();
        strBuf.append("DROP VIEW IF EXISTS ").append(viewName);
        runSingleSqlSentence(db, strBuf.toString());
    }

    private static void runSingleSqlSentence(SQLiteDatabase db, String sql) {
        try {
            db.execSQL(sql);
        } catch (SQLException ex) {
            ex.printStackTrace();
        } catch (Exception ex2) {
            ex2.printStackTrace();
        }
    }
}
