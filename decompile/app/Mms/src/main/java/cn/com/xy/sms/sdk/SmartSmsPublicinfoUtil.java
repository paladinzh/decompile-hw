package cn.com.xy.sms.sdk;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.text.TextUtils;
import cn.com.xy.sms.sdk.constant.Constant;
import cn.com.xy.sms.sdk.ui.popu.util.ThemeUtil;
import cn.com.xy.sms.sdk.util.DuoquUtils;
import cn.com.xy.sms.sdk.util.StringUtils;
import cn.com.xy.sms.util.ParseManager;
import cn.com.xy.sms.util.SdkCallBack;
import com.android.mms.MmsConfig;
import com.android.mms.data.Contact;
import com.android.mms.util.ItemLoadedCallback;
import com.google.android.gms.R;
import com.huawei.mms.util.AvatarCache;
import com.huawei.mms.util.HwBackgroundLoader;
import com.huawei.mms.util.ResEx;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import org.json.JSONObject;

public class SmartSmsPublicinfoUtil {
    private static int AVATART_WIDTH_HEIGHT = 0;
    private static String HUAWEI_NUMBER_NAME = null;
    static Map<String, String> mExtend = new HashMap();
    private static SmartSmsLogoCache mLogoCache = new SmartSmsLogoCache();
    private static ArrayList<Contact> mNeedLoadContact = new ArrayList();

    public static int getAvatartWidthHeight(Context context) {
        if (AVATART_WIDTH_HEIGHT > 0) {
            return AVATART_WIDTH_HEIGHT;
        }
        if (context != null) {
            try {
                AVATART_WIDTH_HEIGHT = (int) context.getResources().getDimension(R.dimen.duoqu_avatart_width_height);
            } catch (Throwable e) {
                SmartSmsSdkUtil.smartSdkExceptionLog("SmartSmsPublicinfoUtil getAvatartWidthHeight error: " + e.getMessage(), e);
            }
        }
        return AVATART_WIDTH_HEIGHT;
    }

    public static Drawable getDrawableFromCache(Context context, Contact c) {
        Drawable cacheResult = null;
        if (c == null || !MmsConfig.getSupportSmartSmsFeature() || !c.isXiaoyuanContact()) {
            return null;
        }
        if (c.isXyHwNumber()) {
            return ResEx.self().getAvtarDefault(c, -4);
        }
        if (!TextUtils.isEmpty(c.getXiaoyuanPhotoUri())) {
            cacheResult = mLogoCache.get(c.getXiaoyuanPhotoUri());
        }
        if (cacheResult == null) {
            return getClassifyAvatarFromCache(null, c.getClassifyCode());
        }
        return cacheResult;
    }

    public static boolean setDrawableByLogoName(Context context, String logoName, String classifyCode, boolean square, ItemLoadedCallback<Drawable> onLoaded, int width, int height, boolean isNotificationIcon) {
        Object obj = null;
        try {
            if (!TextUtils.isEmpty(logoName)) {
                obj = mLogoCache.get(logoName);
                if (obj == null) {
                    obj = getClassifyAvatarFromCache(context, classifyCode);
                    if (obj != null) {
                        loadImage(context, logoName, classifyCode, square, onLoaded, width, height, isNotificationIcon);
                    }
                }
            }
            if (obj != null) {
                onLoaded.onItemLoaded(obj, null);
                return true;
            }
            loadImage(context, logoName, classifyCode, square, onLoaded, width, height, isNotificationIcon);
            onLoaded.onItemLoaded(null, null);
            return false;
        } catch (Throwable e) {
            SmartSmsSdkUtil.smartSdkExceptionLog("SmartSmsPublicinfoUtil setDrawableByLogoName error: " + e.getMessage(), e);
        }
    }

    private static void loadImage(Context context, String logoName, String classifyCode, boolean square, ItemLoadedCallback<Drawable> onLoaded, int width, int height, boolean isNotificationIcon) {
        final Context context2 = context;
        final String str = classifyCode;
        final String str2 = logoName;
        final boolean z = square;
        final int i = width;
        final int i2 = height;
        final boolean z2 = isNotificationIcon;
        final ItemLoadedCallback<Drawable> itemLoadedCallback = onLoaded;
        HwBackgroundLoader.getBackgroundHandler().post(new Runnable() {
            public void run() {
                final Drawable classAvatar = SmartSmsPublicinfoUtil.loadClassifyAvatar(context2, str);
                final Drawable avatar = SmartSmsPublicinfoUtil.loadAvatar(context2, str2, z, i, i2, z2);
                Handler uIHandler = HwBackgroundLoader.getUIHandler();
                final ItemLoadedCallback itemLoadedCallback = itemLoadedCallback;
                uIHandler.post(new Runnable() {
                    public void run() {
                        if (avatar != null) {
                            itemLoadedCallback.onItemLoaded(avatar, null);
                        } else {
                            itemLoadedCallback.onItemLoaded(classAvatar, null);
                        }
                    }
                });
            }
        });
    }

    public static Drawable loadAvatar(Context context, String logoName, boolean square, int width, int height, boolean isNotificationIcon) {
        Drawable avatar = null;
        try {
            mExtend.put("syn", "true");
            BitmapDrawable drawable = ParseManager.findLogoByLogoName(context, logoName, width, height, 1, mExtend, null);
            if (drawable != null) {
                Bitmap orgBmp = drawable.getBitmap();
                if (square) {
                    Drawable avatar2 = new BitmapDrawable(context.getResources(), ResEx.getRoundedCornerBitmap(orgBmp, 6.0f));
                    try {
                        mLogoCache.put(logoName, avatar2);
                        avatar = avatar2;
                    } catch (Throwable th) {
                        Throwable e = th;
                        avatar = avatar2;
                        SmartSmsSdkUtil.smartSdkExceptionLog("SmartSmsPublicinfoUtil loadAvatar error: " + e.getMessage(), e);
                        return avatar;
                    }
                }
                avatar = createRoundPhotoDrawable(Constant.getContext(), orgBmp, 8, context.getResources().getColor(R.color.duoqu_avatar_stroke_color));
                if (avatar != null) {
                    mLogoCache.put(logoName, avatar);
                }
                orgBmp.recycle();
            }
        } catch (Throwable th2) {
            e = th2;
            SmartSmsSdkUtil.smartSdkExceptionLog("SmartSmsPublicinfoUtil loadAvatar error: " + e.getMessage(), e);
            return avatar;
        }
        return avatar;
    }

    public static Drawable loadClassifyAvatar(Context context, String classifyCode) {
        Drawable avatar = null;
        try {
            classifyCode = getClassifyCode(classifyCode);
            avatar = mLogoCache.get(classifyCode);
            if (avatar != null) {
                return avatar;
            }
            Drawable drawable = DuoquUtils.getSdkDoAction().getDrawableByNumber(context, classifyCode, null);
            if (drawable != null) {
                avatar = AvatarCache.createRoundPhotoDrawable(context, drawable, AvatarCache.pickColor(null, -2));
                if (avatar != null) {
                    mLogoCache.put(classifyCode, avatar);
                }
            }
            return avatar;
        } catch (Throwable e) {
            SmartSmsSdkUtil.smartSdkExceptionLog("SmartSmsPublicinfoUtil loadClassifyAvatar error: " + e.getMessage(), e);
        }
    }

    private static Drawable getClassifyAvatarFromCache(Context context, String classifyCode) {
        return mLogoCache.get(getClassifyCode(classifyCode));
    }

    private static String getClassifyCode(String classifyCode) {
        if (TextUtils.isEmpty(classifyCode) || classifyCode.length() < 3) {
            return "";
        }
        return classifyCode.substring(0, 3);
    }

    public static boolean loadPublicInfoByPhoneNumber(final Context context, final String phoneNumber, final Contact contact) {
        if (TextUtils.isEmpty(phoneNumber) || contact == null || StringUtils.isPhoneNumber(phoneNumber)) {
            return false;
        }
        final boolean[] queryResult = new boolean[]{false};
        try {
            Context context2 = context;
            String str = phoneNumber;
            ParseManager.queryPublicInfo(context2, str, 1, "", null, new SdkCallBack() {
                public void execute(Object... obj) {
                    if (obj != null && obj.length != 0) {
                        try {
                            String result = obj[0];
                            if (ThemeUtil.SET_NULL_STR.equals(result)) {
                                SmartSmsPublicinfoUtil.addNeedLoadContact(contact, true);
                                return;
                            }
                            if (!TextUtils.isEmpty(result)) {
                                Contact ct = Contact.get(phoneNumber, false);
                                JSONObject jsonObject = new JSONObject(result);
                                String name = jsonObject.optString("name");
                                contact.setXiaoyuanPhotoUri(jsonObject.optString("logoc"));
                                contact.setName(name);
                                contact.setPurpose(jsonObject.optString("purpose"));
                                contact.setClassifyCode(jsonObject.optString("classifyCode"));
                                contact.setIsXiaoyuanContact(true);
                                contact.setIsXyHwNumber(SmartSmsPublicinfoUtil.isXyHwNumber(context, name));
                                if (ct != null) {
                                    contact.refreshContact(ct, contact, ct.equals(contact));
                                }
                                queryResult[0] = true;
                            }
                        } catch (Throwable e) {
                            SmartSmsSdkUtil.smartSdkExceptionLog("loadPublicInfoByPhoneNumber callback error: " + e.getMessage(), e);
                        }
                    }
                }
            });
        } catch (Throwable e) {
            SmartSmsSdkUtil.smartSdkExceptionLog("loadPublicInfoByPhoneNumber error: " + e.getMessage(), e);
        }
        return queryResult[0];
    }

    public static void clearPublicInfo() {
        try {
            mLogoCache.clear();
        } catch (Exception e) {
            SmartSmsSdkUtil.smartSdkExceptionLog("clearPublicInfo error: ", e);
        }
    }

    public static void addNeedLoadContact(Contact contact, boolean highLevel) {
        if (contact != null) {
            long loadTimePeriod = 10000;
            if (!highLevel) {
                loadTimePeriod = 1000000;
            }
            long loadTime = System.currentTimeMillis();
            if (loadTime - contact.getLoadXyPubInfoTime() >= loadTimePeriod) {
                contact.setLoadXyPubInfoTime(loadTime);
                synchronized (mNeedLoadContact) {
                    if (!mNeedLoadContact.contains(contact)) {
                        mNeedLoadContact.add(contact);
                        if (mNeedLoadContact.size() > 15) {
                            mNeedLoadContact.remove(0);
                        }
                    }
                }
            }
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static void reLoadContact() {
        Throwable e;
        ArrayList<Contact> arrayList;
        Throwable th;
        try {
            synchronized (mNeedLoadContact) {
                try {
                    if (mNeedLoadContact.size() == 0) {
                        return;
                    }
                    ArrayList<Contact> loadContact = new ArrayList();
                    try {
                        loadContact.addAll(mNeedLoadContact);
                        mNeedLoadContact.clear();
                        try {
                        } catch (Throwable th2) {
                            e = th2;
                            arrayList = loadContact;
                            SmartSmsSdkUtil.smartSdkExceptionLog("loadPublicInfoByPhoneNumber error: " + e.getMessage(), e);
                        }
                    } catch (Throwable th3) {
                        th = th3;
                        arrayList = loadContact;
                        throw th;
                    }
                } catch (Throwable th4) {
                    th = th4;
                    throw th;
                }
            }
        } catch (Throwable th5) {
            e = th5;
            SmartSmsSdkUtil.smartSdkExceptionLog("loadPublicInfoByPhoneNumber error: " + e.getMessage(), e);
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static void setScrollStatu(int scrollStatu) {
        if (scrollStatu == 0) {
            synchronized (mNeedLoadContact) {
                if (mNeedLoadContact.size() == 0) {
                }
            }
        }
    }

    public static Drawable createRoundPhotoDrawable(Context context, Bitmap bitmap, int strokeWidth, int strokeColor) {
        if (bitmap == null) {
            return null;
        }
        int size = Math.min(bitmap.getWidth(), bitmap.getHeight());
        float roundPx = ((float) size) / 2.0f;
        Rect rect = new Rect(0, 0, size, size);
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        Bitmap output = Bitmap.createBitmap(size, size, Config.ARGB_8888);
        Canvas canvas = new Canvas(output);
        canvas.drawARGB(0, 0, 0, 0);
        canvas.drawCircle(roundPx, roundPx, roundPx, paint);
        paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);
        paint.setFilterBitmap(true);
        paint.setDither(true);
        paint.setColor(strokeColor);
        paint.setStyle(Style.STROKE);
        paint.setStrokeWidth((float) strokeWidth);
        canvas.drawCircle(roundPx, roundPx, roundPx, paint);
        BitmapDrawable bitmapDrawable = new BitmapDrawable(context.getResources(), output);
        bitmapDrawable.setBounds(0, 0, size, size);
        return bitmapDrawable;
    }

    public static String getName(String phoneNumber) {
        if (TextUtils.isEmpty(phoneNumber)) {
            return "";
        }
        Contact ct = Contact.get(phoneNumber, false);
        if (ct == null) {
            return "";
        }
        return ct.getName();
    }

    public static void reflashPublicInfo(final Context context, final String phoneNumber) {
        try {
            HwBackgroundLoader.getBackgroundHandler().post(new Runnable() {
                public void run() {
                    try {
                        Map<String, String> extend = new HashMap();
                        extend.put("QUERY_NOW", "true");
                        ParseManager.queryPublicInfo(context, phoneNumber, 1, "", extend);
                    } catch (Throwable e) {
                        SmartSmsSdkUtil.smartSdkExceptionLog("reflashPublicInfo error: " + e.getMessage(), e);
                    }
                }
            });
        } catch (Throwable e) {
            SmartSmsSdkUtil.smartSdkExceptionLog("reflashPublicInfo error: " + e.getMessage(), e);
        }
    }

    private static boolean isXyHwNumber(Context context, String name) {
        if (StringUtils.isNull(name)) {
            return false;
        }
        if (HUAWEI_NUMBER_NAME == null && context != null) {
            HUAWEI_NUMBER_NAME = context.getResources().getString(R.string.duoqu_huawei_number_name);
        }
        return name.equals(HUAWEI_NUMBER_NAME);
    }
}
