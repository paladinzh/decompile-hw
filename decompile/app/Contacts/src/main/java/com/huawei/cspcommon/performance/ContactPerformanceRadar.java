package com.huawei.cspcommon.performance;

import com.android.contacts.util.HwLog;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class ContactPerformanceRadar {
    private static Map<String, Field> fields = new HashMap();
    private static String[] names = new String[]{"JLID_DEF_CONTACT_ITEM_CLICK", "JLID_CONTACT_DETAIL_BIND_VIEW", "JLID_NEW_CONTACT_SAVE_CLICK", "JLID_NEW_CONTACT_CLICK", "JLID_NEW_CONTACT_SELECT_ACCOUNT", "JLID_CONTACT_BIND_EDITOR_FOR_NEW", "JLID_DIALPAD_AFTER_TEXT_CHANGE", "JLID_DIALPAD_ADAPTER_GET_VIEW", "JLID_CONTACT_MULTISELECT_ACTIVITY_ONCREATE", "JLID_CONTACT_MULTISELECT_BIND_VIEW", "JLID_DIALPAD_ONTOUCH_NOT_FIRST_DOWN", "JLID_EDIT_CONTACT_CLICK", "JLID_EDIT_CONTACT_END"};
    private static boolean sIsInit = false;
    private static Method sLogMethod;

    public static void init() {
        try {
            sLogMethod = Class.forName("android.util.Jlog").getDeclaredMethod("d", new Class[]{Integer.TYPE, String.class});
            Class<?> clazz = Class.forName("android.util.JlogConstants");
            for (int i = 0; i < names.length; i++) {
                fields.put(names[i], clazz.getDeclaredField(names[i]));
            }
            sIsInit = true;
        } catch (NoSuchMethodException e) {
            HwLog.e("ContactsPerformanceRadar", "NoSuchMethodException android.util.JLog Not supported~:" + e);
            sIsInit = false;
        } catch (ClassNotFoundException e2) {
            HwLog.e("ContactsPerformanceRadar", "ClassNotFoundException android.util.JLog Not supported~:" + e2);
            sIsInit = false;
        } catch (NoSuchFieldException e3) {
            HwLog.e("ContactsPerformanceRadar", "NoSuchFieldException android.util.JLogConstants Not supported~:" + e3);
            sIsInit = false;
        }
    }

    private static Field getFieldByName(String name) {
        return (Field) fields.get(name);
    }

    public static void reportPerformanceRadar(String bugType, String body) {
        if (sIsInit) {
            try {
                sLogMethod.invoke(null, new Object[]{Integer.valueOf(getFieldByName(bugType).getInt(null)), body});
            } catch (IllegalAccessException e) {
                HwLog.e("ContactsPerformanceRadar", "IllegalAccessException when reportPerformanceRadar:" + e);
            } catch (InvocationTargetException e2) {
                HwLog.e("ContactsPerformanceRadar", "InvocationTargetException when reportPerformanceRadar:" + e2);
            } catch (Exception e3) {
                HwLog.e("ContactsPerformanceRadar", "Some other exception when reportPerformanceRadar: " + e3);
            }
        }
    }
}
