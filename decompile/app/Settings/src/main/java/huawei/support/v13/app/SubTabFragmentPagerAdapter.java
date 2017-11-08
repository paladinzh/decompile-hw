package huawei.support.v13.app;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import huawei.android.widget.SubTabWidget;
import huawei.android.widget.SubTabWidget.SubTab;
import huawei.android.widget.SubTabWidget.SubTabListener;
import java.util.ArrayList;

public class SubTabFragmentPagerAdapter extends FragmentPagerAdapter implements SubTabListener, OnPageChangeListener {
    private final SubTabWidget mSubTabWidget;
    private final ArrayList<SubTabInfo> mSubTabs = new ArrayList();
    private final ViewPager mViewPager;

    static final class SubTabInfo {
        private final Bundle args;
        private Fragment fragment;

        SubTabInfo(Fragment _fragment, Bundle _args) {
            this.fragment = _fragment;
            this.args = _args;
        }
    }

    public SubTabFragmentPagerAdapter(Activity activity, ViewPager pager, SubTabWidget subTabWidget) {
        super(activity.getFragmentManager());
        this.mSubTabWidget = subTabWidget;
        this.mViewPager = pager;
        this.mViewPager.setAdapter(this);
        this.mViewPager.setOnPageChangeListener(this);
    }

    public void addSubTab(SubTab subTab, int position, Fragment frag, Bundle args, boolean selected) {
        if (!(frag.isAdded() || frag.isDetached())) {
            frag.setArguments(args);
        }
        SubTabInfo info = new SubTabInfo(frag, args);
        subTab.setTag(info);
        if (subTab.getCallback() == null) {
            subTab.setSubTabListener(this);
        }
        this.mSubTabs.add(position, info);
        this.mSubTabWidget.addSubTab(subTab, position, selected);
        notifyDataSetChanged();
    }

    public Fragment getItem(int position) {
        return ((SubTabInfo) this.mSubTabs.get(position)).fragment;
    }

    public int getCount() {
        return this.mSubTabs.size();
    }

    public void onPageScrollStateChanged(int state) {
    }

    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
    }

    public void onPageSelected(int position) {
        this.mSubTabWidget.setSubTabSelected(position);
    }

    public void onSubTabReselected(SubTab subTab, FragmentTransaction ft) {
    }

    public void onSubTabSelected(SubTab subTab, FragmentTransaction ft) {
        if (subTab.getTag() instanceof SubTabInfo) {
            SubTabInfo tag = (SubTabInfo) subTab.getTag();
            for (int i = 0; i < this.mSubTabs.size(); i++) {
                if (this.mSubTabs.get(i) == tag) {
                    notifyDataSetChanged();
                    this.mViewPager.setCurrentItem(i);
                }
            }
        }
    }

    public void onSubTabUnselected(SubTab subTab, FragmentTransaction ft) {
    }
}
