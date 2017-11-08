package com.android.mms.ui;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import cn.com.xy.sms.sdk.ui.popu.util.ContentUtil;
import com.google.android.gms.R;
import com.huawei.mms.util.HwBackgroundLoader;

public class FromLayout extends LinearLayout {
    private TextView mFromView;
    private LinearLayout mIconLayout;
    private String mOldText = "";
    private Runnable mResizeLayoutRunnable = new Runnable() {
        public void run() {
            FromLayout.this.resizeLayoutDelay();
        }
    };

    public FromLayout(Context context) {
        super(context);
    }

    public FromLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
        this.mFromView = (TextView) findViewById(R.id.from);
        this.mIconLayout = (LinearLayout) findViewById(R.id.icon_layout);
    }

    public void resizeLayout() {
        resizeLayout(false);
    }

    public void resizeLayout(boolean screenChanged) {
        LayoutParams fromLayoutParams = (LayoutParams) this.mFromView.getLayoutParams();
        String newText = this.mFromView.getText().toString();
        if (fromLayoutParams.weight == ContentUtil.FONT_SIZE_NORMAL && (!TextUtils.equals(newText, this.mOldText) || screenChanged)) {
            fromLayoutParams.weight = 0.0f;
            fromLayoutParams.width = -2;
            this.mFromView.setLayoutParams(fromLayoutParams);
        }
        HwBackgroundLoader.getUIHandler().removeCallbacks(this.mResizeLayoutRunnable);
        HwBackgroundLoader.getUIHandler().postDelayed(this.mResizeLayoutRunnable, 10);
    }

    private void resizeLayoutDelay() {
        LayoutParams fromLayoutParams = (LayoutParams) this.mFromView.getLayoutParams();
        int marginEnd = fromLayoutParams.getMarginEnd();
        int fromWidth = this.mFromView.getWidth();
        int iconLayoutWidth = 0;
        if (this.mIconLayout != null) {
            iconLayoutWidth = this.mIconLayout.getWidth();
        }
        int width = getWidth();
        if (width != 0 && width <= (fromWidth + marginEnd) + iconLayoutWidth && fromLayoutParams.weight == 0.0f) {
            fromLayoutParams.weight = ContentUtil.FONT_SIZE_NORMAL;
            fromLayoutParams.width = 0;
            this.mFromView.setLayoutParams(fromLayoutParams);
        }
        this.mOldText = this.mFromView.getText().toString();
    }
}
