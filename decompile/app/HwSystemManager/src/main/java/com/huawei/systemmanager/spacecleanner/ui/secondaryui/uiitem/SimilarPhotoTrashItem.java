package com.huawei.systemmanager.spacecleanner.ui.secondaryui.uiitem;

import com.huawei.systemmanager.spacecleanner.engine.tencentadapter.SimilarPhotoTrash;
import com.huawei.systemmanager.spacecleanner.engine.trash.Trash;
import com.huawei.systemmanager.spacecleanner.ui.commonitem.CommonTrashItem.TrashTransFunc;
import com.huawei.systemmanager.util.HwLog;

public class SimilarPhotoTrashItem extends FileTrashItem<SimilarPhotoTrash> {
    private static final String TAG = "SimilarPhotoTrashItem";
    public static final TrashTransFunc<SimilarPhotoTrashItem> sTransFunc = new TrashTransFunc<SimilarPhotoTrashItem>() {
        public SimilarPhotoTrashItem apply(Trash input) {
            if (input == null) {
                HwLog.e(TrashTransFunc.TAG, "SimilarPhotoTrashItem trans, input is null!");
                return null;
            } else if (input instanceof SimilarPhotoTrash) {
                return new SimilarPhotoTrashItem((SimilarPhotoTrash) input);
            } else {
                HwLog.e(TrashTransFunc.TAG, "SimilarPhotoTrashItem trans, trans error, origin type:" + input.getType() + "  " + 4194304);
                return null;
            }
        }

        public int getTrashType() {
            return 4194304;
        }
    };

    public SimilarPhotoTrashItem(SimilarPhotoTrash trash) {
        super(trash);
    }
}
