package com.android.mms.attachment.utils;

public class ContentType {
    private ContentType() {
    }

    public static boolean isTextType(String contentType) {
        if ("text/plain".equals(contentType) || "text/html".equals(contentType)) {
            return true;
        }
        return "application/vnd.wap.xhtml+xml".equals(contentType);
    }

    public static boolean isMediaType(String contentType) {
        if (isImageType(contentType) || isVideoType(contentType) || isAudioType(contentType)) {
            return true;
        }
        return isVCardType(contentType);
    }

    public static boolean isImageType(String contentType) {
        return contentType != null ? contentType.startsWith("image/") : false;
    }

    public static boolean isAudioType(String contentType) {
        if (contentType != null) {
            return !contentType.startsWith("audio/") ? contentType.equalsIgnoreCase("application/ogg") : true;
        } else {
            return false;
        }
    }

    public static boolean isVideoType(String contentType) {
        return contentType != null ? contentType.startsWith("video/") : false;
    }

    public static boolean isVCardType(String contentType) {
        return contentType != null ? contentType.equalsIgnoreCase("text/x-vCard") : false;
    }
}
