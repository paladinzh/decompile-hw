package com.huawei.gallery.photoshare.utils;

import com.android.gallery3d.util.GalleryLog;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MD5Utils {
    public static String getMD5(File file) {
        FileNotFoundException e;
        Throwable th;
        NoSuchAlgorithmException e2;
        IOException e3;
        String ret = null;
        if (file != null) {
            FileInputStream fileInputStream = null;
            FileChannel fileChannel = null;
            try {
                MessageDigest md = MessageDigest.getInstance("MD5");
                FileInputStream fis = new FileInputStream(file);
                try {
                    fileChannel = fis.getChannel();
                    ByteBuffer buffer = ByteBuffer.allocate(1048576);
                    while (true) {
                        buffer.clear();
                        int length = fileChannel.read(buffer);
                        if (length == -1) {
                            break;
                        }
                        md.update(buffer.array(), 0, length);
                    }
                    ret = bytesToString(md.digest());
                    if (fileChannel != null) {
                        try {
                            fileChannel.close();
                        } catch (IOException ex) {
                            GalleryLog.v("MD5Util", " closeException " + ex.toString());
                        }
                    }
                    if (fis != null) {
                        fis.close();
                    }
                } catch (FileNotFoundException e4) {
                    e = e4;
                    fileInputStream = fis;
                    try {
                        GalleryLog.v("MD5Util", " FileNotFoundException " + e.toString());
                        if (fileChannel != null) {
                            try {
                                fileChannel.close();
                            } catch (IOException ex2) {
                                GalleryLog.v("MD5Util", " closeException " + ex2.toString());
                            }
                        }
                        if (fileInputStream != null) {
                            fileInputStream.close();
                        }
                        return ret;
                    } catch (Throwable th2) {
                        th = th2;
                        if (fileChannel != null) {
                            try {
                                fileChannel.close();
                            } catch (IOException ex22) {
                                GalleryLog.v("MD5Util", " closeException " + ex22.toString());
                                throw th;
                            }
                        }
                        if (fileInputStream != null) {
                            fileInputStream.close();
                        }
                        throw th;
                    }
                } catch (NoSuchAlgorithmException e5) {
                    e2 = e5;
                    fileInputStream = fis;
                    GalleryLog.v("MD5Util", " NoSuchAlgorithmException " + e2.toString());
                    if (fileChannel != null) {
                        try {
                            fileChannel.close();
                        } catch (IOException ex222) {
                            GalleryLog.v("MD5Util", " closeException " + ex222.toString());
                        }
                    }
                    if (fileInputStream != null) {
                        fileInputStream.close();
                    }
                    return ret;
                } catch (IOException e6) {
                    e3 = e6;
                    fileInputStream = fis;
                    GalleryLog.v("MD5Util", " IOException " + e3.toString());
                    if (fileChannel != null) {
                        try {
                            fileChannel.close();
                        } catch (IOException ex2222) {
                            GalleryLog.v("MD5Util", " closeException " + ex2222.toString());
                        }
                    }
                    if (fileInputStream != null) {
                        fileInputStream.close();
                    }
                    return ret;
                } catch (Throwable th3) {
                    th = th3;
                    fileInputStream = fis;
                    if (fileChannel != null) {
                        fileChannel.close();
                    }
                    if (fileInputStream != null) {
                        fileInputStream.close();
                    }
                    throw th;
                }
            } catch (FileNotFoundException e7) {
                e = e7;
                GalleryLog.v("MD5Util", " FileNotFoundException " + e.toString());
                if (fileChannel != null) {
                    fileChannel.close();
                }
                if (fileInputStream != null) {
                    fileInputStream.close();
                }
                return ret;
            } catch (NoSuchAlgorithmException e8) {
                e2 = e8;
                GalleryLog.v("MD5Util", " NoSuchAlgorithmException " + e2.toString());
                if (fileChannel != null) {
                    fileChannel.close();
                }
                if (fileInputStream != null) {
                    fileInputStream.close();
                }
                return ret;
            } catch (IOException e9) {
                e3 = e9;
                GalleryLog.v("MD5Util", " IOException " + e3.toString());
                if (fileChannel != null) {
                    fileChannel.close();
                }
                if (fileInputStream != null) {
                    fileInputStream.close();
                }
                return ret;
            }
        }
        return ret;
    }

    private static String bytesToString(byte[] data) {
        char[] hexDigits = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
        char[] temp = new char[(data.length * 2)];
        for (int i = 0; i < data.length; i++) {
            byte b = data[i];
            temp[i * 2] = hexDigits[(b >>> 4) & 15];
            temp[(i * 2) + 1] = hexDigits[b & 15];
        }
        return new String(temp);
    }
}
