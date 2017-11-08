package com.android.mms.ui;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.view.MenuItem;
import com.android.mms.MmsConfig;
import com.android.mms.util.SmileyParser;
import com.android.mms.util.SmileyParser.SMILEY_TYPE;
import com.google.android.gms.R;
import com.huawei.mms.ui.HwPreferenceActivity;
import com.huawei.mms.ui.HwPreferenceFragment;
import com.huawei.mms.util.StatisticalHelper;

public class AddSignature extends HwPreferenceActivity {

    public static class AddSignatureFragment extends HwPreferenceFragment implements OnPreferenceChangeListener {
        private Activity mActivity;
        private EmuiSwitchPreference mSignature;
        private Preference mSignatureEdit;
        private SignatureEditDialog mSignatureEditDialog;

        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            this.mActivity = getActivity();
            addPreferencesFromResource(R.xml.add_signature_settings);
            setPreference();
        }

        private void setPreference() {
            this.mSignature = (EmuiSwitchPreference) findPreference("pref_key_signature");
            this.mSignature.setOnPreferenceChangeListener(this);
            this.mSignatureEdit = findPreference("pref_key_signature_edit");
            if (MmsConfig.isShowSignatureDialog() && this.mSignatureEdit != null) {
                this.mSignatureEdit.setSummary(SmileyParser.getInstance().addSmileySpans(PreferenceManager.getDefaultSharedPreferences(this.mActivity).getString("pref_key_signature_content", ""), SMILEY_TYPE.LIST_TEXTVIEW));
            }
        }

        public boolean onPreferenceChange(Preference preference, Object newValue) {
            String str;
            Boolean isChecked = (Boolean) newValue;
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this.mActivity);
            Context context = this.mActivity;
            if (isChecked.booleanValue()) {
                str = "on";
            } else {
                str = "off";
            }
            StatisticalHelper.reportEvent(context, 2136, str);
            sp.edit().putBoolean("pref_key_signature", this.mSignature.isChecked());
            return true;
        }

        public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
            int themeID = getResources().getIdentifier("androidhwext:style/Theme.Emui.Dialog.Alert", null, null);
            if (themeID == 0) {
                themeID = 3;
            }
            if (preference == this.mSignatureEdit) {
                if (this.mSignatureEditDialog == null) {
                    this.mSignatureEditDialog = new SignatureEditDialog(this.mActivity, themeID, R.string.signature_edit_dialog_title, this.mSignatureEdit);
                }
                if (!this.mSignatureEditDialog.isShowing()) {
                    this.mSignatureEditDialog.show();
                }
            }
            return super.onPreferenceTreeClick(preferenceScreen, preference);
        }

        public void onDestroy() {
            if (this.mSignatureEditDialog != null) {
                this.mSignatureEditDialog.dismiss();
                this.mSignatureEditDialog = null;
            }
            super.onDestroy();
        }
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar actionBar = getActionBar();
        actionBar.setTitle(R.string.signature_title);
        actionBar.setDisplayHomeAsUpEnabled(true);
        getFragmentManager().beginTransaction().replace(16908290, new AddSignatureFragment()).commit();
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 16908332:
                onBackPressed();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
