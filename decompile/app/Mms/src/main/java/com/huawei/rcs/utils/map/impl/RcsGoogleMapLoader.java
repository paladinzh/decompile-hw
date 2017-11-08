package com.huawei.rcs.utils.map.impl;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import cn.com.xy.sms.sdk.db.entity.NumberInfo;
import com.huawei.rcs.utils.map.abs.RcsMapLoader;
import java.util.HashMap;

public class RcsGoogleMapLoader extends RcsMapLoader {
    public void loadMap(Context context, HashMap<String, String> locInfo) {
        Intent location_intent = new Intent(context, RcsGoogleMapActivity.class);
        location_intent.putExtra(NumberInfo.TYPE_KEY, "look");
        location_intent.putExtra("latitude", (String) locInfo.get("latitude"));
        location_intent.putExtra("longitude", (String) locInfo.get("longitude"));
        location_intent.putExtra("title", (String) locInfo.get("title"));
        location_intent.putExtra("subtitle", (String) locInfo.get("subtitle"));
        context.startActivity(location_intent);
    }

    public void requestMap(Context context, int requestCode) {
        Intent location_intent = new Intent(context, RcsGoogleLocationActivity.class);
        location_intent.putExtra(NumberInfo.TYPE_KEY, "select");
        ((Activity) context).startActivityForResult(location_intent, requestCode);
    }
}
