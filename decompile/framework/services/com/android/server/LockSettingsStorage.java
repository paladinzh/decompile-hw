package com.android.server;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.os.UserManager;
import android.util.ArrayMap;
import android.util.Log;
import android.util.Slog;
import android.util.SparseArray;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

class LockSettingsStorage {
    private static final String BASE_ZERO_LOCK_PATTERN_FILE = "gatekeeper.gesture.key";
    private static final String CHILD_PROFILE_LOCK_FILE = "gatekeeper.profile.key";
    private static final String[] COLUMNS_FOR_PREFETCH = new String[]{COLUMN_KEY, COLUMN_VALUE};
    private static final String[] COLUMNS_FOR_QUERY = new String[]{COLUMN_VALUE};
    private static final String COLUMN_KEY = "name";
    private static final String COLUMN_USERID = "user";
    private static final String COLUMN_VALUE = "value";
    private static final boolean DEBUG = false;
    private static final Object DEFAULT = new Object();
    private static final String LEGACY_LOCK_PASSWORD_FILE = "password.key";
    private static final String LEGACY_LOCK_PATTERN_FILE = "gesture.key";
    private static final String LOCK_PASSWORD_FILE = "gatekeeper.password.key";
    private static final String LOCK_PATTERN_FILE = "gatekeeper.pattern.key";
    private static final String SYSTEM_DIRECTORY = "/system/";
    private static final String TABLE = "locksettings";
    private static final String TAG = "LockSettingsStorage";
    private final Cache mCache = new Cache();
    private final Context mContext;
    private final Object mFileWriteLock = new Object();
    private final DatabaseHelper mOpenHelper;
    private SparseArray<Integer> mStoredCredentialType;

    public interface Callback {
        void initialize(SQLiteDatabase sQLiteDatabase);
    }

    private static class Cache {
        private final ArrayMap<CacheKey, Object> mCache;
        private final CacheKey mCacheKey;
        private int mVersion;

        private static final class CacheKey {
            static final int TYPE_FETCHED = 2;
            static final int TYPE_FILE = 1;
            static final int TYPE_KEY_VALUE = 0;
            String key;
            int type;
            int userId;

            private CacheKey() {
            }

            public CacheKey set(int type, String key, int userId) {
                this.type = type;
                this.key = key;
                this.userId = userId;
                return this;
            }

            public boolean equals(Object obj) {
                boolean z = false;
                if (!(obj instanceof CacheKey)) {
                    return false;
                }
                CacheKey o = (CacheKey) obj;
                if (this.userId == o.userId && this.type == o.type) {
                    z = this.key.equals(o.key);
                }
                return z;
            }

            public int hashCode() {
                return (this.key.hashCode() ^ this.userId) ^ this.type;
            }
        }

        private Cache() {
            this.mCache = new ArrayMap();
            this.mCacheKey = new CacheKey();
            this.mVersion = 0;
        }

        String peekKeyValue(String key, String defaultValue, int userId) {
            Object cached = peek(0, key, userId);
            return cached == LockSettingsStorage.DEFAULT ? defaultValue : (String) cached;
        }

        boolean hasKeyValue(String key, int userId) {
            return contains(0, key, userId);
        }

        void putKeyValue(String key, String value, int userId) {
            put(0, key, value, userId);
        }

        void putKeyValueIfUnchanged(String key, Object value, int userId, int version) {
            putIfUnchanged(0, key, value, userId, version);
        }

        byte[] peekFile(String fileName) {
            return (byte[]) peek(1, fileName, -1);
        }

        boolean hasFile(String fileName) {
            return contains(1, fileName, -1);
        }

        void putFile(String key, byte[] value) {
            put(1, key, value, -1);
        }

        void putFileIfUnchanged(String key, byte[] value, int version) {
            putIfUnchanged(1, key, value, -1, version);
        }

        void setFetched(int userId) {
            put(2, "isFetched", "true", userId);
        }

        boolean isFetched(int userId) {
            return contains(2, "", userId);
        }

        private synchronized void put(int type, String key, Object value, int userId) {
            this.mCache.put(new CacheKey().set(type, key, userId), value);
            this.mVersion++;
        }

        private synchronized void putIfUnchanged(int type, String key, Object value, int userId, int version) {
            if (!contains(type, key, userId) && this.mVersion == version) {
                put(type, key, value, userId);
            }
        }

        private synchronized boolean contains(int type, String key, int userId) {
            return this.mCache.containsKey(this.mCacheKey.set(type, key, userId));
        }

        private synchronized Object peek(int type, String key, int userId) {
            return this.mCache.get(this.mCacheKey.set(type, key, userId));
        }

        private synchronized int getVersion() {
            return this.mVersion;
        }

        synchronized void removeUser(int userId) {
            for (int i = this.mCache.size() - 1; i >= 0; i--) {
                if (((CacheKey) this.mCache.keyAt(i)).userId == userId) {
                    this.mCache.removeAt(i);
                }
            }
            this.mVersion++;
        }

        synchronized void clear() {
            this.mCache.clear();
            this.mVersion++;
        }
    }

    static class CredentialHash {
        static final int TYPE_NONE = -1;
        static final int TYPE_PASSWORD = 2;
        static final int TYPE_PATTERN = 1;
        static final int VERSION_GATEKEEPER = 1;
        static final int VERSION_LEGACY = 0;
        byte[] hash;
        boolean isBaseZeroPattern;
        int version;

        CredentialHash(byte[] hash, int version) {
            this.hash = hash;
            this.version = version;
            this.isBaseZeroPattern = false;
        }

        CredentialHash(byte[] hash, boolean isBaseZeroPattern) {
            this.hash = hash;
            this.version = 1;
            this.isBaseZeroPattern = isBaseZeroPattern;
        }
    }

    class DatabaseHelper extends SQLiteOpenHelper {
        private static final String DATABASE_NAME = "locksettings.db";
        private static final int DATABASE_VERSION = 2;
        private static final String TAG = "LockSettingsDB";
        private final Callback mCallback;

        public DatabaseHelper(Context context, Callback callback) {
            super(context, DATABASE_NAME, null, 2);
            setWriteAheadLoggingEnabled(true);
            this.mCallback = callback;
        }

        private void createTable(SQLiteDatabase db) {
            db.execSQL("CREATE TABLE locksettings (_id INTEGER PRIMARY KEY AUTOINCREMENT,name TEXT,user INTEGER,value TEXT);");
        }

        public void onCreate(SQLiteDatabase db) {
            createTable(db);
            this.mCallback.initialize(db);
        }

        public void onUpgrade(SQLiteDatabase db, int oldVersion, int currentVersion) {
            int upgradeVersion = oldVersion;
            if (oldVersion == 1) {
                upgradeVersion = 2;
            }
            if (upgradeVersion != 2) {
                Log.w(TAG, "Failed to upgrade database!");
            }
        }
    }

    public LockSettingsStorage(Context context, Callback callback) {
        this.mContext = context;
        this.mOpenHelper = new DatabaseHelper(context, callback);
        this.mStoredCredentialType = new SparseArray();
    }

    public void writeKeyValue(String key, String value, int userId) {
        writeKeyValue(this.mOpenHelper.getWritableDatabase(), key, value, userId);
    }

    public void writeKeyValue(SQLiteDatabase db, String key, String value, int userId) {
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_KEY, key);
        cv.put(COLUMN_USERID, Integer.valueOf(userId));
        cv.put(COLUMN_VALUE, value);
        db.beginTransaction();
        try {
            db.delete(TABLE, "name=? AND user=?", new String[]{key, Integer.toString(userId)});
            db.insert(TABLE, null, cv);
            db.setTransactionSuccessful();
            this.mCache.putKeyValue(key, value, userId);
        } finally {
            db.endTransaction();
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public String readKeyValue(String key, String defaultValue, int userId) {
        synchronized (this.mCache) {
            if (this.mCache.hasKeyValue(key, userId)) {
                String peekKeyValue = this.mCache.peekKeyValue(key, defaultValue, userId);
                return peekKeyValue;
            }
            int version = this.mCache.getVersion();
        }
        this.mCache.putKeyValueIfUnchanged(key, result, userId, version);
        if (result != DEFAULT) {
            defaultValue = (String) result;
        }
        return defaultValue;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void prefetchUser(int userId) {
        synchronized (this.mCache) {
            if (this.mCache.isFetched(userId)) {
                return;
            } else {
                this.mCache.setFetched(userId);
                int version = this.mCache.getVersion();
            }
        }
        readPasswordHash(userId);
        readPatternHash(userId);
    }

    public int getStoredCredentialType(int userId) {
        Integer cachedStoredCredentialType = (Integer) this.mStoredCredentialType.get(userId);
        if (cachedStoredCredentialType != null) {
            return cachedStoredCredentialType.intValue();
        }
        int storedCredentialType;
        if (readPatternHash(userId) == null) {
            Log.i(TAG, "getStoredCredentialType by user " + userId + ", there is no pattern saved");
            if (readPasswordHash(userId) != null) {
                storedCredentialType = 2;
            } else {
                Log.i(TAG, "getStoredCredentialType by user " + userId + ", there is no password saved either");
                storedCredentialType = -1;
            }
        } else {
            Log.i(TAG, "getStoredCredentialType by user " + userId + ", has pattern");
            CredentialHash password = readPasswordHash(userId);
            if (password != null) {
                Log.i(TAG, "getStoredCredentialType by user " + userId + ", has password also");
                if (password.version == 1) {
                    storedCredentialType = 1;
                } else {
                    storedCredentialType = 1;
                }
            } else {
                storedCredentialType = 1;
            }
        }
        this.mStoredCredentialType.put(userId, Integer.valueOf(storedCredentialType));
        return storedCredentialType;
    }

    public CredentialHash readPasswordHash(int userId) {
        byte[] stored = readFile(getLockPasswordFilename(userId));
        if (stored == null || stored.length <= 0) {
            stored = readFile(getLegacyLockPasswordFilename(userId));
            if (stored != null && stored.length > 0) {
                return new CredentialHash(stored, 0);
            }
            Log.i(TAG, "readPasswordHash , cannot get any PasswordHash");
            return null;
        }
        Log.i(TAG, "readPasswordHash ok");
        return new CredentialHash(stored, 1);
    }

    public CredentialHash readPatternHash(int userId) {
        byte[] stored = readFile(getLockPatternFilename(userId));
        if (stored == null || stored.length <= 0) {
            stored = readFile(getBaseZeroLockPatternFilename(userId));
            if (stored != null && stored.length > 0) {
                return new CredentialHash(stored, true);
            }
            stored = readFile(getLegacyLockPatternFilename(userId));
            if (stored != null && stored.length > 0) {
                return new CredentialHash(stored, 0);
            }
            Log.i(TAG, "readPatternHash , cannot get any PatternHash");
            return null;
        }
        Log.i(TAG, "readPatternHash ok");
        return new CredentialHash(stored, 1);
    }

    public void removeChildProfileLock(int userId) {
        try {
            deleteFile(getChildProfileLockFile(userId));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void writeChildProfileLock(int userId, byte[] lock) {
        writeFile(getChildProfileLockFile(userId), lock);
    }

    public byte[] readChildProfileLock(int userId) {
        return readFile(getChildProfileLockFile(userId));
    }

    public boolean hasChildProfileLock(int userId) {
        return hasFile(getChildProfileLockFile(userId));
    }

    public boolean hasPassword(int userId) {
        if (hasFile(getLockPasswordFilename(userId))) {
            return true;
        }
        return hasFile(getLegacyLockPasswordFilename(userId));
    }

    public boolean hasPattern(int userId) {
        if (hasFile(getLockPatternFilename(userId)) || hasFile(getBaseZeroLockPatternFilename(userId))) {
            return true;
        }
        return hasFile(getLegacyLockPatternFilename(userId));
    }

    private boolean hasFile(String name) {
        byte[] contents = readFile(name);
        if (contents == null || contents.length <= 0) {
            return false;
        }
        return true;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private byte[] readFile(String name) {
        synchronized (this.mCache) {
            if (this.mCache.hasFile(name)) {
                byte[] peekFile = this.mCache.peekFile(name);
                return peekFile;
            }
            int version = this.mCache.getVersion();
        }
        RandomAccessFile randomAccessFile = raf;
        this.mCache.putFileIfUnchanged(name, stored, version);
        return stored;
    }

    private void writeFile(String name, byte[] hash) {
        IOException e;
        Throwable th;
        synchronized (this.mFileWriteLock) {
            RandomAccessFile randomAccessFile = null;
            try {
                RandomAccessFile raf = new RandomAccessFile(name, "rw");
                if (hash != null) {
                    try {
                        if (hash.length != 0) {
                            raf.write(hash, 0, hash.length);
                            raf.close();
                            if (raf != null) {
                                try {
                                    raf.close();
                                } catch (IOException e2) {
                                    Slog.e(TAG, "Error closing file " + e2);
                                } catch (Throwable th2) {
                                    th = th2;
                                    throw th;
                                }
                            }
                            randomAccessFile = raf;
                            this.mCache.putFile(name, hash);
                        }
                    } catch (IOException e3) {
                        e2 = e3;
                        randomAccessFile = raf;
                        try {
                            Slog.e(TAG, "Error writing to file " + e2);
                            if (randomAccessFile != null) {
                                try {
                                    randomAccessFile.close();
                                } catch (IOException e22) {
                                    Slog.e(TAG, "Error closing file " + e22);
                                } catch (Throwable th3) {
                                    th = th3;
                                    throw th;
                                }
                            }
                            this.mCache.putFile(name, hash);
                        } catch (Throwable th4) {
                            th = th4;
                            if (randomAccessFile != null) {
                                try {
                                    randomAccessFile.close();
                                } catch (IOException e222) {
                                    Slog.e(TAG, "Error closing file " + e222);
                                }
                            }
                            throw th;
                        }
                    } catch (Throwable th5) {
                        th = th5;
                        randomAccessFile = raf;
                        if (randomAccessFile != null) {
                            randomAccessFile.close();
                        }
                        throw th;
                    }
                }
                raf.setLength(0);
                raf.close();
                if (raf != null) {
                    raf.close();
                }
                randomAccessFile = raf;
            } catch (IOException e4) {
                e222 = e4;
                Slog.e(TAG, "Error writing to file " + e222);
                if (randomAccessFile != null) {
                    randomAccessFile.close();
                }
                this.mCache.putFile(name, hash);
            }
            this.mCache.putFile(name, hash);
        }
    }

    private void deleteFile(String name) {
        synchronized (this.mFileWriteLock) {
            File file = new File(name);
            if (file.exists()) {
                file.delete();
                this.mCache.putFile(name, null);
            }
        }
    }

    public void writePatternHash(byte[] hash, int userId) {
        int i;
        SparseArray sparseArray = this.mStoredCredentialType;
        if (hash == null) {
            i = -1;
        } else {
            i = 1;
        }
        sparseArray.put(userId, Integer.valueOf(i));
        writeFile(getLockPatternFilename(userId), hash);
        clearPasswordHash(userId);
    }

    private void clearPatternHash(int userId) {
        writeFile(getLockPatternFilename(userId), null);
    }

    public void writePasswordHash(byte[] hash, int userId) {
        int i;
        SparseArray sparseArray = this.mStoredCredentialType;
        if (hash == null) {
            i = -1;
        } else {
            i = 2;
        }
        sparseArray.put(userId, Integer.valueOf(i));
        writeFile(getLockPasswordFilename(userId), hash);
        clearPatternHash(userId);
    }

    private void clearPasswordHash(int userId) {
        writeFile(getLockPasswordFilename(userId), null);
    }

    String getLockPatternFilename(int userId) {
        return getLockCredentialFilePathForUser(userId, LOCK_PATTERN_FILE);
    }

    String getLockPasswordFilename(int userId) {
        return getLockCredentialFilePathForUser(userId, LOCK_PASSWORD_FILE);
    }

    String getLegacyLockPatternFilename(int userId) {
        return getLockCredentialFilePathForUser(userId, LEGACY_LOCK_PATTERN_FILE);
    }

    String getLegacyLockPasswordFilename(int userId) {
        return getLockCredentialFilePathForUser(userId, LEGACY_LOCK_PASSWORD_FILE);
    }

    private String getBaseZeroLockPatternFilename(int userId) {
        return getLockCredentialFilePathForUser(userId, BASE_ZERO_LOCK_PATTERN_FILE);
    }

    String getChildProfileLockFile(int userId) {
        return getLockCredentialFilePathForUser(userId, CHILD_PROFILE_LOCK_FILE);
    }

    private String getLockCredentialFilePathForUser(int userId, String basename) {
        String dataSystemDirectory = Environment.getDataDirectory().getAbsolutePath() + "/system/";
        if (userId == 0) {
            return dataSystemDirectory + basename;
        }
        return new File(Environment.getUserSystemDirectory(userId), basename).getAbsolutePath();
    }

    public void removeUser(int userId) {
        SQLiteDatabase db = this.mOpenHelper.getWritableDatabase();
        if (((UserManager) this.mContext.getSystemService(COLUMN_USERID)).getProfileParent(userId) == null) {
            synchronized (this.mFileWriteLock) {
                String name = getLockPasswordFilename(userId);
                File file = new File(name);
                if (file.exists()) {
                    file.delete();
                    this.mCache.putFile(name, null);
                }
                name = getLockPatternFilename(userId);
                file = new File(name);
                if (file.exists()) {
                    file.delete();
                    this.mCache.putFile(name, null);
                }
            }
        } else {
            removeChildProfileLock(userId);
        }
        try {
            db.beginTransaction();
            db.delete(TABLE, "user='" + userId + "'", null);
            db.setTransactionSuccessful();
            this.mCache.removeUser(userId);
        } finally {
            db.endTransaction();
        }
    }

    void closeDatabase() {
        this.mOpenHelper.close();
    }

    void clearCache() {
        this.mCache.clear();
    }

    protected String getLockCredentialFilePathForUser2(int userId, String basename) {
        return getLockCredentialFilePathForUser(userId, basename);
    }
}
