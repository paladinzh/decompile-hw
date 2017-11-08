package com.huawei.systemmanager.rainbow.db.recommend;

import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import com.google.android.collect.Maps;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.huawei.harassmentinterception.common.ConstValues;
import com.huawei.systemmanager.comm.misc.CursorHelper;
import com.huawei.systemmanager.rainbow.comm.meta.CloudMetaMgr;
import com.huawei.systemmanager.rainbow.db.CloudDBHelper;
import com.huawei.systemmanager.rainbow.vaguerule.VagueRegConst;
import com.huawei.systemmanager.util.HwLog;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

abstract class AbsRecommendQuery {
    private static final String TAG = AbsRecommendQuery.class.getSimpleName();
    private List<Integer> mIdList = null;
    private List<String> mPkgRangeList = null;

    protected abstract List<Integer> getItemIdList(Bundle bundle);

    protected abstract List<String> getRangeOfPackages(Bundle bundle);

    AbsRecommendQuery() {
    }

    Bundle queryRecommendData(CloudDBHelper helper, Bundle bundle) {
        Bundle toBundle;
        String viewName = CloudMetaMgr.getBusinessInstance(RecommendQueryInput.extractQueryBusinessId(bundle)).getRecommendViewName();
        this.mIdList = getItemIdList(bundle);
        this.mPkgRangeList = getRangeOfPackages(bundle);
        Cursor cursor = null;
        try {
            cursor = helper.queryComm(viewName, getProjections(), getSelection(), getSelectionArgs());
            if (cursor == null || cursor.getCount() == 0) {
                HwLog.w(TAG, "queryRecommendData get a invalid cursor, maybe no matched result exist!");
                CursorHelper.closeCursor(cursor);
                return null;
            }
            HwLog.d(TAG, "queryRecommendData dumpCursor:" + DatabaseUtils.dumpCursorToString(cursor));
            toBundle = RecommendQueryOutput.toBundle(cursorResult(cursor));
            return toBundle;
        } catch (SQLiteException ex) {
            toBundle = TAG;
            HwLog.e(toBundle, "queryRecommendData SQLiteException: " + ex.getMessage());
            ex.printStackTrace();
        } catch (Exception ex2) {
            toBundle = TAG;
            HwLog.e(toBundle, "queryRecommendData Exception: " + ex2.getMessage());
            ex2.printStackTrace();
        } finally {
            CursorHelper.closeCursor(cursor);
        }
    }

    private String[] getProjections() {
        if (this.mIdList == null || this.mIdList.isEmpty()) {
            return new String[0];
        }
        List<String> projList = Lists.newArrayList("packageName");
        for (Integer id : this.mIdList) {
            projList.add(CloudMetaMgr.getItemInstance(id.intValue()).getColumnlName());
        }
        return (String[]) projList.toArray(new String[projList.size()]);
    }

    private String getSelection() {
        if (this.mPkgRangeList == null || this.mPkgRangeList.isEmpty()) {
            return null;
        }
        StringBuffer buf = new StringBuffer();
        buf.append("packageName").append(" in (");
        Iterable questionMarkList = Lists.newArrayList();
        for (int i = 0; i < this.mPkgRangeList.size(); i++) {
            questionMarkList.add(VagueRegConst.REG_ONE_CHAR);
        }
        buf.append(Joiner.on(ConstValues.SEPARATOR_KEYWORDS_EN).join(questionMarkList));
        buf.append(")");
        return buf.toString();
    }

    private String[] getSelectionArgs() {
        if (this.mPkgRangeList == null || this.mPkgRangeList.isEmpty()) {
            return new String[0];
        }
        return (String[]) this.mPkgRangeList.toArray(new String[this.mPkgRangeList.size()]);
    }

    private Map<String, List<RecommendItem>> cursorResult(Cursor cursor) {
        Map<String, List<RecommendItem>> result = Maps.newHashMap();
        Map<Integer, Integer> idIndexMap = Maps.newHashMap();
        int pkgColIndex = -1;
        for (String colName : cursor.getColumnNames()) {
            if (colName.equals("packageName")) {
                pkgColIndex = cursor.getColumnIndex(colName);
            } else {
                int id = CloudMetaMgr.getItemId(colName);
                if (id != -1) {
                    idIndexMap.put(Integer.valueOf(id), Integer.valueOf(cursor.getColumnIndex(colName)));
                }
            }
        }
        if (-1 == pkgColIndex || idIndexMap.isEmpty()) {
            return null;
        }
        while (cursor.moveToNext()) {
            String pkgName = cursor.getString(pkgColIndex);
            if (result.containsKey(pkgName)) {
                ((List) result.get(pkgName)).addAll(cursorToItems(cursor, idIndexMap));
            } else {
                result.put(pkgName, cursorToItems(cursor, idIndexMap));
            }
        }
        return result;
    }

    private List<RecommendItem> cursorToItems(Cursor cursor, Map<Integer, Integer> idIndexMap) {
        List<RecommendItem> result = Lists.newArrayList();
        for (Entry<Integer, Integer> entry : idIndexMap.entrySet()) {
            String dbValue = cursor.getString(((Integer) entry.getValue()).intValue());
            if (dbValue != null) {
                try {
                    result.add(new RecommendItem(((Integer) entry.getKey()).intValue(), RecommendCvtUtils.cvtStoreValueToConfigType(dbValue), RecommendCvtUtils.cvtStoreValueToPercentage(dbValue)));
                } catch (RecommendParamException ex) {
                    HwLog.e(TAG, "cursorToItems catch RecommendParamException: " + ex.getMessage());
                } catch (Exception ex2) {
                    HwLog.e(TAG, "cursorToItems catch Exception: " + ex2.getMessage());
                }
            }
        }
        return result;
    }

    public static AbsRecommendQuery newInstance(int queryType) {
        switch (queryType) {
            case 1:
                return new MultiPkgOneItemQuery();
            case 2:
                return new OnePkgMultiItemQuery();
            case 3:
                return new AllPkgAndItemQuery();
            default:
                HwLog.e(TAG, "newInstance should not be here! type: " + queryType);
                return null;
        }
    }
}
