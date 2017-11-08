package com.fyusion.sdk.common;

import android.graphics.Bitmap;
import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

/* compiled from: Unknown */
public class h implements Serializable {
    private static final String TAG = "FyuseDescriptor";
    private transient Bitmap blurImage;
    public String fyuseId;
    protected boolean hasLowResolutionSlice = true;
    protected int lowResolutionSliceIndex = 0;
    protected n magic;
    protected String name;
    protected int previewHeight = 0;
    protected int previewWidth = 0;
    protected long timeStamp = 0;
    protected List<Integer> traversalIndex = null;
    protected String url;
    protected String userName;

    public Bitmap getBlurImage() {
        return this.blurImage;
    }

    public int getHeight(boolean z) {
        return (!z && this.hasLowResolutionSlice) ? this.previewHeight : getMagic().getHeight();
    }

    public String getId() {
        return this.fyuseId;
    }

    public n getMagic() {
        return this.magic;
    }

    public int getPreviewHeight() {
        return this.previewHeight;
    }

    public int getPreviewWidth() {
        return this.previewWidth;
    }

    public String getUrl() {
        return this.url;
    }

    public int getWidth(boolean z) {
        return (!z && this.hasLowResolutionSlice) ? this.previewWidth : getMagic().getWidth();
    }

    public boolean hasLowResolutionSlice() {
        return this.hasLowResolutionSlice;
    }

    public void setBlurImage(Bitmap bitmap) {
        this.blurImage = bitmap;
    }

    public void setHasLowResolutionSlice(boolean z) {
        this.hasLowResolutionSlice = z;
    }

    public void setId(String str) {
        this.fyuseId = str;
    }

    public void setMagic(n nVar) {
        this.magic = nVar;
    }

    public void setName(String str) {
        this.name = str;
    }

    public void setPreviewHeight(int i) {
        this.previewHeight = i;
    }

    public void setPreviewWidth(int i) {
        this.previewWidth = i;
    }

    public void setTimeStamp(long j) {
        this.timeStamp = j;
    }

    public void setUrl(String str) {
        this.url = str;
    }

    public void setUserName(String str) {
        this.userName = str;
    }

    public List<Integer> sliceTraversalIndex() {
        if (this.traversalIndex == null) {
            this.traversalIndex = new LinkedList();
            int thumbSlice = getMagic().getThumbSlice();
            int noSlices = getMagic().getNoSlices();
            int i = 1;
            this.traversalIndex.add(Integer.valueOf(thumbSlice));
            while (true) {
                int i2 = thumbSlice - i;
                if (i2 >= 0) {
                    this.traversalIndex.add(Integer.valueOf(i2));
                }
                int i3 = thumbSlice + i;
                if (i3 < noSlices) {
                    this.traversalIndex.add(Integer.valueOf(i3));
                }
                if (i3 >= noSlices) {
                    if (i2 < 0) {
                        break;
                    }
                }
                i++;
            }
        }
        return this.traversalIndex;
    }

    public String toString() {
        return "FyuseDescriptor{, fyuseId='" + this.fyuseId + '\'' + ", name='" + this.name + '\'' + ", userName='" + this.userName + '\'' + ", url='" + this.url + '\'' + ", hasLowResolutionSlice=" + this.hasLowResolutionSlice + ", previewWidth=" + this.previewWidth + ", previewHeight=" + this.previewHeight + ", lowResolutionSliceIndex=" + this.lowResolutionSliceIndex + ", magic=" + this.magic + ", timeStamp=" + this.timeStamp + ", traversalIndex=" + this.traversalIndex + '}';
    }
}
