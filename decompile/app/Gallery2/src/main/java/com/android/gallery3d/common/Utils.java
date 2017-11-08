package com.android.gallery3d.common;

import android.os.Build;
import android.os.ParcelFileDescriptor;
import com.android.gallery3d.util.GalleryLog;
import java.io.Closeable;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;

public class Utils {
    private static final boolean IS_DEBUG_BUILD;
    private static long[] sCrcTable = new long[256];

    static {
        boolean z;
        if (Build.TYPE.equals("eng")) {
            z = true;
        } else {
            z = Build.TYPE.equals("userdebug");
        }
        IS_DEBUG_BUILD = z;
        for (int i = 0; i < 256; i++) {
            long part = (long) i;
            for (int j = 0; j < 8; j++) {
                part = (part >> 1) ^ ((((int) part) & 1) != 0 ? -7661587058870466123L : 0);
            }
            sCrcTable[i] = part;
        }
    }

    public static void assertTrue(boolean cond) {
        if (!cond) {
            throw new AssertionError();
        }
    }

    public static void fail(String message, Object... args) {
        if (args.length != 0) {
            message = String.format(message, args);
        }
        throw new AssertionError(message);
    }

    public static <T> T checkNotNull(T object) {
        if (object != null) {
            return object;
        }
        throw new NullPointerException();
    }

    public static boolean equals(Object a, Object b) {
        if (a != b) {
            return a == null ? false : a.equals(b);
        } else {
            return true;
        }
    }

    public static int nextPowerOf2(int n) {
        if (n <= 0 || n > 1073741824) {
            throw new IllegalArgumentException("n is invalid: " + n);
        }
        n--;
        n |= n >> 16;
        n |= n >> 8;
        n |= n >> 4;
        n |= n >> 2;
        return (n | (n >> 1)) + 1;
    }

    public static int prevPowerOf2(int n) {
        if (n > 0) {
            return Integer.highestOneBit(n);
        }
        throw new IllegalArgumentException();
    }

    public static int clamp(int x, int min, int max) {
        if (x > max) {
            return max;
        }
        if (x < min) {
            return min;
        }
        return x;
    }

    public static float clamp(float x, float min, float max) {
        if (x > max) {
            return max;
        }
        if (x < min) {
            return min;
        }
        return x;
    }

    public static long clamp(long x, long min, long max) {
        if (x > max) {
            return max;
        }
        if (x < min) {
            return min;
        }
        return x;
    }

    public static boolean isOpaque(int color) {
        return (color >>> 24) == 255;
    }

    public static void swap(int[] array, int i, int j) {
        int temp = array[i];
        array[i] = array[j];
        array[j] = temp;
    }

    public static void swap(float[] array, int i, int j) {
        float temp = array[i];
        array[i] = array[j];
        array[j] = temp;
    }

    public static final long crc64Long(String in) {
        if (in == null || in.length() == 0) {
            return 0;
        }
        return crc64Long(getBytes(in));
    }

    public static final long crc64Long(byte[] buffer) {
        long crc = -1;
        for (byte b : buffer) {
            crc = sCrcTable[(((int) crc) ^ b) & 255] ^ (crc >> 8);
        }
        return crc;
    }

    public static byte[] getBytes(String in) {
        byte[] result = new byte[(in.length() * 2)];
        int output = 0;
        for (char ch : in.toCharArray()) {
            int i = output + 1;
            result[output] = (byte) (ch & 255);
            output = i + 1;
            result[i] = (byte) (ch >> 8);
        }
        return result;
    }

    public static void closeSilently(Closeable c) {
        if (c != null) {
            try {
                c.close();
            } catch (Throwable t) {
                GalleryLog.w("Utils", "close fail." + t.getMessage());
            }
        }
    }

    public static int compare(long a, long b) {
        if (a < b) {
            return -1;
        }
        return a == b ? 0 : 1;
    }

    public static int ceilLog2(float value) {
        int i = 0;
        while (i < 31 && ((float) (1 << i)) < value) {
            i++;
        }
        return i;
    }

    public static int floorLog2(float value) {
        int i = 0;
        while (i < 31 && ((float) (1 << i)) <= value) {
            i++;
        }
        return i - 1;
    }

    public static int byteToInt(byte b) {
        return b & 255;
    }

    public static void closeSilently(ParcelFileDescriptor fd) {
        if (fd != null) {
            try {
                fd.close();
            } catch (Throwable t) {
                GalleryLog.w("Utils", "fail to close." + t.getMessage());
            }
        }
    }

    public static String ensureNotNull(String value) {
        return value == null ? "" : value;
    }

    public static int parseIntSafely(String content, int defaultValue) {
        if (content == null) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(content);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public static void waitWithoutInterrupt(Object object) {
        try {
            object.wait();
        } catch (InterruptedException e) {
            GalleryLog.w("Utils", "unexpected interrupt: " + object);
        }
    }

    public static void waitWithoutInterrupt(Object object, long timeout) {
        try {
            object.wait(timeout);
        } catch (InterruptedException e) {
            GalleryLog.w("Utils", "unexpected interrupt: " + object);
        }
    }

    public static long getVoiceOffset(String path) {
        IOException ex;
        Exception ex2;
        Throwable th;
        RandomAccessFile randomAccessFile = null;
        try {
            RandomAccessFile randomFile = new RandomAccessFile(path, "r");
            try {
                long fileLength = randomFile.length();
                if (fileLength < 20) {
                    if (randomFile != null) {
                        try {
                            randomFile.close();
                        } catch (IOException e) {
                        }
                    }
                    return -1;
                }
                randomFile.seek(fileLength - 20);
                byte[] buffer = new byte[20];
                if (randomFile.read(buffer) != 20) {
                    if (randomFile != null) {
                        try {
                            randomFile.close();
                        } catch (IOException e2) {
                        }
                    }
                    return -1;
                }
                String tag = new String(buffer, "ISO-8859-1").trim();
                if (tag.startsWith("HWVOICE_")) {
                    long longValue = Long.valueOf(tag.split("_")[1]).longValue();
                    if (randomFile != null) {
                        try {
                            randomFile.close();
                        } catch (IOException e3) {
                        }
                    }
                    return longValue;
                }
                if (randomFile != null) {
                    try {
                        randomFile.close();
                    } catch (IOException e4) {
                    }
                }
                randomAccessFile = randomFile;
                return -1;
            } catch (IOException e5) {
                ex = e5;
                randomAccessFile = randomFile;
                GalleryLog.w("Utils", "fail to getVoiceOffset." + ex.getMessage());
                if (randomAccessFile != null) {
                    try {
                        randomAccessFile.close();
                    } catch (IOException e6) {
                    }
                }
                return -1;
            } catch (Exception e7) {
                ex2 = e7;
                randomAccessFile = randomFile;
                try {
                    GalleryLog.w("Utils", "fail to getVoiceOffset." + ex2.getMessage());
                    if (randomAccessFile != null) {
                        try {
                            randomAccessFile.close();
                        } catch (IOException e8) {
                        }
                    }
                    return -1;
                } catch (Throwable th2) {
                    th = th2;
                    if (randomAccessFile != null) {
                        try {
                            randomAccessFile.close();
                        } catch (IOException e9) {
                        }
                    }
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                randomAccessFile = randomFile;
                if (randomAccessFile != null) {
                    randomAccessFile.close();
                }
                throw th;
            }
        } catch (IOException e10) {
            ex = e10;
            GalleryLog.w("Utils", "fail to getVoiceOffset." + ex.getMessage());
            if (randomAccessFile != null) {
                randomAccessFile.close();
            }
            return -1;
        } catch (Exception e11) {
            ex2 = e11;
            GalleryLog.w("Utils", "fail to getVoiceOffset." + ex2.getMessage());
            if (randomAccessFile != null) {
                randomAccessFile.close();
            }
            return -1;
        }
    }

    public static long getVoiceOffset(FileInputStream in) {
        if (in == null) {
            return -1;
        }
        try {
            long fileLength = (long) in.available();
            if (fileLength < 20) {
                return -1;
            }
            in.skip(fileLength - 20);
            byte[] buffer = new byte[20];
            if (in.read(buffer) != 20) {
                return -1;
            }
            String tag = new String(buffer, "ISO-8859-1").trim();
            if (tag.startsWith("HWVOICE_")) {
                return Long.valueOf(tag.split("_")[1]).longValue();
            }
            return -1;
        } catch (Exception ex) {
            GalleryLog.w("Utils", "fail to getVoiceOffset (InputParma FileInputStream)." + ex.getMessage());
        }
    }

    public static int getElasticInterpolation(int delta, int currentPos, int coefficient) {
        float len = (float) Math.abs(currentPos);
        return (int) ((Math.sqrt(((double) (((float) coefficient) * ((float) Math.abs(delta)))) + Math.pow((double) len, 2.0d)) - ((double) len)) * ((double) Math.signum((float) delta)));
    }

    public static boolean isNil(float value) {
        return equal(value, 0.0f);
    }

    public static boolean equal(float x, float y) {
        return ((double) Math.abs(x - y)) < 1.0E-7d;
    }

    public static int severalDaysFromNow(long at, long curTime, boolean isRoundup) {
        float duration = (float) (curTime - at);
        return (int) (isRoundup ? Math.ceil((double) (duration / 8.64E7f)) : Math.floor((double) (duration / 8.64E7f)));
    }
}
