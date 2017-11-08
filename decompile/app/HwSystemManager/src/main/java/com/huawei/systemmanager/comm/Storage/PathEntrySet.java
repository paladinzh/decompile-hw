package com.huawei.systemmanager.comm.Storage;

import android.text.TextUtils;
import com.google.common.collect.Lists;
import com.huawei.systemmanager.comm.collections.HsmCollections;
import java.util.Collections;
import java.util.List;

public class PathEntrySet {
    private final List<PathEntry> mEntries = Lists.newArrayList();

    PathEntrySet() {
    }

    PathEntrySet(List<PathEntry> entries) {
        if (!HsmCollections.isEmpty(entries)) {
            for (PathEntry entry : entries) {
                if (entry != null) {
                    this.mEntries.add(entry);
                }
            }
        }
    }

    public List<PathEntry> getPathEntry() {
        return Collections.unmodifiableList(this.mEntries);
    }

    public boolean hasSdcard() {
        for (PathEntry entry : this.mEntries) {
            if (entry.mPosition == 3) {
                return true;
            }
        }
        return false;
    }

    public int getPosition(String path) {
        if (TextUtils.isEmpty(path)) {
            return 0;
        }
        for (PathEntry entry : this.mEntries) {
            if (path.startsWith(entry.mPath)) {
                return entry.mPosition;
            }
        }
        return 0;
    }

    public PathEntry getPathEntry(String path) {
        if (TextUtils.isEmpty(path)) {
            return null;
        }
        for (PathEntry entry : this.mEntries) {
            if (path.startsWith(entry.mPath)) {
                return entry;
            }
        }
        return null;
    }

    public List<PathEntry> getPathEntryWithFileName(int position, String fileName) {
        List<PathEntry> result = Lists.newArrayList();
        for (PathEntry entry : this.mEntries) {
            if (position == 1 || position == entry.mPosition) {
                result.add(entry.appendPath(fileName));
            }
        }
        return result;
    }
}
