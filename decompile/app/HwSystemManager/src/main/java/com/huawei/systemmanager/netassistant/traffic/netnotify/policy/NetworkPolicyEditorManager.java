package com.huawei.systemmanager.netassistant.traffic.netnotify.policy;

import android.content.Context;
import android.net.NetworkPolicyManager;
import android.net.NetworkTemplate;
import android.os.ServiceManager;
import android.telephony.TelephonyManager;
import com.huawei.netassistant.service.INetAssistantService;
import com.huawei.netassistant.service.INetAssistantService.Stub;
import com.huawei.netassistant.service.NetAssistantService;
import com.huawei.systemmanager.comm.misc.GlobalContext;
import com.huawei.systemmanager.util.HwLog;

public class NetworkPolicyEditorManager {
    private static final String TAG = NetworkPolicyEditorManager.class.getSimpleName();
    private static NetworkPolicyEditorManager sInstance;
    private Context mContext;
    private NetworkPolicyEditor mEditor;
    private NetworkPolicyManager mPolicyManager;
    private INetAssistantService mService;
    private TelephonyManager mTelephonyManager;

    private NetworkPolicyEditorManager() {
    }

    public static synchronized NetworkPolicyEditorManager getInstance() {
        NetworkPolicyEditorManager networkPolicyEditorManager;
        synchronized (NetworkPolicyEditorManager.class) {
            if (sInstance == null) {
                sInstance = new NetworkPolicyEditorManager();
                sInstance.init();
            }
            networkPolicyEditorManager = sInstance;
        }
        return networkPolicyEditorManager;
    }

    private void init() {
        this.mContext = GlobalContext.getContext();
        this.mPolicyManager = NetworkPolicyManager.from(this.mContext);
        this.mEditor = new NetworkPolicyEditor(this.mPolicyManager);
        this.mTelephonyManager = (TelephonyManager) this.mContext.getSystemService("phone");
        this.mEditor.read();
        this.mService = Stub.asInterface(ServiceManager.getService(NetAssistantService.NET_ASSISTANT));
        if (this.mService == null) {
            HwLog.d(TAG, "service is null !");
        }
    }

    public void setPolicyWarningBytes(long bytes) {
        this.mEditor.setPolicyWarningBytes(NetworkTemplate.buildTemplateMobileAll(this.mTelephonyManager.getSubscriberId()), bytes);
    }

    public void setPolicyLimitBytes(long bytes) {
        this.mEditor.setPolicyLimitBytes(NetworkTemplate.buildTemplateMobileAll(this.mTelephonyManager.getSubscriberId()), bytes);
    }
}
