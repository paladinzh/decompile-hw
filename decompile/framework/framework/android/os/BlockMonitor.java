package android.os;

import android.app.ActivityThread;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.net.ProxyInfo;
import android.util.Log;
import java.util.HashMap;
import java.util.Map;

public class BlockMonitor {
    private static final int BUG_TYPE = 104;
    private static final boolean DEBUG_MAIN_THREAD_BLOCK;
    private static final long HALF_MAIN_THREAD_MESSAGE_LIMIT = 150;
    private static final long MAIN_THREAD_BLOCK_TIMEOUT = 4000;
    private static final String MAIN_THREAD_MESSAGE_CALLBACK = "android.app.LoadedApk$ReceiverDispatcher$Args";
    private static final long MAIN_THREAD_MESSAGE_LIMIT = 300;
    private static final int SCENE_TYPE_BINDER_BLOCK = 2903;
    private static final int SCENE_TYPE_INPUT_BLOCK = 2904;
    private static final int SCENE_TYPE_MESSAGE_BLOCK = 2901;
    private static final int SCENE_TYPE_MESSAGE_COUNT_BLOCK = 2902;
    private static final String TAG = "BlockMonitor";
    private static final int TOP_MESSAGE_TO_REPORT = 3;
    private static volatile Thread sMainThread = null;
    private static boolean sReportMessageCount = false;

    private static class BlockRadar {
        private static final long AUTO_UPLOAD_MIN_INTERVAL_TIME = 43200000;
        private static final String CATEGORY_PREFIX = "app-";
        private static final int LEVEL_B = 66;
        private static final int LOG_MASK = 31;
        private static long sLastAutoUploadTime = 0;

        private BlockRadar() {
        }

        public static void upload(int bugType, int sceneDef, String msg) {
            Log.w(BlockMonitor.TAG, msg);
        }
    }

    private static class MessageInfo {
        public int mCount;
        public String mKey;

        public MessageInfo(String key, int count) {
            this.mKey = key;
            this.mCount = count;
        }
    }

    static {
        boolean z = true;
        if (SystemProperties.getInt("ro.logsystem.usertype", 1) != 3) {
            z = false;
        }
        DEBUG_MAIN_THREAD_BLOCK = z;
    }

    public static boolean isNeedMonitor() {
        return isDebugMainThreadBlock() ? isInMainThread() : false;
    }

    private static boolean isDebugMainThreadBlock() {
        return DEBUG_MAIN_THREAD_BLOCK;
    }

    private static boolean isInMainThread() {
        boolean z = true;
        if (sMainThread != null) {
            if (sMainThread != Thread.currentThread()) {
                z = false;
            }
            return z;
        } else if (Process.myPid() != Process.myTid()) {
            return false;
        } else {
            sMainThread = Thread.currentThread();
            return true;
        }
    }

    public static void checkMessageTime(long startTime, Message message) {
        long processTime = SystemClock.uptimeMillis() - startTime;
        if (processTime >= MAIN_THREAD_BLOCK_TIMEOUT) {
            StringBuilder sb = new StringBuilder();
            sb.append("Package name: ");
            sb.append(getPackageName());
            sb.append("\n");
            sb.append("The Message");
            sb.append(dumpMessage(message));
            sb.append(" took ");
            sb.append(processTime);
            sb.append("ms.");
            BlockRadar.upload(104, SCENE_TYPE_MESSAGE_BLOCK, sb.toString());
        }
    }

    public static void checkInputTime(long startTime) {
        long processTime = SystemClock.uptimeMillis() - startTime;
        if (processTime >= MAIN_THREAD_BLOCK_TIMEOUT) {
            StringBuilder sb = new StringBuilder();
            sb.append("Package name: ");
            sb.append(getPackageName());
            sb.append("\n");
            sb.append("The input took ");
            sb.append(processTime);
            sb.append("ms.");
            sb.append("\n");
            BlockRadar.upload(104, SCENE_TYPE_INPUT_BLOCK, sb.toString());
        }
    }

    public static void checkBinderTime(long startTime) {
        long processTime = SystemClock.uptimeMillis() - startTime;
        if (processTime >= MAIN_THREAD_BLOCK_TIMEOUT) {
            StringBuilder sb = new StringBuilder();
            sb.append("Package name: ");
            sb.append(getPackageName());
            sb.append("\n");
            sb.append("The binder calling took ");
            sb.append(processTime);
            sb.append("ms.");
            sb.append("\n");
            StackTraceElement[] elements = new Throwable().getStackTrace();
            for (StackTraceElement element : elements) {
                sb.append(element);
                sb.append("\n");
            }
            BlockRadar.upload(104, 2903, sb.toString());
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static void checkMessageCount(MessageQueue messageQueue) {
        Throwable th;
        synchronized (messageQueue) {
            try {
                if (sReportMessageCount) {
                    if (((long) messageQueue.mMessageCount) < HALF_MAIN_THREAD_MESSAGE_LIMIT) {
                        Log.d(TAG, "Message queue size is below 150");
                        sReportMessageCount = false;
                    }
                } else if (((long) messageQueue.mMessageCount) >= MAIN_THREAD_MESSAGE_LIMIT) {
                    Log.d(TAG, "Message queue size is too large!");
                    sReportMessageCount = true;
                    StringBuilder sb = new StringBuilder();
                    try {
                        sb.append("Package name: ");
                        sb.append(getPackageName());
                        sb.append("\n");
                        sb.append("Message queue size is too large: ");
                        sb.append(messageQueue.mMessageCount);
                        sb.append("\n");
                        int n = 0;
                        Map<String, MessageInfo> msgMap = new HashMap();
                        try {
                            for (Message msg = messageQueue.mMessages; msg != null; msg = msg.next) {
                                String key = dumpMessage(msg);
                                MessageInfo info = (MessageInfo) msgMap.get(key);
                                if (info != null) {
                                    info.mCount++;
                                } else {
                                    msgMap.put(key, new MessageInfo(key, 1));
                                }
                                n++;
                            }
                            if (n != messageQueue.mMessageCount) {
                                Log.w(TAG, "Message queue size is not correct, count: " + messageQueue.mMessageCount + ", real size: " + n);
                            }
                        } catch (Throwable th2) {
                            th = th2;
                            Map<String, MessageInfo> map = msgMap;
                        }
                    } catch (Throwable th3) {
                        th = th3;
                        throw th;
                    }
                }
            } catch (Throwable th4) {
                th = th4;
                throw th;
            }
        }
    }

    private static String dumpMessage(Message message) {
        StringBuilder b = new StringBuilder();
        b.append("{");
        if (message.target != null) {
            if (message.callback != null) {
                try {
                    String callbackClass = message.callback.getClass().getName();
                    b.append(" callback=");
                    b.append(callbackClass);
                    if (MAIN_THREAD_MESSAGE_CALLBACK.equals(callbackClass)) {
                        b.append(message.callback.toString());
                    }
                } catch (Exception e) {
                    Log.i(TAG, "Could not get Class Name", e);
                }
            } else {
                b.append(" what=");
                b.append(message.what);
            }
            if (message.arg1 != 0) {
                b.append(" arg1=");
                b.append(message.arg1);
            }
            if (message.arg2 != 0) {
                b.append(" arg2=");
                b.append(message.arg2);
            }
            if (message.obj != null) {
                b.append(" obj=");
                b.append(message.obj);
            }
            b.append(" target=");
            b.append(message.target.getClass().getName());
        } else {
            b.append(" barrier=");
            b.append(message.arg1);
        }
        b.append(" }");
        return b.toString();
    }

    private static String getPackageName() {
        String packageName = ActivityThread.currentPackageName();
        if (packageName == null) {
            return "system_server";
        }
        return packageName;
    }

    private static int getVersionCode() {
        if (ActivityThread.currentApplication() != null) {
            return ActivityThread.currentApplication().getApplicationContext().getApplicationInfo().versionCode;
        }
        return 0;
    }

    private static String getVersionName() {
        try {
            if (ActivityThread.currentApplication() != null) {
                Context context = ActivityThread.currentApplication().getApplicationContext();
                if (context != null) {
                    PackageInfo info = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
                    if (!(info == null || info.versionName == null)) {
                        return info.versionName;
                    }
                }
            }
        } catch (Exception e) {
            Log.w(TAG, "Could not get package info", e);
        }
        return getVersionCode() + ProxyInfo.LOCAL_EXCL_LIST;
    }
}
