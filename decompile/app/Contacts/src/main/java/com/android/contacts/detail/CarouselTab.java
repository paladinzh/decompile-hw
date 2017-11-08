package com.android.contacts.detail;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;
import com.android.contacts.widget.FrameLayoutWithOverlay;
import com.google.android.gms.R;

public class CarouselTab extends FrameLayoutWithOverlay {
    private View mAlphaLayer;
    private View mLabelBackgroundView;
    private TextView mLabelView;

    public CarouselTab(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
        this.mLabelView = (TextView) findViewById(R.id.label);
        this.mLabelBackgroundView = findViewById(R.id.label_background);
        this.mAlphaLayer = findViewById(R.id.alpha_overlay);
        setAlphaLayer(this.mAlphaLayer);
    }
}
