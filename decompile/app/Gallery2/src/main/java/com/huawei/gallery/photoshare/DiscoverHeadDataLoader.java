package com.huawei.gallery.photoshare;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import com.android.gallery3d.R;
import com.android.gallery3d.data.ContentListener;
import com.android.gallery3d.data.DataManager;
import com.android.gallery3d.data.MediaItem;
import com.android.gallery3d.data.MediaSet;
import com.android.gallery3d.data.PhotoShareCategoryAlbumSet;
import com.android.gallery3d.util.GalleryLog;
import com.android.gallery3d.util.GalleryUtils;
import com.android.gallery3d.util.ThreadPool;
import com.huawei.gallery.data.AutoLoaderThread;
import com.huawei.gallery.photoshare.utils.PhotoShareUtils;
import com.huawei.gallery.util.MyPrinter;
import com.huawei.gallery.util.UIUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DiscoverHeadDataLoader {
    private static final MyPrinter LOG = new MyPrinter("DiscoverHeadAlbumSetDataLoader");
    private final Activity mActivity;
    private PhotoShareCategoryAlbumSet mCategoryRoot;
    private AlbumSet[] mData;
    private DataManager mDataManager;
    private Handler mHandler;
    private MediaSetListener mListener;
    private volatile boolean mReloadLock;
    private ReloadTask mReloadTask;
    private final MySourceListener mSourceListener = new MySourceListener();

    public static class AlbumSet {
        private long[] coverVersion = new long[6];
        private int dataLoadedCount;
        public final DiscoverAlbumSet entry;
        private Bitmap mDefaultCover = null;
        private int[] rotation = new int[6];
        private long[] setVersion = new long[6];
        private MediaSet sourceMediaSet;
        private long sourceVersion = -1;
        private int subMediaCount;
        private MediaSet[] subMediaSet = new MediaSet[6];
        private String[] subMediaSetNm = new String[6];
        private Bitmap[] thumbnail = new Bitmap[6];
        private int[] totalCount = new int[6];
        private int[] totalVideoCount = new int[6];

        AlbumSet(DiscoverAlbumSet ety) {
            this.entry = ety;
        }

        public int getAlbumName() {
            return this.entry.titleId;
        }

        public String getMediaSetPath() {
            return this.entry.mediaSetPath;
        }

        public int getLoadedDataCount() {
            return this.dataLoadedCount;
        }

        public Bitmap getThumbnail(int index) {
            if (index < 0 || index >= 6) {
                return null;
            }
            return this.thumbnail[index];
        }

        public int getRotation(int index) {
            if (index < 0 || index >= 6) {
                return 0;
            }
            return this.rotation[index];
        }

        public MediaSet getSubMediaSet(int index) {
            if (index < 0 || index >= 6) {
                return null;
            }
            return this.subMediaSet[index];
        }

        public String getSubMediaSetName(int index) {
            if (index < 0 || index >= 6) {
                return null;
            }
            return this.subMediaSetNm[index];
        }

        public Bitmap getDefaultCover() {
            return this.mDefaultCover;
        }
    }

    public enum DiscoverAlbumSet {
        STORY("/gallery/album/story", R.string.highlights_album_title, R.drawable.img_bg_storyalbum, R.string.discover_empty_moments_notice, -1),
        PEOPLE("/photoshare/classify/0", R.string.photoshare_classify_people, R.drawable.img_bg_people, R.string.discover_empty_grouped_by_people, 3),
        PLACE("/gallery/album/place", R.string.location, R.drawable.img_bg_place, R.string.discover_empty_grouped_by_place, -1),
        CATEGORY("/photoshare/category/{/photoshare/classify/Landscape/-1,/photoshare/classify/1/-1,/photoshare/classify/File_document/-1}", R.string.set_label_things, R.drawable.img_bg_category, R.string.discover_empty_grouped_by_category, 3);
        
        public final int emptyCover;
        public final int emptyLabel;
        private final String mediaSetPath;
        private final int supportedBy;
        private final int titleId;

        private DiscoverAlbumSet(String path, int title, int cover, int label, int support) {
            this.titleId = title;
            this.mediaSetPath = path;
            this.emptyCover = cover;
            this.emptyLabel = label;
            this.supportedBy = support;
        }
    }

    public interface MediaSetListener {
        void onLoadFinished();

        void onMediaSetLoad(AlbumSet albumSet);
    }

    private class MySourceListener implements ContentListener {
        private MySourceListener() {
        }

        public void onContentDirty() {
            if (DiscoverHeadDataLoader.this.mReloadTask != null) {
                DiscoverHeadDataLoader.this.mReloadTask.notifyDirty();
            }
        }
    }

    private class ReloadTask extends AutoLoaderThread {
        private ReloadTask() {
        }

        protected boolean isRenderLock() {
            return DiscoverHeadDataLoader.this.mReloadLock;
        }

        protected void onLoad() {
            DiscoverHeadDataLoader.this.mCategoryRoot.reload();
            for (AlbumSet album : DiscoverHeadDataLoader.this.mData) {
                DiscoverHeadDataLoader.LOG.d("start load " + album.entry);
                if (album.mDefaultCover == null) {
                    album.mDefaultCover = UIUtils.getBitmapFromDrawable(DiscoverHeadDataLoader.this.mActivity.getResources().getDrawable(album.entry.emptyCover));
                }
                if (album.sourceMediaSet == null) {
                    MediaSet source = DiscoverHeadDataLoader.this.mDataManager.getMediaSet(album.entry.mediaSetPath);
                    if (source == null) {
                        DiscoverHeadDataLoader.LOG.w("create mediaset failed. " + album.entry.mediaSetPath);
                    } else {
                        source.addContentListener(DiscoverHeadDataLoader.this.mSourceListener);
                        album.sourceMediaSet = source;
                    }
                }
                MediaSet sourceMediaSet = album.sourceMediaSet;
                if (sourceMediaSet == null) {
                    DiscoverHeadDataLoader.LOG.d("can't find mediaset for " + album.entry.mediaSetPath);
                } else {
                    long version = sourceMediaSet.reload();
                    if (album.sourceVersion != version) {
                        DiscoverHeadDataLoader.LOG.d(String.format("%s version changed from %s to %s ", new Object[]{album.entry, Long.valueOf(album.sourceVersion), Long.valueOf(version)}));
                        UpdateInfo info = new UpdateInfo(album);
                        info.mSourceVersion = version;
                        if (sourceMediaSet.getTotalMediaItemCount() == 0) {
                            DiscoverHeadDataLoader.this.mHandler.obtainMessage(1, info).sendToTarget();
                        } else {
                            int subCount = sourceMediaSet.getSubMediaSetCount();
                            info.mSubMediaCount = subCount;
                            int len = Math.min(6, subCount);
                            info.mDataLoadedCount = len;
                            if (len == 0) {
                                DiscoverHeadDataLoader.this.mHandler.obtainMessage(1, info).sendToTarget();
                            } else {
                                int index = 0;
                                while (index < len) {
                                    MediaSet mediaSet = sourceMediaSet.getSubMediaSet(index);
                                    if (mediaSet != null) {
                                        long mediaSetVersion = mediaSet.reload();
                                        if (info.mSetVersion[index] != mediaSetVersion) {
                                            DiscoverHeadDataLoader.LOG.d(String.format("%s mediaset: %s[%s] version changed from %s to %s ", new Object[]{album, mediaSet.getName(), mediaSet.getPath(), Long.valueOf(info.mSetVersion[index]), Long.valueOf(mediaSetVersion)}));
                                            info.mSubMediaSet[index] = mediaSet;
                                            info.mSubMediaSetNm[index] = mediaSet.getName();
                                            info.mSetVersion[index] = mediaSetVersion;
                                            info.mTotalCount[index] = mediaSet.getMediaItemCount();
                                            info.mTotalVideoCount[index] = mediaSet.getTotalVideoCount();
                                            MediaItem coverItem = mediaSet.getCoverMediaItem();
                                            if (!(coverItem == null || info.mCoverVersion[index] == coverItem.getDataVersion())) {
                                                DiscoverHeadDataLoader.LOG.d(String.format("%s cover: %s[%s] version changed from %s to %s ", new Object[]{album, coverItem.getName(), coverItem.getPath(), Long.valueOf(info.mCoverVersion[index]), Long.valueOf(coverItem.getDataVersion())}));
                                                info.mRotation[index] = coverItem.getRotation();
                                                info.mCoverVersion[index] = coverItem.getDataVersion();
                                                info.mThumbnail[index] = (Bitmap) coverItem.requestImage(24).run(ThreadPool.JOB_CONTEXT_STUB);
                                            }
                                        }
                                    }
                                    index++;
                                }
                                DiscoverHeadDataLoader.this.mHandler.obtainMessage(1, info).sendToTarget();
                            }
                        }
                    }
                }
            }
            DiscoverHeadDataLoader.this.mHandler.sendEmptyMessage(2);
        }
    }

    private static class UpdateInfo {
        private AlbumSet albumset;
        private long[] mCoverVersion = new long[6];
        private int mDataLoadedCount;
        private int[] mRotation = new int[6];
        private long[] mSetVersion = new long[6];
        private long mSourceVersion = -1;
        private int mSubMediaCount;
        private MediaSet[] mSubMediaSet = new MediaSet[6];
        private String[] mSubMediaSetNm = new String[6];
        private Bitmap[] mThumbnail = new Bitmap[6];
        private int[] mTotalCount = new int[6];
        private int[] mTotalVideoCount = new int[6];

        UpdateInfo(AlbumSet entry) {
            this.albumset = entry;
            reset();
            this.mSourceVersion = this.albumset.sourceVersion;
            System.arraycopy(this.albumset.coverVersion, 0, this.mCoverVersion, 0, 6);
            System.arraycopy(this.albumset.setVersion, 0, this.mSetVersion, 0, 6);
        }

        private void reset() {
            this.mSubMediaCount = 0;
            Arrays.fill(this.mSubMediaSet, null);
            Arrays.fill(this.mSubMediaSetNm, null);
            Arrays.fill(this.mThumbnail, null);
            Arrays.fill(this.mTotalCount, 0);
            Arrays.fill(this.mTotalVideoCount, 0);
            Arrays.fill(this.mRotation, 0);
            Arrays.fill(this.mCoverVersion, -1);
            Arrays.fill(this.mSetVersion, -1);
        }

        private void updateAlbumSetInfo() {
            if (this.albumset.sourceVersion != this.mSourceVersion) {
                this.albumset.sourceVersion = this.mSourceVersion;
                this.albumset.subMediaCount = this.mSubMediaCount;
                this.albumset.dataLoadedCount = this.mDataLoadedCount;
                for (int index = 0; index < this.mDataLoadedCount; index++) {
                    if (this.mSetVersion[index] != this.albumset.setVersion[index]) {
                        this.albumset.subMediaSet[index] = this.mSubMediaSet[index];
                        this.albumset.subMediaSetNm[index] = this.mSubMediaSetNm[index];
                        this.albumset.setVersion[index] = this.mSetVersion[index];
                        this.albumset.totalCount[index] = this.mTotalCount[index];
                        this.albumset.totalVideoCount[index] = this.mTotalVideoCount[index];
                        if (this.mCoverVersion[index] != this.albumset.coverVersion[index]) {
                            this.albumset.rotation[index] = this.mRotation[index];
                            this.albumset.coverVersion[index] = this.mCoverVersion[index];
                            this.albumset.thumbnail[index] = this.mThumbnail[index];
                        }
                    }
                }
            }
        }
    }

    public DiscoverHeadDataLoader(Activity activity, DataManager manager) {
        this.mActivity = activity;
        this.mDataManager = manager;
        this.mHandler = new Handler(Looper.getMainLooper()) {
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 1:
                        if (DiscoverHeadDataLoader.this.mListener != null) {
                            UpdateInfo updateInfo = msg.obj;
                            updateInfo.updateAlbumSetInfo();
                            GalleryLog.d("DiscoverHeadAlbumSetDataLoader", "albumset loaded " + updateInfo.albumset);
                            DiscoverHeadDataLoader.this.mListener.onMediaSetLoad(updateInfo.albumset);
                            return;
                        }
                        return;
                    case 2:
                        if (DiscoverHeadDataLoader.this.mListener != null) {
                            DiscoverHeadDataLoader.this.mListener.onLoadFinished();
                            return;
                        }
                        return;
                    default:
                        super.handleMessage(msg);
                        return;
                }
            }
        };
        initHeadData();
        this.mCategoryRoot = (PhotoShareCategoryAlbumSet) manager.getMediaSet("/photoshare/classify");
    }

    private void initHeadData() {
        DiscoverAlbumSet[] albums = DiscoverAlbumSet.values();
        int length = albums.length;
        List<AlbumSet> data = new ArrayList(length);
        int supportFeature = -3;
        LOG.d("don't support group whith local");
        if (!isSupportCloudClassify()) {
            LOG.d("don't support group with photoshare");
            supportFeature = -3 & -2;
        }
        for (int i = 0; i < length; i++) {
            if ((albums[i].supportedBy & supportFeature) != 0) {
                data.add(new AlbumSet(albums[i]));
                LOG.d(albums[i].name() + "is supported");
            }
        }
        this.mData = new AlbumSet[data.size()];
        data.toArray(this.mData);
    }

    private static boolean isSupportCloudClassify() {
        return GalleryUtils.IS_CHINESE_VERSION ? PhotoShareUtils.isSupportPhotoShare() : false;
    }

    public void pause() {
        this.mReloadTask.terminate();
        this.mReloadTask = null;
        this.mCategoryRoot.removeContentListener(this.mSourceListener);
    }

    public void resume() {
        this.mCategoryRoot.addContentListener(this.mSourceListener);
        this.mReloadTask = new ReloadTask();
        this.mReloadTask.start();
    }

    public void setDataLoadListener(MediaSetListener listener) {
        this.mListener = listener;
    }

    public int getDataSize() {
        return this.mData.length;
    }

    public AlbumSet getData(int index) {
        return this.mData[index];
    }

    public void freeze() {
        this.mReloadLock = true;
    }

    public void unfreeze() {
        this.mReloadLock = false;
        if (this.mSourceListener != null) {
            this.mSourceListener.onContentDirty();
        }
    }
}
