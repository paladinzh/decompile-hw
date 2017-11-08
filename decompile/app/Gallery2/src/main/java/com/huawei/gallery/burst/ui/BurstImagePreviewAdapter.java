package com.huawei.gallery.burst.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;
import android.view.ViewGroup;
import com.huawei.gallery.burst.BurstSelectManager;
import java.util.Arrays;

public class BurstImagePreviewAdapter extends BurstImageGalleryAdapterBase {
    private int[] mIndex = new int[15];
    private BurstImagePreviewView[] mViewCache = new BurstImagePreviewView[15];

    public BurstImagePreviewAdapter(Context context, BurstSelectManager selectManager) {
        super(context, selectManager);
        Arrays.fill(this.mIndex, -1);
    }

    public void clear() {
        for (int i = 0; i < 15; i++) {
            View v = this.mViewCache[i];
            if (v != null) {
                v.setOnClickListener(null);
            }
            this.mViewCache[i] = null;
        }
        super.clear();
    }

    private Bitmap getBitmap(int position) {
        if (this.mThumbnailLoader != null) {
            return this.mThumbnailLoader.getPreview(position);
        }
        return null;
    }

    void updateViewForImageHeight(int height) {
        for (int i = 0; i < 15; i++) {
            BurstImagePreviewView v = this.mViewCache[i];
            if (v != null) {
                v.onHeightChange(height);
            }
        }
        notifyDataChangedByHandler();
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        BurstImagePreviewView view;
        boolean selectState = this.mSelectManager.isSelected(position);
        int cacheIndex = position % 15;
        int index = this.mIndex[cacheIndex];
        View cacheView = this.mViewCache[cacheIndex];
        if (index == position && cacheView != null && convertView == null) {
            convertView = cacheView;
        }
        if (convertView == null) {
            view = new BurstImagePreviewView(this.mContext);
        } else {
            view = (BurstImagePreviewView) convertView;
        }
        Bitmap bitmap = getBitmap(position);
        if (bitmap == null) {
            Bitmap cover = getCoverBitmap();
            if (cover != null) {
                view.setAspect(cover.getWidth(), cover.getHeight());
            } else {
                view.setAspect(1, 1);
            }
        } else {
            view.setAspect(bitmap.getWidth(), bitmap.getHeight());
        }
        view.updateView(bitmap);
        view.setImgRotation(getImgRotation(position));
        view.onHeightChange(parent.getHeight());
        view.setSelectState(selectState);
        this.mIndex[cacheIndex] = position;
        this.mViewCache[cacheIndex] = view;
        return view;
    }

    public void updateViewForPosition(int index, Bitmap bitmap) {
        BurstImagePreviewView view = getViewAt(index);
        if (view != null) {
            view.updateView(bitmap);
        }
        super.updateViewForPosition(index, bitmap);
    }

    public BurstImagePreviewView getViewAt(int position) {
        return this.mViewCache[position % 15];
    }
}
