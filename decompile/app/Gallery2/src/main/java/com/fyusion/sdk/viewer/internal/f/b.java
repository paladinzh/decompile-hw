package com.fyusion.sdk.viewer.internal.f;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Color;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.util.Base64;
import com.fyusion.sdk.common.DLog;
import com.fyusion.sdk.common.FyuseSDK;
import com.huawei.watermark.manager.parse.WMElement;

/* compiled from: Unknown */
public class b {
    public static Bitmap a(String str) {
        Allocation createFromBitmap;
        Allocation createTyped;
        Throwable e;
        ScriptIntrinsicBlur scriptIntrinsicBlur;
        ScriptIntrinsicBlur scriptIntrinsicBlur2 = null;
        try {
            byte[] decode = Base64.decode(str, 0);
            int length = (decode.length - 2) / 3;
            int[] iArr = new int[length];
            for (int i = 0; i < length; i++) {
                int i2 = i * 3;
                int i3 = decode[i2 + 3] & 255;
                iArr[i] = Color.rgb(decode[i2 + 4] & 255, i3, decode[i2 + 2] & 255);
            }
            Bitmap createBitmap = Bitmap.createBitmap(iArr, decode[0], decode[1], Config.ARGB_4444);
            RenderScript renderScript = FyuseSDK.getInstance().getRenderScript();
            createFromBitmap = Allocation.createFromBitmap(renderScript, createBitmap);
            try {
                createTyped = Allocation.createTyped(renderScript, createFromBitmap.getType());
            } catch (Exception e2) {
                e = e2;
                scriptIntrinsicBlur = null;
                createTyped = null;
                try {
                    DLog.w("ImageHelper", "Failed make blur thumbnail", e);
                    if (createFromBitmap != null) {
                        createFromBitmap.destroy();
                    }
                    if (createTyped != null) {
                        createTyped.destroy();
                    }
                    if (scriptIntrinsicBlur != null) {
                        scriptIntrinsicBlur.destroy();
                    }
                    return null;
                } catch (Throwable th) {
                    e = th;
                    scriptIntrinsicBlur2 = scriptIntrinsicBlur;
                    if (createFromBitmap != null) {
                        createFromBitmap.destroy();
                    }
                    if (createTyped != null) {
                        createTyped.destroy();
                    }
                    if (scriptIntrinsicBlur2 != null) {
                        scriptIntrinsicBlur2.destroy();
                    }
                    throw e;
                }
            } catch (Throwable th2) {
                e = th2;
                createTyped = null;
                if (createFromBitmap != null) {
                    createFromBitmap.destroy();
                }
                if (createTyped != null) {
                    createTyped.destroy();
                }
                if (scriptIntrinsicBlur2 != null) {
                    scriptIntrinsicBlur2.destroy();
                }
                throw e;
            }
            try {
                scriptIntrinsicBlur = ScriptIntrinsicBlur.create(renderScript, Element.U8_4(renderScript));
                try {
                    scriptIntrinsicBlur.setRadius(WMElement.CAMERASIZEVALUE1B1);
                    scriptIntrinsicBlur.setInput(createFromBitmap);
                    scriptIntrinsicBlur.forEach(createTyped);
                    createTyped.copyTo(createBitmap);
                    if (createFromBitmap != null) {
                        createFromBitmap.destroy();
                    }
                    if (createTyped != null) {
                        createTyped.destroy();
                    }
                    if (scriptIntrinsicBlur != null) {
                        scriptIntrinsicBlur.destroy();
                    }
                    return createBitmap;
                } catch (Exception e3) {
                    e = e3;
                    DLog.w("ImageHelper", "Failed make blur thumbnail", e);
                    if (createFromBitmap != null) {
                        createFromBitmap.destroy();
                    }
                    if (createTyped != null) {
                        createTyped.destroy();
                    }
                    if (scriptIntrinsicBlur != null) {
                        scriptIntrinsicBlur.destroy();
                    }
                    return null;
                }
            } catch (Exception e4) {
                e = e4;
                scriptIntrinsicBlur = null;
                DLog.w("ImageHelper", "Failed make blur thumbnail", e);
                if (createFromBitmap != null) {
                    createFromBitmap.destroy();
                }
                if (createTyped != null) {
                    createTyped.destroy();
                }
                if (scriptIntrinsicBlur != null) {
                    scriptIntrinsicBlur.destroy();
                }
                return null;
            } catch (Throwable th3) {
                e = th3;
                if (createFromBitmap != null) {
                    createFromBitmap.destroy();
                }
                if (createTyped != null) {
                    createTyped.destroy();
                }
                if (scriptIntrinsicBlur2 != null) {
                    scriptIntrinsicBlur2.destroy();
                }
                throw e;
            }
        } catch (Exception e5) {
            e = e5;
            scriptIntrinsicBlur = null;
            createTyped = null;
            createFromBitmap = null;
        } catch (Throwable th4) {
            e = th4;
            createTyped = null;
            createFromBitmap = null;
        }
    }
}
