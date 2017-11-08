package com.huawei.systemmanager.optimize.monitor;

import com.huawei.systemmanager.util.HwLog;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public final class CPUInfoManager {
    private static final int BUFFSIZE = 8192;
    public static final String IDLE_CPU_TIME = "idlecputime";
    private static final int IDLE_CPU_TIME_COLUMN = 4;
    private static final String TAG = "CPUInfoManager";
    public static final String TOTAL_CPU_TIME = "totalcputime";

    public Map<String, Integer> getCpuInfo() {
        Throwable th;
        Exception ex;
        InputStreamReader inputStreamReader = null;
        BufferedReader bufferedReader = null;
        Map<String, Integer> cpuInfo = new HashMap();
        try {
            InputStreamReader localCpuinfoReader = new InputStreamReader(new FileInputStream(MemoryCpuConst.CPUINFOFILE), "utf-8");
            try {
                BufferedReader localBufferedReader = new BufferedReader(localCpuinfoReader, 8192);
                try {
                    String oneRow = localBufferedReader.readLine();
                    if (oneRow != null) {
                        String[] dataArray = oneRow.split("\\s+");
                        int totalCpuTime = 0;
                        for (int i = 1; i < dataArray.length; i++) {
                            totalCpuTime += Integer.parseInt(dataArray[i]);
                        }
                        cpuInfo.put(TOTAL_CPU_TIME, Integer.valueOf(totalCpuTime));
                        cpuInfo.put(IDLE_CPU_TIME, Integer.valueOf(dataArray[4]));
                    } else {
                        cpuInfo.put(TOTAL_CPU_TIME, Integer.valueOf(0));
                        cpuInfo.put(IDLE_CPU_TIME, Integer.valueOf(0));
                    }
                    if (localBufferedReader != null) {
                        try {
                            localBufferedReader.close();
                        } catch (IOException e) {
                            HwLog.e(TAG, "getCpuInfo() localBufferedReader generate exception finally block");
                        }
                    }
                    if (localCpuinfoReader != null) {
                        try {
                            localCpuinfoReader.close();
                        } catch (IOException e2) {
                            HwLog.e(TAG, "getCpuInfo() localCpuinfoReader generate exception finally block");
                        }
                    }
                    inputStreamReader = localCpuinfoReader;
                } catch (IOException e3) {
                    bufferedReader = localBufferedReader;
                    inputStreamReader = localCpuinfoReader;
                    try {
                        HwLog.e(TAG, "getCpuInfo() generate IOException");
                        if (bufferedReader != null) {
                            try {
                                bufferedReader.close();
                            } catch (IOException e4) {
                                HwLog.e(TAG, "getCpuInfo() localBufferedReader generate exception finally block");
                            }
                        }
                        if (inputStreamReader != null) {
                            try {
                                inputStreamReader.close();
                            } catch (IOException e5) {
                                HwLog.e(TAG, "getCpuInfo() localCpuinfoReader generate exception finally block");
                            }
                        }
                        return cpuInfo;
                    } catch (Throwable th2) {
                        th = th2;
                        if (bufferedReader != null) {
                            try {
                                bufferedReader.close();
                            } catch (IOException e6) {
                                HwLog.e(TAG, "getCpuInfo() localBufferedReader generate exception finally block");
                            }
                        }
                        if (inputStreamReader != null) {
                            try {
                                inputStreamReader.close();
                            } catch (IOException e7) {
                                HwLog.e(TAG, "getCpuInfo() localCpuinfoReader generate exception finally block");
                            }
                        }
                        throw th;
                    }
                } catch (Exception e8) {
                    ex = e8;
                    bufferedReader = localBufferedReader;
                    inputStreamReader = localCpuinfoReader;
                    HwLog.e(TAG, "getCpuInfo() generate Other Exception" + ex.toString());
                    if (bufferedReader != null) {
                        try {
                            bufferedReader.close();
                        } catch (IOException e9) {
                            HwLog.e(TAG, "getCpuInfo() localBufferedReader generate exception finally block");
                        }
                    }
                    if (inputStreamReader != null) {
                        try {
                            inputStreamReader.close();
                        } catch (IOException e10) {
                            HwLog.e(TAG, "getCpuInfo() localCpuinfoReader generate exception finally block");
                        }
                    }
                    return cpuInfo;
                } catch (Throwable th3) {
                    th = th3;
                    bufferedReader = localBufferedReader;
                    inputStreamReader = localCpuinfoReader;
                    if (bufferedReader != null) {
                        bufferedReader.close();
                    }
                    if (inputStreamReader != null) {
                        inputStreamReader.close();
                    }
                    throw th;
                }
            } catch (IOException e11) {
                inputStreamReader = localCpuinfoReader;
                HwLog.e(TAG, "getCpuInfo() generate IOException");
                if (bufferedReader != null) {
                    bufferedReader.close();
                }
                if (inputStreamReader != null) {
                    inputStreamReader.close();
                }
                return cpuInfo;
            } catch (Exception e12) {
                ex = e12;
                inputStreamReader = localCpuinfoReader;
                HwLog.e(TAG, "getCpuInfo() generate Other Exception" + ex.toString());
                if (bufferedReader != null) {
                    bufferedReader.close();
                }
                if (inputStreamReader != null) {
                    inputStreamReader.close();
                }
                return cpuInfo;
            } catch (Throwable th4) {
                th = th4;
                inputStreamReader = localCpuinfoReader;
                if (bufferedReader != null) {
                    bufferedReader.close();
                }
                if (inputStreamReader != null) {
                    inputStreamReader.close();
                }
                throw th;
            }
        } catch (IOException e13) {
            HwLog.e(TAG, "getCpuInfo() generate IOException");
            if (bufferedReader != null) {
                bufferedReader.close();
            }
            if (inputStreamReader != null) {
                inputStreamReader.close();
            }
            return cpuInfo;
        } catch (Exception e14) {
            ex = e14;
            HwLog.e(TAG, "getCpuInfo() generate Other Exception" + ex.toString());
            if (bufferedReader != null) {
                bufferedReader.close();
            }
            if (inputStreamReader != null) {
                inputStreamReader.close();
            }
            return cpuInfo;
        }
        return cpuInfo;
    }
}
