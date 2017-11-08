package com.android.settings.inputmethod;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.support.v7.preference.Preference.OnPreferenceClickListener;
import android.support.v7.preference.PreferenceScreen;
import android.support.v7.preference.TwoStatePreference;
import android.view.textservice.SpellCheckerInfo;
import android.view.textservice.SpellCheckerSubtype;
import android.view.textservice.TextServicesManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.android.settings.ItemUseStat;
import com.android.settings.SettingsPreferenceFragment;

public class SpellCheckersSettings extends SettingsPreferenceFragment implements OnPreferenceClickListener, OnPreferenceChangeListener {
    private static final String TAG = SpellCheckersSettings.class.getSimpleName();
    private SpellCheckerInfo mCurrentSci;
    private AlertDialog mDialog = null;
    private SpellCheckerInfo[] mEnabledScis;
    private Preference mSpellCheckerLanaguagePref;
    private TwoStatePreference mSwitch;
    private TextServicesManager mTsm;

    protected int getMetricsCategory() {
        return 59;
    }

    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        addPreferencesFromResource(2131230903);
        this.mSpellCheckerLanaguagePref = findPreference("spellchecker_language");
        this.mSpellCheckerLanaguagePref.setOnPreferenceClickListener(this);
        this.mTsm = (TextServicesManager) getSystemService("textservices");
        this.mCurrentSci = this.mTsm.getCurrentSpellChecker();
        this.mEnabledScis = this.mTsm.getEnabledSpellCheckers();
        populatePreferenceScreen();
        this.mSwitch = (TwoStatePreference) findPreference("spellchecker_switch");
        this.mSwitch.setChecked(this.mTsm.isSpellCheckerEnabled());
        this.mSwitch.setOnPreferenceChangeListener(this);
    }

    public void onDestroy() {
        super.onDestroy();
        if (this.mDialog != null && this.mDialog.isShowing()) {
            this.mDialog.dismiss();
        }
    }

    private void populatePreferenceScreen() {
        int count = 0;
        SpellCheckerPreference pref = new SpellCheckerPreference(getPrefContext(), this.mEnabledScis);
        pref.setTitle(2131627179);
        if (this.mEnabledScis != null) {
            count = this.mEnabledScis.length;
        }
        if (count > 0) {
            pref.setSummary("%s");
        } else {
            pref.setSummary(2131627181);
        }
        pref.setKey("default_spellchecker");
        pref.setOnPreferenceChangeListener(this);
        getPreferenceScreen().addPreference(pref);
    }

    public void onResume() {
        super.onResume();
        updatePreferenceScreen();
    }

    public void onPause() {
        super.onPause();
    }

    private void updatePreferenceScreen() {
        SpellCheckerSubtype currentSpellCheckerSubtype;
        boolean z = false;
        this.mCurrentSci = this.mTsm.getCurrentSpellChecker();
        boolean isSpellCheckerEnabled = this.mTsm.isSpellCheckerEnabled();
        if (this.mCurrentSci != null) {
            currentSpellCheckerSubtype = this.mTsm.getCurrentSpellCheckerSubtype(false);
        } else {
            currentSpellCheckerSubtype = null;
        }
        this.mSpellCheckerLanaguagePref.setSummary(getSpellCheckerSubtypeLabel(this.mCurrentSci, currentSpellCheckerSubtype));
        PreferenceScreen screen = getPreferenceScreen();
        int count = screen.getPreferenceCount();
        for (int index = 0; index < count; index++) {
            Preference preference = screen.getPreference(index);
            if (!"spellchecker_switch".equals(preference.getKey())) {
                preference.setEnabled(isSpellCheckerEnabled);
            }
            if (preference instanceof SpellCheckerPreference) {
                ((SpellCheckerPreference) preference).setSelected(this.mCurrentSci);
            }
        }
        Preference preference2 = this.mSpellCheckerLanaguagePref;
        if (isSpellCheckerEnabled && this.mCurrentSci != null) {
            z = true;
        }
        preference2.setEnabled(z);
    }

    private CharSequence getSpellCheckerSubtypeLabel(SpellCheckerInfo sci, SpellCheckerSubtype subtype) {
        if (sci == null) {
            return getString(2131627181);
        }
        if (subtype == null) {
            return getString(2131625819);
        }
        return subtype.getDisplayName(getActivity(), sci.getPackageName(), sci.getServiceInfo().applicationInfo);
    }

    public boolean onPreferenceClick(Preference pref) {
        if (pref != this.mSpellCheckerLanaguagePref) {
            return false;
        }
        showChooseLanguageDialog();
        ItemUseStat.getInstance().handleNonTwoStatePreferenceClick(getActivity(), pref);
        return true;
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if ("spellchecker_switch".equals(preference.getKey())) {
            this.mTsm.setSpellCheckerEnabled(((Boolean) newValue).booleanValue());
            updatePreferenceScreen();
            ItemUseStat.getInstance().handleTwoStatePreferenceClick(getActivity(), preference, newValue);
            return true;
        } else if (!(preference instanceof SpellCheckerPreference)) {
            return false;
        } else {
            boolean isSystemApp;
            SpellCheckerInfo sci = (SpellCheckerInfo) newValue;
            if ((sci.getServiceInfo().applicationInfo.flags & 1) != 0) {
                isSystemApp = true;
            } else {
                isSystemApp = false;
            }
            if (isSystemApp) {
                changeCurrentSpellChecker(sci);
                return true;
            }
            showSecurityWarnDialog(sci);
            return false;
        }
    }

    private static int convertSubtypeIndexToDialogItemId(int index) {
        return index + 1;
    }

    private static int convertDialogItemIdToSubtypeIndex(int item) {
        return item - 1;
    }

    private void showChooseLanguageDialog() {
        if (this.mDialog != null && this.mDialog.isShowing()) {
            this.mDialog.dismiss();
        }
        final SpellCheckerInfo currentSci = this.mTsm.getCurrentSpellChecker();
        if (currentSci != null) {
            SpellCheckerSubtype currentScs = this.mTsm.getCurrentSpellCheckerSubtype(false);
            Builder builder = new Builder(getActivity());
            builder.setTitle(2131625749);
            int subtypeCount = currentSci.getSubtypeCount();
            CharSequence[] items = new CharSequence[(subtypeCount + 1)];
            items[0] = getSpellCheckerSubtypeLabel(currentSci, null);
            int checkedItemId = 0;
            for (int index = 0; index < subtypeCount; index++) {
                SpellCheckerSubtype subtype = currentSci.getSubtypeAt(index);
                int itemId = convertSubtypeIndexToDialogItemId(index);
                items[itemId] = getSpellCheckerSubtypeLabel(currentSci, subtype);
                if (subtype.equals(currentScs)) {
                    checkedItemId = itemId;
                }
            }
            builder.setSingleChoiceItems(items, checkedItemId, new OnClickListener() {
                public void onClick(DialogInterface dialog, int item) {
                    if (item == 0) {
                        SpellCheckersSettings.this.mTsm.setSpellCheckerSubtype(null);
                    } else {
                        SpellCheckersSettings.this.mTsm.setSpellCheckerSubtype(currentSci.getSubtypeAt(SpellCheckersSettings.convertDialogItemIdToSubtypeIndex(item)));
                    }
                    dialog.dismiss();
                    SpellCheckersSettings.this.updatePreferenceScreen();
                }
            });
            this.mDialog = builder.create();
            this.mDialog.show();
        }
    }

    private void showSecurityWarnDialog(final SpellCheckerInfo sci) {
        if (this.mDialog != null && this.mDialog.isShowing()) {
            this.mDialog.dismiss();
        }
        Builder builder = new Builder(getActivity());
        builder.setTitle(17039380);
        builder.setMessage(getString(2131625760, new Object[]{sci.loadLabel(getPackageManager())}));
        builder.setCancelable(true);
        builder.setPositiveButton(17039370, new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                SpellCheckersSettings.this.changeCurrentSpellChecker(sci);
            }
        });
        builder.setNegativeButton(17039360, new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        this.mDialog = builder.create();
        this.mDialog.show();
    }

    private void changeCurrentSpellChecker(SpellCheckerInfo sci) {
        this.mTsm.setCurrentSpellChecker(sci);
        updatePreferenceScreen();
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        LinearLayout emptyView = (LinearLayout) getView().findViewById(2131886922);
        Button emptyBtn = (Button) getView().findViewById(2131886923);
        TextView emptyTv = (TextView) getView().findViewById(2131886561);
        ((ImageView) getView().findViewById(2131886560)).setBackgroundResource(2130838684);
        emptyBtn.setVisibility(8);
        emptyTv.setText(2131627583);
        setEmptyView(emptyView);
    }
}
