package com.fyusion.sdk.ext.shareinterface;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import com.fyusion.sdk.common.FyuseSDK;
import com.fyusion.sdk.common.FyuseSDKException;
import com.fyusion.sdk.common.ext.util.FyuseUtils;
import com.fyusion.sdk.common.ext.util.FyuseUtils.FyuseContainerVersion;
import com.fyusion.sdk.common.internal.analytics.Fyulytics;
import com.fyusion.sdk.share.ActivityCallback;
import com.fyusion.sdk.share.CallbackManager;
import com.fyusion.sdk.share.CallbackManagerImplementation;
import com.fyusion.sdk.share.FyuseShare;
import com.fyusion.sdk.share.exception.NoInternetPermissionSatisfiedException;
import com.fyusion.sdk.share.exception.NoNetworkConnectionException;
import java.io.File;

/* compiled from: Unknown */
public class FyuseShareInterface {
    private Uri a;
    private Context b;
    private boolean c = false;
    private boolean d = false;
    private boolean e = false;

    static File a(Context context, Uri uri) {
        Throwable th;
        Cursor cursor = null;
        if ("file".equals(uri.getScheme())) {
            return new File(uri.getPath());
        }
        Cursor query;
        try {
            query = context.getContentResolver().query(uri, new String[]{"_data"}, null, null, null);
            if (query != null) {
                try {
                    if (query.moveToFirst()) {
                        File file = new File(query.getString(0));
                        if (file.exists()) {
                            if (query != null) {
                                query.close();
                            }
                            return file;
                        }
                    }
                } catch (Exception e) {
                    if (query != null) {
                        query.close();
                    }
                    return new File(uri.getPath());
                } catch (Throwable th2) {
                    cursor = query;
                    th = th2;
                    if (cursor != null) {
                        cursor.close();
                    }
                    throw th;
                }
            }
            if (query != null) {
                query.close();
            }
        } catch (Exception e2) {
            query = null;
            if (query != null) {
                query.close();
            }
            return new File(uri.getPath());
        } catch (Throwable th3) {
            th = th3;
            if (cursor != null) {
                cursor.close();
            }
            throw th;
        }
        return new File(uri.getPath());
    }

    static boolean a(File file) {
        boolean z = false;
        if (file.isDirectory()) {
            String str = "fyuse.fyu";
            if (new File(file, "fyuse.fyu").exists()) {
                return true;
            }
            str = "fyuse_raw.mp4";
            return new File(file, "fyuse_raw.mp4").exists();
        }
        if (FyuseUtils.isFyuseContainerVersion(file, FyuseContainerVersion.VERSION_3) || FyuseUtils.isFyuseContainerVersion(file, FyuseContainerVersion.VERSION_1)) {
            z = true;
        }
        return z;
    }

    private static boolean b(int i, Intent intent, ShareInterfaceListener shareInterfaceListener) {
        FyuseShare.handleIncomingIntent(intent);
        Fyulytics.sharedInstance().flushEvents();
        if (shareInterfaceListener != null) {
            switch (i) {
                case -1:
                    if (!(intent == null || intent.getExtras() == null)) {
                        shareInterfaceListener.onSuccess("https://fyu.se/v/" + intent.getStringExtra("fyuseID"));
                        break;
                    }
                case 0:
                    shareInterfaceListener.onUserCancel();
                    break;
                case 1:
                    if (intent != null && intent.getExtras() != null && intent.hasExtra("exception")) {
                        shareInterfaceListener.onError(new FyuseSDKException(intent.getExtras().getString("exception")));
                        break;
                    }
                    shareInterfaceListener.onError(new FyuseSDKException("FyuseShareInterface error"));
                    break;
                    break;
                default:
                    return false;
            }
        }
        return true;
    }

    public static FyuseShareInterface build() {
        return new FyuseShareInterface();
    }

    public FyuseShareInterface dismissWindowAfterUpload() {
        this.c = true;
        return this;
    }

    public void displayInterface() throws FyuseSDKException {
        int i = 0;
        if (this.a == null) {
            throw new FyuseSDKException("Uri can't be null");
        } else if (this.b == null) {
            throw new FyuseSDKException("Context can't be null");
        } else if (this.b instanceof Activity) {
            Intent intent = new Intent();
            intent.setAction("com.fyusion.fyuse.SHARE");
            intent.addFlags(3);
            intent.setDataAndType(this.a, "fyuse/fyuse");
            intent.addCategory("android.intent.category.DEFAULT");
            String str = "com.fyusion.fyuse";
            intent.setComponent(new ComponentName("com.fyusion.fyuse", "com.fyusion.fyuse.activities.ShareInterfaceActivity"));
            PackageManager packageManager = this.b.getPackageManager();
            if (packageManager.queryIntentActivities(intent, 0).size() > 0) {
                try {
                    if (packageManager.getPackageInfo("com.fyusion.fyuse", 0).versionCode >= FyuseShare.FYUSE_APP_MIN_SUPPORTED_VERSIONCODE_FSI) {
                        if (this.c) {
                            intent.putExtra("BUNDLE_DISMISS", true);
                        }
                        if (this.d) {
                            intent.putExtra("BUNDLE_ENABLE_SHARE", true);
                        }
                        if (this.e) {
                            intent.putExtra("BUNDLE_ENABLE_LOCATION", true);
                        }
                        FyuseShare.startShareInterface(this.b, intent, 6199);
                        i = 1;
                    }
                } catch (Exception e) {
                }
            }
            if (i == 0) {
                intent = new Intent(this.b, FyuseShareActivity.class);
                if (this.c) {
                    intent.putExtra("BUNDLE_DISMISS", true);
                }
                if (this.d) {
                    intent.putExtra("BUNDLE_ENABLE_SHARE", true);
                }
                if (this.e) {
                    intent.putExtra("BUNDLE_ENABLE_LOCATION", true);
                }
                intent.setDataAndType(this.a, "fyuse/fyuse");
                try {
                    ((Activity) this.b).startActivityForResult(intent, 6199);
                } catch (ActivityNotFoundException e2) {
                    throw new FyuseSDKException(e2.getMessage());
                }
            }
        } else {
            throw new FyuseSDKException("Illegal Context");
        }
    }

    public FyuseShareInterface registerCallback(CallbackManager callbackManager, final ShareInterfaceListener shareInterfaceListener) {
        if (callbackManager == null || shareInterfaceListener == null) {
            return this;
        }
        ((CallbackManagerImplementation) callbackManager).registerCallback(6199, new ActivityCallback(this) {
            final /* synthetic */ FyuseShareInterface b;

            public boolean onActivityResult(int i, Intent intent) {
                return FyuseShareInterface.b(i, intent, shareInterfaceListener);
            }
        });
        return this;
    }

    public FyuseShareInterface setUri(Uri uri) throws FyuseSDKException {
        this.a = uri;
        if (a(a(this.b, uri))) {
            return this;
        }
        throw new FyuseSDKException("Not a known fyuse file or unsupported.");
    }

    public FyuseShareInterface showLocationPicker() {
        this.e = true;
        return this;
    }

    public FyuseShareInterface showSocialNetworks() {
        this.d = true;
        return this;
    }

    public FyuseShareInterface with(Context context) throws FyuseSDKException {
        if (FyuseSDK.getContext() == null || FyuseSDK.getContext().isRestricted()) {
            throw new FyuseSDKException("Context is null");
        }
        try {
            Object obj;
            NetworkInfo activeNetworkInfo = ((ConnectivityManager) FyuseSDK.getContext().getSystemService("connectivity")).getActiveNetworkInfo();
            if (activeNetworkInfo != null) {
                if (activeNetworkInfo.isConnected()) {
                    obj = 1;
                    if (obj == null) {
                        this.b = context;
                        return this;
                    }
                    throw new NoNetworkConnectionException();
                }
            }
            obj = null;
            if (obj == null) {
                throw new NoNetworkConnectionException();
            }
            this.b = context;
            return this;
        } catch (SecurityException e) {
            throw new NoInternetPermissionSatisfiedException(e.getMessage());
        }
    }
}
