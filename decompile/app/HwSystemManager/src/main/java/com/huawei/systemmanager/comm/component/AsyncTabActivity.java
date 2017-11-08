package com.huawei.systemmanager.comm.component;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.ActionBar.TabListener;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import com.google.common.collect.Lists;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.emui.activities.HsmActivity;
import com.huawei.systemmanager.util.HwLog;
import java.util.ArrayList;

public abstract class AsyncTabActivity extends HsmActivity {
    private static final long DELAY_LOAD_ALL_FRAGMENT = 3000;
    private static final String KEY_SELECT_ITEM = "select_item";
    private static final int MSG_LOAD_ALL_FRAGMENT = 2;
    private static final String TAG = AsyncTabActivity.class.getSimpleName();
    private ActionBar mActionBar;
    private LoadState mCurState = this.mInitialState;
    private Fragment mCurrentFragment;
    private ArrayList<FragmentInfo> mFragmentInfos = Lists.newArrayList();
    private LoadState mFullState = new FullState();
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 2:
                    AsyncTabActivity.this.mCurState.onLoadAllMsg();
                    return;
                default:
                    return;
            }
        }
    };
    private LoadState mInitialState = new InitialState();
    private LoadState mOnePageState = new OnePageState();
    protected ViewPager mViewPager;
    private ViewPagerAdapter mViewPagerAdapter;

    static class FragmentInfo {
        final Class<? extends Fragment> cls;
        Fragment frg;
        final int id;
        final String title;

        public FragmentInfo(Class<? extends Fragment> cls, String title, int id) {
            this.cls = cls;
            this.title = title;
            this.id = id;
        }

        Fragment buildFragment() {
            if (this.frg != null) {
                return this.frg;
            }
            try {
                this.frg = (Fragment) this.cls.newInstance();
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e2) {
                e2.printStackTrace();
            } catch (Exception e3) {
                e3.printStackTrace();
            }
            if (this.frg == null) {
                HwLog.e(AsyncTabActivity.TAG, "getRealFragment initial failed!!");
                this.frg = new Fragment();
            }
            return this.frg;
        }
    }

    private abstract class LoadState {
        private LoadState() {
        }

        public void setState() {
            AsyncTabActivity.this.mCurState = this;
        }

        public void enter(int idx) {
        }

        public void onResume() {
        }

        public void onLoadAllMsg() {
        }

        public void onTabSelected(int position) {
        }
    }

    private class FullState extends LoadState {
        private boolean loaded;
        private int selectIndex;

        private FullState() {
            super();
        }

        public void enter(int index) {
            HwLog.i(AsyncTabActivity.TAG, "enter FullState, index:" + index);
            this.selectIndex = index;
            AsyncTabActivity.this.mViewPager.setOnTouchListener(null);
            setState();
            loadAllPage();
        }

        public void onResume() {
            HwLog.d(AsyncTabActivity.TAG, "FullState onTabSelected, onResume");
            loadAllPage();
        }

        public void onTabSelected(int position) {
            HwLog.d(AsyncTabActivity.TAG, "FullState onTabSelected, position:" + position);
            AsyncTabActivity.this.mViewPager.setCurrentItem(position);
        }

        public void onLoadAllMsg() {
            HwLog.d(AsyncTabActivity.TAG, "FullState onLoadAllMsg");
            if (AsyncTabActivity.this.mViewPager != null) {
                AsyncTabActivity.this.mViewPager.setOnTouchListener(null);
            }
        }

        private void loadAllPage() {
            if (AsyncTabActivity.this.mViewPagerAdapter == null) {
                HwLog.e(AsyncTabActivity.TAG, "loadOnePage, mViewPagerAdapter is null");
            } else if (this.loaded) {
                HwLog.d(AsyncTabActivity.TAG, "loadAllPage, already loaded.");
            } else if (AsyncTabActivity.this.isResumed()) {
                this.loaded = true;
                AsyncTabActivity.this.mViewPagerAdapter.loadAllFragment();
                AsyncTabActivity.this.mViewPager.setCurrentItem(this.selectIndex);
            } else {
                HwLog.i(AsyncTabActivity.TAG, "loadAllPage, not resumed");
            }
        }
    }

    private class InitialState extends LoadState {
        private int selectIndex;

        private InitialState() {
            super();
        }

        public void enter(int idx) {
            this.selectIndex = idx;
        }

        public void onLoadAllMsg() {
            HwLog.d(AsyncTabActivity.TAG, "InitialState hanlder load all message, index:" + this.selectIndex);
            AsyncTabActivity.this.mFullState.enter(this.selectIndex);
        }
    }

    private class OnePageState extends LoadState {
        private boolean loaded;
        private int selectIndex;

        private OnePageState() {
            super();
        }

        public void enter(int idx) {
            HwLog.i(AsyncTabActivity.TAG, "enter OnePageState, index:" + idx);
            this.selectIndex = idx;
            setState();
            loadOnePage();
        }

        public void onResume() {
            HwLog.d(AsyncTabActivity.TAG, "OnePageState  onResume");
            loadOnePage();
        }

        public void onTabSelected(int position) {
            HwLog.d(AsyncTabActivity.TAG, "OnePageState onTabSelected, position:" + position);
            AsyncTabActivity.this.mFullState.enter(position);
        }

        public void onLoadAllMsg() {
            HwLog.d(AsyncTabActivity.TAG, "OnePageState onLoadAllMsg");
            AsyncTabActivity.this.mFullState.enter(this.selectIndex);
        }

        private void loadOnePage() {
            if (AsyncTabActivity.this.mViewPagerAdapter == null) {
                HwLog.e(AsyncTabActivity.TAG, "loadOnePage, mViewPagerAdapter is null");
            } else if (this.loaded) {
                HwLog.d(AsyncTabActivity.TAG, "loadOnePage, already loaded.");
            } else if (AsyncTabActivity.this.isResumed()) {
                this.loaded = true;
                AsyncTabActivity.this.mViewPagerAdapter.loadOneFragment(this.selectIndex);
                AsyncTabActivity.this.mViewPager.setCurrentItem(this.selectIndex);
            } else {
                HwLog.i(AsyncTabActivity.TAG, "loadOnePage, not resumed");
            }
        }
    }

    class PagerListener implements OnPageChangeListener {
        PagerListener() {
        }

        public void onPageScrollStateChanged(int arg0) {
            AsyncTabActivity.this.onPageScrollStateChanged(arg0);
        }

        public void onPageScrolled(int arg0, float arg1, int arg2) {
            AsyncTabActivity.this.onPageScrolled(arg0, arg1, arg2);
        }

        public void onPageSelected(int arg0) {
            HwLog.i(AsyncTabActivity.TAG, "PagerListener, onPageSelected:" + arg0);
            AsyncTabActivity.this.onPageSelected(arg0);
            AsyncTabActivity.this.getActionBar().selectTab(AsyncTabActivity.this.getActionBar().getTabAt(arg0));
        }
    }

    class TabClickListener implements TabListener {
        TabClickListener() {
        }

        public void onTabReselected(Tab tab, FragmentTransaction ft) {
            AsyncTabActivity.this.onTabReselected(tab, ft);
        }

        public void onTabSelected(Tab tab, FragmentTransaction ft) {
            AsyncTabActivity.this.mCurState.onTabSelected(tab.getPosition());
            AsyncTabActivity.this.onTabSelected(tab, ft);
        }

        public void onTabUnselected(Tab tab, FragmentTransaction ft) {
            AsyncTabActivity.this.onTabUnselected(tab, ft);
        }
    }

    protected class ViewPagerAdapter extends FragmentPagerAdapter {
        private ArrayList<FragmentInfo> mChildFrgs = Lists.newArrayList();

        public ViewPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        public Fragment getItem(int position) {
            Fragment frg = ((FragmentInfo) this.mChildFrgs.get(position)).frg;
            if (frg == null) {
                HwLog.e(AsyncTabActivity.TAG, "Fragment == null!");
            }
            return frg;
        }

        public int getCount() {
            return this.mChildFrgs.size();
        }

        public void loadOneFragment(int index) {
            HwLog.i(AsyncTabActivity.TAG, "begin to load page:" + index);
            this.mChildFrgs.clear();
            FragmentInfo info = (FragmentInfo) AsyncTabActivity.this.mFragmentInfos.get(index);
            info.buildFragment();
            this.mChildFrgs.add(info);
            notifyDataSetChanged();
        }

        public void loadAllFragment() {
            this.mChildFrgs.clear();
            HwLog.i(AsyncTabActivity.TAG, "begin to load all pages");
            for (FragmentInfo info : AsyncTabActivity.this.mFragmentInfos) {
                this.mChildFrgs.add(info);
            }
            notifyDataSetChanged();
        }

        public long getItemId(int position) {
            return (long) ((FragmentInfo) this.mChildFrgs.get(position)).id;
        }

        public int getItemPosition(Object object) {
            int size = this.mChildFrgs.size();
            for (int i = 0; i < size; i++) {
                if (((FragmentInfo) this.mChildFrgs.get(i)).frg == object) {
                    return i;
                }
            }
            return -2;
        }

        public void setPrimaryItem(ViewGroup container, int position, Object object) {
            super.setPrimaryItem(container, position, object);
            AsyncTabActivity.this.mCurrentFragment = (Fragment) object;
        }
    }

    protected abstract void onInitialFragment();

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.base_tab_activity);
        initialFragment(savedInstanceState);
        initActionBar();
        initViewPager();
        if (savedInstanceState != null) {
            int selectIndex = savedInstanceState.getInt(KEY_SELECT_ITEM, 0);
            this.mFullState.enter(selectIndex);
            HwLog.i(TAG, "savedInstanceState!= null, selectIndex = " + selectIndex);
            return;
        }
        this.mHandler.sendEmptyMessageDelayed(2, DELAY_LOAD_ALL_FRAGMENT);
    }

    protected void onResume() {
        super.onResume();
        this.mCurState.onResume();
    }

    protected final boolean isInInitialState() {
        return this.mCurState == this.mInitialState;
    }

    private void initialFragment(Bundle savedInstanceState) {
        onInitialFragment();
        if (savedInstanceState != null) {
            HwLog.i(TAG, "initialFragment, savedInstanceState not null, restore fragment");
            FragmentManager fm = getFragmentManager();
            int size = this.mFragmentInfos.size();
            for (int i = 0; i < size; i++) {
                Fragment frg = fm.findFragmentByTag(savedInstanceState.getString(buildFragmentKey(i), null));
                if (frg != null) {
                    FragmentInfo fInfo = (FragmentInfo) this.mFragmentInfos.get(i);
                    if (fInfo.cls.getName().equals(frg.getClass().getName())) {
                        fInfo.frg = frg;
                        HwLog.d(TAG, "initialFragment, " + fInfo.cls.getName() + "reuse!");
                    } else {
                        HwLog.e(TAG, "class name not equal! " + fInfo.cls.getName());
                    }
                }
            }
        }
    }

    private String buildFragmentKey(int position) {
        return "fragment#" + position;
    }

    protected final void addFragment(Class<? extends Fragment> fragCls, String title) {
        this.mFragmentInfos.add(new FragmentInfo(fragCls, title, this.mFragmentInfos.size()));
    }

    protected void onSaveInstanceState(Bundle outState) {
        HwLog.i(TAG, "onSaveInstanceState");
        int size = this.mFragmentInfos.size();
        for (int i = 0; i < size; i++) {
            Fragment frg = ((FragmentInfo) this.mFragmentInfos.get(i)).frg;
            if (frg != null) {
                outState.putString(buildFragmentKey(i), frg.getTag());
            }
        }
        if (this.mViewPager != null) {
            outState.putInt(KEY_SELECT_ITEM, this.mViewPager.getCurrentItem());
        }
        super.onSaveInstanceState(outState);
    }

    private void initActionBar() {
        this.mActionBar = getActionBar();
        this.mActionBar.setDisplayShowHomeEnabled(false);
        this.mActionBar.setDisplayShowTitleEnabled(false);
        this.mActionBar.setNavigationMode(2);
        this.mActionBar.setDisplayOptions(16, 16);
        TabListener tabListener = new TabClickListener();
        for (FragmentInfo fInfo : this.mFragmentInfos) {
            this.mActionBar.addTab(this.mActionBar.newTab().setText(fInfo.title).setTabListener(tabListener));
        }
    }

    private void initViewPager() {
        this.mViewPagerAdapter = new ViewPagerAdapter(getFragmentManager());
        this.mViewPager = (ViewPager) findViewById(R.id.pager);
        this.mViewPager.setAdapter(this.mViewPagerAdapter);
        this.mViewPager.setOnPageChangeListener(new PagerListener());
        this.mViewPager.setOnTouchListener(new OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                AsyncTabActivity.this.mHandler.sendEmptyMessage(2);
                return false;
            }
        });
    }

    public void onBackPressed() {
        boolean handled = false;
        Fragment currentFragment = this.mCurrentFragment;
        if (currentFragment != null && (currentFragment instanceof IBackPressListener)) {
            handled = ((IBackPressListener) currentFragment).onBackPressed();
        }
        if (!handled) {
            super.onBackPressed();
        }
    }

    public void onWindowFocusChanged(boolean hasFocus) {
        for (FragmentInfo info : this.mFragmentInfos) {
            Fragment frg = info.frg;
            if (frg != null && (frg instanceof IWindowFocusChangedListener)) {
                ((IWindowFocusChangedListener) frg).onWindowFocusChanged(hasFocus);
            }
        }
    }

    public void setSelectTab(int idx) {
        if (this.mActionBar != null) {
            this.mActionBar.setSelectedNavigationItem(idx);
            if (this.mCurState == this.mInitialState) {
                HwLog.i(TAG, "current is initial state setSelectTab called, idx=" + idx);
                this.mOnePageState.enter(idx);
            }
            return;
        }
        HwLog.e(TAG, "setSelectTab ,actionbar is null! idx=" + idx);
    }

    protected void onPageScrollStateChanged(int arg0) {
    }

    protected void onTabReselected(Tab tab, FragmentTransaction ft) {
    }

    protected void onTabSelected(Tab tab, FragmentTransaction ft) {
    }

    protected void onTabUnselected(Tab tab, FragmentTransaction ft) {
    }

    protected void onPageScrolled(int arg0, float arg1, int arg2) {
    }

    protected void onPageSelected(int arg0) {
    }

    public ViewPagerAdapter getAdapter() {
        return this.mViewPagerAdapter;
    }

    public ViewPager getViewPager() {
        return this.mViewPager;
    }
}
