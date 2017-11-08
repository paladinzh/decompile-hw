package com.huawei.systemmanager.spacecleanner.ui.trashlistadapter;

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
import com.huawei.systemmanager.comm.widget.ViewUtil;
import com.huawei.systemmanager.spacecleanner.SpaceCleannerManager;
import com.huawei.systemmanager.spacecleanner.ui.commonitem.ITrashItem;
import com.huawei.systemmanager.spacecleanner.ui.deepscan.TopVideoTrashItem;
import com.huawei.systemmanager.spacecleanner.ui.secondaryui.uiitem.AppModelTrashItem;
import com.huawei.systemmanager.spacecleanner.ui.secondaryui.uiitem.LargeFileTrashItem;
import com.huawei.systemmanager.spacecleanner.ui.trashlistadapter.ChildType.ChildViewHolder;
import com.huawei.systemmanager.util.HwLog;

public class AppCacheChildTypeCheck extends ChildType {
    private static final String TAG = "AppCacheChildTypeCheck";
    private static final SimpleImageLoadingListener sLoadListener = new SimpleImageLoadingListener() {
        public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
            ImageView iconView = (ImageView) view;
            iconView.setScaleType(ScaleType.CENTER_CROP);
            iconView.setImageBitmap(loadedImage);
        }
    };
    private DisplayImageOptions mAppOption;
    private OnClickListener mCheckClicker;
    private OnClickListener mItemClicker;
    private OnLongClickListener mLongClicker;
    private DisplayImageOptions options;

    private static class ViewHolder extends ChildViewHolder {
        private final CheckBox checkbox;
        private final TextView size;
        private final TextView title;
        private final TextView trashDesc;
        private final ImageView trashIcon;

        public ViewHolder(ChildType type, View convertView) {
            super(type);
            this.title = (TextView) convertView.findViewById(ViewUtil.HWID_TEXT_1);
            this.size = (TextView) convertView.findViewById(ViewUtil.HWID_TEXT_2);
            this.trashDesc = (TextView) convertView.findViewById(R.id.list_item_summary);
            this.checkbox = (CheckBox) convertView.findViewById(R.id.list_item_checkbox);
            this.trashIcon = (ImageView) convertView.findViewById(R.id.image);
        }
    }

    public AppCacheChildTypeCheck(LayoutInflater inflater, OnClickListener checkClicker, OnClickListener itemClicker, OnLongClickListener longClicker) {
        super(inflater);
        this.mCheckClicker = checkClicker;
        this.mItemClicker = itemClicker;
        this.mLongClicker = longClicker;
        buildSyncLoadOption();
    }

    public View newView(int groupPosition, int childPosition, boolean isLastChild, ViewGroup parent, ITrashItem item) {
        View convertView = getInflater().inflate(R.layout.spaceclean_expand_list_item_twolines_image_summary_checkbox, parent, false);
        convertView.setTag(new ViewHolder(this, convertView));
        convertView.setOnClickListener(this.mItemClicker);
        convertView.setOnLongClickListener(this.mLongClicker);
        if (this.options == null) {
            this.options = new Builder().cacheInMemory(true).cacheOnDisk(false).showImageOnLoading(item.getItemIcon()).showImageOnFail(item.getItemIcon()).considerExifParams(true).build();
        }
        return convertView;
    }

    public void bindView(boolean isLastChild, View convertView, ITrashItem item) {
        ViewHolder holder = (ViewHolder) convertView.getTag();
        convertView.setTag(R.id.convertview_tag_item, item);
        if (item.getName() != null) {
            holder.title.setText(item.getName().trim());
        } else {
            holder.title.setText("");
        }
        bindIcon(holder.trashIcon, item);
        holder.checkbox.setOnClickListener(this.mCheckClicker);
        holder.checkbox.setChecked(item.isChecked());
        holder.checkbox.setTag(item);
        if (item instanceof LargeFileTrashItem) {
            if (((LargeFileTrashItem) item).isNotCommonlyUsed()) {
                SpaceCleannerManager.getInstance();
                if (SpaceCleannerManager.isSupportHwFileAnalysis()) {
                    holder.trashDesc.setVisibility(0);
                    holder.trashDesc.setText(GlobalContext.getContext().getResources().getQuantityString(R.plurals.spaceclean_not_commonly_used_data_tip, 30, new Object[]{Integer.valueOf(30)}));
                }
            }
            holder.trashDesc.setVisibility(8);
        } else if ((item instanceof TopVideoTrashItem) || (item instanceof AppModelTrashItem)) {
            holder.trashDesc.setVisibility(0);
            if (item.isSuggestClean()) {
                holder.trashDesc.setText(R.string.space_clean_suggest_desc);
            } else {
                holder.trashDesc.setText(R.string.space_clean_not_suggest_desc);
            }
        } else {
            holder.trashDesc.setVisibility(8);
        }
        holder.size.setText(item.getDescription(GlobalContext.getContext()));
    }

    private void bindIcon(ImageView imageView, ITrashItem trashItem) {
        if (trashItem.shouldLoadPic()) {
            if (trashItem instanceof IAppInfo) {
                ImageLoader.getInstance().displayImage(((IAppInfo) trashItem).getPackageName(), imageView, this.mAppOption, sLoadListener);
            }
            HwLog.e(TAG, "bind icon trashItem error! trash type:" + trashItem.getTrashType());
            return;
        }
        if (trashItem instanceof LargeFileTrashItem) {
            ImageLoader.getInstance().displayImage(Utility.getLocalPath(trashItem.getTrashPath()), imageView, this.options, sLoadListener);
        } else {
            imageView.setImageDrawable(trashItem.getItemIcon());
        }
    }

    private void buildSyncLoadOption() {
        this.mAppOption = new Builder().loadDrawableByPkgInfo(true).cacheInMemory(true).cacheOnDisk(false).showImageOnLoading((int) R.drawable.ic_storagecleaner_app).considerExifParams(true).build();
    }

    public int getType() {
        return 0;
    }
}
