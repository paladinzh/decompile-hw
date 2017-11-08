package com.huawei.gallery.photoshare;

import android.content.Context;
import android.text.TextUtils;
import com.android.gallery3d.R;
import com.android.gallery3d.util.ContextedUtils;
import com.huawei.android.cg.vo.AccountInfo;
import com.huawei.android.cg.vo.ShareReceiver;
import com.huawei.gallery.photoshare.ui.PhotoShareReceiverViewGroup;
import com.huawei.gallery.photoshare.utils.PhotoShareUtils;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.regex.Pattern;

public class PhotoShareAddReceiverHandler {
    public static boolean checkInputValid(Context context, String receiver, PhotoShareReceiverViewGroup receiverViewGroup, ArrayList<ShareReceiver> hasAddedFriends) {
        if (endWithSeparator(receiver)) {
            if (receiver.length() < 2) {
                showInvalidAccountToast(context, receiver);
                return false;
            }
            receiver = receiver.subSequence(0, receiver.length() - 1).toString();
        }
        if (isPhoneNumber(receiver) || receiver.contains("@")) {
            AccountInfo accountinfo = PhotoShareUtils.getLogOnAccount();
            if (accountinfo != null && receiver.equalsIgnoreCase(accountinfo.getAccountName())) {
                ContextedUtils.showToastQuickly(context, (int) R.string.photoshare_toast_not_allow_to_add_myself_Toast, 0);
                return false;
            } else if (!receiverViewGroup.exist(receiver, hasAddedFriends)) {
                return true;
            } else {
                ContextedUtils.showToastQuickly(context, MessageFormat.format(context.getString(R.string.photoshare_toast_receiver_exist_Toast), new Object[]{receiver}), 0);
                return false;
            }
        }
        showInvalidAccountToast(context, receiver);
        return false;
    }

    public static void showInvalidAccountToast(Context context, String receiver) {
        if (context != null) {
            ContextedUtils.showToastQuickly(context, MessageFormat.format(context.getString(R.string.photoshare_toast_invalidate_account_Toast), new Object[]{receiver}), 0);
        }
    }

    private static boolean isPhoneNumber(String number) {
        if (TextUtils.isEmpty(number)) {
            return false;
        }
        return Pattern.compile("(\\+[0-9]+[\\- \\.]*)?(\\([0-9]+\\)[\\- \\.]*)?([0-9][0-9\\- \\.]+[0-9])").matcher(number).matches();
    }

    public static boolean isNeedCheck(ShareReceiver info, ArrayList<ShareReceiver> topFriendsList) {
        if (info == null || TextUtils.isEmpty(info.getReceiverAcc())) {
            return false;
        }
        String account = info.getReceiverAcc();
        for (ShareReceiver item : topFriendsList) {
            if (item.getReceiverAcc().equalsIgnoreCase(account)) {
                return false;
            }
        }
        return true;
    }

    public static boolean endWithSeparator(String receiver) {
        if (receiver.endsWith(",") || receiver.endsWith(";")) {
            return true;
        }
        return false;
    }
}
