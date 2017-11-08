package com.android.gallery3d.util;

import android.net.Uri;
import com.android.gallery3d.app.GalleryContext;
import com.android.gallery3d.common.Utils;
import com.android.gallery3d.data.DataManager;
import com.android.gallery3d.data.IImage;
import com.android.gallery3d.data.MediaItem;
import com.android.gallery3d.data.MediaObject;
import com.android.gallery3d.data.Path;
import com.android.gallery3d.ui.MenuExecutor;
import com.huawei.gallery.video.VideoConvertor;
import com.huawei.gallery.video.VideoConvertor.ImageToVideoListener;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;

public class ImageVideoTranser {
    private static final String[] CACHE_FILE_NAME = new String[]{".0", ".1", ".2", ".3", ".4", ".5", ".6", ".7", ".8", ".9"};
    private static final String LOG_TAG = ImageVideoTranser.class.getSimpleName();

    public static boolean isItemSupportTransVer(MediaObject item) {
        if (item instanceof IImage) {
            return ((IImage) item).isSupportTranslateVoiceImageToVideo();
        }
        return false;
    }

    public static int getVoiceImageCountInArray(ArrayList<Path> pathArray, GalleryContext activity) {
        if (pathArray == null || pathArray.isEmpty()) {
            return 0;
        }
        int count = 0;
        DataManager manager = activity.getDataManager();
        for (Path path : pathArray) {
            MediaObject item = manager.getMediaObject(path);
            if (item != null && isItemSupportTransVer(item)) {
                count++;
            }
        }
        return count;
    }

    public static File getWorkSpaceFile() {
        return FileUtils.chooseFileInSequence(new File(GalleryUtils.getVolumePaths()[0], ".videotrans"), CACHE_FILE_NAME, true);
    }

    private static File getTmpWorkSpaceDir() {
        return new File(GalleryUtils.getVolumePaths()[0], ".tmpvideotrans");
    }

    private static File getOutputVideoFile(MediaItem originImage, File outputDir) {
        File exportFile = FileUtils.getNoneDuplicateFile(outputDir.getAbsoluteFile(), originImage.getName(), ".mp4");
        if (exportFile == null || (exportFile.exists() && !exportFile.delete())) {
            return null;
        }
        return exportFile;
    }

    private static File extractVoiceFile(MediaItem voiceImage, String tmpWorkSpace) throws Exception {
        Throwable th;
        File voiceImageFile = new File(voiceImage.getFilePath());
        File tmpVoiceFile = new File(tmpWorkSpace, "tmp.amr");
        Closeable is = null;
        Closeable os = null;
        try {
            Closeable is2 = new BufferedInputStream(new FileInputStream(voiceImageFile));
            try {
                Closeable os2 = new BufferedOutputStream(new FileOutputStream(tmpVoiceFile));
                try {
                    FileUtils.skip(is2, (voiceImageFile.length() - voiceImage.getVoiceOffset()) - 20);
                    FileUtils.readDataToWrite(is2, os2, new byte[32768], voiceImage.getVoiceOffset());
                    Utils.closeSilently(is2);
                    Utils.closeSilently(os2);
                    return tmpVoiceFile;
                } catch (Throwable th2) {
                    th = th2;
                    os = os2;
                    is = is2;
                    Utils.closeSilently(is);
                    Utils.closeSilently(os);
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                is = is2;
                Utils.closeSilently(is);
                Utils.closeSilently(os);
                throw th;
            }
        } catch (Throwable th4) {
            th = th4;
            Utils.closeSilently(is);
            Utils.closeSilently(os);
            throw th;
        }
    }

    public static Uri translateVoiceImageToVideo(MediaObject item, final int currentProgress, final int maxProgress, final MenuExecutor executor, File outputFileDir) {
        File outputFile = translateVoiceImageToVideoInternal(item, new ImageToVideoListener() {
            public void onProgress(String filename, int progress) {
                executor.updateProgress(((currentProgress + progress) * 100) / maxProgress, null);
            }
        }, outputFileDir);
        return outputFile != null ? Uri.fromFile(outputFile) : item.getContentUri();
    }

    private static File translateVoiceImageToVideoInternal(MediaObject item, ImageToVideoListener listener, File outputDir) {
        Throwable e;
        Throwable th;
        if (!isItemSupportTransVer(item) || outputDir == null) {
            return null;
        }
        MediaItem voiceImage = (MediaItem) item;
        File tmpWorkSpace = getTmpWorkSpaceDir();
        String tmpWorkSpacePath = tmpWorkSpace.getAbsolutePath();
        try {
            if (!tmpWorkSpace.exists()) {
                GalleryLog.e(LOG_TAG, "create workspace result is : " + tmpWorkSpace.mkdirs());
                GalleryLog.e(LOG_TAG, "create file .nomedia result is : " + new File(tmpWorkSpace, ".nomedia").createNewFile());
            }
            VideoConvertor editor = new VideoConvertor();
            try {
                File tmpVoiceFile = extractVoiceFile(voiceImage, tmpWorkSpacePath);
                editor.setImageFile(voiceImage.getFilePath(), voiceImage.getRotation());
                editor.setAudioFile(tmpVoiceFile.getAbsolutePath());
                File exportFile = getOutputVideoFile(voiceImage, outputDir);
                if (exportFile == null) {
                    FileUtils.deleteFile(tmpWorkSpace);
                    return null;
                }
                editor.exportVideo(exportFile.getAbsolutePath(), listener);
                FileUtils.deleteFile(tmpWorkSpace);
                return exportFile;
            } catch (Throwable th2) {
                th = th2;
                FileUtils.deleteFile(tmpWorkSpace);
                throw th;
            }
        } catch (Throwable th3) {
            e = th3;
            GalleryLog.e(LOG_TAG, "Error when translate to video." + e.getMessage());
            FileUtils.deleteFile(tmpWorkSpace);
            return null;
        }
    }
}
