package com.huawei.systemmanager.spacecleanner.engine.trash;

import com.huawei.harassmentinterception.common.ConstValues;
import com.huawei.systemmanager.spacecleanner.engine.trash.Trash.SimpleTrash;
import java.io.IOException;

public abstract class AppProcessTrash extends SimpleTrash implements IAppTrashInfo {
    public abstract boolean checkAlive();

    public abstract boolean isProtect();

    public abstract void refreshProtectState();

    public abstract void setProtect(boolean z);

    public String getName() {
        return getAppLabel();
    }

    public int getType() {
        return 32768;
    }

    public boolean isNormal() {
        return true;
    }

    public void printf(Appendable appendable) throws IOException {
        appendable.append("    ").append(getAppLabel()).append(ConstValues.SEPARATOR_KEYWORDS_EN).append(getPackageName()).append(ConstValues.SEPARATOR_KEYWORDS_EN).append("memory:").append(String.valueOf(getTrashSize()));
    }

    public int getPosition() {
        return 2;
    }
}
