package com.huawei.watermark.ui.watermarklib;

import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.RelativeLayout.LayoutParams;
import com.huawei.watermark.WatermarkDelegate.CategoryDataPreparedListener;
import com.huawei.watermark.report.HwWatermarkReporter;
import com.huawei.watermark.ui.WMComponent;
import com.huawei.watermark.ui.baseview.viewpager.WMViewPager.OnPageChangeListener;
import com.huawei.watermark.ui.watermarklib.WMLocalLibDataTransform.SingleCategoryWMData;
import com.huawei.watermark.ui.watermarklib.WMLocalLibDataTransform.WMLocalLibDataChangedListener;
import com.huawei.watermark.wmdata.WMFileProcessor;
import com.huawei.watermark.wmutil.WMBaseUtil;
import com.huawei.watermark.wmutil.WMResourceUtil;
import com.huawei.watermark.wmutil.WMUIUtil;
import java.util.Vector;

public class WMLocalLib implements WMLocalLibDataChangedListener {
    public static final int REFRESHCATEGORYDATA = 1;
    public static final int REFRESHWATERMARKDATA = 2;
    private static final String TAG = ("CAMERA3WATERMARK_" + WMLocalLib.class.getSimpleName());
    private WMLocalLibDotListViewAdapter hDotListViewAdapter;
    private WMLocalLibCategoryListViewAdapter hListViewAdapter;
    private CategoryDataPreparedListener mCategoryDataPreparedListener;
    private WMCategoryListView mWMCategoryListView;
    private WMComponent mWMComponent;
    private WMDotListView mWMDotListView;
    public WMLocalLibDataTransform mWMLocalLibDataTransform;
    private WMLocalLibPager mWMLocalLibPager;
    private WMLocalLibPagerAdapter mWMLocalLibPagerAdapter;
    private WMLocalLibBaseView mWatermarkBaseView;
    private ViewGroup mWatermarkMenuBaseView;

    public WMLocalLib(WMComponent temp) {
        this.mWMComponent = temp;
        int[] wh = WMBaseUtil.getScreenPixel((Activity) this.mWMComponent.getContext());
        this.mWMLocalLibDataTransform = new WMLocalLibDataTransform(this.mWMComponent.getContext(), Math.min(wh[0], wh[1]), Math.max(wh[0], wh[1]) - WMResourceUtil.getDimensionPixelSize(this.mWMComponent.getContext(), "watermark_locallibpage_viewpager_margintop"), this);
    }

    public void setWMLocalLibMenuBaseView(ViewGroup temp) {
        if (this.mWatermarkMenuBaseView == null) {
            this.mWatermarkMenuBaseView = temp;
        }
    }

    public void setWMLocalLibBaseView(WMLocalLibBaseView temp) {
        this.mWatermarkBaseView = temp;
    }

    public WMLocalLibBaseView getWMLocalLibBaseView() {
        return this.mWatermarkBaseView;
    }

    public void setWMCategoryListView(WMCategoryListView temp) {
        this.mWMCategoryListView = temp;
    }

    public WMCategoryListView getWMCategoryListView() {
        return this.mWMCategoryListView;
    }

    public void setWMDotListView(WMDotListView temp) {
        this.mWMDotListView = temp;
    }

    public WMDotListView getWMDotListView() {
        return this.mWMDotListView;
    }

    public void setWMLocalLibPager(WMLocalLibPager temp) {
        this.mWMLocalLibPager = temp;
    }

    public WMLocalLibPager getWMLocalLibPager() {
        return this.mWMLocalLibPager;
    }

    public void initView() {
        this.mWMLocalLibPagerAdapter = new WMLocalLibPagerAdapter(this.mWMComponent, this.mWMLocalLibDataTransform);
        if (this.mWMLocalLibPager != null) {
            this.mWMLocalLibPager.setAdapter(this.mWMLocalLibPagerAdapter);
            this.mWMLocalLibPager.setOnPageChangeListener(new OnPageChangeListener() {
                public void onPageScrolled(int i, float v, int i2) {
                }

                public void onPageSelected(int i) {
                    WMLocalLib.this.setSelectedStatusFromViewPager(i);
                }

                public void onPageScrollStateChanged(int i) {
                }
            });
            if (this.mWMLocalLibPager.getParent() == null || !(this.mWMLocalLibPager.getParent() instanceof WMLocalLibBaseView)) {
                WMUIUtil.separateView(this.mWMLocalLibPager);
                this.mWatermarkBaseView.addView(this.mWMLocalLibPager);
            }
            this.hDotListViewAdapter = new WMLocalLibDotListViewAdapter(this.mWMComponent.getContext());
            this.mWMDotListView.setAdapter(this.hDotListViewAdapter);
            if (this.mWMDotListView.getParent() == null || !(this.mWMDotListView.getParent() instanceof WMLocalLibBaseView)) {
                WMUIUtil.separateView(this.mWMDotListView);
                this.mWatermarkBaseView.addView(this.mWMDotListView);
            }
            this.hListViewAdapter = new WMLocalLibCategoryListViewAdapter(this.mWMComponent.getContext());
            if (this.mWMCategoryListView != null) {
                this.mWMCategoryListView.setAdapter(this.hListViewAdapter);
                this.hListViewAdapter.setListView(this.mWMCategoryListView);
                this.mWMCategoryListView.setOnItemClickListener(new OnItemClickListener() {
                    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                        WMLocalLib.this.setSelectedStatusFromCategoryButton(position);
                    }
                });
                this.mWMCategoryListView.showAlways(this.mWMComponent);
                Log.d(TAG, "[watermark] initView. ");
                refreshAllViewWithPosition(WMFileProcessor.getInstance().getNowCategoryIndex(this.mWMComponent.getContext(), this.mWMComponent.getToken()), WMFileProcessor.getInstance().getNowWatermarkInCategoryIndex(this.mWMComponent.getContext(), this.mWMComponent.getToken()));
            }
        }
    }

    private void refreshAllView(int viewpagerIndex, int categoryIndex) {
        if (!(!isShow() || this.mWMLocalLibPager == null || this.mWMLocalLibPager.getAdapter() == null)) {
            this.mWMLocalLibPager.getAdapter().notifyDataSetChanged();
            this.mWMLocalLibPager.setCurrentItem(viewpagerIndex);
        }
        refreshDotListView(viewpagerIndex);
        refreshHListView(categoryIndex);
    }

    private void selectedPositionChangedUpdateAllView(int viewpagerIndex, int categoryIndex) {
        if (!(this.mWMLocalLibPager == null || this.mWMLocalLibPager.getAdapter() == null)) {
            this.mWMLocalLibPager.setCurrentItem(viewpagerIndex);
        }
        refreshDotListView(viewpagerIndex);
        refreshHListView(categoryIndex);
    }

    private void setSelectedStatusFromViewPager(int viewpagerIndex) {
        selectedPositionChangedUpdateAllView(viewpagerIndex, this.mWMLocalLibDataTransform.getCategoryIndexWithViewpagerIndex(viewpagerIndex));
        HwWatermarkReporter.reportSlidingSelectWatermark(this.mWMComponent.getContext());
    }

    private void refreshAllViewWithPosition(int categoryIndex, int wmindex) {
        int viewpagerIndex = this.mWMLocalLibDataTransform.getViewpagerIndexWithCategoryIndexAndWMIndex(categoryIndex, wmindex);
        Log.d(TAG, "[watermark] viewpagerIndex = " + viewpagerIndex + ",  categoryIndex = " + categoryIndex);
        refreshAllView(viewpagerIndex, categoryIndex);
    }

    private void setSelectedStatusFromCategoryButton(int categoryIndex) {
        if (this.mWMCategoryListView != null) {
            if (this.mWMCategoryListView.getShowType() == 2 && !isShow()) {
                this.mWMComponent.showLocalLibMenu();
            }
            selectedPositionChangedUpdateAllView(this.mWMLocalLibDataTransform.getViewpagerIndexWithCategoryIndex(categoryIndex), categoryIndex);
            HwWatermarkReporter.reportWatermarkSelectCategory(this.mWMComponent.getContext(), WMFileProcessor.getInstance().getTypeNameWithIndex(categoryIndex));
        }
    }

    public void initOrientationChanged(int orientation, int type) {
        this.mWMLocalLibDataTransform.initOrientationChanged(orientation, type);
        if (this.mWMDotListView != null) {
            this.mWMDotListView.setOrientationType(type);
        }
        if (this.mWMCategoryListView != null) {
            this.mWMCategoryListView.setOrientationType(type);
        }
        onViewOrientationChanged(orientation);
    }

    public void onOrientationChanged(int orientation) {
        this.mWMLocalLibDataTransform.onOrientationChanged(orientation);
        onViewOrientationChanged(orientation);
    }

    private void onViewOrientationChanged(int orientation) {
        if (this.mWMLocalLibPager != null) {
            this.mWMLocalLibPager.onOrientationChanged(orientation);
        }
        if (this.mWatermarkBaseView != null) {
            this.mWatermarkBaseView.onOrientationChanged(orientation);
        }
        if (this.mWMDotListView != null) {
            this.mWMDotListView.onOrientationChanged(orientation);
        }
        if (this.mWMCategoryListView != null) {
            this.mWMCategoryListView.onOrientationChanged(orientation);
        }
    }

    public void refreshData(int type) {
        switch (type) {
            case 1:
                onCategoryDataInitFinished();
                return;
            case 2:
                this.mWMLocalLibDataTransform.refreshData();
                return;
            default:
                return;
        }
    }

    public void showView() {
        if (this.mWatermarkBaseView != null && this.mWMCategoryListView != null) {
            WMUIUtil.separateView(this.mWatermarkBaseView);
            if (this.mWatermarkMenuBaseView == null) {
                this.mWMComponent.addView(this.mWatermarkBaseView);
                this.mWMCategoryListView.showWhenMenuOpen(this.mWMComponent);
            } else {
                this.mWatermarkMenuBaseView.addView(this.mWatermarkBaseView);
                this.mWMCategoryListView.showWhenMenuOpen(this.mWatermarkMenuBaseView);
            }
            this.mWatermarkBaseView.sendAccessibilityEvent(32768);
            Log.d(TAG, "[watermark] showView. ");
            refreshAllViewWithPosition(WMFileProcessor.getInstance().getNowCategoryIndex(this.mWMComponent.getContext(), this.mWMComponent.getToken()), WMFileProcessor.getInstance().getNowWatermarkInCategoryIndex(this.mWMComponent.getContext(), this.mWMComponent.getToken()));
            if (this.mWMLocalLibPagerAdapter != null) {
                this.mWMLocalLibPagerAdapter.notifyDataSetChanged();
            }
        }
    }

    public void hideView() {
        if (this.mWatermarkBaseView != null && this.mWMCategoryListView != null) {
            if (this.mWatermarkMenuBaseView == null) {
                this.mWMComponent.removeView(this.mWatermarkBaseView);
            } else {
                this.mWatermarkMenuBaseView.removeView(this.mWatermarkBaseView);
            }
            WMUIUtil.recycleViewGroup(this.mWMLocalLibPager);
            this.mWMLocalLibPager.removeAllViews();
            if (this.mWatermarkMenuBaseView == null) {
                this.mWMCategoryListView.hideWhenMenuClose(this.mWMComponent);
            } else {
                this.mWMCategoryListView.hideWhenMenuClose(this.mWatermarkMenuBaseView);
            }
        }
    }

    public boolean isShow() {
        ViewParent viewParent;
        if (this.mWatermarkMenuBaseView == null) {
            if (this.mWatermarkBaseView != null) {
                viewParent = this.mWatermarkBaseView.getParent();
                if (viewParent != null && (viewParent instanceof WMComponent)) {
                    return true;
                }
            }
        } else if (this.mWatermarkBaseView != null) {
            viewParent = this.mWatermarkBaseView.getParent();
            if (viewParent != null && viewParent.equals(this.mWatermarkMenuBaseView)) {
                return true;
            }
        }
        return false;
    }

    private void refreshDotListView(int pageindex) {
        if (isShow() && this.hDotListViewAdapter != null && this.mWMDotListView != null && this.mWMDotListView.getAdapter() != null) {
            int[] pageinfo = this.mWMLocalLibDataTransform.getDotPositionAndCountWithViewpagerIndex(pageindex);
            ((LayoutParams) this.mWMDotListView.getLayoutParams()).width = WMBaseUtil.dpToPixel((float) (pageinfo[1] * 12), this.mWMDotListView.getContext());
            this.mWMDotListView.setSelection(pageinfo[0]);
            this.hDotListViewAdapter.setSelectIndex(pageinfo[0]);
            this.hDotListViewAdapter.setCount(pageinfo[1]);
            this.hDotListViewAdapter.notifyDataSetChanged();
        }
    }

    private void refreshHListView(int categoryindex) {
        if (this.mWMCategoryListView != null) {
            if ((this.mWMCategoryListView.getShowType() == 2 || (this.mWMCategoryListView.getShowType() == 1 && isShow())) && this.hListViewAdapter != null) {
                if (this.mWMCategoryListView != null) {
                    this.mWMCategoryListView.setSelection(categoryindex);
                }
                this.hListViewAdapter.setSelectIndex(categoryindex);
                this.hListViewAdapter.notifyDataSetChanged();
            }
        }
    }

    public void setCategoryDataPreparedListener(CategoryDataPreparedListener temp) {
        this.mCategoryDataPreparedListener = temp;
    }

    private void onCategoryDataInitFinished() {
        if (this.hListViewAdapter != null && this.hListViewAdapter.getCount() != 0 && this.mWMCategoryListView != null) {
            this.mWMCategoryListView.refreshListViewWhenCategoryDataPrepared(WMFileProcessor.getInstance().getNowCategoryIndex(this.mWMComponent.getContext(), this.mWMComponent.getToken()));
            if (this.mCategoryDataPreparedListener != null) {
                this.mCategoryDataPreparedListener.onCategoryDataPrepared();
            }
        }
    }

    private void releaseWaterMarkBaseView() {
        if (this.mWMLocalLibPager != null) {
            this.mWMLocalLibPager.removeAllViews();
            this.mWMLocalLibPager.setAdapter(null);
        }
        this.mWMLocalLibPagerAdapter = null;
        if (this.mWMDotListView != null) {
            this.mWMDotListView.removeAllViewsInLayout();
            this.mWMDotListView.setAdapter(null);
        }
        this.hDotListViewAdapter = null;
    }

    public void release() {
        releaseWaterMarkBaseView();
        if (this.mWMCategoryListView != null) {
            ViewParent viewParent = this.mWMCategoryListView.getParent();
            if (viewParent != null && (viewParent instanceof ViewGroup)) {
                this.mWMCategoryListView.hideAlways((ViewGroup) viewParent);
            }
            this.mWMCategoryListView.removeAllViewsInLayout();
            this.mWMCategoryListView.setAdapter(null);
        }
        this.hListViewAdapter = null;
    }

    public void onWMLocalLibDataChanged(Vector<SingleCategoryWMData> oldSinglePageWMDataList) {
        int categoryIndex;
        int watermarkInCategoryIndex;
        boolean z = true;
        if (isShow()) {
            int[] tempIndex = this.mWMLocalLibDataTransform.getCategoryIndexAndWMIndexFromViewpagerPosAndWMPos(this.mWMLocalLibPager.getCurrentItem(), 0, oldSinglePageWMDataList);
            categoryIndex = tempIndex[0];
            watermarkInCategoryIndex = tempIndex[1];
        } else {
            categoryIndex = WMFileProcessor.getInstance().getNowCategoryIndex(this.mWMComponent.getContext(), this.mWMComponent.getToken());
            watermarkInCategoryIndex = WMFileProcessor.getInstance().getNowWatermarkInCategoryIndex(this.mWMComponent.getContext(), this.mWMComponent.getToken());
        }
        final int[] pos = new int[]{categoryIndex, watermarkInCategoryIndex};
        String str = TAG;
        StringBuilder append = new StringBuilder().append("[watermark] onWMLocalLibDataChanged. mWatermarkBaseView != null ");
        if (this.mWatermarkBaseView == null) {
            z = false;
        }
        Log.d(str, append.append(z).append("  categoryIndex = ").append(categoryIndex).toString());
        if (this.mWatermarkBaseView != null && this.mWatermarkBaseView.isShown()) {
            this.mWatermarkBaseView.post(new Runnable() {
                public void run() {
                    Log.d(WMLocalLib.TAG, "[watermark] mWatermarkBaseView post run. ");
                    WMLocalLib.this.refreshAllViewWithPosition(pos[0], pos[1]);
                }
            });
        }
    }
}
