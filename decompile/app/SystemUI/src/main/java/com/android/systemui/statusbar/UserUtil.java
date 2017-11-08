package com.android.systemui.statusbar;

import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import com.android.systemui.R;
import com.android.systemui.statusbar.phone.SystemUIDialog;
import com.android.systemui.statusbar.policy.UserSwitcherController;

public class UserUtil {

    private static final class RemoveUserDialog extends SystemUIDialog implements OnClickListener {
        private final int mUserId;
        private final UserSwitcherController mUserSwitcherController;

        public RemoveUserDialog(Context context, int userId, UserSwitcherController userSwitcherController) {
            super(context);
            setTitle(R.string.user_remove_user_title);
            setMessage(context.getString(R.string.user_remove_user_message));
            setButton(-2, context.getString(17039360), this);
            setButton(-1, context.getString(R.string.user_remove_user_remove), this);
            setCanceledOnTouchOutside(false);
            this.mUserId = userId;
            this.mUserSwitcherController = userSwitcherController;
        }

        public void onClick(DialogInterface dialog, int which) {
            if (which == -2) {
                cancel();
                return;
            }
            dismiss();
            this.mUserSwitcherController.removeUserId(this.mUserId);
        }
    }

    public static void deleteUserWithPrompt(Context context, int userId, UserSwitcherController userSwitcherController) {
        new RemoveUserDialog(context, userId, userSwitcherController).show();
    }
}
