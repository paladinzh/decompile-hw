package com.huawei.systemmanager.spacecleanner.engine.hwscanner.custom;

import android.content.Context;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.ArrayMap;
import com.google.android.collect.Maps;
import com.google.common.collect.Lists;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.comm.Storage.PathEntry;
import com.huawei.systemmanager.comm.collections.HsmCollections;
import com.huawei.systemmanager.comm.misc.FileUtil;
import com.huawei.systemmanager.spacecleanner.engine.ScanParams;
import com.huawei.systemmanager.spacecleanner.engine.base.Task;
import com.huawei.systemmanager.spacecleanner.engine.hwadapter.HwAppCustomDataTrash;
import com.huawei.systemmanager.spacecleanner.engine.hwscanner.FileTraverse;
import com.huawei.systemmanager.spacecleanner.engine.hwscanner.FileVisitSign;
import com.huawei.systemmanager.util.HwLog;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class HwCustomScanTask extends Task {
    private static final String HWVPLAYER_PKG_NAME_VERSION_4 = "com.huawei.hwvplayer.youku";
    private static final String TAG = "HwCustomScanTask";
    private final FileTraverse mFileTraver = new InnerFileTravsers();

    private class InnerFileTravsers extends FileTraverse {
        private Map<String, Long> mTempFileMap;
        private List<HwAppCustomDataTrash> mTrashList;

        private InnerFileTravsers() {
            this.mTempFileMap = HsmCollections.newArrayMap();
            this.mTrashList = Lists.newArrayList();
        }

        public void start() {
            Map<PathEntry, HwCustTrashInfo> map = ((HwAppCustomMgr) HwCustomScanTask.this.mParams.getCarry()).getHwDirectories();
            if (map.isEmpty()) {
                HwLog.w(HwCustomScanTask.TAG, "getHwDirectories map is empty");
                return;
            }
            int progress = 0;
            int totalSize = map.size();
            for (Entry<PathEntry, HwCustTrashInfo> mapEntry : map.entrySet()) {
                PathEntry pathEntry = (PathEntry) mapEntry.getKey();
                HwCustTrashInfo directoryDetail = (HwCustTrashInfo) mapEntry.getValue();
                String path = pathEntry.mPath;
                progress++;
                HwCustomScanTask.this.onPublishProgress((progress / totalSize) * 100, path);
                if (!TextUtils.isEmpty(path) && FileUtil.isExsist(path)) {
                    this.mTempFileMap.clear();
                    scanFile(path, FileVisitSign.create(pathEntry), 0);
                    if (!this.mTempFileMap.isEmpty()) {
                        Map<String, Long> fileMap = HsmCollections.newArrayMap((ArrayMap) this.mTempFileMap);
                        String pkgName = directoryDetail.getPkgName();
                        if (pkgName != null) {
                            HwAppCustomDataTrash trash;
                            if (pkgName.equals(HwCustomScanTask.HWVPLAYER_PKG_NAME_VERSION_4)) {
                                trash = HwAppCustomDataTrash.create(fileMap, directoryDetail, 65536, pathEntry);
                            } else {
                                trash = HwAppCustomDataTrash.create(fileMap, directoryDetail, 16384, pathEntry);
                            }
                            this.mTrashList.add(trash);
                        }
                    }
                }
            }
            dealResult();
        }

        private void dealResult() {
            if (this.mTrashList == null || this.mTrashList.size() < 1) {
                HwLog.e(HwCustomScanTask.TAG, "dealResult, mTrashList is null");
                return;
            }
            long start = SystemClock.elapsedRealtime();
            Map<String, HwCustAppGroup> trashMap = Maps.newHashMap();
            for (HwAppCustomDataTrash trash : this.mTrashList) {
                String pkgName = trash.getPackageName();
                HwCustAppGroup appTrash = (HwCustAppGroup) trashMap.get(pkgName);
                if (appTrash == null) {
                    appTrash = new HwCustAppGroup(trash.getType(), pkgName, trash.getAppLabel(), false);
                    trashMap.put(pkgName, appTrash);
                }
                appTrash.addChild(trash);
            }
            List<HwCustAppGroup> trashes = Lists.newArrayList();
            trashes.addAll(trashMap.values());
            for (HwCustAppGroup trash2 : trashes) {
                HwCustomScanTask.this.onPublishItemUpdate(trash2);
            }
            HwLog.i(HwCustomScanTask.this.getTaskName(), "dealResult cost time:" + (SystemClock.elapsedRealtime() - start));
        }

        protected void onCheckFile(String path, FileVisitSign visitResult, int deepLevel) {
            this.mTempFileMap.put(path, Long.valueOf(FileUtil.getSingleFileSize(path)));
        }
    }

    public HwCustomScanTask(Context context) {
        super(context);
    }

    public void cancel() {
        super.cancel();
        this.mFileTraver.cancel();
    }

    protected void doTask(ScanParams p) {
        this.mFileTraver.start();
        onPublishEnd();
    }

    public String getTaskName() {
        return TAG;
    }

    public int getType() {
        return 3;
    }

    protected int getWeight() {
        return getContext().getResources().getInteger(R.integer.scan_weight_trash_hw_cust);
    }

    public List<Integer> getSupportTrashType() {
        return HsmCollections.newArrayList(Integer.valueOf(16384));
    }

    public boolean isNormal() {
        return true;
    }
}
