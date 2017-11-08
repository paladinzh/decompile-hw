package com.huawei.systemmanager.spacecleanner.engine.hwscanner;

import com.google.common.collect.Sets;
import com.huawei.systemmanager.comm.Storage.PathEntry;
import com.huawei.systemmanager.comm.misc.FileUtil;
import com.huawei.systemmanager.comm.misc.GlobalContext;
import com.huawei.systemmanager.spacecleanner.engine.ScanParams;
import com.huawei.systemmanager.spacecleanner.engine.SpaceXmlHelper;
import com.huawei.systemmanager.spacecleanner.engine.base.SpaceConst;
import com.huawei.systemmanager.spacecleanner.engine.hwscanner.custom.HwAppCustomMgr;
import com.huawei.systemmanager.util.HwLog;
import java.util.List;
import java.util.Set;

public class TypeFilter {
    private static final String BAK_SUFFIX = ".bak";
    private static final long LARGE_FILE_SIZE = 10485760;
    private static final String LOG_SUFFIX = ".log";
    private static final String TEMP_SUFFIX = ".temp";
    private static final String THUMBNAIL_FOLDER_NAME = ".thumbnails";
    private static final String TMP_SUFFIX = ".tmp";
    private volatile boolean mInited = false;
    private final Set<String> mSkipFolders = Sets.newHashSet();
    private final Set<String> mTempSuffix = Sets.newHashSet();
    private final Set<String> mThumbnailFolder = Sets.newHashSet();

    public void initial(ScanParams p) {
        if (!this.mInited) {
            this.mInited = true;
            initSkipFolders(p);
            initThumbnailsPath();
            initTempSuffix();
        }
    }

    public boolean checkIsThumbnailFolder(String fileName) {
        return this.mThumbnailFolder.contains(fileName);
    }

    public boolean checkTempFileSuffix(String suffix) {
        return this.mTempSuffix.contains(suffix);
    }

    public boolean checkLogFileSuffix(String suffix) {
        return LOG_SUFFIX.equals(suffix);
    }

    public boolean checkBakFileSuffix(String suffix) {
        return BAK_SUFFIX.equals(suffix);
    }

    public boolean checkIfLargeFile(String filePath) {
        return FileUtil.getSingleFileSize(filePath) > 10485760;
    }

    public boolean checkShouldSkip(String fileName, int deep) {
        return this.mSkipFolders.contains(fileName);
    }

    private void initSkipFolders(ScanParams p) {
        List<String> ignorePath = SpaceXmlHelper.getIgnorePath(GlobalContext.getContext(), p.getEntrySet());
        HwAppCustomMgr appMgr = (HwAppCustomMgr) p.getCarry();
        List<String> protectPath = appMgr.getHwProtectPath();
        this.mSkipFolders.addAll(ignorePath);
        this.mSkipFolders.addAll(protectPath);
        HwLog.i(SpaceConst.TAG, "ignore folder is:" + this.mSkipFolders);
        for (PathEntry pathEntry : appMgr.getCustomFolders()) {
            this.mSkipFolders.add(pathEntry.mPath);
        }
    }

    private void initThumbnailsPath() {
        this.mThumbnailFolder.add(THUMBNAIL_FOLDER_NAME);
    }

    private void initTempSuffix() {
        this.mTempSuffix.add(TMP_SUFFIX);
        this.mTempSuffix.add(TEMP_SUFFIX);
    }
}
