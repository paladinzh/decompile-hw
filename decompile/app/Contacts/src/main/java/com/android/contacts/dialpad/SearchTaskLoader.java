package com.android.contacts.dialpad;

import android.content.Context;
import android.content.CursorLoader;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.MergeCursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.net.Uri.Builder;
import android.provider.CallLog.Calls;
import android.provider.ContactsContract.DisplayPhoto;
import android.text.TextUtils;
import android.widget.CursorAdapter;
import com.android.common.content.ProjectionMap;
import com.android.contacts.ContactsUtils;
import com.android.contacts.compatibility.DirectoryCompat;
import com.android.contacts.compatibility.ExtendedSubscriptionCursor;
import com.android.contacts.compatibility.QueryUtil;
import com.android.contacts.hap.CommonUtilMethods;
import com.android.contacts.hap.EmuiFeatureManager;
import com.android.contacts.hap.hwsearch.HwSearchCursor.HwSearchDialerCursor;
import com.android.contacts.hap.provider.ContactsAppDatabaseHelper;
import com.android.contacts.hap.provider.ContactsAppProvider;
import com.android.contacts.hap.util.MultiUsersUtils;
import com.android.contacts.util.HwLog;
import com.huawei.cspcommon.performance.PLog;
import com.huawei.cspcommon.util.SearchContract$DataSearch;
import com.huawei.cspcommon.util.SearchContract$YellowPageSearch;
import com.huawei.cspcommon.util.SmartDialType;
import java.util.ArrayList;

public class SearchTaskLoader extends CursorLoader {
    static final String[] _projection = new String[]{"_id", "number", "date", "duration", "type", "countryiso", "voicemail_uri", "geocoded_location", "name", "formatted_number", "lookup_uri", "presentation", "normalized_number", "features"};
    static String[] mMatixCallLogProjection;
    private static final String[] sSmartdialYellowPageProjection = new String[]{"_ID", "name", "number", "photouri", "photo", "NULL AS sort_key", "dial_map", "NULL AS data2", "NULL AS data3", "NULL AS data4", "'" + ContactsAppProvider.YELLOW_PAGE_URI + "/'||" + "ypid" + " AS " + "contact_id", "NULL AS dialer_map", "NULL AS times_contacted"};
    private static final ProjectionMap sSmartdialYellowPageProjectionMap = ProjectionMap.builder().addAll(sYellowPageProjectionMap).add("photo").add("sort_key", "NULL").add("dial_map").add("data2", "NULL").add("data3", "NULL").add("data4", "NULL").add("contact_id", "'" + ContactsAppProvider.YELLOW_PAGE_URI + "/'||" + "ypid").add("dialer_map", "NULL").add("times_contacted", "NULL").build();
    private static final ProjectionMap sYellowPageProjectionMap = ProjectionMap.builder().add("_ID").add("name").add("number").add("photo_uri", "'" + DisplayPhoto.CONTENT_URI + "/'||" + "photo").build();
    static String[] sub_projection;
    private String filterString = null;

    private static final class DirectoryQuery {
        public static final String[] PROJECTION = new String[]{"_id", "displayName"};
        public static final Uri URI = DirectoryCompat.getContentUri();

        private DirectoryQuery() {
        }
    }

    private android.database.Cursor queryEnterpriseContacts(int r15) {
        /* JADX: method processing error */
/*
Error: jadx.core.utils.exceptions.JadxRuntimeException: Can't find block by offset: 0x00af in list []
	at jadx.core.utils.BlockUtils.getBlockByOffset(BlockUtils.java:43)
	at jadx.core.dex.instructions.IfNode.initBlocks(IfNode.java:60)
	at jadx.core.dex.visitors.blocksmaker.BlockFinish.initBlocksInIfNodes(BlockFinish.java:48)
	at jadx.core.dex.visitors.blocksmaker.BlockFinish.visit(BlockFinish.java:33)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:306)
	at jadx.api.JavaClass.decompile(JavaClass.java:62)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:199)
*/
        /*
        r14 = this;
        r1 = r14.getContext();
        r0 = r1.getContentResolver();
        r6 = 0;
        r12 = 0;
        r1 = com.android.contacts.dialpad.SearchTaskLoader.DirectoryQuery.URI;	 Catch:{ RuntimeException -> 0x009f, all -> 0x00b0 }
        r2 = com.android.contacts.dialpad.SearchTaskLoader.DirectoryQuery.PROJECTION;	 Catch:{ RuntimeException -> 0x009f, all -> 0x00b0 }
        r5 = "_id";	 Catch:{ RuntimeException -> 0x009f, all -> 0x00b0 }
        r3 = 0;	 Catch:{ RuntimeException -> 0x009f, all -> 0x00b0 }
        r4 = 0;	 Catch:{ RuntimeException -> 0x009f, all -> 0x00b0 }
        r6 = r0.query(r1, r2, r3, r4, r5);	 Catch:{ RuntimeException -> 0x009f, all -> 0x00b0 }
        if (r6 != 0) goto L_0x0020;
    L_0x0019:
        r1 = 0;
        if (r6 == 0) goto L_0x001f;
    L_0x001c:
        r6.close();
    L_0x001f:
        return r1;
    L_0x0020:
        r10 = -1;
    L_0x0022:
        r1 = r6.moveToNext();	 Catch:{ RuntimeException -> 0x009f, all -> 0x00b0 }
        if (r1 == 0) goto L_0x0034;	 Catch:{ RuntimeException -> 0x009f, all -> 0x00b0 }
    L_0x0028:
        r1 = 0;	 Catch:{ RuntimeException -> 0x009f, all -> 0x00b0 }
        r8 = r6.getLong(r1);	 Catch:{ RuntimeException -> 0x009f, all -> 0x00b0 }
        r1 = com.android.contacts.compatibility.DirectoryCompat.isEnterpriseDirectoryId(r8);	 Catch:{ RuntimeException -> 0x009f, all -> 0x00b0 }
        if (r1 == 0) goto L_0x0022;
    L_0x0033:
        r10 = r8;
    L_0x0034:
        r2 = -1;
        r1 = (r10 > r2 ? 1 : (r10 == r2 ? 0 : -1));
        if (r1 != 0) goto L_0x0041;
    L_0x003a:
        r1 = 0;
        if (r6 == 0) goto L_0x0040;
    L_0x003d:
        r6.close();
    L_0x0040:
        return r1;
    L_0x0041:
        r1 = com.android.contacts.compatibility.PhoneCompat.getContentFilterUri();	 Catch:{ RuntimeException -> 0x009f, all -> 0x00b0 }
        r2 = r14.filterString;	 Catch:{ RuntimeException -> 0x009f, all -> 0x00b0 }
        r2 = android.net.Uri.encode(r2);	 Catch:{ RuntimeException -> 0x009f, all -> 0x00b0 }
        r1 = android.net.Uri.withAppendedPath(r1, r2);	 Catch:{ RuntimeException -> 0x009f, all -> 0x00b0 }
        r13 = r1.buildUpon();	 Catch:{ RuntimeException -> 0x009f, all -> 0x00b0 }
        r1 = "search_type";	 Catch:{ RuntimeException -> 0x009f, all -> 0x00b0 }
        r2 = "search_dialer";	 Catch:{ RuntimeException -> 0x009f, all -> 0x00b0 }
        r13.appendQueryParameter(r1, r2);	 Catch:{ RuntimeException -> 0x009f, all -> 0x00b0 }
        r1 = "directory";	 Catch:{ RuntimeException -> 0x009f, all -> 0x00b0 }
        r2 = java.lang.String.valueOf(r10);	 Catch:{ RuntimeException -> 0x009f, all -> 0x00b0 }
        r13.appendQueryParameter(r1, r2);	 Catch:{ RuntimeException -> 0x009f, all -> 0x00b0 }
        r1 = com.android.contacts.hap.EmuiFeatureManager.isSupportRussiaNumberRelevance();	 Catch:{ RuntimeException -> 0x009f, all -> 0x00b0 }
        if (r1 == 0) goto L_0x007e;	 Catch:{ RuntimeException -> 0x009f, all -> 0x00b0 }
    L_0x006c:
        r1 = "search_number";	 Catch:{ RuntimeException -> 0x009f, all -> 0x00b0 }
        r2 = com.android.contacts.hap.EmuiFeatureManager.isRussiaNumberSearchEnabled();	 Catch:{ RuntimeException -> 0x009f, all -> 0x00b0 }
        r2 = java.lang.String.valueOf(r2);	 Catch:{ RuntimeException -> 0x009f, all -> 0x00b0 }
        r1 = r13.appendQueryParameter(r1, r2);	 Catch:{ RuntimeException -> 0x009f, all -> 0x00b0 }
        r1.build();	 Catch:{ RuntimeException -> 0x009f, all -> 0x00b0 }
    L_0x007e:
        r1 = "limit";	 Catch:{ RuntimeException -> 0x009f, all -> 0x00b0 }
        r2 = java.lang.String.valueOf(r15);	 Catch:{ RuntimeException -> 0x009f, all -> 0x00b0 }
        r13.appendQueryParameter(r1, r2);	 Catch:{ RuntimeException -> 0x009f, all -> 0x00b0 }
        r1 = r13.build();	 Catch:{ RuntimeException -> 0x009f, all -> 0x00b0 }
        r2 = com.huawei.cspcommon.util.SmartDialType.getProjection();	 Catch:{ RuntimeException -> 0x009f, all -> 0x00b0 }
        r5 = "pinyin_name";	 Catch:{ RuntimeException -> 0x009f, all -> 0x00b0 }
        r3 = 0;	 Catch:{ RuntimeException -> 0x009f, all -> 0x00b0 }
        r4 = 0;	 Catch:{ RuntimeException -> 0x009f, all -> 0x00b0 }
        r12 = r0.query(r1, r2, r3, r4, r5);	 Catch:{ RuntimeException -> 0x009f, all -> 0x00b0 }
        if (r6 == 0) goto L_0x009e;
    L_0x009b:
        r6.close();
    L_0x009e:
        return r12;
    L_0x009f:
        r7 = move-exception;
        r1 = "SearchTaskLoader";	 Catch:{ RuntimeException -> 0x009f, all -> 0x00b0 }
        r2 = "Runtime Exception when querying directory";	 Catch:{ RuntimeException -> 0x009f, all -> 0x00b0 }
        com.android.contacts.util.HwLog.w(r1, r2);	 Catch:{ RuntimeException -> 0x009f, all -> 0x00b0 }
        r1 = 0;
        if (r6 == 0) goto L_0x00af;
    L_0x00ac:
        r6.close();
    L_0x00af:
        return r1;
    L_0x00b0:
        r1 = move-exception;
        if (r6 == 0) goto L_0x00b6;
    L_0x00b3:
        r6.close();
    L_0x00b6:
        throw r1;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.contacts.dialpad.SearchTaskLoader.queryEnterpriseContacts(int):android.database.Cursor");
    }

    static {
        rebuild();
    }

    private static void rebuild() {
        int current_column_index = 14;
        if (QueryUtil.isSupportDualSim()) {
            current_column_index = 15;
        }
        if (EmuiFeatureManager.isNumberMarkFeatureEnabled() && MultiUsersUtils.isCurrentUserOwner()) {
            current_column_index += 4;
        }
        sub_projection = new String[current_column_index];
        System.arraycopy(_projection, 0, sub_projection, 0, _projection.length);
        current_column_index = 14;
        if (EmuiFeatureManager.isNumberMarkFeatureEnabled() && MultiUsersUtils.isCurrentUserOwner()) {
            SmartDialType.setMarkTypeColumnIndex(14);
            sub_projection[14] = "mark_type";
            SmartDialType.setMarkContentColumnIndex(15);
            int i = 15 + 1;
            sub_projection[15] = "mark_content";
            SmartDialType.setIsCloudMarkColumnIndex(i);
            current_column_index = i + 1;
            sub_projection[i] = "is_cloud_mark";
            SmartDialType.setMarkCountColumnIndex(current_column_index);
            i = current_column_index + 1;
            sub_projection[current_column_index] = "mark_count";
            current_column_index = i;
        } else {
            SmartDialType.setMarkTypeColumnIndex(0);
            SmartDialType.setMarkContentColumnIndex(0);
            SmartDialType.setIsCloudMarkColumnIndex(0);
            SmartDialType.setMarkCountColumnIndex(0);
        }
        if (QueryUtil.isSupportDualSim()) {
            i = current_column_index + 1;
            sub_projection[current_column_index] = "subscription";
            current_column_index = i;
        }
        mMatixCallLogProjection = new String[(sub_projection.length + 1)];
        System.arraycopy(sub_projection, 0, mMatixCallLogProjection, 0, sub_projection.length);
        mMatixCallLogProjection[sub_projection.length] = "TIMES_CONTACTED";
    }

    private static void checkAndRebuild() {
        if (!(EmuiFeatureManager.isNumberMarkFeatureEnabled() && MultiUsersUtils.isCurrentUserOwner() && SmartDialType.getMarkTypeColumnIndex() == 0)) {
            if (EmuiFeatureManager.isNumberMarkFeatureEnabled() || MultiUsersUtils.isCurrentUserOwner() || SmartDialType.getMarkTypeColumnIndex() == 0) {
                return;
            }
        }
        rebuild();
    }

    public SearchTaskLoader(Context context, CursorAdapter adpater, String queryStr) {
        super(context);
        this.filterString = queryStr;
        Builder uriBuilder = Uri.withAppendedPath(SearchContract$DataSearch.PHONE_CONTENT_FILTER_URI, Uri.encode(queryStr)).buildUpon();
        uriBuilder.appendQueryParameter("search_type", "search_dialer");
        if (EmuiFeatureManager.isSupportRussiaNumberRelevance()) {
            uriBuilder.appendQueryParameter("search_number", String.valueOf(EmuiFeatureManager.isRussiaNumberSearchEnabled())).build();
        }
        uriBuilder.appendQueryParameter("limit", String.valueOf(200));
        setUri(uriBuilder.build());
        setSortOrder("pinyin_name");
        setProjection(SmartDialType.getProjection());
    }

    public Cursor loadInBackground() {
        int yellowPageOffset;
        int enterpriseOffset;
        long getCursorTime = System.currentTimeMillis();
        int yellowPageCount = 0;
        int enterpriseCount = 0;
        ArrayList<Cursor> cursors = new ArrayList(3);
        Cursor contactsCursor = super.loadInBackground();
        cursors.add(contactsCursor);
        if (contactsCursor != null) {
            yellowPageOffset = contactsCursor.getCount();
            enterpriseOffset = yellowPageOffset;
        } else {
            yellowPageOffset = 0;
            enterpriseOffset = 0;
            HwLog.w("SearchTaskLoader", "contactsCursor is NULL");
        }
        if (HwLog.HWFLOW) {
            HwLog.i("SearchTaskLoader", "loadInBackground,contacts query result,count=" + enterpriseOffset);
        }
        int queryLimt = 200 - yellowPageOffset;
        if (queryLimt > 0 && ContactsUtils.FLAG_N_FEATURE) {
            Cursor enterpriseCursor = queryEnterpriseContacts(queryLimt);
            if (enterpriseCursor != null) {
                cursors.add(enterpriseCursor);
                yellowPageOffset += enterpriseCursor.getCount();
                enterpriseCount = enterpriseCursor.getCount();
                queryLimt -= enterpriseCount;
            }
        }
        if (queryLimt > 0 && this.filterString.length() >= 3) {
            String tmpFilter = this.filterString;
            int length = this.filterString.length();
            if (length > 0 && this.filterString.indexOf("*") == length - 1) {
                tmpFilter = this.filterString.substring(0, length - 1);
            }
            Cursor callLogCursor = loadCallLog(tmpFilter, queryLimt);
            cursors.add(callLogCursor);
            yellowPageOffset += callLogCursor.getCount();
            queryLimt -= callLogCursor.getCount();
            if (HwLog.HWFLOW) {
                HwLog.i("SearchTaskLoader", "loadInBackground,callLog query result,count=" + callLogCursor.getCount());
            }
        }
        int callLogAndContactResultSize = yellowPageOffset;
        if (EmuiFeatureManager.isChinaArea() && callLogAndContactResultSize < 100) {
            Cursor yellowPageCursor;
            if (TextUtils.isEmpty(this.filterString)) {
                yellowPageCursor = loadYellowPages(this.filterString, queryLimt);
            } else {
                yellowPageCursor = loadYellowPages2(this.filterString, queryLimt);
            }
            if (yellowPageCursor != null) {
                cursors.add(yellowPageCursor);
                yellowPageCount = yellowPageCursor.getCount();
            }
        }
        cursors.trimToSize();
        Cursor cursor = new HwSearchDialerCursor(new MergeCursor((Cursor[]) cursors.toArray(new Cursor[cursors.size()])), yellowPageOffset, yellowPageCount, enterpriseOffset, enterpriseCount);
        PLog.d(0, "SearchTaskLoader loadInBackground, cost = " + (System.currentTimeMillis() - getCursorTime));
        return cursor;
    }

    private Cursor loadYellowPages(String filter, int countLimit) {
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        SQLiteDatabase db = ContactsAppDatabaseHelper.getInstance(getContext()).getWritableDatabase();
        StringBuilder selection = new StringBuilder();
        ArrayList<String> selectionArgs = new ArrayList();
        qb.setProjectionMap(sSmartdialYellowPageProjectionMap);
        qb.setTables("yellow_page_view");
        if (filter != null) {
            selection.append("number").append("LIKE").append("?");
            selectionArgs.add("%" + filter + "%");
            qb.appendWhere(selection.toString());
        }
        return db.rawQuery(qb.buildQuery(sSmartdialYellowPageProjection, null, null, null, "name COLLATE LOCALIZED", String.valueOf(countLimit)), (String[]) selectionArgs.toArray(new String[selectionArgs.size()]));
    }

    private Cursor loadYellowPages2(String filter, int countLimit) {
        Builder uriBuilder = Uri.withAppendedPath(SearchContract$YellowPageSearch.CONTENT_FILTER_URI, Uri.encode(filter)).buildUpon();
        uriBuilder.appendQueryParameter("search_type", "search_yellowpage");
        uriBuilder.appendQueryParameter("limit", String.valueOf(countLimit));
        int column_size = sSmartdialYellowPageProjection.length;
        String[] projections = new String[column_size];
        System.arraycopy(sSmartdialYellowPageProjection, 0, projections, 0, column_size);
        return getContext().getContentResolver().query(uriBuilder.build(), projections, null, null, "name COLLATE LOCALIZED");
    }

    private String[] getCallLogProjection() {
        checkAndRebuild();
        return sub_projection;
    }

    private Cursor loadCallLog(String filter, int countLimit) {
        String voiceMailNumber = CommonUtilMethods.getTelephonyManager(getContext()).getVoiceMailNumber();
        StringBuffer selection = new StringBuffer();
        ArrayList<String> selectionArgs = new ArrayList();
        if (TextUtils.isEmpty(voiceMailNumber) || !isNum(voiceMailNumber)) {
            selection.append("lookup_uri").append(" is null ").append(" AND ").append("name").append(" is null ").append(" AND ").append("formatted_number").append(" is not null ");
        } else {
            selection.append("lookup_uri").append(" is null ").append(" AND ").append("name").append(" is null ").append(" AND (").append("formatted_number").append(" is not null ").append(" OR ").append("number").append(" = ").append(voiceMailNumber).append(" )");
        }
        selection.insert(0, "(").append(") AND (").append("deleted").append("=0)");
        String searchOrder = "";
        if (!TextUtils.isEmpty(filter)) {
            selection.append(" AND ").append("number").append(" Like ?");
            selectionArgs.add("%" + filter + "%");
            searchOrder = "instr(number,'" + filter + "'" + ")" + ",";
        }
        Cursor c = null;
        try {
            c = getContext().getContentResolver().query(Calls.CONTENT_URI_WITH_VOICEMAIL, getCallLogProjection(), selection.toString(), (String[]) selectionArgs.toArray(new String[selectionArgs.size()]), searchOrder + "number");
        } catch (SQLiteException e) {
            HwLog.e("SearchTaskLoader", "open calllog provider failed " + e);
        }
        MatrixCursor matrix = new MatrixCursor(mMatixCallLogProjection);
        if (c == null) {
            return matrix;
        }
        if (!QueryUtil.isContainColumn(c.getColumnNames(), "subscription")) {
            c = new ExtendedSubscriptionCursor(c);
        }
        try {
            Object[] row = new Object[mMatixCallLogProjection.length];
            int count = 0;
            String lastNumber = "-1";
            while (countLimit > 0 && c.moveToNext()) {
                String number = c.getString(1);
                int i;
                if (lastNumber == null || !lastNumber.equals(number)) {
                    lastNumber = number;
                    if (count > 0) {
                        row[row.length - 1] = Integer.valueOf(count);
                        matrix.addRow(row);
                        countLimit--;
                    }
                    for (i = 0; i < mMatixCallLogProjection.length - 1; i++) {
                        row[i] = c.getString(i);
                    }
                    count = 1;
                } else {
                    for (i = 0; i < mMatixCallLogProjection.length - 1; i++) {
                        row[i] = c.getString(i);
                    }
                    count++;
                }
            }
            if (countLimit > 0 && count > 0) {
                row[row.length - 1] = Integer.valueOf(count);
                matrix.addRow(row);
            }
            c.close();
            return matrix;
        } catch (Throwable th) {
            c.close();
        }
    }

    private static boolean isNum(String str) {
        int i = str.length();
        do {
            i--;
            if (i < 0) {
                return true;
            }
        } while (Character.isDigit(str.charAt(i)));
        return false;
    }

    public String getQueryString() {
        return this.filterString;
    }
}
