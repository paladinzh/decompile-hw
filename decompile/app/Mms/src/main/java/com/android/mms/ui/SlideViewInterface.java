package com.android.mms.ui;

import android.graphics.Bitmap;
import android.net.Uri;
import com.android.mms.MmsConfig;
import java.util.Map;

public interface SlideViewInterface extends ViewInterface {
    public static final long SLIDE_SIZE_LIMIT = (((long) MmsConfig.getMaxMessageSize()) / 1024);
    public static final boolean mIsShowAttachmentSize = MmsConfig.getIsShowAttachmentSize();

    void pauseAudio();

    void pauseVideo();

    void seekAudio(int i);

    void seekVideo(int i);

    void setAudio(Uri uri, String str, Map<String, ?> map);

    boolean setGifImage(String str, Uri uri);

    void setImage(String str, Bitmap bitmap);

    void setImageRegionFit(String str);

    void setImageVisibility(boolean z);

    void setSize(int i);

    void setText(String str, String str2);

    void setTextVisibility(boolean z);

    void setVcalendar(String str, String str2);

    void setVcard(String str, String str2);

    void setVideo(String str, Uri uri);

    void setVideoThumbnail(String str, Bitmap bitmap);

    void setVideoVisibility(boolean z);

    void startAudio();

    void startVideo();

    void stopAudio();

    void stopVideo();
}
