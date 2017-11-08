package com.huawei.watermark.ui.watermarklib;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.RelativeLayout;
import com.android.gallery3d.R;
import com.huawei.watermark.ui.WMComponent;
import com.huawei.watermark.ui.WMThumbImageView;
import com.huawei.watermark.wmdata.WMFileProcessor;
import com.huawei.watermark.wmdata.wmlistdata.basedata.WMSingleWatermarkData;
import com.huawei.watermark.wmutil.WMBaseUtil;
import com.huawei.watermark.wmutil.WMResourceUtil;
import com.huawei.watermark.wmutil.WMStringUtil;
import com.huawei.watermark.wmutil.WMUIUtil;
import com.huawei.watermark.wmutil.WMUtil;
import java.util.Vector;

public class WMLocalLibSingleCategoryGridAdapter extends BaseAdapter {
    private WMComponent mWMComponent;
    private Vector<WMSingleWatermarkData> mWMDataVec = new Vector();

    WMLocalLibSingleCategoryGridAdapter(WMComponent component, Vector<WMSingleWatermarkData> wmDataVec) {
        this.mWMComponent = component;
        this.mWMDataVec = wmDataVec;
    }

    public int getCount() {
        return this.mWMDataVec.size();
    }

    public Object getItem(int item) {
        return Integer.valueOf(item);
    }

    public long getItemId(int id) {
        return (long) id;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        RelativeLayout backgroundLayout;
        WMThumbImageView imageView;
        ImageView selectedImageView;
        if (convertView == null) {
            backgroundLayout = new RelativeLayout(this.mWMComponent.getContext());
            int gridCellWidth = (WMBaseUtil.getScreenWidth((Activity) this.mWMComponent.getContext()) * 3) / 10;
            int gridCellHeight = (gridCellWidth * 40) / 27;
            Log.d("WMLocLibSingCatGridApt", " gridCellWidth = " + gridCellWidth + " , gridCellHeight = " + gridCellHeight);
            backgroundLayout.setLayoutParams(new LayoutParams(gridCellWidth, gridCellHeight));
            imageView = new WMThumbImageView(this.mWMComponent.getContext());
            imageView.setTag("imageView");
            imageView.setLayoutParams(new LayoutParams(-1, -1));
            imageView.setAdjustViewBounds(false);
            imageView.setScaleType(ScaleType.FIT_XY);
            backgroundLayout.addView(imageView);
            selectedImageView = new ImageView(this.mWMComponent.getContext());
            selectedImageView.setTag("selectedImageView");
            selectedImageView.setLayoutParams(new LayoutParams(-1, -1));
            int color = WMUtil.getControlColor(backgroundLayout.getContext());
            if (color != 0) {
                Drawable drawable = backgroundLayout.getContext().getResources().getDrawable(WMResourceUtil.getDrawableId(this.mWMComponent.getContext(), "wm_jar_watermark_selected"));
                drawable.setTint(color);
                selectedImageView.setBackground(drawable);
            } else {
                selectedImageView.setBackgroundResource(WMResourceUtil.getDrawableId(this.mWMComponent.getContext(), "wm_jar_watermark_selected"));
            }
            backgroundLayout.addView(selectedImageView);
        } else {
            backgroundLayout = (RelativeLayout) convertView;
            imageView = (WMThumbImageView) WMUIUtil.getChildViewByTag(backgroundLayout, "imageView");
            selectedImageView = (ImageView) WMUIUtil.getChildViewByTag(backgroundLayout, "selectedImageView");
        }
        WMSingleWatermarkData singleWatermarkData = (WMSingleWatermarkData) this.mWMDataVec.elementAt(position);
        if (imageView == null || selectedImageView == null) {
            return backgroundLayout;
        }
        if (imageView.isRecycled()) {
            imageView.setImageBitmap(null);
            imageView.setWMImagePath(singleWatermarkData.getWMPath(), singleWatermarkData.getWMThumbnailFileName());
        }
        String selectedWMPath = WMFileProcessor.getInstance().getWmPath(WMFileProcessor.getInstance().getNowCategoryIndex(this.mWMComponent.getContext(), this.mWMComponent.getToken()), WMFileProcessor.getInstance().getNowWatermarkInCategoryIndex(this.mWMComponent.getContext(), this.mWMComponent.getToken()));
        if (WMStringUtil.isEmptyString(singleWatermarkData.getWMPath()) || WMStringUtil.isEmptyString(selectedWMPath) || !singleWatermarkData.getWMPath().equals(selectedWMPath)) {
            if (selectedImageView != null) {
                selectedImageView.setVisibility(4);
            }
        } else if (selectedImageView != null) {
            selectedImageView.setVisibility(0);
        }
        imageView.setContentDescription(this.mWMComponent.getContext().getResources().getString(R.string.accessubility_watermark_tapwatermarkprompt));
        return backgroundLayout;
    }
}
