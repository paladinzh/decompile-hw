package com.huawei.systemmanager.rainbow.client.connect.request;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import com.google.android.collect.Maps;
import com.huawei.systemmanager.comm.misc.CursorHelper;
import com.huawei.systemmanager.rainbow.comm.base.CloudProviderUtils;
import com.huawei.systemmanager.rainbow.comm.request.AbsRequest;
import com.huawei.systemmanager.rainbow.comm.request.AbsRequestGroup;
import com.huawei.systemmanager.rainbow.comm.request.GroupRequestPolicy.FailRequestPolicy;
import com.huawei.systemmanager.rainbow.db.recommend.RecommendConst;
import com.huawei.systemmanager.rainbow.db.recommend.RecommendPkgVerView;
import com.huawei.systemmanager.rainbow.recommend.RecommendDataMgr;
import com.huawei.systemmanager.util.HwLog;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class RecommendMultiPkgRequest extends AbsRecommendRequest {
    private static final int BATCH_REQUEST_PKG_NUMBER = 40;
    private static final String TAG = "RecommendMultiPkgRequest";
    private Map<String, String> mPkgVerMap = Maps.newHashMap();

    public void addPkgAndVer(String pkgName, String version) {
        this.mPkgVerMap.put(pkgName, version);
    }

    public boolean dataFull() {
        return 40 == this.mPkgVerMap.size();
    }

    public boolean dataEmpty() {
        return this.mPkgVerMap.isEmpty();
    }

    protected Map<String, String> getRequestPkgVerMap(Context ctx) {
        return this.mPkgVerMap;
    }

    public static AbsRequest generateRequestGroup(Context ctx) {
        AbsRequestGroup recMultiGroup = new AbsRequestGroup(FailRequestPolicy.CONTINUE_WHEN_FAILED);
        RecommendMultiPkgRequest request = null;
        for (Entry<String, String> entry : retrievePkgVerMap(ctx).entrySet()) {
            if (request == null) {
                request = new RecommendMultiPkgRequest();
            }
            request.addPkgAndVer((String) entry.getKey(), (String) entry.getValue());
            if (request.dataFull()) {
                recMultiGroup.addRequest(request);
                request = null;
            }
        }
        if (!(request == null || request.dataEmpty())) {
            recMultiGroup.addRequest(request);
        }
        if (!recMultiGroup.isEmpty()) {
            return recMultiGroup;
        }
        HwLog.w(TAG, "generateRequestGroup empty request group!");
        return null;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static Map<String, String> retrievePkgVerMap(Context ctx) {
        List<String> pkgs = RecommendDataMgr.collectControlledAppList(ctx);
        Map<String, String> result = Maps.newHashMap();
        for (String pkg : pkgs) {
            result.put(pkg, "0");
        }
        try {
            Cursor cursor = ctx.getContentResolver().query(CloudProviderUtils.generateExtendViewUri(RecommendPkgVerView.VIEW_NAME), new String[]{"packageName", RecommendConst.RECOMMEND_FEATURE_PV_VIEW_COL_KEY}, null, null, null);
            if (cursor != null && cursor.getCount() > 0) {
                while (cursor.moveToNext()) {
                    String pkgName = cursor.getString(0);
                    String version = cursor.getString(1);
                    if (result.containsKey(pkgName)) {
                        result.put(pkgName, version);
                    }
                }
            }
            CursorHelper.closeCursor(cursor);
        } catch (SQLiteException ex) {
            HwLog.e(TAG, "retrievePkgVerMap catch SQLiteException: " + ex.getMessage());
        } catch (Exception ex2) {
            HwLog.e(TAG, "retrievePkgVerMap catch Exception: " + ex2.getMessage());
        } catch (Throwable th) {
            CursorHelper.closeCursor(null);
        }
        return result;
    }
}
