package com.huawei.watermark.wmutil;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.util.Log;
import android.widget.TextView;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class WMUtil {
    private static final String TAG = WMUtil.class.getSimpleName();
    private static final String[] lkSupportLanguages = new String[]{"en", "it", "fr", "de", "ru", "es"};
    private static Typeface lkTypeface = null;
    private static Method mGetControlColorMethod;
    private static boolean sIsInitImmersion;

    static {
        sIsInitImmersion = false;
        try {
            mGetControlColorMethod = Class.forName("com.huawei.android.immersion.ImmersionStyle").getMethod("getControlColor", new Class[]{Context.class});
            sIsInitImmersion = true;
        } catch (ClassNotFoundException e) {
            sIsInitImmersion = false;
        } catch (NoSuchMethodException e2) {
            sIsInitImmersion = false;
        }
    }

    public static int getControlColor(Context context) {
        if (!sIsInitImmersion || context == null) {
            return 0;
        }
        Object colorObj = null;
        try {
            colorObj = mGetControlColorMethod.invoke(null, new Object[]{context});
        } catch (IllegalAccessException e) {
        } catch (InvocationTargetException e2) {
        }
        if (colorObj == null) {
            return 0;
        }
        return Integer.parseInt(colorObj.toString());
    }

    public static ColorStateList getStateColorStateList(Context context, int color2, int... states) {
        int color1 = getControlColor(context);
        ColorStateList colorStateList = null;
        int[][] state;
        switch (states.length) {
            case 1:
                if (states[0] == 16842919) {
                    state = new int[2][];
                    state[0] = new int[]{16842919};
                    state[1] = new int[]{-16842919};
                    colorStateList = new ColorStateList(state, new int[]{color1, color2});
                }
                if (states[0] == 16842913) {
                    state = new int[2][];
                    state[0] = new int[]{16842913};
                    state[1] = new int[]{-16842913};
                    colorStateList = new ColorStateList(state, new int[]{color1, color2});
                    break;
                }
                break;
            case 2:
                state = new int[4][];
                state[0] = new int[]{16842919};
                state[1] = new int[]{16842913};
                state[2] = new int[]{-16842919};
                state[3] = new int[]{-16842913};
                colorStateList = new ColorStateList(state, new int[]{color1, color1, color2, color2});
                break;
            case 3:
                state = new int[6][];
                state[0] = new int[]{16842919};
                state[1] = new int[]{16842913};
                state[2] = new int[]{16842908};
                state[3] = new int[]{-16842919};
                state[4] = new int[]{-16842913};
                state[5] = new int[]{-16842908};
                colorStateList = new ColorStateList(state, new int[]{color1, color1, color1, color2, color2, color2});
                break;
        }
        if (colorStateList == null) {
            Log.e(TAG, "illegal params, colorStateList is null");
        }
        return colorStateList;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static synchronized Typeface createLKTypeFace(Context context) {
        synchronized (WMUtil.class) {
            if (context != null) {
                try {
                    if (WMCustomConfigurationUtil.isDMSupported()) {
                        if (lkTypeface == null) {
                            lkTypeface = Typeface.createFromAsset(context.getAssets(), "LG1055_Regular.ttf");
                        }
                        Typeface typeface = lkTypeface;
                        return typeface;
                    }
                } catch (RuntimeException e) {
                    return null;
                }
            }
        }
    }

    public static void setLKTypeFace(Context context, TextView... textViews) {
        if (textViews != null && textViews.length != 0) {
            lkTypeface = getLKTypeFace(context);
            if (lkTypeface != null) {
                for (TextView tv : textViews) {
                    if (tv != null) {
                        tv.setTypeface(lkTypeface);
                    }
                }
            }
        }
    }

    public static Typeface getLKTypeFace(Context context) {
        if (isLKSupportedLanguages(context)) {
            return createLKTypeFace(context);
        }
        return null;
    }

    public static boolean isLKSupportedLanguages(Context context) {
        if (context == null) {
            return false;
        }
        String language = context.getResources().getConfiguration().locale.getLanguage();
        for (String i : lkSupportLanguages) {
            if (language.endsWith(i)) {
                return true;
            }
        }
        return false;
    }
}
