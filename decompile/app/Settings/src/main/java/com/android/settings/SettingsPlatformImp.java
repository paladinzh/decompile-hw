package com.android.settings;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.UserManager;
import android.provider.Settings.System;
import android.support.v7.preference.Preference;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.TextView;
import com.android.settings.Settings.TestingSettingsActivity;
import com.android.settings.Settings.VersionRequireActivity;
import com.android.settingslib.accounts.AuthenticatorHelper;
import com.huawei.cust.HwCustUtils;
import java.util.ArrayList;
import java.util.List;

public class SettingsPlatformImp extends SettingsExtAbsBase {
    public /* bridge */ /* synthetic */ AlphaStateListDrawable getAlphaStateListDrawable(Resources res, int drawableID) {
        return super.getAlphaStateListDrawable(res, drawableID);
    }

    public /* bridge */ /* synthetic */ int getMultiSimSignalAsu(SignalStrength signalStrength, Context context) {
        return super.getMultiSimSignalAsu(signalStrength, context);
    }

    public /* bridge */ /* synthetic */ int getMultiSimSignalDbm(SignalStrength signalStrength, Context context) {
        return super.getMultiSimSignalDbm(signalStrength, context);
    }

    public /* bridge */ /* synthetic */ boolean hideActionBarInStartupGuide(Activity activity) {
        return super.hideActionBarInStartupGuide(activity);
    }

    public /* bridge */ /* synthetic */ boolean isStartupGuideMode(ContentResolver cr) {
        return super.isStartupGuideMode(cr);
    }

    public /* bridge */ /* synthetic */ void resetPreferenceLayout(Preference pref, int layoutId, int widgetId) {
        super.resetPreferenceLayout(pref, layoutId, widgetId);
    }

    public /* bridge */ /* synthetic */ void setAnimationReflection(Context context) {
        super.setAnimationReflection(context);
    }

    public /* bridge */ /* synthetic */ void setEmuiTheme(Context context) {
        super.setEmuiTheme(context);
    }

    public /* bridge */ /* synthetic */ void setOrGoneTextView(TextView tv, CharSequence text) {
        super.setOrGoneTextView(tv, text);
    }

    public /* bridge */ /* synthetic */ void showEmuiToast(Context context, String message) {
        super.showEmuiToast(context, message);
    }

    public /* bridge */ /* synthetic */ void transferToSmartHelper(Context context, String queryStr) {
        super.transferToSmartHelper(context, queryStr);
    }

    public void setApnPreferenceClickListener(View root, boolean selectable, OnClickListener listener) {
        if (root != null) {
            View clickableView;
            if (selectable) {
                clickableView = root.findViewById(16908312);
                if (clickableView != null) {
                    clickableView.setOnClickListener(listener);
                }
                clickableView = root.findViewById(2131886238);
            } else {
                root.findViewById(2131886238).setBackgroundResource(0);
                clickableView = root.findViewById(2131886237);
            }
            if (clickableView != null) {
                clickableView.setOnClickListener(listener);
            }
        }
    }

    public void setWidgetLayout(Preference pref, boolean selectable) {
        if (pref != null) {
            if (selectable) {
                pref.setWidgetLayoutResource(2130969000);
            } else {
                pref.setWidgetLayoutResource(2130968998);
            }
        }
    }

    public Class<?> getClassForCommandCode(Context context, Intent intent) {
        if (intent == null || intent.getData() == null) {
            return TestingSettingsActivity.class;
        }
        String commandCode = intent.getData().getHost();
        HwCustSettingsPlatform mCustSettingsPlatform = (HwCustSettingsPlatform) HwCustUtils.createObj(HwCustSettingsPlatform.class, new Object[]{this});
        if (mCustSettingsPlatform != null) {
            Class<?> custClass = mCustSettingsPlatform.getClassForCommandCode(context, commandCode);
            if (custClass != null) {
                if (custClass.equals(Utils.class)) {
                    return null;
                }
                return custClass;
            }
        }
        if ("4636".equals(commandCode)) {
            return TestingSettingsActivity.class;
        }
        if ("0000".equals(commandCode)) {
            return VersionRequireActivity.class;
        }
        if ("1357946".equals(commandCode)) {
            return ProjectFSJActivity.class;
        }
        return null;
    }

    public View setLayoutOfUserDictionary(LayoutInflater inflater, ViewGroup container) {
        View view = inflater.inflate(2130969015, container, false);
        Utils.prepareCustomPreferencesList(container, view, (ListView) view.findViewById(16908298), true);
        ((ImageView) view.findViewById(2131886560)).setImageResource(2130838388);
        ((TextView) view.findViewById(2131886561)).setText(2131627438);
        ((LayoutParams) ((TextView) view.findViewById(16908292)).getLayoutParams()).height = 0;
        return view;
    }

    public void checkHideSoftInput(Activity context) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService("input_method");
        View focus = context.getCurrentFocus();
        if (focus != null && context.isFinishing()) {
            imm.hideSoftInputFromWindow(focus.getApplicationWindowToken(), 2);
        }
    }

    public int getScreenlockSettingsId(Activity context) {
        boolean isHuaweiUnlockEnabled = false;
        if (System.getInt(context.getContentResolver(), "hw_unlock_enabled", 0) > 0) {
            isHuaweiUnlockEnabled = true;
        }
        if (isHuaweiUnlockEnabled) {
            return 2131230876;
        }
        return 2131230875;
    }

    public boolean isSimCardPresent() {
        boolean z = true;
        TelephonyManager tm = TelephonyManager.getDefault();
        if (tm != null) {
            return tm.hasIccCard();
        }
        if (!Utils.isSimCardPresent(0)) {
            z = Utils.isSimCardPresent(1);
        }
        return z;
    }

    public boolean isGlobalVersion() {
        if ("zh".equals(SystemProperties.get("ro.product.locale.language")) && "CN".equals(SystemProperties.get("ro.product.locale.region"))) {
            return false;
        }
        return true;
    }

    public List<ResolveInfo> filterDualCardSettings(List<ResolveInfo> activities) {
        if (activities == null) {
            return null;
        }
        if (Utils.isMultiSimEnabled()) {
            if (!Utils.isChinaTelecomArea()) {
                for (ResolveInfo item : activities) {
                    if (item.activityInfo.name.endsWith("InternationalRoamingDualCardSettings")) {
                        activities.remove(item);
                        break;
                    }
                }
            }
            for (ResolveInfo item2 : activities) {
                if (item2.activityInfo.name.endsWith("DualCardSettings") && !item2.activityInfo.name.endsWith("InternationalRoamingDualCardSettings")) {
                    activities.remove(item2);
                    break;
                }
            }
        } else {
            List<ResolveInfo> tempList = new ArrayList();
            for (ResolveInfo item22 : activities) {
                if (item22.activityInfo.name.endsWith("DualCardSettings") || item22.activityInfo.name.endsWith("InternationalRoamingDualCardSettings")) {
                    tempList.add(item22);
                }
            }
            if (!tempList.isEmpty()) {
                activities.removeAll(tempList);
            }
        }
        return activities;
    }

    public static boolean isGoogleBackupDisabled(Context context) {
        if (context == null) {
            return false;
        }
        AuthenticatorHelper authHelper = new AuthenticatorHelper(context, ((UserManager) context.getSystemService("user")).getUserInfo(UserHandle.myUserId()).getUserHandle(), null);
        boolean hideGoogle = System.getInt(context.getContentResolver(), "is_show_google", 1) == 0;
        boolean hasGoogleAccount = false;
        for (String accountType : authHelper.getEnabledAccountTypes()) {
            if ("com.google".equalsIgnoreCase(accountType)) {
                hasGoogleAccount = true;
                break;
            }
        }
        if (!hideGoogle || hasGoogleAccount) {
            return false;
        }
        return true;
    }
}
