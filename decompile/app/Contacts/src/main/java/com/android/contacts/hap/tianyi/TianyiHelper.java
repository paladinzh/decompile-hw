package com.android.contacts.hap.tianyi;

import android.app.Activity;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import com.google.android.gms.R;
import java.util.ArrayList;

public class TianyiHelper extends Activity {
    private int currentIndex;
    private ArrayList<View> list;
    private int mLength;
    private ImageView[] points;
    private ViewPager viewPager;

    class MyAdapter extends PagerAdapter {
        MyAdapter() {
        }

        public int getCount() {
            return TianyiHelper.this.list.size();
        }

        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        public int getItemPosition(Object object) {
            return super.getItemPosition(object);
        }

        public void destroyItem(View viewPager, int index, Object object) {
            if (viewPager instanceof ViewPager) {
                ((ViewPager) viewPager).removeView((View) TianyiHelper.this.list.get(index));
            }
        }

        public Object instantiateItem(View viewPager, int index) {
            if (viewPager instanceof ViewPager) {
                ((ViewPager) viewPager).addView((View) TianyiHelper.this.list.get(index));
            }
            return TianyiHelper.this.list.get(index);
        }

        public void restoreState(Parcelable arg0, ClassLoader arg1) {
        }

        public Parcelable saveState() {
            return null;
        }

        public void startUpdate(View arg0) {
        }

        public void finishUpdate(View arg0) {
        }
    }

    class MyListener implements OnPageChangeListener {
        MyListener() {
        }

        public void onPageScrollStateChanged(int arg0) {
        }

        public void onPageScrolled(int arg0, float arg1, int arg2) {
        }

        public void onPageSelected(int index) {
            TianyiHelper.this.setCurDot(index);
        }
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(128);
        if (!getResources().getBoolean(R.bool.config_tianyi_dialer)) {
            finish();
        }
        LayoutInflater inflater = getLayoutInflater();
        this.list = new ArrayList();
        this.list.add(inflater.inflate(R.layout.tianyi_item1, null));
        this.list.add(inflater.inflate(R.layout.tianyi_item2, null));
        this.list.add(inflater.inflate(R.layout.tianyi_item3, null));
        this.list.add(inflater.inflate(R.layout.tianyi_item4, null));
        this.list.add(inflater.inflate(R.layout.tianyi_item5, null));
        this.mLength = this.list.size();
        ViewGroup main = (ViewGroup) inflater.inflate(R.layout.tianyi_helper, null);
        this.viewPager = (ViewPager) main.findViewById(R.id.viewPager);
        setContentView(main);
        initPoint();
        this.viewPager.setAdapter(new MyAdapter());
        this.viewPager.setOnPageChangeListener(new MyListener());
        if (getActionBar() != null) {
            getActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == 16908332) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    private void initPoint() {
        LinearLayout linearLayout = (LinearLayout) findViewById(R.id.ll);
        this.points = new ImageView[this.mLength];
        for (int i = 0; i < this.mLength; i++) {
            this.points[i] = (ImageView) linearLayout.getChildAt(i);
            this.points[i].setEnabled(false);
            this.points[i].setTag(Integer.valueOf(i));
        }
        this.currentIndex = 0;
        this.points[this.currentIndex].setEnabled(true);
    }

    private void setCurDot(int positon) {
        if (positon >= 0 && positon <= this.mLength - 1 && this.currentIndex != positon) {
            this.points[positon].setEnabled(true);
            this.points[this.currentIndex].setEnabled(false);
            this.currentIndex = positon;
        }
    }
}
