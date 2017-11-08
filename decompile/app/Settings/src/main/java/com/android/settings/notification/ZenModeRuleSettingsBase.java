package com.android.settings.notification;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.AutomaticZenRule;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import com.android.internal.logging.MetricsLogger;
import com.android.settings.ItemUseStat;

public abstract class ZenModeRuleSettingsBase extends ZenModeSettingsBase implements OnPreferenceChangeListener {
    protected static final boolean DEBUG = ZenModeSettingsBase.DEBUG;
    private static final String[] ENTRY_VALUES_ZEN_MODE = new String[]{"ZEN_MODE_IMPORTANT_INTERRUPTIONS", "ZEN_MODE_ALARMS", "ZEN_MODE_NO_INTERRUPTIONS"};
    protected Context mContext;
    private AlertDialog mDeleteRuleDialog;
    protected boolean mDeleting;
    protected boolean mDisableListeners;
    protected String mId;
    protected AutomaticZenRule mRule;
    private Preference mRuleName;
    private int mTitle;
    private ListPreference mZenMode;

    protected abstract void onCreateInternal();

    protected abstract boolean setRule(AutomaticZenRule automaticZenRule);

    protected abstract void updateControlsInternal();

    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        this.mContext = getActivity();
        Intent intent = getActivity().getIntent();
        if (DEBUG) {
            Log.d("ZenModeSettings", "onCreate getIntent()=" + intent);
        }
        if (intent == null) {
            Log.w("ZenModeSettings", "No intent");
            toastAndFinish();
            return;
        }
        this.mId = intent.getStringExtra("android.service.notification.extra.RULE_ID");
        if (DEBUG) {
            Log.d("ZenModeSettings", "mId=" + this.mId);
        }
        if (!refreshRuleOrFinish()) {
            setHasOptionsMenu(true);
            onCreateInternal();
        }
    }

    public void onResume() {
        super.onResume();
        if (!isUiRestricted()) {
            updateControls();
        }
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    public void onDestroyView() {
        super.onDestroyView();
        if (this.mDeleteRuleDialog != null && this.mDeleteRuleDialog.isShowing()) {
            this.mDeleteRuleDialog.dismiss();
        }
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        return false;
    }

    protected void updateRule(Uri newConditionId) {
        this.mRule.setConditionId(newConditionId);
        setZenRule(this.mId, this.mRule);
    }

    protected void onZenModeChanged() {
    }

    protected void onZenModeConfigChanged() {
        if (!refreshRuleOrFinish()) {
            updateControls();
        }
    }

    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (DEBUG) {
            Log.d("ZenModeSettings", "onCreateOptionsMenu");
        }
        inflater.inflate(2132017163, menu);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        if (DEBUG) {
            Log.d("ZenModeSettings", "onOptionsItemSelected " + item.getItemId());
        }
        if (item.getItemId() != 2131887382) {
            return super.onOptionsItemSelected(item);
        }
        MetricsLogger.action(this.mContext, 174);
        showDeleteRuleDialog();
        return true;
    }

    private boolean refreshRuleOrFinish() {
        this.mRule = getZenRule();
        if (DEBUG) {
            Log.d("ZenModeSettings", "mRule=" + this.mRule);
        }
        if (setRule(this.mRule)) {
            return false;
        }
        toastAndFinish();
        return true;
    }

    public void setDeleteTitle(int titleId) {
        this.mTitle = titleId;
    }

    private void showDeleteRuleDialog() {
        if (this.mRule != null) {
            String defaultRuleName = this.mContext.getResources().getString(this.mTitle);
            if (this.mDeleteRuleDialog != null && this.mDeleteRuleDialog.isShowing()) {
                this.mDeleteRuleDialog.dismiss();
            }
            this.mDeleteRuleDialog = new Builder(this.mContext).setMessage(getString(2131626785, new Object[]{defaultRuleName})).setNegativeButton(2131624572, null).setPositiveButton(2131626786, new OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    MetricsLogger.action(ZenModeRuleSettingsBase.this.mContext, 175);
                    ZenModeRuleSettingsBase.this.mDeleting = true;
                    ZenModeRuleSettingsBase.this.removeZenRule(ZenModeRuleSettingsBase.this.mId);
                    ItemUseStat.getInstance().handleClick(ZenModeRuleSettingsBase.this.getActivity(), 2, "delete_automation_rule");
                }
            }).show();
            Button button = this.mDeleteRuleDialog.getButton(-1);
            if (button != null) {
                button.setTextColor(-65536);
            }
            View messageView = this.mDeleteRuleDialog.findViewById(16908299);
            if (messageView != null) {
                messageView.setTextDirection(5);
            }
        }
    }

    protected void toastAndFinish() {
        if (!this.mDeleting) {
            Toast.makeText(this.mContext, 2131626803, 0).show();
        }
        getActivity().finish();
    }

    private void updateRuleName() {
        CharSequence defaultRuleName = ZenModeUtils.getDefaultRuleName(this.mContext, this.mRule.getName());
        if (getActivity() != null) {
            getActivity().setTitle(defaultRuleName);
        }
        if (this.mRuleName != null) {
            this.mRuleName.setSummary(defaultRuleName);
        }
    }

    private AutomaticZenRule getZenRule() {
        if (this.mId == null) {
            return null;
        }
        return NotificationManager.from(this.mContext).getAutomaticZenRule(this.mId);
    }

    private void updateControls() {
        if (this.mRule != null) {
            this.mDisableListeners = true;
            updateRuleName();
            updateControlsInternal();
            ZenModeUtils.setSelectedValue(this.mZenMode, String.valueOf(this.mRule.getInterruptionFilter()));
            this.mDisableListeners = false;
        }
    }
}
