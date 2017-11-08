package com.android.settings;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.provider.Settings.Global;
import android.support.v7.preference.Preference;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.view.ContextThemeWrapper;
import android.widget.TextView;
import android.widget.Toast;
import com.android.internal.telephony.SubscriptionController;

class SettingsExtAbsBase {
    SettingsExtAbsBase() {
    }

    public static Context getEmuiContextThemeWrapper(Context context) {
        if (context == null) {
            return context;
        }
        int themeId = context.getResources().getIdentifier("androidhwext:style/Theme.Emui", null, null);
        if (themeId != 0) {
            context = new ContextThemeWrapper(context, themeId);
        }
        return context;
    }

    public void setEmuiTheme(Context context) {
        if (context != null) {
            int themeId = context.getResources().getIdentifier("androidhwext:style/Theme.Emui", null, null);
            if (themeId != 0) {
                context.setTheme(themeId);
            }
        }
    }

    public void showEmuiToast(Context context, String message) {
        if (context != null && message != null) {
            Toast.makeText(getEmuiContextThemeWrapper(context), message, 0).show();
        }
    }

    public void setAnimationReflection(Context context) {
        if (context != null) {
            new HwAnimationReflection(context).overrideTransition(1);
        }
    }

    public void resetPreferenceLayout(Preference pref, int layoutId, int widgetId) {
        if (pref != null) {
            pref.setLayoutResource(layoutId);
            pref.setWidgetLayoutResource(widgetId);
        }
    }

    public void setOrGoneTextView(TextView tv, CharSequence text) {
        if (tv != null) {
            if (TextUtils.isEmpty(text)) {
                tv.setVisibility(8);
            } else {
                tv.setText(text);
                tv.setVisibility(0);
            }
        }
    }

    private int getSubId(Context context) {
        SubscriptionController mSubscriptionController = SubscriptionController.init(context, null);
        if (mSubscriptionController != null) {
            return mSubscriptionController.getDefaultSubId();
        }
        return -1;
    }

    public int getMultiSimSignalDbm(SignalStrength signalStrength, Context context) {
        if (signalStrength == null) {
            return -1;
        }
        int signalDbm = -1;
        int networkType = TelephonyManager.getDefault().getNetworkType(getSubId(context));
        if (signalStrength.isGsm()) {
            signalDbm = signalStrength.getDbm();
        } else if (networkType == 13) {
            signalDbm = signalStrength.getLteDbm();
        } else if (networkType == 5 || networkType == 6 || networkType == 12 || networkType == 14) {
            signalDbm = signalStrength.getEvdoDbm();
        } else if (networkType == 4 || networkType == 7) {
            signalDbm = signalStrength.getCdmaDbm();
        }
        return signalDbm;
    }

    public int getMultiSimSignalAsu(SignalStrength signalStrength, Context context) {
        if (signalStrength == null) {
            return -1;
        }
        int signalAsu = -1;
        int networkType = TelephonyManager.getDefault().getNetworkType(getSubId(context));
        if (signalStrength.isGsm()) {
            signalAsu = signalStrength.getAsuLevel();
        } else if (networkType == 13) {
            signalAsu = signalStrength.getLteAsuLevel();
        } else if (networkType == 5 || networkType == 6 || networkType == 12 || networkType == 14) {
            signalAsu = signalStrength.getEvdoAsuLevel();
        } else if (networkType == 4 || networkType == 7) {
            signalAsu = signalStrength.getCdmaAsuLevel();
        }
        return signalAsu;
    }

    public AlphaStateListDrawable getAlphaStateListDrawable(Resources res, int drawableID) {
        if (drawableID < 0) {
            return null;
        }
        AlphaStateListDrawable drawable = new AlphaStateListDrawable();
        drawable.addState(new int[0], res.getDrawable(drawableID));
        return drawable;
    }

    public void transferToSmartHelper(Context context, String queryStr) {
        if (context != null) {
            Intent intent = new Intent();
            intent.setAction("com.huawei.phoneservice.intent.action.SmartHelperActivity");
            intent.setPackage("com.huawei.phoneservice");
            Bundle b = new Bundle();
            b.putString("query_word", queryStr);
            intent.putExtras(b);
            Utils.cancelSplit(context, intent);
            try {
                context.startActivity(intent);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public boolean isStartupGuideMode(ContentResolver cr) {
        boolean z = true;
        if (cr == null) {
            return false;
        }
        if (Global.getInt(cr, "device_provisioned", 1) != 0) {
            z = false;
        }
        return z;
    }

    public boolean hideActionBarInStartupGuide(Activity activity) {
        if (activity == null || !isStartupGuideMode(activity.getContentResolver())) {
            return false;
        }
        if (activity.getActionBar() != null) {
            activity.getActionBar().hide();
        }
        if (!(activity.getWindow() == null || activity.getWindow().getDecorView() == null)) {
            activity.getWindow().getDecorView().setSystemUiVisibility(5382);
        }
        if (!Utils.isTablet()) {
            activity.setRequestedOrientation(1);
        }
        return true;
    }
}
