package com.android.server;

import android.app.ActivityManager;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemProperties;
import android.util.Slog;
import huawei.android.os.IHwAntiTheftManager.Stub;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

public class HwAntiTheftService extends Stub {
    private static final int ASCII_ONE = 49;
    private static final int ASCII_ZERO = 48;
    private static final boolean DEBUG_EXE = false;
    private static final boolean DEBUG_TRANSMISSION = false;
    public static final int DIGEST_SIZE_BYTES = 32;
    private static final int MAX_BYTE_BUFFER_SIZE = 1024;
    private static final int MSG_BOOT_COMPLETED = 1;
    private static final int MSG_CANCEL_NOTIFICATION = 3;
    private static final int MSG_MESSAGE_LOG = 4;
    private static final int MSG_WIPE_ANTITHEFTDATA = 2;
    static final String TAG = "HwAntiTheftService";
    private static final int TYPE_ALL_PARTITION = 3;
    private static final int TYPE_DATA_PARTITION = 1;
    private static final int TYPE_SWITCH_PARTITION = 2;
    private static boolean mIsAntiTheftSupportedState;
    private Context mContext;
    private AntiTheftHandler mHandler;
    private final ServiceThread mHandlerThread;
    private final Object mLock = new Object();
    private NotificationManager mNotificationManager;

    private static final class AntiTheftHandler extends Handler {
        public AntiTheftHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 4:
                    Slog.d(HwAntiTheftService.TAG, "buffer data:" + ((String) msg.obj));
                    return;
                default:
                    Slog.d(HwAntiTheftService.TAG, "Receive default");
                    return;
            }
        }
    }

    private static native boolean checkBootloaderLock() throws IOException;

    private static native int getDataBlockSize(int i) throws IOException;

    private static native int operateBootloaderLock(boolean z) throws IOException;

    private static native int read(int i, byte[] bArr) throws IOException;

    private static native int wipe(int i) throws IOException;

    private static native int write(int i, byte[] bArr, int i2) throws IOException;

    static {
        mIsAntiTheftSupportedState = true;
        try {
            System.loadLibrary("AntiTheftService");
        } catch (UnsatisfiedLinkError e) {
            mIsAntiTheftSupportedState = false;
            Slog.e(TAG, "Load antitheft libarary failed >>>>>" + e);
        }
    }

    public HwAntiTheftService(Context context) {
        this.mContext = context;
        this.mNotificationManager = (NotificationManager) this.mContext.getSystemService("notification");
        this.mHandlerThread = new ServiceThread(TAG, -4, false);
        this.mHandlerThread.start();
        this.mHandler = new AntiTheftHandler(this.mHandlerThread.getLooper());
    }

    private void printDebugArrayLog(byte[] ViewBuffer, int length) {
        Message m = Message.obtain(this.mHandler, 4);
        m.obj = new String(ViewBuffer, 0, length, Charset.defaultCharset());
        this.mHandler.sendMessage(m);
    }

    public byte[] readAntiTheftData() {
        this.mContext.enforceCallingOrSelfPermission("com.huawei.android.permission.ANTITHEFT", null);
        if (ActivityManager.isUserAMonkey()) {
            Slog.d(TAG, "ignoring monkey's attempt to call read anti");
            return null;
        }
        int maxByteBufferSize = getDataBlockSizeNative(1);
        if (maxByteBufferSize == 0) {
            Slog.e(TAG, "get data block size error!");
            return null;
        }
        byte[] readBuffer = new byte[maxByteBufferSize];
        int length = readBufferFromNative(1, readBuffer);
        byte[] retAarray = new byte[length];
        if (length <= 0) {
            return null;
        }
        System.arraycopy(readBuffer, 0, retAarray, 0, length);
        return retAarray;
    }

    public int writeAntiTheftData(byte[] writeToNative) {
        this.mContext.enforceCallingOrSelfPermission("com.huawei.android.permission.ANTITHEFT", null);
        if (ActivityManager.isUserAMonkey()) {
            Slog.d(TAG, "ignoring monkey's attempt to call write anti");
            return 0;
        } else if (writeToNative == null) {
            return 0;
        } else {
            if (writeToNative.length == 0 || writeToNative.length > getDataBlockSizeNative(1)) {
                Slog.e(TAG, "write too long data!!");
                return 0;
            }
            if (writeToNative.length > 0) {
                Slog.d(TAG, "HwAntiTheftService writeAntiTheftData length = " + writeToNative.length);
            } else {
                Slog.d(TAG, "HwAntiTheftService writeAntiTheftData length = " + writeToNative.length);
            }
            return writeBufferToNative(1, writeToNative, writeToNative.length);
        }
    }

    public int wipeAntiTheftData() {
        this.mContext.enforceCallingOrSelfPermission("com.huawei.android.permission.ANTITHEFT", null);
        if (ActivityManager.isUserAMonkey()) {
            Slog.d(TAG, "ignoring monkey's attempt to call wipe anti");
            return -1;
        } else if (wipeDataNative(3) == -1) {
            return -1;
        } else {
            return 0;
        }
    }

    public int getAntiTheftDataBlockSize() {
        this.mContext.enforceCallingOrSelfPermission("com.huawei.android.permission.ANTITHEFT", null);
        if (!ActivityManager.isUserAMonkey()) {
            return getDataBlockSizeNative(1);
        }
        Slog.d(TAG, "ignoring monkey's attempt to call get anti block size");
        return 0;
    }

    public int setAntiTheftEnabled(boolean enable) {
        int i = 0;
        this.mContext.enforceCallingOrSelfPermission("com.huawei.android.permission.ANTITHEFT", null);
        if (ActivityManager.isUserAMonkey()) {
            Slog.d(TAG, "ignoring monkey's attempt to call set anti enable");
            return -1;
        }
        int retSet = 0;
        try {
            ByteBuffer data = ByteBuffer.allocate(1);
            data.put(enable ? (byte) 49 : (byte) 48);
            data.flip();
            retSet = writeBufferToNative(2, data.array(), 1);
        } catch (Exception e) {
        }
        if (!enable) {
            wipeDataNative(2);
        }
        operateBootloaderLockNative(enable);
        if (retSet <= 0) {
            i = -1;
        }
        return i;
    }

    public boolean getAntiTheftEnabled() {
        this.mContext.enforceCallingOrSelfPermission("com.huawei.android.permission.ANTITHEFT", null);
        boolean enable = false;
        if (ActivityManager.isUserAMonkey()) {
            Slog.d(TAG, "ignoring monkey's attempt to call get anti enable");
            return false;
        }
        byte[] readBuffer = new byte[1];
        if (readBufferFromNative(2, readBuffer) > 0 && readBuffer[0] == (byte) 49) {
            enable = true;
        }
        return enable;
    }

    public boolean checkRootState() {
        this.mContext.enforceCallingOrSelfPermission("com.huawei.android.permission.ANTITHEFT", null);
        boolean ret = false;
        if (ActivityManager.isUserAMonkey()) {
            Slog.d(TAG, "ignoring monkey's attempt to call check root state");
            return false;
        }
        String rootState = SystemProperties.get("huawei.check_root.hotapermit", "safe");
        Slog.d(TAG, "root state :" + rootState);
        if ("safe".equals(rootState)) {
            ret = false;
        } else if ("risk".equals(rootState)) {
            ret = true;
        }
        return ret;
    }

    public boolean isAntiTheftSupported() {
        this.mContext.enforceCallingOrSelfPermission("com.huawei.android.permission.ANTITHEFT", null);
        if (!ActivityManager.isUserAMonkey()) {
            return mIsAntiTheftSupportedState;
        }
        Slog.d(TAG, "ignoring monkey's attempt to call if support anti theft");
        return false;
    }

    private int readBufferFromNative(int type, byte[] readData) {
        int ret = 0;
        try {
            synchronized (this.mLock) {
                ret = read(type, readData);
            }
        } catch (UnsatisfiedLinkError e) {
            Slog.e(TAG, "libarary hwantiTheft read from native failed >>>>>" + e);
        } catch (IOException e2) {
        }
        return ret;
    }

    private int writeBufferToNative(int type, byte[] wrtieData, int length) {
        int ret = 0;
        try {
            synchronized (this.mLock) {
                ret = write(type, wrtieData, length);
            }
        } catch (UnsatisfiedLinkError e) {
            Slog.e(TAG, "libarary hwantiTheft write to native failed >>>>>" + e);
        } catch (IOException e2) {
        }
        return ret;
    }

    private int wipeDataNative(int type) {
        try {
            int wipe;
            synchronized (this.mLock) {
                wipe = wipe(type);
            }
            return wipe;
        } catch (UnsatisfiedLinkError e) {
            Slog.e(TAG, "libarary hwantiTheft wipe native failed >>>>>" + e);
            return -1;
        } catch (IOException e2) {
            return -1;
        }
    }

    private int getDataBlockSizeNative(int type) {
        int size = 0;
        try {
            synchronized (this.mLock) {
                size = getDataBlockSize(type);
            }
        } catch (UnsatisfiedLinkError e) {
            Slog.e(TAG, "libarary hwantiTheft get block size native failed >>>>>" + e);
        } catch (IOException e2) {
        }
        return size;
    }

    private boolean checkBootloaderLockNative() {
        try {
            boolean checkBootloaderLock;
            synchronized (this.mLock) {
                checkBootloaderLock = checkBootloaderLock();
            }
            return checkBootloaderLock;
        } catch (UnsatisfiedLinkError e) {
            Slog.e(TAG, "libarary hwantiTheft check boot lock failed >>>>>" + e);
            return false;
        } catch (IOException e2) {
            return false;
        }
    }

    private int operateBootloaderLockNative(boolean enable) {
        try {
            int operateBootloaderLock;
            synchronized (this.mLock) {
                operateBootloaderLock = operateBootloaderLock(enable);
            }
            return operateBootloaderLock;
        } catch (UnsatisfiedLinkError e) {
            Slog.e(TAG, "libarary hwantiTheft opt boot lock  failed >>>>>" + e);
            return -1;
        } catch (IOException e2) {
            return -1;
        }
    }
}
