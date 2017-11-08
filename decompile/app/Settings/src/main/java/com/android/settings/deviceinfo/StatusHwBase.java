package com.android.settings.deviceinfo;

import android.provider.Settings.System;
import android.support.v7.preference.Preference;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import com.android.settings.SettingsPreferenceFragment;
import com.huawei.cust.HwCustUtils;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

public class StatusHwBase {
    private static HwCustStatusHwBase sHwCustStatusHwBase = ((HwCustStatusHwBase) HwCustUtils.createObj(HwCustStatusHwBase.class, new Object[0]));

    public static void initExtralPreferences(SettingsPreferenceFragment context) {
        setImsiStatus(context);
        setHardWareVersionStatus(context);
    }

    private static void setSummaryText(SettingsPreferenceFragment context, String preference, String text) {
        if (TextUtils.isEmpty(text)) {
            CharSequence text2 = context.getResources().getString(2131624355);
        }
        if (context.findPreference(preference) != null) {
            context.findPreference(preference).setSummary(text2);
        }
    }

    public static String readHardWareVersion() {
        FileNotFoundException e;
        UnsupportedEncodingException e2;
        IOException e3;
        Throwable th;
        String strValue = null;
        BufferedReader bufferedReader = null;
        FileInputStream fileInputStream = null;
        try {
            FileInputStream fis = new FileInputStream("/proc/app_info");
            try {
                BufferedReader in = new BufferedReader(new InputStreamReader(fis, "UTF-8"));
                String strName;
                do {
                    try {
                        strName = in.readLine();
                        if (strName == null) {
                            break;
                        }
                    } catch (FileNotFoundException e4) {
                        e = e4;
                        fileInputStream = fis;
                        bufferedReader = in;
                    } catch (UnsupportedEncodingException e5) {
                        e2 = e5;
                        fileInputStream = fis;
                        bufferedReader = in;
                    } catch (IOException e6) {
                        e3 = e6;
                        fileInputStream = fis;
                        bufferedReader = in;
                    } catch (Throwable th2) {
                        th = th2;
                        fileInputStream = fis;
                        bufferedReader = in;
                    }
                } while (strName.compareToIgnoreCase("board_id:") != 0);
                strValue = in.readLine();
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException e32) {
                        e32.printStackTrace();
                    }
                }
                if (fis != null) {
                    try {
                        fis.close();
                    } catch (IOException e322) {
                        e322.printStackTrace();
                    }
                }
                bufferedReader = in;
            } catch (FileNotFoundException e7) {
                e = e7;
                fileInputStream = fis;
                e.printStackTrace();
                if (bufferedReader != null) {
                    try {
                        bufferedReader.close();
                    } catch (IOException e3222) {
                        e3222.printStackTrace();
                    }
                }
                if (fileInputStream != null) {
                    try {
                        fileInputStream.close();
                    } catch (IOException e32222) {
                        e32222.printStackTrace();
                    }
                }
                return strValue;
            } catch (UnsupportedEncodingException e8) {
                e2 = e8;
                fileInputStream = fis;
                e2.printStackTrace();
                if (bufferedReader != null) {
                    try {
                        bufferedReader.close();
                    } catch (IOException e322222) {
                        e322222.printStackTrace();
                    }
                }
                if (fileInputStream != null) {
                    try {
                        fileInputStream.close();
                    } catch (IOException e3222222) {
                        e3222222.printStackTrace();
                    }
                }
                return strValue;
            } catch (IOException e9) {
                e3222222 = e9;
                fileInputStream = fis;
                try {
                    e3222222.printStackTrace();
                    if (bufferedReader != null) {
                        try {
                            bufferedReader.close();
                        } catch (IOException e32222222) {
                            e32222222.printStackTrace();
                        }
                    }
                    if (fileInputStream != null) {
                        try {
                            fileInputStream.close();
                        } catch (IOException e322222222) {
                            e322222222.printStackTrace();
                        }
                    }
                    return strValue;
                } catch (Throwable th3) {
                    th = th3;
                    if (bufferedReader != null) {
                        try {
                            bufferedReader.close();
                        } catch (IOException e3222222222) {
                            e3222222222.printStackTrace();
                        }
                    }
                    if (fileInputStream != null) {
                        try {
                            fileInputStream.close();
                        } catch (IOException e32222222222) {
                            e32222222222.printStackTrace();
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
        } catch (FileNotFoundException e10) {
            e = e10;
            e.printStackTrace();
            if (bufferedReader != null) {
                bufferedReader.close();
            }
            if (fileInputStream != null) {
                fileInputStream.close();
            }
            return strValue;
        } catch (UnsupportedEncodingException e11) {
            e2 = e11;
            e2.printStackTrace();
            if (bufferedReader != null) {
                bufferedReader.close();
            }
            if (fileInputStream != null) {
                fileInputStream.close();
            }
            return strValue;
        } catch (IOException e12) {
            e32222222222 = e12;
            e32222222222.printStackTrace();
            if (bufferedReader != null) {
                bufferedReader.close();
            }
            if (fileInputStream != null) {
                fileInputStream.close();
            }
            return strValue;
        }
        return strValue;
    }

    private static void setImsiStatus(SettingsPreferenceFragment context) {
        int hw_imsi_enabled = System.getInt(context.getContext().getContentResolver(), "hw_imsi_enabled", 0);
        Preference pref = context.findPreference("imsi");
        if (1 == hw_imsi_enabled) {
            setSummaryText(context, "imsi", ((TelephonyManager) context.getContext().getSystemService("phone")).getSubscriberId());
        } else if (pref != null) {
            context.getPreferenceScreen().removePreference(pref);
        }
    }

    private static void setHardWareVersionStatus(SettingsPreferenceFragment context) {
        int is_show_hardwareversion = System.getInt(context.getContext().getContentResolver(), "is_show_hardwareversion", 0);
        Preference pref = context.findPreference("hardwareversion");
        if (1 == is_show_hardwareversion) {
            String hardWareVersion = readHardWareVersion();
            if (sHwCustStatusHwBase != null) {
                hardWareVersion = sHwCustStatusHwBase.getCustHardwareVersion(hardWareVersion);
            }
            setSummaryText(context, "hardwareversion", hardWareVersion);
        } else if (pref != null) {
            context.getPreferenceScreen().removePreference(pref);
        }
    }
}
