package tmsdkobf;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteException;
import java.io.File;
import java.lang.reflect.Method;

/* compiled from: Unknown */
public abstract class ij {
    private static Class<?> rV = null;
    private static Method rW = null;
    private static Method rX = null;
    private SQLiteDatabase mDatabase = null;
    private final String mName;
    private final int mNewVersion;
    private final CursorFactory rS;
    private final String rT;
    private boolean rU = false;

    public ij(Context context, String str, CursorFactory cursorFactory, int i, String str2) {
        if (i >= 1) {
            synchronized (ij.class) {
                this.mName = str;
                this.rS = cursorFactory;
                this.mNewVersion = i;
                this.rT = str2;
                try {
                    rV = Class.forName("android.database.sqlite.SQLiteDatabase");
                    rW = rV.getDeclaredMethod("lock", new Class[0]);
                    rX = rV.getDeclaredMethod("unlock", new Class[0]);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return;
        }
        throw new IllegalArgumentException("Version must be >= 1, was " + i);
    }

    public File bs(String str) {
        File file = new File(this.rT);
        if (!file.exists()) {
            file.mkdirs();
        }
        return new File(this.rT + str);
    }

    public void close() {
        synchronized (ij.class) {
            if (this.rU) {
                throw new IllegalStateException("Closed during initialization");
            }
            if (this.mDatabase != null) {
                if (this.mDatabase.isOpen()) {
                    this.mDatabase.close();
                    this.mDatabase = null;
                }
            }
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public SQLiteDatabase getWritableDatabase() {
        SQLiteDatabase openOrCreateDatabase;
        Throwable th;
        synchronized (ij.class) {
            SQLiteDatabase sQLiteDatabase;
            if (this.mDatabase != null && this.mDatabase.isOpen() && !this.mDatabase.isReadOnly()) {
                sQLiteDatabase = this.mDatabase;
                return sQLiteDatabase;
            } else if (this.rU) {
                throw new IllegalStateException("getWritableDatabase called recursively");
            } else {
                if (this.mDatabase != null) {
                    try {
                        rW.invoke(this.mDatabase, new Object[0]);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                try {
                    this.rU = true;
                    openOrCreateDatabase = this.mName != null ? SQLiteDatabase.openOrCreateDatabase(bs(this.mName).getPath(), this.rS) : SQLiteDatabase.create(null);
                    if (openOrCreateDatabase != null) {
                        try {
                            int version = openOrCreateDatabase.getVersion();
                            if (version != this.mNewVersion) {
                                openOrCreateDatabase.beginTransaction();
                                if (version != 0) {
                                    onUpgrade(openOrCreateDatabase, version, this.mNewVersion);
                                } else {
                                    onCreate(openOrCreateDatabase);
                                }
                                openOrCreateDatabase.setVersion(this.mNewVersion);
                                openOrCreateDatabase.setTransactionSuccessful();
                                openOrCreateDatabase.endTransaction();
                            }
                            onOpen(openOrCreateDatabase);
                            this.rU = false;
                            if (this.mDatabase != null) {
                                try {
                                    this.mDatabase.close();
                                    rX.invoke(this.mDatabase, new Object[0]);
                                } catch (Exception e2) {
                                }
                            }
                            this.mDatabase = openOrCreateDatabase;
                            return openOrCreateDatabase;
                        } catch (SQLiteException e3) {
                            sQLiteDatabase = openOrCreateDatabase;
                            this.rU = false;
                            if (this.mDatabase != null) {
                                try {
                                    rX.invoke(this.mDatabase, new Object[0]);
                                } catch (Exception e4) {
                                }
                            }
                            if (sQLiteDatabase != null) {
                                sQLiteDatabase.close();
                            }
                            return null;
                        } catch (Throwable th2) {
                            th = th2;
                            this.rU = false;
                            if (this.mDatabase != null) {
                                try {
                                    rX.invoke(this.mDatabase, new Object[0]);
                                } catch (Exception e5) {
                                }
                            }
                            if (openOrCreateDatabase != null) {
                                openOrCreateDatabase.close();
                            }
                            throw th;
                        }
                    }
                    this.rU = false;
                    if (this.mDatabase != null) {
                        try {
                            rX.invoke(this.mDatabase, new Object[0]);
                        } catch (Exception e6) {
                        }
                    }
                    if (openOrCreateDatabase != null) {
                        openOrCreateDatabase.close();
                    }
                } catch (SQLiteException e7) {
                    sQLiteDatabase = null;
                    this.rU = false;
                    if (this.mDatabase != null) {
                        rX.invoke(this.mDatabase, new Object[0]);
                    }
                    if (sQLiteDatabase != null) {
                        sQLiteDatabase.close();
                    }
                    return null;
                } catch (Throwable th3) {
                    th = th3;
                    openOrCreateDatabase = null;
                    this.rU = false;
                    if (this.mDatabase != null) {
                        rX.invoke(this.mDatabase, new Object[0]);
                    }
                    if (openOrCreateDatabase != null) {
                        openOrCreateDatabase.close();
                    }
                    throw th;
                }
            }
        }
    }

    public abstract void onCreate(SQLiteDatabase sQLiteDatabase);

    public void onOpen(SQLiteDatabase sQLiteDatabase) {
    }

    public abstract void onUpgrade(SQLiteDatabase sQLiteDatabase, int i, int i2);
}
