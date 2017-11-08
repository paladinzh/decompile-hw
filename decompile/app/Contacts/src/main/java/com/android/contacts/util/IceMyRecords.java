package com.android.contacts.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import com.android.contacts.hap.HwCustCommonConstants;
import com.google.android.gms.R;

public class IceMyRecords {
    private Context context;
    private String[] my_info_title;
    private String[] my_info_values = new String[5];

    public IceMyRecords(Context c) {
        this.context = c;
        this.my_info_title = this.context.getResources().getStringArray(R.array.ice_my_info_fields);
        fetchSavedDataFromSharedPref();
    }

    public void fetchSavedDataFromSharedPref() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this.context);
        this.my_info_values[0] = sp.getString(HwCustCommonConstants.ICE_MY_INFO_SP_NAME, "").trim();
        this.my_info_values[1] = sp.getString(HwCustCommonConstants.ICE_MY_INFO_SP_HEALTH_RECORD, "").trim();
        this.my_info_values[2] = sp.getString(HwCustCommonConstants.ICE_MY_INFO_SP_ALLERGIES, "").trim();
        this.my_info_values[3] = sp.getString(HwCustCommonConstants.ICE_MY_INFO_SP_CURRENT_MEDICATION, "").trim();
        this.my_info_values[4] = sp.getString(HwCustCommonConstants.ICE_MY_INFO_SP_OTHER, "").trim();
    }

    public void saveDataToSharedPref(String[] values) {
        Editor editor = PreferenceManager.getDefaultSharedPreferences(this.context).edit();
        editor.putString(HwCustCommonConstants.ICE_MY_INFO_SP_NAME, values[0]);
        editor.putString(HwCustCommonConstants.ICE_MY_INFO_SP_HEALTH_RECORD, values[1]);
        editor.putString(HwCustCommonConstants.ICE_MY_INFO_SP_ALLERGIES, values[2]);
        editor.putString(HwCustCommonConstants.ICE_MY_INFO_SP_CURRENT_MEDICATION, values[3]);
        editor.putString(HwCustCommonConstants.ICE_MY_INFO_SP_OTHER, values[4]);
        editor.commit();
    }

    public String[] getMyRecordValues() {
        return (String[]) this.my_info_values.clone();
    }

    public String[] getMyRecordTitles() {
        return (String[]) this.my_info_title.clone();
    }

    public void setMyRecordValue(String val, int position) {
        this.my_info_values[position] = val;
    }
}
