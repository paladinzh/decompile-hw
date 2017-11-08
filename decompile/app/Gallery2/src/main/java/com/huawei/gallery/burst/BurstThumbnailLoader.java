package com.huawei.gallery.burst;

import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;
import android.os.Process;
import android.util.SparseArray;
import com.android.gallery3d.common.Utils;
import com.android.gallery3d.data.MediaItem;
import com.android.gallery3d.gadget.XmlUtils;
import com.android.gallery3d.util.GalleryLog;
import com.android.gallery3d.util.GalleryUtils;
import com.android.gallery3d.util.ThreadPool;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BurstThumbnailLoader {
    private static final Pattern BURST_PATTERN_OTHERS = Pattern.compile("([^/]*_BURST)\\d{3}.(jpe?g|JPE?G)$");
    private int mActiveEnd;
    private int mActiveStart;
    private int mBestPhotoIndex = -1;
    private int mCurrentIndex = 0;
    private Handler mHandler = new Handler(Looper.getMainLooper());
    private ImageEntry[] mImageCache = new ImageEntry[25];
    private final ThumbNailListener mListener;
    private ReloadTask mReloadTask;
    private int mSize = 0;
    private final SparseArray<MediaItem> mSourceItems;
    private boolean mSupportBestPhoto = false;

    public interface ThumbNailListener {
        void onBestPhotoFound(int i);

        void onScreenNailLoaded(Bitmap bitmap, Bitmap bitmap2, int i);
    }

    private class BestPhotoIndex implements Runnable {
        private int mIndex;

        BestPhotoIndex(int index) {
            this.mIndex = index;
        }

        public void run() {
            GalleryLog.d("BurstThumbnailLoader", "BestPhotoIndex run : " + this.mIndex);
            if (BurstThumbnailLoader.this.mListener != null) {
                BurstThumbnailLoader.this.mListener.onBestPhotoFound(this.mIndex);
            }
        }
    }

    private class Callback implements Runnable {
        private final ImageEntry entry;
        private final int index;

        Callback(ImageEntry imageEntry, int imageIndex) {
            this.entry = imageEntry;
            this.index = imageIndex;
        }

        public void run() {
            GalleryLog.d("BurstThumbnailLoader", "Callback onScreenNailLoaded " + this.index);
            if (BurstThumbnailLoader.this.mListener != null) {
                BurstThumbnailLoader.this.mListener.onScreenNailLoaded(this.entry.thumbnail, this.entry.microThumNail, this.index);
            }
        }
    }

    private static class ImageEntry {
        public boolean isBest;
        public Bitmap microThumNail;
        public int rotation;
        public Bitmap thumbnail;
        private long version;

        private ImageEntry() {
            this.version = -1;
        }
    }

    private class ReloadTask extends Thread {
        private volatile boolean mActive;
        private volatile boolean mDirty;

        private ReloadTask() {
            this.mActive = true;
            this.mDirty = true;
        }

        public void run() {
            Process.setThreadPriority(10);
            while (this.mActive) {
                synchronized (this) {
                    if (this.mDirty || !this.mActive) {
                        BurstThumbnailLoader.this.mHandler.removeCallbacksAndMessages(null);
                        BurstThumbnailLoader.this.findBestPhoto();
                        this.mDirty = false;
                        int start = BurstThumbnailLoader.this.mActiveStart;
                        int end = BurstThumbnailLoader.this.mActiveEnd;
                        int post = BurstThumbnailLoader.this.mCurrentIndex;
                        int pre = post - 1;
                        while (true) {
                            if (pre < start && post >= end) {
                                break;
                            }
                            if (post < end) {
                                reloadItem(post);
                            }
                            if (pre >= start) {
                                reloadItem(pre);
                            }
                            pre--;
                            post++;
                        }
                        GalleryLog.d("BurstThumbnailLoader", "dataLoader onLoadFinished");
                    } else {
                        Utils.waitWithoutInterrupt(this);
                    }
                }
            }
        }

        private void reloadItem(int index) {
            MediaItem item = (MediaItem) BurstThumbnailLoader.this.mSourceItems.get(index);
            if (item != null) {
                int dataIndex = index % 25;
                ImageEntry entry = BurstThumbnailLoader.this.mImageCache[dataIndex];
                if (entry == null) {
                    entry = new ImageEntry();
                }
                long version = item.getDataVersion();
                if (entry.version != version) {
                    GalleryLog.d("BurstThumbnailLoader", " reload data index: " + index);
                    BurstThumbnailLoader.this.mImageCache[dataIndex] = null;
                    entry.version = version;
                    if (BurstThumbnailLoader.this.mBestPhotoIndex == -2) {
                        entry.isBest = item.isBurstCover();
                        BurstThumbnailLoader.this.mBestPhotoIndex = index;
                    } else {
                        entry.isBest = BurstThumbnailLoader.this.mBestPhotoIndex == index;
                    }
                    entry.thumbnail = (Bitmap) item.requestImage(1).run(ThreadPool.JOB_CONTEXT_STUB);
                    entry.microThumNail = (Bitmap) item.requestImage(2).run(ThreadPool.JOB_CONTEXT_STUB);
                    entry.rotation = item.getRotation();
                    BurstThumbnailLoader.this.mImageCache[dataIndex] = entry;
                    BurstThumbnailLoader.this.mHandler.post(new Callback(entry, index));
                }
            }
        }

        public synchronized void notifyDirty() {
            this.mDirty = true;
            notifyAll();
        }

        public synchronized void terminate() {
            this.mActive = false;
            notifyAll();
        }
    }

    public BurstThumbnailLoader(ArrayList<MediaItem> itemArray, ThumbNailListener listener) {
        int index = 0;
        this.mSize = itemArray.size();
        this.mSourceItems = new SparseArray(this.mSize);
        for (MediaItem item : itemArray) {
            int index2 = index + 1;
            this.mSourceItems.append(index, item);
            index = index2;
        }
        updateSlidingWindow();
        this.mListener = listener;
    }

    public Bitmap getPreview(int position) {
        ImageEntry entry = get(position);
        if (entry != null) {
            return entry.thumbnail;
        }
        return null;
    }

    public Bitmap getThumbnail(int position) {
        ImageEntry entry = get(position);
        if (entry != null) {
            return entry.microThumNail;
        }
        return null;
    }

    public int getRotation(int position) {
        ImageEntry entry = get(position);
        return entry != null ? entry.rotation : 0;
    }

    public boolean isBest(int position) {
        ImageEntry entry = get(position);
        return entry != null ? entry.isBest : false;
    }

    private ImageEntry get(int position) {
        return this.mImageCache[position % 25];
    }

    public void setCurrentIndex(int index) {
        if (this.mCurrentIndex != index) {
            GalleryLog.d("BurstThumbnailLoader", "current index change from " + this.mCurrentIndex + " to " + index);
            this.mCurrentIndex = index;
            updateSlidingWindow();
        }
    }

    public int size() {
        return this.mSize;
    }

    private void updateSlidingWindow() {
        int start = Utils.clamp(this.mCurrentIndex - 12, 0, Math.max(0, this.mSize - 25));
        int end = Math.min(this.mSize, start + 25);
        GalleryLog.d("BurstThumbnailLoader", String.format("updateSlidingWindow current:[%s,%s) target:[%s,%s)", new Object[]{Integer.valueOf(this.mActiveStart), Integer.valueOf(this.mActiveEnd), Integer.valueOf(start), Integer.valueOf(end)}));
        if (this.mActiveStart != start || this.mActiveEnd != end) {
            if (this.mCurrentIndex < this.mActiveStart + 5 || this.mCurrentIndex >= this.mActiveEnd - 5) {
                this.mActiveStart = start;
                this.mActiveEnd = end;
                if (this.mReloadTask != null) {
                    this.mReloadTask.notifyDirty();
                }
                return;
            }
            GalleryLog.d("BurstThumbnailLoader", "index in cache range, no need to reload.");
        }
    }

    public void resume() {
        this.mReloadTask = new ReloadTask();
        this.mReloadTask.start();
    }

    public void pause() {
        int len = this.mImageCache.length;
        for (int i = 0; i < len; i++) {
            this.mImageCache[i] = null;
        }
        this.mHandler.removeCallbacksAndMessages(null);
        if (this.mReloadTask != null) {
            this.mReloadTask.terminate();
            this.mReloadTask = null;
        }
    }

    private void findBestPhoto() {
        if (this.mBestPhotoIndex == -1) {
            int len = this.mSourceItems.size();
            if (len == 0) {
                GalleryLog.d("BurstThumbnailLoader", "there is none item.");
                return;
            }
            MediaItem last = (MediaItem) this.mSourceItems.get(len - 1);
            if (last == null) {
                GalleryLog.e("BurstThumbnailLoader", "can't find last item.");
                return;
            }
            String fileName = findBestPhotoName(last.getFilePath());
            if (fileName == null) {
                GalleryLog.w("BurstThumbnailLoader", "can't find target(best) item.");
                if (this.mSupportBestPhoto) {
                    setBestPhotoIndex(findCoverIndex(this.mSourceItems, len));
                }
                return;
            }
            GalleryLog.d("BurstThumbnailLoader", "best photo name : " + fileName);
            for (int i = 0; i < len; i++) {
                String path = ((MediaItem) this.mSourceItems.get(i)).getFilePath();
                if (path.endsWith(fileName)) {
                    GalleryLog.d("BurstThumbnailLoader", "found target(best) item. " + path);
                    setBestPhotoIndex(i);
                    return;
                }
            }
            if (this.mSupportBestPhoto) {
                setBestPhotoIndex(findCoverIndex(this.mSourceItems, len));
            }
        }
    }

    private void setBestPhotoIndex(int bestPhotoIndex) {
        this.mBestPhotoIndex = bestPhotoIndex;
        setCurrentIndex(bestPhotoIndex);
        if (this.mBestPhotoIndex != -2) {
            this.mHandler.post(new BestPhotoIndex(this.mBestPhotoIndex));
        }
    }

    private int findCoverIndex(SparseArray<MediaItem> items, int len) {
        int i = 0;
        while (i < len) {
            MediaItem item = (MediaItem) items.get(i);
            if (item == null || !item.isBurstCover()) {
                i++;
            } else {
                GalleryLog.d("BurstThumbnailLoader", "use cover as best photo : " + i);
                return i;
            }
        }
        return -2;
    }

    private String findBestPhotoName(String infoFromFile) {
        Throwable th;
        RandomAccessFile randomAccessFile = null;
        if (infoFromFile == null) {
            return null;
        }
        try {
            Matcher matcher = BURST_PATTERN_OTHERS.matcher(infoFromFile);
            if (matcher.find()) {
                String burstId = matcher.group(1);
                String surfix = matcher.group(2);
                RandomAccessFile file = new RandomAccessFile(new File(infoFromFile), "r");
                try {
                    long len = file.length();
                    byte[] nameBuffer = new byte[8];
                    byte[] dataLengthBuffer = new byte[4];
                    file.seek(len - 12);
                    int dataLengthBytes = file.read(dataLengthBuffer);
                    if (file.read(nameBuffer) == 8 && dataLengthBytes == 4 && Arrays.equals(nameBuffer, "best\u0000\u0000\u0000\u0000".getBytes(XmlUtils.INPUT_ENCODING))) {
                        int dataLength = GalleryUtils.littleEdianByteArrayToInt(dataLengthBuffer, 0, 4);
                        if (dataLength > 4) {
                            GalleryLog.d("BurstThumbnailLoader", "dataLength error. " + dataLength);
                            if (file != null) {
                                try {
                                    file.close();
                                } catch (IOException e) {
                                    GalleryLog.i("BurstThumbnailLoader", "RandomAccessFile.close() failed int findBestPhotoName() method, reason: IOException.");
                                }
                            }
                            return null;
                        }
                        this.mSupportBestPhoto = true;
                        file.seek((len - 12) - ((long) dataLength));
                        byte[] indexBuffer = new byte[dataLength];
                        int index = GalleryUtils.littleEdianByteArrayToInt(indexBuffer, 0, file.read(indexBuffer));
                        GalleryLog.d("BurstThumbnailLoader", "found sequence . " + index);
                        String cover = index == 1 ? "_COVER" : "";
                        String format = String.format("%s%03d%s.%s", new Object[]{burstId, Integer.valueOf(index), cover, surfix});
                        if (file != null) {
                            try {
                                file.close();
                            } catch (IOException e2) {
                                GalleryLog.i("BurstThumbnailLoader", "RandomAccessFile.close() failed int findBestPhotoName() method, reason: IOException.");
                            }
                        }
                        return format;
                    }
                    int i;
                    StringBuffer stringBuffer = new StringBuffer("headInfo: ");
                    stringBuffer.append(" dataLen");
                    for (i = 0; i < 4; i++) {
                        stringBuffer.append(" [").append(i).append("]=").append(dataLengthBuffer[i]);
                    }
                    stringBuffer.append(" nameBuffer");
                    for (i = 0; i < 8; i++) {
                        stringBuffer.append(" [").append(i).append("]=").append((char) nameBuffer[i]).append("|").append(nameBuffer[i]);
                    }
                    GalleryLog.w("BurstThumbnailLoader", stringBuffer.toString());
                    if (file != null) {
                        try {
                            file.close();
                        } catch (IOException e3) {
                            GalleryLog.i("BurstThumbnailLoader", "RandomAccessFile.close() failed int findBestPhotoName() method, reason: IOException.");
                        }
                    }
                    return null;
                } catch (FileNotFoundException e4) {
                    randomAccessFile = file;
                    GalleryLog.i("BurstThumbnailLoader", "findBestPhotoName() failed, reason: FileNotFoundException.");
                    if (randomAccessFile != null) {
                        try {
                            randomAccessFile.close();
                        } catch (IOException e5) {
                            GalleryLog.i("BurstThumbnailLoader", "RandomAccessFile.close() failed int findBestPhotoName() method, reason: IOException.");
                        }
                    }
                    return null;
                } catch (IOException e6) {
                    randomAccessFile = file;
                    try {
                        GalleryLog.i("BurstThumbnailLoader", "findBestPhotoName() failed, reason: IOException.");
                        if (randomAccessFile != null) {
                            try {
                                randomAccessFile.close();
                            } catch (IOException e7) {
                                GalleryLog.i("BurstThumbnailLoader", "RandomAccessFile.close() failed int findBestPhotoName() method, reason: IOException.");
                            }
                        }
                        return null;
                    } catch (Throwable th2) {
                        th = th2;
                        if (randomAccessFile != null) {
                            try {
                                randomAccessFile.close();
                            } catch (IOException e8) {
                                GalleryLog.i("BurstThumbnailLoader", "RandomAccessFile.close() failed int findBestPhotoName() method, reason: IOException.");
                            }
                        }
                        throw th;
                    }
                } catch (Throwable th3) {
                    th = th3;
                    randomAccessFile = file;
                    if (randomAccessFile != null) {
                        randomAccessFile.close();
                    }
                    throw th;
                }
            }
            GalleryLog.w("BurstThumbnailLoader", "not burst photo path " + infoFromFile);
            return null;
        } catch (FileNotFoundException e9) {
            GalleryLog.i("BurstThumbnailLoader", "findBestPhotoName() failed, reason: FileNotFoundException.");
            if (randomAccessFile != null) {
                randomAccessFile.close();
            }
            return null;
        } catch (IOException e10) {
            GalleryLog.i("BurstThumbnailLoader", "findBestPhotoName() failed, reason: IOException.");
            if (randomAccessFile != null) {
                randomAccessFile.close();
            }
            return null;
        }
    }
}
