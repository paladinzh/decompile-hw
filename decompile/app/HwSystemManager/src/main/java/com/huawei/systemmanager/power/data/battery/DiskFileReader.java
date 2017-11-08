package com.huawei.systemmanager.power.data.battery;

import com.huawei.systemmanager.util.HwLog;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

public class DiskFileReader {
    private static final String TAG = DiskFileReader.class.getSimpleName();

    public static int readFileByInt(String fileName) {
        String lineValue = readFileByChars(fileName);
        try {
            if (!lineValue.equals("")) {
                return Integer.parseInt(lineValue.trim());
            }
        } catch (NumberFormatException e) {
            HwLog.e(TAG, "readFileByInt catch NumberFormatException first line value: " + lineValue);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        HwLog.w(TAG, "readFileByInt return invalid value");
        return -1;
    }

    private static String readFileByChars(String fileName) {
        IOException e1;
        Throwable th;
        File file = new File(fileName);
        if (!file.exists() || !file.canRead()) {
            return "";
        }
        Reader reader = null;
        char[] tempChars = new char[512];
        StringBuilder sb = new StringBuilder();
        try {
            Reader reader2 = new InputStreamReader(new FileInputStream(fileName), "UTF-8");
            while (true) {
                try {
                    int charRead = reader2.read(tempChars, 0, tempChars.length);
                    if (charRead == -1) {
                        break;
                    }
                    sb.append(tempChars, 0, charRead);
                } catch (IOException e) {
                    e1 = e;
                    reader = reader2;
                } catch (Throwable th2) {
                    th = th2;
                    reader = reader2;
                }
            }
            if (reader2 != null) {
                try {
                    reader2.close();
                } catch (IOException e2) {
                }
            }
        } catch (IOException e3) {
            e1 = e3;
            try {
                e1.printStackTrace();
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e4) {
                    }
                }
                return sb.toString();
            } catch (Throwable th3) {
                th = th3;
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e5) {
                    }
                }
                throw th;
            }
        }
        return sb.toString();
    }

    public static String readFileByString(String fileName) {
        return readFileByChars(fileName);
    }
}
