package com.huawei.rcs.util;

import android.content.Context;
import android.content.IContentProvider;
import android.net.Uri;
import android.os.Bundle;
import com.android.rcs.RcsCommonConfig;
import com.huawei.cspcommon.MLog;
import com.huawei.mms.util.FavoritesUtils;
import com.huawei.mms.util.HwCustFavoritesUtils;
import java.util.Collection;
import java.util.List;

public class RcsFavoritesUtils {
    private static final long[] NULL_ARRAY_NULL = new long[0];
    public static final Uri URI_FAV_GROUP_CHAT = Uri.parse("content://fav-mms/groupchat");
    public static final Uri URI_FAV_IM = Uri.parse("content://fav-mms/chat");

    private long[] getDuplicateIm(IContentProvider icp, List<Long> ids, int recipientSize) {
        if (ids == null || ids.size() == 0) {
            return NULL_ARRAY_NULL;
        }
        Bundle bd = null;
        if (recipientSize == 1) {
            try {
                bd = icp.call("com.android.mms", "CHECK-DUPLICATE", HwCustFavoritesUtils.OPER_TYPE_IM_MULTY, getBundle(ids));
            } catch (Throwable e) {
                MLog.e("RcsFavoritesUtils", "CHECK existsDuplicateIM ", e);
            } catch (Throwable e2) {
                MLog.e("RcsFavoritesUtils", "CHECK existsDuplicateIM ", e2);
            }
        } else if (recipientSize > 1) {
            bd = icp.call("com.android.mms", "CHECK-DUPLICATE", HwCustFavoritesUtils.OPER_TYPE_MASS_MULTY, getBundle(ids));
        }
        if (bd != null && bd.containsKey("result-array")) {
            return bd.getLongArray("result-array");
        }
        return NULL_ARRAY_NULL;
    }

    public int checkAndRemoveDuplicateImMsgs(Context context, List<Long> imIds, int recipientSize) {
        if (!RcsCommonConfig.isRCSSwitchOn()) {
            return 0;
        }
        IContentProvider icp = context.getContentResolver().acquireProvider(FavoritesUtils.URI_FAV);
        if (icp == null) {
            return -1;
        }
        return removeDuplicate(imIds, FavoritesUtils.filterDumplicateFromTable(getDuplicateIm(icp, imIds, recipientSize), Uri.parse("content://rcsim/chat")));
    }

    public int checkAndRemoveDuplicateGroupChatMsgs(Context context, List<Long> groupChatMessageIds) {
        if (!RcsCommonConfig.isRCSSwitchOn()) {
            return 0;
        }
        IContentProvider icp = context.getContentResolver().acquireProvider(FavoritesUtils.URI_FAV);
        if (icp == null) {
            return -1;
        }
        return removeDuplicate(groupChatMessageIds, FavoritesUtils.filterDumplicateFromTable(getDuplicateGroupChatMessage(icp, groupChatMessageIds), Uri.parse("content://rcsim/rcs_group_message")));
    }

    private long[] getDuplicateGroupChatMessage(IContentProvider icp, List<Long> ids) {
        if (ids == null || ids.size() == 0) {
            return NULL_ARRAY_NULL;
        }
        try {
            Bundle bd = icp.call("com.android.mms", "CHECK-DUPLICATE", HwCustFavoritesUtils.OPER_TYPE_GROUP_CHAT_MULTY, getBundle(ids));
            if (bd != null && bd.containsKey("result-array")) {
                return bd.getLongArray("result-array");
            }
        } catch (Throwable e) {
            MLog.e("RcsFavoritesUtils", "CHECK existsDuplicateGroupchat ", e);
        } catch (Throwable e2) {
            MLog.e("RcsFavoritesUtils", "CHECK existsDuplicateGroupchat ", e2);
        }
        return NULL_ARRAY_NULL;
    }

    private int removeDuplicate(Collection<Long> from, long[] tomove) {
        int i = 0;
        if (tomove == null || tomove.length == 0) {
            return 0;
        }
        int removed = 0;
        int length = tomove.length;
        while (i < length) {
            if (from.remove(Long.valueOf(tomove[i]))) {
                removed++;
            }
            i++;
        }
        return removed;
    }

    private Bundle getBundle(List<Long> ids) {
        Bundle bd = new Bundle();
        bd.putString("where-condition", FavoritesUtils.getSelectionString("origin_id", (List) ids));
        return bd;
    }
}
