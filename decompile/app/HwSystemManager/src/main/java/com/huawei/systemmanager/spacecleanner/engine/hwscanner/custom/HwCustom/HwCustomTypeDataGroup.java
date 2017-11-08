package com.huawei.systemmanager.spacecleanner.engine.hwscanner.custom.HwCustom;

import android.content.Context;
import com.huawei.systemmanager.spacecleanner.engine.hwadapter.HwCustomDataItemTrash;
import com.huawei.systemmanager.spacecleanner.engine.trash.Trash;
import com.huawei.systemmanager.spacecleanner.engine.trash.TrashGroup;
import java.io.IOException;

public class HwCustomTypeDataGroup extends TrashGroup {
    private static final String TAG = "HwCustomTypeDataGroup";
    private int mFileType;

    public int getFileType() {
        return this.mFileType;
    }

    public HwCustomTypeDataGroup(int type, boolean suggestClean, int fileType) {
        super(type, suggestClean);
        this.mFileType = fileType;
    }

    public boolean clean(Context context) {
        return super.clean(context);
    }

    public boolean addChild(Trash trash) {
        if (trash == null) {
            throw new IllegalArgumentException();
        } else if (trash.getType() != getType() || !(trash instanceof HwCustomDataItemTrash)) {
            return false;
        } else {
            this.mTrashList.add(trash);
            return true;
        }
    }

    public void printf(Appendable appendable) throws IOException {
        appendable.append("  ").append("\n");
        for (Trash trash : this) {
            trash.printf(appendable);
            appendable.append("\n");
        }
        appendable.append("\n");
    }
}
