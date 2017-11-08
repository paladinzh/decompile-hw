package com.huawei.hwid.update.b;

import com.huawei.hwid.core.d.b.e;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public final class a {
    private static final char[] a = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};

    public static String a(String str) {
        FileInputStream fileInputStream;
        FileChannel channel;
        FileInputStream fileInputStream2;
        Throwable th;
        FileChannel fileChannel = null;
        try {
            MessageDigest instance = MessageDigest.getInstance("MD5");
            try {
                File file = new File(str);
                fileInputStream = new FileInputStream(file);
                try {
                    channel = fileInputStream.getChannel();
                    try {
                        instance.update(channel.map(MapMode.READ_ONLY, 0, file.length()));
                        String a = a(instance.digest());
                        if (fileInputStream != null) {
                            try {
                                fileInputStream.close();
                            } catch (IOException e) {
                                e.d("EncryptUtils", "MD5 encrypt close inputstream error");
                            }
                        }
                        if (channel != null) {
                            try {
                                channel.close();
                            } catch (IOException e2) {
                                e.d("EncryptUtils", "MD5 encrypt close Channel error");
                            }
                        }
                        return a;
                    } catch (IOException e3) {
                        fileInputStream2 = fileInputStream;
                        if (fileInputStream2 != null) {
                            try {
                                fileInputStream2.close();
                            } catch (IOException e4) {
                                e.d("EncryptUtils", "MD5 encrypt close inputstream error");
                            }
                        }
                        if (channel != null) {
                            try {
                                channel.close();
                            } catch (IOException e5) {
                                e.d("EncryptUtils", "MD5 encrypt close Channel error");
                            }
                        }
                        return null;
                    } catch (Throwable th2) {
                        fileChannel = channel;
                        th = th2;
                        if (fileInputStream != null) {
                            try {
                                fileInputStream.close();
                            } catch (IOException e6) {
                                e.d("EncryptUtils", "MD5 encrypt close inputstream error");
                            }
                        }
                        if (fileChannel != null) {
                            try {
                                fileChannel.close();
                            } catch (IOException e7) {
                                e.d("EncryptUtils", "MD5 encrypt close Channel error");
                            }
                        }
                        throw th;
                    }
                } catch (IOException e8) {
                    channel = null;
                    fileInputStream2 = fileInputStream;
                    if (fileInputStream2 != null) {
                        fileInputStream2.close();
                    }
                    if (channel != null) {
                        channel.close();
                    }
                    return null;
                } catch (Throwable th3) {
                    th = th3;
                    if (fileInputStream != null) {
                        fileInputStream.close();
                    }
                    if (fileChannel != null) {
                        fileChannel.close();
                    }
                    throw th;
                }
            } catch (IOException e9) {
                channel = null;
                fileInputStream2 = null;
                if (fileInputStream2 != null) {
                    fileInputStream2.close();
                }
                if (channel != null) {
                    channel.close();
                }
                return null;
            } catch (Throwable th4) {
                th = th4;
                fileInputStream = null;
                if (fileInputStream != null) {
                    fileInputStream.close();
                }
                if (fileChannel != null) {
                    fileChannel.close();
                }
                throw th;
            }
        } catch (NoSuchAlgorithmException e10) {
            return null;
        }
    }

    public static String a(byte[] bArr) {
        if (bArr != null) {
            return a(bArr, 0, bArr.length);
        }
        return null;
    }

    public static String a(byte[] bArr, int i, int i2) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i3 = i; i3 < i + i2; i3++) {
            stringBuilder.append(a(bArr[i3]));
        }
        return stringBuilder.toString();
    }

    public static String a(byte b) {
        return a[(b & 240) >> 4] + "" + a[b & 15];
    }
}
