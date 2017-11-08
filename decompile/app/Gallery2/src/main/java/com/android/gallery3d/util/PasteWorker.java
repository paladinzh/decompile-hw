package com.android.gallery3d.util;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore.Images.Media;
import android.provider.MediaStore.Video;
import android.text.TextUtils;
import com.android.gallery3d.app.GalleryApp;
import com.android.gallery3d.common.Utils;
import com.android.gallery3d.data.DataManager;
import com.android.gallery3d.data.IRecycle;
import com.android.gallery3d.data.MediaItem;
import com.android.gallery3d.data.MediaObject;
import com.android.gallery3d.data.MediaSet;
import com.android.gallery3d.data.MediaSet.ItemConsumer;
import com.android.gallery3d.data.Path;
import com.huawei.gallery.burst.BurstPhotoSet;
import com.huawei.gallery.extfile.FyuseFile;
import com.huawei.gallery.photoshare.utils.PhotoShareUtils;
import com.huawei.gallery.recycle.utils.RecycleUtils;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

public class PasteWorker {
    private static final HashMap<String, Boolean> sHasAddExtraHashMap = new HashMap();
    private GalleryApp mApp;
    private int mFilePasted = 0;
    private boolean mFirstPaste = false;
    private SimpleLock mLock = new SimpleLock();
    private DataManager mManager;
    private int mPasteState = 0;
    private int mToBePastedFileCount = 0;
    private int mUserState = 0;
    private long mVolumePasted = 0;

    public interface PasteEventHandler {
        public static final int PASTE_EVENT_NO_SPACE = 3;
        public static final int PASTE_EVENT_PROGRESS_UPDATE = 11;
        public static final int PASTE_EVENT_SAME_FILENAME = 2;
        public static final int PASTE_EVENT_SAME_SOURCE_DESTINATION = 1;
        public static final int PASTE_EVENT_SOURCE_NOT_EXIST = 4;
        public static final int PASTE_EVENT_UNKNOWN = 99;
        public static final int PASTE_EVENT_USER_CANCEL = 5;
        public static final int PASTE_OK = 0;
        public static final int PASTE_STRATEGY_SAMENAME_ALLJUMPOVER = 23;
        public static final int PASTE_STRATEGY_SAMENAME_ALLOVERRIDE = 22;
        public static final int PASTE_STRATEGY_SAMENAME_USER = 21;

        boolean getCheckBoxState();

        boolean onPasteCompleteEvent(int i, Bundle bundle);

        boolean onPasteEvent(int i, Bundle bundle);

        boolean onPasteInitEvent(int i, Bundle bundle);
    }

    static class CachedComparator<KT> implements Comparator<KT>, Serializable {
        private static final long serialVersionUID = 1;
        transient SmallItem left;
        HashMap<KT, SmallItem> mCache = new HashMap();
        transient SmallItem right;

        CachedComparator() {
        }

        void put(KT key, SmallItem item) {
            this.mCache.put(key, item);
        }

        public int compare(KT lhs, KT rhs) {
            this.left = (SmallItem) this.mCache.get(lhs);
            this.right = (SmallItem) this.mCache.get(rhs);
            if (this.left == null || this.right == null) {
                return 0;
            }
            int cmp = Utils.compare(this.left.date, this.right.date);
            if (cmp == 0) {
                cmp = Utils.compare(this.left.modifytime, this.right.modifytime);
            }
            if (cmp == 0) {
                cmp = Utils.compare((long) this.left.id, (long) this.right.id);
            }
            return cmp;
        }
    }

    static class SmallItem {
        final long date;
        final int id;
        final long modifytime;

        public SmallItem(MediaItem item) {
            this.id = item.getId();
            this.modifytime = item.getDateModifiedInSec();
            this.date = item.getDateInMs();
        }
    }

    public PasteWorker(GalleryApp app, DataManager manager) {
        this.mApp = app;
        this.mManager = manager;
    }

    private void resetFlag() {
        sHasAddExtraHashMap.clear();
        this.mPasteState = 0;
        this.mUserState = 0;
        this.mFilePasted = 0;
        this.mVolumePasted = 0;
        this.mToBePastedFileCount = 0;
    }

    public int getCurrentPasteState() {
        return this.mPasteState;
    }

    private boolean getUserDecision() {
        if (this.mUserState == 1) {
            GalleryLog.d("PasteWorker", "User pressed cancel, wait for final decision");
            this.mLock.waitUntilNotify();
        }
        if (this.mUserState != 2) {
            return true;
        }
        GalleryLog.d("PasteWorker", "User chose to exit the pasting process");
        return false;
    }

    public void onPasteCanceled(Bundle data, PasteEventHandler eventHandler) {
        this.mUserState = 1;
        int currentPasteState = getCurrentPasteState();
        Bundle pasteStateData = new Bundle();
        pasteStateData.putInt("key-pastestate", currentPasteState);
        if (eventHandler.onPasteEvent(5, pasteStateData)) {
            this.mUserState = 0;
        } else {
            this.mUserState = 2;
        }
        this.mLock.notifyAllWaitingLock();
    }

    private long getTotalSpace(ArrayList<Path> processList) {
        long totalSpace = 0;
        CachedComparator<Path> comparator = new CachedComparator();
        int size = processList.size();
        for (int j = 0; j < size; j++) {
            Path itemPath = (Path) processList.get(j);
            if (!getUserDecision()) {
                return 0;
            }
            MediaObject mediaObject = this.mManager.getMediaObject(itemPath);
            if (mediaObject instanceof MediaItem) {
                MediaItem item = (MediaItem) mediaObject;
                comparator.put(itemPath, new SmallItem(item));
                totalSpace += item.getSize();
            }
            if (mediaObject instanceof MediaSet) {
                MediaSet set = (MediaSet) mediaObject;
                for (int i = 0; i < set.getMediaItemCount(); i++) {
                    item = (MediaItem) set.getMediaItem(i, 1).get(0);
                    comparator.put(itemPath, new SmallItem(item));
                    totalSpace += item.getSize();
                }
            }
        }
        Collections.sort(processList, comparator);
        return totalSpace;
    }

    public boolean initPaste(Bundle pasteInfo, PasteEventHandler eventHandler, ArrayList<Path> processList) {
        resetFlag();
        this.mFirstPaste = true;
        this.mPasteState = pasteInfo.getInt("key-pastestate", 0);
        this.mToBePastedFileCount = processList.size();
        int flag = pasteInfo.getInt("recycle_flag", 0);
        if (flag != 0) {
            return notifyEvent(0, 0, pasteInfo, eventHandler);
        }
        long totalSpace = getTotalSpace(processList);
        String targetFileParent = pasteInfo.getString("key-targetpath", "");
        GalleryLog.d("PasteWorker", "initPaste paste state = " + this.mPasteState + "  target parent path = " + targetFileParent);
        return initPaste(targetFileParent, flag, totalSpace, eventHandler);
    }

    private String getTargetDir(MediaItem item, Bundle data) {
        switch (data.getInt("recycle_flag", 0)) {
            case 1:
                if (!(item instanceof IRecycle)) {
                    return "";
                }
                String filePath = ((IRecycle) item).getSourcePath();
                if (TextUtils.isEmpty(filePath)) {
                    return "";
                }
                return new File(filePath).getParent();
            case 2:
                return RecycleUtils.getGalleryRecycleBinDir(item.getFilePath());
            default:
                return data.getString("key-targetpath");
        }
    }

    private String getTargetName(MediaItem item, Bundle data, String defaultName, long recycleTime, String name) {
        switch (data.getInt("recycle_flag", 0)) {
            case 1:
                return data.getString("recovery_file_name", defaultName);
            case 2:
                return RecycleUtils.getRecycleName(item, recycleTime, name);
            default:
                return defaultName;
        }
    }

    private boolean initPaste(String targetFileParent, int flag, long totalSpace, PasteEventHandler eventHandler) {
        if (TextUtils.isEmpty(targetFileParent)) {
            return false;
        }
        File rootFile = new File(targetFileParent);
        while (!rootFile.exists()) {
            rootFile = rootFile.getParentFile();
        }
        if (flag == 0) {
            long spaceAvailable = rootFile.getUsableSpace();
            if (spaceAvailable <= totalSpace) {
                return notifyEventNoSpace(0, spaceAvailable, totalSpace, eventHandler);
            }
        }
        dealWithTargetDir(targetFileParent, flag);
        Bundle spaceData = new Bundle();
        spaceData.putLong("key-volumeneed", totalSpace);
        return notifyEvent(0, 0, spaceData, eventHandler);
    }

    public void paste(MediaObject mediaObject, Bundle data, PasteEventHandler handler) {
        if (mediaObject != null) {
            switch (getCurrentPasteState()) {
                case 1:
                    copyFile(mediaObject, data, handler);
                    break;
                case 2:
                    cutFile(mediaObject, data, handler);
                    break;
                default:
                    GalleryLog.d("PasteWorker", "the paste state is " + getCurrentPasteState() + ", do nothing and return");
                    break;
            }
        }
    }

    private boolean sameFileExist(File targetFile, File originFile) {
        return targetFile.exists() && targetFile.length() == originFile.length();
    }

    private boolean sameNameDifferentFile(File targetFile, File originFile) {
        return targetFile.exists() && targetFile.length() != originFile.length();
    }

    private File getRenameFileWithNew(String targetFileParent, String originName) {
        File newTargetFile;
        do {
            newTargetFile = new File(targetFileParent, originName + "_new.jpg");
            originName = newTargetFile.getName().replaceAll(".jpg$", "");
        } while (newTargetFile.exists());
        return newTargetFile;
    }

    private int validityCheck(File itemFile, String targetFileParent, PasteEventHandler handler, int flag) {
        if (itemFile.exists()) {
            if (targetFileParent.equals(itemFile.getParent())) {
                GalleryLog.d("PasteWorker", "Copy a file to its own place: " + itemFile.getAbsolutePath());
                if (!handler.onPasteEvent(1, null)) {
                    return 1;
                }
            }
            if (!(flag == 1 || flag == 2)) {
                File targetFile = new File(targetFileParent, itemFile.getName());
                boolean isTargetFileExist = sameFileExist(targetFile, itemFile);
                if (isTargetFileExist && !notifyEventSameNameFile(1, itemFile.getName(), handler)) {
                    return 2;
                }
                long availableSpace = new File(targetFileParent).getUsableSpace();
                if (isTargetFileExist) {
                    availableSpace += targetFile.length();
                }
                if (availableSpace <= itemFile.length() && !notifyEventNoSpace(1, availableSpace, itemFile.length(), handler)) {
                    return 3;
                }
            }
            return 0;
        }
        GalleryLog.d("PasteWorker", "File does not exist any more: " + itemFile.getAbsolutePath());
        return 4;
    }

    public int cutFile(MediaObject itemObject, Bundle data, PasteEventHandler handler) {
        int result = copyFile(itemObject, data, handler);
        if (result == 0) {
            RecycleUtils.delete(this.mApp.getContentResolver(), itemObject, data);
        } else if (result == 4) {
            if (data.getInt("recycle_flag", 0) == 1 && (itemObject instanceof IRecycle)) {
                GalleryLog.w("PasteWorker", "insert empty record: " + ((IRecycle) itemObject).getSourcePath());
                ((IRecycle) itemObject).insertMediaFile();
            }
            RecycleUtils.delete(this.mApp.getContentResolver(), itemObject, data);
        }
        return result;
    }

    public int copyFile(MediaObject itemObject, Bundle data, PasteEventHandler handler) {
        if (itemObject instanceof MediaItem) {
            return copyFile((MediaItem) itemObject, data, handler, false);
        }
        if (itemObject instanceof MediaSet) {
            return copyDir((MediaSet) itemObject, data, handler);
        }
        return 99;
    }

    private int copyDir(MediaSet mediaSet, Bundle data, PasteEventHandler handler) {
        final int[] resultCopyDir = new int[]{0};
        final boolean[] isSameNameBurstExist = new boolean[]{false};
        final boolean isBurstSet = mediaSet instanceof BurstPhotoSet;
        final Bundle bundle = data;
        final PasteEventHandler pasteEventHandler = handler;
        mediaSet.enumerateTotalMediaItems(new ItemConsumer() {
            public void consume(int index, MediaItem item) {
                int result;
                int[] iArr;
                if (item.isBurstCover() && !isBurstSet) {
                    BurstPhotoSet burstPhotoSet = (BurstPhotoSet) PasteWorker.this.mManager.getMediaObject(item.getBurstSetPath());
                    burstPhotoSet.setOrderClauseReverse(true);
                    result = PasteWorker.this.copyDir(burstPhotoSet, bundle, pasteEventHandler);
                    burstPhotoSet.setOrderClauseReverse(false);
                    if (result != 0) {
                        iArr = resultCopyDir;
                        iArr[0] = iArr[0] + 1;
                    }
                } else if (pasteEventHandler.getCheckBoxState() && !isSameNameBurstExist[0]) {
                    if (item.isBurstCover() && !item.isOnlyCloudItem()) {
                        File itemFile = new File(item.getFilePath());
                        String targetFileParent = PasteWorker.this.getTargetDir(item, bundle);
                        if (!TextUtils.isEmpty(targetFileParent)) {
                            if (new File(targetFileParent, PasteWorker.this.getTargetName(item, bundle, itemFile.getName(), System.currentTimeMillis(), RecycleUtils.getLimitedName(item.getName()))).exists()) {
                                isSameNameBurstExist[0] = true;
                            }
                        } else {
                            return;
                        }
                    }
                    result = PasteWorker.this.copyFile(item, bundle, pasteEventHandler, true);
                    int recycleFlag = bundle.getInt("recycle_flag");
                    boolean recycleSourceNotExist = result == 4 ? recycleFlag != 0 : false;
                    if (result == 0 || recycleSourceNotExist) {
                        if (item.isBurstCover()) {
                            PasteWorker pasteWorker = PasteWorker.this;
                            pasteWorker.mFilePasted = pasteWorker.mFilePasted + 1;
                        }
                        if (recycleFlag != 0) {
                            RecycleUtils.delete(PasteWorker.this.mApp.getContentResolver(), item, bundle);
                        }
                    } else {
                        iArr = resultCopyDir;
                        iArr[0] = iArr[0] + 1;
                    }
                }
            }

            public boolean dynamic() {
                return bundle.getInt("recycle_flag") != 0;
            }
        });
        if (resultCopyDir[0] > 0) {
            return 99;
        }
        return 0;
    }

    public int copyFile(MediaItem item, Bundle data, PasteEventHandler handler, boolean isBurstPhoto) {
        Object os;
        Closeable is;
        Closeable os2;
        boolean isMoveToRecycle;
        Throwable th;
        if (item.isOnlyCloudItem()) {
            return 0;
        }
        String targetFileParent = getTargetDir(item, data);
        int flag = data.getInt("recycle_flag", 0);
        if (!dealWithTargetDir(targetFileParent, flag)) {
            return 4;
        }
        File file = new File(item.getFilePath());
        int validity = validityCheck(file, targetFileParent, handler, flag);
        if (validity == 0 || flag != 0 || validity == 4) {
            File targetFile;
            long recycleTime = 0;
            String limitName = "";
            if (flag == 2) {
                recycleTime = System.currentTimeMillis();
                limitName = RecycleUtils.getLimitedName(item.getName());
            }
            String targetName = getTargetName(item, data, file.getName(), recycleTime, limitName);
            file = new File(targetFileParent, targetName);
            if (flag != 2) {
                if (sameFileExist(file, file)) {
                    if (item.isBurstCover()) {
                        return 0;
                    }
                    if (flag == 1 && RecycleUtils.supportRecycle() && PhotoShareUtils.isGUIDSupport()) {
                        deleteExistFile(file);
                        data.putBoolean("delete_recycle_file", true);
                        return 0;
                    }
                    deleteExistFile(file);
                }
                if (sameNameDifferentFile(file, file)) {
                    targetFile = getRenameFileWithNew(targetFileParent, targetName);
                    if (PhotoShareUtils.isGUIDSupport()) {
                        data.putBoolean("rename_recycle_file", true);
                    }
                }
            }
            if (flag == 2) {
                data.putString("recycle_file_name", targetFile.getName());
                data.putLong("recycle_time", recycleTime);
                data.putString("title", limitName);
            }
            if (validity != 0) {
                return validity;
            }
            long fileLength = file.length();
            try {
                boolean renameTo = getCurrentPasteState() == 2 ? file.renameTo(targetFile) : false;
                GalleryLog.d("PasteWorker", "cutState[2] " + getCurrentPasteState() + ", rename result " + renameTo);
                if (renameTo) {
                    data.putString("file_source", file.getPath());
                    data.putString("file_renameto", targetFile.getPath());
                    if (getUserDecision()) {
                        os = null;
                        Object is2 = null;
                    } else {
                        throw new Exception();
                    }
                }
                is = new BufferedInputStream(new FileInputStream(file));
                try {
                    os2 = new BufferedOutputStream(new FileOutputStream(targetFile));
                    try {
                        copyFileByStream(is, os2, handler, item, targetFile);
                    } catch (IOException e) {
                        this.mVolumePasted += fileLength;
                        if (flag == 0) {
                            notifyPastedVolumeChanged(this.mVolumePasted, handler);
                        }
                        Utils.closeSilently(is);
                        Utils.closeSilently(os2);
                        isMoveToRecycle = false;
                        isMoveToRecycle = true;
                        if (insertDatabase(99, targetFile, item, isMoveToRecycle)) {
                            return 99;
                        }
                        return 99;
                    } catch (Exception e2) {
                        this.mVolumePasted += fileLength;
                        if (flag == 0) {
                            notifyPastedVolumeChanged(this.mVolumePasted, handler);
                        }
                        Utils.closeSilently(is);
                        Utils.closeSilently(os2);
                        isMoveToRecycle = false;
                        isMoveToRecycle = true;
                        if (insertDatabase(99, targetFile, item, isMoveToRecycle)) {
                            return 99;
                        }
                        return 99;
                    } catch (Throwable th2) {
                        th = th2;
                        this.mVolumePasted += fileLength;
                        if (flag == 0) {
                            notifyPastedVolumeChanged(this.mVolumePasted, handler);
                        }
                        Utils.closeSilently(is);
                        Utils.closeSilently(os2);
                        isMoveToRecycle = false;
                        isMoveToRecycle = true;
                        if (!insertDatabase(99, targetFile, item, isMoveToRecycle)) {
                            return 99;
                        }
                        throw th;
                    }
                } catch (IOException e3) {
                    os = null;
                    this.mVolumePasted += fileLength;
                    if (flag == 0) {
                        notifyPastedVolumeChanged(this.mVolumePasted, handler);
                    }
                    Utils.closeSilently(is);
                    Utils.closeSilently(os2);
                    isMoveToRecycle = false;
                    if (RecycleUtils.supportRecycle() && flag == 2) {
                        isMoveToRecycle = true;
                    }
                    if (insertDatabase(99, targetFile, item, isMoveToRecycle)) {
                        return 99;
                    }
                    return 99;
                } catch (Exception e4) {
                    os = null;
                    this.mVolumePasted += fileLength;
                    if (flag == 0) {
                        notifyPastedVolumeChanged(this.mVolumePasted, handler);
                    }
                    Utils.closeSilently(is);
                    Utils.closeSilently(os2);
                    isMoveToRecycle = false;
                    if (RecycleUtils.supportRecycle() && flag == 2) {
                        isMoveToRecycle = true;
                    }
                    if (insertDatabase(99, targetFile, item, isMoveToRecycle)) {
                        return 99;
                    }
                    return 99;
                } catch (Throwable th3) {
                    th = th3;
                    os = null;
                    this.mVolumePasted += fileLength;
                    if (flag == 0) {
                        notifyPastedVolumeChanged(this.mVolumePasted, handler);
                    }
                    Utils.closeSilently(is);
                    Utils.closeSilently(os2);
                    isMoveToRecycle = false;
                    if (RecycleUtils.supportRecycle() && flag == 2) {
                        isMoveToRecycle = true;
                    }
                    if (!insertDatabase(99, targetFile, item, isMoveToRecycle)) {
                        return 99;
                    }
                    throw th;
                }
                if (!isBurstPhoto) {
                    this.mFilePasted++;
                }
                if (this.mFirstPaste) {
                    this.mFirstPaste = false;
                    GalleryUtils.deleteExtraFile(this.mApp.getContentResolver(), targetFileParent, ".empty_out", ".empty_in");
                }
                this.mVolumePasted += fileLength;
                if (flag == 0) {
                    notifyPastedVolumeChanged(this.mVolumePasted, handler);
                }
                Utils.closeSilently(is);
                Utils.closeSilently(os2);
                isMoveToRecycle = false;
                if (RecycleUtils.supportRecycle() && flag == 2) {
                    isMoveToRecycle = true;
                }
                if (insertDatabase(0, targetFile, item, isMoveToRecycle)) {
                    return 99;
                }
                return 0;
            } catch (IOException e5) {
                os2 = null;
                is = null;
                this.mVolumePasted += fileLength;
                if (flag == 0) {
                    notifyPastedVolumeChanged(this.mVolumePasted, handler);
                }
                Utils.closeSilently(is);
                Utils.closeSilently(os2);
                isMoveToRecycle = false;
                isMoveToRecycle = true;
                if (insertDatabase(99, targetFile, item, isMoveToRecycle)) {
                    return 99;
                }
                return 99;
            } catch (Exception e6) {
                os2 = null;
                is = null;
                this.mVolumePasted += fileLength;
                if (flag == 0) {
                    notifyPastedVolumeChanged(this.mVolumePasted, handler);
                }
                Utils.closeSilently(is);
                Utils.closeSilently(os2);
                isMoveToRecycle = false;
                isMoveToRecycle = true;
                if (insertDatabase(99, targetFile, item, isMoveToRecycle)) {
                    return 99;
                }
                return 99;
            } catch (Throwable th4) {
                th = th4;
                os2 = null;
                is = null;
                this.mVolumePasted += fileLength;
                if (flag == 0) {
                    notifyPastedVolumeChanged(this.mVolumePasted, handler);
                }
                Utils.closeSilently(is);
                Utils.closeSilently(os2);
                isMoveToRecycle = false;
                isMoveToRecycle = true;
                if (!insertDatabase(99, targetFile, item, isMoveToRecycle)) {
                    return 99;
                }
                throw th;
            }
        }
        this.mVolumePasted += file.length();
        notifyPastedVolumeChanged(this.mVolumePasted, handler);
        return validity;
    }

    private boolean insertDatabase(int ret, File targetFile, MediaItem item, boolean isMoveToRecycle) {
        if (this.mUserState != 2 && ret == 0) {
            if (!isMoveToRecycle) {
                addFileIntoDatabase(this.mApp.getAndroidContext(), targetFile, item);
            }
            if (!item.is3DPanorama()) {
                return false;
            }
            if (item.getSpecialFileType() == 11) {
                FyuseFile.updateFyusePath(this.mApp.getAndroidContext().getContentResolver(), targetFile.getAbsolutePath());
            }
            if (getCurrentPasteState() != 2) {
                return false;
            }
            FyuseFile.startDeleteFyuseFile(this.mApp.getAndroidContext().getContentResolver(), item.getFilePath(), item.getSpecialFileType());
            return false;
        } else if (targetFile.delete()) {
            return false;
        } else {
            return true;
        }
    }

    private void copyFileByStream(InputStream is, OutputStream os, PasteEventHandler handler, MediaItem srcItem, File targetFile) throws Exception {
        long currentCopiedBits = 0;
        byte[] buffer = new byte[1048576];
        while (true) {
            int readCount = is.read(buffer);
            if (readCount == -1) {
                return;
            }
            if (getUserDecision()) {
                os.write(buffer, 0, readCount);
                currentCopiedBits += (long) readCount;
                notifyPastedVolumeChanged(this.mVolumePasted + currentCopiedBits, handler);
            } else {
                throw new Exception();
            }
        }
    }

    private boolean deleteExistFile(File file) {
        if (!file.delete()) {
            return false;
        }
        String selection = "_data=?";
        try {
            Uri baseUri = Media.EXTERNAL_CONTENT_URI;
            this.mApp.getContentResolver().delete(baseUri, selection, new String[]{file.getAbsolutePath()});
            baseUri = Video.Media.EXTERNAL_CONTENT_URI;
            this.mApp.getContentResolver().delete(baseUri, selection, new String[]{file.getAbsolutePath()});
        } catch (SecurityException e) {
            GalleryLog.noPermissionForMediaProviderLog("PasteWorker");
        }
        this.mManager.broadcastLocalDeletion();
        return true;
    }

    private void addFileIntoDatabase(Context context, File targetFile, MediaItem item) {
        MediaScannerClient mediaScannerClient = new MediaScannerClient(context, targetFile, item);
    }

    public void onPasteComplete(PasteEventHandler handler) {
        Bundle data = new Bundle();
        data.putLong("key-pastedfilecount", (long) this.mFilePasted);
        data.putInt("key-pastestate", this.mPasteState);
        handler.onPasteCompleteEvent(0, data);
        resetFlag();
    }

    private boolean notifyEvent(int pasteProcess, int eventID, Bundle data, PasteEventHandler handler) {
        switch (pasteProcess) {
            case 0:
                return handler.onPasteInitEvent(eventID, data);
            case 1:
                return handler.onPasteEvent(eventID, data);
            case 2:
                return handler.onPasteCompleteEvent(eventID, data);
            default:
                return false;
        }
    }

    private boolean notifyEventNoSpace(int pasteProcess, long spaceAvailable, long spaceNeed, PasteEventHandler handler) {
        GalleryLog.d("PasteWorker", "No space when " + pasteProcess + ", need " + spaceNeed + " bytes, left " + spaceAvailable + " bytes");
        Bundle spaceData = new Bundle();
        spaceData.putLong("key-volumeavailable", spaceAvailable);
        spaceData.putLong("key-volumeneed", spaceNeed);
        return notifyEvent(pasteProcess, 3, spaceData, handler);
    }

    private boolean notifyEventSameNameFile(int pasteProcess, String fileName, PasteEventHandler handler) {
        Bundle sameNameData = new Bundle();
        sameNameData.putString("key-filename", fileName);
        sameNameData.putInt("key-tobepastedfilecount", this.mToBePastedFileCount);
        return notifyEvent(pasteProcess, 2, sameNameData, handler);
    }

    private void notifyPastedVolumeChanged(long volume, PasteEventHandler handler) {
        Bundle data = new Bundle();
        data.putLong("key-volumecoped", volume);
        handler.onPasteEvent(11, data);
    }

    private boolean needAddExtraFile(String targetFileParent, int flag) {
        boolean ignore;
        if (targetFileParent.endsWith(Constant.CAMERA_PATH) || targetFileParent.endsWith("/Pictures/Screenshots")) {
            ignore = true;
        } else {
            ignore = WhiteList.getInstance().onMatchFile(targetFileParent);
        }
        return (ignore || flag != 1 || GalleryUtils.isDirContainMultimedia(this.mApp.getAndroidContext(), targetFileParent)) ? false : true;
    }

    private boolean dealWithTargetDir(String targetFileParent, int flag) {
        if (TextUtils.isEmpty(targetFileParent)) {
            return false;
        }
        File targetDir = new File(targetFileParent);
        if (targetDir.exists()) {
            if (!sHasAddExtraHashMap.containsKey(targetFileParent)) {
                if (needAddExtraFile(targetFileParent, flag)) {
                    GalleryUtils.makeOutsideFileForNewAlbum(this.mApp.getContentResolver(), this.mApp, targetFileParent, false);
                }
                sHasAddExtraHashMap.put(targetFileParent, Boolean.valueOf(false));
            }
        } else if (!targetDir.mkdirs()) {
            return false;
        } else {
            if (needAddExtraFile(targetFileParent, flag)) {
                GalleryUtils.makeOutsideFileForNewAlbum(this.mApp.getContentResolver(), this.mApp, targetFileParent, false);
            }
            sHasAddExtraHashMap.put(targetFileParent, Boolean.valueOf(false));
        }
        return true;
    }
}
