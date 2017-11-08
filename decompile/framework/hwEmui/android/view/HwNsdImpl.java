package android.view;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.FreezeScreenScene;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.util.Log;
import android.view.IWindowManager.Stub;
import android.view.View.AttachInfo;
import android.widget.TextView;
import com.huawei.android.hwaps.HwapsWrapper;
import com.huawei.android.hwaps.IEventAnalyzed;
import com.huawei.android.hwaps.IFpsController;
import com.huawei.hsm.permission.ConnectPermission;
import huawei.com.android.internal.widget.HwFragmentMenuItemView;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class HwNsdImpl implements IHwNsdImpl {
    private static final int APS_SUPPORT_2DSDR = 4096;
    private static final int BINDER_NSD_CURSOR_SET_BMP = 2000;
    private static final int BINDER_NSD_INTERRUPT_CMD = 2002;
    public static final int BINDER_NSD_QUERY_REFRESH_TIME = 2003;
    private static final int BINDER_NSD_QUERY_VISIBLE_LAYER = 2001;
    private static final int CHECK_ADVIEW_PARENT_NUM_MAX = 5;
    private static final int COMPAT_MODE_ENABLE_BIT = 32768;
    private static final int CONFIGTYPE_BLACKLIST = 7000;
    private static final int CONFIGTYPE_QUERYRESULTLIST = 7001;
    private static final int CONFIGTYPE_WHITELIST = 9998;
    private static final boolean HWDBG = true;
    private static final boolean HWLOGW_E = true;
    private static int Level = 2;
    private static final int NSD_CASE_LAUNCHER_IDLE = 1;
    private static final int NSD_CASE_NOTHING = 0;
    private static final int NSD_CMD_DRAW_AND_CONTROL_1 = 101;
    private static final int NSD_CMD_DRAW_AND_CONTROL_2 = 82;
    private static final int NSD_CMD_DRAW_AND_CONTROL_3 = 65;
    private static final int NSD_CMD_DRAW_AND_SAVE_1 = 101;
    private static final int NSD_CMD_DRAW_AND_SAVE_2 = 65;
    private static final int NSD_CMD_DRAW_AND_SAVE_3 = 82;
    private static final int NSD_CMD_NODRAW_AND_SAVE_1 = 67;
    private static final int NSD_CMD_NODRAW_AND_SAVE_2 = 101;
    private static final int NSD_CMD_NODRAW_AND_SAVE_3 = 135;
    private static final int NSD_CMD_NULL = 255;
    private static final int NSD_HIGH_SCREEN_HIGHT = 1920;
    private static final int NSD_INTERRUPT_CMD_READY_SCROLL = 104;
    private static final int NSD_INTERRUPT_CMD_RELEASE = 101;
    private static final int NSD_INTERRUPT_CMD_REQUIRE = 102;
    private static final int NSD_INTERRUPT_CMD_RESET = 103;
    private static final int NSD_INTERRUPT_CMD_STOP = 100;
    private static final int NSD_ISREADY_CONTROL_CASE = 2;
    private static final int NSD_ISREADY_SAVE_CASE = 1;
    private static final int NSD_ISREADY_SCROLL_CASE = 0;
    private static final int NSD_LOW_SCREEN_HIGHT = 960;
    private static final int NSD_MIDDLE_SCREEN_HIGHT = 1280;
    private static final int NSD_SHOULD_RELEASE = 0;
    private static final int NSD_SHOULD_RESET_DRAWN = 1;
    private static final int NSD_SHOULD_START_SCROLL = 2;
    private static final int NSD_SUPPORT_CURSOR = 256;
    private static final int NSD_SUPPORT_LAUNCHER = 512;
    private static final int NSD_SUPPORT_NONE_MASK = -4;
    private static final int NSD_SUPPORT_NOTHING = 0;
    private static final String TAG = "NSD";
    private static HwNsdImpl sInstance = null;
    private boolean mCanSaveBySf = false;
    private int mCheckSFControlCount = 0;
    private int mCheckTimes = 0;
    private int mCmdPosition = SystemProperties.getInt("sys.nsd.launchertop", -1);
    private Context mContext = null;
    private int mCtrlBySf;
    private int[] mCursorPosition = new int[2];
    private IEventAnalyzed mEventAnalyzed = null;
    private int mFirstBlinkCount = 0;
    private IFpsController mFpsController = null;
    private boolean mHasReadNsdSupportProperty = false;
    private boolean mHasReleaseNSD = false;
    private boolean mIsFirstEnterAPS = true;
    private int mIsNsdSupport;
    private long mLastBlinkTime = 0;
    private int mLastX;
    private int mLastY;
    private String mLayerName;
    private Map<String, Integer> mMapCheckResult = new HashMap();
    private int mMaxSavaPageNo;
    private int mMinSavePageNo;
    private boolean mNeedCheckSFControl = false;
    private String mOldPkgName = "";
    private int mPageMustSaveFlag = 0;
    private int mPageNo = 255;
    private int mPageNum;
    private int mPagesSaveFlag;
    private int mRecheckFlag = 100;
    private int mScreenWidth = 0;
    private View mView;
    private IWindowManager mWindowManagerService = Stub.asInterface(ServiceManager.getService(FreezeScreenScene.WINDOW_PARAM));
    private int mZorder;
    private int nsdCaseNo = 0;

    private boolean isSFReady(int r12) {
        /* JADX: method processing error */
/*
Error: jadx.core.utils.exceptions.JadxRuntimeException: Can't find block by offset: 0x0061 in list []
	at jadx.core.utils.BlockUtils.getBlockByOffset(BlockUtils.java:43)
	at jadx.core.dex.instructions.IfNode.initBlocks(IfNode.java:60)
	at jadx.core.dex.visitors.blocksmaker.BlockFinish.initBlocksInIfNodes(BlockFinish.java:48)
	at jadx.core.dex.visitors.blocksmaker.BlockFinish.visit(BlockFinish.java:33)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:306)
	at jadx.api.JavaClass.decompile(JavaClass.java:62)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:199)
*/
        /*
        r11 = this;
        r10 = 0;
        r9 = 1;
        r5 = 0;
        r6 = 0;
        r4 = 0;
        r0 = android.os.Parcel.obtain();
        r3 = android.os.Parcel.obtain();
        r7 = "SurfaceFlinger";	 Catch:{ RemoteException -> 0x0041, all -> 0x0067 }
        r2 = android.os.ServiceManager.getService(r7);	 Catch:{ RemoteException -> 0x0041, all -> 0x0067 }
        if (r2 == 0) goto L_0x0033;	 Catch:{ RemoteException -> 0x0041, all -> 0x0067 }
    L_0x0016:
        r7 = "android.ui.ISurfaceComposer";	 Catch:{ RemoteException -> 0x0041, all -> 0x0067 }
        r0.writeInterfaceToken(r7);	 Catch:{ RemoteException -> 0x0041, all -> 0x0067 }
        r7 = 102; // 0x66 float:1.43E-43 double:5.04E-322;	 Catch:{ RemoteException -> 0x0041, all -> 0x0067 }
        r0.writeInt(r7);	 Catch:{ RemoteException -> 0x0041, all -> 0x0067 }
        r7 = 2002; // 0x7d2 float:2.805E-42 double:9.89E-321;	 Catch:{ RemoteException -> 0x0041, all -> 0x0067 }
        r8 = 0;	 Catch:{ RemoteException -> 0x0041, all -> 0x0067 }
        r2.transact(r7, r0, r3, r8);	 Catch:{ RemoteException -> 0x0041, all -> 0x0067 }
        r5 = r3.readInt();	 Catch:{ RemoteException -> 0x0041, all -> 0x0067 }
        r6 = r3.readInt();	 Catch:{ RemoteException -> 0x0041, all -> 0x0067 }
        r4 = r3.readInt();	 Catch:{ RemoteException -> 0x0041, all -> 0x0067 }
    L_0x0033:
        if (r0 == 0) goto L_0x0038;
    L_0x0035:
        r0.recycle();
    L_0x0038:
        if (r3 == 0) goto L_0x003d;
    L_0x003a:
        r3.recycle();
    L_0x003d:
        switch(r12) {
            case 0: goto L_0x0073;
            case 1: goto L_0x0096;
            case 2: goto L_0x008a;
            default: goto L_0x0040;
        };
    L_0x0040:
        return r10;
    L_0x0041:
        r1 = move-exception;
        r7 = "NSD";	 Catch:{ RemoteException -> 0x0041, all -> 0x0067 }
        r8 = new java.lang.StringBuilder;	 Catch:{ RemoteException -> 0x0041, all -> 0x0067 }
        r8.<init>();	 Catch:{ RemoteException -> 0x0041, all -> 0x0067 }
        r9 = "isSFReady error : ";	 Catch:{ RemoteException -> 0x0041, all -> 0x0067 }
        r8 = r8.append(r9);	 Catch:{ RemoteException -> 0x0041, all -> 0x0067 }
        r8 = r8.append(r1);	 Catch:{ RemoteException -> 0x0041, all -> 0x0067 }
        r8 = r8.toString();	 Catch:{ RemoteException -> 0x0041, all -> 0x0067 }
        android.util.Log.e(r7, r8);	 Catch:{ RemoteException -> 0x0041, all -> 0x0067 }
        if (r0 == 0) goto L_0x0061;
    L_0x005e:
        r0.recycle();
    L_0x0061:
        if (r3 == 0) goto L_0x0066;
    L_0x0063:
        r3.recycle();
    L_0x0066:
        return r10;
    L_0x0067:
        r7 = move-exception;
        if (r0 == 0) goto L_0x006d;
    L_0x006a:
        r0.recycle();
    L_0x006d:
        if (r3 == 0) goto L_0x0072;
    L_0x006f:
        r3.recycle();
    L_0x0072:
        throw r7;
    L_0x0073:
        if (r9 != r5) goto L_0x0083;
    L_0x0075:
        r7 = r11.mPagesSaveFlag;
        if (r6 != r7) goto L_0x0083;
    L_0x0079:
        r7 = "NSD";
        r8 = "NSD_ISREADY_SCROLL_CASE  ok";
        android.util.Log.v(r7, r8);
        return r9;
    L_0x0083:
        r7 = r11.mPagesSaveFlag;
        if (r7 == r6) goto L_0x0040;
    L_0x0087:
        r11.mPagesSaveFlag = r6;
        return r10;
    L_0x008a:
        if (r9 != r4) goto L_0x0040;
    L_0x008c:
        r7 = "NSD";
        r8 = "NSD_ISREADY_CONTROL_CASE  ok";
        android.util.Log.v(r7, r8);
        return r9;
    L_0x0096:
        if (r9 != r5) goto L_0x0040;
    L_0x0098:
        r7 = "NSD";
        r8 = "NSD_ISREADY_SAVE_CASE  ok";
        android.util.Log.v(r7, r8);
        return r9;
        */
        throw new UnsupportedOperationException("Method not decompiled: android.view.HwNsdImpl.isSFReady(int):boolean");
    }

    private void sendInterruptCmd(int r7) {
        /* JADX: method processing error */
/*
Error: java.util.NoSuchElementException
	at java.util.HashMap$HashIterator.nextNode(HashMap.java:1431)
	at java.util.HashMap$KeyIterator.next(HashMap.java:1453)
	at jadx.core.dex.visitors.blocksmaker.BlockFinallyExtract.applyRemove(BlockFinallyExtract.java:535)
	at jadx.core.dex.visitors.blocksmaker.BlockFinallyExtract.extractFinally(BlockFinallyExtract.java:175)
	at jadx.core.dex.visitors.blocksmaker.BlockFinallyExtract.processExceptionHandler(BlockFinallyExtract.java:80)
	at jadx.core.dex.visitors.blocksmaker.BlockFinallyExtract.visit(BlockFinallyExtract.java:51)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:306)
	at jadx.api.JavaClass.decompile(JavaClass.java:62)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:199)
*/
        /*
        r6 = this;
        r0 = android.os.Parcel.obtain();
        r3 = "SurfaceFlinger";	 Catch:{ RemoteException -> 0x004f, all -> 0x0070 }
        r2 = android.os.ServiceManager.getService(r3);	 Catch:{ RemoteException -> 0x004f, all -> 0x0070 }
        if (r2 == 0) goto L_0x0049;	 Catch:{ RemoteException -> 0x004f, all -> 0x0070 }
    L_0x000d:
        r3 = "android.ui.ISurfaceComposer";	 Catch:{ RemoteException -> 0x004f, all -> 0x0070 }
        r0.writeInterfaceToken(r3);	 Catch:{ RemoteException -> 0x004f, all -> 0x0070 }
        r0.writeInt(r7);	 Catch:{ RemoteException -> 0x004f, all -> 0x0070 }
        r3 = r6.mPagesSaveFlag;	 Catch:{ RemoteException -> 0x004f, all -> 0x0070 }
        r0.writeInt(r3);	 Catch:{ RemoteException -> 0x004f, all -> 0x0070 }
        r3 = r6.mPageNo;	 Catch:{ RemoteException -> 0x004f, all -> 0x0070 }
        r4 = r6.mMinSavePageNo;	 Catch:{ RemoteException -> 0x004f, all -> 0x0070 }
        r3 = r3 - r4;	 Catch:{ RemoteException -> 0x004f, all -> 0x0070 }
        r0.writeInt(r3);	 Catch:{ RemoteException -> 0x004f, all -> 0x0070 }
        r3 = r6.mPageNum;	 Catch:{ RemoteException -> 0x004f, all -> 0x0070 }
        r0.writeInt(r3);	 Catch:{ RemoteException -> 0x004f, all -> 0x0070 }
        r3 = 2002; // 0x7d2 float:2.805E-42 double:9.89E-321;	 Catch:{ RemoteException -> 0x004f, all -> 0x0070 }
        r4 = 0;	 Catch:{ RemoteException -> 0x004f, all -> 0x0070 }
        r5 = 0;	 Catch:{ RemoteException -> 0x004f, all -> 0x0070 }
        r2.transact(r3, r0, r4, r5);	 Catch:{ RemoteException -> 0x004f, all -> 0x0070 }
        r3 = "NSD";	 Catch:{ RemoteException -> 0x004f, all -> 0x0070 }
        r4 = new java.lang.StringBuilder;	 Catch:{ RemoteException -> 0x004f, all -> 0x0070 }
        r4.<init>();	 Catch:{ RemoteException -> 0x004f, all -> 0x0070 }
        r5 = "sendInterruptCmd   cmdcode  : ";	 Catch:{ RemoteException -> 0x004f, all -> 0x0070 }
        r4 = r4.append(r5);	 Catch:{ RemoteException -> 0x004f, all -> 0x0070 }
        r4 = r4.append(r7);	 Catch:{ RemoteException -> 0x004f, all -> 0x0070 }
        r4 = r4.toString();	 Catch:{ RemoteException -> 0x004f, all -> 0x0070 }
        android.util.Log.v(r3, r4);	 Catch:{ RemoteException -> 0x004f, all -> 0x0070 }
    L_0x0049:
        if (r0 == 0) goto L_0x004e;
    L_0x004b:
        r0.recycle();
    L_0x004e:
        return;
    L_0x004f:
        r1 = move-exception;
        r3 = "NSD";	 Catch:{ RemoteException -> 0x004f, all -> 0x0070 }
        r4 = new java.lang.StringBuilder;	 Catch:{ RemoteException -> 0x004f, all -> 0x0070 }
        r4.<init>();	 Catch:{ RemoteException -> 0x004f, all -> 0x0070 }
        r5 = "sendInterruptCmd error : ";	 Catch:{ RemoteException -> 0x004f, all -> 0x0070 }
        r4 = r4.append(r5);	 Catch:{ RemoteException -> 0x004f, all -> 0x0070 }
        r4 = r4.append(r1);	 Catch:{ RemoteException -> 0x004f, all -> 0x0070 }
        r4 = r4.toString();	 Catch:{ RemoteException -> 0x004f, all -> 0x0070 }
        android.util.Log.e(r3, r4);	 Catch:{ RemoteException -> 0x004f, all -> 0x0070 }
        if (r0 == 0) goto L_0x004e;
    L_0x006c:
        r0.recycle();
        goto L_0x004e;
    L_0x0070:
        r3 = move-exception;
        if (r0 == 0) goto L_0x0076;
    L_0x0073:
        r0.recycle();
    L_0x0076:
        throw r3;
        */
        throw new UnsupportedOperationException("Method not decompiled: android.view.HwNsdImpl.sendInterruptCmd(int):void");
    }

    public void resetSaveNSD(int r1) {
        /* JADX: method processing error */
/*
Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.view.HwNsdImpl.resetSaveNSD(int):void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:116)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:249)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:306)
	at jadx.api.JavaClass.decompile(JavaClass.java:62)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:199)
Caused by: jadx.core.utils.exceptions.DecodeException: Unknown instruction: not-int
	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:569)
	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:56)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:102)
	... 5 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.view.HwNsdImpl.resetSaveNSD(int):void");
    }

    protected void HwNsdImpl() {
        Log.e(TAG, "nsd new HwNsdImpl ");
    }

    public static synchronized HwNsdImpl getDefault() {
        HwNsdImpl hwNsdImpl;
        synchronized (HwNsdImpl.class) {
            if (sInstance == null) {
                sInstance = new HwNsdImpl();
            }
            hwNsdImpl = sInstance;
        }
        return hwNsdImpl;
    }

    private void readNsdSupportProperty() {
        this.mIsNsdSupport = SystemProperties.getInt("sys.aps.support", 0);
        Log.v(TAG, "NSD readNsdSupportProperty  :  " + this.mIsNsdSupport);
        this.mHasReadNsdSupportProperty = true;
    }

    public boolean checkIfSupportNsd() {
        if (!this.mHasReadNsdSupportProperty) {
            readNsdSupportProperty();
        }
        if (this.mIsNsdSupport != 0) {
            return true;
        }
        return false;
    }

    public boolean checkIfNsdSupportLauncher() {
        if (this.mHasReleaseNSD || -1 == this.mCmdPosition) {
            return false;
        }
        if (!this.mHasReadNsdSupportProperty) {
            readNsdSupportProperty();
        }
        if (512 == (this.mIsNsdSupport & 512)) {
            return true;
        }
        return false;
    }

    public boolean checkIfNsdSupportCursor() {
        this.mIsNsdSupport = SystemProperties.getInt("sys.aps.support", 0);
        if (256 == (this.mIsNsdSupport & 256)) {
            return true;
        }
        return false;
    }

    public boolean isCase(View v) {
        if (this.mCtrlBySf < 1) {
            return false;
        }
        this.mCheckTimes++;
        if (this.mCheckTimes >= this.mRecheckFlag) {
            this.mCheckTimes = 0;
            this.mHasReadNsdSupportProperty = false;
        }
        if (isLauncherIdle(v)) {
            this.nsdCaseNo = 1;
            return true;
        }
        this.nsdCaseNo = 0;
        return false;
    }

    private boolean isLauncherIdle(View view) {
        if (view == null || !"com.huawei.android.launcher".equals(view.getContext().getPackageName())) {
            return false;
        }
        ViewGroup pV = getCaseVGInLauncherIdle(view);
        if (pV == null || !pV.getClass().getSimpleName().contains("Workspace")) {
            return false;
        }
        ViewGroup pViewGroup = pV;
        int i = 0;
        do {
            pV = getViewGroup(pViewGroup.getChildAt(i));
            i++;
        } while (pV != null);
        this.mPageNum = i - 1;
        computeSavePageNo();
        return true;
    }

    public boolean isNeedAppDraw() {
        if (this.mCtrlBySf < 2) {
            return true;
        }
        if (!this.mNeedCheckSFControl || isSFReady(2)) {
            this.mNeedCheckSFControl = false;
        } else {
            if (this.mCheckSFControlCount > 3) {
                Log.d(TAG, "isNeedAppDraw stop draw !");
                stopNsd();
                this.mNeedCheckSFControl = false;
            }
            this.mCheckSFControlCount++;
        }
        return false;
    }

    public void startNsd(int cmd) {
        if (checkIfNsdSupportLauncher()) {
            switch (cmd) {
                case 2:
                    try {
                        if (!isSFReady(0)) {
                            Log.d(TAG, "SF is not ready!");
                            break;
                        }
                        Log.v(TAG, "NSD_SHOULD_START_SCROLL");
                        if (this.mCtrlBySf == 0) {
                            this.mCtrlBySf = 1;
                            break;
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "startNsd error : " + e);
                        return;
                    }
                    break;
            }
        }
    }

    public void stopNsd() {
        if (this.mCtrlBySf >= 1) {
            this.mCtrlBySf = 0;
            setCmdStopFlag(100);
            Log.v(TAG, "NSD stop scroll");
        }
        this.mNeedCheckSFControl = true;
        this.mCheckSFControlCount = 0;
        this.mPageNo = 255;
    }

    public void releaseNSD() {
        this.mCtrlBySf = 0;
        this.mHasReleaseNSD = true;
        this.mIsNsdSupport &= -513;
        sendInterruptCmd(101);
        Log.w(TAG, "NSD releaseNSD");
    }

    public void adjNsd(String para) {
        setCmdAdjFlag(para);
    }

    public void enableNsdSave() {
        this.mCanSaveBySf = true;
    }

    private ViewGroup getCaseVGInLauncherIdle(View view) {
        if (view == null) {
            return null;
        }
        try {
            ViewGroup pV = getViewGroup(view);
            if (pV == null) {
                return null;
            }
            pV = getViewGroup(pV.getChildAt(0));
            if (pV == null) {
                return null;
            }
            pV = getViewGroup(pV.getChildAt(1));
            if (pV == null) {
                return null;
            }
            pV = getViewGroup(pV.getChildAt(0));
            if (pV == null) {
                return null;
            }
            return pV;
        } catch (Exception e) {
            return null;
        }
    }

    private boolean canSave(View view) {
        if (view == null || !this.mCanSaveBySf) {
            return false;
        }
        if (this.mScreenWidth == 0) {
            this.mScreenWidth = view.getResources().getDisplayMetrics().widthPixels;
        }
        switch (this.nsdCaseNo) {
            case 0:
                return false;
            case 1:
                ViewGroup pV = getCaseVGInLauncherIdle(view);
                if (pV == null || pV.mScrollX % this.mScreenWidth != 0) {
                    return false;
                }
                this.mPageNo = Math.round(((float) pV.mScrollX) / ((float) this.mScreenWidth));
                return (this.mPagesSaveFlag & (1 << this.mPageNo)) != (1 << this.mPageNo) && isSFReady(1);
            default:
                return false;
        }
    }

    private void computeSavePageNo() {
        if (this.mPageMustSaveFlag == 0) {
            switch (this.mPageNum) {
                case 2:
                case 3:
                case 4:
                    this.mMinSavePageNo = 0;
                    this.mMaxSavaPageNo = this.mPageNum - 1;
                    break;
                case 5:
                    this.mMinSavePageNo = 1;
                    this.mMaxSavaPageNo = this.mMinSavePageNo + 3;
                    break;
                case 6:
                    this.mMinSavePageNo = 1;
                    this.mMaxSavaPageNo = this.mMinSavePageNo + 3;
                    break;
                case 7:
                    this.mMinSavePageNo = 2;
                    this.mMaxSavaPageNo = this.mMinSavePageNo + 3;
                    break;
                case 8:
                    this.mMinSavePageNo = 2;
                    this.mMaxSavaPageNo = this.mMinSavePageNo + 3;
                    break;
                case 9:
                    this.mMinSavePageNo = 3;
                    this.mMaxSavaPageNo = this.mMinSavePageNo + 3;
                    break;
                default:
                    this.mMaxSavaPageNo = 0;
                    this.mMinSavePageNo = 0;
                    break;
            }
            for (int index = this.mMinSavePageNo; index <= this.mMaxSavaPageNo; index++) {
                this.mPageMustSaveFlag |= 1 << index;
            }
        }
    }

    private boolean setCmdSaveFlag(DisplayListCanvas cv) {
        if (cv == null || this.mPageNo < this.mMinSavePageNo || this.mPageNo > this.mMaxSavaPageNo || this.mMinSavePageNo == this.mMaxSavaPageNo) {
            return false;
        }
        Paint paint = new Paint();
        paint.setARGB(254, 101, 65, 82);
        cv.drawLine((float) ((this.mPageNo * this.mScreenWidth) + 0), 0.0f, (float) ((this.mPageNo * this.mScreenWidth) + 1), 0.0f, paint);
        paint.setARGB(255, 1, this.mPageNo - this.mMinSavePageNo, this.mPageNum);
        cv.drawLine((float) ((this.mPageNo * this.mScreenWidth) + 2), 0.0f, (float) ((this.mPageNo * this.mScreenWidth) + 3), 0.0f, paint);
        this.mPagesSaveFlag |= 1 << this.mPageNo;
        this.mCtrlBySf = 0;
        Log.v(TAG, "NSDAP : cmd save : screen [ " + this.mPageNo + " ]");
        return true;
    }

    private ViewGroup getViewGroup(View view) {
        if (view == null) {
            return null;
        }
        ViewGroup retVG = null;
        if (view instanceof ViewGroup) {
            retVG = (ViewGroup) view;
        }
        return retVG;
    }

    private boolean canDrawBySf(View view) {
        switch (this.nsdCaseNo) {
            case 0:
                return false;
            case 1:
                if (this.mPagesSaveFlag != this.mPageMustSaveFlag || this.mCtrlBySf == 0) {
                    return false;
                }
                ViewGroup pV = getCaseVGInLauncherIdle(view);
                if (pV == null) {
                    return false;
                }
                this.mCtrlBySf++;
                if (this.mScreenWidth == 0) {
                    this.mScreenWidth = view.getResources().getDisplayMetrics().widthPixels;
                }
                this.mPageNo = Math.round(((float) pV.mScrollX) / ((float) this.mScreenWidth));
                return true;
            default:
                return false;
        }
    }

    private boolean setCmdDrawFlag(DisplayListCanvas cv) {
        if (cv == null || this.mCtrlBySf > 2) {
            return false;
        }
        this.mNeedCheckSFControl = true;
        this.mCheckSFControlCount = 0;
        sendCmdReadyScrollFlag(104);
        Log.e("yubo", "NSD send draw cmd time : " + SystemClock.uptimeMillis());
        Log.v(TAG, "NSDAP : cmd draw : screen [ " + this.mPageNo + " ]");
        return true;
    }

    private void setCmdStopFlag(int stopCmd) {
        sendInterruptCmd(stopCmd);
    }

    private void sendCmdReadyScrollFlag(int readyScrollCmd) {
        sendInterruptCmd(readyScrollCmd);
    }

    private boolean setCmdAdjFlag(String para) {
        if (para == null) {
            return false;
        }
        Log.v(TAG, "NSD setCmdAdjFlag");
        sendInterruptCmd(Integer.parseInt(para));
        return true;
    }

    private void processCase(View view, DisplayListCanvas cv) {
        if (view != null && cv != null) {
            if (canSave(view)) {
                this.mCanSaveBySf = false;
                setCmdSaveFlag(cv);
                return;
            }
            this.mCanSaveBySf = false;
            if (canDrawBySf(view)) {
                setCmdDrawFlag(cv);
            }
        }
    }

    public void setView(View view) {
        this.mView = view;
    }

    public void processCase(DisplayListCanvas cv) {
        processCase(this.mView, cv);
    }

    public boolean isScreenHight(int hight) {
        if (NSD_HIGH_SCREEN_HIGHT == hight || NSD_MIDDLE_SCREEN_HIGHT == hight || NSD_LOW_SCREEN_HIGHT == hight) {
            return true;
        }
        return false;
    }

    public boolean checkAdBlock(View inView, String pkgName) {
        boolean retBlockStatus = false;
        int prop = SystemProperties.getInt("debug.aps.enable", 0);
        if ((prop != 1 && prop != 2 && prop != 9999) || inView == null) {
            return false;
        }
        String viewClsName = inView.getClass().getName();
        if (this.mMapCheckResult.containsKey(viewClsName)) {
            if (1 == ((Integer) this.mMapCheckResult.get(viewClsName)).intValue()) {
                blockAdView(inView);
                retBlockStatus = true;
            }
            return retBlockStatus;
        }
        IEventAnalyzed eventAnalyzed = HwapsWrapper.getEventAnalyzed();
        if (eventAnalyzed != null && eventAnalyzed.isAdCheckEnable(pkgName)) {
            return checkViewAndParent(inView, viewClsName, eventAnalyzed, false);
        }
        return false;
    }

    private boolean checkViewAndParent(View inView, String viewClsName, IEventAnalyzed eventAnalyzed, boolean retBlockStatus) {
        int nCount = 0;
        String clsName = "";
        ViewParent parent = null;
        while (true) {
            if (nCount == 0) {
                clsName = viewClsName;
                parent = inView.getParent();
            } else if (parent != null) {
                clsName = parent.getClass().getName();
                parent = parent.getParent();
            }
            int checkResult = eventAnalyzed.checkAd(clsName);
            if (checkResult <= 0) {
                nCount++;
                if (parent == null || nCount >= 5) {
                    break;
                }
            } else {
                break;
            }
        }
        if (1 == checkResult) {
            blockAdView(inView);
            this.mMapCheckResult.put(viewClsName, Integer.valueOf(1));
            return true;
        } else if (2 == checkResult) {
            unBlockAdView(inView);
            return retBlockStatus;
        } else {
            this.mMapCheckResult.put(viewClsName, Integer.valueOf(0));
            return retBlockStatus;
        }
    }

    private void blockAdView(View inView) {
        if (inView.getVisibility() != 8) {
            inView.setVisibility(8);
            Log.i("AdCheck", "APS: blockAdView! ");
        }
    }

    private void unBlockAdView(View inView) {
        if (inView.getVisibility() == 8) {
            inView.setVisibility(0);
            Log.i("AdCheck", "APS: unBlockAdView! ");
        }
    }

    public void drawBackground(TextView view, Canvas canvas) {
        if (view == null || canvas == null) {
            Log.w(TAG, "NSD drawBackground fail!");
            return;
        }
        ArrayList<View> mViewList = new ArrayList();
        mViewList.clear();
        mViewList.add(view);
        for (View parent = (View) view.getParent(); parent != null; parent = (View) parent.getParent()) {
            mViewList.add(parent);
            if (parent == view.getRootView()) {
                break;
            }
        }
        int[] locationOnScreen = new int[2];
        for (int i = mViewList.size() - 1; i >= 0; i--) {
            View v = (View) mViewList.get(i);
            if (v.getBackground() != null) {
                locationOnScreen[1] = 0;
                locationOnScreen[0] = 0;
                v.getLocationOnScreen(locationOnScreen);
                canvas.save();
                canvas.translate((float) locationOnScreen[0], (float) locationOnScreen[1]);
                v.getBackground().draw(canvas);
                canvas.restore();
            }
        }
    }

    public int getTextViewZOrderId(AttachInfo attachInfo) {
        int zOrder = -1;
        try {
            zOrder = this.mWindowManagerService.getNsdWindowInfo(getCurrentWindowToken(attachInfo));
        } catch (RemoteException e) {
            Log.e(TAG, "RemoteException is thrown when trying to get zid of this layer");
        }
        return zOrder;
    }

    public boolean isCursorCompleteVisible(TextView view, Rect cursorGlobalBounds, AttachInfo mAttachInfo) {
        if (view == null || cursorGlobalBounds == null || mAttachInfo == null || this.mWindowManagerService == null) {
            Log.w(TAG, "NSD isCursorCompleteVisible fail!");
            return false;
        }
        Rect mVisibleRect = new Rect();
        if (!view.getGlobalVisibleRect(mVisibleRect)) {
            return false;
        }
        mVisibleRect.left -= 10;
        if (!mVisibleRect.contains(cursorGlobalBounds)) {
            return false;
        }
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        boolean z;
        try {
            int cwinfo = this.mWindowManagerService.getNsdWindowInfo(getCurrentWindowToken(mAttachInfo));
            if (cwinfo == 0) {
                z = false;
                return z;
            }
            this.mLayerName = this.mWindowManagerService.getNsdWindowTitle(getCurrentWindowToken(mAttachInfo));
            if (this.mLayerName == null || this.mLayerName.isEmpty()) {
                recycleParcel(data, reply);
                return false;
            }
            this.mZorder = cwinfo;
            this.mFirstBlinkCount++;
            IBinder flinger = ServiceManager.getService("SurfaceFlinger");
            if (flinger != null) {
                data.writeInterfaceToken("android.ui.ISurfaceComposer");
                flinger.transact(2001, data, reply, 0);
                long lastRefreshTime = reply.readLong();
                long now = System.nanoTime() / 1000000;
                if (now - lastRefreshTime < 300 || now - this.mLastBlinkTime > 700) {
                    Log.d(TAG, " NSD isCursorCompleteVisible time not match : refresh : " + (now - lastRefreshTime) + " , blink time : " + (now - this.mLastBlinkTime));
                    this.mLastBlinkTime = now;
                    this.mFirstBlinkCount = 0;
                    recycleParcel(data, reply);
                    return false;
                }
                this.mLastBlinkTime = now;
                if (this.mFirstBlinkCount <= 2) {
                    recycleParcel(data, reply);
                    return false;
                }
                int count = reply.readInt();
                boolean overlap = false;
                boolean currentLayerIsVisible = false;
                for (int i = 0; i < count; i++) {
                    int isVisible = reply.readInt();
                    int layer = reply.readInt();
                    int left = reply.readInt();
                    int top = reply.readInt();
                    int right = reply.readInt();
                    int bottom = reply.readInt();
                    if (isVisible != 0 && layer >= cwinfo) {
                        if (layer == cwinfo) {
                            currentLayerIsVisible = true;
                        } else if (Rect.intersects(new Rect(left, top, right, bottom), cursorGlobalBounds)) {
                            overlap = true;
                            break;
                        }
                    }
                }
                if (!currentLayerIsVisible) {
                    Log.w(TAG, "current layer is not visible");
                }
                if (currentLayerIsVisible && !overlap) {
                    recycleParcel(data, reply);
                    return true;
                }
            }
            recycleParcel(data, reply);
            return false;
        } catch (RemoteException e) {
            z = TAG;
            Log.e(z, "isCursorCompleteVisible error");
        } finally {
            recycleParcel(data, reply);
        }
    }

    public boolean isCursorOpaque(TextView view) {
        if (view == null) {
            Log.w(TAG, "NSD isCursorOpaque fail!");
            return false;
        }
        View v = view;
        while (v.getAlpha() >= HwFragmentMenuItemView.ALPHA_NORMAL) {
            if (v != view.getRootView()) {
                v = (View) v.getParent();
                if (v == null) {
                }
            }
            return true;
        }
        return false;
    }

    public int getDisplayOrientation(TextView view) {
        if (view != null) {
            return view.getResources().getConfiguration().orientation;
        }
        Log.w(TAG, "NSD getDisplayOrientation fail!");
        return -1;
    }

    public boolean drawBitmapCursor(int refreshTimes, TextView txView, Rect cursorbounds) {
        if (txView == null || cursorbounds == null || cursorbounds.width() <= 0 || cursorbounds.height() <= 0) {
            Log.w(TAG, "NSD drawBitmapCursor fail!");
            return false;
        }
        String curPkgName = txView.getContext().getPackageName();
        if ((curPkgName.equals("com.sina.weibo") || curPkgName.equals("com.tencent.mm")) && cursorbounds.width() < 3) {
            cursorbounds.left--;
            cursorbounds.right++;
        }
        this.mCursorPosition[0] = 0;
        this.mCursorPosition[1] = 0;
        txView.getLocationOnScreen(this.mCursorPosition);
        int[] iArr = this.mCursorPosition;
        iArr[0] = iArr[0] + ((txView.getTotalPaddingLeft() + cursorbounds.left) - txView.mScrollX);
        iArr = this.mCursorPosition;
        iArr[1] = iArr[1] + (txView.getTotalPaddingTop() + cursorbounds.top);
        Bitmap mCursorBmp = Bitmap.createBitmap(cursorbounds.width(), cursorbounds.height(), Config.ARGB_8888);
        if (mCursorBmp == null) {
            Log.w(TAG, "NSD mCursorBmp is null!");
            return false;
        }
        Canvas mCursorCanvas = new Canvas(mCursorBmp);
        boolean isUseHintLayout = false;
        mCursorCanvas.save();
        mCursorCanvas.translate((float) (-this.mCursorPosition[0]), (float) (-this.mCursorPosition[1]));
        if ((txView.getText() == null || txView.getText().toString().equals("")) && txView.getHint() != null) {
            isUseHintLayout = true;
        }
        drawBackground(txView, mCursorCanvas);
        mCursorCanvas.restore();
        mCursorCanvas.save();
        mCursorCanvas.translate((float) (-cursorbounds.left), (float) (-cursorbounds.top));
        mCursorCanvas.clipRect(cursorbounds);
        txView.editorUpdate(mCursorCanvas, isUseHintLayout);
        mCursorCanvas.restore();
        boolean ret = sendCursorBmpToSF(refreshTimes, this.mCursorPosition[0], this.mCursorPosition[1], mCursorBmp);
        mCursorBmp.recycle();
        return ret;
    }

    public boolean isCursorBlinkCase(TextView txView, Rect cursorbounds) {
        if (txView == null || cursorbounds == null || cursorbounds.width() <= 0 || cursorbounds.height() <= 0) {
            Log.w(TAG, "NSD isCursorBlinkCase fail!");
            return false;
        } else if (txView.mScrollY != 0) {
            Log.d(TAG, "txView.mScrollX or mScrollX not match case return");
            return false;
        } else if (!isCurPkgNameMatched(txView.getContext().getPackageName())) {
            return false;
        } else {
            this.mCursorPosition[0] = 0;
            this.mCursorPosition[1] = 0;
            txView.getLocationOnScreen(this.mCursorPosition);
            int[] iArr = this.mCursorPosition;
            iArr[0] = iArr[0] + ((txView.getTotalPaddingLeft() + cursorbounds.left) - txView.mScrollX);
            iArr = this.mCursorPosition;
            iArr[1] = iArr[1] + (txView.getTotalPaddingTop() + cursorbounds.top);
            if (this.mLastX == this.mCursorPosition[0] && this.mLastY == this.mCursorPosition[1]) {
                return isCursorCompleteVisible(txView, new Rect(this.mCursorPosition[0], this.mCursorPosition[1], this.mCursorPosition[0] + cursorbounds.width(), this.mCursorPosition[1] + cursorbounds.height()), txView.getAttachInfo()) && isCursorOpaque(txView) && getDisplayOrientation(txView) == 1;
            } else {
                this.mLastX = this.mCursorPosition[0];
                this.mLastY = this.mCursorPosition[1];
                Log.d(TAG, "cursor position changed");
                return false;
            }
        }
    }

    private boolean isCurPkgNameMatched(String curPkgName) {
        if (curPkgName == null) {
            Log.d(TAG, "curPkgName is null");
            return false;
        }
        boolean equals = (curPkgName.equals("com.android.contacts") || curPkgName.equals("com.android.mms") || curPkgName.equals(ConnectPermission.MMS_PACKAGE) || curPkgName.equals("com.android.email") || curPkgName.equals("com.sina.weibo") || curPkgName.equals("com.sina.weibog3")) ? true : curPkgName.equals("com.tencent.mm");
        if (!equals) {
            Log.d(TAG, "curPkgName is not in list");
            return false;
        } else if (curPkgName.equals(this.mOldPkgName)) {
            return true;
        } else {
            this.mOldPkgName = curPkgName;
            Log.d(TAG, "mOldPkgName! = curPkgName!");
            return false;
        }
    }

    public boolean sendCursorBmpToSF(int refreshTimes, int x, int y, Bitmap bmp) {
        if (bmp == null) {
            Log.w(TAG, "sendCursorBmpToSF bmp is null");
            return false;
        }
        Parcel data = Parcel.obtain();
        try {
            IBinder flinger = ServiceManager.getService("SurfaceFlinger");
            if (flinger != null) {
                data.writeInterfaceToken("android.ui.ISurfaceComposer");
                int width = bmp.getWidth();
                int height = bmp.getHeight();
                int len = bmp.getByteCount();
                data.writeInt(refreshTimes);
                data.writeString(this.mLayerName);
                data.writeInt(this.mZorder);
                data.writeInt(x);
                data.writeInt(y);
                data.writeInt(width);
                data.writeInt(height);
                data.writeInt(len);
                data.writeInt(6);
                ByteBuffer bb = ByteBuffer.allocate(bmp.getByteCount());
                bmp.copyPixelsToBuffer(bb);
                data.writeByteArray(bb.array());
                flinger.transact(2000, data, null, 0);
                return true;
            }
            Log.e(TAG, "Error! get service:SurfaceFlinger return null!");
            recycleParcel(data, null);
            return false;
        } catch (RemoteException e) {
            Log.e(TAG, "Error! call finger.transact throws RemoteException!");
        } finally {
            recycleParcel(data, null);
        }
    }

    public IBinder getCurrentWindowToken(AttachInfo mAttachInfo) {
        return mAttachInfo.mWindowToken;
    }

    public void setCurrentDrawTime(long currentDrawTime) {
    }

    public void setIsDrawCursor(int isDrawCursor) {
    }

    private void recycleParcel(Parcel data, Parcel reply) {
        if (data != null) {
            data.recycle();
        }
        if (reply != null) {
            reply.recycle();
        }
    }

    public void adaptPowerSave(Context context, MotionEvent event) {
        createEventAnalyzed();
        if (this.mEventAnalyzed != null && isSupportAps()) {
            this.mEventAnalyzed.processAnalyze(context, event.getAction(), event.getEventTime(), (int) event.getX(), (int) event.getY(), event.getPointerCount(), event.getDownTime());
        }
    }

    public void initAPS(Context context, int screenWidth, int myPid) {
        if (this.mIsFirstEnterAPS) {
            createEventAnalyzed();
            if (this.mEventAnalyzed != null) {
                this.mEventAnalyzed.initAPS(context, screenWidth, myPid);
            }
            this.mIsFirstEnterAPS = false;
        }
    }

    public synchronized void createEventAnalyzed() {
        if (this.mEventAnalyzed == null) {
            this.mEventAnalyzed = HwapsWrapper.getEventAnalyzed();
        }
    }

    public boolean isGameProcess(String pkgName) {
        createEventAnalyzed();
        if (this.mEventAnalyzed != null) {
            return this.mEventAnalyzed.isGameProcess(pkgName);
        }
        return false;
    }

    public boolean isSupportAps() {
        return SystemProperties.getInt("sys.aps.support", 0) > 0;
    }

    public boolean isSupportAPSEventAnalysis() {
        return 1 == (SystemProperties.getInt("sys.aps.support", 0) & 1);
    }

    public boolean isAPSReady() {
        if (this.mEventAnalyzed == null) {
            this.mEventAnalyzed = HwapsWrapper.getEventAnalyzed();
        }
        if (this.mEventAnalyzed != null) {
            return this.mEventAnalyzed.isAPSReady();
        }
        return false;
    }

    public void powerCtroll() {
        if (this.mFpsController == null) {
            this.mFpsController = HwapsWrapper.getFpsController();
        } else {
            this.mFpsController.powerCtroll();
        }
    }

    public void setAPSOnPause() {
        if (this.mEventAnalyzed == null) {
            this.mEventAnalyzed = HwapsWrapper.getEventAnalyzed();
        }
        if (this.mEventAnalyzed != null) {
            this.mEventAnalyzed.setHasOnPaused(true);
        }
    }

    private boolean isNsdLauncherCase(View view) {
        if (checkIfNsdSupportLauncher() && isCase(view) && !isNeedAppDraw()) {
            return true;
        }
        return false;
    }

    public boolean StopSdrForSpecial(String strinfo, int keycode) {
        if (this.mEventAnalyzed == null) {
            this.mEventAnalyzed = HwapsWrapper.getEventAnalyzed();
        }
        if (this.mEventAnalyzed != null) {
            return this.mEventAnalyzed.StopSdrForSpecial(strinfo, keycode);
        }
        Log.e(TAG, "APS: SDR: mEventAnalyzed is null");
        return false;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean checkIs2DSDRCase(Context context, ViewRootImpl viewRoot) {
        String APP_NAME_WILD_CARD_CHAR = "*";
        if (viewRoot == null || context == null) {
            Log.i("2DSDR", "APS: 2DSDR: AbsListView.java, viewRoot or context is null");
            return false;
        }
        ApplicationInfo info = context.getApplicationInfo();
        if (info == null || info.targetSdkVersion < 14 || (SystemProperties.getInt("sys.aps.support", 0) & 4096) == 0) {
            return false;
        }
        float startRatio = Float.parseFloat(SystemProperties.get("sys.2dsdr.startratio", "-1.0"));
        if (startRatio <= 0.0f || HwFragmentMenuItemView.ALPHA_NORMAL <= startRatio || viewRoot.getView() == null) {
            return false;
        }
        Display display = viewRoot.getView().getDisplay();
        if (display == null || display.getDisplayId() != 0) {
            return false;
        }
        String appPkgName = context.getApplicationInfo().packageName;
        String targetPkgName = SystemProperties.get("sys.2dsdr.pkgname", "");
        if (targetPkgName.equals("*") || targetPkgName.equals(appPkgName)) {
            return true;
        }
        return false;
    }

    private float computeSDRStartRatio(Context context, View rootView, View scrollView) {
        float areaRatio = (float) ((((double) (scrollView.getWidth() * scrollView.getHeight())) * 1.0d) / ((double) (rootView.getWidth() * rootView.getHeight())));
        if (((double) areaRatio) < 0.7d) {
            return -1.0f;
        }
        float startRatio = Float.parseFloat(SystemProperties.get("sys.2dsdr.startratio", "-1.0"));
        if (0.0f >= startRatio || startRatio >= HwFragmentMenuItemView.ALPHA_NORMAL) {
            return (float) (240.0d / ((double) (context.getResources().getDisplayMetrics().ydpi * areaRatio)));
        }
        return startRatio;
    }

    private float doComputeSDRRatioChange(float startRatio, float initialVelocity, float currentVelocity, int ratioBase) {
        int startNum = (int) Math.ceil((double) (((float) ratioBase) * startRatio));
        if (startNum == ratioBase) {
            return -1.0f;
        }
        return (float) ((((double) (startNum + ((int) (0.5d + (((double) (Math.abs(initialVelocity) - Math.abs(currentVelocity))) / ((double) (Math.abs(initialVelocity) / ((float) (ratioBase - startNum))))))))) * 1.0d) / ((double) ratioBase));
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public float computeSDRRatio(Context context, View rootView, View scrollView, float initialVelocity, float currentVelocity, int ratioBase) {
        float startRatio = computeSDRStartRatio(context, rootView, scrollView);
        if (startRatio < 0.0f || HwFragmentMenuItemView.ALPHA_NORMAL < startRatio || ratioBase <= 1) {
            return -1.0f;
        }
        return doComputeSDRRatioChange(startRatio, initialVelocity, currentVelocity, ratioBase);
    }

    public int computeSDRRatioBase(Context context, View viewRoot, View scrollView) {
        if (viewRoot.getWidth() == scrollView.getWidth() && viewRoot.getHeight() == scrollView.getHeight()) {
            for (int ratioBase = 16; ratioBase > 1; ratioBase--) {
                if (viewRoot.getHeight() % ratioBase == 0) {
                    return ratioBase;
                }
            }
        } else {
            float density = context.getResources().getDisplayMetrics().density;
            if (((int) (10.0f * density)) % 10 == 0) {
                return (int) density;
            }
        }
        return -1;
    }

    public void setContext(Context context) {
        this.mContext = context;
    }

    public String[] getCustAppList(int type) {
        IEventAnalyzed eventAnalyzed = HwapsWrapper.getEventAnalyzed();
        String[] result = new String[0];
        if (eventAnalyzed != null && this.mContext != null) {
            return eventAnalyzed.getCustAppList(this.mContext, type);
        }
        Log.w(TAG, "APS: SDR: Customized2d: getCustApplist eventAnalyzed null");
        return result;
    }

    public int getCustScreenDimDurationLocked(int screenOffTimeout) {
        IEventAnalyzed eventAnalyzed = HwapsWrapper.getEventAnalyzed();
        if (eventAnalyzed != null) {
            return eventAnalyzed.getCustScreenDimDurationLocked(screenOffTimeout);
        }
        Log.w(TAG, "APS: Screen Dim: getCustScreenDimDuration eventAnalyzed null");
        return -1;
    }

    public String[] getCustAppList(Context context, int type) {
        IEventAnalyzed eventAnalyzed = HwapsWrapper.getEventAnalyzed();
        String[] result = new String[0];
        if (eventAnalyzed != null && context != null) {
            return eventAnalyzed.getCustAppList(context, type);
        }
        Log.w(TAG, "APS: SDR: Customized2d: getCustApplist eventAnalyzed null");
        return result;
    }

    public String[] getQueryResultGameList(Context context, int type) {
        IEventAnalyzed eventAnalyzed = HwapsWrapper.getEventAnalyzed();
        String[] result = new String[0];
        if (eventAnalyzed != null && context != null) {
            return eventAnalyzed.getQueryResultGameList(context, type);
        }
        Log.w(TAG, "APS: SDR: HwNsdImp: getQueryResultGameList eventAnalyzed null or context is null.");
        return result;
    }

    public void setLowResolutionMode(Context context, boolean enableLowResolutionMode) {
        int i = 0;
        Log.i("sdr", "APS: SDR: HwNsdImpl.setLowResolutionMod, enableLowResolutionMode = " + enableLowResolutionMode);
        String[] queryResultList = getQueryResultGameList(context, CONFIGTYPE_QUERYRESULTLIST);
        ActivityManager am = (ActivityManager) context.getSystemService(FreezeScreenScene.ACTIVITY_PARAM);
        int length;
        String packageName;
        if (enableLowResolutionMode) {
            String[] blackListCompatModeAppsArray = getCustAppList(context, CONFIGTYPE_BLACKLIST);
            ArrayList<String> compatModeBlackListApps = new ArrayList();
            for (String tmp : blackListCompatModeAppsArray) {
                compatModeBlackListApps.add(tmp);
            }
            length = queryResultList.length;
            while (i < length) {
                packageName = queryResultList[i];
                if (!compatModeBlackListApps.contains(packageName)) {
                    am.setPackageScreenCompatMode(packageName, 1);
                }
                i++;
            }
            return;
        }
        String[] whiteListCompatModeAppsArray = getCustAppList(context, CONFIGTYPE_WHITELIST);
        ArrayList<String> compatModeWhiteListApps = new ArrayList();
        for (String tmp2 : whiteListCompatModeAppsArray) {
            compatModeWhiteListApps.add(tmp2);
        }
        for (String packageName2 : queryResultList) {
            if (!compatModeWhiteListApps.contains(packageName2)) {
                am.setPackageScreenCompatMode(packageName2, 0);
            }
        }
    }

    public boolean isLowResolutionSupported() {
        if ((32768 & SystemProperties.getInt("sys.aps.support", 0)) != 0) {
            return true;
        }
        return false;
    }
}
