package com.android.settings;

import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceViewHolder;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;
import com.android.settingslib.dream.DreamBackend;
import com.android.settingslib.dream.DreamBackend.DreamInfo;
import java.util.List;

public class DreamSettings extends SettingsPreferenceFragment {
    private static final String TAG = DreamSettings.class.getSimpleName();
    private DreamBackend mBackend;
    private Context mContext;
    private MenuItem[] mMenuItemsWhenEnabled;
    private final PackageReceiver mPackageReceiver = new PackageReceiver();
    private boolean mRefreshing;

    private class DreamInfoPreference extends Preference {
        private final DreamInfo mInfo;

        public DreamInfoPreference(Context context, DreamInfo info) {
            super(context);
            this.mInfo = info;
            setLayoutResource(2130968761);
            setTitle(this.mInfo.caption);
            setIcon(this.mInfo.icon);
        }

        public void onBindViewHolder(final PreferenceViewHolder holder) {
            int i;
            int i2 = 0;
            super.onBindViewHolder(holder);
            RadioButton radioButton = (RadioButton) holder.findViewById(16908313);
            radioButton.setChecked(this.mInfo.isActive);
            radioButton.setOnTouchListener(new OnTouchListener() {
                public boolean onTouch(View v, MotionEvent event) {
                    holder.itemView.onTouchEvent(event);
                    return false;
                }
            });
            boolean showSettings = this.mInfo.settingsComponentName != null;
            View settingsDivider = holder.findViewById(2131886538);
            if (showSettings) {
                i = 0;
            } else {
                i = 4;
            }
            settingsDivider.setVisibility(i);
            ImageView settingsButton = (ImageView) holder.findViewById(16908314);
            if (!showSettings) {
                i2 = 4;
            }
            settingsButton.setVisibility(i2);
            settingsButton.setAlpha(this.mInfo.isActive ? 1.0f : 0.4f);
            settingsButton.setEnabled(this.mInfo.isActive);
            settingsButton.setFocusable(this.mInfo.isActive);
            settingsButton.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    DreamSettings.this.mBackend.launchSettings(DreamInfoPreference.this.mInfo);
                }
            });
        }

        public void performClick() {
            if (!this.mInfo.isActive) {
                for (int i = 0; i < DreamSettings.this.getPreferenceScreen().getPreferenceCount(); i++) {
                    DreamInfoPreference preference = (DreamInfoPreference) DreamSettings.this.getPreferenceScreen().getPreference(i);
                    preference.mInfo.isActive = false;
                    preference.notifyChanged();
                }
                this.mInfo.isActive = true;
                DreamSettings.this.mBackend.setActiveDream(this.mInfo.componentName);
                notifyChanged();
            }
        }
    }

    private class PackageReceiver extends BroadcastReceiver {
        private PackageReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            DreamSettings.logd("PackageReceiver.onReceive", new Object[0]);
            DreamSettings.this.refreshFromBackend();
        }
    }

    public int getHelpResource() {
        return 2131626548;
    }

    public void onAttach(Activity activity) {
        logd("onAttach(%s)", activity.getClass().getSimpleName());
        super.onAttach(activity);
        this.mContext = activity;
    }

    protected int getMetricsCategory() {
        return 47;
    }

    public void onCreate(Bundle icicle) {
        logd("onCreate(%s)", icicle);
        super.onCreate(icicle);
        this.mBackend = new DreamBackend(getActivity());
        setHasOptionsMenu(true);
    }

    public void onStart() {
        logd("onStart()", new Object[0]);
        super.onStart();
    }

    public void onDestroyView() {
        logd("onDestroyView()", new Object[0]);
        super.onDestroyView();
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        logd("onActivityCreated(%s)", savedInstanceState);
        super.onActivityCreated(savedInstanceState);
        TextView emptyView = (TextView) getView().findViewById(16908292);
        emptyView.setText(2131625171);
        setEmptyView(emptyView);
    }

    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        logd("onCreateOptionsMenu()", new Object[0]);
        boolean isEnabled = this.mBackend.isEnabled();
        MenuItem start = createMenuItem(menu, 2131625173, 0, isEnabled, new Runnable() {
            public void run() {
                DreamSettings.this.mBackend.startDreaming();
            }
        });
        MenuItem whenToDream = createMenuItem(menu, 2131625172, 0, isEnabled, new Runnable() {
            public void run() {
                DreamSettings.this.showDialog(1);
            }
        });
        super.onCreateOptionsMenu(menu, inflater);
        this.mMenuItemsWhenEnabled = new MenuItem[]{start, whenToDream};
    }

    private MenuItem createMenuItem(Menu menu, int titleRes, int actionEnum, boolean isEnabled, final Runnable onClick) {
        MenuItem item = menu.add(titleRes);
        item.setShowAsAction(actionEnum);
        item.setEnabled(isEnabled);
        item.setOnMenuItemClickListener(new OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
                onClick.run();
                return true;
            }
        });
        return item;
    }

    public Dialog onCreateDialog(int dialogId) {
        logd("onCreateDialog(%s)", Integer.valueOf(dialogId));
        if (dialogId == 1) {
            return createWhenToDreamDialog();
        }
        return super.onCreateDialog(dialogId);
    }

    private Dialog createWhenToDreamDialog() {
        int initialSelection;
        CharSequence[] items = new CharSequence[]{this.mContext.getString(2131625169), this.mContext.getString(2131625168), this.mContext.getString(2131625167)};
        if (this.mBackend.isActivatedOnDock() && this.mBackend.isActivatedOnSleep()) {
            initialSelection = 2;
        } else if (this.mBackend.isActivatedOnDock()) {
            initialSelection = 0;
        } else if (this.mBackend.isActivatedOnSleep()) {
            initialSelection = 1;
        } else {
            initialSelection = -1;
        }
        return new Builder(this.mContext).setTitle(2131625172).setSingleChoiceItems(items, initialSelection, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {
                boolean z;
                boolean z2 = true;
                DreamBackend -get0 = DreamSettings.this.mBackend;
                if (item == 0 || item == 2) {
                    z = true;
                } else {
                    z = false;
                }
                -get0.setActivatedOnDock(z);
                DreamBackend -get02 = DreamSettings.this.mBackend;
                if (!(item == 1 || item == 2)) {
                    z2 = false;
                }
                -get02.setActivatedOnSleep(z2);
                dialog.dismiss();
            }
        }).create();
    }

    public void onPause() {
        logd("onPause()", new Object[0]);
        super.onPause();
        this.mContext.unregisterReceiver(this.mPackageReceiver);
    }

    public void onResume() {
        logd("onResume()", new Object[0]);
        super.onResume();
        refreshFromBackend();
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.PACKAGE_ADDED");
        filter.addAction("android.intent.action.PACKAGE_CHANGED");
        filter.addAction("android.intent.action.PACKAGE_REMOVED");
        filter.addAction("android.intent.action.PACKAGE_REPLACED");
        filter.addDataScheme("package");
        this.mContext.registerReceiver(this.mPackageReceiver, filter);
    }

    private void refreshFromBackend() {
        logd("refreshFromBackend()", new Object[0]);
        this.mRefreshing = true;
        boolean dreamsEnabled = this.mBackend.isEnabled();
        if (getPreferenceScreen() == null) {
            setPreferenceScreen(getPreferenceManager().createPreferenceScreen(getContext()));
        }
        getPreferenceScreen().removeAll();
        if (dreamsEnabled) {
            List<DreamInfo> dreamInfos = this.mBackend.getDreamInfos();
            int N = dreamInfos.size();
            for (int i = 0; i < N; i++) {
                getPreferenceScreen().addPreference(new DreamInfoPreference(getPrefContext(), (DreamInfo) dreamInfos.get(i)));
            }
        }
        if (this.mMenuItemsWhenEnabled != null) {
            for (MenuItem menuItem : this.mMenuItemsWhenEnabled) {
                menuItem.setEnabled(dreamsEnabled);
            }
        }
        this.mRefreshing = false;
    }

    private static void logd(String msg, Object... args) {
    }
}
