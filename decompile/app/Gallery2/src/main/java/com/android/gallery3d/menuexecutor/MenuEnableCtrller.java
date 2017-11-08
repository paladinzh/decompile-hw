package com.android.gallery3d.menuexecutor;

import android.support.v4.print.PrintHelper;
import com.android.gallery3d.ui.SelectionManager;
import com.android.gallery3d.util.MediaSetUtils;
import com.huawei.gallery.actionbar.Action;
import com.huawei.gallery.actionbar.ActionBarStateBase;
import com.huawei.gallery.photoshare.utils.PhotoShareUtils;

public class MenuEnableCtrller {
    public static void updateMenuOperation(ActionBarStateBase mode, int supported) {
        boolean supportDelete = (supported & 1) != 0;
        boolean supportRotate = (supported & 2) != 0;
        boolean supportShare = (supported & 4) != 0;
        boolean supportSetAs = (supported & 32) != 0;
        boolean supportEdit = (supported & 4608) != 0;
        boolean supportInfo = (supported & 1024) != 0;
        boolean supportPhotoshareDownload = (268435456 & supported) != 0;
        boolean supportMyFavorite = (536870912 & supported) != 0;
        boolean supportMove = (8388608 & supported) != 0;
        boolean supportCopy = (16777216 & supported) != 0;
        boolean supportRename = (1073741824 & supported) != 0;
        boolean supportPrint = ((131072 & supported) != 0) & PrintHelper.systemSupportsPrint();
        boolean supportShowOnMap = (supported & 16) != 0;
        boolean supportRangeMeasure = (4194304 & supported) != 0;
        boolean supportPhotoShareDelete = (supported & 1) != 0;
        boolean z = PhotoShareUtils.isSupportPhotoShare() ? supportShare : false;
        mode.setActionEnable(supportDelete, Action.ACTION_ID_DEL);
        mode.setActionEnable(supportShare, Action.ACTION_ID_SHARE);
        mode.setActionEnable(supportSetAs, Action.ACTION_ID_SETAS);
        mode.setActionEnable(supportEdit, Action.ACTION_ID_EDIT);
        mode.setActionEnable(supportInfo, Action.ACTION_ID_DETAIL);
        if (!supportPhotoshareDownload) {
            mode.changeAction(Action.ACTION_ID_PHOTOSHARE_DOWNLOADING, Action.ACTION_ID_PHOTOSHARE_DOWNLOAD);
        }
        mode.setActionEnable(supportPhotoshareDownload, Action.ACTION_ID_PHOTOSHARE_DOWNLOAD);
        mode.setActionEnable(supportMyFavorite, Action.ACTION_ID_MYFAVORITE);
        mode.setActionEnable(supportMyFavorite, Action.ACTION_ID_NOT_MYFAVORITE);
        mode.setActionEnable(supportMove, Action.ACTION_ID_MOVE);
        mode.setActionEnable(supportCopy, Action.ACTION_ID_COPY);
        mode.setActionEnable(supportPrint, Action.ACTION_ID_PRINT);
        mode.setActionEnable(supportRename, Action.ACTION_ID_RENAME);
        mode.setActionEnable(supportShowOnMap, Action.ACTION_ID_SHOW_ON_MAP);
        mode.setActionEnable(supportRotate, Action.ACTION_ID_ROTATE_LEFT);
        mode.setActionEnable(supportRotate, Action.ACTION_ID_ROTATE_RIGHT);
        mode.setActionEnable(supportRangeMeasure, Action.ACTION_ID_RANGE_MEASURE);
        mode.setActionEnable(supportPhotoShareDelete, Action.ACTION_ID_PHOTOSHARE_DELETE);
        mode.setActionEnable(z, Action.ACTION_ID_PHOTOSHARE_BACKUP);
    }

    public static void updateMenuOperationForList(ActionBarStateBase mode, int supported, SelectionManager selectManager) {
        if (mode != null && selectManager != null) {
            int selectCount = selectManager.getSelectedCount();
            boolean isEmptyAlbumOnly = selectManager.isOnlyEmptyAlbumSelected();
            boolean supportShare = (supported & 4) != 0 ? !isEmptyAlbumOnly : false;
            boolean supportMoveIn = (1048576 & supported) != 0;
            boolean supportMoveOut = (2097152 & supported) != 0;
            boolean supportDelete = (supported & 1) != 0;
            boolean supportRename = ((supported & 1073741824) == 0 || selectCount != 1 || selectManager.isSpecificAlbumSelected(MediaSetUtils.MAGAZINE_UNLOCK_BUCKET_ID) || selectManager.isSpecificAlbumSelected(MediaSetUtils.PRELOAD_PICTURES_BUCKET_ID)) ? false : !selectManager.isSpecificAlbumSelected(MediaSetUtils.HWTHEME_WALLPAPER_BUCKET_ID);
            boolean supportInfo = (supported & 1024) != 0 && selectCount == 1;
            boolean supportPhotoShareBackUp = (!PhotoShareUtils.isSupportPhotoShare() || (supported & 1073741824) == 0 || selectCount != 1 || isEmptyAlbumOnly) ? false : !selectManager.isSpecificAlbumSelected(MediaSetUtils.SCREENSHOTS_BUCKET_ID);
            mode.setActionEnable(supportShare, Action.ACTION_ID_SHARE);
            mode.setActionEnable(supportMoveIn, Action.ACTION_ID_MOVEIN);
            mode.setActionEnable(supportMoveOut, Action.ACTION_ID_MOVEOUT);
            mode.setActionEnable(supportDelete, Action.ACTION_ID_DEL);
            mode.setActionEnable(supportRename, Action.ACTION_ID_RENAME);
            mode.setActionEnable(supportInfo, Action.ACTION_ID_DETAIL);
            mode.setActionEnable(supportPhotoShareBackUp, Action.ACTION_ID_PHOTOSHARE_BACKUP);
        }
    }
}
