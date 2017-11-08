package com.huawei.android.quickaction;

import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import com.huawei.android.quickaction.IQuickActionService.Stub;
import java.util.List;

public abstract class QuickActionService extends Service {
    static final String BIND_PERMISSION = "com.huawei.android.permission.BIND_QUICK_ACTION_SERVICE";
    private static final boolean DEBUG = false;
    static final String META_DATA_NAME = "com.huawei.android.quickaction.quick_action_service";
    static final String SERVICE_INTERFACE = "com.huawei.android.quickaction.QuickActionService";
    private final String TAG = new StringBuilder(String.valueOf(QuickActionService.class.getSimpleName())).append('[').append(getClass().getSimpleName()).append(']').toString();
    private IQuickActionServiceWrapper mWrapper = null;

    class IQuickActionServiceWrapper extends Stub {
        IQuickActionServiceWrapper() {
        }

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void getQuickActions(ComponentName componentName, IQuickActionResult iQuickActionResult) throws RemoteException {
            List onGetQuickActions;
            List list = null;
            try {
                onGetQuickActions = QuickActionService.this.onGetQuickActions(componentName);
            } finally {
                iQuickActionResult.sendResult(
/*
Method generation error in method: com.huawei.android.quickaction.QuickActionService.IQuickActionServiceWrapper.getQuickActions(android.content.ComponentName, com.huawei.android.quickaction.IQuickActionResult):void
jadx.core.utils.exceptions.CodegenException: Error generate insn: 0x000c: INVOKE  (r4_0 'iQuickActionResult' com.huawei.android.quickaction.IQuickActionResult), (wrap: java.util.List
  ?: MERGE  (r1_1 java.util.List) = (r1_0 'list' java.util.List), (r0_3 'onGetQuickActions' java.util.List)) com.huawei.android.quickaction.IQuickActionResult.sendResult(java.util.List):void type: INTERFACE in method: com.huawei.android.quickaction.QuickActionService.IQuickActionServiceWrapper.getQuickActions(android.content.ComponentName, com.huawei.android.quickaction.IQuickActionResult):void
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
	at jadx.core.codegen.ClassGen.addInnerClasses(ClassGen.java:241)
	at jadx.core.codegen.ClassGen.addClassBody(ClassGen.java:227)
	at jadx.core.codegen.ClassGen.addClassCode(ClassGen.java:118)
	at jadx.core.codegen.ClassGen.makeClass(ClassGen.java:83)
	at jadx.core.codegen.CodeGen.visit(CodeGen.java:19)
	at jadx.core.ProcessClass.process(ProcessClass.java:43)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:306)
	at jadx.api.JavaClass.decompile(JavaClass.java:62)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:199)
Caused by: jadx.core.utils.exceptions.CodegenException: Error generate insn: ?: MERGE  (r1_1 java.util.List) = (r1_0 'list' java.util.List), (r0_3 'onGetQuickActions' java.util.List) in method: com.huawei.android.quickaction.QuickActionService.IQuickActionServiceWrapper.getQuickActions(android.content.ComponentName, com.huawei.android.quickaction.IQuickActionResult):void
	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:226)
	at jadx.core.codegen.InsnGen.addArg(InsnGen.java:101)
	at jadx.core.codegen.InsnGen.generateMethodArguments(InsnGen.java:679)
	at jadx.core.codegen.InsnGen.makeInvoke(InsnGen.java:649)
	at jadx.core.codegen.InsnGen.makeInsnBody(InsnGen.java:343)
	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:220)
	... 24 more
Caused by: jadx.core.utils.exceptions.CodegenException: MERGE can be used only in fallback mode
	at jadx.core.codegen.InsnGen.fallbackOnlyInsn(InsnGen.java:530)
	at jadx.core.codegen.InsnGen.makeInsnBody(InsnGen.java:514)
	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:211)
	... 29 more

*/
            }

            public abstract List<QuickAction> onGetQuickActions(ComponentName componentName);

            public IBinder onBind(Intent intent) {
                if (!SERVICE_INTERFACE.equals(intent.getAction())) {
                    return null;
                }
                if (this.mWrapper == null) {
                    this.mWrapper = new IQuickActionServiceWrapper();
                }
                return this.mWrapper;
            }
        }
