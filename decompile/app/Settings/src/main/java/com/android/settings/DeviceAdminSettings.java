package com.android.settings;

import android.app.Activity;
import android.app.AppGlobals;
import android.app.ListFragment;
import android.app.admin.DeviceAdminInfo;
import android.app.admin.DevicePolicyManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.IPackageManager;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources.NotFoundException;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.UserHandle;
import android.os.UserManager;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class DeviceAdminSettings extends ListFragment {
    private final ArrayList<DeviceAdminListItem> mAdmins = new ArrayList();
    private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if ("android.app.action.DEVICE_POLICY_MANAGER_STATE_CHANGED".equals(intent.getAction())) {
                DeviceAdminSettings.this.updateList();
            }
        }
    };
    private DevicePolicyManager mDPM;
    private String mDeviceOwnerPkg;
    private SparseArray<ComponentName> mProfileOwnerComponents = new SparseArray();
    private UserManager mUm;

    private static class DeviceAdminListItem implements Comparable<DeviceAdminListItem> {
        public boolean active;
        public DeviceAdminInfo info;
        public String name;

        private DeviceAdminListItem() {
        }

        public int compareTo(DeviceAdminListItem other) {
            if (this.active == other.active) {
                return this.name.compareTo(other.name);
            }
            return this.active ? -1 : 1;
        }
    }

    class PolicyListAdapter extends BaseAdapter {
        final LayoutInflater mInflater;

        PolicyListAdapter() {
            this.mInflater = (LayoutInflater) DeviceAdminSettings.this.getActivity().getSystemService("layout_inflater");
        }

        public boolean hasStableIds() {
            return false;
        }

        public int getCount() {
            return DeviceAdminSettings.this.mAdmins.size();
        }

        public Object getItem(int position) {
            return ((DeviceAdminListItem) DeviceAdminSettings.this.mAdmins.get(position)).info;
        }

        public long getItemId(int position) {
            return (long) position;
        }

        public boolean areAllItemsEnabled() {
            return false;
        }

        public int getViewTypeCount() {
            return 1;
        }

        public int getItemViewType(int position) {
            return 0;
        }

        public boolean isEnabled(int position) {
            return isEnabled(getItem(position));
        }

        private boolean isEnabled(Object o) {
            if (DeviceAdminSettings.this.isRemovingAdmin((DeviceAdminInfo) o)) {
                return false;
            }
            return true;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            Object o = getItem(position);
            if (convertView == null) {
                convertView = newDeviceAdminView(parent);
            }
            bindView(convertView, (DeviceAdminInfo) o);
            return convertView;
        }

        private View newDeviceAdminView(ViewGroup parent) {
            View v = this.mInflater.inflate(2130968736, parent, false);
            ViewHolder h = new ViewHolder();
            h.icon = (ImageView) v.findViewById(2131886147);
            h.name = (TextView) v.findViewById(2131886300);
            h.checkbox = new TwoStateTextView((TextView) v.findViewById(2131886501), 2131627306, 2131627305);
            h.description = (TextView) v.findViewById(2131886500);
            v.setTag(h);
            return v;
        }

        private void bindView(View view, DeviceAdminInfo item) {
            Activity activity = DeviceAdminSettings.this.getActivity();
            ViewHolder vh = (ViewHolder) view.getTag();
            vh.icon.setImageDrawable(activity.getPackageManager().getUserBadgedIcon(item.loadIcon(activity.getPackageManager()), new UserHandle(DeviceAdminSettings.this.getUserId(item))));
            vh.name.setText(item.loadLabel(activity.getPackageManager()));
            vh.checkbox.setChecked(DeviceAdminSettings.this.isActiveAdmin(item));
            boolean enabled = isEnabled((Object) item);
            try {
                SettingsExtUtils.setOrGoneTextView(vh.description, item.loadDescription(activity.getPackageManager()));
            } catch (NotFoundException e) {
                SettingsExtUtils.setOrGoneTextView(vh.description, null);
            }
            vh.checkbox.setEnabled(enabled);
            vh.name.setEnabled(enabled);
            vh.description.setEnabled(enabled);
            vh.icon.setEnabled(enabled);
        }
    }

    static class ViewHolder {
        TwoStateTextView checkbox;
        TextView description;
        ImageView icon;
        TextView name;

        ViewHolder() {
        }
    }

    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.mDPM = (DevicePolicyManager) getActivity().getSystemService("device_policy");
        this.mUm = (UserManager) getActivity().getSystemService("user");
        View view = inflater.inflate(2130968737, container, false);
        ListView list = (ListView) view.findViewById(16908298);
        LinearLayout emptyView = (LinearLayout) view.findViewById(2131886564);
        ImageView emptyIcon = (ImageView) view.findViewById(2131886560);
        ((TextView) view.findViewById(2131886561)).setText(2131626168);
        emptyIcon.setBackgroundResource(2130838387);
        if (emptyView != null) {
            list.setEmptyView(emptyView);
        }
        list.setScrollBarStyle(33554432);
        Utils.prepareCustomPreferencesList(container, view, list, true);
        setHasOptionsMenu(true);
        return view;
    }

    public void onResume() {
        String str = null;
        super.onResume();
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.app.action.DEVICE_POLICY_MANAGER_STATE_CHANGED");
        getActivity().registerReceiverAsUser(this.mBroadcastReceiver, UserHandle.ALL, filter, null, null);
        ComponentName deviceOwnerComponent = this.mDPM.getDeviceOwnerComponentOnAnyUser();
        if (deviceOwnerComponent != null) {
            str = deviceOwnerComponent.getPackageName();
        }
        this.mDeviceOwnerPkg = str;
        this.mProfileOwnerComponents.clear();
        List<UserHandle> profiles = this.mUm.getUserProfiles();
        int profilesSize = profiles.size();
        for (int i = 0; i < profilesSize; i++) {
            int profileId = ((UserHandle) profiles.get(i)).getIdentifier();
            this.mProfileOwnerComponents.put(profileId, this.mDPM.getProfileOwnerAsUser(profileId));
        }
        updateList();
    }

    public void onPause() {
        getActivity().unregisterReceiver(this.mBroadcastReceiver);
        super.onPause();
    }

    void updateList() {
        this.mAdmins.clear();
        List<UserHandle> profiles = this.mUm.getUserProfiles();
        int profilesSize = profiles.size();
        for (int i = 0; i < profilesSize; i++) {
            updateAvailableAdminsForProfile(((UserHandle) profiles.get(i)).getIdentifier());
        }
        Collections.sort(this.mAdmins);
        getListView().setAdapter(new PolicyListAdapter());
        getListView().setDivider(getResources().getDrawable(2130838530));
    }

    public void onListItemClick(ListView l, View v, int position, long id) {
        DeviceAdminInfo dpi = (DeviceAdminInfo) l.getAdapter().getItem(position);
        UserHandle user = new UserHandle(getUserId(dpi));
        Activity activity = getActivity();
        Intent intent = new Intent(activity, DeviceAdminAdd.class);
        intent.putExtra("android.app.extra.DEVICE_ADMIN", dpi.getComponent());
        activity.startActivityAsUser(intent, user);
    }

    private boolean isActiveAdmin(DeviceAdminInfo item) {
        return this.mDPM.isAdminActiveAsUser(item.getComponent(), getUserId(item));
    }

    private boolean isRemovingAdmin(DeviceAdminInfo item) {
        return this.mDPM.isRemovingAdmin(item.getComponent(), getUserId(item));
    }

    private void updateAvailableAdminsForProfile(int profileId) {
        List<ComponentName> activeAdminsListForProfile = this.mDPM.getActiveAdminsAsUser(profileId);
        addActiveAdminsForProfile(activeAdminsListForProfile, profileId);
        addDeviceAdminBroadcastReceiversForProfile(activeAdminsListForProfile, profileId);
    }

    private void addDeviceAdminBroadcastReceiversForProfile(Collection<ComponentName> alreadyAddedComponents, int profileId) {
        PackageManager pm = getActivity().getPackageManager();
        List<ResolveInfo> enabledForProfile = pm.queryBroadcastReceiversAsUser(new Intent("android.app.action.DEVICE_ADMIN_ENABLED"), 32896, profileId);
        if (enabledForProfile == null) {
            enabledForProfile = Collections.emptyList();
        }
        int n = enabledForProfile.size();
        for (int i = 0; i < n; i++) {
            ResolveInfo resolveInfo = (ResolveInfo) enabledForProfile.get(i);
            ComponentName riComponentName = new ComponentName(resolveInfo.activityInfo.packageName, resolveInfo.activityInfo.name);
            if (alreadyAddedComponents == null || !alreadyAddedComponents.contains(riComponentName)) {
                DeviceAdminInfo deviceAdminInfo = createDeviceAdminInfo(resolveInfo.activityInfo);
                if (deviceAdminInfo != null && deviceAdminInfo.isVisible() && deviceAdminInfo.getActivityInfo().applicationInfo.isInternal()) {
                    DeviceAdminListItem item = new DeviceAdminListItem();
                    item.info = deviceAdminInfo;
                    item.name = deviceAdminInfo.loadLabel(pm).toString();
                    item.active = false;
                    this.mAdmins.add(item);
                }
            }
        }
    }

    private void addActiveAdminsForProfile(List<ComponentName> activeAdmins, int profileId) {
        if (activeAdmins != null) {
            PackageManager packageManager = getActivity().getPackageManager();
            IPackageManager iPackageManager = AppGlobals.getPackageManager();
            int n = activeAdmins.size();
            for (int i = 0; i < n; i++) {
                ComponentName activeAdmin = (ComponentName) activeAdmins.get(i);
                try {
                    DeviceAdminInfo deviceAdminInfo = createDeviceAdminInfo(iPackageManager.getReceiverInfo(activeAdmin, 819328, profileId));
                    if (deviceAdminInfo != null) {
                        DeviceAdminListItem item = new DeviceAdminListItem();
                        item.info = deviceAdminInfo;
                        item.name = deviceAdminInfo.loadLabel(packageManager).toString();
                        item.active = true;
                        this.mAdmins.add(item);
                    }
                } catch (RemoteException e) {
                    Log.w("DeviceAdminSettings", "Unable to load component: " + activeAdmin);
                }
            }
        }
    }

    private DeviceAdminInfo createDeviceAdminInfo(ActivityInfo ai) {
        try {
            return new DeviceAdminInfo(getActivity(), ai);
        } catch (Exception e) {
            Log.w("DeviceAdminSettings", "Skipping " + ai, e);
            return null;
        }
    }

    private int getUserId(DeviceAdminInfo adminInfo) {
        return UserHandle.getUserId(adminInfo.getActivityInfo().applicationInfo.uid);
    }
}
