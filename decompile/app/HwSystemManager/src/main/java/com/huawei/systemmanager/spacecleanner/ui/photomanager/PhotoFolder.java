package com.huawei.systemmanager.spacecleanner.ui.photomanager;

import android.text.TextUtils;
import com.google.common.collect.Lists;
import com.huawei.systemmanager.comm.misc.FileUtil;
import com.huawei.systemmanager.comm.misc.GlobalContext;
import com.huawei.systemmanager.spacecleanner.engine.trash.MiminumFolderTrashGroup;
import com.huawei.systemmanager.spacecleanner.engine.trash.Trash;
import com.huawei.systemmanager.spacecleanner.ui.commonitem.CommonTrashItem;
import com.huawei.systemmanager.spacecleanner.ui.commonitem.CommonTrashItem.TrashTransFunc;
import com.huawei.systemmanager.spacecleanner.utils.nameconvertor.FolderConvertor;
import com.huawei.systemmanager.util.HwLog;
import java.util.Comparator;
import java.util.List;

public class PhotoFolder extends CommonTrashItem<MiminumFolderTrashGroup> {
    private static final int PHOTO_FIRST_INDEX = 0;
    public static final Comparator<PhotoFolder> PHOTO_FOLDER_COMPARATOR = new Comparator<PhotoFolder>() {
        public int compare(PhotoFolder lhs, PhotoFolder rhs) {
            if (lhs.getFolderIndex() == rhs.getFolderIndex()) {
                return 0;
            }
            if (lhs.getFolderIndex() < 0) {
                return 1;
            }
            if (rhs.getFolderIndex() < 0) {
                return -1;
            }
            if (lhs.getFolderIndex() < 0 || rhs.getFolderIndex() < 0) {
                return 0;
            }
            return lhs.getFolderIndex() > rhs.getFolderIndex() ? 1 : -1;
        }
    };
    private static final int PHOTO_SECOND_INDEX = 1;
    private static final String TAG = "PhotoFolder";
    public static final TrashTransFunc<PhotoFolder> sTransFunc = new TrashTransFunc<PhotoFolder>() {
        public int getTrashType() {
            return 128;
        }

        public PhotoFolder apply(Trash input) {
            if (input == null || !(input instanceof MiminumFolderTrashGroup)) {
                return null;
            }
            PhotoFolder photoFolder = new PhotoFolder((MiminumFolderTrashGroup) input);
            photoFolder.refreshContent();
            return photoFolder;
        }
    };
    private int folderIndex = -1;
    private String mPhotoFolderName;
    private final List<Trash> mUncleanedTrash = Lists.newArrayList();

    public PhotoFolder(MiminumFolderTrashGroup trash) {
        super(trash);
    }

    public String getFolderPath() {
        return ((MiminumFolderTrashGroup) this.mTrash).getPath();
    }

    public String getFolderName() {
        if (!TextUtils.isEmpty(this.mPhotoFolderName)) {
            return this.mPhotoFolderName;
        }
        int convertResId = FolderConvertor.getConvertorResId(((MiminumFolderTrashGroup) this.mTrash).getMiniumFolderName());
        if (convertResId > 0) {
            this.mPhotoFolderName = GlobalContext.getContext().getString(convertResId);
        } else {
            this.mPhotoFolderName = FileUtil.getFileName(getFolderPath());
        }
        if (!TextUtils.isEmpty(this.mPhotoFolderName)) {
            return this.mPhotoFolderName;
        }
        HwLog.e(TAG, "mPhotoFolderName is empty.");
        return "";
    }

    public List<Trash> getTrashs() {
        return this.mUncleanedTrash;
    }

    public void refreshContent() {
        super.refreshContent();
        getAllUnCleanedTrash();
    }

    private void getAllUnCleanedTrash() {
        this.mUncleanedTrash.clear();
        this.mUncleanedTrash.addAll(((MiminumFolderTrashGroup) this.mTrash).getTrashListUnclened());
    }

    public int getFolderIndex() {
        return this.folderIndex;
    }

    public void setFolderIndex(int folderIndex) {
        this.folderIndex = folderIndex;
    }

    public void checkIndex() {
        String miniumName = ((MiminumFolderTrashGroup) this.mTrash).getMiniumFolderName();
        if (FolderConvertor.isMazineUnlock(miniumName)) {
            setFolderIndex(0);
        } else if (FolderConvertor.isScreenShot(miniumName)) {
            setFolderIndex(1);
        }
    }
}
