package com.huawei.gallery.editor.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import com.android.gallery3d.R;
import com.android.gallery3d.app.TransitionStore;
import com.android.gallery3d.util.GalleryUtils;
import com.huawei.gallery.editor.animation.EditorAnimation;
import com.huawei.gallery.editor.animation.EditorAnimation.EditorAnimationListener;
import com.huawei.gallery.editor.tools.EditorConstant;
import com.huawei.gallery.editor.ui.BasePaintMenu.Delegate;
import com.huawei.gallery.editor.ui.BasePaintMenu.MenuClickListener;
import edu.umd.cs.findbugs.annotations.SuppressWarnings;
import tmsdk.common.module.intelli_sms.SmsCheckResult;

public abstract class BasePaintBar extends LinearLayout implements MenuClickListener, OnClickListener, Delegate {
    static final int[] MENU_STROKE_DRAWABLE = new int[]{R.drawable.ic_gallery_edit_stroke_1, R.drawable.ic_gallery_edit_stroke_2, R.drawable.ic_gallery_edit_stroke_3, R.drawable.ic_gallery_edit_stroke_4, R.drawable.ic_gallery_edit_stroke_5};
    static final int[] SUB_STROKE_LEVEL_BUTTON_ID = new int[]{R.id.storke_level_1, R.id.storke_level_2, R.id.storke_level_3, R.id.storke_level_4, R.id.storke_level_5};
    @SuppressWarnings({"MS_MUTABLE_ARRAY"})
    public static final int[] SUB_STROKE_LEVEL_VALUE = new int[]{GalleryUtils.dpToPixel(3), GalleryUtils.dpToPixel(4), GalleryUtils.dpToPixel(6), GalleryUtils.dpToPixel(8), GalleryUtils.dpToPixel(10)};
    protected BasePaintMenu mBasePaintMenu;
    private final LayoutInflater mInflater;
    private boolean mIsSelectAnimeEnd = true;
    private boolean mIsUnSelectAnimeEnd = true;
    private boolean mLock = false;
    private UIListener mUIListener;

    public interface UIListener {
        void onClickBar(View view);

        void onEraseSelectedChanged(boolean z);

        void onSubMenuHide();

        void onSubMenuShow();
    }

    protected abstract int getEraseButtonId();

    protected abstract int[] getSubMenuChildButtonsId(int i);

    protected abstract int getSubMenuChildLayout(int i);

    protected abstract int getSubMenuChildRootId(int i);

    protected abstract void initMenuButtonImageSource();

    protected abstract void processClickView(View view);

    protected abstract void selectSubMenuChildButton(int i);

    public BasePaintBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mInflater = (LayoutInflater) context.getSystemService("layout_inflater");
    }

    public int getSubMenuHeight() {
        return EditorConstant.SUB_MENU_HEIGHT_SMALL;
    }

    public boolean isAnimationRunning() {
        return (this.mIsSelectAnimeEnd && this.mIsUnSelectAnimeEnd) ? false : true;
    }

    protected boolean isPort() {
        return getContext().getResources().getConfiguration().orientation == 1;
    }

    public void initialize(UIListener uiListener) {
        this.mUIListener = uiListener;
        this.mBasePaintMenu = (BasePaintMenu) findViewById(R.id.paint_menu);
        this.mBasePaintMenu.setMenuClickListener(this);
        this.mBasePaintMenu.setDelegate(this);
        initMenuButtonImageSource();
    }

    public void hide() {
        LinearLayout subMenuRoot = (LinearLayout) findViewById(R.id.paint_sub_menu_root);
        subMenuRoot.clearAnimation();
        subMenuRoot.removeAllViews();
    }

    public void onSelect(int viewId, boolean isChange) {
        if (viewId == getEraseButtonId()) {
            if (this.mUIListener != null) {
                this.mUIListener.onEraseSelectedChanged(true);
            }
            return;
        }
        this.mInflater.inflate(getSubMenuChildLayout(viewId), (LinearLayout) findViewById(R.id.paint_sub_menu_root), true);
        initSubMenuButtonListener(viewId);
        selectSubMenuChildButton(viewId);
        this.mIsSelectAnimeEnd = false;
        if (isChange) {
            EditorAnimation.startFadeAnimationForViewGroup(findViewById(getSubMenuChildRootId(viewId)), 1, SmsCheckResult.ESCT_200, new EditorAnimationListener() {
                public void onAnimationEnd() {
                    BasePaintBar.this.mIsSelectAnimeEnd = true;
                }
            });
        } else {
            EditorAnimation.startTranslationAnimationForViewGroup(findViewById(getSubMenuChildRootId(viewId)), 1, getSubMenuHeight(), 0, isPort(), new EditorAnimationListener() {
                public void onAnimationEnd() {
                    BasePaintBar.this.mIsSelectAnimeEnd = true;
                }
            });
        }
        if (this.mUIListener != null) {
            this.mUIListener.onSubMenuShow();
        }
    }

    @SuppressWarnings({"SIC_INNER_SHOULD_BE_STATIC_ANON"})
    public void onUnSelect(int viewId) {
        if (viewId == getEraseButtonId()) {
            if (this.mUIListener != null) {
                this.mUIListener.onEraseSelectedChanged(false);
            }
            return;
        }
        final LinearLayout subMenuRoot = (LinearLayout) findViewById(R.id.paint_sub_menu_root);
        final View view = findViewById(getSubMenuChildRootId(viewId));
        this.mIsUnSelectAnimeEnd = false;
        EditorAnimation.startTranslationAnimationForViewGroup(findViewById(getSubMenuChildRootId(viewId)), 2, getSubMenuHeight(), 0, isPort(), new EditorAnimationListener() {
            public void onAnimationEnd() {
                BasePaintBar.this.mIsUnSelectAnimeEnd = true;
                subMenuRoot.removeView(view);
            }
        });
        if (this.mUIListener != null) {
            this.mUIListener.onSubMenuHide();
        }
    }

    public void onChangeSelect(int oldViewId, int newViewId) {
        onUnSelect(oldViewId);
        onSelect(newViewId, oldViewId != getEraseButtonId());
    }

    private void initSubMenuButtonListener(int viewId) {
        int[] buttonId = getSubMenuChildButtonsId(viewId);
        if (buttonId != null) {
            for (int id : buttonId) {
                findViewById(id).setOnClickListener(this);
            }
        }
    }

    public void onClick(View v) {
        if (v != null && !this.mLock) {
            processClickView(v);
            if (this.mUIListener != null) {
                this.mUIListener.onClickBar(v);
            }
        }
    }

    public void lock() {
        this.mLock = true;
        this.mBasePaintMenu.lock();
    }

    public void unLock() {
        this.mLock = false;
        this.mBasePaintMenu.unLock();
    }

    protected void saveUIController(TransitionStore store) {
        this.mBasePaintMenu.saveUIController(store);
    }

    protected void restoreUIController(TransitionStore store) {
        this.mBasePaintMenu.restoreUIController(store);
    }
}
