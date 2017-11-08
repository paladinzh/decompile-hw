package com.huawei.systemmanager.spacecleanner.engine.hwscanner;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.NonNull;
import com.huawei.systemmanager.comm.Storage.PathEntry;
import com.huawei.systemmanager.comm.Storage.PathEntrySet;
import com.huawei.systemmanager.comm.collections.HsmCollections;
import com.huawei.systemmanager.comm.misc.Closeables;
import com.huawei.systemmanager.spacecleanner.engine.ScanParams;
import com.huawei.systemmanager.spacecleanner.engine.base.Task;
import com.huawei.systemmanager.spacecleanner.engine.trash.AudioTrash;
import com.huawei.systemmanager.spacecleanner.engine.trash.PhotoTrash;
import com.huawei.systemmanager.spacecleanner.engine.trash.Trash;
import com.huawei.systemmanager.spacecleanner.engine.trash.VideoTrash;
import com.huawei.systemmanager.spacecleanner.utils.MediaUtil;
import com.huawei.systemmanager.util.HwLog;
import java.io.Closeable;
import java.io.File;
import java.util.List;

public class MediaScanTask extends Task {
    private static final String TAG = "MediaScanTask";
    private PathEntrySet mEntrySet;

    private abstract class MediaDataHandler {
        abstract String[] getProjection();

        abstract Uri getUri();

        abstract Trash handlerData(String str, @NonNull PathEntry pathEntry, @NonNull Cursor cursor);

        private MediaDataHandler() {
        }
    }

    private class AudioDataHandler extends MediaDataHandler {
        private AudioDataHandler() {
            super();
        }

        Uri getUri() {
            return MediaUtil.AUDIO_URI;
        }

        String[] getProjection() {
            return new String[]{"_data", "artist"};
        }

        Trash handlerData(String filePath, PathEntry pathEntry, @NonNull Cursor cursor) {
            String musicArtist = cursor.getString(1);
            AudioTrash audioTrash = new AudioTrash(filePath, pathEntry);
            audioTrash.setArtist(musicArtist);
            return audioTrash;
        }
    }

    private class PhotoDataHandler extends MediaDataHandler {
        private PhotoDataHandler() {
            super();
        }

        Uri getUri() {
            return MediaUtil.PHOTO_RUI;
        }

        String[] getProjection() {
            return new String[]{"_data"};
        }

        Trash handlerData(String filePath, PathEntry pathEntry, Cursor cursor) {
            return new PhotoTrash(filePath, pathEntry);
        }
    }

    private class VideoDataHandler extends MediaDataHandler {
        private VideoDataHandler() {
            super();
        }

        Uri getUri() {
            return MediaUtil.VIDEO_RUI;
        }

        String[] getProjection() {
            return new String[]{"_data"};
        }

        Trash handlerData(String filePath, PathEntry pathEntry, @NonNull Cursor cursor) {
            return new VideoTrash(filePath, pathEntry);
        }
    }

    public MediaScanTask(Context ctx) {
        super(ctx);
    }

    public String getTaskName() {
        return TAG;
    }

    public int getType() {
        return 53;
    }

    protected void doTask(ScanParams p) {
        this.mEntrySet = p.getEntrySet();
        queryMeida(new PhotoDataHandler());
        queryMeida(new VideoDataHandler());
        queryMeida(new AudioDataHandler());
        onPublishEnd();
    }

    public List<Integer> getSupportTrashType() {
        return HsmCollections.newArrayList(Integer.valueOf(128), Integer.valueOf(512), Integer.valueOf(256));
    }

    private void queryMeida(MediaDataHandler dataHandler) {
        if (!isCanceled()) {
            if (this.mEntrySet == null) {
                HwLog.e(TAG, "queryMeida failed! mEntrySet is null");
                return;
            }
            Context ctx = getContext();
            Closeable closeable = null;
            Uri uri = dataHandler.getUri();
            closeable = ctx.getContentResolver().query(uri, dataHandler.getProjection(), null, null, null);
            if (closeable == null) {
                HwLog.w(TAG, "queryMeida error, cursor is null. uri=" + uri);
                return;
            }
            int dataIndex = closeable.getColumnIndex("_data");
            if (dataIndex < 0) {
                HwLog.w(TAG, "queryMeida error, data index is:" + dataIndex);
                Closeables.close(closeable);
                return;
            }
            while (closeable.moveToNext()) {
                if (isCanceled()) {
                    Closeables.close(closeable);
                    return;
                }
                try {
                    String path = closeable.getString(dataIndex);
                    if (new File(path).exists()) {
                        PathEntry pathEntry = this.mEntrySet.getPathEntry(path);
                        if (pathEntry == null) {
                            HwLog.d(TAG, "queryMeida, unknow position");
                        } else {
                            Trash trash = dataHandler.handlerData(path, pathEntry, closeable);
                            onPublishProgress(0, path);
                            if (trash != null) {
                                onPublishItemUpdate(trash);
                            }
                        }
                    } else {
                        HwLog.d(TAG, "queryMeida, file is not exist");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    Closeables.close(closeable);
                }
            }
            Closeables.close(closeable);
        }
    }

    public boolean isNormal() {
        return false;
    }
}
