package com.huawei.systemmanager.power.util;

import android.content.Context;
import android.content.res.Resources;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout.LayoutParams;
import com.huawei.systemmanager.emui.EmuiBackgroundRelativeLayout;

public class PowerEmuiRelativeLayout extends EmuiBackgroundRelativeLayout {
    private View mBottomView;
    private Context mContext;

    public PowerEmuiRelativeLayout(Context context) {
        super(context);
        this.mContext = context;
        addBottomLine();
    }

    public PowerEmuiRelativeLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
        addBottomLine();
    }

    public PowerEmuiRelativeLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.mContext = context;
        addBottomLine();
    }

    public void setBottomLineVisibility(int visible) {
        if (this.mBottomView != null) {
            this.mBottomView.setVisibility(visible);
        }
    }

    private void addBottomLine() {
        LayoutParams wsLp = new LayoutParams(-1, 2);
        wsLp.addRule(12);
        this.mBottomView = new View(this.mContext);
        this.mBottomView.setBackgroundResource(Resources.getSystem().getIdentifier("divider_horizontal_gray_emui", "drawable", "androidhwext"));
        this.mBottomView.setLayoutParams(wsLp);
        addView(this.mBottomView);
    }
}
