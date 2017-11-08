package com.huawei.mms.util;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import cn.com.xy.sms.sdk.service.msgurlservice.MsgUrlService;
import com.android.mms.MmsConfig;
import com.google.android.gms.R;
import com.huawei.cspcommon.MLog;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class HwUiStyleUtils {
    private static int sCurrentSuggestionValue = MsgUrlService.RESULT_NOT_IMPL;
    private static boolean sInitImmersionStyleFlag = false;

    public static final float getScalableFontSize(Resources res) {
        return MmsConfig.isExtraHugeEnabled(res.getConfiguration().fontScale) ? 21.5f : 15.0f;
    }

    public static final float getFavouritesScalableFontSize(Resources res) {
        return MmsConfig.isExtraHugeEnabled(res.getConfiguration().fontScale) ? 21.5f : 15.0f;
    }

    public static final float getPopMessageFontSize(Resources res) {
        return MmsConfig.isExtraHugeEnabled(res.getConfiguration().fontScale) ? 21.5f : 15.0f;
    }

    public static int getPrimaryColor(Context context) {
        int i = MsgUrlService.RESULT_NOT_IMPL;
        try {
            Class<?> clazz = Class.forName("com.huawei.android.immersion.ImmersionStyle");
            i = ((Integer) clazz.getMethod("getPrimaryColor", new Class[]{Context.class}).invoke(clazz.newInstance(), new Object[]{context})).intValue();
        } catch (ClassNotFoundException e) {
            MLog.e("HwUiStyleUtils", "getPrimaryColor ClassNotFoundException");
        } catch (NoSuchMethodException e2) {
            MLog.e("HwUiStyleUtils", "getPrimaryColor NoSuchMethodException");
        } catch (IllegalAccessException e3) {
            MLog.e("HwUiStyleUtils", "getPrimaryColor IllegalAccessException");
        } catch (InvocationTargetException e4) {
            MLog.e("HwUiStyleUtils", "getPrimaryColor InvocationTargetException");
        } catch (InstantiationException e5) {
            MLog.e("HwUiStyleUtils", "getPrimaryColor InstantiationException");
        }
        return i;
    }

    private static int getSuggestionForgroundColorStyle(int colorBackground) {
        int i = MsgUrlService.RESULT_NOT_IMPL;
        try {
            Class<?> clazz = Class.forName("com.huawei.android.immersion.ImmersionStyle");
            i = ((Integer) clazz.getMethod("getSuggestionForgroundColorStyle", new Class[]{Integer.TYPE}).invoke(clazz.newInstance(), new Object[]{Integer.valueOf(colorBackground)})).intValue();
        } catch (ClassNotFoundException e) {
            MLog.e("HwUiStyleUtils", "getSuggestionForgroundColorStyle ClassNotFoundException");
        } catch (NoSuchMethodException e2) {
            MLog.e("HwUiStyleUtils", "getSuggestionForgroundColorStyle NoSuchMethodException");
        } catch (IllegalAccessException e3) {
            MLog.e("HwUiStyleUtils", "getSuggestionForgroundColorStyle IllegalAccessException");
        } catch (IllegalArgumentException e4) {
            MLog.e("HwUiStyleUtils", "getSuggestionForgroundColorStyle IllegalArgumentException");
        } catch (InvocationTargetException e5) {
            MLog.e("HwUiStyleUtils", "getSuggestionForgroundColorStyle InvocationTargetException");
        } catch (InstantiationException e6) {
            MLog.e("HwUiStyleUtils", "getSuggestionForgroundColorStyle InstantiationException");
        }
        return i;
    }

    private static int getSuggestionForgroundColorStyle(Context context) {
        if (context == null) {
            MLog.e("HwUiStyleUtils", "getSuggestionForgroundColorStyle->context = null ");
            return sCurrentSuggestionValue;
        }
        int primaryColor = getPrimaryColor(context);
        if (primaryColor == MsgUrlService.RESULT_NOT_IMPL) {
            MLog.w("HwUiStyleUtils", "getSuggestionForgroundColorStyle getPrimaryColor failed. ");
            sCurrentSuggestionValue = primaryColor;
            return sCurrentSuggestionValue;
        }
        int suggestColor = getSuggestionForgroundColorStyle(primaryColor);
        if (suggestColor == MsgUrlService.RESULT_NOT_IMPL) {
            MLog.w("HwUiStyleUtils", "getSuggestionForgroundColorStyle failed. ");
            sCurrentSuggestionValue = suggestColor;
            return sCurrentSuggestionValue;
        }
        sCurrentSuggestionValue = suggestColor;
        return suggestColor;
    }

    private static boolean isValidSuggestionForgroundColorStyle(int style) {
        return style == 1 || style == 0;
    }

    public static boolean isSuggestDarkStyle(Context context) {
        if (getSuggestionForgroundColorStyle(context) == 0) {
            return true;
        }
        return false;
    }

    public static boolean isNewImmersionStyle(Context context) {
        return isValidSuggestionForgroundColorStyle(getSuggestionForgroundColorStyle(context));
    }

    public static int getControlColor(Resources res) {
        if (res != null) {
            int colorfulId = res.getIdentifier("colorful_emui", "color", "androidhwext");
            if (colorfulId != 0) {
                return res.getColor(colorfulId);
            }
        }
        return 0;
    }

    public static Drawable getColorfulThemeDrawable(Context context, int controlColor) {
        Resources resources = context.getResources();
        Drawable stateDrawable = context.getResources().getDrawable(R.drawable.btn_check_on_mask);
        layers = new Drawable[2];
        stateDrawable.setTint(controlColor);
        layers[0] = stateDrawable;
        layers[1] = resources.getDrawable(R.drawable.btn_check_on_element_dark);
        return new LayerDrawable(layers);
    }

    public static void removeWhiteTitle(Object instance) {
        Object[] object = new Object[]{Boolean.valueOf(true)};
        Method method = getMethod("setHwFloating", instance.getClass(), object);
        if (method != null) {
            try {
                method.invoke(instance, object);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static Method getMethod(String methodName, Class<?> clazz, Object[] args) {
        for (Method method : clazz.getMethods()) {
            if (method.getName().equals(methodName)) {
                Class<?>[] ptypes = method.getParameterTypes();
                if (args == null) {
                    if (ptypes.length == 0) {
                        return method;
                    }
                } else if (args.length == ptypes.length) {
                    return method;
                }
            }
        }
        return null;
    }
}
