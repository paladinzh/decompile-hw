package com.android.settings.users;

import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface.OnClickListener;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.UserInfo;
import android.os.UserHandle;
import android.os.UserManager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.android.settings.PrivacySpaceSettingsHelper;
import com.android.settings.Utils;

public final class UserDialogs {
    public static Dialog createRemoveDialog(Context context, int removingUserId, OnClickListener onConfirmListener) {
        UserInfo userInfo = ((UserManager) context.getSystemService("user")).getUserInfo(removingUserId);
        Builder builder = new Builder(context).setPositiveButton(2131626480, onConfirmListener).setNegativeButton(17039360, null);
        if (userInfo.isManagedProfile()) {
            builder.setTitle(2131626472);
            View view = createRemoveManagedUserDialogView(context, removingUserId);
            if (view != null) {
                builder.setView(view);
            } else {
                builder.setMessage(2131626475);
            }
        } else if (PrivacySpaceSettingsHelper.isPrivacyUser(userInfo)) {
            View content1 = ((Activity) context).getLayoutInflater().inflate(2130969051, null);
            ((TextView) content1.findViewById(2131887075)).setText(context.getString(2131628773, new Object[]{userInfo.name}));
            builder.setView(content1);
        } else if (UserHandle.myUserId() == removingUserId) {
            if (PrivacySpaceSettingsHelper.isPrivacyUser(userInfo)) {
                builder.setView(((Activity) context).getLayoutInflater().inflate(2130969051, null));
            } else if (userInfo.isGuest()) {
                builder.setMessage(2131628730);
            } else {
                builder.setMessage(2131628699);
            }
        } else if (userInfo.isRestricted()) {
            builder.setTitle(2131626471);
            builder.setMessage(2131626477);
        } else {
            builder.setMessage(2131628703);
        }
        return builder.create();
    }

    private static View createRemoveManagedUserDialogView(Context context, int userId) {
        PackageManager packageManager = context.getPackageManager();
        ApplicationInfo mdmApplicationInfo = Utils.getAdminApplicationInfo(context, userId);
        if (mdmApplicationInfo == null) {
            return null;
        }
        View view = ((LayoutInflater) context.getSystemService("layout_inflater")).inflate(2130968734, null);
        ((ImageView) view.findViewById(2131886485)).setImageDrawable(packageManager.getUserBadgedIcon(packageManager.getApplicationIcon(mdmApplicationInfo), new UserHandle(userId)));
        CharSequence appLabel = packageManager.getApplicationLabel(mdmApplicationInfo);
        CharSequence badgedAppLabel = packageManager.getUserBadgedLabel(appLabel, new UserHandle(userId));
        TextView textView = (TextView) view.findViewById(2131886486);
        textView.setText(appLabel);
        if (!appLabel.toString().contentEquals(badgedAppLabel)) {
            textView.setContentDescription(badgedAppLabel);
        }
        return view;
    }

    public static Dialog createEnablePhoneCallsAndSmsDialog(Context context, OnClickListener onConfirmListener) {
        return new Builder(context).setTitle(2131626491).setMessage(2131626492).setPositiveButton(2131624573, onConfirmListener).setNegativeButton(17039360, null).create();
    }

    public static Dialog createEnablePhoneCallsDialog(Context context, OnClickListener onConfirmListener) {
        return new Builder(context).setTitle(2131626489).setMessage(2131626490).setPositiveButton(2131624573, onConfirmListener).setNegativeButton(17039360, null).create();
    }

    public static Dialog createQuitDialog(Context context, int removingUserId, OnClickListener onConfirmListener) {
        String username = ((UserManager) context.getSystemService("user")).getUserInfo(removingUserId).name;
        return new Builder(context).setPositiveButton(2131628695, onConfirmListener).setNegativeButton(2131628693, null).setTitle(context.getString(2131628721, new Object[]{username})).setMessage(2131628727).create();
    }
}
