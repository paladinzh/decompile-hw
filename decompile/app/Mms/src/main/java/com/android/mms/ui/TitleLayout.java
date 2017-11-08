package com.android.mms.ui;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.View.MeasureSpec;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import cn.com.xy.sms.sdk.ui.popu.util.ContentUtil;
import com.google.android.gms.R;

public class TitleLayout extends LinearLayout {
    private ImageView mAppsIcon;
    private int mAppsIconMaginWidth;
    private int mAppsIconWidth;
    private boolean mIsAppsIconShow;
    private boolean mIsNumberNull;
    private int mLayoutWidth;
    private int mNumberWidth;
    private TextView mTitle;
    private TextView mTitleNumber;
    private int mTitledWidth;

    public TitleLayout(Context context) {
        super(context);
    }

    public TitleLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
        this.mTitle = (TextView) findViewById(R.id.title);
        this.mTitleNumber = (TextView) findViewById(R.id.title_number);
        this.mAppsIcon = (ImageView) findViewById(R.id.action_bar_apps);
        this.mAppsIconWidth = (int) getResources().getDimension(R.dimen.actionbar_apps_logo_size);
        this.mAppsIconMaginWidth = (int) getResources().getDimension(R.dimen.actionbar_apps_logo_margin);
        this.mTitle.addTextChangedListener(new TextWatcher() {
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void afterTextChanged(Editable s) {
                TitleLayout.this.resizeLayout();
            }
        });
    }

    public void resizeLayout() {
        LayoutParams fromLayoutParams = (LayoutParams) this.mTitle.getLayoutParams();
        int marginStart = ((LayoutParams) this.mTitleNumber.getLayoutParams()).getMarginStart();
        if (this.mLayoutWidth == 0 || this.mLayoutWidth > (this.mTitledWidth + marginStart) + this.mNumberWidth || fromLayoutParams.weight != 0.0f) {
            fromLayoutParams.weight = 0.0f;
            fromLayoutParams.width = -2;
            this.mTitle.setLayoutParams(fromLayoutParams);
            return;
        }
        fromLayoutParams.weight = ContentUtil.FONT_SIZE_NORMAL;
        fromLayoutParams.width = 0;
        this.mTitle.setLayoutParams(fromLayoutParams);
    }

    public void resizeLayoutWithAppsIcon() {
        if (this.mTitle != null) {
            LayoutParams fromLayoutParams = (LayoutParams) this.mTitle.getLayoutParams();
            int appIconTotalWidth = (this.mAppsIconWidth + this.mAppsIconMaginWidth) + this.mAppsIconMaginWidth;
            if (this.mLayoutWidth == 0 || this.mLayoutWidth >= this.mTitledWidth + appIconTotalWidth) {
                fromLayoutParams.width = -2;
                this.mTitle.setLayoutParams(fromLayoutParams);
            } else {
                fromLayoutParams.width = this.mLayoutWidth - appIconTotalWidth;
                this.mTitle.setLayoutParams(fromLayoutParams);
            }
        }
    }

    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        if (this.mIsNumberNull || this.mNumberWidth < 99) {
        }
        if (this.mIsAppsIconShow) {
            resizeLayoutWithAppsIcon();
        }
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        boolean z = false;
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        this.mTitledWidth = this.mTitle.getMeasuredWidth();
        this.mNumberWidth = this.mTitleNumber.getMeasuredWidth();
        this.mLayoutWidth = MeasureSpec.getSize(widthMeasureSpec);
        this.mIsNumberNull = this.mTitleNumber.getText().equals("");
        if (this.mAppsIcon != null) {
            if (this.mAppsIcon.getVisibility() == 0) {
                z = true;
            }
            this.mIsAppsIconShow = z;
        }
    }
}
