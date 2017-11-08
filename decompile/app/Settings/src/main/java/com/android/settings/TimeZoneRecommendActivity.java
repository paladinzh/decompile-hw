package com.android.settings;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnDismissListener;
import android.os.Bundle;
import android.provider.SettingsEx.Systemex;
import android.telephony.MSimTelephonyManager;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;
import com.android.settings.TimeZoneUtil.TimeZoneTips;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

public class TimeZoneRecommendActivity extends Activity {
    private AlertDialog mDialog;
    private TextView mNoMatchResultSView;
    private EditText mSearchText;
    private int mTimeZoneIdx = 0;
    private TimeZoneItemAdapter mZoneItemAdpater;
    private List<TimeZoneTips> mZoneTips;
    private List<TimeZoneTips> mZoneTipsCache;

    private class TimeZoneItemAdapter extends ArrayAdapter<TimeZoneTips> {
        private LayoutInflater mInflater;

        public TimeZoneItemAdapter(Context context, List<TimeZoneTips> tips) {
            super(context, 2130968764, tips);
            this.mInflater = LayoutInflater.from(context);
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = this.mInflater.inflate(2130968764, null);
            }
            TimeZoneTips tip = (TimeZoneTips) getItem(position);
            TextView zoneName = (TextView) convertView.findViewById(2131886544);
            if (zoneName != null) {
                zoneName.setText(tip.getZoneName());
            }
            TextView zoneTime = (TextView) convertView.findViewById(2131886545);
            if (zoneTime != null) {
                zoneTime.setText(tip.getZoneTime());
            }
            if (position == TimeZoneRecommendActivity.this.mTimeZoneIdx) {
                ((RadioButton) convertView.findViewById(2131886546)).setChecked(true);
            } else {
                ((RadioButton) convertView.findViewById(2131886546)).setChecked(false);
            }
            return convertView;
        }
    }

    private class TimeZoneItemClickListener implements OnItemClickListener {
        private TimeZoneItemClickListener() {
        }

        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            View selectedView = ((ListView) parent).getChildAt(TimeZoneRecommendActivity.this.mTimeZoneIdx);
            if (selectedView != null) {
                ((RadioButton) selectedView.findViewById(2131886546)).setChecked(false);
            }
            ((RadioButton) view.findViewById(2131886546)).setChecked(true);
            TimeZoneRecommendActivity.this.mTimeZoneIdx = (int) id;
        }
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        popDialog();
    }

    private void popDialog() {
        LinearLayout contentView = (LinearLayout) ((LayoutInflater) getSystemService("layout_inflater")).inflate(2130968765, null);
        String countryCode = getIntent().getStringExtra("iso");
        if (countryCode == null || countryCode.isEmpty()) {
            if (TelephonyManager.getDefault().isMultiSimEnabled()) {
                int mainCard = Utils.getMainCardSlotId();
                MSimTelephonyManager.getDefault();
                countryCode = MSimTelephonyManager.getTelephonyProperty("gsm.operator.iso-country", mainCard, null);
            } else {
                countryCode = TelephonyManager.getDefault().getNetworkCountryIso();
            }
        }
        this.mZoneTipsCache = TimeZoneUtil.queryTimeZoneByCountry(countryCode, this);
        this.mZoneTips = new ArrayList(this.mZoneTipsCache);
        this.mTimeZoneIdx = getDefaultSelPos();
        this.mSearchText = (EditText) contentView.findViewById(2131886547);
        this.mSearchText.setOnEditorActionListener(null);
        this.mNoMatchResultSView = (TextView) contentView.findViewById(2131886550);
        this.mSearchText.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
                TimeZoneRecommendActivity.this.search(TimeZoneRecommendActivity.this.mSearchText.getText().toString());
                if (TimeZoneRecommendActivity.this.mZoneTips.size() == 0) {
                    TimeZoneRecommendActivity.this.mNoMatchResultSView.setVisibility(0);
                } else {
                    TimeZoneRecommendActivity.this.mNoMatchResultSView.setVisibility(8);
                }
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
        });
        ListView zoneItemListView = (ListView) contentView.findViewById(2131886549);
        this.mZoneItemAdpater = new TimeZoneItemAdapter(this, this.mZoneTips);
        zoneItemListView.setAdapter(this.mZoneItemAdpater);
        zoneItemListView.setChoiceMode(1);
        zoneItemListView.setOnItemClickListener(new TimeZoneItemClickListener());
        this.mDialog = new Builder(this).setTitle(2131629067).setView(contentView).setPositiveButton(17039370, new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                try {
                    TimeZoneTips tip = (TimeZoneTips) TimeZoneRecommendActivity.this.mZoneTips.get(TimeZoneRecommendActivity.this.mTimeZoneIdx);
                    String oldTimeZoneID = TimeZone.getDefault().getID();
                    if (!(tip == null || tip.getZoneID().equals(oldTimeZoneID))) {
                        ((AlarmManager) TimeZoneRecommendActivity.this.getSystemService("alarm")).setTimeZone(tip.getZoneID());
                    }
                } catch (IndexOutOfBoundsException e) {
                    Log.e("TimeZoneRecommendActivity", "IndexOutOfBoundsException");
                }
                TimeZoneRecommendActivity.this.finish();
            }
        }).setNegativeButton(17039360, new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                TimeZoneRecommendActivity.this.finish();
            }
        }).create();
        this.mDialog.setOnDismissListener(new OnDismissListener() {
            public void onDismiss(DialogInterface dialog) {
                TimeZoneRecommendActivity.this.finish();
            }
        });
        this.mDialog.show();
    }

    private int getDefaultSelPos() {
        String defaultID = Systemex.getString(getContentResolver(), "localtime_zone_id");
        for (int i = 0; i < this.mZoneTips.size(); i++) {
            if (((TimeZoneTips) this.mZoneTips.get(i)).getZoneID().equals(defaultID)) {
                return i;
            }
        }
        return 0;
    }

    private void search(String condition) {
        if (condition != null && condition.length() > 0) {
            this.mZoneTips.clear();
            for (TimeZoneTips tip : this.mZoneTipsCache) {
                if (tip.getZoneName().toUpperCase(getResources().getConfiguration().locale).contains(condition)) {
                    this.mZoneTips.add(tip);
                }
            }
            this.mTimeZoneIdx = getDefaultSelPos();
            this.mZoneItemAdpater.notifyDataSetChanged();
        }
    }
}
