package com.huawei.systemmanager.netassistant.traffic.datasaver;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceFragment;
import android.preference.SwitchPreference;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ListView;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.hsmstat.HsmStat;
import com.huawei.systemmanager.hsmstat.base.HsmStatConst.Events;
import com.huawei.systemmanager.netassistant.traffic.datasaver.IDataSaver.View;

public class DataSaverSwitchFragment extends PreferenceFragment implements View {
    private OnPreferenceChangeListener mDataSaverPreferenceChangeListener = new OnPreferenceChangeListener() {
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            if (preference.getKey().equals(DataSaverConstants.KEY_PREF_DATA_SAVER_SWITCH) && DataSaverSwitchFragment.this.mDateSaverManager != null) {
                boolean opened = ((Boolean) newValue).booleanValue();
                DataSaverSwitchFragment.this.mDateSaverManager.setDataSaverEnable(opened);
                if (opened) {
                    HsmStat.statE(Events.E_DATA_SAVER_OPEN_CLICKED);
                } else {
                    HsmStat.statE(Events.E_DATA_SAVER_CLOSE_CLICKED);
                }
            }
            return true;
        }
    };
    private SwitchPreference mDataSaverSwitch;
    private DataSaverManager mDateSaverManager;

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ListView lv = (ListView) getActivity().findViewById(16908298);
        if (lv != null) {
            lv.setDivider(null);
        }
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.data_saver_preference);
        this.mDateSaverManager = new DataSaverManager(getContext(), this);
    }

    public void onResume() {
        super.onResume();
        if (this.mDateSaverManager != null) {
            this.mDateSaverManager.registerListener();
        }
    }

    public void onPause() {
        super.onPause();
        if (this.mDateSaverManager != null) {
            this.mDateSaverManager.unRegisterListener();
        }
    }

    public void onDestroy() {
        super.onDestroy();
        if (this.mDateSaverManager != null) {
            this.mDateSaverManager.release();
        }
    }

    public android.view.View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    public void onViewCreated(android.view.View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.mDataSaverSwitch = (SwitchPreference) findPreference(DataSaverConstants.KEY_PREF_DATA_SAVER_SWITCH);
        this.mDataSaverSwitch.setOnPreferenceChangeListener(this.mDataSaverPreferenceChangeListener);
    }

    public void onWhiteListedChanged(int uid, boolean whiteListed) {
    }

    public void onBlacklistedChanged(int uid, boolean blackListed) {
    }

    public void onDataSaverStateChanged(boolean enable) {
        if (this.mDataSaverSwitch != null) {
            this.mDataSaverSwitch.setChecked(enable);
        }
    }
}
