package com.huawei.systemmanager.spacecleanner.ui.normalscan;

import android.content.Context;
import android.view.View.OnClickListener;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.comm.misc.GlobalContext;
import com.huawei.systemmanager.spacecleanner.ui.commonitem.CommonEnterDeepItem;

public class EnterDeepItemCreate {
    public static CommonEnterDeepItem createSaveMoreItem(OnClickListener listerner) {
        Context ctx = GlobalContext.getContext();
        return new CommonEnterDeepItem(ctx.getResources().getDrawable(R.drawable.ic_storgemanager), ctx.getString(R.string.space_clean_space_manager), ctx.getString(R.string.space_clean_save_more_sub), listerner, 1);
    }

    public static CommonEnterDeepItem createRestoreItem(OnClickListener listerner) {
        Context ctx = GlobalContext.getContext();
        return new CommonEnterDeepItem(ctx.getResources().getDrawable(R.drawable.ic_storgemanager_restore), ctx.getString(R.string.space_clean_app_restore), ctx.getString(R.string.space_clean_app_restore_sub), listerner, 2);
    }

    public static CommonEnterDeepItem createWeChatItem(OnClickListener listerner) {
        Context ctx = GlobalContext.getContext();
        return new CommonEnterDeepItem(ctx.getResources().getDrawable(R.drawable.ic_storagecleaner_wechatclean), ctx.getString(R.string.space_clean_wechat), ctx.getString(R.string.space_clean_wechat_des), listerner, 3);
    }
}
