package com.android.mms.ui;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import java.util.ArrayList;

public class MyPaperAdapter extends PagerAdapter {
    private ArrayList<GridView> mAl;

    public MyPaperAdapter(Context context, ArrayList<GridView> al) {
        this.mAl = al;
    }

    public int getCount() {
        return this.mAl.size();
    }

    public Object instantiateItem(ViewGroup container, int position) {
        ((ViewPager) container).addView((View) this.mAl.get(position));
        return this.mAl.get(position);
    }

    public void destroyItem(View container, int position, Object object) {
        ((ViewPager) container).removeView((View) object);
    }

    public boolean isViewFromObject(View view, Object object) {
        return view == ((View) object);
    }
}
