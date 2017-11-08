package com.huawei.systemmanager.spacecleanner.ui.trashlistadapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.TextView;
import com.common.imageloader.core.DisplayImageOptions;
import com.common.imageloader.core.DisplayImageOptions.Builder;
import com.common.imageloader.core.ImageLoader;
import com.common.imageloader.core.listener.SimpleImageLoadingListener;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.comm.misc.GlobalContext;
import com.huawei.systemmanager.comm.misc.Utility;
import com.huawei.systemmanager.comm.widget.CommonAdapter;
import com.huawei.systemmanager.comm.widget.ViewUtil;
import com.huawei.systemmanager.spacecleanner.engine.hwadapter.HwAppDataTrash;
import com.huawei.systemmanager.spacecleanner.engine.hwadapter.HwUnusedAppTrash;
import com.huawei.systemmanager.spacecleanner.engine.trash.PreInstalledAppTrash;
import com.huawei.systemmanager.spacecleanner.ui.commonitem.ITrashItem;
import com.huawei.systemmanager.spacecleanner.ui.secondaryui.uiitem.ApkDataItem;
import com.huawei.systemmanager.spacecleanner.ui.secondaryui.uiitem.PreInstallAppTrashItem;
import com.huawei.systemmanager.spacecleanner.ui.secondaryui.uiitem.UnusedAppTrashItem;
import com.huawei.systemmanager.spacecleanner.view.FrameBorderImageView;

public class BaseTrashListAdapter extends CommonAdapter<ITrashItem> {
    private static final SimpleImageLoadingListener sLoadListener = new SimpleImageLoadingListener() {
        public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
            ImageView iconView = (ImageView) view;
            iconView.setScaleType(ScaleType.CENTER_CROP);
            iconView.setImageBitmap(loadedImage);
        }
    };
    private Drawable defaultIcon;
    public final OnClickListener mCheckClicker;
    private DisplayImageOptions options;

    private static class ViewHolder {
        final CheckBox checkBox;
        final TextView description;
        final FrameBorderImageView icon;
        final TextView summary;
        final TextView title;

        ViewHolder(FrameBorderImageView ic, TextView title, TextView des, TextView summary, CheckBox cb) {
            this.icon = ic;
            this.title = title;
            this.description = des;
            this.checkBox = cb;
            this.summary = summary;
        }
    }

    public BaseTrashListAdapter(OnClickListener clickListener, Context context) {
        if (context == null) {
            context = GlobalContext.getContext();
        }
        super(context);
        this.mCheckClicker = clickListener;
    }

    protected void bindView(int position, View view, ITrashItem item) {
        ViewHolder holder = (ViewHolder) view.getTag();
        holder.icon.setScaleType(ScaleType.FIT_CENTER);
        if (item.shouldLoadPic() && this.options != null) {
            String uri;
            switch (item.getTrashType()) {
                case 2:
                    uri = ((HwUnusedAppTrash) ((UnusedAppTrashItem) item).getTrash()).getPkgInfo().getPackageName();
                    break;
                case 262144:
                    uri = ((HwAppDataTrash) ((ApkDataItem) item).getTrash()).getPkgInfo().getPackageName();
                    break;
                case 524288:
                    uri = ((PreInstalledAppTrash) ((PreInstallAppTrashItem) item).getTrash()).getPkgInfo().getPackageName();
                    break;
                default:
                    uri = Utility.getLocalPath(item.getTrashPath());
                    break;
            }
            ImageLoader.getInstance().displayImage(uri, holder.icon, this.options, sLoadListener);
        } else if (this.defaultIcon != null) {
            holder.icon.setImageDrawable(this.defaultIcon);
        } else {
            holder.icon.setImageDrawable(item.getIcon(GlobalContext.getContext()));
        }
        if (item.getName() != null) {
            holder.title.setText(item.getName().trim());
        } else {
            holder.title.setText("");
        }
        holder.description.setText(item.getDescription(GlobalContext.getContext()));
        holder.checkBox.setTag(item);
        holder.checkBox.setChecked(item.isChecked());
        String summary = item.getSummary();
        if (TextUtils.isEmpty(summary)) {
            holder.summary.setVisibility(8);
            return;
        }
        holder.summary.setVisibility(0);
        holder.summary.setText(summary);
    }

    protected View newView(int position, ViewGroup parent, ITrashItem item) {
        View convertView = getInflater().inflate(R.layout.spaceclean_list_item_twolines_image_summary_checkbox, parent, false);
        FrameBorderImageView ic = (FrameBorderImageView) convertView.findViewById(R.id.image);
        TextView title = (TextView) convertView.findViewById(ViewUtil.HWID_TEXT_1);
        TextView des = (TextView) convertView.findViewById(ViewUtil.HWID_TEXT_2);
        TextView summary = (TextView) convertView.findViewById(R.id.list_item_summary);
        CheckBox cb = (CheckBox) convertView.findViewById(R.id.list_item_checkbox);
        if (item.isUseIconAlways() && this.defaultIcon == null) {
            this.defaultIcon = item.getIcon(GlobalContext.getContext());
        }
        if (item.shouldLoadPic() && this.options == null) {
            Builder cacheOnDisk;
            Drawable drawable;
            boolean sholudCacheOnDisk = false;
            if (item.getTrashType() == 128) {
                sholudCacheOnDisk = true;
            }
            boolean useApkDecoder = false;
            if (item.getTrashType() == 1024) {
                useApkDecoder = true;
            }
            boolean loadByPkgInfo = false;
            int trashType = item.getTrashType();
            if (!(trashType == 2 || trashType == 262144)) {
                if (trashType == 524288) {
                }
                cacheOnDisk = new Builder().useApkDecoder(useApkDecoder).loadDrawableByPkgInfo(loadByPkgInfo).cacheInMemory(true).cacheOnDisk(sholudCacheOnDisk);
                if (this.defaultIcon == null) {
                    drawable = this.defaultIcon;
                } else {
                    drawable = item.getIcon(GlobalContext.getContext());
                }
                this.options = cacheOnDisk.showImageOnLoading(drawable).considerExifParams(true).build();
            }
            loadByPkgInfo = true;
            cacheOnDisk = new Builder().useApkDecoder(useApkDecoder).loadDrawableByPkgInfo(loadByPkgInfo).cacheInMemory(true).cacheOnDisk(sholudCacheOnDisk);
            if (this.defaultIcon == null) {
                drawable = item.getIcon(GlobalContext.getContext());
            } else {
                drawable = this.defaultIcon;
            }
            this.options = cacheOnDisk.showImageOnLoading(drawable).considerExifParams(true).build();
        }
        if (item.getTrashType() == 256) {
            ic.setHasBorder(true);
        }
        if (item.shouldLoadPic()) {
            int width = item.getIconWidth(GlobalContext.getContext());
            int height = item.getIconHeight(GlobalContext.getContext());
            if (!(width == 0 || height == 0)) {
                LayoutParams param = ic.getLayoutParams();
                param.width = width;
                param.height = height;
            }
        }
        cb.setOnClickListener(this.mCheckClicker);
        convertView.setTag(new ViewHolder(ic, title, des, summary, cb));
        return convertView;
    }
}
