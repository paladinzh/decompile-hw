package com.android.contacts.preference;

import android.content.Context;
import android.content.Intent;
import com.google.android.gms.R;

public class UpdateUtil {
    public static final String getSummaryKey(int id) {
        return "key_ver_" + id;
    }

    public static final String getItemKey(int id) {
        return "auto_item_" + id;
    }

    public static final String getItemString(Context context, int item) {
        if (context == null) {
            return null;
        }
        String itemStr;
        switch (item) {
            case 0:
                itemStr = context.getString(R.string.update_all_network);
                break;
            case 1:
                itemStr = context.getString(R.string.update_wlan_only);
                break;
            case 2:
                itemStr = context.getString(R.string.str_contacts_menu_disabled);
                break;
            default:
                itemStr = null;
                break;
        }
        return itemStr;
    }

    public static void startUpdateActivity(Context context, int fileId) {
        if (context != null) {
            Intent intent = new Intent("com.android.contacts.action.UPDATE");
            intent.putExtra("fileId", fileId);
            intent.setPackage(context.getPackageName());
            context.startActivity(intent);
        }
    }
}
