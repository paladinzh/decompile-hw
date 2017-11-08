package com.android.settings.widget;

import android.content.Context;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.accessibility.AccessibilityNodeInfoCompat;
import android.support.v4.widget.ExploreByTouchHelper;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.accessibility.AccessibilityEvent;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import java.util.List;

public class LabeledSeekBar extends SeekBar {
    private final ExploreByTouchHelper mAccessHelper;
    private String[] mLabels;
    private OnSeekBarChangeListener mOnSeekBarChangeListener;
    private final OnSeekBarChangeListener mProxySeekBarListener;

    private class LabeledSeekBarExploreByTouchHelper extends ExploreByTouchHelper {
        private boolean mIsLayoutRtl;
        final /* synthetic */ LabeledSeekBar this$0;

        public LabeledSeekBarExploreByTouchHelper(LabeledSeekBar this$0, LabeledSeekBar forView) {
            boolean z = true;
            this.this$0 = this$0;
            super(forView);
            if (forView.getResources().getConfiguration().getLayoutDirection() != 1) {
                z = false;
            }
            this.mIsLayoutRtl = z;
        }

        protected int getVirtualViewAt(float x, float y) {
            return getVirtualViewIdIndexFromX(x);
        }

        protected void getVisibleVirtualViews(List<Integer> list) {
            int c = this.this$0.getMax();
            for (int i = 0; i <= c; i++) {
                list.add(Integer.valueOf(i));
            }
        }

        protected boolean onPerformActionForVirtualView(int virtualViewId, int action, Bundle arguments) {
            if (virtualViewId == -1) {
                return false;
            }
            switch (action) {
                case 16:
                    this.this$0.setProgress(virtualViewId);
                    sendEventForVirtualView(virtualViewId, 1);
                    return true;
                default:
                    return false;
            }
        }

        protected void onPopulateNodeForVirtualView(int virtualViewId, AccessibilityNodeInfoCompat node) {
            boolean z = true;
            node.setClassName(RadioButton.class.getName());
            node.setBoundsInParent(getBoundsInParentFromVirtualViewId(virtualViewId));
            node.addAction(16);
            node.setContentDescription(this.this$0.mLabels[virtualViewId]);
            node.setClickable(true);
            node.setCheckable(true);
            if (virtualViewId != this.this$0.getProgress()) {
                z = false;
            }
            node.setChecked(z);
        }

        protected void onPopulateEventForVirtualView(int virtualViewId, AccessibilityEvent event) {
            event.setClassName(RadioButton.class.getName());
            event.setContentDescription(this.this$0.mLabels[virtualViewId]);
            event.setChecked(virtualViewId == this.this$0.getProgress());
        }

        protected void onPopulateNodeForHost(AccessibilityNodeInfoCompat node) {
            node.setClassName(RadioGroup.class.getName());
        }

        protected void onPopulateEventForHost(AccessibilityEvent event) {
            event.setClassName(RadioGroup.class.getName());
        }

        private int getHalfVirtualViewWidth() {
            return Math.max(0, ((this.this$0.getWidth() - this.this$0.getPaddingStart()) - this.this$0.getPaddingEnd()) / (this.this$0.getMax() * 2));
        }

        private int getVirtualViewIdIndexFromX(float x) {
            int posBase = (Math.max(0, (((int) x) - this.this$0.getPaddingStart()) / getHalfVirtualViewWidth()) + 1) / 2;
            return this.mIsLayoutRtl ? this.this$0.getMax() - posBase : posBase;
        }

        private Rect getBoundsInParentFromVirtualViewId(int virtualViewId) {
            int updatedVirtualViewId;
            if (this.mIsLayoutRtl) {
                updatedVirtualViewId = this.this$0.getMax() - virtualViewId;
            } else {
                updatedVirtualViewId = virtualViewId;
            }
            int left = (((updatedVirtualViewId * 2) - 1) * getHalfVirtualViewWidth()) + this.this$0.getPaddingStart();
            int right = (((updatedVirtualViewId * 2) + 1) * getHalfVirtualViewWidth()) + this.this$0.getPaddingStart();
            if (updatedVirtualViewId == 0) {
                left = 0;
            }
            if (updatedVirtualViewId == this.this$0.getMax()) {
                right = this.this$0.getWidth();
            }
            Rect r = new Rect();
            r.set(left, 0, right, this.this$0.getHeight());
            return r;
        }
    }

    public LabeledSeekBar(Context context, AttributeSet attrs) {
        this(context, attrs, 16842875);
    }

    public LabeledSeekBar(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public LabeledSeekBar(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.mProxySeekBarListener = new OnSeekBarChangeListener() {
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (LabeledSeekBar.this.mOnSeekBarChangeListener != null) {
                    LabeledSeekBar.this.mOnSeekBarChangeListener.onStopTrackingTouch(seekBar);
                }
            }

            public void onStartTrackingTouch(SeekBar seekBar) {
                if (LabeledSeekBar.this.mOnSeekBarChangeListener != null) {
                    LabeledSeekBar.this.mOnSeekBarChangeListener.onStartTrackingTouch(seekBar);
                }
            }

            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (LabeledSeekBar.this.mOnSeekBarChangeListener != null) {
                    LabeledSeekBar.this.mOnSeekBarChangeListener.onProgressChanged(seekBar, progress, fromUser);
                    LabeledSeekBar.this.sendClickEventForAccessibility(progress);
                }
            }
        };
        this.mAccessHelper = new LabeledSeekBarExploreByTouchHelper(this, this);
        ViewCompat.setAccessibilityDelegate(this, this.mAccessHelper);
        super.setOnSeekBarChangeListener(this.mProxySeekBarListener);
    }

    public synchronized void setProgress(int progress) {
        if (this.mAccessHelper != null) {
            this.mAccessHelper.invalidateRoot();
        }
        super.setProgress(progress);
    }

    public void setLabels(String[] labels) {
        this.mLabels = labels;
    }

    public void setOnSeekBarChangeListener(OnSeekBarChangeListener l) {
        this.mOnSeekBarChangeListener = l;
    }

    protected boolean dispatchHoverEvent(MotionEvent event) {
        return !this.mAccessHelper.dispatchHoverEvent(event) ? super.dispatchHoverEvent(event) : true;
    }

    private void sendClickEventForAccessibility(int progress) {
        this.mAccessHelper.invalidateRoot();
        this.mAccessHelper.sendEventForVirtualView(progress, 1);
    }
}
