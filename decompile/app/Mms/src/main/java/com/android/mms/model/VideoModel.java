package com.android.mms.model;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.webkit.MimeTypeMap;
import com.android.mms.ContentRestrictionException;
import com.android.mms.MmsApp;
import com.android.mms.UnsupportContentTypeException;
import com.android.mms.dom.events.EventImpl;
import com.android.mms.model.MediaModel.MediaAction;
import com.android.mms.util.ItemLoadedCallback;
import com.android.mms.util.ItemLoadedFuture;
import com.google.android.gms.location.places.Place;
import com.google.android.mms.MmsException;
import com.huawei.cspcommon.MLog;
import com.huawei.cspcommon.ex.SqliteWrapper;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Locale;
import org.w3c.dom.events.Event;

public class VideoModel extends RegionMediaModel {
    private ItemLoadedFuture mItemLoadedFuture;

    public VideoModel(Context context, Uri uri, RegionModel region) throws MmsException {
        this(context, null, null, uri, region);
        initModelFromUri(uri);
        checkContentRestriction(uri);
    }

    public VideoModel(Context context, String contentType, String src, Uri uri, RegionModel region) throws MmsException {
        super(context, "video", contentType, src, uri, region);
    }

    private void initModelFromUri(Uri uri) throws MmsException {
        if (uri.getScheme().equals("content")) {
            initFromContentUri(uri);
        } else if (uri.getScheme().equals("file")) {
            initFromFile(uri);
        }
        initMediaDuration();
    }

    private void initFromFile(Uri uri) {
        String path = uri.getPath();
        this.mSrc = path.substring(path.lastIndexOf(47) + 1);
        if (this.mSrc.startsWith(".") && this.mSrc.length() > 1) {
            this.mSrc = this.mSrc.substring(1);
        }
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        String extension = MimeTypeMap.getFileExtensionFromUrl(this.mSrc);
        if (TextUtils.isEmpty(extension)) {
            int dotPos = this.mSrc.lastIndexOf(46);
            if (dotPos >= 0) {
                extension = this.mSrc.substring(dotPos + 1);
            }
        }
        if (extension.equals("3gpp")) {
            extension = "3gp";
        }
        this.mContentType = mimeTypeMap.getMimeTypeFromExtension(extension.toLowerCase());
        if (this.mContentType == null) {
            throw new UnsupportContentTypeException("Unsupported video content type : " + this.mSrc);
        }
    }

    private void initFromContentUri(Uri uri) throws MmsException {
        IOException ioException;
        FileInputStream fileInputStream;
        int index;
        Throwable th;
        Cursor c = SqliteWrapper.query(this.mContext, this.mContext.getContentResolver(), uri, null, null, null, null);
        if (c == null) {
            throw new MmsException("Bad URI: " + uri);
        }
        String path;
        try {
            if (c.getCount() == 1 && c.moveToFirst()) {
                String extension;
                path = null;
                if (MediaModel.isMmsUri(uri)) {
                    path = c.getString(c.getColumnIndexOrThrow("fn"));
                    if (TextUtils.isEmpty(path)) {
                        path = c.getString(c.getColumnIndexOrThrow("_data"));
                    }
                    this.mContentType = c.getString(c.getColumnIndexOrThrow("ct"));
                } else {
                    if (c.getColumnIndex("_data") != -1) {
                        path = c.getString(c.getColumnIndexOrThrow("_data"));
                        try {
                            this.mContentType = c.getString(c.getColumnIndexOrThrow("mime_type"));
                        } catch (IllegalArgumentException e) {
                            try {
                                this.mContentType = c.getString(c.getColumnIndexOrThrow("mimetype"));
                            } catch (IllegalArgumentException e2) {
                                this.mContentType = PreferenceManager.getDefaultSharedPreferences(this.mContext).getString("contentType", "wrong");
                            }
                        }
                    } else {
                        boolean isFileProvider = false;
                        ParcelFileDescriptor parcelFileDescriptor = null;
                        FileOutputStream fileOutputStream = null;
                        FileInputStream fileInputStream2 = null;
                        File file = null;
                        try {
                            parcelFileDescriptor = this.mContext.getContentResolver().openFileDescriptor(uri, "r");
                            if (parcelFileDescriptor == null) {
                                if (parcelFileDescriptor != null) {
                                    try {
                                        parcelFileDescriptor.close();
                                    } catch (IOException ioException2) {
                                        MLog.e("Mms/media", ioException2.getMessage());
                                    }
                                }
                                c.close();
                                return;
                            }
                            FileOutputStream fileOutputStream2;
                            File file2 = new File(this.mContext.getCacheDir(), "shared_video_file");
                            try {
                                fileInputStream = new FileInputStream(parcelFileDescriptor.getFileDescriptor());
                                try {
                                    if (file2.exists()) {
                                        MLog.i("Mms/media", "delete shared image file result " + file2.delete());
                                    }
                                    fileOutputStream2 = new FileOutputStream(file2);
                                } catch (IOException e3) {
                                    ioException2 = e3;
                                    file = file2;
                                    fileInputStream2 = fileInputStream;
                                    try {
                                        MLog.e("Mms/media", " ioException.getMessage() " + ioException2.getMessage());
                                        if (fileInputStream2 != null) {
                                            try {
                                                fileInputStream2.close();
                                            } catch (IOException ioException22) {
                                                MLog.e("Mms/media", ioException22.getMessage());
                                            }
                                        }
                                        if (fileOutputStream != null) {
                                            isFileProvider = true;
                                            try {
                                                fileOutputStream.close();
                                            } catch (IOException ioException222) {
                                                MLog.e("Mms/media", ioException222.getMessage());
                                            }
                                        }
                                        if (parcelFileDescriptor != null) {
                                            try {
                                                parcelFileDescriptor.close();
                                            } catch (IOException ioException2222) {
                                                MLog.e("Mms/media", ioException2222.getMessage());
                                            }
                                        }
                                        if (isFileProvider) {
                                            if (file != null) {
                                                path = file.getAbsolutePath();
                                                this.mSrc = file.getName();
                                            }
                                            this.mContentType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(MimeTypeMap.getFileExtensionFromUrl(uri.toString()).toLowerCase(Locale.getDefault()));
                                        } else {
                                            path = c.getString(c.getColumnIndexOrThrow("_display_name"));
                                            this.mContentType = c.getString(c.getColumnIndexOrThrow("mime_type"));
                                        }
                                        if (TextUtils.isEmpty(this.mContentType)) {
                                            throw new MmsException("Type of media is unknown.");
                                        }
                                        if (path == null) {
                                            c.close();
                                            return;
                                        }
                                        this.mSrc = path.substring(path.lastIndexOf(47) + 1);
                                        index = this.mSrc.lastIndexOf(".");
                                        if (index != -1) {
                                            try {
                                                extension = this.mSrc.substring(index + 1);
                                                this.mContentType = "video/3gpp";
                                            } catch (IndexOutOfBoundsException e4) {
                                            }
                                        }
                                        if (MLog.isLoggable("Mms_app", 2)) {
                                            MLog.v("Mms/media", "New VideoModel initFromContentUri created: mSrc=" + this.mSrc + " mContentType=" + this.mContentType + " mUri=" + uri);
                                        }
                                        c.close();
                                        return;
                                    } catch (Throwable th2) {
                                        th = th2;
                                        if (fileInputStream2 != null) {
                                            try {
                                                fileInputStream2.close();
                                            } catch (IOException ioException22222) {
                                                MLog.e("Mms/media", ioException22222.getMessage());
                                            }
                                        }
                                        if (fileOutputStream != null) {
                                            try {
                                                fileOutputStream.close();
                                            } catch (IOException ioException222222) {
                                                MLog.e("Mms/media", ioException222222.getMessage());
                                            }
                                        }
                                        if (parcelFileDescriptor != null) {
                                            try {
                                                parcelFileDescriptor.close();
                                            } catch (IOException ioException2222222) {
                                                MLog.e("Mms/media", ioException2222222.getMessage());
                                            }
                                        }
                                        throw th;
                                    }
                                } catch (Throwable th3) {
                                    th = th3;
                                    file = file2;
                                    fileInputStream2 = fileInputStream;
                                    if (fileInputStream2 != null) {
                                        fileInputStream2.close();
                                    }
                                    if (fileOutputStream != null) {
                                        fileOutputStream.close();
                                    }
                                    if (parcelFileDescriptor != null) {
                                        parcelFileDescriptor.close();
                                    }
                                    throw th;
                                }
                            } catch (IOException e5) {
                                ioException2222222 = e5;
                                file = file2;
                                MLog.e("Mms/media", " ioException.getMessage() " + ioException2222222.getMessage());
                                if (fileInputStream2 != null) {
                                    fileInputStream2.close();
                                }
                                if (fileOutputStream != null) {
                                    isFileProvider = true;
                                    fileOutputStream.close();
                                }
                                if (parcelFileDescriptor != null) {
                                    parcelFileDescriptor.close();
                                }
                                if (isFileProvider) {
                                    path = c.getString(c.getColumnIndexOrThrow("_display_name"));
                                    this.mContentType = c.getString(c.getColumnIndexOrThrow("mime_type"));
                                } else {
                                    if (file != null) {
                                        path = file.getAbsolutePath();
                                        this.mSrc = file.getName();
                                    }
                                    this.mContentType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(MimeTypeMap.getFileExtensionFromUrl(uri.toString()).toLowerCase(Locale.getDefault()));
                                }
                                if (TextUtils.isEmpty(this.mContentType)) {
                                    throw new MmsException("Type of media is unknown.");
                                }
                                if (path == null) {
                                    this.mSrc = path.substring(path.lastIndexOf(47) + 1);
                                    index = this.mSrc.lastIndexOf(".");
                                    if (index != -1) {
                                        extension = this.mSrc.substring(index + 1);
                                        this.mContentType = "video/3gpp";
                                    }
                                    if (MLog.isLoggable("Mms_app", 2)) {
                                        MLog.v("Mms/media", "New VideoModel initFromContentUri created: mSrc=" + this.mSrc + " mContentType=" + this.mContentType + " mUri=" + uri);
                                    }
                                    c.close();
                                    return;
                                }
                                c.close();
                                return;
                            } catch (Throwable th4) {
                                th = th4;
                                file = file2;
                                if (fileInputStream2 != null) {
                                    fileInputStream2.close();
                                }
                                if (fileOutputStream != null) {
                                    fileOutputStream.close();
                                }
                                if (parcelFileDescriptor != null) {
                                    parcelFileDescriptor.close();
                                }
                                throw th;
                            }
                            try {
                                MLog.i("Mms/media", "Mms/media outputFile.getAbsolutePath() " + file2.getAbsolutePath());
                                byte[] buffer = new byte[Place.TYPE_SUBLOCALITY_LEVEL_2];
                                while (true) {
                                    int length = fileInputStream.read(buffer);
                                    if (-1 == length) {
                                        break;
                                    }
                                    fileOutputStream2.write(buffer, 0, length);
                                }
                                fileOutputStream2.flush();
                                if (fileInputStream != null) {
                                    try {
                                        fileInputStream.close();
                                    } catch (IOException ioException22222222) {
                                        MLog.e("Mms/media", ioException22222222.getMessage());
                                    }
                                }
                                if (fileOutputStream2 != null) {
                                    isFileProvider = true;
                                    try {
                                        fileOutputStream2.close();
                                    } catch (IOException ioException222222222) {
                                        MLog.e("Mms/media", ioException222222222.getMessage());
                                    }
                                }
                                if (parcelFileDescriptor != null) {
                                    try {
                                        parcelFileDescriptor.close();
                                    } catch (IOException ioException2222222222) {
                                        MLog.e("Mms/media", ioException2222222222.getMessage());
                                    }
                                }
                                file = file2;
                                fileInputStream2 = fileInputStream;
                                fileOutputStream = fileOutputStream2;
                            } catch (IOException e6) {
                                ioException2222222222 = e6;
                                file = file2;
                                fileInputStream2 = fileInputStream;
                                fileOutputStream = fileOutputStream2;
                                MLog.e("Mms/media", " ioException.getMessage() " + ioException2222222222.getMessage());
                                if (fileInputStream2 != null) {
                                    fileInputStream2.close();
                                }
                                if (fileOutputStream != null) {
                                    isFileProvider = true;
                                    fileOutputStream.close();
                                }
                                if (parcelFileDescriptor != null) {
                                    parcelFileDescriptor.close();
                                }
                                if (isFileProvider) {
                                    if (file != null) {
                                        path = file.getAbsolutePath();
                                        this.mSrc = file.getName();
                                    }
                                    this.mContentType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(MimeTypeMap.getFileExtensionFromUrl(uri.toString()).toLowerCase(Locale.getDefault()));
                                } else {
                                    path = c.getString(c.getColumnIndexOrThrow("_display_name"));
                                    this.mContentType = c.getString(c.getColumnIndexOrThrow("mime_type"));
                                }
                                if (TextUtils.isEmpty(this.mContentType)) {
                                    throw new MmsException("Type of media is unknown.");
                                }
                                if (path == null) {
                                    c.close();
                                    return;
                                }
                                this.mSrc = path.substring(path.lastIndexOf(47) + 1);
                                index = this.mSrc.lastIndexOf(".");
                                if (index != -1) {
                                    extension = this.mSrc.substring(index + 1);
                                    this.mContentType = "video/3gpp";
                                }
                                if (MLog.isLoggable("Mms_app", 2)) {
                                    MLog.v("Mms/media", "New VideoModel initFromContentUri created: mSrc=" + this.mSrc + " mContentType=" + this.mContentType + " mUri=" + uri);
                                }
                                c.close();
                                return;
                            } catch (Throwable th5) {
                                th = th5;
                                file = file2;
                                fileInputStream2 = fileInputStream;
                                fileOutputStream = fileOutputStream2;
                                if (fileInputStream2 != null) {
                                    fileInputStream2.close();
                                }
                                if (fileOutputStream != null) {
                                    fileOutputStream.close();
                                }
                                if (parcelFileDescriptor != null) {
                                    parcelFileDescriptor.close();
                                }
                                throw th;
                            }
                            if (isFileProvider) {
                                if (file != null) {
                                    path = file.getAbsolutePath();
                                    this.mSrc = file.getName();
                                }
                                this.mContentType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(MimeTypeMap.getFileExtensionFromUrl(uri.toString()).toLowerCase(Locale.getDefault()));
                            } else {
                                path = c.getString(c.getColumnIndexOrThrow("_display_name"));
                                this.mContentType = c.getString(c.getColumnIndexOrThrow("mime_type"));
                            }
                        } catch (IOException e7) {
                            ioException2222222222 = e7;
                            MLog.e("Mms/media", " ioException.getMessage() " + ioException2222222222.getMessage());
                            if (fileInputStream2 != null) {
                                fileInputStream2.close();
                            }
                            if (fileOutputStream != null) {
                                isFileProvider = true;
                                fileOutputStream.close();
                            }
                            if (parcelFileDescriptor != null) {
                                parcelFileDescriptor.close();
                            }
                            if (isFileProvider) {
                                path = c.getString(c.getColumnIndexOrThrow("_display_name"));
                                this.mContentType = c.getString(c.getColumnIndexOrThrow("mime_type"));
                            } else {
                                if (file != null) {
                                    path = file.getAbsolutePath();
                                    this.mSrc = file.getName();
                                }
                                this.mContentType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(MimeTypeMap.getFileExtensionFromUrl(uri.toString()).toLowerCase(Locale.getDefault()));
                            }
                            if (TextUtils.isEmpty(this.mContentType)) {
                                throw new MmsException("Type of media is unknown.");
                            }
                            if (path == null) {
                                this.mSrc = path.substring(path.lastIndexOf(47) + 1);
                                index = this.mSrc.lastIndexOf(".");
                                if (index != -1) {
                                    extension = this.mSrc.substring(index + 1);
                                    this.mContentType = "video/3gpp";
                                }
                                if (MLog.isLoggable("Mms_app", 2)) {
                                    MLog.v("Mms/media", "New VideoModel initFromContentUri created: mSrc=" + this.mSrc + " mContentType=" + this.mContentType + " mUri=" + uri);
                                }
                                c.close();
                                return;
                            }
                            c.close();
                            return;
                        }
                    }
                    if (TextUtils.isEmpty(this.mContentType)) {
                        throw new MmsException("Type of media is unknown.");
                    }
                }
                if (path == null) {
                    c.close();
                    return;
                }
                this.mSrc = path.substring(path.lastIndexOf(47) + 1);
                if (this.mContentType.equals("video/mp4") && !TextUtils.isEmpty(this.mSrc)) {
                    index = this.mSrc.lastIndexOf(".");
                    if (index != -1) {
                        extension = this.mSrc.substring(index + 1);
                        if (!TextUtils.isEmpty(extension) && (extension.equalsIgnoreCase("3gp") || extension.equalsIgnoreCase("3gpp") || extension.equalsIgnoreCase("3g2"))) {
                            this.mContentType = "video/3gpp";
                        }
                    }
                }
                if (MLog.isLoggable("Mms_app", 2)) {
                    MLog.v("Mms/media", "New VideoModel initFromContentUri created: mSrc=" + this.mSrc + " mContentType=" + this.mContentType + " mUri=" + uri);
                }
                c.close();
                return;
            }
            throw new MmsException("Nothing found: " + uri);
        } catch (IllegalArgumentException e8) {
            path = uri.toString();
        } catch (Throwable th6) {
            c.close();
        }
    }

    public void handleEvent(Event evt) {
        String evtType = evt.getType();
        if (MLog.isLoggable("Mms_app", 2)) {
            MLog.v("Mms/media", "[VideoModel] handleEvent " + evt.getType() + " on " + this);
        }
        MediaAction action = MediaAction.NO_ACTIVE_ACTION;
        if (evtType.equals("SmilMediaStart")) {
            action = MediaAction.START;
            pauseMusicPlayer();
            this.mVisible = true;
        } else if (evtType.equals("SmilMediaEnd")) {
            action = MediaAction.STOP;
            if (this.mFill != (short) 1) {
                this.mVisible = false;
            }
        } else if (evtType.equals("SmilMediaPause")) {
            action = MediaAction.PAUSE;
            this.mVisible = true;
        } else if (evtType.equals("SmilMediaSeek")) {
            action = MediaAction.SEEK;
            this.mSeekTo = ((EventImpl) evt).getSeekTo();
            this.mVisible = true;
        }
        appendAction(action);
        notifyModelChanged(false);
    }

    protected void checkContentRestriction(Uri uri) throws ContentRestrictionException {
        ContentRestrictionFactory.getContentRestriction().checkVideoContentType(this.mContentType, this.mContext, uri);
    }

    protected boolean isPlayable() {
        return true;
    }

    public ItemLoadedFuture loadThumbnailBitmap(ItemLoadedCallback callback) {
        this.mItemLoadedFuture = MmsApp.getApplication().getThumbnailManager().getVideoThumbnail(getUri(), callback);
        return this.mItemLoadedFuture;
    }
}
