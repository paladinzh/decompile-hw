package com.android.contacts.hap.camcard.groups;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import com.android.contacts.hap.camcard.bcr.CCSaveService;

public class GroupUpdateReceiver extends BroadcastReceiver {
    private static final String TAG = GroupUpdateReceiver.class.getSimpleName();

    public void onReceive(Context context, Intent intent) {
        context.startService(CCSaveService.createGroupUpdateIntent(context));
    }
}
