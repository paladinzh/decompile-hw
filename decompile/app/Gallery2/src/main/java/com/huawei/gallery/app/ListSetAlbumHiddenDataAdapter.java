package com.huawei.gallery.app;

import android.app.Activity;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import com.android.gallery3d.R;
import com.android.gallery3d.data.MediaItem;
import com.android.gallery3d.data.MediaSet;
import com.android.gallery3d.ui.MultiImageView;
import com.android.gallery3d.util.GalleryLog;
import com.android.gallery3d.util.ReportToBigData;
import com.huawei.gallery.ui.AlbumSetSlidingWindow;
import com.huawei.gallery.ui.AlbumSetSlidingWindow.AlbumSetEntry;
import com.huawei.gallery.ui.AlbumSetSlidingWindowListener;

public class ListSetAlbumHiddenDataAdapter extends ListAlbumBaseDataAdapter {

    private static class MyOnClickListener implements OnClickListener {
        private MyOnClickListener() {
        }

        public void onClick(View v) {
            if (v instanceof Switch) {
                String status = ((Switch) v).isChecked() ? "On" : "Off";
                ReportToBigData.report(108, String.format("{HiddenAlbumSet:%s}", new Object[]{status}));
            }
        }
    }

    static class ViewHolder {
        private MultiImageView albumCover;
        private TextView albumName;
        private ImageView frameOverlayIcon;
        private ImageView frameOverlayMask;
        private TextView photoCount;
        private Switch switchButton;

        ViewHolder() {
        }
    }

    public ListSetAlbumHiddenDataAdapter(Activity context, AlbumSetDataLoader source, ListView listView) {
        this.mContext = context;
        this.mSource = source;
        this.mListView = listView;
        this.mInflater = (LayoutInflater) this.mContext.getSystemService("layout_inflater");
        this.mDataWindow = new AlbumSetSlidingWindow(context, source, 32);
        this.mDataWindow.setListener(new AlbumSetSlidingWindowListener(this));
    }

    protected int getListViewLayoutId() {
        return R.layout.list_sethide_albumset;
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
        final MediaSet mediaSet = this.mSource.getMediaSet(position);
        MediaItem[] coverItems = this.mSource.getCoverItem(position);
        if (mediaSet != null) {
            AlbumSetEntry entry = this.mDataWindow.get(position);
            Switch switchbutton = viewHolder.switchButton;
            switchbutton.setOnCheckedChangeListener(null);
            if (switchbutton.isChecked() != mediaSet.isHidden()) {
                switchbutton.setChecked(mediaSet.isHidden());
                switchbutton.jumpDrawablesToCurrentState();
            }
            switchbutton.setOnCheckedChangeListener(new OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        try {
                            mediaSet.hide();
                            return;
                        } catch (Exception e) {
                            GalleryLog.d("ListSetAlbumHiddenDataAdapter", "Not support hide or show Operation mediaSet " + mediaSet.getName());
                            return;
                        }
                    }
                    mediaSet.show();
                }
            });
            switchbutton.setOnClickListener(new MyOnClickListener());
            viewHolder.switchButton.setVisibility(0);
            viewHolder.albumName.setText(mediaSet.getName());
            int iconVisibility = 8;
            if (mediaSet.isSdcardIconNeedShow()) {
                iconVisibility = 0;
                viewHolder.frameOverlayIcon.setImageResource(R.drawable.ic_gallery_frame_overlay_sd);
            }
            viewHolder.frameOverlayIcon.setVisibility(iconVisibility);
            viewHolder.frameOverlayMask.setVisibility(iconVisibility);
            int totalCount = this.mSource.getTotalCount(position);
            int totalVideoCount = this.mSource.getTotalVideoCount(position);
            viewHolder.photoCount.setText(getMediaCountString(totalCount, totalVideoCount, totalCount - totalVideoCount));
            if (totalCount == 0) {
                viewHolder.albumCover.resetToEmpty();
            } else {
                addAlbumCover(viewHolder, coverItems, entry);
            }
        }
    }

    private void addAlbumCover(ViewHolder viewHolder, MediaItem[] coverItems, AlbumSetEntry entry) {
        if (coverItems != null) {
            viewHolder.albumCover.setBackground(0);
            viewHolder.albumCover.setScaleType(ScaleType.FIT_XY);
            if (!(entry == null || coverItems.length <= 0 || coverItems[0] == null)) {
                Bitmap bmp = (Bitmap) entry.bitmapContainer.get(coverItems[0].getPath());
                viewHolder.albumCover.setImageRotation(coverItems[0].getRotation());
                if (bmp != null) {
                    viewHolder.albumCover.setImage(bmp);
                    viewHolder.albumCover.setStackBackground(bmp, bmp);
                }
            }
        }
    }

    private String getMediaCountString(int totalCount, int totalVideoCount, int totalImageCount) {
        if (totalImageCount == 0 && totalVideoCount > 0) {
            return this.mContext.getResources().getQuantityString(R.plurals.video_count, totalVideoCount, new Object[]{Integer.valueOf(totalVideoCount)});
        } else if (totalImageCount <= 0 || totalVideoCount <= 0) {
            return this.mContext.getResources().getQuantityString(R.plurals.photo_count, totalCount, new Object[]{Integer.valueOf(totalCount)});
        } else {
            return this.mContext.getResources().getQuantityString(R.plurals.photo_count, totalImageCount, new Object[]{Integer.valueOf(totalImageCount)}) + ", " + this.mContext.getResources().getQuantityString(R.plurals.video_count, totalVideoCount, new Object[]{Integer.valueOf(totalVideoCount)});
        }
    }

    private void initViewHolder(View convertView, ViewHolder holder) {
        holder.albumCover = (MultiImageView) convertView.findViewById(R.id.album_cover_image);
        holder.albumName = (TextView) convertView.findViewById(R.id.album_name);
        holder.photoCount = (TextView) convertView.findViewById(R.id.photo_count);
        holder.switchButton = (Switch) convertView.findViewById(R.id.switchButton);
        holder.frameOverlayIcon = (ImageView) convertView.findViewById(R.id.frame_overlay_icon);
        holder.frameOverlayMask = (ImageView) convertView.findViewById(R.id.frame_overlay_mask);
    }
}
