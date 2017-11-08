package com.huawei.gallery.provider;

import android.content.Context;
import android.content.ContextWrapper;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import java.io.File;

public class ExternalDataBaseContext extends ContextWrapper {
    private String mDirectory;

    public ExternalDataBaseContext(Context base, String directory) {
        super(base);
        this.mDirectory = directory;
    }

    public File getDatabasePath(String name) {
        return new File(this.mDirectory + File.separator + name);
    }

    public SQLiteDatabase openOrCreateDatabase(String name, int mode, CursorFactory factory) {
        return openOrCreateDatabase(name, mode, factory, null);
    }

    public SQLiteDatabase openOrCreateDatabase(String name, int mode, CursorFactory factory, DatabaseErrorHandler errorHandler) {
        File f = getDatabasePath(name);
        int flags = 268435456;
        if ((mode & 8) != 0) {
            flags = 805306368;
        }
        if ((mode & 16) != 0) {
            flags |= 16;
        }
        return SQLiteDatabase.openDatabase(f.getPath(), factory, flags, errorHandler);
    }
}
