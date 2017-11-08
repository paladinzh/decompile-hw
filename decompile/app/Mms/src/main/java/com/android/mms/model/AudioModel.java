package com.android.mms.model;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.AudioManager.OnAudioFocusChangeListener;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.text.TextUtils;
import android.webkit.MimeTypeMap;
import com.android.mms.ContentRestrictionException;
import com.android.mms.UnsupportContentTypeException;
import com.android.mms.dom.events.EventImpl;
import com.android.mms.model.MediaModel.MediaAction;
import com.google.android.gms.location.places.Place;
import com.google.android.mms.MmsException;
import com.huawei.cspcommon.MLog;
import com.huawei.cspcommon.ex.SqliteWrapper;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import org.w3c.dom.events.Event;

public class AudioModel extends MediaModel {
    OnAudioFocusChangeListener mAudioFocusChangeListener;
    private AudioManager mAudioManager;
    private AudioManagerFocusCallback mAudioManagerFocusCallback;
    private final HashMap<String, String> mExtras;

    public interface AudioManagerFocusCallback {
        void onAbandonAudioFocus();
    }

    public void setAudioManagerFocusCallback(AudioManagerFocusCallback audioManagerFocusCallback) {
        this.mAudioManagerFocusCallback = audioManagerFocusCallback;
    }

    public AudioModel(Context context, Uri uri) throws MmsException {
        this(context, null, null, uri);
        initModelFromUri(uri);
        checkContentRestriction(uri);
    }

    public AudioModel(Context context, String contentType, String src, Uri uri) throws MmsException {
        super(context, "audio", contentType, src, uri);
        this.mAudioFocusChangeListener = new OnAudioFocusChangeListener() {
            public void onAudioFocusChange(int focusChange) {
                if (focusChange == -2) {
                    AudioModel.this.mAudioManager.abandonAudioFocus(this);
                    AudioModel.this.mAudioManagerFocusCallback.onAbandonAudioFocus();
                } else if (focusChange != 1 && focusChange == -1) {
                    AudioModel.this.mAudioManager.abandonAudioFocus(this);
                    AudioModel.this.mAudioManagerFocusCallback.onAbandonAudioFocus();
                }
            }
        };
        this.mExtras = new HashMap();
    }

    private void initModelFromUri(Uri uri) throws MmsException {
        if (uri == null) {
            MLog.e("Mms/media", "initModelFromUri uri is null");
            return;
        }
        String scheme = uri.getScheme();
        if ("content".equals(scheme)) {
            initFromContentUri(uri);
        } else if ("file".equals(scheme)) {
            initFromFile(uri);
        }
        try {
            initMediaDuration();
        } catch (MmsException e) {
            MLog.e("Mms/media", "initModelFromUri has an error >>>" + e);
        }
    }

    private void initFromFile(Uri uri) {
        String path = uri.getPath();
        this.mSrc = path.substring(path.lastIndexOf(47) + 1);
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        String extension = MimeTypeMap.getFileExtensionFromUrl(this.mSrc);
        if (TextUtils.isEmpty(extension)) {
            int dotPos = this.mSrc.lastIndexOf(46);
            if (dotPos >= 0) {
                extension = this.mSrc.substring(dotPos + 1);
            }
        }
        this.mContentType = mimeTypeMap.getMimeTypeFromExtension(extension.toLowerCase());
        if (this.mContentType == null) {
            throw new UnsupportContentTypeException("Unsupported audio content type : " + this.mSrc);
        } else if (MLog.isLoggable("Mms_app", 2)) {
            MLog.v("Mms/media", "New AudioModel initFromFile created: mContentType=" + this.mContentType);
        }
    }

    private void initFromContentUri(Uri uri) throws MmsException {
        Uri parse;
        IOException ioException;
        File file;
        String album;
        String artist;
        MimeTypeMap mimeTypeMap;
        String extension;
        Throwable th;
        ContentResolver cr = this.mContext.getContentResolver();
        String uriStr = null;
        if (uri.toString().startsWith("content://com.android.providers.media.documents")) {
            String[] uriDiv = uri.getLastPathSegment().split(":");
            uriStr = "content://media/external/audio/media/" + uriDiv[uriDiv.length - 1];
        }
        Context context = this.mContext;
        if (uriStr != null) {
            parse = Uri.parse(uriStr);
        } else {
            parse = uri;
        }
        Cursor c = SqliteWrapper.query(context, cr, parse, null, null, null, null);
        int index = -1;
        if (c != null) {
            try {
                if (c.moveToFirst()) {
                    int dotPos;
                    String path = null;
                    if (MediaModel.isMmsUri(uri)) {
                        path = c.getString(c.getColumnIndexOrThrow("_data"));
                        this.mContentType = c.getString(c.getColumnIndexOrThrow("ct"));
                    } else {
                        index = c.getColumnIndex("_data");
                        if (index != -1) {
                            path = c.getString(c.getColumnIndexOrThrow("_data"));
                            this.mContentType = c.getString(c.getColumnIndexOrThrow("mime_type"));
                        } else {
                            MLog.v("Mms/media", "initFromContentUri::query results is not contains data, uri is: " + uri);
                            boolean isFileProvider = false;
                            ParcelFileDescriptor parcelFileDescriptor = null;
                            FileInputStream fileInputStream = null;
                            FileOutputStream fileOutputStream = null;
                            File file2 = null;
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
                                FileInputStream fileInputStream2 = new FileInputStream(parcelFileDescriptor.getFileDescriptor());
                                try {
                                    file = new File(this.mContext.getCacheDir(), "shared_video_file");
                                } catch (IOException e) {
                                    ioException2 = e;
                                    fileInputStream = fileInputStream2;
                                    try {
                                        MLog.e("Mms/media", "Mms/media ioException.getMessage() " + ioException2.getMessage());
                                        if (fileInputStream != null) {
                                            try {
                                                fileInputStream.close();
                                            } catch (IOException ioException22) {
                                                MLog.e("Mms/media", ioException22.getMessage());
                                            }
                                        }
                                        if (fileOutputStream != null) {
                                            try {
                                                fileOutputStream.close();
                                                isFileProvider = true;
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
                                            path = c.getString(c.getColumnIndexOrThrow("_display_name"));
                                            this.mContentType = c.getString(c.getColumnIndexOrThrow("mime_type"));
                                        } else {
                                            if (file2 != null) {
                                                path = file2.getAbsolutePath();
                                                this.mSrc = file2.getName();
                                            }
                                            this.mContentType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(MimeTypeMap.getFileExtensionFromUrl(uri.toString()).toLowerCase(Locale.getDefault()));
                                        }
                                        if (this.mContentType.equals("audio/*")) {
                                            album = c.getString(c.getColumnIndexOrThrow("album"));
                                            if (!TextUtils.isEmpty(album)) {
                                                this.mExtras.put("album", album);
                                            }
                                            artist = c.getString(c.getColumnIndexOrThrow("artist"));
                                            if (!TextUtils.isEmpty(artist)) {
                                                this.mExtras.put("artist", artist);
                                            }
                                        }
                                        if (index != -1) {
                                            this.mSrc = path;
                                        } else {
                                            this.mSrc = path.substring(path.lastIndexOf(47) + 1);
                                        }
                                        MLog.i("Mms/media", "content type audio/quicktime: " + this.mSrc);
                                        mimeTypeMap = MimeTypeMap.getSingleton();
                                        extension = MimeTypeMap.getFileExtensionFromUrl(this.mSrc);
                                        if (TextUtils.isEmpty(extension)) {
                                            dotPos = this.mSrc.lastIndexOf(46);
                                            if (dotPos >= 0) {
                                                extension = this.mSrc.substring(dotPos + 1);
                                            }
                                        }
                                        this.mContentType = mimeTypeMap.getMimeTypeFromExtension(extension.toLowerCase());
                                        if (TextUtils.isEmpty(this.mContentType)) {
                                            c.close();
                                            return;
                                        }
                                        throw new MmsException("Type of media is unknown.");
                                    } catch (Throwable th2) {
                                        th = th2;
                                        if (fileInputStream != null) {
                                            try {
                                                fileInputStream.close();
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
                                    fileInputStream = fileInputStream2;
                                    if (fileInputStream != null) {
                                        fileInputStream.close();
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
                                    if (file.exists()) {
                                        MLog.i("Mms/media", "delete shared image file result " + file.delete());
                                    }
                                    FileOutputStream fileOutputStream2 = new FileOutputStream(file);
                                    try {
                                        MLog.i("Mms/media", "Mms/media outputFile.getAbsolutePath() " + file.getAbsolutePath());
                                        byte[] buffer = new byte[Place.TYPE_SUBLOCALITY_LEVEL_2];
                                        while (true) {
                                            int length = fileInputStream2.read(buffer);
                                            if (-1 == length) {
                                                break;
                                            }
                                            fileOutputStream2.write(buffer, 0, length);
                                        }
                                        fileOutputStream2.flush();
                                        if (fileInputStream2 != null) {
                                            try {
                                                fileInputStream2.close();
                                            } catch (IOException ioException22222222) {
                                                MLog.e("Mms/media", ioException22222222.getMessage());
                                            }
                                        }
                                        if (fileOutputStream2 != null) {
                                            try {
                                                fileOutputStream2.close();
                                                isFileProvider = true;
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
                                        file2 = file;
                                        fileOutputStream = fileOutputStream2;
                                        fileInputStream = fileInputStream2;
                                    } catch (IOException e2) {
                                        ioException2222222222 = e2;
                                        file2 = file;
                                        fileOutputStream = fileOutputStream2;
                                        fileInputStream = fileInputStream2;
                                        MLog.e("Mms/media", "Mms/media ioException.getMessage() " + ioException2222222222.getMessage());
                                        if (fileInputStream != null) {
                                            fileInputStream.close();
                                        }
                                        if (fileOutputStream != null) {
                                            fileOutputStream.close();
                                            isFileProvider = true;
                                        }
                                        if (parcelFileDescriptor != null) {
                                            parcelFileDescriptor.close();
                                        }
                                        if (isFileProvider) {
                                            if (file2 != null) {
                                                path = file2.getAbsolutePath();
                                                this.mSrc = file2.getName();
                                            }
                                            this.mContentType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(MimeTypeMap.getFileExtensionFromUrl(uri.toString()).toLowerCase(Locale.getDefault()));
                                        } else {
                                            path = c.getString(c.getColumnIndexOrThrow("_display_name"));
                                            this.mContentType = c.getString(c.getColumnIndexOrThrow("mime_type"));
                                        }
                                        if (this.mContentType.equals("audio/*")) {
                                            album = c.getString(c.getColumnIndexOrThrow("album"));
                                            if (TextUtils.isEmpty(album)) {
                                                this.mExtras.put("album", album);
                                            }
                                            artist = c.getString(c.getColumnIndexOrThrow("artist"));
                                            if (TextUtils.isEmpty(artist)) {
                                                this.mExtras.put("artist", artist);
                                            }
                                        }
                                        if (index != -1) {
                                            this.mSrc = path.substring(path.lastIndexOf(47) + 1);
                                        } else {
                                            this.mSrc = path;
                                        }
                                        MLog.i("Mms/media", "content type audio/quicktime: " + this.mSrc);
                                        mimeTypeMap = MimeTypeMap.getSingleton();
                                        extension = MimeTypeMap.getFileExtensionFromUrl(this.mSrc);
                                        if (TextUtils.isEmpty(extension)) {
                                            dotPos = this.mSrc.lastIndexOf(46);
                                            if (dotPos >= 0) {
                                                extension = this.mSrc.substring(dotPos + 1);
                                            }
                                        }
                                        this.mContentType = mimeTypeMap.getMimeTypeFromExtension(extension.toLowerCase());
                                        if (TextUtils.isEmpty(this.mContentType)) {
                                            throw new MmsException("Type of media is unknown.");
                                        }
                                        c.close();
                                        return;
                                    } catch (Throwable th4) {
                                        th = th4;
                                        file2 = file;
                                        fileOutputStream = fileOutputStream2;
                                        fileInputStream = fileInputStream2;
                                        if (fileInputStream != null) {
                                            fileInputStream.close();
                                        }
                                        if (fileOutputStream != null) {
                                            fileOutputStream.close();
                                        }
                                        if (parcelFileDescriptor != null) {
                                            parcelFileDescriptor.close();
                                        }
                                        throw th;
                                    }
                                } catch (IOException e3) {
                                    ioException2222222222 = e3;
                                    file2 = file;
                                    fileInputStream = fileInputStream2;
                                    MLog.e("Mms/media", "Mms/media ioException.getMessage() " + ioException2222222222.getMessage());
                                    if (fileInputStream != null) {
                                        fileInputStream.close();
                                    }
                                    if (fileOutputStream != null) {
                                        fileOutputStream.close();
                                        isFileProvider = true;
                                    }
                                    if (parcelFileDescriptor != null) {
                                        parcelFileDescriptor.close();
                                    }
                                    if (isFileProvider) {
                                        if (file2 != null) {
                                            path = file2.getAbsolutePath();
                                            this.mSrc = file2.getName();
                                        }
                                        this.mContentType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(MimeTypeMap.getFileExtensionFromUrl(uri.toString()).toLowerCase(Locale.getDefault()));
                                    } else {
                                        path = c.getString(c.getColumnIndexOrThrow("_display_name"));
                                        this.mContentType = c.getString(c.getColumnIndexOrThrow("mime_type"));
                                    }
                                    if (this.mContentType.equals("audio/*")) {
                                        album = c.getString(c.getColumnIndexOrThrow("album"));
                                        if (TextUtils.isEmpty(album)) {
                                            this.mExtras.put("album", album);
                                        }
                                        artist = c.getString(c.getColumnIndexOrThrow("artist"));
                                        if (TextUtils.isEmpty(artist)) {
                                            this.mExtras.put("artist", artist);
                                        }
                                    }
                                    if (index != -1) {
                                        this.mSrc = path.substring(path.lastIndexOf(47) + 1);
                                    } else {
                                        this.mSrc = path;
                                    }
                                    MLog.i("Mms/media", "content type audio/quicktime: " + this.mSrc);
                                    mimeTypeMap = MimeTypeMap.getSingleton();
                                    extension = MimeTypeMap.getFileExtensionFromUrl(this.mSrc);
                                    if (TextUtils.isEmpty(extension)) {
                                        dotPos = this.mSrc.lastIndexOf(46);
                                        if (dotPos >= 0) {
                                            extension = this.mSrc.substring(dotPos + 1);
                                        }
                                    }
                                    this.mContentType = mimeTypeMap.getMimeTypeFromExtension(extension.toLowerCase());
                                    if (TextUtils.isEmpty(this.mContentType)) {
                                        throw new MmsException("Type of media is unknown.");
                                    }
                                    c.close();
                                    return;
                                } catch (Throwable th5) {
                                    th = th5;
                                    file2 = file;
                                    fileInputStream = fileInputStream2;
                                    if (fileInputStream != null) {
                                        fileInputStream.close();
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
                                    if (file2 != null) {
                                        path = file2.getAbsolutePath();
                                        this.mSrc = file2.getName();
                                    }
                                    this.mContentType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(MimeTypeMap.getFileExtensionFromUrl(uri.toString()).toLowerCase(Locale.getDefault()));
                                } else {
                                    path = c.getString(c.getColumnIndexOrThrow("_display_name"));
                                    this.mContentType = c.getString(c.getColumnIndexOrThrow("mime_type"));
                                }
                            } catch (IOException e4) {
                                ioException2222222222 = e4;
                                MLog.e("Mms/media", "Mms/media ioException.getMessage() " + ioException2222222222.getMessage());
                                if (fileInputStream != null) {
                                    fileInputStream.close();
                                }
                                if (fileOutputStream != null) {
                                    fileOutputStream.close();
                                    isFileProvider = true;
                                }
                                if (parcelFileDescriptor != null) {
                                    parcelFileDescriptor.close();
                                }
                                if (isFileProvider) {
                                    path = c.getString(c.getColumnIndexOrThrow("_display_name"));
                                    this.mContentType = c.getString(c.getColumnIndexOrThrow("mime_type"));
                                } else {
                                    if (file2 != null) {
                                        path = file2.getAbsolutePath();
                                        this.mSrc = file2.getName();
                                    }
                                    this.mContentType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(MimeTypeMap.getFileExtensionFromUrl(uri.toString()).toLowerCase(Locale.getDefault()));
                                }
                                if (this.mContentType.equals("audio/*")) {
                                    album = c.getString(c.getColumnIndexOrThrow("album"));
                                    if (TextUtils.isEmpty(album)) {
                                        this.mExtras.put("album", album);
                                    }
                                    artist = c.getString(c.getColumnIndexOrThrow("artist"));
                                    if (TextUtils.isEmpty(artist)) {
                                        this.mExtras.put("artist", artist);
                                    }
                                }
                                if (index != -1) {
                                    this.mSrc = path;
                                } else {
                                    this.mSrc = path.substring(path.lastIndexOf(47) + 1);
                                }
                                MLog.i("Mms/media", "content type audio/quicktime: " + this.mSrc);
                                mimeTypeMap = MimeTypeMap.getSingleton();
                                extension = MimeTypeMap.getFileExtensionFromUrl(this.mSrc);
                                if (TextUtils.isEmpty(extension)) {
                                    dotPos = this.mSrc.lastIndexOf(46);
                                    if (dotPos >= 0) {
                                        extension = this.mSrc.substring(dotPos + 1);
                                    }
                                }
                                this.mContentType = mimeTypeMap.getMimeTypeFromExtension(extension.toLowerCase());
                                if (TextUtils.isEmpty(this.mContentType)) {
                                    c.close();
                                    return;
                                }
                                throw new MmsException("Type of media is unknown.");
                            }
                        }
                        if (this.mContentType.equals("audio/*")) {
                            album = c.getString(c.getColumnIndexOrThrow("album"));
                            if (TextUtils.isEmpty(album)) {
                                this.mExtras.put("album", album);
                            }
                            artist = c.getString(c.getColumnIndexOrThrow("artist"));
                            if (TextUtils.isEmpty(artist)) {
                                this.mExtras.put("artist", artist);
                            }
                        }
                    }
                    if (index != -1) {
                        this.mSrc = path.substring(path.lastIndexOf(47) + 1);
                    } else {
                        this.mSrc = path;
                    }
                    if (this.mContentType.equals("audio/quicktime") || this.mContentType.equals("audio/ogg")) {
                        MLog.i("Mms/media", "content type audio/quicktime: " + this.mSrc);
                        mimeTypeMap = MimeTypeMap.getSingleton();
                        extension = MimeTypeMap.getFileExtensionFromUrl(this.mSrc);
                        if (TextUtils.isEmpty(extension)) {
                            dotPos = this.mSrc.lastIndexOf(46);
                            if (dotPos >= 0) {
                                extension = this.mSrc.substring(dotPos + 1);
                            }
                        }
                        this.mContentType = mimeTypeMap.getMimeTypeFromExtension(extension.toLowerCase());
                    }
                    if (TextUtils.isEmpty(this.mContentType)) {
                        throw new MmsException("Type of media is unknown.");
                    }
                    c.close();
                    return;
                }
                throw new MmsException("Nothing found: " + uri);
            } catch (IllegalArgumentException e5) {
                this.mContentType = c.getString(c.getColumnIndexOrThrow("mimetype"));
            } catch (Throwable th6) {
                c.close();
            }
        } else {
            throw new MmsException("Bad URI: " + uri);
        }
    }

    public void handleEvent(Event evt) {
        String evtType = evt.getType();
        MediaAction action = MediaAction.NO_ACTIVE_ACTION;
        if (evtType.equals("SmilMediaStart")) {
            action = MediaAction.START;
            pauseMusicPlayer();
        } else if (evtType.equals("SmilMediaEnd")) {
            action = MediaAction.STOP;
        } else if (evtType.equals("SmilMediaPause")) {
            action = MediaAction.PAUSE;
        } else if (evtType.equals("SmilMediaSeek")) {
            action = MediaAction.SEEK;
            this.mSeekTo = ((EventImpl) evt).getSeekTo();
        }
        appendAction(action);
        notifyModelChanged(false);
    }

    public void requestAudioFocus() {
        if (this.mAudioManager == null) {
            this.mAudioManager = (AudioManager) this.mContext.getSystemService("audio");
        }
        this.mAudioManager.requestAudioFocus(this.mAudioFocusChangeListener, 3, 2);
    }

    public void abandonAudioFocus() {
        if (this.mAudioManager == null) {
            this.mAudioManager = (AudioManager) this.mContext.getSystemService("audio");
        }
        this.mAudioManager.abandonAudioFocus(this.mAudioFocusChangeListener);
    }

    public Map<String, ?> getExtras() {
        return this.mExtras;
    }

    protected void checkContentRestriction(Uri uri) throws ContentRestrictionException {
        ContentRestrictionFactory.getContentRestriction().checkAudioContentType(this.mContentType, this.mContext, uri);
    }

    protected boolean isPlayable() {
        return true;
    }
}
