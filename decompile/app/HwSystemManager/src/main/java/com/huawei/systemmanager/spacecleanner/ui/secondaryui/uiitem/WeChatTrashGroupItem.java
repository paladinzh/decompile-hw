package com.huawei.systemmanager.spacecleanner.ui.secondaryui.uiitem;

import android.content.Context;
import com.huawei.systemmanager.comm.misc.FileUtil;
import com.huawei.systemmanager.spacecleanner.engine.tencentadapter.WeChatTrashGroup;
import com.huawei.systemmanager.spacecleanner.engine.trash.Trash;
import com.huawei.systemmanager.spacecleanner.ui.commonitem.CommonTrashItem;
import com.huawei.systemmanager.spacecleanner.ui.commonitem.CommonTrashItem.TrashTransFunc;
import com.huawei.systemmanager.util.HwLog;

public class WeChatTrashGroupItem extends CommonTrashItem<WeChatTrashGroup> {
    public static final TrashTransFunc<WeChatTrashGroupItem> sTransFunc = new TrashTransFunc<WeChatTrashGroupItem>() {
        public WeChatTrashGroupItem apply(Trash input) {
            if (input == null) {
                HwLog.e(TrashTransFunc.TAG, "WeChatTrashGroupItem trans, input is null!");
                return null;
            } else if (input instanceof WeChatTrashGroup) {
                return new WeChatTrashGroupItem((WeChatTrashGroup) input);
            } else {
                HwLog.e(TrashTransFunc.TAG, "WeChatTrashGroupItem trans, trans error, origin type:" + input.getType());
                return null;
            }
        }

        public int getTrashType() {
            return 1048576;
        }
    };

    public WeChatTrashGroupItem(WeChatTrashGroup trash) {
        super(trash);
    }

    public boolean hasIcon() {
        return false;
    }

    public String getName() {
        return ((WeChatTrashGroup) getTrash()).getName();
    }

    public String getDescription(Context ctx) {
        return FileUtil.getFileSize(getTrashSize());
    }

    public int getSubTrashType() {
        return ((WeChatTrashGroup) getTrash()).getWeChatTrashType();
    }
}
