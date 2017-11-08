package com.android.systemui.statusbar.policy;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraManager.TorchCallback;
import android.os.Handler;
import android.os.HandlerThread;
import android.provider.Settings.Global;
import android.text.TextUtils;
import android.util.Log;
import com.android.systemui.flashlight.FlashlightUtils;
import com.android.systemui.utils.HwLog;
import com.android.systemui.utils.SystemUIThread;
import com.android.systemui.utils.SystemUIThread.SimpleAsyncTask;
import com.android.systemui.utils.analyze.MonitorReporter;
import com.huawei.keyguard.inf.IFlashlightController;
import com.huawei.keyguard.inf.IFlashlightController.FlashlightListener;
import java.lang.ref.WeakReference;
import java.util.ArrayList;

public class FlashlightController implements IFlashlightController {
    private static final boolean DEBUG = Log.isLoggable("FlashlightController", 3);
    private final String mCameraId;
    private final CameraManager mCameraManager;
    private Context mContext;
    private boolean mFlashlightEnabled = false;
    private ContentObserver mFlashlightObserver = new ContentObserver(new Handler()) {
        public void onChange(boolean selfChange) {
            if (FlashlightController.this.mContext != null) {
                final int state = FlashlightController.getState(FlashlightController.this.mContext);
                HwLog.i("FlashlightController", "mFlashlightObserver::onChange state=" + state);
                SystemUIThread.runAsync(new SimpleAsyncTask() {
                    public boolean runInThread() {
                        if (state == 0) {
                            FlashlightController.this.setFlashlight(false);
                            FlashlightController.this.mTorchAvailable = true;
                        } else if (state == 1) {
                            FlashlightController.this.setFlashlight(true);
                            FlashlightController.this.mTorchAvailable = true;
                        } else if (state == -1) {
                            FlashlightController.this.dispatchAvailabilityChanged(false);
                            FlashlightController.this.mTorchAvailable = false;
                        }
                        return false;
                    }
                });
            }
        }
    };
    private Handler mHandler;
    private final ArrayList<WeakReference<FlashlightListener>> mListeners = new ArrayList(1);
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            HwLog.i("FlashlightController", "onReceive:" + intent);
            if (intent.getAction() == null) {
                HwLog.e("FlashlightController", "mReceiver action == null");
                return;
            }
            if ("android.intent.action.ACTION_SHUTDOWN".equals(intent.getAction()) || "android.intent.action.BOOT_COMPLETED".equals(intent.getAction())) {
                SystemUIThread.runAsync(new SimpleAsyncTask() {
                    public boolean runInThread() {
                        FlashlightController.this.setFlashlight(false);
                        return false;
                    }
                });
            }
        }
    };
    private boolean mTorchAvailable = true;
    private final TorchCallbackImp mTorchCallback = new TorchCallbackImp();

    private class TorchCallbackImp extends TorchCallback {
        public void onTorchModeUnavailable(String cameraId) {
            if (TextUtils.equals(cameraId, FlashlightController.this.mCameraId)) {
                setCameraAvailable(false);
            }
        }

        public void onTorchModeChanged(String cameraId, boolean enabled) {
            if (TextUtils.equals(cameraId, FlashlightController.this.mCameraId)) {
                setCameraAvailable(true);
                setTorchMode(enabled);
            }
        }

        private void setCameraAvailable(boolean available) {
            synchronized (FlashlightController.this) {
                boolean changed = FlashlightController.this.mTorchAvailable != available;
                FlashlightController.this.mTorchAvailable = available;
                HwLog.i("FlashlightController", "setCameraAvailable mTorchAvailable:" + FlashlightController.this.mTorchAvailable);
            }
            if (changed) {
                Log.d("FlashlightController", "dispatchAvailabilityChanged(" + available + ")");
                FlashlightController.this.dispatchAvailabilityChanged(available);
            }
        }

        private void setTorchMode(boolean enabled) {
            synchronized (FlashlightController.this) {
                boolean changed = FlashlightController.this.mFlashlightEnabled != enabled;
                FlashlightController.this.mFlashlightEnabled = enabled;
                HwLog.i("FlashlightController", "setTorchMode mFlashlightEnabled:" + FlashlightController.this.mFlashlightEnabled);
            }
            if (changed) {
                Log.d("FlashlightController", "dispatchModeChanged(" + enabled + ")");
                FlashlightController.this.dispatchModeChanged(enabled);
            }
        }
    }

    public FlashlightController(Context mContext) {
        String cameraId;
        String str = null;
        this.mContext = mContext;
        this.mCameraManager = (CameraManager) mContext.getSystemService("camera");
        try {
            cameraId = getCameraId();
        } catch (Throwable e) {
            Log.e("FlashlightController", "Couldn't initialize.", e);
            return;
        } finally {
            this.mCameraId = 
/*
Method generation error in method: com.android.systemui.statusbar.policy.FlashlightController.<init>(android.content.Context):void
jadx.core.utils.exceptions.CodegenException: Error generate insn: 0x0061: IPUT  (wrap: java.lang.String
  ?: MERGE  (r4_1 java.lang.String) = (r4_0 'str' java.lang.String), (r0_2 'cameraId' java.lang.String)), (r5_0 'this' com.android.systemui.statusbar.policy.FlashlightController) com.android.systemui.statusbar.policy.FlashlightController.mCameraId java.lang.String in method: com.android.systemui.statusbar.policy.FlashlightController.<init>(android.content.Context):void
	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:226)
	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:203)
	at jadx.core.codegen.RegionGen.makeSimpleBlock(RegionGen.java:100)
	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:50)
	at jadx.core.codegen.RegionGen.makeSimpleRegion(RegionGen.java:87)
	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:53)
	at jadx.core.codegen.RegionGen.makeRegionIndent(RegionGen.java:93)
	at jadx.core.codegen.RegionGen.makeTryCatch(RegionGen.java:297)
	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:63)
	at jadx.core.codegen.RegionGen.makeSimpleRegion(RegionGen.java:87)
	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:53)
	at jadx.core.codegen.MethodGen.addInstructions(MethodGen.java:187)
	at jadx.core.codegen.ClassGen.addMethod(ClassGen.java:328)
	at jadx.core.codegen.ClassGen.addMethods(ClassGen.java:265)
	at jadx.core.codegen.ClassGen.addClassBody(ClassGen.java:228)
	at jadx.core.codegen.ClassGen.addClassCode(ClassGen.java:118)
	at jadx.core.codegen.ClassGen.makeClass(ClassGen.java:83)
	at jadx.core.codegen.CodeGen.visit(CodeGen.java:19)
	at jadx.core.ProcessClass.process(ProcessClass.java:43)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:306)
	at jadx.api.JavaClass.decompile(JavaClass.java:62)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:199)
Caused by: jadx.core.utils.exceptions.CodegenException: Error generate insn: ?: MERGE  (r4_1 java.lang.String) = (r4_0 'str' java.lang.String), (r0_2 'cameraId' java.lang.String) in method: com.android.systemui.statusbar.policy.FlashlightController.<init>(android.content.Context):void
	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:226)
	at jadx.core.codegen.InsnGen.addArg(InsnGen.java:101)
	at jadx.core.codegen.InsnGen.makeInsnBody(InsnGen.java:393)
	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:220)
	... 21 more
Caused by: jadx.core.utils.exceptions.CodegenException: MERGE can be used only in fallback mode
	at jadx.core.codegen.InsnGen.fallbackOnlyInsn(InsnGen.java:530)
	at jadx.core.codegen.InsnGen.makeInsnBody(InsnGen.java:514)
	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:211)
	... 24 more

*/

            public void setFlashlight(boolean enabled) {
                int i;
                int i2 = 1;
                HwLog.i("FlashlightController", "setFlashlight enabled:" + enabled + " mFlashlightEnabled:" + this.mFlashlightEnabled);
                ContentResolver contentResolver = this.mContext.getContentResolver();
                if (enabled) {
                    i = 1;
                } else {
                    i = 0;
                }
                putState(contentResolver, i);
                boolean pendingError = false;
                synchronized (this) {
                    if (this.mFlashlightEnabled != enabled) {
                        this.mFlashlightEnabled = enabled;
                        HwLog.i("FlashlightController", "setFlashlight mFlashlightEnabled:" + this.mFlashlightEnabled);
                        if (FlashlightUtils.HW_POSTCAMERA_SUPPORT) {
                            HwLog.i("FlashlightController", "setFlashlight with huawei post camera, mFlashlightEnabled:" + this.mFlashlightEnabled);
                            Context context = this.mContext;
                            if (!this.mFlashlightEnabled) {
                                i2 = 0;
                            }
                            FlashlightUtils.controlCamera(context, i2);
                            dispatchModeChanged(this.mFlashlightEnabled);
                            return;
                        }
                        try {
                            this.mCameraManager.setTorchMode(this.mCameraId, enabled);
                        } catch (CameraAccessException e) {
                            Log.e("FlashlightController", "Couldn't set torch mode", e);
                            this.mFlashlightEnabled = false;
                            HwLog.e("FlashlightController", "setFlashlight CameraAccessException mFlashlightEnabled false");
                            pendingError = true;
                            triggerOperatedFlashlightError(e.getMessage(), enabled);
                        } catch (Exception e2) {
                            HwLog.e("FlashlightController", "setFlashlight Exception ", e2);
                            this.mFlashlightEnabled = false;
                            pendingError = true;
                            triggerOperatedFlashlightError(e2.getMessage(), enabled);
                        }
                    }
                }
                dispatchModeChanged(this.mFlashlightEnabled);
                if (pendingError) {
                    dispatchError();
                }
            }

            private void triggerOperatedFlashlightError(String errorMsg, boolean enable) {
                MonitorReporter.doMonitor(MonitorReporter.createInfoIntent(907033008, MonitorReporter.createFlashlightStateInfo(errorMsg, enable)));
            }

            public boolean hasFlashlight() {
                return this.mCameraId != null;
            }

            public synchronized boolean isEnabled() {
                return this.mFlashlightEnabled;
            }

            public synchronized boolean isAvailable() {
                return this.mTorchAvailable;
            }

            public void addListener(FlashlightListener l) {
                synchronized (this.mListeners) {
                    cleanUpListenersLocked(l);
                    this.mListeners.add(new WeakReference(l));
                }
            }

            public void removeListener(FlashlightListener l) {
                synchronized (this.mListeners) {
                    cleanUpListenersLocked(l);
                }
            }

            public void addRegister(Context mContext) {
                if (mContext != null && this.mFlashlightObserver != null) {
                    registerBraodcastReceiver(mContext);
                    mContext.getContentResolver().registerContentObserver(Global.getUriFor("flashlight_current_state"), true, this.mFlashlightObserver);
                }
            }

            public void removeRegister(Context mContext) {
                if (mContext != null && this.mFlashlightObserver != null) {
                    mContext.unregisterReceiver(this.mReceiver);
                    mContext.getContentResolver().unregisterContentObserver(this.mFlashlightObserver);
                }
            }

            private void registerBraodcastReceiver(Context mContext) {
                IntentFilter filter = new IntentFilter();
                filter.addAction("android.intent.action.ACTION_SHUTDOWN");
                filter.addAction("android.intent.action.BOOT_COMPLETED");
                mContext.registerReceiver(this.mReceiver, filter);
            }

            public static int getState(Context context) {
                return Global.getInt(context.getContentResolver(), "flashlight_current_state", 0);
            }

            public static void putState(ContentResolver cr, int state) {
                Global.putInt(cr, "flashlight_current_state", state);
            }

            private synchronized void ensureHandler() {
                if (this.mHandler == null) {
                    HandlerThread thread = new HandlerThread("FlashlightController", 10);
                    thread.start();
                    this.mHandler = new Handler(thread.getLooper());
                }
            }

            private String getCameraId() throws CameraAccessException {
                for (String id : this.mCameraManager.getCameraIdList()) {
                    CameraCharacteristics c = this.mCameraManager.getCameraCharacteristics(id);
                    Boolean flashAvailable = (Boolean) c.get(CameraCharacteristics.FLASH_INFO_AVAILABLE);
                    Integer lensFacing = (Integer) c.get(CameraCharacteristics.LENS_FACING);
                    if (flashAvailable != null && flashAvailable.booleanValue() && lensFacing != null && lensFacing.intValue() == 1) {
                        return id;
                    }
                }
                return null;
            }

            private void dispatchModeChanged(boolean enabled) {
                dispatchListeners(1, enabled);
            }

            private void dispatchError() {
                dispatchListeners(1, false);
            }

            private void dispatchAvailabilityChanged(boolean available) {
                dispatchListeners(2, available);
            }

            private void dispatchListeners(int message, boolean argument) {
                synchronized (this.mListeners) {
                    int N = this.mListeners.size();
                    boolean cleanup = false;
                    for (int i = 0; i < N; i++) {
                        FlashlightListener l = (FlashlightListener) ((WeakReference) this.mListeners.get(i)).get();
                        if (l == null) {
                            cleanup = true;
                        } else if (message == 0) {
                            l.onFlashlightError();
                        } else if (message == 1) {
                            l.onFlashlightChanged(argument);
                        } else if (message == 2) {
                            l.onFlashlightAvailabilityChanged(argument);
                        } else {
                            continue;
                        }
                    }
                    if (cleanup) {
                        cleanUpListenersLocked(null);
                    }
                }
            }

            private void cleanUpListenersLocked(FlashlightListener listener) {
                for (int i = this.mListeners.size() - 1; i >= 0; i--) {
                    FlashlightListener found = (FlashlightListener) ((WeakReference) this.mListeners.get(i)).get();
                    if (found == null || found == listener) {
                        this.mListeners.remove(i);
                    }
                }
            }
        }
