package com.huawei.systemmanager.spacecleanner.ui.photomanager;

import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import com.common.imageloader.core.DisplayImageOptions;
import com.common.imageloader.core.DisplayImageOptions.Builder;
import com.common.imageloader.core.ImageLoader;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.comm.misc.GlobalContext;
import com.huawei.systemmanager.comm.misc.Utility;
import com.huawei.systemmanager.comm.widget.CommonAdapter;
import com.huawei.systemmanager.spacecleanner.ui.commonitem.ITrashItem;
import com.huawei.systemmanager.spacecleanner.ui.secondaryui.uiitem.FileTrashItem;
import com.huawei.systemmanager.spacecleanner.view.SquareImageView;
import com.huawei.systemmanager.util.HwLog;

public class PhotoGridSetAdapter extends CommonAdapter<ITrashItem> {
    private static final String TAG = "PhotoGridSetAdapter";
    private OnClickListener mCheckClicker;
    private DisplayImageOptions options = new Builder().cacheInMemory(true).cacheOnDisk(true).considerExifParams(true).squareLayout(true).resetViewBeforeLoading(true).showImageOnFail(GlobalContext.getContext().getResources().getDrawable(R.drawable.ic_gallery_list_damage_phonemanager)).build();

    static class ViewHolder {
        private CheckBox checkbox;
        private FrameLayout photoCheckArea;
        private SquareImageView photoItem;
        private SquareImageView photoItemView;

        ViewHolder(SquareImageView photoItem, SquareImageView photoItemView, CheckBox checkbox, FrameLayout photoCheckArea) {
            this.photoItem = photoItem;
            this.checkbox = checkbox;
            this.photoItemView = photoItemView;
            this.photoCheckArea = photoCheckArea;
        }
    }

    public PhotoGridSetAdapter(Context context) {
        super(context);
    }

    public void setCheckBoxClickListener(OnClickListener listener) {
        this.mCheckClicker = listener;
    }

    protected View newView(int position, ViewGroup parent, ITrashItem item) {
        View convertView = getInflater().inflate(R.layout.list_photo_item, parent, false);
        ViewHolder holder = new ViewHolder();
        holder.photoItem = (SquareImageView) convertView.findViewById(R.id.photo_item);
        holder.checkbox = (CheckBox) convertView.findViewById(R.id.photo_item_checkbox);
        holder.photoItemView = (SquareImageView) convertView.findViewById(R.id.photo_item_view);
        holder.photoCheckArea = (FrameLayout) convertView.findViewById(R.id.photo_check_area);
        holder.photoCheckArea.setOnClickListener(this.mCheckClicker);
        convertView.setTag(holder);
        return convertView;
    }

    protected void bindView(int position, View view, ITrashItem item) {
        ViewHolder holder = (ViewHolder) view.getTag();
        if (item instanceof FileTrashItem) {
            ImageLoader.getInstance().displayImage(Utility.getLocalPath(item.getTrashPath()), holder.photoItem, this.options, null);
        } else {
            HwLog.e(TAG, "item is not filetrashitem");
        }
        holder.checkbox.setChecked(item.isChecked());
        holder.photoCheckArea.setTag(item);
        if (item.isChecked()) {
            if (holder.photoItemView.getVisibility() != 0) {
                holder.photoItemView.setVisibility(0);
            }
        } else if (holder.photoItemView.getVisibility() == 0) {
            holder.photoItemView.setVisibility(8);
        }
    }
}
