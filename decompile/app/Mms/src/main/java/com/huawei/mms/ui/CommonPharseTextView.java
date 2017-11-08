package com.huawei.mms.ui;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.widget.TextView;
import com.google.android.gms.R;

public class CommonPharseTextView extends TextView {
    private int mDoubleLinePaddingTop;
    private int mMultiLinePaddingTop;
    private int mPaddingEnd;
    private int mSingleLinePaddingTop;

    public CommonPharseTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initPaddingValues();
    }

    public CommonPharseTextView(Context context) {
        super(context);
        initPaddingValues();
    }

    public CommonPharseTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initPaddingValues();
    }

    private void initPaddingValues() {
        Resources resources = getContext().getResources();
        this.mSingleLinePaddingTop = resources.getDimensionPixelSize(R.dimen.single_line_paddingtop);
        this.mDoubleLinePaddingTop = resources.getDimensionPixelSize(R.dimen.double_line_paddingtop);
        this.mMultiLinePaddingTop = resources.getDimensionPixelSize(R.dimen.at_least_three_line_paddingtop);
        this.mPaddingEnd = resources.getDimensionPixelSize(R.dimen.common_phrase_list_item_paddingEnd);
    }

    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int lineNumber = getLineCount();
        if (lineNumber > 0) {
            int currentPaddingTop = getPaddingTop();
            int currentPaddingBottom = getPaddingBottom();
            switch (lineNumber) {
                case 1:
                    if (!(currentPaddingTop == this.mSingleLinePaddingTop && currentPaddingBottom == this.mSingleLinePaddingTop)) {
                        setPaddingRelative(0, this.mSingleLinePaddingTop, this.mPaddingEnd, this.mSingleLinePaddingTop);
                        break;
                    }
                case 2:
                    if (!(currentPaddingTop == this.mDoubleLinePaddingTop && currentPaddingBottom == this.mDoubleLinePaddingTop)) {
                        setPaddingRelative(0, this.mDoubleLinePaddingTop, this.mPaddingEnd, this.mDoubleLinePaddingTop);
                        break;
                    }
                default:
                    if (!(currentPaddingTop == this.mMultiLinePaddingTop && currentPaddingBottom == this.mMultiLinePaddingTop)) {
                        setPaddingRelative(0, this.mMultiLinePaddingTop, this.mPaddingEnd, this.mMultiLinePaddingTop);
                        break;
                    }
            }
        }
    }
}
