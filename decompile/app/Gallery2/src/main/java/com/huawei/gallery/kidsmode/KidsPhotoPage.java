package com.huawei.gallery.kidsmode;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MotionEvent;
import com.android.gallery3d.data.MediaItem;
import com.android.gallery3d.util.Constant;
import com.android.gallery3d.util.GalleryLog;
import com.huawei.gallery.app.AbsPhotoPage;
import java.io.File;
import java.util.ArrayList;

public class KidsPhotoPage extends AbsPhotoPage {
    private boolean mAutoPlaySlide = false;

    private class KidsOrientationManager extends MyOrientationManager {
        public KidsOrientationManager(Handler handler) {
            super(handler);
        }

        public void onChange(boolean selfChange) {
            this.mNeedResetOrientation = true;
            KidsPhotoPage.this.mHost.getActivity().setRequestedOrientation(4);
        }
    }

    protected void onGLRootLayout(boolean changeSize, int left, int top, int right, int bottom) {
        this.mPhotoView.layout(0, 0, right - left, bottom - top);
    }

    protected void onCreate(Bundle data, Bundle storedState) {
        super.onCreate(data, storedState);
        this.mAutoPlaySlide = data.getBoolean("key-auto-play-slide");
        this.mFlags |= 18;
        this.mPhotoView.setFilmModeAllowed(false);
        this.mOrientationManager = new KidsOrientationManager(null);
    }

    protected boolean onCreateActionBar(Menu menu) {
        this.mHost.requestFeature(348);
        this.mActionBar.setActionBarVisible(false);
        return true;
    }

    protected void onResume() {
        super.onResume();
        if (this.mAutoPlaySlide) {
            this.mHandler.removeMessages(100);
            this.mHandler.sendEmptyMessageDelayed(100, 5000);
        }
    }

    protected void onPause() {
        super.onPause();
        if (this.mAutoPlaySlide) {
            this.mHandler.removeMessages(100);
        }
    }

    public void onSingleTapUp(int x, int y) {
        MediaItem item = this.mModel.getMediaItem(0);
        if (item != null) {
            boolean playVideo = (item.getSupportedOperations() & 128) != 0;
            boolean isDrmVideo = item.isDrm() && 4 == item.getMediaType();
            if (playVideo || isDrmVideo) {
                GalleryLog.v("KidsPhotoPage", "check the play video icon position");
                int w = this.mPhotoView.getWidth();
                int h = this.mPhotoView.getHeight();
                playVideo = Math.abs(x - (w / 2)) * 12 <= w ? Math.abs(y - (h / 2)) * 12 <= h : false;
            }
            if (playVideo && (!isDrmVideo || item.getRight())) {
                playVideo(this.mHost.getActivity(), item);
            }
        }
    }

    protected void playVideo(Activity activity, MediaItem item) {
        String[] videoPackage = Constant.getPlayPackageName();
        int i = 0;
        while (i < videoPackage.length) {
            try {
                playVideoByHwVPlayer(activity, item, videoPackage[i]);
                return;
            } catch (RuntimeException e) {
                GalleryLog.d("playVideoByHwVPlayer", "can't find activity. " + videoPackage[i]);
                i++;
            }
        }
    }

    private void playVideoByHwVPlayer(Activity activity, MediaItem item, String packageName) {
        Intent intent = new Intent("android.intent.action.VIEW").setComponent(new ComponentName(packageName, "com.huawei.hwvplayer.service.player.FullscreenActivity"));
        Bundle extra = getPlayListExtra(item);
        if (extra != null) {
            intent.putExtras(extra);
        } else {
            intent.setDataAndType(Uri.fromFile(new File(item.getFilePath())), "video/*");
        }
        intent.putExtra("viewtype", 3);
        activity.startActivity(intent);
    }

    private Bundle getPlayListExtra(MediaItem item) {
        if (this.mMediaSet != null) {
            ArrayList<String> videoList = this.mMediaSet.getVideoFileList();
            if (videoList != null) {
                Bundle extra = new Bundle();
                extra.putStringArrayList("play_info_uri_list", videoList);
                extra.putInt("uri_index", videoList.indexOf(item.getFilePath()));
                return extra;
            }
        }
        return null;
    }

    protected void showBars(boolean barWithAnim) {
    }

    protected void hideBars(boolean barWithAnim) {
        this.mHost.getGLRoot().setLightsOutMode(true);
        if (this.mShowBars) {
            this.mShowBars = false;
            this.mActionBar.setActionBarVisible(false, barWithAnim);
            this.mHandler.removeMessages(1);
        }
    }

    public void onNavigationBarChanged(boolean show, int height) {
        this.mRootPane.requestLayout();
    }

    public void onTouchEventReceived(MotionEvent event) {
        if (!this.mAutoPlaySlide) {
            return;
        }
        if (event.getAction() == 0) {
            this.mHandler.removeMessages(100);
        } else if (event.getAction() == 1 || event.getAction() == 3) {
            this.mHandler.sendEmptyMessageDelayed(100, 5000);
        }
    }
}
