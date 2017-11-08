package com.huawei.systemmanager.spacecleanner.ui.photomanager;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView.ScaleType;
import android.widget.TextView;
import com.common.imageloader.core.DisplayImageOptions;
import com.common.imageloader.core.DisplayImageOptions.Builder;
import com.common.imageloader.core.ImageLoader;
import com.common.imageloader.core.listener.SimpleImageLoadingListener;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.comm.misc.Utility;
import com.huawei.systemmanager.spacecleanner.engine.trash.PhotoTrash;
import com.huawei.systemmanager.spacecleanner.view.MultiImageView;
import com.huawei.systemmanager.util.HwLog;
import java.util.ArrayList;
import java.util.List;

public class ListAlbumSetDataAdapter extends BaseAdapter {
    private static final String TAG = "ListAlbumSetDataAdapter";
    private static final SimpleImageLoadingListener sLoadListener = new SimpleImageLoadingListener() {
        public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
            MultiImageView iconView = (MultiImageView) view;
            iconView.setScaleType(ScaleType.FIT_XY);
            iconView.setRotation(0.0f);
            iconView.setImage(loadedImage);
            iconView.setStackBackground(loadedImage, loadedImage);
        }
    };
    private Context mContext;
    private LayoutInflater mInflater;
    private List<PhotoFolder> mPhotoFolders = new ArrayList();
    private DisplayImageOptions options;

    private static class ViewHolder {
        MultiImageView albumCover;
        TextView albumName;
        TextView photoCount;

        private ViewHolder() {
        }
    }

    public ListAlbumSetDataAdapter(Context context, List<PhotoFolder> photoFolders) {
        this.mContext = context;
        this.mPhotoFolders = photoFolders;
        this.mInflater = (LayoutInflater) this.mContext.getSystemService("layout_inflater");
        this.options = new Builder().cacheInMemory(true).cacheOnDisk(false).considerExifParams(true).showImageOnLoading((int) R.drawable.ic_storagecleaner_list_albumframe).build();
    }

    public int getCount() {
        return this.mPhotoFolders.size();
    }

    public Object getItem(int position) {
        return null;
    }

    public long getItemId(int position) {
        return (long) position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = this.mInflater.inflate(R.layout.list_albumset, parent, false);
        }
        setLayoutInfo(convertView, position);
        return convertView;
    }

    private final void setLayoutInfo(View convertView, int position) {
        ViewHolder viewHolder;
        if (convertView.getTag() == null) {
            viewHolder = new ViewHolder();
            viewHolder.albumCover = (MultiImageView) convertView.findViewById(R.id.album_cover_image);
            viewHolder.albumName = (TextView) convertView.findViewById(R.id.album_name);
            viewHolder.photoCount = (TextView) convertView.findViewById(R.id.photo_count);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        PhotoFolder entry = (PhotoFolder) this.mPhotoFolders.get(position);
        if (entry == null || entry.getTrashs() == null || entry.getTrashs().size() <= 0) {
            HwLog.e(TAG, "PhotoTrash is empty!");
            return;
        }
        int totalCount = entry.getTrashs().size();
        if (entry.getTrashs().get(0) instanceof PhotoTrash) {
            PhotoTrash firstPhoto = (PhotoTrash) entry.getTrashs().get(0);
            viewHolder.albumName.setText(entry.getFolderName());
            viewHolder.photoCount.setText(this.mContext.getResources().getQuantityString(R.plurals.photo_count, totalCount, new Object[]{Integer.valueOf(totalCount)}));
            if (totalCount > 0 && firstPhoto != null) {
                ImageLoader.getInstance().displayImage(Utility.getLocalPath(firstPhoto.getPath()), viewHolder.albumCover, this.options, sLoadListener);
            }
            return;
        }
        HwLog.e(TAG, "Trash is not PhotoTrash");
    }
}
