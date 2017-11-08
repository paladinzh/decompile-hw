package com.huawei.gallery.util;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import com.android.gallery3d.R;

public class ColorfulUtils {
    public static Drawable mappingColorfulDrawable(Context context, boolean light, int lightRes, int darkRes) {
        if (context == null) {
            return null;
        }
        int res;
        if (light) {
            res = lightRes;
        } else {
            res = darkRes;
        }
        if (res == 0) {
            return null;
        }
        int color = ImmersionUtils.getControlColor(context);
        int hwExtRes;
        Drawable drawable;
        if (color == 0) {
            if (res == R.drawable.ic_public_more) {
                hwExtRes = getHwExtDrawable(context.getResources(), "ic_public_more");
                if (hwExtRes != 0) {
                    res = hwExtRes;
                }
            }
            drawable = context.getDrawable(res);
            if (!isNeedKeepColorIcon(res)) {
                drawable.setTint(light ? context.getColor(R.color.icon_white_background_color) : context.getColor(R.color.icon_black_background_color));
            }
            return drawable;
        }
        switch (res) {
            case R.drawable.allfocus_mono_pinfocus:
                hwExtRes = getHwExtDrawable(context.getResources(), light ? "btn_check_on_colorful" : "btn_check_on_colorful_dark");
                if (hwExtRes != 0) {
                    return context.getDrawable(hwExtRes);
                }
                break;
            case R.drawable.ic_gallery_info_actived:
            case R.drawable.ic_gallery_multiscreen_activited:
            case R.drawable.ic_public_favor:
            case R.drawable.ic_public_remove:
                drawable = context.getDrawable(res);
                drawable.setTint(color);
                return drawable;
        }
        return context.getDrawable(res);
    }

    private static boolean isNeedKeepColorIcon(int res) {
        if (res == R.drawable.allfocus_mono || res == R.drawable.allfocus_mono_pinfocus || res == R.drawable.ic_gallery_info_actived || res == R.drawable.ic_public_download_tips_gallery || res == R.drawable.ic_gallery_multiscreen_activited) {
            return true;
        }
        return false;
    }

    public static Drawable mappingColorfulDrawableForce(Context context, int res) {
        if (context == null || res == 0) {
            return null;
        }
        int color = ImmersionUtils.getControlColor(context);
        if (color == 0) {
            return context.getDrawable(res);
        }
        Drawable drawable = context.getDrawable(res);
        drawable.setTint(color);
        return drawable;
    }

    public static int mappingColorfulColor(Context context, int defaultColor) {
        if (context == null) {
            return defaultColor;
        }
        int color = ImmersionUtils.getControlColor(context);
        if (color == 0) {
            return defaultColor;
        }
        return color;
    }

    public static int getHwExtDrawable(Resources resources, String drawableName) {
        if (resources == null || drawableName == null) {
            return 0;
        }
        return resources.getIdentifier(drawableName, "drawable", "androidhwext");
    }

    public static void decorateColorfulForEditText(Context context, EditText editText) {
        if (context != null && editText != null) {
            int color = ImmersionUtils.getControlColor(context);
            if (color != 0) {
                editText.setHighlightColor(color);
            }
        }
    }

    public static void decorateColorfulForImageView(Context context, ImageView imageView) {
        if (context != null && imageView != null) {
            int color = ImmersionUtils.getControlColor(context);
            if (color != 0) {
                Drawable drawable = imageView.getDrawable();
                if (!(drawable == null || drawable.getCurrent() == null)) {
                    drawable.getCurrent().setTint(color);
                }
            }
        }
    }

    public static void decorateColorfulForTextView(Context context, TextView textView) {
        if (context != null && textView != null) {
            int color = ImmersionUtils.getControlColor(context);
            if (color != 0) {
                textView.setTextColor(color);
                Drawable[] drawables = textView.getCompoundDrawables();
                if (drawables != null) {
                    for (Drawable drawable : drawables) {
                        if (drawable != null) {
                            drawable.getCurrent().setTint(color);
                        }
                    }
                }
            }
        }
    }

    public static void decorateColorfulForSeekbar(Context context, SeekBar seekBar) {
        if (seekBar != null) {
            int color = mappingColorfulColor(context, 0);
            if (color != 0) {
                seekBar.getThumb().setTint(color);
                Drawable drawable = seekBar.getProgressDrawable().getCurrent();
                if (drawable instanceof LayerDrawable) {
                    Drawable drawable1 = ((LayerDrawable) drawable).findDrawableByLayerId(16908301);
                    if (drawable1 != null) {
                        drawable1.setTint(color);
                    }
                }
            }
        }
    }
}
