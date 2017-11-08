package com.huawei.gallery.editor.tools;

import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.text.Layout.Alignment;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.view.View;
import android.view.ViewParent;
import android.widget.ScrollView;
import com.android.gallery3d.R;
import com.android.gallery3d.common.Utils;
import com.android.gallery3d.ui.Texture;
import com.android.gallery3d.util.GalleryLog;
import com.huawei.gallery.editor.animation.EditorOpenOrQuitEffect;
import com.huawei.gallery.editor.filters.FilterRepresentation;
import com.huawei.gallery.editor.step.EditorStep;
import com.huawei.gallery.editor.ui.BasePaintBar;
import com.huawei.gallery.editor.ui.ElasticHorizontalScrollView;
import com.huawei.watermark.manager.parse.WMElement;
import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.AbstractList;
import java.util.Stack;

public class EditorUtils {
    private static Method sCompressForGalleryMethod;
    public static final EditorBrushData sEditorBrushData = new EditorBrushData();
    private static final int[] sIds = new int[]{R.id.default_id_01, R.id.default_id_02, R.id.default_id_03, R.id.default_id_04, R.id.default_id_05, R.id.default_id_06, R.id.default_id_07, R.id.default_id_08, R.id.default_id_09, R.id.default_id_10, R.id.default_id_11, R.id.default_id_12, R.id.default_id_13, R.id.default_id_14, R.id.default_id_15};

    public static class EditorBrushData {
        public int brushColorIndex;
        public int brushShapeIndex;
        public int brushStrokeIndex = 2;
        public int mosaicShapeIndex;
        public int mosaicStrokeIndex = 2;
        public int splashStrokeIndex = 2;
    }

    public static class RectComputer {
        private static Matrix DEFAULT_MATRIX = new Matrix();
        private static Rect DEFAULT_RECT = new Rect(0, 0, 0, 0);
        private static Rect sDisplayRect = new Rect();
        private static RectF sDisplayRectF = new RectF();
        private static Rect sMargins = new Rect();
        private static Matrix sMatrix = new Matrix();
        private static Rect sPaddings = new Rect();
        private static Rect sRenderRect = new Rect();
        private static RectF sRenderRectF = new RectF();
        private static RectF sTempArg1RectF = new RectF();
        private static RectF sTempArg2RectF = new RectF();
        private static Rect sTempRect = new Rect();
        private static RectF sTempResultRectF = new RectF();
        private static float[] sValues = new float[9];

        public interface RenderDelegate {
            int getBitmapHeight();

            int getBitmapWidth();

            Rect getDisplayPaddings();

            Matrix getScaleMatrix();

            int getViewHeight();

            Rect getViewMargins();

            int getViewWidth();

            boolean isLongEdgeFull();
        }

        public static void computerRect(RenderDelegate delegate, Rect source, Rect target) {
            computerRect(delegate, source, target, true);
        }

        public static void computerRect(RenderDelegate delegate, RectF source, RectF target) {
            computerRect(delegate, source, target, true);
        }

        private static void initParamRect(RenderDelegate delegate) {
            if (delegate != null) {
                int viewWidth = delegate.getViewWidth();
                int viewHeight = delegate.getViewHeight();
                int bitmapWidth = delegate.getBitmapWidth();
                int bitmapHeight = delegate.getBitmapHeight();
                if (viewWidth != 0 && viewHeight != 0 && bitmapWidth != 0 && bitmapHeight != 0) {
                    GalleryLog.v("EditorUtils", "computerRect with over");
                    sMargins.set(delegate.getViewMargins() == null ? DEFAULT_RECT : delegate.getViewMargins());
                    sPaddings.set(delegate.getDisplayPaddings() == null ? DEFAULT_RECT : delegate.getDisplayPaddings());
                    sMatrix.set(delegate.getScaleMatrix() == null ? DEFAULT_MATRIX : delegate.getScaleMatrix());
                    sDisplayRect.set(sMargins.left, sMargins.top, viewWidth - sMargins.right, viewHeight - sMargins.bottom);
                    sDisplayRectF.set((float) sMargins.left, (float) sMargins.top, (float) (viewWidth - sMargins.right), (float) (viewHeight - sMargins.bottom));
                }
            }
        }

        public static void computerRect(RenderDelegate delegate, RectF source, RectF target, boolean needOver) {
            if (delegate != null) {
                initParamRect(delegate);
                computerRect(delegate.getBitmapWidth(), delegate.getBitmapHeight(), sPaddings, sMatrix, source, target, needOver, delegate.isLongEdgeFull());
            }
        }

        public static void computerRect(RenderDelegate delegate, Rect source, Rect target, boolean needOver) {
            if (delegate != null) {
                initParamRect(delegate);
                computerRect(delegate.getBitmapWidth(), delegate.getBitmapHeight(), sPaddings, sMatrix, source, target, needOver, delegate.isLongEdgeFull());
            }
        }

        public static void computeCropMaskRect(RenderDelegate delegate, Rect target) {
            if (delegate != null) {
                int viewWidth = delegate.getViewWidth();
                int viewHeight = delegate.getViewHeight();
                if (viewWidth != 0 && viewHeight != 0) {
                    Rect margins = delegate.getViewMargins() == null ? DEFAULT_RECT : delegate.getViewMargins();
                    Rect paddings = delegate.getDisplayPaddings() == null ? DEFAULT_RECT : delegate.getDisplayPaddings();
                    target.set(margins.left + paddings.left, margins.top + paddings.top, (viewWidth - margins.right) - paddings.right, (viewHeight - margins.bottom) - paddings.bottom);
                }
            }
        }

        public static void computerRect(RenderDelegate delegate, int bw, int bh, Rect source, Rect target) {
            if (delegate != null) {
                initParamRect(delegate);
                computerRect(bw, bh, sPaddings, sMatrix, source, target, true, delegate.isLongEdgeFull());
            }
        }

        private static void compute(int bw, int bh, Rect paddings, Matrix matrix, boolean isLongEdgeFull) {
            float scale;
            int displayWidth = sDisplayRect.width();
            int displayHeight = sDisplayRect.height();
            if (isLongEdgeFull) {
                scale = Math.min(((float) ((displayWidth - paddings.left) - paddings.right)) / ((float) bw), ((float) ((displayHeight - paddings.top) - paddings.bottom)) / ((float) bh));
            } else {
                scale = Math.max(((float) ((displayWidth - paddings.left) - paddings.right)) / ((float) bw), ((float) ((displayHeight - paddings.top) - paddings.bottom)) / ((float) bh));
            }
            float renderWidth = ((float) bw) * scale;
            float renderHeight = ((float) bh) * scale;
            float displayCenterX = (((float) displayWidth) / 2.0f) + ((float) sDisplayRect.left);
            float displayCenterY = (((float) displayHeight) / 2.0f) + ((float) sDisplayRect.top);
            matrix.getValues(sValues);
            renderWidth *= sValues[0];
            renderHeight *= sValues[4];
            displayCenterX += sValues[2];
            displayCenterY += sValues[5];
            sRenderRectF.set(displayCenterX - (renderWidth / 2.0f), displayCenterY - (renderHeight / 2.0f), (renderWidth / 2.0f) + displayCenterX, (renderHeight / 2.0f) + displayCenterY);
            sRenderRect.set(Math.round(displayCenterX - (renderWidth / 2.0f)), Math.round(displayCenterY - (renderHeight / 2.0f)), Math.round((renderWidth / 2.0f) + displayCenterX), Math.round((renderHeight / 2.0f) + displayCenterY));
        }

        private static void computerRect(int bw, int bh, Rect paddings, Matrix matrix, RectF source, RectF target, boolean needOver, boolean isLongEdgeFull) {
            compute(bw, bh, paddings, matrix, isLongEdgeFull);
            if (target != null) {
                target.set(needOver ? getOverRect(sDisplayRectF, sRenderRectF) : sRenderRectF);
            }
            if (source != null && target != null) {
                sTempRect.set(0, 0, bw, bh);
                RectF rectf = getRatioTargetRect(sTempRect, getRatioRect(target, sRenderRectF));
                if (rectf != null) {
                    source.set(rectf);
                }
            }
        }

        private static void computerRect(int bw, int bh, Rect paddings, Matrix matrix, Rect source, Rect target, boolean needOver, boolean isLongEdgeFull) {
            compute(bw, bh, paddings, matrix, isLongEdgeFull);
            if (target != null) {
                target.set(needOver ? getOverRect(sDisplayRect, sRenderRect) : sRenderRect);
            }
            if (source != null && target != null) {
                sTempRect.set(0, 0, bw, bh);
                RectF rectf = getRatioTargetRect(sTempRect, getRatioRect(target, sRenderRect));
                if (rectf != null) {
                    source.set((int) rectf.left, (int) rectf.top, (int) rectf.right, (int) rectf.bottom);
                }
            }
        }

        public static RectF getRatioRect(Rect rect1, Rect rect2) {
            sTempArg1RectF.set(rect1);
            sTempArg2RectF.set(rect2);
            return getRatioRect(sTempArg1RectF, sTempArg2RectF);
        }

        public static RectF getRatioRect(RectF target, RectF baseLineRect) {
            sTempResultRectF.set((target.left - baseLineRect.left) / baseLineRect.width(), (target.top - baseLineRect.top) / baseLineRect.height(), WMElement.CAMERASIZEVALUE1B1 - ((baseLineRect.right - target.right) / baseLineRect.width()), WMElement.CAMERASIZEVALUE1B1 - ((baseLineRect.bottom - target.bottom) / baseLineRect.height()));
            return sTempResultRectF;
        }

        public static Rect getOverRect(Rect rect1, Rect rect2) {
            sTempRect.set(rect1.left > rect2.left ? rect1.left : rect2.left, rect1.top > rect2.top ? rect1.top : rect2.top, rect1.right > rect2.right ? rect2.right : rect1.right, rect1.bottom > rect2.bottom ? rect2.bottom : rect1.bottom);
            return sTempRect;
        }

        public static RectF getOverRect(RectF rect1, RectF rect2) {
            sTempResultRectF.set(rect1.left > rect2.left ? rect1.left : rect2.left, rect1.top > rect2.top ? rect1.top : rect2.top, rect1.right > rect2.right ? rect2.right : rect1.right, rect1.bottom > rect2.bottom ? rect2.bottom : rect1.bottom);
            return sTempResultRectF;
        }

        private static RectF getRatioTargetRect(Rect baseLineRect, RectF ratioRect) {
            sTempArg1RectF.set(baseLineRect);
            return getRatioTargetRect(sTempArg1RectF, ratioRect);
        }

        public static RectF getRatioTargetRect(RectF baseLineRect, RectF ratioRect) {
            return getRatioTargetRect(baseLineRect, ratioRect, WMElement.CAMERASIZEVALUE1B1, WMElement.CAMERASIZEVALUE1B1);
        }

        public static RectF getRatioTargetRect(RectF baseLineRect, RectF ratioRect, float targetWidth, float targetHeight) {
            if (baseLineRect == null || ratioRect == null) {
                return null;
            }
            sTempResultRectF.set((baseLineRect.left + (ratioRect.left * baseLineRect.width())) / targetWidth, (baseLineRect.top + (ratioRect.top * baseLineRect.height())) / targetHeight, (baseLineRect.right - ((WMElement.CAMERASIZEVALUE1B1 - ratioRect.right) * baseLineRect.width())) / targetWidth, (baseLineRect.bottom - ((WMElement.CAMERASIZEVALUE1B1 - ratioRect.bottom) * baseLineRect.height())) / targetHeight);
            return sTempResultRectF;
        }
    }

    public static class RenderDelegateWithTexture implements RenderDelegate {
        private int mHeight;
        private Matrix mMatrix;
        private final Rect mRect = new Rect(0, 0, 0, 0);
        private int mRotate;
        private Texture mTexture;
        private int mWidth;

        public void init(Texture texture, Matrix matrix, int w, int h, int rotate) {
            this.mTexture = texture;
            this.mMatrix = matrix;
            this.mRotate = rotate;
            this.mWidth = w;
            this.mHeight = h;
        }

        public RenderDelegateWithTexture(Texture texture, Matrix matrix, int w, int h, int rotate) {
            init(texture, matrix, w, h, rotate);
        }

        public int getViewWidth() {
            return isOverTurn() ? this.mHeight : this.mWidth;
        }

        public int getViewHeight() {
            return isOverTurn() ? this.mWidth : this.mHeight;
        }

        public int getBitmapWidth() {
            return this.mTexture.getWidth();
        }

        public int getBitmapHeight() {
            return this.mTexture.getHeight();
        }

        public Rect getViewMargins() {
            return this.mRect;
        }

        public boolean isLongEdgeFull() {
            return false;
        }

        public Rect getDisplayPaddings() {
            return this.mRect;
        }

        public Matrix getScaleMatrix() {
            return this.mMatrix;
        }

        public boolean isOverTurn() {
            return this.mRotate % 90 == 0 && this.mRotate % 180 != 0;
        }
    }

    static {
        try {
            sCompressForGalleryMethod = Bitmap.class.getDeclaredMethod("compressForGallery", new Class[]{CompressFormat.class, Integer.TYPE, OutputStream.class});
        } catch (NoSuchMethodException e) {
            sCompressForGalleryMethod = null;
            GalleryLog.d("EditorUtils", "we have not found compressForGallery int bitmap calss.");
        }
    }

    public static void compressToJpeg(Bitmap source, int quality, OutputStream s) {
        if (quality <= 0) {
            quality = 90;
        }
        boolean compressSuccess = false;
        if (sCompressForGalleryMethod != null) {
            try {
                sCompressForGalleryMethod.invoke(source, new Object[]{CompressFormat.JPEG, Integer.valueOf(quality), s});
                compressSuccess = true;
            } catch (IllegalAccessException e) {
                GalleryLog.d("EditorUtils", "compressForGallery IllegalAccessException");
            } catch (InvocationTargetException e2) {
                GalleryLog.d("EditorUtils", "compressForGallery InvocationTargetException");
            }
        }
        if (!compressSuccess) {
            GalleryLog.d("EditorUtils", "compressForGallery failed, call google api.");
            source.compress(CompressFormat.JPEG, quality, s);
        }
    }

    public static boolean sameSerializationName(FilterRepresentation a, FilterRepresentation b) {
        if (a == null || b == null) {
            return false;
        }
        return sameSerializationName(a.getSerializationName(), b.getSerializationName());
    }

    private static boolean sameSerializationName(String a, String b) {
        boolean z = false;
        if (a != null && b != null) {
            return a.equals(b);
        }
        if (a == null && b == null) {
            z = true;
        }
        return z;
    }

    public static FilterRepresentation getRepresentation(AbstractList<FilterRepresentation> filters, FilterRepresentation filterRepresentation) {
        for (int i = 0; i < filters.size(); i++) {
            FilterRepresentation representation = (FilterRepresentation) filters.get(i);
            if (sameSerializationName(representation, filterRepresentation)) {
                return representation;
            }
        }
        return null;
    }

    public static void removeFilter(AbstractList<FilterRepresentation> filters, FilterRepresentation filterRepresentation) {
        for (int i = 0; i < filters.size(); i++) {
            if (sameSerializationName((FilterRepresentation) filters.get(i), filterRepresentation)) {
                filters.remove(i);
                return;
            }
        }
    }

    public static boolean addOrUpdateFilterRepresentation(AbstractList<FilterRepresentation> filters, FilterRepresentation representation) {
        FilterRepresentation rep = getRepresentation(filters, representation);
        if (rep == null) {
            if (representation.isNil()) {
                return false;
            }
            filters.add(representation);
        } else if (representation.isNil()) {
            removeFilter(filters, rep);
        } else {
            rep.useParametersFrom(representation);
        }
        return true;
    }

    public static boolean equals(Stack<EditorStep> s1, Stack<EditorStep> s2) {
        if (s1.size() != s2.size()) {
            return false;
        }
        for (int index = 0; index < s1.size(); index++) {
            if (!((EditorStep) s1.get(index)).equals(s2.get(index))) {
                return false;
            }
        }
        return true;
    }

    private static boolean ensureFileExists(String path) {
        File file = new File(path);
        if (file.exists()) {
            return true;
        }
        int secondSlash = path.indexOf(47, 1);
        if (secondSlash < 1 || !new File(path.substring(0, secondSlash)).exists()) {
            return false;
        }
        GalleryLog.d("EditorUtils", "create file:" + file.getParentFile().mkdirs());
        try {
            return file.createNewFile();
        } catch (IOException ioe) {
            GalleryLog.e("EditorUtils", "File creation failed." + ioe.getMessage());
            return false;
        }
    }

    public static boolean writeToPNGFile(Bitmap src, String file) {
        Throwable th;
        ensureFileExists(file);
        Closeable closeable = null;
        try {
            Closeable outputStream = new FileOutputStream(file);
            try {
                src.compress(CompressFormat.PNG, 100, outputStream);
                outputStream.flush();
                outputStream.close();
                Utils.closeSilently(outputStream);
                closeable = outputStream;
                return true;
            } catch (FileNotFoundException e) {
                closeable = outputStream;
                GalleryLog.i("EditorUtils", "new FileOutputStream() failed in writeToPNGFile() method, reason: FileNotFoundException.");
                Utils.closeSilently(closeable);
                return false;
            } catch (IOException e2) {
                closeable = outputStream;
                try {
                    GalleryLog.i("EditorUtils", "FileOutputStream.flush() or FileOutputStream.close() failed in writeToPNGFile() method, reason: IOException.");
                    Utils.closeSilently(closeable);
                    return false;
                } catch (Throwable th2) {
                    th = th2;
                    Utils.closeSilently(closeable);
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                closeable = outputStream;
                Utils.closeSilently(closeable);
                throw th;
            }
        } catch (FileNotFoundException e3) {
            GalleryLog.i("EditorUtils", "new FileOutputStream() failed in writeToPNGFile() method, reason: FileNotFoundException.");
            Utils.closeSilently(closeable);
            return false;
        } catch (IOException e4) {
            GalleryLog.i("EditorUtils", "FileOutputStream.flush() or FileOutputStream.close() failed in writeToPNGFile() method, reason: IOException.");
            Utils.closeSilently(closeable);
            return false;
        }
    }

    public static float getPaintRadius(int paintType, Matrix matrix, int strokeType, Rect bound, int sw, int sh) {
        if (bound.isEmpty()) {
            return 0.0f;
        }
        float scale = Math.min(((float) sw) / ((float) bound.width()), ((float) sh) / ((float) bound.height()));
        float radius = 0.0f;
        switch (paintType) {
            case 0:
                radius = ((float) BasePaintBar.SUB_STROKE_LEVEL_VALUE[sEditorBrushData.brushStrokeIndex]) / matrix.mapRadius(scale);
                if (strokeType != 1) {
                    radius /= 2.0f;
                    break;
                }
                break;
            case 1:
                radius = ((float) BasePaintBar.SUB_STROKE_LEVEL_VALUE[sEditorBrushData.mosaicStrokeIndex]) / matrix.mapRadius(scale);
                break;
            case 2:
                radius = ((float) BasePaintBar.SUB_STROKE_LEVEL_VALUE[sEditorBrushData.splashStrokeIndex]) / matrix.mapRadius(scale);
                break;
        }
        return radius;
    }

    public static boolean isOnParentBottomSide(View view) {
        if (view == null) {
            return false;
        }
        boolean z;
        int bottom = view.getBottom();
        int top = view.getTop();
        int height = 0;
        int scrollY = 0;
        for (ViewParent parent = view.getParent(); parent instanceof View; parent = parent.getParent()) {
            scrollY += ((View) parent).getScrollY();
            if (parent instanceof ScrollView) {
                height = ((ScrollView) parent).getHeight();
                break;
            }
            bottom += ((View) parent).getTop();
            top += ((View) parent).getTop();
        }
        if (bottom + view.getHeight() < scrollY || top > scrollY + height) {
            z = false;
        } else {
            z = true;
        }
        return z;
    }

    public static boolean isOnParentRightSide(View view) {
        if (view == null) {
            return false;
        }
        boolean z;
        int right = view.getRight();
        int left = view.getLeft();
        int width = 0;
        int scrollX = 0;
        for (ViewParent parent = view.getParent(); parent instanceof View; parent = parent.getParent()) {
            scrollX += ((View) parent).getScrollX();
            if (parent instanceof ElasticHorizontalScrollView) {
                width = ((ElasticHorizontalScrollView) parent).getWidth();
                break;
            }
            right += ((View) parent).getLeft();
            left += ((View) parent).getLeft();
        }
        if (right + view.getWidth() < scrollX || left > scrollX + width) {
            z = false;
        } else {
            z = true;
        }
        return z;
    }

    public static boolean isAlmostEquals(float a, float b) {
        return ((double) Math.abs(a - b)) < 1.0E-6d;
    }

    public static boolean isAlmostEquals(PointF a, PointF b) {
        return isAlmostEquals(a.x, b.x) ? isAlmostEquals(a.y, b.y) : false;
    }

    public static boolean isAlmostEquals(RectF a, RectF b) {
        if (isAlmostEquals(a.left, b.left) && isAlmostEquals(a.top, b.top) && isAlmostEquals(a.right, b.right)) {
            return isAlmostEquals(a.bottom, b.bottom);
        }
        return false;
    }

    public static int indexOf(int[] container, int target) {
        if (container == null) {
            return -1;
        }
        for (int index = 0; index < container.length; index++) {
            if (container[index] == target) {
                return index;
            }
        }
        return -1;
    }

    public static StaticLayout getStaticLayout(String text, TextPaint textPaint, RectF textInitRect) {
        return getStaticLayout(text, textPaint, textInitRect, 100.0f, WMElement.CAMERASIZEVALUE1B1);
    }

    public static StaticLayout getStaticLayout(String text, TextPaint textPaint, RectF textInitRect, float maxSize, float minSize) {
        long startTime = System.currentTimeMillis();
        float size = maxSize;
        while (true) {
            textPaint.setTextSize(size);
            StaticLayout staticLayout = new StaticLayout(text, textPaint, (int) (textInitRect.width() + 0.5f), Alignment.ALIGN_CENTER, WMElement.CAMERASIZEVALUE1B1, 0.0f, false);
            if (((float) staticLayout.getHeight()) <= textInitRect.height()) {
                GalleryLog.d("EditorUtils", "TextPaint size is " + size + ", time:" + (System.currentTimeMillis() - startTime));
                return staticLayout;
            } else if (size <= minSize) {
                GalleryLog.d("EditorUtils", "TextPaint min size is " + size + ", time:" + (System.currentTimeMillis() - startTime));
                return staticLayout;
            } else {
                size -= WMElement.CAMERASIZEVALUE1B1;
            }
        }
    }

    public static float calculateAngle(float touchPointX, float touchPointY, float centerX, float centerY) {
        return calculateAngle(touchPointX - centerX, centerY - touchPointY);
    }

    public static float calculateAngle(float x, float y) {
        float angle = x == 0.0f ? y >= 0.0f ? 1.5707964f : -1.5707964f : (float) Math.atan((double) (y / x));
        if (angle >= 0.0f && x < 0.0f) {
            return angle - 3.1415927f;
        }
        if (angle >= 0.0f || x >= 0.0f) {
            return angle;
        }
        return angle + 3.1415927f;
    }

    public static boolean isAlmostEquals(Matrix source, Matrix target) {
        if (source == null || target == null) {
            return false;
        }
        float[] values1 = new float[9];
        source.getValues(values1);
        float[] values2 = new float[9];
        target.getValues(values2);
        for (int i = 0; i < values1.length; i++) {
            if (!isAlmostEquals(values1[i], values2[i])) {
                return false;
            }
        }
        return true;
    }

    public static Rect getDrawRect(EditorOpenOrQuitEffect openAnimation) {
        PointF centerPoint = openAnimation.getCenterPoint();
        int width = openAnimation.getWidth();
        int height = openAnimation.getHeight();
        Rect rect = new Rect();
        new RectF(centerPoint.x - (((float) width) / 2.0f), centerPoint.y - (((float) height) / 2.0f), centerPoint.x + (((float) width) / 2.0f), centerPoint.y + (((float) height) / 2.0f)).round(rect);
        return rect;
    }

    public static float getScaleByHalfScreen(int w, int h, int sw, int sh) {
        if (w == 0 || h == 0) {
            return WMElement.CAMERASIZEVALUE1B1;
        }
        float scale = Math.min((((float) sw) / 2.0f) / ((float) w), (((float) sh) / 2.0f) / ((float) h));
        if (scale >= WMElement.CAMERASIZEVALUE1B1) {
            scale = WMElement.CAMERASIZEVALUE1B1;
        }
        return scale;
    }

    public static int getPointDistance(float x1, float y1, float x2, float y2) {
        return (int) Math.sqrt((double) (((x1 - x2) * (x1 - x2)) + ((y1 - y2) * (y1 - y2))));
    }

    public static int getViewId(int index) {
        if (index < sIds.length && index >= 0) {
            return sIds[index];
        }
        GalleryLog.d("EditorUtils", "warning ids not support! ids.length = " + sIds.length + ", index = " + index);
        return R.id.default_id_invalid;
    }
}
