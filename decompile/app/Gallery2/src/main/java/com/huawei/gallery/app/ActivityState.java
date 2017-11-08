package com.huawei.gallery.app;

import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.Window;
import android.view.WindowManager.LayoutParams;
import com.android.gallery3d.R;
import com.android.gallery3d.common.ApiHelper;
import com.android.gallery3d.ui.GLView;
import com.android.gallery3d.ui.PreparePageFadeoutTexture;
import com.android.gallery3d.util.GalleryUtils;
import com.huawei.gallery.actionbar.Action;
import com.huawei.gallery.anim.StateTransitionAnimation;
import com.huawei.gallery.anim.StateTransitionAnimation.Transition;
import com.huawei.gallery.kidsmode.KidsAlbumPage;
import com.huawei.gallery.kidsmode.KidsPhotoPage;
import com.huawei.gallery.kidsmode.ParentAddedAlbumPage;
import com.huawei.gallery.map.app.MapAlbumPage;
import com.huawei.gallery.recycle.app.RecycleAlbumPage;
import com.huawei.gallery.refocus.app.RefocusPage;
import com.huawei.gallery.refocus.wideaperture.RangeMeasure.app.RangeMeasurePage;
import com.huawei.gallery.story.app.StoryAlbumPage;
import com.huawei.gallery.ui.OpenAnimationProxyView;

public abstract class ActivityState {
    protected static final int FLAG_ALLOW_LOCK_WHILE_SCREEN_ON = 4096;
    protected static final int FLAG_FULLSCREEN = 32;
    protected static final int FLAG_HIDE_ACTION_BAR = 1;
    protected static final int FLAG_HIDE_NAVIGATION_BAR = 4;
    protected static final int FLAG_HIDE_STATUS_BAR = 2;
    protected static final int FLAG_LAYOUT_HIDE_NAVIGATION = 8;
    protected static final int FLAG_SCREEN_ON_ALWAYS = 2048;
    protected static final int FLAG_SCREEN_ON_WHEN_PLUGGED = 1024;
    protected static final int FLAG_SHOW_WHEN_LOCKED = 8192;
    protected static final int FLAG_TRANSLUCENT_NAVIGATION = 16;
    protected static final int FLAG_TRANSLUCENT_STATUS = 64;
    private static final String KEY_TRANSITION_IN = "transition-in";
    private boolean mActionBarCreated = false;
    protected float[] mBackgroundColor;
    private GLView mContentPane;
    protected Bundle mData;
    private boolean mDestroyed = false;
    protected int mFlags;
    protected GLHost mHost;
    private StateTransitionAnimation mIntroAnimation;
    boolean mIsFinishing = false;
    private Transition mNextTransition = Transition.None;
    protected boolean mPausedByKeyguard = false;
    private boolean mPlugged = false;
    BroadcastReceiver mPowerIntentReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if ("android.intent.action.BATTERY_CHANGED".equals(intent.getAction())) {
                boolean plugged = intent.getIntExtra("plugged", 0) != 0;
                if (plugged != ActivityState.this.mPlugged) {
                    ActivityState.this.mPlugged = plugged;
                    ActivityState.this.setScreenFlags();
                }
            }
        }
    };
    protected ResultEntry mReceivedResults;
    protected ResultEntry mResult;

    protected static class ResultEntry {
        public int requestCode;
        public int resultCode = 0;
        public Intent resultData;

        protected ResultEntry() {
        }
    }

    protected int getBackgroundColor(Context context) {
        return context.getResources().getColor(R.color.default_background);
    }

    protected float[] getBackgroundColor() {
        return this.mBackgroundColor;
    }

    protected ActivityState() {
    }

    protected void setContentPane(GLView content) {
        this.mContentPane = content;
        if (this.mIntroAnimation != null) {
            this.mContentPane.setIntroAnimation(this.mIntroAnimation);
            this.mIntroAnimation = null;
        }
        this.mContentPane.setBackgroundColor(getBackgroundColor());
        this.mHost.getGLRoot().setContentPane(this.mContentPane);
    }

    void initialize(GLHost host, Bundle data) {
        this.mHost = host;
        this.mData = data;
    }

    public Bundle getData() {
        return this.mData;
    }

    protected boolean onBackPressed() {
        if (this.mHost.getStateManager().getStateCount() > 1) {
            this.mHost.getStateManager().finishState(this);
            return true;
        }
        this.mHost.getStateManager().setActivityResult();
        return false;
    }

    protected void setStateResult(int resultCode, Intent data) {
        if (this.mResult != null) {
            this.mResult.resultCode = resultCode;
            this.mResult.resultData = data;
        }
    }

    protected void onConfigurationChanged(Configuration config) {
    }

    protected void onSaveState(Bundle outState) {
    }

    protected void onStateResult(int requestCode, int resultCode, Intent data) {
    }

    protected void onCreate(Bundle data, Bundle storedState) {
        this.mBackgroundColor = GalleryUtils.intColorToFloatARGBArray(getBackgroundColor(this.mHost.getActivity()));
    }

    protected void clearStateResult() {
    }

    @TargetApi(19)
    public void setScreenFlags() {
        Window win = this.mHost.getActivity().getWindow();
        LayoutParams params = win.getAttributes();
        if ((this.mFlags & FLAG_SCREEN_ON_ALWAYS) != 0 || (this.mPlugged && (this.mFlags & 1024) != 0)) {
            params.flags |= 128;
        } else {
            params.flags &= -129;
        }
        if ((this.mFlags & 4096) != 0) {
            params.flags |= 1;
        } else {
            params.flags &= -2;
        }
        if ((this.mFlags & 8192) != 0) {
            params.flags |= 524288;
        } else {
            params.flags &= -524289;
        }
        if ((this.mFlags & 32) != 0) {
            params.flags |= 1024;
        } else {
            params.flags &= -1025;
        }
        setTranslucentFlag(params);
        win.setAttributes(params);
    }

    private void setTranslucentFlag(LayoutParams params) {
        if (ApiHelper.HAS_VIEW_FLAG_TRANSLUCENT_NAVIGATION) {
            if ((this.mFlags & 16) != 0) {
                params.flags |= 134217728;
            } else {
                params.flags &= -134217729;
            }
        }
        if (!ApiHelper.HAS_VIEW_FLAG_TRANSLUCENT_STATUS) {
            return;
        }
        if ((this.mFlags & 64) != 0) {
            params.flags |= 67108864;
        } else {
            params.flags &= -67108865;
        }
    }

    protected void transitionOnNextPause(Class<? extends ActivityState> outgoing, Class<? extends ActivityState> incoming, Transition hint) {
        if (isAlbumFromPhoto(outgoing, incoming)) {
            this.mNextTransition = Transition.None;
        } else if (isPhotoFromAlbum(outgoing, incoming)) {
            this.mNextTransition = Transition.PhotoIncoming;
        } else if (needDisableTransition(outgoing, incoming)) {
            this.mNextTransition = Transition.None;
        } else if (outgoing != SlideShowPage.class) {
            this.mNextTransition = hint;
        } else if (incoming == TimeBucketPage.class) {
            this.mNextTransition = Transition.SlideOutgoing;
        } else {
            this.mNextTransition = Transition.Outgoing;
        }
        this.mHost.getGalleryActionBar().resetHeadAndFootActionContainer();
    }

    private boolean needDisableTransition(Class<? extends ActivityState> outgoing, Class<? extends ActivityState> incoming) {
        if (outgoing == RefocusPage.class || incoming == RefocusPage.class) {
            return true;
        }
        if (outgoing == PhotoPage.class && incoming == PhotoPage.class) {
            return true;
        }
        if (outgoing == GalleryMediaPhotoPage.class && incoming == PhotoPage.class) {
            return true;
        }
        if ((outgoing == PhotoPage.class && incoming == GalleryMediaPhotoPage.class) || outgoing == RangeMeasurePage.class || incoming == RangeMeasurePage.class) {
            return true;
        }
        return false;
    }

    private boolean isPhotoFromAlbum(Class<? extends ActivityState> outgoing, Class<? extends ActivityState> incoming) {
        if (outgoing == SlotAlbumPage.class && incoming == PhotoPage.class) {
            return true;
        }
        if (outgoing == PhotoShareAlbumPage.class && incoming == GalleryMediaPhotoPage.class) {
            return true;
        }
        if (outgoing == SlotAlbumPage.class && incoming == SelectionPreview.class) {
            return true;
        }
        if (outgoing == TimeBucketPage.class && incoming == GalleryMediaPhotoPage.class) {
            return true;
        }
        if (outgoing == KeyguardPage.class && incoming == KeyguardPhotoPage.class) {
            return true;
        }
        if (outgoing == TimeBucketPage.class && incoming == PhotoPage.class) {
            return true;
        }
        if (outgoing == TimeBucketPage.class && incoming == SelectionPreview.class) {
            return true;
        }
        if (outgoing == PhotoShareAlbumPage.class && incoming == PhotoSharePhotoPage.class) {
            return true;
        }
        if (outgoing == PhotoShareAlbumPage.class && incoming == SelectionPreview.class) {
            return true;
        }
        if (outgoing == MapAlbumPage.class && incoming == SelectionPreview.class) {
            return true;
        }
        if (outgoing == MapAlbumPage.class && incoming == PhotoPage.class) {
            return true;
        }
        if (outgoing == KidsAlbumPage.class && incoming == KidsPhotoPage.class) {
            return true;
        }
        if (outgoing == ParentAddedAlbumPage.class && incoming == KidsPhotoPage.class) {
            return true;
        }
        if (outgoing == PhotoShareTimeBucketPage.class && incoming == PhotoSharePhotoPage.class) {
            return true;
        }
        if (outgoing == PhotoShareTimeBucketPage.class && incoming == SelectionPreview.class) {
            return true;
        }
        if (outgoing == MapAlbumPage.class && incoming == GalleryMediaPhotoPage.class) {
            return true;
        }
        if (outgoing == StoryAlbumPage.class && incoming == GalleryMediaPhotoPage.class) {
            return true;
        }
        if (outgoing == StoryAlbumPage.class && incoming == SelectionPreview.class) {
            return true;
        }
        if (outgoing == RecycleAlbumPage.class && incoming == SelectionPreview.class) {
            return true;
        }
        if (outgoing == RecycleAlbumPage.class && incoming == GalleryMediaPhotoPage.class) {
            return true;
        }
        return false;
    }

    private boolean isAlbumFromPhoto(Class<? extends ActivityState> outgoing, Class<? extends ActivityState> incoming) {
        if (outgoing == PhotoPage.class && incoming == SlotAlbumPage.class) {
            return true;
        }
        if (outgoing == SelectionPreview.class && incoming == SlotAlbumPage.class) {
            return true;
        }
        if (outgoing == KeyguardPhotoPage.class && incoming == KeyguardPage.class) {
            return true;
        }
        if (outgoing == PhotoPage.class && incoming == TimeBucketPage.class) {
            return true;
        }
        if (outgoing == SelectionPreview.class && incoming == TimeBucketPage.class) {
            return true;
        }
        if (outgoing == GalleryMediaPhotoPage.class && incoming == TimeBucketPage.class) {
            return true;
        }
        if (outgoing == PhotoSharePhotoPage.class && incoming == PhotoShareAlbumPage.class) {
            return true;
        }
        if (outgoing == GalleryMediaPhotoPage.class && incoming == PhotoShareAlbumPage.class) {
            return true;
        }
        if (outgoing == SelectionPreview.class && incoming == PhotoShareAlbumPage.class) {
            return true;
        }
        if (outgoing == PhotoPage.class && incoming == MapAlbumPage.class) {
            return true;
        }
        if (outgoing == SelectionPreview.class && incoming == MapAlbumPage.class) {
            return true;
        }
        if (outgoing == KidsPhotoPage.class && incoming == KidsAlbumPage.class) {
            return true;
        }
        if (outgoing == KidsPhotoPage.class && incoming == ParentAddedAlbumPage.class) {
            return true;
        }
        if (outgoing == PhotoSharePhotoPage.class && incoming == PhotoShareTimeBucketPage.class) {
            return true;
        }
        if (outgoing == SelectionPreview.class && incoming == PhotoShareTimeBucketPage.class) {
            return true;
        }
        if (outgoing == GalleryMediaPhotoPage.class && incoming == MapAlbumPage.class) {
            return true;
        }
        if (outgoing == GalleryMediaPhotoPage.class && incoming == StoryAlbumPage.class) {
            return true;
        }
        if (outgoing == SelectionPreview.class && incoming == StoryAlbumPage.class) {
            return true;
        }
        if (outgoing == GalleryMediaPhotoPage.class && incoming == RecycleAlbumPage.class) {
            return true;
        }
        if (outgoing == SelectionPreview.class && incoming == RecycleAlbumPage.class) {
            return true;
        }
        return false;
    }

    protected void performHapticFeedback(int feedbackConstant) {
        this.mHost.getActivity().getWindow().getDecorView().performHapticFeedback(feedbackConstant, 1);
    }

    protected void onPause() {
        if ((this.mFlags & 1024) != 0) {
            this.mHost.getActivity().unregisterReceiver(this.mPowerIntentReceiver);
        }
        if (this.mNextTransition != Transition.None) {
            PreparePageFadeoutTexture.prepareFadeOutTexture(this.mHost, this.mContentPane);
            if (this.mNextTransition == Transition.PhotoIncoming) {
                OpenAnimationProxyView openAnimationProxyView = (OpenAnimationProxyView) this.mHost.getTransitionStore().get(AbsAlbumPage.KEY_PROXY_VIEW, null);
                if (openAnimationProxyView != null) {
                    openAnimationProxyView.setIntroAnimation(new StateTransitionAnimation(this.mNextTransition, this.mHost.getTransitionStore()));
                    this.mHost.getGLRoot().setAnimationProxyView(openAnimationProxyView);
                } else {
                    this.mHost.getTransitionStore().put(KEY_TRANSITION_IN, this.mNextTransition);
                }
            } else {
                this.mHost.getTransitionStore().put(KEY_TRANSITION_IN, this.mNextTransition);
            }
            this.mNextTransition = Transition.None;
        }
    }

    void resume() {
        GLHost holder = this.mHost;
        setScreenFlags();
        holder.getActivity().invalidateOptionsMenu();
        if (this.mHost.inflatable()) {
            onCreateActionBar(null);
            this.mActionBarCreated = true;
        }
        this.mHost.getGLRoot().setLightsOutMode((this.mFlags & 2) != 0);
        ResultEntry entry = this.mReceivedResults;
        if (entry != null) {
            this.mReceivedResults = null;
            onStateResult(entry.requestCode, entry.resultCode, entry.resultData);
        }
        if ((this.mFlags & 1024) != 0) {
            IntentFilter filter = new IntentFilter();
            filter.addAction("android.intent.action.BATTERY_CHANGED");
            holder.getActivity().registerReceiver(this.mPowerIntentReceiver, filter);
        }
        onResume();
        this.mHost.getTransitionStore().clear();
    }

    protected void onResume() {
        if (needClearAnimationProxyViewWhenResume()) {
            this.mHost.getGLRoot().clearAnimationProxyView(true);
        }
        this.mNextTransition = (Transition) this.mHost.getTransitionStore().get(KEY_TRANSITION_IN, Transition.None);
        if (this.mNextTransition != Transition.None) {
            this.mIntroAnimation = new StateTransitionAnimation(this.mNextTransition, this.mHost.getTransitionStore());
            this.mNextTransition = Transition.None;
        }
    }

    protected boolean onCreateActionBar(Menu menu) {
        return true;
    }

    protected boolean onItemSelected(Action action) {
        return false;
    }

    protected void onDestroy() {
        this.mDestroyed = true;
    }

    boolean isDestroyed() {
        return this.mDestroyed;
    }

    public boolean isFinishing() {
        return this.mIsFinishing;
    }

    protected boolean isActionBarCreated() {
        return this.mActionBarCreated;
    }

    protected MenuInflater getSupportMenuInflater() {
        return this.mHost.getActivity().getMenuInflater();
    }

    public void onNavigationBarChanged(boolean show, int height) {
    }

    protected void onStart() {
    }

    protected void onStop() {
    }

    protected void onUserLeaveHint() {
    }

    protected void onCleanGLContext() {
    }

    protected boolean onKeyDown(int keyCode, KeyEvent event) {
        return false;
    }

    protected boolean onKeyUp(int keyCode, KeyEvent event) {
        return false;
    }

    protected void onUserSelected(boolean selected) {
    }

    protected boolean needClearAnimationProxyViewWhenResume() {
        return true;
    }

    public void pauseByKeyguard(boolean keyguardLocked) {
        this.mPausedByKeyguard = keyguardLocked;
    }

    protected boolean needFreeSlotContent() {
        return !this.mPausedByKeyguard;
    }
}
