package com.huawei.systemmanager.spacecleanner.engine.trash;

import android.content.Context;
import android.text.TextUtils;
import com.google.android.collect.Maps;
import com.google.common.collect.Lists;
import com.huawei.systemmanager.comm.misc.FileUtil;
import com.huawei.systemmanager.spacecleanner.engine.TrashSorter;
import com.huawei.systemmanager.spacecleanner.engine.trash.Trash.SimpleTrash;
import com.huawei.systemmanager.util.HwLog;
import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class TrashGroup extends SimpleTrash implements Iterable<Trash> {
    public static final TrashGroup EMPTY = new TrashGroup();
    private boolean mIsNormal;
    private long mSizeCache;
    protected final List<Trash> mTrashList;
    private final int mType;

    private TrashGroup() {
        this.mTrashList = Lists.newArrayList();
        this.mSizeCache = -1;
        this.mType = -1;
    }

    public TrashGroup(int type) {
        this.mTrashList = Lists.newArrayList();
        this.mSizeCache = -1;
        this.mType = type;
    }

    public TrashGroup(int type, boolean isNormal) {
        this.mTrashList = Lists.newArrayList();
        this.mSizeCache = -1;
        this.mType = type;
        this.mIsNormal = isNormal;
    }

    public String getName() {
        return "";
    }

    public int getType() {
        return this.mType;
    }

    public long getTrashSize(int position) {
        long size = 0;
        for (Trash trash : this.mTrashList) {
            size += trash.getTrashSize(position);
        }
        return size;
    }

    public long getTrashSize() {
        if (this.mSizeCache >= 0) {
            return this.mSizeCache;
        }
        long size = 0;
        for (Trash trash : this.mTrashList) {
            size += trash.getTrashSize();
        }
        this.mSizeCache = size;
        return this.mSizeCache;
    }

    public long getTrashSizeCleaned(boolean cleaned) {
        long size = 0;
        List<Trash> tmpTrashList = Lists.newArrayList();
        tmpTrashList.addAll(this.mTrashList);
        for (Trash item : tmpTrashList) {
            size += item.getTrashSizeCleaned(cleaned);
        }
        return size;
    }

    public boolean isSuggestClean() {
        return this.mIsNormal;
    }

    public Trash getTrash(int index) {
        if (index < 0 || index >= this.mTrashList.size()) {
            return null;
        }
        return (Trash) this.mTrashList.get(index);
    }

    public boolean addChild(Trash trash) {
        if (trash == null) {
            throw new IllegalArgumentException();
        }
        this.mTrashList.add(trash);
        this.mSizeCache = -1;
        return true;
    }

    public boolean addChildList(List<? extends Trash> trashList) {
        if (trashList == null) {
            throw new IllegalArgumentException();
        }
        this.mTrashList.addAll(trashList);
        this.mSizeCache = -1;
        return true;
    }

    public int getSize() {
        return this.mTrashList.size();
    }

    public boolean isEmpty() {
        return this.mTrashList.isEmpty();
    }

    public List<String> getFiles() {
        List<String> files = Lists.newArrayList();
        for (Trash trash : this.mTrashList) {
            files.addAll(trash.getFiles());
        }
        return files;
    }

    public boolean removeFile(String path) {
        Iterator<Trash> it = this.mTrashList.iterator();
        while (it.hasNext()) {
            Trash trash = (Trash) it.next();
            if (trash.removeFile(path)) {
                if (!trash.isValidate()) {
                    it.remove();
                }
                return true;
            }
        }
        return false;
    }

    public boolean cleanFile(String path) {
        boolean changed = false;
        for (Trash trash : this.mTrashList) {
            if (trash.cleanFile(path)) {
                changed = true;
            }
        }
        if (changed) {
            refreshContent();
        }
        return changed;
    }

    public boolean isValidate() {
        return !this.mTrashList.isEmpty();
    }

    public Iterator<Trash> iterator() {
        return this.mTrashList.iterator();
    }

    public boolean clean(Context context) {
        setCleaned();
        for (Trash trash : this.mTrashList) {
            trash.clean(context);
        }
        return true;
    }

    public int getPosition() {
        return 1;
    }

    public void printf(Appendable appendable) throws IOException {
        appendable.append("  ").append("Type:").append(TrashConst.getTypeTitle(getType())).append(",Count:").append(String.valueOf(getSize())).append(",Size:").append(FileUtil.getFileSize(getTrashSize())).append(",SuggestClean:").append(String.valueOf(isSuggestClean())).append("\n");
        for (Trash trash : this) {
            trash.printf(appendable);
            appendable.append("\n");
        }
        appendable.append("\n").append("\n");
    }

    public List<Trash> getTrashList() {
        return Collections.unmodifiableList(this.mTrashList);
    }

    public List<Trash> getTrashListUnclened() {
        List<Trash> trashes = Lists.newLinkedList();
        for (Trash trash : this.mTrashList) {
            if (!trash.isCleaned()) {
                trashes.add(trash);
            }
        }
        return trashes;
    }

    public int getTrashCount() {
        int count = 0;
        for (Trash trash : this.mTrashList) {
            count += trash.getTrashCount();
        }
        return count;
    }

    public Trash findTrashByuniqueDes(String uniqueDes) {
        if (TextUtils.isEmpty(uniqueDes)) {
            return null;
        }
        for (Trash trash : this.mTrashList) {
            if (uniqueDes.equals(trash.getUniqueDes())) {
                return trash;
            }
        }
        return null;
    }

    public void prepare() {
        Iterator<Trash> it = this.mTrashList.iterator();
        while (it.hasNext()) {
            if (!((Trash) it.next()).isValidate()) {
                it.remove();
            }
        }
        assembleRootFolderTrash();
        assembleTermateFolderTrash();
        sort();
    }

    private void assembleRootFolderTrash() {
        int trashType = getType();
        if (TrashSorter.sRootFolderTrash.contains(Integer.valueOf(trashType))) {
            Map<String, TrashGroup> folderMap = Maps.newHashMap();
            for (Trash trash : this.mTrashList) {
                if (trash instanceof FileTrash) {
                    FileTrash fileTrash = (FileTrash) trash;
                    String rootFolderPath = fileTrash.getRootFolderPath();
                    if (TextUtils.isEmpty(rootFolderPath)) {
                        HwLog.w(Trash.TAG, "assembleRootFolderTrash failed! rootFolderPath is empty!");
                    } else {
                        TrashGroup folderGroup = (TrashGroup) folderMap.get(rootFolderPath);
                        if (folderGroup == null) {
                            folderGroup = new RootFolderTrashGroup(trashType, isSuggestClean(), rootFolderPath, fileTrash.getPathEntry());
                            folderMap.put(rootFolderPath, folderGroup);
                        }
                        folderGroup.addChild(fileTrash);
                    }
                } else {
                    HwLog.e(Trash.TAG, "prepare data, trash is in sRootFolderTrash but is not filetrash, error! trash type:" + trash.getType());
                }
            }
            this.mTrashList.clear();
            this.mTrashList.addAll(folderMap.values());
            this.mSizeCache = -1;
        }
    }

    private void assembleTermateFolderTrash() {
        int trashType = getType();
        if (TrashSorter.sMinimumFoldeTrash.contains(Integer.valueOf(trashType))) {
            Map<String, TrashGroup> folderMap = Maps.newHashMap();
            for (Trash trash : this.mTrashList) {
                if (trash instanceof FileTrash) {
                    FileTrash fileTrash = (FileTrash) trash;
                    String minimumFolder = fileTrash.getTerminateFolderPath();
                    TrashGroup folderGroup = (TrashGroup) folderMap.get(minimumFolder);
                    if (folderGroup == null) {
                        folderGroup = new MiminumFolderTrashGroup(trashType, isSuggestClean(), minimumFolder, fileTrash.getPathEntry());
                        folderMap.put(minimumFolder, folderGroup);
                    }
                    folderGroup.addChild(trash);
                } else {
                    HwLog.e(Trash.TAG, "prepare data, trash is in sRootFolderTrash but is not filetrash, error! trash type:" + trash.getType());
                }
            }
            this.mTrashList.clear();
            this.mTrashList.addAll(folderMap.values());
            this.mSizeCache = -1;
        }
    }

    private void sort() {
        if (this.mType == 2) {
            Collections.sort(this.mTrashList, TrashSorter.UNUSED_APP_COMPARATOR);
        } else if (this.mType == 32768) {
            Collections.sort(this.mTrashList, TrashSorter.APP_PROCESS_COMPARATOR);
        } else if (this.mType != 4194304) {
            Collections.sort(this.mTrashList, TrashSorter.SIZE_COMPARATOR);
        } else {
            return;
        }
        for (Trash trash : this.mTrashList) {
            if (trash instanceof TrashGroup) {
                ((TrashGroup) trash).sort();
            }
        }
    }

    public void refreshContent() {
        if (!isCleaned()) {
            boolean cleaned = true;
            for (Trash trash : this.mTrashList) {
                trash.refreshContent();
                if (!trash.isCleaned()) {
                    cleaned = false;
                }
            }
            if (cleaned) {
                setCleaned();
            }
            this.mSizeCache = -1;
            getTrashSize();
        }
    }

    public int getTrashIndex(Trash t) {
        return this.mTrashList.indexOf(t);
    }
}
