package com.android.settingslib.drawer;

import android.content.Context;
import android.content.res.Resources;
import android.support.v4.widget.DrawerLayout;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewGroup.MarginLayoutParams;
import com.android.settingslib.R$dimen;
import com.android.settingslib.R$id;

public class DrawerLayoutEx extends DrawerLayout {
    private static boolean sShouldPreventMeasure;

    public DrawerLayoutEx(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public DrawerLayoutEx(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public DrawerLayoutEx(Context context) {
        super(context);
    }

    private boolean needToPreventMeasure(View child) {
        if (child.getVisibility() == 0 || !sShouldPreventMeasure) {
            return false;
        }
        return true;
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        measureDrawer(widthMeasureSpec, heightMeasureSpec);
    }

    private void measureDrawer(int widthMeasureSpec, int heightMeasureSpec) {
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int chidCount = getChildCount();
        for (int i = 0; i < chidCount; i++) {
            View child = getChildAt(i);
            if (child.getId() == R$id.left_drawer) {
                if (!needToPreventMeasure(child)) {
                    int width;
                    Resources res = getResources();
                    int marginTop = (int) res.getDimension(R$dimen.left_drawer_margin_top);
                    if (res.getConfiguration().orientation == 2) {
                        marginTop = (int) res.getDimension(R$dimen.left_drawer_margin_top_landscape);
                        width = (int) (((float) widthSize) * 0.5f);
                    } else {
                        width = (int) (((float) widthSize) * 0.75f);
                    }
                    MarginLayoutParams lp = (MarginLayoutParams) child.getLayoutParams();
                    lp.topMargin = marginTop;
                    lp.bottomMargin = marginTop;
                    child.setLayoutParams(lp);
                    child.measure(MeasureSpec.makeMeasureSpec(width, 1073741824), lp.topMargin + getChildMeasureSpec(heightMeasureSpec, lp.topMargin + lp.bottomMargin, lp.height));
                } else {
                    return;
                }
            }
        }
    }
}
