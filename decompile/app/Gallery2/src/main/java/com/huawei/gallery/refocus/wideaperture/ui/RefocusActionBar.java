package com.huawei.gallery.refocus.wideaperture.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import com.android.gallery3d.R;
import com.android.gallery3d.app.TransitionStore;
import com.android.gallery3d.util.GalleryLog;
import com.huawei.gallery.editor.animation.EditorAnimation;
import com.huawei.gallery.editor.animation.EditorAnimation.Delegate;
import com.huawei.gallery.editor.animation.EditorAnimation.EditorAnimationListener;
import com.huawei.gallery.refocus.wideaperture.ui.ApertureMenu.MENU;
import com.huawei.gallery.refocus.wideaperture.ui.ApertureMenu.MenuClickListener;
import com.huawei.gallery.ui.VerticalSeekBar;
import com.huawei.gallery.util.ColorfulUtils;
import com.huawei.gallery.util.LayoutHelper;
import com.huawei.watermark.manager.parse.WMElement;
import java.util.Locale;

public class RefocusActionBar extends LinearLayout implements MenuClickListener, OnSeekBarChangeListener {
    private FrameLayout mFilterScrollView;
    private Delegate mFilterScrollViewDelegate = new Delegate() {
        public View getAnimationTargetView() {
            return RefocusActionBar.this.findViewById(R.id.listItems);
        }

        public int getAnimationDuration() {
            return 350;
        }

        public int getTipHeight() {
            return 300;
        }

        public boolean isPort() {
            return LayoutHelper.isPort();
        }
    };
    private boolean mIsAnimationEnd = true;
    private boolean mIsSeekBarTrackingTouch = false;
    private int mLevelCount;
    private ViewGroup mSeekBarContoller;
    private SeekBar mSeekbar;
    private ApertureMenu mSubMenuContainer;
    private TextView mTextView;
    private UIListener mUIListener;

    public interface UIListener {
        int getCurrentSeekbarValue();

        String getCurrentTextValue();

        int getLevelCount();

        void onMenuChanged(MENU menu);

        void onProgressChanged(int i);

        void showRefocusIndictor(float f);
    }

    public RefocusActionBar(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void initialize(UIListener uiListener) {
        this.mUIListener = uiListener;
        this.mSubMenuContainer = (ApertureMenu) findViewById(R.id.aperture_menu);
        this.mSubMenuContainer.setMenuClickListener(this);
        initSubMenuListener();
    }

    public void onSelect(int viewId, boolean isShowSubMenu) {
        if (isShowSubMenu) {
            if (viewId == MENU.APERTURE.ordinal()) {
                EditorAnimation.startFadeAnimationForViewGroup(this.mSeekBarContoller, 1, 0, null);
            } else if (viewId == MENU.FILTER.ordinal()) {
                this.mFilterScrollView.setVisibility(0);
                EditorAnimation.startAnimationForAllChildView(this.mFilterScrollViewDelegate.getAnimationTargetView(), this.mFilterScrollViewDelegate.getAnimationDuration(), this.mFilterScrollViewDelegate.getTipHeight(), 1, 0, 30, null, this.mFilterScrollViewDelegate.isPort());
            }
        } else if (viewId == MENU.APERTURE.ordinal()) {
            EditorAnimation.startFadeAnimationForViewGroup(this.mSeekBarContoller, 2, 0, null);
        } else if (viewId == MENU.FILTER.ordinal()) {
            EditorAnimation.startAnimationForAllChildView(this.mFilterScrollViewDelegate.getAnimationTargetView(), this.mFilterScrollViewDelegate.getAnimationDuration(), this.mFilterScrollViewDelegate.getTipHeight(), 1, 0, 30, new EditorAnimationListener() {
                public void onAnimationEnd() {
                    RefocusActionBar.this.mFilterScrollView.setVisibility(4);
                }
            }, this.mFilterScrollViewDelegate.isPort());
        }
    }

    public void onChangeSelect(int oldViewId, final int newViewId) {
        if (oldViewId == MENU.APERTURE.ordinal()) {
            this.mIsAnimationEnd = false;
            EditorAnimation.startFadeAnimationForViewGroup(this.mSeekBarContoller, 2, 0, new EditorAnimationListener() {
                public void onAnimationEnd() {
                    RefocusActionBar.this.mSeekBarContoller.setVisibility(8);
                    RefocusActionBar.this.showView(newViewId);
                }
            });
        } else if (oldViewId == MENU.FILTER.ordinal()) {
            this.mIsAnimationEnd = false;
            EditorAnimation.startAnimationForAllChildView(this.mFilterScrollViewDelegate.getAnimationTargetView(), this.mFilterScrollViewDelegate.getAnimationDuration(), this.mFilterScrollViewDelegate.getTipHeight(), 2, 0, 30, new EditorAnimationListener() {
                public void onAnimationEnd() {
                    RefocusActionBar.this.mFilterScrollView.setVisibility(8);
                    RefocusActionBar.this.showView(newViewId);
                }
            }, this.mFilterScrollViewDelegate.isPort());
        }
    }

    public void restoreOperationViewSelectionSate(int restoreViewId) {
        if (restoreViewId == MENU.APERTURE.ordinal()) {
            this.mFilterScrollView.setVisibility(8);
            this.mSeekBarContoller.setVisibility(0);
        } else if (restoreViewId == MENU.FILTER.ordinal()) {
            this.mSeekBarContoller.setVisibility(8);
            this.mFilterScrollView.setVisibility(0);
        }
    }

    private void showView(final int viewId) {
        EditorAnimationListener animationListener = new EditorAnimationListener() {
            public void onAnimationEnd() {
                if (RefocusActionBar.this.mUIListener != null) {
                    RefocusActionBar.this.mUIListener.onMenuChanged((MENU) RefocusActionBar.this.findViewById(viewId).getTag());
                }
                RefocusActionBar.this.mIsAnimationEnd = true;
            }
        };
        if (viewId == MENU.APERTURE.ordinal()) {
            this.mSeekBarContoller.setVisibility(0);
            EditorAnimation.startFadeAnimationForViewGroup(this.mSeekBarContoller, 1, 0, animationListener);
        } else if (viewId == MENU.FILTER.ordinal()) {
            this.mFilterScrollView.setVisibility(0);
            EditorAnimation.startAnimationForAllChildView(this.mFilterScrollViewDelegate.getAnimationTargetView(), this.mFilterScrollViewDelegate.getAnimationDuration(), this.mFilterScrollViewDelegate.getTipHeight(), 1, 0, 30, animationListener, this.mFilterScrollViewDelegate.isPort());
        }
    }

    public boolean isMenuChangeAnimationEnd() {
        return this.mIsAnimationEnd;
    }

    public void restoreOperationMenuSelectionState(TransitionStore transitionStore) {
        this.mSubMenuContainer.restoreSelectionSate(transitionStore);
        this.mSeekbar.setProgress(((Integer) transitionStore.get("seekbar_value")).intValue());
    }

    public void saveSelectionState(TransitionStore transitionStore) {
        this.mSubMenuContainer.saveSelectionState(transitionStore);
        transitionStore.put("seekbar_value", Integer.valueOf(this.mSeekbar.getProgress()));
    }

    private void initSubMenuListener() {
        if ("pk".equals(getResources().getConfiguration().locale.getCountry().toLowerCase(Locale.US))) {
            ((ViewGroup) findViewById(R.id.seekbar_controls)).setLayoutDirection(0);
        }
        this.mFilterScrollView = (FrameLayout) findViewById(R.id.itemScrollView);
        this.mFilterScrollView.setVisibility(8);
        this.mSeekBarContoller = (ViewGroup) findViewById(R.id.seekbar_controls);
        this.mTextView = (TextView) findViewById(R.id.filter_seekbar_text);
        this.mSeekbar = (SeekBar) findViewById(R.id.filter_seekbar);
        this.mSeekbar.setVisibility(0);
        this.mSeekbar.setOnSeekBarChangeListener(this);
        this.mSeekbar.setAlpha(WMElement.CAMERASIZEVALUE1B1);
        ColorfulUtils.decorateColorfulForSeekbar(getContext(), this.mSeekbar);
        this.mLevelCount = this.mUIListener.getLevelCount();
    }

    public void prepareComplete() {
        initSeekbarValue();
        updateTextValue(this.mUIListener.getCurrentTextValue());
    }

    public void initSeekbarValue() {
        this.mSeekbar.setProgress(this.mUIListener.getCurrentSeekbarValue());
    }

    public boolean isSeekBarTrackingTouch() {
        return this.mIsSeekBarTrackingTouch;
    }

    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
        float progress = (float) seekBar.getProgress();
        if (this.mUIListener != null) {
            if (b) {
                this.mUIListener.showRefocusIndictor(progress);
            }
            if ((seekBar instanceof VerticalSeekBar) && ((VerticalSeekBar) seekBar).isDragging()) {
                this.mUIListener.showRefocusIndictor(progress);
            }
            updateTextValue(this.mUIListener.getCurrentTextValue());
        }
    }

    public void onStartTrackingTouch(SeekBar seekBar) {
        this.mIsSeekBarTrackingTouch = true;
        this.mSubMenuContainer.setMenuItemClickable(false);
    }

    public void onStopTrackingTouch(SeekBar seekBar) {
        this.mIsSeekBarTrackingTouch = false;
        this.mSubMenuContainer.setMenuItemClickable(true);
        int progress = progressTransform(seekBar);
        GalleryLog.d("RefocusActionBar", "current seekbar progress: " + progress);
        if (this.mUIListener != null) {
            this.mUIListener.onProgressChanged(progress);
        }
    }

    public int progressTransform(SeekBar seekBar) {
        return progressTransform((float) seekBar.getProgress(), (float) seekBar.getMax());
    }

    public int progressTransform(float progress, float maxProgress) {
        return (int) ((progress / maxProgress) * ((float) (this.mLevelCount - 1)));
    }

    public void updateTextValue(String value) {
        this.mTextView.setText(value);
    }

    public void setAllViewsClickable(boolean enableClick) {
        for (int i = 0; i < getChildCount(); i++) {
            getChildAt(i).setClickable(enableClick);
        }
    }
}
