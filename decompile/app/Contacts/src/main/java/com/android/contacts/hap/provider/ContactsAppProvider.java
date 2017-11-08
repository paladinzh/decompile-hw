package com.android.contacts.hap.provider;

import android.content.ContentProvider;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentValues;
import android.content.Context;
import android.content.OperationApplicationException;
import android.content.UriMatcher;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.text.TextUtils;
import com.android.common.content.ProjectionMap;
import com.android.contacts.compatibility.QueryUtil;
import com.android.contacts.hap.EmuiFeatureManager;
import com.android.contacts.hap.numbermark.NumberMarkManager;
import com.android.contacts.hap.numbermark.PhoneMatch;
import com.android.contacts.hap.numbermark.YellowPageContactUtil;
import com.android.contacts.hap.provider.ContactsAppDatabaseHelper.NumberMark;
import com.android.contacts.hap.utils.FixedPhoneNumberMatchUtils;
import com.android.contacts.util.ContactsThreadPool;
import com.android.contacts.util.HwLog;
import com.android.vcard.VCardComposer;
import com.autonavi.amap.mapcore.MapTilsCacheAndResManager;
import com.autonavi.amap.mapcore.VTMCDataCache;
import com.google.android.gms.location.LocationRequest;
import com.huawei.cust.HwCustUtils;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.json.JSONException;
import org.json.JSONObject;

public class ContactsAppProvider extends ContentProvider {
    public static final Uri AUTHORITY_URI = Uri.parse("content://com.android.contacts.app");
    public static final Uri YELLOW_PAGE_AS_CARD_URI = Uri.parse("content://com.android.contacts.app/yellow_page_as_card");
    public static final Uri YELLOW_PAGE_DATA_URI = Uri.parse("content://com.android.contacts.app/yellow_page_data");
    public static final Uri YELLOW_PAGE_FILTER_URI = Uri.parse("content://com.android.contacts.app/yellow_page/filter");
    public static final Uri YELLOW_PAGE_URI = Uri.parse("content://com.android.contacts.app/yellow_page");
    private static final ProjectionMap sContactsVCardProjectionMap = ProjectionMap.builder().add("_id", "_ID").add("_display_name", "name || '.vcf'").add("_size", "NULL").build();
    private static final ProjectionMap sNumberMarkExtrasProjectionMap = ProjectionMap.builder().add("_ID").add("NUMBER").add("TITLE").add("CONTENT").add("TYPE").add("ICON").add("INTERNAL_LINK").add("EXTERNAL_LINK").add("LONGITUDE").add("LATITUDE").add("TIMESTAMP").build();
    private static final ProjectionMap sNumberMarkProjectionMap = ProjectionMap.builder().add("_ID").add("NUMBER").add("NAME").add("CLASSIFY").add("MARKED_COUNT").add("IS_CLOUD").add("DESCRIPTION").build();
    static ContactsAppDatabaseHelper sSingleton = null;
    private static final ProjectionMap sSpeedDialProjectionMap = ProjectionMap.builder().add("key_number", "key_number").add("phone_data_id", "phone_data_id").add("number").build();
    private static final UriMatcher sUriMatcher = new UriMatcher(-1);
    private static final ProjectionMap sYellowPageProjectionMap = ProjectionMap.builder().add("_ID").add("name").add(MapTilsCacheAndResManager.AUTONAVI_DATA_PATH).add("photo").add("group_name").build();
    private static final ProjectionMap sYellowPageViewProjectionMap = ProjectionMap.builder().add("_ID").add("name").add("number").add("hot_points").add("dial_map").add("ypid").add("photo").add("photouri").build();
    private HwCustContactsAppProvider mCust = null;
    private NumberMarkManager mMarkManager = null;
    private ContactsAppDatabaseHelper mOpenHelper;

    static {
        sUriMatcher.addURI("com.android.contacts.app", "speed_dial", 100);
        sUriMatcher.addURI("com.android.contacts.app", "number_mark_local", 200);
        sUriMatcher.addURI("com.android.contacts.app", "number_mark", 201);
        sUriMatcher.addURI("com.android.contacts.app", "number_mark_extras", 202);
        sUriMatcher.addURI("com.android.contacts.app", "number_mark_to_system_manager", 203);
        sUriMatcher.addURI("com.android.contacts.app", "yellow_page", VTMCDataCache.MAX_EXPIREDTIME);
        sUriMatcher.addURI("com.android.contacts.app", "yellow_page_data", 301);
        sUriMatcher.addURI("com.android.contacts.app", "yellow_page_data/*", 302);
        sUriMatcher.addURI("com.android.contacts.app", "yellow_page/#/*", 303);
        sUriMatcher.addURI("com.android.contacts.app", "yellow_page/#", 303);
        sUriMatcher.addURI("com.android.contacts.app", "yellow_page_as_card/*", 305);
        sUriMatcher.addURI("com.android.contacts.app", "yellow_page/filter", 400);
        sUriMatcher.addURI("com.android.contacts.app", "yellow_page/filter/*", 400);
        sUriMatcher.addURI("com.android.contacts.app", "parse_fixed_phone_number", VTMCDataCache.MAXSIZE);
    }

    public boolean onCreate() {
        QueryUtil.init(getContext());
        Context context = getContext();
        moveDatabaseFromDe(context);
        this.mOpenHelper = ContactsAppDatabaseHelper.getInstance(context);
        return true;
    }

    public ContentProviderResult[] applyBatch(ArrayList<ContentProviderOperation> operations) throws OperationApplicationException {
        SQLiteDatabase sQLiteDatabase = null;
        try {
            sQLiteDatabase = this.mOpenHelper.getWritableDatabase();
            sQLiteDatabase.beginTransaction();
            int numOperations = operations.size();
            ContentProviderResult[] results = new ContentProviderResult[numOperations];
            for (int i = 0; i < numOperations; i++) {
                results[i] = ((ContentProviderOperation) operations.get(i)).apply(this, results, i);
            }
            sQLiteDatabase.setTransactionSuccessful();
            getContext().getContentResolver().notifyChange(AUTHORITY_URI, null);
            if (sQLiteDatabase != null) {
                try {
                    sQLiteDatabase.endTransaction();
                } catch (SQLiteException e) {
                }
            }
            return results;
        } catch (SQLiteException e2) {
            e2.printStackTrace();
            if (sQLiteDatabase != null) {
                try {
                    sQLiteDatabase.endTransaction();
                } catch (SQLiteException e3) {
                }
            }
            return null;
        } catch (Throwable th) {
            if (sQLiteDatabase != null) {
                try {
                    sQLiteDatabase.endTransaction();
                } catch (SQLiteException e4) {
                }
            }
        }
    }

    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        initCust();
        if (!EmuiFeatureManager.isChinaArea() && this.mCust != null && !this.mCust.addYellowPagesContactInList() && 100 != sUriMatcher.match(uri)) {
            return null;
        }
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        String groupby = uri.getQueryParameter("groupby");
        switch (sUriMatcher.match(uri)) {
            case LocationRequest.PRIORITY_HIGH_ACCURACY /*100*/:
                qb.setTables("speed_dial");
                qb.setProjectionMap(sSpeedDialProjectionMap);
                break;
            case 200:
                qb.setTables("number_mark");
                qb.setProjectionMap(sNumberMarkProjectionMap);
                break;
            case 201:
                if (this.mMarkManager == null) {
                    this.mMarkManager = new NumberMarkManager(getContext(), null);
                }
                String number = uri.getQueryParameter("number");
                Cursor cursor = this.mMarkManager.getNumberMarkCursor(Uri.decode(number), Uri.decode(uri.getQueryParameter("call_type")), uri.getQueryParameter("type"));
                if (HwLog.HWFLOW && !cursor.isClosed()) {
                    HwLog.i("ContactsAppProvider", "query NUMBER_MARK_SINGLE,return cursor count:" + cursor.getCount());
                }
                return cursor;
            case 202:
                qb.setTables("number_mark_extras");
                qb.setProjectionMap(sNumberMarkExtrasProjectionMap);
                break;
            case 203:
                if (selectionArgs == null || selectionArgs.length <= 0) {
                    return null;
                }
                String str = selectionArgs[0];
                String[] targetSelectionArgs = new String[]{new PhoneMatch(NumberMarkManager.standardizationPhoneNum(str, getContext())).getMatchPhone()};
                return this.mOpenHelper.getReadableDatabase().query("number_mark", null, "NUMBER=?", targetSelectionArgs, null, null, null);
            case 301:
                qb.setTables("yellow_page_view");
                qb.setProjectionMap(sYellowPageViewProjectionMap);
                break;
            case 302:
                qb.setTables("yellow_page_view");
                qb.setProjectionMap(sYellowPageViewProjectionMap);
                qb.appendWhere("number= '" + ((String) uri.getPathSegments().get(1)) + "'");
                break;
            case 303:
                qb.setTables("yellow_page");
                qb.setProjectionMap(sYellowPageProjectionMap);
                qb.appendWhere("_ID= " + ((String) uri.getPathSegments().get(1)));
                break;
            case 305:
                qb.setTables("yellow_page");
                qb.setProjectionMap(sContactsVCardProjectionMap);
                qb.appendWhere("_ID= " + ((String) uri.getPathSegments().get(1)));
                break;
            case 400:
                SQLiteDatabase db = this.mOpenHelper.getReadableDatabase();
                String search_type = "";
                String filterParam = "";
                String limit = getLimit(uri);
                int searchMode = 0;
                if (uri.getQueryParameter("search_type") != null) {
                    search_type = getQueryParameter(uri, "search_type");
                }
                if (uri.getPathSegments().size() <= 2 || !"search_yellowpage".equals(search_type) || projection == null) {
                    return null;
                }
                String[] newProjection = new String[(projection.length + 1)];
                System.arraycopy(projection, 0, newProjection, 0, projection.length);
                newProjection[projection.length] = "HW_SEARCH(?, ?, ?, dial_map, number) AS search_result";
                filterParam = uri.getLastPathSegment();
                if (!TextUtils.isEmpty(filterParam)) {
                    filterParam = filterParam.toLowerCase(Locale.getDefault());
                    int length = filterParam.length();
                    if (length > 0) {
                        if (filterParam.indexOf("*") == length - 1) {
                            filterParam = filterParam.substring(0, length - 1);
                            searchMode = 8192;
                        }
                    }
                }
                return db.query("yellow_page_view", newProjection, "search_result > 0 AND hot_points != -1", new String[]{String.valueOf(3), filterParam, String.valueOf(searchMode)}, null, null, "search_result," + sortOrder, limit);
            case VTMCDataCache.MAXSIZE /*500*/:
                if (projection == null || projection.length == 0) {
                    return null;
                }
                String noAreaNum = FixedPhoneNumberMatchUtils.parseFixedPhoneNumber(getContext(), projection[0]);
                MatrixCursor matrixCursor = new MatrixCursor(new String[]{"noAreaNum"});
                matrixCursor.addRow(new Object[]{noAreaNum});
                return matrixCursor;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
        Cursor c = qb.query(this.mOpenHelper.getReadableDatabase(), projection, selection, selectionArgs, groupby, null, sortOrder);
        if (c != null) {
            c.setNotificationUri(getContext().getContentResolver(), uri);
        }
        return c;
    }

    private String getLimit(Uri uri) {
        String limitParam = getQueryParameter(uri, "limit");
        if (limitParam == null) {
            return null;
        }
        try {
            int l = Integer.parseInt(limitParam);
            if (l >= 0) {
                return String.valueOf(l);
            }
            HwLog.w("ContactsAppProvider", "Invalid limit parameter: " + limitParam);
            return null;
        } catch (NumberFormatException e) {
            HwLog.w("ContactsAppProvider", "Invalid limit parameter: " + limitParam);
            return null;
        }
    }

    public static void insertNumberMarkInfo(Context context, ContentValues contentValues) {
        ContactsAppDatabaseHelper.getInstance(context).getWritableDatabase().insert("number_mark", null, contentValues);
        context.getContentResolver().notifyChange(NumberMark.CONTENT_URI, null);
    }

    public static void deleteNumberMarkInfo(Context context, String where, String[] selectionArgs) {
        ContactsAppDatabaseHelper.getInstance(context).getWritableDatabase().delete("number_mark", where, selectionArgs);
        context.getContentResolver().notifyChange(NumberMark.CONTENT_URI, null);
    }

    public static void updateNumberMarkInfo(Context context, ContentValues values, String where, String[] selectionArgs) {
        ContactsAppDatabaseHelper.getInstance(context).getWritableDatabase().update("number_mark", values, where, selectionArgs);
        context.getContentResolver().notifyChange(NumberMark.CONTENT_URI, null);
    }

    public Uri insert(Uri aUri, ContentValues aInitialValues) {
        long lRowId;
        int lMatch = sUriMatcher.match(aUri);
        SQLiteDatabase lDb = this.mOpenHelper.getWritableDatabase();
        boolean isBatchOnly = false;
        switch (lMatch) {
            case LocationRequest.PRIORITY_HIGH_ACCURACY /*100*/:
                try {
                    lRowId = lDb.replace("speed_dial", "phone_data_id", aInitialValues);
                    break;
                } catch (SQLiteException e) {
                    e.printStackTrace();
                    return null;
                }
            case 200:
                lRowId = lDb.insert("number_mark", null, aInitialValues);
                break;
            case 201:
                HwLog.d("ContactsAppProvider", "NUMBER_MARK_SINGLE");
                lRowId = lDb.insert("number_mark", null, aInitialValues);
                break;
            case 202:
                lRowId = lDb.insert("number_mark_extras", null, aInitialValues);
                break;
            case VTMCDataCache.MAX_EXPIREDTIME /*300*/:
                isBatchOnly = true;
                lRowId = lDb.insert("yellow_page", null, aInitialValues);
                break;
            case 301:
                isBatchOnly = true;
                lRowId = lDb.insert("yellow_page_phone", null, aInitialValues);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + aUri);
        }
        if (lRowId > 0) {
            if (!isBatchOnly) {
                getContext().getContentResolver().notifyChange(aUri, null);
            }
            return Uri.withAppendedPath(aUri, String.valueOf(lRowId));
        }
        throw new SQLException("Failed to insert row into " + aUri);
    }

    public int delete(Uri aUri, String aSelection, String[] aSelectionArgs) {
        int count;
        SQLiteDatabase db = this.mOpenHelper.getWritableDatabase();
        switch (sUriMatcher.match(aUri)) {
            case LocationRequest.PRIORITY_HIGH_ACCURACY /*100*/:
                count = db.delete("speed_dial", aSelection, aSelectionArgs);
                break;
            case 201:
                count = db.delete("number_mark", aSelection, aSelectionArgs);
                break;
            case 202:
                count = db.delete("number_mark_extras", aSelection, aSelectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + aUri);
        }
        getContext().getContentResolver().notifyChange(aUri, null);
        return count;
    }

    public int update(Uri aUri, ContentValues values, String aSelection, String[] aSelectionArgs) {
        int count;
        SQLiteDatabase db = this.mOpenHelper.getWritableDatabase();
        switch (sUriMatcher.match(aUri)) {
            case LocationRequest.PRIORITY_HIGH_ACCURACY /*100*/:
                count = db.update("speed_dial", values, aSelection, aSelectionArgs);
                break;
            case 201:
                count = db.update("number_mark", values, aSelection, aSelectionArgs);
                break;
            case 202:
                count = db.update("number_mark_extras", values, aSelection, aSelectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + aUri);
        }
        getContext().getContentResolver().notifyChange(aUri, null);
        return count;
    }

    public String getType(Uri uri) {
        switch (sUriMatcher.match(uri)) {
            case VTMCDataCache.MAX_EXPIREDTIME /*300*/:
            case 303:
                return "vnd.android.cursor.item/yellow_page";
            case 301:
            case 302:
                return "vnd.android.cursor.item/yellow_page_data";
            case 305:
                return "text/x-vcard";
            default:
                return null;
        }
    }

    public ParcelFileDescriptor openFile(Uri uri, String mode) throws FileNotFoundException {
        int match = sUriMatcher.match(uri);
        if (HwLog.HWDBG) {
            HwLog.d("ContactsAppProvider", "openFile: uri=" + uri + ", mode=" + mode);
        }
        return openFileHelper(uri, mode);
    }

    public AssetFileDescriptor openAssetFile(Uri uri, String mode) throws FileNotFoundException {
        boolean success = false;
        try {
            AssetFileDescriptor ret = openAssetFileInner(uri, mode);
            success = true;
            return ret;
        } finally {
            if (HwLog.HWDBG) {
                HwLog.v("ContactsAppProvider", "openAssetFile uri=" + uri + " mode=" + mode + " success=" + success);
            }
        }
    }

    private AssetFileDescriptor openAssetFileInner(Uri uri, String mode) throws FileNotFoundException {
        switch (sUriMatcher.match(uri)) {
            case 301:
            case 302:
                File file = new File(uri.getQueryParameter("path"));
                return makeAssetFileDescriptor(ParcelFileDescriptor.open(file, 268435456), file.length());
            case 305:
                ByteArrayOutputStream localStream = new ByteArrayOutputStream();
                outputYellowPageAsVCard(uri, localStream);
                return buildAssetFileDescriptor(localStream);
            default:
                return null;
        }
    }

    private void outputYellowPageAsVCard(Uri uri, OutputStream stream) {
        JSONException jse;
        IOException e;
        Throwable th;
        if (uri != null) {
            int vcardconfig = -1073741823;
            if (uri.getBooleanQueryParameter("no_photo", false)) {
                vcardconfig = -1065353215;
            }
            VCardComposer composer = new VCardComposer(getContext(), vcardconfig, false);
            String ypid = uri.getLastPathSegment();
            if (!TextUtils.isEmpty(ypid)) {
                Map<String, List<ContentValues>> contentValuesListMap = new HashMap();
                Cursor cursor = query(YELLOW_PAGE_URI.buildUpon().appendEncodedPath(ypid).build(), new String[]{MapTilsCacheAndResManager.AUTONAVI_DATA_PATH}, null, null, null);
                if (cursor != null) {
                    Writer writer = null;
                    try {
                        if (cursor.moveToFirst()) {
                            Writer bufferedWriter = new BufferedWriter(new OutputStreamWriter(stream, Charset.forName("UTF-8")));
                            try {
                                YellowPageContactUtil.buildContentValuesList(new JSONObject(cursor.getString(0)), contentValuesListMap);
                                bufferedWriter.write(composer.buildVCard(contentValuesListMap));
                                writer = bufferedWriter;
                            } catch (JSONException e2) {
                                jse = e2;
                                writer = bufferedWriter;
                                HwLog.e("ContactsAppProvider", "JSONException: " + jse);
                                cursor.close();
                                if (writer != null) {
                                    try {
                                        writer.close();
                                    } catch (IOException e3) {
                                        HwLog.w("ContactsAppProvider", "IOException during closing output stream: " + e3);
                                    }
                                }
                            } catch (IOException e4) {
                                e3 = e4;
                                writer = bufferedWriter;
                                try {
                                    HwLog.e("ContactsAppProvider", "IOException: " + e3);
                                    cursor.close();
                                    if (writer != null) {
                                        try {
                                            writer.close();
                                        } catch (IOException e32) {
                                            HwLog.w("ContactsAppProvider", "IOException during closing output stream: " + e32);
                                        }
                                    }
                                } catch (Throwable th2) {
                                    th = th2;
                                    cursor.close();
                                    if (writer != null) {
                                        try {
                                            writer.close();
                                        } catch (IOException e322) {
                                            HwLog.w("ContactsAppProvider", "IOException during closing output stream: " + e322);
                                        }
                                    }
                                    throw th;
                                }
                            } catch (Throwable th3) {
                                th = th3;
                                writer = bufferedWriter;
                                cursor.close();
                                if (writer != null) {
                                    writer.close();
                                }
                                throw th;
                            }
                        }
                        cursor.close();
                        if (writer != null) {
                            try {
                                writer.close();
                            } catch (IOException e3222) {
                                HwLog.w("ContactsAppProvider", "IOException during closing output stream: " + e3222);
                            }
                        }
                    } catch (JSONException e5) {
                        jse = e5;
                        HwLog.e("ContactsAppProvider", "JSONException: " + jse);
                        cursor.close();
                        if (writer != null) {
                            writer.close();
                        }
                    } catch (IOException e6) {
                        e3222 = e6;
                        HwLog.e("ContactsAppProvider", "IOException: " + e3222);
                        cursor.close();
                        if (writer != null) {
                            writer.close();
                        }
                    }
                }
            }
        }
    }

    private AssetFileDescriptor buildAssetFileDescriptor(ByteArrayOutputStream stream) {
        try {
            stream.flush();
            byte[] byteData = stream.toByteArray();
            return makeAssetFileDescriptor(ParcelFileDescriptor.fromData(byteData, "yellowPageAssetFile"), (long) byteData.length);
        } catch (IOException e) {
            HwLog.w("ContactsAppProvider", "Problem writing stream into an ParcelFileDescriptor: " + e.toString());
            return null;
        }
    }

    private AssetFileDescriptor makeAssetFileDescriptor(ParcelFileDescriptor fd, long length) {
        return fd != null ? new AssetFileDescriptor(fd, 0, length) : null;
    }

    public static String getQueryParameter(Uri uri, String parameter) {
        String query = uri.getEncodedQuery();
        if (query == null) {
            return null;
        }
        String value;
        int queryLength = query.length();
        int parameterLength = parameter.length();
        int index = 0;
        while (true) {
            index = query.indexOf(parameter, index);
            if (index == -1) {
                return null;
            }
            if (index > 0) {
                char prevChar = query.charAt(index - 1);
                if (!(prevChar == '?' || prevChar == '&')) {
                    index += parameterLength;
                }
            }
            index += parameterLength;
            if (queryLength == index) {
                return null;
            }
            if (query.charAt(index) == '=') {
                break;
            }
        }
        index++;
        int ampIndex = query.indexOf(38, index);
        if (ampIndex == -1) {
            value = query.substring(index);
        } else {
            value = query.substring(index, ampIndex);
        }
        return Uri.decode(value);
    }

    private void initCust() {
        if (EmuiFeatureManager.isProductCustFeatureEnable() && this.mCust == null) {
            this.mCust = (HwCustContactsAppProvider) HwCustUtils.createObj(HwCustContactsAppProvider.class, new Object[0]);
        }
    }

    private void moveDatabaseFromDe(final Context context) {
        ContactsThreadPool.getInstance().execute(new Runnable() {
            public void run() {
                boolean databaseExists = new File("data/user_de/0/com.android.contacts/databases/contacts_app.db").exists();
                if (!context.createDeviceProtectedStorageContext().moveDatabaseFrom(context, "contacts_app.db")) {
                    HwLog.e("ContactsAppProvider", "Failed to move database");
                }
                if (databaseExists) {
                    HwLog.d("ContactsAppProvider", "database is exists,not required reset");
                    return;
                }
                if (ContactsAppProvider.this.mOpenHelper != null) {
                    try {
                        ContactsAppProvider.this.mOpenHelper.close();
                    } catch (IllegalStateException e) {
                        HwLog.w("ContactsAppProvider", "Closed during initialization");
                    }
                }
                ContactsAppProvider.this.mOpenHelper = ContactsAppDatabaseHelper.resetInstance(context);
            }
        });
    }
}
