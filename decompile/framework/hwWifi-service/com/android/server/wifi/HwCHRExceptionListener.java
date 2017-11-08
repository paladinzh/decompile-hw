package com.android.server.wifi;

import android.content.Context;
import android.net.LocalServerSocket;
import android.net.LocalSocket;
import android.util.Log;
import com.android.server.HwServiceFactory;
import com.android.server.location.IHwGpsLogServices;
import com.huawei.connectivitylog.ConnectivityLogManager;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.HashMap;
import org.json.JSONException;
import org.json.JSONObject;

public class HwCHRExceptionListener {
    protected static final String ESCEP_CHIPSET = "chipset";
    protected static final String ESCEP_DESC = "desc";
    protected static final String ESCEP_ERROR = "error";
    protected static final String ESCEP_EVENT = "event";
    protected static final String ESCEP_LAYER = "layer";
    protected static final String ESCEP_SUBSYS = "subsys";
    private static final int EXCEPTION_CODE_LEN = 256;
    private static final int EXPIRATION_TIME = 10000;
    protected static final boolean HWFLOW;
    private static final int LISTENER_SEP_TIME = 2000;
    private static final int RECEIVE_BUF_LENGTH = 4096;
    private static String SOCKET_ADDRESS = "fourinone_socket";
    private static final int SOCKET_IO_TIMEOUT = 2000;
    private static final String TAG = "HwChrExceptionListener";
    private static HwCHRExceptionListener gHwChrExpListener = null;
    private static HashMap<String, Long> mapExp = new HashMap();
    private ExceptionListenerServer els = null;
    private HwWifiCHRStateManagerImpl hwWifiCHRManagerImpl = null;
    private Context mContext = null;
    private IHwGpsLogServices mHwGpsLogServices = null;

    protected static class ExceptionFields {
        public int error;
        public int event;
        public String strChipset;
        public String strDesc;
        public String strLayer;
        public String strSubsys;

        protected ExceptionFields(JSONObject jso) {
            try {
                this.strChipset = jso.getString(HwCHRExceptionListener.ESCEP_CHIPSET);
                this.strSubsys = jso.getString(HwCHRExceptionListener.ESCEP_SUBSYS);
                this.strLayer = jso.getString(HwCHRExceptionListener.ESCEP_LAYER);
                this.strDesc = jso.getString(HwCHRExceptionListener.ESCEP_DESC);
                this.event = jso.getInt(HwCHRExceptionListener.ESCEP_EVENT);
                this.error = jso.getInt(HwCHRExceptionListener.ESCEP_ERROR);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private class ExceptionListenerServer extends Thread {
        private boolean keepRunning;
        private LocalSocket socketClient;
        private LocalServerSocket socketServer;

        private ExceptionListenerServer() {
            this.keepRunning = true;
            this.socketServer = null;
            this.socketClient = null;
        }

        public void setRun(boolean bRun) {
            this.keepRunning = bRun;
        }

        public void run() {
            try {
                this.socketServer = new LocalServerSocket(HwCHRExceptionListener.SOCKET_ADDRESS);
                while (this.keepRunning) {
                    if (HwCHRExceptionListener.HWFLOW) {
                        Log.d(HwCHRExceptionListener.TAG, "ExceptionListenerServer::run, wait client connect");
                    }
                    this.socketClient = this.socketServer.accept();
                    if (this.socketClient == null) {
                        try {
                            Thread.sleep(2000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    } else {
                        new Thread(new ProcessExceptionRunnable(this.socketClient)).start();
                        try {
                            Thread.sleep(2000);
                        } catch (InterruptedException e2) {
                            e2.printStackTrace();
                        }
                    }
                }
                try {
                    if (this.socketServer != null) {
                        this.socketServer.close();
                        this.socketServer = null;
                    }
                    if (this.socketClient != null) {
                        this.socketClient.close();
                        this.socketClient = null;
                    }
                } catch (IOException e3) {
                    if (HwCHRExceptionListener.HWFLOW) {
                        Log.e(getClass().getName(), e3.getMessage());
                    }
                }
            } catch (IOException e32) {
                if (HwCHRExceptionListener.HWFLOW) {
                    Log.e(getClass().getName(), e32.getMessage());
                }
            } finally {
                try {
                    if (this.socketServer != null) {
                        this.socketServer.close();
                        this.socketServer = null;
                    }
                    if (this.socketClient != null) {
                        this.socketClient.close();
                        this.socketClient = null;
                    }
                } catch (IOException e322) {
                    if (HwCHRExceptionListener.HWFLOW) {
                        Log.e(getClass().getName(), e322.getMessage());
                    }
                }
            }
        }
    }

    private class ProcessExceptionRunnable implements Runnable {
        private int errorCode = -1;
        private int expCode = -1;
        private LocalSocket mClientSock = null;

        public ProcessExceptionRunnable(LocalSocket cliSocket) {
            this.mClientSock = cliSocket;
        }

        public synchronized void run() {
            try {
                this.mClientSock.setSoTimeout(2000);
            } catch (Exception e) {
                e.printStackTrace();
            }
            processException();
        }

        public void processException() {
            JSONException e;
            if (this.mClientSock != null) {
                int totalBytes = 0;
                InputStream inStream = null;
                ExceptionFields expObj = null;
                StringBuilder sb = new StringBuilder();
                byte[] bufByte = new byte[HwCHRExceptionListener.EXCEPTION_CODE_LEN];
                try {
                    inStream = this.mClientSock.getInputStream();
                } catch (IOException e2) {
                    e2.printStackTrace();
                }
                if (inStream != null) {
                    while (totalBytes < HwCHRExceptionListener.EXCEPTION_CODE_LEN) {
                        int readBytes = 0;
                        Arrays.fill(bufByte, (byte) 0);
                        try {
                            readBytes = inStream.read(bufByte, 0, bufByte.length - 1);
                        } catch (IOException e22) {
                            if (HwCHRExceptionListener.HWFLOW) {
                                Log.d(HwCHRExceptionListener.TAG, "ProcessExceptionRunnable processException, IOException ");
                            }
                            e22.printStackTrace();
                        }
                        if (readBytes > 0) {
                            totalBytes += readBytes;
                            sb.append(new String(bufByte, Charset.defaultCharset()).trim());
                        }
                    }
                    try {
                        inStream.close();
                    } catch (IOException e222) {
                        e222.printStackTrace();
                    }
                }
                String strBody = sb.toString();
                if (HwCHRExceptionListener.HWFLOW) {
                    Log.d(HwCHRExceptionListener.TAG, "ProcessExceptionRunnable processException, exception = " + strBody);
                }
                try {
                    JSONObject jsonStr = new JSONObject(strBody);
                    try {
                        expObj = new ExceptionFields(jsonStr);
                        JSONObject jSONObject = jsonStr;
                    } catch (JSONException e3) {
                        e = e3;
                        e.printStackTrace();
                        if (expObj != null) {
                            if (HwCHRExceptionListener.HWFLOW) {
                                Log.d(HwCHRExceptionListener.TAG, "ProcessExceptionRunnable processException, exception json parse = chipset : " + expObj.strChipset + ", subsys : " + expObj.strSubsys + ", layer : " + expObj.strLayer + ", event : " + expObj.event + ", error : " + expObj.error + " ,desc : " + expObj.strDesc);
                            }
                            this.expCode = expObj.event;
                            this.errorCode = expObj.error;
                            if (!getTimeLimit(this.expCode, this.errorCode)) {
                                if (HwCHRExceptionListener.HWFLOW) {
                                    Log.d(HwCHRExceptionListener.TAG, "ProcessExceptionRunnable processException, beTimeLimit is false");
                                }
                                HwCHRExceptionListener.this.dispatchException(strBody, expObj);
                            }
                        }
                        if (this.mClientSock != null) {
                            try {
                                this.mClientSock.shutdownInput();
                            } catch (IOException e2222) {
                                e2222.printStackTrace();
                            }
                            try {
                                this.mClientSock.shutdownOutput();
                            } catch (IOException e22222) {
                                e22222.printStackTrace();
                            }
                            try {
                                this.mClientSock.close();
                                this.mClientSock = null;
                            } catch (IOException e222222) {
                                e222222.printStackTrace();
                            }
                        }
                    }
                } catch (JSONException e4) {
                    e = e4;
                    e.printStackTrace();
                    if (expObj != null) {
                        if (HwCHRExceptionListener.HWFLOW) {
                            Log.d(HwCHRExceptionListener.TAG, "ProcessExceptionRunnable processException, exception json parse = chipset : " + expObj.strChipset + ", subsys : " + expObj.strSubsys + ", layer : " + expObj.strLayer + ", event : " + expObj.event + ", error : " + expObj.error + " ,desc : " + expObj.strDesc);
                        }
                        this.expCode = expObj.event;
                        this.errorCode = expObj.error;
                        if (getTimeLimit(this.expCode, this.errorCode)) {
                            if (HwCHRExceptionListener.HWFLOW) {
                                Log.d(HwCHRExceptionListener.TAG, "ProcessExceptionRunnable processException, beTimeLimit is false");
                            }
                            HwCHRExceptionListener.this.dispatchException(strBody, expObj);
                        }
                    }
                    if (this.mClientSock != null) {
                        this.mClientSock.shutdownInput();
                        this.mClientSock.shutdownOutput();
                        this.mClientSock.close();
                        this.mClientSock = null;
                    }
                }
                if (expObj != null) {
                    if (HwCHRExceptionListener.HWFLOW) {
                        Log.d(HwCHRExceptionListener.TAG, "ProcessExceptionRunnable processException, exception json parse = chipset : " + expObj.strChipset + ", subsys : " + expObj.strSubsys + ", layer : " + expObj.strLayer + ", event : " + expObj.event + ", error : " + expObj.error + " ,desc : " + expObj.strDesc);
                    }
                    this.expCode = expObj.event;
                    this.errorCode = expObj.error;
                    if (getTimeLimit(this.expCode, this.errorCode)) {
                        if (HwCHRExceptionListener.HWFLOW) {
                            Log.d(HwCHRExceptionListener.TAG, "ProcessExceptionRunnable processException, beTimeLimit is false");
                        }
                        HwCHRExceptionListener.this.dispatchException(strBody, expObj);
                    }
                }
                if (this.mClientSock != null) {
                    this.mClientSock.shutdownInput();
                    this.mClientSock.shutdownOutput();
                    this.mClientSock.close();
                    this.mClientSock = null;
                }
            }
        }

        private boolean getTimeLimit(int expCode, int errCode) {
            long expTime = 0;
            long curTime = System.currentTimeMillis();
            String key = Integer.toString(expCode) + "_" + Integer.toString(errCode);
            if (HwCHRExceptionListener.mapExp.containsKey(key)) {
                expTime = ((Long) HwCHRExceptionListener.mapExp.get(key)).longValue();
            }
            if (0 != expTime) {
                long diff = curTime - expTime;
                if (HwCHRExceptionListener.HWFLOW) {
                    Log.d(HwCHRExceptionListener.TAG, "ProcessExceptionRunnable::getTimeLimit curTime = " + curTime + ", prevTime = " + expTime + " diff = " + diff + " , kye = " + key);
                }
                if (diff <= 10000) {
                    return true;
                }
                HwCHRExceptionListener.mapExp.put(key, Long.valueOf(curTime));
                return false;
            }
            HwCHRExceptionListener.mapExp.put(key, Long.valueOf(curTime));
            return false;
        }
    }

    static {
        boolean isLoggable = !Log.HWINFO ? Log.HWModuleLog ? Log.isLoggable(TAG, 4) : false : true;
        HWFLOW = isLoggable;
    }

    public static HwCHRExceptionListener getInstance(Context context) {
        if (gHwChrExpListener == null) {
            gHwChrExpListener = new HwCHRExceptionListener(context);
            ConnectivityLogManager.getInstance();
            ConnectivityLogManager.initCHRDataPlusMap();
            if (HWFLOW) {
                Log.d(TAG, "new HwChrWifiExceptionListener");
            }
        }
        return gHwChrExpListener;
    }

    public void startChrWifiListener() {
        this.els = new ExceptionListenerServer();
        this.els.start();
        if (HWFLOW) {
            Log.d(TAG, "startChrWifiListener");
        }
    }

    private HwCHRExceptionListener(Context context) {
        this.mContext = context;
        this.hwWifiCHRManagerImpl = (HwWifiCHRStateManagerImpl) HwWifiServiceFactory.getHwWifiCHRStateManager();
        this.mHwGpsLogServices = HwServiceFactory.getHwGpsLogServices(this.mContext);
    }

    private void dispatchException(String strJsonExp, ExceptionFields exp) {
        if (exp.strSubsys != null && !exp.strSubsys.isEmpty()) {
            String str = exp.strSubsys;
            if (str.equals("wifi")) {
                processWifiEvent(strJsonExp);
            } else if (str.equals("gnss")) {
                processGnssEvent(strJsonExp);
            } else if (str.equals("bt")) {
                processBtEvent(strJsonExp);
            } else if (HWFLOW) {
                Log.d(TAG, "unknown exception module");
            }
        }
    }

    private void processWifiEvent(String strJsonExp) {
        if (HWFLOW) {
            Log.d(TAG, "processWifiEvent, " + strJsonExp);
        }
        this.hwWifiCHRManagerImpl.processWifiHalDriverEvent(strJsonExp);
    }

    private void processGnssEvent(String strJsonExp) {
        if (this.mHwGpsLogServices == null) {
            this.mHwGpsLogServices = HwServiceFactory.getHwGpsLogServices(this.mContext);
        }
        if (HWFLOW) {
            Log.d(TAG, "processGnssEvent, " + strJsonExp);
        }
        this.mHwGpsLogServices.processGnssHalDriverEvent(strJsonExp);
    }

    private void processBtEvent(String strJsonExp) {
        if (HWFLOW) {
            Log.d(TAG, "processBtEvent, " + strJsonExp);
        }
    }
}
