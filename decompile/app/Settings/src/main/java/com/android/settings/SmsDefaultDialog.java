package com.android.settings;

import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.SystemProperties;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.android.internal.app.AlertActivity;
import com.android.internal.app.AlertController.AlertParams;
import com.android.internal.telephony.SmsApplication;
import com.android.internal.telephony.SmsApplication.SmsApplicationData;
import java.util.ArrayList;
import java.util.List;

public final class SmsDefaultDialog extends AlertActivity implements OnClickListener {
    private SmsApplicationData mNewSmsApplicationData;

    private class AppListAdapter extends BaseAdapter {
        private final List<Item> mItems = getItems();
        private final int mSelectedIndex;

        private class Item {
            final Drawable icon;
            final String label;
            final String packgeName;

            public Item(String label, Drawable icon, String packageName) {
                this.label = label;
                this.icon = icon;
                this.packgeName = packageName;
            }
        }

        public AppListAdapter() {
            int selected = getSelectedIndex();
            if (selected > 0) {
                this.mItems.add(0, (Item) this.mItems.remove(selected));
                selected = 0;
            }
            this.mSelectedIndex = selected;
        }

        public int getCount() {
            return this.mItems != null ? this.mItems.size() : 0;
        }

        public Object getItem(int position) {
            return (this.mItems == null || position >= this.mItems.size()) ? null : this.mItems.get(position);
        }

        public long getItemId(int position) {
            return (long) position;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            Item item = (Item) getItem(position);
            View view = SmsDefaultDialog.this.getLayoutInflater().inflate(2130968634, parent, false);
            ((TextView) view.findViewById(16908310)).setText(item.label);
            if (position == this.mSelectedIndex) {
                view.findViewById(2131886260).setVisibility(0);
            } else {
                view.findViewById(2131886260).setVisibility(8);
            }
            ((ImageView) view.findViewById(16908294)).setImageDrawable(item.icon);
            return view;
        }

        public String getPackageName(int position) {
            Item item = (Item) getItem(position);
            if (item != null) {
                return item.packgeName;
            }
            return null;
        }

        public boolean isSelected(int position) {
            return position == this.mSelectedIndex;
        }

        private List<Item> getItems() {
            PackageManager pm = SmsDefaultDialog.this.getPackageManager();
            List<Item> items = new ArrayList();
            for (SmsApplicationData app : SmsApplication.getApplicationCollection(SmsDefaultDialog.this)) {
                try {
                    String packageName = app.mPackageName;
                    ApplicationInfo appInfo = pm.getApplicationInfo(packageName, 0);
                    if (appInfo != null) {
                        items.add(new Item(appInfo.loadLabel(pm).toString(), appInfo.loadIcon(pm), packageName));
                    }
                } catch (NameNotFoundException e) {
                }
            }
            return items;
        }

        private int getSelectedIndex() {
            ComponentName appName = SmsApplication.getDefaultSmsApplication(SmsDefaultDialog.this, true);
            if (appName != null) {
                String defaultSmsAppPackageName = appName.getPackageName();
                if (!TextUtils.isEmpty(defaultSmsAppPackageName)) {
                    for (int i = 0; i < this.mItems.size(); i++) {
                        if (TextUtils.equals(((Item) this.mItems.get(i)).packgeName, defaultSmsAppPackageName)) {
                            return i;
                        }
                    }
                }
            }
            return -1;
        }
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String packageName = getIntent().getStringExtra("package");
        setResult(0);
        if (!buildDialog(packageName)) {
            finish();
        }
        getWindow().setGravity(80);
    }

    public void onClick(DialogInterface dialog, int which) {
        switch (which) {
            case -2:
                return;
            case -1:
                SmsApplication.setDefaultApplication(this.mNewSmsApplicationData.mPackageName, this);
                setResult(-1);
                return;
            default:
                if (which >= 0) {
                    AppListAdapter adapter = this.mAlertParams.mAdapter;
                    if (!adapter.isSelected(which)) {
                        String packageName = adapter.getPackageName(which);
                        if (!TextUtils.isEmpty(packageName)) {
                            SmsApplication.setDefaultApplication(packageName, this);
                            setResult(-1);
                            return;
                        }
                        return;
                    }
                    return;
                }
                return;
        }
    }

    private boolean buildDialog(String packageName) {
        if (!((TelephonyManager) getSystemService("phone")).isSmsCapable()) {
            return false;
        }
        AlertParams p = this.mAlertParams;
        p.mTitle = getString(2131628327);
        this.mNewSmsApplicationData = SmsApplication.getSmsApplicationData(packageName, this);
        if (this.mNewSmsApplicationData != null) {
            SmsApplicationData smsApplicationData = null;
            ComponentName oldSmsComponent = SmsApplication.getDefaultSmsApplication(this, true);
            boolean thirdPartyToSystem = this.mNewSmsApplicationData.mPackageName.equals("com.android.mms");
            boolean z = false;
            if (oldSmsComponent != null) {
                smsApplicationData = SmsApplication.getSmsApplicationData(oldSmsComponent.getPackageName(), this);
                if (smsApplicationData.mPackageName.equals("com.android.mms")) {
                    z = "CN".equals(SystemProperties.get("ro.product.locale.region"));
                } else {
                    z = false;
                }
                if (smsApplicationData.mPackageName.equals(this.mNewSmsApplicationData.mPackageName)) {
                    return false;
                }
            }
            if (smsApplicationData == null) {
                p.mMessage = getString(2131625458, new Object[]{getLabel(this.mNewSmsApplicationData)});
            } else if (z) {
                p.mMessage = getString(2131628633, new Object[]{getLabel(this.mNewSmsApplicationData)});
            } else if (thirdPartyToSystem) {
                p.mMessage = getString(2131628330);
            } else {
                p.mMessage = getString(2131627272, new Object[]{getLabel(this.mNewSmsApplicationData), getLabel(smsApplicationData)});
            }
            p.mPositiveButtonText = getString(2131626902);
            p.mNegativeButtonText = getString(2131624572);
            p.mPositiveButtonListener = this;
            p.mNegativeButtonListener = this;
        } else {
            p.mAdapter = new AppListAdapter();
            p.mOnClickListener = this;
            p.mNegativeButtonText = getString(2131624572);
            p.mNegativeButtonListener = this;
        }
        setupAlert();
        return true;
    }

    private String getLabel(SmsApplicationData data) {
        String label = data.mApplicationName;
        try {
            if (!"com.android.contacts".equals(data.mPackageName)) {
                return label;
            }
            PackageManager pm = getPackageManager();
            return (String) pm.getActivityInfo(new ComponentName(data.mPackageName, data.mSendToClass), 0).loadLabel(pm);
        } catch (NameNotFoundException e) {
            MLog.e("SmsDefaultDialog", "Error happens when quering information of package: " + data.mPackageName);
            return label;
        } catch (Exception e2) {
            MLog.e("SmsDefaultDialog", "Error happens when quering package info. error msg is: " + e2.getMessage());
            return label;
        }
    }
}
