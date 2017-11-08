package com.autonavi.amap.mapcore;

public class FPointBounds {
    private final int mVersionCode;
    public final FPoint northeast;
    public final FPoint southwest;

    public static final class Builder {
        private float mEast = Float.NEGATIVE_INFINITY;
        private float mNorth = Float.NEGATIVE_INFINITY;
        private float mSouth = Float.POSITIVE_INFINITY;
        private float mWest = Float.POSITIVE_INFINITY;

        public Builder include(FPoint fPoint) {
            this.mSouth = Math.min(this.mSouth, fPoint.y);
            this.mNorth = Math.max(this.mNorth, fPoint.y);
            this.mWest = Math.min(this.mWest, fPoint.x);
            this.mEast = Math.max(this.mEast, fPoint.x);
            return this;
        }

        private boolean containsx(double d) {
            boolean z = true;
            boolean z2 = false;
            if (this.mWest <= this.mEast) {
                if (((double) this.mWest) > d || d > ((double) this.mEast)) {
                    z = false;
                }
                return z;
            }
            if ((((double) this.mWest) <= d) || d <= ((double) this.mEast)) {
                z2 = true;
            }
            return z2;
        }

        public FPointBounds build() {
            return new FPointBounds(new FPoint(this.mWest, this.mSouth), new FPoint(this.mEast, this.mNorth));
        }
    }

    FPointBounds(int i, FPoint fPoint, FPoint fPoint2) {
        this.mVersionCode = i;
        this.southwest = fPoint;
        this.northeast = fPoint2;
    }

    public FPointBounds(FPoint fPoint, FPoint fPoint2) {
        this(1, fPoint, fPoint2);
    }

    int getVersionCode() {
        return this.mVersionCode;
    }

    public static Builder builder() {
        return new Builder();
    }

    public boolean contains(FPoint fPoint) {
        if (containsy((double) fPoint.y) && containsx((double) fPoint.x)) {
            return true;
        }
        return false;
    }

    public boolean contains(FPointBounds fPointBounds) {
        boolean z = false;
        if (fPointBounds == null) {
            return false;
        }
        if (contains(fPointBounds.southwest) && contains(fPointBounds.northeast)) {
            z = true;
        }
        return z;
    }

    public boolean intersects(FPointBounds fPointBounds) {
        boolean z = false;
        if (fPointBounds == null) {
            return false;
        }
        if (intersect(fPointBounds) || fPointBounds.intersect(this)) {
            z = true;
        }
        return z;
    }

    private boolean intersect(FPointBounds fPointBounds) {
        boolean z = false;
        if (fPointBounds == null || fPointBounds.northeast == null || fPointBounds.southwest == null || this.northeast == null || this.southwest == null) {
            return false;
        }
        double d = (double) (((fPointBounds.northeast.y + fPointBounds.southwest.y) - this.northeast.y) - this.southwest.y);
        double d2 = (double) (((this.northeast.y - this.southwest.y) + fPointBounds.northeast.y) - fPointBounds.southwest.y);
        if (Math.abs((double) (((fPointBounds.northeast.x + fPointBounds.southwest.x) - this.northeast.x) - this.southwest.x)) < ((double) (((this.northeast.x - this.southwest.x) + fPointBounds.northeast.x) - this.southwest.x)) && Math.abs(d) < d2) {
            z = true;
        }
        return z;
    }

    private boolean containsy(double d) {
        return ((double) this.southwest.y) <= d && d <= ((double) this.northeast.y);
    }

    private boolean containsx(double d) {
        boolean z = true;
        boolean z2 = false;
        if (this.southwest.x <= this.northeast.x) {
            if (((double) this.southwest.x) > d || d > ((double) this.northeast.x)) {
                z = false;
            }
            return z;
        }
        if ((((double) this.southwest.x) <= d) || d <= ((double) this.northeast.x)) {
            z2 = true;
        }
        return z2;
    }

    public int hashCode() {
        return super.hashCode();
    }

    public boolean equals(Object obj) {
        boolean z = true;
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof FPointBounds)) {
            return false;
        }
        FPointBounds fPointBounds = (FPointBounds) obj;
        if (this.southwest.equals(fPointBounds.southwest)) {
            if (!this.northeast.equals(fPointBounds.northeast)) {
            }
            return z;
        }
        z = false;
        return z;
    }

    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("southwest = (");
        stringBuilder.append(this.southwest.x);
        stringBuilder.append(",");
        stringBuilder.append(this.southwest.y);
        stringBuilder.append(") northeast = (");
        stringBuilder.append(this.northeast.x);
        stringBuilder.append(",");
        stringBuilder.append(this.northeast.y);
        stringBuilder.append(")");
        return stringBuilder.toString();
    }
}
