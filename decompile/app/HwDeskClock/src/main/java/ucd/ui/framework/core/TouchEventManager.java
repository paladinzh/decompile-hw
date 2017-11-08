package ucd.ui.framework.core;

import android.os.SystemClock;
import android.view.MotionEvent;

public class TouchEventManager {
    private static float[] mTouchPosition = null;
    private static GLObject mTouchTarget = null;
    private GLObject currentObj;

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public final synchronized boolean dispatchTouchEvent(GLBase root, MotionEvent event) {
        if (event.getPointerCount() > 1) {
            return false;
        }
        boolean z = false;
        if (event.getAction() != 0) {
            if (event.getAction() == 1 || event.getAction() == 3) {
                root.resetTouchState();
            }
            if (!(mTouchTarget == null || this.currentObj == null)) {
                z = this.currentObj.dispatchTouchEvent(event);
            }
            if (!z) {
                z = handleTouch(root, event);
            }
        } else {
            resetTouchState();
            if (!root.getDisAllowInterceptTouchEvent()) {
                z = root.interceptTouchEvent(event);
                if (z) {
                    this.currentObj = null;
                    return handleTouch(root, event);
                }
            }
            mTouchPosition = new float[]{event.getX(), event.getY()};
            this.currentObj = ChildrenUtil._findByPos(root.list, mTouchPosition, false, false);
            if (this.currentObj != null) {
                z = this.currentObj.dispatchTouchEvent(event);
            }
            if (!z) {
                z = handleTouch(root, event);
            }
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public final synchronized boolean dispatchTouchEvent(Group obj, MotionEvent event) {
        if (event.getPointerCount() > 1) {
            return false;
        }
        boolean flag = false;
        if (!obj.getDisAllowInterceptTouchEvent()) {
            flag = obj.interceptTouchEvent(event);
            if (flag) {
                if (!(obj.equals(mTouchTarget) || mTouchTarget == null)) {
                    long now = SystemClock.uptimeMillis();
                    MotionEvent cancelEvent = MotionEvent.obtain(now, now, 3, 0.0f, 0.0f, 0);
                    event.setSource(4098);
                    handleTouch(mTouchTarget, cancelEvent);
                    cancelEvent.recycle();
                }
                mTouchTarget = obj;
                flag = handleTouch((GLObject) obj, event);
            }
        }
        if (obj.equals(mTouchTarget)) {
            flag = handleTouch((GLObject) obj, event);
        } else {
            if (event.getAction() == 0) {
                this.currentObj = ChildrenUtil._findByPos(obj.list, mTouchPosition, false, false);
            }
            if (event.getAction() == 1 || event.getAction() == 3) {
                obj.resetTouchState();
            }
            if (this.currentObj != null) {
                flag = this.currentObj.dispatchTouchEvent(event);
            }
            if (!flag && mTouchTarget == null) {
                flag = handleTouch((GLObject) obj, event);
            }
        }
    }

    public boolean dispatchTouchEvent(GLObject obj, MotionEvent event) {
        if (obj == null) {
            return false;
        }
        if (obj instanceof Group) {
            return dispatchTouchEvent((Group) obj, event);
        }
        return handleTouch(obj, event);
    }

    private boolean handleTouch(GLBase obj, MotionEvent event) {
        boolean flag = false;
        if (obj.onTouchListener != null && obj.isEnabled()) {
            flag = obj.onTouchListener.onTouch(obj, event);
        }
        if (flag) {
            return flag;
        }
        return obj.onTouchEvent(event);
    }

    private boolean handleTouch(GLObject obj, MotionEvent event) {
        if (obj == null || event == null) {
            return false;
        }
        boolean flag = false;
        int action = event.getAction();
        if (obj.onTouchListener != null && obj.isEnabled()) {
            flag = obj.onTouchListener.onTouch(event);
        }
        if (!flag) {
            flag = obj.onTouchEvent(event);
        }
        if (flag && action == 0) {
            mTouchTarget = obj;
        }
        return flag;
    }

    private void resetTouchState() {
        mTouchTarget = null;
        mTouchPosition = null;
    }
}
