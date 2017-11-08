package com.huawei.watermark.ui.watermarklib;

import android.app.Activity;
import android.content.Context;
import android.os.Parcelable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.RelativeLayout.LayoutParams;
import com.huawei.watermark.ui.WMComponent;
import com.huawei.watermark.ui.baseview.viewpager.WMBasePagerAdapter;
import com.huawei.watermark.ui.watermarklib.WMLocalLibDataTransform.SingleCategoryWMData;
import com.huawei.watermark.wmutil.WMBaseUtil;
import com.huawei.watermark.wmutil.WMResourceUtil;
import com.huawei.watermark.wmutil.WMUIUtil;

public class WMLocalLibPagerAdapter extends WMBasePagerAdapter {
    private Context mContext;
    private LayoutInflater mInflater;
    private WMComponent mWMComponent;
    private WMLocalLibDataTransform mWMLocalLibDataTransform = null;

    public WMLocalLibPagerAdapter(WMComponent temp, WMLocalLibDataTransform param2) {
        this.mWMComponent = temp;
        this.mContext = this.mWMComponent.getContext();
        this.mInflater = (LayoutInflater) this.mContext.getSystemService("layout_inflater");
        this.mWMLocalLibDataTransform = param2;
    }

    public int getCount() {
        return this.mWMLocalLibDataTransform.mSinglePageWMDataList.size();
    }

    public boolean isViewFromObject(View arg0, Object arg1) {
        return arg0 == arg1;
    }

    public int getItemPosition(Object object) {
        return -2;
    }

    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((ViewGroup) object);
        WMUIUtil.recycleViewGroup((ViewGroup) object);
    }

    public Object instantiateItem(ViewGroup container, final int position) {
        View view = this.mInflater.inflate(WMResourceUtil.getLayoutId(container.getContext(), "wm_jar_locallib_single_category_gridview"), null);
        GridView gv = (GridView) view.findViewById(WMResourceUtil.getId(container.getContext(), "wm_locallib_single_category_cell_gridview"));
        LayoutParams rl = (LayoutParams) gv.getLayoutParams();
        int columnNum = this.mWMLocalLibDataTransform.getColumnNum();
        int screenWidth = WMBaseUtil.getScreenWidth((Activity) this.mContext);
        int gridCellWidth = (screenWidth * 3) / 10;
        int gridspace = screenWidth / 45;
        rl.width = (columnNum * gridCellWidth) + ((columnNum - 1) * gridspace);
        gv.setLayoutParams(rl);
        WMLocalLibSingleCategoryGridAdapter gridadapter = new WMLocalLibSingleCategoryGridAdapter(this.mWMComponent, ((SingleCategoryWMData) this.mWMLocalLibDataTransform.mSinglePageWMDataList.elementAt(position)).categoryWMData);
        gv.setColumnWidth(gridCellWidth);
        gv.setAdapter(gridadapter);
        gv.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> adapterView, View v, int pos, long id) {
                WMLocalLibPagerAdapter.this.mWMLocalLibDataTransform.setCategorySelectStatus(WMLocalLibPagerAdapter.this.mContext, position, pos, WMLocalLibPagerAdapter.this.mWMComponent.getToken());
                WMLocalLibPagerAdapter.this.mWMComponent.hideLocalLibMenu(true);
            }
        });
        gv.setVerticalSpacing(gridspace);
        gv.setHorizontalSpacing(gridspace);
        Log.d("WMLocalLibPagerAdapter", " gridCellWidth = " + gridCellWidth + " , gridspace = " + gridspace);
        container.addView(view, 0);
        return view;
    }

    public Parcelable saveState() {
        return null;
    }
}
