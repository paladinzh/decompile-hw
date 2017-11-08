package com.huawei.gallery.editor.filters;

import android.graphics.Matrix;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.PointF;
import android.graphics.Rect;
import com.amap.api.maps.model.GroundOverlayOptions;
import com.android.gallery3d.R;
import com.android.gallery3d.util.GalleryLog;
import com.android.gallery3d.util.GalleryUtils;
import com.huawei.gallery.editor.tools.EditorUtils;
import com.huawei.gallery.editor.ui.BasePaintBar;
import com.huawei.gallery.editor.ui.PaintBrushBar;
import edu.umd.cs.findbugs.annotations.SuppressWarnings;
import java.util.Iterator;
import java.util.Stack;

public class FilterMosaicRepresentation extends FilterRepresentation {
    private Stack<StrokeData> mAppliedMosaic = new Stack();
    private Rect mBounds = new Rect();
    private StrokeData mCurrent;
    private StrokeData mCurrentSegment;
    private boolean mDisableNil = false;
    private Stack<StrokeData> mRedoMosaic = new Stack();
    private boolean mUseCache;

    public static class StrokeData implements Cloneable {
        public PointF lastPoint = new PointF(GroundOverlayOptions.NO_DIMENSION, GroundOverlayOptions.NO_DIMENSION);
        public int mColor;
        public Path mPath;
        public float mRadius;
        public PointF startPoint = new PointF(GroundOverlayOptions.NO_DIMENSION, GroundOverlayOptions.NO_DIMENSION);
        public int type;

        public StrokeData(StrokeData copy) {
            this.type = copy.type;
            this.mPath = new Path(copy.mPath);
            this.mRadius = copy.mRadius;
            this.mColor = copy.mColor;
            this.startPoint.set(copy.startPoint);
            this.lastPoint.set(copy.lastPoint);
        }

        @SuppressWarnings({"HE_EQUALS_USE_HASHCODE", "EQ_SELF_USE_OBJECT"})
        public boolean equals(Object o) {
            boolean z = false;
            if (!(o instanceof StrokeData)) {
                return false;
            }
            StrokeData sd = (StrokeData) o;
            if (this.mPath.equals(sd.mPath) && ((double) Math.abs(this.mRadius - sd.mRadius)) < 1.0E-6d && this.type == sd.type && this.mColor == sd.mColor && this.startPoint.equals(sd.startPoint)) {
                z = this.lastPoint.equals(sd.lastPoint);
            }
            return z;
        }

        public String toString() {
            return "stroke(, path(" + this.mPath + "), " + this.mRadius + "," + this.type + "," + Integer.toHexString(this.mColor) + ", startPoint:" + this.startPoint + ", lastPoint:" + this.lastPoint + ")";
        }

        public StrokeData clone() throws CloneNotSupportedException {
            return (StrokeData) super.clone();
        }

        public boolean isValid() {
            PathMeasure pathMeasure = new PathMeasure();
            pathMeasure.setPath(this.mPath, false);
            if (pathMeasure.getLength() < 2.0f) {
                return false;
            }
            return true;
        }

        public void startNewSection(float x, float y, int type, int paintType, Matrix matrix, float scale) {
            this.mPath = new Path();
            this.mPath.moveTo(x, y);
            this.startPoint.set(x, y);
            int drawingType;
            switch (paintType) {
                case 0:
                    drawingType = EditorUtils.sEditorBrushData.brushShapeIndex;
                    if (type == 1) {
                        drawingType = 5;
                    }
                    this.type = drawingType;
                    this.mColor = PaintBrushBar.SUB_MENU_COLOR_VALUE[EditorUtils.sEditorBrushData.brushColorIndex];
                    this.mRadius = ((float) BasePaintBar.SUB_STROKE_LEVEL_VALUE[EditorUtils.sEditorBrushData.brushStrokeIndex]) / matrix.mapRadius(scale);
                    return;
                case 1:
                    drawingType = (EditorUtils.sEditorBrushData.mosaicShapeIndex + 5) + 1;
                    if (type == 1) {
                        drawingType = 5;
                    }
                    this.type = drawingType;
                    this.mRadius = ((float) BasePaintBar.SUB_STROKE_LEVEL_VALUE[EditorUtils.sEditorBrushData.mosaicStrokeIndex]) / matrix.mapRadius(scale);
                    return;
                case 2:
                    this.type = 5;
                    this.mRadius = ((float) BasePaintBar.SUB_STROKE_LEVEL_VALUE[EditorUtils.sEditorBrushData.splashStrokeIndex]) / matrix.mapRadius(scale);
                    return;
                default:
                    return;
            }
        }

        public boolean addPoint(float x, float y) {
            if (this.lastPoint.x == GroundOverlayOptions.NO_DIMENSION && this.lastPoint.y == GroundOverlayOptions.NO_DIMENSION && EditorUtils.getPointDistance(this.startPoint.x, this.startPoint.y, x, y) > 100) {
                return false;
            }
            this.mPath.lineTo(x, y);
            this.lastPoint.set(x, y);
            return true;
        }

        public void updateByTargetStrokeData(StrokeData strokeData, float x, float y) {
            this.mPath.lineTo(x, y);
            this.lastPoint.set(x, y);
            PathMeasure pm = new PathMeasure(this.mPath, false);
            Path tempPath = new Path();
            if (pm.getLength() - 1000.0f > 0.0f) {
                pm.getSegment(pm.getLength() - 1000.0f, pm.getLength(), tempPath, true);
            } else {
                tempPath.addPath(strokeData.mPath);
            }
            this.mPath.reset();
            this.mPath.addPath(tempPath);
        }

        private void setReportType(boolean[] result) {
            if (result != null && result.length >= 3) {
                if (this.type < 5) {
                    result[0] = true;
                } else if (this.type > 5) {
                    result[1] = true;
                } else {
                    result[2] = true;
                }
            }
        }
    }

    public void disableNil() {
        this.mDisableNil = true;
    }

    public FilterMosaicRepresentation() {
        super("MOSAIC");
        setSerializationName("MOSAIC");
        setFilterClass(ImageFilterMosaic.class);
        setFilterType(9);
        setTextId(R.string.simple_editor_mosaic);
    }

    public StrokeData getCurrentSegment() {
        return (this.mCurrentSegment == null || !this.mCurrentSegment.isValid()) ? null : this.mCurrentSegment;
    }

    public boolean isNil() {
        return !this.mDisableNil && this.mAppliedMosaic.isEmpty() && this.mCurrent == null;
    }

    public Stack<StrokeData> getAppliedMosaic() {
        return this.mAppliedMosaic;
    }

    public boolean canUndo() {
        return this.mAppliedMosaic.size() > 0 || this.mCurrent != null;
    }

    public boolean canRedo() {
        return this.mRedoMosaic.size() > 0;
    }

    public void undo() {
        if (this.mCurrent != null) {
            this.mRedoMosaic.push(this.mCurrent);
            this.mCurrent = null;
        } else if (!this.mAppliedMosaic.empty()) {
            this.mRedoMosaic.push((StrokeData) this.mAppliedMosaic.pop());
        }
    }

    public void redo() {
        if (!this.mRedoMosaic.empty()) {
            this.mAppliedMosaic.push((StrokeData) this.mRedoMosaic.pop());
        }
    }

    public StrokeData getCurrentStrokeData() {
        return this.mCurrent;
    }

    public boolean isCurrentStrokeDataValid() {
        if (this.mCurrent == null) {
            return false;
        }
        return this.mCurrent.isValid();
    }

    public void startNewSection(float x, float y, int type, boolean force, int paintType, Matrix matrix) {
        if (force || type != 1 || !this.mAppliedMosaic.isEmpty()) {
            float scale = Math.min(((float) GalleryUtils.getWidthPixels()) / ((float) this.mBounds.width()), ((float) GalleryUtils.getHeightPixels()) / ((float) this.mBounds.height()));
            this.mCurrent = new StrokeData();
            this.mCurrent.startNewSection(x, y, type, paintType, matrix, scale);
            this.mCurrentSegment = new StrokeData(this.mCurrent);
        }
    }

    public boolean addPoint(float x, float y) {
        if (this.mCurrent.addPoint(x, y)) {
            this.mCurrentSegment.updateByTargetStrokeData(this.mCurrent, x, y);
            return true;
        }
        close();
        return false;
    }

    public void close() {
        if (this.mCurrent != null) {
            if (isCurrentStrokeDataValid()) {
                this.mAppliedMosaic.add(this.mCurrent);
                this.mRedoMosaic.clear();
            }
            this.mCurrent = null;
        }
    }

    public void discard() {
        this.mCurrent = null;
    }

    public boolean equals(FilterRepresentation representation) {
        if (!super.equals(representation) || !(representation instanceof FilterMosaicRepresentation)) {
            return false;
        }
        FilterMosaicRepresentation fdRep = (FilterMosaicRepresentation) representation;
        if (fdRep.mAppliedMosaic.size() != this.mAppliedMosaic.size()) {
            return false;
        }
        int i;
        if (fdRep.mCurrent == null) {
            i = 1;
        } else {
            i = 0;
        }
        int i2 = (this.mCurrent == null || this.mCurrent.mPath == null) ? 1 : 0;
        if ((i ^ i2) != 0 || !fdRep.mBounds.equals(this.mBounds)) {
            return false;
        }
        int n = this.mAppliedMosaic.size();
        for (int i3 = 0; i3 < n; i3++) {
            if (!((StrokeData) this.mAppliedMosaic.get(i3)).equals((StrokeData) this.mAppliedMosaic.get(i3))) {
                return false;
            }
        }
        return true;
    }

    public FilterRepresentation copy() {
        FilterMosaicRepresentation representation = new FilterMosaicRepresentation();
        copyAllParameters(representation);
        return representation;
    }

    protected void copyAllParameters(FilterRepresentation representation) {
        super.copyAllParameters(representation);
        representation.useParametersFrom(this);
    }

    public void useParametersFrom(FilterRepresentation a) {
        if (a instanceof FilterMosaicRepresentation) {
            FilterMosaicRepresentation representation = (FilterMosaicRepresentation) a;
            try {
                Iterator<StrokeData> elem;
                if (representation.mCurrent != null) {
                    this.mCurrent = new StrokeData(representation.mCurrent);
                } else {
                    this.mCurrent = null;
                }
                if (representation.mCurrentSegment != null) {
                    this.mCurrentSegment = new StrokeData(representation.mCurrentSegment);
                } else {
                    this.mCurrentSegment = null;
                }
                if (representation.mRedoMosaic != null) {
                    this.mRedoMosaic = new Stack();
                    elem = representation.mRedoMosaic.iterator();
                    while (elem.hasNext()) {
                        this.mRedoMosaic.add(new StrokeData((StrokeData) elem.next()));
                    }
                } else {
                    this.mRedoMosaic = null;
                }
                if (representation.mAppliedMosaic != null) {
                    this.mAppliedMosaic = new Stack();
                    elem = representation.mAppliedMosaic.iterator();
                    while (elem.hasNext()) {
                        this.mAppliedMosaic.add(new StrokeData((StrokeData) elem.next()));
                    }
                } else {
                    this.mAppliedMosaic = null;
                }
                this.mBounds.set(representation.mBounds);
                this.mDisableNil = representation.mDisableNil;
                return;
            } catch (RuntimeException e) {
                GalleryLog.i("FilterMosaicRepresentation", "catch a RuntimeException." + e.getMessage());
                return;
            }
        }
        GalleryLog.v("FilterMosaicRepresentation", "cannot use parameters from " + a);
    }

    public void reset() {
        super.reset();
        this.mBounds.setEmpty();
        clearStrokeData();
        this.mUseCache = false;
    }

    public void clearSegment() {
        this.mCurrentSegment = null;
    }

    public void clearStrokeData() {
        this.mAppliedMosaic.clear();
        this.mRedoMosaic.clear();
        this.mCurrent = null;
    }

    public void setBounds(Rect bounds) {
        this.mBounds.set(bounds);
    }

    public Rect getBounds() {
        return this.mBounds;
    }

    public boolean useDrawCache() {
        return this.mUseCache;
    }

    public void setUseCache(boolean useCache) {
        this.mUseCache = useCache;
    }

    public boolean[] getReportStrokeType() {
        boolean[] result = new boolean[]{false, false, false};
        int size = this.mAppliedMosaic.size();
        for (int index = 0; index < size; index++) {
            StrokeData sd = (StrokeData) this.mAppliedMosaic.get(index);
            if (sd != null) {
                sd.setReportType(result);
            }
            if (result[0] && result[1] && result[2]) {
                return result;
            }
        }
        if (this.mCurrent != null) {
            this.mCurrent.setReportType(result);
        }
        return result;
    }
}
