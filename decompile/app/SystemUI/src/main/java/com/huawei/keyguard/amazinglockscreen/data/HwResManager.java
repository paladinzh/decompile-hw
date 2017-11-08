package com.huawei.keyguard.amazinglockscreen.data;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.text.TextUtils;
import com.android.keyguard.R$drawable;
import com.huawei.keyguard.GlobalContext;
import com.huawei.keyguard.amazinglockscreen.HwPropertyManager;
import com.huawei.keyguard.util.HwLog;
import java.util.HashMap;

public class HwResManager {
    private static final int[] TIME_DIGIT_DRAWABLE_ID = new int[]{R$drawable.number_0, R$drawable.number_1, R$drawable.number_2, R$drawable.number_3, R$drawable.number_4, R$drawable.number_5, R$drawable.number_6, R$drawable.number_7, R$drawable.number_8, R$drawable.number_9};
    private static HashMap<String, Bitmap> sHwImageViewBitmap = new HashMap();
    private static HwResManager sInstance;
    private Context mContext;

    public static synchronized HwResManager getInstance() {
        HwResManager hwResManager;
        synchronized (HwResManager.class) {
            if (sInstance == null) {
                sInstance = new HwResManager();
            }
            hwResManager = sInstance;
        }
        return hwResManager;
    }

    public Bitmap getSourceBitmap(final String srcName, Context context, final Handler handler, final boolean isMask) {
        if (handler == null || context == null || TextUtils.isEmpty(srcName)) {
            return null;
        }
        if (sHwImageViewBitmap.containsKey(srcName)) {
            return (Bitmap) sHwImageViewBitmap.get(srcName);
        }
        GlobalContext.getSerialExecutor().execute(new Runnable() {
            public void run() {
                int i;
                HwResManager.this.decodeImageBitmapSrc(srcName);
                Handler handler = handler;
                if (isMask) {
                    i = 1;
                } else {
                    i = 0;
                }
                handler.obtainMessage(256, i, 0, srcName).sendToTarget();
            }
        });
        return null;
    }

    private void decodeImageBitmapSrc(String src) {
        Object bm = null;
        String language = HwPropertyManager.getInstance().getLanguage();
        if ("ar".equals(language) || "fa".equals(language)) {
            if (src != null) {
                try {
                    if (src.startsWith("number_") && this.mContext != null) {
                        bm = BitmapFactory.decodeResource(this.mContext.getResources(), getDigitResourceId(src.charAt("number_".length())));
                        if (bm == null) {
                            bm = decodeBitmapSrc(src);
                        }
                    }
                } catch (OutOfMemoryError e) {
                    HwLog.e("HwResManager", "setImageBitmapSrc OutOfMemoryError src = " + src);
                }
            }
            if (src != null) {
                if (src.startsWith("colon") && this.mContext != null) {
                    bm = BitmapFactory.decodeResource(this.mContext.getResources(), R$drawable.colon);
                }
            }
            if (bm == null) {
                bm = decodeBitmapSrc(src);
            }
        } else {
            bm = decodeBitmapSrc(src);
        }
        sHwImageViewBitmap.put(src, bm);
    }

    private Bitmap decodeBitmapSrc(String srcName) {
        Bitmap bm = null;
        if (srcName != null) {
            try {
                bm = BitmapFactory.decodeFile("/data/skin/unlock/drawable-hdpi/" + srcName);
            } catch (OutOfMemoryError e) {
                HwLog.e("HwResManager", "decodeBitmapSrc OutOfMemoryError srcName = " + srcName);
            }
        }
        return bm;
    }

    private int getDigitResourceId(char digit) {
        int index = digit - 48;
        if (index < 0 || TIME_DIGIT_DRAWABLE_ID.length <= index) {
            return 0;
        }
        return TIME_DIGIT_DRAWABLE_ID[index];
    }

    public void clearCache() {
        if (sHwImageViewBitmap.size() > 0) {
            for (Bitmap bitmap : sHwImageViewBitmap.values()) {
                if (!(bitmap == null || bitmap.isRecycled())) {
                    bitmap.recycle();
                }
            }
            sHwImageViewBitmap.clear();
        }
    }
}
