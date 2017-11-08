package com.huawei.systemmanager.spacecleanner.ui.ListGrid;

import android.content.Context;
import android.view.View;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.comm.misc.Utility;
import com.huawei.systemmanager.spacecleanner.ui.ListGrid.ListGridListener.OnSizeChangeListener;
import com.huawei.systemmanager.spacecleanner.ui.commonitem.ITrashItem;

public class VideoListGridAdapter extends BaseListGridAdapter {
    private static final String TAG = "VideoListGridAdapter";

    protected View createContentView(View v) {
        if (v != null) {
            return v;
        }
        v = this.mLayoutInflater.inflate(R.layout.list_grid_video, null);
        ListGridItemAdapter adapter = new ListGridItemAdapter(this);
        adapter.initHolder(v);
        v.setTag(adapter);
        return v;
    }

    public VideoListGridAdapter(Context ct, OnSizeChangeListener l) {
        super(ct, l);
    }

    protected void custOnItemClick(View view) {
        Utility.startSimpleVideoView(this.mContext, ((ITrashItem) view.getTag()).getTrashPath());
    }
}
