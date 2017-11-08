package com.huawei.gallery.editor.ui;

import android.content.Context;
import android.graphics.RectF;
import android.util.SparseArray;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.FrameLayout.LayoutParams;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.android.gallery3d.R;
import com.android.gallery3d.util.GalleryUtils;
import com.android.gallery3d.util.ReportToBigData;
import com.huawei.gallery.editor.category.EditorTextView;
import com.huawei.gallery.editor.category.IconData;
import com.huawei.gallery.editor.glrender.BaseRender.EditorViewDelegate;
import com.huawei.gallery.editor.tools.EditorUtils;
import com.huawei.gallery.editor.ui.DialView.OnResetChangeListener;
import com.huawei.gallery.editor.ui.DialView.OnRotateChangeListener;
import com.huawei.gallery.editor.ui.EditorUIController.Listener;
import com.huawei.gallery.util.ColorfulUtils;

public class RotateUIController extends EditorUIController {
    private static final int HOT_PRESS_SIZE = GalleryUtils.dpToPixel(6);
    protected static final SparseArray<IconData> sIconDataContainer = new SparseArray();
    private OnClickListener mClickListener = new OnClickListener() {
        public void onClick(View view) {
            switch (view.getId()) {
                case 0:
                    RotateUIController.this.viewGone(RotateUIController.this.mDialView);
                    RotateUIController.this.viewGone(RotateUIController.this.mResetView);
                    RotateUIController.this.mRotateListener.onRotateClicked();
                    RotateUIController.this.reportDataForRotateState(RotateAction.ROTATE);
                    return;
                case 1:
                    RotateUIController.this.mRotateListener.onMirrorClicked();
                    RotateUIController.this.reportDataForRotateState(RotateAction.MIRROR);
                    return;
                default:
                    return;
            }
        }
    };
    private DialView mDialView;
    private TextView mResetView;
    private RotateListener mRotateListener;
    private final int[] sRotateTextViewId = new int[]{0, 1};

    public interface RotateListener extends Listener {
        void onAngleChanged(float f, boolean z);

        void onMirrorClicked();

        void onResetClicked();

        void onRotateClicked();

        void onStartTrackingTouch();

        void onStopTrackingTouch();
    }

    private enum RotateAction {
        STRAIGHTEN,
        RESET,
        ROTATE,
        MIRROR
    }

    static {
        sIconDataContainer.put(0, new IconData(0, (int) R.drawable.ic_gallery_edit_rotate_90, (int) R.string.rotate));
        sIconDataContainer.put(1, new IconData(1, (int) R.drawable.ic_gallery_edit_rotate_flip, (int) R.string.mirror));
    }

    public RotateUIController(Context context, ViewGroup parentLayout, RotateListener listener, EditorViewDelegate EditorViewDelegate) {
        super(context, parentLayout, listener, EditorViewDelegate);
        this.mRotateListener = listener;
    }

    protected int getControlLayoutId() {
        return R.layout.editor_rotate_controls;
    }

    public void show() {
        super.show();
        this.mDialView = (DialView) this.mContainer.findViewById(R.id.editor_rotate_dial);
        this.mDialView.setOnResetChangeListener(new OnResetChangeListener() {
            private int mMarginTop = GalleryUtils.dpToPixel(3);

            public void onResetLayoutChanged(float x, float y) {
                LayoutParams params = (LayoutParams) RotateUIController.this.mResetView.getLayoutParams();
                if (RotateUIController.this.mEditorViewDelegate.isPort()) {
                    params.topMargin = Math.round(y) - DialView.TRIANGLE_HEIGHT;
                    params.leftMargin = 0;
                    params.gravity = 49;
                    RotateUIController.this.mResetView.setLayoutParams(params);
                    RotateUIController.this.mResetView.setPadding(RotateUIController.HOT_PRESS_SIZE, DialView.TRIANGLE_HEIGHT + this.mMarginTop, RotateUIController.HOT_PRESS_SIZE, 0);
                    return;
                }
                params.leftMargin = Math.round(x) - DialView.TRIANGLE_HEIGHT;
                params.topMargin = (int) y;
                params.gravity = 51;
                RotateUIController.this.mResetView.setLayoutParams(params);
                RotateUIController.this.mResetView.setPadding(DialView.TRIANGLE_HEIGHT, RotateUIController.HOT_PRESS_SIZE + this.mMarginTop, 0, RotateUIController.HOT_PRESS_SIZE);
            }

            public void onVisibilityChanged(boolean visible) {
                RotateUIController.this.mResetView.setVisibility(visible ? 0 : 8);
            }
        });
        this.mDialView.setOnRotateChangeListener(new OnRotateChangeListener() {
            private boolean mShouldReportStraighten = false;

            public void onAngleChanged(float degrees, boolean fromUser) {
                RotateUIController.this.mRotateListener.onAngleChanged(degrees, fromUser);
                this.mShouldReportStraighten = true;
            }

            public void onStartTrackingTouch() {
                RotateUIController.this.mRotateListener.onStartTrackingTouch();
            }

            public void onStopTrackingTouch() {
                RotateUIController.this.mRotateListener.onStopTrackingTouch();
                if (this.mShouldReportStraighten) {
                    RotateUIController.this.reportDataForRotateState(RotateAction.STRAIGHTEN);
                    this.mShouldReportStraighten = false;
                }
            }
        });
        this.mResetView = (TextView) this.mContainer.findViewById(R.id.reset);
        int color = ColorfulUtils.mappingColorfulColor(this.mContext, 0);
        if (color != 0) {
            this.mResetView.setTextColor(color);
        }
        this.mResetView.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                RotateUIController.this.mDialView.setRotatedAngle(0.0f);
                RotateUIController.this.reportDataForRotateState(RotateAction.RESET);
                RotateUIController.this.mRotateListener.onResetClicked();
            }
        });
    }

    protected void inflateFootLayout() {
        int i = 0;
        super.inflateFootLayout();
        LinearLayout linearlayout = (LinearLayout) this.mContainer.findViewById(R.id.state_root);
        for (int i2 = 0; i2 < sIconDataContainer.size(); i2++) {
            IconData iconData = (IconData) sIconDataContainer.get(sIconDataContainer.keyAt(i2));
            EditorTextView editorTextView = new EditorTextView(linearlayout.getContext());
            editorTextView.setId(EditorUtils.getViewId(i2));
            editorTextView.setAttributes(iconData, i2, sIconDataContainer.size(), true, false);
            editorTextView.setOnClickListener(this);
            linearlayout.addView(editorTextView);
        }
        int[] iArr = this.sRotateTextViewId;
        int length = iArr.length;
        while (i < length) {
            this.mContainer.findViewById(iArr[i]).setOnClickListener(this.mClickListener);
            i++;
        }
        this.mContainer.findViewById(R.id.bar_background).setVisibility(4);
    }

    protected int getFootLayout() {
        return this.mEditorViewDelegate.isPort() ? R.layout.editor_crop_foot_bar : R.layout.editor_crop_foot_bar_land;
    }

    protected void startHideAnime() {
        super.startHideAnime();
        viewGone(this.mDialView);
        viewGone(this.mResetView);
        if (this.mDialView != null) {
            this.mDialView.hide();
        }
    }

    private void viewGone(View view) {
        if (view != null) {
            view.clearAnimation();
            view.setVisibility(8);
        }
    }

    public void setText(int stringId) {
        if (this.mResetView != null) {
            this.mResetView.setText(stringId);
        }
    }

    public void setRotatedAngle(float value) {
        if (this.mDialView != null) {
            this.mDialView.setRotatedAngle(value);
        }
    }

    public void updateDisplayBounds(RectF currentRect, float currentRotatedAngle, Animation fadeIn) {
        int i = 0;
        if (this.mDialView != null) {
            this.mDialView.setPhotoDisplayBounds(new RectF(currentRect));
            this.mDialView.setVisibility(0);
            this.mDialView.startAnimation(fadeIn);
            if (this.mResetView != null) {
                TextView textView = this.mResetView;
                if (Math.abs(currentRotatedAngle) <= 0.0f) {
                    i = 8;
                }
                textView.setVisibility(i);
            }
        }
    }

    private void reportDataForRotateState(RotateAction action) {
        ReportToBigData.report(114, String.format("{RotateAction:%s}", new Object[]{action.toString()}));
    }
}
