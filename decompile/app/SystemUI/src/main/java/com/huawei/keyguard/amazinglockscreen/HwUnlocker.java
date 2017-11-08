package com.huawei.keyguard.amazinglockscreen;

import android.content.Context;
import android.graphics.Rect;
import com.android.keyguard.hwlockscreen.HwUnlockInterface$VisibilityCallback;
import com.huawei.keyguard.HwUnlockConstants$ViewPropertyType;
import com.huawei.keyguard.util.HwLog;
import java.util.ArrayList;

public class HwUnlocker implements HwUnlockInterface$VisibilityCallback {
    private Context mContext;
    private ArrayList<EndPoint> mEndPointList = new ArrayList();
    private int mMaxMove_X;
    private int mMaxMove_Y;
    private int mMinMove_X;
    private int mMinMove_Y;
    private String mName;
    private Rect mStartPointRect;
    private int mState = 0;
    private boolean mVisible = true;
    private HwViewProperty mVisiblity;

    public static class EndPoint {
        private String mIntentType;
        private int mPath_x;
        private int mPath_y;
        private Rect mRect;

        public EndPoint(Rect rect) {
            this.mRect = rect;
        }

        public String getIntentType() {
            return this.mIntentType;
        }

        public void setIntentType(String type) {
            this.mIntentType = type;
        }

        public Rect getRect() {
            return this.mRect;
        }

        public void setPath(int pathX, int pathY) {
            this.mPath_x = pathX;
            this.mPath_y = pathY;
        }

        public int getPathX() {
            return this.mPath_x;
        }

        public int getPathY() {
            return this.mPath_y;
        }
    }

    public int getMaxMoveX() {
        for (EndPoint endpoint : this.mEndPointList) {
            if (this.mMaxMove_X < endpoint.getPathX()) {
                this.mMaxMove_X = endpoint.getPathX();
            }
        }
        HwLog.i("getMaxMoveX", "mytest onTouchEvent getMaxMoveX  mMaxMove_X=" + this.mMaxMove_X);
        return this.mMaxMove_X;
    }

    public int getMaxMoveY() {
        for (EndPoint endpoint : this.mEndPointList) {
            if (this.mMaxMove_Y < endpoint.getPathY()) {
                this.mMaxMove_Y = endpoint.getPathY();
            }
        }
        return this.mMaxMove_Y;
    }

    public int getMinMoveX() {
        for (EndPoint endpoint : this.mEndPointList) {
            if (this.mMinMove_X > endpoint.getPathX()) {
                this.mMinMove_X = endpoint.getPathX();
            }
        }
        return this.mMinMove_X;
    }

    public int getMinMoveY() {
        for (EndPoint endpoint : this.mEndPointList) {
            if (this.mMinMove_Y > endpoint.getPathY()) {
                this.mMinMove_Y = endpoint.getPathY();
            }
        }
        return this.mMinMove_Y;
    }

    public HwUnlocker(Context context) {
        this.mContext = context;
    }

    public void setVisiblityProp(String visible) {
        this.mVisiblity = new HwViewProperty(this.mContext, visible, HwUnlockConstants$ViewPropertyType.TYPE_VISIBILITY, this);
        refreshVisibility(((Boolean) this.mVisiblity.getValue()).booleanValue());
    }

    public boolean getVisible() {
        return this.mVisible;
    }

    public void setName(String name) {
        this.mName = name;
    }

    public String getName() {
        return this.mName;
    }

    public void setStartPointRect(Rect rect) {
        this.mStartPointRect = rect;
    }

    public Rect getStartPointRect() {
        return this.mStartPointRect;
    }

    public void addEndPoint(EndPoint rect) {
        this.mEndPointList.add(rect);
    }

    public ArrayList<EndPoint> getEndPoint() {
        return this.mEndPointList;
    }

    public void refreshVisibility(boolean visible) {
        this.mVisible = visible;
    }
}
