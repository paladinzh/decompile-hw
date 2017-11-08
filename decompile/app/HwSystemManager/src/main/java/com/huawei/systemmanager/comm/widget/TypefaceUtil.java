package com.huawei.systemmanager.comm.widget;

import android.content.Context;
import android.graphics.Typeface;
import android.text.TextUtils;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.secpatch.common.ConstValues;
import com.huawei.systemmanager.util.HwLog;
import java.io.File;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class TypefaceUtil {
    private static final Map<String, CustomTypefaceFactory> sCustomTypefaces = new HashMap();

    public static abstract class CustomTypefaceFactory {
        abstract Typeface getTypeface(Context context);
    }

    public static class QianheiTypeface extends CustomTypefaceFactory {
        public static final String NAME = "qianhei";
        private boolean checked = false;
        private Typeface mTf;

        Typeface getTypeface(Context ctx) {
            if (!shouldUseAdditionalChnFont()) {
                return null;
            }
            if (this.checked) {
                return this.mTf;
            }
            try {
                this.mTf = (Typeface) Typeface.class.getField("CHNFZSLIM").get(null);
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (NoSuchFieldException e2) {
                e2.printStackTrace();
            } catch (IllegalAccessException e3) {
                e3.printStackTrace();
            } catch (Exception e4) {
                e4.printStackTrace();
            }
            if (this.mTf == null) {
                HwLog.w(NAME, "can not get qianhei typeface");
            }
            this.checked = true;
            return this.mTf;
        }

        private boolean shouldUseAdditionalChnFont() {
            String curLang = Locale.getDefault().getLanguage();
            if (TextUtils.isEmpty(curLang) || !curLang.contains(ConstValues.CHINA_COUNTRY_CODE)) {
                return false;
            }
            return true;
        }
    }

    public static class RobotoLightTypeface extends CustomTypefaceFactory {
        public static final String NAME = "robotolight";
        private static final String ROBOTO_LIGHT_NAME = "sans-serif-light";
        private Typeface mTypeface;

        Typeface getTypeface(Context ctx) {
            if (this.mTypeface == null) {
                this.mTypeface = Typeface.create(ROBOTO_LIGHT_NAME, 0);
            }
            return this.mTypeface;
        }
    }

    static {
        sCustomTypefaces.put(QianheiTypeface.NAME, new QianheiTypeface());
        sCustomTypefaces.put(RobotoLightTypeface.NAME, new RobotoLightTypeface());
    }

    public static Typeface getTypefaceFromName(Context ctx, String name) {
        if (haveCustomFont(ctx) || TextUtils.isEmpty(name)) {
            return null;
        }
        CustomTypefaceFactory tfFactory = (CustomTypefaceFactory) sCustomTypefaces.get(name);
        if (tfFactory == null) {
            return null;
        }
        return tfFactory.getTypeface(ctx);
    }

    public static boolean haveCustomFont(Context ctx) {
        File file = new File(ctx.getString(R.string.skin_font_file_path));
        String[] fileNames = file.list();
        if (!file.exists() || fileNames == null || fileNames.length == 0) {
            return false;
        }
        return true;
    }
}
