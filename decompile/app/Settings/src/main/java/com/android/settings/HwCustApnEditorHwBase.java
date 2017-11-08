package com.android.settings;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.support.v7.preference.DialogPreference;
import android.support.v7.preference.EditTextPreference;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.PreferenceScreen;

public class HwCustApnEditorHwBase {
    public ApnEditorHwBase mApnEditorHwBase;

    public HwCustApnEditorHwBase(ApnEditorHwBase apnEditorHwBase) {
        this.mApnEditorHwBase = apnEditorHwBase;
    }

    public String getApnDisplayTitle(Context context, String name) {
        return name;
    }

    public boolean isApnReadable(Context context, int subId) {
        return false;
    }

    public boolean isSprintConvertibleApn(Context context, String apnName) {
        return false;
    }

    public Uri getApnUri(Uri apn_uri, int sub) {
        return apn_uri;
    }

    public void removeApnMvno(ListPreference mMvnoType, EditTextPreference mMvnoMatchData, PreferenceScreen mPreferenceScreen) {
    }

    public boolean disableProtocol() {
        return true;
    }

    public void custForApnBearer(PreferenceScreen mPreferenceScreen, DialogPreference mBearer) {
    }

    public void setDefaultPort(Context context, EditTextPreference mPort) {
    }

    public boolean isDunApnEditableAndDeletable(Context context, Cursor cursor) {
        return false;
    }

    public String[] getDefaultProtocol(Context context, String curMccmnc) {
        return new String[0];
    }

    public void setDefaultApnType(EditTextPreference editTextPreference) {
    }
}
