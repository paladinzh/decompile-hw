package com.huawei.systemmanager.netassistant.traffic.setting;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceScreen;
import android.preference.TextArrowPreference;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import com.huawei.netassistant.ui.view.NetAssistantDialogManager;
import com.huawei.netassistant.ui.view.NetAssistantDialogManager.TrafficSetListener;
import com.huawei.netassistant.util.CommonConstantUtil;
import com.huawei.netassistant.util.CommonMethodUtil;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.comm.misc.GlobalContext;
import com.huawei.systemmanager.emui.activities.HsmPreferenceActivity;
import com.huawei.systemmanager.hsmstat.HsmStat;
import com.huawei.systemmanager.hsmstat.base.HsmStatConst;
import com.huawei.systemmanager.hsmstat.base.HsmStatConst.Events;
import com.huawei.systemmanager.netassistant.traffic.leisuretraffic.LeisureTrafficSetting;
import com.huawei.systemmanager.netassistant.traffic.leisuretraffic.LeisureTrafficSettingsActivity;
import com.huawei.systemmanager.netassistant.traffic.notrafficapp.NoTrafficAppActivity;
import com.huawei.systemmanager.netassistant.traffic.notrafficapp.NoTrafficAppDbInfo;

public class OtherTrafficSettingsActivity extends HsmPreferenceActivity {
    private static final String EXTRA_TRAFFIC_PACKAGE_KEY = "extra_traffic_package";
    private static final String LEISURE_TIME_TRAFFIC_KEY = "leisure_time_traffic";
    private static final String NO_TRAFFIC_APP_KEY = "no_traffic_app";
    private static final String ROAMING_TRAFFIC_KEY = "roaming_traffic_package";
    private TextArrowPreference mExtraTraffic;
    private String mImsi = "";
    private TextArrowPreference mLeisureTime;
    private TextArrowPreference mNoTrafficApp;
    private TextArrowPreference mRoamingTraffic;

    public class HelpAsyncTask extends AsyncTask<Void, Integer, Void> {
        public static final int QUERY_ALL_TASK = 4;
        public static final int QUERY_EXTRA_TASK = 1;
        public static final int QUERY_LEISURE_TASK = 2;
        public static final int QUERY_NO_APP_TASK = 3;
        public static final int QUERY_ROAMING_TASK = 5;
        public static final int UPDATE_ROAMING_TASK = 12;
        public static final int UPDATE_TASK = 11;
        private String resultExtra;
        private String resultLeisure;
        private String resultNoApp;
        private String resultRoaming;
        private float size;
        private int taskId;
        private String unit;

        public HelpAsyncTask(int taskId) {
            this.taskId = taskId;
        }

        public HelpAsyncTask(int taskId, float s, String u) {
            this.taskId = taskId;
            this.size = s;
            this.unit = u;
        }

        protected Void doInBackground(Void... params) {
            ExtraTrafficSetting extraTrafficSetting = new ExtraTrafficSetting(OtherTrafficSettingsActivity.this.mImsi);
            RoamingTrafficSetting roamingTrafficSetting = new RoamingTrafficSetting(OtherTrafficSettingsActivity.this.mImsi);
            if (this.taskId == 4) {
                extraTrafficSetting.get();
                this.resultExtra = CommonMethodUtil.formatBytes(OtherTrafficSettingsActivity.this, extraTrafficSetting.getPackage());
                publishProgress(new Integer[]{Integer.valueOf(1)});
                roamingTrafficSetting.get();
                this.resultRoaming = CommonMethodUtil.formatBytes(OtherTrafficSettingsActivity.this, roamingTrafficSetting.getPackage());
                publishProgress(new Integer[]{Integer.valueOf(5)});
                LeisureTrafficSetting leisureTrafficSetting = new LeisureTrafficSetting(OtherTrafficSettingsActivity.this.mImsi);
                leisureTrafficSetting.get();
                this.resultLeisure = !leisureTrafficSetting.ismSwitch() ? OtherTrafficSettingsActivity.this.getString(R.string.is_not_open) : leisureTrafficSetting.getDesString();
                publishProgress(new Integer[]{Integer.valueOf(2)});
                NoTrafficAppDbInfo noTrafficAppDbInfo = new NoTrafficAppDbInfo(OtherTrafficSettingsActivity.this.mImsi);
                noTrafficAppDbInfo.initDbData();
                this.resultNoApp = OtherTrafficSettingsActivity.this.getResources().getQuantityString(R.plurals.app_cnt_suffix, count, new Object[]{Integer.valueOf(noTrafficAppDbInfo.getNoTrafficSize())});
                publishProgress(new Integer[]{Integer.valueOf(3)});
            } else if (this.taskId == 11) {
                byteCount = CommonMethodUtil.unitConvert(this.size, this.unit);
                extraTrafficSetting.get();
                extraTrafficSetting.setPackage(byteCount);
                extraTrafficSetting.save(null);
                this.resultExtra = CommonMethodUtil.formatBytes(OtherTrafficSettingsActivity.this, byteCount);
                publishProgress(new Integer[]{Integer.valueOf(1)});
            } else if (this.taskId == 12) {
                byteCount = CommonMethodUtil.unitConvert(this.size, this.unit);
                roamingTrafficSetting.get();
                roamingTrafficSetting.setPackage(byteCount);
                roamingTrafficSetting.save(null);
                this.resultRoaming = CommonMethodUtil.formatBytes(OtherTrafficSettingsActivity.this, byteCount);
                String statParam1 = HsmStatConst.constructJsonParams(HsmStatConst.PARAM_VAL, String.valueOf(this.unit));
                String statParam2 = HsmStatConst.constructJsonParams(HsmStatConst.PARAM_VAL, this.resultRoaming);
                HsmStat.statE((int) Events.E_NETASSISTANT_VALUE_ROAMING, statParam1, statParam2);
                publishProgress(new Integer[]{Integer.valueOf(5)});
            }
            return null;
        }

        protected void onProgressUpdate(Integer... values) {
            if (values != null && values.length > 0) {
                int i = values[0].intValue();
                if (i == 1 && OtherTrafficSettingsActivity.this.mExtraTraffic != null && !TextUtils.isEmpty(this.resultExtra)) {
                    OtherTrafficSettingsActivity.this.mExtraTraffic.setDetail(this.resultExtra);
                } else if (i == 2 && OtherTrafficSettingsActivity.this.mLeisureTime != null && !TextUtils.isEmpty(this.resultLeisure)) {
                    OtherTrafficSettingsActivity.this.mLeisureTime.setDetail(this.resultLeisure);
                } else if (i == 3 && OtherTrafficSettingsActivity.this.mNoTrafficApp != null && !TextUtils.isEmpty(this.resultNoApp)) {
                    OtherTrafficSettingsActivity.this.mNoTrafficApp.setDetail(this.resultNoApp);
                } else if (!(i != 5 || OtherTrafficSettingsActivity.this.mRoamingTraffic == null || TextUtils.isEmpty(this.resultRoaming))) {
                    OtherTrafficSettingsActivity.this.mRoamingTraffic.setDetail(this.resultRoaming);
                }
            }
        }
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.other_traffic_settings_preference);
        if (getIntent() == null) {
            finish();
            return;
        }
        this.mImsi = getIntent().getStringExtra(CommonConstantUtil.KEY_NETASSISTANT_IMSI);
        if (TextUtils.isEmpty(this.mImsi)) {
            finish();
        }
        if (getIntent().getBooleanExtra(CommonConstantUtil.KEY_NETASSISTANT_FIRST_SETTING, false)) {
            addCustomButton();
        }
        initPreferenceItem();
    }

    private void addCustomButton() {
        RelativeLayout layout = new RelativeLayout(this);
        Button button = new Button(this);
        button.setText(R.string.other_package_set_next);
        LayoutParams lp = new LayoutParams(-1, -2);
        button.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                HsmStat.statE(Events.E_NETASSISTANT_OTHER_PACKAGE_SET_NEXT);
                OtherTrafficSettingsActivity.this.finish();
            }
        });
        lp.addRule(12);
        lp.addRule(14);
        int marginStartAndEnd = getResources().getDimensionPixelSize(R.dimen.list_item_padding_start_end_emui);
        lp.setMargins((int) getRawSize(0, (float) marginStartAndEnd), 0, (int) getRawSize(0, (float) marginStartAndEnd), (int) getRawSize(0, (float) getResources().getDimensionPixelSize(R.dimen.net_assistant_fst_operator_marginbottom)));
        button.setId(R.id.systemmanager_other_package_set_next);
        layout.addView(button, lp);
        addContentView(layout, new LayoutParams(-1, -1));
    }

    public float getRawSize(int unit, float value) {
        return TypedValue.applyDimension(unit, value, getResources().getDisplayMetrics());
    }

    protected void onResume() {
        super.onResume();
        new HelpAsyncTask(4).execute(new Void[0]);
    }

    private void initPreferenceItem() {
        this.mExtraTraffic = (TextArrowPreference) findPreference(EXTRA_TRAFFIC_PACKAGE_KEY);
        this.mRoamingTraffic = (TextArrowPreference) findPreference(ROAMING_TRAFFIC_KEY);
        this.mLeisureTime = (TextArrowPreference) findPreference(LEISURE_TIME_TRAFFIC_KEY);
        this.mNoTrafficApp = (TextArrowPreference) findPreference(NO_TRAFFIC_APP_KEY);
    }

    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        ViewGroup container = (ViewGroup) findViewById(16908290);
        if (preference == this.mExtraTraffic) {
            NetAssistantDialogManager.createTrafficSettingsDialog(this, preference, new TrafficSetListener() {
                public void onSet(float size, String unit) {
                    HsmStat.statE(Events.E_NETASSISTANT_SET_EXTRA);
                    new HelpAsyncTask(11, size, unit).execute(new Void[0]);
                }
            }, GlobalContext.getString(R.string.extra_traffic_package_title), container);
            return true;
        } else if (preference == this.mRoamingTraffic) {
            HsmStat.statE(Events.E_NETASSISTANT_ENTER_ROAMING);
            NetAssistantDialogManager.createTrafficSettingsDialog(this, preference, new TrafficSetListener() {
                public void onSet(float size, String unit) {
                    new HelpAsyncTask(12, size, unit).execute(new Void[0]);
                }
            }, GlobalContext.getString(R.string.roaming_traffic_title), container);
            return true;
        } else if (preference == this.mLeisureTime) {
            HsmStat.statE(Events.E_NETASSISTANT_ENTER_LEISURE);
            i = new Intent();
            i.setClass(this, LeisureTrafficSettingsActivity.class);
            i.putExtra(CommonConstantUtil.KEY_NETASSISTANT_IMSI, this.mImsi);
            startActivity(i);
            return true;
        } else if (preference != this.mNoTrafficApp) {
            return false;
        } else {
            i = new Intent();
            i.setClass(this, NoTrafficAppActivity.class);
            i.putExtra(CommonConstantUtil.KEY_NETASSISTANT_IMSI, this.mImsi);
            startActivity(i);
            return true;
        }
    }
}
