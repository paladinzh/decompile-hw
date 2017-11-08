package com.android.settings.search;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import com.android.settings.Utils;

public class GlobalSearchDetail extends Activity {
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        if ("com.android.settings.GLOBAL_SEARCH_CLICKED".equals(intent.getAction())) {
            String intstr = intent.getStringExtra("suggest_intent_extra_data");
            String className = "";
            String packagename = "";
            String action = "";
            String targetClassName = "";
            String keyIndex = "";
            if (intstr != null) {
                try {
                    Intent i = Intent.parseUri(intstr, 0);
                    className = (String) i.getExtra("classname");
                    packagename = (String) i.getExtra("packagename");
                    action = (String) i.getExtra("action");
                    targetClassName = (String) i.getExtra("targetclassname");
                    keyIndex = (String) i.getExtra("keyIndex");
                } catch (Exception e) {
                    Log.e("GlobalSearchDetail", "GlobalSearchDetail-->onCreate()-->Exception e = " + e);
                }
                if (TextUtils.isEmpty(action)) {
                    Bundle args = new Bundle();
                    args.putString(":settings:fragment_args_key", keyIndex);
                    Utils.startWithFragment(this, className, args, null, 0, -1, null);
                } else {
                    if ("com.android.settings.action.unknown".equals(action)) {
                        intent = new Intent();
                    } else {
                        intent = new Intent(action);
                        if ("com.huawei.hwid.ACTION_START_FOR_GOTO_ACCOUNTCENTER".equals(action)) {
                            intent.putExtra("START_FOR_GOTO_ACCOUNTCENTER", true);
                        }
                    }
                    intent.putExtra("extra_setting_key", keyIndex);
                    if (!(TextUtils.isEmpty(packagename) || TextUtils.isEmpty(targetClassName))) {
                        intent.setComponent(new ComponentName(packagename, targetClassName));
                    }
                    if (Utils.hasIntentActivity(getPackageManager(), intent)) {
                        intent.putExtra(":settings:fragment_args_key", keyIndex);
                        startActivity(intent);
                    }
                }
                finish();
                return;
            }
            return;
        }
        finish();
    }
}
