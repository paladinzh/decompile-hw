package com.huawei.gallery.editor.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import com.android.gallery3d.R;
import com.android.gallery3d.app.TransitionStore;
import com.android.gallery3d.util.ReportToBigData;
import com.huawei.gallery.editor.animation.EditorAnimation;
import com.huawei.gallery.editor.ui.PaintIllusionMenu.MenuClickListener;
import com.huawei.gallery.util.ColorfulUtils;
import com.huawei.watermark.manager.parse.WMElement;
import java.util.Locale;

public class IllusionBar extends LinearLayout implements MenuClickListener {
    public OnSeekBarChangeListener mOnSeekBarChangeListener = new OnSeekBarChangeListener() {
        public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
            int progress = (int) ((((float) seekBar.getProgress()) / ((float) seekBar.getMax())) * 100.0f);
            if (IllusionBar.this.mUIListener != null) {
                IllusionBar.this.mUIListener.onProgressChanged((float) progress);
            }
        }

        public void onStartTrackingTouch(SeekBar seekBar) {
        }

        public void onStopTrackingTouch(SeekBar seekBar) {
            IllusionBar.this.reportDataForIllusionProgress(seekBar);
        }
    };
    private SeekBar mSeekbar;
    private PaintIllusionMenu mSubMenuContainer;
    private UIListener mUIListener;

    public enum STYLE {
        CIRCLE,
        BAND,
        WHOLE,
        UNKONW
    }

    public interface UIListener {
        int getCurrentSeekbarValue();

        void onProgressChanged(float f);

        void onStyleChanged(STYLE style);

        void onSubMenuHide();

        void onSubMenuShow();
    }

    public IllusionBar(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void initialize(UIListener uiListener) {
        this.mUIListener = uiListener;
        this.mSubMenuContainer = (PaintIllusionMenu) findViewById(R.id.paint_illusion_menu);
        this.mSubMenuContainer.setMenuClickListener(this);
        initSubMenuListener();
    }

    public void saveUIController(TransitionStore transitionStore) {
        this.mSubMenuContainer.saveUIController(transitionStore);
    }

    public void restoreUIController(TransitionStore transitionStore) {
        this.mSubMenuContainer.restoreUIController(transitionStore);
        Object obj = transitionStore.get("seekbar_show");
        if (obj instanceof Boolean) {
            this.mSeekbar.setAlpha(((Boolean) obj).booleanValue() ? WMElement.CAMERASIZEVALUE1B1 : 0.0f);
        }
    }

    public void onSelect(int viewId, boolean isShowSubMenu) {
        if (isShowSubMenu) {
            EditorAnimation.startFadeAnimationForViewGroup(this.mSeekbar, 1, 0, null);
            if (this.mUIListener != null) {
                this.mUIListener.onSubMenuShow();
            }
        } else {
            EditorAnimation.startFadeAnimationForViewGroup(this.mSeekbar, 2, 0, null);
            if (this.mUIListener != null) {
                this.mUIListener.onSubMenuHide();
            }
        }
        if (this.mUIListener != null) {
            this.mUIListener.onStyleChanged((STYLE) findViewById(viewId).getTag());
        }
    }

    public void onChangeSelect(int oldViewId, int newViewId) {
        if (this.mUIListener != null) {
            this.mUIListener.onStyleChanged((STYLE) findViewById(newViewId).getTag());
        }
    }

    public void hide() {
        this.mSubMenuContainer.hide();
    }

    private void initSubMenuListener() {
        if ("pk".equals(getResources().getConfiguration().locale.getCountry().toLowerCase(Locale.US))) {
            ((ViewGroup) findViewById(R.id.seekbar_controls)).setLayoutDirection(0);
        }
        this.mSeekbar = (SeekBar) findViewById(R.id.progress_seekbar);
        this.mSeekbar.setOnSeekBarChangeListener(this.mOnSeekBarChangeListener);
        this.mSeekbar.setAlpha(0.0f);
        if (this.mUIListener != null) {
            this.mSeekbar.setProgress(this.mUIListener.getCurrentSeekbarValue());
        }
        ColorfulUtils.decorateColorfulForSeekbar(getContext(), this.mSeekbar);
    }

    private void reportDataForIllusionProgress(SeekBar seekBar) {
        if (seekBar != null) {
            int progress = (int) ((((float) seekBar.getProgress()) / ((float) seekBar.getMax())) * 100.0f);
            ReportToBigData.report(120, String.format("{IllusionProgress:%s}", new Object[]{Integer.valueOf(progress)}));
        }
    }
}
