package com.android.contacts.hap.rcs.service;

import android.content.Context;
import com.huawei.rcs.capability.CapabilityService;

public class RcsService {
    public void initCapabilityService(Context context) {
        CapabilityService.init(context, "contacts");
    }

    public void endCapabilityService() {
        CapabilityService.getInstance("contacts").end();
        CapabilityService.deinit("contacts");
    }
}
