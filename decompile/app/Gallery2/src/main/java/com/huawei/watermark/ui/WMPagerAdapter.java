package com.huawei.watermark.ui;

import android.content.Context;
import android.os.Parcelable;
import android.view.View;
import android.view.ViewGroup;
import com.huawei.watermark.manager.WMManager;
import com.huawei.watermark.ui.baseview.viewpager.WMBasePagerAdapter;
import com.huawei.watermark.wmdata.WMFileProcessor;

public class WMPagerAdapter extends WMBasePagerAdapter {
    private boolean mBeReverse = false;
    private Context mContext;
    private boolean mCountIsZero;
    private WMPager mWMPager;
    private WMManager mWMmanager;

    public WMPagerAdapter(Context context, WMManager mWMmanager, WMPager mWMPager, boolean countIsZero) {
        this.mContext = context;
        this.mWMmanager = mWMmanager;
        this.mWMPager = mWMPager;
        this.mBeReverse = false;
        this.mCountIsZero = countIsZero;
    }

    public int getCount() {
        if (this.mCountIsZero) {
            return 0;
        }
        return WMFileProcessor.getInstance().getNowTypeWMCount(this.mContext, this.mWMmanager.getToken());
    }

    public void setCountIsZero(boolean countIsZero) {
        this.mCountIsZero = countIsZero;
    }

    public boolean getCountIsZero() {
        return this.mCountIsZero;
    }

    public boolean isViewFromObject(View arg0, Object arg1) {
        return arg0 == arg1;
    }

    public int getItemPosition(Object object) {
        return -2;
    }

    public void destroyItem(ViewGroup container, int position, Object arg2) {
        container.removeView((View) arg2);
        position = getPosition(position);
        if (position >= 0 && position != this.mWMPager.getCurrentItemIfNeedReverse()) {
            this.mWMmanager.destroyWaterMark(position);
        }
    }

    public Object instantiateItem(ViewGroup container, int position) {
        View view = this.mWMmanager.getWaterMark(getPosition(position));
        container.addView(view, 0);
        return view;
    }

    public Parcelable saveState() {
        return null;
    }

    public void setBeReverse(boolean beReverse) {
        this.mBeReverse = beReverse;
    }

    public int getPosition(int position) {
        if (this.mBeReverse) {
            return (getCount() - 1) - position;
        }
        return position;
    }
}
