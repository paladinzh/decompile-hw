package com.huawei.gallery.refocus.app;

import android.content.res.Configuration;
import android.graphics.Point;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ProgressBar;
import com.android.gallery3d.app.GalleryContext;
import com.android.gallery3d.ui.BitmapScreenNail;
import com.android.gallery3d.ui.ScreenNail;
import com.android.gallery3d.ui.TiledScreenNail;
import com.huawei.gallery.actionbar.Action;
import com.huawei.gallery.app.GLHost;
import com.huawei.gallery.refocus.app.RefocusPage.ActionInfo;
import com.huawei.gallery.refocus.ui.RefocusIndicator;

public abstract class State {
    protected ActionInfo mActionInfo;
    protected GalleryContext mContext;
    protected boolean mDisableRefocusAndExit;
    protected AbsRefocusController mEditorController;
    protected AbsRefocusDelegate mEditorDelegate;
    protected volatile boolean mEnableDoRefocus = true;
    protected volatile boolean mEnableSaveAs = true;
    protected RefocusIndicator mFocusIndicator;
    protected ProgressBar mProgressbar;
    protected View mView;

    public State(GalleryContext context, View parentLayout, AbsRefocusDelegate delegate) {
        this.mContext = context;
        this.mView = parentLayout;
        this.mEditorDelegate = delegate;
        initData();
        initView();
    }

    public void resume() {
    }

    public void pause() {
    }

    public void destroy() {
        this.mEditorController.cleanUp();
    }

    public boolean onActionItemClick(Action action) {
        return false;
    }

    public boolean onBackPressed() {
        return false;
    }

    public RefocusIndicator getFocusIndicatorView() {
        return this.mFocusIndicator;
    }

    public ProgressBar getProgressBar() {
        return this.mProgressbar;
    }

    public ActionInfo getActionInfo() {
        return this.mActionInfo;
    }

    public AbsRefocusController getAllFocusController() {
        return this.mEditorController;
    }

    public void onLayoutChanged(GLHost host, int naviHeight) {
    }

    public void setWideApertureValue(int value) {
    }

    public void scrollToSelectedFilter() {
    }

    public void doRefocus(Message msg) {
        if (!this.mDisableRefocusAndExit) {
            Point touchPoint = new Point(msg.arg1, msg.arg2);
            if (this.mEnableDoRefocus && this.mEnableSaveAs) {
                this.mEnableDoRefocus = false;
                if (!this.mEditorController.doRefocus(touchPoint)) {
                    this.mEnableDoRefocus = true;
                }
                return;
            }
            this.mEditorDelegate.sendMessageDelayed(this.mEditorDelegate.getDoRefocusMessageID(), msg.arg1, msg.arg2, 30);
        }
    }

    public void finishRefocus(ScreenNail screenNail) {
        if (screenNail instanceof BitmapScreenNail) {
            if (!((BitmapScreenNail) screenNail).isLoaded()) {
                this.mEditorDelegate.sendMessageDelayed(this.mEditorDelegate.getFinishRefocusMessageID(), 0, 0, 100);
                return;
            }
        } else if ((screenNail instanceof TiledScreenNail) && !((TiledScreenNail) screenNail).isLoaded()) {
            this.mEditorDelegate.sendMessageDelayed(this.mEditorDelegate.getFinishRefocusMessageID(), 0, 0, 100);
            return;
        }
        this.mEnableDoRefocus = true;
    }

    public void applyFilter(Message msg) {
    }

    public void showActionToast() {
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return false;
    }

    public boolean onKeyUp(int keyCode, KeyEvent event) {
        return false;
    }

    public void enableSaveAction() {
    }

    public void disableExitEditPage(boolean enabled) {
    }

    public void enableOperations(boolean enabled) {
    }

    public void onWideApertureValueChanged(int value) {
    }

    protected void initData() {
    }

    protected void initView() {
    }

    public int getScrollViewHeight() {
        return 0;
    }

    public int getScrollViewWidth() {
        return 0;
    }

    public void onConfigurationChanged(Configuration config) {
    }

    public void onNavigationBarChanged(boolean show, int height) {
    }

    public void resetIndicatorLocation() {
    }

    public void showFootGroupView() {
    }
}
