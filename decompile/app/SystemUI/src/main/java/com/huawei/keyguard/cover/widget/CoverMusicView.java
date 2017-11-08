package com.huawei.keyguard.cover.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.drawable.BitmapDrawable;
import android.os.Handler.Callback;
import android.os.Message;
import android.os.PowerManager;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.android.huawei.coverscreen.HwCustCoverMusicView;
import com.android.keyguard.R$drawable;
import com.android.keyguard.R$id;
import com.android.keyguard.R$string;
import com.huawei.cust.HwCustUtils;
import com.huawei.keyguard.HwKeyguardUpdateMonitor;
import com.huawei.keyguard.cover.CoverCfg;
import com.huawei.keyguard.data.MusicInfo;
import com.huawei.keyguard.events.AppHandler;
import com.huawei.keyguard.events.MusicMonitor;
import com.huawei.keyguard.monitor.HwLockScreenReporter;
import com.huawei.keyguard.util.HwLog;
import com.huawei.keyguard.util.MusicUtils;
import com.huawei.keyguard.view.widget.AlbumView;
import fyusion.vislib.BuildConfig;

public class CoverMusicView extends RelativeLayout implements OnClickListener, Callback {
    private AlbumView mAlbum;
    private ImageView mAlbumImageView;
    private TextView mArtistNameText;
    private ImageView mBtnNext;
    private ImageView mBtnPlay;
    private ImageView mBtnPrev;
    private NewCoverDigitalClock mClockView;
    CoverMuiscCallback mCoverMusicCallback;
    private HwCustCoverMusicView mHwCoverMusicViewCust;
    private long mLastClickTime;
    private TextView mMusicNameText;
    private PowerManager mPM;

    public interface CoverMuiscCallback {
        boolean isShowClockView();

        void onShowClockView();

        void onShowMuiscView();
    }

    public CoverMusicView(Context context) {
        this(context, null);
    }

    public CoverMusicView(Context context, AttributeSet attr) {
        super(context, attr);
        this.mLastClickTime = 0;
        initHwCustMusicView();
        this.mPM = (PowerManager) getContext().getSystemService("power");
    }

    private void initHwCustMusicView() {
        this.mHwCoverMusicViewCust = (HwCustCoverMusicView) HwCustUtils.createObj(HwCustCoverMusicView.class, new Object[]{getContext()});
    }

    public void setCoverMusicCallback(CoverMuiscCallback callback) {
        this.mCoverMusicCallback = callback;
        invokeCallback();
    }

    private void invokeCallback() {
        if (this.mCoverMusicCallback == null) {
            HwLog.w("CoverMusicView", "invokeCallback skiped. ");
            return;
        }
        if (MusicInfo.getInst().needShowMusicView()) {
            this.mCoverMusicCallback.onShowMuiscView();
        } else {
            this.mCoverMusicCallback.onShowClockView();
        }
    }

    public void onFinishInflate() {
        super.onFinishInflate();
        HwLog.i("CoverMusicView", "onFinishInflate..");
        initView();
    }

    private void initView() {
        this.mBtnPrev = (ImageView) findViewById(R$id.cover_music_prev);
        if (this.mBtnPrev != null) {
            this.mBtnPrev.setOnClickListener(this);
            this.mBtnPrev.setContentDescription(getResources().getString(R$string.keyguard_accessibility_transport_prev_description_new));
        }
        this.mBtnPlay = (ImageView) findViewById(R$id.cover_music_play);
        if (this.mBtnPlay != null) {
            this.mBtnPlay.setOnClickListener(this);
        }
        this.mBtnNext = (ImageView) findViewById(R$id.cover_music_next);
        if (this.mBtnNext != null) {
            this.mBtnNext.setOnClickListener(this);
            this.mBtnNext.setContentDescription(getResources().getString(R$string.keyguard_accessibility_transport_next_description_new));
        }
        this.mMusicNameText = (TextView) findViewById(R$id.cover_music_name_textview);
        if (this.mMusicNameText != null) {
            this.mMusicNameText.setSelected(true);
        }
        this.mArtistNameText = (TextView) findViewById(R$id.cover_music_artistname_textview);
        if (this.mArtistNameText != null) {
            this.mArtistNameText.setSelected(true);
        }
        View v = findViewById(R$id.artwork_background);
        if (v instanceof AlbumView) {
            this.mAlbum = (AlbumView) v;
        } else if (v instanceof ImageView) {
            this.mAlbumImageView = (ImageView) v;
        }
    }

    private void updateState() {
        if (this.mMusicNameText == null || this.mArtistNameText == null) {
            HwLog.w("CoverMusicView", "updateState skiped " + this.mMusicNameText + ", mArtistNameText=" + this.mArtistNameText);
            return;
        }
        MusicInfo musicInfo = MusicInfo.getInst();
        HwLog.w("CoverMusicView", "updateState. " + musicInfo.needShowMusicView());
        if (musicInfo.needShowMusicView()) {
            if (!TextUtils.equals(this.mMusicNameText.getText(), musicInfo.getSongName())) {
                this.mMusicNameText.setText(musicInfo.getSongName());
            }
            if (!TextUtils.equals(this.mArtistNameText.getText(), musicInfo.getArtist())) {
                this.mArtistNameText.setText(musicInfo.getArtist());
            }
            setAlbum(musicInfo.getAlbumBitmap(this.mContext));
            invokeCallback();
            updatePlayPauseState();
            return;
        }
        invokeCallback();
    }

    public void userActivity() {
        this.mPM.userActivity(SystemClock.uptimeMillis(), false);
    }

    public boolean handleMessage(Message msg) {
        if (msg.what == 110) {
            updateState();
        }
        return false;
    }

    private void setAlbum(Bitmap bitmap) {
        Options opt;
        if (this.mAlbum != null) {
            if (bitmap == null) {
                opt = new Options();
                opt.inPurgeable = true;
                opt.inInputShareable = true;
                bitmap = BitmapFactory.decodeResource(this.mContext.getResources(), R$drawable.cover_music_artwork, opt);
            }
            this.mAlbum.setAlumBitmap(bitmap);
            refreshCoverHomeBackgroud();
        } else if (this.mAlbumImageView == null) {
            HwLog.w("CoverMusicView", "setAlbum, mAlbum is null!");
        } else {
            if (bitmap == null) {
                opt = new Options();
                opt.inPurgeable = true;
                opt.inInputShareable = true;
                bitmap = BitmapFactory.decodeResource(this.mContext.getResources(), R$drawable.cover_music_artwork, opt);
            }
            this.mAlbumImageView.setImageBitmap(bitmap);
        }
    }

    public void refreshCoverHomeBackgroud() {
        if (this.mAlbum != null) {
            Bitmap albumBitmap = this.mAlbum.getmAlbumBmp();
            View root = getRootView();
            if (albumBitmap == null || root == null) {
                HwLog.e("CoverMusicView", "Skip refresh music. ");
                return;
            }
            ViewGroup coverHomeView = (ViewGroup) root.findViewById(R$id.cover_home);
            if (!(coverHomeView == null || this.mCoverMusicCallback == null || !this.mCoverMusicCallback.isShowClockView())) {
                try {
                    Bitmap bgAlumBitmap = MusicInfo.getInst().getBlurBitmap(this.mContext);
                    if (this.mHwCoverMusicViewCust != null) {
                        bgAlumBitmap = this.mHwCoverMusicViewCust.getAlbumCutBmp(bgAlumBitmap);
                    }
                    coverHomeView.setBackground(new BitmapDrawable(getContext().getResources(), bgAlumBitmap));
                } catch (Exception ex) {
                    HwLog.w("CoverMusicView", "Blur music background error and got error:", ex);
                }
            }
        }
    }

    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        HwLog.i("CoverMusicView", "onAttachedToWindow.");
        AppHandler.addListener(this);
        updateState();
        MusicMonitor.getInst(getContext()).freshState();
        if (CoverCfg.isUseThemeOnlineFonts()) {
            if (this.mClockView == null) {
                int id = getContext().getResources().getIdentifier("cover_music_clock_view", "id", "com.android.systemui");
                if (id != 0) {
                    this.mClockView = (NewCoverDigitalClock) findViewById(id);
                }
            }
            if (this.mClockView != null) {
                this.mClockView.setDigitalTimeFontSize(getContext(), 2);
            }
        }
    }

    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        HwKeyguardUpdateMonitor monitor = HwKeyguardUpdateMonitor.getInstance();
        if (!(monitor == null || monitor.isShowing() || monitor.isOccluded())) {
            MusicInfo.getInst().resetShowingState();
        }
        AppHandler.removeListener(this);
    }

    private void updatePlayPauseState() {
        int imageResId;
        int imageDescId;
        int state = MusicInfo.getInst().getPlayState();
        switch (state) {
            case 3:
                imageResId = R$drawable.btn_music_pause_selector;
                imageDescId = R$string.keyguard_accessibility_transport_pause_description_new;
                break;
            default:
                imageResId = R$drawable.btn_music_play_selector;
                imageDescId = R$string.keyguard_accessibility_transport_play_description_new;
                break;
        }
        if (this.mHwCoverMusicViewCust != null) {
            imageResId = this.mHwCoverMusicViewCust.getStateImageResId(state, imageResId);
        }
        if (this.mBtnPlay == null) {
            HwLog.w("CoverMusicView", "updatePlayPauseState, mBtnPlay is null!");
            return;
        }
        this.mBtnPlay.setImageResource(imageResId);
        this.mBtnPlay.setContentDescription(getResources().getString(imageDescId));
    }

    public void onClick(View v) {
        if (System.currentTimeMillis() - this.mLastClickTime < 300) {
            HwLog.i("CoverMusicView", "onClick invalid !");
            return;
        }
        this.mLastClickTime = System.currentTimeMillis();
        if (v == this.mBtnPrev) {
            MusicUtils.sendMediaButtonClick(this.mContext, 88);
            userActivity();
            HwLockScreenReporter.report(this.mContext, 142, BuildConfig.FLAVOR);
        } else if (v == this.mBtnNext) {
            MusicUtils.sendMediaButtonClick(this.mContext, 87);
            userActivity();
            HwLockScreenReporter.report(this.mContext, 141, BuildConfig.FLAVOR);
        } else if (v == this.mBtnPlay) {
            MusicUtils.sendMediaButtonClick(this.mContext, 85);
            userActivity();
            if (MusicInfo.getInst().isPlaying()) {
                HwLockScreenReporter.report(this.mContext, 144, BuildConfig.FLAVOR);
            } else {
                HwLockScreenReporter.report(this.mContext, 143, BuildConfig.FLAVOR);
            }
        }
    }
}
