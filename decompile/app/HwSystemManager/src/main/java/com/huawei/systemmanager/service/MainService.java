package com.huawei.systemmanager.service;

import android.app.Service;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.IBinder;
import android.os.ServiceManager;
import android.os.UserHandle;
import android.text.TextUtils;
import com.google.common.collect.Lists;
import com.huawei.netassistant.service.NetAssistantService;
import com.huawei.systemmanager.adblock.background.AdBlockService;
import com.huawei.systemmanager.antimal.AntiMalService;
import com.huawei.systemmanager.comm.collections.HsmCollections;
import com.huawei.systemmanager.comm.misc.GlobalContext;
import com.huawei.systemmanager.competitorintercept.NotificationGuideService;
import com.huawei.systemmanager.hsmstat.HandleInstalledPackageInfoService;
import com.huawei.systemmanager.hsmstat.HsmStatBinder;
import com.huawei.systemmanager.hsmstat.base.HsmStatConst;
import com.huawei.systemmanager.netassistant.traffic.netnotify.NatNetworkPolicyService;
import com.huawei.systemmanager.netassistant.traffic.netnotify.policy.NatTrafficNotifyService;
import com.huawei.systemmanager.optimize.ProcessTrimService;
import com.huawei.systemmanager.optimize.ProtectAppService;
import com.huawei.systemmanager.preventmode.PreventHsmService;
import com.huawei.systemmanager.rainbow.client.background.service.CloudBroadcastHandlerService;
import com.huawei.systemmanager.rainbow.service.PackageInstallService;
import com.huawei.systemmanager.rainbow.util.PackageInfoConst;
import com.huawei.systemmanager.securitythreats.background.SecurityThreatsService;
import com.huawei.systemmanager.spacecleanner.autoclean.AutoCleanReceiveServer;
import com.huawei.systemmanager.util.HwLog;
import java.util.ArrayList;
import java.util.Map;

public class MainService extends Service {
    public static final String BINDER_NAME = "binder_name";
    public static final String TAG = MainService.class.getSimpleName();
    private final Map<String, IBinder> mBinderMap = HsmCollections.newArrayMap();
    private final ArrayList<HsmService> mServices = Lists.newArrayList();

    public interface GenericService {
        void init();
    }

    public interface HsmService extends GenericService {
        void onConfigurationChange(Configuration configuration);

        void onDestroy();

        void onStartCommand(Intent intent, int i, int i2);
    }

    public void onCreate() {
        super.onCreate();
        addService(CloudBroadcastHandlerService.TAG, new CloudBroadcastHandlerService(this));
        addService(InitValueService.DEFAULT_VALUE_SETTING, new InitValueService(this));
        addService(NotificationGuideService.TAG, new NotificationGuideService(this));
        if (true) {
            NatNetworkPolicyService networkPolicyBinder = new NatNetworkPolicyService(this);
            addService(NatNetworkPolicyService.NAT_NETWORK_POLICY, networkPolicyBinder);
            addService(NatTrafficNotifyService.NAT_TRAFFIC_NOTIFY, new NatTrafficNotifyService(this, networkPolicyBinder));
            if (UserHandle.myUserId() == 0) {
                addService(NetAssistantService.NET_ASSISTANT, new NetAssistantService(this));
            }
        }
        if (UserHandle.myUserId() == 0) {
            addService(PackageInfoConst.PACKAGE_INSTALL_SERVICE_NAME, new PackageInstallService(this));
        }
        addService(HsmStatBinder.NAME, new HsmStatBinder(this));
        addService(ProtectAppService.NAME, new ProtectAppService(this));
        addService(ProcessTrimService.TAG, new ProcessTrimService(this));
        if (UserHandle.myUserId() == 0) {
            addService(AntiMalService.TAG, AntiMalService.getInstance(GlobalContext.getContext()));
        } else {
            HwLog.i(TAG, "current user is not owner user, do not need to add antimal service!");
        }
        addService(HsmStatConst.HANDLE_INSTALLED_PACKAGE_SERVICE_NAME, new HandleInstalledPackageInfoService(this));
        if (UserHandle.myUserId() == 0) {
            addService(AutoCleanReceiveServer.TAG, new AutoCleanReceiveServer(this));
        } else {
            HwLog.i(TAG, "current user is not owner user,so not add autoclean service!");
        }
        if (UserHandle.myUserId() == 0) {
            addService(SecurityThreatsService.TAG, new SecurityThreatsService(this));
        }
        addService(PreventHsmService.TAG, new PreventHsmService(this));
        addService(AdBlockService.TAG, new AdBlockService(this));
    }

    public IBinder onBind(Intent intent) {
        if (intent == null) {
            return null;
        }
        String binderName = intent.getStringExtra("binder_name");
        if (TextUtils.isEmpty(binderName)) {
            return null;
        }
        return (IBinder) this.mBinderMap.get(binderName);
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null) {
            return 1;
        }
        for (HsmService hsmSer : this.mServices) {
            hsmSer.onStartCommand(intent, flags, startId);
        }
        return 1;
    }

    private void addService(String serviceName, GenericService ser) {
        long beginTime = System.currentTimeMillis();
        ser.init();
        if (ser instanceof HsmService) {
            this.mServices.add((HsmService) ser);
        }
        if (ser instanceof IBinder) {
            if (this.mBinderMap.get(serviceName) != null) {
                throw new IllegalArgumentException("The service name already registered!");
            }
            IBinder binder = (IBinder) ser;
            try {
                ServiceManager.addService(serviceName, binder);
                this.mBinderMap.put(serviceName, binder);
            } catch (SecurityException e) {
                e.printStackTrace();
                HwLog.e(TAG, "add binder:" + serviceName + " failed!");
            }
        }
        long endTime = System.currentTimeMillis();
        HwLog.i(TAG, "add service " + serviceName + " end, " + endTime + ",cost " + (endTime - beginTime));
    }

    public void onConfigurationChanged(Configuration newConfig) {
        for (HsmService hsmSer : this.mServices) {
            hsmSer.onConfigurationChange(newConfig);
        }
    }

    public void onDestroy() {
        for (HsmService hsmSer : this.mServices) {
            hsmSer.onDestroy();
        }
        super.onDestroy();
    }
}
