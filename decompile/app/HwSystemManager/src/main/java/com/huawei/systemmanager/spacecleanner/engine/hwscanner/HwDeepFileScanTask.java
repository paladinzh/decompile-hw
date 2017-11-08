package com.huawei.systemmanager.spacecleanner.engine.hwscanner;

import android.content.Context;
import android.text.TextUtils;
import com.google.common.collect.Lists;
import com.huawei.systemmanager.comm.Storage.PathEntry;
import com.huawei.systemmanager.comm.Storage.PathEntrySet;
import com.huawei.systemmanager.comm.collections.HsmCollections;
import com.huawei.systemmanager.comm.misc.FileUtil;
import com.huawei.systemmanager.spacecleanner.engine.ScanParams;
import com.huawei.systemmanager.spacecleanner.engine.base.Task;
import com.huawei.systemmanager.spacecleanner.engine.trash.BakFileTrash;
import com.huawei.systemmanager.spacecleanner.engine.trash.EmptyFileTrash;
import com.huawei.systemmanager.spacecleanner.engine.trash.LargeFileTrash;
import com.huawei.systemmanager.spacecleanner.engine.trash.LogFileTrash;
import com.huawei.systemmanager.spacecleanner.engine.trash.TempFileTrash;
import com.huawei.systemmanager.spacecleanner.engine.trash.ThumbnailTrash;
import com.huawei.systemmanager.spacecleanner.engine.trash.Trash;
import com.huawei.systemmanager.util.HwLog;
import java.io.File;
import java.util.List;
import java.util.Locale;

public class HwDeepFileScanTask extends Task {
    public static final int DEFAULT_MAX_DEEP_LEVEL = 10;
    private static final String TAG = "HwDeepFileScanner";
    private final DeepFileTraverse mFileScanner = new DeepFileTraverse();

    public class DeepFileTraverse extends FileTraverse {
        private static final int FLAG_EMPTY_FOLDER = 3;
        private static final int FLAG_FILE = 1;
        private static final int FLAG_NOT_EMPTY_FOLDER = 2;
        private static final int FLAG_TERMINATE = -1;
        private final TypeFilter mTypeFilter = new TypeFilter();

        public void start(ScanParams params) {
            this.mTypeFilter.initial(params);
            PathEntrySet entrySet = params.getEntrySet();
            if (params.getType() == 4) {
                entrySet = PathEntry.buildInternalRootEntrySet();
            }
            for (PathEntry entry : HsmCollections.newArrayList(entrySet.getPathEntry())) {
                String path = entry.mPath;
                if (TextUtils.isEmpty(path)) {
                    HwLog.e(HwDeepFileScanTask.TAG, "path is empty! position = " + entry.mPosition);
                } else {
                    scanFileWithFlag(path, FileVisitSign.create(entry), 0);
                }
            }
        }

        private int scanFileWithFlag(String path, FileVisitSign visitSign, int deepLevel) {
            if (checkCancelSignal(path, visitSign, deepLevel)) {
                return -1;
            }
            if (FileUtil.isDirectory(path)) {
                File file = new File(path);
                FileVisitSign vr = onCheckBeforeListDirectory(file, visitSign, deepLevel);
                if (vr == FileVisitSign.TERMINATE) {
                    HwLog.d(HwDeepFileScanTask.TAG, "onCheckBeforeListDirectory end");
                    return -1;
                }
                String[] children = file.list();
                List list = null;
                boolean allEmpty = true;
                if (children != null && children.length > 0) {
                    list = Lists.newLinkedList();
                    for (String childName : children) {
                        String childPath = path + File.separatorChar + childName;
                        if (scanFileWithFlag(childPath, vr, deepLevel + 1) == 3) {
                            list.add(childPath);
                        } else {
                            allEmpty = false;
                        }
                    }
                }
                if (deepLevel != 0 && allEmpty) {
                    return 3;
                }
                checkEmptyFolder(list, visitSign, deepLevel);
                return 2;
            }
            onCheckFile(path, visitSign, deepLevel);
            return 1;
        }

        private void checkEmptyFolder(List<String> emptyFolders, FileVisitSign visitSign, int deepLevel) {
            if (!HsmCollections.isEmpty(emptyFolders) && deepLevel <= 10) {
                for (String path : emptyFolders) {
                    onTrashFound(new EmptyFileTrash(path, visitSign.getPathEntry()));
                }
            }
        }

        protected FileVisitSign onCheckBeforeListDirectory(File file, FileVisitSign sign, int deepLevel) {
            FileVisitSign res = sign;
            String path = file.getPath();
            onProgressChange(true, 0, path);
            if (this.mTypeFilter.checkShouldSkip(path, deepLevel)) {
                return FileVisitSign.TERMINATE;
            }
            String fileName = file.getName();
            if (deepLevel > 10) {
                return FileVisitSign.TERMINATE;
            }
            if (this.mTypeFilter.checkIsThumbnailFolder(fileName)) {
                return sign.addInclude(2048);
            }
            return sign;
        }

        protected void onCheckFile(String filePath, FileVisitSign visitSign, int deepLevel) {
            onProgressChange(false, 0, filePath);
            if (visitSign.checkInclude(2048)) {
                onTrashFound(new ThumbnailTrash(filePath, visitSign.getPathEntry()));
            }
            if (deepLevel <= 10) {
                String suffix = FileUtil.getFileSuffix(filePath).toLowerCase(Locale.ENGLISH);
                if (this.mTypeFilter.checkTempFileSuffix(suffix)) {
                    onTrashFound(new TempFileTrash(filePath, visitSign.getPathEntry()));
                }
                if (this.mTypeFilter.checkLogFileSuffix(suffix)) {
                    onTrashFound(LogFileTrash.create(filePath, visitSign.getPathEntry()));
                }
                if (this.mTypeFilter.checkBakFileSuffix(suffix)) {
                    onTrashFound(BakFileTrash.create(filePath, visitSign.getPathEntry()));
                }
                if (this.mTypeFilter.checkIfLargeFile(filePath)) {
                    onTrashFound(new LargeFileTrash(filePath, visitSign.getPathEntry()));
                }
            }
        }

        protected void onTrashFound(Trash trash) {
            HwDeepFileScanTask.this.onPublishItemUpdate(trash);
        }

        protected void onProgressChange(boolean folder, int progress, String info) {
            HwDeepFileScanTask.this.onPublishProgress(progress, info);
        }
    }

    public HwDeepFileScanTask(Context ctx) {
        super(ctx);
    }

    public void cancel() {
        super.cancel();
        this.mFileScanner.cancel();
    }

    public String getTaskName() {
        return TAG;
    }

    public int getType() {
        return 52;
    }

    protected void doTask(ScanParams p) {
        this.mFileScanner.start(p);
        onPublishEnd();
    }

    public List<Integer> getSupportTrashType() {
        return HsmCollections.newArrayList(Integer.valueOf(2048), Integer.valueOf(16), Integer.valueOf(8), Integer.valueOf(4), Integer.valueOf(32), Integer.valueOf(2097152));
    }

    public boolean isNormal() {
        return false;
    }
}
