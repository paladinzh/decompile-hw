package cn.com.xy.sms.sdk.ui.popu.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.util.LruCache;
import android.view.View;
import android.widget.TextView;
import cn.com.xy.sms.sdk.constant.Constant;
import cn.com.xy.sms.sdk.util.StringUtils;
import com.amap.api.services.core.AMapException;
import com.google.android.gms.R;
import com.google.android.gms.location.places.Place;

@SuppressLint({"NewApi"})
public class ThemeUtil {
    private static final int ERROR_INDEX = -9999;
    private static final int ERROR_RESID = -9998;
    public static final int SET_NULL = -1;
    public static final String SET_NULL_STR = "-1";
    private static LruCache<String, Integer> mResCache = new LruCache(100);
    private static LruCache<String, Integer> mTextColorCache = new LruCache(100);

    public static int getResIndex(String name) {
        try {
            return Integer.parseInt(name);
        } catch (Exception e) {
            return ERROR_INDEX;
        }
    }

    public static int getColorId(int colorNameIndex) {
        switch (colorNameIndex) {
            case 1000:
                return R.color.duoqu_theme_color_1000;
            case 1001:
                return R.color.duoqu_theme_color_1001;
            case 1010:
                return R.color.duoqu_theme_color_1010;
            case 1011:
                return R.color.duoqu_theme_color_1011;
            case 1012:
                return R.color.duoqu_theme_color_1012;
            case 1013:
                return R.color.duoqu_theme_color_1013;
            case Place.TYPE_ROUTE /*1020*/:
                return R.color.duoqu_theme_color_1020;
            case Place.TYPE_TRANSIT_STATION /*1030*/:
                return R.color.duoqu_theme_color_1030;
            case 1031:
                return R.color.duoqu_theme_color_1031;
            case 1050:
                return R.color.duoqu_theme_color_1050;
            case 1051:
                return R.color.duoqu_theme_color_1051;
            case 1090:
                return R.color.duoqu_theme_color_1090;
            case AMapException.CODE_AMAP_ENGINE_RESPONSE_ERROR /*1100*/:
                return R.color.duoqu_theme_color_1100;
            case 1110:
                return R.color.duoqu_theme_color_1110;
            case 3010:
                return R.color.duoqu_theme_color_3010;
            case 3011:
                return R.color.duoqu_theme_color_3011;
            case 3012:
                return R.color.duoqu_theme_color_3012;
            case 3020:
                return R.color.duoqu_theme_color_3020;
            case 3021:
                return R.color.duoqu_theme_color_3021;
            case 3023:
                return R.color.duoqu_theme_color_3023;
            case 4010:
                return R.color.duoqu_theme_color_4010;
            case 4011:
                return R.color.duoqu_theme_color_4011;
            case 4012:
                return R.color.duoqu_theme_color_4012;
            case 4013:
                return R.color.duoqu_theme_color_4013;
            case 5010:
                return R.color.duoqu_theme_color_5010;
            case 5011:
                return R.color.duoqu_theme_color_5011;
            case 5012:
                return R.color.duoqu_theme_color_5012;
            case 5013:
                return R.color.duoqu_theme_color_5013;
            case 5021:
                return R.color.duoqu_theme_color_5021;
            default:
                return ERROR_RESID;
        }
    }

    public static void setColorStateList(Context context, TextView textView, String relativePath_Pressed, int defaultResId_Pressed, String relativePath_nomal, int defaultResId_normal) {
        if (context != null && textView != null) {
            try {
                int pressedColor = getTextColor(context, relativePath_Pressed, defaultResId_Pressed);
                int normalColor = getTextColor(context, relativePath_nomal, defaultResId_normal);
                int[] colors = new int[]{pressedColor, pressedColor, normalColor, pressedColor, pressedColor, normalColor};
                int[][] states = new int[6][];
                states[0] = new int[]{16842919, 16842910};
                states[1] = new int[]{16842910, 16842908};
                states[2] = new int[]{16842910};
                states[3] = new int[]{16842908};
                states[4] = new int[]{16842909};
                states[5] = new int[0];
                textView.setTextColor(new ColorStateList(states, colors));
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
    }

    public static int getTextColor(Context context, String relativePath, int defaultResId) {
        if (StringUtils.isNull(relativePath)) {
            return context.getResources().getColor(defaultResId);
        }
        relativePath = relativePath.trim();
        Integer textColorId = (Integer) mTextColorCache.get(relativePath);
        if (textColorId == null) {
            int colorIndex = getResIndex(relativePath);
            if (colorIndex != ERROR_INDEX) {
                textColorId = Integer.valueOf(getColorId(colorIndex));
                if (textColorId.intValue() == ERROR_RESID) {
                    textColorId = Integer.valueOf(defaultResId);
                } else {
                    mTextColorCache.put(relativePath, textColorId);
                }
            } else {
                try {
                    return Color.parseColor(relativePath);
                } catch (Throwable th) {
                    textColorId = Integer.valueOf(defaultResId);
                }
            }
        }
        return context.getResources().getColor(textColorId.intValue());
    }

    public static void setTextColor(Context context, TextView textView, String relativePath, int defaultResId) {
        if (context != null && textView != null) {
            Integer colorId;
            try {
                if (StringUtils.isNull(relativePath)) {
                    textView.setTextColor(context.getResources().getColor(defaultResId));
                } else {
                    relativePath = relativePath.trim();
                    colorId = (Integer) mTextColorCache.get(relativePath);
                    if (colorId == null) {
                        int colorIndex = getResIndex(relativePath);
                        if (colorIndex != ERROR_INDEX) {
                            colorId = Integer.valueOf(getColorId(colorIndex));
                            if (colorId.intValue() == ERROR_RESID) {
                                colorId = Integer.valueOf(defaultResId);
                            } else {
                                mTextColorCache.put(relativePath, colorId);
                            }
                        } else {
                            textView.setTextColor(Color.parseColor(relativePath));
                            return;
                        }
                    }
                    textView.setTextColor(context.getResources().getColor(colorId.intValue()));
                }
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
    }

    public static int getColorInteger(Context context, String num) {
        if (StringUtils.isNull(num)) {
            return 0;
        }
        return context.getResources().getColor(getColorId(getResIndex(num)));
    }

    public static void setViewBg(Context context, View view, String relativePath, int defaultResId) {
        setViewBg(context, view, relativePath, -1, -1, false, defaultResId);
    }

    public static void setViewBg(Context context, View view, String relativePath, int resId, int width, int defaultResId) {
        setViewBg(context, view, relativePath, resId, width, false, defaultResId);
    }

    public static void setViewBg(Context context, View view, String relativePath, int resId, int width, boolean cache, int defaultColorId) {
        if (context != null && view != null) {
            try {
                Drawable dw = ViewUtil.getDrawable(context, relativePath, false, cache);
                if (dw != null) {
                    ViewUtil.setBackground(view, dw);
                    return;
                }
                if (StringUtils.isNull(relativePath)) {
                    view.setBackgroundResource(defaultColorId);
                } else {
                    relativePath = relativePath.trim();
                    Integer colorId = (Integer) mResCache.get(relativePath);
                    if (colorId == null) {
                        int colorIndex = getResIndex(relativePath);
                        if (colorIndex != ERROR_INDEX || !ViewUtil.setViewBg2(context, view, relativePath)) {
                            colorId = Integer.valueOf(getColorId(colorIndex));
                            if (colorId.intValue() == ERROR_RESID) {
                                colorId = Integer.valueOf(defaultColorId);
                            } else {
                                mResCache.put(relativePath, colorId);
                            }
                        } else {
                            return;
                        }
                    }
                    if (resId != -1) {
                        view.setBackgroundResource(resId);
                        GradientDrawable myGrad = (GradientDrawable) view.getBackground();
                        if (width > 0) {
                            myGrad.setStroke(width, Constant.getContext().getResources().getColor(colorId.intValue()));
                        } else {
                            myGrad.setColor(Constant.getContext().getResources().getColor(colorId.intValue()));
                        }
                    } else {
                        view.setBackgroundResource(colorId.intValue());
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
