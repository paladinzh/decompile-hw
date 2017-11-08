package com.android.deskclock.smartcover;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import com.android.deskclock.R;
import java.util.ArrayList;
import java.util.List;

@SuppressLint({"NewApi"})
public class SmartCoverDigitalClockView extends RelativeLayout {
    private DigitalClockAdapter mDigitalClockAdapter;
    private List<ImageView> mDigitalTimeImageViewList = null;

    public SmartCoverDigitalClockView(Context context) {
        super(context);
    }

    public SmartCoverDigitalClockView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mDigitalClockAdapter = new DigitalClockAdapter(context);
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
        initView();
    }

    private List<ImageView> initTimeImageViewList() {
        List<ImageView> imageViewList = new ArrayList();
        imageViewList.add((ImageView) findViewById(R.id.imageViewNum1));
        imageViewList.add((ImageView) findViewById(R.id.imageViewNum2));
        imageViewList.add((ImageView) findViewById(R.id.imageViewNum3));
        imageViewList.add((ImageView) findViewById(R.id.imageViewNum4));
        return imageViewList;
    }

    private void initView() {
        if (this.mDigitalClockAdapter != null) {
            this.mDigitalTimeImageViewList = initTimeImageViewList();
            this.mDigitalClockAdapter.setDigitalTimeViewList(this.mDigitalTimeImageViewList);
        }
    }

    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (this.mDigitalClockAdapter != null) {
            this.mDigitalClockAdapter.registerContentObserver();
        }
    }

    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (this.mDigitalClockAdapter != null) {
            this.mDigitalClockAdapter.unregisterContentObserver();
        }
    }
}
