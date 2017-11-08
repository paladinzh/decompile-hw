package com.android.gallery3d.settings;

import android.app.Activity;
import android.content.Context;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.preference.PreferenceManager;
import com.android.gallery3d.app.HicloudAccountCallbacks;
import com.android.gallery3d.common.Utils;
import com.android.gallery3d.util.FileUtils;
import com.android.gallery3d.util.GalleryLog;
import com.android.gallery3d.util.GalleryUtils;
import com.huawei.cloudservice.CloudAccount;
import com.huawei.cloudservice.CloudRequestHandler;
import com.huawei.cloudservice.LoginHandler;
import com.huawei.hwid.core.datatype.UserInfo;
import com.huawei.hwid.core.helper.handler.ErrorStatus;
import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class HicloudAccountManager {
    public static final int CHANNEL = 16000001;
    public static final String HEAD_PORTRAIT_NAME = "head_portrait.data";
    private static final int INVALID_INDEX = -1;
    public static final String PACKAGE_NAME = "com.android.gallery3d";
    private static final int QUERY_FLAG_COUNTS = 4;
    public static final int QUERY_FLAG_DEVICE_INFO = 2;
    public static final int QUERY_FLAG_USER_ACCOUNT_INFO = 1;
    public static final int QUERY_FLAG_USER_BASE_INFO = 8;
    public static final int QUERY_FLAG_USER_LOGIN_INFO = 4;
    private static final String TAG = "HicloudAccountManager";
    private CloudAccount mCloudAccount;
    private CloudLoginHandler mCloudLoginHandler = new CloudLoginHandler() {
        public void onLogin(CloudAccount[] cloudAccounts, int i) {
            super.onLogin(cloudAccounts, i);
            HicloudAccountManager.this.mCloudAccount = cloudAccounts[i];
            HicloudAccountManager.this.mHeadPortrait = HicloudAccountManager.getHeadPortrait(HicloudAccountManager.this.mContext, null);
            HicloudAccountManager.this.queryUserInfo();
        }

        public void onLogout(CloudAccount[] cloudAccounts, int i) {
            super.onLogout(cloudAccounts, i);
            HicloudAccountManager.this.clearAccount();
            HicloudAccountManager.this.onHicloudAccountLogout();
        }

        public void onError(ErrorStatus errorStatus) {
            super.onError(errorStatus);
            HicloudAccountManager.this.clearAccount();
            HicloudAccountManager.this.onHicloudAccountLogout();
        }
    };
    private CloudRequestHandler mCloudRequestHandler = new CloudRequestHandler() {
        public void onFinish(Bundle bundle) {
            if (bundle != null) {
                HicloudAccountManager.this.mUserInfo = (UserInfo) bundle.getParcelable("userInfo");
                if (HicloudAccountManager.this.mUserInfo != null) {
                    final String uri = HicloudAccountManager.this.mUserInfo.getHeadPictureURL();
                    if (uri == null || uri.isEmpty()) {
                        GalleryLog.d(HicloudAccountManager.TAG, "head portrait URL is null");
                        HicloudAccountManager.this.deleteHeadPortrait();
                        HicloudAccountManager.this.mHeadPortrait = null;
                        HicloudAccountManager.this.onHicloudAccountUserInfoChanged();
                        return;
                    }
                    new Thread(new Runnable() {
                        public void run() {
                            HicloudAccountManager.this.mHeadPortrait = HicloudAccountManager.getHeadPortrait(HicloudAccountManager.this.mContext, uri);
                            HicloudAccountManager.this.onHicloudAccountUserInfoChanged();
                        }
                    }).start();
                    HicloudAccountManager.this.onHicloudAccountUserInfoChanged();
                } else {
                    return;
                }
            }
            GalleryLog.d(HicloudAccountManager.TAG, "CloudRequest bundle is null");
        }

        public void onError(ErrorStatus errorStatus) {
            if (errorStatus != null) {
                GalleryLog.d(HicloudAccountManager.TAG, "error code " + errorStatus.getErrorCode());
                GalleryLog.d(HicloudAccountManager.TAG, "error reason " + errorStatus.getErrorReason());
            }
        }
    };
    private Context mContext;
    private Handler mHandler;
    private Bitmap mHeadPortrait;
    private ArrayList<HicloudAccountCallbacks> mHicloudAccountCallbacks = new ArrayList();
    private HicloudAccountReceiver mHicloudAccountReceiver;
    private IntentFilter mReceiverFilter;
    private UserInfo mUserInfo;

    public static class CloudLoginHandler implements LoginHandler {
        public void onLogin(CloudAccount[] cloudAccounts, int i) {
            if (cloudAccounts == null || i == -1 || cloudAccounts.length <= i) {
                GalleryLog.e(HicloudAccountManager.TAG, "get invalid  params");
            }
        }

        public void onLogout(CloudAccount[] cloudAccounts, int i) {
            if (cloudAccounts == null || i == -1 || cloudAccounts.length <= i) {
                GalleryLog.e(HicloudAccountManager.TAG, "get invalid  params");
            }
        }

        public void onFinish(CloudAccount[] cloudAccounts) {
        }

        public void onError(ErrorStatus errorStatus) {
            if (errorStatus != null) {
                GalleryLog.d(HicloudAccountManager.TAG, "error code " + errorStatus.getErrorCode());
                GalleryLog.d(HicloudAccountManager.TAG, "error reason " + errorStatus.getErrorReason());
            }
        }
    }

    public static android.graphics.Bitmap getHeadPortrait(android.content.Context r11, java.lang.String r12) {
        /* JADX: method processing error */
/*
Error: jadx.core.utils.exceptions.JadxRuntimeException: Can't find block by offset: 0x0070 in list []
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
        r9 = 0;
        r8 = getHeadPortraitPath(r11);
        r7 = 0;
        r0 = 0;
        if (r12 != 0) goto L_0x001d;
    L_0x0009:
        r0 = android.graphics.BitmapFactory.decodeFile(r8);
        r1 = com.android.gallery3d.util.ImageUtils.getCircleImage(r0);
        if (r0 == r1) goto L_0x0019;
    L_0x0013:
        if (r1 == 0) goto L_0x0019;
    L_0x0015:
        r0.recycle();
        return r1;
    L_0x0019:
        com.android.gallery3d.common.Utils.closeSilently(r7);
        return r9;
    L_0x001d:
        r4 = new org.apache.http.client.methods.HttpGet;	 Catch:{ IOException -> 0x0056, all -> 0x0074 }
        r4.<init>(r12);	 Catch:{ IOException -> 0x0056, all -> 0x0074 }
        r6 = new org.apache.http.impl.client.DefaultHttpClient;	 Catch:{ IOException -> 0x0056, all -> 0x0074 }
        r6.<init>();	 Catch:{ IOException -> 0x0056, all -> 0x0074 }
        r5 = r6.execute(r4);	 Catch:{ IOException -> 0x0056, all -> 0x0074 }
        r9 = r5.getStatusLine();	 Catch:{ IOException -> 0x0056, all -> 0x0074 }
        r9 = r9.getStatusCode();	 Catch:{ IOException -> 0x0056, all -> 0x0074 }
        r10 = 200; // 0xc8 float:2.8E-43 double:9.9E-322;	 Catch:{ IOException -> 0x0056, all -> 0x0074 }
        if (r9 != r10) goto L_0x0042;	 Catch:{ IOException -> 0x0056, all -> 0x0074 }
    L_0x0037:
        r3 = r5.getEntity();	 Catch:{ IOException -> 0x0056, all -> 0x0074 }
        r7 = r3.getContent();	 Catch:{ IOException -> 0x0056, all -> 0x0074 }
        saveImage(r8, r7);	 Catch:{ IOException -> 0x0056, all -> 0x0074 }
    L_0x0042:
        r0 = android.graphics.BitmapFactory.decodeFile(r8);
        r1 = com.android.gallery3d.util.ImageUtils.getCircleImage(r0);
        if (r0 == r1) goto L_0x0052;
    L_0x004c:
        if (r1 == 0) goto L_0x0052;
    L_0x004e:
        r0.recycle();
        return r1;
    L_0x0052:
        com.android.gallery3d.common.Utils.closeSilently(r7);
    L_0x0055:
        return r0;
    L_0x0056:
        r2 = move-exception;
        r9 = "HicloudAccountManager";	 Catch:{ IOException -> 0x0056, all -> 0x0074 }
        r10 = "get head portrait fail";	 Catch:{ IOException -> 0x0056, all -> 0x0074 }
        com.android.gallery3d.util.GalleryLog.d(r9, r10);	 Catch:{ IOException -> 0x0056, all -> 0x0074 }
        r0 = android.graphics.BitmapFactory.decodeFile(r8);
        r1 = com.android.gallery3d.util.ImageUtils.getCircleImage(r0);
        if (r0 == r1) goto L_0x0070;
    L_0x006a:
        if (r1 == 0) goto L_0x0070;
    L_0x006c:
        r0.recycle();
        return r1;
    L_0x0070:
        com.android.gallery3d.common.Utils.closeSilently(r7);
        goto L_0x0055;
    L_0x0074:
        r9 = move-exception;
        r0 = android.graphics.BitmapFactory.decodeFile(r8);
        r1 = com.android.gallery3d.util.ImageUtils.getCircleImage(r0);
        if (r0 == r1) goto L_0x0085;
    L_0x007f:
        if (r1 == 0) goto L_0x0085;
    L_0x0081:
        r0.recycle();
        return r1;
    L_0x0085:
        com.android.gallery3d.common.Utils.closeSilently(r7);
        throw r9;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.gallery3d.settings.HicloudAccountManager.getHeadPortrait(android.content.Context, java.lang.String):android.graphics.Bitmap");
    }

    public HicloudAccountManager(Context context) {
        this.mContext = context;
        this.mHandler = new Handler(Looper.getMainLooper()) {
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case HicloudAccountReceiver.MSG_HICLOUD_ACCOUNT_LOGOUT /*65536*/:
                        HicloudAccountManager.this.clearAccount();
                        HicloudAccountManager.this.onHicloudAccountLogout();
                        return;
                    case HicloudAccountReceiver.MSG_HICLOUD_ACCOUNT_USER_INFO_CHANGED /*65537*/:
                        HicloudAccountManager.this.queryUserInfo();
                        return;
                    case HicloudAccountReceiver.MSG_HICLOUD_ACCOUNT_CHANGED /*65538*/:
                        HicloudAccountManager.this.onHicloudAccountChanged();
                        return;
                    default:
                        super.handleMessage(msg);
                        return;
                }
            }
        };
        this.mHicloudAccountReceiver = new HicloudAccountReceiver(this.mHandler);
        this.mReceiverFilter = new IntentFilter();
        this.mReceiverFilter.addAction(HicloudAccountReceiver.ACTION_HICLOUD_ACCOUNT_LOGOUT);
        this.mReceiverFilter.addAction(HicloudAccountReceiver.ACTION_HICLOUD_ACCOUNT_USER_INFO_CHANGE);
        this.mReceiverFilter.addAction(HicloudAccountReceiver.ACTION_HICLOUD_ACCOUNT_CHANGE);
    }

    public void registerReceiver() {
        this.mContext.registerReceiver(this.mHicloudAccountReceiver, this.mReceiverFilter);
    }

    public void unregisterReceiver() {
        this.mContext.unregisterReceiver(this.mHicloudAccountReceiver);
    }

    public static void getCloudAccount(Context context, CloudLoginHandler loginHandler) {
        if (context != null) {
            Bundle bundle = new Bundle();
            bundle.putInt("loginChannel", CHANNEL);
            bundle.putBoolean("needAuth", false);
            bundle.putBoolean("AIDL", true);
            CloudAccount.getAccountsByType(context, PACKAGE_NAME, bundle, loginHandler);
        }
    }

    public void queryUserInfo() {
        if (this.mCloudAccount == null) {
            GalleryLog.e(TAG, "get cloud account first before get user info");
        } else {
            getUserInfo(this.mContext, this.mCloudAccount, 8, this.mCloudRequestHandler);
        }
    }

    public static void getUserInfo(Context context, CloudAccount account, int queryFlag, CloudRequestHandler requestHandler) {
        if (account != null && context != null) {
            account.getUserInfo(context, intToBinaryString(queryFlag, 4), requestHandler);
        }
    }

    public static String getHeadPortraitPath(Context context) {
        return context == null ? null : context.getFilesDir() + File.separator + HEAD_PORTRAIT_NAME;
    }

    public void registerHicloudAccountCallbacks(HicloudAccountCallbacks callback) {
        synchronized (this.mHicloudAccountCallbacks) {
            if (!this.mHicloudAccountCallbacks.contains(callback)) {
                this.mHicloudAccountCallbacks.add(callback);
            }
        }
    }

    public void unregisterHicloudAccountCallbacks(HicloudAccountCallbacks callback) {
        synchronized (this.mHicloudAccountCallbacks) {
            this.mHicloudAccountCallbacks.remove(callback);
        }
    }

    public void queryHicloudAccount(Activity activity, boolean force) {
        synchronized (this) {
            boolean allowNetWork = PreferenceManager.getDefaultSharedPreferences(this.mContext).getBoolean(GallerySettings.KEY_USE_NETWORK, false) || !GalleryUtils.IS_CHINESE_VERSION;
            if (allowNetWork && ((this.mCloudAccount == null || force) && CloudAccount.hasLoginAccount(this.mContext))) {
                getCloudAccount(this.mContext, this.mCloudLoginHandler);
            }
        }
    }

    public CloudAccount getHicloudAccount() {
        return this.mCloudAccount;
    }

    public Bitmap getHeadPortrait() {
        return this.mHeadPortrait;
    }

    public UserInfo getUserInfo() {
        return this.mUserInfo;
    }

    private void onHicloudAccountLogout() {
        synchronized (this.mHicloudAccountCallbacks) {
            for (HicloudAccountCallbacks callback : this.mHicloudAccountCallbacks) {
                callback.onAccountLogout();
            }
        }
    }

    private void onHicloudAccountUserInfoChanged() {
        synchronized (this.mHicloudAccountCallbacks) {
            for (HicloudAccountCallbacks callback : this.mHicloudAccountCallbacks) {
                callback.onAccountUserInfoChanged();
            }
        }
    }

    private void onHicloudAccountChanged() {
        synchronized (this.mHicloudAccountCallbacks) {
            for (HicloudAccountCallbacks callback : this.mHicloudAccountCallbacks) {
                callback.onAccountChanged();
            }
        }
    }

    private static void saveImage(String path, InputStream inStream) {
        Throwable th;
        File outFile = new File(path);
        byte[] buffer = new byte[1024];
        Closeable closeable = null;
        if (inStream == null) {
            GalleryLog.e(TAG, "no data to save");
            return;
        }
        try {
            Closeable outStream = new FileOutputStream(outFile);
            try {
                FileUtils.readDataUtilEndToWrite(inStream, outStream, buffer);
                Utils.closeSilently(outStream);
                closeable = outStream;
            } catch (FileNotFoundException e) {
                closeable = outStream;
                GalleryLog.e(TAG, "cannot find file");
                Utils.closeSilently(closeable);
            } catch (IOException e2) {
                closeable = outStream;
                try {
                    GalleryLog.e(TAG, "write file exception");
                    Utils.closeSilently(closeable);
                } catch (Throwable th2) {
                    th = th2;
                    Utils.closeSilently(closeable);
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                closeable = outStream;
                Utils.closeSilently(closeable);
                throw th;
            }
        } catch (FileNotFoundException e3) {
            GalleryLog.e(TAG, "cannot find file");
            Utils.closeSilently(closeable);
        } catch (IOException e4) {
            GalleryLog.e(TAG, "write file exception");
            Utils.closeSilently(closeable);
        }
    }

    private static String intToBinaryString(int i, int stringWidth) {
        char[] buf = new char[stringWidth];
        int cursor = stringWidth;
        do {
            cursor--;
            buf[cursor] = (i & 1) == 0 ? '0' : '1';
            i >>>= 1;
        } while (cursor > 0);
        return new String(buf);
    }

    private void deleteHeadPortrait() {
        if (isHeadPortraitExists() && !new File(getHeadPortraitPath(this.mContext)).delete()) {
            GalleryLog.e(TAG, "delete head portrait fail");
        }
    }

    private boolean isHeadPortraitExists() {
        String path = getHeadPortraitPath(this.mContext);
        if (path == null) {
            return false;
        }
        return new File(path).exists();
    }

    private void clearAccount() {
        this.mCloudAccount = null;
        this.mHeadPortrait = null;
        this.mUserInfo = null;
        deleteHeadPortrait();
    }
}
