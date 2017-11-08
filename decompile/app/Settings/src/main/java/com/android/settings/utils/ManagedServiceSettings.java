package com.android.settings.utils;

import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.pm.PackageItemInfo.DisplayNameComparator;
import android.content.pm.PackageManager;
import android.content.pm.ServiceInfo;
import android.os.Bundle;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.support.v7.preference.PreferenceScreen;
import android.view.View;
import com.android.settings.notification.EmptyTextSettings;
import com.android.settings.utils.ServiceListing.Callback;
import java.util.Collections;
import java.util.List;

public abstract class ManagedServiceSettings extends EmptyTextSettings {
    private final Config mConfig = getConfig();
    protected Context mContext;
    private PackageManager mPM;
    protected ServiceListing mServiceListing;

    public static class Config {
        public int emptyText;
        public String intentAction;
        public String noun;
        public String permission;
        public String secondarySetting;
        public String setting;
        public String tag;
        public int warningDialogSummary;
        public int warningDialogTitle;
    }

    public static class ScaryWarningDialogFragment extends DialogFragment {
        public ScaryWarningDialogFragment setServiceInfo(ComponentName cn, String label, Fragment target) {
            Bundle args = new Bundle();
            args.putString("c", cn.flattenToString());
            args.putString("l", label);
            setArguments(args);
            setTargetFragment(target, 0);
            return this;
        }

        public Dialog onCreateDialog(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            Bundle args = getArguments();
            String label = args.getString("l");
            final ComponentName cn = ComponentName.unflattenFromString(args.getString("c"));
            final ManagedServiceSettings parent = (ManagedServiceSettings) getTargetFragment();
            return new Builder(getContext()).setMessage(getResources().getString(parent.mConfig.warningDialogSummary, new Object[]{label})).setTitle(getResources().getString(parent.mConfig.warningDialogTitle, new Object[]{label})).setCancelable(true).setPositiveButton(2131624351, new OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    parent.mServiceListing.setEnabled(cn, true);
                }
            }).setNegativeButton(2131624352, new OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                }
            }).create();
        }
    }

    protected abstract Config getConfig();

    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        this.mContext = getActivity();
        this.mPM = this.mContext.getPackageManager();
        this.mServiceListing = new ServiceListing(this.mContext, this.mConfig);
        this.mServiceListing.addCallback(new Callback() {
            public void onServicesReloaded(List<ServiceInfo> services) {
                ManagedServiceSettings.this.updateList(services);
            }
        });
        setPreferenceScreen(getPreferenceManager().createPreferenceScreen(this.mContext));
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setEmptyText(this.mConfig.emptyText);
    }

    public void onResume() {
        super.onResume();
        this.mServiceListing.reload();
        this.mServiceListing.setListening(true);
    }

    public void onPause() {
        super.onPause();
        this.mServiceListing.setListening(false);
    }

    private void updateList(List<ServiceInfo> services) {
        PreferenceScreen screen = getPreferenceScreen();
        screen.removeAll();
        Collections.sort(services, new DisplayNameComparator(this.mPM));
        for (ServiceInfo service : services) {
            final ComponentName cn = new ComponentName(service.packageName, service.name);
            final String title = service.loadLabel(this.mPM).toString();
            SwitchPreference pref = new SwitchPreference(getPrefContext());
            pref.setPersistent(false);
            pref.setIcon(service.loadIcon(this.mPM));
            pref.setTitle((CharSequence) title);
            pref.setChecked(this.mServiceListing.isEnabled(cn));
            pref.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    return ManagedServiceSettings.this.setEnabled(cn, title, ((Boolean) newValue).booleanValue());
                }
            });
            screen.addPreference(pref);
        }
    }

    protected boolean setEnabled(ComponentName service, String title, boolean enable) {
        if (!enable) {
            this.mServiceListing.setEnabled(service, false);
            return true;
        } else if (this.mServiceListing.isEnabled(service)) {
            return true;
        } else {
            new ScaryWarningDialogFragment().setServiceInfo(service, title, this).show(getFragmentManager(), "dialog");
            return false;
        }
    }
}
