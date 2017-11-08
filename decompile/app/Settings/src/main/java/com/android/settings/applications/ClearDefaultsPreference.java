package com.android.settings.applications;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.usb.IUsbManager;
import android.hardware.usb.IUsbManager.Stub;
import android.hdm.HwDeviceManager;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.UserHandle;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceViewHolder;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.BulletSpan;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import com.android.settingslib.applications.AppUtils;
import com.android.settingslib.applications.ApplicationsState.AppEntry;
import java.util.ArrayList;

public class ClearDefaultsPreference extends Preference {
    protected static final String TAG = ClearDefaultsPreference.class.getSimpleName();
    private Button mActivitiesButton;
    protected AppEntry mAppEntry;
    private AppWidgetManager mAppWidgetManager;
    private String mPackageName;
    private PackageManager mPm;
    private IUsbManager mUsbManager;

    public ClearDefaultsPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        setLayoutResource(2130968635);
        this.mAppWidgetManager = AppWidgetManager.getInstance(context);
        this.mPm = context.getPackageManager();
        this.mUsbManager = Stub.asInterface(ServiceManager.getService("usb"));
    }

    public ClearDefaultsPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public ClearDefaultsPreference(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ClearDefaultsPreference(Context context) {
        this(context, null);
    }

    public void setPackageName(String packageName) {
        this.mPackageName = packageName;
    }

    public void setAppEntry(AppEntry entry) {
        this.mAppEntry = entry;
    }

    public void onBindViewHolder(final PreferenceViewHolder view) {
        super.onBindViewHolder(view);
        this.mActivitiesButton = (Button) view.findViewById(2131886265);
        this.mActivitiesButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (ClearDefaultsPreference.this.mUsbManager != null) {
                    int userId = UserHandle.myUserId();
                    ClearDefaultsPreference.this.mPm.clearPackagePreferredActivities(ClearDefaultsPreference.this.mPackageName);
                    if (ClearDefaultsPreference.this.isDefaultBrowser(ClearDefaultsPreference.this.mPackageName)) {
                        ClearDefaultsPreference.this.mPm.setDefaultBrowserPackageNameAsUser(null, userId);
                    }
                    try {
                        ClearDefaultsPreference.this.mUsbManager.clearDefaults(ClearDefaultsPreference.this.mPackageName, userId);
                    } catch (RemoteException e) {
                        Log.e(ClearDefaultsPreference.TAG, "mUsbManager.clearDefaults", e);
                    }
                    ClearDefaultsPreference.this.mAppWidgetManager.setBindAppWidgetPermission(ClearDefaultsPreference.this.mPackageName, false);
                    ClearDefaultsPreference.this.resetLaunchDefaultsUi((TextView) view.findViewById(2131886263));
                }
            }
        });
        updateUI(view);
    }

    public boolean updateUI(PreferenceViewHolder view) {
        boolean hasBindAppWidgetPermission;
        boolean autoLaunchEnabled;
        boolean z;
        Context context;
        CharSequence text;
        int bulletIndent;
        CharSequence autoLaunchEnableText;
        SpannableString s;
        CharSequence alwaysAllowBindAppWidgetsText;
        ComponentName currentDefaultHome;
        if (this.mAppEntry == null) {
            Log.e(TAG, "ClearDefaultsPreference-->updateUI-->mAppEntry is null !");
        }
        if (this.mAppEntry != null) {
            hasBindAppWidgetPermission = this.mAppWidgetManager.hasBindAppWidgetPermission(this.mAppEntry.info.packageName);
        } else {
            hasBindAppWidgetPermission = false;
        }
        TextView autoLaunchView = (TextView) view.findViewById(2131886263);
        if (!AppUtils.hasPreferredActivities(this.mPm, this.mPackageName)) {
            if (!isDefaultBrowser(this.mPackageName)) {
                autoLaunchEnabled = AppUtils.hasUsbDefaults(this.mUsbManager, this.mPackageName);
                if (!autoLaunchEnabled || hasBindAppWidgetPermission) {
                    z = hasBindAppWidgetPermission ? autoLaunchEnabled : false;
                    if (hasBindAppWidgetPermission) {
                        autoLaunchView.setText(2131625601);
                    } else {
                        autoLaunchView.setText(2131625602);
                    }
                    context = getContext();
                    text = null;
                    bulletIndent = context.getResources().getDimensionPixelSize(2131558599);
                    if (autoLaunchEnabled) {
                        autoLaunchEnableText = context.getText(2131625623);
                        s = new SpannableString(autoLaunchEnableText);
                        if (z) {
                            s.setSpan(new BulletSpan(bulletIndent), 0, autoLaunchEnableText.length(), 0);
                        }
                        text = TextUtils.concat(new CharSequence[]{s, "\n"});
                    }
                    if (hasBindAppWidgetPermission) {
                        alwaysAllowBindAppWidgetsText = context.getText(2131625624);
                        s = new SpannableString(alwaysAllowBindAppWidgetsText);
                        if (z) {
                            s.setSpan(new BulletSpan(bulletIndent), 0, alwaysAllowBindAppWidgetsText.length(), 0);
                        }
                        text = text != null ? TextUtils.concat(new CharSequence[]{s, "\n"}) : TextUtils.concat(new CharSequence[]{text, "\n", s, "\n"});
                    }
                    autoLaunchView.setText(text);
                    currentDefaultHome = this.mPm.getHomeActivities(new ArrayList());
                    if (HwDeviceManager.disallowOp(17) || !this.mPackageName.equals(currentDefaultHome.getPackageName())) {
                        this.mActivitiesButton.setEnabled(true);
                    } else {
                        this.mActivitiesButton.setEnabled(false);
                    }
                } else {
                    resetLaunchDefaultsUi(autoLaunchView);
                }
                return true;
            }
        }
        autoLaunchEnabled = true;
        if (autoLaunchEnabled) {
        }
        if (hasBindAppWidgetPermission) {
        }
        if (hasBindAppWidgetPermission) {
            autoLaunchView.setText(2131625601);
        } else {
            autoLaunchView.setText(2131625602);
        }
        context = getContext();
        text = null;
        bulletIndent = context.getResources().getDimensionPixelSize(2131558599);
        if (autoLaunchEnabled) {
            autoLaunchEnableText = context.getText(2131625623);
            s = new SpannableString(autoLaunchEnableText);
            if (z) {
                s.setSpan(new BulletSpan(bulletIndent), 0, autoLaunchEnableText.length(), 0);
            }
            text = TextUtils.concat(new CharSequence[]{s, "\n"});
        }
        if (hasBindAppWidgetPermission) {
            alwaysAllowBindAppWidgetsText = context.getText(2131625624);
            s = new SpannableString(alwaysAllowBindAppWidgetsText);
            if (z) {
                s.setSpan(new BulletSpan(bulletIndent), 0, alwaysAllowBindAppWidgetsText.length(), 0);
            }
            if (text != null) {
            }
        }
        autoLaunchView.setText(text);
        currentDefaultHome = this.mPm.getHomeActivities(new ArrayList());
        if (HwDeviceManager.disallowOp(17)) {
        }
        this.mActivitiesButton.setEnabled(true);
        return true;
    }

    private boolean isDefaultBrowser(String packageName) {
        return packageName.equals(this.mPm.getDefaultBrowserPackageNameAsUser(UserHandle.myUserId()));
    }

    private void resetLaunchDefaultsUi(TextView autoLaunchView) {
        autoLaunchView.setText(2131625625);
        this.mActivitiesButton.setEnabled(false);
    }
}
