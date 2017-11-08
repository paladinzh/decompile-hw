package com.huawei.rcs.ui;

import android.app.ActionBar;
import android.app.Activity;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import com.google.android.gms.R;
import java.util.ArrayList;
import java.util.List;

public class RcsAboutMessagePagerActivity extends Activity implements OnPageChangeListener {
    private static final int[] content = new int[]{R.string.rcs_about_message_guide_one, R.string.rcs_about_message_guide_two, R.string.rcs_about_message_guide_three};
    private static final int[] pics = new int[]{R.drawable.about_message_01, R.drawable.about_message_02, R.drawable.about_message_03};
    private int currentIndex;
    private ImageView[] dots;
    private MyPagerAdapter mMyPagerAdapter;
    private TextView mTextView;
    private List<View> views;
    private ViewPager vp;

    static class MyPagerAdapter extends PagerAdapter {
        private List<View> views;

        public MyPagerAdapter(List<View> listView) {
            this.views = listView;
        }

        public void destroyItem(View arg0, int arg1, Object arg2) {
            ((ViewPager) arg0).removeView((View) this.views.get(arg1));
        }

        public void finishUpdate(View arg0) {
        }

        public int getCount() {
            if (this.views != null) {
                return this.views.size();
            }
            return 0;
        }

        public Object instantiateItem(View arg0, int arg1) {
            ((ViewPager) arg0).addView((View) this.views.get(arg1), 0);
            return this.views.get(arg1);
        }

        public boolean isViewFromObject(View arg0, Object arg1) {
            return arg0 == arg1;
        }

        public Parcelable saveState() {
            return null;
        }

        public void restoreState(Parcelable arg0, ClassLoader arg1) {
        }

        public void startUpdate(View arg0) {
        }
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about_message_layout);
        this.mTextView = (TextView) findViewById(R.id.text_about_message);
        ActionBar acationBar = getActionBar();
        if (acationBar != null) {
            acationBar.setTitle(R.string.rcs_about_message);
            acationBar.setDisplayHomeAsUpEnabled(true);
        }
        this.views = new ArrayList();
        LayoutParams mParams = new LayoutParams(-2, -2);
        for (int imageResource : pics) {
            ImageView iv = new ImageView(this);
            iv.setLayoutParams(mParams);
            iv.setImageResource(imageResource);
            this.views.add(iv);
        }
        this.vp = (ViewPager) findViewById(R.id.about_message_pager);
        this.mMyPagerAdapter = new MyPagerAdapter(this.views);
        this.vp.setAdapter(this.mMyPagerAdapter);
        this.vp.setOnPageChangeListener(this);
        initDots();
    }

    private void initDots() {
        LinearLayout ll = (LinearLayout) findViewById(R.id.linear);
        this.dots = new ImageView[pics.length];
        for (int i = 0; i < pics.length; i++) {
            this.dots[i] = (ImageView) ll.getChildAt(i);
            this.dots[i].setEnabled(false);
            this.dots[i].setTag(Integer.valueOf(i));
        }
        this.currentIndex = 0;
        this.dots[this.currentIndex].setEnabled(true);
    }

    private void setCurDot(int positon) {
        if (positon >= 0 && positon <= pics.length - 1 && this.currentIndex != positon) {
            this.currentIndex = positon;
            for (int i = 0; i < this.dots.length; i++) {
                if (i == this.currentIndex) {
                    this.dots[this.currentIndex].setImageDrawable(getResources().getDrawable(R.drawable.message_plus_about_bluedot));
                } else {
                    this.dots[i].setImageDrawable(getResources().getDrawable(R.drawable.message_plus_about_greydot));
                }
            }
        }
    }

    public void onPageScrollStateChanged(int arg0) {
    }

    public void onPageScrolled(int arg0, float arg1, int arg2) {
    }

    public void onPageSelected(int arg0) {
        setCurDot(arg0);
        this.mTextView.setText(content[arg0]);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 16908332:
                goUpToTopLevelSetting(this);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public static void goUpToTopLevelSetting(Activity activity) {
        activity.finish();
    }
}
