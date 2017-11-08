package android.support.v17.leanback.widget;

class WindowAlignment {
    public final Axis horizontal = new Axis("horizontal");
    private Axis mMainAxis = this.horizontal;
    private int mOrientation = 0;
    private Axis mSecondAxis = this.vertical;
    public final Axis vertical = new Axis("vertical");

    public static class Axis {
        private int mMaxEdge;
        private int mMaxScroll;
        private int mMinEdge;
        private int mMinScroll;
        private String mName;
        private int mPaddingHigh;
        private int mPaddingLow;
        private boolean mReversedFlow;
        private float mScrollCenter;
        private int mSize;
        private int mWindowAlignment = 3;
        private int mWindowAlignmentOffset = 0;
        private float mWindowAlignmentOffsetPercent = 50.0f;

        public Axis(String name) {
            reset();
            this.mName = name;
        }

        public final void setWindowAlignment(int windowAlignment) {
            this.mWindowAlignment = windowAlignment;
        }

        public final void setMinEdge(int minEdge) {
            this.mMinEdge = minEdge;
        }

        public final int getMinEdge() {
            return this.mMinEdge;
        }

        public final void setMinScroll(int minScroll) {
            this.mMinScroll = minScroll;
        }

        public final int getMinScroll() {
            return this.mMinScroll;
        }

        public final void invalidateScrollMin() {
            this.mMinEdge = Integer.MIN_VALUE;
            this.mMinScroll = Integer.MIN_VALUE;
        }

        public final void setMaxEdge(int maxEdge) {
            this.mMaxEdge = maxEdge;
        }

        public final int getMaxEdge() {
            return this.mMaxEdge;
        }

        public final void setMaxScroll(int maxScroll) {
            this.mMaxScroll = maxScroll;
        }

        public final int getMaxScroll() {
            return this.mMaxScroll;
        }

        public final void invalidateScrollMax() {
            this.mMaxEdge = Integer.MAX_VALUE;
            this.mMaxScroll = Integer.MAX_VALUE;
        }

        private void reset() {
            this.mScrollCenter = -2.14748365E9f;
            this.mMinEdge = Integer.MIN_VALUE;
            this.mMaxEdge = Integer.MAX_VALUE;
        }

        public final boolean isMinUnknown() {
            return this.mMinEdge == Integer.MIN_VALUE;
        }

        public final boolean isMaxUnknown() {
            return this.mMaxEdge == Integer.MAX_VALUE;
        }

        public final void setSize(int size) {
            this.mSize = size;
        }

        public final int getSize() {
            return this.mSize;
        }

        public final void setPadding(int paddingLow, int paddingHigh) {
            this.mPaddingLow = paddingLow;
            this.mPaddingHigh = paddingHigh;
        }

        public final int getPaddingLow() {
            return this.mPaddingLow;
        }

        public final int getPaddingHigh() {
            return this.mPaddingHigh;
        }

        public final int getClientSize() {
            return (this.mSize - this.mPaddingLow) - this.mPaddingHigh;
        }

        public final int getSystemScrollPos(int scrollCenter, boolean isAtMin, boolean isAtMax) {
            int middlePosition;
            if (this.mReversedFlow) {
                if (this.mWindowAlignmentOffset >= 0) {
                    middlePosition = (this.mSize - this.mWindowAlignmentOffset) - this.mPaddingLow;
                } else {
                    middlePosition = (-this.mWindowAlignmentOffset) - this.mPaddingLow;
                }
                if (this.mWindowAlignmentOffsetPercent != -1.0f) {
                    middlePosition -= (int) ((((float) this.mSize) * this.mWindowAlignmentOffsetPercent) / 100.0f);
                }
            } else {
                if (this.mWindowAlignmentOffset >= 0) {
                    middlePosition = this.mWindowAlignmentOffset - this.mPaddingLow;
                } else {
                    middlePosition = (this.mSize + this.mWindowAlignmentOffset) - this.mPaddingLow;
                }
                if (this.mWindowAlignmentOffsetPercent != -1.0f) {
                    middlePosition += (int) ((((float) this.mSize) * this.mWindowAlignmentOffsetPercent) / 100.0f);
                }
            }
            int clientSize = getClientSize();
            int afterMiddlePosition = clientSize - middlePosition;
            boolean isMinUnknown = isMinUnknown();
            boolean isMaxUnknown = isMaxUnknown();
            if (isMinUnknown || isMaxUnknown || (this.mWindowAlignment & 3) != 3 || this.mMaxEdge - this.mMinEdge > clientSize) {
                if (!isMinUnknown) {
                    if (this.mReversedFlow) {
                    }
                    if (isAtMin || scrollCenter - this.mMinEdge <= middlePosition) {
                        return this.mMinEdge - this.mPaddingLow;
                    }
                }
                if (isMaxUnknown || (this.mReversedFlow ? (this.mWindowAlignment & 1) != 0 : (this.mWindowAlignment & 2) != 0) || (!isAtMax && this.mMaxEdge - scrollCenter > afterMiddlePosition)) {
                    return (scrollCenter - middlePosition) - this.mPaddingLow;
                }
                return (this.mMaxEdge - this.mPaddingLow) - clientSize;
            }
            int i;
            if (this.mReversedFlow) {
                i = (this.mMaxEdge - this.mPaddingLow) - clientSize;
            } else {
                i = this.mMinEdge - this.mPaddingLow;
            }
            return i;
        }

        public final void setReversedFlow(boolean reversedFlow) {
            this.mReversedFlow = reversedFlow;
        }

        public String toString() {
            return "center: " + this.mScrollCenter + " min:" + this.mMinEdge + " max:" + this.mMaxEdge;
        }
    }

    WindowAlignment() {
    }

    public final Axis mainAxis() {
        return this.mMainAxis;
    }

    public final Axis secondAxis() {
        return this.mSecondAxis;
    }

    public final void setOrientation(int orientation) {
        this.mOrientation = orientation;
        if (this.mOrientation == 0) {
            this.mMainAxis = this.horizontal;
            this.mSecondAxis = this.vertical;
            return;
        }
        this.mMainAxis = this.vertical;
        this.mSecondAxis = this.horizontal;
    }

    public final void reset() {
        mainAxis().reset();
    }

    public String toString() {
        return new StringBuffer().append("horizontal=").append(this.horizontal.toString()).append("; vertical=").append(this.vertical.toString()).toString();
    }
}
