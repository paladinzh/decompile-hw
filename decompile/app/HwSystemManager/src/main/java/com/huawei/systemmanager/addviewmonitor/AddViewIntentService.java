package com.huawei.systemmanager.addviewmonitor;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import com.huawei.systemmanager.util.HwLog;
import java.util.ArrayList;

public class AddViewIntentService extends IntentService {
    private static final String SERVICE_TAG = "AddViewIntentService";
    private static final String TAG = "AddViewIntentService";
    private Context mContext;

    public AddViewIntentService() {
        super("AddViewIntentService");
    }

    public void onCreate() {
        super.onCreate();
        this.mContext = getApplicationContext();
    }

    protected void onHandleIntent(Intent intent) {
        if (intent == null) {
            HwLog.e("AddViewIntentService", "onHandleIntent get null intent");
            return;
        }
        if (AddViewConst.ADD_VIEW_RECORD_LIST_ACTION.equals(intent.getAction())) {
            saveInitedPkgIntoFile(intent.getStringArrayListExtra(AddViewConst.ADD_VIEW_PKGLIST_KEY));
        }
    }

    private void saveInitedPkgIntoFile(ArrayList<String> pkgNameList) {
        if (pkgNameList != null && !pkgNameList.isEmpty()) {
            try {
                Editor edit = this.mContext.getSharedPreferences(AddViewConst.ADD_VIEW_SHAREPREFERENCE, 0).edit();
                for (String pkgName : pkgNameList) {
                    edit.putString(pkgName, "");
                }
                edit.commit();
            } catch (Exception e) {
                HwLog.e("AddViewIntentService", "saveInitedPkgIntoFile Exception msg is: " + e.getMessage());
            }
        }
    }
}
