package com.huawei.systemmanager.spacecleanner.ui.trashlistadapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.TextView;
import com.common.imageloader.core.DisplayImageOptions;
import com.common.imageloader.core.DisplayImageOptions.Builder;
import com.common.imageloader.core.ImageLoader;
import com.common.imageloader.core.assist.FailReason;
import com.common.imageloader.core.listener.SimpleImageLoadingListener;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.comm.misc.GlobalContext;
import com.huawei.systemmanager.comm.misc.Utility;
import com.huawei.systemmanager.comm.widget.ViewUtil;
import com.huawei.systemmanager.spacecleanner.ui.commonitem.ITrashItem;
import com.huawei.systemmanager.spacecleanner.ui.trashlistadapter.ChildType.ChildViewHolder;
import com.huawei.systemmanager.util.HwLog;

public class ApkChildTypeCheck extends ChildType {
    private static final String TAG = "ApkChildTypeCheck";
    private static final SimpleImageLoadingListener sLoadListener = new SimpleImageLoadingListener() {
        public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
            if (view == null) {
                HwLog.e(ApkChildTypeCheck.TAG, "onLoadingFailed,view is null!");
                return;
            }
            ImageView iconView = (ImageView) view;
            ITrashItem item = (ITrashItem) view.getTag();
            if (item == null || item.getItemIcon() == null) {
                HwLog.e(ApkChildTypeCheck.TAG, "item's default icon is null.it is wrong!");
            } else {
                iconView.setImageDrawable(item.getItemIcon());
            }
        }

        public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
            if (view == null) {
                HwLog.e(ApkChildTypeCheck.TAG, "onLoadingComplete,view is null!");
                return;
            }
            ImageView iconView = (ImageView) view;
            iconView.setScaleType(ScaleType.CENTER_CROP);
            iconView.setImageBitmap(loadedImage);
        }
    };
    private DisplayImageOptions mApkFileOption;
    private OnClickListener mCheckClicker;
    private OnClickListener mItemClicker;
    private OnLongClickListener mLongClicker;

    private static class ViewHolder extends ChildViewHolder {
        private final CheckBox checkbox;
        private final TextView label;
        private final TextView size;
        private final TextView title;
        private final ImageView trashIcon;

        public ViewHolder(ChildType type, View convertView) {
            super(type);
            this.title = (TextView) convertView.findViewById(ViewUtil.HWID_TEXT_1);
            this.size = (TextView) convertView.findViewById(ViewUtil.HWID_TEXT_2);
            this.label = (TextView) convertView.findViewById(R.id.list_item_summary);
            this.checkbox = (CheckBox) convertView.findViewById(R.id.list_item_checkbox);
            this.trashIcon = (ImageView) convertView.findViewById(R.id.image);
        }
    }

    public ApkChildTypeCheck(LayoutInflater inflater, OnClickListener checkClicker, OnClickListener itemClicker, OnLongClickListener longClicker) {
        super(inflater);
        this.mCheckClicker = checkClicker;
        this.mItemClicker = itemClicker;
        this.mLongClicker = longClicker;
    }

    public View newView(int groupPosition, int childPosition, boolean isLastChild, ViewGroup parent, ITrashItem item) {
        View convertView = getInflater().inflate(R.layout.spaceclean_expand_list_item_twolines_image_summary_checkbox, parent, false);
        convertView.setTag(new ViewHolder(this, convertView));
        convertView.setOnClickListener(this.mItemClicker);
        convertView.setOnLongClickListener(this.mLongClicker);
        if (this.mApkFileOption == null) {
            this.mApkFileOption = new Builder().useApkDecoder(true).loadDrawableByPkgInfo(false).cacheInMemory(true).cacheOnDisk(false).showImageOnLoading((int) R.drawable.ic_storagecleaner_apppackages).considerExifParams(true).build();
        }
        return convertView;
    }

    public void bindView(boolean isLastChild, View convertView, ITrashItem item) {
        ViewHolder holder = (ViewHolder) convertView.getTag();
        convertView.setTag(R.id.convertview_tag_item, item);
        holder.title.setText(item.getName());
        holder.trashIcon.setScaleType(ScaleType.FIT_CENTER);
        Context ctx = GlobalContext.getContext();
        holder.trashIcon.setImageBitmap(null);
        holder.trashIcon.setTag(item);
        if (this.mApkFileOption != null) {
            ImageLoader.getInstance().displayImage(Utility.getLocalPath(item.getTrashPath()), holder.trashIcon, this.mApkFileOption, sLoadListener);
        }
        holder.checkbox.setOnClickListener(this.mCheckClicker);
        holder.checkbox.setChecked(item.isChecked());
        holder.checkbox.setTag(item);
        holder.size.setText(item.getDescription(ctx));
        holder.label.setText(item.getDesctiption2());
    }

    public int getType() {
        return 5;
    }
}
