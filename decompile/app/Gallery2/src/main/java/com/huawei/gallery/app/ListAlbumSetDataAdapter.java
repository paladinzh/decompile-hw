package com.huawei.gallery.app;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.TextView;
import com.android.gallery3d.R;
import com.android.gallery3d.data.GalleryRecycleAlbum;
import com.android.gallery3d.data.MediaItem;
import com.android.gallery3d.data.MediaSet;
import com.android.gallery3d.ui.MultiImageView;
import com.android.gallery3d.ui.MultiImageView.Mode;
import com.android.gallery3d.ui.SelectionManager;
import com.android.gallery3d.util.GalleryLog;
import com.huawei.gallery.ui.AlbumSetSlidingWindow;
import com.huawei.gallery.ui.AlbumSetSlidingWindow.AlbumSetEntry;
import com.huawei.gallery.ui.AlbumSetSlidingWindowListener;
import com.huawei.gallery.util.DragListView;
import com.huawei.gallery.util.UIUtils;
import com.huawei.watermark.manager.parse.WMElement;
import java.util.Arrays;

public class ListAlbumSetDataAdapter extends ListAlbumBaseDataAdapter {
    private boolean mDragging = false;
    private boolean mIsOutside = false;
    private SelectionManager mSelectionManager;

    static class ViewHolder {
        private MultiImageView albumCover;
        private TextView albumName;
        private ImageView arrowBtn;
        private ImageView categoryDivider;
        private CheckBox checkBoxBtn;
        private ImageView dragBtn;
        private ImageView frameOverlayIcon;
        private ImageView frameOverlayMask;
        private TextView photoCount;

        ViewHolder() {
        }
    }

    public ListAlbumSetDataAdapter(Activity context, AlbumSetDataLoader source, SelectionManager selectionManager, boolean isOutside) {
        this.mContext = context;
        this.mSource = source;
        this.mSelectionManager = selectionManager;
        this.mIsOutside = isOutside;
        this.mDataWindow = new AlbumSetSlidingWindow(context, source, 32);
        this.mDataWindow.setListener(new AlbumSetSlidingWindowListener(this));
        this.mInflater = (LayoutInflater) this.mContext.getSystemService("layout_inflater");
    }

    public Object getItem(int position) {
        return null;
    }

    protected View initConvertViewAtGetView(View convertView, ViewGroup parent) {
        if (convertView == null) {
            int style = 0;
            if (this.mContext instanceof AbstractGalleryActivity) {
                style = ((AbstractGalleryActivity) this.mContext).getGalleryActionBar().getStyle();
            }
            if (style == 1) {
                this.mContext.getTheme().applyStyle(this.mContext.getResources().getIdentifier("androidhwext:style/Theme.Emui", null, null), true);
                convertView = this.mInflater.inflate(R.layout.list_albumset, parent, false);
                this.mContext.getTheme().applyStyle(this.mContext.getResources().getIdentifier("androidhwext:style/Theme.Emui.Dark", null, null), true);
            } else {
                convertView = this.mInflater.inflate(R.layout.list_albumset, parent, false);
            }
            UIUtils.addStateListAnimation(convertView, this.mContext);
        }
        return convertView;
    }

    private void relayoutItemAfterDragIfNeed(View convertView) {
        if (convertView != null && !this.mDragging) {
            if (convertView.getTag(R.id.list_albumset) != null) {
                LayoutParams params = convertView.getLayoutParams();
                if (params != null && (this.mListView instanceof DragListView)) {
                    params.height = ((DragListView) this.mListView).getItemHeightNormal();
                    convertView.setLayoutParams(params);
                    convertView.setTag(R.id.list_albumset, null);
                }
            }
            if (convertView.getVisibility() == 4) {
                convertView.setVisibility(0);
            }
        }
    }

    protected void setLayoutInfo(View convertView, int position) {
        ViewHolder viewHolder;
        if (convertView.getTag() == null) {
            viewHolder = new ViewHolder();
            initViewHolder(convertView, viewHolder);
            convertView.setTag(viewHolder);
            GalleryLog.v("ListAlbumSetDataAdapter", "initialize the view holder");
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        MediaSet mediaSet = this.mSource.getMediaSet(position);
        MediaItem[] coverItems = this.mSource.getCoverItem(position);
        int totalCount = this.mSource.getTotalCount(position);
        int totalVideoCount = this.mSource.getTotalVideoCount(position);
        int totalImageCount = totalCount - totalVideoCount;
        if (mediaSet == null) {
            GalleryLog.e("ListAlbumSetDataAdapter", "mediaSet == null");
            return;
        }
        relayoutItemAfterDragIfNeed(convertView);
        AlbumSetEntry entry = this.mDataWindow.get(position);
        addCheckbox(viewHolder, mediaSet);
        viewHolder.frameOverlayIcon.setVisibility(8);
        viewHolder.frameOverlayMask.setVisibility(8);
        viewHolder.categoryDivider.setVisibility(8);
        if (mediaSet.isVirtual() && ("other".equalsIgnoreCase(mediaSet.getLabel()) || "photoshare".equalsIgnoreCase(mediaSet.getLabel()))) {
            addOtherAndPhotoShareVitualAlbumSetLayout(convertView, viewHolder, mediaSet, coverItems, totalCount, totalVideoCount, totalImageCount, entry);
        } else if (mediaSet instanceof GalleryRecycleAlbum) {
            addRecycleAlbumsetLayout(viewHolder, mediaSet, coverItems, totalCount, totalVideoCount, totalImageCount, entry);
        } else if (totalCount == 0) {
            addEmptyAlbumsetLayout(viewHolder, mediaSet, totalCount);
        } else {
            addNormalAlbumsetLayout(viewHolder, mediaSet, coverItems, totalCount, totalVideoCount, totalImageCount, entry);
        }
    }

    private void addNormalAlbumsetLayout(ViewHolder viewHolder, MediaSet mediaSet, MediaItem[] coverItems, int totalCount, int totalVideoCount, int totalImageCount, AlbumSetEntry entry) {
        String mediaCount;
        viewHolder.albumCover.switchTo(Mode.NORMAL);
        viewHolder.albumName.setText(mediaSet.getName());
        if ("3d_model_image".equals(mediaSet.getLabel())) {
            mediaCount = this.mContext.getResources().getQuantityString(R.plurals.portrait3d_front_views_counts, totalCount, new Object[]{Integer.valueOf(totalCount)});
        } else {
            mediaCount = getMediaCountString(totalVideoCount, totalImageCount, totalCount);
        }
        viewHolder.photoCount.setText(mediaCount);
        if (coverItems != null) {
            if (!(entry == null || coverItems.length <= 0 || coverItems[0] == null)) {
                Bitmap bmp = (Bitmap) entry.bitmapContainer.get(coverItems[0].getPath());
                viewHolder.albumCover.setImageRotation(coverItems[0].getRotation());
                viewHolder.albumCover.setScaleType(ScaleType.FIT_XY);
                if (bmp != null) {
                    viewHolder.albumCover.setImage(bmp);
                    viewHolder.albumCover.setStackBackground(bmp, bmp);
                }
            }
            addFrameOverlayIcon(viewHolder, mediaSet);
        }
    }

    private void addRecycleAlbumsetLayout(ViewHolder viewHolder, MediaSet mediaSet, MediaItem[] coverItems, int totalCount, int totalVideoCount, int totalImageCount, AlbumSetEntry entry) {
        viewHolder.albumCover.switchTo(Mode.NORMAL);
        viewHolder.albumName.setText(mediaSet.getName());
        viewHolder.photoCount.setText(getMediaCountString(totalVideoCount, totalImageCount, totalCount));
        viewHolder.albumCover.setImageRotation(0);
        viewHolder.albumCover.setImage((int) R.drawable.cover_recycle_album);
        viewHolder.albumCover.setBackground(R.drawable.album_cover_background);
        viewHolder.albumCover.setStackBackground((int) R.drawable.album_cover_background, (int) R.drawable.album_cover_background);
    }

    private void addEmptyAlbumsetLayout(ViewHolder viewHolder, MediaSet mediaSet, int totalCount) {
        viewHolder.albumCover.switchTo(Mode.NORMAL);
        viewHolder.albumCover.setBackground(0);
        viewHolder.albumCover.resetToEmpty();
        viewHolder.albumName.setText(mediaSet.getName());
        if (mediaSet.isSdcardIconNeedShow()) {
            viewHolder.frameOverlayIcon.setVisibility(0);
            viewHolder.frameOverlayMask.setVisibility(0);
            viewHolder.frameOverlayIcon.setImageResource(R.drawable.ic_gallery_frame_overlay_sd);
        }
        viewHolder.photoCount.setText(this.mContext.getResources().getQuantityString(R.plurals.photo_count, totalCount, new Object[]{Integer.valueOf(totalCount)}));
    }

    private void addOtherAndPhotoShareVitualAlbumSetLayout(View convertView, ViewHolder viewHolder, MediaSet mediaSet, MediaItem[] coverItems, int totalCount, int totalVideoCount, int totalImageCount, AlbumSetEntry entry) {
        String albums;
        viewHolder.albumCover.switchTo(Mode.GRID);
        viewHolder.albumCover.setBackground(R.drawable.album_cover_background);
        viewHolder.albumCover.setStackBackground((int) R.drawable.album_cover_background, (int) R.drawable.album_cover_background);
        viewHolder.albumName.setText(mediaSet.getName());
        if ("photoshare".equalsIgnoreCase(mediaSet.getLabel())) {
            viewHolder.categoryDivider.setVisibility(0);
            LayoutParams params = convertView.getLayoutParams();
            if (params != null && (this.mListView instanceof DragListView)) {
                params.height = ((DragListView) this.mListView).getPhotoShareItemHeight();
                convertView.setLayoutParams(params);
            }
        }
        if ("photoshare".equalsIgnoreCase(mediaSet.getLabel())) {
            albums = getCloudAlbumCount(mediaSet, totalImageCount, totalVideoCount);
        } else {
            albums = this.mContext.getResources().getQuantityString(R.plurals.album_count, totalCount, new Object[]{Integer.valueOf(totalCount)});
        }
        viewHolder.photoCount.setText(albums);
        if (coverItems != null) {
            Drawable[] items = new Drawable[4];
            int[] rotation = new int[4];
            Arrays.fill(rotation, 0);
            int length = Math.min(coverItems.length, 4);
            for (int index = 0; index < Math.min(length, mediaSet.getSubMediaSetCount()); index++) {
                if (entry != null) {
                    if (coverItems[index] == null) {
                        items[index] = this.mContext.getResources().getDrawable(R.drawable.pic_gallery_album_empty_day);
                    } else {
                        Bitmap bmp = (Bitmap) entry.bitmapContainer.get(coverItems[index].getPath());
                        if (bmp != null) {
                            items[index] = new BitmapDrawable(this.mContext.getResources(), bmp);
                            rotation[index] = coverItems[index].getRotation();
                        }
                    }
                }
            }
            viewHolder.albumCover.setGridDrawable(items[0], items[1], items[2], items[3]);
            viewHolder.albumCover.setGridRotation(rotation[0], rotation[1], rotation[2], rotation[3]);
        }
    }

    private String getMediaCountString(int totalVideoCount, int totalImageCount, int totalCount) {
        if (totalImageCount == 0 && totalVideoCount > 0) {
            return this.mContext.getResources().getQuantityString(R.plurals.video_count, totalVideoCount, new Object[]{Integer.valueOf(totalVideoCount)});
        } else if (totalImageCount <= 0 || totalVideoCount <= 0) {
            return this.mContext.getResources().getQuantityString(R.plurals.photo_count, totalCount, new Object[]{Integer.valueOf(totalCount)});
        } else {
            String photoCount = this.mContext.getResources().getQuantityString(R.plurals.photo_count, totalImageCount, new Object[]{Integer.valueOf(totalImageCount)});
            String videoCount = this.mContext.getResources().getQuantityString(R.plurals.video_count, totalVideoCount, new Object[]{Integer.valueOf(totalVideoCount)});
            return String.format(this.mContext.getResources().getString(R.string.photo_video_count), new Object[]{photoCount, videoCount});
        }
    }

    private void addCheckbox(ViewHolder viewHolder, final MediaSet mediaSet) {
        CheckBox checkBox = viewHolder.checkBoxBtn;
        if (this.mSelectionManager.inSelectionMode()) {
            if (this.mIsOutside) {
                viewHolder.dragBtn.setVisibility(0);
                if (mediaSet.isVirtual()) {
                    viewHolder.dragBtn.setAlpha(0.3f);
                } else {
                    viewHolder.dragBtn.setAlpha(WMElement.CAMERASIZEVALUE1B1);
                    viewHolder.dragBtn.setEnabled(true);
                }
            }
            viewHolder.arrowBtn.setVisibility(8);
            checkBox.setVisibility(0);
            if ("photoshare".equalsIgnoreCase(mediaSet.getLabel()) || (mediaSet instanceof GalleryRecycleAlbum)) {
                checkBox.setVisibility(8);
            } else {
                checkBox.setEnabled(true);
                checkBox.setOnClickListener(new OnClickListener() {
                    public void onClick(View v) {
                        ListAlbumSetDataAdapter.this.mSelectionManager.toggle(mediaSet.getPath());
                    }
                });
            }
            if (this.mSelectionManager.isItemSelected(mediaSet.getPath())) {
                viewHolder.checkBoxBtn.setChecked(true);
                return;
            } else {
                viewHolder.checkBoxBtn.setChecked(false);
                return;
            }
        }
        viewHolder.arrowBtn.setVisibility(0);
        viewHolder.dragBtn.setVisibility(8);
        viewHolder.checkBoxBtn.setChecked(false);
        viewHolder.checkBoxBtn.setVisibility(8);
    }

    private void addFrameOverlayIcon(ViewHolder viewHolder, MediaSet mediaSet) {
        viewHolder.frameOverlayIcon.setVisibility(0);
        viewHolder.frameOverlayMask.setVisibility(0);
        if (mediaSet.isSdcardIconNeedShow()) {
            viewHolder.frameOverlayIcon.setImageResource(R.drawable.ic_gallery_frame_overlay_sd);
        } else if (mediaSet.isVirtual() && ("camera_video".equalsIgnoreCase(mediaSet.getLabel()) || "screenshots_video".equalsIgnoreCase(mediaSet.getLabel()))) {
            viewHolder.frameOverlayIcon.setImageResource(R.drawable.ic_gallery_frame_overlay_video);
        } else if (mediaSet.isVirtual() && "favorite".equalsIgnoreCase(mediaSet.getLabel())) {
            viewHolder.frameOverlayIcon.setImageResource(R.drawable.ic_gallery_frame_overlay_favorite);
        } else if (mediaSet.isVirtual() && "doc_rectify".equalsIgnoreCase(mediaSet.getLabel())) {
            viewHolder.frameOverlayIcon.setImageResource(R.drawable.ic_gallery_frame_overlay_rectify);
        } else if (mediaSet.isVirtual() && "3d_model_image".equalsIgnoreCase(mediaSet.getLabel())) {
            viewHolder.frameOverlayIcon.setImageResource(R.drawable.ic_gallery_frame_overlay_3d_portrait);
        } else if (mediaSet.isVirtual() && "3d_panorama".equalsIgnoreCase(mediaSet.getLabel())) {
            viewHolder.frameOverlayIcon.setImageResource(R.drawable.ic_unlock_3d);
        } else {
            viewHolder.frameOverlayIcon.setVisibility(8);
            viewHolder.frameOverlayMask.setVisibility(8);
        }
    }

    private String getCloudAlbumCount(MediaSet mediaSet, int imageCount, int videoCount) {
        int albumCount = mediaSet.getSubMediaSetCount();
        String videoStr = this.mContext.getResources().getQuantityString(R.plurals.message_type_video, videoCount, new Object[]{Integer.valueOf(videoCount)});
        String imageStr = this.mContext.getResources().getQuantityString(R.plurals.message_type_image, imageCount, new Object[]{Integer.valueOf(imageCount)});
        if (imageCount == 0 && videoCount > 0) {
            return this.mContext.getResources().getQuantityString(R.plurals.cloud_album_count_one_type, albumCount, new Object[]{Integer.valueOf(albumCount), videoStr});
        } else if (imageCount <= 0 || videoCount <= 0) {
            return this.mContext.getResources().getQuantityString(R.plurals.cloud_album_count_one_type, albumCount, new Object[]{Integer.valueOf(albumCount), imageStr});
        } else {
            return this.mContext.getResources().getQuantityString(R.plurals.cloud_album_count_two_type, albumCount, new Object[]{Integer.valueOf(albumCount), imageStr, videoStr});
        }
    }

    private void initViewHolder(View convertView, ViewHolder holder) {
        holder.dragBtn = (ImageView) convertView.findViewById(R.id.list_drag_control_btn);
        holder.albumCover = (MultiImageView) convertView.findViewById(R.id.album_cover_image);
        holder.albumName = (TextView) convertView.findViewById(R.id.album_name);
        holder.photoCount = (TextView) convertView.findViewById(R.id.photo_count);
        holder.arrowBtn = (ImageView) convertView.findViewById(R.id.ic_public_arrow_right);
        holder.checkBoxBtn = (CheckBox) convertView.findViewById(R.id.list_checkbox);
        holder.frameOverlayIcon = (ImageView) convertView.findViewById(R.id.frame_overlay_icon);
        holder.frameOverlayMask = (ImageView) convertView.findViewById(R.id.frame_overlay_mask);
        holder.categoryDivider = (ImageView) convertView.findViewById(R.id.category_divider);
    }

    public void setDraggingStatus(boolean dragging) {
        this.mDragging = dragging;
    }
}
