package com.huawei.systemmanager.comm.widget;

import android.content.Context;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import java.util.ArrayList;

public class CommonPageAdapter extends PagerAdapter {
    private final Context mContext;
    protected ArrayList<View> mViews;

    public CommonPageAdapter(Context ctx, ArrayList<View> views) {
        this.mContext = ctx;
        this.mViews = views;
    }

    public int getCount() {
        return this.mViews.size();
    }

    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    public Object instantiateItem(View collection, int position) {
        if (collection instanceof ViewPager) {
            ((ViewPager) collection).addView((View) this.mViews.get(position));
        }
        return this.mViews.get(position);
    }

    public void destroyItem(View arg0, int arg1, Object arg2) {
        this.mViews.remove(this.mViews.indexOf(arg0));
    }

    public void finishUpdate(View arg0) {
    }

    public void restoreState(Parcelable arg0, ClassLoader arg1) {
    }

    public Parcelable saveState() {
        return null;
    }

    public void startUpdate(View arg0) {
    }

    public void updateView(ArrayList<View> views) {
        this.mViews = views;
        notifyDataSetChanged();
    }

    public Context getContext() {
        return this.mContext;
    }
}
