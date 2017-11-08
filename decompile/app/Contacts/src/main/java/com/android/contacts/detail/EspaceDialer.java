package com.android.contacts.detail;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.widget.Toast;
import com.android.contacts.ContactsUtils;
import com.android.contacts.hap.CommonUtilMethods;
import com.android.contacts.hap.EmuiFeatureManager;
import com.android.contacts.hap.util.HwAnimationReflection;
import com.android.contacts.statistical.StatisticalHelper;
import com.android.contacts.util.HwLog;
import com.google.android.gms.R;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class EspaceDialer {
    private static Dialog mDialog;
    private static boolean mIsCheckEspaceSupport = false;
    private static boolean mIsShowEspace = false;

    public static void dialVoIpCall(Context context, String numberString) {
        dialVoIpCall(context, numberString, null);
    }

    public static void dialVoIpCall(Context context, String numberString, String name) {
        String number = ContactsUtils.removeDashesAndBlanks(numberString);
        if (context != null) {
            if (!querySupport(context)) {
                Toast.makeText(context, R.string.espace_uninstall, 0).show();
            } else if (CommonUtilMethods.checkConnectivityStatus(context)) {
                try {
                    HwLog.d("EspaceDialer", "eSpace calling ...");
                    Intent intent = new Intent("android.intent.action.VIEW");
                    intent.setData(getUri(number, name));
                    context.startActivity(intent);
                    StatisticalHelper.report(1175);
                } catch (ActivityNotFoundException e) {
                    Toast.makeText(context, R.string.quickcontact_missing_app_Toast, 0).show();
                }
            } else {
                showConnectExceptionDialog(context);
            }
        }
    }

    private static Uri getUri(String number, String name) {
        String callInfo = "timestamp=" + (System.currentTimeMillis() / 1000) + "&callmode=" + "VoIP" + "&callnum=" + number + "&noticeMode=" + 2;
        if (name != null) {
            callInfo = callInfo + "&cachedName=" + Uri.encode(name);
        }
        return Uri.parse("eSpace://tel?" + callInfo + "&sign=" + encrypt("eSpace://tel?9e04f98bfddd35f5ae58ea9ca04e2ea7&" + callInfo));
    }

    private static String encrypt(String strSrc) {
        try {
            byte[] bt = strSrc.getBytes("UTF-8");
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(bt);
            return bytes2Hex(md.digest());
        } catch (NoSuchAlgorithmException e) {
            return null;
        } catch (UnsupportedEncodingException e2) {
            return null;
        }
    }

    private static String bytes2Hex(byte[] bts) {
        StringBuffer buf = new StringBuffer();
        for (byte b : bts) {
            String tmp = Integer.toHexString(b & 255);
            if (tmp.length() == 1) {
                buf.append("0");
            }
            buf.append(tmp);
        }
        return buf.toString();
    }

    private static void showConnectExceptionDialog(final Context context) {
        Builder builder = new Builder(context);
        builder.setTitle(R.string.carmcard_notice_title);
        builder.setMessage(R.string.network_connection_disabled);
        builder.setNegativeButton(17039360, null);
        builder.setPositiveButton(R.string.contact_set, new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                try {
                    context.startActivity(new Intent("android.settings.SETTINGS"));
                    new HwAnimationReflection(context).overrideTransition(1);
                } catch (ActivityNotFoundException e) {
                    HwLog.w("EspaceDialer", "exception : setting activity is not found.");
                }
            }
        });
        mDialog = builder.create();
        mDialog.show();
        if (EmuiFeatureManager.isSuperSaverMode()) {
            ((AlertDialog) mDialog).getButton(-1).setEnabled(false);
        }
    }

    public static void closeDialog() {
        if (mDialog != null && mDialog.isShowing()) {
            mDialog.dismiss();
        }
    }

    public static boolean checkIsShowEspace(Context context) {
        return mIsShowEspace ? mIsCheckEspaceSupport : false;
    }

    public static boolean querySupport(Context context) {
        int isSupport = 0;
        Cursor cursor = null;
        Uri uri = Uri.parse("content://com.huawei.espace.provider/supportEMUI");
        try {
            cursor = context.getContentResolver().query(uri, new String[]{"supportEMUI"}, null, null, null);
            if (cursor == null || cursor.getCount() <= 0 || !cursor.moveToFirst()) {
                if (cursor != null) {
                    cursor.close();
                }
                if (isSupport != 1) {
                    return true;
                }
                return false;
            }
            do {
                isSupport = cursor.getInt(cursor.getColumnIndexOrThrow("support_emui"));
            } while (cursor.moveToNext());
            if (cursor != null) {
                cursor.close();
            }
            if (isSupport != 1) {
                return false;
            }
            return true;
        } catch (IllegalArgumentException e) {
            HwLog.e("EspaceDialer", "Cursor Exception !");
            if (cursor != null) {
                cursor.close();
            }
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public static void setIsShowEspace(boolean is) {
        mIsShowEspace = is;
    }

    public static void setIsCheckSuppot(boolean is) {
        mIsCheckEspaceSupport = is;
    }
}
