package com.huawei.gallery.app;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;
import com.android.gallery3d.R;
import com.android.gallery3d.data.MediaItem;
import com.android.gallery3d.data.MediaSet;
import com.android.gallery3d.ui.MultiImageView;
import com.android.gallery3d.ui.SelectionManager;
import com.android.gallery3d.util.GalleryLog;
import com.android.gallery3d.util.MediaSetUtils;
import com.huawei.gallery.storage.GalleryStorageManager;
import com.huawei.gallery.ui.AlbumSetSlidingWindow;
import com.huawei.gallery.ui.AlbumSetSlidingWindow.AlbumSetEntry;
import com.huawei.gallery.ui.AlbumSetSlidingWindowListener;
import java.util.ArrayList;

public class ListAlbumPickerDataAdapter extends ListAlbumBaseDataAdapter {
    private String mGetAlbumPath;
    private boolean mIsGetAlbum;
    private boolean mIsGetAlbumIncludeVirtual;
    private SelectionManager mSelectionManager;

    static class ViewHolder {
        private MultiImageView albumCover;
        private TextView albumName;
        private ImageView arrowBtn;
        private CheckBox checkBoxBtn;
        private ImageView frameOverlayIcon;
        private ImageView frameOverlayMask;
        private TextView photoCount;
        private RadioButton radioBtn;

        ViewHolder() {
        }
    }

    public ListAlbumPickerDataAdapter(Activity context, AlbumSetDataLoader source, SelectionManager selectionManager, boolean isGetAlbum, String getAblumPath, boolean getAlbumIncludeVirtual) {
        this.mContext = context;
        this.mSource = source;
        this.mSelectionManager = selectionManager;
        this.mIsGetAlbum = isGetAlbum;
        this.mGetAlbumPath = getAblumPath;
        this.mIsGetAlbumIncludeVirtual = getAlbumIncludeVirtual;
        this.mDataWindow = new AlbumSetSlidingWindow(context, source, 32);
        this.mDataWindow.setListener(new AlbumSetSlidingWindowListener(this));
        this.mInflater = (LayoutInflater) this.mContext.getSystemService("layout_inflater");
    }

    protected int getListViewLayoutId() {
        return R.layout.list_album_picker;
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
        int totalCount = this.mSource.getTotalCount(position);
        int totalVideoCount = this.mSource.getTotalVideoCount(position);
        int totalImageCount = totalCount - totalVideoCount;
        if (mediaSet != null) {
            String mediaCount;
            AlbumSetEntry entry = this.mDataWindow.get(position);
            CheckBox checkBox = viewHolder.checkBoxBtn;
            RadioButton radioButton = viewHolder.radioBtn;
            int iconVisibility = 8;
            if (this.mSelectionManager.inSelectionMode()) {
                viewHolder.arrowBtn.setVisibility(4);
                checkBox.setVisibility(0);
                radioButton.setVisibility(4);
                checkBox.setOnClickListener(new OnClickListener() {
                    public void onClick(View v) {
                        ListAlbumPickerDataAdapter.this.mSelectionManager.toggle(mediaSet.getPath());
                        GalleryLog.v("ListAlbumPickerDataAdapter", "checkBox clicked album is" + mediaSet.getName());
                    }
                });
                if (this.mSelectionManager.isItemSelected(mediaSet.getPath())) {
                    viewHolder.checkBoxBtn.setChecked(true);
                } else {
                    viewHolder.checkBoxBtn.setChecked(false);
                }
            } else {
                if (this.mIsGetAlbum && mediaSet.isLeafAlbum()) {
                    viewHolder.arrowBtn.setVisibility(4);
                    radioButton.setVisibility(0);
                    radioButton.setOnClickListener(new OnClickListener() {
                        public void onClick(View v) {
                            Intent result = new Intent();
                            if (!ListAlbumPickerDataAdapter.this.mIsGetAlbumIncludeVirtual) {
                                result.putExtra("album-path", mediaSet.getPath().toString());
                            } else if ("camera".equalsIgnoreCase(mediaSet.getLabel())) {
                                ArrayList<String> galleryStorageCameraBucketPathList = GalleryStorageManager.getInstance().getOuterGalleryStorageCameraBucketPathList();
                                galleryStorageCameraBucketPathList.add(0, "/local/image/" + MediaSetUtils.getCameraBucketId());
                                result.putExtra("albums-path", (String[]) galleryStorageCameraBucketPathList.toArray(new String[galleryStorageCameraBucketPathList.size()]));
                            } else if ("screenshots".equalsIgnoreCase(mediaSet.getLabel())) {
                                ArrayList<String> galleryStorageScreenshotsBucketPathList = GalleryStorageManager.getInstance().getOuterGalleryStorageScreenshotsBucketPathList();
                                galleryStorageScreenshotsBucketPathList.add(0, "/local/image/" + MediaSetUtils.getScreenshotsBucketID());
                                result.putExtra("albums-path", (String[]) galleryStorageScreenshotsBucketPathList.toArray(new String[galleryStorageScreenshotsBucketPathList.size()]));
                            } else {
                                result.putExtra("albums-path", new String[]{mediaSet.getPath().toString()});
                            }
                            result.putExtra("album-name", mediaSet.getName());
                            ListAlbumPickerDataAdapter.this.mGetAlbumPath = mediaSet.getPath().toString();
                            ListAlbumPickerDataAdapter.this.notifyDataSetChanged();
                            ListAlbumPickerDataAdapter.this.mContext.setResult(-1, result);
                            ListAlbumPickerDataAdapter.this.mContext.finish();
                        }
                    });
                    if (this.mSelectionManager.isItemSelected(mediaSet.getPath())) {
                        radioButton.setChecked(true);
                    } else {
                        radioButton.setChecked(false);
                    }
                    if (mediaSet.isVirtual() && "camera".equalsIgnoreCase(mediaSet.getLabel())) {
                        if (isCameraAlbum(this.mGetAlbumPath)) {
                            radioButton.setChecked(true);
                        }
                    }
                    if (mediaSet.isVirtual() && "screenshots".equalsIgnoreCase(mediaSet.getLabel())) {
                        if (isScreenshotsAlbum(this.mGetAlbumPath)) {
                            radioButton.setChecked(true);
                        }
                    }
                    if (mediaSet.getPath() == null || !mediaSet.getPath().equalsIgnoreCase(this.mGetAlbumPath)) {
                        radioButton.setChecked(false);
                    } else {
                        radioButton.setChecked(true);
                    }
                } else {
                    viewHolder.arrowBtn.setVisibility(0);
                    radioButton.setVisibility(4);
                }
                checkBox.setVisibility(4);
            }
            viewHolder.photoCount.setVisibility(0);
            viewHolder.albumName.setText(mediaSet.getName());
            if (mediaSet.isSdcardIconNeedShow()) {
                iconVisibility = 0;
                viewHolder.frameOverlayIcon.setImageResource(R.drawable.ic_gallery_frame_overlay_sd);
            }
            if (mediaSet.isVirtual() && ("camera_video".equalsIgnoreCase(mediaSet.getLabel()) || "screenshots_video".equalsIgnoreCase(mediaSet.getLabel()))) {
                iconVisibility = 0;
                viewHolder.frameOverlayIcon.setImageResource(R.drawable.ic_gallery_frame_overlay_video);
            }
            if (mediaSet.isVirtual() && "favorite".equalsIgnoreCase(mediaSet.getLabel())) {
                iconVisibility = 0;
                viewHolder.frameOverlayIcon.setImageResource(R.drawable.ic_gallery_frame_overlay_favorite);
            }
            if (mediaSet.isVirtual() && "doc_rectify".equalsIgnoreCase(mediaSet.getLabel())) {
                iconVisibility = 0;
                viewHolder.frameOverlayIcon.setImageResource(R.drawable.ic_gallery_frame_overlay_rectify);
            }
            if (mediaSet.isVirtual() && "3d_model_image".equalsIgnoreCase(mediaSet.getLabel())) {
                iconVisibility = 0;
                viewHolder.frameOverlayIcon.setImageResource(R.drawable.ic_gallery_frame_overlay_3d_portrait);
            }
            if (mediaSet.isVirtual() && "3d_panorama".equalsIgnoreCase(mediaSet.getLabel())) {
                iconVisibility = 0;
                viewHolder.frameOverlayIcon.setImageResource(R.drawable.ic_unlock_3d);
            }
            viewHolder.frameOverlayIcon.setVisibility(iconVisibility);
            viewHolder.frameOverlayMask.setVisibility(iconVisibility);
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
            if (coverItems == null) {
                GalleryLog.d("ListAlbumPickerDataAdapter", "coverItem is null!");
                return;
            }
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

    private void initViewHolder(View convertView, ViewHolder holder) {
        holder.albumCover = (MultiImageView) convertView.findViewById(R.id.album_cover_image);
        holder.albumName = (TextView) convertView.findViewById(R.id.album_name);
        holder.photoCount = (TextView) convertView.findViewById(R.id.photo_count);
        holder.arrowBtn = (ImageView) convertView.findViewById(R.id.ic_public_arrow_right);
        holder.checkBoxBtn = (CheckBox) convertView.findViewById(R.id.list_checkbox);
        holder.radioBtn = (RadioButton) convertView.findViewById(R.id.list_radiobtn);
        holder.frameOverlayIcon = (ImageView) convertView.findViewById(R.id.frame_overlay_icon);
        holder.frameOverlayMask = (ImageView) convertView.findViewById(R.id.frame_overlay_mask);
    }

    private boolean isCameraAlbum(String albumPath) {
        ArrayList<String> outerGalleryStorageCameraBucketPathList = GalleryStorageManager.getInstance().getOuterGalleryStorageCameraBucketPathList();
        if (("/local/image/" + MediaSetUtils.getCameraBucketId()).equalsIgnoreCase(albumPath) || outerGalleryStorageCameraBucketPathList.contains(albumPath) || "/local/image/camera".equalsIgnoreCase(albumPath)) {
            return true;
        }
        return false;
    }

    private boolean isScreenshotsAlbum(String albumPath) {
        ArrayList<String> outerGalleryStorageScreenshotsBucketPathList = GalleryStorageManager.getInstance().getOuterGalleryStorageScreenshotsBucketPathList();
        if (("/local/image/" + MediaSetUtils.getScreenshotsBucketID()).equalsIgnoreCase(albumPath) || outerGalleryStorageScreenshotsBucketPathList.contains(albumPath) || "/local/image/screenshots".equalsIgnoreCase(albumPath)) {
            return true;
        }
        return false;
    }

    public void updateChoosedPath(String newChoosedPath) {
        this.mGetAlbumPath = newChoosedPath;
    }
}
