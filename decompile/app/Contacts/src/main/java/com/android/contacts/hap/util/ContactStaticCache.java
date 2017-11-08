package com.android.contacts.hap.util;

import com.google.android.gms.R;
import java.util.HashMap;

public class ContactStaticCache {
    private static HashMap<String, Integer> sMimeTypeHashmap = null;

    public static boolean isMimeTypeEqual(String aMimeType, int aIntMimeType, String aMimeTypeConstantant) {
        boolean z = false;
        int mimeType = getMimeTypeIntFromMap(aMimeType);
        if (mimeType == 0) {
            return aMimeTypeConstantant.equals(aMimeType);
        }
        if (mimeType == aIntMimeType) {
            z = true;
        }
        return z;
    }

    public static int getMimeTypeIntFromMap(String aMimeType) {
        int mimeType;
        if (sMimeTypeHashmap == null) {
            sMimeTypeHashmap = new HashMap();
            mimeType = getMimeTypeInt(aMimeType);
            if (mimeType == 0) {
                return mimeType;
            }
            sMimeTypeHashmap.put(aMimeType, Integer.valueOf(mimeType));
            return mimeType;
        } else if (sMimeTypeHashmap.containsKey(aMimeType)) {
            return ((Integer) sMimeTypeHashmap.get(aMimeType)).intValue();
        } else {
            mimeType = getMimeTypeInt(aMimeType);
            if (mimeType == 0) {
                return mimeType;
            }
            sMimeTypeHashmap.put(aMimeType, Integer.valueOf(mimeType));
            return mimeType;
        }
    }

    public static int getContactDetailLayoutForItem(String aMimeType, int aMimeTypeInt, String aCustomMimeType) {
        switch (aMimeTypeInt) {
            case 1:
                if (aCustomMimeType == null) {
                    return R.layout.detail_item_phone;
                }
                if ("ip_call".equals(aCustomMimeType)) {
                    return R.layout.detail_item_escape;
                }
                return R.layout.detail_item_with_action;
            case 2:
            case 3:
            case 4:
            case 5:
            case 6:
            case 7:
            case 9:
            case 11:
            case 13:
            case 14:
            case 15:
            case 18:
            case 20:
            case 24:
                return R.layout.detail_item_with_label_default;
            case 17:
            case 19:
                return R.layout.detail_item_wechat;
            case 21:
            case 23:
                return R.layout.detail_item_whatsapp;
            case 22:
                return R.layout.detail_item_skype;
            default:
                return R.layout.detail_item_with_action;
        }
    }

    private static int getMimeTypeInt(String aMimeType) {
        if ("vnd.android.cursor.item/name".equals(aMimeType)) {
            return 12;
        }
        if ("vnd.android.cursor.item/phone_v2".equals(aMimeType)) {
            return 1;
        }
        if ("vnd.android.cursor.item/email_v2".equals(aMimeType)) {
            return 2;
        }
        if ("vnd.android.cursor.item/contact_event".equals(aMimeType)) {
            return 3;
        }
        if ("vnd.android.cursor.item/group_membership".equals(aMimeType)) {
            return 4;
        }
        if ("vnd.android.cursor.item/im".equals(aMimeType)) {
            return 5;
        }
        if ("vnd.android.cursor.item/nickname".equals(aMimeType)) {
            return 6;
        }
        if ("vnd.android.cursor.item/note".equals(aMimeType)) {
            return 7;
        }
        if ("vnd.android.cursor.item/organization".equals(aMimeType)) {
            return 8;
        }
        if ("vnd.android.cursor.item/relation".equals(aMimeType)) {
            return 9;
        }
        if ("vnd.android.cursor.item/photo".equals(aMimeType)) {
            return 10;
        }
        if ("vnd.android.cursor.item/sip_address".equals(aMimeType)) {
            return 11;
        }
        if ("vnd.android.cursor.item/postal-address_v2".equals(aMimeType)) {
            return 13;
        }
        if ("vnd.android.cursor.item/website".equals(aMimeType)) {
            return 14;
        }
        if ("vnd.android.huawei.cursor.item/ringtone".equals(aMimeType)) {
            return 15;
        }
        if ("wechat".equals(aMimeType)) {
            return 17;
        }
        if ("qq".equals(aMimeType)) {
            return 19;
        }
        if ("whatsapp".equals(aMimeType)) {
            return 21;
        }
        if ("#phoneticName".equals(aMimeType)) {
            return 18;
        }
        if ("capability".equals(aMimeType)) {
            return 20;
        }
        if ("skype".equals(aMimeType)) {
            return 22;
        }
        if ("hwsns".equals(aMimeType)) {
            return 23;
        }
        if ("emergency".equals(aMimeType)) {
            return 24;
        }
        return 0;
    }
}
