package com.huawei.systemmanager.spacecleanner.ui;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import com.huawei.systemmanager.comm.database.IDatabaseConst.SqlMarker;
import com.huawei.systemmanager.hsmstat.HsmStat;
import com.huawei.systemmanager.hsmstat.base.HsmStatConst.Events;
import com.huawei.systemmanager.spacecleanner.ui.commonitem.OpenSecondaryParam;
import java.util.HashMap;
import java.util.Map;

public class StatisticalData {
    private static final Map<String, Integer> DELETE_MAP = new HashMap();
    public static final String FRAGMENT_TYPE_EXPAND_LIST = "expand_list";
    public static final String FRAGMENT_TYPE_LIST = "list";
    public static final String FRAGMENT_TYPE_LIST_GRID = "list_grid";
    private static final Map<String, Integer> ITEM_PREVIEW_MAP = new HashMap();
    private static final int MSG_DELETE = 1;
    private static final int MSG_ITEM_PREVIEW = 3;
    private static final int MSG_SELECET_ALL = 2;
    private static final Map<String, Integer> SELECT_MAP = new HashMap();
    private static final String STR_DELETE = "delete:";
    private static final String STR_SELECTALL = "selectAll:";
    private static final String STR_SUBTRASH_TYPE = "subtype:";
    private static final String STR_TITLE = "title:";
    private static final String STR_TRASH_TYPE = "type:";
    private static final String TAG = "StatisticalData";
    private String mFragmentType;
    private Handler mHandler;
    private int mSubTrashType = -1;
    private String mTitle;
    private int mTrashType;

    private class CustomHandler extends Handler {
        public CustomHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            if (msg != null) {
                Integer integer = null;
                StringBuilder builder = StatisticalData.this.createStringBuilder();
                switch (msg.what) {
                    case 1:
                        integer = (Integer) StatisticalData.DELETE_MAP.get(StatisticalData.this.mFragmentType);
                        if (msg.obj instanceof String) {
                            builder.append(SqlMarker.COMMA_SEPARATE).append(StatisticalData.STR_DELETE).append(msg.obj);
                            break;
                        }
                        break;
                    case 2:
                        integer = (Integer) StatisticalData.SELECT_MAP.get(StatisticalData.this.mFragmentType);
                        builder.append(SqlMarker.COMMA_SEPARATE).append(StatisticalData.STR_SELECTALL).append(msg.arg1);
                        break;
                    case 3:
                        integer = (Integer) StatisticalData.ITEM_PREVIEW_MAP.get(StatisticalData.this.mFragmentType);
                        break;
                }
                if (integer != null) {
                    HsmStat.statE(integer.intValue(), builder.toString());
                }
            }
        }
    }

    static {
        DELETE_MAP.put(FRAGMENT_TYPE_LIST, Integer.valueOf(Events.E_OPTMIZE_REPORT_SPACE_MANAGER_LIST_DELETE));
        DELETE_MAP.put(FRAGMENT_TYPE_LIST_GRID, Integer.valueOf(Events.E_OPTMIZE_REPORT_SPACE_MANAGER_LIST_GRID_DELETE));
        DELETE_MAP.put(FRAGMENT_TYPE_EXPAND_LIST, Integer.valueOf(Events.E_OPTMIZE_REPORT_SPACE_MANAGER_EXPAND_LIST_DELETE));
        SELECT_MAP.put(FRAGMENT_TYPE_LIST, Integer.valueOf(Events.E_OPTMIZE_REPORT_SPACE_MANAGER_LIST_SELECTALL));
        SELECT_MAP.put(FRAGMENT_TYPE_LIST_GRID, Integer.valueOf(Events.E_OPTMIZE_REPORT_SPACE_MANAGER_LIST_GRID_SELECTALL));
        SELECT_MAP.put(FRAGMENT_TYPE_EXPAND_LIST, Integer.valueOf(Events.E_OPTMIZE_REPORT_SPACE_MANAGER_EXPAND_LIST_SELECTALL));
        ITEM_PREVIEW_MAP.put(FRAGMENT_TYPE_LIST, Integer.valueOf(Events.E_OPTMIZE_REPORT_SPACE_MANAGER_LIST_ITEM_PREVIEW));
        ITEM_PREVIEW_MAP.put(FRAGMENT_TYPE_LIST_GRID, Integer.valueOf(Events.E_OPTMIZE_REPORT_SPACE_MANAGER_LIST_GRID_ITEM_PREVIEW));
        ITEM_PREVIEW_MAP.put(FRAGMENT_TYPE_EXPAND_LIST, Integer.valueOf(Events.E_OPTMIZE_REPORT_SPACE_MANAGER_EXPAND_LIST_ITEM_PREVIEW));
    }

    public static StatisticalData newInstance(OpenSecondaryParam param, String fragmentType) {
        if (param == null) {
            return null;
        }
        return new StatisticalData(param.getTrashType(), param.getTitleStr(), fragmentType);
    }

    public StatisticalData(int trashType, String title, String fragmentType) {
        this.mTrashType = trashType;
        this.mTitle = title;
        this.mFragmentType = fragmentType;
        HandlerThread workThread = new HandlerThread(TAG);
        workThread.start();
        this.mHandler = new CustomHandler(workThread.getLooper());
    }

    public void destroy() {
        if (this.mHandler != null) {
            this.mHandler.getLooper().quit();
            this.mHandler = null;
        }
    }

    public void sendDeleteMsg(String deleteSize) {
        if (this.mHandler != null) {
            Message msg = this.mHandler.obtainMessage(1);
            msg.obj = deleteSize;
            this.mHandler.sendMessage(msg);
        }
    }

    public void sendSelectAllMsg(boolean value) {
        if (this.mHandler != null) {
            Message msg = this.mHandler.obtainMessage(2);
            msg.arg1 = value ? 1 : 0;
            this.mHandler.sendMessage(msg);
        }
    }

    public void sendItemPreviewMsg() {
        if (this.mHandler != null) {
            this.mHandler.sendMessage(this.mHandler.obtainMessage(3));
        }
    }

    private StringBuilder createStringBuilder() {
        StringBuilder builder = new StringBuilder();
        builder.append(STR_TITLE).append(this.mTitle);
        builder.append(SqlMarker.COMMA_SEPARATE).append(STR_TRASH_TYPE).append(this.mTrashType);
        if (this.mSubTrashType != -1) {
            builder.append(SqlMarker.COMMA_SEPARATE).append(STR_SUBTRASH_TYPE).append(this.mSubTrashType);
        }
        return builder;
    }

    public void setSubTrashType(int type) {
        this.mSubTrashType = type;
    }
}
