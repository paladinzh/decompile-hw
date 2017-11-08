package com.android.contacts.hap.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import com.android.contacts.compatibility.NumberLocationCache;
import com.android.contacts.hap.numbermark.NumberMarkUtil;
import com.android.contacts.util.DateUtils;
import com.android.contacts.util.PhoneCapabilityTester;

public class LocaleChangeReceiver extends BroadcastReceiver {
    public void onReceive(Context context, Intent intent) {
        DateUtils.onLocaleChange();
        Intent updateWidget = new Intent("com.huawei.android.CLICK_DONE");
        updateWidget.putExtra("localeChanged", true);
        context.sendBroadcast(updateWidget);
        PhoneCapabilityTester.reset();
        NumberLocationCache.clearLocation();
        NumberLocationCache.clear();
        NumberMarkUtil.clean();
    }
}
