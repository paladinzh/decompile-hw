package com.android.keyguard.hwlockscreen;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.keyguard.KeyguardUpdateMonitorCallback;
import com.android.keyguard.R$dimen;
import com.android.keyguard.R$drawable;
import com.android.keyguard.R$id;
import com.android.keyguard.R$string;
import com.huawei.keyguard.GlobalContext;
import com.huawei.keyguard.data.KeyguardInfo;
import com.huawei.keyguard.data.MusicInfo;
import com.huawei.keyguard.data.SportInfo;
import com.huawei.keyguard.data.SportInfo.TrackingStatus;
import com.huawei.keyguard.events.AppHandler;
import com.huawei.keyguard.monitor.HwLockScreenReporter;
import com.huawei.keyguard.monitor.StateMonitor;
import com.huawei.keyguard.theme.KeyguardTheme;
import com.huawei.keyguard.util.HwLog;
import com.huawei.keyguard.util.HwUnlockUtils;
import com.huawei.keyguard.util.MusicUtils;
import fyusion.vislib.BuildConfig;

public class HwSportView extends RelativeLayout implements Callback, OnClickListener {
    private boolean isShowing = true;
    private RelativeLayout mCalorierLayout;
    private TextView mCalorierText;
    private RelativeLayout mDistanceLayout;
    private TextView mDistanceText;
    private View mDividerView;
    private LinearLayout mDurationPaceHrLayout;
    private TextView mDuratonText;
    private ImageView mGPSImageView;
    private TextView mGPSTextView;
    private RelativeLayout mHRLayout;
    private TextView mHRText;
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            if (msg.what == 102) {
                HwLog.w("HwSportView", "MSG_SPORT_TRACING_STATE_CHANGED has notification?:" + HwSportView.this.isHasNotificationOnKeyguard());
                if (HwSportView.this.isHasNotificationOnKeyguard()) {
                    HwSportView.this.refreshSportTopSwitchRes();
                } else {
                    HwSportView.this.refreshSportSwitchRes();
                }
            }
        }
    };
    private TextView mPaceText;
    private ImageView mRunningButton;
    private RelativeLayout mSportHealthLayout;
    private ImageView mSportMusicAlbum;
    private TextView mSportMusicArtist;
    private RelativeLayout mSportMusicFLayout;
    private ImageView mSportMusicNext;
    private ImageView mSportMusicPlay;
    private TextView mSportMusicSongName;
    private FrameLayout mSportSwitchLayout;
    private FrameLayout mSportTopLayout;
    private TextView mTopDistanceText;
    private ImageView mTopRunningButton;
    private KeyguardUpdateMonitorCallback mUpdateCallback = new KeyguardUpdateMonitorCallback() {
        public void onKeyguardVisibilityChanged(boolean showing) {
            HwSportView.this.isShowing = showing;
            if (!MusicInfo.getInst().isPlaying() && HwSportView.this.oldState) {
                HwSportView.this.oldState = false;
                HwSportView.this.updateSportMusic2Sport();
            }
        }
    };
    private int oldSportState = 0;
    private boolean oldState = false;

    public HwSportView(Context context) {
        super(context);
    }

    public HwSportView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
        this.mSportMusicAlbum = (ImageView) findViewById(R$id.music_top_album);
        this.mSportMusicPlay = (ImageView) findViewById(R$id.hwmusic_top_play);
        this.mSportMusicNext = (ImageView) findViewById(R$id.hwmusic_top_next);
        this.mSportMusicSongName = (TextView) findViewById(R$id.music_top_name);
        this.mSportMusicArtist = (TextView) findViewById(R$id.music_top_artist);
        this.mTopDistanceText = (TextView) findViewById(R$id.distance_top_value);
        this.mTopRunningButton = (ImageView) findViewById(R$id.sport_top_switch);
        this.mGPSImageView = (ImageView) findViewById(R$id.gps_signal_bitmap);
        this.mGPSTextView = (TextView) findViewById(R$id.gps_no_signal_name);
        this.mDistanceText = (TextView) findViewById(R$id.distance_name);
        this.mDuratonText = (TextView) findViewById(R$id.duration_name_value);
        this.mPaceText = (TextView) findViewById(R$id.pace_name_value);
        this.mCalorierLayout = (RelativeLayout) findViewById(R$id.sport_calorie_layout);
        this.mHRLayout = (RelativeLayout) findViewById(R$id.sport_hr_layout);
        this.mCalorierText = (TextView) findViewById(R$id.calorie_name_value);
        this.mHRText = (TextView) findViewById(R$id.hr_name_value);
        this.mRunningButton = (ImageView) findViewById(R$id.sport_switch);
        this.mSportHealthLayout = (RelativeLayout) findViewById(R$id.sport_health_layout);
        this.mSportTopLayout = (FrameLayout) findViewById(R$id.sport_top_layout);
        this.mSportMusicFLayout = (RelativeLayout) findViewById(R$id.sport_music_layout);
        this.mSportSwitchLayout = (FrameLayout) findViewById(R$id.sport_framelayout);
        this.mDividerView = findViewById(R$id.divider_view);
        this.mDurationPaceHrLayout = (LinearLayout) findViewById(R$id.duration_pace_hr_layout);
        this.mDistanceLayout = (RelativeLayout) findViewById(R$id.distance_layout);
        this.mSportMusicPlay.setOnClickListener(this);
        this.mSportMusicNext.setOnClickListener(this);
        this.mRunningButton.setOnClickListener(this);
        this.mTopRunningButton.setOnClickListener(this);
        AppHandler.sendMessage(115);
        if (HwUnlockUtils.isTablet()) {
            if (isHasNotificationOnKeyguard()) {
                this.mSportTopLayout.setVisibility(0);
                this.mSportHealthLayout.setVisibility(8);
            } else {
                this.mSportTopLayout.setVisibility(8);
                this.mSportHealthLayout.setVisibility(0);
            }
            setSportTopLayout(MusicInfo.getInst().isPlaying());
        }
    }

    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (MusicInfo.getInst().isPlaying()) {
            updateMusicView();
        } else {
            updateSportMusic2Sport();
        }
        AppHandler.addListener(this);
        KeyguardUpdateMonitor.getInstance(this.mContext).registerCallback(this.mUpdateCallback);
    }

    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        AppHandler.removeListener(this);
        KeyguardUpdateMonitor.getInstance(this.mContext).removeCallback(this.mUpdateCallback);
    }

    private void updateGpsSignal(int signal) {
        if (this.mGPSImageView == null) {
            HwLog.w("HwSportView", "mGPSImageView is null");
            return;
        }
        switch (signal) {
            case 0:
                this.mGPSImageView.setVisibility(8);
                this.mGPSTextView.setVisibility(0);
                this.mGPSTextView.setText(R$string.gps_sport_signal_text_closed);
                break;
            case 1:
                this.mGPSImageView.setVisibility(8);
                this.mGPSTextView.setVisibility(0);
                this.mGPSTextView.setText(R$string.gps_sport_signal_text_search);
                break;
            case 2:
                this.mGPSImageView.setVisibility(0);
                this.mGPSTextView.setVisibility(8);
                this.mGPSImageView.setImageResource(R$drawable.ic_health_running_signal1);
                break;
            case 3:
                this.mGPSImageView.setVisibility(0);
                this.mGPSTextView.setVisibility(8);
                this.mGPSImageView.setImageResource(R$drawable.ic_health_running_signal2);
                break;
            case 4:
                this.mGPSImageView.setVisibility(0);
                this.mGPSTextView.setVisibility(8);
                this.mGPSImageView.setImageResource(R$drawable.ic_health_running_signal3);
                break;
        }
    }

    public void onClick(View v) {
        int resId = v.getId();
        if (resId == R$id.hwmusic_top_play) {
            if (3 == MusicUtils.getMusicState()) {
                StateMonitor.getInst().triggerEvent(402);
            } else {
                StateMonitor.getInst().triggerEvent(401);
            }
            HwLog.d("HwSportView", "sendMediaButtonClick  pause");
            MusicUtils.sendMediaButtonClick(this.mContext, 85);
        } else if (resId == R$id.hwmusic_top_next) {
            HwLog.d("HwSportView", "sendMediaButtonClick  next");
            MusicUtils.sendMediaButtonClick(this.mContext, 87);
        } else if (resId == R$id.sport_switch || resId == R$id.sport_top_switch) {
            HwLog.d("HwSportView", "mRunningButton is clicked");
            sendRunningButtonClick();
        }
    }

    private void sendRunningButtonClick() {
        SportInfo sportInfo = SportInfo.getInst();
        if (sportInfo.getTrackingState() == 10) {
            sportInfo.executeRemoteCommand(101);
            HwLockScreenReporter.report(this.mContext, 165, BuildConfig.FLAVOR);
        } else if (sportInfo.getTrackingState() == 11) {
            sportInfo.executeRemoteCommand(100);
            HwLockScreenReporter.report(this.mContext, 167, BuildConfig.FLAVOR);
        }
    }

    public boolean handleMessage(Message msg) {
        switch (msg.what) {
            case 4:
                updateSportNotificationView();
                break;
            case 110:
                updateMusicView();
                break;
            case 115:
                if (msg.arg1 != 1) {
                    updateSportView();
                    break;
                }
                removeAllViews();
                KeyguardTheme.getInst().checkStyle(GlobalContext.getContext(), false, false);
                return true;
        }
        return false;
    }

    public void updateSportMusic2Sport() {
        SportInfo.getInst().setTopMusicShowing(false);
        this.mSportMusicFLayout.setVisibility(8);
        this.mDividerView.setVisibility(8);
        if (HwUnlockUtils.isTablet()) {
            setDistanceLayout(false);
            setDurationPaceHrLayout(false);
            setSportSwitchLayout(false);
            return;
        }
        LayoutParams layoutParams = (LayoutParams) this.mDistanceLayout.getLayoutParams();
        layoutParams.topMargin = getResources().getDimensionPixelSize(R$dimen.sport_distance_top);
        this.mDistanceLayout.setLayoutParams(layoutParams);
        layoutParams = (LayoutParams) this.mDurationPaceHrLayout.getLayoutParams();
        layoutParams.topMargin = getResources().getDimensionPixelSize(R$dimen.sport_health_margin_bottom);
        this.mDurationPaceHrLayout.setLayoutParams(layoutParams);
        layoutParams = (LayoutParams) this.mSportSwitchLayout.getLayoutParams();
        layoutParams.topMargin = getResources().getDimensionPixelSize(R$dimen.sport_switch_margin_top);
        layoutParams.bottomMargin = getResources().getDimensionPixelSize(R$dimen.sport_switch_margin_bottom);
        this.mSportSwitchLayout.setLayoutParams(layoutParams);
    }

    private void updateSport2SportMusic() {
        LayoutParams layoutParams = (LayoutParams) this.mSportMusicFLayout.getLayoutParams();
        layoutParams.topMargin = getResources().getDimensionPixelSize(R$dimen.sport_music_notification_position_y);
        this.mSportMusicFLayout.setLayoutParams(layoutParams);
        SportInfo.getInst().setTopMusicShowing(true);
        this.mSportMusicFLayout.setVisibility(0);
        if (HwUnlockUtils.isTablet()) {
            setDivierViewVisible();
            setDistanceLayout(true);
            setDurationPaceHrLayout(true);
            setSportSwitchLayout(true);
            return;
        }
        this.mDividerView.setVisibility(0);
        layoutParams = (LayoutParams) this.mDistanceLayout.getLayoutParams();
        layoutParams.topMargin = getResources().getDimensionPixelSize(R$dimen.sport_music_distance_top);
        this.mDistanceLayout.setLayoutParams(layoutParams);
        layoutParams = (LayoutParams) this.mDurationPaceHrLayout.getLayoutParams();
        layoutParams.topMargin = getResources().getDimensionPixelSize(R$dimen.sport_music_health_margin_top);
        this.mDurationPaceHrLayout.setLayoutParams(layoutParams);
        layoutParams = (LayoutParams) this.mSportSwitchLayout.getLayoutParams();
        layoutParams.topMargin = getResources().getDimensionPixelSize(R$dimen.sport_music_switch_margin_top);
        layoutParams.bottomMargin = getResources().getDimensionPixelSize(R$dimen.sport_music_switch_margin_bottom);
        this.mSportSwitchLayout.setLayoutParams(layoutParams);
    }

    private void updateSport2Notification() {
        this.mSportHealthLayout.setVisibility(8);
        if (this.mSportMusicFLayout.getVisibility() != 0) {
            if (HwUnlockUtils.isTablet()) {
                setSportTopLayout(false);
            } else {
                LayoutParams layoutParams = (LayoutParams) this.mSportTopLayout.getLayoutParams();
                layoutParams.topMargin = getResources().getDimensionPixelSize(R$dimen.sport_notification_position_y);
                this.mSportTopLayout.setLayoutParams(layoutParams);
            }
        }
        this.mSportTopLayout.setVisibility(0);
        refreshSportTopSwitchRes();
    }

    private void updateNotification2Sport() {
        this.mSportTopLayout.setVisibility(8);
        this.mSportHealthLayout.setVisibility(0);
        refreshSportSwitchRes();
        updateSportView();
    }

    private void updateSportMusic2Notification() {
        this.mSportHealthLayout.setVisibility(8);
        LayoutParams layoutParams = (LayoutParams) this.mSportMusicFLayout.getLayoutParams();
        layoutParams.topMargin = getResources().getDimensionPixelSize(R$dimen.sport_music_notification_position_y);
        this.mSportMusicFLayout.setLayoutParams(layoutParams);
        if (HwUnlockUtils.isTablet()) {
            setSportTopLayout(true);
        } else {
            layoutParams = (LayoutParams) this.mSportTopLayout.getLayoutParams();
            layoutParams.topMargin = getResources().getDimensionPixelSize(R$dimen.sport_music_divider_view_bottom);
            this.mSportTopLayout.setLayoutParams(layoutParams);
        }
        this.mSportTopLayout.setVisibility(0);
        refreshSportTopSwitchRes();
    }

    private void updateNotification2SportMusic() {
        this.mSportTopLayout.setVisibility(8);
        this.mSportHealthLayout.setVisibility(0);
        refreshSportSwitchRes();
        updateSportView();
        LayoutParams layoutParams = (LayoutParams) this.mSportMusicFLayout.getLayoutParams();
        layoutParams.topMargin = getResources().getDimensionPixelSize(R$dimen.sport_music_notification_position_y);
        this.mSportMusicFLayout.setLayoutParams(layoutParams);
    }

    private void updateMusicView() {
        MusicInfo musicInfo = MusicInfo.getInst();
        refreshMusicText(musicInfo.getSongName(), musicInfo.getArtist());
        updateAlbumBitmap(musicInfo.getCircleBitmap(this.mContext));
        if (musicInfo.isPlaying() && !this.oldState) {
            this.oldState = true;
            updateSport2SportMusic();
            HwLockScreenReporter.report(this.mContext, 169, BuildConfig.FLAVOR);
        }
    }

    private void updateSportView() {
        if (this.isShowing) {
            TrackingStatus trackingStatus = TrackingStatus.getInst();
            if (trackingStatus == null) {
                HwLog.i("HwSportView", "trackingStatus is null");
                return;
            }
            refreshSportDataText(trackingStatus);
            if (!isHasNotificationOnKeyguard()) {
                updateGpsSignal(trackingStatus.getGps());
            }
        }
    }

    private boolean isHasNotificationOnKeyguard() {
        KeyguardInfo keyguardInfo = KeyguardInfo.getInst(this.mContext);
        if (!keyguardInfo.getShowOnKeyguard() || keyguardInfo.getKeyguardNotificationSize() <= 0) {
            return false;
        }
        return true;
    }

    private void updateSportNotificationView() {
        boolean isPlaying = MusicInfo.getInst().isPlaying();
        boolean isHasNotification = isHasNotificationOnKeyguard();
        if (!isPlaying && isHasNotification) {
            updateSport2Notification();
        } else if (isPlaying && isHasNotification) {
            updateSportMusic2Notification();
        } else if (!isPlaying && !isHasNotification) {
            updateNotification2Sport();
        } else if (isPlaying && !isHasNotification) {
            updateNotification2SportMusic();
        }
    }

    public void updateAlbumBitmap(Bitmap bitmap) {
        if (bitmap == null) {
            HwLog.w("HwSportView", "updateAlbumBitmap bitmap is null");
            return;
        }
        BitmapDrawable drawable = new BitmapDrawable(getContext().getResources(), bitmap);
        if (this.mSportMusicAlbum != null) {
            HwLog.w("HwSportView", "setBackground");
            this.mSportMusicAlbum.setBackground(drawable);
        }
    }

    public void refreshMusicText(String songName, String artist) {
        if (this.mSportMusicSongName != null) {
            this.mSportMusicSongName.setText(songName);
        }
        if (this.mSportMusicArtist != null) {
            this.mSportMusicArtist.setText(artist);
        }
        refreshMusicPlayRes();
    }

    private void refreshMusicPlayRes() {
        if (this.mSportMusicPlay == null) {
            HwLog.w("HwSportView", "refreshMusicPlayRes mPlayButton is null");
            return;
        }
        int resId;
        if (MusicInfo.getInst().isPlaying()) {
            this.mSportMusicPlay.setImageResource(R$drawable.hwlockscreen_sport_music_pause);
            resId = R$string.keyguard_accessibility_transport_pause_description;
        } else {
            this.mSportMusicPlay.setImageResource(R$drawable.hwlockscreen_sport_music_play);
            resId = R$string.keyguard_accessibility_transport_play_description;
        }
        this.mSportMusicPlay.setContentDescription(getContext().getText(resId));
        Rect outRect = new Rect();
        this.mSportMusicPlay.getHitRect(outRect);
        invalidate(outRect);
    }

    private void refreshSportDataText(TrackingStatus trackData) {
        if (trackData != null) {
            if (!checkHRLayoutVisibility(trackData.getHeartRate())) {
                this.mCalorierText.setText(trackData.getCalorie());
            } else if (trackData.getHeartRate() == 0) {
                this.mHRText.setText("--");
            } else {
                this.mHRText.setText(Integer.toString(trackData.getHeartRate()));
            }
            refreshSwitchRes(trackData.getSportState());
            this.mDistanceText.setText(trackData.getDistance());
            this.mTopDistanceText.setText(trackData.getDistance());
            this.mDuratonText.setText(trackData.getDuration());
            this.mPaceText.setText(trackData.getPace());
        }
    }

    private boolean checkHRLayoutVisibility(int hr) {
        if (hr != 0 || SportInfo.getInst().isHRValueDisplay()) {
            if (!(hr == 0 && SportInfo.getInst().isHRValueDisplay()) && hr > 0) {
                if (this.mHRLayout.getVisibility() == 8) {
                    this.mCalorierLayout.setVisibility(8);
                    this.mHRLayout.setVisibility(0);
                }
                if (!SportInfo.getInst().isHRValueDisplay()) {
                    SportInfo.getInst().setHRValueDisplay(true);
                }
            }
            return true;
        }
        if (this.mCalorierLayout.getVisibility() == 8) {
            this.mCalorierLayout.setVisibility(0);
            this.mHRLayout.setVisibility(8);
        }
        return false;
    }

    private void refreshSportSwitchRes() {
        if (this.mRunningButton == null) {
            HwLog.w("HwSportView", "refreshSportSwtichRes mRunningButton is null");
            return;
        }
        if (this.oldSportState == 1) {
            this.mRunningButton.setImageResource(R$drawable.ic_unlock_sport_pause);
        } else {
            this.mRunningButton.setImageResource(R$drawable.ic_unlock_sport_play);
        }
    }

    private void refreshSportTopSwitchRes() {
        if (this.mTopRunningButton == null) {
            HwLog.w("HwSportView", "refreshTopSportSwtichRes mRunningButton is null");
            return;
        }
        if (this.oldSportState == 1) {
            this.mTopRunningButton.setImageResource(R$drawable.ic_unlock_sport_pause);
        } else {
            this.mTopRunningButton.setImageResource(R$drawable.ic_unlock_sport_play);
        }
    }

    private void refreshSwitchRes(int newSportState) {
        if (newSportState != this.oldSportState) {
            this.oldSportState = newSportState;
            Message message = new Message();
            message.what = 102;
            this.mHandler.sendMessage(message);
            HwLog.w("HwSportView", "refreshSwitchRes newSportState " + newSportState);
        }
    }

    private void setDurationPaceHrLayout(boolean isMusicOn) {
        LayoutParams layoutParams = (LayoutParams) this.mDurationPaceHrLayout.getLayoutParams();
        if (isMusicOn) {
            layoutParams.topMargin = getResources().getDimensionPixelSize(R$dimen.sport_music_health_margin_top);
            layoutParams.bottomMargin = getResources().getDimensionPixelSize(R$dimen.sport_health_margin_bottom_add_music_on);
        } else {
            layoutParams.topMargin = getResources().getDimensionPixelSize(R$dimen.sport_health_margin_bottom);
            layoutParams.bottomMargin = getResources().getDimensionPixelSize(R$dimen.sport_health_margin_bottom_add_music_off);
        }
        this.mDurationPaceHrLayout.setLayoutParams(layoutParams);
    }

    private void setDivierViewVisible() {
        if (this.mContext.getResources().getConfiguration().orientation == 2) {
            this.mDividerView.setVisibility(4);
        } else {
            this.mDividerView.setVisibility(0);
        }
    }

    private void setSportSwitchLayout(boolean isMusicOn) {
        LayoutParams layoutParams = (LayoutParams) this.mSportSwitchLayout.getLayoutParams();
        if (isMusicOn) {
            layoutParams.topMargin = getResources().getDimensionPixelSize(R$dimen.sport_music_switch_margin_top);
            layoutParams.bottomMargin = getResources().getDimensionPixelSize(R$dimen.sport_music_switch_margin_bottom);
        } else {
            layoutParams.topMargin = getResources().getDimensionPixelSize(R$dimen.sport_switch_margin_top);
            layoutParams.bottomMargin = getResources().getDimensionPixelSize(R$dimen.sport_switch_margin_bottom);
        }
        this.mSportSwitchLayout.setLayoutParams(layoutParams);
    }

    private void setSportTopLayout(boolean isMusicOn) {
        LayoutParams layoutParams = (LayoutParams) this.mSportTopLayout.getLayoutParams();
        if (isMusicOn) {
            layoutParams.topMargin = getResources().getDimensionPixelSize(R$dimen.sport_music_divider_view_bottom);
        } else {
            layoutParams.topMargin = getResources().getDimensionPixelSize(R$dimen.sport_notification_position_y);
        }
        this.mSportTopLayout.setLayoutParams(layoutParams);
    }

    private void setDistanceLayout(boolean isMusicOn) {
        LayoutParams layoutParams = (LayoutParams) this.mDistanceLayout.getLayoutParams();
        if (isMusicOn) {
            layoutParams.topMargin = getResources().getDimensionPixelSize(R$dimen.sport_music_distance_top);
        } else {
            layoutParams.topMargin = getResources().getDimensionPixelSize(R$dimen.sport_distance_top);
        }
        this.mDistanceLayout.setLayoutParams(layoutParams);
    }
}
