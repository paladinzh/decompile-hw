package com.huawei.keyguard.view.widget;

import android.content.Context;
import android.graphics.Rect;
import android.os.Handler.Callback;
import android.os.Message;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.android.keyguard.R$bool;
import com.android.keyguard.R$dimen;
import com.android.keyguard.R$drawable;
import com.android.keyguard.R$id;
import com.android.keyguard.R$string;
import com.huawei.keyguard.data.MusicInfo;
import com.huawei.keyguard.events.AppHandler;
import com.huawei.keyguard.inf.HwKeyguardPolicy;
import com.huawei.keyguard.util.HwLog;
import com.huawei.keyguard.util.HwUnlockUtils;
import com.huawei.keyguard.util.MusicUtils;
import fyusion.vislib.BuildConfig;

public class HwSmallMusicView extends LinearLayout implements OnClickListener, Callback {
    private String mArtist;
    private TextView mArtistText;
    private ImageView mNextButton;
    private ImageView mPlayButton;
    private ImageView mPrevButton;
    private String mSongName;
    private TextView mSongNameText;

    public HwSmallMusicView(Context context) {
        this(context, null);
    }

    public HwSmallMusicView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mSongName = BuildConfig.FLAVOR;
        this.mArtist = BuildConfig.FLAVOR;
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
        initMusicView();
    }

    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        AppHandler.addListener(this);
    }

    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        HwLog.d("HwSmallMusicView", "onDetachedFromWindow ");
        AppHandler.removeListener(this);
    }

    private void initMusicView() {
        this.mPrevButton = (ImageView) findViewById(R$id.hwmusic_prev);
        if (this.mPrevButton != null) {
            this.mPrevButton.setOnClickListener(this);
        }
        this.mPlayButton = (ImageView) findViewById(R$id.hwmusic_play);
        if (this.mPlayButton != null) {
            this.mPlayButton.setOnClickListener(this);
        }
        this.mNextButton = (ImageView) findViewById(R$id.hwmusic_next);
        if (this.mNextButton != null) {
            this.mNextButton.setOnClickListener(this);
        }
        this.mSongNameText = (TextView) findViewById(R$id.hwmusic_name);
        if (this.mSongNameText != null) {
            this.mSongNameText.setSelected(true);
            if (getContext().getResources().getBoolean(R$bool.kg_multiuser_enable)) {
                float paddingWidth = getContext().getResources().getDimension(R$dimen.kg_multiuser_musicname_padding);
                this.mSongNameText.setPadding((int) paddingWidth, 0, (int) paddingWidth, 0);
            }
        }
        this.mArtistText = (TextView) findViewById(R$id.hwmusic_artist);
        if (this.mArtistText != null) {
            this.mArtistText.setSelected(true);
        }
    }

    public void onClick(View v) {
        HwKeyguardPolicy.getInst().userActivity();
        int resId = v.getId();
        if (resId == R$id.hwmusic_play) {
            HwLog.d("HwSmallMusicView", "onClick(View v) setPlayOrPause=");
            MusicUtils.sendMediaButtonClick(this.mContext, 85);
        } else if (resId == R$id.hwmusic_next) {
            MusicUtils.sendMediaButtonClick(this.mContext, 87);
        } else if (resId == R$id.hwmusic_prev) {
            MusicUtils.sendMediaButtonClick(this.mContext, 88);
        } else {
            HwLog.e("HwSmallMusicView", "onClick unknow target");
        }
    }

    public void refreshMusicText(String songName, String artist) {
        this.mSongName = songName;
        this.mArtist = artist;
        if (HwUnlockUtils.isSupportOrientation()) {
            if (this.mSongNameText != null) {
                this.mSongNameText.setText(this.mSongName);
            }
            if (this.mArtistText != null) {
                this.mArtistText.setText(this.mArtist);
            }
        } else if (this.mSongNameText != null) {
            this.mSongNameText.setText(this.mSongName + "/" + this.mArtist);
        }
        refreshMusicPlayRes();
    }

    private void refreshMusicPlayRes() {
        if (this.mPlayButton == null) {
            HwLog.w("HwSmallMusicView", "refreshMusicPlayRes mPlayButton is null");
            return;
        }
        int musicState = MusicUtils.getMusicState();
        HwLog.i("HwSmallMusicView", "refreshMusicPlayRes musicState = " + musicState);
        Rect outRect = new Rect();
        this.mPlayButton.getHitRect(outRect);
        int resId = R$string.keyguard_accessibility_transport_play_description;
        if (musicState == 2 || musicState == 0) {
            this.mPlayButton.setImageResource(R$drawable.hwlockscreen_music_play);
        } else if (musicState == 3) {
            this.mPlayButton.setImageResource(R$drawable.hwlockscreen_music_pause);
            resId = R$string.keyguard_accessibility_transport_pause_description;
        }
        this.mPlayButton.setContentDescription(getContext().getText(resId));
        invalidate(outRect);
    }

    public boolean handleMessage(Message msg) {
        if (msg.what != 110) {
            MusicInfo musicInfo = MusicInfo.getInst();
            refreshMusicText(musicInfo.getSongName(), musicInfo.getArtist());
        }
        return false;
    }
}
