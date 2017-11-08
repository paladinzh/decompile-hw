package com.huawei.gallery.photoshare.ui;

import android.content.Context;
import android.content.res.Configuration;
import android.util.AttributeSet;
import android.view.View.MeasureSpec;
import android.widget.GridView;
import com.fyusion.sdk.viewer.internal.request.target.Target;

public class PhotoShareEditFriendsGridview extends GridView {
    public PhotoShareEditFriendsGridview(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PhotoShareEditFriendsGridview(Context context) {
        super(context);
    }

    public PhotoShareEditFriendsGridview(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void updateGridViewColums() {
        int ori = getResources().getConfiguration().orientation;
        if (ori == 2) {
            setNumColumns(7);
        } else if (ori == 1) {
            setNumColumns(5);
        }
    }

    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        updateGridViewColums();
    }

    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(536870911, Target.SIZE_ORIGINAL));
    }
}
