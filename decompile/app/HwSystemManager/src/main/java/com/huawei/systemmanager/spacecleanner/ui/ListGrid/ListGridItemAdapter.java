package com.huawei.systemmanager.spacecleanner.ui.ListGrid;

import android.view.View;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.ImageView;
import com.common.imageloader.core.DisplayImageOptions;
import com.common.imageloader.core.DisplayImageOptions.Builder;
import com.common.imageloader.core.ImageLoader;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.comm.misc.Utility;
import com.huawei.systemmanager.netassistant.utils.ViewUtils;
import com.huawei.systemmanager.spacecleanner.ui.ListGrid.ListGridListener.OnClickListener;
import com.huawei.systemmanager.spacecleanner.ui.commonitem.ITrashItem;

public class ListGridItemAdapter {
    public static final int LIST_GRID_CONTENT_ITEMS = 4;
    private static DisplayImageOptions options = new Builder().cacheInMemory(true).cacheOnDisk(true).considerExifParams(true).resetViewBeforeLoading(true).showImageOnLoading((int) R.drawable.bg_empty_pic).build();
    private ViewHolder[] mHolders = new ViewHolder[4];
    private OnClickListener mListener;

    private static class ViewHolder {
        CheckBox mCheckBox;
        View mCheckBoxArea;
        FrameLayout mContainer;
        ImageView mPhoto;
        ImageView mShadow;

        private ViewHolder() {
        }
    }

    public ListGridItemAdapter(OnClickListener l) {
        this.mListener = l;
    }

    public void initHolder(View convertView) {
        int[] idArray = new int[]{R.id.image_item_0, R.id.image_item_1, R.id.image_item_2, R.id.image_item_3};
        for (int i = 0; i < 4; i++) {
            ViewHolder holder = new ViewHolder();
            holder.mContainer = (FrameLayout) convertView.findViewById(idArray[i]);
            holder.mPhoto = (ImageView) holder.mContainer.findViewById(R.id.photo);
            holder.mShadow = (ImageView) holder.mContainer.findViewById(R.id.shadow);
            holder.mCheckBox = (CheckBox) holder.mContainer.findViewById(R.id.checkbox);
            holder.mCheckBoxArea = holder.mContainer.findViewById(R.id.checkbox_area);
            holder.mCheckBoxArea.setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                    ListGridItemAdapter.this.mListener.onItemCheckBoxClick(view);
                }
            });
            holder.mPhoto.setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                    ListGridItemAdapter.this.mListener.onItemClick(view);
                }
            });
            this.mHolders[i] = holder;
        }
    }

    public void bindView(ListGridContentItem gridItem) {
        int i;
        int size = gridItem.getCount();
        for (i = 0; i < size; i++) {
            ITrashItem item = gridItem.getTrashItem(i);
            if (item != null) {
                this.mHolders[i].mContainer.setVisibility(0);
                ImageLoader.getInstance().displayImage(Utility.getLocalPath(item.getTrashPath()), this.mHolders[i].mPhoto, options, null);
                this.mHolders[i].mCheckBoxArea.setTag(item);
                this.mHolders[i].mPhoto.setTag(item);
                this.mHolders[i].mCheckBox.setChecked(item.isChecked());
                ViewUtils.setVisibility(this.mHolders[i].mShadow, item.isChecked());
            }
        }
        for (i = size; i < 4; i++) {
            this.mHolders[i].mContainer.setVisibility(4);
        }
    }
}
