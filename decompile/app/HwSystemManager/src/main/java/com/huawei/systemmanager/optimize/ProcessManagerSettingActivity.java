package com.huawei.systemmanager.optimize;

import android.app.ActionBar;
import android.app.NotificationManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.RelativeLayout;
import android.widget.Switch;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.backup.CommonPrefBackupProvider;
import com.huawei.systemmanager.backup.CommonPrefBackupProvider.IPreferenceBackup.BasePreferenceBackup;
import com.huawei.systemmanager.emui.activities.HsmActivity;
import com.huawei.systemmanager.hsmstat.HsmStat;
import com.huawei.systemmanager.hsmstat.base.HsmStatConst;
import com.huawei.systemmanager.optimize.monitor.MemCPUMonitorSwitchManager;
import com.huawei.systemmanager.optimize.process.ProtectActivity;
import com.huawei.systemmanager.util.HwLog;
import java.util.HashSet;
import java.util.Set;

public class ProcessManagerSettingActivity extends HsmActivity {
    private final String TAG = "ProcessManagerSettingActivity";
    private RelativeLayout mContainer;
    private Handler mHandler = new Handler();
    private Switch mMemCPUReminder;
    private ContentObserver mObserver = new ContentObserver(this.mHandler) {
        public void onChange(boolean selfChange) {
            HwLog.i("ProcessManagerSettingActivity", "recover complete, refresh views");
            ProcessManagerSettingActivity.this.initValues();
        }
    };

    public static class ProcessSettingBackup extends BasePreferenceBackup {
        private final String TAG = ProcessSettingBackup.class.getSimpleName();
        private final Context mContext;

        public ProcessSettingBackup(Context context) {
            this.mContext = context;
        }

        public ContentValues onQueryPreferences() {
            int state = MemCPUMonitorSwitchManager.getMemCPUSwitchState(this.mContext);
            ContentValues values = new ContentValues(1);
            values.put("processmanagersetting", Integer.valueOf(state));
            return values;
        }

        public Set<String> onQueryPreferenceKeys() {
            HashSet<String> set = new HashSet(1);
            set.add("processmanagersetting");
            return set;
        }

        public int onRecoverPreference(String key, String value) {
            if ("processmanagersetting".equals(key)) {
                Integer state = Integer.valueOf(-1);
                try {
                    state = Integer.valueOf(Integer.parseInt(value));
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
                if (MemCPUMonitorSwitchManager.setMemCPUSwitchState(this.mContext, state.intValue())) {
                    return 1;
                }
                return 0;
            }
            HwLog.i(this.TAG, "key error, key = " + key);
            return 0;
        }
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initViews();
        initValues();
        addIconAndTitle();
        setListener();
        getContentResolver().registerContentObserver(CommonPrefBackupProvider.URI_RECOVER_COMPLETE, false, this.mObserver);
    }

    protected void onDestroy() {
        getContentResolver().unregisterContentObserver(this.mObserver);
        super.onDestroy();
    }

    private void initViews() {
        setContentView(R.layout.process_manager_settings);
        this.mMemCPUReminder = (Switch) findViewById(R.id.set_memcpu_reminder);
        this.mContainer = (RelativeLayout) findViewById(R.id.container);
        findViewById(R.id.protece_app_indicate_layout).setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                HwLog.i("ProcessManagerSettingActivity", "protected activity button click");
                Intent settingIntent = new Intent(ProcessManagerSettingActivity.this, ProtectActivity.class);
                settingIntent.putExtra(ProtectActivity.TITLE, ProcessManagerSettingActivity.this.getString(R.string.progress_manager_protect_apps));
                ProcessManagerSettingActivity.this.startActivity(settingIntent);
            }
        });
    }

    private void initValues() {
        updateMemCPUReminder(getMemCPUReminderValue());
    }

    private boolean getMemCPUReminderValue() {
        return MemCPUMonitorSwitchManager.getMemCPUSwitchState(getApplicationContext()) == 1;
    }

    private void updateMemCPUReminder(boolean b) {
        this.mMemCPUReminder.setChecked(b);
    }

    private void updateNotifyChoice(boolean stat) {
        int checked;
        if (stat) {
            checked = 1;
        } else {
            checked = 0;
        }
        MemCPUMonitorSwitchManager.setMemCPUSwitchState(getApplicationContext(), checked);
        String param = HsmStatConst.constructJsonParams(HsmStatConst.PARAM_OP, String.valueOf(checked));
        HsmStat.statE(15, param);
    }

    private void addIconAndTitle() {
        ActionBar actionBar = getActionBar();
        actionBar.setTitle(R.string.title_schedule_clean_new);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.show();
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 16908332:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setListener() {
        this.mContainer.setOnClickListener(new OnClickListener() {
            public void onClick(View arg0) {
                if (ProcessManagerSettingActivity.this.mMemCPUReminder != null) {
                    ProcessManagerSettingActivity.this.mMemCPUReminder.performClick();
                }
            }
        });
        this.mMemCPUReminder.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton arg0, boolean value) {
                boolean checked = value;
                if (!value) {
                    ((NotificationManager) ProcessManagerSettingActivity.this.getApplicationContext().getSystemService("notification")).cancel(R.string.optimize_app_protected);
                }
                ProcessManagerSettingActivity.this.updateNotifyChoice(value);
            }
        });
    }
}
