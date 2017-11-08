package com.android.mms.model;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.text.TextUtils;
import com.android.mms.MmsApp;
import com.android.mms.MmsConfig;
import com.android.mms.ui.MessageUtils;
import com.google.android.mms.MmsException;
import com.huawei.cspcommon.MLog;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import org.w3c.dom.events.EventListener;

public abstract class MediaModel extends Model implements EventListener {
    private boolean isLocation = false;
    protected int mBegin;
    protected String mContentType;
    protected Context mContext;
    private byte[] mData;
    protected int mDuration;
    protected short mFill;
    private HashMap<String, String> mLocationMap;
    private final ArrayList<MediaAction> mMediaActions;
    protected boolean mMediaResizeable;
    private String mPartName = null;
    protected int mSeekTo;
    protected int mSize;
    protected String mSourceBuild;
    protected String mSrc;
    protected String mTag;
    private Uri mUri;

    public enum MediaAction {
        NO_ACTIVE_ACTION,
        START,
        STOP,
        PAUSE,
        SEEK
    }

    public MediaModel(Context context, String tag, String contentType, String src, Uri uri) throws MmsException {
        this.mContext = context;
        this.mTag = tag;
        this.mContentType = contentType;
        this.mSrc = src;
        this.mUri = uri;
        initMediaSize();
        this.mMediaActions = new ArrayList();
        this.mMediaResizeable = false;
    }

    public MediaModel(Context context, String tag, String contentType, String src, byte[] data) {
        if (data == null) {
            throw new IllegalArgumentException("data may not be null.");
        }
        this.mContext = context;
        this.mTag = tag;
        this.mContentType = contentType;
        this.mSrc = src;
        this.mData = (byte[]) data.clone();
        this.mSize = this.mData.length;
        this.mMediaActions = new ArrayList();
        this.mMediaResizeable = false;
    }

    public int getBegin() {
        return this.mBegin;
    }

    public void setBegin(int begin) {
        this.mBegin = begin;
        notifyModelChanged(true);
    }

    public int getDuration() {
        return this.mDuration;
    }

    public void setDuration(int duration) {
        if (!isPlayable() || duration >= 0) {
            this.mDuration = duration;
        } else {
            try {
                initMediaDuration();
            } catch (MmsException e) {
                MLog.e("Mms/media", e.getMessage(), (Throwable) e);
                return;
            }
        }
        notifyModelChanged(true);
    }

    public void setLocation(boolean islocation) {
        this.isLocation = islocation;
    }

    public boolean isLocation() {
        return this.isLocation;
    }

    public String getContentType() {
        return this.mContentType;
    }

    public Uri getUri() {
        return this.mUri;
    }

    public byte[] getData() {
        return this.mData != null ? Arrays.copyOf(this.mData, this.mData.length) : null;
    }

    void setUri(Uri uri) {
        if (this.mUri != null && ((isImage() || isVideo()) && !this.mUri.equals(uri))) {
            MmsApp.getApplication().getThumbnailManager().removeThumbnail(this.mUri);
        }
        this.mUri = uri;
    }

    public String getSrc() {
        return this.mSrc;
    }

    public String getSourceBuild() {
        return this.mSourceBuild;
    }

    public void setBuildSource(String sourceBuild) {
        this.mSourceBuild = sourceBuild;
    }

    public String getSmilAndPartName() {
        if (this.mPartName == null) {
            if (!TextUtils.isEmpty(this.mSrc) && MessageUtils.isNormalASCII(this.mSrc)) {
                this.mPartName = this.mSrc;
            } else if (this.mSrc == null || this.mSrc.lastIndexOf(46) <= 0) {
                this.mPartName = "" + (System.currentTimeMillis() + ((long) this.mSize));
            } else {
                this.mPartName = (System.currentTimeMillis() + ((long) this.mSize)) + this.mSrc.substring(this.mSrc.lastIndexOf(46));
            }
        }
        return this.mPartName;
    }

    public void setFill(short fill) {
        this.mFill = fill;
        notifyModelChanged(true);
    }

    public int getMediaSize() {
        return this.mSize;
    }

    public int getType() {
        if (isText()) {
            return 1;
        }
        if (isImage()) {
            return 2;
        }
        if (isAudio()) {
            return 3;
        }
        if (isVideo()) {
            return 5;
        }
        if (isVCalendar()) {
            return 7;
        }
        if (isVcard()) {
            return 6;
        }
        return 0;
    }

    public boolean isText() {
        return this.mTag.equals("text");
    }

    public boolean isImage() {
        return this.mTag.equals("img");
    }

    public boolean isVideo() {
        return this.mTag.equals("video");
    }

    public boolean isAudio() {
        return this.mTag.equals("audio");
    }

    public boolean isVcard() {
        return this.mTag.equals("vcard");
    }

    public boolean isVCalendar() {
        return this.mTag.equals("vcalendar");
    }

    protected void initMediaDuration() throws MmsException {
        if (this.mUri == null) {
            throw new IllegalArgumentException("Uri may not be null.");
        }
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        int duration = 0;
        try {
            retriever.setDataSource(this.mContext, this.mUri);
            String dur = retriever.extractMetadata(9);
            if (dur != null) {
                duration = Integer.parseInt(dur);
            }
            this.mDuration = duration;
            retriever.release();
        } catch (Exception ex) {
            MLog.e("Mms/media", "MediaMetadataRetriever failed to get duration for Uri", (Throwable) ex);
            throw new MmsException(ex);
        } catch (Throwable th) {
            retriever.release();
        }
    }

    private void initMediaSize() throws MmsException {
        ContentResolver cr = this.mContext.getContentResolver();
        InputStream inputStream = null;
        this.mSize = 0;
        FileInputStream fileInputStream = null;
        FileChannel fileChannel = null;
        try {
            inputStream = cr.openInputStream(this.mUri);
            if (inputStream != null) {
                if (inputStream instanceof FileInputStream) {
                    fileInputStream = (FileInputStream) inputStream;
                    fileChannel = fileInputStream.getChannel();
                    this.mSize = (int) fileChannel.size();
                }
                if (this.mSize == 0) {
                    byte[] buf = new byte[256];
                    while (true) {
                        int rSize = inputStream.read(buf);
                        if (-1 != rSize) {
                            this.mSize += rSize;
                        }
                    }
                    if (this.mSize > MmsConfig.getMaxMessageSize()) {
                        MLog.w("Mms/media", "initMediaSize: Video size: f.getChannel().size(): " + this.mSize + " larger than max message size: " + MmsConfig.getMaxMessageSize() + " " + getClass().getSimpleName());
                    }
                }
                if (this.mSize > MmsConfig.getMaxMessageSize()) {
                    MLog.w("Mms/media", "initMediaSize: Video size: f.getChannel().size(): " + this.mSize + " larger than max message size: " + MmsConfig.getMaxMessageSize() + " " + getClass().getSimpleName());
                }
            }
            if (fileChannel != null) {
                try {
                    fileChannel.close();
                } catch (IOException e) {
                    MLog.e("Mms/media", "IOException caught while closing stream", (Throwable) e);
                    return;
                }
            }
            if (fileInputStream != null) {
                fileInputStream.close();
            }
            if (inputStream != null) {
                inputStream.close();
            }
        } catch (IOException e2) {
            MLog.e("Mms/media", "IOException caught while opening or reading stream", (Throwable) e2);
            if (e2 instanceof FileNotFoundException) {
                throw new MmsException(e2.getMessage());
            }
            if (fileChannel != null) {
                try {
                    fileChannel.close();
                } catch (IOException e22) {
                    MLog.e("Mms/media", "IOException caught while closing stream", (Throwable) e22);
                    return;
                }
            }
            if (fileInputStream != null) {
                fileInputStream.close();
            }
            if (inputStream != null) {
                inputStream.close();
            }
        } catch (Exception e3) {
            MLog.e("Mms/media", "Exception caught while opening or reading stream", (Throwable) e3);
            if (fileChannel != null) {
                try {
                    fileChannel.close();
                } catch (IOException e222) {
                    MLog.e("Mms/media", "IOException caught while closing stream", (Throwable) e222);
                    return;
                }
            }
            if (fileInputStream != null) {
                fileInputStream.close();
            }
            if (inputStream != null) {
                inputStream.close();
            }
        } catch (Throwable th) {
            if (fileChannel != null) {
                try {
                    fileChannel.close();
                } catch (IOException e2222) {
                    MLog.e("Mms/media", "IOException caught while closing stream", (Throwable) e2222);
                }
            }
            if (fileInputStream != null) {
                fileInputStream.close();
            }
            if (inputStream != null) {
                inputStream.close();
            }
        }
    }

    public static boolean isMmsUri(Uri uri) {
        return uri.getAuthority().startsWith("mms");
    }

    public int getSeekTo() {
        return this.mSeekTo;
    }

    public void appendAction(MediaAction action) {
        this.mMediaActions.add(action);
    }

    public MediaAction getCurrentAction() {
        if (this.mMediaActions.size() == 0) {
            return MediaAction.NO_ACTIVE_ACTION;
        }
        return (MediaAction) this.mMediaActions.remove(0);
    }

    protected boolean isPlayable() {
        return false;
    }

    protected void pauseMusicPlayer() {
        if (MLog.isLoggable("Mms_app", 2)) {
            MLog.d("Mms/media", "pauseMusicPlayer");
        }
        this.mContext.sendBroadcast(new Intent("com.android.mediacenter.musicservicecommand.pause"));
        this.mContext.sendBroadcast(new Intent("com.huawei.android.FMRadio.fmradioservicecommand.stop"), "com.android.huawei.permission.OUTSIDE_STOP_FM");
    }

    public void setData(byte[] data) {
        if (data != null) {
            this.mData = Arrays.copyOf(data, data.length);
        }
    }

    public void setLocationSource(HashMap<String, String> hashMap) {
        this.mLocationMap = hashMap;
    }

    public HashMap<String, String> getLocationSource() {
        return this.mLocationMap;
    }
}
