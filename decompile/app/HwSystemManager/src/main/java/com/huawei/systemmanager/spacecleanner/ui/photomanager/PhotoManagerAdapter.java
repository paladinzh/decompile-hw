package com.huawei.systemmanager.spacecleanner.ui.photomanager;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.common.imageloader.core.DisplayImageOptions;
import com.common.imageloader.core.DisplayImageOptions.Builder;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.comm.misc.GlobalContext;
import com.huawei.systemmanager.comm.widget.CommonAdapter;
import com.huawei.systemmanager.comm.widget.ViewUtil;
import com.huawei.systemmanager.util.HwLog;

public class PhotoManagerAdapter extends CommonAdapter<PhotoManagerBean> {
    private static final String TAG = "PhotoManagerAdapter";
    private DisplayImageOptions options = new Builder().cacheInMemory(true).cacheOnDisk(true).considerExifParams(true).resetViewBeforeLoading(true).build();

    private static class ViewHolder {
        ImageView albumCover;
        TextView albumName;
        TextView photoCount;

        private ViewHolder() {
        }
    }

    public PhotoManagerAdapter() {
        super(GlobalContext.getContext());
        HwLog.d(TAG, "options is never used" + this.options);
    }

    protected View newView(int position, ViewGroup parent, PhotoManagerBean item) {
        View convertView = this.mInflater.inflate(R.layout.common_list_item_twolines_image_arrow, parent, false);
        ViewHolder viewHolder = new ViewHolder();
        viewHolder.albumCover = (ImageView) convertView.findViewById(R.id.image);
        viewHolder.albumName = (TextView) convertView.findViewById(ViewUtil.HWID_TEXT_1);
        viewHolder.photoCount = (TextView) convertView.findViewById(ViewUtil.HWID_TEXT_2);
        convertView.setTag(viewHolder);
        return convertView;
    }

    protected void bindView(int position, View view, PhotoManagerBean bean) {
        ViewHolder viewHolder = (ViewHolder) view.getTag();
        viewHolder.albumName.setText(bean.mNameId);
        viewHolder.photoCount.setText(bean.mSizeDes);
        viewHolder.albumCover.setBackgroundResource(bean.mIconID);
    }
}
