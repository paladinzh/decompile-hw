package com.huawei.gallery.editor.filters;

import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import com.huawei.gallery.editor.tools.EditorUtils;
import com.huawei.gallery.editor.ui.LabelBar;
import com.huawei.gallery.editor.ui.LabelPainData;
import java.util.Comparator;
import java.util.Iterator;
import java.util.PriorityQueue;

public class FilterLabelRepresentation extends FilterRepresentation {
    private Rect mDisplayBounds;
    private PriorityQueue<LabelHolder> mLabelHolderQueue;

    public static class LabelHolder {
        public Rect bubbleInitRect = new Rect();
        public PointF centerPoint = new PointF();
        public String content = "";
        public long id = -1;
        public int initHeight;
        public int initWidth;
        public LabelPainData labelPainData = LabelBar.LABEL_PAIN_DATAS[0];
        public float rotatedAngle;
        public float scale;
        public RectF textInitRect = new RectF();

        public void updateParameters(long id, int initWidth, int initHeight, Rect bubbleInitRect, RectF textInitRect, float rotatedAngle, float scale, String content, PointF centerPoint, LabelPainData labelPainData) {
            this.id = id;
            this.initWidth = initWidth;
            this.initHeight = initHeight;
            this.bubbleInitRect.set(bubbleInitRect);
            this.textInitRect.set(textInitRect);
            this.rotatedAngle = rotatedAngle;
            this.scale = scale;
            this.content = content;
            this.centerPoint.set(centerPoint);
            this.labelPainData = new LabelPainData(labelPainData);
        }

        public void set(LabelHolder holder) {
            this.id = holder.id;
            this.initWidth = holder.initWidth;
            this.initHeight = holder.initHeight;
            this.bubbleInitRect.set(holder.bubbleInitRect);
            this.textInitRect.set(holder.textInitRect);
            this.rotatedAngle = holder.rotatedAngle;
            this.scale = holder.scale;
            this.content = holder.content;
            this.centerPoint.set(holder.centerPoint);
            this.labelPainData = new LabelPainData(holder.labelPainData);
        }

        public boolean isNil() {
            return this.id < 0;
        }

        public boolean equals(Object o) {
            if (!(o instanceof LabelHolder)) {
                return false;
            }
            LabelHolder holder = (LabelHolder) o;
            if (this.labelPainData.equals(holder.labelPainData) && this.bubbleInitRect.equals(holder.bubbleInitRect) && EditorUtils.isAlmostEquals(this.textInitRect, holder.textInitRect) && this.content.equals(holder.content) && this.id == holder.id && this.initWidth == holder.initWidth && this.initHeight == holder.initHeight && EditorUtils.isAlmostEquals(this.rotatedAngle, holder.rotatedAngle) && EditorUtils.isAlmostEquals(this.scale, holder.scale) && EditorUtils.isAlmostEquals(this.centerPoint, holder.centerPoint)) {
                return true;
            }
            return false;
        }

        public int hashCode() {
            return (String.valueOf(this.id) + "id" + this.initWidth + this.initHeight).hashCode();
        }

        static Comparator<LabelHolder> getComparator() {
            return new Comparator<LabelHolder>() {
                public int compare(LabelHolder r1, LabelHolder r2) {
                    return r1.id <= r2.id ? -1 : 1;
                }
            };
        }
    }

    public FilterLabelRepresentation() {
        super("LABEL");
        this.mLabelHolderQueue = new PriorityQueue(10, LabelHolder.getComparator());
        this.mDisplayBounds = new Rect();
        setSerializationName("LABEL");
        setFilterClass(ImageFilterLabel.class);
        setFilterType(10);
    }

    public FilterLabelRepresentation(FilterLabelRepresentation m) {
        this();
        setName(m.getName());
        this.mDisplayBounds.set(m.mDisplayBounds);
        Iterator<LabelHolder> i = m.mLabelHolderQueue.iterator();
        while (i.hasNext()) {
            LabelHolder labelHolder = new LabelHolder();
            labelHolder.set((LabelHolder) i.next());
            this.mLabelHolderQueue.add(labelHolder);
        }
    }

    public void addLabelHolder(LabelHolder labelHolder) {
        if (!labelHolder.isNil()) {
            this.mLabelHolderQueue.add(labelHolder);
        }
    }

    public boolean equals(FilterRepresentation representation) {
        if (!(representation instanceof FilterLabelRepresentation)) {
            return false;
        }
        FilterLabelRepresentation labelRepresentation = (FilterLabelRepresentation) representation;
        if (!this.mDisplayBounds.equals(labelRepresentation.mDisplayBounds) || this.mLabelHolderQueue.size() != labelRepresentation.mLabelHolderQueue.size()) {
            return false;
        }
        Iterator<LabelHolder> i1 = this.mLabelHolderQueue.iterator();
        Iterator<LabelHolder> i2 = labelRepresentation.mLabelHolderQueue.iterator();
        while (i1.hasNext() && i2.hasNext()) {
            if (!((LabelHolder) i1.next()).equals(i2.next())) {
                return false;
            }
        }
        return true;
    }

    public FilterRepresentation copy() {
        return new FilterLabelRepresentation(this);
    }

    protected void copyAllParameters(FilterRepresentation representation) {
        if (representation instanceof FilterLabelRepresentation) {
            super.copyAllParameters(representation);
            representation.useParametersFrom(this);
            return;
        }
        throw new IllegalArgumentException("calling copyAllParameters with incompatible types!");
    }

    public void useParametersFrom(FilterRepresentation a) {
        if (a instanceof FilterLabelRepresentation) {
            FilterLabelRepresentation representation = (FilterLabelRepresentation) a;
            this.mDisplayBounds.set(representation.mDisplayBounds);
            Iterator<LabelHolder> i = representation.mLabelHolderQueue.iterator();
            while (i.hasNext()) {
                LabelHolder labelHolder = new LabelHolder();
                labelHolder.set((LabelHolder) i.next());
                this.mLabelHolderQueue.add(labelHolder);
            }
            return;
        }
        throw new IllegalArgumentException("calling useParametersFrom with incompatible types!");
    }

    public boolean isNil() {
        return !this.mDisplayBounds.isEmpty() ? this.mLabelHolderQueue.isEmpty() : true;
    }

    public void reset() {
        super.reset();
        this.mLabelHolderQueue.clear();
    }

    public void setDisplayBounds(Rect displayBounds) {
        this.mDisplayBounds.set(displayBounds);
    }

    public Rect getDisplayBounds() {
        return this.mDisplayBounds;
    }

    public PriorityQueue<LabelHolder> getLabelHolderQueue() {
        return this.mLabelHolderQueue;
    }
}
