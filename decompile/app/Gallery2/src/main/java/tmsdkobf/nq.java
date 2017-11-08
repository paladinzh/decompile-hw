package tmsdkobf;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.Context;
import android.content.pm.IPackageDataObserver;
import android.content.pm.IPackageStatsObserver;
import android.content.pm.PackageManager;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/* compiled from: Unknown */
final class nq implements np {
    private static Method CV;
    private static Method CW;
    private static Field CX;
    private static long CY = -1;
    byte[] CZ = new byte[1024];
    private ActivityManager mActivityManager;
    private Context mContext;
    private PackageManager mPackageManager;

    static {
        try {
            CV = PackageManager.class.getDeclaredMethod("freeStorageAndNotify", new Class[]{Long.TYPE, IPackageDataObserver.class});
            CV.setAccessible(true);
            CW = PackageManager.class.getDeclaredMethod("getPackageSizeInfo", new Class[]{String.class, IPackageStatsObserver.class});
            CW.setAccessible(true);
            CX = RunningAppProcessInfo.class.getDeclaredField("flags");
            CX.setAccessible(true);
        } catch (Throwable e) {
            e.printStackTrace();
        } catch (Throwable e2) {
            e2.printStackTrace();
        } catch (Throwable e22) {
            e22.printStackTrace();
        }
    }

    public nq(Context context) {
        this.mContext = context;
        this.mPackageManager = this.mContext.getPackageManager();
        this.mActivityManager = (ActivityManager) this.mContext.getSystemService("activity");
    }

    public long fu() {
        DataInputStream dataInputStream;
        IOException e;
        FileNotFoundException e2;
        Throwable th;
        NumberFormatException e3;
        if (CY == -1) {
            File file = new File("/proc/meminfo");
            if (file.exists()) {
                try {
                    dataInputStream = new DataInputStream(new FileInputStream(file));
                    try {
                        String readLine = dataInputStream.readLine();
                        if (readLine != null) {
                            CY = Long.parseLong(readLine.trim().split("[\\s]+")[1]);
                            if (dataInputStream != null) {
                                try {
                                    dataInputStream.close();
                                } catch (IOException e4) {
                                    e4.printStackTrace();
                                }
                            }
                        } else {
                            throw new IOException("/proc/meminfo is empty!");
                        }
                    } catch (FileNotFoundException e5) {
                        e2 = e5;
                        try {
                            e2.printStackTrace();
                            if (dataInputStream != null) {
                                try {
                                    dataInputStream.close();
                                } catch (IOException e42) {
                                    e42.printStackTrace();
                                }
                            }
                            return (CY <= 0 ? null : 1) == null ? 1 : CY;
                        } catch (Throwable th2) {
                            th = th2;
                            if (dataInputStream != null) {
                                try {
                                    dataInputStream.close();
                                } catch (IOException e6) {
                                    e6.printStackTrace();
                                }
                            }
                            throw th;
                        }
                    } catch (IOException e7) {
                        e42 = e7;
                        e42.printStackTrace();
                        if (dataInputStream != null) {
                            try {
                                dataInputStream.close();
                            } catch (IOException e422) {
                                e422.printStackTrace();
                            }
                        }
                        if (CY <= 0) {
                        }
                        if ((CY <= 0 ? null : 1) == null) {
                        }
                    } catch (NumberFormatException e8) {
                        e3 = e8;
                        e3.printStackTrace();
                        if (dataInputStream != null) {
                            try {
                                dataInputStream.close();
                            } catch (IOException e4222) {
                                e4222.printStackTrace();
                            }
                        }
                        if (CY <= 0) {
                        }
                        if ((CY <= 0 ? null : 1) == null) {
                        }
                    }
                } catch (FileNotFoundException e9) {
                    e2 = e9;
                    dataInputStream = null;
                    e2.printStackTrace();
                    if (dataInputStream != null) {
                        dataInputStream.close();
                    }
                    if (CY <= 0) {
                    }
                    if ((CY <= 0 ? null : 1) == null) {
                    }
                } catch (IOException e10) {
                    e4222 = e10;
                    dataInputStream = null;
                    e4222.printStackTrace();
                    if (dataInputStream != null) {
                        dataInputStream.close();
                    }
                    if (CY <= 0) {
                    }
                    if ((CY <= 0 ? null : 1) == null) {
                    }
                } catch (NumberFormatException e11) {
                    e3 = e11;
                    dataInputStream = null;
                    e3.printStackTrace();
                    if (dataInputStream != null) {
                        dataInputStream.close();
                    }
                    if (CY <= 0) {
                    }
                    if ((CY <= 0 ? null : 1) == null) {
                    }
                } catch (Throwable th3) {
                    th = th3;
                    dataInputStream = null;
                    if (dataInputStream != null) {
                        dataInputStream.close();
                    }
                    throw th;
                }
            }
        }
        if (CY <= 0) {
        }
        if ((CY <= 0 ? null : 1) == null) {
        }
    }
}
