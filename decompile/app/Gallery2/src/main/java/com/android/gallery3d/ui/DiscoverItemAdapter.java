package com.android.gallery3d.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.TextView;
import com.android.gallery3d.R;
import com.android.gallery3d.common.BitmapUtils;
import com.android.gallery3d.ui.DiscoverItemView.Style;
import com.huawei.gallery.photoshare.DiscoverHeadDataLoader.AlbumSet;
import com.huawei.gallery.util.MyPrinter;

public class DiscoverItemAdapter extends BaseAdapter {
    private static Entry[] EMPTY = new Entry[0];
    private static final MyPrinter LOG = new MyPrinter("DiscoverItemAdapter");
    private ViewHolder[] mCachedView = new ViewHolder[6];
    private Context mContext;
    private Entry[] mData = EMPTY;
    private boolean mIsEmpty = true;
    private int mSize = -1;
    private String mUnnamed;

    public static class DiscoverStyleKey {
        private static final /* synthetic */ int[] -com-android-gallery3d-ui-DiscoverItemView$StyleSwitchesValues = null;
        final boolean needMarkness;
        final int position;
        final Style style;

        private static /* synthetic */ int[] -getcom-android-gallery3d-ui-DiscoverItemView$StyleSwitchesValues() {
            if (-com-android-gallery3d-ui-DiscoverItemView$StyleSwitchesValues != null) {
                return -com-android-gallery3d-ui-DiscoverItemView$StyleSwitchesValues;
            }
            int[] iArr = new int[Style.values().length];
            try {
                iArr[Style.Nomal.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                iArr[Style.Story.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            -com-android-gallery3d-ui-DiscoverItemView$StyleSwitchesValues = iArr;
            return iArr;
        }

        public DiscoverStyleKey(Style style, int position, boolean needMarkness) {
            this.style = style;
            this.position = position;
            this.needMarkness = needMarkness;
        }

        public DiscoverStyleKey(int position) {
            this(Style.Nomal, position, false);
        }

        public int getLayoutId(boolean isEmpty) {
            if (isEmpty) {
                return R.layout.discover_label_image_item;
            }
            switch (-getcom-android-gallery3d-ui-DiscoverItemView$StyleSwitchesValues()[this.style.ordinal()]) {
                case 1:
                    return R.layout.discover_label_image_item;
                case 2:
                    int i;
                    if (this.needMarkness) {
                        i = R.layout.discover_label_image_item_text_center_big;
                    } else {
                        i = R.layout.discover_label_image_item_text_center;
                    }
                    return i;
                default:
                    return R.layout.discover_label_image_item;
            }
        }
    }

    public static class Entry {
        public String label;
        public int rotation;
        public Bitmap thumbnail;
    }

    private static class ViewHolder {
        private final TextView label;
        private final ImageView thumbnail;

        private ViewHolder(View view) {
            this.thumbnail = (ImageView) view.findViewById(R.id.thumbnail);
            this.label = (TextView) view.findViewById(R.id.label);
        }
    }

    public DiscoverItemAdapter(Context context) {
        this.mContext = context;
        this.mUnnamed = this.mContext.getString(R.string.photoshare_tag_unnamed);
    }

    public void setEntry(AlbumSet data) {
        if (data == null) {
            this.mData = EMPTY;
            notifyDataSetChanged();
            return;
        }
        int size = data.getLoadedDataCount();
        if (this.mSize != size) {
            this.mSize = size;
            notifyDataSetChanged();
        }
        Entry entry;
        if (size == 0) {
            entry = new Entry();
            entry.label = this.mContext.getString(data.entry.emptyLabel);
            entry.thumbnail = data.getDefaultCover();
            this.mIsEmpty = true;
            this.mData = new Entry[]{entry};
        } else {
            Entry[] newData = new Entry[size];
            for (int index = 0; index < size; index++) {
                entry = new Entry();
                entry.thumbnail = data.getThumbnail(index);
                entry.rotation = data.getRotation(index);
                entry.label = data.getSubMediaSetName(index);
                newData[index] = entry;
                if (entry.rotation != 0) {
                    entry.thumbnail = BitmapUtils.rotateBitmap(entry.thumbnail, entry.rotation, false);
                }
                updateItemInfo(this.mCachedView[index], entry);
            }
            this.mIsEmpty = false;
            this.mData = newData;
        }
    }

    public int getCount() {
        return this.mData.length;
    }

    public Object getItem(int position) {
        return this.mData[position];
    }

    public long getItemId(int position) {
        return (long) position;
    }

    public int getItemViewType(int position) {
        return 0;
    }

    public View getView(DiscoverStyleKey key, View convertView, ViewGroup parent) {
        LOG.d("getView from adapter");
        if (key.position > this.mData.length) {
            return null;
        }
        ViewHolder holder;
        Entry entry = this.mData[key.position];
        if (convertView == null) {
            convertView = LayoutInflater.from(parent.getContext()).inflate(key.getLayoutId(this.mIsEmpty), null);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        updateItemInfo(holder, entry);
        this.mCachedView[key.position] = holder;
        return convertView;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        return getView(new DiscoverStyleKey(position), convertView, parent);
    }

    private void updateItemInfo(ViewHolder holder, Entry entry) {
        if (holder == null) {
            LOG.w("holder is null, cann't update item info.");
            return;
        }
        if (entry.thumbnail != null) {
            holder.thumbnail.setScaleType(ScaleType.CENTER_CROP);
            holder.thumbnail.setImageBitmap(entry.thumbnail);
        } else {
            holder.thumbnail.setScaleType(ScaleType.CENTER);
            holder.thumbnail.setImageResource(R.drawable.pic_gallery_album_empty_album_cover);
        }
        if (this.mUnnamed.equals(entry.label)) {
            holder.label.setText("");
        } else {
            holder.label.setText(entry.label);
        }
    }

    public int getViewTypeCount() {
        return 1;
    }

    public boolean hasStableIds() {
        return false;
    }

    public boolean isEmpty() {
        return false;
    }
}
