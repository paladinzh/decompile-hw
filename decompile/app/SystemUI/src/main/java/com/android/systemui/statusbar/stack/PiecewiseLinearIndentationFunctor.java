package com.android.systemui.statusbar.stack;

import java.util.ArrayList;

public class PiecewiseLinearIndentationFunctor extends StackIndentationFunctor {
    private final ArrayList<Float> mBaseValues;
    private final float mLinearPart;

    PiecewiseLinearIndentationFunctor(int maxItemsInStack, int peekSize, int distanceToPeekStart, float linearPart) {
        super(maxItemsInStack, peekSize, distanceToPeekStart);
        this.mBaseValues = new ArrayList(maxItemsInStack + 1);
        initBaseValues();
        this.mLinearPart = linearPart;
    }

    private void initBaseValues() {
        int sumOfSquares = getSumOfSquares(this.mMaxItemsInStack - 1);
        int totalWeight = 0;
        this.mBaseValues.add(Float.valueOf(0.0f));
        for (int i = 0; i < this.mMaxItemsInStack - 1; i++) {
            totalWeight += ((this.mMaxItemsInStack - i) - 1) * ((this.mMaxItemsInStack - i) - 1);
            this.mBaseValues.add(Float.valueOf(((float) totalWeight) / ((float) sumOfSquares)));
        }
    }

    private int getSumOfSquares(int n) {
        return (((n + 1) * n) * ((n * 2) + 1)) / 6;
    }

    public float getValue(float itemsBefore) {
        if (this.mStackStartsAtPeek) {
            itemsBefore += 1.0f;
        }
        if (itemsBefore < 0.0f) {
            return 0.0f;
        }
        if (itemsBefore >= ((float) this.mMaxItemsInStack)) {
            return (float) this.mTotalTransitionDistance;
        }
        int below = (int) itemsBefore;
        float partialIn = itemsBefore - ((float) below);
        if (below == 0) {
            return ((float) this.mDistanceToPeekStart) * partialIn;
        }
        return ((float) this.mDistanceToPeekStart) + ((((1.0f - this.mLinearPart) * (((1.0f - partialIn) * ((Float) this.mBaseValues.get(below - 1)).floatValue()) + (((Float) this.mBaseValues.get(below)).floatValue() * partialIn))) + (((itemsBefore - 1.0f) / ((float) (this.mMaxItemsInStack - 1))) * this.mLinearPart)) * ((float) this.mPeekSize));
    }
}
