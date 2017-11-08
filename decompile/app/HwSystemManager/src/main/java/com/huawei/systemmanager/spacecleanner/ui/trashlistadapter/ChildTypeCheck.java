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
import com.common.imageloader.core.listener.SimpleImageLoadingListener;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.comm.component.IAppInfo;
import com.huawei.systemmanager.comm.misc.GlobalContext;
import com.huawei.systemmanager.comm.misc.Utility;
import com.huawei.systemmanager.spacecleanner.ui.commonitem.ITrashItem;
import com.huawei.systemmanager.spacecleanner.ui.normalscan.ApkFileTrashItem;
import com.huawei.systemmanager.spacecleanner.ui.normalscan.AppProcessTrashItem;
import com.huawei.systemmanager.spacecleanner.ui.trashlistadapter.ChildType.ChildViewHolder;
import com.huawei.systemmanager.util.HwLog;

public class ChildTypeCheck extends ChildType {
    private static final String TAG = "ChildTypeCheck";
    private static final SimpleImageLoadingListener sLoadListener = new SimpleImageLoadingListener() {
        public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
            ImageView iconView = (ImageView) view;
            iconView.setScaleType(ScaleType.CENTER_CROP);
            iconView.setImageBitmap(loadedImage);
        }
    };
    private DisplayImageOptions mApkFileOption;
    private DisplayImageOptions mAppOption;
    private OnClickListener mCheckClicker;
    private OnClickListener mItemClicker;
    private OnLongClickListener mLongClicker;

    private static class ViewHolder extends ChildViewHolder {
        private final CheckBox checkbox;
        private final TextView description;
        private final ImageView icon;
        private final ImageView image;
        private ImageView lockIcon;
        private final TextView size;
        private final TextView title;

        public ViewHolder(ChildType type, View convertView) {
            super(type);
            this.title = (TextView) convertView.findViewById(R.id.text1);
            this.description = (TextView) convertView.findViewById(R.id.list_item_summary);
            this.size = (TextView) convertView.findViewById(R.id.text2);
            this.checkbox = (CheckBox) convertView.findViewById(R.id.list_item_checkbox);
            this.image = (ImageView) convertView.findViewById(R.id.imageview);
            this.lockIcon = (ImageView) convertView.findViewById(R.id.lock_icon);
            this.icon = (ImageView) convertView.findViewById(R.id.image);
        }
    }

    public ChildTypeCheck(LayoutInflater inflater, OnClickListener checkClicker, OnClickListener itemClicker, OnLongClickListener longClicker) {
        super(inflater);
        this.mCheckClicker = checkClicker;
        this.mItemClicker = itemClicker;
        this.mLongClicker = longClicker;
        buildSyncLoadOption();
    }

    public View newView(int groupPosition, int childPosition, boolean isLastChild, ViewGroup parent, ITrashItem item) {
        View convertView = getInflater().inflate(R.layout.spaceclean_expand_list_item_twolines_tip_lock_image_summary_checkbox, parent, false);
        convertView.setTag(new ViewHolder(this, convertView));
        convertView.setOnClickListener(this.mItemClicker);
        convertView.setOnLongClickListener(this.mLongClicker);
        return convertView;
    }

    public void bindView(boolean isLastChild, View convertView, ITrashItem trashItem) {
        ViewHolder holder = (ViewHolder) convertView.getTag();
        convertView.setTag(R.id.convertview_tag_item, trashItem);
        holder.description.setText(trashItem.getDesctiption2());
        holder.title.setText(trashItem.getName());
        Context ctx = GlobalContext.getContext();
        bindIcon(holder.icon, trashItem);
        if (this.mState.isCleanEnd()) {
            holder.checkbox.setVisibility(8);
            holder.checkbox.setTag(null);
        } else {
            holder.checkbox.setOnClickListener(this.mCheckClicker);
            holder.checkbox.setVisibility(0);
            holder.checkbox.setChecked(trashItem.isChecked());
            holder.checkbox.setTag(trashItem);
        }
        if (trashItem.isCleaned()) {
            holder.image.setVisibility(0);
            holder.checkbox.setVisibility(8);
            holder.size.setText(R.string.space_cache_item_cleaned);
        } else {
            holder.image.setVisibility(8);
            holder.size.setText(trashItem.getDescription(ctx));
        }
        if ((trashItem instanceof AppProcessTrashItem) && ((AppProcessTrashItem) trashItem).isProtect()) {
            holder.lockIcon.setVisibility(0);
        } else {
            holder.lockIcon.setVisibility(4);
        }
    }

    private void bindIcon(ImageView imageView, ITrashItem trashItem) {
        if (!trashItem.shouldLoadPic()) {
            imageView.setImageDrawable(trashItem.getItemIcon());
        } else if (trashItem instanceof ApkFileTrashItem) {
            imageView.setImageDrawable(null);
            ImageLoader.getInstance().displayImage(Utility.getLocalPath(trashItem.getTrashPath()), imageView, this.mApkFileOption, sLoadListener);
        } else if (trashItem instanceof IAppInfo) {
            ImageLoader.getInstance().displayImage(((IAppInfo) trashItem).getPackageName(), imageView, this.mAppOption, sLoadListener);
        } else {
            imageView.setImageDrawable(null);
            HwLog.e(TAG, "bind icon trashItem error! trash type:" + trashItem.getTrashType());
        }
    }

    int getType() {
        return 0;
    }

    private void buildSyncLoadOption() {
        this.mApkFileOption = new Builder().useApkDecoder(true).loadDrawableByPkgInfo(false).cacheInMemory(true).cacheOnDisk(false).showImageOnLoading((int) R.drawable.ic_storagecleaner_apppackages).considerExifParams(true).build();
        this.mAppOption = new Builder().useApkDecoder(true).loadDrawableByPkgInfo(true).cacheInMemory(true).cacheOnDisk(false).showImageOnLoading((int) R.drawable.ic_storagecleaner_app).considerExifParams(true).build();
    }
}
