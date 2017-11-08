package com.huawei.watermark.ui.watermarklib;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.huawei.watermark.ui.baseview.HorizontalListViewAdapter;
import com.huawei.watermark.wmdata.WMFileProcessor;
import com.huawei.watermark.wmutil.WMResourceUtil;
import com.huawei.watermark.wmutil.WMUtil;

public class WMLocalLibCategoryListViewAdapter extends HorizontalListViewAdapter {
    private WMCategoryListView mListView;

    private static class ViewHolder {
        private ImageView mImage;
        private TextView mTitle;

        private ViewHolder() {
        }
    }

    public WMLocalLibCategoryListViewAdapter(Context context) {
        super(context);
    }

    public void setListView(WMCategoryListView target) {
        this.mListView = target;
    }

    public int getCount() {
        return WMFileProcessor.getInstance().getTypeNameListCount();
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = this.mInflater.inflate(WMResourceUtil.getLayoutId(parent.getContext(), "wm_jar_category_horizontal_list_item"), null);
            holder.mImage = (ImageView) convertView.findViewById(WMResourceUtil.getId(parent.getContext(), "wm_category_img_list_item"));
            holder.mTitle = (TextView) convertView.findViewById(WMResourceUtil.getId(parent.getContext(), "wm_category_text_list_item"));
            WMUtil.setLKTypeFace(convertView.getContext(), holder.mTitle);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        if (position == this.selectIndex) {
            convertView.setSelected(true);
            if (this.mListView != null) {
                int itemWidth = WMResourceUtil.getDimensionPixelSize(parent.getContext(), "watermark_category_list_item_width");
                int scrollToLeft = this.mListView.getScrollToLeft();
                if ((this.selectIndex * itemWidth) - scrollToLeft < 0) {
                    this.mListView.scrollTo((this.selectIndex * itemWidth) - scrollToLeft);
                } else if (((this.selectIndex + 1) * itemWidth) - scrollToLeft > this.mListView.getWidth()) {
                    this.mListView.scrollTo((((this.selectIndex + 1) * itemWidth) - scrollToLeft) - this.mListView.getWidth());
                }
            }
        } else {
            convertView.setSelected(false);
        }
        String categoryShowName = WMFileProcessor.getInstance().getCategoryShowNamefromName(parent.getContext(), WMFileProcessor.getInstance().getTypeNameWithIndex(position));
        if (categoryShowName != null) {
            holder.mTitle.setText(categoryShowName);
            if (WMUtil.getControlColor(this.mContext) != 0) {
                ColorStateList colorStateList = WMUtil.getStateColorStateList(this.mContext, -1, 16842913);
                holder.mTitle.setTextColor(colorStateList);
                Drawable drawable = this.mContext.getResources().getDrawable(WMFileProcessor.getInstance().getCategoryIconIdFromName(holder.mImage.getContext(), WMFileProcessor.getInstance().getTypeNameWithIndex(position)));
                drawable.setTintList(colorStateList);
                holder.mImage.setImageDrawable(drawable);
            } else {
                holder.mImage.setImageResource(WMFileProcessor.getInstance().getCategoryIconIdFromName(holder.mImage.getContext(), WMFileProcessor.getInstance().getTypeNameWithIndex(position)));
            }
            convertView.setContentDescription(categoryShowName);
        }
        return convertView;
    }
}
