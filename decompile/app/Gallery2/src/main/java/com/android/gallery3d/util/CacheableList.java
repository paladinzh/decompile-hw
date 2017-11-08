package com.android.gallery3d.util;

import java.util.HashMap;
import java.util.Map;

public abstract class CacheableList {
    private Map<String, Boolean> mCachedList = new HashMap(50);

    protected abstract boolean onMatchFile(String str);

    public boolean match(String filePath) {
        if (this.mCachedList.containsKey(filePath)) {
            return ((Boolean) this.mCachedList.get(filePath)).booleanValue();
        }
        boolean match = matchFilePath(filePath);
        this.mCachedList.put(filePath, Boolean.valueOf(match));
        return match;
    }

    private boolean matchFilePath(String filePath) {
        if (filePath == null) {
            return false;
        }
        return onMatchFile(filePath);
    }
}
