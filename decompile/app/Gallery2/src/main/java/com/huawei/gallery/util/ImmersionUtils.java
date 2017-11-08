package com.huawei.gallery.util;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.android.gallery3d.R;
import com.android.gallery3d.ui.ColorTexture;
import com.android.gallery3d.util.ImmersionStyleWrapper;

public class ImmersionUtils {
    public static Drawable getColorDrawable(Context context) {
        Integer color = ImmersionStyleWrapper.getPrimaryColor(context);
        if (color == null) {
            return null;
        }
        return new ColorDrawable(color.intValue());
    }

    public static ColorTexture getColorTexture(Context context) {
        Integer color = ImmersionStyleWrapper.getPrimaryColor(context);
        if (color == null) {
            return null;
        }
        return new ColorTexture(color.intValue());
    }

    public static int getImmersionStyle(Context context) {
        int i = -1;
        Integer color = ImmersionStyleWrapper.getPrimaryColor(context);
        if (color == null) {
            return -1;
        }
        Integer style = ImmersionStyleWrapper.getSuggestionForgroundColorStyle(color.intValue());
        if (style != null) {
            i = style.intValue();
        }
        return i;
    }

    public static void setTextViewDefaultColorImmersionStyle(TextView textView, int galleryStyle) {
        if (textView != null) {
            int style = getImmersionStyle(textView.getContext());
            if (style != -1) {
                int colorRes;
                Resources res = textView.getResources();
                int idColorDark = res.getIdentifier("emui_action_bar_title_text_color", "color", "androidhwext");
                if (idColorDark == 0) {
                    idColorDark = R.color.emui_action_bar_title_text_color;
                }
                int idColorLight = res.getIdentifier("emui_action_bar_title_text_color_light", "color", "androidhwext");
                if (idColorLight == 0) {
                    idColorLight = R.color.emui_action_bar_title_text_color_light;
                }
                if (style == 0 && galleryStyle == 0) {
                    colorRes = idColorLight;
                } else {
                    colorRes = idColorDark;
                }
                textView.setTextColor(res.getColor(colorRes));
            }
        }
    }

    public static void setTextViewColorImmersionStyle(TextView textView, int lightRes, int darkRes) {
        if (textView != null) {
            textView.setTextColor(textView.getContext().getResources().getColor(getImmersionStyle(textView.getContext()) == 0 ? lightRes : darkRes));
        }
    }

    public static void setViewBackgroundImmersionStyle(View view, int lightRes, int darkRes, int galleryStyle) {
        if (view != null) {
            int style = getImmersionStyle(view.getContext());
            if (style != -1) {
                int bgRes;
                if (style == 0 && galleryStyle == 0) {
                    bgRes = lightRes;
                } else {
                    bgRes = darkRes;
                }
                view.setBackground(getDrawableWithEmuiDefaultColor(view, bgRes));
            }
        }
    }

    public static Drawable getDrawableWithEmuiDefaultColor(View view, int drawableId) {
        Context currentContext = view.getContext();
        Drawable drawable = currentContext.getResources().getDrawable(drawableId);
        drawable.setTint(currentContext.getResources().getColor(R.color.actionbar_bar_title_icon_light));
        return drawable;
    }

    public static void setImageViewSrcImmersionStyle(ImageView imageView, int lightRes, int darkRes, int galleryStyle) {
        setImageViewSrcImmersionStyle(imageView, lightRes, darkRes, galleryStyle, false);
    }

    public static void setImageViewSrcImmersionStyle(ImageView imageView, int lightRes, int darkRes, int galleryStyle, boolean force) {
        boolean z = false;
        if (imageView != null) {
            int style = getImmersionStyle(imageView.getContext());
            if (style != -1 || force) {
                Context context = imageView.getContext();
                if (style == 0 && galleryStyle == 0) {
                    z = true;
                }
                imageView.setImageDrawable(ColorfulUtils.mappingColorfulDrawable(context, z, lightRes, darkRes));
            }
        }
    }

    public static final boolean isActionbarBackgroundThemed(Context context) {
        boolean z = false;
        if (context == null) {
            return false;
        }
        Resources res = context.getResources();
        int colorID = res.getIdentifier("emui_custom_panel_background", "color", "androidhwext");
        if (colorID == 0) {
            return false;
        }
        if (res.getColor(colorID) == 0) {
            z = true;
        }
        return z;
    }

    public static int getControlColor(Context context) {
        if (context == null) {
            return 0;
        }
        Resources res = context.getResources();
        if (res == null) {
            return 0;
        }
        int colorfulId = res.getIdentifier("colorful_emui", "color", "androidhwext");
        if (colorfulId != 0) {
            return res.getColor(colorfulId);
        }
        return 0;
    }
}
