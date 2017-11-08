package com.huawei.powergenie.integration.adapter;

import android.util.Log;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;

public class HardwareAdapter {
    public static boolean setChargingLimit(int limitCurrent, String filePath) {
        Exception e;
        Throwable th;
        Log.i("HardwareAdapter", "set charging limit: " + limitCurrent);
        FileOutputStream fileOutputStream = null;
        boolean result = false;
        byte[] byCurrent = Integer.toString(limitCurrent).getBytes();
        try {
            FileOutputStream fos = new FileOutputStream(filePath);
            try {
                fos.write(byCurrent, 0, byCurrent.length);
                result = true;
                if (fos != null) {
                    try {
                        fos.close();
                    } catch (Exception e2) {
                        Log.d("HardwareAdapter", "close failed " + e2);
                    }
                }
                fileOutputStream = fos;
            } catch (Exception e3) {
                e2 = e3;
                fileOutputStream = fos;
                try {
                    Log.e("HardwareAdapter", "set charging limit error", e2);
                    if (fileOutputStream != null) {
                        try {
                            fileOutputStream.close();
                        } catch (Exception e22) {
                            Log.d("HardwareAdapter", "close failed " + e22);
                        }
                    }
                    return result;
                } catch (Throwable th2) {
                    th = th2;
                    if (fileOutputStream != null) {
                        try {
                            fileOutputStream.close();
                        } catch (Exception e222) {
                            Log.d("HardwareAdapter", "close failed " + e222);
                        }
                    }
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                fileOutputStream = fos;
                if (fileOutputStream != null) {
                    fileOutputStream.close();
                }
                throw th;
            }
        } catch (Exception e4) {
            e222 = e4;
            Log.e("HardwareAdapter", "set charging limit error", e222);
            if (fileOutputStream != null) {
                fileOutputStream.close();
            }
            return result;
        }
        return result;
    }

    public static boolean setWlanLimit(int level, String filePath) {
        boolean result;
        Exception e;
        Throwable th;
        Log.i("HardwareAdapter", "set wlan limit: " + level + ", path: " + filePath);
        FileOutputStream fileOutputStream = null;
        try {
            FileOutputStream fos = new FileOutputStream(filePath);
            try {
                byte[] bytes = new byte[2];
                switch (level) {
                    case NativeAdapter.PLATFORM_QCOM /*0*/:
                        bytes[0] = (byte) 48;
                        break;
                    case NativeAdapter.PLATFORM_MTK /*1*/:
                        bytes[0] = (byte) 49;
                        break;
                    case NativeAdapter.PLATFORM_HI /*2*/:
                        bytes[0] = (byte) 50;
                        break;
                    case NativeAdapter.PLATFORM_K3V3 /*3*/:
                        bytes[0] = (byte) 51;
                        break;
                    case 4:
                        bytes[0] = (byte) 52;
                        break;
                }
                bytes[1] = (byte) 10;
                fos.write(bytes);
                fos.close();
                result = true;
                if (fos != null) {
                    try {
                        fos.close();
                    } catch (Exception e2) {
                    }
                }
                fileOutputStream = fos;
            } catch (Exception e3) {
                e = e3;
                fileOutputStream = fos;
                try {
                    Log.e("HardwareAdapter", "set wlan Limit failed", e);
                    result = false;
                    if (fileOutputStream != null) {
                        try {
                            fileOutputStream.close();
                        } catch (Exception e4) {
                        }
                    }
                    return result;
                } catch (Throwable th2) {
                    th = th2;
                    if (fileOutputStream != null) {
                        try {
                            fileOutputStream.close();
                        } catch (Exception e5) {
                        }
                    }
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                fileOutputStream = fos;
                if (fileOutputStream != null) {
                    fileOutputStream.close();
                }
                throw th;
            }
        } catch (Exception e6) {
            e = e6;
            Log.e("HardwareAdapter", "set wlan Limit failed", e);
            result = false;
            if (fileOutputStream != null) {
                fileOutputStream.close();
            }
            return result;
        }
        return result;
    }

    public static boolean setCameraFps(int value) {
        FileNotFoundException fe;
        SecurityException se;
        FileOutputStream fileOutputStream;
        IOException e;
        try {
            FileOutputStream out = new FileOutputStream("/sys/bus/platform/drivers/huawei,camcfgdev/guard_thermal");
            if (value >= 0) {
                try {
                    StringBuilder sb = new StringBuilder();
                    sb.append(String.valueOf(value));
                    sb.append("\n");
                    byte[] byteFps = sb.toString().getBytes();
                    out.write(byteFps, 0, byteFps.length);
                    Log.i("HardwareAdapter", "writeFpsNode success value = " + value);
                } catch (FileNotFoundException e2) {
                    fe = e2;
                    Log.w("HardwareAdapter", "FileNotFoundException : writeFpsNode fail :" + fe);
                    return false;
                } catch (SecurityException e3) {
                    se = e3;
                    fileOutputStream = out;
                    Log.w("HardwareAdapter", "SecurityException : writeFpsNode fail :" + se);
                    return false;
                } catch (IOException e4) {
                    e = e4;
                    fileOutputStream = out;
                    Log.w("HardwareAdapter", "IOException : writeFpsNode fail :" + e);
                    return false;
                }
            }
            out.close();
            return true;
        } catch (FileNotFoundException e5) {
            fe = e5;
            Log.w("HardwareAdapter", "FileNotFoundException : writeFpsNode fail :" + fe);
            return false;
        } catch (SecurityException e6) {
            se = e6;
            Log.w("HardwareAdapter", "SecurityException : writeFpsNode fail :" + se);
            return false;
        } catch (IOException e7) {
            e = e7;
            Log.w("HardwareAdapter", "IOException : writeFpsNode fail :" + e);
            return false;
        }
    }

    public static boolean supportCinemaMode() {
        if (new File("/sys/class/graphics/fb0/lcd_cinema_mode").exists()) {
            return true;
        }
        Log.i("HardwareAdapter", "/sys/class/graphics/fb0/lcd_cinema_mode not exists");
        return false;
    }

    public static boolean setCinemaMode(boolean enable) {
        int value = enable ? 1 : 0;
        try {
            FileOutputStream out = new FileOutputStream("/sys/class/graphics/fb0/lcd_cinema_mode");
            byte[] byValue = Integer.toString(value).getBytes();
            out.write(byValue, 0, byValue.length);
            Log.i("HardwareAdapter", "write LcdCinemaNode success value :" + value);
            out.close();
            return true;
        } catch (FileNotFoundException fe) {
            Log.e("HardwareAdapter", "write LcdCinemaNode failure throw FileNotFoundException :" + fe);
            return false;
        } catch (SecurityException se) {
            Log.e("HardwareAdapter", "write LcdCinemaNode failure throw SecurityException :" + se);
            return false;
        } catch (IOException e) {
            Log.e("HardwareAdapter", "write LcdCinemaNode failure throw IOException :" + e);
            return false;
        }
    }

    public static int getLCDCurNodeBrightness() {
        String path = "/sys/class/leds/lcd_backlight0/brightness";
        if (!new File(path).exists()) {
            path = "/sys/class/leds/lcd-backlight/brightness";
            if (!new File(path).exists()) {
                Log.w("HardwareAdapter", "the lcd current node path is not exist! ");
                return -1;
            }
        }
        return getNodeVal(path);
    }

    public static int getLCDMaxNodeBrightness() {
        String path = "/sys/class/leds/lcd_backlight0/max_brightness";
        if (!new File(path).exists()) {
            path = "/sys/class/leds/lcd-backlight/max_brightness";
            if (!new File(path).exists()) {
                Log.w("HardwareAdapter", "the lcd max node path is not exist! ");
                return -1;
            }
        }
        return getNodeVal(path);
    }

    public static int getNodeVal(String path) {
        Throwable th;
        int retVal = -1;
        FileInputStream fileInputStream = null;
        byte[] bytes = new byte[10];
        try {
            Arrays.fill(bytes, (byte) 0);
            FileInputStream fis = new FileInputStream(path);
            try {
                int len = fis.read(bytes);
                if (len > 0) {
                    String strVal = new String(bytes, 0, len, "UTF-8");
                    try {
                        retVal = Integer.parseInt(strVal.trim());
                        String str = strVal;
                    } catch (Exception e) {
                        fileInputStream = fis;
                        try {
                            Log.e("HardwareAdapter", "read fail: " + path);
                            if (fileInputStream != null) {
                                try {
                                    fileInputStream.close();
                                } catch (Exception e2) {
                                    Log.w("HardwareAdapter", "close fail: " + path);
                                }
                            }
                            return retVal;
                        } catch (Throwable th2) {
                            th = th2;
                            if (fileInputStream != null) {
                                try {
                                    fileInputStream.close();
                                } catch (Exception e3) {
                                    Log.w("HardwareAdapter", "close fail: " + path);
                                }
                            }
                            throw th;
                        }
                    } catch (Throwable th3) {
                        th = th3;
                        fileInputStream = fis;
                        if (fileInputStream != null) {
                            fileInputStream.close();
                        }
                        throw th;
                    }
                }
                if (fis != null) {
                    try {
                        fis.close();
                    } catch (Exception e4) {
                        Log.w("HardwareAdapter", "close fail: " + path);
                    }
                }
                fileInputStream = fis;
            } catch (Exception e5) {
                fileInputStream = fis;
                Log.e("HardwareAdapter", "read fail: " + path);
                if (fileInputStream != null) {
                    fileInputStream.close();
                }
                return retVal;
            } catch (Throwable th4) {
                th = th4;
                fileInputStream = fis;
                if (fileInputStream != null) {
                    fileInputStream.close();
                }
                throw th;
            }
        } catch (Exception e6) {
            Log.e("HardwareAdapter", "read fail: " + path);
            if (fileInputStream != null) {
                fileInputStream.close();
            }
            return retVal;
        }
        return retVal;
    }
}
