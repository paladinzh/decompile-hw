package com.huawei.systemmanager.urlrecognition;

import android.database.Cursor;
import android.database.MatrixCursor;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import com.huawei.systemmanager.antivirus.engine.AntiVirusEngineFactory;
import com.huawei.systemmanager.antivirus.engine.IAntiVirusEngine;
import com.huawei.systemmanager.comm.concurrent.HsmExecutor;
import com.huawei.systemmanager.comm.misc.GlobalContext;
import com.huawei.systemmanager.comm.misc.Utility;
import com.huawei.systemmanager.customize.AbroadUtils;
import com.huawei.systemmanager.sdk.tmsdk.TMSEngineFeature;
import com.huawei.systemmanager.spacecleanner.engine.base.SpaceConst;
import com.huawei.systemmanager.util.HwLog;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import tmsdk.common.module.urlcheck.UrlCheckResult;
import tmsdk.fg.creator.ManagerCreatorF;
import tmsdk.fg.module.urlcheck.UrlCheckManager;

public class UrlRecognitionManager {
    private static final String[] RESULT_COLUMNS = new String[]{"result"};
    private static final String TAG = "UrlRecognitionManager";
    private static final long WAIT_TIMEOUT_DFT = 5000;
    private IAntiVirusEngine mAntiVirusEngine;
    private AtomicInteger mRequestSequence = new AtomicInteger(0);
    private UrlCheckManager mUrlCheckManager = null;

    private static class CheckUrlHandler extends Handler {
        private final HsmNumberQueryRequest mRequest;

        public CheckUrlHandler(HsmNumberQueryRequest request) {
            super(Looper.getMainLooper());
            this.mRequest = request;
        }

        public void handleMessage(Message msg) {
            HwLog.i(UrlRecognitionManager.TAG, "handleMessage:" + msg.what);
            super.handleMessage(msg);
            if (this.mRequest.isExpired()) {
                HwLog.i(UrlRecognitionManager.TAG, "onResult: [" + this.mRequest.getRequestSequence() + "], Request is expired ,skip");
                return;
            }
            boolean retValue = false;
            switch (msg.what) {
                case 19:
                    retValue = false;
                    break;
                case 20:
                    retValue = true;
                    break;
            }
            this.mRequest.setResult(UrlRecognitionManager.getCursorFromCheckUrlResult(retValue));
            this.mRequest.releaseLock();
        }
    }

    interface ICheckUrlCallback {
        void onCheckUrlCallback(UrlCheckResult urlCheckResult);
    }

    static class HsmCheckUrlListener implements ICheckUrlCallback {
        private HsmNumberQueryRequest mRequestObj;

        public HsmCheckUrlListener(HsmNumberQueryRequest request) {
            this.mRequestObj = request;
        }

        public void onCheckUrlCallback(UrlCheckResult result) {
            if (this.mRequestObj.isExpired()) {
                HwLog.i(UrlRecognitionManager.TAG, "onResult: [" + this.mRequestObj.getRequestSequence() + "]" + ", Request is expired ,skip");
                return;
            }
            boolean retValue = false;
            if (result == null || SpaceConst.SCANNER_TYPE_ALL == result.result) {
                this.mRequestObj.setResult(null);
                this.mRequestObj.releaseLock();
                return;
            }
            HwLog.i(UrlRecognitionManager.TAG, "onCheckUrlCallback is value:" + result.result);
            switch (result.result) {
                case 0:
                    retValue = false;
                    break;
                case 2:
                case 3:
                    retValue = true;
                    break;
            }
            this.mRequestObj.setResult(UrlRecognitionManager.getCursorFromCheckUrlResult(retValue));
            this.mRequestObj.releaseLock();
        }
    }

    static class HsmNumberQueryRequest {
        AtomicBoolean mExpired = new AtomicBoolean(false);
        Object mLock = new Object();
        int mRequestSequence = 0;
        Cursor mResult = null;
        String mUrl;

        public HsmNumberQueryRequest(int nRequestSequence, String url) {
            this.mRequestSequence = nRequestSequence;
            this.mUrl = url;
        }

        public int getRequestSequence() {
            return this.mRequestSequence;
        }

        public String getUrl() {
            return this.mUrl;
        }

        public synchronized Cursor getResult() {
            return this.mResult;
        }

        public synchronized void setResult(Cursor result) {
            this.mResult = result;
        }

        public void waitRequestResult(long nTimeOut) {
            synchronized (this.mLock) {
                long begin;
                do {
                    try {
                        if (getResult() != null) {
                            break;
                        }
                        HwLog.i(UrlRecognitionManager.TAG, "waitRequestResult: [" + this.mRequestSequence + "], timeout = " + nTimeOut);
                        begin = System.currentTimeMillis();
                        this.mLock.wait(nTimeOut);
                    } catch (InterruptedException e) {
                        HwLog.i(UrlRecognitionManager.TAG, "InterruptedException, [" + this.mRequestSequence + "]");
                    }
                } while (System.currentTimeMillis() - begin < nTimeOut);
                HwLog.i(UrlRecognitionManager.TAG, "waitRequestResult time out");
                this.mExpired.set(true);
            }
        }

        public void releaseLock() {
            synchronized (this.mLock) {
                this.mLock.notifyAll();
            }
        }

        public boolean isExpired() {
            return this.mExpired.get();
        }
    }

    public Cursor getUrlCheckInfo(String url) {
        return getUrlCheckInfo(url, WAIT_TIMEOUT_DFT);
    }

    public Cursor getUrlCheckInfo(String url, long timeOut) {
        int nSequence = getNextRequestSequence();
        HwLog.i(TAG, "getUrlCheckInfo: Starts, [" + nSequence + "]");
        HsmNumberQueryRequest request = new HsmNumberQueryRequest(nSequence, url);
        cloudCheckUrlInfo(request);
        if (timeOut <= 0) {
            timeOut = WAIT_TIMEOUT_DFT;
        }
        request.waitRequestResult(timeOut);
        HwLog.i(TAG, "getUrlCheckInfo: Ends, [" + nSequence + "]");
        return request.getResult();
    }

    private int getNextRequestSequence() {
        return this.mRequestSequence.incrementAndGet();
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private synchronized boolean initTmsUrlCheckManager() {
        if (this.mUrlCheckManager != null) {
            return true;
        }
        Utility.initSDK(GlobalContext.getContext());
        if (TMSEngineFeature.isSupportTMS()) {
            this.mUrlCheckManager = (UrlCheckManager) ManagerCreatorF.getManager(UrlCheckManager.class);
        } else {
            HwLog.w(TAG, "initTmsUrlCheckManager: TMS feature is not supported");
            return false;
        }
    }

    private void cloudCheckUrlInfo(HsmNumberQueryRequest request) {
        HwLog.i(TAG, "cloudCheckUrlInfo");
        if (AbroadUtils.isAbroad()) {
            initAntiVirusEngine();
            asyncCheckUrlAbroad(request.mUrl, new CheckUrlHandler(request));
        } else if (initTmsUrlCheckManager()) {
            asyncCheckUrl(request.mUrl, new HsmCheckUrlListener(request));
        } else {
            HwLog.e(TAG, "cloudCheckUrlInfo: Invalid TMS check url manager");
        }
    }

    private void asyncCheckUrl(final String url, final ICheckUrlCallback callback) {
        new Thread(new Runnable() {
            public void run() {
                UrlCheckResult result;
                synchronized (UrlRecognitionManager.this) {
                    try {
                        result = UrlRecognitionManager.this.mUrlCheckManager.checkUrl(url);
                    } catch (Exception e) {
                        result = null;
                    }
                }
                callback.onCheckUrlCallback(result);
            }
        }).start();
    }

    private static Cursor getCursorFromCheckUrlResult(boolean reValue) {
        int i = 1;
        MatrixCursor cursor = new MatrixCursor(RESULT_COLUMNS);
        Object[] objArr = new Object[1];
        if (!reValue) {
            i = 0;
        }
        objArr[0] = Integer.valueOf(i);
        cursor.addRow(objArr);
        return cursor;
    }

    private void initAntiVirusEngine() {
        if (this.mAntiVirusEngine == null) {
            this.mAntiVirusEngine = AntiVirusEngineFactory.newInstance();
            this.mAntiVirusEngine.onInit(GlobalContext.getContext());
        }
    }

    private void asyncCheckUrlAbroad(final String url, final Handler handler) {
        HsmExecutor.THREAD_POOL_EXECUTOR.execute(new Runnable() {
            public void run() {
                if (UrlRecognitionManager.this.mAntiVirusEngine != null) {
                    UrlRecognitionManager.this.mAntiVirusEngine.onCheckUrl(url, handler);
                }
            }
        });
    }
}
