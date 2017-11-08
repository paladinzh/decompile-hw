package android.content.pm.permission;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ApplicationInfo;
import android.content.pm.permission.IRuntimePermissionPresenter.Stub;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteCallback;
import android.os.RemoteCallback.OnResultListener;
import android.os.RemoteException;
import android.permissionpresenterservice.RuntimePermissionPresenterService;
import android.util.Log;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.os.SomeArgs;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class RuntimePermissionPresenter {
    public static final String KEY_RESULT = "android.content.pm.permission.RuntimePermissionPresenter.key.result";
    private static final String TAG = "RuntimePermPresenter";
    @GuardedBy("sLock")
    private static RuntimePermissionPresenter sInstance;
    private static final Object sLock = new Object();
    private final RemoteService mRemoteService;

    public static abstract class OnResultCallback {
        public void onGetAppPermissions(List<RuntimePermissionPresentationInfo> list) {
        }

        public void getAppsUsingPermissions(boolean system, List<ApplicationInfo> list) {
        }
    }

    private static final class RemoteService extends Handler implements ServiceConnection {
        public static final int MSG_GET_APPS_USING_PERMISSIONS = 2;
        public static final int MSG_GET_APP_PERMISSIONS = 1;
        public static final int MSG_UNBIND = 3;
        private static final long UNBIND_TIMEOUT_MILLIS = 10000;
        @GuardedBy("mLock")
        private boolean mBound;
        private final Context mContext;
        private final Object mLock = new Object();
        @GuardedBy("mLock")
        private final List<Message> mPendingWork = new ArrayList();
        @GuardedBy("mLock")
        private IRuntimePermissionPresenter mRemoteInstance;

        public RemoteService(Context context) {
            super(context.getMainLooper(), null, false);
            this.mContext = context;
        }

        public void processMessage(Message message) {
            synchronized (this.mLock) {
                if (!this.mBound) {
                    Intent intent = new Intent(RuntimePermissionPresenterService.SERVICE_INTERFACE);
                    intent.setPackage(this.mContext.getPackageManager().getPermissionControllerPackageName());
                    this.mBound = this.mContext.bindService(intent, this, 1);
                }
                this.mPendingWork.add(message);
                scheduleNextMessageIfNeededLocked();
            }
        }

        public void onServiceConnected(ComponentName name, IBinder service) {
            synchronized (this.mLock) {
                this.mRemoteInstance = Stub.asInterface(service);
                scheduleNextMessageIfNeededLocked();
            }
        }

        public void onServiceDisconnected(ComponentName name) {
            synchronized (this.mLock) {
                this.mRemoteInstance = null;
            }
        }

        public void handleMessage(Message msg) {
            SomeArgs args;
            final OnResultCallback callback;
            final Handler handler;
            IRuntimePermissionPresenter remoteInstance;
            switch (msg.what) {
                case 1:
                    args = msg.obj;
                    String packageName = args.arg1;
                    callback = args.arg2;
                    handler = args.arg3;
                    args.recycle();
                    synchronized (this.mLock) {
                        remoteInstance = this.mRemoteInstance;
                    }
                    if (remoteInstance != null) {
                        try {
                            remoteInstance.getAppPermissions(packageName, new RemoteCallback(new OnResultListener() {
                                public void onResult(Bundle result) {
                                    List<RuntimePermissionPresentationInfo> permissions = null;
                                    if (result != null) {
                                        permissions = result.getParcelableArrayList(RuntimePermissionPresenter.KEY_RESULT);
                                    }
                                    if (permissions == null) {
                                        permissions = Collections.emptyList();
                                    }
                                    final List<RuntimePermissionPresentationInfo> reportedPermissions = permissions;
                                    if (handler != null) {
                                        Handler handler = handler;
                                        final OnResultCallback onResultCallback = callback;
                                        handler.post(new Runnable() {
                                            public void run() {
                                                onResultCallback.onGetAppPermissions(reportedPermissions);
                                            }
                                        });
                                        return;
                                    }
                                    callback.onGetAppPermissions(reportedPermissions);
                                }
                            }, this));
                        } catch (RemoteException re) {
                            Log.e(RuntimePermissionPresenter.TAG, "Error getting app permissions", re);
                        }
                        scheduleUnbind();
                        break;
                    }
                    return;
                case 2:
                    args = (SomeArgs) msg.obj;
                    callback = (OnResultCallback) args.arg1;
                    handler = (Handler) args.arg2;
                    final boolean system = args.argi1 == 1;
                    args.recycle();
                    synchronized (this.mLock) {
                        remoteInstance = this.mRemoteInstance;
                    }
                    if (remoteInstance != null) {
                        try {
                            remoteInstance.getAppsUsingPermissions(system, new RemoteCallback(new OnResultListener() {
                                public void onResult(Bundle result) {
                                    List<ApplicationInfo> apps = null;
                                    if (result != null) {
                                        apps = result.getParcelableArrayList(RuntimePermissionPresenter.KEY_RESULT);
                                    }
                                    if (apps == null) {
                                        apps = Collections.emptyList();
                                    }
                                    final List<ApplicationInfo> reportedApps = apps;
                                    if (handler != null) {
                                        Handler handler = handler;
                                        final OnResultCallback onResultCallback = callback;
                                        final boolean z = system;
                                        handler.post(new Runnable() {
                                            public void run() {
                                                onResultCallback.getAppsUsingPermissions(z, reportedApps);
                                            }
                                        });
                                        return;
                                    }
                                    callback.getAppsUsingPermissions(system, reportedApps);
                                }
                            }, this));
                        } catch (RemoteException re2) {
                            Log.e(RuntimePermissionPresenter.TAG, "Error getting apps using permissions", re2);
                        }
                        scheduleUnbind();
                        break;
                    }
                    return;
                case 3:
                    synchronized (this.mLock) {
                        if (this.mBound) {
                            this.mContext.unbindService(this);
                            this.mBound = false;
                        }
                        this.mRemoteInstance = null;
                    }
                    break;
            }
            synchronized (this.mLock) {
                scheduleNextMessageIfNeededLocked();
            }
        }

        private void scheduleNextMessageIfNeededLocked() {
            if (this.mBound && this.mRemoteInstance != null && !this.mPendingWork.isEmpty()) {
                sendMessage((Message) this.mPendingWork.remove(0));
            }
        }

        private void scheduleUnbind() {
            removeMessages(3);
            sendEmptyMessageDelayed(3, UNBIND_TIMEOUT_MILLIS);
        }
    }

    public static RuntimePermissionPresenter getInstance(Context context) {
        RuntimePermissionPresenter runtimePermissionPresenter;
        synchronized (sLock) {
            if (sInstance == null) {
                sInstance = new RuntimePermissionPresenter(context.getApplicationContext());
            }
            runtimePermissionPresenter = sInstance;
        }
        return runtimePermissionPresenter;
    }

    private RuntimePermissionPresenter(Context context) {
        this.mRemoteService = new RemoteService(context);
    }

    public void getAppPermissions(String packageName, OnResultCallback callback, Handler handler) {
        SomeArgs args = SomeArgs.obtain();
        args.arg1 = packageName;
        args.arg2 = callback;
        args.arg3 = handler;
        this.mRemoteService.processMessage(this.mRemoteService.obtainMessage(1, args));
    }

    public void getAppsUsingPermissions(boolean system, OnResultCallback callback, Handler handler) {
        SomeArgs args = SomeArgs.obtain();
        args.arg1 = callback;
        args.arg2 = handler;
        args.argi1 = system ? 1 : 0;
        this.mRemoteService.processMessage(this.mRemoteService.obtainMessage(2, args));
    }
}
