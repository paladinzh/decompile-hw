package com.huawei.gallery.ui.stackblur;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.renderscript.Allocation;
import android.renderscript.Allocation.MipmapControl;
import android.renderscript.Element;
import android.renderscript.RSInvalidStateException;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import com.android.gallery3d.common.Utils;
import com.android.gallery3d.util.GalleryLog;

class RSBlurProcess implements BlurProcess {
    private final RenderScript rs;
    private final ScriptIntrinsicBlur theIntrinsic = ScriptIntrinsicBlur.create(this.rs, Element.U8_4(this.rs));

    public RSBlurProcess(Context context) {
        this.rs = RenderScript.create(context.getApplicationContext());
    }

    public Bitmap blur(Bitmap input, float radius) {
        if (input.getConfig() != Config.ARGB_8888) {
            input = input.copy(Config.ARGB_8888, true);
        }
        Bitmap output = input.copy(Config.ARGB_8888, true);
        radius = Utils.clamp(radius, 0.0f, 25.0f);
        Allocation tmpIn = Allocation.createFromBitmap(this.rs, input, MipmapControl.MIPMAP_NONE, 1);
        Allocation tmpOut = Allocation.createTyped(this.rs, tmpIn.getType());
        this.theIntrinsic.setRadius(radius);
        this.theIntrinsic.setInput(tmpIn);
        this.theIntrinsic.forEach(tmpOut);
        tmpOut.copyTo(output);
        try {
            tmpIn.destroy();
        } catch (RSInvalidStateException e) {
            GalleryLog.w("RSBlurProcess", "destroy tmpIn failed");
        }
        try {
            tmpOut.destroy();
        } catch (RSInvalidStateException e2) {
            GalleryLog.w("RSBlurProcess", "destroy tmpOut failed");
        }
        return output;
    }

    protected void finalize() throws Throwable {
        if (this.theIntrinsic != null) {
            this.theIntrinsic.destroy();
        }
        if (this.rs != null) {
            this.rs.destroy();
        }
        super.finalize();
    }
}
