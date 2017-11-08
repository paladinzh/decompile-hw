package com.android.settings.deviceinfo;

import android.os.SystemProperties;
import com.huawei.android.manufacture.ProjectMenuCustEx;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;

public class HwCustStatusHwBaseImpl extends HwCustStatusHwBase {
    public String getCustHardwareVersion(String hardWareVersion) {
        if (SystemProperties.getBoolean("ro.config.hw_version_app_info", false)) {
            return readHwHardWareVersion();
        }
        return hardWareVersion;
    }

    private static String readHwHardWareVersion() {
        Exception e;
        Throwable th;
        String str = null;
        BufferedReader bufferedReader = null;
        FileInputStream fileInputStream = null;
        String[] tokens = null;
        try {
            FileInputStream fis = new FileInputStream("/proc/app_info");
            try {
                BufferedReader in = new BufferedReader(new InputStreamReader(fis, "UTF-8"));
                do {
                    try {
                        String strName = in.readLine();
                        if (strName == null) {
                            break;
                        }
                        tokens = strName.split(":");
                        if (tokens.length == 0) {
                            if (in != null) {
                                try {
                                    in.close();
                                } catch (Exception e1) {
                                    e1.printStackTrace();
                                }
                            }
                            if (fis != null) {
                                try {
                                    fis.close();
                                } catch (Exception e2) {
                                    e2.printStackTrace();
                                }
                            }
                            return null;
                        }
                    } catch (Exception e3) {
                        e2 = e3;
                        fileInputStream = fis;
                        bufferedReader = in;
                    } catch (Throwable th2) {
                        th = th2;
                        fileInputStream = fis;
                        bufferedReader = in;
                    }
                } while ("huawei_hardware_version".compareToIgnoreCase(tokens[0].trim()) != 0);
                if (tokens != null && tokens.length > 1) {
                    str = tokens[1] != null ? tokens[1].trim() : null;
                }
                if (in != null) {
                    try {
                        in.close();
                    } catch (Exception e12) {
                        e12.printStackTrace();
                    }
                }
                if (fis != null) {
                    try {
                        fis.close();
                    } catch (Exception e22) {
                        e22.printStackTrace();
                    }
                }
                bufferedReader = in;
            } catch (Exception e4) {
                e22 = e4;
                fileInputStream = fis;
                try {
                    e22.printStackTrace();
                    if (bufferedReader != null) {
                        try {
                            bufferedReader.close();
                        } catch (Exception e122) {
                            e122.printStackTrace();
                        }
                    }
                    if (fileInputStream != null) {
                        try {
                            fileInputStream.close();
                        } catch (Exception e222) {
                            e222.printStackTrace();
                        }
                    }
                    if (str == null) {
                        str = ProjectMenuCustEx.getVersionAndTime(4);
                    }
                    return str;
                } catch (Throwable th3) {
                    th = th3;
                    if (bufferedReader != null) {
                        try {
                            bufferedReader.close();
                        } catch (Exception e1222) {
                            e1222.printStackTrace();
                        }
                    }
                    if (fileInputStream != null) {
                        try {
                            fileInputStream.close();
                        } catch (Exception e2222) {
                            e2222.printStackTrace();
                        }
                    }
                    throw th;
                }
            } catch (Throwable th4) {
                th = th4;
                fileInputStream = fis;
                if (bufferedReader != null) {
                    bufferedReader.close();
                }
                if (fileInputStream != null) {
                    fileInputStream.close();
                }
                throw th;
            }
        } catch (Exception e5) {
            e2222 = e5;
            e2222.printStackTrace();
            if (bufferedReader != null) {
                bufferedReader.close();
            }
            if (fileInputStream != null) {
                fileInputStream.close();
            }
            if (str == null) {
                str = ProjectMenuCustEx.getVersionAndTime(4);
            }
            return str;
        }
        if (str == null) {
            str = ProjectMenuCustEx.getVersionAndTime(4);
        }
        return str;
    }
}
