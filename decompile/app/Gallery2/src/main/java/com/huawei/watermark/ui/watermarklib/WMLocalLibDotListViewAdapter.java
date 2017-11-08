package com.huawei.watermark.ui.watermarklib;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import com.huawei.watermark.ui.baseview.HorizontalListViewAdapter;
import com.huawei.watermark.wmutil.WMResourceUtil;

public class WMLocalLibDotListViewAdapter extends HorizontalListViewAdapter {
    public static final int SINGLEDOTWIDTH = 12;
    public int mCount = 0;

    private static class ViewHolder {
        private ImageView mImage;

        private ViewHolder() {
        }
    }

    public WMLocalLibDotListViewAdapter(Context context) {
        super(context);
    }

    public void setCount(int count) {
        this.mCount = count;
    }

    public int getCount() {
        return this.mCount;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = this.mInflater.inflate(WMResourceUtil.getLayoutId(parent.getContext(), "wm_jar_category_horizontal_dot_list_item"), null);
            holder.mImage = (ImageView) convertView.findViewById(WMResourceUtil.getId(parent.getContext(), "wm_category_img_list_item"));
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        if (position == this.selectIndex) {
            holder.mImage.setEnabled(true);
        } else {
            holder.mImage.setEnabled(false);
        }
        return convertView;
    }
}
