package com.huawei.systemmanager.power.ui;

import android.app.ActionBar;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.SimpleOnPageChangeListener;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.ImageView;
import android.widget.TextView;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.comm.widget.CommonPageAdapter;
import com.huawei.systemmanager.emui.activities.HsmActivity;
import com.huawei.systemmanager.util.HwLog;
import java.util.ArrayList;

public class SuperPowerSavingModeAboutActivity extends HsmActivity {
    private static final int PAGE_LENGTH = 2;
    private static OnTouchListener mViewPagerTouchListener = new OnTouchListener() {
        public boolean onTouch(View v, MotionEvent event) {
            return true;
        }
    };
    private Context mAppContext = null;
    private ImageView[] mIconViews = new ImageView[2];
    private ViewPager mViewPager;
    private ArrayList<View> mViews;

    private static class AboutPageAdapter extends CommonPageAdapter {
        public AboutPageAdapter(Context ctx, ArrayList<View> views) {
            super(ctx, views);
        }

        public void destroyItem(View arg0, int arg1, Object arg2) {
            ((ViewPager) arg0).removeView((View) this.mViews.get(arg1));
        }
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.super_power_saving_mode_about);
        ActionBar actionBar = getActionBar();
        actionBar.setTitle(R.string.super_power_saving_about_title);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.show();
        this.mAppContext = getApplicationContext();
        this.mViewPager = (ViewPager) findViewById(R.id.about_viewpager);
        this.mIconViews[0] = (ImageView) findViewById(R.id.icon_first);
        this.mIconViews[1] = (ImageView) findViewById(R.id.icon_second);
        this.mViews = new ArrayList(2);
        initPages();
        this.mViewPager.removeAllViews();
        this.mViewPager.setAdapter(new AboutPageAdapter(this, this.mViews));
        this.mViewPager.setOnPageChangeListener(new SimpleOnPageChangeListener() {
            public void onPageSelected(int position) {
                for (int i = 0; i < 2; i++) {
                    if (i == position) {
                        SuperPowerSavingModeAboutActivity.this.mIconViews[i].setImageResource(R.drawable.navi_dot_selected);
                    } else {
                        SuperPowerSavingModeAboutActivity.this.mIconViews[i].setImageResource(R.drawable.navi_dot_unselected);
                    }
                }
            }
        });
    }

    private void initPages() {
        LayoutInflater inflater = (LayoutInflater) getSystemService("layout_inflater");
        String[] descriptionArray = getResources().getStringArray(R.array.Other_SystemManager_Tip002_1);
        String[] descriptionArray_new = new String[]{this.mAppContext.getResources().getString(R.string.super_power_saving_mode_guide_content), descriptionArray[3]};
        int[] imgResourceArray = new int[]{R.drawable.pic_home, R.drawable.pic_home};
        for (int idx = 0; idx < 2; idx++) {
            HwLog.e("", "idx = " + idx);
            View pageContent = inflater.inflate(R.layout.super_power_saving_mode_about_item, null);
            TextView tvDescription = (TextView) pageContent.findViewById(R.id.page_description);
            ImageView aboutImg = (ImageView) pageContent.findViewById(R.id.page_image);
            ImageView handImageLast = (ImageView) pageContent.findViewById(R.id.page_description_hand_last);
            pageContent.setOnTouchListener(mViewPagerTouchListener);
            this.mViews.add(pageContent);
            if (1 == idx) {
                handImageLast.setVisibility(0);
            }
            tvDescription.setText(descriptionArray_new[idx]);
            aboutImg.setImageResource(imgResourceArray[idx]);
        }
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 16908332:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
