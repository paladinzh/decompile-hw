package com.huawei.systemmanager.util.content;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import com.huawei.systemmanager.util.HwLog;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class HsmIntentService extends Service {
    private static final int CORE_POOL_SIZE = (CPU_COUNT + 3);
    private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();
    private static final ThreadPoolExecutor HSM_THREAD_POOL_EXECUTOR = new ThreadPoolExecutor(CORE_POOL_SIZE, MAXIMUM_POOL_SIZE, 1, TimeUnit.SECONDS, sPoolWorkQueue, sThreadFactory);
    private static final int KEEP_ALIVE = 1;
    private static final int KEEP_SERVICE_ALIVE_IDLE_TIME = 1000;
    private static final int KEEP_SERVICE_ALIVE_TIME = 50;
    private static final int MAXIMUM_POOL_SIZE = (CPU_COUNT + 10);
    private static final String TAG = "HsmIntentService";
    private static final BlockingQueue<Runnable> sPoolWorkQueue = new LinkedBlockingQueue(256);
    private static final ThreadFactory sThreadFactory = new ThreadFactory() {
        private final AtomicInteger mCount = new AtomicInteger(1);

        public Thread newThread(Runnable r) {
            return new Thread(r, "HsmIntentServiceTask #" + this.mCount.getAndIncrement());
        }
    };
    private Handler mHandler = new H();

    private static class ClazzCache {
        private static final HashMap<String, Class<?>> sClazz = new HashMap();

        private ClazzCache() {
        }

        public static synchronized void cacheClazz(String name, Class<?> clazz) {
            synchronized (ClazzCache.class) {
                sClazz.put(name, clazz);
            }
        }

        public static Class<?> getClazz(String name) {
            return (Class) sClazz.get(name);
        }
    }

    class H extends Handler {
        public static final int STOP_SERVICE = 1;

        H() {
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    HsmIntentService.this.tryToStopSelf();
                    break;
            }
            super.handleMessage(msg);
        }
    }

    class Work implements Runnable {
        private Intent mIntent;
        private String mTargeClass;

        public Work(Intent intent, String className) {
            this.mIntent = intent;
            this.mTargeClass = className;
        }

        public void run() {
            try {
                doBackgroud(HsmIntentService.this.getApplicationContext(), this.mIntent, this.mTargeClass);
            } finally {
                checkToStopService();
            }
        }

        private void checkToStopService() {
            if (Log.HWLog) {
                HwLog.d(HsmIntentService.TAG, "task completed for intent:" + this.mIntent + ", action:" + this.mIntent.getAction());
            }
            long activeCount = (long) HsmIntentService.HSM_THREAD_POOL_EXECUTOR.getActiveCount();
            if (HsmIntentService.HSM_THREAD_POOL_EXECUTOR.getQueue().size() == 0 && activeCount <= 1) {
                HsmIntentService.this.mHandler.removeMessages(1);
                HsmIntentService.this.mHandler.sendEmptyMessageDelayed(1, 50);
            }
        }

        private void doBackgroud(Context context, Intent intent, String target) {
            reflectTargetReceiver(context, intent, target);
        }

        private void reflectTargetReceiver(Context context, Intent intent, String target) {
            try {
                Class<?> clazz = ClazzCache.getClazz(target);
                if (clazz == null) {
                    clazz = Class.forName(target);
                    ClazzCache.cacheClazz(target, clazz);
                }
                clazz.getDeclaredMethod(HsmBroadcastReceiver.METHOD_DO_IN_BACKGROUND, new Class[]{Context.class, Intent.class}).invoke(clazz.newInstance(), new Object[]{context, intent});
            } catch (ClassNotFoundException e) {
                HwLog.e(HsmIntentService.TAG, "can't find class:" + target);
            } catch (NoSuchMethodException e2) {
                HwLog.e(HsmIntentService.TAG, "can't find method:doInBackground");
            } catch (IllegalArgumentException e3) {
                HwLog.e(HsmIntentService.TAG, "invoke error, IllegalArgumentException:" + e3);
            } catch (IllegalAccessException e4) {
                HwLog.e(HsmIntentService.TAG, "invoke error, IllegalAccessException:" + e4);
            } catch (InvocationTargetException e5) {
                HwLog.e(HsmIntentService.TAG, "invoke error, InvocationTargetException:" + e5);
            } catch (InstantiationException e6) {
                HwLog.e(HsmIntentService.TAG, "invoke error, InstantiationException:" + e6);
            } catch (Exception e7) {
                HwLog.e(HsmIntentService.TAG, "invoke error, Exception:" + e7);
                e7.printStackTrace();
            }
        }
    }

    public void onCreate() {
        super.onCreate();
    }

    public void onDestroy() {
        super.onDestroy();
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            onHandleIntent(intent, startId);
            return super.onStartCommand(intent, flags, startId);
        }
        HwLog.i(TAG, "onStartCommand intent is null");
        return 0;
    }

    public IBinder onBind(Intent intent) {
        return null;
    }

    private void tryToStopSelf() {
        long activeCount = (long) HSM_THREAD_POOL_EXECUTOR.getActiveCount();
        if (HSM_THREAD_POOL_EXECUTOR.getQueue().size() > 0 || activeCount > 0) {
            HwLog.w(TAG, "still has some task running, don't stop service.");
        } else {
            stopSelf();
        }
    }

    private void onHandleIntent(Intent intent, int startId) {
        Intent origIntent = (Intent) intent.getParcelableExtra(HsmBroadcastReceiver.EXTRA_ORIGINAL_INTENT);
        if (origIntent == null) {
            HwLog.w(TAG, "no intent to handle in intent service, stop self aflter(ms) 1000");
            this.mHandler.removeMessages(1);
            this.mHandler.sendEmptyMessageDelayed(1, 1000);
            return;
        }
        String className = getClassName(intent, origIntent);
        if (className == null) {
            HwLog.w(TAG, "no component in this intent!.");
            return;
        }
        if (Log.HWLog) {
            HwLog.d(TAG, "started for intent:" + origIntent + ", action:" + origIntent.getAction() + ", class:" + className);
        }
        HSM_THREAD_POOL_EXECUTOR.execute(new Work(origIntent, className));
    }

    private String getClassName(Intent intent, Intent origIntent) {
        ComponentName cn = origIntent.getComponent();
        if (cn != null) {
            return cn.getClassName();
        }
        return intent.getStringExtra(HsmBroadcastReceiver.EXTRA_ORIGINAL_CLASS);
    }
}
