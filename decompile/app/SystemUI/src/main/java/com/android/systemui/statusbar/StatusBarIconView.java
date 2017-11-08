package com.android.systemui.statusbar;

import android.app.Notification;
import android.app.Notification.Builder;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Icon;
import android.os.Parcelable;
import android.os.UserHandle;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.ViewDebug.ExportedProperty;
import android.view.accessibility.AccessibilityEvent;
import android.widget.ImageView.ScaleType;
import com.android.internal.statusbar.StatusBarIcon;
import com.android.systemui.R;
import com.android.systemui.utils.HwLog;
import com.android.systemui.utils.badgedicon.BadgedIconHelper;
import fyusion.vislib.BuildConfig;
import java.text.NumberFormat;

public class StatusBarIconView extends AnimatedImageView {
    private boolean mAlwaysScaleIcon;
    private final boolean mBlocked;
    private int mDensity;
    private StatusBarIcon mIcon;
    private int mInitialPid;
    private boolean mIsCloneIcon;
    protected Notification mNotification;
    private Drawable mNumberBackground;
    private Paint mNumberPain;
    private String mNumberText;
    private int mNumberX;
    private int mNumberY;
    private String mPackageName;
    @ExportedProperty
    private String mSlot;
    private String mTag;

    public StatusBarIconView(Context context, String slot, Notification notification) {
        this(context, slot, notification, false);
    }

    public StatusBarIconView(Context context, String slot, Notification notification, boolean blocked) {
        this(context, slot, notification, blocked, -1, null, null);
    }

    public StatusBarIconView(Context context, String slot, Notification notification, boolean blocked, int initialPid, String tag, String packageName) {
        super(context);
        this.mInitialPid = -1;
        this.mBlocked = blocked;
        this.mSlot = slot;
        this.mNumberPain = new Paint();
        this.mNumberPain.setTextAlign(Align.CENTER);
        this.mNumberPain.setColor(context.getColor(R.drawable.notification_number_text_color));
        this.mNumberPain.setAntiAlias(true);
        this.mInitialPid = initialPid;
        this.mTag = tag;
        this.mPackageName = packageName;
        setNotification(notification);
        maybeUpdateIconScale();
        setScaleType(ScaleType.CENTER);
        this.mDensity = context.getResources().getDisplayMetrics().densityDpi;
        setCloneIcon();
        updateTint();
    }

    public void setInitialPid(int initialPid) {
        this.mInitialPid = initialPid;
    }

    public void setPackageName(String packageName) {
        this.mPackageName = packageName;
    }

    protected void maybeUpdateIconScale() {
        if (this.mNotification != null || this.mAlwaysScaleIcon) {
            updateIconScale();
        }
    }

    protected void updateIconScale() {
        Resources res = this.mContext.getResources();
        float scale = ((float) res.getDimensionPixelSize(R.dimen.notification_icon_drawing_size)) / ((float) res.getDimensionPixelSize(R.dimen.notification_icon_size));
        setScaleX(scale);
        setScaleY(scale);
    }

    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        int density = newConfig.densityDpi;
        if (density != this.mDensity) {
            this.mDensity = density;
            maybeUpdateIconScale();
            updateDrawable();
        }
    }

    public void setNotification(Notification notification) {
        this.mNotification = notification;
        setContentDescription(notification);
        updateTint();
    }

    public StatusBarIconView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mInitialPid = -1;
        this.mBlocked = false;
        this.mAlwaysScaleIcon = true;
        updateIconScale();
        this.mDensity = context.getResources().getDisplayMetrics().densityDpi;
        setIsResever(true);
    }

    public boolean equalIcons(Icon a, Icon b) {
        boolean z = true;
        if (a == b) {
            return true;
        }
        if (a.getType() != b.getType()) {
            return false;
        }
        switch (a.getType()) {
            case 2:
                if (!(a.getResPackage().equals(b.getResPackage()) && a.getResId() == b.getResId())) {
                    z = false;
                }
                return z;
            case 4:
                return a.getUriString().equals(b.getUriString());
            default:
                return false;
        }
    }

    public boolean set(StatusBarIcon icon) {
        boolean iconEquals;
        int i = 0;
        if (this.mIcon != null) {
            iconEquals = equalIcons(this.mIcon.icon, icon.icon);
        } else {
            iconEquals = false;
        }
        boolean levelEquals = iconEquals ? this.mIcon.iconLevel == icon.iconLevel : false;
        boolean visibilityEquals = this.mIcon != null ? this.mIcon.visible == icon.visible : false;
        boolean numberEquals = this.mIcon != null ? this.mIcon.number == icon.number : false;
        this.mIcon = icon.clone();
        setContentDescription(icon.contentDescription);
        if (!iconEquals && !updateDrawable(false)) {
            return false;
        }
        if (!levelEquals) {
            setImageLevel(icon.iconLevel);
        }
        if (!numberEquals) {
            if (icon.number <= 0 || !getContext().getResources().getBoolean(R.bool.config_statusBarShowNumber)) {
                this.mNumberBackground = null;
                this.mNumberText = null;
            } else {
                if (this.mNumberBackground == null) {
                    this.mNumberBackground = getContext().getResources().getDrawable(R.drawable.ic_notification_overlay);
                }
                placeNumber();
            }
            invalidate();
        }
        if (!visibilityEquals) {
            if (!icon.visible || this.mBlocked) {
                i = 8;
            }
            setVisibility(i);
        }
        if (!(this.mIcon == null || this.mIcon.icon == null || this.mIcon.icon.hasTint())) {
            setTint();
        }
        return true;
    }

    public void updateDrawable() {
        updateDrawable(true);
    }

    private boolean updateDrawable(boolean withClear) {
        if (this.mIcon == null) {
            return false;
        }
        Drawable drawable = getIcon(this.mIcon);
        if (drawable == null) {
            Log.w("StatusBarIconView", "No icon for slot " + this.mSlot);
            return false;
        }
        if (withClear) {
            setImageDrawable(null);
        }
        setImageDrawable(drawable);
        return true;
    }

    private Drawable getIcon(StatusBarIcon icon) {
        Drawable iconDrawable = getIcon(getContext(), icon);
        if (BadgedIconHelper.isCloneProcess(this.mInitialPid, this.mTag)) {
            return getContext().getPackageManager().getUserBadgedIcon(iconDrawable, new UserHandle(2147383647));
        }
        return iconDrawable;
    }

    public static Drawable getIcon(Context context, StatusBarIcon statusBarIcon) {
        int userId = statusBarIcon.user.getIdentifier();
        if (userId == -1) {
            userId = 0;
        }
        Drawable icon = statusBarIcon.icon.loadDrawableAsUser(context, userId);
        TypedValue typedValue = new TypedValue();
        context.getResources().getValue(R.dimen.status_bar_icon_scale_factor, typedValue, true);
        float scaleFactor = typedValue.getFloat();
        if (scaleFactor == 1.0f) {
            return icon;
        }
        return new ScalingDrawableWrapper(icon, scaleFactor);
    }

    public StatusBarIcon getStatusBarIcon() {
        return this.mIcon;
    }

    public void onInitializeAccessibilityEvent(AccessibilityEvent event) {
        super.onInitializeAccessibilityEvent(event);
        if (this.mNotification != null) {
            event.setParcelableData(this.mNotification);
        }
    }

    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (this.mNumberBackground != null) {
            placeNumber();
        }
    }

    public void onRtlPropertiesChanged(int layoutDirection) {
        super.onRtlPropertiesChanged(layoutDirection);
        updateDrawable();
    }

    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (this.mNumberBackground != null) {
            this.mNumberBackground.draw(canvas);
            canvas.drawText(this.mNumberText, (float) this.mNumberX, (float) this.mNumberY, this.mNumberPain);
        }
    }

    protected void debug(int depth) {
        super.debug(depth);
        Log.d("View", debugIndent(depth) + "slot=" + this.mSlot);
        Log.d("View", debugIndent(depth) + "icon=" + this.mIcon);
    }

    void placeNumber() {
        String str;
        if (this.mIcon.number > getContext().getResources().getInteger(17694723)) {
            str = getContext().getResources().getString(17039383);
        } else {
            str = NumberFormat.getIntegerInstance().format((long) this.mIcon.number);
        }
        this.mNumberText = str;
        int w = getWidth();
        int h = getHeight();
        Rect r = new Rect();
        this.mNumberPain.getTextBounds(str, 0, str.length(), r);
        int tw = r.right - r.left;
        int th = r.bottom - r.top;
        this.mNumberBackground.getPadding(r);
        int dw = (r.left + tw) + r.right;
        if (dw < this.mNumberBackground.getMinimumWidth()) {
            dw = this.mNumberBackground.getMinimumWidth();
        }
        this.mNumberX = (w - r.right) - (((dw - r.right) - r.left) / 2);
        int dh = (r.top + th) + r.bottom;
        if (dh < this.mNumberBackground.getMinimumWidth()) {
            dh = this.mNumberBackground.getMinimumWidth();
        }
        this.mNumberY = (h - r.bottom) - ((((dh - r.top) - th) - r.bottom) / 2);
        this.mNumberBackground.setBounds(w - dw, h - dh, w, h);
    }

    private void setContentDescription(Notification notification) {
        if (notification != null) {
            String d = contentDescForNotification(this.mContext, notification);
            if (!TextUtils.isEmpty(d)) {
                setContentDescription(d);
            }
        }
    }

    public String toString() {
        return "StatusBarIconView(slot=" + this.mSlot + " icon=" + this.mIcon + " notification=" + this.mNotification + ")";
    }

    public String getSlot() {
        return this.mSlot;
    }

    public static String contentDescForNotification(Context c, Notification n) {
        String appName = BuildConfig.FLAVOR;
        try {
            appName = Builder.recoverBuilder(c, n).loadHeaderAppName();
        } catch (RuntimeException e) {
            Log.e("StatusBarIconView", "Unable to recover builder", e);
            Parcelable appInfo = n.extras.getParcelable("android.appInfo");
            if (appInfo != null && (appInfo instanceof ApplicationInfo)) {
                appName = String.valueOf(((ApplicationInfo) appInfo).loadLabel(c.getPackageManager()));
            }
        }
        CharSequence title = n.extras.getCharSequence("android.title");
        CharSequence ticker = n.tickerText;
        CharSequence desc = !TextUtils.isEmpty(ticker) ? ticker : !TextUtils.isEmpty(title) ? title : BuildConfig.FLAVOR;
        return c.getString(R.string.accessibility_desc_notification_icon, new Object[]{appName, desc});
    }

    public void updateTint() {
        int tint = 1;
        if (this.mNotification != null) {
            tint = this.mNotification.extras.getInt("hw_small_icon_tint", 0);
            HwLog.i("StatusBarIconView", "updateTint: tint=" + tint);
        }
        if (tint == 0 || "bluetooth".equals(this.mSlot)) {
            setIsResever(false);
            setAlpha(0.7f);
        } else {
            setIsResever(true);
            setAlpha(1.0f);
        }
        setTint();
    }

    public void setCloneIcon() {
        this.mIsCloneIcon = BadgedIconHelper.isCloneProcess(this.mInitialPid, this.mTag);
    }

    public boolean isSameAs(StatusBarIconView other) {
        if (other == null || other.mIcon == null || this.mIcon == null || this.mIsCloneIcon != other.mIsCloneIcon || !equalIcons(this.mIcon.icon, other.mIcon.icon)) {
            return false;
        }
        return true;
    }
}
