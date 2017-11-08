package cn.com.xy.sms.sdk.ui.popu.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View.MeasureSpec;
import android.widget.ListView;

public class DuoquListViewForScrollView extends ListView {
    public DuoquListViewForScrollView(Context context) {
        super(context);
    }

    public DuoquListViewForScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public DuoquListViewForScrollView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(536870911, Integer.MIN_VALUE));
    }
}
