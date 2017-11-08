package com.huawei.thermal.adapter;

import android.content.Context;
import android.telephony.MSimTelephonyManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import com.huawei.thermal.TContext;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class DeviceAdapter {
    private static final boolean DEBUG = true;
    private final Context mContext;
    private final TContext mTContext;

    static {
        if (Log.isLoggable("DeviceAdapter", 2)) {
        }
    }

    public DeviceAdapter(TContext tcontext) {
        this.mContext = tcontext.getContext();
        this.mTContext = tcontext;
    }

    public static int getBatteryLevelFromNode() {
        NumberFormatException ex;
        Throwable th;
        Exception e;
        int batteryLevel = -1;
        File batteryFile = new File("/sys/class/power_supply/Battery", "capacity");
        if (batteryFile == null || !batteryFile.exists()) {
            batteryFile = new File("/sys/class/power_supply/battery", "capacity");
            if (batteryFile == null || !batteryFile.exists()) {
                batteryFile = new File("/sys/class/power_supply/MainBattery", "capacity");
                if (!batteryFile.exists()) {
                    Log.w("DeviceAdapter", "The battery node is not existed!");
                    return -1;
                }
            }
        }
        FileReader fileReader = null;
        BufferedReader bufferedReader = null;
        try {
            FileReader reader = new FileReader(batteryFile);
            try {
                try {
                    BufferedReader buffreader = new BufferedReader(reader);
                    try {
                        String batteryStr = buffreader.readLine();
                        if (batteryStr != null) {
                            batteryLevel = Integer.parseInt(batteryStr.trim());
                        }
                        if (buffreader != null) {
                            try {
                                buffreader.close();
                            } catch (IOException ex2) {
                                Log.w("DeviceAdapter", "IOException ! " + ex2);
                            }
                            bufferedReader = buffreader;
                        } else if (reader == null) {
                            fileReader = reader;
                        } else {
                            try {
                                reader.close();
                            } catch (IOException ex22) {
                                Log.w("DeviceAdapter", "IOException ! " + ex22);
                            }
                        }
                    } catch (NumberFormatException e2) {
                        ex = e2;
                        bufferedReader = buffreader;
                        fileReader = reader;
                        try {
                            Log.w("DeviceAdapter", "NumberFormatException ! " + ex);
                            if (bufferedReader != null) {
                                try {
                                    bufferedReader.close();
                                } catch (IOException ex222) {
                                    Log.w("DeviceAdapter", "IOException ! " + ex222);
                                }
                            } else if (fileReader != null) {
                                try {
                                    fileReader.close();
                                } catch (IOException ex2222) {
                                    Log.w("DeviceAdapter", "IOException ! " + ex2222);
                                }
                            }
                            return batteryLevel;
                        } catch (Throwable th2) {
                            th = th2;
                            if (bufferedReader != null) {
                                try {
                                    bufferedReader.close();
                                } catch (IOException ex22222) {
                                    Log.w("DeviceAdapter", "IOException ! " + ex22222);
                                }
                            } else if (fileReader != null) {
                                try {
                                    fileReader.close();
                                } catch (IOException ex222222) {
                                    Log.w("DeviceAdapter", "IOException ! " + ex222222);
                                }
                            }
                            throw th;
                        }
                    } catch (Exception e3) {
                        e = e3;
                        bufferedReader = buffreader;
                        fileReader = reader;
                        Log.w("DeviceAdapter", "getCurBatteryLevel Exception ! " + e);
                        if (bufferedReader != null) {
                            try {
                                bufferedReader.close();
                            } catch (IOException ex2222222) {
                                Log.w("DeviceAdapter", "IOException ! " + ex2222222);
                            }
                        } else if (fileReader != null) {
                            try {
                                fileReader.close();
                            } catch (IOException ex22222222) {
                                Log.w("DeviceAdapter", "IOException ! " + ex22222222);
                            }
                        }
                        return batteryLevel;
                    } catch (Throwable th3) {
                        th = th3;
                        bufferedReader = buffreader;
                        fileReader = reader;
                        if (bufferedReader != null) {
                            bufferedReader.close();
                        } else if (fileReader != null) {
                            fileReader.close();
                        }
                        throw th;
                    }
                } catch (NumberFormatException e4) {
                    ex = e4;
                    fileReader = reader;
                    Log.w("DeviceAdapter", "NumberFormatException ! " + ex);
                    if (bufferedReader != null) {
                        bufferedReader.close();
                    } else if (fileReader != null) {
                        fileReader.close();
                    }
                    return batteryLevel;
                } catch (Exception e5) {
                    e = e5;
                    fileReader = reader;
                    Log.w("DeviceAdapter", "getCurBatteryLevel Exception ! " + e);
                    if (bufferedReader != null) {
                        bufferedReader.close();
                    } else if (fileReader != null) {
                        fileReader.close();
                    }
                    return batteryLevel;
                } catch (Throwable th4) {
                    th = th4;
                    fileReader = reader;
                    if (bufferedReader != null) {
                        bufferedReader.close();
                    } else if (fileReader != null) {
                        fileReader.close();
                    }
                    throw th;
                }
            } catch (NumberFormatException e6) {
                ex = e6;
                fileReader = reader;
                Log.w("DeviceAdapter", "NumberFormatException ! " + ex);
                if (bufferedReader != null) {
                    bufferedReader.close();
                } else if (fileReader != null) {
                    fileReader.close();
                }
                return batteryLevel;
            } catch (Exception e7) {
                e = e7;
                fileReader = reader;
                Log.w("DeviceAdapter", "getCurBatteryLevel Exception ! " + e);
                if (bufferedReader != null) {
                    bufferedReader.close();
                } else if (fileReader != null) {
                    fileReader.close();
                }
                return batteryLevel;
            } catch (Throwable th5) {
                th = th5;
                fileReader = reader;
                if (bufferedReader != null) {
                    bufferedReader.close();
                } else if (fileReader != null) {
                    fileReader.close();
                }
                throw th;
            }
        } catch (NumberFormatException e8) {
            ex = e8;
            Log.w("DeviceAdapter", "NumberFormatException ! " + ex);
            if (bufferedReader != null) {
                bufferedReader.close();
            } else if (fileReader != null) {
                fileReader.close();
            }
            return batteryLevel;
        } catch (Exception e9) {
            e = e9;
            Log.w("DeviceAdapter", "getCurBatteryLevel Exception ! " + e);
            if (bufferedReader != null) {
                bufferedReader.close();
            } else if (fileReader != null) {
                fileReader.close();
            }
            return batteryLevel;
        }
        return batteryLevel;
    }

    public static boolean isPhoneInCall() {
        if (isMultiSimEnabled()) {
            for (int i = 0; i < MSimTelephonyManager.getDefault().getPhoneCount(); i++) {
                if (MSimTelephonyManager.getDefault().getCallState(i) != 0) {
                    return true;
                }
            }
            return false;
        } else if (TelephonyManager.getDefault().getCallState() == 0) {
            return false;
        } else {
            return true;
        }
    }

    private static boolean isMultiSimEnabled() {
        try {
            return MSimTelephonyManager.getDefault().isMultiSimEnabled();
        } catch (Exception e) {
            Log.w("DeviceAdapter", "CoverManagerService->isMultiSimEnabled->NoExtAPIException!");
            return false;
        }
    }
}
