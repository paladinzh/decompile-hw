package com.android.mms.ui.views;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import com.google.android.gms.R;

public class FunctionTipsButtonSuperLayout extends RelativeLayout {
    Button mCancelTipsButton;
    Button mOpenFunctionButton;

    public FunctionTipsButtonSuperLayout(Context context) {
        super(context);
    }

    public FunctionTipsButtonSuperLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public FunctionTipsButtonSuperLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    protected void onFinishInflate() {
        this.mCancelTipsButton = (Button) findViewById(R.id.cancel_tips);
        this.mOpenFunctionButton = (Button) findViewById(R.id.open_function);
    }

    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
    }

    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        int cancelWidth = this.mCancelTipsButton.getMeasuredWidth();
        int openWidth = this.mOpenFunctionButton.getMeasuredWidth();
        if (cancelWidth > openWidth) {
            LayoutParams openLayoutParams = (LayoutParams) this.mOpenFunctionButton.getLayoutParams();
            openLayoutParams.width = cancelWidth;
            this.mOpenFunctionButton.setLayoutParams(openLayoutParams);
        } else if (cancelWidth < openWidth) {
            LayoutParams cancelLayoutParams = (LayoutParams) this.mCancelTipsButton.getLayoutParams();
            cancelLayoutParams.width = openWidth;
            this.mCancelTipsButton.setLayoutParams(cancelLayoutParams);
        }
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
}
