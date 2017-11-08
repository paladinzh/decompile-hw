package com.android.gallery3d.ui;

import android.content.Context;
import android.graphics.Rect;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnInfoListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.MediaPlayer.OnVideoSizeChangedListener;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.view.Surface;
import com.android.gallery3d.anim.FloatAnimation;
import com.android.gallery3d.common.Utils;
import com.android.gallery3d.data.MediaItem;
import com.android.gallery3d.ui.LivePhotoScreenNail.OnLivePhotoChangedListener;
import com.android.gallery3d.util.TimeLog;
import com.huawei.gallery.livephoto.LiveUtils;
import com.huawei.gallery.util.MyPrinter;
import com.huawei.watermark.manager.parse.WMElement;
import edu.umd.cs.findbugs.annotations.SuppressWarnings;
import java.io.Closeable;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;

public class LivePhotoView extends GLView {
    private int DELAY_UPDATE_PHOTO = 50;
    private int DURATION_TRANSITION_OUT = 300;
    private MyPrinter LOG = new MyPrinter("LivePhotoView");
    @SuppressWarnings({"URF_UNREAD_FIELD"})
    private Context mContext;
    private int mCurrentIndex;
    private int mDuration = 0;
    private boolean mFrameArrived = false;
    private int mFrameIndex = 0;
    private Handler mHandler;
    private boolean mIgnorePlay = true;
    private boolean mIsFilmMode = false;
    @SuppressWarnings({"URF_UNREAD_FIELD"})
    private boolean mIsFingerOnScreen = false;
    private boolean mIsPlaying = false;
    private MediaItem mItem = null;
    private MediaPlayer mMediaPlayer;
    private MediaPlayerListener mMediaPlayerListener;
    private OnLivePhotoChangedListener mOnLivePhotoChangedListener = new OnLivePhotoChangedListener() {
        public void onFrameAvailable() {
            if (LivePhotoView.this.mIsPlaying) {
                LivePhotoView.this.mFrameArrived = true;
            }
            if (LivePhotoView.this.mFrameIndex == 0) {
                LivePhotoView.this.mTransitionOut.start();
                LivePhotoView.this.mTransitionOut.setDelay(LivePhotoView.this.mPlayDuration - LivePhotoView.this.DURATION_TRANSITION_OUT);
            }
            LivePhotoView livePhotoView = LivePhotoView.this;
            livePhotoView.mFrameIndex = livePhotoView.mFrameIndex + 1;
            LivePhotoView.this.invalidate();
        }
    };
    private AbsPhotoView mPhotoView;
    private int mPlayDuration;
    private GLRoot mRoot;
    private LivePhotoScreenNail mScreenNail;
    private int mStartPosition = -1;
    private boolean mStopIsRequested = false;
    private HandlerThread mThread;
    private FloatAnimation mTransitionOut = new FloatAnimation(WMElement.CAMERASIZEVALUE1B1, 0.0f, this.DURATION_TRANSITION_OUT);

    private class MediaPlayerCloser implements Runnable {
        private MediaPlayer mp;

        private MediaPlayerCloser(MediaPlayer player) {
            this.mp = player;
        }

        public void run() {
            if (this.mp != null) {
                TimeLog.start();
                this.mp.release();
                TimeLog.end("mMediaPlayer.release");
                LivePhotoView.this.LOG.d("release mediaplayer. " + this.mp);
            }
        }
    }

    private class MediaPlayerListener implements OnPreparedListener, OnVideoSizeChangedListener, OnCompletionListener, OnErrorListener, OnBufferingUpdateListener, OnInfoListener {
        private int mIndex;

        MediaPlayerListener(int index) {
            this.mIndex = index;
        }

        public void onPrepared(MediaPlayer mp) {
            if (LivePhotoView.this.mMediaPlayer != mp) {
                LivePhotoView.this.LOG.w(this.mIndex + "[onPrepared]player has been change to " + LivePhotoView.this.mMediaPlayer);
                return;
            }
            LivePhotoView.this.mDuration = mp.getDuration();
            boolean first = false;
            int duration = LivePhotoView.this.mDuration;
            if (LivePhotoView.this.mStartPosition < 0) {
                LivePhotoView.this.mStartPosition = 0;
                first = true;
                int[] playInfo = LiveUtils.readPlayInfo(LivePhotoView.this.mItem.getFilePath());
                if (playInfo == null || playInfo.length != 2) {
                    LivePhotoView.this.LOG.d("bad file cann't find play info. ");
                    return;
                }
                mp.seekTo(playInfo[0]);
                mp.setVolume(0.0f, 0.0f);
                duration = playInfo[1];
                LivePhotoView.this.mHandler.sendEmptyMessageDelayed(2, (long) duration);
            }
            LivePhotoView.this.mPlayDuration = duration;
            LivePhotoView.this.LOG.d(first + " media player is prepared duration is " + LivePhotoView.this.mDuration + ", play  " + duration);
            mp.start();
            LivePhotoView.this.mIsPlaying = true;
        }

        public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
            LivePhotoView.this.LOG.d("media player size changed to (" + width + "x" + height + ")");
            if (width != 0 && height != 0) {
                LivePhotoView.this.mScreenNail.setSize(width, height);
                LivePhotoView.this.mScreenNail.resizeTexture();
            }
        }

        public void onCompletion(MediaPlayer mp) {
            LivePhotoView.this.LOG.d("play completed, total frame: " + LivePhotoView.this.mFrameIndex);
            if (LivePhotoView.this.mMediaPlayer != mp) {
                LivePhotoView.this.LOG.w("[onCompletion]player has been change to " + LivePhotoView.this.mMediaPlayer);
                return;
            }
            LivePhotoView.this.mIsPlaying = false;
            LivePhotoView.this.stop();
            LivePhotoView.this.invalidate();
        }

        public boolean onError(MediaPlayer mp, int what, int extra) {
            LivePhotoView.this.LOG.d("player encounter a error what:" + what + ", " + extra);
            if (LivePhotoView.this.mMediaPlayer == mp && this.mIndex == LivePhotoView.this.mCurrentIndex) {
                LivePhotoView.this.mIsPlaying = false;
                return false;
            }
            LivePhotoView.this.LOG.w("[onError]player has been change to " + LivePhotoView.this.mMediaPlayer);
            return false;
        }

        public boolean onInfo(MediaPlayer mp, int what, int extra) {
            LivePhotoView.this.LOG.d("onInfo what:" + what + ", " + extra);
            return false;
        }

        public void onBufferingUpdate(MediaPlayer mp, int percent) {
            if (percent == 0 || percent == 100) {
                LivePhotoView.this.LOG.d("buffer update (start or done): " + percent + "%");
            }
        }
    }

    private class MyHandler extends Handler {
        public MyHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 2:
                    LivePhotoView.this.LOG.d("handle stop");
                    LivePhotoView.this.releaseInner();
                    LivePhotoView.this.mFrameArrived = false;
                    LivePhotoView.this.invalidate();
                    break;
                case 3:
                    LivePhotoView.this.mItem = (MediaItem) msg.obj;
                    LivePhotoView.this.LOG.d("handle updatePhoto, switch to photo " + LivePhotoView.this.mItem.getFilePath());
                    LivePhotoView.this.mStartPosition = -1;
                    if (!LivePhotoView.this.mIgnorePlay) {
                        LivePhotoView.this.mFrameArrived = false;
                        if (!LivePhotoView.this.mIsFilmMode) {
                            TimeLog.start();
                            LivePhotoView.this.open();
                            TimeLog.end("open");
                            break;
                        }
                        return;
                    }
                    LivePhotoView.this.LOG.d("ignore play for the first time.");
                    LivePhotoView.this.mIgnorePlay = false;
                    return;
                case 5:
                    LivePhotoView.this.LOG.d("handle start");
                    LivePhotoView.this.mStartPosition = 0;
                    LivePhotoView.this.mFrameArrived = false;
                    LivePhotoView.this.open();
                    break;
                default:
                    super.handleMessage(msg);
                    break;
            }
        }
    }

    public LivePhotoView(Context context) {
        this.mContext = context;
        this.mScreenNail = new LivePhotoScreenNail();
        this.mScreenNail.setListener(this.mOnLivePhotoChangedListener);
        this.mScreenNail.setSize(500, 500);
        this.mThread = new HandlerThread("moving-picture-play-thread");
        this.mThread.start();
        this.mHandler = new MyHandler(this.mThread.getLooper());
    }

    public void onResume() {
    }

    public void onPause() {
    }

    public void destroy() {
        this.mThread.quitSafely();
    }

    protected void onAttachToRoot(GLRoot root) {
        this.mRoot = root;
        super.onAttachToRoot(root);
    }

    protected void onDetachFromRoot() {
        this.mRoot = null;
        super.onDetachFromRoot();
    }

    public void setFilmMode(boolean isFilmMode) {
        this.mIsFilmMode = isFilmMode;
    }

    public void setPhotoView(AbsPhotoView photoView) {
        this.mPhotoView = photoView;
    }

    public void updatePhoto(int index, MediaItem item) {
        if (!LiveUtils.LIVE_ENABLE) {
            return;
        }
        if (item == null) {
            this.mItem = null;
            this.LOG.d("item is null, should never arrive here.");
            return;
        }
        this.mCurrentIndex = index;
        this.mStopIsRequested = true;
        this.LOG.d("called updatePhoto " + item.getName());
        this.mHandler.removeMessages(3);
        this.mHandler.removeMessages(2);
        this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(3, item), (long) this.DELAY_UPDATE_PHOTO);
    }

    public boolean isPlaying() {
        return this.mIsPlaying;
    }

    public void start() {
        this.LOG.d("called start");
        this.mHandler.removeMessages(2);
        this.mHandler.obtainMessage(5).sendToTarget();
    }

    public void stop() {
        this.LOG.d("called stop");
        this.mStopIsRequested = true;
        this.mHandler.removeMessages(2);
        this.mTransitionOut.setDelay(0);
        this.mTransitionOut.start();
        this.mHandler.sendEmptyMessageDelayed(2, (long) this.DURATION_TRANSITION_OUT);
    }

    public void setLongPress(boolean longPress) {
        this.mIsFingerOnScreen = longPress;
    }

    private void releaseInner() {
        this.mIsPlaying = false;
        this.LOG.d("[releaseInner] stop play");
        if (this.mMediaPlayerListener != null) {
            this.mMediaPlayerListener.mIndex = -1;
        }
        if (this.mMediaPlayer != null) {
            this.mMediaPlayer.setSurface(null);
            this.mScreenNail.releaseSurfaceTexture();
            new Thread(new MediaPlayerCloser(this.mMediaPlayer)).start();
            this.mMediaPlayer = null;
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    @SuppressWarnings({"UWF_FIELD_NOT_INITIALIZED_IN_CONSTRUCTOR"})
    protected void render(GLCanvas canvas) {
        if (this.mIsPlaying && this.mFrameArrived && !this.mStopIsRequested && this.mItem == this.mPhotoView.getMediaItem(0)) {
            super.render(canvas);
            int width = getWidth();
            if (getHeight() > 0 && width > 0 && this.mTransitionOut.calculate(AnimationTime.get())) {
                float alpha = this.mTransitionOut.get();
                MediaItem item = this.mItem;
                int rotation = item == null ? 0 : item.getRotation();
                Rect rect = this.mPhotoView.getPhotoRect(0);
                canvas.save();
                canvas.translate((float) rect.centerX(), (float) rect.centerY());
                if (rotation != 0) {
                    canvas.rotate((float) rotation, 0.0f, 0.0f, WMElement.CAMERASIZEVALUE1B1);
                }
                int dw = getRotated(rotation, rect.width(), rect.height());
                int dh = getRotated(rotation, rect.height(), rect.width());
                this.mScreenNail.draw(canvas, (-dw) / 2, (-dh) / 2, dw, dh, alpha);
                canvas.restore();
                invalidate();
            }
        }
    }

    private static int getRotated(int degree, int original, int theother) {
        return degree % 180 == 0 ? original : theother;
    }

    @SuppressWarnings({"UWF_FIELD_NOT_INITIALIZED_IN_CONSTRUCTOR"})
    private void open() {
        if (this.mScreenNail == null || this.mItem == null) {
            this.LOG.w("fail to open player screennail or item is null");
            return;
        }
        int index = this.mCurrentIndex;
        TimeLog.start();
        releaseInner();
        TimeLog.end("release ");
        Closeable closeable = null;
        try {
            String filePath = this.mItem.getFilePath();
            FileDescriptor fd = this.mItem.getFileDescriptor();
            if (!TextUtils.isEmpty(filePath)) {
                closeable = new FileInputStream(new File(filePath));
            } else if (fd != null) {
                Object fis = new FileInputStream(fd);
            } else {
                this.LOG.d("file path and FD both are null");
                Utils.closeSilently(null);
                if (this.mItem != null) {
                    this.mItem.closeParcelFileDescriptor();
                }
                return;
            }
            long videolength = LiveUtils.getVideoOffset(filePath);
            if (videolength <= 0) {
                this.LOG.d("getVideoOffset is illegal.");
                Utils.closeSilently(closeable);
                if (this.mItem != null) {
                    this.mItem.closeParcelFileDescriptor();
                }
                return;
            }
            long offset = (((long) closeable.available()) - videolength) - ((long) LiveUtils.getExtInfoLength());
            long length = videolength;
            if (offset < 0) {
                this.LOG.d("offset is illegal.");
                Utils.closeSilently(closeable);
                if (this.mItem != null) {
                    this.mItem.closeParcelFileDescriptor();
                }
            } else if (this.mRoot.getCanvas() == null) {
                this.LOG.w("canvas is null, illegal state.");
                Utils.closeSilently(closeable);
                if (this.mItem != null) {
                    this.mItem.closeParcelFileDescriptor();
                }
            } else {
                this.mMediaPlayer = new MediaPlayer();
                this.mMediaPlayerListener = new MediaPlayerListener(index);
                this.mMediaPlayer.setOnPreparedListener(this.mMediaPlayerListener);
                this.mMediaPlayer.setOnVideoSizeChangedListener(this.mMediaPlayerListener);
                this.mMediaPlayer.setOnCompletionListener(this.mMediaPlayerListener);
                this.mMediaPlayer.setOnErrorListener(this.mMediaPlayerListener);
                this.mMediaPlayer.setOnInfoListener(this.mMediaPlayerListener);
                this.mMediaPlayer.setOnBufferingUpdateListener(this.mMediaPlayerListener);
                this.mMediaPlayer.setDataSource(closeable.getFD(), offset, videolength);
                Surface surface = this.mScreenNail.getSurface();
                if (surface == null) {
                    this.mScreenNail.acquireSurfaceTexture();
                    surface = this.mScreenNail.getSurface();
                }
                this.mMediaPlayer.setAudioStreamType(3);
                this.mMediaPlayer.setScreenOnWhilePlaying(true);
                this.LOG.d("create mediaplayer success [" + index + "-" + this.mCurrentIndex + "]. " + this.mItem.getName());
                if (index == this.mCurrentIndex) {
                    this.mStopIsRequested = false;
                    this.mMediaPlayer.setSurface(surface);
                    this.mMediaPlayer.prepareAsync();
                }
                this.mFrameIndex = 0;
                Utils.closeSilently(closeable);
                if (this.mItem != null) {
                    this.mItem.closeParcelFileDescriptor();
                }
            }
        } catch (Exception e) {
            this.LOG.w("unable to paly. ", e);
            Utils.closeSilently(closeable);
            if (this.mItem != null) {
                this.mItem.closeParcelFileDescriptor();
            }
        } catch (Exception e2) {
            this.LOG.w("unexpected exception " + e2);
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
}
