package com.huawei.systemmanager.spacecleanner.engine.hwscanner.custom;

import android.text.TextUtils;
import com.google.common.collect.Lists;
import com.huawei.systemmanager.comm.Storage.PathEntry;
import com.huawei.systemmanager.comm.Storage.PathEntrySet;
import com.huawei.systemmanager.comm.collections.HsmCollections;
import com.huawei.systemmanager.rainbow.vaguerule.VagueRegConst;
import com.huawei.systemmanager.spacecleanner.engine.TrashSorter;
import com.huawei.systemmanager.spacecleanner.engine.hwadapter.HwAppCustomDataTrash;
import com.huawei.systemmanager.spacecleanner.engine.trash.Trash;
import com.huawei.systemmanager.util.HwLog;
import com.huawei.systemmanager.util.app.HsmPackageManager;
import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class HwAppCustomMgr {
    private static final long ONE_DAY = 86400000;
    private static final String TAG = "HwAppCustomMgr";
    private Map<String, HwAppDirectory> mDirectoryMap;
    private List<String> mProtectPath;
    private final PathEntrySet mRootEntrySet;

    public HwAppCustomMgr(PathEntrySet set) {
        this.mRootEntrySet = set;
    }

    public Set<String> getHwCustomPkgs() {
        return getDirectoryMap().keySet();
    }

    public List<String> getHwProtectPath() {
        if (this.mProtectPath != null) {
            return this.mProtectPath;
        }
        if (this.mRootEntrySet == null) {
            return Collections.emptyList();
        }
        this.mProtectPath = HwCustAppDataObtain.getInstance().getHwCustProtectPath(this.mRootEntrySet);
        return this.mProtectPath;
    }

    public Map<PathEntry, HwCustTrashInfo> getHwDirectories() {
        Map<String, HwAppDirectory> directoryMap = getDirectoryMap();
        Map<PathEntry, HwCustTrashInfo> result = HsmCollections.newArrayMap();
        if (directoryMap == null || directoryMap.size() < 1) {
            return result;
        }
        for (HwAppDirectory directory : directoryMap.values()) {
            result.putAll(directory.mDirectoryMap);
        }
        return result;
    }

    public List<PathEntry> getCustomFolders() {
        Map<String, HwAppDirectory> directoryMap = getDirectoryMap();
        List<PathEntry> result = Lists.newArrayList();
        if (directoryMap == null || directoryMap.values() == null) {
            return result;
        }
        for (HwAppDirectory directory : directoryMap.values()) {
            result.addAll(directory.mDirectoryMap.keySet());
        }
        return result;
    }

    private synchronized Map<String, HwAppDirectory> getDirectoryMap() {
        if (this.mDirectoryMap != null) {
            return this.mDirectoryMap;
        }
        Map<String, HwAppDirectory> directoryMap = HsmCollections.newArrayMap();
        HsmPackageManager hsmPackageManager = HsmPackageManager.getInstance();
        List<HwCustTrashInfo> trashDetails = HwCustAppDataObtain.getInstance().getHwCustTrashs();
        if (trashDetails.size() < 1) {
            HwLog.i(TAG, "There is no cust trash");
            this.mDirectoryMap = Collections.emptyMap();
            return this.mDirectoryMap;
        }
        checkDirectorysValidate(trashDetails);
        for (HwCustTrashInfo trash : trashDetails) {
            String pkg = trash.getPkgName();
            if (hsmPackageManager.getPkgInfo(pkg) != null || pkg.equals(HwCustTrashConst.DOWMLOAD_DEFAULT_NAME)) {
                HwAppDirectory appDirectory = (HwAppDirectory) directoryMap.get(pkg);
                if (appDirectory == null) {
                    appDirectory = new HwAppDirectory(pkg);
                    directoryMap.put(pkg, appDirectory);
                }
                for (PathEntry entry : this.mRootEntrySet.getPathEntryWithFileName(1, trash.getTrashPath())) {
                    for (PathEntry child : getvalidPath(entry, trash)) {
                        appDirectory.mDirectoryMap.put(child, trash);
                    }
                }
            } else {
                HwLog.i(TAG, "getDirectoryMap, pkg not installled, pkg:" + pkg);
            }
        }
        this.mDirectoryMap = Collections.unmodifiableMap(directoryMap);
        return this.mDirectoryMap;
    }

    private void checkDirectorysValidate(List<HwCustTrashInfo> trashInfoList) {
        Map<String, Integer> map = new TreeMap();
        for (HwCustTrashInfo info : trashInfoList) {
            String trashPath = info.getTrashPath();
            if (map.get(trashPath) != null) {
                map.put(trashPath, Integer.valueOf(((Integer) map.get(trashPath)).intValue() + 1));
            } else {
                map.put(trashPath, Integer.valueOf(1));
            }
        }
        for (Entry<String, Integer> entrys : map.entrySet()) {
            if (((Integer) entrys.getValue()).intValue() > 1) {
                HwLog.e(TAG, VagueRegConst.PTAH_PREFIX + ((String) entrys.getKey()) + "   this path have more than one in db.This is not invalidate!!");
            }
        }
    }

    private List<PathEntry> getvalidPath(PathEntry entry, HwCustTrashInfo detail) {
        List<PathEntry> result = Lists.newArrayList();
        if (entry == null || TextUtils.isEmpty(entry.mPath)) {
            HwLog.e(TAG, "entry path is not valid");
            return result;
        }
        File file = new File(entry.mPath);
        int position = entry.mPosition;
        String rule = detail.getMatchRule();
        int keepTime = detail.getKeepTime();
        int keepLatest = detail.getKeeplatest();
        List<String> filterPaths = detail.getFilterPaths();
        if (!TextUtils.isEmpty(rule) || keepTime >= 1 || keepLatest >= 1 || filterPaths != null) {
            if (file.isDirectory()) {
                List<File> list = Lists.newArrayList();
                try {
                    File[] files = file.listFiles();
                    if (files == null) {
                        HwLog.e(TAG, "getvalidPath files is null");
                        return result;
                    }
                    for (File fileDetail : files) {
                        String fileName = fileDetail.getName();
                        String filePath = fileDetail.getPath();
                        if (TextUtils.isEmpty(rule) || isRuleMatched(rule, fileName)) {
                            if (filterPaths != null && filterPaths.size() > 0) {
                                if (TextUtils.isEmpty(filePath)) {
                                    HwLog.e(TAG, "file path is null!");
                                } else {
                                    PathEntry pathEntry = this.mRootEntrySet.getPathEntry(filePath);
                                    if (pathEntry == null) {
                                        HwLog.e(TAG, "pathEntry is null!");
                                    } else {
                                        boolean showlidDoFilter = false;
                                        for (String path : filterPaths) {
                                            if (filePath.startsWith(pathEntry.appendPath(path).mPath)) {
                                                showlidDoFilter = true;
                                                break;
                                            }
                                        }
                                        if (showlidDoFilter) {
                                        }
                                    }
                                }
                            }
                            if (keepTime <= 0 || isKeepTimeMatched(keepTime, fileDetail)) {
                                list.add(fileDetail);
                            }
                        }
                    }
                    if (list.size() < 1) {
                        HwLog.e(TAG, "there is no child in entry path");
                        return result;
                    }
                    if (keepLatest > 0) {
                        list = getKeepLatestMatched(keepLatest, list);
                    }
                    for (File resultFile : list) {
                        result.add(new PathEntry(resultFile.getAbsolutePath(), position));
                    }
                } catch (NullPointerException ex) {
                    HwLog.e(TAG, "NullPointerException excepiton: " + ex.getMessage());
                    ex.printStackTrace();
                }
            } else if (file.isFile() && (keepTime < 1 || isKeepTimeMatched(keepTime, file))) {
                result.add(entry);
            }
            return result;
        }
        result.add(entry);
        return result;
    }

    public static boolean isRuleMatched(String rule, String fileName) {
        boolean result = false;
        if (TextUtils.isEmpty(fileName)) {
            HwLog.e(TAG, "isRuleMatched: invalid file");
            return false;
        } else if (TextUtils.isEmpty(rule)) {
            HwLog.i(TAG, "isRuleMatched: no rule");
            return true;
        } else {
            try {
                return Pattern.compile(rule).matcher(fileName).matches();
            } catch (PatternSyntaxException e) {
                HwLog.e(TAG, "isRuleMatched excepiton: " + e.getMessage());
                e.printStackTrace();
                return result;
            } catch (Exception e2) {
                HwLog.e(TAG, "isRuleMatched excepiton: " + e2.getMessage());
                e2.printStackTrace();
                return result;
            } catch (Error e3) {
                HwLog.e(TAG, "isRuleMatched Error: " + e3.getMessage());
                e3.printStackTrace();
                return result;
            }
        }
    }

    private boolean isKeepTimeMatched(int keepTime, File file) {
        boolean result = false;
        if (keepTime < 1) {
            HwLog.i(TAG, "isKeepTimeMatched: no keeptime info");
            result = true;
        }
        if (System.currentTimeMillis() - file.lastModified() >= ((long) keepTime) * 86400000) {
            return true;
        }
        return result;
    }

    private List<File> getKeepLatestMatched(int keepLatest, List<File> list) {
        List<File> result = Lists.newArrayList();
        if (list == null || keepLatest >= list.size()) {
            HwLog.i(TAG, "getKeepLatestMatched: file num < " + keepLatest);
            return result;
        }
        Collections.sort(list, TrashSorter.MODIFY_COMPARATOR);
        for (int i = 0; i < list.size() - keepLatest; i++) {
            result.add((File) list.get(i));
        }
        return result;
    }

    public void removeProtect(Trash trash) {
        if (!(trash instanceof HwAppCustomDataTrash) && !(trash instanceof HwCustAppGroup)) {
            for (String file : trash.getFiles()) {
                if (isProtectTrash(file)) {
                    trash.removeFile(file);
                }
            }
        }
    }

    public boolean isProtectTrash(String trash) {
        for (String protect : getHwProtectPath()) {
            if (trash.startsWith(protect)) {
                return true;
            }
        }
        return false;
    }
}
