package com.huawei.systemmanager.push;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import com.huawei.android.pushagent.PushReceiver;
import com.huawei.systemmanager.comm.misc.Utility;
import com.huawei.systemmanager.rainbow.client.connect.RequestMgr;
import com.huawei.systemmanager.util.HwLog;

public class CustomTaskHandler extends Handler {
    private static final int MAX_COUNT = 2;
    public static final int MSG_REG_TOKEN = 1;
    private static final String TAG = "CustomTaskHandler";
    private static CustomTaskHandler mInstance = null;
    private Context mContext = null;
    private int registCount = 0;

    public static synchronized CustomTaskHandler getInstance(Context context) {
        CustomTaskHandler customTaskHandler;
        synchronized (CustomTaskHandler.class) {
            if (mInstance == null) {
                HandlerThread t = new HandlerThread("");
                t.start();
                mInstance = new CustomTaskHandler(context, t.getLooper());
            }
            customTaskHandler = mInstance;
        }
        return customTaskHandler;
    }

    private CustomTaskHandler(Context context, Looper looper) {
        super(looper);
        this.mContext = context.getApplicationContext();
    }

    public void handleMessage(Message msg) {
        switch (msg.what) {
            case 1:
                if (!Utility.isTokenRegistered(this.mContext)) {
                    try {
                        Class.forName("com.huawei.android.pushagent.PushReceiver");
                        String token = Utility.getReisteToken(this.mContext);
                        if (TextUtils.isEmpty(token)) {
                            if (this.registCount < 2) {
                                PushReceiver.getToken(this.mContext);
                                this.registCount++;
                                if (this.registCount >= 2) {
                                    this.registCount = 0;
                                    break;
                                }
                                getInstance(this.mContext).removeMessages(1);
                                getInstance(this.mContext).sendEmptyMessageDelayed(1, 60000);
                                break;
                            }
                        }
                        RequestMgr.generateDeviceTokenRequest(this.mContext, token).processRequest(this.mContext);
                        break;
                    } catch (ClassNotFoundException e) {
                        this.registCount++;
                        HwLog.w(TAG, "get token error", e);
                        break;
                    } catch (Exception ex) {
                        this.registCount++;
                        HwLog.w(TAG, "get token error", ex);
                        break;
                    }
                }
                break;
            default:
                HwLog.w(TAG, "handleMessage: Invalid message ," + msg.arg1);
                break;
        }
        super.handleMessage(msg);
    }
}
