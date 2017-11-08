package com.huawei.systemmanager.spacecleanner.engine.tencentadapter;

import android.graphics.drawable.Drawable;
import com.google.common.collect.Lists;
import com.huawei.systemmanager.comm.Storage.PathEntry;
import com.huawei.systemmanager.comm.Storage.PathEntrySet;
import com.huawei.systemmanager.comm.collections.HsmCollections;
import com.huawei.systemmanager.comm.misc.FileUtil;
import com.huawei.systemmanager.rainbow.vaguerule.VagueRegConst;
import com.huawei.systemmanager.spacecleanner.engine.ScanParams;
import com.huawei.systemmanager.spacecleanner.engine.hwscanner.FileTraverse;
import com.huawei.systemmanager.spacecleanner.engine.hwscanner.FileVisitSign;
import com.huawei.systemmanager.spacecleanner.engine.trash.AppModelTrash;
import com.huawei.systemmanager.spacecleanner.utils.TrashUtils;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import tmsdk.fg.module.deepclean.RubbishEntity;

public class TencentAppTrash extends AppModelTrash {
    private static final String WECHAT_PKG_NAME = "com.tencent.mm";
    private String mAppName;
    private boolean mCarefulDelete;
    private Drawable mIcon;
    private String mModelName;
    private String mPkgName;
    private int mTrashType;

    public static class PathFileTraverse extends FileTraverse {
        private final Map<String, Long> mResult = HsmCollections.newArrayMap();

        private void start(ScanParams params, List<String> pathes) {
            PathEntrySet entrySet = params.getEntrySet();
            List<PathEntry> entries = Lists.newArrayListWithCapacity(pathes.size());
            for (String path : pathes) {
                entries.add(new PathEntry(path, entrySet.getPosition(path)));
            }
            startScan(entries);
        }

        protected FileVisitSign onCheckBeforeListDirectory(File file, FileVisitSign visitResult, int deepLevel) {
            this.mResult.put(file.getPath(), Long.valueOf(file.length()));
            return visitResult;
        }

        protected void onCheckFile(String path, FileVisitSign visitResult, int deepLevel) {
            this.mResult.put(path, Long.valueOf(FileUtil.getSingleFileSize(path)));
        }
    }

    private TencentAppTrash(int type, Map<String, Long> fileMap) {
        super(fileMap);
        this.mTrashType = type;
    }

    public String getModelName() {
        return this.mModelName;
    }

    public int getDiffDays() {
        return 0;
    }

    public int getType() {
        return this.mTrashType;
    }

    public long getTrashSize(int position) {
        return getTrashSize();
    }

    public String getPackageName() {
        return this.mPkgName;
    }

    public boolean isSuggestClean() {
        return !this.mCarefulDelete;
    }

    public String getAppLabel() {
        return this.mAppName;
    }

    public Drawable getAppIcon() {
        if (this.mIcon != null) {
            return this.mIcon;
        }
        this.mIcon = TrashUtils.getAppIcon(TrashUtils.getPackageInfo(getPackageName()));
        return this.mIcon;
    }

    public void printf(Appendable appendable) throws IOException {
        appendable.append("  ").append("name:").append(this.mModelName).append(",size:").append(FileUtil.getFileSize(getTrashSize())).append("\n");
        for (String p : getFiles()) {
            appendable.append("    ").append(VagueRegConst.PTAH_PREFIX).append(p).append("\n");
        }
    }

    public static final TencentAppTrash create(RubbishEntity model, int type, ScanParams p) {
        boolean z = true;
        PathFileTraverse traverse = new PathFileTraverse();
        traverse.start(p, model.getRubbishKey());
        TencentAppTrash trash = new TencentAppTrash(type, traverse.mResult);
        trash.mAppName = model.getAppName();
        trash.mModelName = model.getDescription();
        trash.mPkgName = model.getPackageName();
        if (WECHAT_PKG_NAME.equalsIgnoreCase(trash.mPkgName)) {
            trash.mCarefulDelete = true;
        } else {
            if (model.isSuggest()) {
                z = false;
            }
            trash.mCarefulDelete = z;
        }
        return trash;
    }
}
