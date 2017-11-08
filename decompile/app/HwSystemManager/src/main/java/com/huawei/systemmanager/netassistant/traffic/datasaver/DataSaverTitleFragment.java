package com.huawei.systemmanager.netassistant.traffic.datasaver;

import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.view.View;
import com.huawei.systemmanager.R;

public class DataSaverTitleFragment extends PreferenceFragment {
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.data_saver_sub_title_preference);
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getListView().setDivider(null);
    }
}
