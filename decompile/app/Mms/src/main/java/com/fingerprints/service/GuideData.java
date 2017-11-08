package com.fingerprints.service;

import android.graphics.Point;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import java.util.ArrayList;

public class GuideData implements Parcelable {
    public static final Creator<GuideData> CREATOR = new Creator<GuideData>() {
        public GuideData createFromParcel(Parcel source) {
            return new GuideData(source);
        }

        public GuideData[] newArray(int size) {
            return new GuideData[size];
        }
    };
    public int guidedAcceptance;
    public int guidedCoverage;
    public GuidedRect guidedLastTouch;
    public int[] guidedLastTouchArray;
    private int guidedLastTouchArrayLen;
    public int guidedMainClusterIdentified;
    public int[] guidedMaskArray;
    private int guidedMaskArrayLen;
    public GuidedMaskList guidedMaskList;
    public int guidedNextDirection;
    public GuidedRect guidedNextTouch;
    public int[] guidedNextTouchArray;
    private int guidedNextTouchArrayLen;
    public int guidedProgress;
    public int guidedQuality;
    public int guidedRejectReason;
    public int guidedStitched;
    public int guidedThresholdCoverage;
    public int guidedThresholdQuality;
    public int guidedTouchTooClose;
    public int guidedTouchTooFar;

    public static class GuidedMaskList {
        public ArrayList<GuidedRect> guidedMaskList = new ArrayList();
    }

    public static class GuidedRect {
        public Point guidedBottomLeft = new Point();
        public Point guidedBottomRight = new Point();
        public Point guidedTopLeft = new Point();
        public Point guidedTopRight = new Point();
    }

    public GuideData() {
        this.guidedLastTouchArrayLen = 0;
        this.guidedNextTouchArrayLen = 0;
        this.guidedMaskArrayLen = 0;
        this.guidedLastTouchArray = new int[0];
        this.guidedNextTouchArray = new int[0];
        this.guidedMaskArray = new int[0];
        this.guidedProgress = 0;
        this.guidedCoverage = 0;
        this.guidedQuality = 0;
        this.guidedAcceptance = 0;
        this.guidedRejectReason = 0;
        this.guidedStitched = 0;
        this.guidedThresholdCoverage = 0;
        this.guidedThresholdQuality = 0;
        this.guidedNextDirection = 0;
        this.guidedMainClusterIdentified = 0;
        this.guidedTouchTooClose = 0;
        this.guidedTouchTooFar = 0;
        this.guidedLastTouch = new GuidedRect();
        this.guidedNextTouch = new GuidedRect();
        this.guidedMaskList = new GuidedMaskList();
    }

    public GuideData(Parcel in) {
        this.guidedLastTouchArrayLen = 0;
        this.guidedNextTouchArrayLen = 0;
        this.guidedMaskArrayLen = 0;
        this.guidedLastTouchArray = new int[0];
        this.guidedNextTouchArray = new int[0];
        this.guidedMaskArray = new int[0];
        this.guidedAcceptance = in.readInt();
        this.guidedCoverage = in.readInt();
        this.guidedMainClusterIdentified = in.readInt();
        this.guidedNextDirection = in.readInt();
        this.guidedProgress = in.readInt();
        this.guidedQuality = in.readInt();
        this.guidedRejectReason = in.readInt();
        this.guidedStitched = in.readInt();
        this.guidedThresholdCoverage = in.readInt();
        this.guidedThresholdQuality = in.readInt();
        this.guidedTouchTooClose = in.readInt();
        this.guidedTouchTooFar = in.readInt();
        this.guidedLastTouchArrayLen = in.readInt();
        if (this.guidedLastTouchArrayLen > 0) {
            this.guidedLastTouchArray = new int[this.guidedLastTouchArrayLen];
            in.readIntArray(this.guidedLastTouchArray);
        }
        this.guidedNextTouchArrayLen = in.readInt();
        if (this.guidedNextTouchArrayLen > 0) {
            this.guidedNextTouchArray = new int[this.guidedNextTouchArrayLen];
            in.readIntArray(this.guidedNextTouchArray);
        }
        this.guidedMaskArrayLen = in.readInt();
        if (this.guidedMaskArrayLen > 0) {
            this.guidedMaskArray = new int[this.guidedMaskArrayLen];
            in.readIntArray(this.guidedMaskArray);
        }
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.guidedAcceptance);
        dest.writeInt(this.guidedCoverage);
        dest.writeInt(this.guidedMainClusterIdentified);
        dest.writeInt(this.guidedNextDirection);
        dest.writeInt(this.guidedProgress);
        dest.writeInt(this.guidedQuality);
        dest.writeInt(this.guidedRejectReason);
        dest.writeInt(this.guidedStitched);
        dest.writeInt(this.guidedThresholdCoverage);
        dest.writeInt(this.guidedThresholdQuality);
        dest.writeInt(this.guidedTouchTooClose);
        dest.writeInt(this.guidedTouchTooFar);
        this.guidedLastTouchArrayLen = this.guidedLastTouchArray.length;
        dest.writeInt(this.guidedLastTouchArrayLen);
        if (this.guidedLastTouchArrayLen > 0) {
            dest.writeIntArray(this.guidedLastTouchArray);
        }
        this.guidedNextTouchArrayLen = this.guidedNextTouchArray.length;
        dest.writeInt(this.guidedNextTouchArrayLen);
        if (this.guidedNextTouchArrayLen > 0) {
            dest.writeIntArray(this.guidedNextTouchArray);
        }
        this.guidedMaskArrayLen = this.guidedMaskArray.length;
        dest.writeInt(this.guidedMaskArrayLen);
        if (this.guidedMaskArrayLen > 0) {
            dest.writeIntArray(this.guidedMaskArray);
        }
    }
}
