package com.huawei.gallery.provider;

import android.content.ContentProvider;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.OperationApplicationException;
import android.content.UriMatcher;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.text.TextUtils;
import com.amap.api.services.core.AMapException;
import com.android.gallery3d.app.GalleryApp;
import com.android.gallery3d.common.Utils;
import com.android.gallery3d.data.DataManager;
import com.android.gallery3d.data.MediaSet;
import com.android.gallery3d.data.Path;
import com.android.gallery3d.util.InstantShareUtils;
import com.android.gallery3d.util.TraceController;
import com.huawei.gallery.media.GalleryMedia;
import com.huawei.gallery.media.LocalRecycledFile;
import com.huawei.gallery.media.database.CloudRecycleTableOperateHelper;
import com.huawei.gallery.media.database.CloudTableOperateHelper;
import com.huawei.gallery.photoshare.utils.PhotoShareUtils;
import com.huawei.gallery.recycle.utils.CloudRecycleUtils;
import com.huawei.gallery.recycle.utils.RecycleUtils;
import com.huawei.gallery.storage.GalleryStorage;
import com.huawei.gallery.storage.GalleryStorageManager;
import com.huawei.gallery.util.MyPrinter;
import edu.umd.cs.findbugs.annotations.SuppressWarnings;
import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;
import tmsdk.common.module.intelli_sms.SmsCheckResult;

public class GalleryProvider extends ContentProvider {
    public static final Uri BASE_URI = Uri.parse("content://com.huawei.gallery.provider/");
    private static MyPrinter LOG = new MyPrinter("GalleryProvider");
    private static final UriMatcher URI_MATCHER = new UriMatcher(-1);
    private CloudTableOperateHelper mCloudTableOperateHelper;
    private GalleryDBHelper mDataBase;
    private int mDataCount = 0;
    private final ReentrantLock mInsertGalleryMediaLock = new ReentrantLock();
    private boolean mIsSyncingData = false;

    private static final class GetTableAndWhereOutParameter {
        public String table;
        public String where;

        private GetTableAndWhereOutParameter() {
        }
    }

    static {
        URI_MATCHER.addURI("com.huawei.gallery.provider", "none/sycnner/#", 22);
        URI_MATCHER.addURI("com.huawei.gallery.provider", "merge/gallery_media", 24);
        URI_MATCHER.addURI("com.huawei.gallery.provider", "merge/*", 5);
        URI_MATCHER.addURI("com.huawei.gallery.provider", "merge/*/#", 6);
        URI_MATCHER.addURI("com.huawei.gallery.provider", "cloud_album", 10);
        URI_MATCHER.addURI("com.huawei.gallery.provider", "cloud_file", 11);
        URI_MATCHER.addURI("com.huawei.gallery.provider", "auto_upload_album", 12);
        URI_MATCHER.addURI("com.huawei.gallery.provider", "dirty/*", 13);
        URI_MATCHER.addURI("com.huawei.gallery.provider", "local_album", 14);
        URI_MATCHER.addURI("com.huawei.gallery.provider", "query_wait_upload_count", 15);
        URI_MATCHER.addURI("com.huawei.gallery.provider", "clearData", 16);
        URI_MATCHER.addURI("com.huawei.gallery.provider", "rename/*", 17);
        URI_MATCHER.addURI("com.huawei.gallery.provider", "deletedAlbum", 18);
        URI_MATCHER.addURI("com.huawei.gallery.provider", "query_empty", 20);
        URI_MATCHER.addURI("com.huawei.gallery.provider", "fversioninfo", 21);
        URI_MATCHER.addURI("com.huawei.gallery.provider", "instantshare/*", 23);
        URI_MATCHER.addURI("com.huawei.gallery.provider", "cloud_recycled_file", 28);
        URI_MATCHER.addURI("com.huawei.gallery.provider", "recycle_operation", 29);
        URI_MATCHER.addURI("com.huawei.gallery.provider", "general_cloud_file", 30);
        URI_MATCHER.addURI("com.huawei.gallery.provider", "restore_file_not_exists", 31);
    }

    public boolean onCreate() {
        return true;
    }

    private synchronized GalleryDBHelper getDataBase() {
        if (this.mDataBase == null) {
            TraceController.beginSection("GalleryProvider.getDataBase");
            this.mDataBase = new GalleryDBHelper(getContext());
            TraceController.endSection();
        }
        return this.mDataBase;
    }

    private synchronized CloudTableOperateHelper getCloudTableOperateHelper() {
        if (this.mCloudTableOperateHelper == null) {
            TraceController.beginSection("GalleryProvider.getCloudTableOperateHelper");
            this.mCloudTableOperateHelper = new CloudTableOperateHelper(getContext().getContentResolver());
            TraceController.endSection();
        }
        return this.mCloudTableOperateHelper;
    }

    private void notifyChange(Uri uri, ContentObserver observer, int count) {
        if (uri != null) {
            if (!"1".equals(uri.getQueryParameter("nonotify"))) {
                if (this.mIsSyncingData) {
                    this.mDataCount += count;
                    if (this.mDataCount >= SmsCheckResult.ESCT_200) {
                        LOG.d("notify mIsSyncingData " + this.mIsSyncingData + ", mDataCount " + this.mDataCount);
                        this.mDataCount = 0;
                    } else {
                        return;
                    }
                }
                getContext().getContentResolver().notifyChange(uri, observer);
            }
        }
    }

    public int delete(Uri uri, String userWhere, String[] whereArgs) {
        int count = 0;
        int match = URI_MATCHER.match(uri);
        GetTableAndWhereOutParameter parameter = getTableAndWhere(uri, match, userWhere);
        switch (match) {
            case 5:
            case 6:
            case 12:
            case 21:
            case 24:
                count = getDataBase().delete(parameter.table, parameter.where, whereArgs);
                break;
            case 10:
                LOG.d("Recycle_deleteCloudAlbum " + parameter.where + " : " + whereArgs[0]);
                PhotoShareUtils.initialCloudAlbumBucketId();
                count = getCloudTableOperateHelper().deleteCloudAlbum(getDataBase().getWritableDatabase(), parameter.where, whereArgs);
                break;
            case 11:
            case 30:
                LOG.d("Recycle_deleteCloudFile[" + match + "] " + parameter.where + " : " + whereArgs[0]);
                count = getCloudTableOperateHelper().deleteCloudFile(getContext(), getDataBase().getWritableDatabase(), parameter.where, whereArgs, false);
                break;
            case 16:
                PhotoShareUtils.setLogOnAccount(null);
                count = getCloudTableOperateHelper().clearData(getDataBase().getWritableDatabase());
                PhotoShareUtils.clearDeletedPhoto();
                PhotoShareUtils.setHasNeverSynchronizedCloudData(true);
                break;
            case AMapException.ERROR_CODE_UNKNOW_SERVICE /*28*/:
                LOG.d("Recycle_deleteRecycleFile " + parameter.where + ":" + whereArgs[0]);
                count = CloudRecycleTableOperateHelper.deleteCloudRecycleFileTable(getDataBase().getWritableDatabase(), parameter.table, parameter.where, whereArgs);
                break;
            case 31:
                LOG.d("Recycle_recoverCloudFileFail " + parameter.where + ":" + whereArgs[0]);
                count = getCloudTableOperateHelper().deleteCloudFile(getContext(), getDataBase().getWritableDatabase(), parameter.where, whereArgs, true);
                break;
        }
        if (match == 12 || match == 16) {
            resetGallerySource();
        }
        if (count > 0) {
            notifyChange(uri, null, count);
        }
        return count;
    }

    public String getType(Uri uri) {
        return null;
    }

    @SuppressWarnings({"UL_UNRELEASED_LOCK"})
    public Uri insert(Uri uri, ContentValues initialValues) {
        Uri newUri = null;
        SQLiteDatabase db = getDataBase().getWritableDatabase();
        int match = URI_MATCHER.match(uri);
        if (22 == match) {
            LOG.d("sync uri: " + uri);
            this.mIsSyncingData = "1".equals(uri.getLastPathSegment());
            if (!(this.mIsSyncingData || this.mDataCount == 0)) {
                this.mDataCount = 0;
                notifyChange(BASE_URI, null, 1);
            }
            return null;
        }
        boolean needLock = needLockGalleryMediaTable(match);
        if (needLock) {
            this.mInsertGalleryMediaLock.lock();
        }
        try {
            long rowId = insertInternal(uri, db, match, initialValues);
            if (rowId > 0) {
                newUri = ContentUris.withAppendedId(uri, rowId);
            }
            if (needLock) {
                this.mInsertGalleryMediaLock.unlock();
            }
            if (newUri != null) {
                notifyChange(newUri, null, 1);
            }
            return newUri;
        } catch (Throwable th) {
            if (needLock) {
                this.mInsertGalleryMediaLock.unlock();
            }
        }
    }

    @SuppressWarnings({"UL_UNRELEASED_LOCK"})
    public int bulkInsert(Uri uri, ContentValues[] values) {
        if (values == null || values.length == 0) {
            return 0;
        }
        SQLiteDatabase db = getDataBase().getWritableDatabase();
        int match = URI_MATCHER.match(uri);
        long startTime = System.currentTimeMillis();
        int count = values.length;
        boolean needLock = needLockGalleryMediaTable(match);
        if (needLock) {
            this.mInsertGalleryMediaLock.lock();
        }
        db.beginTransaction();
        int i = 0;
        while (i < count) {
            try {
                insertInternal(uri, db, match, values[i]);
                i++;
            } catch (Throwable th) {
                db.endTransaction();
                if (needLock) {
                    this.mInsertGalleryMediaLock.unlock();
                }
                LOG.d("bulk insert: " + count + ", time cost: " + (System.currentTimeMillis() - startTime));
            }
        }
        db.setTransactionSuccessful();
        db.endTransaction();
        if (needLock) {
            this.mInsertGalleryMediaLock.unlock();
        }
        LOG.d("bulk insert: " + count + ", time cost: " + (System.currentTimeMillis() - startTime));
        notifyChange(uri, null, count);
        return values.length;
    }

    private boolean needLockGalleryMediaTable(int match) {
        if (match == 11 || match == 24) {
            return true;
        }
        return false;
    }

    private long insertInternal(Uri uri, SQLiteDatabase db, int match, ContentValues values) {
        long rowId;
        switch (match) {
            case 5:
                return db.insert((String) uri.getPathSegments().get(1), null, values);
            case 10:
                LOG.d("Recycle_insertCloudAlbum " + values);
                return getCloudTableOperateHelper().insertCloudAlbumTable(db, values);
            case 11:
            case 30:
                getCloudTableOperateHelper().insertCloudFileTable(db, values, getContext(), true);
                LOG.d("Recycle_insertCloudFile[" + match + "] " + RecycleUtils.getPrintableContentValues(values));
                return 0;
            case 12:
                LOG.d("Recycle_insertAutoUploadAlbumTable " + values);
                rowId = getCloudTableOperateHelper().insertAutoUploadAlbumTable(db, values);
                resetGallerySource();
                return rowId;
            case 21:
                db.insert("fversioninfo", null, values);
                return 0;
            case 24:
                rowId = insertGalleryMedia(db, values);
                LOG.d("Recycle_insertGalleryMedia " + RecycleUtils.getPrintableContentValues(values));
                return rowId;
            case AMapException.ERROR_CODE_UNKNOW_SERVICE /*28*/:
                rowId = db.insert("cloud_recycled_file", null, values);
                LOG.d("Recycle_insertRecycleFile " + RecycleUtils.getPrintableContentValues(values));
                return rowId;
            default:
                throw new UnsupportedOperationException("Invalid URI " + uri);
        }
    }

    public Cursor query(Uri uri, String[] projectionIn, String selection, String[] selectionArgs, String sort) {
        int table = URI_MATCHER.match(uri);
        List<String> prependArgs = new ArrayList();
        SQLiteDatabase db = getDataBase().getReadableDatabase();
        if (db == null) {
            return null;
        }
        Cursor c;
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        String limit = uri.getQueryParameter("limit");
        if (uri.getQueryParameter("distinct") != null) {
            qb.setDistinct(true);
        }
        switch (table) {
            case 5:
            case 24:
                qb.setTables((String) uri.getPathSegments().get(1));
                break;
            case 6:
                qb.setTables((String) uri.getPathSegments().get(1));
                qb.appendWhere("_id=?");
                prependArgs.add((String) uri.getPathSegments().get(2));
                break;
            case 10:
                qb.setTables("cloud_album");
                break;
            case 11:
                qb.setTables("cloud_file");
                break;
            case 12:
                qb.setTables("auto_upload_album");
                break;
            case 13:
            case 14:
            case 15:
            case 17:
            case 18:
            case 20:
                break;
            case 21:
                qb.setTables("fversioninfo");
                break;
            case 23:
                return InstantShareUtils.query(uri, projectionIn, selection, selectionArgs, sort);
            case AMapException.ERROR_CODE_UNKNOW_SERVICE /*28*/:
                qb.setTables("cloud_recycled_file");
                break;
            case 30:
                qb.setTables("general_cloud_file");
                if (!TextUtils.isEmpty(selection)) {
                    selection = selection.concat(" AND ((recycleFlag != -1 AND recycleFlag != 2) OR recycleFlag IS NULL)");
                    break;
                }
                selection = "1 = 1".concat(" AND ((recycleFlag != -1 AND recycleFlag != 2) OR recycleFlag IS NULL)");
                break;
            default:
                throw new IllegalStateException("Unknown URL: " + uri.toString());
        }
        if (table == 14) {
            c = getCloudTableOperateHelper().queryLocalAlbum(db, getContext());
        } else if (table == 13) {
            c = getCloudTableOperateHelper().queryGalleryMedia(db, projectionIn, (String) uri.getPathSegments().get(1));
        } else if (table == 15) {
            c = getCloudTableOperateHelper().queryWaitToUploadCount(db);
        } else if (table == 17) {
            c = getCloudTableOperateHelper().queryRenamedFiles(db, projectionIn, (String) uri.getPathSegments().get(1));
        } else if (table == 18) {
            c = getCloudTableOperateHelper().queryDeletedAlbum(db);
        } else if (table == 20) {
            c = getCloudTableOperateHelper().queryDefaultAlbumIsEmpty(db);
        } else {
            c = qb.query(db, projectionIn, selection, combine(prependArgs, selectionArgs), null, null, sort, limit);
        }
        if (c != null) {
            c.setNotificationUri(getContext().getContentResolver(), uri);
        }
        return c;
    }

    private String[] combine(List<String> prepend, String[] userArgs) {
        int userSize = 0;
        int preSize = prepend.size();
        if (preSize == 0) {
            return userArgs;
        }
        int i;
        if (userArgs != null) {
            userSize = userArgs.length;
        }
        String[] combined = new String[(preSize + userSize)];
        for (i = 0; i < preSize; i++) {
            combined[i] = (String) prepend.get(i);
        }
        for (i = 0; i < userSize; i++) {
            combined[preSize + i] = userArgs[i];
        }
        return combined;
    }

    private void resetGallerySource() {
        PhotoShareUtils.initialAutoUploadAlbumBucketId();
        MediaSet timeGroupAlbum = (MediaSet) Path.fromString("/gallery/album/timebucket").getObject();
        if (timeGroupAlbum != null) {
            timeGroupAlbum.reset();
        }
        getDataManager().notifyChange();
    }

    private DataManager getDataManager() {
        return ((GalleryApp) getContext().getApplicationContext()).getDataManager();
    }

    public ContentProviderResult[] applyBatch(ArrayList<ContentProviderOperation> operations) throws OperationApplicationException {
        SQLiteDatabase db = getDataBase().getWritableDatabase();
        ContentProviderResult[] contentProviderResultArr = null;
        db.beginTransaction();
        try {
            contentProviderResultArr = super.applyBatch(operations);
            db.setTransactionSuccessful();
            return contentProviderResultArr;
        } finally {
            db.endTransaction();
        }
    }

    private boolean isRecycleOperation(int match) {
        return match == 29;
    }

    public int update(Uri uri, ContentValues initialValues, String userWhere, String[] whereArgs) {
        int match = URI_MATCHER.match(uri);
        SQLiteDatabase db = getDataBase().getWritableDatabase();
        if (isRecycleOperation(match)) {
            RecycleUtils.executeDbOperation(db, getDataManager().getMediaObject((String) initialValues.get("opera_params")), initialValues);
            ContentResolver resolver = getContext().getContentResolver();
            resolver.notifyChange(LocalRecycledFile.URI, null);
            resolver.notifyChange(CloudRecycleUtils.CLOUD_RECYCLED_FILE_TABLE_URI, null);
            resolver.notifyChange(GalleryMedia.URI, null);
            return 0;
        }
        int count;
        GetTableAndWhereOutParameter parameter = getTableAndWhere(uri, match, userWhere);
        if (match == 11 || match == 30) {
            count = getCloudTableOperateHelper().updateCloudFileTable(db, initialValues, parameter.where, whereArgs, match == 30);
        } else if (match == 28) {
            count = CloudRecycleUtils.updateCloudRecycleFileTable(getContext(), db, initialValues, parameter.where, whereArgs);
        } else {
            count = db.update(parameter.table, initialValues, parameter.where, whereArgs);
        }
        MyPrinter myPrinter = LOG;
        StringBuilder append = new StringBuilder().append("Recycle_update ").append(parameter.table).append(" ").append(RecycleUtils.getPrintableContentValues(initialValues)).append("  to ").append(parameter.where).append(":");
        String str = (whereArgs == null || whereArgs.length <= 0) ? "" : whereArgs[0];
        myPrinter.d(append.append(str).toString());
        if (count > 0 && !db.inTransaction()) {
            notifyChange(uri, null, count);
        }
        return count;
    }

    private GetTableAndWhereOutParameter getTableAndWhere(Uri uri, int match, String userWhere) {
        List<String> segements = uri.getPathSegments();
        GetTableAndWhereOutParameter out = new GetTableAndWhereOutParameter();
        String where = null;
        switch (match) {
            case 5:
            case 24:
                out.table = (String) segements.get(1);
                break;
            case 6:
                out.table = (String) segements.get(1);
                where = "_id = " + ((String) segements.get(2));
                break;
            case 10:
                out.table = "cloud_album";
                break;
            case 11:
                out.table = "cloud_file";
                break;
            case 12:
                out.table = "auto_upload_album";
                break;
            case 16:
                out.table = "";
                break;
            case 21:
                out.table = "fversioninfo";
                break;
            case AMapException.ERROR_CODE_UNKNOW_SERVICE /*28*/:
                out.table = "cloud_recycled_file";
                break;
            case 30:
                out.table = "general_cloud_file";
                break;
            case 31:
                out.table = "restore_file_not_exists";
                break;
            default:
                throw new UnsupportedOperationException("Unknown or unsupported URL: " + uri.toString());
        }
        if (TextUtils.isEmpty(userWhere)) {
            out.where = where;
        } else if (TextUtils.isEmpty(where)) {
            out.where = userWhere;
        } else {
            out.where = where + " AND (" + userWhere + ")";
        }
        return out;
    }

    public ParcelFileDescriptor openFile(Uri uri, String mode) throws FileNotFoundException {
        int modeBits = ParcelFileDescriptor.parseMode(mode);
        File file = queryForDataFile(uri);
        checkAccess(uri, file, modeBits);
        if ((134217728 & modeBits) != 0) {
            ensureFileExists(uri, file.getPath());
        }
        if (modeBits == 268435456) {
            file = Environment.maybeTranslateEmulatedPathToInternal(file);
        }
        return ParcelFileDescriptor.open(file, modeBits);
    }

    private boolean ensureFileExists(Uri uri, String path) {
        File file = new File(path);
        if (file.exists()) {
            return true;
        }
        try {
            checkAccess(uri, file, 939524096);
            int secondSlash = path.indexOf(47, 1);
            if (secondSlash < 1 || !new File(path.substring(0, secondSlash)).exists()) {
                return false;
            }
            LOG.w("file.getParentFile().mkdirs() " + file.getParentFile().mkdirs());
            try {
                return file.createNewFile();
            } catch (IOException ioe) {
                LOG.e("File creation failed", ioe);
                return false;
            }
        } catch (FileNotFoundException e) {
            return false;
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private File queryForDataFile(Uri uri) throws FileNotFoundException {
        Closeable cursor = query(uri, new String[]{"_data"}, null, null, null);
        if (cursor == null) {
            throw new FileNotFoundException("Missing cursor for " + uri);
        }
        try {
            switch (cursor.getCount()) {
                case 0:
                    throw new FileNotFoundException("No entry for " + uri);
                case 1:
                    if (cursor.moveToFirst()) {
                        String data = cursor.getString(0);
                        if (data != null) {
                            File file = new File(data);
                            break;
                        }
                        throw new FileNotFoundException("Null path for " + uri);
                    }
                    throw new FileNotFoundException("Unable to read entry for " + uri);
                default:
                    throw new FileNotFoundException("Multiple items at " + uri);
            }
        } finally {
            Utils.closeSilently(cursor);
        }
        Utils.closeSilently(cursor);
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private long insertGalleryMedia(SQLiteDatabase db, ContentValues initialValues) {
        if (initialValues == null || initialValues.size() == 0) {
            return -1;
        }
        long size;
        long resultValue;
        String bucketId = initialValues.getAsString("bucket_id");
        Long longSize = initialValues.getAsLong("_size");
        if (longSize == null) {
            size = 0;
        } else {
            size = longSize.longValue();
        }
        String hash = initialValues.getAsString("hash");
        String displayName = initialValues.getAsString("_display_name");
        int galleryId = -1;
        boolean findCloudData = false;
        boolean findRelativeData = false;
        if (!TextUtils.isEmpty(PhotoShareUtils.getCloudAlbumIdByBucketId(bucketId))) {
            String selection;
            String[] selectionArgs;
            if (size == 0) {
                selection = "cloud_bucket_id = ? and _display_name = ? and relative_cloud_media_id = -1";
                selectionArgs = new String[]{albumID, displayName};
            } else {
                selection = "cloud_bucket_id = ? and hash = ? and relative_cloud_media_id = -1" + (PhotoShareUtils.isGUIDSupport() ? " and _display_name = ?" : "");
                selectionArgs = PhotoShareUtils.isGUIDSupport() ? new String[]{albumID, hash, displayName} : new String[]{albumID, hash};
            }
            try {
                Closeable cursor = db.query("gallery_media", new String[]{"_id", "local_media_id", "cloud_media_id"}, selection, selectionArgs, null, null, null);
                if (cursor != null && cursor.moveToNext()) {
                    galleryId = cursor.getInt(0);
                    if (cursor.getInt(1) != -1) {
                        initialValues.put("relative_cloud_media_id", Integer.valueOf(cursor.getInt(2)));
                        findRelativeData = true;
                    } else {
                        findCloudData = true;
                    }
                }
                Utils.closeSilently(cursor);
            } catch (Exception e) {
                LOG.d("queryOnlyCloudData exception " + e.toString());
            } catch (Throwable th) {
                Utils.closeSilently(null);
            }
        }
        if (findCloudData) {
            resultValue = (long) galleryId;
            db.update("gallery_media", initialValues, "_id = ?", new String[]{String.valueOf(galleryId)});
            LOG.d("update galleryMedia " + galleryId);
        } else {
            resultValue = db.insert("gallery_media", null, initialValues);
            LOG.d("insert galleryMedia " + resultValue);
            if (PhotoShareUtils.isGUIDSupport() && !findRelativeData) {
                PhotoShareUtils.checkAutoUpload(-1, initialValues.getAsString("_data"), bucketId, initialValues.getAsString("bucket_relative_path"), hash, initialValues.getAsInteger("media_type").intValue(), size == 0);
            }
        }
        return resultValue;
    }

    private void checkAccess(Uri uri, File file, int modeBits) throws FileNotFoundException {
        boolean isWrite = (536870912 & modeBits) != 0;
        try {
            String path = file.getCanonicalPath();
            Context c = getContext();
            boolean readGranted = false;
            boolean writeGranted = false;
            if (isWrite) {
                writeGranted = c.checkCallingOrSelfUriPermission(uri, 2) == 0;
            } else {
                readGranted = c.checkCallingOrSelfUriPermission(uri, 1) == 0;
            }
            GalleryStorage galleryStorage = GalleryStorageManager.getInstance().getInnerGalleryStorage();
            if (galleryStorage != null) {
                String primaryPath = galleryStorage.getPath();
                if (primaryPath == null || !path.startsWith(primaryPath)) {
                    if (!readGranted && c.checkCallingOrSelfPermission("android.permission.WRITE_MEDIA_STORAGE") == -1) {
                        c.enforceCallingOrSelfPermission("android.permission.READ_EXTERNAL_STORAGE", "External path: " + path);
                    }
                    if (isWrite && c.checkCallingOrSelfUriPermission(uri, 2) != 0) {
                        c.enforceCallingOrSelfPermission("android.permission.WRITE_MEDIA_STORAGE", "External path: " + path);
                    }
                } else if (isWrite) {
                    if (!writeGranted) {
                        c.enforceCallingOrSelfPermission("android.permission.WRITE_EXTERNAL_STORAGE", "External path: " + path);
                    }
                } else if (!readGranted) {
                    c.enforceCallingOrSelfPermission("android.permission.READ_EXTERNAL_STORAGE", "External path: " + path);
                }
            }
        } catch (IOException e) {
            throw new IllegalArgumentException("Unable to resolve canonical path for " + file, e);
        }
    }
}
