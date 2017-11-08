package com.android.contacts.hap.util;

public class ReflelctionConstant {
    private static String MSIM_TELEPHONY_SERVICE = "-1";
    private static String OPTION_APPWIDGET_HOST_CATEGORY = "-1";
    private static String PROPERTY_GLOBAL_MULTI_SIM_CONFIG = "-1";
    private static String PROPERTY_GLOBAL_VERSION_NUM_MATCH = "-1";
    private static String PROPERTY_GLOBAL_VERSION_NUM_MATCH_SHORT = "-1";
    private static int WIDGET_CATEGORY_KEYGUARD = -1;

    public static boolean isInitialized(String aVarName) {
        return aVarName == null || !"-1".equals(aVarName);
    }

    public static String getAppWidgetHostCategory() {
        if (isInitialized(OPTION_APPWIDGET_HOST_CATEGORY)) {
            return OPTION_APPWIDGET_HOST_CATEGORY;
        }
        try {
            OPTION_APPWIDGET_HOST_CATEGORY = (String) RefelctionUtils.getStaticVariableValue("android.appwidget.AppWidgetManager", "OPTION_APPWIDGET_HOST_CATEGORY");
        } catch (UnsupportedException e) {
            OPTION_APPWIDGET_HOST_CATEGORY = null;
        }
        return OPTION_APPWIDGET_HOST_CATEGORY;
    }

    public static int getWidgetCategoryKeyguard() {
        if (-1 != WIDGET_CATEGORY_KEYGUARD) {
            return WIDGET_CATEGORY_KEYGUARD;
        }
        try {
            WIDGET_CATEGORY_KEYGUARD = ((Integer) RefelctionUtils.getStaticVariableValue("android.appwidget.AppWidgetProviderInfo", "WIDGET_CATEGORY_KEYGUARD")).intValue();
        } catch (UnsupportedException e) {
            WIDGET_CATEGORY_KEYGUARD = -1;
        }
        return WIDGET_CATEGORY_KEYGUARD;
    }

    public static String getGlobalVerNumMatch() {
        if (isInitialized(PROPERTY_GLOBAL_VERSION_NUM_MATCH)) {
            return PROPERTY_GLOBAL_VERSION_NUM_MATCH;
        }
        try {
            PROPERTY_GLOBAL_VERSION_NUM_MATCH = (String) RefelctionUtils.getStaticVariableValue("com.android.internal.telephony.TelephonyProperties", "PROPERTY_GLOBAL_VERSION_NUM_MATCH");
        } catch (UnsupportedException e) {
            PROPERTY_GLOBAL_VERSION_NUM_MATCH = null;
        }
        return PROPERTY_GLOBAL_VERSION_NUM_MATCH;
    }

    public static String getGlobalVerNumMatchShort() {
        if (isInitialized(PROPERTY_GLOBAL_VERSION_NUM_MATCH_SHORT)) {
            return PROPERTY_GLOBAL_VERSION_NUM_MATCH_SHORT;
        }
        try {
            PROPERTY_GLOBAL_VERSION_NUM_MATCH_SHORT = (String) RefelctionUtils.getStaticVariableValue("com.android.internal.telephony.TelephonyProperties", "PROPERTY_GLOBAL_VERSION_NUM_MATCH_SHORT");
        } catch (UnsupportedException e) {
            PROPERTY_GLOBAL_VERSION_NUM_MATCH_SHORT = null;
        }
        return PROPERTY_GLOBAL_VERSION_NUM_MATCH_SHORT;
    }

    public static void cacheData() {
        getAppWidgetHostCategory();
        getWidgetCategoryKeyguard();
        getGlobalVerNumMatch();
        getGlobalVerNumMatchShort();
    }

    public static String getGlobalMultiSimConfig() {
        if (isInitialized(PROPERTY_GLOBAL_MULTI_SIM_CONFIG)) {
            return PROPERTY_GLOBAL_MULTI_SIM_CONFIG;
        }
        try {
            PROPERTY_GLOBAL_MULTI_SIM_CONFIG = (String) RefelctionUtils.getStaticVariableValue("com.android.internal.telephony.TelephonyProperties", "PROPERTY_MULTI_SIM_CONFIG");
        } catch (UnsupportedException e) {
            PROPERTY_GLOBAL_MULTI_SIM_CONFIG = "";
        }
        return PROPERTY_GLOBAL_MULTI_SIM_CONFIG;
    }
}
