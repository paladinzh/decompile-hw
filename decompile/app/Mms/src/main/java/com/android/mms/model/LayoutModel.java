package com.android.mms.model;

import com.android.mms.layout.LayoutManager;
import com.android.mms.layout.LayoutParameters;
import com.huawei.cspcommon.MLog;
import java.util.ArrayList;

public class LayoutModel extends Model {
    private RegionModel mImageRegion;
    private LayoutParameters mLayoutParams = LayoutManager.getInstance().getLayoutParameters();
    private int mLayoutType = 0;
    private ArrayList<RegionModel> mNonStdRegions;
    private RegionModel mRootLayout;
    private RegionModel mTextRegion;

    public LayoutModel() {
        createDefaultRootLayout();
        createDefaultImageRegion();
        createDefaultTextRegion();
    }

    public LayoutModel(RegionModel rootLayout, ArrayList<RegionModel> regions) {
        this.mRootLayout = rootLayout;
        this.mNonStdRegions = new ArrayList();
        for (RegionModel r : regions) {
            String rId = r.getRegionId();
            if (rId.equals("Image")) {
                this.mImageRegion = r;
            } else if (rId.equals("Text")) {
                this.mTextRegion = r;
            } else {
                this.mNonStdRegions.add(r);
            }
        }
        validateLayouts();
    }

    private void createDefaultRootLayout() {
        this.mRootLayout = new RegionModel(null, 0, 0, this.mLayoutParams.getWidth(), this.mLayoutParams.getHeight());
    }

    private void createDefaultImageRegion() {
        if (this.mRootLayout == null) {
            throw new IllegalStateException("Root-Layout uninitialized.");
        }
        this.mImageRegion = new RegionModel("Image", 0, 0, this.mRootLayout.getWidth(), this.mLayoutParams.getImageHeight());
    }

    private void createDefaultTextRegion() {
        if (this.mRootLayout == null) {
            throw new IllegalStateException("Root-Layout uninitialized.");
        }
        this.mTextRegion = new RegionModel("Text", 0, this.mLayoutParams.getImageHeight(), this.mRootLayout.getWidth(), this.mLayoutParams.getTextHeight());
    }

    private void validateLayouts() {
        int i = 1;
        if (this.mRootLayout == null) {
            createDefaultRootLayout();
        }
        boolean hasNoImageRegion = false;
        if (this.mImageRegion == null) {
            createDefaultImageRegion();
            hasNoImageRegion = true;
        }
        boolean hasNoTextRegion = false;
        if (this.mTextRegion == null) {
            createDefaultTextRegion();
            hasNoTextRegion = true;
        }
        if (this.mImageRegion.getTop() > this.mTextRegion.getTop()) {
            this.mLayoutType = 1;
        } else if (this.mImageRegion.getTop() < this.mTextRegion.getTop()) {
            this.mLayoutType = 0;
        } else if (this.mImageRegion.getLeft() > this.mTextRegion.getLeft()) {
            this.mLayoutType = 2;
        } else if (this.mImageRegion.getLeft() < this.mTextRegion.getLeft()) {
            this.mLayoutType = 3;
        } else {
            int i2;
            if (this.mImageRegion.getTop() == 0) {
                i2 = 0;
            } else {
                i2 = 1;
            }
            this.mLayoutType = i2;
            if (hasNoImageRegion && !hasNoTextRegion) {
                if (this.mTextRegion.getTop() != 0) {
                    i = 0;
                }
                this.mLayoutType = i;
            }
        }
    }

    public RegionModel getImageRegion() {
        return this.mImageRegion;
    }

    public RegionModel getTextRegion() {
        return this.mTextRegion;
    }

    public ArrayList<RegionModel> getRegions() {
        ArrayList<RegionModel> regions = new ArrayList();
        if (this.mImageRegion != null) {
            regions.add(this.mImageRegion);
        }
        if (this.mTextRegion != null) {
            regions.add(this.mTextRegion);
        }
        return regions;
    }

    public RegionModel findRegionById(String rId) {
        if ("Image".equals(rId)) {
            return this.mImageRegion;
        }
        if ("Text".equals(rId)) {
            return this.mTextRegion;
        }
        for (RegionModel r : this.mNonStdRegions) {
            if (r.getRegionId().equals(rId)) {
                return r;
            }
        }
        return null;
    }

    public int getLayoutWidth() {
        return this.mRootLayout.getWidth();
    }

    public int getLayoutHeight() {
        return this.mRootLayout.getHeight();
    }

    public String getBackgroundColor() {
        return this.mRootLayout.getBackgroundColor();
    }

    public void changeTo(int layout) {
        if (this.mRootLayout == null) {
            throw new IllegalStateException("Root-Layout uninitialized.");
        }
        if (this.mLayoutParams == null) {
            this.mLayoutParams = LayoutManager.getInstance().getLayoutParameters();
        }
        if (this.mLayoutType != layout) {
            switch (layout) {
                case 0:
                    this.mImageRegion.setTop(0);
                    this.mImageRegion.setLeft(0);
                    this.mImageRegion.setWidth(this.mRootLayout.getWidth());
                    this.mImageRegion.setHeight(this.mLayoutParams.getImageHeight());
                    this.mTextRegion.setTop(this.mLayoutParams.getImageHeight());
                    this.mTextRegion.setLeft(0);
                    this.mTextRegion.setWidth(this.mRootLayout.getWidth());
                    this.mTextRegion.setHeight(this.mLayoutParams.getTextHeight());
                    this.mLayoutType = layout;
                    notifyModelChanged(true);
                    return;
                case 1:
                    this.mImageRegion.setTop(this.mLayoutParams.getTextHeight());
                    this.mImageRegion.setLeft(0);
                    this.mImageRegion.setWidth(this.mRootLayout.getWidth());
                    this.mImageRegion.setHeight(this.mLayoutParams.getImageHeight());
                    this.mTextRegion.setTop(0);
                    this.mTextRegion.setLeft(0);
                    this.mTextRegion.setWidth(this.mRootLayout.getWidth());
                    this.mTextRegion.setHeight(this.mLayoutParams.getTextHeight());
                    this.mLayoutType = layout;
                    notifyModelChanged(true);
                    return;
                case 2:
                    this.mImageRegion.setTop(0);
                    this.mImageRegion.setLeft(this.mLayoutParams.getTextWidth());
                    this.mImageRegion.setWidth(this.mLayoutParams.getImageWidth());
                    this.mImageRegion.setHeight(this.mRootLayout.getHeight());
                    this.mTextRegion.setTop(0);
                    this.mTextRegion.setLeft(0);
                    this.mTextRegion.setWidth(this.mLayoutParams.getTextWidth());
                    this.mTextRegion.setHeight(this.mRootLayout.getHeight());
                    this.mLayoutType = layout;
                    notifyModelChanged(true);
                    return;
                case 3:
                    this.mImageRegion.setTop(0);
                    this.mImageRegion.setLeft(0);
                    this.mImageRegion.setWidth(this.mLayoutParams.getImageWidth());
                    this.mImageRegion.setHeight(this.mRootLayout.getHeight());
                    this.mTextRegion.setTop(0);
                    this.mTextRegion.setLeft(this.mLayoutParams.getImageWidth());
                    this.mTextRegion.setWidth(this.mLayoutParams.getTextWidth());
                    this.mTextRegion.setHeight(this.mRootLayout.getHeight());
                    this.mLayoutType = layout;
                    notifyModelChanged(true);
                    return;
                default:
                    MLog.w("Mms/slideshow", "Unknown layout type: " + layout);
                    return;
            }
        }
    }

    public int getLayoutType() {
        return this.mLayoutType;
    }

    protected void registerModelChangedObserverInDescendants(IModelChangedObserver observer) {
        if (this.mRootLayout != null) {
            this.mRootLayout.registerModelChangedObserver(observer);
        }
        if (this.mImageRegion != null) {
            this.mImageRegion.registerModelChangedObserver(observer);
        }
        if (this.mTextRegion != null) {
            this.mTextRegion.registerModelChangedObserver(observer);
        }
    }

    protected void unregisterModelChangedObserverInDescendants(IModelChangedObserver observer) {
        if (this.mRootLayout != null) {
            this.mRootLayout.unregisterModelChangedObserver(observer);
        }
        if (this.mImageRegion != null) {
            this.mImageRegion.unregisterModelChangedObserver(observer);
        }
        if (this.mTextRegion != null) {
            this.mTextRegion.unregisterModelChangedObserver(observer);
        }
    }

    protected void unregisterAllModelChangedObserversInDescendants() {
        if (this.mRootLayout != null) {
            this.mRootLayout.unregisterAllModelChangedObservers();
        }
        if (this.mImageRegion != null) {
            this.mImageRegion.unregisterAllModelChangedObservers();
        }
        if (this.mTextRegion != null) {
            this.mTextRegion.unregisterAllModelChangedObservers();
        }
    }
}
