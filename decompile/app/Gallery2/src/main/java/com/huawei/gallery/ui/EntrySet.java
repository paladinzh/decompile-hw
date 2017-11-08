package com.huawei.gallery.ui;

import android.graphics.Bitmap;
import com.android.gallery3d.common.Utils;
import com.android.gallery3d.data.Path;
import java.util.HashMap;
import java.util.Map.Entry;

public abstract class EntrySet {
    private int mActiveEnd = 0;
    private boolean mActiveIsDirty = false;
    private int mActiveRequestCount = 0;
    private int mActiveStart = 0;
    private int mContentEnd = 0;
    private boolean mContentIsDirty = false;
    private int mContentStart = 0;
    protected final BaseEntry[] mData;
    private final EntrySetListener mEntryListener;
    private boolean mIsActive = false;
    private int mNonActiveRequestCount = 0;
    private int mSize;

    protected abstract BaseEntry getEntry(int i);

    protected abstract Object getObjectIndex(int i);

    protected abstract boolean supportEntry(BaseEntry baseEntry);

    public EntrySet(EntrySetListener entryListener, int cacheSize, int size) {
        this.mData = new BaseEntry[cacheSize];
        this.mSize = size;
        this.mEntryListener = entryListener;
    }

    public void set(EntrySet entrySet) {
        this.mActiveEnd = entrySet.mActiveEnd;
        this.mActiveStart = entrySet.mActiveStart;
        for (int i = 0; i < entrySet.mData.length; i++) {
            if (this.mData[i] == null) {
                this.mData[i] = new BaseEntry();
            }
            this.mData[i].set(entrySet.mData[i]);
        }
    }

    public boolean isActive(int index) {
        return index >= this.mActiveStart && index <= this.mActiveEnd;
    }

    public BaseEntry get(int index) {
        if (!isActive(index)) {
            Utils.fail("invalid slot: %s outsides (%s, %s)", Integer.valueOf(index), Integer.valueOf(this.mActiveStart), Integer.valueOf(this.mActiveEnd));
        }
        return this.mData[index % this.mData.length];
    }

    private boolean requestEntry(int index) {
        boolean z = false;
        if (index < this.mContentStart || index >= this.mContentEnd) {
            return false;
        }
        BaseEntry entry = this.mData[index % this.mData.length];
        if (this.mActiveStart <= index && this.mActiveEnd > index) {
            onRequestEntry(entry);
        }
        if (entry != null) {
            z = entry.startLoad();
        }
        return z;
    }

    protected void onRequestEntry(BaseEntry entry) {
    }

    protected void requestEntryNoActive() {
        int range = Math.max(this.mContentEnd - this.mActiveEnd, this.mActiveStart - this.mContentStart);
        this.mNonActiveRequestCount = 0;
        int i = 0;
        while (i < range) {
            if (requestEntry(this.mActiveEnd + i)) {
                this.mNonActiveRequestCount++;
            }
            if (this.mNonActiveRequestCount != 36) {
                if (requestEntry((this.mActiveStart - 1) - i)) {
                    this.mNonActiveRequestCount++;
                }
                if (this.mNonActiveRequestCount != 36) {
                    i++;
                } else {
                    return;
                }
            }
            return;
        }
    }

    private void cancelEntry(int index) {
        if (index >= this.mContentStart && index < this.mContentEnd) {
            this.mData[index % this.mData.length].cancelLoad();
        }
    }

    private void cancelEntryNoActive() {
        int range = Math.max(this.mContentEnd - this.mActiveEnd, this.mActiveStart - this.mContentStart);
        for (int i = 0; i < range; i++) {
            cancelEntry(this.mActiveEnd + i);
            cancelEntry((this.mActiveStart - 1) - i);
        }
    }

    private void prepareEntry(int index) {
        this.mData[index % this.mData.length] = getEntry(index);
    }

    public void prepareEntry() {
        int n = this.mContentEnd;
        for (int i = this.mContentStart; i < n; i++) {
            prepareEntry(i);
        }
        updateAllRequest();
    }

    private void freeEntry(int index) {
        BaseEntry[] data = this.mData;
        index %= this.mData.length;
        BaseEntry entry = data[index];
        if (entry != null) {
            entry.recycle();
            data[index] = null;
        }
    }

    public void freeEntry() {
        int n = this.mContentEnd;
        for (int i = this.mContentStart; i < n; i++) {
            freeEntry(i);
        }
    }

    public void updateAllRequest() {
        this.mActiveRequestCount = 0;
        int n = this.mActiveEnd;
        for (int i = this.mActiveStart; i < n; i++) {
            if (requestEntry(i)) {
                this.mActiveRequestCount++;
            }
        }
        if (this.mActiveRequestCount == 0) {
            requestEntryNoActive();
        } else {
            cancelEntryNoActive();
        }
    }

    private void uploadBgTexture(int index) {
        if (index < this.mContentEnd && index >= this.mContentStart) {
            this.mEntryListener.addBgTexture(this.mData[index % this.mData.length].bitmapTexture);
        }
    }

    private void updateTextureUploadQueue() {
        if (this.mIsActive) {
            int i;
            int n = this.mActiveEnd;
            for (i = this.mActiveStart; i < n; i++) {
                this.mEntryListener.addFgTexture(this.mData[i % this.mData.length].bitmapTexture);
            }
            int range = Math.max(this.mContentEnd - this.mActiveEnd, this.mActiveStart - this.mContentStart);
            for (i = 0; i < range; i++) {
                uploadBgTexture(this.mActiveEnd + i);
                uploadBgTexture((this.mActiveStart - i) - 1);
            }
        }
    }

    private void setContentWindow(int contentStart, int contentEnd) {
        if (contentStart != this.mContentStart || contentEnd != this.mContentEnd || this.mContentIsDirty) {
            updateSourceRange(contentStart, contentEnd);
            int oldStart = this.mContentStart;
            int oldEnd = this.mContentEnd;
            this.mContentStart = contentStart;
            this.mContentEnd = contentEnd;
            this.mContentIsDirty = false;
            int n;
            int i;
            if (contentStart >= oldEnd || oldStart >= contentEnd) {
                n = oldEnd;
                for (i = oldStart; i < oldEnd; i++) {
                    freeEntry(i);
                }
                for (i = contentStart; i < contentEnd; i++) {
                    prepareEntry(i);
                }
            } else {
                for (i = oldStart; i < contentStart; i++) {
                    freeEntry(i);
                }
                n = oldEnd;
                for (i = contentEnd; i < oldEnd; i++) {
                    freeEntry(i);
                }
                n = oldStart;
                for (i = contentStart; i < oldStart; i++) {
                    prepareEntry(i);
                }
                for (i = oldEnd; i < contentEnd; i++) {
                    prepareEntry(i);
                }
            }
        }
    }

    public boolean checkActiveRegion(int start, int end) {
        return this.mActiveStart == start && this.mActiveEnd == end && !this.mActiveIsDirty;
    }

    public void setActiveWindow(int start, int end) {
        BaseEntry[] data;
        int contentStart;
        if (start <= end && end - start <= this.mData.length) {
            if (end > this.mSize) {
            }
            data = this.mData;
            this.mActiveStart = start;
            this.mActiveEnd = end;
            this.mActiveIsDirty = false;
            contentStart = Utils.clamp(((start + end) / 2) - (data.length / 2), 0, Math.max(0, this.mSize - data.length));
            setContentWindow(contentStart, Math.min(data.length + contentStart, this.mSize));
            updateUIRange(this.mActiveStart, this.mActiveEnd);
            updateTextureUploadQueue();
            if (this.mIsActive) {
                updateAllRequest();
            }
        }
        Utils.fail("%s, %s, %s, %s", Integer.valueOf(start), Integer.valueOf(end), Integer.valueOf(this.mData.length), Integer.valueOf(this.mSize));
        data = this.mData;
        this.mActiveStart = start;
        this.mActiveEnd = end;
        this.mActiveIsDirty = false;
        contentStart = Utils.clamp(((start + end) / 2) - (data.length / 2), 0, Math.max(0, this.mSize - data.length));
        setContentWindow(contentStart, Math.min(data.length + contentStart, this.mSize));
        updateUIRange(this.mActiveStart, this.mActiveEnd);
        updateTextureUploadQueue();
        if (this.mIsActive) {
            updateAllRequest();
        }
    }

    public void updateEntry(int index) {
        if (index >= this.mContentStart && index < this.mContentEnd) {
            freeEntry(index);
            prepareEntry(index);
            updateAllRequest();
        }
    }

    public void updateRange(int count) {
        if (this.mContentEnd > count) {
            this.mContentEnd = count;
            this.mContentIsDirty = true;
        }
        if (this.mActiveEnd > count) {
            this.mActiveEnd = count;
            this.mActiveIsDirty = true;
        }
        this.mSize = count;
    }

    public void updateTexture(int index, Bitmap bitmap, boolean isStateError) {
        BaseEntry entry = this.mData[index % this.mData.length];
        if (entry != null) {
            if (bitmap == null) {
                if (isStateError) {
                    entry.isNoThumb = true;
                    if (isActive(index)) {
                        this.mEntryListener.invalidate();
                        this.mActiveRequestCount--;
                        if (this.mActiveRequestCount == 0) {
                            requestEntryNoActive();
                        }
                    } else {
                        this.mNonActiveRequestCount--;
                        if (this.mNonActiveRequestCount <= 0) {
                            requestEntryNoActive();
                        }
                    }
                }
                return;
            }
            entry.updateTexture(bitmap, isBitmapTextureOpaque(), false);
            if (isActive(index)) {
                this.mEntryListener.addFgTexture(entry.bitmapTexture);
                this.mActiveRequestCount--;
                if (this.mActiveRequestCount == 0) {
                    requestEntryNoActive();
                }
            } else {
                this.mEntryListener.addBgTexture(entry.bitmapTexture);
                this.mNonActiveRequestCount--;
                if (this.mNonActiveRequestCount <= 0) {
                    requestEntryNoActive();
                }
            }
        }
    }

    public void updatePreviewTexture(int index, Bitmap bitmap) {
        BaseEntry entry = this.mData[index % this.mData.length];
        if (entry != null && bitmap != null) {
            entry.updateTexture(bitmap, isBitmapTextureOpaque(), true);
            if (isActive(index)) {
                this.mEntryListener.addFgTexture(entry.bitmapTexture);
            } else {
                this.mEntryListener.addBgTexture(entry.bitmapTexture);
            }
        }
    }

    protected void updateSourceRange(int start, int end) {
    }

    protected void updateUIRange(int start, int end) {
    }

    protected boolean isBitmapTextureOpaque() {
        return true;
    }

    public void resume() {
        this.mIsActive = true;
    }

    public void pause() {
        this.mIsActive = false;
    }

    public void prepareVisibleRangeItemIndex(HashMap<Path, Object> visiblePathMap, HashMap<Object, Object> visibleIndexMap) {
        for (int index = this.mContentStart; index < this.mContentEnd; index++) {
            BaseEntry entry = this.mData[index % this.mData.length];
            if (!(entry == null || entry.path == null)) {
                entry.index = getObjectIndex(index);
                entry.inDeleteAnimation = true;
                entry.guessDeleted = true;
                visiblePathMap.put(entry.path, entry);
                visibleIndexMap.put(entry.index, entry);
            }
        }
    }

    public void freeVisibleRangeItem(HashMap<Path, Object> visiblePathMap) {
        for (int index = this.mContentStart; index < this.mContentEnd; index++) {
            BaseEntry entry = this.mData[index % this.mData.length];
            if (!(entry == null || entry.path == null)) {
                BaseEntry lastEntry = (BaseEntry) visiblePathMap.get(entry.path);
                if (lastEntry != null && entry == lastEntry) {
                    lastEntry.index = null;
                    lastEntry.inDeleteAnimation = false;
                    visiblePathMap.remove(entry.path);
                }
            }
        }
        for (Entry<Path, Object> entry2 : visiblePathMap.entrySet()) {
            BaseEntry baseEntry = (BaseEntry) entry2.getValue();
            if (baseEntry != null && supportEntry(baseEntry)) {
                baseEntry.inDeleteAnimation = false;
                baseEntry.recycle();
            }
        }
    }
}
