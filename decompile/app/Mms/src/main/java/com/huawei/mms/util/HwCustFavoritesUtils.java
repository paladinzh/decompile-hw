package com.huawei.mms.util;

import android.content.Context;
import android.net.Uri;
import java.util.List;

public class HwCustFavoritesUtils {
    public static final String OPER_TYPE_GROUP_CHAT_MULTY = "groupchat-multy";
    public static final String OPER_TYPE_IM_MULTY = "chat-multy";
    public static final String OPER_TYPE_MASS_MULTY = "mass-multy";
    public static final Uri URI_FAV_GROUP_CHAT = Uri.parse("content://fav-mms/groupchat");
    public static final Uri URI_FAV_IM = Uri.parse("content://fav-mms/chat");

    public int checkAndRemoveDuplicateImMsgs(Context context, List<Long> list, int recipientSize) {
        return 0;
    }

    public int checkAndRemoveDuplicateGroupChatMsgs(Context context, List<Long> list) {
        return 0;
    }
}
