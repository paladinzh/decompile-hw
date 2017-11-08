package com.android.settings.localepicker;

import android.util.Log;
import com.android.internal.app.LocaleStore.LocaleInfo;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public class LocaleUtil {
    public static LocaleInfo getLocaleInfo(String localeId) {
        LocaleInfo locale = null;
        try {
            Constructor c = Class.forName("com.android.internal.app.LocaleStore$LocaleInfo").getDeclaredConstructor(new Class[]{String.class});
            c.setAccessible(true);
            return (LocaleInfo) c.newInstance(new Object[]{localeId});
        } catch (ClassNotFoundException e) {
            Log.d("LocaleUtil", "getLocaleInfo()-->getLocaleInfo --> ClassNotFoundException ");
            e.printStackTrace();
            return locale;
        } catch (NoSuchMethodException e2) {
            Log.d("LocaleUtil", "getLocaleInfo()-->getLocaleInfo --> NoSuchMethodException ");
            e2.printStackTrace();
            return locale;
        } catch (IllegalAccessException e3) {
            Log.d("LocaleUtil", "getLocaleInfo()-->getLocaleInfo --> IllegalAccessException ");
            e3.printStackTrace();
            return locale;
        } catch (IllegalArgumentException e4) {
            Log.d("LocaleUtil", "getLocaleInfo()-->getLocaleInfo --> IllegalArgumentException ");
            e4.printStackTrace();
            return locale;
        } catch (InvocationTargetException e5) {
            Log.d("LocaleUtil", "getLocaleInfo()-->getLocaleInfo --> InvocationTargetException ");
            e5.printStackTrace();
            return locale;
        } catch (InstantiationException e6) {
            e6.printStackTrace();
            return locale;
        }
    }
}
