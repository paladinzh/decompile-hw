package com.android.systemui.volume;

import android.content.Context;
import android.media.SoundPool;
import android.os.Handler;
import android.os.Message;
import com.android.systemui.R;
import com.android.systemui.statusbar.policy.ZenModeController;
import com.android.systemui.utils.HwLog;
import com.android.systemui.utils.SystemUIThread;
import com.android.systemui.utils.SystemUIThread.SimpleAsyncTask;
import com.android.systemui.volume.HwVolumeSilentView.HwSilentViewCallback;
import com.android.systemui.volume.VolumeDialog.Callback;

public class HwVolumeDialog extends VolumeDialog {
    private SoundPool mPlayer = new SoundPool(1, 1, 0);
    private HwVolumeSilentView mSilentViewGroup;
    private SoundHandler mSoundHandler;
    private int mSoundId;
    private int mStreamId;

    private final class SoundHandler extends Handler {
        private SoundHandler() {
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    HwVolumeDialog.this.onPlaySound(msg.arg1);
                    return;
                case 2:
                    HwVolumeDialog.this.onStopSound();
                    return;
                default:
                    HwLog.e("HwVolumeDialog", "SoundHandler::handleMessage: unknown tag:" + msg.what);
                    return;
            }
        }
    }

    public HwVolumeDialog(Context context, int windowType, VolumeDialogController controller, ZenModeController zenModeController, Callback callback) {
        super(context, windowType, controller, zenModeController, callback);
        this.mSoundId = this.mPlayer.load(context, R.raw.volume_effect, 1);
        this.mSoundHandler = new SoundHandler();
    }

    public void updateStreamVolume(final int streamType, final int ringMode) {
        this.mHandler.post(new Runnable() {
            public void run() {
                if (HwVolumeDialog.this.mSilentViewGroup != null) {
                    HwVolumeDialog.this.mSilentViewGroup.updateViewGroupState(streamType, ringMode, HwVolumeDialog.this.mExpanded);
                }
            }
        });
    }

    public void initView() {
        this.mSilentViewGroup = (HwVolumeSilentView) this.mDialog.findViewById(R.id.silent_view_group);
    }

    public void updateVisibility(boolean show) {
        if (this.mSilentViewGroup != null) {
            this.mSilentViewGroup.updateVisibility(show);
        }
    }

    public void updateExpandButton() {
    }

    public void playSound(int streamType) {
        this.mSoundHandler.removeMessages(1);
        this.mSoundHandler.removeMessages(2);
        this.mSoundHandler.sendMessage(this.mSoundHandler.obtainMessage(1, Integer.valueOf(streamType)));
    }

    public void stopSound() {
        this.mSoundHandler.removeMessages(2);
        this.mSoundHandler.removeMessages(1);
        this.mSoundHandler.sendMessage(this.mSoundHandler.obtainMessage(2));
    }

    public HwSilentViewCallback getSilentViewCallback() {
        return this;
    }

    protected void onPlaySound(int streamType) {
        if (this.mSoundHandler.hasMessages(2)) {
            this.mSoundHandler.removeMessages(2);
        }
        onStopSound();
        final float leftVolume = ((float) this.mAudioManager.getStreamVolume(streamType)) / ((float) this.mAudioManager.getStreamMaxVolume(streamType));
        float rightVolume = leftVolume;
        try {
            SystemUIThread.runAsync(new SimpleAsyncTask(leftVolume) {
                final /* synthetic */ float val$leftVolume;

                public boolean runInThread() {
                    HwVolumeDialog.this.mStreamId = HwVolumeDialog.this.mPlayer.play(HwVolumeDialog.this.mSoundId, this.val$leftVolume, leftVolume, 1, 0, 1.0f);
                    HwLog.i("HwVolumeDialog", "onPlaySound::SoundId=" + HwVolumeDialog.this.mSoundId + ", StreamId=" + HwVolumeDialog.this.mStreamId + ", left=" + this.val$leftVolume + ", right=" + leftVolume);
                    return false;
                }
            });
        } catch (Exception e) {
            HwLog.e("HwVolumeDialog", "onPlaySound::occur exception=" + e);
        }
    }

    protected void onStopSound() {
        try {
            this.mPlayer.stop(this.mStreamId);
        } catch (Exception e) {
            HwLog.e("HwVolumeDialog", "onStopSounds::occur exception=" + e);
        }
        HwLog.i("HwVolumeDialog", "onStopSounds::play stop:StreamId=" + this.mStreamId);
    }
}
