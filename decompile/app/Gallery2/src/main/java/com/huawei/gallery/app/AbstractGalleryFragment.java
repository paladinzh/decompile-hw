package com.huawei.gallery.app;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import com.android.gallery3d.app.GalleryContext;
import com.android.gallery3d.util.GalleryLog;
import com.android.gallery3d.util.MultiWindowStatusHolder;
import com.android.gallery3d.util.MultiWindowStatusHolder.IMultiWindowModeChangeListener;
import com.android.gallery3d.util.TraceController;
import com.huawei.gallery.actionbar.Action;
import com.huawei.gallery.actionbar.GalleryActionBar;
import com.huawei.gallery.util.LayoutHelper;
import com.huawei.gallery.util.UIUtils;

public abstract class AbstractGalleryFragment extends Fragment {
    private boolean mActionBarUpdateWhenUserHint;
    private IMultiWindowModeChangeListener mMultiWindowModeChangeListener;
    protected int mRequestedFeature = -1;
    protected boolean mUserHaveFirstLook;

    public abstract void onActionItemClicked(Action action);

    public abstract boolean onBackPressed();

    public void onCreate(Bundle savedInstanceState) {
        TraceController.beginSection("AbstractGalleryFragement.onCreate");
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            boolean z;
            GalleryLog.d("AbstractGalleryFragment", this + " start savedInstanceState mUserHaveFirstLook:" + this.mUserHaveFirstLook);
            if (this.mUserHaveFirstLook) {
                z = true;
            } else {
                z = savedInstanceState.getBoolean("key-user-have-first-look", false);
            }
            this.mUserHaveFirstLook = z;
            GalleryLog.d("AbstractGalleryFragment", this + " end savedInstanceState mUserHaveFirstLook:" + this.mUserHaveFirstLook);
        }
        if (needMultiWindowFocusChangeCallback()) {
            this.mMultiWindowModeChangeListener = new IMultiWindowModeChangeListener() {
                public void multiWindowModeChangeCallback(boolean isInMultiWindowMode) {
                    AbstractGalleryFragment.this.relayoutIfNeed();
                }
            };
        }
        TraceController.endSection();
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        TraceController.beginSection("AbstractGalleryFragment.onActivityCreated");
        super.onActivityCreated(savedInstanceState);
        if (getUserVisibleHint()) {
            onCreateActionBar(null);
        }
        TraceController.endSection();
    }

    public void onResume() {
        super.onResume();
        if (needMultiWindowFocusChangeCallback()) {
            MultiWindowStatusHolder.registerMultiWindowModeChangeListener(this.mMultiWindowModeChangeListener, false);
        }
    }

    protected void onCreateActionBar(Menu menu) {
    }

    public GalleryContext getGalleryContext() {
        return (GalleryContext) getActivity();
    }

    public GalleryActionBar getGalleryActionBar() {
        if (getActivity() instanceof AbstractGalleryActivity) {
            return ((AbstractGalleryActivity) getActivity()).getGalleryActionBar();
        }
        throw new IllegalStateException(getClass().getSimpleName() + " should be holden by " + AbstractGalleryActivity.class.getSimpleName());
    }

    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (!isVisibleToUser || !isResumed()) {
            this.mActionBarUpdateWhenUserHint = false;
        } else if (!this.mActionBarUpdateWhenUserHint) {
            onCreateActionBar(null);
            this.mActionBarUpdateWhenUserHint = true;
        }
        if (!isVisibleToUser) {
            this.mRequestedFeature = -1;
        }
    }

    public void requestFeature(int feature) {
        this.mRequestedFeature = feature;
        if (getUserVisibleHint()) {
            boolean z;
            GalleryActionBar actionBar = getGalleryActionBar();
            if ((feature & 4) != 0) {
                actionBar.setHeadBackground(0);
                actionBar.setActionPanelStyle(1);
            } else {
                actionBar.setHeadDefaultBackground();
                actionBar.setActionPanelStyle(0);
            }
            if ((feature & 2) == 0) {
                z = true;
            } else {
                z = false;
            }
            actionBar.setActionPanelVisible(z);
            if ((feature & 1) == 0) {
                z = true;
            } else {
                z = false;
            }
            actionBar.setHeadBarVisible(z);
            View view = getView();
            if (view != null) {
                if ((feature & 256) != 0) {
                    UIUtils.setNavigationBarIsOverlay(view, true);
                    actionBar.setNavigationMargin(LayoutHelper.getNavigationBarHeight());
                } else {
                    UIUtils.setNavigationBarIsOverlay(view, false);
                    actionBar.setNavigationMargin(0);
                }
                setWindowPadding(feature);
                return;
            }
            return;
        }
        setWindowPadding(feature);
    }

    protected void setWindowPadding(int feature, View view) {
        int topMargin = 0;
        if ((feature & 9) == 0) {
            topMargin = getGalleryActionBar().getActionBarHeight();
        }
        if (!MultiWindowStatusHolder.isInMultiWindowMode() && (feature & 96) == 0) {
            topMargin += LayoutHelper.getStatusBarHeight();
        }
        int bottomMargin = 0;
        int rightMargin = 0;
        if ((feature & 256) != 0) {
            if (LayoutHelper.isPort()) {
                bottomMargin = LayoutHelper.getNavigationBarHeight() + 0;
            } else {
                rightMargin = LayoutHelper.getNavigationBarHeight() + 0;
            }
        }
        view.setPadding(0, topMargin, rightMargin, bottomMargin);
    }

    protected void setWindowPadding(int feature) {
        setWindowPadding(feature, getView());
    }

    public void onNavigationBarChanged(boolean show, int height) {
        if (getUserVisibleHint() && getView() != null) {
            if (navigationFeatureOverlay()) {
                getGalleryActionBar().setNavigationMargin(height);
            }
            setWindowPadding(this.mRequestedFeature);
        }
    }

    protected boolean navigationFeatureOverlay() {
        return (this.mRequestedFeature & 256) != 0;
    }

    protected boolean onKeyDown(int keyCode, KeyEvent event) {
        return false;
    }

    protected boolean onKeyUp(int keyCode, KeyEvent event) {
        return false;
    }

    protected void onUserSelected(boolean selected) {
        GalleryLog.d("AbstractGalleryFragment", this + " on user selected");
        this.mUserHaveFirstLook = true;
    }

    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("key-user-have-first-look", this.mUserHaveFirstLook);
    }

    protected boolean needMultiWindowFocusChangeCallback() {
        return false;
    }

    protected void relayoutIfNeed() {
    }
}
