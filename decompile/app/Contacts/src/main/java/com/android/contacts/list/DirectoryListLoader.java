package com.android.contacts.list;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.Handler;
import android.text.TextUtils;
import com.android.contacts.compatibility.DirectoryCompat;
import com.android.contacts.util.HwLog;
import com.google.android.gms.R;

public class DirectoryListLoader extends AsyncTaskLoader<Cursor> {
    private static final String[] RESULT_PROJECTION = new String[]{"_id", "directoryType", "displayName", "photoSupport"};
    private MatrixCursor mDefaultDirectoryList;
    private int mDirectorySearchMode;
    private boolean mLocalInvisibleDirectoryEnabled;
    private final ContentObserver mObserver = new ContentObserver(new Handler()) {
        public void onChange(boolean selfChange) {
            DirectoryListLoader.this.forceLoad();
        }
    };

    private static final class DirectoryQuery {
        public static final String[] PROJECTION = new String[]{"_id", "packageName", "typeResourceId", "displayName", "photoSupport"};
        public static final Uri URI = DirectoryCompat.getContentUri();

        private DirectoryQuery() {
        }
    }

    public DirectoryListLoader(Context context) {
        super(context);
    }

    public void setDirectorySearchMode(int mode) {
        this.mDirectorySearchMode = mode;
    }

    public void setLocalInvisibleDirectoryEnabled(boolean flag) {
        this.mLocalInvisibleDirectoryEnabled = flag;
    }

    protected void onStartLoading() {
        getContext().getContentResolver().registerContentObserver(DirectoryQuery.URI, false, this.mObserver);
        forceLoad();
    }

    protected void onStopLoading() {
        getContext().getContentResolver().unregisterContentObserver(this.mObserver);
    }

    public Cursor loadInBackground() {
        if (this.mDirectorySearchMode == 0) {
            return getDefaultDirectories();
        }
        String str;
        MatrixCursor matrixCursor = new MatrixCursor(RESULT_PROJECTION);
        Context context = getContext();
        PackageManager pm = context.getPackageManager();
        switch (this.mDirectorySearchMode) {
            case 1:
                str = null;
                break;
            case 2:
                str = "shortcutSupport=2";
                break;
            case 3:
                str = "shortcutSupport IN (2, 1)";
                break;
            default:
                matrixCursor.close();
                throw new RuntimeException("Unsupported directory search mode: " + this.mDirectorySearchMode);
        }
        Cursor cursor = context.getContentResolver().query(DirectoryQuery.URI, DirectoryQuery.PROJECTION, str, null, "_id");
        if (cursor != null) {
            while (cursor.moveToNext()) {
                long directoryId = cursor.getLong(0);
                if (this.mLocalInvisibleDirectoryEnabled || !DirectoryCompat.isInvisibleDirectory(directoryId)) {
                    String directoryType = null;
                    String packageName = cursor.getString(1);
                    int typeResourceId = cursor.getInt(2);
                    if (!(TextUtils.isEmpty(packageName) || typeResourceId == 0)) {
                        try {
                            directoryType = pm.getResourcesForApplication(packageName).getString(typeResourceId);
                        } catch (Exception e) {
                            HwLog.e("ContactEntryListAdapter", "Cannot obtain directory type from package: " + packageName);
                        } catch (Throwable th) {
                            cursor.close();
                        }
                    }
                    String displayName = cursor.getString(3);
                    int photoSupport = cursor.getInt(4);
                    matrixCursor.addRow(new Object[]{Long.valueOf(directoryId), directoryType, displayName, Integer.valueOf(photoSupport)});
                }
            }
            cursor.close();
        }
        return matrixCursor;
    }

    private Cursor getDefaultDirectories() {
        if (this.mDefaultDirectoryList == null) {
            this.mDefaultDirectoryList = new MatrixCursor(RESULT_PROJECTION);
            this.mDefaultDirectoryList.addRow(new Object[]{Long.valueOf(0), getContext().getString(R.string.contactsList), null, Integer.valueOf(3)});
            this.mDefaultDirectoryList.addRow(new Object[]{Long.valueOf(1), getContext().getString(R.string.local_invisible_directory), null, Integer.valueOf(3)});
        }
        return this.mDefaultDirectoryList;
    }

    protected void onReset() {
        stopLoading();
    }
}
