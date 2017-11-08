package com.huawei.systemmanager.spacecleanner.ui.secondaryui.uiitem;

import android.content.Context;
import com.huawei.systemmanager.comm.misc.FileUtil;
import com.huawei.systemmanager.spacecleanner.engine.trash.FileTrash;
import com.huawei.systemmanager.spacecleanner.engine.trash.Trash;
import com.huawei.systemmanager.spacecleanner.ui.commonitem.CommonTrashItem;
import com.huawei.systemmanager.spacecleanner.ui.commonitem.CommonTrashItem.TrashTransFunc;
import com.huawei.systemmanager.util.HwLog;

public class FileTrashItem<T extends FileTrash> extends CommonTrashItem<T> {

    private static class FileTransFunc extends TrashTransFunc<FileTrashItem> {
        public int mType;

        private FileTransFunc(int fileTrashType) {
            this.mType = fileTrashType;
        }

        public int getTrashType() {
            return this.mType;
        }

        public FileTrashItem apply(Trash input) {
            if (input == null) {
                HwLog.e(TrashTransFunc.TAG, "FileTransFunc trans error, input is null!");
            }
            if (input instanceof FileTrash) {
                return new FileTrashItem((FileTrash) input);
            }
            HwLog.e(TrashTransFunc.TAG, "FileTransFunc trans error, instance error");
            return null;
        }
    }

    public FileTrashItem(T trash) {
        super(trash);
    }

    public String getName() {
        return FileUtil.getFileName(((FileTrash) this.mTrash).getPath());
    }

    public String getTrashPath() {
        return ((FileTrash) this.mTrash).getPath();
    }

    public String getDescription(Context ctx) {
        return FileUtil.getFileSize(getTrashSize());
    }

    public static FileTransFunc getTransFunc(int type) {
        return new FileTransFunc(type);
    }
}
