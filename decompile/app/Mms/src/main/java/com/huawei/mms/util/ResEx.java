package com.huawei.mms.util;

import android.app.Application;
import android.content.Context;
import android.content.res.Resources.NotFoundException;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.widget.TextView;
import android.widget.TextView.BufferType;
import android.widget.Toast;
import com.android.mms.MmsApp;
import com.android.mms.data.Contact;
import com.android.mms.util.LruSoftCache;
import com.android.mms.util.MemCache;
import com.android.rcs.RcsCommonConfig;
import com.google.android.gms.R;
import com.huawei.csp.util.MmsInfo;
import com.huawei.cspcommon.MLog;
import com.huawei.cspcommon.util.CustomStateListDrawable;
import com.huawei.cspcommon.util.WallPaperImageHelper;
import com.huawei.rcs.util.RcsResEx;

public class ResEx {
    private static int emuiThemdId = 0;
    private static Drawable mDefaultContactImage = null;
    private static Drawable mUnknownContactImage = null;
    private static Drawable sHwLogo = null;
    private static final int[] sSubscriptionResIds = new int[]{R.drawable.icon_card_sim1, R.drawable.icon_card_sim2};
    private static ResEx selfInstance = null;
    final int cacheSize = (this.maxMemory / 8);
    private LruSoftCache<Integer, Drawable> mAvatarCache = new LruSoftCache(8);
    private int mBackgrounChanged = -1;
    private LruSoftCache<Integer, Integer> mColorCache = new LruSoftCache(10);
    private MemCache mContactDrawableCache;
    private Context mContext = null;
    private int mConvItemCountTextColor = -1;
    private int mConvItemErrorMsgTextColor = -1;
    private int mConvItemNewMsgColor = -1;
    private int mConvItemNormalColor = -1;
    private int mConvItemNormalTextColor = -1;
    private int mConvItemTextColor = -1;
    private int mConvItemUnreadTextColor = -1;
    private LruSoftCache<Integer, Drawable> mDrawableCache = new LruSoftCache(20);
    private Typeface mDroidSansChineseslim = null;
    private MemCache mHwNotificationCache;
    private int mMessageItemTextColorRecv = -1;
    private int mMessageItemTextColorSend = -1;
    private Toast mMmsToast;
    private int mMsgItemUnderPopColor = -1;
    private MemCache mNotificationCache;
    private RcsResEx mRcsResEx;
    private Typeface mRobotoLight = null;
    private int mSearchTextHightColor = -1;
    private LruSoftCache<Integer, Drawable> mStateListDrawableCache = new LruSoftCache(20);
    final int maxMemory = ((int) Runtime.getRuntime().maxMemory());
    private Drawable sBlackContactImage = null;
    private Drawable sDefaultContactImage = null;
    private Drawable sDefaultContactImage64 = null;
    private Bitmap sEmptyImagePic = null;
    private Bitmap sEmptyVedioPic = null;
    private Drawable sFavoritesContactImage = null;
    private Drawable sHuaweiNotificationContactImage = null;
    private Drawable sMultyContactImage = null;
    private Drawable sNotificationContactImage = null;

    private ResEx(Context context) {
        this.mContext = context;
        if (RcsCommonConfig.isRCSSwitchOn()) {
            this.mRcsResEx = new RcsResEx();
        }
        if (this.mRcsResEx != null) {
            this.mRcsResEx.setStateListDrawableCache(this.mStateListDrawableCache);
        }
        this.mContactDrawableCache = new MemCache(this.cacheSize);
    }

    public static ResEx init(Context context) {
        ResEx resEx;
        synchronized (ResEx.class) {
            if (selfInstance == null) {
                selfInstance = new ResEx(context);
            }
            resEx = selfInstance;
        }
        return resEx;
    }

    public static final ResEx self() {
        ResEx resEx;
        synchronized (ResEx.class) {
            resEx = selfInstance;
        }
        return resEx;
    }

    public String getOperTextDelete(int size) {
        return this.mContext.getResources().getString(R.string.delete_message_number, new Object[]{Integer.valueOf(size)});
    }

    public String getOperTextMultiForward(int size) {
        return this.mContext.getResources().getString(R.string.forward_message_number, new Object[]{Integer.valueOf(size)});
    }

    public static Bitmap getRoundedCornerBitmap(Bitmap bitmap, float roundPx) {
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Config.ARGB_8888);
        Canvas canvas = new Canvas(output);
        Paint paint = new Paint();
        Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        RectF rectF = new RectF(rect);
        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(-12434878);
        canvas.drawRoundRect(rectF, roundPx, roundPx, paint);
        paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);
        return output;
    }

    public static void makeToast(int strId) {
        makeToast(strId, 0);
    }

    public static void makeToast(CharSequence toast, int duration) {
        Application app = MmsApp.getApplication();
        if (emuiThemdId == 0) {
            emuiThemdId = app.getResources().getIdentifier("androidhwext:style/Theme.Emui", null, null);
        }
        if (emuiThemdId != 0) {
            Toast.makeText(new ContextThemeWrapper(app, emuiThemdId), toast, 1).show();
        } else {
            Toast.makeText(app, toast, duration).show();
        }
    }

    public static void makeToast(int resId, int duration) {
        Application app = MmsApp.getApplication();
        if (emuiThemdId == 0) {
            emuiThemdId = app.getResources().getIdentifier("androidhwext:style/Theme.Emui", null, null);
        }
        if (emuiThemdId != 0) {
            Toast.makeText(new ContextThemeWrapper(app, emuiThemdId), resId, 1).show();
        } else {
            Toast.makeText(app, resId, duration).show();
        }
    }

    public Drawable getAvtarDefault(Contact contact) {
        return getAvtarDefault(contact, 0);
    }

    public Drawable getAvtarDefault(Contact contact, int contactImgFlag) {
        int i;
        int bkColor = AvatarCache.pickColor(contact, contactImgFlag);
        LruSoftCache lruSoftCache = this.mAvatarCache;
        Context context = this.mContext;
        if (contactImgFlag == 0) {
            i = bkColor;
        } else {
            i = contactImgFlag;
        }
        Drawable avatar = (Drawable) lruSoftCache.get(context, Integer.valueOf(i));
        if (avatar != null) {
            return avatar;
        }
        Drawable drawable;
        switch (contactImgFlag) {
            case -4:
                drawable = getHuaweiNotificationContactImage();
                break;
            case -3:
                drawable = getFavoritesContactImage();
                break;
            case -2:
                drawable = getNotificationContactImage();
                break;
            default:
                drawable = getBlackContactImage();
                break;
        }
        avatar = AvatarCache.createRoundPhotoDrawable(this.mContext, drawable, bkColor);
        LruSoftCache lruSoftCache2 = this.mAvatarCache;
        if (contactImgFlag != 0) {
            bkColor = contactImgFlag;
        }
        lruSoftCache2.put(Integer.valueOf(bkColor), avatar);
        return avatar;
    }

    private Drawable getBlackContactImage() {
        if (this.sBlackContactImage == null) {
            this.sBlackContactImage = this.mContext.getResources().getDrawable(R.drawable.ic_contact_default);
        }
        return this.sBlackContactImage;
    }

    private Drawable getHuaweiNotificationContactImage() {
        if (this.sHuaweiNotificationContactImage == null) {
            this.sHuaweiNotificationContactImage = this.mContext.getResources().getDrawable(R.drawable.ic_huawei_avatar_default);
        }
        return this.sHuaweiNotificationContactImage;
    }

    private Drawable getNotificationContactImage() {
        if (this.sNotificationContactImage == null) {
            this.sNotificationContactImage = this.mContext.getResources().getDrawable(R.drawable.ic_notice_avatar_default);
        }
        return this.sNotificationContactImage;
    }

    private Drawable getFavoritesContactImage() {
        if (this.sFavoritesContactImage == null) {
            this.sFavoritesContactImage = this.mContext.getResources().getDrawable(R.drawable.ic_favorite_default);
        }
        return this.sFavoritesContactImage;
    }

    public Bitmap getEmptyImage() {
        if (this.sEmptyImagePic == null) {
            this.sEmptyImagePic = BitmapFactory.decodeResource(this.mContext.getResources(), R.drawable.csp_bottom_emui);
        }
        return this.sEmptyImagePic;
    }

    public Bitmap getEmptyVedio() {
        if (this.sEmptyVedioPic == null) {
            this.sEmptyVedioPic = BitmapFactory.decodeResource(this.mContext.getResources(), R.drawable.csp_default_avatar);
        }
        return this.sEmptyVedioPic;
    }

    public Drawable getCachedDrawable(int resId) {
        return getCachedDrawable(this.mContext, resId);
    }

    public Drawable getCachedDrawable(Context context, int resId) {
        Drawable dr = (Drawable) this.mDrawableCache.get(this.mContext, Integer.valueOf(resId));
        if (dr != null) {
            return dr;
        }
        try {
            dr = context.getResources().getDrawable(resId);
            this.mDrawableCache.put(Integer.valueOf(resId), dr);
            return dr;
        } catch (NotFoundException e) {
            MLog.e("Mms_app", "can't find drawable : ", (Throwable) e);
            return dr;
        }
    }

    public int getCachedColor(int resId) {
        if (resId <= 0) {
            return 0;
        }
        Integer color = (Integer) this.mColorCache.get(this.mContext, Integer.valueOf(resId));
        if (color == null) {
            color = Integer.valueOf(this.mContext.getResources().getColor(resId));
            this.mColorCache.put(Integer.valueOf(resId), color);
        }
        return color.intValue();
    }

    public void clearCachedRes() {
        this.mColorCache.evictAll();
        this.mDrawableCache.evictAll();
        this.mStateListDrawableCache.evictAll();
    }

    public void initResColor() {
        this.mConvItemTextColor = getCachedColor(R.drawable.text_color_black_sub);
        this.mConvItemNormalTextColor = getCachedColor(R.color.text_color_pre);
        this.mConvItemCountTextColor = getCachedColor(R.color.text_color_black_sub_1);
        this.mConvItemUnreadTextColor = getCachedColor(R.color.mms_unread_text_color);
        this.mConvItemErrorMsgTextColor = getCachedColor(R.color.conversation_list_item_error_msg_color);
        this.mSearchTextHightColor = getCachedColor(R.color.incoming_msg_text_color);
        this.mBackgrounChanged = getCachedColor(R.color.mms_bg_change);
        this.mConvItemNormalColor = getCachedColor(R.color.conversation_text_normal_color);
        this.mConvItemNewMsgColor = getCachedColor(R.color.conversation_text_new_message_color);
        this.mMsgItemUnderPopColor = getCachedColor(R.color.text_color_black_sub_1);
        this.mMessageItemTextColorSend = getCachedColor(R.color.send_message_item_pop_text_color);
        this.mMessageItemTextColorRecv = getCachedColor(R.color.black);
    }

    public int getConvItemNormalTextColor() {
        if (this.mConvItemNormalTextColor == -1) {
            return MmsApp.getApplication().getApplicationContext().getResources().getColor(R.color.text_color_pre);
        }
        return this.mConvItemNormalTextColor;
    }

    public int getConvItemCountTextColor() {
        if (this.mConvItemCountTextColor == -1) {
            return MmsApp.getApplication().getApplicationContext().getResources().getColor(R.color.text_color_black_sub_1);
        }
        return this.mConvItemCountTextColor;
    }

    public int getConvItemUnreadTextColor() {
        if (this.mConvItemUnreadTextColor == -1) {
            return MmsApp.getApplication().getApplicationContext().getResources().getColor(R.color.mms_unread_text_color);
        }
        return this.mConvItemUnreadTextColor;
    }

    public int getConvItemErrorMsgTextColor() {
        if (this.mConvItemErrorMsgTextColor == -1) {
            return MmsApp.getApplication().getApplicationContext().getResources().getColor(R.color.conversation_list_item_error_msg_color);
        }
        return this.mConvItemErrorMsgTextColor;
    }

    public int getConvItemNormalColor() {
        if (this.mConvItemNormalColor == -1) {
            return MmsApp.getApplication().getApplicationContext().getResources().getColor(R.color.conversation_text_normal_color);
        }
        return this.mConvItemNormalColor;
    }

    public int getConvItemNewMsgColor() {
        if (this.mConvItemNewMsgColor == -1) {
            return MmsApp.getApplication().getApplicationContext().getResources().getColor(R.color.conversation_text_new_message_color);
        }
        return this.mConvItemNewMsgColor;
    }

    public int getMsgItemUnderPopColor() {
        if (this.mMsgItemUnderPopColor == -1) {
            return MmsApp.getApplication().getApplicationContext().getResources().getColor(R.color.text_color_black_sub_1);
        }
        return this.mMsgItemUnderPopColor;
    }

    public int getMsgItemTextColorSend() {
        if (this.mMessageItemTextColorSend == -1) {
            return MmsApp.getApplication().getApplicationContext().getResources().getColor(R.color.send_message_item_pop_text_color);
        }
        return this.mMessageItemTextColorSend;
    }

    public int getMsgItemTextColorRecv() {
        if (this.mMessageItemTextColorRecv == -1) {
            return MmsApp.getApplication().getApplicationContext().getResources().getColor(R.color.black);
        }
        return this.mMessageItemTextColorRecv;
    }

    public int getMsgItemTextColor(boolean sendMessage) {
        if (this.mMessageItemTextColorSend == -1 || this.mMessageItemTextColorRecv == -1) {
            int color;
            if (sendMessage) {
                color = MmsApp.getApplication().getApplicationContext().getResources().getColor(R.color.send_message_item_pop_text_color);
            } else {
                color = MmsApp.getApplication().getApplicationContext().getResources().getColor(R.color.black);
            }
            return color;
        }
        return sendMessage ? this.mMessageItemTextColorSend : this.mMessageItemTextColorRecv;
    }

    public int getSearchTextHightColor() {
        if (this.mSearchTextHightColor == -1) {
            return MmsApp.getApplication().getApplicationContext().getResources().getColor(R.color.incoming_msg_text_color);
        }
        return this.mSearchTextHightColor;
    }

    public Drawable getCardIcon(int slot) {
        if (slot < 0 || slot > 1) {
            return null;
        }
        return getCachedDrawable(sSubscriptionResIds[slot]);
    }

    public static final void setMarqueeText(TextView textView, CharSequence text) {
        textView.setText(text, BufferType.SPANNABLE);
    }

    public static final Drawable getSmsAppIcon(Context context) {
        Drawable ret = (Drawable) self().mDrawableCache.get(context, Integer.valueOf(R.drawable.ic_launcher_icon));
        if (ret != null) {
            return ret;
        }
        ret = MmsInfo.getSmsAppIcon(context);
        if (ret == null) {
            ret = context.getResources().getDrawable(R.drawable.ic_launcher_icon);
        }
        self().mDrawableCache.put(Integer.valueOf(R.drawable.ic_launcher_icon), ret);
        return ret;
    }

    public static final Bitmap getBitMapFromDrawable(Drawable drawable, int width, int height) {
        if ((drawable instanceof BitmapDrawable) && width == drawable.getIntrinsicWidth() && height == drawable.getIntrinsicHeight()) {
            return ((BitmapDrawable) drawable).getBitmap();
        }
        Bitmap bitmap = Bitmap.createBitmap(width, width, Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }

    public Drawable getStateListDrawable(Context context, int resId) {
        Drawable normal = (Drawable) this.mStateListDrawableCache.get(context, Integer.valueOf(resId));
        if (normal != null) {
            return normal;
        }
        try {
            normal = context.getResources().getDrawable(resId);
        } catch (NotFoundException e) {
            MLog.e("Mms_app", "get the resources from the id failed, the id is: " + resId);
        }
        if (normal == null) {
            MLog.e("Mms_app", "EmuiMenu getIcon fail " + resId);
            return null;
        } else if (!(normal instanceof BitmapDrawable)) {
            return normal;
        } else {
            Drawable listDrawable = CustomStateListDrawable.createStateDrawable(context, (BitmapDrawable) normal);
            this.mStateListDrawableCache.put(Integer.valueOf(resId), listDrawable);
            return listDrawable;
        }
    }

    public RcsResEx getHwCust() {
        return this.mRcsResEx;
    }

    public static Bitmap duplicateBitmap(Bitmap bitmap, int alpha) {
        Bitmap duplicated = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Config.ARGB_8888);
        Canvas canvas = new Canvas(duplicated);
        Paint paint = new Paint();
        paint.setAlpha(alpha);
        canvas.drawBitmap(bitmap, 0.0f, 0.0f, paint);
        return duplicated;
    }

    public void showMmsToast(Context context, int resId, int length) {
        if (this.mMmsToast != null) {
            this.mMmsToast.cancel();
        }
        this.mMmsToast = Toast.makeText(context, resId, length);
        this.mMmsToast.show();
        HwBackgroundLoader.getInst().postTaskDelayed(new Runnable() {
            public void run() {
                ResEx.this.mMmsToast = null;
            }
        }, (long) length);
    }

    public boolean isUseThemeBackground(Context context) {
        if (this.mBackgrounChanged == -1) {
            this.mBackgrounChanged = context.getResources().getColor(R.color.mms_bg_change);
        }
        if (this.mBackgrounChanged == 0) {
            return true;
        }
        return false;
    }

    public Drawable setBlurWallpaperBackground(Context context, View view) {
        int[] loc = new int[2];
        view.getLocationOnScreen(loc);
        Rect rect = new Rect(loc[0], loc[1], loc[0] + view.getWidth(), loc[1] + view.getHeight());
        if (rect.width() <= 0 || rect.height() <= 0) {
            return null;
        }
        Bitmap src = WallPaperImageHelper.getInstance(context).getBitmap(rect, 0.0f, 0.0f, 0.0f, 0.0f);
        if (src == null) {
            return null;
        }
        Drawable drawable = new BitmapDrawable(context.getResources(), src);
        view.setBackground(drawable);
        return drawable;
    }

    public void addDrawableCache(Drawable drawable, int key) {
        if (this.mStateListDrawableCache != null && drawable != null) {
            this.mStateListDrawableCache.put(Integer.valueOf(key), drawable);
        }
    }

    public Drawable getContactDrawable(long key) {
        return (Drawable) this.mContactDrawableCache.get(Long.valueOf(key));
    }

    public void putContactDrawable(long key, Drawable drawable) {
        this.mContactDrawableCache.put(Long.valueOf(key), drawable);
    }

    public void clearContactDrawableCache() {
        if (this.mContactDrawableCache != null) {
            this.mContactDrawableCache.evictAll();
        }
    }

    public void createNotificationCache() {
        this.mNotificationCache = new MemCache(this.cacheSize);
    }

    public void createHwNotificationCache() {
        this.mHwNotificationCache = new MemCache(this.cacheSize);
    }

    public Drawable getHwNotificationDrawable(long key) {
        if (this.mHwNotificationCache == null) {
            createHwNotificationCache();
        }
        return (Drawable) this.mHwNotificationCache.get(Long.valueOf(key));
    }

    public void clearHwNotificationDrawableCache() {
        if (this.mHwNotificationCache != null) {
            this.mHwNotificationCache.evictAll();
        }
    }

    public void clearNotificationDrawableCache() {
        if (this.mNotificationCache != null) {
            this.mNotificationCache.evictAll();
        }
    }
}
