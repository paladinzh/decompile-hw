package com.android.settings.notification;

import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.AsyncTask;
import android.os.Bundle;
import com.android.settings.utils.ManagedServiceSettings;
import com.android.settings.utils.ManagedServiceSettings.Config;

public class NotificationAccessSettings extends ManagedServiceSettings {
    private static final Config CONFIG = getNotificationListenerConfig();
    private static final String TAG = NotificationAccessSettings.class.getSimpleName();

    public static class FriendlyWarningDialogFragment extends DialogFragment {
        public FriendlyWarningDialogFragment setServiceInfo(ComponentName cn, String label, Fragment target) {
            Bundle args = new Bundle();
            args.putString("c", cn.flattenToString());
            args.putString("l", label);
            setTargetFragment(target, 0);
            setArguments(args);
            return this;
        }

        public Dialog onCreateDialog(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            Bundle args = getArguments();
            String label = args.getString("l");
            final ComponentName cn = ComponentName.unflattenFromString(args.getString("c"));
            final NotificationAccessSettings parent = (NotificationAccessSettings) getTargetFragment();
            return new Builder(getContext()).setMessage(getResources().getString(2131626756, new Object[]{label})).setCancelable(true).setPositiveButton(2131626757, new OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    parent.mServiceListing.setEnabled(cn, false);
                    NotificationAccessSettings.deleteRules(FriendlyWarningDialogFragment.this.getContext(), cn);
                }
            }).setNegativeButton(2131626758, new OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                }
            }).create();
        }
    }

    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
    }

    private static Config getNotificationListenerConfig() {
        Config c = new Config();
        c.tag = TAG;
        c.setting = "enabled_notification_listeners";
        c.intentAction = "android.service.notification.NotificationListenerService";
        c.permission = "android.permission.BIND_NOTIFICATION_LISTENER_SERVICE";
        c.noun = "notification listener";
        c.warningDialogTitle = 2131626754;
        c.warningDialogSummary = 2131626755;
        c.emptyText = 2131626753;
        return c;
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (getActivity().getActionBar() != null) {
            getActivity().getActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    protected int getMetricsCategory() {
        return 179;
    }

    protected Config getConfig() {
        return CONFIG;
    }

    protected boolean setEnabled(ComponentName service, String title, boolean enable) {
        if (enable) {
            return super.setEnabled(service, title, enable);
        }
        if (!this.mServiceListing.isEnabled(service)) {
            return true;
        }
        new FriendlyWarningDialogFragment().setServiceInfo(service, title, this).show(getFragmentManager(), "friendlydialog");
        return false;
    }

    private static void deleteRules(final Context context, final ComponentName cn) {
        AsyncTask.execute(new Runnable() {
            public void run() {
                NotificationManager mgr = (NotificationManager) context.getSystemService(NotificationManager.class);
                if (!mgr.isNotificationPolicyAccessGrantedForPackage(cn.getPackageName())) {
                    mgr.removeAutomaticZenRules(cn.getPackageName());
                }
            }
        });
    }
}
