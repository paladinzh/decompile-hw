package com.android.settings;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.support.v7.preference.Preference;
import android.telephony.SignalStrength;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;
import java.util.List;

public class SettingsExtUtils {
    public static void setEmuiTheme(Context context) {
        if (context != null) {
            new SettingsPlatformImp().setEmuiTheme(context);
        }
    }

    public static void showEmuiToast(Context context, String message) {
        if (context != null && message != null) {
            new SettingsPlatformImp().showEmuiToast(context, message);
        }
    }

    public static void setAnimationReflection(Context context) {
        if (context != null) {
            new SettingsPlatformImp().setAnimationReflection(context);
        }
    }

    public static void resetPreferenceLayout(Preference pref, int layoutId, int widgetId) {
        if (pref != null) {
            new SettingsPlatformImp().resetPreferenceLayout(pref, layoutId, widgetId);
        }
    }

    public static int getMultiSimSignalDbm(SignalStrength signalStrength, Context context) {
        if (signalStrength == null) {
            return -1;
        }
        return new SettingsPlatformImp().getMultiSimSignalDbm(signalStrength, context);
    }

    public static int getMultiSimSignalAsu(SignalStrength signalStrength, Context context) {
        if (signalStrength == null) {
            return -1;
        }
        return new SettingsPlatformImp().getMultiSimSignalAsu(signalStrength, context);
    }

    public static AlphaStateListDrawable getAlphaStateListDrawable(Resources res, int drawableID) {
        if (drawableID < 0) {
            return null;
        }
        return new SettingsPlatformImp().getAlphaStateListDrawable(res, drawableID);
    }

    public static void transferToSmartHelper(Context context, String queryStr) {
        if (context != null) {
            new SettingsPlatformImp().transferToSmartHelper(context, queryStr);
        }
    }

    public static void setApnPreferenceClickListener(View root, boolean selectable, OnClickListener listener) {
        if (root != null) {
            new SettingsPlatformImp().setApnPreferenceClickListener(root, selectable, listener);
        }
    }

    public static void setWidgetLayout(Preference pref, boolean selectable) {
        if (pref != null) {
            new SettingsPlatformImp().setWidgetLayout(pref, selectable);
        }
    }

    public static void setOrGoneTextView(TextView tv, CharSequence text) {
        if (tv != null) {
            new SettingsPlatformImp().setOrGoneTextView(tv, text);
        }
    }

    public static Class<?> getClassForCommandCode(Context context, Intent intent) {
        return new SettingsPlatformImp().getClassForCommandCode(context, intent);
    }

    public static View setLayoutOfUserDictionary(LayoutInflater inflater, ViewGroup container) {
        if (inflater == null || container == null) {
            return null;
        }
        return new SettingsPlatformImp().setLayoutOfUserDictionary(inflater, container);
    }

    public static void checkHideSoftInput(Activity context) {
        new SettingsPlatformImp().checkHideSoftInput(context);
    }

    public static int getScreenlockSettingsId(Activity context) {
        return new SettingsPlatformImp().getScreenlockSettingsId(context);
    }

    public static boolean isSimCardPresent() {
        return new SettingsPlatformImp().isSimCardPresent();
    }

    public static boolean isGlobalVersion() {
        return new SettingsPlatformImp().isGlobalVersion();
    }

    public static List<ResolveInfo> filterDualCardSettings(List<ResolveInfo> activities) {
        if (activities == null) {
            return null;
        }
        return new SettingsPlatformImp().filterDualCardSettings(activities);
    }

    public static boolean isStartupGuideMode(ContentResolver cr) {
        if (cr == null) {
            return false;
        }
        return new SettingsPlatformImp().isStartupGuideMode(cr);
    }

    public static boolean hideActionBarInStartupGuide(Activity activity) {
        if (activity == null) {
            return false;
        }
        return new SettingsPlatformImp().hideActionBarInStartupGuide(activity);
    }

    public static boolean isGoogleBackupDisabled(Context context) {
        SettingsPlatformImp platformImp = new SettingsPlatformImp();
        return SettingsPlatformImp.isGoogleBackupDisabled(context);
    }
}
