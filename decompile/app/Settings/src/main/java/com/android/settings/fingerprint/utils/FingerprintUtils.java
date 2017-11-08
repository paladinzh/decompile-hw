package com.android.settings.fingerprint.utils;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.hardware.fingerprint.Fingerprint;
import android.hardware.fingerprint.FingerprintManager;
import android.hardware.fingerprint.FingerprintManager.RemovalCallback;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.provider.Settings.Secure;
import android.provider.Settings.System;
import android.util.Log;
import android.widget.Toast;
import com.android.settings.ConfirmLockPassword.InternalActivity;
import com.android.settings.HwCustSplitUtils;
import com.android.settings.SettingsExtUtils;
import com.android.settings.Utils;
import com.android.settings.fingerprint.FingerprintSettingsActivity;
import com.android.settings.search.Index;
import com.huawei.cust.HwCustUtils;

public class FingerprintUtils {
    public static final boolean FP_SHOW_NOTIFICATION_ON = SystemProperties.getBoolean("ro.config.fp_add_notification", true);
    public static final boolean HAS_FP_CUST_NAVIGATION = SystemProperties.getBoolean("ro.config.fp_navigation_plk", false);
    public static final boolean HAS_FP_NAVIGATION = SystemProperties.getBoolean("ro.config.fp_navigation", true);
    public static final boolean QUICK_HWPAY_ON = SystemProperties.getBoolean("ro.config.quick_hwpay_on", false);

    public static class DelayFinishHander extends Handler {
        Activity mActivity;

        public DelayFinishHander(Activity activity) {
            this.mActivity = activity;
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    if (this.mActivity != null) {
                        FingerprintUtils.closeSplitContent(this.mActivity);
                        this.mActivity.finish();
                        return;
                    }
                    return;
                default:
                    return;
            }
        }
    }

    public static class FingerRemovalCallback extends RemovalCallback {
        Activity activity;

        public FingerRemovalCallback(Activity activity) {
            this.activity = activity;
        }

        public void onRemovalSucceeded(Fingerprint fingerprint) {
            Log.i("FingerprintUtils", "Fingerprint removed: " + fingerprint.getFingerId());
            if (System.getIntForUser(this.activity.getContentResolver(), "fingerprint_alipay_dialog", 1, UserHandle.myUserId()) != 1) {
                System.putIntForUser(this.activity.getContentResolver(), "fingerprint_alipay_dialog", 1, UserHandle.myUserId());
            }
            FingerprintUtils.onFingerprintNumChanged(this.activity, UserHandle.myUserId());
        }

        public void onRemovalError(Fingerprint fp, int errMsgId, CharSequence errString) {
            if (this.activity != null) {
                Toast.makeText(this.activity, errString, 0);
            }
            Log.e("FingerprintUtils", "Remove fingerprint failed, error msg id: " + errMsgId);
        }
    }

    public static int getAppLockAssociation(android.content.ContentResolver r10) {
        /* JADX: method processing error */
/*
Error: jadx.core.utils.exceptions.JadxRuntimeException: Can't find block by offset: 0x0072 in list []
	at jadx.core.utils.BlockUtils.getBlockByOffset(BlockUtils.java:43)
	at jadx.core.dex.instructions.IfNode.initBlocks(IfNode.java:60)
	at jadx.core.dex.visitors.blocksmaker.BlockFinish.initBlocksInIfNodes(BlockFinish.java:48)
	at jadx.core.dex.visitors.blocksmaker.BlockFinish.visit(BlockFinish.java:33)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:306)
	at jadx.api.JavaClass.decompile(JavaClass.java:62)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:199)
*/
        /*
        r9 = -1;
        r2 = 0;
        if (r10 != 0) goto L_0x000e;
    L_0x0004:
        r0 = "FingerprintUtils";
        r2 = "contentResolver is NULL!";
        android.util.Log.e(r0, r2);
        return r9;
    L_0x000e:
        r0 = "content://com.huawei.systemmanager.applockprovider/fingerprintstatus";
        r1 = android.net.Uri.parse(r0);
        r6 = 0;
        r0 = r10;
        r3 = r2;
        r4 = r2;
        r5 = r2;
        r6 = r0.query(r1, r2, r3, r4, r5);
        if (r6 != 0) goto L_0x0030;
    L_0x0020:
        r0 = "FingerprintUtils";	 Catch:{ Exception -> 0x005f, all -> 0x0073 }
        r2 = "app lock provider get query cursor failed";	 Catch:{ Exception -> 0x005f, all -> 0x0073 }
        android.util.Log.w(r0, r2);	 Catch:{ Exception -> 0x005f, all -> 0x0073 }
        if (r6 == 0) goto L_0x002f;
    L_0x002b:
        r6.close();
        r6 = 0;
    L_0x002f:
        return r9;
    L_0x0030:
        r6.moveToFirst();	 Catch:{ Exception -> 0x005f, all -> 0x0073 }
        r0 = "fingerprintBindType";	 Catch:{ Exception -> 0x005f, all -> 0x0073 }
        r0 = r6.getColumnIndex(r0);	 Catch:{ Exception -> 0x005f, all -> 0x0073 }
        r8 = r6.getInt(r0);	 Catch:{ Exception -> 0x005f, all -> 0x0073 }
        r0 = "FingerprintUtils";	 Catch:{ Exception -> 0x005f, all -> 0x0073 }
        r2 = new java.lang.StringBuilder;	 Catch:{ Exception -> 0x005f, all -> 0x0073 }
        r2.<init>();	 Catch:{ Exception -> 0x005f, all -> 0x0073 }
        r3 = "app lock bind stat val = ";	 Catch:{ Exception -> 0x005f, all -> 0x0073 }
        r2 = r2.append(r3);	 Catch:{ Exception -> 0x005f, all -> 0x0073 }
        r2 = r2.append(r8);	 Catch:{ Exception -> 0x005f, all -> 0x0073 }
        r2 = r2.toString();	 Catch:{ Exception -> 0x005f, all -> 0x0073 }
        android.util.Log.i(r0, r2);	 Catch:{ Exception -> 0x005f, all -> 0x0073 }
        if (r6 == 0) goto L_0x005e;
    L_0x005a:
        r6.close();
        r6 = 0;
    L_0x005e:
        return r8;
    L_0x005f:
        r7 = move-exception;
        r0 = "FingerprintUtils";	 Catch:{ Exception -> 0x005f, all -> 0x0073 }
        r2 = "app lock provider query failed";	 Catch:{ Exception -> 0x005f, all -> 0x0073 }
        android.util.Log.w(r0, r2);	 Catch:{ Exception -> 0x005f, all -> 0x0073 }
        r7.printStackTrace();	 Catch:{ Exception -> 0x005f, all -> 0x0073 }
        if (r6 == 0) goto L_0x0072;
    L_0x006e:
        r6.close();
        r6 = 0;
    L_0x0072:
        return r9;
    L_0x0073:
        r0 = move-exception;
        if (r6 == 0) goto L_0x007a;
    L_0x0076:
        r6.close();
        r6 = 0;
    L_0x007a:
        throw r0;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.settings.fingerprint.utils.FingerprintUtils.getAppLockAssociation(android.content.ContentResolver):int");
    }

    public static int getHwidAssociation(android.content.ContentResolver r10) {
        /* JADX: method processing error */
/*
Error: jadx.core.utils.exceptions.JadxRuntimeException: Can't find block by offset: 0x0072 in list []
	at jadx.core.utils.BlockUtils.getBlockByOffset(BlockUtils.java:43)
	at jadx.core.dex.instructions.IfNode.initBlocks(IfNode.java:60)
	at jadx.core.dex.visitors.blocksmaker.BlockFinish.initBlocksInIfNodes(BlockFinish.java:48)
	at jadx.core.dex.visitors.blocksmaker.BlockFinish.visit(BlockFinish.java:33)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:306)
	at jadx.api.JavaClass.decompile(JavaClass.java:62)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:199)
*/
        /*
        r9 = -1;
        if (r10 != 0) goto L_0x000d;
    L_0x0003:
        r0 = "FingerprintUtils";
        r2 = "contentResolver is NULL!";
        android.util.Log.e(r0, r2);
        return r9;
    L_0x000d:
        r0 = "content://com.huawei.hwid.api.provider/query/0";
        r1 = android.net.Uri.parse(r0);
        r6 = 0;
        r2 = 0;
        r3 = 0;
        r4 = 0;
        r5 = 0;
        r0 = r10;
        r6 = r0.query(r1, r2, r3, r4, r5);	 Catch:{ Exception -> 0x005f, all -> 0x0073 }
        if (r6 != 0) goto L_0x0030;	 Catch:{ Exception -> 0x005f, all -> 0x0073 }
    L_0x0020:
        r0 = "FingerprintUtils";	 Catch:{ Exception -> 0x005f, all -> 0x0073 }
        r2 = "huawei account provider get query cursor failed";	 Catch:{ Exception -> 0x005f, all -> 0x0073 }
        android.util.Log.w(r0, r2);	 Catch:{ Exception -> 0x005f, all -> 0x0073 }
        if (r6 == 0) goto L_0x002f;
    L_0x002b:
        r6.close();
        r6 = 0;
    L_0x002f:
        return r9;
    L_0x0030:
        r6.moveToFirst();	 Catch:{ Exception -> 0x005f, all -> 0x0073 }
        r0 = "fingerprintBindType";	 Catch:{ Exception -> 0x005f, all -> 0x0073 }
        r0 = r6.getColumnIndex(r0);	 Catch:{ Exception -> 0x005f, all -> 0x0073 }
        r8 = r6.getInt(r0);	 Catch:{ Exception -> 0x005f, all -> 0x0073 }
        r0 = "FingerprintUtils";	 Catch:{ Exception -> 0x005f, all -> 0x0073 }
        r2 = new java.lang.StringBuilder;	 Catch:{ Exception -> 0x005f, all -> 0x0073 }
        r2.<init>();	 Catch:{ Exception -> 0x005f, all -> 0x0073 }
        r3 = "hwid bind stat val = ";	 Catch:{ Exception -> 0x005f, all -> 0x0073 }
        r2 = r2.append(r3);	 Catch:{ Exception -> 0x005f, all -> 0x0073 }
        r2 = r2.append(r8);	 Catch:{ Exception -> 0x005f, all -> 0x0073 }
        r2 = r2.toString();	 Catch:{ Exception -> 0x005f, all -> 0x0073 }
        android.util.Log.i(r0, r2);	 Catch:{ Exception -> 0x005f, all -> 0x0073 }
        if (r6 == 0) goto L_0x005e;
    L_0x005a:
        r6.close();
        r6 = 0;
    L_0x005e:
        return r8;
    L_0x005f:
        r7 = move-exception;
        r0 = "FingerprintUtils";	 Catch:{ Exception -> 0x005f, all -> 0x0073 }
        r2 = "huawei account provider query failed";	 Catch:{ Exception -> 0x005f, all -> 0x0073 }
        android.util.Log.w(r0, r2);	 Catch:{ Exception -> 0x005f, all -> 0x0073 }
        r7.printStackTrace();	 Catch:{ Exception -> 0x005f, all -> 0x0073 }
        if (r6 == 0) goto L_0x0072;
    L_0x006e:
        r6.close();
        r6 = 0;
    L_0x0072:
        return r9;
    L_0x0073:
        r0 = move-exception;
        if (r6 == 0) goto L_0x007a;
    L_0x0076:
        r6.close();
        r6 = 0;
    L_0x007a:
        throw r0;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.settings.fingerprint.utils.FingerprintUtils.getHwidAssociation(android.content.ContentResolver):int");
    }

    public static boolean delayFinishActivity(Activity activity) {
        return new DelayFinishHander(activity).sendEmptyMessageDelayed(1, 100);
    }

    public static String generateFpPrefKey(int fpId) {
        StringBuffer sb = new StringBuffer("fp_");
        sb.append(fpId);
        return sb.toString();
    }

    public static int getStrongBoxAssociation(ContentResolver contentResolver) {
        if (contentResolver == null) {
            Log.e("FingerprintUtils", "contentResolver is NULL!");
            return -1;
        }
        try {
            Bundle assBundle = contentResolver.call(Uri.parse("content://com.huawei.hidisk.fingerprint"), "query_is_box_bindstat", null, null);
            if (assBundle == null) {
                Log.w("FingerprintUtils", "strong box provider get call bundle failed");
                return -1;
            }
            int val = assBundle.getInt("fingerprintBindType");
            Log.i("FingerprintUtils", "strong box bind stat val = " + val);
            return val;
        } catch (Exception e) {
            Log.w("FingerprintUtils", "strong box provider call failed");
            e.printStackTrace();
            return -1;
        }
    }

    public static boolean unbindByProviderCall(int whichApp, ContentResolver contentResolver) {
        boolean z = false;
        if (contentResolver == null) {
            Log.e("FingerprintUtils", "contentResolver is NULL!");
            return false;
        }
        Uri providerUri;
        switch (whichApp) {
            case 301:
                providerUri = Uri.parse("content://com.huawei.hwid.api.provider/unbind/0");
                break;
            case 302:
                providerUri = Uri.parse("content://com.huawei.systemmanager.applockprovider");
                break;
            case 303:
                providerUri = Uri.parse("content://com.huawei.hidisk.fingerprint");
                break;
            default:
                Log.e("FingerprintUtils", "Unexpected App : " + whichApp);
                return false;
        }
        try {
            Bundle bundle = contentResolver.call(providerUri, "unbind_fingerprint", null, null);
            if (bundle == null) {
                Log.w("FingerprintUtils", "unbind: get bundle fail, request is " + whichApp);
                return false;
            }
            int val = bundle.getInt("fingerprintBindType");
            Log.i("FingerprintUtils", "unbind: get bind stat val = " + val);
            if (val == 0) {
                z = true;
            }
            return z;
        } catch (Exception e) {
            Log.w("FingerprintUtils", "unbind call fail");
            e.printStackTrace();
            return false;
        }
    }

    public static boolean unbindApp(int whichApp, ContentResolver contentResolver) {
        if (contentResolver == null) {
            Log.e("FingerprintUtils", "contentResolver is NULL!");
            return false;
        }
        int status;
        switch (whichApp) {
            case 301:
                status = getHwidAssociation(contentResolver);
                break;
            case 302:
                status = getAppLockAssociation(contentResolver);
                break;
            case 303:
                status = getStrongBoxAssociation(contentResolver);
                break;
            default:
                Log.e("FingerprintUtils", "Unbinf unexpected App : " + whichApp);
                return false;
        }
        if (status == 1) {
            return unbindByProviderCall(whichApp, contentResolver);
        }
        return true;
    }

    public static void removeAllFingerprintTemplates(Context context, RemovalCallback callback, FingerprintManager manager) {
        if (context == null || callback == null) {
            Log.e("FingerprintUtils", "Null arguments, do not remove any fingerprint");
            return;
        }
        FingerprintManager fpm = manager;
        if (manager == null) {
            fpm = (FingerprintManager) context.getSystemService("fingerprint");
        }
        if (fpm == null || !fpm.isHardwareDetected()) {
            Log.e("FingerprintUtils", "Fingerprint service not supported, do not remove any fingerprint");
            return;
        }
        int userId = UserHandle.myUserId();
        fpm.remove(new Fingerprint(null, userId, 0, 0), userId, callback);
        ContentResolver contentResolver = context.getContentResolver();
        if (!unbindApp(302, contentResolver)) {
            Log.e("FingerprintUtils", "Failed to unbind AppLock!");
        }
        if (!unbindApp(303, contentResolver)) {
            Log.e("FingerprintUtils", "Failed to unbind StrongBox!");
        }
        if (!unbindApp(301, contentResolver)) {
            Log.e("FingerprintUtils", "Failed to unbind Huawei ID!");
        }
    }

    public static int getPaymentStatus(Context context) {
        int paymentStatus = 0;
        if (context == null) {
            Log.e("FingerprintUtils", "getPaymentStatus null context!");
            return paymentStatus;
        }
        try {
            return Secure.getIntForUser(context.getContentResolver(), "fp_shortcut_enabled", UserHandle.myUserId());
        } catch (Exception e) {
            Log.e("FingerprintUtils", "Failed to query shortcut payment finger.");
            e.printStackTrace();
            return paymentStatus;
        }
    }

    public static boolean isHwpayInstalled(Context context) {
        if (context != null) {
            return Utils.hasPackageInfo(context.getPackageManager(), "com.huawei.wallet");
        }
        Log.e("FingerprintUtils", "isHwpayInstalled : context is null!");
        return false;
    }

    public static boolean isPackageInstalled(Context context, String pkgName) {
        if (context == null) {
            Log.e("FingerprintUtils", "isPackageInstalled : context is null!");
            return false;
        }
        try {
            context.getPackageManager().getPackageInfo(pkgName, 1);
            return true;
        } catch (NameNotFoundException e) {
            return false;
        } catch (Exception e2) {
            return false;
        }
    }

    public static boolean isQuickHwpayOn() {
        return SettingsExtUtils.isGlobalVersion() ? QUICK_HWPAY_ON : true;
    }

    public static int getKeyguardAssociationStatus(Context context, int userId) {
        if (context == null) {
            Log.e("FingerprintUtils", "getKeyguardAssociationStatus, null context.");
            return -1;
        }
        try {
            int queryState = Secure.getIntForUser(context.getContentResolver(), "fp_keyguard_enable", userId);
            if (queryState != 0) {
                queryState = 1;
            }
            return queryState;
        } catch (Exception e) {
            Log.d("FingerprintUtils", "no keyguard value!");
            return -1;
        }
    }

    public static void setKeyguardAssociationStatus(Context context, int userId, boolean enabled) {
        if (context == null) {
            Log.e("FingerprintUtils", "putKeyguardAssociationStatus, null context.");
            return;
        }
        int i;
        ContentResolver contentResolver = context.getContentResolver();
        String str = "fp_keyguard_enable";
        if (enabled) {
            i = 1;
        } else {
            i = 0;
        }
        if (!Secure.putIntForUser(contentResolver, str, i, userId)) {
            Log.e("FingerprintUtils", "Failed to set keyguard association");
        }
    }

    public static void onFingerprintNumChanged(Context context, int userId) {
        Index.getInstance(context).updateFromClassNameResource("com.android.settings.fingerprint.FingerprintSettingsFragment", true, true);
    }

    public static boolean isHwidSupported(Context context) {
        boolean isHwidSupported = false;
        if (context == null) {
            return false;
        }
        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(Uri.parse("content://com.huawei.hwid.api.provider/is_support_fingerprint"), null, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                isHwidSupported = cursor.getInt(cursor.getColumnIndex("isSupport")) == 1;
            }
            if (cursor != null) {
                cursor.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (cursor != null) {
                cursor.close();
            }
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
        }
        return isHwidSupported;
    }

    public static boolean shouldShowAppLock(Context context, int userId) {
        if (context == null) {
            Log.w("FingerprintUtils", "context is null. Do not support applock!");
            return false;
        } else if (!isPackageInstalled(context, "com.huawei.systemmanager")) {
            Log.i("FingerprintUtils", "app not installed. Do not support applock!");
            return false;
        } else if (userId == 0) {
            return true;
        } else {
            Log.i("FingerprintUtils", "the current user is not owner, userId = " + userId + ". Do not support applock!");
            return false;
        }
    }

    public static void startConfirmPasswordForResult(Activity activity, long challenge, int userId) {
        if (activity != null) {
            Intent intent = new Intent();
            intent.setClassName("com.android.settings", InternalActivity.class.getName());
            intent.putExtra("android.intent.extra.USER_ID", userId);
            intent.putExtra("has_challenge", true);
            intent.putExtra("challenge", challenge);
            intent.putExtra("return_credentials", true);
            activity.startActivityForResult(intent, 3001);
        }
    }

    public static void startChoosePasswordForResult(Activity activity, long challenge, int userId) {
        if (activity != null) {
            Intent intent = new Intent();
            intent.setClassName("com.android.settings", "com.android.settings.ChooseLockGeneric");
            intent.putExtra("is_fp_screen_lock", true);
            intent.putExtra("minimum_quality", 65536);
            intent.putExtra("android.intent.extra.USER_ID", userId);
            intent.putExtra("hide_disabled_prefs", true);
            intent.putExtra("has_challenge", true);
            intent.putExtra("challenge", challenge);
            activity.startActivityForResult(intent, 3003);
        }
    }

    private static void closeSplitContent(Activity activity) {
        if (activity != null && (activity instanceof FingerprintSettingsActivity)) {
            HwCustSplitUtils splitter = (HwCustSplitUtils) HwCustUtils.createObj(HwCustSplitUtils.class, new Object[]{activity});
            if (splitter != null && splitter.reachSplitSize()) {
                splitter.hideAllContent();
            }
        }
    }
}
