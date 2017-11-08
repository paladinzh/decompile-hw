package com.huawei.systemmanager.mainscreen.entrance.entry;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.comm.misc.GlobalContext;
import com.huawei.systemmanager.comm.misc.Utility;

public class TipView {
    private final Handler mHandler = new Handler(Looper.getMainLooper());
    private int mNum;
    private TextView mText;

    public void setTipTextView(TextView tv) {
        this.mText = tv;
        showNumber();
    }

    public void postSetNumber(final int num) {
        this.mHandler.post(new Runnable() {
            public void run() {
                TipView.this.setNumber(num);
            }
        });
    }

    public void setNumber(int num) {
        this.mNum = num;
        showNumber();
    }

    public void showNumber() {
        if (this.mText != null) {
            if (this.mNum <= 0) {
                this.mText.setVisibility(8);
                return;
            }
            int marginEnd;
            if (this.mText.getVisibility() != 0) {
                this.mText.setVisibility(0);
            }
            Context ctx = GlobalContext.getContext();
            if (this.mNum < 10) {
                marginEnd = ctx.getResources().getDimensionPixelSize(R.dimen.main_screen_entry_tip_marginEnd_1) + ctx.getResources().getDimensionPixelSize(R.dimen.main_screen_entry_tip_marginAdjust);
            } else if (this.mNum < 100) {
                marginEnd = ctx.getResources().getDimensionPixelSize(R.dimen.main_screen_entry_tip_marginEnd_2) + ctx.getResources().getDimensionPixelSize(R.dimen.main_screen_entry_tip_marginAdjust);
            } else {
                marginEnd = ctx.getResources().getDimensionPixelSize(R.dimen.main_screen_entry_tip_marginEnd_3) + ctx.getResources().getDimensionPixelSize(R.dimen.main_screen_entry_tip_marginAdjust);
            }
            LayoutParams lp = (LayoutParams) this.mText.getLayoutParams();
            if (lp.getMarginEnd() != marginEnd) {
                lp.setMarginEnd(marginEnd);
                this.mText.requestLayout();
            }
            if (this.mNum < 100) {
                this.mText.setText(Utility.getLocaleNumber(this.mNum));
            } else {
                this.mText.setText(R.string.main_screen_entry_tip_hundreds);
            }
        }
    }
}
