package com.android.mms.ui;

import android.content.Context;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import android.provider.Settings.System;
import android.text.TextUtils;
import cn.com.xy.sms.sdk.SmartSmsSdkUtil;
import com.android.mms.HwCustMmsConfig;
import com.android.mms.MmsConfig;
import com.android.mms.data.Contact;
import com.huawei.cust.HwCustUtils;
import com.huawei.mms.util.ProviderCallUtils;
import com.huawei.mms.util.ProviderCallUtils.CallRequest;
import com.huawei.mms.util.VerifitionSmsManager;

public class PreferenceUtils {
    private static HwCustMmsConfig mHwCustMmsConfig = ((HwCustMmsConfig) HwCustUtils.createObj(HwCustMmsConfig.class, new Object[0]));
    private static int mMaxSimMessageCount = 50;

    public static boolean getNotificationEnabled(Context context) {
        return true;
    }

    public static boolean getUsingConversation(Context context) {
        return true;
    }

    public static boolean getIsGroupMmsEnabled(Context context) {
        boolean groupMmsPrefOn = PreferenceManager.getDefaultSharedPreferences(context).getBoolean("pref_key_mms_group_mms", true);
        if (MmsConfig.getGroupMmsEnabled() && groupMmsPrefOn && !TextUtils.isEmpty(MessageUtils.getLocalNumber())) {
            return true;
        }
        return false;
    }

    public static boolean getForwardMessageFrom(Context context) {
        boolean ret = PreferenceManager.getDefaultSharedPreferences(context).getBoolean("pref_key_forward_message_from_settings", true);
        if (mHwCustMmsConfig != null) {
            return mHwCustMmsConfig.custEnableForwardMessageFrom(ret);
        }
        return ret;
    }

    public static boolean isEnableAutoReplyMmsRR(Context context) {
        if (MmsConfig.getMMSReadReportsEnabled()) {
            return PreferenceManager.getDefaultSharedPreferences(context).getBoolean("pref_key_mms_auto_reply_read_reports", MmsConfig.getDefaultMMSAutoReplyReadReports());
        }
        return false;
    }

    public static boolean isSmsRecoveryEnable(final Context context) {
        if (MmsConfig.isSmsRecyclerEnable()) {
            return isSmsRecoveryPreferenceEnabel(context);
        }
        if (isSmsRecoveryPreferenceEnabel(context)) {
            new CallRequest("method_enable_recovery", context) {
                protected void setParam() {
                    this.mRequest.putBoolean("recovery_status", false);
                }

                protected void onCallBack() {
                    Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
                    editor.putBoolean("pref_key_recovery_support", false);
                    editor.commit();
                    ProviderCallUtils.cleanTrashBox(context, -1);
                }
            }.makeCall();
        }
        return false;
    }

    private static boolean isSmsRecoveryPreferenceEnabel(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean("pref_key_recovery_support", false);
    }

    public static void enableMessagePreview(boolean enabled, Context context) {
        Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        editor.putBoolean("pref_key_enable_notifications_preview", enabled);
        editor.apply();
    }

    public static int getSimMaxMessageCount() {
        return mMaxSimMessageCount;
    }

    public static boolean getUsingSIMCardStorage(Context context) {
        return false;
    }

    public static boolean getUsingSIMCardStorageWithSubId(Context context, int sub) {
        return false;
    }

    public static void setAutoRetrieval(String state, Context context) {
        Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        editor.putString("pref_key_mms_auto_retrieval_mms", state);
        editor.commit();
    }

    public static boolean isCancelSendEnable(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean("pref_key_cancel_send_enable", false);
    }

    public static void setIsAskBeforeDeleting(boolean enabled, Context context) {
        Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        editor.putBoolean("pref_key_ask_before_deleting_new", enabled);
        editor.commit();
    }

    public static boolean isAskBeforeDeleting(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean("pref_key_ask_before_deleting_new", true);
    }

    public static synchronized boolean saveFontScale(Context context, float prefsValue) {
        synchronized (PreferenceUtils.class) {
            if (prefsValue < 0.7f || prefsValue > 3.0f) {
                return false;
            }
            boolean preferenceFloat = setPreferenceFloat(context, "pref_key_sms_font_scale", prefsValue);
            return preferenceFloat;
        }
    }

    public static synchronized boolean setPreferenceFloat(Context context, String prefsKey, float prefsValue) {
        boolean commit;
        synchronized (PreferenceUtils.class) {
            commit = PreferenceManager.getDefaultSharedPreferences(context).edit().putFloat(prefsKey, prefsValue).commit();
        }
        return commit;
    }

    public static synchronized float getPreferenceFloat(Context context, String prefKey, float defaultValue) {
        float f;
        synchronized (PreferenceUtils.class) {
            f = PreferenceManager.getDefaultSharedPreferences(context).getFloat(prefKey, defaultValue);
        }
        return f;
    }

    public static void setVerifitionSmsProtectEnable(Context context, boolean isChecked) {
        System.putInt(context.getContentResolver(), "verifition_sms_protect_enable", isChecked ? 1 : 0);
        if (!isChecked) {
            VerifitionSmsManager.getInstance().resetSecretFlag(context);
        }
    }

    public static void setFunctionTipsNoShowAgain(Context context, boolean noShowAgain) {
        Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        if (Contact.IS_CHINA_REGION) {
            editor.putBoolean(SmartSmsSdkUtil.SMARTSMS_NO_SHOW_AGAIN, true);
        } else {
            editor.putBoolean("riskUrlPermission_not_show", true);
        }
        editor.apply();
    }

    public static void setRiskUrlFunctionTipsOpen(Context context) {
        Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        editor.putBoolean("pref_key_risk_url_check", true);
        editor.putBoolean("riskUrlPermission_not_show", true);
        editor.apply();
    }
}
