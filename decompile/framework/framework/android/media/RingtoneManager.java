package android.media;

import android.Manifest.permission;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.database.StaleDataException;
import android.net.Uri;
import android.os.Environment;
import android.os.ParcelFileDescriptor.AutoCloseInputStream;
import android.os.Process;
import android.provider.MediaStore.Audio.AudioColumns;
import android.provider.MediaStore.Audio.Media;
import android.provider.Settings.System;
import android.util.Log;
import com.android.internal.database.SortCursor;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import libcore.io.Streams;

public class RingtoneManager {
    public static final String ACTION_RINGTONE_PICKER = "android.intent.action.RINGTONE_PICKER";
    public static final String EXTRA_RINGTONE_AUDIO_ATTRIBUTES_FLAGS = "android.intent.extra.ringtone.AUDIO_ATTRIBUTES_FLAGS";
    public static final String EXTRA_RINGTONE_DEFAULT_URI = "android.intent.extra.ringtone.DEFAULT_URI";
    public static final String EXTRA_RINGTONE_EXISTING_URI = "android.intent.extra.ringtone.EXISTING_URI";
    @Deprecated
    public static final String EXTRA_RINGTONE_INCLUDE_DRM = "android.intent.extra.ringtone.INCLUDE_DRM";
    public static final String EXTRA_RINGTONE_PICKED_URI = "android.intent.extra.ringtone.PICKED_URI";
    public static final String EXTRA_RINGTONE_SHOW_DEFAULT = "android.intent.extra.ringtone.SHOW_DEFAULT";
    public static final String EXTRA_RINGTONE_SHOW_SILENT = "android.intent.extra.ringtone.SHOW_SILENT";
    public static final String EXTRA_RINGTONE_TITLE = "android.intent.extra.ringtone.TITLE";
    public static final String EXTRA_RINGTONE_TYPE = "android.intent.extra.ringtone.TYPE";
    public static final int HW_TYPE_ALL = 15;
    public static final int ID_COLUMN_INDEX = 0;
    private static final String[] INTERNAL_COLUMNS = new String[]{"_id", "title", "\"" + Media.INTERNAL_CONTENT_URI + "\"", "title_key"};
    private static final String[] MEDIA_COLUMNS = new String[]{"_id", "title", "\"" + Media.EXTERNAL_CONTENT_URI + "\"", "title_key"};
    private static final String TAG = "RingtoneManager";
    public static final int TITLE_COLUMN_INDEX = 1;
    public static final int TYPE_ALARM = 4;
    public static final int TYPE_ALL = 7;
    public static final int TYPE_NOTIFICATION = 2;
    public static final int TYPE_RINGTONE = 1;
    public static final int TYPE_RINGTONE2 = 8;
    public static final int URI_COLUMN_INDEX = 2;
    private static boolean mSetUriStat = false;
    private final Activity mActivity;
    private final Context mContext;
    private Cursor mCursor;
    private final List<String> mFilterColumns;
    private Ringtone mPreviousRingtone;
    private boolean mStopPreviousRingtone;
    private int mType;

    public RingtoneManager(Activity activity) {
        this.mType = 1;
        this.mFilterColumns = new ArrayList();
        this.mStopPreviousRingtone = true;
        this.mActivity = activity;
        this.mContext = activity;
        setType(this.mType);
    }

    public RingtoneManager(Context context) {
        this.mType = 1;
        this.mFilterColumns = new ArrayList();
        this.mStopPreviousRingtone = true;
        this.mActivity = null;
        this.mContext = context;
        setType(this.mType);
    }

    public void setType(int type) {
        if (this.mCursor != null) {
            throw new IllegalStateException("Setting filter columns should be done before querying for ringtones.");
        }
        this.mType = type;
        setFilterColumnsList(type);
    }

    public int inferStreamType() {
        switch (this.mType) {
            case 2:
                return 5;
            case 4:
                return 4;
            default:
                return 2;
        }
    }

    public void setStopPreviousRingtone(boolean stopPreviousRingtone) {
        this.mStopPreviousRingtone = stopPreviousRingtone;
    }

    public boolean getStopPreviousRingtone() {
        return this.mStopPreviousRingtone;
    }

    public void stopPreviousRingtone() {
        if (this.mPreviousRingtone != null) {
            this.mPreviousRingtone.stop();
        }
    }

    @Deprecated
    public boolean getIncludeDrm() {
        return false;
    }

    @Deprecated
    public void setIncludeDrm(boolean includeDrm) {
        if (includeDrm) {
            Log.w(TAG, "setIncludeDrm no longer supported");
        }
    }

    public Cursor getCursor() {
        try {
            if (!(this.mCursor == null || this.mCursor.isClosed())) {
                if (this.mCursor.requery()) {
                    return this.mCursor;
                }
            }
        } catch (StaleDataException e) {
            Log.w(TAG, "requery failded: ");
            this.mCursor = null;
        }
        Cursor internalCursor = getInternalRingtones();
        Cursor mediaCursor = getMediaRingtones();
        Cursor sortCursor = new SortCursor(new Cursor[]{internalCursor, mediaCursor}, "title_key");
        this.mCursor = sortCursor;
        return sortCursor;
    }

    public Ringtone getRingtone(int position) {
        if (this.mStopPreviousRingtone && this.mPreviousRingtone != null) {
            this.mPreviousRingtone.stop();
        }
        this.mPreviousRingtone = getRingtone(this.mContext, getRingtoneUri(position), inferStreamType());
        return this.mPreviousRingtone;
    }

    public Uri getRingtoneUri(int position) {
        try {
            if (this.mCursor == null || !this.mCursor.moveToPosition(position)) {
                return null;
            }
            return getUriFromCursor(this.mCursor);
        } catch (IllegalStateException e) {
            Log.e(TAG, "attempt to re-open an already-closed object");
            return null;
        } catch (StaleDataException staleDataException) {
            Log.e(TAG, "getRingtoneUri -- " + position, staleDataException);
            return null;
        }
    }

    private static Uri getUriFromCursor(Cursor cursor) {
        Uri uri = null;
        try {
            uri = ContentUris.withAppendedId(Uri.parse(cursor.getString(2)), cursor.getLong(0));
        } catch (Exception e) {
            Log.e(TAG, "Failed to get uri from cursor!!!!!!!!!!!!");
        }
        return uri;
    }

    public int getRingtonePosition(Uri ringtoneUri) {
        if (ringtoneUri == null) {
            return -1;
        }
        Cursor cursor = getCursor();
        int cursorCount = cursor.getCount();
        if (!cursor.moveToFirst()) {
            return -1;
        }
        Uri currentUri = null;
        Object previousUriString = null;
        for (int i = 0; i < cursorCount; i++) {
            String uriString = cursor.getString(2);
            if (currentUri == null || !uriString.equals(r4)) {
                currentUri = Uri.parse(uriString);
            }
            if (ringtoneUri.equals(ContentUris.withAppendedId(currentUri, cursor.getLong(0)))) {
                return i;
            }
            cursor.move(1);
            String previousUriString2 = uriString;
        }
        return -1;
    }

    public static Uri getValidRingtoneUri(Context context) {
        RingtoneManager rm = new RingtoneManager(context);
        Uri uri = getValidRingtoneUriFromCursorAndClose(context, rm.getInternalRingtones());
        if (uri == null) {
            return getValidRingtoneUriFromCursorAndClose(context, rm.getMediaRingtones());
        }
        return uri;
    }

    private static Uri getValidRingtoneUriFromCursorAndClose(Context context, Cursor cursor) {
        if (cursor == null) {
            return null;
        }
        Uri uri = null;
        if (cursor.moveToFirst()) {
            uri = getUriFromCursor(cursor);
        }
        cursor.close();
        return uri;
    }

    private Cursor getInternalRingtones() {
        return query(Media.INTERNAL_CONTENT_URI, INTERNAL_COLUMNS, constructBooleanTrueWhereClause(this.mFilterColumns), null, "title_key");
    }

    private Cursor getMediaRingtones() {
        Cursor cursor = null;
        if (this.mContext.checkPermission(permission.READ_EXTERNAL_STORAGE, Process.myPid(), Process.myUid()) != 0) {
            Log.w(TAG, "No READ_EXTERNAL_STORAGE permission, ignoring ringtones on ext storage");
            return null;
        }
        String status = Environment.getExternalStorageState();
        if (status.equals(Environment.MEDIA_MOUNTED) || status.equals(Environment.MEDIA_MOUNTED_READ_ONLY)) {
            cursor = query(Media.EXTERNAL_CONTENT_URI, MEDIA_COLUMNS, constructBooleanTrueWhereClause(this.mFilterColumns), null, "title_key");
        }
        return cursor;
    }

    private void setFilterColumnsList(int type) {
        List<String> columns = this.mFilterColumns;
        columns.clear();
        if ((type & 1) != 0) {
            columns.add(AudioColumns.IS_RINGTONE);
        }
        if ((type & 8) != 0) {
            columns.add(AudioColumns.IS_RINGTONE);
        }
        if ((type & 2) != 0) {
            columns.add(AudioColumns.IS_NOTIFICATION);
        }
        if ((type & 4) != 0) {
            columns.add(AudioColumns.IS_ALARM);
        }
    }

    private static String constructBooleanTrueWhereClause(List<String> columns) {
        if (columns == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        sb.append("(");
        for (int i = columns.size() - 1; i >= 0; i--) {
            sb.append((String) columns.get(i)).append("=1 or ");
        }
        if (columns.size() > 0) {
            sb.setLength(sb.length() - 4);
        }
        sb.append(")");
        return sb.toString();
    }

    private Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        if (this.mActivity != null) {
            return this.mActivity.managedQuery(uri, projection, selection, selectionArgs, sortOrder);
        }
        return this.mContext.getContentResolver().query(uri, projection, selection, selectionArgs, sortOrder);
    }

    public static Ringtone getRingtone(Context context, Uri ringtoneUri) {
        return getRingtone(context, ringtoneUri, -1);
    }

    private static Ringtone getRingtone(Context context, Uri ringtoneUri, int streamType) {
        try {
            mSetUriStat = false;
            Ringtone r = new Ringtone(context, true);
            if (streamType >= 0) {
                r.setStreamType(streamType);
            }
            if (ringtoneUri == null) {
                Log.w(TAG, "ringtoneUri is null ...");
                return null;
            }
            r.setUri(ringtoneUri);
            if (r.getPrepareStat()) {
                Log.w(TAG, "prepare failed ......");
                mSetUriStat = true;
            }
            return r;
        } catch (Exception ex) {
            Log.e(TAG, "Failed to open ringtone " + ringtoneUri + ": " + ex);
            return null;
        }
    }

    public static boolean getSetUriStat() {
        return mSetUriStat;
    }

    public static Uri getActualDefaultRingtoneUri(Context context, int type) {
        Uri uri = null;
        String setting = getSettingForType(type);
        if (setting == null) {
            return null;
        }
        String uriString = System.getStringForUser(context.getContentResolver(), setting, context.getUserId());
        if (uriString != null) {
            uri = Uri.parse(uriString);
        }
        return uri;
    }

    public static void setActualDefaultRingtoneUri(Context context, int type, Uri ringtoneUri) {
        OutputStream outputStream;
        Throwable th;
        Throwable th2 = null;
        ContentResolver resolver = context.getContentResolver();
        String setting = getSettingForType(type);
        InputStream inputStream;
        Throwable th3;
        if (setting != null) {
            String uri;
            if (ringtoneUri != null) {
                uri = ringtoneUri.toString();
            } else {
                uri = null;
            }
            System.putStringForUser(resolver, setting, uri, context.getUserId());
            if (ringtoneUri != null) {
                Uri cacheUri = getCacheForType(type);
                inputStream = null;
                outputStream = null;
                try {
                    inputStream = openRingtone(context, ringtoneUri);
                    outputStream = resolver.openOutputStream(cacheUri);
                    Streams.copy(inputStream, outputStream);
                    if (outputStream != null) {
                        try {
                            outputStream.close();
                        } catch (Throwable th4) {
                            th2 = th4;
                        }
                    }
                    if (inputStream != null) {
                        try {
                            inputStream.close();
                        } catch (Throwable th5) {
                            th3 = th5;
                            if (th2 != null) {
                                if (th2 != th3) {
                                    th2.addSuppressed(th3);
                                    th3 = th2;
                                }
                            }
                        }
                    }
                    th3 = th2;
                    if (th3 != null) {
                        try {
                            throw th3;
                        } catch (IOException e) {
                            Log.w(TAG, "Failed to cache ringtone: " + e);
                        }
                    }
                } catch (Throwable th22) {
                    Throwable th6 = th22;
                    th22 = th3;
                    th3 = th6;
                }
            }
            return;
        }
        return;
        if (outputStream != null) {
            try {
                outputStream.close();
            } catch (Throwable th7) {
                th = th7;
                if (th22 != null) {
                    if (th22 != th) {
                        th22.addSuppressed(th);
                        th = th22;
                    }
                }
            }
        }
        th = th22;
        if (inputStream != null) {
            try {
                inputStream.close();
            } catch (Throwable th8) {
                th22 = th8;
                if (th != null) {
                    if (th != th22) {
                        th.addSuppressed(th22);
                        th22 = th;
                    }
                }
            }
        }
        th22 = th;
        if (th22 != null) {
            throw th22;
        }
        throw th3;
    }

    private static InputStream openRingtone(Context context, Uri uri) throws IOException {
        try {
            return context.getContentResolver().openInputStream(uri);
        } catch (Exception e) {
            Log.w(TAG, "Failed to open directly; attempting failover: " + e);
            try {
                return new AutoCloseInputStream(((AudioManager) context.getSystemService(AudioManager.class)).getRingtonePlayer().openRingtone(uri));
            } catch (Exception e2) {
                throw new IOException(e2);
            }
        }
    }

    private static String getSettingForType(int type) {
        if ((type & 1) != 0) {
            return System.RINGTONE;
        }
        if ((type & 8) != 0) {
            return "ringtone2";
        }
        if ((type & 2) != 0) {
            return System.NOTIFICATION_SOUND;
        }
        if ((type & 4) != 0) {
            return System.ALARM_ALERT;
        }
        return null;
    }

    public static Uri getCacheForType(int type) {
        if ((type & 1) != 0) {
            return System.RINGTONE_CACHE_URI;
        }
        if ((type & 8) != 0) {
            return System.RINGTONE2_CACHE_URI;
        }
        if ((type & 2) != 0) {
            return System.NOTIFICATION_SOUND_CACHE_URI;
        }
        if ((type & 4) != 0) {
            return System.ALARM_ALERT_CACHE_URI;
        }
        return null;
    }

    public static boolean isDefault(Uri ringtoneUri) {
        return getDefaultType(ringtoneUri) != -1;
    }

    public static int getDefaultType(Uri defaultRingtoneUri) {
        if (defaultRingtoneUri == null) {
            return -1;
        }
        if (defaultRingtoneUri.equals(System.HUAWEI_RINGTONE2_URI)) {
            return 8;
        }
        if (defaultRingtoneUri.equals(System.DEFAULT_RINGTONE_URI)) {
            return 1;
        }
        if (defaultRingtoneUri.equals(System.DEFAULT_NOTIFICATION_URI)) {
            return 2;
        }
        if (defaultRingtoneUri.equals(System.DEFAULT_ALARM_ALERT_URI)) {
            return 4;
        }
        return -1;
    }

    public static Uri getDefaultUri(int type) {
        if ((type & 1) != 0) {
            return System.DEFAULT_RINGTONE_URI;
        }
        if ((type & 8) != 0) {
            return System.HUAWEI_RINGTONE2_URI;
        }
        if ((type & 2) != 0) {
            return System.DEFAULT_NOTIFICATION_URI;
        }
        if ((type & 4) != 0) {
            return System.DEFAULT_ALARM_ALERT_URI;
        }
        return null;
    }
}
