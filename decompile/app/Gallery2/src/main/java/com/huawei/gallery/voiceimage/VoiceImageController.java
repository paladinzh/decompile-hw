package com.huawei.gallery.voiceimage;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.os.Handler;
import com.android.gallery3d.common.Utils;
import com.android.gallery3d.data.MediaItem;
import com.android.gallery3d.util.GalleryLog;
import com.android.gallery3d.util.ReportToBigData;
import com.huawei.gallery.app.PhotoPage.ActionBarProgressActionListener;
import com.huawei.gallery.app.plugin.PhotoExtraButton;
import java.io.Closeable;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;

public class VoiceImageController {
    private Context mContext;
    private int mDuration;
    private Handler mHandler;
    private boolean mIsPlaying = false;
    private MediaItem mItem = null;
    private int mLastPosition = -1;
    private final ActionBarProgressActionListener mListener;
    private OnCompletionListener mOnCompletionListener = new OnCompletionListener() {
        public void onCompletion(MediaPlayer mp) {
            VoiceImageController.this.stopVoice();
        }
    };
    private OnErrorListener mOnErrorListener = new OnErrorListener() {
        public boolean onError(MediaPlayer mp, int what, int extra) {
            GalleryLog.d("VoiceImageController", "MeidaPlayer Error (" + what + "," + extra + ")");
            VoiceImageController.this.stopVoice();
            return true;
        }
    };
    private String mPath = "";
    private PhotoExtraButton mPhotoExtraButton;
    private MediaPlayer mPlayer = null;
    private Runnable mProgressChecker = new Runnable() {
        public void run() {
            if (VoiceImageController.this.mIsPlaying && VoiceImageController.this.mPlayer != null) {
                int pos = VoiceImageController.this.mPlayer.getCurrentPosition();
                if (pos < VoiceImageController.this.mLastPosition) {
                    pos = VoiceImageController.this.mLastPosition;
                } else {
                    VoiceImageController.this.mLastPosition = pos;
                }
                VoiceImageController.this.mVoiceImageOverlay.setSweepAngle((((float) pos) / ((float) VoiceImageController.this.mDuration)) * 360.0f);
                VoiceImageController.this.mPhotoExtraButton.refreshOverlayAnim();
                int timeLeft = VoiceImageController.this.mDuration - pos;
                Handler -get1 = VoiceImageController.this.mHandler;
                Runnable -get6 = VoiceImageController.this.mProgressChecker;
                if (timeLeft >= 40) {
                    timeLeft = 40;
                }
                -get1.postDelayed(-get6, (long) timeLeft);
            }
        }
    };
    private VoiceImageOverlay mVoiceImageOverlay;

    public VoiceImageController(Context context, ActionBarProgressActionListener listener) {
        this.mContext = context;
        this.mListener = listener;
        this.mHandler = new Handler(context.getMainLooper());
        this.mVoiceImageOverlay = new VoiceImageOverlay(context);
    }

    public void refresh(PhotoExtraButton button, MediaItem item) {
        this.mPhotoExtraButton = button;
        this.mPhotoExtraButton.setPhotoExtraButtonOverlay(this.mVoiceImageOverlay);
        if (!item.getPath().equalsIgnoreCase(this.mPath)) {
            this.mItem = item;
            this.mPath = item.getPath().toString();
            this.mVoiceImageOverlay.setSweepAngle(0.0f);
            this.mPhotoExtraButton.refreshOverlayAnim();
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean updateVoicePlayer() {
        Closeable closeable = null;
        try {
            FileDescriptor fd = this.mItem.getFileDescriptor();
            if (!this.mItem.getFilePath().equals("")) {
                closeable = new FileInputStream(new File(this.mItem.getFilePath()));
            } else if (fd != null) {
                Object fis = new FileInputStream(fd);
            } else {
                Utils.closeSilently(null);
                if (this.mItem != null) {
                    this.mItem.closeParcelFileDescriptor();
                }
                return false;
            }
            long offset = (((long) closeable.available()) - this.mItem.getVoiceOffset()) - 20;
            if (offset < 0) {
                Utils.closeSilently(closeable);
                if (this.mItem != null) {
                    this.mItem.closeParcelFileDescriptor();
                }
                return false;
            }
            this.mPlayer = new MediaPlayer();
            this.mPlayer.setOnErrorListener(this.mOnErrorListener);
            this.mPlayer.setOnCompletionListener(this.mOnCompletionListener);
            this.mPlayer.setDataSource(closeable.getFD(), offset, this.mItem.getVoiceOffset());
            this.mPlayer.prepare();
            this.mDuration = this.mPlayer.getDuration();
            Utils.closeSilently(closeable);
            if (this.mItem != null) {
                this.mItem.closeParcelFileDescriptor();
            }
            return true;
        } catch (IllegalArgumentException e) {
            GalleryLog.i("VoiceImageController", "updateVoicePlayer() failed, reason: IllegalArgumentException.");
            Utils.closeSilently(closeable);
            if (this.mItem != null) {
                this.mItem.closeParcelFileDescriptor();
            }
        } catch (IllegalStateException e2) {
            GalleryLog.i("VoiceImageController", "updateVoicePlayer() failed, reason: IllegalStateException.");
            Utils.closeSilently(closeable);
            if (this.mItem != null) {
                this.mItem.closeParcelFileDescriptor();
            }
        } catch (SecurityException e3) {
            GalleryLog.i("VoiceImageController", "updateVoicePlayer() failed, reason: SecurityException.");
            Utils.closeSilently(closeable);
            if (this.mItem != null) {
                this.mItem.closeParcelFileDescriptor();
            }
        } catch (IOException e4) {
            GalleryLog.i("VoiceImageController", "updateVoicePlayer() failed, reason: IOException.");
            Utils.closeSilently(closeable);
            if (this.mItem != null) {
                this.mItem.closeParcelFileDescriptor();
            }
        } catch (Throwable th) {
            Utils.closeSilently(closeable);
            if (this.mItem != null) {
                this.mItem.closeParcelFileDescriptor();
            }
        }
    }

    public void playPause() {
        if (this.mIsPlaying) {
            stop();
            return;
        }
        ReportToBigData.report(40, String.format("{PhotoButton:%s}", new Object[]{"Voice"}));
        play();
    }

    private void play() {
        if (!this.mIsPlaying) {
            if (playVoice()) {
                this.mIsPlaying = true;
                this.mLastPosition = -1;
                this.mHandler.postDelayed(this.mProgressChecker, 40);
            } else {
                stopVoice();
            }
        }
    }

    private boolean playVoice() {
        ((AudioManager) this.mContext.getSystemService("audio")).requestAudioFocus(null, 3, 3);
        try {
            updateVoicePlayer();
            if (this.mPlayer != null) {
                this.mPlayer.start();
                this.mListener.onStart();
                return true;
            }
        } catch (IllegalStateException e) {
            GalleryLog.i("VoiceImageController", "playVoice() failed, reason: IllegalStateException.");
        } catch (IllegalArgumentException e2) {
            GalleryLog.i("VoiceImageController", "playVoice() failed, reason: IllegalArgumentException.");
        } catch (SecurityException e3) {
            GalleryLog.i("VoiceImageController", "playVoice() failed, reason: SecurityException.");
        }
        return false;
    }

    public void stop() {
        if (this.mIsPlaying) {
            stopVoice();
        }
    }

    private void stopVoice() {
        this.mIsPlaying = false;
        ((AudioManager) this.mContext.getSystemService("audio")).abandonAudioFocus(null);
        this.mVoiceImageOverlay.setSweepAngle(0.0f);
        this.mPhotoExtraButton.refreshOverlayAnim();
        try {
            if (this.mPlayer != null) {
                this.mPlayer.stop();
                this.mPlayer.release();
                this.mPlayer = null;
                this.mListener.onEnd();
            }
        } catch (IllegalStateException e) {
            GalleryLog.i("VoiceImageController", "stopVoice() failed, reason: IllegalStateException.");
        }
    }
}
