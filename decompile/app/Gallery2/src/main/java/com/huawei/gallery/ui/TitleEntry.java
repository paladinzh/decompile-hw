package com.huawei.gallery.ui;

import android.graphics.Bitmap;
import android.graphics.RectF;
import com.android.gallery3d.data.Path;
import com.android.gallery3d.ui.BitmapLoader;
import com.android.gallery3d.ui.StringTexture;
import com.huawei.gallery.data.AddressRegionLoader;
import com.huawei.gallery.data.AddressRegionLoader.AddressRegionListener;
import com.huawei.gallery.data.AddressStringLoader;
import com.huawei.gallery.data.AddressStringLoader.AddressStringListener;
import com.huawei.gallery.ui.TimeAxisLabel.TitleArgs;
import com.huawei.gallery.util.GalleryPool;

public class TitleEntry extends BaseEntry {
    public RectF mAddressRect;
    private AddressRegionLoader mAddressRectLoader;
    private String mAddressString = null;
    private AddressStringLoader mAddressStringLoader;
    private BitmapLoader mAddressTitleLoader = null;
    public long mDatetaken;
    private final int mModeValue;
    private boolean mNeedAddressTitle = true;
    private Bitmap mPreviewBitmap;
    public StringTexture mSimpleTitleTexture;
    private final TitleArgs mTitleArgs;
    private final TitleEntrySetListener mTitleEntrySetListener;
    private final TitleLoaderListener mTitleLoaderListener;

    public TitleEntry(TitleEntrySetListener titleEntrySetListener, TitleLoaderListener loaderListener, AddressRegionListener addressListener, AddressStringListener addressStringListener, TitleArgs titleArgs) {
        AddressRegionLoader addressRegionLoader;
        AddressStringLoader addressStringLoader = null;
        this.mTitleEntrySetListener = titleEntrySetListener;
        this.mTitleLoaderListener = loaderListener;
        this.mTitleArgs = titleArgs;
        this.mModeValue = loaderListener.getCurrentTitleModeValue();
        this.contentLoader = new TitleLoader(titleEntrySetListener, loaderListener, titleArgs);
        if (addressListener == null) {
            addressRegionLoader = null;
        } else {
            addressRegionLoader = new AddressRegionLoader(titleArgs.index, addressListener);
        }
        this.mAddressRectLoader = addressRegionLoader;
        if (addressStringListener != null) {
            addressStringLoader = new AddressStringLoader(titleArgs.index, addressStringListener);
        }
        this.mAddressStringLoader = addressStringLoader;
        this.mDatetaken = titleArgs.groupData.dateTaken;
        this.path = Path.fromString("/title/entry/" + titleArgs.index);
    }

    public boolean startLoad() {
        boolean result = super.startLoad();
        if (this.mAddressRect == null && this.mAddressRectLoader != null) {
            this.mAddressRectLoader.startLoad();
        }
        if (this.mAddressString == null && this.mAddressStringLoader != null) {
            this.mAddressStringLoader.startLoad();
        }
        return result;
    }

    public void cancelLoad() {
        super.cancelLoad();
        if (this.mAddressRectLoader != null) {
            this.mAddressRectLoader.cancelLoad();
        }
        if (this.mAddressStringLoader != null) {
            this.mAddressStringLoader.cancelLoad();
        }
        if (this.mAddressTitleLoader != null) {
            this.mAddressTitleLoader.cancelLoad();
        }
        if (this.mPreviewBitmap != null) {
            GalleryPool.recycle(this.mTitleArgs.groupData.defaultTitle, this.mModeValue, this.mPreviewBitmap, this.mTitleLoaderListener);
            this.mPreviewBitmap = null;
            if (this.bitmapTexture != null) {
                this.bitmapTexture.recycle();
                this.bitmapTexture = null;
            }
            this.content = null;
        }
        recycleSimpleTitleTexture();
    }

    private void recycleSimpleTitleTexture() {
        if (this.mSimpleTitleTexture != null) {
            this.mSimpleTitleTexture.recycle();
            this.mSimpleTitleTexture = null;
        }
    }

    public void recycle() {
        if (!this.inDeleteAnimation) {
            if (this.mPreviewBitmap != null) {
                GalleryPool.recycle(this.mTitleArgs.groupData.defaultTitle, this.mModeValue, this.mPreviewBitmap, this.mTitleLoaderListener);
                this.mPreviewBitmap = null;
            }
            super.recycle();
            if (this.mAddressTitleLoader != null) {
                this.mAddressTitleLoader.recycle();
            }
        }
        if (this.mAddressRectLoader != null) {
            this.mAddressRectLoader.cancelLoad();
        }
        if (this.mAddressStringLoader != null) {
            this.mAddressStringLoader.cancelLoad();
        }
        recycleSimpleTitleTexture();
    }

    public void clearAddressTitleFlag() {
        this.mNeedAddressTitle = false;
        if (this.mAddressTitleLoader != null) {
            this.mAddressTitleLoader.cancelLoad();
        }
    }

    public void updateAddressRect(RectF addressRect) {
        this.mAddressRect = addressRect;
    }

    public void updateAddressString(String address) {
        this.mAddressString = address;
        if (this.mNeedAddressTitle && this.mAddressString != null) {
            this.mTitleArgs.address = this.mAddressString;
            this.mAddressTitleLoader = new TitleLoader(this.mTitleEntrySetListener, this.mTitleLoaderListener, this.mTitleArgs);
            this.mAddressTitleLoader.startLoad();
        }
    }

    public String getTimeTitle() {
        return this.mTitleArgs.groupData.defaultTitle;
    }

    public void updateTexture(Bitmap bitmap, boolean isOpaque, boolean isPreview) {
        super.updateTexture(bitmap, isOpaque, isPreview);
        StringTexture oldTitle = this.mSimpleTitleTexture;
        this.mSimpleTitleTexture = GroupTitlePool.getStringTexture(this.mTitleArgs.groupData.defaultTitle);
        if (oldTitle != null) {
            oldTitle.recycle();
        }
        if (isPreview) {
            this.mPreviewBitmap = bitmap;
        } else if (this.mPreviewBitmap != null) {
            this.mTitleLoaderListener.recycleTitle(this.mPreviewBitmap);
            this.mPreviewBitmap = null;
        }
    }

    public boolean isAddressDrew() {
        if (this.mTitleArgs == null) {
            return false;
        }
        return this.mTitleArgs.isAddressDrew;
    }
}
