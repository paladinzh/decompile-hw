package com.tencent.tmsecurelite.commom;

import android.content.Intent;
import android.os.IBinder;
import android.os.IInterface;
import com.tencent.tmsecurelite.base.TmsConnectionStub;

public class ServiceManager {
    public static final Intent getTmsIntent(int type) {
        Intent intent = new Intent();
        intent.setPackage("com.tencent.qqpimsecure");
        intent.putExtra("use_common_interface", true);
        intent.putExtra("service_type", type);
        intent.setAction("com.tencent.qqpimsecure.TMS_LITE_SERVICE");
        return intent;
    }

    public static final IInterface getTmsConnection(IBinder binder) {
        return TmsConnectionStub.asInterface(binder);
    }
}
