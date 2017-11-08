package com.android.keyguard.hwlockscreen;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.os.Handler.Callback;
import android.os.Message;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.android.keyguard.R$dimen;
import com.android.keyguard.R$drawable;
import com.android.keyguard.R$id;
import com.android.keyguard.R$string;
import com.huawei.keyguard.HwKeyguardUpdateMonitor;
import com.huawei.keyguard.data.KeyguardInfo;
import com.huawei.keyguard.data.MusicInfo;
import com.huawei.keyguard.events.AppHandler;
import com.huawei.keyguard.inf.ILiftAbleView;
import com.huawei.keyguard.inf.ILiftAbleView.ILiftStateListener;
import com.huawei.keyguard.monitor.HwLockScreenReporter;
import com.huawei.keyguard.monitor.StateMonitor;
import com.huawei.keyguard.util.HwLog;
import com.huawei.keyguard.util.HwUnlockUtils;
import com.huawei.keyguard.util.MultiDpiUtil;
import com.huawei.keyguard.util.MusicUtils;
import com.huawei.keyguard.view.KgViewUtils;
import fyusion.vislib.BuildConfig;

public class HwMusicView extends RelativeLayout implements Callback, OnClickListener {
    private boolean isHasNotification;
    private ImageView mAlbumImageView;
    private String mArtist;
    private TextView mArtistText;
    private ILiftAbleView mInfoCenterView;
    private ILiftStateListener mInfoModeCallback;
    private TextView mLockTip;
    private int mLockTipDistance;
    private int mLyricPosX;
    private int mLyricPosY;
    private boolean mLyricShowing;
    private FrameLayout mMusicFramelayout;
    private RelativeLayout mMusicInfoView;
    private FrameLayout mMusicTopLayout;
    private ImageView mPlayButton;
    private String mSongName;
    private TextView mSongNameText;
    private ImageView mTopAlbumImageView;
    private TextView mTopArtistText;
    private ImageView mTopPlayButton;
    private TextView mTopSongNameText;
    private boolean mViewLifted;

    public HwMusicView(Context context) {
        this(context, null);
    }

    public HwMusicView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mSongName = BuildConfig.FLAVOR;
        this.mArtist = BuildConfig.FLAVOR;
        this.mAlbumImageView = null;
        this.mLyricPosY = 0;
        this.mLyricPosX = 0;
        this.mViewLifted = false;
        this.mLyricShowing = false;
        this.isHasNotification = false;
        this.mInfoModeCallback = new ILiftStateListener() {
            public void onLiftModeStateChange(float delta) {
                if (HwMusicView.this.mLockTipDistance <= 0) {
                    HwMusicView.this.initLockTip();
                    return;
                }
                boolean lifted = Math.abs(delta) > ((float) HwMusicView.this.mLockTipDistance);
                if (HwMusicView.this.mViewLifted != lifted) {
                    HwMusicView.this.mViewLifted = lifted;
                    if (HwMusicView.this.mLockTip != null) {
                        if (lifted) {
                            HwMusicView.this.mLockTip.setVisibility(4);
                        } else {
                            HwMusicView.this.mLockTip.setVisibility(0);
                        }
                    }
                }
            }
        };
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
        initMusicView();
    }

    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        HwLog.d("HwMusicView", "onAttachedToWindow ");
        View tmpView = getRootView();
        if (tmpView != null) {
            tmpView = tmpView.findViewById(R$id.info_center_container);
        }
        if (tmpView instanceof ILiftAbleView) {
            this.mInfoCenterView = (ILiftAbleView) tmpView;
            this.mInfoCenterView.registerLiftStateListener(this.mInfoModeCallback);
        }
        AppHandler.addListener(this);
        updateView();
    }

    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        HwLog.d("HwMusicView", "onDetachedFromWindow ");
        if (this.mInfoCenterView != null) {
            this.mInfoCenterView.unregisterLiftStateListener(this.mInfoModeCallback);
            this.mInfoModeCallback = null;
        }
        if (this.mLyricShowing) {
            MusicUtils.sendLyricsBroadcast(getContext(), false, 0, 0);
        }
        AppHandler.removeListener(this);
    }

    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (HwUnlockUtils.isTablet() && this.isHasNotification) {
            refreshTopMusicPlayRes();
        }
        if (this.mLockTip != null) {
            this.mLockTip.setText(R$string.slide_to_unlock);
        }
        Resources res = getContext().getResources();
        if (HwUnlockUtils.isTablet() && res.getConfiguration().orientation == 2) {
            this.mLyricPosX = res.getDimensionPixelOffset(MultiDpiUtil.getResId(this.mContext, R$dimen.music_lyric_display_position_X));
        }
    }

    private void initMusicView() {
        ImageView imgView = (ImageView) findViewById(R$id.hwmusic_prev);
        if (imgView != null) {
            imgView.setOnClickListener(this);
            imgView.setContentDescription(getResources().getString(R$string.keyguard_accessibility_transport_prev_description_new));
        }
        imgView = (ImageView) findViewById(R$id.hwmusic_next);
        if (imgView != null) {
            imgView.setOnClickListener(this);
            imgView.setContentDescription(getResources().getString(R$string.keyguard_accessibility_transport_next_description_new));
        }
        imgView = (ImageView) findViewById(R$id.hwmusic_top_next);
        if (imgView != null) {
            imgView.setContentDescription(getResources().getString(R$string.keyguard_accessibility_transport_next_description_new));
            imgView.setOnClickListener(this);
        }
        this.mTopAlbumImageView = (ImageView) findViewById(R$id.music_top_album);
        this.mPlayButton = (ImageView) findViewById(R$id.hwmusic_play);
        this.mPlayButton.setOnClickListener(this);
        this.mTopPlayButton = (ImageView) findViewById(R$id.hwmusic_top_play);
        this.mTopPlayButton.setOnClickListener(this);
        this.mSongNameText = (TextView) findViewById(R$id.hwmusic_name);
        KgViewUtils.setSelected(this.mSongNameText, true);
        this.mTopSongNameText = (TextView) findViewById(R$id.music_top_name);
        KgViewUtils.setSelected(this.mTopSongNameText, true);
        this.mArtistText = (TextView) findViewById(R$id.hwmusic_artist);
        KgViewUtils.setSelected(this.mArtistText, true);
        this.mTopArtistText = (TextView) findViewById(R$id.music_top_artist);
        KgViewUtils.setSelected(this.mTopArtistText, true);
        this.mAlbumImageView = (ImageView) findViewById(R$id.music_album);
        this.mMusicFramelayout = (FrameLayout) findViewById(R$id.music_framelayout);
        Resources res = getContext().getResources();
        if (!HwUnlockUtils.isTablet()) {
            this.mLyricPosX = getWidth() / 2;
        } else if (this.mContext.getResources().getConfiguration().orientation == 2) {
            this.mLyricPosX = res.getDimensionPixelOffset(MultiDpiUtil.getResId(this.mContext, R$dimen.music_lyric_display_position_X));
        } else {
            this.mLyricPosX = getWidth() / 2;
        }
        this.mLyricPosY = res.getDimensionPixelOffset(R$dimen.music_lyric_display_position_y);
        MusicUtils.checkMusicViewMultiDpiChanged(this.mMusicFramelayout);
        this.mMusicTopLayout = (FrameLayout) findViewById(R$id.music_top_layout);
        this.mMusicInfoView = (RelativeLayout) findViewById(R$id.magazine_music_info);
        this.mLockTip = (TextView) findViewById(R$id.music_locktip);
        if (HwUnlockUtils.isTablet()) {
            this.isHasNotification = KeyguardInfo.getInst(this.mContext).getKeyguardNotificationSize() > 0;
            if (this.isHasNotification) {
                this.mMusicTopLayout.setVisibility(0);
                this.mMusicInfoView.setVisibility(8);
            } else {
                this.mMusicTopLayout.setVisibility(8);
                this.mMusicInfoView.setVisibility(0);
            }
            updateLyricShowingState();
        }
    }

    private void updateContentDescription() {
        ImageView imgView = (ImageView) findViewById(R$id.hwmusic_prev);
        if (imgView != null) {
            imgView.setContentDescription(getResources().getString(R$string.keyguard_accessibility_transport_prev_description_new));
        }
        imgView = (ImageView) findViewById(R$id.hwmusic_next);
        if (imgView != null) {
            imgView.setContentDescription(getResources().getString(R$string.keyguard_accessibility_transport_next_description_new));
        }
        imgView = (ImageView) findViewById(R$id.hwmusic_top_next);
        if (imgView != null) {
            imgView.setContentDescription(getResources().getString(R$string.keyguard_accessibility_transport_next_description_new));
        }
    }

    public void onClick(View v) {
        int resId = v.getId();
        if (resId == R$id.hwmusic_prev) {
            MusicUtils.sendMediaButtonClick(this.mContext, 88);
            HwLockScreenReporter.report(this.mContext, 163, BuildConfig.FLAVOR);
        } else if (resId == R$id.hwmusic_next || resId == R$id.hwmusic_top_next) {
            MusicUtils.sendMediaButtonClick(this.mContext, 87);
            HwLockScreenReporter.report(this.mContext, 164, BuildConfig.FLAVOR);
        } else if (resId == R$id.hwmusic_play || resId == R$id.hwmusic_top_play) {
            int i;
            boolean playNow = 3 == MusicUtils.getMusicState();
            StateMonitor.getInst().triggerEvent(playNow ? 402 : 401);
            MusicUtils.sendMediaButtonClick(this.mContext, 85);
            Context context = this.mContext;
            if (playNow) {
                i = 168;
            } else {
                i = 166;
            }
            HwLockScreenReporter.report(context, i, BuildConfig.FLAVOR);
        }
    }

    public void updateAlbumBitmap(Bitmap bitmap) {
        if (bitmap == null) {
            HwLog.w("HwMusicView", "updateAlbumBitmap bitmap is null");
            return;
        }
        BitmapDrawable drawable = new BitmapDrawable(getContext().getResources(), bitmap);
        if (this.mAlbumImageView != null && this.mAlbumImageView.getVisibility() == 0) {
            HwLog.w("HwMusicView", "setBackground");
            this.mAlbumImageView.setBackground(drawable);
        }
        if (this.mTopAlbumImageView != null && this.mTopAlbumImageView.getVisibility() == 0) {
            HwLog.w("HwMusicView", "setBackground");
            this.mTopAlbumImageView.setBackground(drawable);
        }
    }

    public void refreshMusicText(String songName, String artist) {
        this.mSongName = songName;
        this.mArtist = artist;
        if (this.mSongNameText != null && this.mSongNameText.getVisibility() == 0) {
            this.mSongNameText.setText(this.mSongName);
        }
        if (this.mArtistText != null && this.mArtistText.getVisibility() == 0) {
            this.mArtistText.setText(this.mArtist);
        }
        if (this.mTopSongNameText != null && this.mTopSongNameText.getVisibility() == 0) {
            this.mTopSongNameText.setText(this.mSongName);
        }
        if (this.mTopArtistText != null && this.mTopArtistText.getVisibility() == 0) {
            this.mTopArtistText.setText(this.mArtist);
        }
    }

    private void refreshMusicPlayRes() {
        if (this.mPlayButton == null) {
            HwLog.w("HwMusicView", "refreshMusicPlayRes mPlayButton is null");
            return;
        }
        int resId;
        if (MusicInfo.getInst().isPlaying()) {
            this.mPlayButton.setImageResource(R$drawable.hwlockscreen_music_pause);
            resId = R$string.keyguard_accessibility_transport_pause_description_new;
        } else {
            this.mPlayButton.setImageResource(R$drawable.hwlockscreen_music_play);
            resId = R$string.keyguard_accessibility_transport_play_description_new;
        }
        this.mPlayButton.setContentDescription(getContext().getText(resId));
        Rect outRect = new Rect();
        this.mPlayButton.getHitRect(outRect);
        invalidate(outRect);
    }

    private void refreshTopMusicPlayRes() {
        if (this.mTopPlayButton == null) {
            HwLog.w("HwMusicView", "refreshMusicPlayRes mPlayButton is null");
            return;
        }
        int resId;
        if (MusicInfo.getInst().isPlaying()) {
            this.mTopPlayButton.setImageResource(R$drawable.hwlockscreen_sport_music_pause);
            resId = R$string.keyguard_accessibility_transport_pause_description_new;
        } else {
            this.mTopPlayButton.setImageResource(R$drawable.hwlockscreen_sport_music_play);
            resId = R$string.keyguard_accessibility_transport_play_description_new;
        }
        this.mTopPlayButton.setContentDescription(getContext().getText(resId));
        Rect outRect = new Rect();
        this.mTopPlayButton.getHitRect(outRect);
        invalidate(outRect);
    }

    private void updateNotification2Music() {
        MusicInfo musicInfo = MusicInfo.getInst();
        refreshMusicText(musicInfo.getSongName(), musicInfo.getArtist());
        refreshMusicPlayRes();
        updateAlbumBitmap(musicInfo.getCircleBitmap(this.mContext));
        this.mMusicTopLayout.setVisibility(8);
        this.mMusicInfoView.setVisibility(0);
        updateLyricShowingState();
        updateContentDescription();
    }

    private void updateMusic2Notification() {
        MusicInfo musicInfo = MusicInfo.getInst();
        refreshMusicText(musicInfo.getSongName(), musicInfo.getArtist());
        refreshTopMusicPlayRes();
        updateAlbumBitmap(musicInfo.getCircleBitmap(this.mContext));
        this.mMusicTopLayout.setVisibility(0);
        this.mMusicInfoView.setVisibility(8);
        updateLyricShowingState();
        updateContentDescription();
    }

    private void updateView() {
        MusicInfo musicInfo = MusicInfo.getInst();
        HwLog.i("HwMusicView", "MUSIC_STATE_CHANGE " + musicInfo.isPlaying());
        refreshMusicText(musicInfo.getSongName(), musicInfo.getArtist());
        refreshMusicPlayRes();
        updateAlbumBitmap(musicInfo.getCircleBitmap(this.mContext));
        updateContentDescription();
    }

    public boolean handleMessage(Message msg) {
        switch (msg.what) {
            case 4:
                boolean z;
                if (KeyguardInfo.getInst(this.mContext).getKeyguardNotificationSize() > 0) {
                    z = true;
                } else {
                    z = false;
                }
                this.isHasNotification = z;
                if (!this.isHasNotification) {
                    updateNotification2Music();
                    break;
                }
                updateMusic2Notification();
                break;
            case 10:
                updateLyricShowingState();
                break;
            case 110:
                updateView();
                updateLyricShowingState();
                break;
        }
        return false;
    }

    protected void updateLyricShowingState() {
        HwKeyguardUpdateMonitor monitor = HwKeyguardUpdateMonitor.getInstance(this.mContext);
        boolean showingLyric = (getVisibility() == 0 && monitor.isShowing()) ? (this.mViewLifted || monitor.isInBouncer() || monitor.isOccluded()) ? false : true : false;
        MusicInfo musicInfo = MusicInfo.getInst();
        if (showingLyric && (getParent() instanceof View)) {
            showingLyric = ((View) getParent()).getAlpha() > 0.5f;
        }
        if (showingLyric) {
            showingLyric = (TextUtils.isEmpty(musicInfo.getSongName()) || !"com.android.mediacenter".equalsIgnoreCase(musicInfo.getPlayerApp())) ? false : !this.isHasNotification;
        }
        HwLog.w("HwMusicView", "show lyric " + showingLyric + " playing: " + musicInfo.isPlaying() + " " + musicInfo.getSongName() + " " + monitor.isShowing() + "  " + monitor.isInBouncer() + " " + monitor.isOccluded() + " Lift" + this.mViewLifted);
        if (showingLyric == this.mLyricShowing) {
            HwLog.v("HwMusicView", "Skip notice Lyric state change. " + showingLyric);
            return;
        }
        this.mLyricShowing = showingLyric;
        noticeShowLyrice(this.mLyricShowing);
    }

    private void noticeShowLyrice(boolean show) {
        if (show) {
            MusicUtils.sendLyricsBroadcast(getContext(), true, this.mLyricPosX, this.mLyricPosY);
        } else {
            MusicUtils.sendLyricsBroadcast(getContext(), false, 0, 0);
        }
    }

    private void initLockTip() {
        if (this.mLockTip == null) {
            HwLog.w("HwMusicView", "init lockTip is null");
            return;
        }
        int yPos = this.mLockTip.getTop();
        int viewHeight = getHeight();
        this.mLockTipDistance = viewHeight - yPos;
        HwLog.d("HwMusicView", "inti yPos=" + yPos + ",viewHeight" + viewHeight + ",mLockTipDistance=" + this.mLockTipDistance);
    }
}
