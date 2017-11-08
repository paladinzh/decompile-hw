package cn.com.xy.sms.sdk.ui.popu.util;

import android.content.Context;
import android.content.res.Resources.NotFoundException;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Canvas;
import android.graphics.NinePatch;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.NinePatchDrawable;
import android.os.Build.VERSION;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import cn.com.xy.sms.sdk.SmartSmsSdkUtil;
import cn.com.xy.sms.sdk.constant.Constant;
import cn.com.xy.sms.sdk.smsmessage.BusinessSmsMessage;
import cn.com.xy.sms.sdk.util.KeyManager;
import cn.com.xy.sms.sdk.util.StringUtils;
import cn.com.xy.sms.sdk.util.f;
import com.google.android.gms.location.places.Place;
import java.io.Closeable;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.nio.charset.Charset;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONObject;

/* compiled from: Unknown */
public class ViewUtil {
    private static int a = -1;
    private static int b = -1;
    private static final Charset c = Charset.forName("UTF-8");

    private static String a(InputStream inputStream) {
        return readFully(new InputStreamReader(inputStream, c));
    }

    public static void clearSpan() {
        try {
            ((c) c.a()).b();
        } catch (Throwable th) {
        }
    }

    public static BitmapDrawable createBitmapByPath(Context context, String str, int i, int i2) {
        try {
            Options options = new Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(str, options);
            int i3 = options.outWidth;
            int i4 = options.outHeight;
            options.inDensity = i3;
            options.inTargetDensity = i;
            options.inJustDecodeBounds = false;
            Bitmap decodeFile = BitmapFactory.decodeFile(str, options);
            decodeFile.setDensity(i3);
            return new BitmapDrawable(context.getResources(), decodeFile);
        } catch (Throwable th) {
            return null;
        }
    }

    public static BitmapDrawable createBitmapByPath2(Context context, File file, int i, int i2) {
        Closeable fileInputStream;
        Throwable th;
        Closeable closeable = null;
        try {
            fileInputStream = new FileInputStream(file);
            try {
                FileDescriptor fd = fileInputStream.getFD();
                Options options = new Options();
                options.inJustDecodeBounds = true;
                BitmapFactory.decodeFileDescriptor(fd, null, options);
                int i3 = options.outWidth;
                options.inDensity = i3;
                options.inTargetDensity = i;
                options.inJustDecodeBounds = false;
                Bitmap decodeFileDescriptor = BitmapFactory.decodeFileDescriptor(fd, null, options);
                decodeFileDescriptor.setDensity(i3);
                BitmapDrawable bitmapDrawable = new BitmapDrawable(context.getResources(), decodeFileDescriptor);
                f.a(fileInputStream);
                return bitmapDrawable;
            } catch (Throwable th2) {
                Throwable th3 = th2;
                closeable = fileInputStream;
                th = th3;
                f.a(closeable);
                throw th;
            }
        } catch (Throwable th4) {
            th = th4;
            f.a(closeable);
            throw th;
        }
    }

    public static synchronized BitmapDrawable createBitmapByPath2(Context context, String str, int i, int i2) {
        BitmapDrawable createBitmapByPath2;
        synchronized (ViewUtil.class) {
            createBitmapByPath2 = createBitmapByPath2(context, new File(str), i, i2);
        }
        return createBitmapByPath2;
    }

    public static Drawable createDrawableByPath(Context context, String str) {
        return createDrawableByPath(context, str, true);
    }

    public static Drawable createDrawableByPath(Context context, String str, boolean z) {
        try {
            if (StringUtils.isNull(str)) {
                return null;
            }
            if (str.indexOf(".9.") == -1) {
                Bitmap decodeFile = BitmapFactory.decodeFile(str);
                if (decodeFile != null) {
                    decodeFile.setDensity(getDensity(context));
                    return new BitmapDrawable(context.getResources(), decodeFile);
                } else if (!z) {
                    return null;
                } else {
                    throw new Exception(str);
                }
            }
            Bitmap decodeFile2 = BitmapFactory.decodeFile(str);
            if (decodeFile2 == null) {
                if (z) {
                    throw new Exception(str);
                }
                return null;
            }
            decodeFile2.setDensity(getDensity(context));
            byte[] ninePatchChunk = decodeFile2.getNinePatchChunk();
            return NinePatch.isNinePatchChunk(ninePatchChunk) ? new NinePatchDrawable(context.getResources(), decodeFile2, ninePatchChunk, new Rect(), null) : null;
        } catch (Throwable th) {
            if (z) {
                Exception exception = new Exception(str);
            }
        }
    }

    public static Bitmap createRepeaterX(int i, Bitmap bitmap) {
        Bitmap createBitmap = Bitmap.createBitmap(bitmap.getWidth() * i, bitmap.getHeight(), Config.ARGB_8888);
        Canvas canvas = new Canvas(createBitmap);
        for (int i2 = 0; i2 < i; i2++) {
            canvas.drawBitmap(bitmap, (float) (bitmap.getWidth() * i2), 0.0f, null);
        }
        return createBitmap;
    }

    public static View createView(Context context, int i) {
        View view = new View(context);
        view.setId(i);
        return view;
    }

    public static View createViewFromResource(Context context, int i, ViewGroup viewGroup, boolean z) {
        try {
            return ((LayoutInflater) context.getSystemService("layout_inflater")).inflate(i, viewGroup, z);
        } catch (Throwable th) {
            return null;
        }
    }

    public static int dp2px(Context context, int i) {
        return (int) TypedValue.applyDimension(1, (float) i, context.getResources().getDisplayMetrics());
    }

    public static boolean formatSpanString(TextView textView, CharSequence charSequence, JSONObject jSONObject, Map<String, String> map) {
        if (textView == null || charSequence == null || jSONObject == null) {
            return false;
        }
        try {
            JSONArray optJSONArray = jSONObject.optJSONArray("items");
            if (optJSONArray == null || optJSONArray.length() <= 0) {
                return false;
            }
            boolean z;
            int i;
            int i2;
            Object obj;
            CharSequence optString = jSONObject.optString("sign");
            if (!(TextUtils.isEmpty(optString) || map == null)) {
                map.put("sign", optString);
            }
            CharSequence spannableStringBuilder = new SpannableStringBuilder(charSequence);
            if (map == null) {
                z = false;
                i = 0;
                i2 = 0;
                obj = null;
            } else {
                int intValue;
                String str;
                String valueOf = String.valueOf(map.get("msgId"));
                if (map.containsKey("normal_color")) {
                    try {
                        i = Integer.valueOf((String) map.get("normal_color")).intValue();
                    } catch (Throwable th) {
                    }
                    if (map.containsKey("pressed_color")) {
                        try {
                            intValue = Integer.valueOf((String) map.get("pressed_color")).intValue();
                        } catch (Throwable th2) {
                        }
                        if (map.containsKey("selected_bg_color")) {
                            try {
                                a.a(Integer.valueOf((String) map.get("selected_bg_color")).intValue());
                            } catch (Throwable th3) {
                            }
                        }
                        if (map.containsKey("under_line")) {
                            try {
                                str = valueOf;
                                i2 = i;
                                i = intValue;
                                z = Boolean.parseBoolean((String) map.get("under_line"));
                            } catch (Throwable th4) {
                            }
                        }
                        str = valueOf;
                        i2 = i;
                        i = intValue;
                        z = false;
                    }
                    intValue = 0;
                    if (map.containsKey("selected_bg_color")) {
                        a.a(Integer.valueOf((String) map.get("selected_bg_color")).intValue());
                    }
                    if (map.containsKey("under_line")) {
                        str = valueOf;
                        i2 = i;
                        i = intValue;
                        z = Boolean.parseBoolean((String) map.get("under_line"));
                    }
                    str = valueOf;
                    i2 = i;
                    i = intValue;
                    z = false;
                }
                i = 0;
                if (map.containsKey("pressed_color")) {
                    intValue = Integer.valueOf((String) map.get("pressed_color")).intValue();
                    if (map.containsKey("selected_bg_color")) {
                        a.a(Integer.valueOf((String) map.get("selected_bg_color")).intValue());
                    }
                    if (map.containsKey("under_line")) {
                        str = valueOf;
                        i2 = i;
                        i = intValue;
                        z = Boolean.parseBoolean((String) map.get("under_line"));
                    }
                    str = valueOf;
                    i2 = i;
                    i = intValue;
                    z = false;
                }
                intValue = 0;
                if (map.containsKey("selected_bg_color")) {
                    a.a(Integer.valueOf((String) map.get("selected_bg_color")).intValue());
                }
                if (map.containsKey("under_line")) {
                    str = valueOf;
                    i2 = i;
                    i = intValue;
                    z = Boolean.parseBoolean((String) map.get("under_line"));
                }
                str = valueOf;
                i2 = i;
                i = intValue;
                z = false;
            }
            for (int i3 = 0; i3 < optJSONArray.length(); i3++) {
                JSONObject optJSONObject = optJSONArray.optJSONObject(i3);
                if (obj != null) {
                    optJSONObject.put("msgId", obj);
                }
                int intValue2 = Integer.valueOf((String) optJSONObject.get("startIndex")).intValue();
                int intValue3 = Integer.valueOf((String) optJSONObject.get("endIndex")).intValue();
                d dVar = new d(optJSONObject, map);
                dVar.a(i2, i);
                dVar.a(z);
                spannableStringBuilder.setSpan(dVar, intValue2, intValue3, 33);
            }
            textView.setText(spannableStringBuilder);
            textView.setMovementMethod(c.a());
            return true;
        } catch (Throwable th5) {
            return false;
        }
    }

    public static int getChannelType() {
        if (b == -1) {
            try {
                KeyManager.initAppKey();
            } catch (Throwable th) {
            }
            if (!"NQIDAQABCOOL".equals(KeyManager.channel)) {
                if (!"1w36SBLwVNEW_ZTE".equals(KeyManager.channel)) {
                    if ("GwIDAQABZTE".equals(KeyManager.channel)) {
                        b = 4;
                    } else if ("VMhlWdEwVNEW_LENOVO".equals(KeyManager.channel)) {
                        b = 3;
                    } else if ("Oq3iD6UlMAGIC".equals(KeyManager.channel)) {
                        b = 5;
                    } else if ("1i1BDH2wONE+".equals(KeyManager.channel) || "1i1BDH2wONE+CARD".equals(KeyManager.channel)) {
                        b = 6;
                    } else if ("3GdfMSKwHUAWEI".equals(KeyManager.channel)) {
                        b = 7;
                    } else if ("rq7Fyxl5DUOQU".equals(KeyManager.channel)) {
                        b = 8;
                    } else if ("j3FIT5mwLETV".equals(KeyManager.channel)) {
                        b = 9;
                    } else {
                        if (!"0GCSqGSITOS".equals(KeyManager.channel)) {
                            if (!"D6mKXM8MEIZU".equals(KeyManager.channel)) {
                                if (!("XRyvMvZwSMARTISAN".equals(KeyManager.channel) || "dToXA5JQDAKELE".equals(KeyManager.channel))) {
                                    if (!"p5O4wKmwGIONEE".equals(KeyManager.channel)) {
                                        if (!"z5N7W51wKINGSUN".equals(KeyManager.channel)) {
                                            if (!("Cko59T6wSUGAR".equals(KeyManager.channel) || "oWIH+3ZQLEIDIANOS".equals(KeyManager.channel))) {
                                                if (!"al30zFgQTEST_T".equals(KeyManager.channel)) {
                                                    if (!("gsjHPHwIKOOBEE".equals(KeyManager.channel) || "QlTNSIgQWENTAI2".equals(KeyManager.channel) || "JqyMtaHQNUBIA".equals(KeyManager.channel))) {
                                                        if ("15Du354QGIONEECARD".equals(KeyManager.channel)) {
                                                            b = 13;
                                                        } else if ("rahtBH7wTCL".equals(KeyManager.channel)) {
                                                            b = 14;
                                                        } else if ("xU6UT6pwTOS2".equals(KeyManager.channel)) {
                                                            b = 15;
                                                        } else if ("5Gx84kmwYULONG_COOLPAD".equals(KeyManager.channel)) {
                                                            b = 16;
                                                        } else if ("tnjdWFeQKTOUCH".equals(KeyManager.channel)) {
                                                            b = 19;
                                                        } else if (!("Uj2pznXQHCT".equals(KeyManager.channel) || "XkXZJmwIPPTV".equals(KeyManager.channel))) {
                                                            if ("dGxSiEbwTOSCARD".equals(KeyManager.channel)) {
                                                                b = 17;
                                                            } else if ("PzqP0ONQTOSWATCH".equals(KeyManager.channel) || "VCTyBOSwSmartisan".equals(KeyManager.channel)) {
                                                                b = 18;
                                                            } else if (!("5rLWVKgQMEITU_PHONE".equals(KeyManager.channel) || "zcK2P6yQINNOS".equals(KeyManager.channel) || "RbWRsTYQdroi".equals(KeyManager.channel) || "J2kSrxdQGigaset".equals(KeyManager.channel) || "5zZZdrFQIUNI".equals(KeyManager.channel) || "nZpg6u3wDOOV".equals(KeyManager.channel) || "NsJCCyFwPHILIPS".equals(KeyManager.channel) || "UdcqV6aQLANMO".equals(KeyManager.channel) || "PunKwZfwHISENSE".equals(KeyManager.channel) || "gO0o2CXwVIVO".equals(KeyManager.channel) || "K8wgPuIwFREEMEOS".equals(KeyManager.channel))) {
                                                                if (!"DAS9exiQQIKUBOX".equals(KeyManager.channel)) {
                                                                    if ("d7tjnrkwCNSAMSUNG".equals(KeyManager.channel)) {
                                                                        b = 22;
                                                                    } else if (!"uDM3hYtwGIGASET".equals(KeyManager.channel)) {
                                                                        if ("OmwdltCwONEPLUS2".equals(KeyManager.channel)) {
                                                                            b = 23;
                                                                        } else {
                                                                            if (!"mmNPM4cQVNEW_ZTE2".equals(KeyManager.channel)) {
                                                                                if ("ZkhM4GyQ360OS".equals(KeyManager.channel)) {
                                                                                    b = 25;
                                                                                } else if (SmartSmsSdkUtil.DUOQU_SDK_CHANNEL.equals(KeyManager.channel)) {
                                                                                    b = 26;
                                                                                } else if ("vRICR8qQYULONG_COOLPAD2".equals(KeyManager.channel)) {
                                                                                    b = 27;
                                                                                } else if ("i3GPvZLwASUS".equals(KeyManager.channel)) {
                                                                                    b = 28;
                                                                                } else if ("cNNrw5WQEBEN".equals(KeyManager.channel)) {
                                                                                    b = 29;
                                                                                } else if (!("XHpWJNFQTCLOS".equals(KeyManager.channel) || "R1pU1XXwUNISCOPE".equals(KeyManager.channel))) {
                                                                                    if (!"gOLrCBhQMEIZU2".equals(KeyManager.channel)) {
                                                                                        if (!"MkekV0RQRAGENTEK".equals(KeyManager.channel)) {
                                                                                            if (!("YVmD5UkQ360OSBOX".equals(KeyManager.channel) || "2qqJKJbwZTE_TRIP".equals(KeyManager.channel))) {
                                                                                                if (!"n2zkSOdwZTE3".equals(KeyManager.channel)) {
                                                                                                    b = 0;
                                                                                                }
                                                                                            }
                                                                                        }
                                                                                    }
                                                                                }
                                                                            }
                                                                            b = 24;
                                                                        }
                                                                    }
                                                                }
                                                                b = 20;
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                            b = 12;
                                        }
                                    }
                                }
                            }
                            b = 11;
                        }
                        b = 10;
                    }
                }
                b = 2;
            }
            b = 1;
        }
        return b;
    }

    public static String getCompletePath(String str) {
        return Constant.getDRAWBLE_PATH() + str;
    }

    public static int getDensity(Context context) {
        if (a == -1) {
            if (getChannelType() == 1 || getChannelType() == 2 || getChannelType() == 5 || getChannelType() == 8 || getChannelType() == 20 || getChannelType() == 22 || getChannelType() == 26 || getChannelType() == 28 || getChannelType() == 29) {
                context.getResources().getDisplayMetrics();
                a = 480;
            } else {
                context.getResources().getDisplayMetrics();
                a = 240;
            }
        }
        return a;
    }

    public static float getDimension(int i) {
        try {
            return Constant.getContext().getResources().getDimension(i);
        } catch (NotFoundException e) {
            return 0.0f;
        }
    }

    public static Drawable getDrawable(Context context, String str, boolean z, boolean z2) {
        Drawable drawable = null;
        if (context == null) {
            return null;
        }
        try {
            if (!StringUtils.isNull(str)) {
                String trim = str.trim();
                Drawable imgDrawable;
                if (isImagePath(trim)) {
                    if (z2) {
                        imgDrawable = ResourceCacheUtil.getImgDrawable(trim);
                        if (!(imgDrawable == null || imgDrawable.getBitmap() == null || imgDrawable.getBitmap().isRecycled())) {
                            return imgDrawable;
                        }
                    }
                    drawable = createDrawableByPath(context, getCompletePath(trim));
                    if (z2 && (drawable instanceof BitmapDrawable)) {
                        ResourceCacheUtil.putImgDrawable(trim, (BitmapDrawable) drawable);
                    }
                } else if (isColorParam(trim)) {
                    if (z2) {
                        imgDrawable = ResourceCacheUtil.getColorDrawable(trim);
                        if (imgDrawable != null) {
                            return imgDrawable;
                        }
                    }
                    drawable = b.a(context, trim).a();
                    if (z2 && drawable != null) {
                        ResourceCacheUtil.putColorDrawable(trim, drawable);
                    }
                } else if (z) {
                    if (z2) {
                        imgDrawable = ResourceCacheUtil.getColorDrawable(trim);
                        if (imgDrawable != null) {
                            return imgDrawable;
                        }
                    }
                    imgDrawable = new ColorDrawable(ResourceCacheUtil.parseColor(trim));
                    if (z2) {
                        try {
                            ResourceCacheUtil.putColorDrawable(trim, imgDrawable);
                        } catch (Throwable th) {
                            drawable = imgDrawable;
                        }
                    }
                    drawable = imgDrawable;
                }
            }
        } catch (Throwable th2) {
        }
        return drawable;
    }

    public static String getXCode4() {
        return "3531344537383645";
    }

    public static boolean isColorParam(String str) {
        if (!StringUtils.isNull(str)) {
            if (str.indexOf(";") != -1 || str.indexOf("S#") != -1 || str.indexOf("C#") != -1 || str.indexOf("E#") != -1) {
                return true;
            }
        }
        return false;
    }

    public static boolean isImagePath(String str) {
        if (!StringUtils.isNull(str)) {
            String toLowerCase = str.toLowerCase();
            if (toLowerCase.endsWith("png") || toLowerCase.endsWith("jpg")) {
                return true;
            }
        }
        return false;
    }

    public static String readFully(Reader reader) {
        try {
            StringWriter stringWriter = new StringWriter();
            char[] cArr = new char[Place.TYPE_SUBLOCALITY_LEVEL_2];
            while (true) {
                int read = reader.read(cArr);
                if (read == -1) {
                    break;
                }
                stringWriter.write(cArr, 0, read);
            }
            String stringWriter2 = stringWriter.toString();
            return stringWriter2;
        } finally {
            reader.close();
        }
    }

    public static void recycle(Bitmap bitmap) {
        if (bitmap != null) {
            try {
                if (!bitmap.isRecycled()) {
                    bitmap.recycle();
                }
            } catch (Throwable th) {
            }
        }
    }

    public static void recycle(Drawable drawable) {
        if (drawable != null) {
            try {
                drawable.setCallback(null);
                if (drawable instanceof BitmapDrawable) {
                    recycle(((BitmapDrawable) drawable).getBitmap());
                } else if (drawable instanceof NinePatchDrawable) {
                    try {
                        NinePatchDrawable ninePatchDrawable = (NinePatchDrawable) drawable;
                        Field declaredField = NinePatchDrawable.class.getDeclaredField("mNinePatch");
                        if (declaredField != null) {
                            declaredField.setAccessible(true);
                            Object obj = declaredField.get(ninePatchDrawable);
                            if (obj != null) {
                                Field declaredField2 = NinePatch.class.getDeclaredField("mBitmap");
                                if (declaredField2 != null) {
                                    declaredField2.setAccessible(true);
                                    Bitmap bitmap = (Bitmap) declaredField2.get(obj);
                                    if (bitmap != null) {
                                        recycle(bitmap);
                                    }
                                }
                            }
                        }
                    } catch (Throwable th) {
                    }
                }
            } catch (Throwable th2) {
            }
        }
    }

    public static void recycleImageView(ImageView imageView) {
        if (imageView != null) {
            try {
                Drawable drawable = imageView.getDrawable();
                imageView.setImageDrawable(null);
                recycle(drawable);
            } catch (Throwable th) {
            }
        }
    }

    public static void recycleViewBg(View view) {
        if (view != null) {
            try {
                recycle(view.getBackground());
                view.setBackgroundDrawable(null);
            } catch (Throwable th) {
            }
        }
    }

    public static void setBackground(View view, Drawable drawable) {
        if (view == null) {
            return;
        }
        if (VERSION.SDK_INT <= 16) {
            view.setBackgroundDrawable(drawable);
        } else {
            view.setBackground(drawable);
        }
    }

    public static void setColor(View view, String str) {
        GradientDrawable gradientDrawable = (GradientDrawable) view.getBackground();
        if (!StringUtils.isNull(str)) {
            gradientDrawable.setColor(ResourceCacheUtil.parseColor(str));
        }
    }

    public static boolean setImageSrc(Context context, ImageView imageView, String str) {
        return setImageSrc(context, imageView, str, false);
    }

    public static boolean setImageSrc(Context context, ImageView imageView, String str, boolean z) {
        Drawable drawable = getDrawable(context, str, true, z);
        if (drawable == null) {
            return false;
        }
        imageView.setImageDrawable(drawable);
        return true;
    }

    public static void setTextViewValue(TextView textView, BusinessSmsMessage businessSmsMessage, String str) {
        setTextViewValue(textView, businessSmsMessage, str, "");
    }

    public static void setTextViewValue(TextView textView, BusinessSmsMessage businessSmsMessage, String str, int i, int i2, String str2, Context context) {
        if (!(textView == null || businessSmsMessage == null)) {
            try {
                String str3 = (String) businessSmsMessage.getValue(str);
                if (!StringUtils.isNull(str3)) {
                    if (str3.length() > i) {
                        textView.setTextSize((float) dp2px(context, i2));
                    }
                    setTextViewValue(textView, str3);
                } else if (StringUtils.isNull(str2)) {
                    setTextViewValue(textView, "");
                } else {
                    setTextViewValue(textView, str2);
                }
            } catch (Throwable th) {
            }
        }
    }

    public static void setTextViewValue(TextView textView, BusinessSmsMessage businessSmsMessage, String str, String str2) {
        if (!(textView == null || businessSmsMessage == null)) {
            try {
                String str3 = (String) businessSmsMessage.getValue(str);
                if (str3 == null) {
                    str3 = str2;
                }
                setTextViewValue(textView, str3);
            } catch (Throwable th) {
            }
        }
    }

    public static void setTextViewValue(TextView textView, String str) {
        if (textView != null) {
            CharSequence charSequence;
            if (str == null) {
                charSequence = "";
            }
            textView.setText(charSequence);
            if (getChannelType() == 2) {
                textView.requestLayout();
            }
        }
    }

    public static void setViewBg(Context context, View view, String str) {
        setViewBg(context, view, str, false);
    }

    public static void setViewBg(Context context, View view, String str, boolean z) {
        setBackground(view, getDrawable(context, str, true, z));
    }

    public static boolean setViewBg2(Context context, View view, String str) {
        if (!(context == null || view == null)) {
            try {
                if (!StringUtils.isNull(str)) {
                    String trim = str.trim();
                    Drawable createDrawableByPath;
                    if (isImagePath(trim)) {
                        createDrawableByPath = createDrawableByPath(context, getCompletePath(trim));
                        if (createDrawableByPath == null) {
                            return false;
                        }
                        view.setBackgroundDrawable(createDrawableByPath);
                        return true;
                    } else if (isColorParam(trim)) {
                        b a = b.a(context, trim);
                        if (a != null) {
                            createDrawableByPath = a.a();
                            if (createDrawableByPath != null) {
                                view.setBackground(createDrawableByPath);
                                return true;
                            }
                        }
                    } else {
                        try {
                            view.setBackgroundColor(ResourceCacheUtil.parseColor(trim));
                            return true;
                        } catch (Throwable th) {
                        }
                    }
                }
            } catch (Throwable th2) {
            }
        }
        return false;
    }
}
