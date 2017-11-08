package com.huawei.keyguard.data;

import android.content.Context;
import android.graphics.Bitmap;
import android.media.MediaMetadata;
import android.media.session.PlaybackState;
import com.android.keyguard.R$drawable;
import com.huawei.keyguard.GlobalContext;
import com.huawei.keyguard.HwKeyguardUpdateMonitor;
import com.huawei.keyguard.cover.CoverViewManager;
import com.huawei.keyguard.events.AppHandler;
import com.huawei.keyguard.monitor.StateMonitor;
import com.huawei.keyguard.util.BitmapUtils;
import com.huawei.keyguard.util.HwLog;
import com.huawei.keyguard.util.MusicUtils;
import fyusion.vislib.BuildConfig;
import java.util.ArrayList;
import java.util.List;

public class MusicInfo {
    private static MusicInfo sInst = new MusicInfo();
    private Bitmap mAlbumBitmap = null;
    private String mArtist = BuildConfig.FLAVOR;
    private Bitmap mBlurBitmap = null;
    private Bitmap mCircleBitmap = null;
    private long mCurrentPosition = 0;
    private boolean mIsShowingMusic = false;
    private List<String> mLyricList = new ArrayList();
    private String mLyricString = BuildConfig.FLAVOR;
    private int mPlayState = 0;
    private String mPlayerApp = BuildConfig.FLAVOR;
    private String mSongName = BuildConfig.FLAVOR;
    private List<Integer> mTimeList = new ArrayList();
    private long mTimeWhenPositionChange = 0;

    public static MusicInfo getInst() {
        return sInst;
    }

    public String getSongName() {
        return this.mSongName;
    }

    public String getArtist() {
        return this.mArtist;
    }

    public Bitmap getCircleBitmap(Context context) {
        if (this.mCircleBitmap != null || context == null) {
            return this.mCircleBitmap;
        }
        return BitmapUtils.decodeResource(context, R$drawable.hw_music_album_default, false);
    }

    public Bitmap getAlbumBitmap(Context context) {
        if (this.mAlbumBitmap != null || context == null) {
            return this.mAlbumBitmap;
        }
        return BitmapUtils.decodeResource(context, R$drawable.hw_music_album_default);
    }

    public Bitmap getBlurBitmap(Context context) {
        if (this.mBlurBitmap != null || context == null) {
            return this.mBlurBitmap;
        }
        return BitmapUtils.decodeResource(context, R$drawable.hw_music_no_album);
    }

    public void reset() {
        this.mPlayState = 0;
        this.mSongName = BuildConfig.FLAVOR;
        this.mArtist = BuildConfig.FLAVOR;
        this.mAlbumBitmap = null;
        this.mBlurBitmap = null;
        this.mIsShowingMusic = false;
    }

    public int getPlayState() {
        return this.mPlayState;
    }

    public boolean isPlaying() {
        return 3 == this.mPlayState;
    }

    public void updateState(PlaybackState state) {
        if (state == null) {
            HwLog.w("MusicInfo", "onPlaybackStateChanged state is null");
            return;
        }
        this.mPlayState = state.getState();
        if (3 == this.mPlayState) {
            this.mCurrentPosition = state.getPosition();
            this.mTimeWhenPositionChange = System.currentTimeMillis();
            syncToLyricUI();
        } else {
            HwKeyguardUpdateMonitor monitor = HwKeyguardUpdateMonitor.getInstance();
            Context mContext = GlobalContext.getContext();
            CoverViewManager coverViewManager = null;
            if (mContext != null) {
                coverViewManager = CoverViewManager.getInstance(mContext);
            }
            if (!(monitor == null || monitor.isShowing() || monitor.isOccluded() || r0 == null || !r0.isCoverOpen())) {
                HwLog.v("MusicInfo", "reset music showing state");
                this.mIsShowingMusic = false;
            }
        }
        HwLog.v("MusicInfo", "PlaybackState changed to: " + this.mPlayState);
        if (3 == this.mPlayState) {
            StateMonitor.getInst().cancelEvent(411);
        } else if (2 == this.mPlayState) {
            StateMonitor.getInst().cancelEvent(412);
        }
    }

    public void updateData(final Context context, MediaMetadata metadata) {
        if (metadata == null) {
            HwLog.w("MusicInfo", "updateMediaMetaData metadata = " + metadata);
            return;
        }
        this.mArtist = metadata.getString("android.media.metadata.ARTIST");
        this.mSongName = metadata.getString("android.media.metadata.TITLE");
        this.mAlbumBitmap = metadata.getBitmap("android.media.metadata.ART");
        final String lyricString = metadata.getString("android.media.metadata.LYRIC");
        HwLog.v("MusicInfo", "MediaMetadata changed to: " + this.mSongName + "; album: " + this.mAlbumBitmap);
        if (this.mAlbumBitmap == null) {
            this.mAlbumBitmap = metadata.getBitmap("android.media.metadata.ALBUM_ART");
        }
        GlobalContext.getBackgroundHandler().post(new Runnable() {
            public void run() {
                if (MusicInfo.this.mAlbumBitmap == null) {
                    MusicInfo.this.mCircleBitmap = MusicInfo.this.mBlurBitmap = null;
                    MusicInfo.this.syncToUI();
                    return;
                }
                MusicInfo.this.mCircleBitmap = MusicUtils.getAlbumMaskBitmap(context, MusicInfo.this.mAlbumBitmap);
                MusicInfo.this.mBlurBitmap = MusicUtils.getAlbumBlurBitmap(context, MusicInfo.this.mAlbumBitmap);
                MusicInfo.this.syncToUI();
            }
        });
        if (!isSameLyric(this.mLyricString, lyricString)) {
            this.mLyricString = lyricString;
            GlobalContext.getBackgroundHandler().post(new Runnable() {
                public void run() {
                    MusicUtils.parseLyricStringToList(lyricString, MusicInfo.this.mLyricList, MusicInfo.this.mTimeList);
                    MusicInfo.this.syncToLyricUI();
                }
            });
        }
    }

    public void setPlayerApp(String pkgName) {
        this.mPlayerApp = pkgName;
        HwLog.w("MusicInfo", "Player changed to: " + pkgName);
    }

    public String getPlayerApp() {
        return this.mPlayerApp;
    }

    private void syncToUI() {
        HwLog.w("MusicInfo", "Sync UI");
        AppHandler.sendMessage(110);
    }

    private void syncToLyricUI() {
        HwLog.d("MusicInfo", "Sync Lyric UI");
        AppHandler.sendMessage(111);
    }

    public boolean isPositionValid() {
        return -1 != this.mCurrentPosition;
    }

    public long getNowPosition() {
        HwLog.d("MusicInfo", "mCurrentPosition is : " + this.mCurrentPosition + " interval is : " + (System.currentTimeMillis() - this.mTimeWhenPositionChange));
        return (this.mCurrentPosition + System.currentTimeMillis()) - this.mTimeWhenPositionChange;
    }

    private boolean isSameLyric(String oldLyric, String newLyric) {
        if (oldLyric == null && newLyric == null) {
            return true;
        }
        if (oldLyric == null || newLyric == null) {
            return false;
        }
        return oldLyric.equalsIgnoreCase(newLyric);
    }

    public List<String> getLyricContentList() {
        return this.mLyricList;
    }

    public List<Integer> getLyricTimeList() {
        return this.mTimeList;
    }

    public boolean needShowMusicView() {
        if (!this.mIsShowingMusic && isPlaying()) {
            this.mIsShowingMusic = true;
        }
        HwLog.v("MusicInfo", "showing music " + this.mIsShowingMusic + "  " + isPlaying());
        return this.mIsShowingMusic;
    }

    public void resetShowingState() {
        this.mIsShowingMusic = isPlaying();
    }
}
