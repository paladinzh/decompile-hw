package com.huawei.systemmanager.rainbow.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import com.google.common.collect.Lists;
import com.huawei.systemmanager.comm.database.gfeature.AbsFeatureView;
import com.huawei.systemmanager.comm.database.gfeature.GFeatureDBOpenHelper;
import com.huawei.systemmanager.comm.misc.GlobalContext;
import com.huawei.systemmanager.rainbow.client.base.CloudSpfKeys;
import com.huawei.systemmanager.rainbow.client.helper.LocalSharedPrefrenceHelper;
import com.huawei.systemmanager.rainbow.db.origindata.OriginDataHelper;
import com.huawei.systemmanager.rainbow.db.recommend.RecommendHelper;
import java.util.List;

public class CloudDBHelper extends GFeatureDBOpenHelper {
    public static final int DATABASE_VERSION = 7;
    public static final String DB_NAME = "clouds_permission.db";
    private static CloudDBHelper sUniqueInstance = null;
    private static Object syncObj = new Object();
    private Context mContext = null;

    public CloudDBHelper(Context context) {
        super(context, DB_NAME, null, 7);
        this.mContext = context;
    }

    public static CloudDBHelper getInstance(Context context) {
        CloudDBHelper cloudDBHelper;
        synchronized (syncObj) {
            if (sUniqueInstance == null) {
                sUniqueInstance = new CloudDBHelper(context);
            }
            cloudDBHelper = sUniqueInstance;
        }
        return cloudDBHelper;
    }

    public void onCreate(SQLiteDatabase db) {
        checkApplicationContext();
        super.onCreate(db);
    }

    protected void concreteOnUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        new LocalSharedPrefrenceHelper(this.mContext).putBoolean(CloudSpfKeys.CLOUD_XML_DATA_INITED, false);
        uppgrade(db, oldVersion, newVersion);
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        OriginDataHelper.dropCloudTablesAndViewsWhenDowngrade(db);
        onCreate(db);
        new LocalSharedPrefrenceHelper(this.mContext).putBoolean(CloudSpfKeys.CLOUD_XML_DATA_INITED, false);
    }

    private void checkApplicationContext() {
        if (GlobalContext.getContext() == null) {
            GlobalContext.setContext(this.mContext);
        }
    }

    protected List<AbsFeatureView> getFeatureViews() {
        List<AbsFeatureView> list = Lists.newArrayList();
        list.addAll(OriginDataHelper.getOriginFeatureViews());
        list.addAll(RecommendHelper.getRecommendFeatureViews());
        return list;
    }

    protected void createConcreteTables(SQLiteDatabase db) {
        OriginDataHelper.createOriginConfigTables(db);
    }

    protected void dropConcreteTables(SQLiteDatabase db) {
        dropConcreteTablesInner(db);
    }

    protected void createConcreteViews(SQLiteDatabase db) {
        OriginDataHelper.createOriginConfigViews(db);
    }

    protected void dropConcreteViews(SQLiteDatabase db) {
        dropConcreteViewsInner(db);
    }

    private void uppgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < newVersion) {
            OriginDataHelper.dropCloudTablesAndViews(db);
            onCreate(db);
        }
    }

    private void dropConcreteTablesInner(SQLiteDatabase db) {
    }

    protected void dropConcreteViewsInner(SQLiteDatabase db) {
    }
}
