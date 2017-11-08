package com.huawei.gallery.ui;

import com.huawei.gallery.photoshare.utils.JobBulk;
import com.huawei.gallery.photoshare.utils.PhotoShareUtils;

public class ItemEntrySet extends EntrySet {
    private JobBulk mJobBulk = PhotoShareUtils.getBulk(1);
    private final ItemEntrySetListener mListener;
    private final ThumbnailLoaderListener mLoaderListener;

    public ItemEntrySet(ItemEntrySetListener listener, ThumbnailLoaderListener loaderListener, int cacheSize, int size) {
        super(listener, cacheSize, size);
        this.mListener = listener;
        this.mLoaderListener = loaderListener;
    }

    protected BaseEntry getEntry(int index) {
        return new MediaItemEntry(this.mLoaderListener, index, this.mListener.getMediaItem(index));
    }

    public void updateAllRequest() {
        this.mJobBulk.beginUpdateActiveList();
        super.updateAllRequest();
        this.mJobBulk.endUpdateActiveList();
    }

    protected void onRequestEntry(BaseEntry entry) {
        if (entry != null) {
            this.mJobBulk.addItem(entry.path);
        }
    }

    protected Object getObjectIndex(int index) {
        return this.mListener.getItemObjectIndex(index);
    }

    protected boolean supportEntry(BaseEntry baseEntry) {
        return baseEntry instanceof MediaItemEntry;
    }

    protected void updateSourceRange(int start, int end) {
        this.mListener.updateSourceRange(start, end);
    }

    protected void updateUIRange(int start, int end) {
        this.mListener.updateUIRange(start, end);
    }

    protected void requestEntryNoActive() {
        this.mListener.requestNoneActiveEntry();
        super.requestEntryNoActive();
    }
}
