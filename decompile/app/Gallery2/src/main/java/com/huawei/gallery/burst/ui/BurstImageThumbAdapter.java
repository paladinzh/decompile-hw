package com.huawei.gallery.burst.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;
import android.view.ViewGroup;
import com.android.gallery3d.util.GalleryLog;
import com.android.gallery3d.util.GalleryUtils;
import com.huawei.gallery.burst.BurstSelectManager;
import com.huawei.gallery.burst.ui.MyGallery.LayoutParams;
import java.util.Arrays;

public class BurstImageThumbAdapter extends BurstImageGalleryAdapterBase {
    private int[] mIndex = new int[20];
    private BurstImageThumbView[] mViewCache = new BurstImageThumbView[20];

    public BurstImageThumbAdapter(Context context, BurstSelectManager selectManager) {
        super(context, selectManager);
        Arrays.fill(this.mIndex, -1);
    }

    private Bitmap getBitmap(int position) {
        if (this.mThumbnailLoader != null) {
            return this.mThumbnailLoader.getThumbnail(position);
        }
        return null;
    }

    private void updateCurrentIndex(MyGallery gallery) {
        if (this.mThumbnailLoader != null && gallery != null) {
            this.mThumbnailLoader.setCurrentIndex((gallery.getFirstVisiblePosition() + gallery.getLastVisiblePosition()) / 2);
        }
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        BurstImageThumbView view;
        updateCurrentIndex((MyGallery) parent);
        boolean selectState = this.mSelectManager.isSelected(position);
        int cacheIndex = position % 20;
        int index = this.mIndex[cacheIndex];
        View cacheView = this.mViewCache[cacheIndex];
        if (index == position && cacheView != null && convertView == null) {
            GalleryLog.d("BurstImageThumbAdapter", "use cached view");
            convertView = cacheView;
        }
        if (convertView == null) {
            view = new BurstImageThumbView(this.mContext, getBitmap(position));
            int size = GalleryUtils.dpToPixel(68);
            view.setLayoutParams(new LayoutParams(size, size));
        } else {
            view = (BurstImageThumbView) convertView;
            view.setImageBitmap(getBitmap(position));
        }
        view.setImgRotation(getImgRotation(position));
        boolean best = false;
        if (this.mThumbnailLoader != null) {
            best = this.mThumbnailLoader.isBest(position);
        }
        view.setBest(best);
        view.setSelectState(selectState);
        this.mIndex[cacheIndex] = position;
        this.mViewCache[cacheIndex] = view;
        return view;
    }

    public void clear() {
        Arrays.fill(this.mViewCache, null);
        super.clear();
    }

    public BurstImageThumbView getViewAt(int position) {
        return this.mViewCache[position % 20];
    }
}
