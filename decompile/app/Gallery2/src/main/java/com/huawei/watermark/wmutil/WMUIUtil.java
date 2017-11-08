package com.huawei.watermark.wmutil;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Resources.NotFoundException;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import java.util.HashMap;
import java.util.Vector;

public class WMUIUtil {
    public static View getChildViewByTag(ViewGroup viewGroup, String tag) {
        if (WMStringUtil.isEmptyString(tag)) {
            return null;
        }
        View result = null;
        for (int i = 0; i < viewGroup.getChildCount(); i++) {
            View view = viewGroup.getChildAt(i);
            String tagtemp = (String) view.getTag();
            if (!WMStringUtil.isEmptyString(tag) && tagtemp.equalsIgnoreCase(tag)) {
                result = view;
                break;
            }
        }
        return result;
    }

    public static void recycleViewGroup(ViewGroup viewGroup) {
        if (viewGroup != null) {
            recycleView(viewGroup);
            for (int i = 0; i < viewGroup.getChildCount(); i++) {
                View view = viewGroup.getChildAt(i);
                recycleView(view);
                if (view instanceof ViewGroup) {
                    recycleViewGroup((ViewGroup) view);
                }
            }
        }
    }

    public static void recycleView(View view) {
        if (view != null) {
            recycleDrawable(view.getBackground());
            view.setBackground(null);
            if (view instanceof ImageView) {
                recycleDrawable(((ImageView) view).getDrawable());
                ((ImageView) view).setImageBitmap(null);
            }
        }
    }

    private static void recycleDrawable(Drawable dr) {
        if (dr != null && (dr instanceof BitmapDrawable)) {
            WMBitmapUtil.recycleReuseBitmap(((BitmapDrawable) dr).getBitmap());
        }
    }

    public static void showNumAndIcon(LinearLayout layout, String value, int singlenumw, int numH, HashMap<String, String> iconName, String zipPath, float scale, int marginw) {
        Vector<ImageView> temp_vec = new Vector();
        if (!WMStringUtil.isEmptyString(value)) {
            String[] values = value.split("");
            int i = 0;
            while (i < values.length) {
                boolean isInt;
                int dpToPixel;
                String consNumImageNameFromInt;
                try {
                    Integer.parseInt(values[i]);
                    isInt = true;
                } catch (Exception e) {
                    isInt = false;
                }
                ImageView iv_num = new ImageView(layout.getContext());
                LayoutParams lp = new LayoutParams(-2, -2);
                if (singlenumw > 0) {
                    dpToPixel = WMBaseUtil.dpToPixel((float) singlenumw, layout.getContext());
                } else {
                    dpToPixel = singlenumw;
                }
                lp.width = dpToPixel;
                if (numH > 0) {
                    dpToPixel = WMBaseUtil.dpToPixel((float) numH, layout.getContext());
                } else {
                    dpToPixel = numH;
                }
                lp.height = dpToPixel;
                if (lp.width > 0) {
                    lp.width = Math.round(((float) lp.width) * scale);
                }
                if (lp.height > 0) {
                    lp.height = Math.round(((float) lp.height) * scale);
                }
                if (marginw != 0) {
                    lp.rightMargin = WMBaseUtil.dpToPixel((float) marginw, layout.getContext());
                }
                iv_num.setLayoutParams(lp);
                if (isInt) {
                    consNumImageNameFromInt = consNumImageNameFromInt(Integer.parseInt(values[i]));
                } else if (iconName == null) {
                    consNumImageNameFromInt = null;
                } else {
                    consNumImageNameFromInt = consIconImageNameFromText((String) iconName.get(values[i]));
                    if (consNumImageNameFromInt == null && "-".equalsIgnoreCase(values[i])) {
                        consNumImageNameFromInt = consIconImageNameFromText("-");
                    }
                }
                if (!WMStringUtil.isEmptyString(consNumImageNameFromInt)) {
                    iv_num.setImageBitmap(WMFileUtil.decodeBitmap(layout.getContext(), zipPath, consNumImageNameFromInt));
                    temp_vec.add(iv_num);
                }
                i++;
            }
            layout.removeAllViews();
            for (i = 0; i < temp_vec.size(); i++) {
                layout.addView((View) temp_vec.elementAt(i));
            }
            temp_vec.clear();
        }
    }

    private static String consNumImageNameFromInt(int num) {
        return "" + num + ".png";
    }

    private static String consIconImageNameFromText(String icon) {
        if (WMStringUtil.isEmptyString(icon)) {
            return null;
        }
        return icon + ".png";
    }

    public static String getDecoratorText(Context context, String value) {
        int resId = WMResourceUtil.getStringId(context, value);
        String res = value;
        if (resId > 0) {
            try {
                res = context.getResources().getString(resId);
            } catch (NotFoundException e) {
            }
        }
        return res;
    }

    @TargetApi(17)
    public static boolean isLayoutDirectionRTL(Context context) {
        boolean z = true;
        if (context == null) {
            return false;
        }
        if (context.getResources().getConfiguration().getLayoutDirection() != 1) {
            z = false;
        }
        return z;
    }

    public static void separateView(View wmView) {
        if (wmView.getParent() != null) {
            ((ViewGroup) wmView.getParent()).removeView(wmView);
        }
    }

    public static float[] getWH(float w_src, float h_src, boolean isHORIZONTAL) {
        float[] wh = new float[]{w_src, h_src};
        if (isHORIZONTAL) {
            wh[0] = h_src;
            wh[1] = w_src;
        }
        return wh;
    }

    public static int[] rebasePosition(float x, float y, float w, float h, int rect_w, int rect_h) {
        int[] res = new int[]{(int) x, (int) y};
        if (rect_w > 0 && rect_h > 0) {
            if (x + w > ((float) rect_w)) {
                x = ((float) rect_w) - w;
            }
            if (y + h > ((float) rect_h)) {
                y = ((float) rect_h) - h;
            }
            res[0] = Math.max(0, (int) x);
            res[1] = Math.max(0, (int) y);
        }
        return res;
    }
}
