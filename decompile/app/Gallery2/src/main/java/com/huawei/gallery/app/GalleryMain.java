package com.huawei.gallery.app;

import android.app.ActionBar.Tab;
import android.app.ActionBar.TabListener;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager.SimpleOnPageChangeListener;
import android.util.SparseArray;
import android.view.View;
import com.android.gallery3d.R;
import com.android.gallery3d.app.GalleryAppImpl;
import com.android.gallery3d.common.Utils;
import com.android.gallery3d.settings.HicloudAccountManager;
import com.android.gallery3d.util.GalleryLog;
import com.android.gallery3d.util.GalleryUtils;
import com.android.gallery3d.util.ReportToBigData;
import com.android.gallery3d.util.TraceController;
import com.huawei.gallery.actionbar.ActionBarStateBase;
import com.huawei.gallery.actionbar.GalleryActionBar;
import com.huawei.gallery.actionbar.TabMode;
import com.huawei.gallery.media.services.StorageService;
import com.huawei.gallery.photoshare.ui.PhotoShareMainFragment;
import com.huawei.gallery.photoshare.utils.PhotoShareUtils;
import com.huawei.gallery.photoshare.utils.RefreshHelper;
import com.huawei.gallery.recycle.utils.RecycleAsync;
import com.huawei.gallery.ui.GalleryViewPager;
import com.huawei.gallery.ui.GalleryViewPager.ViewPageCallback;
import com.huawei.gallery.util.BundleUtils;
import com.huawei.gallery.util.TabIndexUtils;
import com.huawei.gallery.util.UIUtils;
import java.util.ArrayList;

public class GalleryMain extends AbstractGalleryActivity implements ViewPageCallback, TabListener {
    private GalleryMainPagerAdapter mAdapter;
    private Handler mAsyncProcessingHandler;
    private View mBackgroudView;
    private OnClickListener mCloudListener = new OnClickListener() {
        public void onClick(DialogInterface dialog, int which) {
            if (which == -1) {
                PhotoShareUtils.saveClickTime(GalleryMain.this, false);
                GalleryUtils.startActivitySafe(GalleryMain.this);
                return;
            }
            PhotoShareUtils.saveClickTime(GalleryMain.this, false);
        }
    };
    private AlertDialog mCloudSwitchDialog;
    private int mDelayPageSelect = -1;
    private boolean mDisableTabSelection;
    private boolean mDisableViewPagerScrool;
    private SparseArray<Fragment> mFragmentArray = new SparseArray();
    private HandlerThread mHandlerThread;
    private boolean mHasInited;
    private boolean mIsScrolled;
    private boolean mIsTabClick;
    private boolean mNeedNotifyDataSetChanged = false;
    private GalleryMainSimpleOnPageChangeListener mPageChangeListener;
    private ArrayList<OnPageChangedListener> mPageChangedListener = new ArrayList();
    private GalleryViewPager mViewPager;
    private int mViewPagerState = 0;

    private class GalleryMainPagerAdapter extends GalleryFragmentPagerAdapter {
        public GalleryMainPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        public Fragment getItem(int position) {
            return (Fragment) GalleryMain.this.mFragmentArray.get(position);
        }

        public boolean isValid(Fragment fragment) {
            return GalleryMain.this.mFragmentArray.indexOfValue(fragment) >= 0;
        }

        public int getCount() {
            return GalleryMain.this.mFragmentArray.size();
        }

        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return GalleryMain.this.getAndroidContext().getString(R.string.tab_photo);
                case 1:
                    return GalleryMain.this.getAndroidContext().getString(R.string.album);
                case 2:
                    return GalleryMain.this.getAndroidContext().getString(R.string.tab_discover);
                default:
                    throw new IllegalStateException("unknow position");
            }
        }

        public int getPageId(int position) {
            switch (position) {
                case 0:
                    return R.id.id_timebucket;
                case 1:
                    return R.id.id_list;
                case 2:
                    return R.id.id_cloud;
                default:
                    throw new IllegalStateException("unknow position");
            }
        }
    }

    private class GalleryMainSimpleOnPageChangeListener extends SimpleOnPageChangeListener {
        private GalleryMainSimpleOnPageChangeListener() {
        }

        public void onPageScrollStateChanged(int state) {
            boolean z;
            boolean z2 = true;
            GalleryMain.this.mViewPagerState = state;
            switch (state) {
                case 0:
                    if (GalleryMain.this.mDelayPageSelect != -1) {
                        setPagePosition(GalleryMain.this.mDelayPageSelect);
                        GalleryMain.this.mDelayPageSelect = -1;
                        break;
                    }
                    break;
                case 1:
                    GalleryMain.this.mIsScrolled = true;
                    break;
            }
            GalleryMain galleryMain = GalleryMain.this;
            if (state != 0) {
                z = true;
            } else {
                z = false;
            }
            galleryMain.enableBackgroudCover(z);
            GalleryActionBar galleryActionBar = GalleryMain.this.getGalleryActionBar();
            if (state != 0) {
                z2 = false;
            }
            galleryActionBar.setMenuClickable(z2);
        }

        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        }

        public void onPageSelected(int position) {
            GalleryLog.d("GalleryMain", "onPageSelected " + position);
            GalleryMain.this.mViewPager.setCurrentItem(position);
            TabIndexUtils.setIndex(GalleryMain.this, position);
            ActionBarStateBase mode = GalleryMain.this.getGalleryActionBar().getCurrentMode();
            if (mode instanceof TabMode) {
                ((TabMode) mode).setSelectedNavigationItem(position);
            }
            if (GalleryMain.this.mViewPagerState != 0) {
                GalleryMain.this.mDelayPageSelect = position;
            } else {
                setPagePosition(position);
            }
        }

        private void setPagePosition(int position) {
            GalleryLog.d("GalleryMain", "setPagePosition " + position);
            String current = null;
            switch (position) {
                case 0:
                    current = "TIME";
                    break;
                case 1:
                    current = "LIST";
                    break;
                case 2:
                    current = "CLOUD";
                    RefreshHelper.refreshDiscovery(System.currentTimeMillis());
                    break;
            }
            if (GalleryMain.this.mIsTabClick) {
                ReportToBigData.report(39, String.format("{SwitchTab:%s,Current:%s}", new Object[]{"Click", current}));
            } else if (GalleryMain.this.mIsScrolled) {
                ReportToBigData.report(39, String.format("{SwitchTab:%s,Current:%s}", new Object[]{"Slide", current}));
            }
            GalleryLog.d("GalleryMain", "onPageSelected, mAdapter count: " + GalleryMain.this.mAdapter.getCount());
            GalleryLog.d("GalleryMain", "onPageSelected, current item: " + GalleryMain.this.mViewPager.getCurrentItem());
            GalleryMain.this.initPages(position);
            for (int index = 0; index < GalleryMain.this.mAdapter.getCount(); index++) {
                boolean selected;
                Fragment fragment = GalleryMain.this.mAdapter.getItem(index);
                AbstractGalleryFragment galleryFragment = null;
                if (fragment instanceof AbstractGalleryFragment) {
                    galleryFragment = (AbstractGalleryFragment) fragment;
                }
                if (position == index) {
                    selected = true;
                } else {
                    selected = false;
                }
                if (selected) {
                    GalleryMain.this.mContent = galleryFragment;
                }
                if (galleryFragment != null) {
                    galleryFragment.onUserSelected(selected);
                }
            }
            GalleryMain.this.mIsTabClick = false;
            GalleryMain.this.mIsScrolled = false;
        }
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        GalleryLog.d("GalleryMain", "GalleryMain onCreate.");
        setContentView(R.layout.gallery_main);
        this.mBackgroudView = findViewById(R.id.gl_Cover_view);
        this.mViewPager = (GalleryViewPager) findViewById(R.id.fragment_viewpage);
        UIUtils.setNavigationBarIsOverlay(this.mViewPager, true);
        init(savedInstanceState);
        TraceController.endSection();
        TraceController.beginSection("UserGuard");
        if (UserGuardPage.isNeedShow(this) && PhotoShareUtils.isSupportPhotoShare() && !PhotoShareUtils.isCloudPhotoSwitchOpen()) {
            TraceController.beginSection("start UserGuard activity");
            startActivityForResult(new Intent(this, UserGuardPage.class), 1103);
            TraceController.endSection();
        }
        this.mHandlerThread = new HandlerThread("GalleryMain Asynchronous Handler", -2);
        this.mHandlerThread.start();
        this.mAsyncProcessingHandler = new Handler(this.mHandlerThread.getLooper()) {
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 1:
                        ((HicloudAccountManager) GalleryMain.this.getGalleryApplication().getAppComponent(HicloudAccountManager.class)).queryHicloudAccount(GalleryMain.this, false);
                        return;
                    default:
                        super.handleMessage(msg);
                        return;
                }
            }
        };
        RecycleAsync.getInstance().start(getContentResolver());
        TraceController.endSection();
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case 1103:
                if (resultCode == 1104) {
                    GalleryLog.d("GalleryMain", "User not selected in user guard page, finish GalleryMain.");
                    finish();
                    return;
                }
                return;
            default:
                super.onActivityResult(requestCode, resultCode, data);
                return;
        }
    }

    private void init(Bundle savedInstanceState) {
        int lastPageIndex;
        TraceController.beginSection("GalleryMain.init");
        if (BundleUtils.getBoolean(getIntent().getExtras(), "key-no-page-history", false)) {
            TabIndexUtils.setIndex(this, 0);
            GalleryLog.d("GalleryMain", "saveCurrentPageIndex: 0");
        }
        if ("android.intent.action.GET_PHOTOSHARE_CONTENT".equals(getIntent().getAction())) {
            lastPageIndex = 2;
        } else if ("android.intent.action.GET_ALBUM_CONTENT".equals(getIntent().getAction())) {
            lastPageIndex = 1;
        } else {
            lastPageIndex = getLastPageIndex();
        }
        GalleryLog.d("GalleryMain", "Last Tab Index = " + lastPageIndex);
        TraceController.beginSection("setupActionBar");
        setUpActionBar();
        TraceController.endSection();
        if (savedInstanceState == null || savedInstanceState.getStringArrayList("key-fragment-tags") == null) {
            this.mFragmentArray.put(0, new PlaceHolderFragment());
            this.mFragmentArray.put(1, new PlaceHolderFragment());
            this.mFragmentArray.put(2, new PlaceHolderFragment());
        } else {
            GalleryLog.d("GalleryMain", "restore fragments");
            ArrayList<String> list = savedInstanceState.getStringArrayList("key-fragment-tags");
            for (int i = 0; i < list.size(); i++) {
                Fragment f = getSupportFragmentManager().findFragmentByTag((String) list.get(i));
                if (f == null) {
                    f = new PlaceHolderFragment();
                }
                this.mFragmentArray.put(i, f);
            }
        }
        initPages(lastPageIndex);
        this.mAdapter = new GalleryMainPagerAdapter(getSupportFragmentManager());
        this.mViewPager.setOffscreenPageLimit(2);
        this.mViewPager.setAdapter(this.mAdapter);
        this.mViewPager.setCallback(this);
        this.mPageChangeListener = new GalleryMainSimpleOnPageChangeListener();
        GalleryLog.d("GalleryMain", "init, mAdapter count: " + this.mAdapter.getCount());
        GalleryLog.d("GalleryMain", "init, set current item: " + lastPageIndex);
        if (this.mFragmentArray.size() > 0) {
            Fragment fragment = this.mAdapter.getItem(Utils.clamp(lastPageIndex, 0, this.mFragmentArray.size() - 1));
            if (fragment instanceof AbstractGalleryFragment) {
                this.mContent = (AbstractGalleryFragment) fragment;
            }
            this.mContent.onUserSelected(true);
        }
        setUpActionBarTab(lastPageIndex);
        TraceController.endSection();
    }

    private void setUpActionBarTab(int tabIndex) {
        TraceController.beginSection("initActionbarTabs");
        setUpTabs();
        getGalleryActionBar().disableAnimation(true);
        this.mViewPager.setOnPageChangeListener(this.mPageChangeListener);
        GalleryLog.d("GalleryMain", "Last Tab Index = " + tabIndex);
        this.mViewPager.setCurrentItem(tabIndex);
        if (tabIndex == 0) {
            this.mPageChangeListener.onPageSelected(0);
        }
        this.mHasInited = true;
        TraceController.endSection();
    }

    private boolean initPages(int index) {
        Fragment oldFragment = (Fragment) this.mFragmentArray.get(index);
        if (!(oldFragment instanceof PlaceHolderFragment)) {
            return false;
        }
        Bundle data;
        Fragment fragment;
        switch (index) {
            case 0:
                GalleryLog.d("GalleryMain", "init TimeBucketAlbumHost");
                this.mFragmentArray.put(0, new TimeBucketAlbumHost());
                getSupportFragmentManager().beginTransaction().remove(oldFragment).commitAllowingStateLoss();
                notifyDataSetChanged();
                return true;
            case 1:
                GalleryLog.d("GalleryMain", "init ListAlbumSetFragment");
                data = new Bundle();
                data.putString("media-path", getDataManager().getTopSetPath(131072));
                data.putBoolean("is_outside", true);
                fragment = new ListAlbumSetFragment();
                fragment.setArguments(data);
                this.mFragmentArray.put(1, fragment);
                getSupportFragmentManager().beginTransaction().remove(oldFragment).commitAllowingStateLoss();
                notifyDataSetChanged();
                return true;
            case 2:
                GalleryLog.d("GalleryMain", "init cloud page");
                data = new Bundle();
                data.putString("media-path", "/photoshare/all");
                fragment = new PhotoShareMainFragment();
                fragment.setArguments(data);
                this.mFragmentArray.put(2, fragment);
                getSupportFragmentManager().beginTransaction().remove(oldFragment).commitAllowingStateLoss();
                notifyDataSetChanged();
                return true;
            default:
                return false;
        }
    }

    private void notifyDataSetChanged() {
        if (this.mAdapter != null) {
            this.mAdapter.notifyDataSetChanged();
            GalleryLog.d("GalleryMain", "notifyDataSetChanged.");
            GalleryLog.d("GalleryMain", "mAdapter count: " + this.mAdapter.getCount());
        }
    }

    protected void onStart() {
        TraceController.beginSection("GalleryMain.onStart");
        super.onStart();
        enableBackgroudCover(false);
        TraceController.endSection();
    }

    protected void onResume() {
        GalleryAppImpl.setBootStartupStatus(false);
        TraceController.beginSection("GalleryMain.onResume");
        super.onResume();
        GalleryLog.d("GalleryMain", "onResume, viewPager current item: " + this.mViewPager.getCurrentItem());
        if (PhotoShareUtils.isSupportPhotoShare()) {
            this.mAsyncProcessingHandler.sendEmptyMessage(1);
        }
        StorageService.checkStorageSpace();
        new Thread(new Runnable() {
            public void run() {
                if (PhotoShareUtils.shouldShowCloudSwitch(GalleryMain.this)) {
                    GalleryMain.this.runOnUiThread(new Runnable() {
                        public void run() {
                            GalleryMain.this.showCloudTipsDialog();
                        }
                    });
                }
            }
        }).start();
        TraceController.endSection();
    }

    protected void onPause() {
        super.onPause();
        if (this.mCloudSwitchDialog != null) {
            this.mCloudSwitchDialog.dismiss();
        }
        saveCurrentPageIndex();
    }

    protected void onDestroy() {
        super.onDestroy();
        this.mHandlerThread.quitSafely();
    }

    public void onBackPressed() {
        super.onBackPressed();
        if ("slide".equals(BundleUtils.getString(getIntent().getExtras(), "camera_to_gallery_mode"))) {
            overridePendingTransition(R.anim.slide_in_down, R.anim.slide_out_down);
        }
    }

    public boolean backAsHome() {
        return moveTaskToBack(false);
    }

    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        ArrayList<String> tags = new ArrayList();
        for (int i = 0; i < this.mFragmentArray.size(); i++) {
            tags.add(i, ((Fragment) this.mFragmentArray.get(i)).getTag());
        }
        outState.putStringArrayList("key-fragment-tags", tags);
    }

    protected void onStop() {
        super.onStop();
        enableBackgroudCover(false);
    }

    public void addPageChangedListener(OnPageChangedListener listener) {
        if (!this.mPageChangedListener.contains(listener)) {
            this.mPageChangedListener.add(listener);
        }
    }

    public void removePageChangedListener(OnPageChangedListener listener) {
        this.mPageChangedListener.remove(listener);
    }

    private void showCloudTipsDialog() {
        if (this.mCloudSwitchDialog == null) {
            this.mCloudSwitchDialog = createCloudSwitchDialog(this, this.mCloudListener);
        }
        this.mCloudSwitchDialog.setCancelable(false);
        this.mCloudSwitchDialog.show();
    }

    private AlertDialog createCloudSwitchDialog(Context context, OnClickListener listener) {
        return new Builder(context).setMessage(R.string.enable_hicloud_gallery_new).setPositiveButton(R.string.photoshare_btn_open_switch, listener).setNegativeButton(R.string.cancel, listener).create();
    }

    public void enableBackgroudCover(boolean visible) {
        if (this.mBackgroudView != null) {
            this.mBackgroudView.setVisibility(visible ? 0 : 8);
        }
    }

    public void onTabSelected(Tab tab, FragmentTransaction ft) {
        if (this.mHasInited && !this.mDisableTabSelection) {
            this.mIsTabClick = true;
            this.mViewPager.setCurrentItem(tab.getPosition());
            onPageChanged(tab.getPosition());
            GalleryLog.d("GalleryMain", "onTabClicked, set current item: " + tab.getPosition());
        }
    }

    public void onTabUnselected(Tab tab, FragmentTransaction ft) {
    }

    public void onTabReselected(Tab tab, FragmentTransaction ft) {
    }

    private void onPageChanged(int pageIndex) {
        for (OnPageChangedListener listener : this.mPageChangedListener) {
            if (listener != null) {
                listener.onPageChanged(pageIndex);
            }
        }
    }

    public void disableViewPagerScrool(boolean disabled) {
        this.mDisableViewPagerScrool = disabled;
    }

    public void disableTabSelection(boolean disabled) {
        this.mDisableTabSelection = disabled;
    }

    public boolean disableScroll() {
        if (getGalleryActionBar().getCurrentMode() instanceof TabMode) {
            return this.mDisableViewPagerScrool;
        }
        return true;
    }

    private void saveCurrentPageIndex() {
        int index = this.mViewPager.getCurrentItem();
        TabIndexUtils.saveIndex(this, index);
        GalleryLog.d("GalleryMain", "saveCurrentPageIndex: " + index);
    }

    public boolean forbidPickPhoto() {
        int currentItem = this.mViewPager.getCurrentItem();
        GalleryLog.d("GalleryMain", "forbidPickPhoto mViewPagerState:" + this.mViewPagerState + " currentItem:" + currentItem);
        if (this.mViewPagerState == 0 && currentItem == 0) {
            return false;
        }
        return true;
    }

    private void setUpActionBar() {
        getGalleryActionBar().enterTabMode(false).show();
    }

    private void setUpTabs() {
        ActionBarStateBase mode = getGalleryActionBar().getCurrentMode();
        if (mode instanceof TabMode) {
            for (int i = 0; i < this.mAdapter.getCount(); i++) {
                ((TabMode) mode).addTab(this.mAdapter.getPageTitle(i), this, this.mAdapter.getPageId(i));
            }
        }
    }

    private int getLastPageIndex() {
        int index = TabIndexUtils.getsTabIndex(this);
        GalleryLog.d("GalleryMain", "get index from SharedPreferences: " + index);
        int lastPageIndex = Utils.clamp(index, 0, 2);
        GalleryLog.d("GalleryMain", "lastPageIndex: " + lastPageIndex);
        return lastPageIndex;
    }
}
