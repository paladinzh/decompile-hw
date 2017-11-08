package com.huawei.watermark.ui.watermarklib;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.util.TypedValue;
import com.android.gallery3d.R;
import com.huawei.watermark.wmdata.WMFileProcessor;
import com.huawei.watermark.wmdata.wmlistdata.basedata.WMSingleTypeData;
import com.huawei.watermark.wmdata.wmlistdata.basedata.WMSingleWatermarkData;
import com.huawei.watermark.wmutil.WMBaseUtil;
import com.huawei.watermark.wmutil.WMCollectionUtil;
import java.util.Vector;

public class WMLocalLibDataTransform {
    private Context context;
    private int mColumnNum;
    private int mRowNum;
    public Vector<Integer> mSingleCategoryCount = new Vector();
    private int mSinglePageMaxWMNum = this.singlepageportraitmaxwmnum;
    public Vector<SingleCategoryWMData> mSinglePageWMDataList = new Vector();
    private WMLocalLibDataChangedListener mWMLocalLibDataChangedListener;
    private int singlepagelandscapemaxwmnum = 3;
    private int singlepageportraitmaxwmnum = 6;

    public interface WMLocalLibDataChangedListener {
        void onWMLocalLibDataChanged(Vector<SingleCategoryWMData> vector);
    }

    public static class SingleCategoryWMData {
        String categoryName;
        Vector<WMSingleWatermarkData> categoryWMData = new Vector();
    }

    public WMLocalLibDataTransform(Context context, int parentWidth, int parentHeight, WMLocalLibDataChangedListener wmLocalLibDataChangedListener) {
        this.context = context;
        this.mWMLocalLibDataChangedListener = wmLocalLibDataChangedListener;
        int[] rowAndColumn = getSingleViewPagerCellCount(context, parentWidth, parentHeight);
        this.mRowNum = rowAndColumn[0];
        this.mColumnNum = rowAndColumn[1];
        countSinglePagePortraitMaxWmNum(context, R.dimen.single_pager_max_num_params);
    }

    private void countSinglePagePortraitMaxWmNum(Context context, int singlePageMaxNum) {
        this.singlepageportraitmaxwmnum = this.mRowNum * this.mColumnNum;
        TypedValue outValue = new TypedValue();
        context.getResources().getValue(singlePageMaxNum, outValue, true);
        this.singlepagelandscapemaxwmnum = (int) (((float) this.singlepageportraitmaxwmnum) / outValue.getFloat());
    }

    public int[] getSingleViewPagerCellCount(Context context, int parentWidth, int parentHeight) {
        int cell_w_px = (WMBaseUtil.getScreenWidth((Activity) context) * 3) / 10;
        int cell_h_px = (cell_w_px * 40) / 27;
        Log.d("WMLocalLibDataTransform", " grid_cell_width = " + cell_w_px + " , grid_cell_height = " + cell_h_px);
        int row_temp = parentHeight / cell_h_px;
        int column_temp = parentWidth / cell_w_px;
        int row = 2 > row_temp ? row_temp : 2;
        int column = 3 > column_temp ? column_temp : 3;
        return new int[]{row, column};
    }

    public int getColumnNum() {
        return this.mColumnNum;
    }

    public void initOrientationChanged(int orientation, int type) {
        if (WMBaseUtil.containType(type, 1)) {
            countSinglePagePortraitMaxWmNum(this.context, R.dimen.single_pager_max_num_params_camera);
        }
        this.mSinglePageMaxWMNum = getOrientationStatus(orientation);
    }

    public void onOrientationChanged(int orientation) {
        this.mSinglePageMaxWMNum = getOrientationStatus(orientation);
        refreshData();
    }

    public int getOrientationStatus(int orientation) {
        if (orientation == 0 || orientation == 180) {
            return this.singlepageportraitmaxwmnum;
        }
        return this.singlepagelandscapemaxwmnum;
    }

    public void refreshData() {
        Vector<SingleCategoryWMData> oldSinglePageWMDataList = WMCollectionUtil.copyVector(this.mSinglePageWMDataList);
        this.mSinglePageWMDataList.clear();
        this.mSingleCategoryCount.clear();
        for (int i = 0; i < WMFileProcessor.getInstance().getTypeNameListCount(); i++) {
            String categoryname = WMFileProcessor.getInstance().getTypeNameWithIndex(i);
            WMSingleTypeData wmsingletypedata = WMFileProcessor.getInstance().getSingleTypeDataFromName(categoryname);
            if (wmsingletypedata != null) {
                int count = 0;
                Object sc = null;
                for (int j = 0; j < wmsingletypedata.wmDataVec.size(); j++) {
                    if (j % this.mSinglePageMaxWMNum == 0) {
                        if (sc != null) {
                            this.mSinglePageWMDataList.add(sc);
                            count++;
                        }
                        sc = new SingleCategoryWMData();
                        sc.categoryName = categoryname;
                    }
                    if (sc != null) {
                        sc.categoryWMData.add((WMSingleWatermarkData) wmsingletypedata.wmDataVec.elementAt(j));
                    }
                }
                this.mSinglePageWMDataList.add(sc);
                this.mSingleCategoryCount.add(Integer.valueOf(count + 1));
            }
        }
        this.mWMLocalLibDataChangedListener.onWMLocalLibDataChanged(oldSinglePageWMDataList);
    }

    public void setCategorySelectStatus(Context mContext, int pageindex, int gridviewindex, String key) {
        int[] pos = getCategoryIndexAndWMIndexFromViewpagerPosAndWMPos(pageindex, gridviewindex, this.mSinglePageWMDataList);
        WMFileProcessor.getInstance().setNowCategoryIndex(mContext, key, pos[0]);
        WMFileProcessor.getInstance().setNowWatermarkInCategoryIndex(mContext, key, pos[1]);
    }

    public int[] getCategoryIndexAndWMIndexFromViewpagerPosAndWMPos(int pageindex, int gridviewindex, Vector<SingleCategoryWMData> singlePageWMDataList) {
        Vector<String> nametemp = new Vector();
        int watermarkincategoryindextemp = 0;
        for (int i = 0; i < singlePageWMDataList.size(); i++) {
            SingleCategoryWMData sc = (SingleCategoryWMData) singlePageWMDataList.elementAt(i);
            if (!nametemp.contains(sc.categoryName)) {
                nametemp.add(sc.categoryName);
                watermarkincategoryindextemp = 0;
            }
            if (i == pageindex) {
                break;
            }
            watermarkincategoryindextemp += sc.categoryWMData.size();
        }
        return new int[]{nametemp.size() - 1, watermarkincategoryindextemp + gridviewindex};
    }

    public int getCategoryIndexWithViewpagerIndex(int pageindex) {
        Vector<String> nametemp = new Vector();
        for (int i = 0; i < this.mSinglePageWMDataList.size(); i++) {
            SingleCategoryWMData sc = (SingleCategoryWMData) this.mSinglePageWMDataList.elementAt(i);
            if (!nametemp.contains(sc.categoryName)) {
                nametemp.add(sc.categoryName);
            }
            if (i == pageindex) {
                break;
            }
        }
        int res = nametemp.size() - 1;
        nametemp.clear();
        return res;
    }

    public int getViewpagerIndexWithCategoryIndexAndWMIndex(int categoryindex, int wmindex) {
        return getViewpagerIndexWithCategoryIndex(categoryindex) + (wmindex / this.mSinglePageMaxWMNum);
    }

    public int getViewpagerIndexWithCategoryIndex(int index) {
        Vector<String> nametemp = new Vector();
        int i = 0;
        while (i < this.mSinglePageWMDataList.size()) {
            SingleCategoryWMData sc = (SingleCategoryWMData) this.mSinglePageWMDataList.elementAt(i);
            if (!nametemp.contains(sc.categoryName)) {
                nametemp.add(sc.categoryName);
            }
            if (nametemp.size() - 1 == index) {
                break;
            }
            i++;
        }
        nametemp.clear();
        return i;
    }

    public int[] getDotPositionAndCountWithViewpagerIndex(int pageindex) {
        int[] res = new int[2];
        int count = 0;
        for (int i = 0; i < this.mSingleCategoryCount.size(); i++) {
            count = ((Integer) this.mSingleCategoryCount.elementAt(i)).intValue();
            pageindex -= count;
            if (pageindex < 0) {
                break;
            }
        }
        res[0] = pageindex + count;
        res[1] = count;
        if (res[0] > res[1] - 1) {
            res[0] = res[1] - 1;
        }
        if (res[0] < 0) {
            res[0] = 0;
        }
        if (res[1] <= 1) {
            res[0] = 0;
            res[1] = 0;
        }
        return res;
    }
}
