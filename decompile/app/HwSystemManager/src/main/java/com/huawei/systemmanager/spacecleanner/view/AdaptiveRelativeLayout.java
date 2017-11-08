package com.huawei.systemmanager.spacecleanner.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View.MeasureSpec;
import android.widget.RelativeLayout;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.comm.Storage.StorageHelper;
import com.huawei.systemmanager.comm.misc.GlobalContext;
import com.huawei.systemmanager.power.util.SysCoreUtils;
import com.huawei.systemmanager.util.HwLog;
import java.util.HashMap;
import java.util.Map;

public class AdaptiveRelativeLayout extends RelativeLayout {
    private static final String TAG = "AdaptiveRelativeLayout";
    private int mAdjustHeigh;
    private String mHeightString;
    private Map<String, Integer> mHeigtValueMap;

    public AdaptiveRelativeLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AdaptiveRelativeLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mHeigtValueMap = new HashMap();
        initHeightValueMap();
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.RelativeLayoutHeight);
        if (ta != null) {
            this.mHeightString = ta.getString(0);
            ta.recycle();
        }
        initHeight();
    }

    private void initHeightValueMap() {
        int actionbarHeight = getActionBarHeight();
        this.mHeigtValueMap.put("16:9", Integer.valueOf(((int) ((((double) SysCoreUtils.getScreenWidth(GlobalContext.getContext())) * 9.0d) / 16.0d)) - actionbarHeight));
        this.mHeigtValueMap.put("4:3", Integer.valueOf(((int) ((((double) SysCoreUtils.getScreenWidth(GlobalContext.getContext())) * 3.0d) / 4.0d)) - actionbarHeight));
        this.mHeigtValueMap.put("3:2", Integer.valueOf(((int) ((((double) SysCoreUtils.getScreenWidth(GlobalContext.getContext())) * 2.0d) / 3.0d)) - actionbarHeight));
        this.mHeigtValueMap.put("21:9", Integer.valueOf(((int) ((((double) SysCoreUtils.getScreenWidth(GlobalContext.getContext())) * 9.0d) / 21.0d)) - actionbarHeight));
    }

    private void initHeight() {
        if (TextUtils.isEmpty(this.mHeightString)) {
            this.mHeightString = "16:9";
        }
        if (this.mHeightString.equalsIgnoreCase("a:a")) {
            if (StorageHelper.getStorage().isSdcardaviliable()) {
                this.mHeightString = "3:2";
            } else {
                this.mHeightString = "16:9";
            }
        }
        this.mAdjustHeigh = ((Integer) this.mHeigtValueMap.get(this.mHeightString)).intValue();
    }

    private int getActionBarHeight() {
        Context context = GlobalContext.getContext();
        if (context == null) {
            HwLog.i(TAG, "Activity not found");
            return 0;
        }
        TypedArray actionBarSizeTypedArray = context.obtainStyledAttributes(new int[]{16843499});
        if (actionBarSizeTypedArray == null) {
            return 0;
        }
        int height = actionBarSizeTypedArray.getDimensionPixelSize(0, 0);
        actionBarSizeTypedArray.recycle();
        return height;
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(this.mAdjustHeigh, MeasureSpec.getMode(heightMeasureSpec)) + getContext().getResources().getDimensionPixelSize(R.dimen.adjustMeasureSpec_increase_value));
    }
}
