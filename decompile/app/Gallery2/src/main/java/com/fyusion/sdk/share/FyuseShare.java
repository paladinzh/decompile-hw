package com.fyusion.sdk.share;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import com.fyusion.sdk.common.AuthenticationException;
import com.fyusion.sdk.common.FyuseSDK;
import com.fyusion.sdk.common.FyuseSDKException;
import com.fyusion.sdk.common.ext.FyuseProcessorParameters.Builder;
import com.fyusion.sdk.common.ext.Key;
import com.fyusion.sdk.common.ext.ProcessError;
import com.fyusion.sdk.common.ext.ProcessItem;
import com.fyusion.sdk.common.ext.ProcessItem.ProcessState;
import com.fyusion.sdk.common.ext.ProcessorListener;
import com.fyusion.sdk.common.ext.j;
import com.fyusion.sdk.common.ext.k;
import com.fyusion.sdk.common.ext.util.FyuseUtils;
import com.fyusion.sdk.common.ext.util.FyuseUtils.FyuseContainerVersion;
import com.fyusion.sdk.processor.FyuseProcessor;
import com.fyusion.sdk.processor.f;
import com.fyusion.sdk.share.a.a;
import com.fyusion.sdk.share.exception.CorruptFyuseException;
import com.fyusion.sdk.share.exception.FyuseAlreadyUploadingException;
import com.fyusion.sdk.share.exception.FyuseShareException;
import com.fyusion.sdk.share.exception.NoInternetPermissionSatisfiedException;
import com.fyusion.sdk.share.exception.NoNetworkConnectionException;
import com.fyusion.sdk.share.exception.ServerException;
import com.fyusion.sdk.share.exception.UriNotFoundException;
import java.io.File;
import java.util.concurrent.RejectedExecutionException;

/* compiled from: Unknown */
public class FyuseShare {
    public static final int FYUSE_APP_MIN_SUPPORTED_VERSIONCODE_FSI = 3921;
    public static final int RETURN_CODE_NOT_FOUND = 0;
    public static final int RETURN_CODE_SUCCESS = 1;
    private static final String a = FyuseShare.class.getSimpleName();
    private File b;
    private Uri c;
    private String d;
    private String e;
    private boolean f = true;
    private ShareListener g;
    private i h;
    private Context i;
    private int j = 0;
    private int k = 0;
    private boolean l = false;
    private FyuseShareParameters m;

    private h a() {
        final h hVar = new h(FyuseSDK.getContext(), this.b, this.f, this.d, this.e);
        try {
            f anonymousClass3 = new f(this) {
                final /* synthetic */ FyuseShare b;
                private String c;

                public void a(int i) {
                    a.a(this.b.b.getName(), this.c, hVar.a(), hVar.c(), hVar.b(), String.valueOf(i), null);
                    b.a().c(this.b.c);
                }

                public void a(Exception exception) {
                    if (this.b.g != null) {
                        this.b.g.onError(exception);
                    }
                    FyuseShare.b(this.b.c);
                }

                public void a(String str, Bitmap bitmap) {
                    if (this.b.g != null) {
                        this.b.g.onSuccess(str, bitmap);
                    }
                }

                public void a(String str, String str2) {
                    this.c = str;
                }

                public void b(int i) {
                    if (this.b.k != i) {
                        this.b.k = i;
                        this.b.b();
                    }
                }

                public void b(String str, String str2) {
                    this.c = str;
                    if (this.b.g != null) {
                        this.b.g.onSuccess(str);
                    }
                    if (this.b.g != null) {
                        this.b.g.onSuccess(str, str2 + j.ad);
                    }
                }
            };
            a.a(this.b.getName());
            hVar.a(anonymousClass3);
            hVar.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, new Void[0]);
        } catch (RejectedExecutionException e) {
            e.printStackTrace();
            if (this.g != null) {
                this.g.onError(new ServerException("Sharing rejected"));
            }
        }
        return hVar;
    }

    private static boolean a(File file) {
        boolean z = false;
        if (file.isDirectory()) {
            return !new File(file, j.ak).exists() ? new File(file, j.ag).exists() : true;
        } else {
            if (FyuseUtils.isFyuseContainerVersion(file, FyuseContainerVersion.VERSION_3) || FyuseUtils.isFyuseContainerVersion(file, FyuseContainerVersion.VERSION_1)) {
                z = true;
            }
            return z;
        }
    }

    private static int b(Uri uri) {
        return uri != null ? b.a().a(uri, false) : 0;
    }

    private void b() {
        if (this.g != null) {
            this.g.onProgress(this.k + this.j);
        }
    }

    private static boolean b(int i, Intent intent) {
        if (i == -1 && intent.getExtras() != null && intent.hasExtra("state")) {
            if (!intent.getExtras().getBoolean("state")) {
                com.fyusion.sdk.common.a.a().n();
            } else if (intent.hasExtra("token")) {
                com.fyusion.sdk.common.a.a().g(intent.getExtras().getString("token"));
            }
        }
        return true;
    }

    public static int cancelShare(Uri uri) {
        return uri != null ? b.a().a(uri, true) : 0;
    }

    public static boolean handleIncomingIntent(Intent intent) {
        if (intent == null || intent.getExtras() == null || !intent.hasExtra("token")) {
            return false;
        }
        com.fyusion.sdk.common.a.a().g(intent.getExtras().getString("token"));
        return true;
    }

    public static FyuseShare init() throws FyuseShareException, AuthenticationException {
        if (FyuseSDK.getContext() == null || FyuseSDK.getContext().isRestricted()) {
            throw new FyuseShareException("Context is null");
        }
        try {
            Object obj;
            NetworkInfo activeNetworkInfo = ((ConnectivityManager) FyuseSDK.getContext().getSystemService("connectivity")).getActiveNetworkInfo();
            if (activeNetworkInfo != null) {
                if (activeNetworkInfo.isConnected()) {
                    obj = 1;
                    if (obj == null) {
                        com.fyusion.sdk.common.a.a().d();
                        if (com.fyusion.sdk.common.a.a().f("share")) {
                            if (com.fyusion.sdk.common.a.a().m() == null) {
                                if (!com.fyusion.sdk.common.a.a().b()) {
                                    throw new AuthenticationException("FyuseSDK not properly initialized");
                                } else if (com.fyusion.sdk.common.a.a().c()) {
                                    com.fyusion.sdk.common.a.a().f();
                                }
                            }
                            return new FyuseShare();
                        }
                        throw new AuthenticationException("share component is disabled.");
                    }
                    throw new NoNetworkConnectionException();
                }
            }
            obj = null;
            if (obj == null) {
                throw new NoNetworkConnectionException();
            }
            com.fyusion.sdk.common.a.a().d();
            if (com.fyusion.sdk.common.a.a().f("share")) {
                throw new AuthenticationException("share component is disabled.");
            }
            if (com.fyusion.sdk.common.a.a().m() == null) {
                if (!com.fyusion.sdk.common.a.a().b()) {
                    throw new AuthenticationException("FyuseSDK not properly initialized");
                } else if (com.fyusion.sdk.common.a.a().c()) {
                    com.fyusion.sdk.common.a.a().f();
                }
            }
            return new FyuseShare();
        } catch (SecurityException e) {
            throw new NoInternetPermissionSatisfiedException(e.getMessage());
        }
    }

    public static boolean isUploading(Uri uri) {
        return uri != null && b.a().b(uri);
    }

    public static boolean startShareInterface(Context context, Intent intent, int i) {
        if (intent == null || context == null) {
            return false;
        }
        intent.setComponent(new ComponentName("com.fyusion.fyuse", "com.fyusion.fyuse.activities.ShareInterfaceActivity"));
        intent.putExtra("access_token", com.fyusion.sdk.common.a.a().m());
        ((Activity) context).startActivityForResult(intent, i);
        return true;
    }

    public FyuseShare makePrivate() {
        this.f = false;
        return this;
    }

    public FyuseShare parameters(FyuseShareParameters fyuseShareParameters) {
        this.m = fyuseShareParameters;
        return this;
    }

    public FyuseShare registerCallback(Context context, CallbackManager callbackManager) throws FyuseSDKException {
        if (callbackManager == null) {
            throw new FyuseSDKException("CallbackManager can't be null");
        } else if (context == null) {
            throw new FyuseSDKException("Context can't be null");
        } else if (context instanceof Activity) {
            this.i = context;
            ((CallbackManagerImplementation) callbackManager).registerCallback(619, new ActivityCallback(this) {
                final /* synthetic */ FyuseShare a;

                {
                    this.a = r1;
                }

                public boolean onActivityResult(int i, Intent intent) {
                    return FyuseShare.b(i, intent);
                }
            });
            return this;
        } else {
            throw new FyuseSDKException("Illegal Context");
        }
    }

    public FyuseShare setShareListener(ShareListener shareListener) {
        this.g = shareListener;
        return this;
    }

    public FyuseShare share(Uri uri) throws FyuseSDKException {
        if (uri != null) {
            this.b = new File(uri.getPath());
            if (!this.b.exists()) {
                throw new UriNotFoundException();
            } else if (a(this.b)) {
                this.c = uri;
                return this;
            } else {
                throw new FyuseSDKException("Not a known fyuse file or unsupported.");
            }
        }
        throw new UriNotFoundException();
    }

    public void start() throws AuthenticationException {
        com.fyusion.sdk.common.a.a().d();
        if (com.fyusion.sdk.common.a.a().f("share")) {
            cancelShare(this.c);
            throw new AuthenticationException("share component is disabled.");
        }
        if (this.i != null) {
            Intent intent = new Intent();
            intent.setAction("com.fyusion.fyuse.SHARE");
            intent.setDataAndType(null, "fyuse/poll");
            intent.addCategory("android.intent.category.DEFAULT");
            String str = "com.fyusion.fyuse";
            intent.setComponent(new ComponentName("com.fyusion.fyuse", "com.fyusion.fyuse.activities.ShareInterfaceActivity"));
            if (FyuseSDK.getContext().getPackageManager().queryIntentActivities(intent, 0).size() > 0) {
                try {
                    if (this.i.getPackageManager().getPackageInfo("com.fyusion.fyuse", 0).versionCode >= FYUSE_APP_MIN_SUPPORTED_VERSIONCODE_FSI) {
                        intent.putExtra("access_token", com.fyusion.sdk.common.a.a().m());
                        ((Activity) this.i).startActivityForResult(intent, 619);
                    }
                } catch (Exception e) {
                }
            }
        }
        this.h = new i(this.c);
        int a = b.a().a(this.h);
        if (a == 0) {
            if (this.g != null) {
                this.g.onError(new FyuseAlreadyUploadingException());
            }
        } else if (a != -1) {
            this.h.a(a());
            this.h.b().e = true;
            f instance = FyuseProcessor.getInstance();
            ProcessorListener anonymousClass2 = new k(this) {
                final /* synthetic */ FyuseShare a;

                {
                    this.a = r1;
                }

                /* JADX WARNING: inconsistent code. */
                /* Code decompiled incorrectly, please refer to instructions dump. */
                public void a(ProcessItem processItem, int i, int i2, Bitmap bitmap, Matrix matrix) {
                    if (processItem.getState() != ProcessState.CANCELLED && !processItem.isCancelled() && i2 >= 1) {
                        int i3;
                        if (processItem.getState() != ProcessState.INITIAL) {
                            i3 = !this.a.l ? (i * 45) / i2 : ((i2 + i) * 45) / (i2 * 2);
                        } else {
                            this.a.l = true;
                            i3 = (i * 45) / (i2 * 2);
                        }
                        if (!(this.a.j == i3 || processItem.isCancelled())) {
                            this.a.j = i3;
                            this.a.b();
                        }
                    }
                }

                public void onError(ProcessItem processItem, ProcessError processError) {
                    if (!processItem.isCancelled()) {
                        FyuseShare.b(this.a.c);
                        if (this.a.g != null) {
                            this.a.g.onError(new CorruptFyuseException("Fyuse can't process"));
                        }
                    }
                }

                public void onImageDataReady(ProcessItem processItem) {
                }

                public void onMetadataReady(ProcessItem processItem, int i) {
                    if (!((processItem.getState() != ProcessState.READY_FOR_VIEW && processItem.getState() != ProcessState.READY_FOR_UPLOAD) || this.a.h.b() == null || processItem.isCancelled())) {
                        this.a.h.b().a(i);
                        this.a.h.b().c = true;
                    }
                }

                public void onProcessComplete(ProcessItem processItem) {
                    if (processItem.getState() == ProcessState.READY_FOR_UPLOAD) {
                        this.a.j = 45;
                        if (!(this.a.h.b() == null || processItem.isCancelled())) {
                            this.a.h.b().c = true;
                            this.a.h.b().d = true;
                        }
                    }
                }

                public void onProgress(ProcessItem processItem, int i, int i2, Bitmap bitmap) {
                    a(processItem, i, i2, bitmap, null);
                }

                public void onSliceFound(ProcessItem processItem, int i) {
                    if (!((processItem.getState() != ProcessState.READY_FOR_VIEW && processItem.getState() != ProcessState.READY_FOR_UPLOAD) || this.a.h.b() == null || processItem.isCancelled())) {
                        this.a.h.b().d = true;
                        this.a.h.b().a(i, false);
                    }
                }

                public void onSliceReady(ProcessItem processItem, int i) {
                    if (!((processItem.getState() != ProcessState.READY_FOR_VIEW && processItem.getState() != ProcessState.READY_FOR_UPLOAD) || this.a.h.b() == null || processItem.isCancelled())) {
                        this.a.h.b().d = true;
                        this.a.h.b().a(i, true);
                    }
                }

                public void onTweensReady(ProcessItem processItem) {
                }
            };
            if (this.m != null) {
                Builder builder = new Builder();
                FyuseShareCapabilities instance2 = FyuseShareCapabilities.getInstance();
                for (Key key : FyuseShareCapabilities.getInstance().getKeys()) {
                    if (this.m.get(key) == null) {
                        builder.set(key, instance2.get(key));
                    } else {
                        builder.set(key, this.m.get(key));
                    }
                }
                ((FyuseProcessor) instance).setFyuseProcessorParameters(builder.build());
            }
            ProcessItem prepareForUpload = instance.prepareForUpload(this.b, anonymousClass2);
            if (!(this.h == null || prepareForUpload == null)) {
                this.h.a(prepareForUpload);
            }
        } else {
            if (this.g != null) {
                this.g.onError(new CorruptFyuseException("Fyuse not found"));
            }
        }
    }

    public FyuseShare withAddress(String str) {
        this.e = str;
        return this;
    }

    public FyuseShare withDescription(String str) {
        this.d = str;
        return this;
    }
}
