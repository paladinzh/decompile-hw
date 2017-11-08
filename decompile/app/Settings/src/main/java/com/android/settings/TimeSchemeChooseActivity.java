package com.android.settings;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnDismissListener;
import android.os.Bundle;
import android.provider.Settings.SettingNotFoundException;
import android.provider.SettingsEx.Systemex;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import java.util.TimeZone;

public class TimeSchemeChooseActivity extends Activity {
    private AlertDialog mDialog;
    private String[] mDoubleTimeZone = new String[]{"Asia/Shanghai", "Asia/Shanghai"};
    private int mSchemeIdx = 0;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            this.mSchemeIdx = Systemex.getInt(getContentResolver(), "default_timescheme_idx");
        } catch (SettingNotFoundException e) {
            this.mSchemeIdx = 0;
        }
        if (this.mSchemeIdx > 1) {
            this.mSchemeIdx = 0;
        }
        String localZone = getIntent().getStringExtra("time-zone");
        if (localZone == null || localZone.isEmpty()) {
            localZone = Systemex.getString(getContentResolver(), "localtime_zone_id");
        }
        if (!(localZone == null || localZone.isEmpty())) {
            this.mDoubleTimeZone[1] = localZone;
        }
        popDialog();
    }

    private void popDialog() {
        LinearLayout contentView = (LinearLayout) ((LayoutInflater) getSystemService("layout_inflater")).inflate(2130968762, null);
        ListView listView = (ListView) contentView.findViewById(2131886541);
        listView.setChoiceMode(1);
        listView.setAdapter(new ArrayAdapter(this, 2130968763, 2131886542, getResources().getStringArray(2131362023)));
        listView.setItemChecked(this.mSchemeIdx, true);
        listView.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                TimeSchemeChooseActivity.this.mSchemeIdx = (int) id;
            }
        });
        this.mDialog = new Builder(this).setTitle(2131629063).setView(contentView).setPositiveButton(17039370, new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Systemex.putInt(TimeSchemeChooseActivity.this.getContentResolver(), "default_timescheme_idx", TimeSchemeChooseActivity.this.mSchemeIdx);
                String schemeZoneID = TimeSchemeChooseActivity.this.mDoubleTimeZone[TimeSchemeChooseActivity.this.mSchemeIdx];
                String displayZoneID = "";
                if (TimeSchemeChooseActivity.this.mSchemeIdx == 0) {
                    displayZoneID = TimeSchemeChooseActivity.this.mDoubleTimeZone[1];
                } else {
                    displayZoneID = TimeSchemeChooseActivity.this.mDoubleTimeZone[0];
                }
                Systemex.putString(TimeSchemeChooseActivity.this.getContentResolver(), "secondtime_city_name", TimeZoneUtil.getZoneNameByID(TimeSchemeChooseActivity.this, displayZoneID));
                String oldZoneID = TimeSchemeChooseActivity.this.getIntent().getStringExtra("time-zone");
                if (oldZoneID == null || oldZoneID.isEmpty()) {
                    oldZoneID = TimeZone.getDefault().getID();
                }
                if (!schemeZoneID.equals(oldZoneID)) {
                    ((AlarmManager) TimeSchemeChooseActivity.this.getSystemService("alarm")).setTimeZone(schemeZoneID);
                }
                TimeSchemeChooseActivity.this.finish();
            }
        }).setNegativeButton(17039360, new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                TimeSchemeChooseActivity.this.finish();
            }
        }).create();
        this.mDialog.setOnDismissListener(new OnDismissListener() {
            public void onDismiss(DialogInterface dialog) {
                TimeSchemeChooseActivity.this.finish();
            }
        });
        this.mDialog.show();
    }
}
