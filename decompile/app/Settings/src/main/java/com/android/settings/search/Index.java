package com.android.settings.search;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.content.res.TypedArray;
import android.content.res.XmlResourceParser;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.MergeCursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.provider.SearchIndexableData;
import android.provider.SearchIndexableResource;
import android.provider.SearchIndexablesContract;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.util.Xml;
import com.android.internal.R;
import com.android.settings.R$styleable;
import com.android.settings.search.Indexable.SearchIndexProvider;
import java.text.Normalizer;
import java.text.Normalizer.Form;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;

public class Index {
    private static final List<String> EMPTY_LIST = Collections.emptyList();
    private static final String[] FUZZY_SEARCH_COLUMNS = new String[]{"data_title", "data_summary_on", "data_keywords"};
    private static final int[] HIDE_SUMMARY_TITLES_ID = new int[]{2131627403, 2131627407, 2131627402, 2131625215, 2131627592, 2131625238, 2131625217, 2131625231, 2131625220, 2131625216, 2131625219, 2131625221, 2131625716, 2131624613, 2131624615, 2131624611, 2131628512, 2131625158, 2131624620};
    public static final String INDEXABLE_LANG_BASE_KEY = (Build.DISPLAY + "_indexble_");
    private static final String[] MATCH_COLUMNS_PRIMARY = new String[]{"data_title", "data_title_normalized", "data_keywords"};
    private static final String[] MATCH_COLUMNS_SECONDARY = new String[]{"data_summary_on", "data_summary_on_normalized", "data_summary_off", "data_summary_off_normalized", "data_entries"};
    private static long MAX_SAVED_SEARCH_QUERY = 64;
    private static final Pattern REMOVE_DIACRITICALS_PATTERN = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
    private static final String[] SELECT_COLUMNS = new String[]{"data_rank", "data_title", "data_summary_on", "data_summary_off", "data_entries", "data_keywords", "class_name", "screen_title", "icon", "intent_action", "intent_target_package", "intent_target_class", "enabled", "data_key_reference"};
    private static Index sInstance;
    private final String mBaseAuthority;
    private Context mContext;
    private final UpdateData mDataToProcess = new UpdateData();
    private final AtomicBoolean mIsAvailable = new AtomicBoolean(false);

    private class SaveSearchQueryTask extends AsyncTask<String, Void, Long> {
        private SaveSearchQueryTask() {
        }

        protected Long doInBackground(String... params) {
            long now = new Date().getTime();
            ContentValues values = new ContentValues();
            values.put("query", params[0]);
            values.put("timestamp", Long.valueOf(now));
            SQLiteDatabase database = Index.this.getWritableDatabase();
            if (database == null) {
                Log.e("Index", "Cannot save Search queries as I cannot get a writable database");
                return Long.valueOf(-1);
            }
            long lastInsertedRowId = -1;
            try {
                database.delete("saved_queries", "query = ?", new String[]{params[0]});
                lastInsertedRowId = database.insertOrThrow("saved_queries", null, values);
                if (lastInsertedRowId - Index.MAX_SAVED_SEARCH_QUERY > 0) {
                    Log.d("Index", "Deleted '" + database.delete("saved_queries", "rowId <= ?", new String[]{Long.toString(lastInsertedRowId - Index.MAX_SAVED_SEARCH_QUERY)}) + "' saved Search query(ies)");
                }
            } catch (Exception e) {
                Log.d("Index", "Cannot update saved Search queries", e);
            }
            return Long.valueOf(lastInsertedRowId);
        }
    }

    private static class UpdateData {
        public boolean clearDataFirst;
        public List<SearchIndexableData> dataToDelete;
        public List<SearchIndexableData> dataToUpdate;
        public boolean forceUpdate;
        public boolean fullIndex;
        public Map<String, List<String>> nonIndexableKeys;

        public UpdateData() {
            this.forceUpdate = false;
            this.fullIndex = true;
            this.clearDataFirst = false;
            this.dataToUpdate = new ArrayList();
            this.dataToDelete = new ArrayList();
            this.nonIndexableKeys = new HashMap();
        }

        public UpdateData(UpdateData other) {
            this.forceUpdate = false;
            this.fullIndex = true;
            this.clearDataFirst = false;
            this.dataToUpdate = new ArrayList(other.dataToUpdate);
            this.dataToDelete = new ArrayList(other.dataToDelete);
            this.nonIndexableKeys = new HashMap(other.nonIndexableKeys);
            this.forceUpdate = other.forceUpdate;
            this.fullIndex = other.fullIndex;
            this.clearDataFirst = other.clearDataFirst;
        }

        public UpdateData copy() {
            return new UpdateData(this);
        }

        public void clear() {
            this.dataToUpdate.clear();
            this.dataToDelete.clear();
            this.nonIndexableKeys.clear();
            this.forceUpdate = false;
            this.fullIndex = false;
            this.clearDataFirst = false;
        }
    }

    private class UpdateIndexTask extends AsyncTask<UpdateData, Integer, Void> {
        protected java.lang.Void doInBackground(com.android.settings.search.Index.UpdateData... r13) {
            /* JADX: method processing error */
/*
Error: jadx.core.utils.exceptions.JadxRuntimeException: Can't find immediate dominator for block B:27:? in {3, 6, 10, 13, 14, 16, 17, 20, 21, 23, 25} preds:[]
	at jadx.core.dex.visitors.blocksmaker.BlockProcessor.computeDominators(BlockProcessor.java:129)
	at jadx.core.dex.visitors.blocksmaker.BlockProcessor.processBlocksTree(BlockProcessor.java:48)
	at jadx.core.dex.visitors.blocksmaker.BlockProcessor.rerun(BlockProcessor.java:44)
	at jadx.core.dex.visitors.blocksmaker.BlockFinallyExtract.visit(BlockFinallyExtract.java:57)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:14)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:306)
	at jadx.api.JavaClass.decompile(JavaClass.java:62)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:199)
*/
            /*
            r12 = this;
            r11 = 0;
            r10 = 0;
            r0 = r13[r10];
            r3 = r0.dataToUpdate;
            r0 = r13[r10];
            r6 = r0.dataToDelete;
            r0 = r13[r10];
            r4 = r0.nonIndexableKeys;
            r0 = r13[r10];
            r5 = r0.forceUpdate;
            r0 = r13[r10];
            r9 = r0.fullIndex;
            r0 = com.android.settings.search.Index.this;
            r1 = r0.getWritableDatabase();
            if (r1 != 0) goto L_0x0028;
        L_0x001e:
            r0 = "Index";
            r10 = "Cannot update Index as I cannot get a writable database";
            android.util.Log.e(r0, r10);
            return r11;
        L_0x0028:
            r0 = java.util.Locale.getDefault();
            r2 = r0.toString();
            r0 = r13[r10];
            r0 = r0.clearDataFirst;
            if (r0 == 0) goto L_0x003c;
        L_0x0036:
            r0 = "prefs_index";
            r1.delete(r0, r11, r11);
        L_0x003c:
            r1.beginTransaction();	 Catch:{ all -> 0x0074 }
            r0 = r6.size();	 Catch:{ all -> 0x0074 }
            if (r0 <= 0) goto L_0x0048;	 Catch:{ all -> 0x0074 }
        L_0x0045:
            r12.processDataToDelete(r1, r2, r6);	 Catch:{ all -> 0x0074 }
        L_0x0048:
            r0 = r3.size();	 Catch:{ all -> 0x0074 }
            if (r0 <= 0) goto L_0x0052;	 Catch:{ all -> 0x0074 }
        L_0x004e:
            r0 = r12;	 Catch:{ all -> 0x0074 }
            r0.processDataToUpdate(r1, r2, r3, r4, r5);	 Catch:{ all -> 0x0074 }
        L_0x0052:
            r1.setTransactionSuccessful();	 Catch:{ all -> 0x0074 }
            r1.endTransaction();
        L_0x0058:
            if (r9 == 0) goto L_0x0063;
        L_0x005a:
            r0 = com.android.settings.search.Index.this;
            r0 = r0.mContext;
            com.android.settings.search.IndexDatabaseHelper.setLocaleIndexed(r0, r2);
        L_0x0063:
            return r11;
        L_0x0064:
            r8 = move-exception;
            r8.printStackTrace();
            goto L_0x0058;
        L_0x0069:
            r7 = move-exception;
            r0 = "Index";
            r10 = "database or disk is full, no space left!";
            android.util.Log.e(r0, r10);
            goto L_0x0058;
        L_0x0074:
            r0 = move-exception;
            r1.endTransaction();	 Catch:{ SQLiteFullException -> 0x007e, Exception -> 0x0079 }
        L_0x0078:
            throw r0;
        L_0x0079:
            r8 = move-exception;
            r8.printStackTrace();
            goto L_0x0078;
        L_0x007e:
            r7 = move-exception;
            r10 = "Index";
            r11 = "database or disk is full, no space left!";
            android.util.Log.e(r10, r11);
            goto L_0x0078;
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.settings.search.Index.UpdateIndexTask.doInBackground(com.android.settings.search.Index$UpdateData[]):java.lang.Void");
        }

        private UpdateIndexTask() {
        }

        protected void onPreExecute() {
            super.onPreExecute();
            Index.this.mIsAvailable.set(false);
        }

        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            Index.this.mIsAvailable.set(true);
        }

        private boolean processDataToUpdate(SQLiteDatabase database, String localeStr, List<SearchIndexableData> dataToUpdate, Map<String, List<String>> nonIndexableKeys, boolean forceUpdate) {
            String indexableLangKey = Index.INDEXABLE_LANG_BASE_KEY + localeStr;
            if (Index.this.mContext == null) {
                return false;
            }
            Editor editor = Index.this.mContext.getSharedPreferences("indexablestate", 0).edit();
            boolean isIndexed = IndexDatabaseHelper.isLocaleAlreadyIndexedEx(Index.this.mContext);
            editor.putBoolean(indexableLangKey, isIndexed).apply();
            if (forceUpdate || !isIndexed) {
                editor.putString("indexed_for_version", Build.DISPLAY).apply();
                long current = System.currentTimeMillis();
                int count = dataToUpdate.size();
                for (int n = 0; n < count; n++) {
                    Object data = (SearchIndexableData) dataToUpdate.get(n);
                    try {
                        Index.this.indexOneSearchIndexableData(database, localeStr, data, nonIndexableKeys);
                    } catch (Exception e) {
                        String str = "Index";
                        StringBuilder append = new StringBuilder().append("Cannot index: ");
                        if (data != null) {
                            data = data.className;
                        }
                        Log.e(str, append.append(data).append(" for locale: ").append(localeStr).toString(), e);
                    }
                }
                Log.d("Index", "Indexing locale '" + localeStr + "' took " + (System.currentTimeMillis() - current) + " millis");
                return false;
            }
            Log.d("Index", "Locale '" + localeStr + "' is already indexed");
            return true;
        }

        private boolean processDataToDelete(SQLiteDatabase database, String localeStr, List<SearchIndexableData> dataToDelete) {
            long current = System.currentTimeMillis();
            int count = dataToDelete.size();
            for (int n = 0; n < count; n++) {
                SearchIndexableData data = (SearchIndexableData) dataToDelete.get(n);
                if (data != null) {
                    if (!TextUtils.isEmpty(data.className)) {
                        delete(database, "class_name", data.className);
                    } else if (data instanceof SearchIndexableRaw) {
                        SearchIndexableRaw raw = (SearchIndexableRaw) data;
                        if (!TextUtils.isEmpty(raw.title)) {
                            delete(database, "data_title", raw.title);
                        }
                    }
                }
            }
            Log.d("Index", "Deleting data for locale '" + localeStr + "' took " + (System.currentTimeMillis() - current) + " millis");
            return false;
        }

        private int delete(SQLiteDatabase database, String columName, String value) {
            int result = 0;
            try {
                result = database.delete("prefs_index", columName + "=?", new String[]{value});
            } catch (Exception exp) {
                Log.e("Index", exp.getMessage().toString());
            }
            return result;
        }
    }

    public static Index getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new Index(context.getApplicationContext(), "com.android.settings");
        }
        return sInstance;
    }

    public Index(Context context, String baseAuthority) {
        this.mContext = context;
        this.mBaseAuthority = baseAuthority;
    }

    public Cursor search(String query) {
        SQLiteDatabase database = getReadableDatabase();
        if (isFuzzySearchLanguage()) {
            return database.rawQuery(buildFuzzySearchSQL(query, FUZZY_SEARCH_COLUMNS, true), null);
        }
        Cursor[] cursors = new Cursor[2];
        String primarySql = buildSearchSQL(query, MATCH_COLUMNS_PRIMARY, true);
        Log.d("Index", "Search primary query: " + primarySql);
        cursors[0] = database.rawQuery(primarySql, null);
        StringBuilder sql = new StringBuilder(buildSearchSQL(query, MATCH_COLUMNS_SECONDARY, false));
        sql.append(" EXCEPT ");
        sql.append(primarySql);
        String secondarySql = sql.toString();
        Log.d("Index", "Search secondary query: " + secondarySql);
        cursors[1] = database.rawQuery(secondarySql, null);
        return new MergeCursor(cursors);
    }

    public Cursor getSuggestions(String query) {
        String sql = buildSuggestionsSQL(query);
        Log.d("Index", "Suggestions query: " + sql);
        return getReadableDatabase().rawQuery(sql, null);
    }

    private String buildSuggestionsSQL(String query) {
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT ");
        sb.append("query");
        sb.append(" FROM ");
        sb.append("saved_queries");
        if (TextUtils.isEmpty(query)) {
            sb.append(" ORDER BY rowId DESC");
        } else {
            sb.append(" WHERE ");
            sb.append("query");
            sb.append(" LIKE ");
            sb.append("'");
            sb.append(query);
            sb.append("%");
            sb.append("'");
        }
        sb.append(" LIMIT ");
        sb.append(5);
        return sb.toString();
    }

    public void addSavedQuery(String query) {
        new SaveSearchQueryTask().execute(new String[]{query});
    }

    public void update(boolean clearDataFirst) {
        if (clearDataFirst) {
            this.mContext.getSharedPreferences("indexablestate", 0).edit().clear().apply();
        }
        this.mDataToProcess.clearDataFirst = clearDataFirst;
        update();
    }

    public void update() {
        AsyncTask.execute(new Runnable() {
            public void run() {
                List<ResolveInfo> list = Index.this.mContext.getPackageManager().queryIntentContentProviders(new Intent("android.content.action.SEARCH_INDEXABLES_PROVIDER"), 0);
                int size = list.size();
                for (int n = 0; n < size; n++) {
                    ResolveInfo info = (ResolveInfo) list.get(n);
                    if (Index.this.isWellKnownProvider(info)) {
                        String authority = info.providerInfo.authority;
                        String packageName = info.providerInfo.packageName;
                        Index.this.addIndexablesFromRemoteProvider(packageName, authority);
                        Index.this.addNonIndexablesKeysFromRemoteProvider(packageName, authority);
                    }
                }
                Index.this.mDataToProcess.fullIndex = true;
                Index.this.updateInternal();
            }
        });
    }

    private boolean addIndexablesFromRemoteProvider(String packageName, String authority) {
        try {
            int baseRank = Ranking.getBaseRankForAuthority(authority);
            Context context = this.mBaseAuthority.equals(authority) ? this.mContext : this.mContext.createPackageContext(packageName, 0);
            addIndexablesForXmlResourceUri(context, packageName, buildUriForXmlResources(authority), SearchIndexablesContract.INDEXABLES_XML_RES_COLUMNS, baseRank);
            addIndexablesForRawDataUri(context, packageName, buildUriForRawData(authority), SearchIndexablesContract.INDEXABLES_RAW_COLUMNS, baseRank);
            return true;
        } catch (NameNotFoundException e) {
            Log.w("Index", "Could not create context for " + packageName + ": " + Log.getStackTraceString(e));
            return false;
        }
    }

    private void addNonIndexablesKeysFromRemoteProvider(String packageName, String authority) {
        addNonIndexableKeys(packageName, getNonIndexablesKeysFromRemoteProvider(packageName, authority));
    }

    private List<String> getNonIndexablesKeysFromRemoteProvider(String packageName, String authority) {
        try {
            return getNonIndexablesKeys(this.mContext.createPackageContext(packageName, 0), buildUriForNonIndexableKeys(authority), SearchIndexablesContract.NON_INDEXABLES_KEYS_COLUMNS);
        } catch (NameNotFoundException e) {
            Log.w("Index", "Could not create context for " + packageName + ": " + Log.getStackTraceString(e));
            return EMPTY_LIST;
        }
    }

    private List<String> getNonIndexablesKeys(Context packageContext, Uri uri, String[] projection) {
        Cursor cursor = packageContext.getContentResolver().query(uri, projection, null, null, null);
        if (cursor == null) {
            Log.w("Index", "Cannot add index data for Uri: " + uri.toString());
            return EMPTY_LIST;
        }
        List<String> result = new ArrayList();
        try {
            if (cursor.getCount() > 0) {
                while (cursor.moveToNext()) {
                    result.add(cursor.getString(0));
                }
            }
            cursor.close();
            return result;
        } catch (Throwable th) {
            cursor.close();
        }
    }

    public void addIndexableData(SearchIndexableData data) {
        synchronized (this.mDataToProcess) {
            this.mDataToProcess.dataToUpdate.add(data);
        }
    }

    public void deleteIndexableData(SearchIndexableData data) {
        synchronized (this.mDataToProcess) {
            this.mDataToProcess.dataToDelete.add(data);
        }
    }

    public void addNonIndexableKeys(String authority, List<String> keys) {
        synchronized (this.mDataToProcess) {
            this.mDataToProcess.nonIndexableKeys.put(authority, keys);
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean isWellKnownProvider(ResolveInfo info) {
        boolean z = true;
        String[] allowedProviders = new String[]{"com.huawei.android.dsdscardmanager", "com.huawei.android.hwouc", "com.huawei.android.launcher", "com.huawei.vassistant"};
        String authority = info.providerInfo.authority;
        String packageName = info.providerInfo.applicationInfo.packageName;
        if (TextUtils.isEmpty(authority) || TextUtils.isEmpty(packageName)) {
            return false;
        }
        String readPermission = info.providerInfo.readPermission;
        String writePermission = info.providerInfo.writePermission;
        if (TextUtils.isEmpty(readPermission) || TextUtils.isEmpty(writePermission) || !"android.permission.READ_SEARCH_INDEXABLES".equals(readPermission) || !"android.permission.READ_SEARCH_INDEXABLES".equals(writePermission)) {
            return false;
        }
        if (!isPrivilegedPackage(packageName)) {
            z = Arrays.asList(allowedProviders).contains(packageName);
        }
        return z;
    }

    private boolean isPrivilegedPackage(String packageName) {
        boolean z = false;
        try {
            if ((this.mContext.getPackageManager().getPackageInfo(packageName, 0).applicationInfo.privateFlags & 8) != 0) {
                z = true;
            }
            return z;
        } catch (NameNotFoundException e) {
            return false;
        }
    }

    public void updateFromClassNameResource(String className, final boolean rebuild, boolean includeInSearchResults) {
        if (className == null) {
            throw new IllegalArgumentException("class name cannot be null!");
        }
        final SearchIndexableResource res = SearchIndexableResources.getResourceByName(className);
        if (res == null) {
            Log.e("Index", "Cannot find SearchIndexableResources for class name: " + className);
            return;
        }
        res.context = this.mContext;
        res.enabled = includeInSearchResults;
        AsyncTask.execute(new Runnable() {
            public void run() {
                if (rebuild) {
                    Index.this.deleteIndexableData(res);
                }
                Index.this.addIndexableData(res);
                Index.this.mDataToProcess.forceUpdate = true;
                Index.this.updateInternal();
                res.enabled = false;
            }
        });
    }

    public void updateFromSearchIndexableData(final SearchIndexableData data) {
        AsyncTask.execute(new Runnable() {
            public void run() {
                Index.this.addIndexableData(data);
                Index.this.mDataToProcess.forceUpdate = true;
                Index.this.updateInternal();
            }
        });
    }

    private SQLiteDatabase getReadableDatabase() {
        return IndexDatabaseHelper.getInstance(this.mContext).getReadableDatabase();
    }

    private SQLiteDatabase getWritableDatabase() {
        try {
            return IndexDatabaseHelper.getInstance(this.mContext).getWritableDatabase();
        } catch (SQLiteException e) {
            Log.e("Index", "Cannot open writable database", e);
            return null;
        }
    }

    private static Uri buildUriForXmlResources(String authority) {
        return Uri.parse("content://" + authority + "/" + "settings/indexables_xml_res");
    }

    private static Uri buildUriForRawData(String authority) {
        return Uri.parse("content://" + authority + "/" + "settings/indexables_raw");
    }

    private static Uri buildUriForNonIndexableKeys(String authority) {
        return Uri.parse("content://" + authority + "/" + "settings/non_indexables_key");
    }

    private void updateInternal() {
        synchronized (this.mDataToProcess) {
            UpdateIndexTask task = new UpdateIndexTask();
            UpdateData copy = this.mDataToProcess.copy();
            task.execute(new UpdateData[]{copy});
            this.mDataToProcess.clear();
        }
    }

    private void addIndexablesForXmlResourceUri(Context packageContext, String packageName, Uri uri, String[] projection, int baseRank) {
        Cursor cursor = packageContext.getContentResolver().query(uri, projection, null, null, null);
        if (cursor == null) {
            Log.w("Index", "Cannot add index data for Uri: " + uri.toString());
            return;
        }
        try {
            if (cursor.getCount() > 0) {
                while (cursor.moveToNext()) {
                    int providerRank = cursor.getInt(0);
                    int rank = providerRank > 0 ? baseRank + providerRank : baseRank;
                    int xmlResId = cursor.getInt(1);
                    String className = cursor.getString(2);
                    int iconResId = cursor.getInt(3);
                    String action = cursor.getString(4);
                    String targetPackage = cursor.getString(5);
                    String targetClass = cursor.getString(6);
                    SearchIndexableResource sir = new SearchIndexableResource(packageContext);
                    sir.rank = rank;
                    sir.xmlResId = xmlResId;
                    sir.className = className;
                    sir.packageName = packageName;
                    sir.iconResId = iconResId;
                    sir.intentAction = action;
                    sir.intentTargetPackage = targetPackage;
                    sir.intentTargetClass = targetClass;
                    addIndexableData(sir);
                }
            }
            cursor.close();
        } catch (Throwable th) {
            cursor.close();
        }
    }

    private void addIndexablesForRawDataUri(Context packageContext, String packageName, Uri uri, String[] projection, int baseRank) {
        if (packageContext != null) {
            Cursor cursor = packageContext.getContentResolver().query(uri, projection, null, null, null);
            if (cursor == null) {
                Log.w("Index", "Cannot add index data for Uri: " + uri.toString());
                return;
            }
            try {
                if (cursor.getCount() > 0) {
                    while (cursor.moveToNext()) {
                        int providerRank = cursor.getInt(0);
                        int rank = providerRank > 0 ? baseRank + providerRank : baseRank;
                        String title = cursor.getString(1);
                        String summaryOn = cursor.getString(2);
                        String summaryOff = cursor.getString(3);
                        String entries = cursor.getString(4);
                        String keywords = cursor.getString(5);
                        String screenTitle = cursor.getString(6);
                        String className = cursor.getString(7);
                        int iconResId = cursor.getInt(8);
                        String action = cursor.getString(9);
                        String targetPackage = cursor.getString(10);
                        String targetClass = cursor.getString(11);
                        String key = cursor.getString(12);
                        int userId = cursor.getInt(13);
                        SearchIndexableRaw data = new SearchIndexableRaw(packageContext);
                        data.rank = rank;
                        data.title = title;
                        data.summaryOn = summaryOn;
                        data.summaryOff = summaryOff;
                        data.entries = entries;
                        data.keywords = keywords;
                        data.screenTitle = screenTitle;
                        data.className = className;
                        data.packageName = packageName;
                        data.iconResId = iconResId;
                        data.intentAction = action;
                        data.intentTargetPackage = targetPackage;
                        data.intentTargetClass = targetClass;
                        data.key = key;
                        data.userId = userId;
                        addIndexableData(data);
                    }
                }
                cursor.close();
            } catch (Throwable th) {
                cursor.close();
            }
        }
    }

    private String buildSearchSQL(String query, String[] colums, boolean withOrderBy) {
        StringBuilder sb = new StringBuilder();
        sb.append(buildSearchSQLForColumn(query, colums));
        if (withOrderBy) {
            sb.append(" ORDER BY ");
            sb.append("data_rank");
        }
        return sb.toString();
    }

    private String buildSearchSQLForColumn(String query, String[] columnNames) {
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT ");
        for (int n = 0; n < SELECT_COLUMNS.length; n++) {
            sb.append(SELECT_COLUMNS[n]);
            if (n < SELECT_COLUMNS.length - 1) {
                sb.append(", ");
            }
        }
        sb.append(" FROM ");
        sb.append("prefs_index");
        sb.append(" WHERE ");
        sb.append(buildSearchWhereStringForColumns(query, columnNames));
        return sb.toString();
    }

    private String buildSearchWhereStringForColumns(String query, String[] columnNames) {
        StringBuilder sb = new StringBuilder("prefs_index");
        sb.append(" MATCH ");
        DatabaseUtils.appendEscapedSQLString(sb, buildSearchMatchStringForColumns(query, columnNames));
        sb.append(" AND ");
        sb.append("locale");
        sb.append(" = ");
        DatabaseUtils.appendEscapedSQLString(sb, Locale.getDefault().toString());
        sb.append(" AND ");
        sb.append("enabled");
        sb.append(" = 1");
        return sb.toString();
    }

    private String buildSearchMatchStringForColumns(String query, String[] columnNames) {
        String value = query + "*";
        StringBuilder sb = new StringBuilder();
        int count = columnNames.length;
        for (int n = 0; n < count; n++) {
            sb.append(columnNames[n]);
            sb.append(":");
            sb.append(value);
            if (n < count - 1) {
                sb.append(" OR ");
            }
        }
        return sb.toString();
    }

    private boolean isFuzzySearchLanguage() {
        Locale locale = Locale.getDefault();
        if (locale.getLanguage().equalsIgnoreCase(Locale.CHINA.getLanguage()) || Locale.JAPAN.equals(locale) || Locale.KOREA.equals(locale)) {
            return true;
        }
        return false;
    }

    private String buildFuzzySearchSQL(String query, String[] colums, boolean withOrderBy) {
        StringBuilder sb = new StringBuilder();
        sb.append(buildFuzzySearchSQLForColumn(query, colums));
        if (withOrderBy) {
            sb.append(" ORDER BY ");
            sb.append("data_rank");
        }
        return sb.toString();
    }

    private String buildFuzzySearchSQLForColumn(String query, String[] columnNames) {
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT ");
        for (int n = 0; n < SELECT_COLUMNS.length; n++) {
            sb.append(SELECT_COLUMNS[n]);
            if (n < SELECT_COLUMNS.length - 1) {
                sb.append(", ");
            }
        }
        sb.append(" FROM ");
        sb.append("prefs_index");
        sb.append(" WHERE ");
        sb.append(buildFuzzySearchWhereStringForColumns(query, columnNames));
        return sb.toString();
    }

    private String buildFuzzySearchWhereStringForColumns(String query, String[] columnNames) {
        String likeVal = "%" + query + "%";
        StringBuilder sb = new StringBuilder();
        sb.append(" (");
        sb.append("locale");
        sb.append(" = ");
        DatabaseUtils.appendEscapedSQLString(sb, Locale.getDefault().toString());
        sb.append(" AND ");
        sb.append("enabled");
        sb.append(" = 1");
        sb.append(") AND (");
        int count = columnNames.length;
        for (int n = 0; n < count; n++) {
            sb.append(columnNames[n]);
            sb.append(" like ");
            DatabaseUtils.appendEscapedSQLString(sb, likeVal);
            if (n < count - 1) {
                sb.append(" OR ");
            }
        }
        sb.append(")");
        return sb.toString();
    }

    private void indexOneSearchIndexableData(SQLiteDatabase database, String localeStr, SearchIndexableData data, Map<String, List<String>> nonIndexableKeys) {
        if (data instanceof SearchIndexableResource) {
            indexOneResource(database, localeStr, (SearchIndexableResource) data, nonIndexableKeys);
        } else if (data instanceof SearchIndexableRaw) {
            indexOneRaw(database, localeStr, (SearchIndexableRaw) data);
        }
    }

    private void indexOneRaw(SQLiteDatabase database, String localeStr, SearchIndexableRaw raw) {
        if (raw.locale.toString().equalsIgnoreCase(localeStr)) {
            updateOneRowWithFilteredData(database, localeStr, raw.title, raw.summaryOn, raw.summaryOff, raw.entries, raw.className, raw.screenTitle, raw.iconResId, raw.rank, raw.keywords, raw.intentAction, raw.intentTargetPackage, raw.intentTargetClass, raw.enabled, raw.key, raw.userId);
        }
    }

    private static boolean isIndexableClass(Class<?> clazz) {
        return clazz != null ? Indexable.class.isAssignableFrom(clazz) : false;
    }

    private static Class<?> getIndexableClass(String className) {
        try {
            Class<?> clazz = Class.forName(className);
            if (!isIndexableClass(clazz)) {
                clazz = null;
            }
            return clazz;
        } catch (ClassNotFoundException e) {
            Log.d("Index", "Cannot find class: " + className);
            return null;
        }
    }

    private void indexOneResource(SQLiteDatabase database, String localeStr, SearchIndexableResource sir, Map<String, List<String>> nonIndexableKeysFromResource) {
        if (sir == null) {
            Log.e("Index", "Cannot index a null resource!");
            return;
        }
        List<String> nonIndexableKeys = new ArrayList();
        if (sir.xmlResId > SearchIndexableResources.NO_DATA_RES_ID) {
            List<String> resNonIndxableKeys = (List) nonIndexableKeysFromResource.get(sir.packageName);
            if (resNonIndxableKeys != null && resNonIndxableKeys.size() > 0) {
                nonIndexableKeys.addAll(resNonIndxableKeys);
            }
            indexFromResource(sir.context, database, localeStr, sir.xmlResId, sir.className, sir.iconResId, sir.rank, sir.intentAction, sir.intentTargetPackage, sir.intentTargetClass, nonIndexableKeys);
        } else if (TextUtils.isEmpty(sir.className)) {
            Log.w("Index", "Cannot index an empty Search Provider name!");
        } else {
            Class<?> clazz = getIndexableClass(sir.className);
            if (clazz == null) {
                Log.d("Index", "SearchIndexableResource '" + sir.className + "' should implement the " + Indexable.class.getName() + " interface!");
                return;
            }
            SearchIndexProvider provider = getSearchIndexProvider(clazz);
            if (provider != null) {
                List<String> providerNonIndexableKeys = provider.getNonIndexableKeys(sir.context);
                if (providerNonIndexableKeys != null && providerNonIndexableKeys.size() > 0) {
                    nonIndexableKeys.addAll(providerNonIndexableKeys);
                }
                indexFromProvider(this.mContext, database, localeStr, provider, sir.className, sir.iconResId, sir.rank, sir.enabled, nonIndexableKeys);
            }
        }
    }

    private SearchIndexProvider getSearchIndexProvider(Class<?> clazz) {
        try {
            return (SearchIndexProvider) clazz.getField("SEARCH_INDEX_DATA_PROVIDER").get(null);
        } catch (NoSuchFieldException e) {
            Log.d("Index", "Cannot find field 'SEARCH_INDEX_DATA_PROVIDER'");
            return null;
        } catch (SecurityException e2) {
            Log.d("Index", "Security exception for field 'SEARCH_INDEX_DATA_PROVIDER'");
            return null;
        } catch (IllegalAccessException e3) {
            Log.d("Index", "Illegal access to field 'SEARCH_INDEX_DATA_PROVIDER'");
            return null;
        } catch (IllegalArgumentException e4) {
            Log.d("Index", "Illegal argument when accessing field 'SEARCH_INDEX_DATA_PROVIDER'");
            return null;
        }
    }

    private void indexFromResource(Context context, SQLiteDatabase database, String localeStr, int xmlResId, String fragmentName, int iconResId, int rank, String intentAction, String intentTargetPackage, String intentTargetClass, List<String> nonIndexableKeys) {
        XmlResourceParser xmlResourceParser = null;
        try {
            int type;
            xmlResourceParser = context.getResources().getXml(xmlResId);
            do {
                type = xmlResourceParser.next();
                if (type == 1) {
                    break;
                }
            } while (type != 2);
            String nodeName = xmlResourceParser.getName();
            if ("PreferenceScreen".equals(nodeName)) {
                int outerDepth = xmlResourceParser.getDepth();
                AttributeSet attrs = Xml.asAttributeSet(xmlResourceParser);
                String screenTitle = getDataTitle(context, attrs);
                String key = getDataKey(context, attrs);
                if (!nonIndexableKeys.contains(key)) {
                    updateOneRowWithFilteredData(database, localeStr, getDataTitle(context, attrs), getDataSummary(context, attrs), null, null, fragmentName, screenTitle, iconResId, rank, getDataKeywords(context, attrs), intentAction, intentTargetPackage, intentTargetClass, true, key, -1);
                }
                while (true) {
                    type = xmlResourceParser.next();
                    if (type == 1 || (type == 3 && xmlResourceParser.getDepth() <= outerDepth)) {
                        if (xmlResourceParser != null) {
                            xmlResourceParser.close();
                            return;
                        }
                        return;
                    } else if (!(type == 3 || type == 4)) {
                        nodeName = xmlResourceParser.getName();
                        key = getDataKey(context, attrs);
                        if (nonIndexableKeys.contains(key)) {
                            continue;
                        } else {
                            String title = getDataTitle(context, attrs);
                            String keywords = getDataKeywords(context, attrs);
                            if (nodeName.equals("CheckBoxPreference")) {
                                String summaryOn = getDataSummaryOn(context, attrs);
                                String summaryOff = getDataSummaryOff(context, attrs);
                                if (TextUtils.isEmpty(summaryOn) && TextUtils.isEmpty(summaryOff)) {
                                    summaryOn = getDataSummary(context, attrs);
                                }
                                updateOneRowWithFilteredData(database, localeStr, title, summaryOn, summaryOff, null, fragmentName, screenTitle, iconResId, rank, keywords, intentAction, intentTargetPackage, intentTargetClass, true, key, -1);
                            } else {
                                String summary = getDataSummary(context, attrs);
                                String entries = null;
                                if (nodeName.endsWith("ListPreference")) {
                                    entries = getDataEntries(context, attrs);
                                }
                                updateOneRowWithFilteredData(database, localeStr, title, summary, null, entries, fragmentName, screenTitle, iconResId, rank, keywords, intentAction, intentTargetPackage, intentTargetClass, true, key, -1);
                            }
                        }
                    }
                }
                if (xmlResourceParser != null) {
                    xmlResourceParser.close();
                    return;
                }
                return;
            }
            throw new RuntimeException("XML document must start with <PreferenceScreen> tag; found" + nodeName + " at " + xmlResourceParser.getPositionDescription());
        } catch (Throwable e) {
            throw new RuntimeException("Error parsing PreferenceScreen", e);
        } catch (Throwable e2) {
            throw new RuntimeException("Error parsing PreferenceScreen", e2);
        } catch (Throwable th) {
            if (xmlResourceParser != null) {
                xmlResourceParser.close();
            }
        }
    }

    private void indexFromProvider(Context context, SQLiteDatabase database, String localeStr, SearchIndexProvider provider, String className, int iconResId, int rank, boolean enabled, List<String> nonIndexableKeys) {
        if (provider == null) {
            Log.w("Index", "Cannot find provider: " + className);
            return;
        }
        int i;
        List<SearchIndexableRaw> rawList = provider.getRawDataToIndex(context, enabled);
        if (rawList != null) {
            int rawSize = rawList.size();
            for (i = 0; i < rawSize; i++) {
                SearchIndexableRaw raw = (SearchIndexableRaw) rawList.get(i);
                if (raw.locale.toString().equalsIgnoreCase(localeStr)) {
                    if (!nonIndexableKeys.contains(raw.key)) {
                        int i2;
                        String str = raw.title;
                        String str2 = raw.summaryOn;
                        String str3 = raw.summaryOff;
                        String str4 = raw.entries;
                        String str5 = raw.screenTitle;
                        if (raw.iconResId > 0) {
                            i2 = raw.iconResId;
                        } else {
                            i2 = iconResId;
                        }
                        updateOneRowWithFilteredData(database, localeStr, str, str2, str3, str4, className, str5, i2, rank, raw.keywords, raw.intentAction, raw.intentTargetPackage, raw.intentTargetClass, raw.enabled, raw.key, raw.userId);
                    }
                }
            }
        }
        List<SearchIndexableResource> resList = provider.getXmlResourcesToIndex(context, enabled);
        if (resList != null) {
            int resSize = resList.size();
            for (i = 0; i < resSize; i++) {
                SearchIndexableResource item = (SearchIndexableResource) resList.get(i);
                if (item.locale.toString().equalsIgnoreCase(localeStr)) {
                    indexFromResource(context, database, localeStr, item.xmlResId, TextUtils.isEmpty(item.className) ? className : item.className, item.iconResId == 0 ? iconResId : item.iconResId, item.rank == 0 ? rank : item.rank, item.intentAction, item.intentTargetPackage, item.intentTargetClass, nonIndexableKeys);
                }
            }
        }
    }

    private void updateOneRowWithFilteredData(SQLiteDatabase database, String locale, String title, String summaryOn, String summaryOff, String entries, String className, String screenTitle, int iconResId, int rank, String keywords, String intentAction, String intentTargetPackage, String intentTargetClass, boolean enabled, String key, int userId) {
        String updatedTitle = normalizeHyphen(title);
        String updatedSummaryOn = normalizeHyphen(summaryOn);
        String updatedSummaryOff = normalizeHyphen(summaryOff);
        updateOneRow(database, locale, updatedTitle, normalizeString(updatedTitle), updatedSummaryOn, normalizeString(updatedSummaryOn), updatedSummaryOff, normalizeString(updatedSummaryOff), entries, className, screenTitle, iconResId, rank, normalizeKeywords(keywords), intentAction, intentTargetPackage, intentTargetClass, enabled, key, userId);
    }

    private static String normalizeHyphen(String input) {
        return input != null ? input.replaceAll("‑", "-") : "";
    }

    private static String normalizeString(String input) {
        return REMOVE_DIACRITICALS_PATTERN.matcher(Normalizer.normalize(input != null ? input.replaceAll("-", "") : "", Form.NFD)).replaceAll("").toLowerCase();
    }

    private boolean shouldHideSummaryBuTitle(Context packageContext, String title) {
        for (int string : HIDE_SUMMARY_TITLES_ID) {
            if (title.equals(packageContext.getResources().getString(string))) {
                return true;
            }
        }
        return false;
    }

    private static String normalizeKeywords(String input) {
        return input != null ? input.replaceAll("[,]\\s*", " ") : "";
    }

    private void updateOneRow(SQLiteDatabase database, String locale, String updatedTitle, String normalizedTitle, String updatedSummaryOn, String normalizedSummaryOn, String updatedSummaryOff, String normalizedSummaryOff, String entries, String className, String screenTitle, int iconResId, int rank, String spaceDelimitedKeywords, String intentAction, String intentTargetPackage, String intentTargetClass, boolean enabled, String key, int userId) {
        if (!TextUtils.isEmpty(updatedTitle)) {
            if (shouldHideSummaryBuTitle(this.mContext, updatedTitle)) {
                updatedSummaryOn = null;
                updatedSummaryOff = null;
                entries = null;
            }
            StringBuilder sb = new StringBuilder(updatedTitle);
            sb.append(screenTitle);
            int docId = sb.toString().hashCode();
            ContentValues values = new ContentValues();
            values.put("docid", Integer.valueOf(docId));
            values.put("locale", locale);
            values.put("data_rank", Integer.valueOf(rank));
            values.put("data_title", updatedTitle);
            values.put("data_title_normalized", normalizedTitle);
            values.put("data_summary_on", updatedSummaryOn);
            values.put("data_summary_on_normalized", normalizedSummaryOn);
            values.put("data_summary_off", updatedSummaryOff);
            values.put("data_summary_off_normalized", normalizedSummaryOff);
            values.put("data_entries", entries);
            values.put("data_keywords", spaceDelimitedKeywords);
            values.put("class_name", className);
            values.put("screen_title", screenTitle);
            values.put("intent_action", intentAction);
            values.put("intent_target_package", intentTargetPackage);
            values.put("intent_target_class", intentTargetClass);
            values.put("icon", Integer.valueOf(iconResId));
            values.put("enabled", Boolean.valueOf(enabled));
            values.put("data_key_reference", key);
            values.put("user_id", Integer.valueOf(userId));
            database.replaceOrThrow("prefs_index", null, values);
        }
    }

    private String getDataKey(Context context, AttributeSet attrs) {
        return getData(context, attrs, R.styleable.Preference, 6);
    }

    private String getDataTitle(Context context, AttributeSet attrs) {
        return getData(context, attrs, R.styleable.Preference, 4);
    }

    private String getDataSummary(Context context, AttributeSet attrs) {
        return getData(context, attrs, R.styleable.Preference, 7);
    }

    private String getDataSummaryOn(Context context, AttributeSet attrs) {
        return getData(context, attrs, R.styleable.CheckBoxPreference, 0);
    }

    private String getDataSummaryOff(Context context, AttributeSet attrs) {
        return getData(context, attrs, R.styleable.CheckBoxPreference, 1);
    }

    private String getDataEntries(Context context, AttributeSet attrs) {
        return getDataEntries(context, attrs, R.styleable.ListPreference, 0);
    }

    private String getDataKeywords(Context context, AttributeSet attrs) {
        return getData(context, attrs, R$styleable.Preference, 28);
    }

    private String getData(Context context, AttributeSet set, int[] attrs, int resId) {
        TypedArray sa = context.obtainStyledAttributes(set, attrs);
        TypedValue tv = sa.peekValue(resId);
        CharSequence data = null;
        if (tv != null && tv.type == 3) {
            data = tv.resourceId != 0 ? context.getText(tv.resourceId) : tv.string;
        }
        sa.recycle();
        if (data != null) {
            return data.toString();
        }
        return null;
    }

    private String getDataEntries(Context context, AttributeSet set, int[] attrs, int resId) {
        int count = 0;
        TypedArray sa = context.obtainStyledAttributes(set, attrs);
        TypedValue tv = sa.peekValue(resId);
        sa.recycle();
        String[] data = null;
        if (!(tv == null || tv.type != 1 || tv.resourceId == 0)) {
            data = context.getResources().getStringArray(tv.resourceId);
        }
        if (data != null) {
            count = data.length;
        }
        if (count == 0) {
            return null;
        }
        StringBuilder result = new StringBuilder();
        for (int n = 0; n < count; n++) {
            result.append(data[n]);
            result.append("|");
        }
        return result.toString();
    }
}
