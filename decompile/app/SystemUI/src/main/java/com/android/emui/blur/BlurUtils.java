package com.android.emui.blur;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.renderscript.Allocation;
import android.renderscript.Allocation.MipmapControl;
import android.renderscript.Element;
import android.renderscript.RSInvalidStateException;
import android.renderscript.RenderScript;
import android.renderscript.RenderScript.RSMessageHandler;
import android.renderscript.ScriptIntrinsicBlur;
import android.util.Log;
import com.android.systemui.utils.HwLog;

public class BlurUtils {
    private RenderScript mRs;
    private RSMessageHandler mRsMsgHandler = new RSMessageHandler();
    private ScriptIntrinsicBlur mScriptIntrinsic;

    public Bitmap blurImage(Context ctx, Bitmap input, Bitmap output, int radius) {
        if (ctx == null || input == null || output == null || radius <= 0 || radius > 25) {
            Log.w("BlurUtils", "blurImage() parameter is incorrect:" + ctx + "," + input + "," + output + "," + radius);
            return null;
        }
        Context c = ctx.getApplicationContext();
        if (c == null) {
            return null;
        }
        ctx = c;
        if (this.mRs == null) {
            HwLog.e("BlurUtils", "mRs == null and need to create!!");
            this.mRs = RenderScript.create(c);
            if (this.mRs != null) {
                this.mRs.setMessageHandler(this.mRsMsgHandler);
            } else {
                HwLog.e("BlurUtils", "mRs == null still!!");
                return null;
            }
        }
        Allocation tmpIn = Allocation.createFromBitmap(this.mRs, input, MipmapControl.MIPMAP_NONE, 1);
        Allocation tmpOut = Allocation.createTyped(this.mRs, tmpIn.getType());
        if (this.mScriptIntrinsic == null) {
            this.mScriptIntrinsic = ScriptIntrinsicBlur.create(this.mRs, Element.U8_4(this.mRs));
        }
        this.mScriptIntrinsic.setRadius((float) radius);
        this.mScriptIntrinsic.setInput(tmpIn);
        this.mScriptIntrinsic.forEach(tmpOut);
        tmpOut.copyTo(output);
        try {
            tmpIn.destroy();
        } catch (RSInvalidStateException e) {
            e.printStackTrace();
        }
        try {
            tmpOut.destroy();
        } catch (RSInvalidStateException e2) {
            e2.printStackTrace();
        }
        destory();
        return output;
    }

    public void destory() {
        if (this.mRs != null) {
            this.mRs.destroy();
            this.mRs = null;
        }
        try {
            if (this.mScriptIntrinsic != null) {
                this.mScriptIntrinsic.destroy();
            }
        } catch (RSInvalidStateException e) {
            e.printStackTrace();
        }
    }

    public static Bitmap resizeImage(Context mContext, Bitmap bitmap) {
        if (mContext == null || bitmap == null) {
            return null;
        }
        int screenW = mContext.getResources().getDisplayMetrics().widthPixels;
        int screenH = mContext.getResources().getDisplayMetrics().heightPixels;
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        Matrix matrix = new Matrix();
        float scale = (float) Math.max(screenH / height, screenW / width);
        matrix.postScale(scale, scale);
        Bitmap resizedBitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
        HwLog.i("BlurUtils", "width:" + width + ",height:" + height + ",screenW:" + screenW + ",screenH:" + screenH + ",scale:" + scale);
        return resizedBitmap;
    }
}
