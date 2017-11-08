package com.huawei.powergenie.integration.adapter;

import android.content.Context;
import android.util.Log;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AlarmAdapter {
    private static AlarmAdapter sInstance;
    private Set<String> mFreezingPkgsAlarms = new HashSet();
    private Object mHwAlarmMgrEx = null;
    private Set<String> mPendingPkgsAlarms = new HashSet();

    public static AlarmAdapter getInstance(Context context) {
        AlarmAdapter alarmAdapter;
        synchronized (AlarmAdapter.class) {
            if (sInstance == null) {
                sInstance = new AlarmAdapter(context);
            }
            alarmAdapter = sInstance;
        }
        return alarmAdapter;
    }

    private AlarmAdapter(Context context) {
        loadAlarmMgrClass(context);
    }

    public synchronized boolean pendingAppAlarms(List<String> pkgList, boolean reasonFrz) {
        if (reasonFrz) {
            this.mFreezingPkgsAlarms.addAll(pkgList);
        } else {
            this.mPendingPkgsAlarms.addAll(pkgList);
        }
        return setAlarmsPending(pkgList, true, 0);
    }

    public synchronized boolean unpendingAppAlarms(List<String> pkgList, boolean reasonFrz) {
        List<String> unpendingPkgs;
        unpendingPkgs = new ArrayList();
        if (reasonFrz) {
            this.mFreezingPkgsAlarms.removeAll(pkgList);
            for (String pkg : pkgList) {
                if (this.mPendingPkgsAlarms.contains(pkg)) {
                    Log.i("AlarmAdapter", "for restrict, not unpending:" + pkg);
                } else {
                    unpendingPkgs.add(pkg);
                }
            }
        } else {
            this.mPendingPkgsAlarms.removeAll(pkgList);
            for (String pkg2 : pkgList) {
                if (this.mFreezingPkgsAlarms.contains(pkg2)) {
                    Log.i("AlarmAdapter", "for frz, not unpending:" + pkg2);
                } else {
                    unpendingPkgs.add(pkg2);
                }
            }
        }
        return setAlarmsPending(unpendingPkgs, false, 0);
    }

    private boolean setAlarmsPending(List<String> pkg, boolean pending, int alarmType) {
        if (pkg == null) {
            return false;
        }
        if (setAlarmsPendingSupportActions(pkg, null, pending, alarmType)) {
            return true;
        }
        if (this.mHwAlarmMgrEx != null) {
            try {
                this.mHwAlarmMgrEx.getClass().getMethod("setAlarmsPending", new Class[]{List.class, Boolean.TYPE, Integer.TYPE}).invoke(this.mHwAlarmMgrEx, new Object[]{pkg, Boolean.valueOf(pending), Integer.valueOf(alarmType)});
                return true;
            } catch (NoSuchMethodException e) {
                Log.w("AlarmAdapter", "Method setAlarmsPending does not exists !");
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }
        return false;
    }

    public boolean unpendingAllAlarms() {
        if (this.mHwAlarmMgrEx != null) {
            try {
                this.mHwAlarmMgrEx.getClass().getMethod("removeAllPendingAlarms", new Class[0]).invoke(this.mHwAlarmMgrEx, new Object[0]);
                return true;
            } catch (NoSuchMethodException e) {
                Log.w("AlarmAdapter", "Method removeAllPendingAlarms does not exists !");
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }
        return false;
    }

    public boolean periodAdjustAlarms(List<String> pkgList, int type, long interval, int mode) {
        return setAlarmsAdjust(pkgList, true, type, interval, mode);
    }

    public boolean removePeriodAdjustAlarms(List<String> pkgList, int type) {
        return setAlarmsAdjust(pkgList, false, type, -1, -1);
    }

    public boolean removeAllPeriodAdjustAlarms() {
        return removeAllAdjustAlarms();
    }

    private boolean setAlarmsAdjust(List<String> pkgList, boolean adjust, int type, long interval, int mode) {
        if (pkgList == null || pkgList.size() <= 0) {
            Log.w("AlarmAdapter", "setAlarmsAdjust, pkgList is null !");
            return false;
        } else if (adjust && interval <= 0) {
            Log.w("AlarmAdapter", "Cannot to adjust alarm in this interval = " + interval);
            return false;
        } else if (setAlarmsAdjustSupportActions(pkgList, null, adjust, type, interval, mode)) {
            return true;
        } else {
            if (this.mHwAlarmMgrEx != null) {
                try {
                    this.mHwAlarmMgrEx.getClass().getMethod("setAlarmsAdjust", new Class[]{List.class, Boolean.TYPE, Integer.TYPE, Long.TYPE, Integer.TYPE}).invoke(this.mHwAlarmMgrEx, new Object[]{pkgList, Boolean.valueOf(adjust), Integer.valueOf(type), Long.valueOf(interval), Integer.valueOf(mode)});
                    return true;
                } catch (NoSuchMethodException e) {
                    Log.w("AlarmAdapter", "Method setAlarmsAdjust does not exists !");
                } catch (Exception e2) {
                    e2.printStackTrace();
                }
            }
            return false;
        }
    }

    private boolean removeAllAdjustAlarms() {
        if (this.mHwAlarmMgrEx != null) {
            try {
                this.mHwAlarmMgrEx.getClass().getMethod("removeAllAdjustAlarms", new Class[0]).invoke(this.mHwAlarmMgrEx, new Object[0]);
                return true;
            } catch (NoSuchMethodException e) {
                Log.w("AlarmAdapter", "Method removeAllAdjustAlarms does not exists !");
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }
        return false;
    }

    private boolean setAlarmsPendingSupportActions(List<String> pkg, List<String> actionList, boolean pending, int alarmType) {
        if (!(pkg == null || this.mHwAlarmMgrEx == null)) {
            try {
                this.mHwAlarmMgrEx.getClass().getMethod("setAlarmsPending", new Class[]{List.class, List.class, Boolean.TYPE, Integer.TYPE}).invoke(this.mHwAlarmMgrEx, new Object[]{pkg, actionList, Boolean.valueOf(pending), Integer.valueOf(alarmType)});
                return true;
            } catch (NoSuchMethodException e) {
                Log.w("AlarmAdapter", "setAlarmsPending For Actions Method does not exists !");
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }
        return false;
    }

    private boolean setAlarmsAdjustSupportActions(List<String> pkgList, List<String> actionList, boolean adjust, int type, long interval, int mode) {
        if (pkgList == null || pkgList.size() <= 0) {
            Log.w("AlarmAdapter", "setAlarmsAdjustForActions, pkgList is null !");
            return false;
        } else if (!adjust || interval > 0) {
            if (this.mHwAlarmMgrEx != null) {
                try {
                    this.mHwAlarmMgrEx.getClass().getMethod("setAlarmsAdjust", new Class[]{List.class, List.class, Boolean.TYPE, Integer.TYPE, Long.TYPE, Integer.TYPE}).invoke(this.mHwAlarmMgrEx, new Object[]{pkgList, actionList, Boolean.valueOf(adjust), Integer.valueOf(type), Long.valueOf(interval), Integer.valueOf(mode)});
                    return true;
                } catch (NoSuchMethodException e) {
                    Log.w("AlarmAdapter", "Method setAlarmsAdjust For Actions does not exists !");
                } catch (Exception e2) {
                    e2.printStackTrace();
                }
            }
            return false;
        } else {
            Log.w("AlarmAdapter", "Cannot to adjust alarm in this interval = " + interval);
            return false;
        }
    }

    private void loadAlarmMgrClass(Context context) {
        if (this.mHwAlarmMgrEx == null) {
            try {
                Class clazz = context.getClassLoader().loadClass("huawei.android.app.HwAlarmManagerEx");
                this.mHwAlarmMgrEx = clazz.getMethod("getInstance", new Class[0]).invoke(clazz, new Object[0]);
            } catch (ClassNotFoundException e) {
                Log.w("AlarmAdapter", "Class huawei.android.app.HwAlarmManagerEx does not exists");
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }
    }
}
