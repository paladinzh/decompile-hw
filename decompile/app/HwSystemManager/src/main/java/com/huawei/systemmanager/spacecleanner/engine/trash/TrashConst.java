package com.huawei.systemmanager.spacecleanner.engine.trash;

import android.content.Context;
import android.util.SparseIntArray;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.comm.misc.GlobalContext;
import com.huawei.systemmanager.util.HwLog;

public class TrashConst {
    private static final String TAG = "Trash.TrashConst";
    private static final SparseIntArray sTypeDesctiption = new SparseIntArray();

    public static String getTypeTitle(int type) {
        Context ctx = GlobalContext.getContext();
        int resId = sTypeDesctiption.get(type, -1);
        if (resId >= 0) {
            return ctx.getString(resId);
        }
        HwLog.e(TAG, "getTypeTitle, unknow type:" + type);
        return "";
    }

    static {
        sTypeDesctiption.put(1, R.string.space_clear_app_cache_group_new);
        sTypeDesctiption.put(4, R.string.space_clean_large_file);
        sTypeDesctiption.put(8, R.string.space_clean_log_file);
        sTypeDesctiption.put(2097152, R.string.space_clean_bak_file);
        sTypeDesctiption.put(16, R.string.space_app_tmp_trashes);
        sTypeDesctiption.put(32, R.string.space_clean_empty_folder);
        sTypeDesctiption.put(2048, R.string.space_clean_thumbnail_file);
        sTypeDesctiption.put(128, R.string.space_clean_trash_photo);
        sTypeDesctiption.put(512, R.string.space_clean_trash_audio);
        sTypeDesctiption.put(256, R.string.space_clean_trash_video);
        sTypeDesctiption.put(1024, R.string.space_clean_apk_file_change1);
        sTypeDesctiption.put(8192, R.string.space_clear_app_residual_group);
        sTypeDesctiption.put(16384, R.string.space_clean_app_cache_data);
        sTypeDesctiption.put(2, R.string.space_clean_unused_app);
        sTypeDesctiption.put(32768, R.string.space_clean_trash_process);
        sTypeDesctiption.put(65536, R.string.space_clean_top_video_app_trash);
        sTypeDesctiption.put(81920, R.string.space_clean_app_cache_data);
    }
}
