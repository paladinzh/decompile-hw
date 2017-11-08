package com.android.contacts.util;

import android.app.AlertDialog;

public class UiClosables {
    public static boolean closeQuietly(AlertDialog dialog) {
        if (dialog == null || !dialog.isShowing()) {
            return false;
        }
        dialog.dismiss();
        return true;
    }
}
