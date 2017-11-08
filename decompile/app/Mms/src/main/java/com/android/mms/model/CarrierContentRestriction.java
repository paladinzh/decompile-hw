package com.android.mms.model;

import android.content.ContentResolver;
import android.content.Context;
import android.drm.DrmManagerClient;
import android.media.MediaFile;
import android.net.Uri;
import com.android.mms.ContentRestrictionException;
import com.android.mms.ExceedMessageSizeException;
import com.android.mms.MmsConfig;
import com.android.mms.UnsupportContentTypeException;
import com.google.android.mms.ContentType;
import com.huawei.rcs.utils.RcseMmsExt;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Set;

public class CarrierContentRestriction implements ContentRestriction {
    private static final HashMap<String, Integer> RESTRICED_MODE_TYPE = new HashMap();
    private static final ArrayList<String> sSupportedAudioTypes = ContentType.getAudioTypes();
    private static final ArrayList<String> sSupportedImageTypes = ContentType.getImageTypes();
    private static final ArrayList<String> sSupportedVideoTypes = ContentType.getVideoTypes();

    static {
        RESTRICED_MODE_TYPE.put("text/plain", Integer.valueOf(30720));
        RESTRICED_MODE_TYPE.put("image/jpeg", Integer.valueOf(102400));
        RESTRICED_MODE_TYPE.put("image/jpg", Integer.valueOf(102400));
        RESTRICED_MODE_TYPE.put("image/gif", Integer.valueOf(102400));
        RESTRICED_MODE_TYPE.put("image/vnd.wap.wbmp", Integer.valueOf(102400));
        RESTRICED_MODE_TYPE.put("audio/amr", Integer.valueOf(307200));
        RESTRICED_MODE_TYPE.put("audio/mid", Integer.valueOf(307200));
        RESTRICED_MODE_TYPE.put("audio/midi", Integer.valueOf(307200));
        RESTRICED_MODE_TYPE.put("audio/x-mid", Integer.valueOf(307200));
        RESTRICED_MODE_TYPE.put("audio/x-midi", Integer.valueOf(307200));
        RESTRICED_MODE_TYPE.put("video/3gpp", Integer.valueOf(307200));
        RESTRICED_MODE_TYPE.put("video/h263", Integer.valueOf(307200));
        RESTRICED_MODE_TYPE.put("video/mp4", Integer.valueOf(307200));
    }

    public void checkMessageSize(int messageSize, int increaseSize, ContentResolver resolver) throws ContentRestrictionException {
        if (messageSize < 0 || increaseSize < 0) {
            throw new ContentRestrictionException("Negative message size or increase size");
        }
        int newSize = messageSize + increaseSize;
        if (!RcseMmsExt.isRcsMode()) {
            if (newSize < 0 || newSize > MmsConfig.getMaxMessageSize() - 4096) {
                throw new ExceedMessageSizeException("Exceed message size limitation");
            }
        }
    }

    public void checkImageContentType(String contentType, Context context, Uri uri) throws ContentRestrictionException {
        if (MmsConfig.getCreationModeEnabled()) {
            if (MmsConfig.getCurrentCreationMode().equals("restrictionmode")) {
                sSupportedImageTypes.clear();
                for (String sContentType : RESTRICED_MODE_TYPE.keySet()) {
                    if (sContentType.startsWith("image")) {
                        sSupportedImageTypes.add(sContentType);
                    }
                }
            } else {
                for (String sContentType2 : ContentType.getImageTypes()) {
                    if (!sSupportedImageTypes.contains(sContentType2)) {
                        sSupportedImageTypes.add(sContentType2);
                    }
                }
            }
        }
        if (contentType == null) {
            throw new ContentRestrictionException("Null content type to be check");
        } else if (!sSupportedImageTypes.contains(contentType) && !checkDrmContentType(context, uri)) {
            throw new UnsupportContentTypeException("Unsupported image content type : " + contentType);
        }
    }

    public void checkAudioContentType(String contentType, Context context, Uri uri) throws ContentRestrictionException {
        if (!sSupportedAudioTypes.contains("audio/amr-wb")) {
            sSupportedAudioTypes.add("audio/amr-wb");
        }
        if (MmsConfig.getCreationModeEnabled()) {
            if (MmsConfig.getCurrentCreationMode().equals("restrictionmode")) {
                sSupportedAudioTypes.clear();
                for (String sContentType : RESTRICED_MODE_TYPE.keySet()) {
                    if (sContentType.startsWith("audio")) {
                        sSupportedAudioTypes.add(sContentType);
                    }
                }
            } else {
                for (String sContentType2 : ContentType.getAudioTypes()) {
                    if (!sSupportedAudioTypes.contains(sContentType2)) {
                        sSupportedAudioTypes.add(sContentType2);
                    }
                }
            }
        }
        if (contentType == null) {
            throw new ContentRestrictionException("Null content type to be check");
        }
        if (!sSupportedAudioTypes.contains("audio/aac-adts")) {
            sSupportedAudioTypes.add("audio/aac-adts");
        }
        if (!sSupportedAudioTypes.contains("audio/flac")) {
            sSupportedAudioTypes.add("audio/flac");
        }
        if (!sSupportedAudioTypes.contains(contentType) && !checkDrmContentType(context, uri)) {
            throw new UnsupportContentTypeException("Unsupported audio content type : " + contentType);
        }
    }

    public void checkVideoContentType(String contentType, Context context, Uri uri) throws ContentRestrictionException {
        if (contentType == null) {
            throw new ContentRestrictionException("Null content type to be check");
        }
        if (MmsConfig.getCreationModeEnabled() && MmsConfig.getCurrentCreationMode().equals("restrictionmode")) {
            sSupportedVideoTypes.clear();
            for (String sContentType : RESTRICED_MODE_TYPE.keySet()) {
                if (sContentType.startsWith("video")) {
                    sSupportedVideoTypes.add(sContentType);
                }
            }
        }
        if (!sSupportedVideoTypes.contains(contentType) && !MediaFile.isVideoFileType(MediaFile.getFileTypeForMimeType(contentType)) && !checkDrmContentType(context, uri)) {
            throw new UnsupportContentTypeException("Unsupported video content type : " + contentType);
        }
    }

    public boolean checkDrmContentType(Context context, Uri uri) {
        if (context == null || uri == null) {
            return false;
        }
        DrmManagerClient dc = new DrmManagerClient(context);
        if (7 == dc.getDrmObjectType(uri, null)) {
            dc.release();
            return true;
        }
        dc.release();
        return false;
    }

    public static Integer getRestricedModeType(String key) {
        return (Integer) RESTRICED_MODE_TYPE.get(key);
    }

    public static Set<String> getRestricedKeySet() {
        return RESTRICED_MODE_TYPE.keySet();
    }

    public static Set<Entry<String, Integer>> getRestricedEntrySet() {
        return RESTRICED_MODE_TYPE.entrySet();
    }
}
