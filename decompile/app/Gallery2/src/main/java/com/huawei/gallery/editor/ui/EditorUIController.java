package com.huawei.gallery.editor.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.FrameLayout.LayoutParams;
import android.widget.RelativeLayout;
import com.android.gallery3d.R;
import com.android.gallery3d.app.TransitionStore;
import com.android.gallery3d.ui.GLRoot;
import com.android.gallery3d.util.GalleryUtils;
import com.huawei.gallery.actionbar.Action;
import com.huawei.gallery.actionbar.view.SimpleActionItem;
import com.huawei.gallery.app.AbsAlbumPage;
import com.huawei.gallery.editor.animation.EditorAnimation;
import com.huawei.gallery.editor.animation.EditorAnimation.Delegate;
import com.huawei.gallery.editor.animation.EditorAnimation.EditorAnimationListener;
import com.huawei.gallery.editor.app.EditorState.ActionInfo;
import com.huawei.gallery.editor.glrender.BaseRender.EditorViewDelegate;
import com.huawei.gallery.editor.tools.EditorConstant;
import com.huawei.gallery.util.LayoutHelper;

public abstract class EditorUIController implements OnClickListener {
    protected ViewGroup mContainer;
    protected Context mContext;
    protected EditorViewDelegate mEditorViewDelegate;
    protected Delegate mFootDelegate = new Delegate() {
        public View getAnimationTargetView() {
            return EditorUIController.this.getFootAnimationTargetView();
        }

        public int getAnimationDuration() {
            return -1;
        }

        public int getTipHeight() {
            return EditorUIController.this.getMenuHeight();
        }

        public boolean isPort() {
            return EditorUIController.this.mEditorViewDelegate.isPort();
        }
    };
    protected ViewGroup mFootGroupRoot;
    protected EditorHeadGroupView mHeadGroupView;
    protected EditorAnimationListener mHideFootAnimeListener = new EditorAnimationListener() {
        public void onAnimationEnd() {
            if (!EditorUIController.this.mListener.isActive()) {
                EditorUIController.this.onHideFootAnimeEnd();
            }
        }
    };
    protected final LayoutInflater mInflater;
    protected final Listener mListener;
    protected ViewGroup mParentLayout;
    protected EditorAnimationListener mShowFootAnimeListener = new EditorAnimationListener() {
        public void onAnimationEnd() {
            if (EditorUIController.this.mListener.isActive()) {
                EditorUIController.this.onShowFootAnimeEnd();
            }
        }
    };
    protected final TransitionStore mTransitionStore = new TransitionStore();

    public interface Listener {
        boolean isActive();

        void onActionItemClick(Action action);
    }

    public EditorUIController(Context context, ViewGroup parentLayout, Listener listener, EditorViewDelegate EditorViewDelegate) {
        this.mContext = context;
        this.mParentLayout = parentLayout;
        this.mListener = listener;
        this.mEditorViewDelegate = EditorViewDelegate;
        this.mInflater = (LayoutInflater) this.mContext.getSystemService("layout_inflater");
    }

    public void updateParentLayout(RelativeLayout layout) {
        this.mParentLayout = layout;
    }

    protected int getControlLayoutId() {
        return R.layout.editor_controls;
    }

    public void show() {
        if (this.mContainer == null) {
            int darkThemeId = this.mContext.getResources().getIdentifier("androidhwext:style/Theme.Emui.Dark", null, null);
            this.mContext.getTheme().applyStyle(this.mContext.getResources().getIdentifier("androidhwext:style/Theme.Emui", null, null), true);
            this.mContainer = (ViewGroup) ((LayoutInflater) this.mContext.getSystemService("layout_inflater")).inflate(getControlLayoutId(), this.mParentLayout, false);
            inflateHeadLayout();
            inflateFootLayout();
            this.mContext.getTheme().applyStyle(darkThemeId, true);
        }
        attachToParent();
        updateContainerLayoutParams();
        startShowAnime();
    }

    protected void attachToParent() {
        if (this.mContainer.getParent() == null) {
            this.mParentLayout.addView(this.mContainer);
        }
    }

    public void hide() {
        startHideAnime();
    }

    protected boolean hasFootAction() {
        return true;
    }

    protected void onShowFootAnimeEnd() {
    }

    protected void onHideFootAnimeEnd() {
        if (this.mContainer != null) {
            this.mParentLayout.removeView(this.mContainer);
            this.mContainer = null;
        }
    }

    protected View getFootAnimationTargetView() {
        return this.mFootGroupRoot;
    }

    protected void showHeadAnime() {
        EditorAnimation.startFadeAnimationForViewGroup(this.mHeadGroupView, 1, AbsAlbumPage.LAUNCH_QUIK_ACTIVITY, 100, null);
        this.mHeadGroupView.startAnimeUp(true, GalleryUtils.dpToPixel(12), AbsAlbumPage.LAUNCH_QUIK_ACTIVITY, 100);
    }

    protected void hideHeadAnime() {
        EditorAnimation.startFadeAnimationForViewGroup(this.mHeadGroupView, 2, 300, 0, null);
        this.mHeadGroupView.startAnimeUp(false, GalleryUtils.dpToPixel(12), 300, 0);
    }

    protected void showFootAnime() {
        EditorAnimation.startEditorAnimation(this.mFootDelegate, 1, AbsAlbumPage.LAUNCH_QUIK_ACTIVITY, this.mShowFootAnimeListener, 100);
    }

    protected void hideFootAnime() {
        EditorAnimation.startEditorAnimation(this.mFootDelegate, 2, 300, this.mHideFootAnimeListener, 0);
    }

    protected void startShowAnime() {
        showHeadAnime();
        showFootAnime();
    }

    protected void startHideAnime() {
        hideHeadAnime();
        hideFootAnime();
    }

    private void inflateHeadLayout() {
        this.mHeadGroupView = (EditorHeadGroupView) this.mContainer.findViewById(R.id.editor_head_layout);
        this.mHeadGroupView.setLayoutParams(new LayoutParams(-1, this.mEditorViewDelegate.getActionBarHeight()));
        this.mHeadGroupView.initView(this);
    }

    private int getFootLayoutId() {
        return this.mEditorViewDelegate.isPort() ? R.id.editor_foot_bar_port : R.id.editor_foot_bar_land;
    }

    protected void saveUIController() {
    }

    protected void restoreUIController() {
    }

    protected void removeUIController() {
        if (this.mFootGroupRoot != null) {
            this.mFootGroupRoot.removeAllViews();
            this.mFootGroupRoot.setVisibility(8);
            this.mFootGroupRoot = null;
        }
    }

    private void inflaterUIController() {
        inflateHeadLayout();
        this.mEditorViewDelegate.updateBackground();
        this.mEditorViewDelegate.updateOriginalCompareButton();
        inflateFootLayout();
    }

    public void onConfigurationChanged() {
        if (this.mContainer != null) {
            int darkThemeId = this.mContext.getResources().getIdentifier("androidhwext:style/Theme.Emui.Dark", null, null);
            this.mContext.getTheme().applyStyle(this.mContext.getResources().getIdentifier("androidhwext:style/Theme.Emui", null, null), true);
            saveUIController();
            removeUIController();
            inflaterUIController();
            restoreUIController();
            this.mContext.getTheme().applyStyle(darkThemeId, true);
        }
    }

    protected int getFootLayout() {
        return 0;
    }

    protected void inflateFootLayout() {
        this.mFootGroupRoot = (ViewGroup) this.mContainer.findViewById(getFootLayoutId());
        this.mFootGroupRoot.setVisibility(0);
        if (getFootLayout() != 0) {
            this.mInflater.inflate(getFootLayout(), this.mFootGroupRoot, true);
            if (hasFootAction()) {
                SimpleActionItem leftActionItem = (SimpleActionItem) this.mFootGroupRoot.findViewById(R.id.foot_select_left);
                SimpleActionItem rightActionItem = (SimpleActionItem) this.mFootGroupRoot.findViewById(R.id.foot_select_right);
                leftActionItem.setAction(Action.NO);
                rightActionItem.setAction(Action.OK);
                leftActionItem.setOnClickListener(this);
                rightActionItem.setOnClickListener(this);
            }
        }
    }

    public void changeActionBar(ActionInfo actionInfo) {
        if (this.mHeadGroupView != null) {
            this.mHeadGroupView.changeActionBar(actionInfo);
        }
    }

    public void onClick(View v) {
        GLRoot root = this.mEditorViewDelegate.getGLRoot();
        if (root != null) {
            root.lockRenderThread();
            try {
                if ((v instanceof SimpleActionItem) && this.mListener != null) {
                    Action action = ((SimpleActionItem) v).getAction();
                    if (action == Action.BACK) {
                        this.mListener.onActionItemClick(Action.NO);
                    } else {
                        this.mListener.onActionItemClick(action);
                    }
                }
                root.unlockRenderThread();
            } catch (Throwable th) {
                root.unlockRenderThread();
            }
        }
    }

    public void updateContainerLayoutParams() {
        if (this.mContainer != null) {
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) this.mContainer.getLayoutParams();
            if (this.mContainer.getResources().getConfiguration().orientation != 2) {
                params.rightMargin = 0;
                params.bottomMargin = this.mEditorViewDelegate.getNavigationBarHeight();
            } else if (LayoutHelper.isDefaultLandOrientationProduct()) {
                params.rightMargin = 0;
                params.bottomMargin = LayoutHelper.getNavigationBarHeightForDefaultLand();
            } else {
                params.rightMargin = this.mEditorViewDelegate.getNavigationBarHeight();
                params.bottomMargin = 0;
            }
            this.mContainer.setLayoutParams(params);
        }
    }

    public int getMenuHeight() {
        return this.mEditorViewDelegate.isPort() ? EditorConstant.MENU_HEIGHT_PORT : EditorConstant.MENU_HEIGHT_LAND;
    }

    public int getSubMenuHeight() {
        return EditorConstant.SUB_MENU_HEIGHT_SMALL;
    }
}
