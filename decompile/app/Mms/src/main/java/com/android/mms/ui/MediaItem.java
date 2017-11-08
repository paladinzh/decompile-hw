package com.android.mms.ui;

import android.text.TextUtils;
import com.android.mms.model.MediaModel;
import java.text.NumberFormat;

public class MediaItem {
    public int mAudioDuration;
    public boolean mAudioPlaying;
    public int mIndex;
    public boolean mIsAutoPlayAudio;
    public int mItemMode;
    public MediaModel mMedia;
    public int mPageCnt;
    public CharSequence mSubject;
    public int mTotalPage;

    public String toString() {
        boolean z = true;
        StringBuilder append = new StringBuilder().append("MediaModel: ").append(this.mMedia).append(" Item_mode_image:").append(this.mItemMode == 3).append(" is divider: ");
        if (this.mItemMode != 1) {
            z = false;
        }
        return append.append(z).append("\n").toString();
    }

    public MediaItem(int mode, MediaModel media, int index) {
        this(mode, media, false, index);
    }

    public MediaItem(int mode, MediaModel media, boolean isAutoPlayAudio, int index) {
        if (mode == 1) {
            throw new RuntimeException("Wrong mode as ITEM_MODE_DEVIDER!");
        }
        this.mItemMode = mode;
        this.mMedia = media;
        this.mIsAutoPlayAudio = isAutoPlayAudio;
        this.mAudioDuration = this.mMedia.getDuration();
        this.mIndex = index;
    }

    public MediaItem(int totalPage, int pageCnt) {
        this.mItemMode = 1;
        this.mMedia = null;
        this.mTotalPage = totalPage;
        this.mPageCnt = pageCnt;
    }

    public MediaItem(CharSequence subject) {
        this.mItemMode = 8;
        this.mMedia = null;
        this.mSubject = subject;
    }

    public void audioStopped() {
        this.mAudioPlaying = false;
    }

    public void audioPlayStarted() {
        this.mAudioPlaying = true;
    }

    public void setAutoPlayAudio(boolean isAuto) {
        this.mIsAutoPlayAudio = isAuto;
    }

    public boolean canBeSaved() {
        if (this.mMedia != null) {
            return this.mItemMode == 3 || this.mItemMode == 4 || this.mItemMode == 5 || this.mItemMode == 6 || this.mItemMode == 7;
        } else {
            return false;
        }
    }

    public boolean isSubjectEmpty() {
        return TextUtils.isEmpty(this.mSubject);
    }

    public boolean isSelectedMediaItem() {
        if (this.mItemMode == 1 || this.mItemMode == 2 || this.mItemMode == 8) {
            return true;
        }
        return false;
    }

    public boolean isTextMode() {
        return this.mItemMode == 2;
    }

    public boolean isHeaderMode() {
        return this.mItemMode == 8;
    }

    public boolean isAudioMode() {
        return this.mItemMode == 4;
    }

    public String getPageText() {
        NumberFormat nf = NumberFormat.getIntegerInstance();
        return new StringBuffer().append(nf.format((long) this.mPageCnt)).append("/").append(nf.format((long) this.mTotalPage)).toString();
    }
}
