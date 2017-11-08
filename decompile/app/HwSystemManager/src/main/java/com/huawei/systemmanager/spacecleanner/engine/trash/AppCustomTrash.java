package com.huawei.systemmanager.spacecleanner.engine.trash;

import com.google.common.collect.Lists;
import java.util.List;

public abstract class AppCustomTrash extends TrashGroup implements IAppTrashInfo {
    public abstract AppCustomTrash splitNormalTrash();

    public AppCustomTrash(int type, boolean suggestClean) {
        super(type, suggestClean);
    }

    public String getName() {
        return getAppLabel();
    }

    public String getUniqueDes() {
        return getPackageName();
    }

    public List<Trash> getNormalChildren() {
        List<Trash> normalTrash = Lists.newArrayList();
        for (Trash trash : getTrashList()) {
            if (trash.isSuggestClean()) {
                normalTrash.add(trash);
            }
        }
        return normalTrash;
    }
}
