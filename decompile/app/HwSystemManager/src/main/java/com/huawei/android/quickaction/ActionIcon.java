package com.huawei.android.quickaction;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.text.TextUtils;
import android.util.Log;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

public final class ActionIcon implements Parcelable {
    public static final Creator<ActionIcon> CREATOR = new Creator<ActionIcon>() {
        public ActionIcon createFromParcel(Parcel parcel) {
            return new ActionIcon(parcel);
        }

        public ActionIcon[] newArray(int i) {
            return new ActionIcon[i];
        }
    };
    private static final String TAG = "ActionIcon";
    private static final int TYPE_BITMAP = 1;
    private static final int TYPE_RESOURCE = 2;
    private static final int TYPE_URI = 3;
    private int mInt1;
    private Object mObj1;
    private String mString1;
    private final int mType;

    private class LoadDrawableTask implements Runnable {
        final Context mContext;
        final Message mMessage;

        public LoadDrawableTask(Context context, Handler handler, final OnDrawableLoadedListener onDrawableLoadedListener) {
            this.mContext = context;
            this.mMessage = Message.obtain(handler, new Runnable() {
                public void run() {
                    onDrawableLoadedListener.onDrawableLoaded((Drawable) LoadDrawableTask.this.mMessage.obj);
                }
            });
        }

        public LoadDrawableTask(Context context, Message message) {
            this.mContext = context;
            this.mMessage = message;
        }

        public void run() {
            this.mMessage.obj = ActionIcon.this.loadDrawable(this.mContext);
            this.mMessage.sendToTarget();
        }

        public void runAsync() {
            AsyncTask.THREAD_POOL_EXECUTOR.execute(this);
        }
    }

    interface OnDrawableLoadedListener {
        void onDrawableLoaded(Drawable drawable);
    }

    private Bitmap getBitmap() {
        if (this.mType == 1) {
            return (Bitmap) this.mObj1;
        }
        throw new IllegalStateException("called getBitmap() on " + this);
    }

    private void setBitmap(Bitmap bitmap) {
        this.mObj1 = bitmap;
    }

    private Resources getResources() {
        if (this.mType == 2) {
            return (Resources) this.mObj1;
        }
        throw new IllegalStateException("called getResources() on " + this);
    }

    private String getResPackage() {
        if (this.mType == 2) {
            return this.mString1;
        }
        throw new IllegalStateException("called getResPackage() on " + this);
    }

    private int getResId() {
        if (this.mType == 2) {
            return this.mInt1;
        }
        throw new IllegalStateException("called getResId() on " + this);
    }

    private String getUriString() {
        if (this.mType == 3) {
            return this.mString1;
        }
        throw new IllegalStateException("called getUriString() on " + this);
    }

    private Uri getUri() {
        return Uri.parse(getUriString());
    }

    private static final String typeToString(int i) {
        switch (i) {
            case 1:
                return "BITMAP";
            case 2:
                return "RESOURCE";
            case 3:
                return "URI";
            default:
                return "UNKNOWN";
        }
    }

    void loadDrawableAsync(Context context, Message message) {
        if (message.getTarget() != null) {
            new LoadDrawableTask(context, message).runAsync();
            return;
        }
        throw new IllegalArgumentException("callback message must have a target handler");
    }

    void loadDrawableAsync(Context context, OnDrawableLoadedListener onDrawableLoadedListener, Handler handler) {
        new LoadDrawableTask(context, handler, onDrawableLoadedListener).runAsync();
    }

    Drawable loadDrawable(Context context) {
        Drawable loadDrawableInner = loadDrawableInner(context);
        if (loadDrawableInner != null) {
            loadDrawableInner.mutate();
        }
        return loadDrawableInner;
    }

    private Drawable loadDrawableInner(Context context) {
        String resPackage;
        switch (this.mType) {
            case 1:
                return new BitmapDrawable(context.getResources(), getBitmap());
            case 2:
                if (getResources() == null) {
                    resPackage = getResPackage();
                    if (TextUtils.isEmpty(resPackage)) {
                        resPackage = context.getPackageName();
                    }
                    if ("android".equals(resPackage)) {
                        this.mObj1 = Resources.getSystem();
                    } else {
                        PackageManager packageManager = context.getPackageManager();
                        try {
                            ApplicationInfo applicationInfo = packageManager.getApplicationInfo(resPackage, 8192);
                            if (applicationInfo != null) {
                                this.mObj1 = packageManager.getResourcesForApplication(applicationInfo);
                            }
                        } catch (Throwable e) {
                            Log.e(TAG, String.format("Unable to find pkg=%s for icon %s", new Object[]{resPackage, this}), e);
                            break;
                        } catch (Throwable e2) {
                            Log.e(TAG, "TYPE_RESOURCE NameNotFoundException", e2);
                        }
                    }
                }
                try {
                    return getResources().getDrawable(getResId(), context.getTheme());
                } catch (Throwable e22) {
                    Log.e(TAG, String.format("Unable to load resource 0x%08x from pkg=%s", new Object[]{Integer.valueOf(getResId()), getResPackage()}), e22);
                    break;
                } catch (Throwable e222) {
                    Log.e(TAG, "TYPE_RESOURCE RuntimeException", e222);
                    break;
                }
                break;
            case 3:
                InputStream openInputStream;
                Uri uri = getUri();
                resPackage = uri.getScheme();
                if ("content".equals(resPackage) || "file".equals(resPackage)) {
                    try {
                        openInputStream = context.getContentResolver().openInputStream(uri);
                    } catch (Throwable e2222) {
                        Log.w(TAG, "Unable to load image from URI: " + uri, e2222);
                        openInputStream = null;
                    }
                } else {
                    try {
                        openInputStream = new FileInputStream(new File(this.mString1));
                    } catch (Throwable e22222) {
                        Log.w(TAG, "Unable to load image from path: " + uri, e22222);
                        openInputStream = null;
                    } catch (Throwable e222222) {
                        Log.w(TAG, "TYPE_URI FileNotFoundException", e222222);
                        openInputStream = null;
                    }
                }
                if (openInputStream != null) {
                    return new BitmapDrawable(context.getResources(), BitmapFactory.decodeStream(openInputStream));
                }
                break;
        }
        return null;
    }

    private ActionIcon(int i) {
        this.mType = i;
    }

    public static ActionIcon createWithResource(Context context, int i) {
        if (context != null) {
            return createWithResource(context.getPackageName(), i);
        }
        throw new IllegalArgumentException("Context must not be null.");
    }

    public static ActionIcon createWithResource(String str, int i) {
        if (str != null) {
            ActionIcon actionIcon = new ActionIcon(2);
            actionIcon.mInt1 = i;
            actionIcon.mString1 = str;
            return actionIcon;
        }
        throw new IllegalArgumentException("Resource package name must not be null.");
    }

    public static ActionIcon createWithBitmap(Bitmap bitmap) {
        if (bitmap != null) {
            ActionIcon actionIcon = new ActionIcon(1);
            actionIcon.setBitmap(bitmap);
            return actionIcon;
        }
        throw new IllegalArgumentException("Bitmap must not be null.");
    }

    public static ActionIcon createWithContentUri(String str) {
        if (str != null) {
            ActionIcon actionIcon = new ActionIcon(3);
            actionIcon.mString1 = str;
            return actionIcon;
        }
        throw new IllegalArgumentException("Uri must not be null.");
    }

    public static ActionIcon createWithContentUri(Uri uri) {
        if (uri != null) {
            return createWithContentUri(uri.toString());
        }
        throw new IllegalArgumentException("Uri must not be null.");
    }

    public static ActionIcon createWithFilePath(String str) {
        return createWithContentUri(str);
    }

    public String toString() {
        StringBuilder append = new StringBuilder("Icon(typ=").append(typeToString(this.mType));
        switch (this.mType) {
            case 1:
                append.append(" size=").append(getBitmap().getWidth()).append("x").append(getBitmap().getHeight());
                break;
            case 2:
                append.append(" pkg=").append(getResPackage()).append(" id=").append(String.format("0x%08x", new Object[]{Integer.valueOf(getResId())}));
                break;
            case 3:
                append.append(" uri=").append(getUriString());
                break;
        }
        append.append(")");
        return append.toString();
    }

    public int describeContents() {
        return this.mType != 1 ? 0 : 1;
    }

    ActionIcon(Parcel parcel) {
        this(parcel.readInt());
        switch (this.mType) {
            case 1:
                this.mObj1 = (Bitmap) Bitmap.CREATOR.createFromParcel(parcel);
                return;
            case 2:
                String readString = parcel.readString();
                int readInt = parcel.readInt();
                this.mString1 = readString;
                this.mInt1 = readInt;
                return;
            case 3:
                this.mString1 = parcel.readString();
                return;
            default:
                throw new RuntimeException("invalid " + getClass().getSimpleName() + " type in parcel: " + this.mType);
        }
    }

    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(this.mType);
        switch (this.mType) {
            case 1:
                getBitmap().writeToParcel(parcel, i);
                return;
            case 2:
                parcel.writeString(getResPackage());
                parcel.writeInt(getResId());
                return;
            case 3:
                parcel.writeString(getUriString());
                return;
            default:
                return;
        }
    }
}
