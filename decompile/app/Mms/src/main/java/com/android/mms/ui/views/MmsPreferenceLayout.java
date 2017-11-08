package com.android.mms.ui.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import com.google.android.gms.R;

public class MmsPreferenceLayout extends LinearLayout {
    private View mDetailFrame;
    private View mTitleFrame;
    private View mWidgetFrame;

    public MmsPreferenceLayout(Context context) {
        this(context, null);
    }

    public MmsPreferenceLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MmsPreferenceLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void addView(View child, int index, LayoutParams params) {
        super.addView(child, index, params);
        switch (child.getId()) {
            case 16908312:
                this.mWidgetFrame = findViewById(16908312);
                return;
            case R.id.title_frame:
                this.mTitleFrame = findViewById(R.id.title_frame);
                return;
            case R.id.detail_frame:
                this.mDetailFrame = findViewById(R.id.detail_frame);
                return;
            default:
                return;
        }
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        this.mTitleFrame.measure(0, heightMeasureSpec);
        this.mDetailFrame.measure(0, heightMeasureSpec);
        int titleWidth_origin = this.mTitleFrame.getMeasuredWidth();
        int detailWidth_origin = this.mDetailFrame.getMeasuredWidth();
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        LinearLayout.LayoutParams title_lp = (LinearLayout.LayoutParams) this.mTitleFrame.getLayoutParams();
        int titleHeightMeasureSpec = getChildMeasureSpec(heightMeasureSpec, ((getPaddingTop() + getPaddingBottom()) + title_lp.topMargin) + title_lp.bottomMargin, title_lp.height);
        LinearLayout.LayoutParams detail_lp = (LinearLayout.LayoutParams) this.mDetailFrame.getLayoutParams();
        int detailHeightMeasureSpec = getChildMeasureSpec(heightMeasureSpec, ((getPaddingTop() + getPaddingBottom()) + detail_lp.topMargin) + detail_lp.bottomMargin, detail_lp.height);
        int availableSpace = ((widthSize - getPaddingStart()) - getPaddingEnd()) - this.mWidgetFrame.getMeasuredWidth();
        int maxTitleWidthWithDetail = (availableSpace * 2) / 3;
        if (this.mTitleFrame.getVisibility() == 0 && this.mDetailFrame.getVisibility() == 0 && titleWidth_origin + detailWidth_origin > availableSpace) {
            if (titleWidth_origin > maxTitleWidthWithDetail) {
                this.mTitleFrame.measure(MeasureSpec.makeMeasureSpec(maxTitleWidthWithDetail, 1073741824), titleHeightMeasureSpec);
                this.mDetailFrame.measure(MeasureSpec.makeMeasureSpec(availableSpace - maxTitleWidthWithDetail, 1073741824), detailHeightMeasureSpec);
            } else if (titleWidth_origin > availableSpace - maxTitleWidthWithDetail) {
                this.mTitleFrame.measure(MeasureSpec.makeMeasureSpec(titleWidth_origin, 1073741824), titleHeightMeasureSpec);
                this.mDetailFrame.measure(MeasureSpec.makeMeasureSpec(availableSpace - titleWidth_origin, 1073741824), detailHeightMeasureSpec);
            } else {
                this.mTitleFrame.measure(MeasureSpec.makeMeasureSpec(availableSpace - maxTitleWidthWithDetail, 1073741824), titleHeightMeasureSpec);
                this.mDetailFrame.measure(MeasureSpec.makeMeasureSpec(maxTitleWidthWithDetail, 1073741824), detailHeightMeasureSpec);
            }
        } else if (this.mTitleFrame.getVisibility() != 0 || this.mDetailFrame.getVisibility() != 0) {
        } else {
            if (titleWidth_origin + detailWidth_origin < availableSpace || titleWidth_origin + detailWidth_origin == availableSpace) {
                this.mTitleFrame.measure(MeasureSpec.makeMeasureSpec(availableSpace - detailWidth_origin, 1073741824), titleHeightMeasureSpec);
                this.mDetailFrame.measure(MeasureSpec.makeMeasureSpec(detailWidth_origin, 1073741824), detailHeightMeasureSpec);
            }
        }
    }
}
