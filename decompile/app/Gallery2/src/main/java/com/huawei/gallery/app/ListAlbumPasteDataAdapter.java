package com.huawei.gallery.app;

import android.app.Activity;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.TextView;
import com.android.gallery3d.R;
import com.android.gallery3d.data.MediaItem;
import com.android.gallery3d.data.MediaSet;
import com.android.gallery3d.ui.MultiImageView;
import com.android.gallery3d.util.GalleryLog;
import com.huawei.gallery.ui.AlbumSetSlidingWindow;
import com.huawei.gallery.ui.AlbumSetSlidingWindow.AlbumSetEntry;
import com.huawei.gallery.ui.AlbumSetSlidingWindowListener;

public class ListAlbumPasteDataAdapter extends ListAlbumBaseDataAdapter {

    static class ViewHolder {
        private MultiImageView albumCover;
        private TextView albumName;
        private ImageView frameOverlayIcon;
        private ImageView frameOverlayMask;
        private TextView photoCount;
    }

    public ListAlbumPasteDataAdapter(Activity context, AlbumSetDataLoader source) {
        this.mContext = context;
        this.mSource = source;
        this.mInflater = (LayoutInflater) this.mContext.getSystemService("layout_inflater");
        this.mDataWindow = new AlbumSetSlidingWindow(context, source, 32);
        this.mDataWindow.setListener(new AlbumSetSlidingWindowListener(this));
    }

    protected int getListViewLayoutId() {
        return R.layout.list_paste_albumset;
    }

    protected void setLayoutInfo(View convertView, int position) {
        ViewHolder viewHolder;
        if (convertView.getTag() == null) {
            viewHolder = new ViewHolder();
            initViewHolder(convertView, viewHolder);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        MediaSet mediaSet = this.mSource.getMediaSet(position);
        if (mediaSet != null) {
            MediaItem[] coverItems = this.mSource.getCoverItem(position);
            int totalCount = this.mSource.getTotalCount(position);
            int iconVisibility = 8;
            if (totalCount == 0) {
                viewHolder.albumCover.resetToEmpty();
                viewHolder.photoCount.setVisibility(0);
                viewHolder.albumName.setText(mediaSet.getName());
                if (mediaSet.isSdcardIconNeedShow()) {
                    iconVisibility = 0;
                    viewHolder.frameOverlayIcon.setImageResource(R.drawable.ic_gallery_frame_overlay_sd);
                }
                viewHolder.frameOverlayIcon.setVisibility(iconVisibility);
                viewHolder.frameOverlayMask.setVisibility(iconVisibility);
                viewHolder.photoCount.setText(this.mContext.getResources().getQuantityString(R.plurals.photo_count, totalCount, new Object[]{Integer.valueOf(totalCount)}));
            } else {
                String mediaCount;
                viewHolder.albumCover.setBackground(0);
                viewHolder.photoCount.setVisibility(0);
                viewHolder.albumName.setText(mediaSet.getName());
                if (mediaSet.isSdcardIconNeedShow()) {
                    iconVisibility = 0;
                    viewHolder.frameOverlayIcon.setImageResource(R.drawable.ic_gallery_frame_overlay_sd);
                }
                viewHolder.frameOverlayIcon.setVisibility(iconVisibility);
                viewHolder.frameOverlayMask.setVisibility(iconVisibility);
                int totalVideoCount = this.mSource.getTotalVideoCount(position);
                int totalImageCount = totalCount - totalVideoCount;
                if (totalImageCount == 0 && totalVideoCount > 0) {
                    mediaCount = this.mContext.getResources().getQuantityString(R.plurals.video_count, totalVideoCount, new Object[]{Integer.valueOf(totalVideoCount)});
                } else if (totalImageCount <= 0 || totalVideoCount <= 0) {
                    mediaCount = this.mContext.getResources().getQuantityString(R.plurals.photo_count, totalCount, new Object[]{Integer.valueOf(totalCount)});
                } else {
                    String photoCount = this.mContext.getResources().getQuantityString(R.plurals.photo_count, totalImageCount, new Object[]{Integer.valueOf(totalImageCount)});
                    String videoCount = this.mContext.getResources().getQuantityString(R.plurals.video_count, totalVideoCount, new Object[]{Integer.valueOf(totalVideoCount)});
                    mediaCount = String.format(this.mContext.getResources().getString(R.string.photo_video_count), new Object[]{photoCount, videoCount});
                }
                viewHolder.photoCount.setText(mediaCount);
                addAlbumSetCover(coverItems, viewHolder, position);
            }
        }
    }

    private void addAlbumSetCover(MediaItem[] coverItems, ViewHolder viewHolder, int position) {
        if (coverItems == null) {
            GalleryLog.d("ListAlbumPasteDataAdapter", "paste coverItem is null!");
            return;
        }
        AlbumSetEntry entry = this.mDataWindow.get(position);
        if (!(entry == null || coverItems.length <= 0 || coverItems[0] == null)) {
            Bitmap bmp = (Bitmap) entry.bitmapContainer.get(coverItems[0].getPath());
            viewHolder.albumCover.setImageRotation(coverItems[0].getRotation());
            viewHolder.albumCover.setScaleType(ScaleType.FIT_XY);
            if (bmp != null) {
                viewHolder.albumCover.setImage(bmp);
                viewHolder.albumCover.setStackBackground(bmp, bmp);
            }
        }
    }

    private void initViewHolder(View convertView, ViewHolder holder) {
        holder.albumCover = (MultiImageView) convertView.findViewById(R.id.album_cover_image);
        holder.albumName = (TextView) convertView.findViewById(R.id.album_name);
        holder.photoCount = (TextView) convertView.findViewById(R.id.photo_count);
        holder.frameOverlayIcon = (ImageView) convertView.findViewById(R.id.frame_overlay_icon);
        holder.frameOverlayMask = (ImageView) convertView.findViewById(R.id.frame_overlay_mask);
    }
}
