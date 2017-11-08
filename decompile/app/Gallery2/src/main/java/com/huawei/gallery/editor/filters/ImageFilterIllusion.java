package com.huawei.gallery.editor.filters;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import com.huawei.gallery.editor.app.EditorState;
import com.huawei.gallery.editor.app.IllusionState;
import com.huawei.gallery.editor.ui.IllusionBar.STYLE;
import com.huawei.gallery.editor.ui.ShapeControl.Circle;
import com.huawei.gallery.editor.ui.ShapeControl.Line;

public class ImageFilterIllusion extends ImageFilterFx {
    private static final /* synthetic */ int[] -com-huawei-gallery-editor-ui-IllusionBar$STYLESwitchesValues = null;
    private FilterIllusionRepresentation mParameters;
    private IllusionState mState;

    private static /* synthetic */ int[] -getcom-huawei-gallery-editor-ui-IllusionBar$STYLESwitchesValues() {
        if (-com-huawei-gallery-editor-ui-IllusionBar$STYLESwitchesValues != null) {
            return -com-huawei-gallery-editor-ui-IllusionBar$STYLESwitchesValues;
        }
        int[] iArr = new int[STYLE.values().length];
        try {
            iArr[STYLE.BAND.ordinal()] = 1;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[STYLE.CIRCLE.ordinal()] = 2;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[STYLE.UNKONW.ordinal()] = 4;
        } catch (NoSuchFieldError e3) {
        }
        try {
            iArr[STYLE.WHOLE.ordinal()] = 3;
        } catch (NoSuchFieldError e4) {
        }
        -com-huawei-gallery-editor-ui-IllusionBar$STYLESwitchesValues = iArr;
        return iArr;
    }

    public void useRepresentation(FilterRepresentation representation) {
        if (representation instanceof FilterIllusionRepresentation) {
            this.mParameters = (FilterIllusionRepresentation) representation;
        }
    }

    public void useEditorState(EditorState state) {
        if (state instanceof IllusionState) {
            this.mState = (IllusionState) state;
        }
    }

    public Bitmap apply(Bitmap bitmap) {
        if (this.mParameters == null) {
            return bitmap;
        }
        if (!(getEnvironment().getQuality() == 2 || this.mState.getCurrentValue() == this.mParameters.getValue() || !this.mState.isActive())) {
            Bitmap bmp = getEnvironment().getBitmapCopy(bitmap);
            if (bmp == null) {
                return bitmap;
            }
            synchronized (FILTER_LOCK) {
                nativeApplyFilterIllusionCircle(bmp, bmp.getWidth(), bmp.getHeight(), this.mParameters.getValue(), bmp.getWidth() / 2, bmp.getHeight() / 2, 0);
            }
            this.mState.setCurrentValue(this.mParameters.getValue());
            this.mState.setApplyBitmap(bmp);
        }
        if (this.mParameters.getNeedApply()) {
            float scaleX = ((float) bitmap.getWidth()) / ((float) this.mParameters.getBound().width());
            float scaleY = ((float) bitmap.getHeight()) / ((float) this.mParameters.getBound().height());
            Matrix matrix = new Matrix();
            matrix.setScale(scaleX, scaleY);
            switch (-getcom-huawei-gallery-editor-ui-IllusionBar$STYLESwitchesValues()[this.mParameters.getStyle().ordinal()]) {
                case 1:
                    applyTiltShift(bitmap, this.mParameters, matrix, this.mParameters.getValue());
                    break;
                case 2:
                    applyBlur(bitmap, this.mParameters, matrix, this.mParameters.getValue());
                    break;
                case 3:
                    synchronized (FILTER_LOCK) {
                        nativeApplyFilterIllusionBand(bitmap, bitmap.getWidth(), bitmap.getHeight(), this.mParameters.getValue(), bitmap.getWidth() / 2, bitmap.getHeight() / 2, bitmap.getWidth() / 2, bitmap.getHeight() / 2);
                    }
                    break;
            }
        }
        return bitmap;
    }

    private void applyBlur(Bitmap bitmap, Circle circle, Matrix matrix, int StrengthValue) {
        float[] point1 = new float[]{circle.getPointX(), circle.getPointY()};
        matrix.mapPoints(point1);
        int radius = (int) matrix.mapRadius(circle.getRadius());
        synchronized (FILTER_LOCK) {
            nativeApplyFilterIllusionCircle(bitmap, bitmap.getWidth(), bitmap.getHeight(), StrengthValue, (int) point1[0], (int) point1[1], radius);
        }
    }

    private void applyTiltShift(Bitmap bitmap, Line line, Matrix matrix, int StrengthValue) {
        float[] point1 = new float[]{line.getPoint1X(), line.getPoint1Y()};
        matrix.mapPoints(point1);
        float[] point2 = new float[]{line.getPoint2X(), line.getPoint2Y()};
        matrix.mapPoints(point2);
        synchronized (FILTER_LOCK) {
            nativeApplyFilterIllusionBand(bitmap, bitmap.getWidth(), bitmap.getHeight(), StrengthValue, (int) point1[0], (int) point1[1], (int) point2[0], (int) point2[1]);
        }
    }
}
