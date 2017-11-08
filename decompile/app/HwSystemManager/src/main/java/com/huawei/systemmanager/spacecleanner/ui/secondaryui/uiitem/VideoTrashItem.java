package com.huawei.systemmanager.spacecleanner.ui.secondaryui.uiitem;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.format.DateUtils;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.comm.misc.Constant;
import com.huawei.systemmanager.comm.misc.FileUtil;
import com.huawei.systemmanager.comm.misc.GlobalContext;
import com.huawei.systemmanager.spacecleanner.engine.trash.Trash;
import com.huawei.systemmanager.spacecleanner.engine.trash.VideoTrash;
import com.huawei.systemmanager.spacecleanner.ui.commonitem.CommonTrashItem.TrashTransFunc;
import com.huawei.systemmanager.util.HwLog;

public class VideoTrashItem extends FileTrashItem<VideoTrash> {
    public static final TrashTransFunc<VideoTrashItem> sTransFunc = new TrashTransFunc<VideoTrashItem>() {
        public VideoTrashItem apply(Trash input) {
            if (input == null) {
                HwLog.e(TrashTransFunc.TAG, "VideoTrashItem trans, input is null!");
                return null;
            } else if (input instanceof VideoTrash) {
                return new VideoTrashItem((VideoTrash) input);
            } else {
                HwLog.e(TrashTransFunc.TAG, "VideoTrashItem trans, trans error, origin type:" + input.getType());
                return null;
            }
        }

        public int getTrashType() {
            return 256;
        }
    };

    public VideoTrashItem(VideoTrash trash) {
        super(trash);
    }

    public String getDescription(Context ctx) {
        return FileUtil.getFileSize(getTrashSize()) + "  " + DateUtils.formatDateTime(GlobalContext.getContext(), ((VideoTrash) this.mTrash).getLastModified(), Constant.SPACECLEAN_VIDEO_TIME_FORMATTER);
    }

    public Drawable getIcon(Context context) {
        return context.getResources().getDrawable(R.drawable.ic_storagecleaner_videodefault);
    }

    public boolean isUseIconAlways() {
        return true;
    }

    public int getIconWidth(Context ctx) {
        return ctx.getResources().getDimensionPixelSize(R.dimen.spaceclean_load_pic_width);
    }

    public int getIconHeight(Context ctx) {
        return ctx.getResources().getDimensionPixelSize(R.dimen.spaceclean_load_pic_height);
    }

    public boolean shouldLoadPic() {
        return true;
    }
}
