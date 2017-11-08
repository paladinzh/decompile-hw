package com.huawei.gallery.photoshare.adapter;

import android.app.Activity;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.android.gallery3d.R;
import com.android.gallery3d.common.BitmapUtils;
import com.android.gallery3d.data.MediaItem;
import com.android.gallery3d.data.MediaSet;
import com.android.gallery3d.ui.SharedItemAdapter;
import com.android.gallery3d.ui.SharedItemAdapter.ViewProvider;
import com.android.gallery3d.ui.SharedItemView;
import com.android.gallery3d.util.GalleryLog;
import com.android.gallery3d.util.GalleryUtils;
import com.huawei.gallery.app.AlbumSetDataLoader;
import com.huawei.gallery.ui.AlbumSetSlidingWindow;
import com.huawei.gallery.ui.AlbumSetSlidingWindow.AlbumCoverController;
import com.huawei.gallery.ui.AlbumSetSlidingWindow.AlbumSetEntry;
import com.huawei.gallery.ui.AlbumSetSlidingWindow.Listener;
import com.huawei.gallery.util.LayoutHelper;
import com.huawei.gallery.util.MyPrinter;
import com.huawei.gallery.util.UIUtils;

public class PhotoShareMainDataAdapter extends BaseAdapter implements ViewProvider {
    private static final MyPrinter LOG = new MyPrinter("PhotoShareMainDataAdapter");
    private AdapterView<?> mAdapterView;
    private Activity mContext;
    private int mCount = 0;
    private AlbumSetSlidingWindow mDataWindow;
    private LayoutInflater mInflater;
    private boolean mIsPhone = true;
    private boolean mIsPort = true;
    private AdapterViewOnClickListener mItemOnClickListener;
    private final AlbumSetDataLoader mSource;
    private int mSourceSize = 0;

    public interface AdapterViewOnClickListener extends OnItemClickListener, OnItemLongClickListener {
    }

    static class ItemViewHolder {
        private ImageView albumCover;
        private LinearLayout albumInfoRoot;
        private TextView albumName;
        private TextView albumTips;
        private TextView emptyInfo;
        private TextView newPictureCount;
        private TextView photoCount;

        ItemViewHolder() {
        }
    }

    private class MyCacheListener implements Listener {
        private MyCacheListener() {
        }

        public void onSizeChanged(int size) {
            PhotoShareMainDataAdapter.this.setCount();
            PhotoShareMainDataAdapter.this.notifyDataSetChanged();
        }

        public void onContentChanged(int index) {
            PhotoShareMainDataAdapter.this.notifyDataSetChanged();
        }
    }

    public void setOnClickListener(AdapterViewOnClickListener itemOnClickListener) {
        this.mItemOnClickListener = itemOnClickListener;
    }

    public PhotoShareMainDataAdapter(Activity context, AlbumSetDataLoader source) {
        boolean z;
        this.mContext = context;
        this.mSource = source;
        this.mDataWindow = new AlbumSetSlidingWindow(context, source, 32);
        this.mDataWindow.setListener(new MyCacheListener());
        this.mDataWindow.setAlbmCoverController(new AlbumCoverController() {
            public int getThumbnailType() {
                return 20;
            }

            public int changModifyTimeBy() {
                return 1000;
            }
        });
        this.mInflater = (LayoutInflater) this.mContext.getSystemService("layout_inflater");
        if (GalleryUtils.isTabletProduct(context)) {
            z = false;
        } else {
            z = true;
        }
        this.mIsPhone = z;
    }

    public int getCount() {
        return this.mCount;
    }

    public void setCount() {
        this.mSourceSize = this.mSource.size();
        this.mIsPort = this.mIsPhone ? LayoutHelper.isPortFeel() : false;
        this.mCount = this.mIsPort ? this.mSourceSize : (this.mSourceSize + 1) / 2;
        LOG.d("set Count " + this.mCount);
    }

    public Object getItem(int position) {
        return null;
    }

    public long getItemId(int position) {
        return (long) position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        LOG.d("getView position " + position);
        if (position >= getCount()) {
            GalleryLog.d("PhotoShareMainDataAdapter", "Invalid view position : " + position + ", Actual size is : " + getCount());
            return null;
        } else if (this.mAdapterView == null) {
            GalleryLog.w("PhotoShareMainDataAdapter", " mListView is never initialized ");
            return null;
        } else {
            SharedItemView item;
            int start = Math.max(0, this.mAdapterView.getFirstVisiblePosition() - 1);
            int end = Math.min(this.mAdapterView.getLastVisiblePosition() + 2, getCount());
            if (this.mIsPort) {
                this.mDataWindow.setActiveWindow(start, end);
            } else {
                this.mDataWindow.setActiveWindow(start * 2, end * 2);
            }
            if (convertView == null) {
                GalleryLog.d("PhotoShareMainDataAdapter", "MediaSet is null getCount " + getCount() + " position " + position);
                item = new SharedItemView((ViewProvider) this, this.mContext);
                if (this.mItemOnClickListener != null) {
                    item.setOnItemClickListener(this.mItemOnClickListener);
                    item.setOnItemLongClickListener(this.mItemOnClickListener);
                }
                convertView = item;
            } else {
                item = (SharedItemView) convertView;
            }
            SharedItemAdapter adapter = item.getAdapter();
            adapter.clear();
            if (this.mIsPort) {
                adapter.addIndex(position);
            } else {
                int index = position * 2;
                if (index < this.mSourceSize) {
                    adapter.addIndex(index);
                }
                index++;
                if (index < this.mSourceSize) {
                    adapter.addIndex(index);
                }
            }
            convertView.requestLayout();
            return convertView;
        }
    }

    public View getItemView(int position, View convertView, ViewGroup parent) {
        LOG.d("getItemView position " + position);
        if (convertView == null) {
            GalleryLog.d("PhotoShareMainDataAdapter", "convertView is null getCount " + getCount() + " position " + position);
            convertView = this.mInflater.inflate(R.layout.shared_label_image_item, parent, false);
            UIUtils.addStateListAnimation(convertView, this.mContext);
            ItemViewHolder viewHolder = new ItemViewHolder();
            initItemViewHolder(convertView, viewHolder);
            convertView.setTag(viewHolder);
        }
        if (this.mSource.isContentValid(position)) {
            setLayoutInfo(convertView, position);
        }
        return convertView;
    }

    private final void setLayoutInfo(View convertView, int position) {
        convertView.setVisibility(0);
        MediaSet mediaSet = this.mSource.getMediaSet(position);
        LOG.d("setLayoutInfo position " + position + ", mediaset: " + mediaSet);
        if (mediaSet != null) {
            MediaItem[] coverItems = this.mSource.getCoverItem(position);
            int totalCount = this.mSource.getTotalCount(position);
            int totalVideoCount = this.mSource.getTotalVideoCount(position);
            AlbumSetEntry entry = this.mDataWindow.get(position);
            ItemViewHolder viewHolder = (ItemViewHolder) convertView.getTag();
            viewHolder.albumInfoRoot.setVisibility(0);
            viewHolder.emptyInfo.setVisibility(8);
            if (mediaSet.isVirtual()) {
                viewHolder.albumInfoRoot.setVisibility(4);
                viewHolder.emptyInfo.setVisibility(0);
                viewHolder.albumCover.setBackgroundResource(R.drawable.img_bg_family_share);
                viewHolder.albumCover.setImageDrawable(null);
                viewHolder.emptyInfo.setText(R.string.discover_empty_family_group);
            } else if (totalCount == 0) {
                viewHolder.albumCover.setScaleType(ScaleType.FIT_XY);
                viewHolder.albumCover.setBackgroundResource(R.drawable.img_discovery_album_bg);
                viewHolder.albumCover.setImageDrawable(null);
                viewHolder.photoCount.setVisibility(0);
                viewHolder.albumName.setText(mediaSet.getName());
                viewHolder.photoCount.setText(createPictureAndVideoCountString(totalCount, totalVideoCount));
                albumTips = getAlbumTips(mediaSet);
                if (albumTips != null) {
                    viewHolder.albumTips.setVisibility(0);
                    viewHolder.albumTips.setText(albumTips);
                } else {
                    viewHolder.albumTips.setVisibility(8);
                }
                viewHolder.newPictureCount.setVisibility(4);
            } else {
                viewHolder.albumCover.setBackgroundResource(0);
                viewHolder.photoCount.setText(createPictureAndVideoCountString(totalCount, totalVideoCount));
                viewHolder.photoCount.setVisibility(0);
                viewHolder.albumName.setText(mediaSet.getName());
                albumTips = getAlbumTips(mediaSet);
                if (albumTips != null) {
                    viewHolder.albumTips.setVisibility(0);
                    viewHolder.albumTips.setText(albumTips);
                } else {
                    viewHolder.albumTips.setVisibility(8);
                }
                if (coverItems == null) {
                    LOG.d("coverItems is null and item count is not 0.");
                    return;
                }
                setCover(position, coverItems, entry, viewHolder);
                if (mediaSet.getNewPictureCount() > 99) {
                    viewHolder.newPictureCount.setText("99+");
                    viewHolder.newPictureCount.setVisibility(0);
                } else if (mediaSet.getNewPictureCount() > 0) {
                    viewHolder.newPictureCount.setText(Integer.toString(mediaSet.getNewPictureCount()));
                    viewHolder.newPictureCount.setVisibility(0);
                } else {
                    viewHolder.newPictureCount.setVisibility(8);
                }
            }
        }
    }

    private void setCover(int position, MediaItem[] coverItems, AlbumSetEntry entry, ItemViewHolder viewHolder) {
        if (entry != null && coverItems.length > 0 && coverItems[0] != null) {
            Bitmap bmp = (Bitmap) entry.bitmapContainer.get(coverItems[0].getPath());
            LOG.d("position " + position + ", rotation " + coverItems[0].getRotation());
            if (bmp == null) {
                return;
            }
            if (entry.isNoThumb) {
                ImageView cover = viewHolder.albumCover;
                cover.setScaleType(ScaleType.CENTER);
                cover.setImageResource(R.drawable.ic_gallery_list_damage);
                return;
            }
            setThumbnail(viewHolder.albumCover, bmp, coverItems[0].getRotation());
        }
    }

    private void setThumbnail(ImageView image, Bitmap bm, int rotation) {
        bm = BitmapUtils.rotateBitmap(bm, rotation, false);
        image.setScaleType(ScaleType.CENTER_CROP);
        image.setImageBitmap(bm);
    }

    private void initItemViewHolder(View convertView, ItemViewHolder holder) {
        holder.emptyInfo = (TextView) convertView.findViewById(R.id.empty_album_name);
        holder.albumInfoRoot = (LinearLayout) convertView.findViewById(R.id.albumset_info);
        holder.albumCover = (ImageView) convertView.findViewById(R.id.album_cover_image);
        holder.albumName = (TextView) convertView.findViewById(R.id.album_name);
        holder.photoCount = (TextView) convertView.findViewById(R.id.photo_count);
        holder.albumTips = (TextView) convertView.findViewById(R.id.album_tips);
        holder.newPictureCount = (TextView) convertView.findViewById(R.id.newpicture_count);
    }

    private String getAlbumTips(MediaSet targetSet) {
        int receiverCount;
        switch (targetSet.getAlbumType()) {
            case 1:
                return this.mContext.getString(R.string.photoshare_system_album);
            case 2:
                receiverCount = targetSet.getAlbumInfo().getReceiverCount() + 1;
                if (receiverCount <= 1) {
                    return this.mContext.getString(R.string.photoshare_no_shared_frends);
                }
                return this.mContext.getResources().getQuantityString(R.plurals.photoshare_member_tips, receiverCount, new Object[]{Integer.valueOf(receiverCount)});
            case 3:
                receiverCount = targetSet.getAlbumInfo().getReceiverCount();
                if (receiverCount <= 0) {
                    return null;
                }
                return this.mContext.getResources().getQuantityString(R.plurals.photoshare_member_tips, receiverCount + 1, new Object[]{Integer.valueOf(receiverCount + 1)});
            case 7:
                return this.mContext.getString(R.string.create_family_share_tips);
            default:
                return null;
        }
    }

    private String createPictureAndVideoCountString(int totalCount, int totalVideoCount) {
        int totalImageCount = totalCount - totalVideoCount;
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

    public void setAdapterView(AdapterView<?> adapterView) {
        this.mAdapterView = adapterView;
    }

    public void resume() {
        this.mDataWindow.resume();
    }

    public void pause() {
        this.mDataWindow.pause();
    }
}
