package com.android.gallery3d.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.android.gallery3d.app.GalleryApp;
import com.android.gallery3d.common.LruCache;
import com.android.gallery3d.common.Utils;
import com.android.gallery3d.util.BaseJob;
import com.android.gallery3d.util.Future;
import com.android.gallery3d.util.FutureListener;
import com.android.gallery3d.util.GalleryLog;
import com.android.gallery3d.util.ThreadPool.CancelListener;
import com.android.gallery3d.util.ThreadPool.JobContext;
import com.autonavi.amap.mapcore.MapTilsCacheAndResManager;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;

public class DownloadCache {
    private static final String FREESPACE_ORDER_BY = String.format("%s ASC", new Object[]{"last_access"});
    private static final String[] FREESPACE_PROJECTION = new String[]{"_id", "_data", "content_url", "_size"};
    private static final String[] QUERY_PROJECTION = new String[]{"_id", "_data"};
    private static final String[] SUM_PROJECTION;
    private static final String TABLE_NAME = DownloadEntry.SCHEMA.getTableName();
    private static final String WHERE_HASH_AND_URL = String.format("%s = ? AND %s = ?", new Object[]{"hash_code", "content_url"});
    private final GalleryApp mApplication;
    private final long mCapacity;
    private final SQLiteDatabase mDatabase;
    private final LruCache<String, Entry> mEntryMap = new LruCache(4);
    private boolean mInitialized = false;
    private final File mRoot;
    private final HashMap<String, DownloadTask> mTaskMap = new HashMap();
    private long mTotalBytes = 0;

    private final class DatabaseHelper extends SQLiteOpenHelper {
        public DatabaseHelper(Context context) {
            super(context, "download.db", null, 2);
        }

        public void onCreate(SQLiteDatabase db) {
            DownloadEntry.SCHEMA.createTables(db);
            for (File file : DownloadCache.this.mRoot.listFiles()) {
                if (!file.delete()) {
                    GalleryLog.w("DownloadCache", "fail to remove: " + file.getAbsolutePath());
                }
            }
        }

        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            DownloadEntry.SCHEMA.dropTables(db);
            onCreate(db);
        }
    }

    private class DownloadTask extends BaseJob<File> implements FutureListener<File> {
        private final String mDir;
        private Future<File> mFuture;
        private final String mName;
        private HashSet<TaskProxy> mProxySet = new HashSet();
        private final String mUrl;

        public DownloadTask(String url, String dir, String name) {
            this.mUrl = (String) Utils.checkNotNull(url);
            this.mDir = dir;
            this.mName = name;
        }

        public void removeProxy(TaskProxy proxy) {
            synchronized (DownloadCache.this.mTaskMap) {
                Utils.assertTrue(this.mProxySet.remove(proxy));
                if (this.mProxySet.isEmpty()) {
                    this.mFuture.cancel();
                    DownloadCache.this.mTaskMap.remove(this.mUrl);
                }
            }
        }

        public void addProxy(TaskProxy proxy) {
            proxy.mTask = this;
            this.mProxySet.add(proxy);
        }

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void onFutureDone(Future<File> future) {
            Throwable th;
            File file = (File) future.get();
            long id = 0;
            if (file != null) {
                id = DownloadCache.this.insertEntry(this.mUrl, file);
            }
            if (future.isCancelled()) {
                Utils.assertTrue(this.mProxySet.isEmpty());
                return;
            }
            synchronized (DownloadCache.this.mTaskMap) {
                Entry entry = null;
                synchronized (DownloadCache.this.mEntryMap) {
                    if (file != null) {
                        try {
                            Entry entry2 = new Entry(id, file);
                            try {
                                Utils.assertTrue(DownloadCache.this.mEntryMap.put(this.mUrl, entry2) == null);
                                entry = entry2;
                            } catch (Throwable th2) {
                                th = th2;
                                throw th;
                            }
                        } catch (Throwable th3) {
                            th = th3;
                            throw th;
                        }
                    }
                }
            }
        }

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public File run(JobContext jc) {
            File file = null;
            try {
                URL url = new URL(this.mUrl);
                if (this.mDir == null && this.mName == null) {
                    file = File.createTempFile(MapTilsCacheAndResManager.MAP_CACHE_PATH_NAME, ".tmp", DownloadCache.this.mRoot);
                } else {
                    file = createTempFile(this.mDir, this.mName, DownloadCache.this.mRoot);
                }
                boolean downloaded = DownloadUtils.requestDownload(jc, url, file);
                jc.setMode(0);
                if (downloaded) {
                    jc.setMode(0);
                    return file;
                }
                jc.setMode(0);
                if (file != null) {
                    file.delete();
                }
                return null;
            } catch (Exception e) {
                GalleryLog.e("DownloadCache", String.format("fail to download %s", new Object[]{this.mUrl}) + "." + e.getMessage());
            } catch (Throwable th) {
                jc.setMode(0);
            }
        }

        private File createTempFile(String dirStr, String name, File rootDir) throws IOException {
            File result = null;
            File dir = new File(rootDir, "picasa-" + dirStr);
            if (!dir.exists() && (!dir.mkdirs() || !new File(dir, ".nomedia").createNewFile())) {
                return null;
            }
            String head = "";
            int i = 0;
            while (i <= 100) {
                result = new File(dir, head + name);
                i++;
                head = "(" + i + ")";
                if (result.createNewFile()) {
                    break;
                }
            }
            return result;
        }

        public String workContent() {
            return String.format("download files. dir: %s, name: %s, root: %s", new Object[]{this.mDir, this.mName, DownloadCache.this.mRoot});
        }
    }

    public class Entry {
        public File cacheFile;
        protected long mId;

        Entry(long id, File cacheFile) {
            this.mId = id;
            this.cacheFile = (File) Utils.checkNotNull(cacheFile);
        }
    }

    public static class TaskProxy {
        private boolean isFinish = false;
        private Entry mEntry;
        private boolean mIsCancelled = false;
        private DownloadTask mTask;

        synchronized void setResult(Entry entry) {
            if (!this.mIsCancelled) {
                this.mEntry = entry;
                this.isFinish = true;
                notifyAll();
            }
        }

        public synchronized Entry get(JobContext jc) {
            jc.setCancelListener(new CancelListener() {
                public void onCancel() {
                    TaskProxy.this.mTask.removeProxy(TaskProxy.this);
                    synchronized (TaskProxy.this) {
                        TaskProxy.this.mIsCancelled = true;
                        TaskProxy.this.notifyAll();
                    }
                }
            });
            while (!this.mIsCancelled && !this.isFinish) {
                try {
                    wait();
                } catch (InterruptedException e) {
                    GalleryLog.w("DownloadCache", "ignore interrupt." + e.getMessage());
                }
            }
            jc.setCancelListener(null);
            return this.mEntry;
        }
    }

    static {
        String[] strArr = new String[1];
        strArr[0] = String.format("sum(%s)", new Object[]{"_size"});
        SUM_PROJECTION = strArr;
    }

    public DownloadCache(GalleryApp application, File root, long capacity) {
        this.mRoot = (File) Utils.checkNotNull(root);
        this.mApplication = (GalleryApp) Utils.checkNotNull(application);
        this.mCapacity = capacity;
        this.mDatabase = new DatabaseHelper(application.getAndroidContext()).getWritableDatabase();
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private Entry findEntryInDatabase(String stringUrl) {
        Throwable th;
        Cursor cursor = this.mDatabase.query(TABLE_NAME, QUERY_PROJECTION, WHERE_HASH_AND_URL, new String[]{String.valueOf(Utils.crc64Long(stringUrl)), stringUrl}, null, null, null);
        try {
            if (cursor.moveToNext()) {
                File file = new File(cursor.getString(1));
                long id = (long) cursor.getInt(0);
                Entry entry = null;
                synchronized (this.mEntryMap) {
                    try {
                        entry = (Entry) this.mEntryMap.get(stringUrl);
                        if (entry == null) {
                            Entry entry2 = new Entry(id, file);
                            try {
                                this.mEntryMap.put(stringUrl, entry2);
                                entry = entry2;
                            } catch (Throwable th2) {
                                th = th2;
                                entry = entry2;
                                throw th;
                            }
                        }
                    } catch (Throwable th3) {
                        th = th3;
                        throw th;
                    }
                }
            }
            cursor.close();
            return null;
        } finally {
            cursor.close();
        }
    }

    public Entry download(JobContext jc, URL url) {
        initialize();
        return download(jc, url, null, null);
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public Entry download(JobContext jc, URL url, String dir, String name) {
        if (!this.mInitialized) {
            initialize();
        }
        String stringUrl = url.toString();
        synchronized (this.mEntryMap) {
            Entry entry = (Entry) this.mEntryMap.get(stringUrl);
            if (entry != null) {
                updateLastAccess(entry.mId);
                return entry;
            }
        }
    }

    private void updateLastAccess(long id) {
        ContentValues values = new ContentValues();
        values.put("last_access", Long.valueOf(System.currentTimeMillis()));
        this.mDatabase.update(TABLE_NAME, values, "_id = ?", new String[]{String.valueOf(id)});
    }

    private synchronized void freeSomeSpaceIfNeed(int maxDeleteFileCount) {
        if (this.mTotalBytes > this.mCapacity) {
            Cursor cursor = this.mDatabase.query(TABLE_NAME, FREESPACE_PROJECTION, null, null, null, null, FREESPACE_ORDER_BY);
            while (maxDeleteFileCount > 0) {
                try {
                    if (this.mTotalBytes > this.mCapacity && cursor.moveToNext()) {
                        boolean containsKey;
                        long id = cursor.getLong(0);
                        String url = cursor.getString(2);
                        long size = cursor.getLong(3);
                        String path = cursor.getString(1);
                        synchronized (this.mEntryMap) {
                            containsKey = this.mEntryMap.containsKey(url);
                        }
                        if (!containsKey) {
                            maxDeleteFileCount--;
                            this.mTotalBytes -= size;
                            new File(path).delete();
                            this.mDatabase.delete(TABLE_NAME, "_id = ?", new String[]{String.valueOf(id)});
                        }
                    }
                } catch (Throwable th) {
                    cursor.close();
                }
            }
            cursor.close();
        }
    }

    private synchronized long insertEntry(String url, File file) {
        ContentValues values;
        long size = file.length();
        this.mTotalBytes += size;
        values = new ContentValues();
        String hashCode = String.valueOf(Utils.crc64Long(url));
        values.put("_data", file.getAbsolutePath());
        values.put("hash_code", hashCode);
        values.put("content_url", url);
        values.put("_size", Long.valueOf(size));
        values.put("last_updated", Long.valueOf(System.currentTimeMillis()));
        return this.mDatabase.insert(TABLE_NAME, "", values);
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private synchronized void initialize() {
        if (!this.mInitialized) {
            this.mInitialized = true;
            if (!this.mRoot.isDirectory()) {
                this.mRoot.mkdirs();
            }
            if (this.mRoot.isDirectory()) {
                Cursor cursor = this.mDatabase.query(TABLE_NAME, SUM_PROJECTION, null, null, null, null, null);
                this.mTotalBytes = 0;
                try {
                    if (cursor.moveToNext()) {
                        this.mTotalBytes = cursor.getLong(0);
                    }
                    if (this.mTotalBytes > this.mCapacity) {
                        freeSomeSpaceIfNeed(16);
                    }
                } finally {
                    cursor.close();
                }
            } else {
                throw new RuntimeException("cannot create " + this.mRoot.getAbsolutePath());
            }
        }
    }
}
