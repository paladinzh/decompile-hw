package com.android.server.rms.test;

import android.os.SystemClock;
import android.rms.iaware.AppTypeRecoManager;
import com.android.server.rms.algorithm.AwareUserHabit;
import java.io.PrintWriter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import junit.framework.Assert;

public final class TestAwareUserHabit extends Assert {
    private static final int MINCOUNT = 3;
    private static final String TAG = "AwareUserHabit";
    private static final int TOPMAX = 10000;
    private static final int TOPN = 5;

    public static final void testAwareUserHabit(PrintWriter pw, String[] args) {
        if (pw != null && args != null) {
            AwareUserHabit userHabit = AwareUserHabit.getInstance();
            if (userHabit == null) {
                pw.println("user habit is not ready");
            } else if (userHabit.isEnable()) {
                boolean isGetPGProtectList = false;
                boolean isGetAppListByType = false;
                boolean isGetAppType = false;
                int i = 0;
                int length = args.length;
                while (i < length) {
                    String arg = args[i];
                    List<String> result;
                    if (isGetPGProtectList) {
                        try {
                            result = userHabit.getForceProtectApps(Integer.parseInt(arg));
                            if (result == null || result.size() <= 0) {
                                pw.println("invald input value or not used data");
                            } else {
                                pw.println(result.toString());
                            }
                            return;
                        } catch (NumberFormatException e) {
                            pw.println("Bad input value: " + arg);
                            return;
                        }
                    } else if ("userTrack".equals(arg)) {
                        Map<String, String> result2 = userHabit.getUserTrackAppSortDumpInfo();
                        if (result2 != null) {
                            pw.println(result2.toString());
                        } else {
                            pw.println("result is null");
                        }
                        return;
                    } else if ("habitProtectList".equals(arg)) {
                        userHabit.dumpHabitProtectList(pw);
                        return;
                    } else if ("getHabitProtectList".equals(arg)) {
                        result = userHabit.getHabitProtectList(10000, 10000);
                        if (result != null) {
                            pw.println(result.toString());
                        } else {
                            pw.println("result is null");
                        }
                        return;
                    } else if ("getHabitProtectListAll".equals(arg)) {
                        result = userHabit.getHabitProtectListAll(10000, 10000);
                        if (result != null) {
                            pw.println(result.toString());
                        } else {
                            pw.println("result is null");
                        }
                        return;
                    } else if ("getMostUsed".equals(arg)) {
                        result = userHabit.getMostFrequentUsedApp(5, 3);
                        if (result != null) {
                            pw.println(result.toString());
                        } else {
                            pw.println("result is null");
                        }
                        return;
                    } else if ("getTopN".equals(arg)) {
                        result = userHabit.getTopN(5);
                        if (result != null) {
                            pw.println(result.toString());
                        } else {
                            pw.println("result is null");
                        }
                        return;
                    } else if ("getLastPkgName".equals(arg)) {
                        pw.println(userHabit.getLastPkgName());
                        return;
                    } else {
                        if ("PGProtectList".equals(arg)) {
                            isGetPGProtectList = true;
                        } else if ("getLongTimeRunningApps".equals(arg)) {
                            result = userHabit.recognizeLongTimeRunningApps();
                            if (result != null) {
                                pw.println(result);
                            } else {
                                pw.println("all the apps are used recently");
                            }
                            return;
                        } else if ("getLruCache".equals(arg)) {
                            LinkedHashMap<String, Long> result3 = userHabit.getLruCache();
                            if (result3 != null) {
                                long now = SystemClock.elapsedRealtime();
                                StringBuffer s = new StringBuffer();
                                s.append("pkgName:      backgroundTime:\n");
                                for (Entry entry : result3.entrySet()) {
                                    s.append((String) entry.getKey()).append("    ").append(String.valueOf((now - ((Long) entry.getValue()).longValue()) / 1000)).append("s \n");
                                }
                                pw.println(s.toString());
                            } else {
                                pw.println("result is null");
                            }
                            return;
                        } else if ("getClockTypeAppList".equals(arg)) {
                            pw.println(AppTypeRecoManager.getInstance().getAlarmApps());
                            return;
                        } else if ("getFilterApp".equals(arg)) {
                            result = userHabit.getFilterApp();
                            if (result != null) {
                                pw.println(result);
                            } else {
                                pw.println("result is null");
                            }
                            return;
                        } else if ("getGCMAppList".equals(arg)) {
                            result = userHabit.getGCMAppList();
                            if (result != null) {
                                pw.println(result);
                            } else {
                                pw.println("result is null");
                            }
                            return;
                        } else if ("getAppListByType".equals(arg)) {
                            isGetAppListByType = true;
                        } else if (isGetAppListByType) {
                            try {
                                result = AppTypeRecoManager.getInstance().getAppsByType(Integer.parseInt(arg));
                                if (result.size() > 0) {
                                    pw.println(result.toString());
                                } else {
                                    pw.println("invald input value or not used data");
                                }
                                return;
                            } catch (NumberFormatException e2) {
                                pw.println("Bad input value: " + arg);
                                return;
                            }
                        } else if ("getAppType".equals(arg)) {
                            isGetAppType = true;
                        } else if (isGetAppType) {
                            pw.println("pkgname:" + arg + " type:" + AppTypeRecoManager.getInstance().getAppType(arg));
                            return;
                        }
                        i++;
                    }
                }
            } else {
                pw.println("user habit is not enable");
            }
        }
    }
}
