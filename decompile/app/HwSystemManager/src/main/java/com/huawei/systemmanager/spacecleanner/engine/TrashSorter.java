package com.huawei.systemmanager.spacecleanner.engine;

import android.os.SystemProperties;
import android.util.ArraySet;
import com.huawei.systemmanager.comm.collections.HsmCollections;
import com.huawei.systemmanager.spacecleanner.engine.trash.Trash;
import com.huawei.systemmanager.spacecleanner.engine.trash.UnusedAppTrash;
import java.io.File;
import java.io.Serializable;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;
import java.util.Set;

public class TrashSorter {
    public static final Comparator<Trash> APP_PROCESS_COMPARATOR = new Comparator<Trash>() {
        public int compare(Trash lhs, Trash rhs) {
            boolean left = lhs.isSuggestClean();
            if ((left ^ rhs.isSuggestClean()) == 0) {
                return TrashSorter.SIZE_COMPARATOR.compare(lhs, rhs);
            }
            if (left) {
                return -1;
            }
            return 1;
        }
    };
    public static final Comparator<File> MODIFY_COMPARATOR = new Comparator<File>() {
        public int compare(File lhs, File rhs) {
            long compareResult = lhs.lastModified() - rhs.lastModified();
            if (compareResult > 0) {
                return 1;
            }
            if (compareResult < 0) {
                return -1;
            }
            return 0;
        }
    };
    public static final Comparator<Trash> SIZE_COMPARATOR = new Comparator<Trash>() {
        public int compare(Trash lhs, Trash rhs) {
            long compareResult = lhs.getTrashSize() - rhs.getTrashSize();
            if (compareResult > 0) {
                return -1;
            }
            if (compareResult < 0) {
                return 1;
            }
            return 0;
        }
    };
    public static final Comparator<Trash> UNUSED_APP_COMPARATOR = new Comparator<Trash>() {
        public int compare(Trash lhs, Trash rhs) {
            int dayDiff = 0;
            if ((lhs instanceof UnusedAppTrash) && (rhs instanceof UnusedAppTrash)) {
                dayDiff = ((UnusedAppTrash) lhs).getUnusedDay() - ((UnusedAppTrash) rhs).getUnusedDay();
            }
            if (dayDiff != 0) {
                return -dayDiff;
            }
            return TrashSorter.SIZE_COMPARATOR.compare(lhs, rhs);
        }
    };
    public static final Set<Integer> sMinimumFoldeTrash = Collections.unmodifiableSet(getMinimumFolderTrashSet());
    private static final TrashPriorityComparator sPriorityComparator = new TrashPriorityComparator();
    public static final Set<Integer> sRootFolderTrash = Collections.unmodifiableSet(getAssebleFolderTrashType());

    private static final class TrashPriorityComparator implements Comparator<Trash>, Serializable {
        private static final long serialVersionUID = -1;

        private TrashPriorityComparator() {
        }

        public int compare(Trash lhs, Trash rhs) {
            int leftType = lhs.getType();
            int rightType = rhs.getType();
            if (leftType == rightType) {
                return 0;
            }
            if (isFolderTrash(leftType)) {
                return isFolderTrash(rightType) ? 0 : 1;
            } else {
                if (isFolderTrash(rightType)) {
                    return -1;
                }
                if (leftType == 2048) {
                    return 1;
                }
                if (rightType == 2048 || leftType == 4) {
                    return -1;
                }
                return rightType == 4 ? 1 : 0;
            }
        }

        private boolean isFolderTrash(int type) {
            if (type == 16384 || type == 8192 || type == 65536) {
                return true;
            }
            return false;
        }
    }

    private static Set<Integer> getAssebleFolderTrashType() {
        ArraySet<Integer> set = HsmCollections.newArraySet();
        set.add(Integer.valueOf(4));
        set.add(Integer.valueOf(8));
        if (SystemProperties.getBoolean("ro.config.hw_bakfile_scan_on", false)) {
            set.add(Integer.valueOf(2097152));
        }
        set.add(Integer.valueOf(16));
        set.add(Integer.valueOf(32));
        set.add(Integer.valueOf(2048));
        return set;
    }

    private static Set<Integer> getMinimumFolderTrashSet() {
        Map<Integer, Object> map = HsmCollections.newArrayMap();
        map.put(Integer.valueOf(128), map);
        return map.keySet();
    }

    public static final int comparePriority(Trash lhs, Trash rhs) {
        return sPriorityComparator.compare(lhs, rhs);
    }
}
