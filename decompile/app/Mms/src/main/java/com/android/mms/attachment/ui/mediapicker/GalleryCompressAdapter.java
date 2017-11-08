package com.android.mms.attachment.ui.mediapicker;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;
import com.huawei.cspcommon.MLog;
import java.util.Collection;
import java.util.HashMap;

public class GalleryCompressAdapter extends PagerAdapter {
    private Context mContext;
    private int mDataCount;
    private Cursor mGalleryCursor;
    private HashMap<Integer, GalleryCompressItemView> mItemCache = new HashMap();

    public GalleryCompressAdapter(Context context, Cursor cursor) {
        this.mContext = context;
        this.mGalleryCursor = cursor;
    }

    public int getCount() {
        if (getGalleryCursorValid()) {
            try {
                if (this.mGalleryCursor != null && this.mGalleryCursor.getCount() > 0) {
                    this.mDataCount = this.mGalleryCursor.getCount() - 1;
                    return this.mDataCount;
                }
            } catch (Exception e) {
                MLog.w("GalleryCompressAdapter", "mGalleryCursor error, " + e.getMessage());
            }
        }
        return this.mDataCount;
    }

    public Object instantiateItem(ViewGroup container, int position) {
        boolean cursorValid = true;
        if (!getGalleryCursorValid()) {
            MLog.e("GalleryCompressAdapter", "instantiateItem cursor is valid");
            cursorValid = false;
        }
        if (!this.mGalleryCursor.moveToPosition(position + 1)) {
            MLog.e("GalleryCompressAdapter", "instantiateItem couldn't move cursor to position " + (position + 1));
            cursorValid = false;
        }
        GalleryCompressItemView galleryCompressItemView = new GalleryCompressItemView(this.mContext);
        galleryCompressItemView.setCursorValid(cursorValid);
        View rootView = galleryCompressItemView.getView(container, this.mGalleryCursor);
        rootView.setTag(galleryCompressItemView);
        container.addView(rootView);
        this.mItemCache.put(Integer.valueOf(position + 1), galleryCompressItemView);
        return galleryCompressItemView;
    }

    public void destroyItem(ViewGroup container, int position, Object object) {
        if (this.mItemCache.containsKey(Integer.valueOf(position + 1))) {
            GalleryCompressItemView galleryCompressItemView = (GalleryCompressItemView) this.mItemCache.get(Integer.valueOf(position + 1));
            if (galleryCompressItemView.getRootView() != null) {
                container.removeView(galleryCompressItemView.getRootView());
            }
            galleryCompressItemView.destroyView();
            this.mItemCache.remove(Integer.valueOf(position + 1));
        }
    }

    public boolean isViewFromObject(View view, Object object) {
        return view.getTag() == object;
    }

    public void refreshGalleryCursor(Cursor cursor) {
        this.mGalleryCursor = cursor;
        notifyDataSetChanged();
    }

    private boolean getGalleryCursorValid() {
        return (this.mGalleryCursor == null || this.mGalleryCursor.isClosed()) ? false : true;
    }

    public String getPicturePath(int position) {
        if (this.mItemCache == null || !this.mItemCache.containsKey(Integer.valueOf(position))) {
            return null;
        }
        return ((GalleryCompressItemView) this.mItemCache.get(Integer.valueOf(position))).getSourcePath();
    }

    public String getContentType(int position) {
        if (this.mItemCache == null || !this.mItemCache.containsKey(Integer.valueOf(position))) {
            return null;
        }
        return ((GalleryCompressItemView) this.mItemCache.get(Integer.valueOf(position))).getContentType();
    }

    public String getFileSize(int position) {
        if (this.mItemCache == null || this.mItemCache.get(Integer.valueOf(position)) == null || !this.mItemCache.containsKey(Integer.valueOf(position))) {
            return null;
        }
        return ((GalleryCompressItemView) this.mItemCache.get(Integer.valueOf(position))).getFileSize();
    }

    public void destoryItemCache() {
        if (this.mItemCache != null) {
            if (this.mItemCache.size() == 0) {
                this.mItemCache = null;
                return;
            }
            Collection<GalleryCompressItemView> itemCaches = this.mItemCache.values();
            if (itemCaches != null && itemCaches.size() > 0) {
                for (GalleryCompressItemView itemView : itemCaches) {
                    if (itemView != null) {
                        itemView.destroyView();
                    }
                }
            }
            this.mItemCache.clear();
            this.mItemCache = null;
        }
    }
}
