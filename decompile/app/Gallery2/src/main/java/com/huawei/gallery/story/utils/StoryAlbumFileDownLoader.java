package com.huawei.gallery.story.utils;

import android.text.TextUtils;
import com.huawei.android.cg.vo.FileInfo;
import com.huawei.gallery.photoshare.utils.PhotoShareUtils;
import com.huawei.gallery.photoshare.utils.PhotoShareUtils.DownLoadProgressListener;
import com.huawei.gallery.util.MyPrinter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class StoryAlbumFileDownLoader {
    private static final MyPrinter LOG = new MyPrinter("Clustering_StoryAlbumFileDownLoader");
    private static DownLoadProgressListener downloadProgressListener = new DownLoadProgressListener() {
        public void downloadProgress(String hash, String albumId, String uniqueId, int thumbType, Long totalSize, Long currentSize) {
        }

        public void downloadFinish(String hash, String albumId, String uniqueId, int thumbType, int result) {
            Iterator<DownloadItem> iterator = StoryAlbumFileDownLoader.sDownloadList.iterator();
            while (iterator.hasNext()) {
                DownloadItem item = (DownloadItem) iterator.next();
                for (FileInfo file : item.downloadFileList) {
                    if ((thumbType == 1 || thumbType == 0) && !TextUtils.isEmpty(albumId) && albumId.equalsIgnoreCase(file.getAlbumId()) && !TextUtils.isEmpty(hash) && hash.equalsIgnoreCase(file.getHash())) {
                        item.countDown--;
                        break;
                    }
                }
                FileDownloadListener listener = (FileDownloadListener) StoryAlbumFileDownLoader.sListenerMap.get(item.clusterCode);
                if (listener != null) {
                    listener.onProgress(((double) (item.downloadFileList.size() - item.countDown)) / ((double) item.downloadFileList.size()));
                    if (item.countDown == 0) {
                        StoryAlbumFileDownLoader.LOG.d("notify download finish to " + listener);
                        listener.onDownloadFinished();
                    }
                }
                if (item.countDown == 0) {
                    StoryAlbumFileDownLoader.sListenerMap.remove(item.clusterCode);
                    iterator.remove();
                }
            }
            if (StoryAlbumFileDownLoader.sDownloadList.size() == 0) {
                PhotoShareUtils.removeListener(StoryAlbumFileDownLoader.downloadProgressListener);
            }
        }
    };
    private static List<DownloadItem> sDownloadList = new ArrayList(50);
    private static Map<String, FileDownloadListener> sListenerMap = new HashMap();

    public interface FileDownloadListener {
        void onDownloadFinished();

        void onProgress(double d);
    }

    static class DownloadItem {
        public String clusterCode;
        public int countDown;
        public List<FileInfo> downloadFileList;

        DownloadItem(String clusterCode, List<FileInfo> downloadFileList) {
            this.clusterCode = clusterCode;
            this.downloadFileList = downloadFileList;
            this.countDown = downloadFileList.size();
        }
    }

    public static boolean downloadUnReadyFiles(String clusterCode, List<FileInfo> downloadFiles, int downloadType) {
        if (downloadFiles.size() == 0) {
            return false;
        }
        for (DownloadItem item : sDownloadList) {
            if (!TextUtils.isEmpty(clusterCode) && clusterCode.equalsIgnoreCase(item.clusterCode)) {
                LOG.d("already in download list ...");
                return true;
            }
        }
        DownloadItem item2 = new DownloadItem(clusterCode, downloadFiles);
        sDownloadList.add(item2);
        LOG.d("downloadUnreadyData for " + downloadFiles.size() + " files");
        List<FileInfo> videoList = new ArrayList();
        List<FileInfo> photoList = new ArrayList();
        switch (downloadType) {
            case 1:
                photoList = downloadFiles;
                break;
            case 2:
                videoList = downloadFiles;
                break;
            case 3:
                for (FileInfo file : downloadFiles) {
                    if (file.getFileType() == 4) {
                        videoList.add(file);
                    } else {
                        photoList.add(file);
                    }
                }
                break;
        }
        int ret = 0;
        if ((downloadType & 2) == 2) {
            try {
                if (videoList.size() > 0) {
                    LOG.d("download video origin files");
                    ret = PhotoShareUtils.getServer().downloadPhotoThumb((FileInfo[]) videoList.toArray(new FileInfo[videoList.size()]), 0, 0, false) | 0;
                }
            } catch (Exception e) {
                LOG.d("download lcd  files fail " + e.getMessage());
                sDownloadList.remove(item2);
                return false;
            }
        }
        if ((downloadType & 1) == 1 && photoList.size() > 0) {
            LOG.d("download lcd photo files");
            ret |= PhotoShareUtils.getServer().downloadPhotoThumb((FileInfo[]) photoList.toArray(new FileInfo[photoList.size()]), 1, 0, false);
        }
        if (ret != 0) {
            sDownloadList.remove(item2);
            return false;
        }
        PhotoShareUtils.addListener(downloadProgressListener);
        return true;
    }

    public static synchronized void addDownloadListener(String clusterCode, FileDownloadListener listener) {
        synchronized (StoryAlbumFileDownLoader.class) {
            sListenerMap.put(clusterCode, listener);
        }
    }

    public static synchronized void removeDownloadListener(String clusterCode) {
        synchronized (StoryAlbumFileDownLoader.class) {
            sListenerMap.remove(clusterCode);
        }
    }
}
