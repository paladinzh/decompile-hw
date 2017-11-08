package com.huawei.watermark.wmutil;

import android.graphics.Typeface;
import android.util.Log;
import android.widget.TextView;
import com.android.gallery3d.R;
import com.huawei.watermark.manager.parse.WMConfig;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

public class WMStringUtil {
    private static final String TAG = ("CAMERA3WATERMARK_" + WMStringUtil.class.getSimpleName());
    private static Typeface mTypeface = null;
    private static Typeface sDroidRobotLightFont = null;
    private static Typeface sDroidRobotRegularFont = null;
    private static Typeface sDroidSansChineseFont = null;
    private static Typeface sDroidSansChineseLimFont = null;

    public static boolean isEmptyString(String str) {
        return str != null ? str.trim().equals("") : true;
    }

    public static List<String> split(String str, String separator) {
        if (str == null) {
            return null;
        }
        StringTokenizer tokenizer = new StringTokenizer(str, separator);
        ArrayList<String> substrings = new ArrayList();
        while (tokenizer.hasMoreElements()) {
            substrings.add(tokenizer.nextToken());
        }
        return substrings;
    }

    private static Typeface getChineseLimType() {
        if (mTypeface != null) {
            return mTypeface;
        }
        try {
            mTypeface = Typeface.createFromFile("/system/fonts/slim.ttf");
            return mTypeface;
        } catch (Exception e) {
            Log.e(TAG, "set font failure");
            return null;
        }
    }

    public static void setChineseLimType(TextView view) {
        if (isChineseLimTypeSupported(view)) {
            Typeface typeface = getChineseLimType();
            if (typeface != null) {
                view.setTypeface(typeface);
            }
        }
    }

    private static boolean isChineseLimTypeSupported(TextView view) {
        return view.getContext().getResources().getConfiguration().locale.getLanguage().endsWith(WMConfig.SUPPORTZH);
    }

    public static void setEditTextStringStyle(TextView view) {
        if (view != null) {
            setChineseLimType(view);
            view.setTextSize(2, (float) view.getResources().getInteger(R.integer.watermark_edit_text_size_sp));
            view.setTextColor(-16777216);
        }
    }
}
