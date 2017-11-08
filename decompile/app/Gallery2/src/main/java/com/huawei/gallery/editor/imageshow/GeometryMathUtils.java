package com.huawei.gallery.editor.imageshow;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import com.amap.api.maps.model.GroundOverlayOptions;
import com.android.gallery3d.common.Utils;
import com.huawei.gallery.editor.cache.BitmapCache;
import com.huawei.gallery.editor.filters.FilterCropRepresentation;
import com.huawei.gallery.editor.filters.FilterMirrorRepresentation;
import com.huawei.gallery.editor.filters.FilterMirrorRepresentation.Mirror;
import com.huawei.gallery.editor.filters.FilterRepresentation;
import com.huawei.gallery.editor.filters.FilterRotateRepresentation;
import com.huawei.gallery.editor.filters.FilterRotateRepresentation.Rotation;
import com.huawei.gallery.editor.filters.FilterStraightenRepresentation;
import com.huawei.watermark.manager.parse.WMElement;
import edu.umd.cs.findbugs.annotations.SuppressWarnings;
import java.util.Collection;

public final class GeometryMathUtils {
    private static final /* synthetic */ int[] -com-huawei-gallery-editor-filters-FilterRotateRepresentation$RotationSwitchesValues = null;

    public static final class GeometryHolder {
        public RectF crop = FilterCropRepresentation.getNil();
        public Mirror mirror = FilterMirrorRepresentation.getNil();
        public Rotation rotation = FilterRotateRepresentation.getNil();
        public float straighten = FilterStraightenRepresentation.getNil();

        public void set(GeometryHolder h) {
            this.rotation = h.rotation;
            this.straighten = h.straighten;
            this.crop.set(h.crop);
            this.mirror = h.mirror;
        }

        public void wipe() {
            this.rotation = FilterRotateRepresentation.getNil();
            this.straighten = FilterStraightenRepresentation.getNil();
            this.crop = FilterCropRepresentation.getNil();
            this.mirror = FilterMirrorRepresentation.getNil();
        }

        public boolean isNil() {
            if (this.rotation == FilterRotateRepresentation.getNil() && Utils.equal(this.straighten, FilterStraightenRepresentation.getNil()) && this.crop.equals(FilterCropRepresentation.getNil()) && this.mirror == FilterMirrorRepresentation.getNil()) {
                return true;
            }
            return false;
        }

        @SuppressWarnings({"HE_EQUALS_USE_HASHCODE"})
        public boolean equals(Object o) {
            boolean z = true;
            if (this == o) {
                return true;
            }
            if (!(o instanceof GeometryHolder)) {
                return false;
            }
            GeometryHolder h = (GeometryHolder) o;
            if (this.rotation != h.rotation || this.straighten != h.straighten || ((this.crop != null || h.crop != null) && (this.crop == null || !this.crop.equals(h.crop)))) {
                z = false;
            } else if (this.mirror != h.mirror) {
                z = false;
            }
            return z;
        }

        public String toString() {
            return getClass().getSimpleName() + "[" + "rotation:" + this.rotation.value() + ",straighten:" + this.straighten + ",crop:" + this.crop.toString() + ",mirror:" + this.mirror.value() + "]";
        }
    }

    private static /* synthetic */ int[] -getcom-huawei-gallery-editor-filters-FilterRotateRepresentation$RotationSwitchesValues() {
        if (-com-huawei-gallery-editor-filters-FilterRotateRepresentation$RotationSwitchesValues != null) {
            return -com-huawei-gallery-editor-filters-FilterRotateRepresentation$RotationSwitchesValues;
        }
        int[] iArr = new int[Rotation.values().length];
        try {
            iArr[Rotation.NINETY.ordinal()] = 1;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[Rotation.ONE_EIGHTY.ordinal()] = 3;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[Rotation.TWO_SEVENTY.ordinal()] = 2;
        } catch (NoSuchFieldError e3) {
        }
        try {
            iArr[Rotation.ZERO.ordinal()] = 4;
        } catch (NoSuchFieldError e4) {
        }
        -com-huawei-gallery-editor-filters-FilterRotateRepresentation$RotationSwitchesValues = iArr;
        return iArr;
    }

    public static void scaleRect(RectF r, float scale) {
        r.set(r.left * scale, r.top * scale, r.right * scale, r.bottom * scale);
    }

    public static float scale(float oldWidth, float oldHeight, float newWidth, float newHeight) {
        if (oldHeight == 0.0f || oldWidth == 0.0f || (oldWidth == newWidth && oldHeight == newHeight)) {
            return WMElement.CAMERASIZEVALUE1B1;
        }
        return Math.min(newWidth / oldWidth, newHeight / oldHeight);
    }

    private static void concatMirrorMatrix(Matrix m, GeometryHolder holder) {
        Mirror type = holder.mirror;
        if (type == Mirror.HORIZONTAL) {
            if (holder.rotation.value() == 90 || holder.rotation.value() == 270) {
                type = Mirror.VERTICAL;
            }
        } else if (type == Mirror.VERTICAL && (holder.rotation.value() == 90 || holder.rotation.value() == 270)) {
            type = Mirror.HORIZONTAL;
        }
        if (type == Mirror.HORIZONTAL) {
            m.postScale(GroundOverlayOptions.NO_DIMENSION, WMElement.CAMERASIZEVALUE1B1);
        } else if (type == Mirror.VERTICAL) {
            m.postScale(WMElement.CAMERASIZEVALUE1B1, GroundOverlayOptions.NO_DIMENSION);
        } else if (type == Mirror.BOTH) {
            m.postScale(WMElement.CAMERASIZEVALUE1B1, GroundOverlayOptions.NO_DIMENSION);
            m.postScale(GroundOverlayOptions.NO_DIMENSION, WMElement.CAMERASIZEVALUE1B1);
        }
    }

    public static GeometryHolder unpackGeometry(Collection<FilterRepresentation> geometry) {
        GeometryHolder holder = new GeometryHolder();
        unpackGeometry(holder, geometry);
        return holder;
    }

    public static void unpackGeometry(GeometryHolder out, Collection<FilterRepresentation> geometry) {
        out.wipe();
        for (FilterRepresentation r : geometry) {
            if (!r.isNil()) {
                if ("ROTATION".equalsIgnoreCase(r.getSerializationName())) {
                    out.rotation = ((FilterRotateRepresentation) r).getRotation();
                } else if ("CROP".equalsIgnoreCase(r.getSerializationName())) {
                    ((FilterCropRepresentation) r).getCrop(out.crop);
                } else if ("MIRROR".equalsIgnoreCase(r.getSerializationName())) {
                    out.mirror = ((FilterMirrorRepresentation) r).getMirror();
                } else if ("STRAIGHTEN".equalsIgnoreCase(r.getSerializationName()) && (r instanceof FilterStraightenRepresentation)) {
                    out.straighten = ((FilterStraightenRepresentation) r).getStraighten();
                }
            }
        }
    }

    private static Bitmap applyFullGeometryMatrix(Bitmap image, GeometryHolder holder, BitmapCache bitmapCache) {
        int width = image.getWidth();
        int height = image.getHeight();
        RectF crop = getTrueCropRect(holder, width, height);
        Rect frame = new Rect();
        crop.roundOut(frame);
        int frameWidth = frame.width();
        int frameHeight = frame.height();
        if (width <= 2 && height <= 2) {
            frameWidth = Math.min(frame.width(), width);
            frameHeight = Math.min(frame.height(), height);
        }
        Matrix m = getCropSelectionToScreenMatrix(null, holder, width, height, frameWidth, frameHeight);
        Bitmap temp = bitmapCache.getBitmap(frameWidth, frameHeight);
        Canvas canvas = new Canvas(temp);
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setFilterBitmap(true);
        paint.setDither(true);
        paint.setXfermode(new PorterDuffXfermode(Mode.SRC));
        canvas.drawBitmap(image, m, paint);
        return temp;
    }

    public static Bitmap applyGeometryRepresentations(Collection<FilterRepresentation> res, Bitmap image, BitmapCache cache) {
        GeometryHolder holder = unpackGeometry(res);
        Bitmap bmap = image;
        if (!holder.isNil()) {
            bmap = applyFullGeometryMatrix(image, holder, cache);
            if (bmap != image) {
                cache.cache(image);
            }
        }
        return bmap;
    }

    private static Matrix getFullGeometryMatrix(GeometryHolder holder, int bitmapWidth, int bitmapHeight) {
        float centerX = ((float) bitmapWidth) / 2.0f;
        float centerY = ((float) bitmapHeight) / 2.0f;
        Matrix m = new Matrix();
        m.setTranslate(-centerX, -centerY);
        holder.straighten = holder.mirror != Mirror.NONE ? -holder.straighten : holder.straighten;
        m.postRotate(holder.straighten + ((float) holder.rotation.value()));
        concatMirrorMatrix(m, holder);
        return m;
    }

    public static RectF getTrueCropRect(GeometryHolder holder, int bitmapWidth, int bitmapHeight) {
        RectF r = new RectF(holder.crop);
        FilterCropRepresentation.findScaledCrop(r, bitmapWidth, bitmapHeight);
        float s = holder.straighten;
        holder.straighten = 0.0f;
        Matrix m1 = getFullGeometryMatrix(holder, bitmapWidth, bitmapHeight);
        holder.straighten = s;
        m1.mapRect(r);
        return r;
    }

    public static void getTrueCropRect(RectF cropRect, GeometryHolder holder, int bitmapWidth, int bitmapHeight) {
        Matrix m = getFullGeometryMatrix(holder, bitmapWidth, bitmapHeight);
        float cropWidth = (float) bitmapWidth;
        float cropHeight = (float) bitmapHeight;
        if (holder.rotation.value() % 180 != 0) {
            cropWidth = (float) bitmapHeight;
            cropHeight = (float) bitmapWidth;
        }
        m.postTranslate(cropWidth / 2.0f, cropHeight / 2.0f);
        Matrix invertMatrix = new Matrix();
        m.invert(invertMatrix);
        cropRect.left *= cropWidth;
        cropRect.top *= cropHeight;
        cropRect.right *= cropWidth;
        cropRect.bottom *= cropHeight;
        invertMatrix.mapRect(cropRect);
        cropRect.left /= (float) bitmapWidth;
        cropRect.top /= (float) bitmapHeight;
        cropRect.right /= (float) bitmapWidth;
        cropRect.bottom /= (float) bitmapHeight;
    }

    private static Matrix getCropSelectionToScreenMatrix(RectF outCrop, GeometryHolder holder, int bitmapWidth, int bitmapHeight, int viewWidth, int viewHeight) {
        Matrix m = getFullGeometryMatrix(holder, bitmapWidth, bitmapHeight);
        RectF crop = getTrueCropRect(holder, bitmapWidth, bitmapHeight);
        float scale = scale(crop.width(), crop.height(), (float) viewWidth, (float) viewHeight);
        m.postScale(scale, scale);
        scaleRect(crop, scale);
        m.postTranslate((((float) viewWidth) / 2.0f) - crop.centerX(), (((float) viewHeight) / 2.0f) - crop.centerY());
        if (outCrop != null) {
            crop.offset((((float) viewWidth) / 2.0f) - crop.centerX(), (((float) viewHeight) / 2.0f) - crop.centerY());
            outCrop.set(crop);
        }
        return m;
    }

    public static boolean needsDimensionSwap(Rotation rotation) {
        switch (-getcom-huawei-gallery-editor-filters-FilterRotateRepresentation$RotationSwitchesValues()[rotation.ordinal()]) {
            case 1:
            case 2:
                return true;
            default:
                return false;
        }
    }

    public static Matrix getFullGeometryToScreenMatrix(GeometryHolder holder, int bitmapWidth, int bitmapHeight, int viewWidth, int viewHeight) {
        int bh = bitmapHeight;
        int bw = bitmapWidth;
        if (needsDimensionSwap(holder.rotation)) {
            bh = bitmapWidth;
            bw = bitmapHeight;
        }
        float scale = scale((float) bw, (float) bh, (float) viewWidth, (float) viewHeight);
        Matrix m = getFullGeometryMatrix(holder, bitmapWidth, bitmapHeight);
        m.postScale(scale, scale);
        m.postTranslate(((float) viewWidth) / 2.0f, ((float) viewHeight) / 2.0f);
        return m;
    }

    public static RectF updateCurrentCrop(GeometryHolder holder, int imageWidth, int imageHeight, int viewWidth, int viewHeight, float angle) {
        int iw;
        int ih;
        RectF tmp = new RectF();
        if (needsDimensionSwap(holder.rotation)) {
            tmp.set(0.0f, 0.0f, (float) imageHeight, (float) imageWidth);
            iw = imageHeight;
            ih = imageWidth;
        } else {
            tmp.set(0.0f, 0.0f, (float) imageWidth, (float) imageHeight);
            iw = imageWidth;
            ih = imageHeight;
        }
        scaleRect(tmp, scale((float) iw, (float) ih, (float) viewWidth, (float) viewHeight));
        getUntranslatedStraightenCropBounds(tmp, angle);
        tmp.offset((((float) viewWidth) / 2.0f) - tmp.centerX(), (((float) viewHeight) / 2.0f) - tmp.centerY());
        holder.straighten = 0.0f;
        Matrix m1 = getFullGeometryToScreenMatrix(holder, imageWidth, imageHeight, viewWidth, viewHeight);
        Matrix m = new Matrix();
        m1.invert(m);
        RectF crop = new RectF();
        crop.set(tmp);
        m.mapRect(crop);
        FilterCropRepresentation.findNormalizedCrop(crop, imageWidth, imageHeight);
        return crop;
    }

    private static void getUntranslatedStraightenCropBounds(RectF outRect, float straightenAngle) {
        float deg = straightenAngle;
        if (straightenAngle < 0.0f) {
            deg = -straightenAngle;
        }
        double a = Math.toRadians((double) deg);
        double sina = Math.sin(a);
        double cosa = Math.cos(a);
        double rw = (double) outRect.width();
        double rh = (double) outRect.height();
        double hh = Math.min((rh * rh) / ((rw * sina) + (rh * cosa)), (rh * rw) / ((rw * cosa) + (rh * sina)));
        double ww = (hh * rw) / rh;
        float left = (float) ((rw - ww) * 0.5d);
        float top = (float) ((rh - hh) * 0.5d);
        outRect.set(left, top, (float) (((double) left) + ww), (float) (((double) top) + hh));
    }
}
