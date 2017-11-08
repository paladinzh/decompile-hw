package com.android.settings;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import com.android.settings.Settings.AdvancedSettingsActivity;
import com.android.settings.Settings.BluetoothSettingsActivity;
import com.android.settings.Settings.BrightnessSettingsActivity;
import com.android.settings.Settings.InputMethodAndLanguageSettingsActivity;
import com.android.settings.Settings.ScreenLockSettingsActivity;
import com.android.settings.Settings.SoundSettingsActivity;
import com.android.settings.Settings.WifiSettingsActivity;
import com.huawei.cust.HwCustUtils;
import java.util.ArrayList;
import java.util.HashMap;

public class SimpleSettings extends Activity {
    private static final Class[] CLASS_LIST = new Class[]{WifiSettingsActivity.class, BluetoothSettingsActivity.class, BrightnessSettingsActivity.class, null, FontsizeSettingsActivity.class, SoundSettingsActivity.class, InputMethodAndLanguageSettingsActivity.class, LauncherModeSettingsActivity.class, ScreenLockSettingsActivity.class};
    private static final int[] ICON_LIST = new int[]{2130838412, 2130838404, 2130838405, 2130838411, 2130838406, 2130838410, 2130838408, 2130838407, 2130838409};
    private static final int[] TITLE_LIST = new int[]{2131624904, 2131624808, 2131625153, 2131625161, 2131625179, 2131625101, 2131625746, 2131627448, 2131627994};
    private Activity mActivity;
    private GridView mGridview;
    private HwCustSplitUtils mHwCustSplitUtils;
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if ("com.android.settings.action.CLEAR_TASK".equals(intent.getAction())) {
                SimpleSettings.this.finish();
            }
        }
    };

    class ItemClickListener implements OnItemClickListener {
        ItemClickListener() {
        }

        public void onItemClick(AdapterView<?> list, View view, int index, long arg3) {
            HashMap<String, Object> item = (HashMap) list.getItemAtPosition(index);
            Intent intent = new Intent();
            if (item.get("ItemTargetClass") != null) {
                intent.setClass(SimpleSettings.this.mActivity, (Class) item.get("ItemTargetClass"));
            } else if (SimpleSettings.this.mActivity.getString(2131625161).equals((String) item.get("ItemText"))) {
                intent.setAction("com.huawei.launcher.wallpaper_setting");
                if (!Utils.hasIntentActivity(SimpleSettings.this.getPackageManager(), intent)) {
                    Utils.startWithFragment(SimpleSettings.this, WallpaperTypeSettings.class.getName(), null, null, 0, 2131625164, SimpleSettings.this.getResources().getString(2131625161));
                    return;
                }
            }
            SimpleSettings.this.startActivity(intent);
        }
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mHwCustSplitUtils = (HwCustSplitUtils) HwCustUtils.createObj(HwCustSplitUtils.class, new Object[]{this});
        Intent tar;
        if (Utils.isSimpleModeOn()) {
            tar = new Intent();
            tar.setClass(this, AdvancedSettingsActivity.class);
            this.mHwCustSplitUtils.setTargetIntent(tar);
        } else {
            if (this.mHwCustSplitUtils.reachSplitSize()) {
                tar = new Intent();
                tar.setClass(this, AdvancedSettingsActivity.class);
                this.mHwCustSplitUtils.setTargetIntent(tar);
            } else {
                startActivity(new Intent("android.settings.SETTINGS"));
            }
            finish();
        }
        setContentView(2130969137);
        this.mGridview = (SettingsExtandableGridView) findViewById(2131887197);
        this.mActivity = this;
        ArrayList<HashMap<String, Object>> lstImageItem = new ArrayList();
        for (int i = 0; i < ICON_LIST.length; i++) {
            HashMap<String, Object> map = new HashMap();
            map.put("ItemImage", Integer.valueOf(ICON_LIST[i]));
            map.put("ItemText", getString(TITLE_LIST[i]));
            map.put("ItemTargetClass", CLASS_LIST[i]);
            lstImageItem.add(map);
        }
        this.mGridview.setAdapter(new SimpleAdapter(this, lstImageItem, 2130969136, new String[]{"ItemImage", "ItemText"}, new int[]{2131887195, 2131887196}));
        this.mGridview.setOnItemClickListener(new ItemClickListener());
        LocalBroadcastManager.getInstance(this).registerReceiver(this.mReceiver, new IntentFilter("com.android.settings.action.CLEAR_TASK"));
    }

    protected void onDestroy() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(this.mReceiver);
        super.onDestroy();
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(2132017159, menu);
        TextView itemView = (TextView) menu.getItem(0).getActionView();
        if (itemView != null) {
            itemView.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    if (SimpleSettings.this.mHwCustSplitUtils.reachSplitSize()) {
                        Intent itt = new Intent(SimpleSettings.this.mActivity, HWSettings.class);
                        itt.putExtra("extra_split", true);
                        SimpleSettings.this.startActivity(itt);
                        return;
                    }
                    Intent intent = new Intent("android.settings.WIFI_SETTINGS");
                    intent.setClass(SimpleSettings.this.mActivity, WifiSettingsActivity.class);
                    SimpleSettings.this.mHwCustSplitUtils.setTargetIntent(intent);
                    Intent i = new Intent(SimpleSettings.this.mActivity, Settings.class);
                    i.putExtra("extra_split", true);
                    SimpleSettings.this.startActivity(i);
                    SimpleSettings.this.mActivity.finish();
                }
            });
        } else {
            Log.e("SimpleSettings", "TextView is null! Failed to set OnClickListener.");
        }
        return super.onCreateOptionsMenu(menu);
    }
}
