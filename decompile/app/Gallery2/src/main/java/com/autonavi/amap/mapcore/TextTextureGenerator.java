package com.autonavi.amap.mapcore;

import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.FontMetrics;
import android.graphics.Paint.FontMetricsInt;
import android.graphics.Typeface;
import android.os.Environment;
import android.text.TextPaint;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.huawei.watermark.manager.parse.WMElement;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.nio.Buffer;
import java.nio.ByteBuffer;

public class TextTextureGenerator {
    private static final int ALIGNCENTER = 51;
    private static final int ALIGNLEFT = 49;
    private static final int ALIGNRIGHT = 50;
    static final int AN_LABEL_MAXCHARINLINE = 7;
    static final int AN_LABEL_MULITYLINE_SPAN = 2;
    static final int TEXT_FONTSIZE = 32;
    static final int TEXT_FONTSIZE_TRUE = 30;
    private float base_line;
    private float start_x;
    private Paint text_paint;

    public static int GetNearstSize2N(int i) {
        int i2 = 1;
        while (i > i2) {
            i2 *= 2;
        }
        return i2;
    }

    public TextTextureGenerator() {
        this.base_line = 0.0f;
        this.start_x = 0.0f;
        this.text_paint = null;
        this.text_paint = newPaint(null, 30, 49);
        this.start_x = 0.0f;
        FontMetrics fontMetrics = this.text_paint.getFontMetrics();
        this.base_line = ((BitmapDescriptorFactory.HUE_ORANGE - (fontMetrics.bottom - fontMetrics.top)) / 2.0f) - fontMetrics.top;
    }

    public byte[] getTextPixelBuffer(int i) {
        try {
            char[] cArr = new char[]{(char) ((char) i)};
            Bitmap createBitmap = Bitmap.createBitmap(32, 32, Config.ALPHA_8);
            Canvas canvas = new Canvas(createBitmap);
            byte[] bArr = new byte[1024];
            Buffer wrap = ByteBuffer.wrap(bArr);
            float measureText = this.text_paint.measureText(cArr[0] + "");
            Align textAlign = this.text_paint.getTextAlign();
            float f = measureText - BitmapDescriptorFactory.HUE_ORANGE;
            if (textAlign != Align.CENTER && f >= 4.0f) {
                this.text_paint.setTextAlign(Align.CENTER);
                this.text_paint.setTextSize(BitmapDescriptorFactory.HUE_ORANGE - f);
                canvas.drawText(cArr, 0, 1, (BitmapDescriptorFactory.HUE_ORANGE - f) / 2.0f, this.base_line, this.text_paint);
                this.text_paint.setTextAlign(textAlign);
            } else {
                canvas.drawText(cArr, 0, 1, this.start_x, this.base_line - 2.0f, this.text_paint);
            }
            createBitmap.copyPixelsToBuffer(wrap);
            createBitmap.recycle();
            return bArr;
        } catch (OutOfMemoryError e) {
            return null;
        } catch (Exception e2) {
            return null;
        }
    }

    public byte[] getCharsWidths(int[] iArr) {
        int length = iArr.length;
        byte[] bArr = new byte[length];
        float[] fArr = new float[1];
        for (int i = 0; i < length; i++) {
            fArr[0] = this.text_paint.measureText(((char) iArr[i]) + "");
            bArr[i] = (byte) ((byte) ((int) (fArr[0] + WMElement.CAMERASIZEVALUE1B1)));
        }
        return bArr;
    }

    private static Paint newPaint(String str, int i, int i2) {
        Paint textPaint = new TextPaint();
        textPaint.setColor(-1);
        textPaint.setTextSize((float) i);
        textPaint.setAntiAlias(true);
        textPaint.setFilterBitmap(true);
        textPaint.setTypeface(Typeface.DEFAULT);
        switch (i2) {
            case 49:
                textPaint.setTextAlign(Align.LEFT);
                break;
            case 50:
                textPaint.setTextAlign(Align.RIGHT);
                break;
            case ALIGNCENTER /*51*/:
                textPaint.setTextAlign(Align.CENTER);
                break;
            default:
                textPaint.setTextAlign(Align.LEFT);
                break;
        }
        return textPaint;
    }

    public static float getFontlength(Paint paint, String str) {
        return paint.measureText(str);
    }

    public static float getFontHeight(Paint paint) {
        FontMetrics fontMetrics = paint.getFontMetrics();
        return fontMetrics.descent - fontMetrics.ascent;
    }

    public static void generaAsccIITexturePng() throws Exception {
        if (Environment.getExternalStorageState().equals("mounted")) {
            OutputStream fileOutputStream = new FileOutputStream(new File(Environment.getExternalStorageDirectory(), "asccii.png"));
            Paint newPaint = newPaint(null, 32, 49);
            Bitmap createBitmap = Bitmap.createBitmap(512, 512, Config.ARGB_8888);
            FontMetricsInt fontMetricsInt = newPaint.getFontMetricsInt();
            Canvas canvas = new Canvas(createBitmap);
            Paint newPaint2 = newPaint(null, 32, 49);
            float[] fArr = new float[1];
            for (int i = 0; i < 16; i++) {
                for (int i2 = 0; i2 < 16; i2++) {
                    char c = (char) ((i * 16) + i2);
                    canvas.drawText(c + "", (float) (i2 * 16), (float) (((i * 16) - fontMetricsInt.ascent) - 2), newPaint2);
                    newPaint2.getTextWidths(c + "", fArr);
                }
            }
            createBitmap.compress(CompressFormat.PNG, 100, fileOutputStream);
            createBitmap.recycle();
        }
    }

    public String getFontVersion() {
        return Md5Utility.getByteArrayMD5(getTextPixelBuffer(39640));
    }
}
