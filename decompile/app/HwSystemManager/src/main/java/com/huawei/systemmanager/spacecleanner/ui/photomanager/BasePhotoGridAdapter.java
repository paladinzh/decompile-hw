package com.huawei.systemmanager.spacecleanner.ui.photomanager;

import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.ImageView;
import com.common.imageloader.core.DisplayImageOptions;
import com.common.imageloader.core.DisplayImageOptions.Builder;
import com.common.imageloader.core.ImageLoader;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.comm.misc.GlobalContext;
import com.huawei.systemmanager.comm.misc.Utility;
import com.huawei.systemmanager.comm.widget.CommonAdapter;
import com.huawei.systemmanager.spacecleanner.ui.ListGrid.ListGridListener.OnSizeChangeListener;
import com.huawei.systemmanager.spacecleanner.ui.commonitem.ITrashItem;
import com.huawei.systemmanager.spacecleanner.view.SquareImageView;
import java.util.List;

public class BasePhotoGridAdapter extends CommonAdapter<ITrashItem> {
    private OnSizeChangeListener mSizeListener;
    private DisplayImageOptions options = new Builder().cacheInMemory(true).cacheOnDisk(true).considerExifParams(true).resetViewBeforeLoading(true).squareLayout(true).showImageOnFail(GlobalContext.getContext().getResources().getDrawable(R.drawable.ic_gallery_list_damage_phonemanager)).build();

    private static class ViewHolder {
        CheckBox checkbox;
        FrameLayout checkboxAera;
        SquareImageView gridPhotoItem;
        ImageView photoItem;

        private ViewHolder() {
        }
    }

    public BasePhotoGridAdapter() {
        super(GlobalContext.getContext());
    }

    protected View newView(int position, ViewGroup parent, ITrashItem item) {
        View convertView = this.mInflater.inflate(R.layout.grid_photo_item, parent, false);
        ViewHolder holder = new ViewHolder();
        holder.photoItem = (ImageView) convertView.findViewById(R.id.photo_item);
        holder.checkboxAera = (FrameLayout) convertView.findViewById(R.id.photo_check_area);
        holder.gridPhotoItem = (SquareImageView) convertView.findViewById(R.id.grid_photo_item_view);
        holder.checkboxAera.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                ((ITrashItem) view.getTag()).toggle();
                BasePhotoGridAdapter.this.notifyDataSetChanged();
                BasePhotoGridAdapter.this.refreshData();
            }
        });
        holder.checkbox = (CheckBox) convertView.findViewById(R.id.photo_item_checkbox);
        convertView.setTag(holder);
        return convertView;
    }

    protected void bindView(int position, View view, ITrashItem item) {
        ViewHolder holder = (ViewHolder) view.getTag();
        holder.checkboxAera.setTag(item);
        holder.checkbox.setChecked(item.isChecked());
        ImageLoader.getInstance().displayImage(Utility.getLocalPath(item.getTrashPath()), holder.photoItem, this.options, null);
        if (item.isChecked()) {
            if (holder.gridPhotoItem.getVisibility() != 0) {
                holder.gridPhotoItem.setVisibility(0);
            }
        } else if (holder.gridPhotoItem.getVisibility() == 0) {
            holder.gridPhotoItem.setVisibility(8);
        }
    }

    public void setSizeListener(OnSizeChangeListener l) {
        this.mSizeListener = l;
    }

    public void selectAll(boolean value) {
        for (ITrashItem item : this.mList) {
            item.setChecked(value);
        }
        notifyDataSetChanged();
        refreshData();
    }

    public void refreshData() {
        int count = 0;
        long size = 0;
        for (ITrashItem item : this.mList) {
            if (item.isChecked()) {
                count++;
                size += item.getTrashSize();
            }
        }
        if (this.mSizeListener != null) {
            boolean z;
            OnSizeChangeListener onSizeChangeListener = this.mSizeListener;
            if (count == this.mList.size()) {
                z = true;
            } else {
                z = false;
            }
            onSizeChangeListener.onSizeChanged(size, 0, z, count);
        }
    }

    public boolean swapData(List<? extends ITrashItem> list) {
        boolean value = super.swapData(list);
        refreshData();
        return value;
    }
}
