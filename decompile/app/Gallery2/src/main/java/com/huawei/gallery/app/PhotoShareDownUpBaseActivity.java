package com.huawei.gallery.app;

import android.app.FragmentTransaction;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.SparseArray;
import android.view.View;
import com.android.gallery3d.R;
import com.android.gallery3d.util.GalleryLog;
import com.android.gallery3d.util.ReportToBigData;
import com.huawei.gallery.actionbar.ActionMode;
import com.huawei.gallery.actionbar.GalleryActionBar;
import com.huawei.gallery.actionbar.TabMode;
import com.huawei.gallery.ui.GalleryViewPager;
import com.huawei.gallery.ui.GalleryViewPager.ViewPageCallback;
import huawei.android.widget.SubTabWidget;
import huawei.android.widget.SubTabWidget.SubTab;
import huawei.android.widget.SubTabWidget.SubTabListener;
import java.util.ArrayList;

public class PhotoShareDownUpBaseActivity extends AbstractGalleryActivity implements OnPageChangeListener, ViewPageCallback {
    private GalleryActionBar mActionBar;
    private ActionMode mActionMode;
    private PhotoShareDownUpPagerAdapter mAdapter;
    private SparseArray<Fragment> mFragmentArray = new SparseArray();
    private SubTab mLeftSubTab = null;
    private View mMainView = null;
    private SubTab mRightSubTab = null;
    private MySubTabListener mSubTabListener = new MySubTabListener();
    private SubTabWidget mSubTabWidget;
    protected int mType;
    private GalleryViewPager mViewPager;

    private class MyPagerAdapter extends FragmentPagerAdapter {
        public MyPagerAdapter() {
            super(PhotoShareDownUpBaseActivity.this.getSupportFragmentManager());
        }

        public int getCount() {
            return 2;
        }

        public Fragment getItem(int position) {
            return PhotoShareDownUpBaseActivity.this.getFragment(position);
        }
    }

    private class MySubTabListener implements SubTabListener {
        private MySubTabListener() {
        }

        public void onSubTabReselected(SubTab arg0, FragmentTransaction arg1) {
        }

        public void onSubTabUnselected(SubTab arg0, FragmentTransaction arg1) {
            ((PhotoShareDownUpFragment) PhotoShareDownUpBaseActivity.this.getFragment(arg0.getPosition())).selectionManagerLeaveSelectionMode();
        }

        public void onSubTabSelected(SubTab arg0, FragmentTransaction arg1) {
            switch (arg0.getPosition()) {
                case 0:
                    PhotoShareDownUpBaseActivity.this.mViewPager.setCurrentItem(0);
                    return;
                case 1:
                    PhotoShareDownUpBaseActivity.this.mViewPager.setCurrentItem(1);
                    return;
                default:
                    return;
            }
        }
    }

    private class PhotoShareDownUpPagerAdapter extends FragmentPagerAdapter {
        public PhotoShareDownUpPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        public Fragment getItem(int position) {
            return (Fragment) PhotoShareDownUpBaseActivity.this.mFragmentArray.get(position);
        }

        public int getCount() {
            return PhotoShareDownUpBaseActivity.this.mFragmentArray.size();
        }

        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    if (1 == PhotoShareDownUpBaseActivity.this.mType) {
                        return PhotoShareDownUpBaseActivity.this.getAndroidContext().getString(R.string.photoshare_down_up_tab_download);
                    }
                    return PhotoShareDownUpBaseActivity.this.getAndroidContext().getString(R.string.photoshare_down_up_tab_upload);
                case 1:
                    if (1 == PhotoShareDownUpBaseActivity.this.mType) {
                        return PhotoShareDownUpBaseActivity.this.getAndroidContext().getString(R.string.photoshare_down_up_tab_download_finish);
                    }
                    return PhotoShareDownUpBaseActivity.this.getAndroidContext().getString(R.string.photoshare_down_up_tab_upload_finish);
                default:
                    throw new IllegalStateException("unknow position");
            }
        }

        public int getItemPosition(Object object) {
            if (PhotoShareDownUpBaseActivity.this.mFragmentArray.indexOfValue((Fragment) object) >= 0) {
                return -1;
            }
            return -2;
        }

        public long getItemId(int position) {
            return (long) ((Fragment) PhotoShareDownUpBaseActivity.this.mFragmentArray.get(position)).hashCode();
        }
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initFragment(savedInstanceState);
        setContentView(R.layout.photoshare_down_up_main);
        this.mViewPager = (GalleryViewPager) findViewById(R.id.fragment_viewpage);
        this.mMainView = findViewById(R.id.gallery_main_root);
        MyPagerAdapter pagerAdapter = new MyPagerAdapter();
        this.mViewPager.setOffscreenPageLimit(1);
        this.mViewPager.setAdapter(pagerAdapter);
        this.mViewPager.setOnPageChangeListener(this);
        this.mViewPager.setCurrentItem(0);
        this.mViewPager.setCallback(this);
        this.mViewPager.setHorizontalScrollBarEnabled(false);
        GalleryLog.d("PhotoShareDownUpBaseActivity", "onCreate, pagerAdapter count: " + pagerAdapter.getCount());
        GalleryLog.d("PhotoShareDownUpBaseActivity", "onCreate, set current item: 0");
        if (getIntent() != null) {
            ReportToBigData.reportCloudUploadDownloadPage(getIntent().getStringExtra("key-enter-from"), this.mType);
        }
        this.mAdapter = new PhotoShareDownUpPagerAdapter(getSupportFragmentManager());
        this.mActionBar = getGalleryActionBar();
        this.mActionMode = this.mActionBar.enterStandardTitleActionMode(false);
        if (this.mType == 1) {
            this.mActionMode.setTitle(getResources().getString(R.string.photoshare_down_up_tab_download));
        } else {
            this.mActionMode.setTitle(getResources().getString(R.string.photoshare_down_up_tab_upload));
        }
        this.mActionMode.show();
        this.mSubTabWidget = (SubTabWidget) findViewById(R.id.subTab_layout);
        this.mLeftSubTab = this.mSubTabWidget.newSubTab(null, this.mSubTabListener, null);
        this.mLeftSubTab.setSubTabId(R.id.sub_tab_left);
        this.mRightSubTab = this.mSubTabWidget.newSubTab(null, this.mSubTabListener, null);
        this.mRightSubTab.setSubTabId(R.id.sub_tab_right);
        this.mSubTabWidget.addSubTab(this.mLeftSubTab, true);
        this.mSubTabWidget.addSubTab(this.mRightSubTab, false);
    }

    public void updateMainViewPadding() {
        this.mMainView.setPadding(0, getGalleryActionBar().getActionBarHeight(), 0, 0);
    }

    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        updateMainViewPadding();
    }

    protected void onResume() {
        super.onResume();
        updateMainViewPadding();
    }

    public void updateTitle(String title, int type) {
        switch (type) {
            case 1:
                if (this.mType == 1) {
                    this.mLeftSubTab.setText(title);
                    return;
                }
                return;
            case 2:
                if (this.mType == 1) {
                    this.mRightSubTab.setText(title);
                    return;
                }
                return;
            case 3:
                if (this.mType == 2) {
                    this.mLeftSubTab.setText(title);
                    return;
                }
                return;
            case 4:
                if (this.mType == 2) {
                    this.mRightSubTab.setText(title);
                    return;
                }
                return;
            default:
                return;
        }
    }

    public void onNavigationBarChanged(boolean show, int height) {
    }

    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        ArrayList<String> tags = new ArrayList();
        for (int i = 0; i < this.mFragmentArray.size(); i++) {
            tags.add(i, ((Fragment) this.mFragmentArray.get(i)).getTag());
        }
        outState.putStringArrayList("fragment_array", tags);
    }

    private void initFragment(Bundle savedInstanceState) {
        int i;
        if (savedInstanceState == null || savedInstanceState.getStringArrayList("fragment_array") == null) {
            for (i = 0; i < 2; i++) {
                this.mFragmentArray.put(i, createFragment(i));
            }
        } else {
            ArrayList<String> list = savedInstanceState.getStringArrayList("fragment_array");
            for (i = 0; i < list.size(); i++) {
                Fragment f = getSupportFragmentManager().findFragmentByTag((String) list.get(i));
                if (f == null) {
                    f = createFragment(i);
                }
                this.mFragmentArray.put(i, f);
            }
        }
        this.mContent = (AbstractGalleryFragment) getFragment(0);
    }

    public void onPageScrollStateChanged(int state) {
    }

    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
    }

    public void onPageSelected(int position) {
        if (this.mAdapter != null && this.mViewPager != null) {
            this.mAdapter.notifyDataSetChanged();
            Fragment fragment = this.mAdapter.getItem(position);
            if (fragment instanceof AbstractGalleryFragment) {
                this.mContent = (AbstractGalleryFragment) fragment;
            }
            this.mContent.onUserSelected(true);
            this.mViewPager.setCurrentItem(position);
        }
    }

    public boolean disableScroll() {
        return !(getGalleryActionBar().getCurrentMode() instanceof TabMode);
    }

    private Fragment createFragment(int position) {
        Bundle data = new Bundle();
        data.putInt("photo_share_down_up_fragment_state", getFragmentType(this.mType, position));
        Fragment fragment = new PhotoShareDownUpFragment();
        fragment.setArguments(data);
        return fragment;
    }

    private Fragment getFragment(int position) {
        return (Fragment) this.mFragmentArray.get(position);
    }

    private static int getFragmentType(int type, int position) {
        if (1 == type) {
            if (position == 0) {
                return 1;
            }
            return 1 == position ? 2 : 0;
        } else if (2 != type) {
            return 0;
        } else {
            if (position == 0) {
                return 3;
            }
            if (1 == position) {
                return 4;
            }
            return 0;
        }
    }

    protected boolean needToRequestPermissions() {
        return false;
    }
}
