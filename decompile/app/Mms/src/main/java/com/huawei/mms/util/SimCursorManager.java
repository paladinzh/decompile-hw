package com.huawei.mms.util;

import android.database.Cursor;
import com.huawei.cspcommon.MLog;

public class SimCursorManager {
    private static SimCursorManager mInst = new SimCursorManager();
    private boolean mSimInit = false;
    private boolean mSimInit1 = false;
    private boolean mSimInit2 = false;
    private boolean mSimInitFinished = false;
    private boolean mSimInitFinished1 = false;
    private boolean mSimInitFinished2 = false;
    public Cursor mSimcursor = null;
    public Cursor mSimcursor1 = null;
    public Cursor mSimcursor2 = null;

    public static SimCursorManager self() {
        return mInst;
    }

    public void clearCursor() {
        this.mSimInit = false;
        this.mSimInitFinished = false;
        closeSimCursor(this.mSimcursor);
        this.mSimcursor = null;
    }

    public void clearCursor(int slot) {
        if (slot == 1) {
            this.mSimInit1 = false;
            this.mSimInitFinished1 = false;
            closeSimCursor(this.mSimcursor1);
            this.mSimcursor1 = null;
        } else if (slot == 2) {
            this.mSimInit2 = false;
            this.mSimInitFinished2 = false;
            closeSimCursor(this.mSimcursor2);
            this.mSimcursor2 = null;
        }
    }

    private void closeSimCursor(final Cursor cursor) {
        if (cursor != null && !cursor.isClosed()) {
            HwBackgroundLoader.getUIHandler().post(new Runnable() {
                public void run() {
                    try {
                        cursor.close();
                    } catch (Exception e) {
                        MLog.e("SimCursorManager", e.getMessage());
                    }
                }
            });
        }
    }
}
