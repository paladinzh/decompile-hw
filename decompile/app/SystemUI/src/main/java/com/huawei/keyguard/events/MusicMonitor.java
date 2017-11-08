package com.huawei.keyguard.events;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaMetadata;
import android.media.session.MediaController;
import android.media.session.MediaController.Callback;
import android.media.session.MediaSessionManager;
import android.media.session.PlaybackState;
import android.text.TextUtils;
import com.huawei.keyguard.GlobalContext;
import com.huawei.keyguard.data.MusicInfo;
import com.huawei.keyguard.util.HwLog;
import com.huawei.keyguard.util.MusicUtils;

public class MusicMonitor {
    private static MusicMonitor sInst = null;
    private Context mContext;
    private MediaController mMediaController;
    private Callback mMediaListener = new Callback() {
        public void onPlaybackStateChanged(PlaybackState state) {
            HwLog.i("MusicMonitor", "DEBUG_MEDIA: onPlaybackStateChanged: " + state);
            super.onPlaybackStateChanged(state);
            MusicMonitor.this.mMusicInfo.updateState(state);
            if (MusicMonitor.this.mMediaController != null) {
                MusicMonitor.this.mMusicInfo.updateData(MusicMonitor.this.mContext, MusicMonitor.this.mMediaController.getMetadata());
            }
        }

        public void onMetadataChanged(MediaMetadata metadata) {
            super.onMetadataChanged(metadata);
            HwLog.i("MusicMonitor", "DEBUG_MEDIA: onMetadataChanged: " + metadata);
            MusicMonitor.this.mMusicInfo.updateData(MusicMonitor.this.mContext, MusicMonitor.this.mMediaController.getMetadata());
        }

        public void onSessionDestroyed() {
            super.onSessionDestroyed();
            HwLog.d("MusicMonitor", "DEBUG_MEDIA: onSessionDestroyed");
            GlobalContext.getUIHandler().post(new Runnable() {
                public void run() {
                    MusicMonitor.this.updateMediaController(null);
                }
            });
        }
    };
    private MediaSessionManager mMediaSessionManager;
    MusicInfo mMusicInfo = MusicInfo.getInst();
    BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (intent == null || !"com.android.mediacenter.playstatechanged".equals(intent.getAction())) {
                HwLog.w("MusicMonitor", "onReceive, the intent is null!");
                return;
            }
            if (intent.getBooleanExtra("isPlaying", false) && !MusicMonitor.this.mMusicInfo.isPlaying()) {
                MusicMonitor.this.updateMediaController(MusicUtils.findCurrentMediaController(context, MusicMonitor.this.mMediaSessionManager));
            }
        }
    };

    public static MusicMonitor getInst(Context context) {
        synchronized (MusicMonitor.class) {
            if (sInst != null) {
                MusicMonitor musicMonitor = sInst;
                return musicMonitor;
            }
            MusicMonitor tmpInst = new MusicMonitor(context);
            sInst = tmpInst;
            tmpInst.init(context);
            return tmpInst;
        }
    }

    private MusicMonitor(Context context) {
        this.mContext = context.getApplicationContext();
    }

    private void init(Context context) {
        this.mMediaSessionManager = (MediaSessionManager) this.mContext.getSystemService("media_session");
        context.registerReceiver(this.mReceiver, new IntentFilter("com.android.mediacenter.playstatechanged"), "com.android.keyguard.permission.SHOW_LYRICS", null);
        updateMediaController(MusicUtils.findCurrentMediaController(this.mContext, this.mMediaSessionManager));
    }

    private static boolean isSameMetaData(MediaMetadata aData, MediaMetadata bData) {
        if (aData == bData) {
            return true;
        }
        if (aData == null || bData == null || !TextUtils.equals(aData.getString("android.media.metadata.ARTIST"), bData.getString("android.media.metadata.ARTIST"))) {
            return false;
        }
        return TextUtils.equals(aData.getString("android.media.metadata.TITLE"), bData.getString("android.media.metadata.TITLE"));
    }

    private static boolean isSameController(MediaController aCtrl, MediaController bCtrl) {
        if (aCtrl == bCtrl) {
            return true;
        }
        if (aCtrl == null || bCtrl == null || aCtrl.getFlags() != bCtrl.getFlags() || !TextUtils.equals(aCtrl.getPackageName(), bCtrl.getPackageName())) {
            return false;
        }
        PlaybackState aplay = aCtrl.getPlaybackState();
        PlaybackState bplay = bCtrl.getPlaybackState();
        if (aplay == null || bplay == null || aplay.getState() != bplay.getState()) {
            return false;
        }
        return isSameMetaData(aCtrl.getMetadata(), bCtrl.getMetadata());
    }

    private void updateMediaController(MediaController mediaController) {
        if (!isSameController(this.mMediaController, mediaController)) {
            if (this.mMediaController != null) {
                HwLog.w("MusicMonitor", " beginObserver mMediaController registerCallback mMediaListener");
                this.mMediaController.unregisterCallback(this.mMediaListener);
            }
            this.mMediaController = mediaController;
            if (mediaController == null) {
                this.mMusicInfo.reset();
            } else {
                this.mMediaController.registerCallback(this.mMediaListener);
                this.mMusicInfo.setPlayerApp(this.mMediaController.getPackageName());
                this.mMusicInfo.updateState(this.mMediaController.getPlaybackState());
                this.mMusicInfo.updateData(this.mContext, this.mMediaController.getMetadata());
            }
        }
    }

    public void freshState() {
        updateMediaController(MusicUtils.findCurrentMediaController(this.mContext, this.mMediaSessionManager));
    }
}
