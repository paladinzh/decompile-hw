package defpackage;

import android.os.IBinder;
import android.os.Parcel;
import android.os.Process;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.text.TextUtils;

/* renamed from: bv */
public class bv {
    private static boolean co = false;
    private static boolean cp = false;

    public static void A(String str) {
        Parcel obtain;
        RemoteException e;
        Throwable th;
        Parcel parcel = null;
        aw.i("PushLog2841", "ctrlScoket registerPackage " + str);
        if (!TextUtils.isEmpty(str)) {
            try {
                IBinder service = ServiceManager.getService("connectivity");
                if (service == null) {
                    aw.e("PushLog2841", "get connectivity service failed ");
                    if (parcel != null) {
                        parcel.recycle();
                    }
                    if (parcel != null) {
                        parcel.recycle();
                        return;
                    }
                    return;
                }
                obtain = Parcel.obtain();
                try {
                    obtain.writeString(str);
                    parcel = Parcel.obtain();
                    service.transact(1001, obtain, parcel, 0);
                    if (obtain != null) {
                        obtain.recycle();
                    }
                    if (parcel != null) {
                        parcel.recycle();
                    }
                } catch (RemoteException e2) {
                    e = e2;
                    try {
                        aw.e("PushLog2841", "registerPackage error:" + e.getMessage());
                        if (obtain != null) {
                            obtain.recycle();
                        }
                        if (parcel != null) {
                            parcel.recycle();
                        }
                    } catch (Throwable th2) {
                        th = th2;
                        if (obtain != null) {
                            obtain.recycle();
                        }
                        if (parcel != null) {
                            parcel.recycle();
                        }
                        throw th;
                    }
                } catch (Exception e3) {
                    th = e3;
                    aw.d("PushLog2841", "registerPackage error:", th);
                    if (obtain != null) {
                        obtain.recycle();
                    }
                    if (parcel != null) {
                        parcel.recycle();
                    }
                }
            } catch (RemoteException e4) {
                e = e4;
                obtain = parcel;
                aw.e("PushLog2841", "registerPackage error:" + e.getMessage());
                if (obtain != null) {
                    obtain.recycle();
                }
                if (parcel != null) {
                    parcel.recycle();
                }
            } catch (Exception e5) {
                th = e5;
                obtain = parcel;
                aw.d("PushLog2841", "registerPackage error:", th);
                if (obtain != null) {
                    obtain.recycle();
                }
                if (parcel != null) {
                    parcel.recycle();
                }
            } catch (Throwable th3) {
                th = th3;
                obtain = parcel;
                if (obtain != null) {
                    obtain.recycle();
                }
                if (parcel != null) {
                    parcel.recycle();
                }
                throw th;
            }
        }
    }

    public static void B(String str) {
        Parcel obtain;
        RemoteException e;
        Throwable th;
        Parcel parcel = null;
        aw.i("PushLog2841", "ctrlScoket deregisterPackage " + str);
        if (!TextUtils.isEmpty(str)) {
            try {
                IBinder service = ServiceManager.getService("connectivity");
                if (service == null) {
                    if (parcel != null) {
                        parcel.recycle();
                    }
                    if (parcel != null) {
                        parcel.recycle();
                        return;
                    }
                    return;
                }
                obtain = Parcel.obtain();
                try {
                    obtain.writeString(str);
                    parcel = Parcel.obtain();
                    service.transact(1002, obtain, parcel, 0);
                    if (obtain != null) {
                        obtain.recycle();
                    }
                    if (parcel != null) {
                        parcel.recycle();
                    }
                } catch (RemoteException e2) {
                    e = e2;
                    try {
                        aw.e("PushLog2841", "deregisterPackage error:" + e.getMessage());
                        if (obtain != null) {
                            obtain.recycle();
                        }
                        if (parcel != null) {
                            parcel.recycle();
                        }
                    } catch (Throwable th2) {
                        th = th2;
                        if (obtain != null) {
                            obtain.recycle();
                        }
                        if (parcel != null) {
                            parcel.recycle();
                        }
                        throw th;
                    }
                } catch (Exception e3) {
                    th = e3;
                    aw.d("PushLog2841", "deregisterPackage error:", th);
                    if (obtain != null) {
                        obtain.recycle();
                    }
                    if (parcel != null) {
                        parcel.recycle();
                    }
                }
            } catch (RemoteException e4) {
                e = e4;
                obtain = parcel;
                aw.e("PushLog2841", "deregisterPackage error:" + e.getMessage());
                if (obtain != null) {
                    obtain.recycle();
                }
                if (parcel != null) {
                    parcel.recycle();
                }
            } catch (Exception e5) {
                th = e5;
                obtain = parcel;
                aw.d("PushLog2841", "deregisterPackage error:", th);
                if (obtain != null) {
                    obtain.recycle();
                }
                if (parcel != null) {
                    parcel.recycle();
                }
            } catch (Throwable th3) {
                th = th3;
                obtain = parcel;
                if (obtain != null) {
                    obtain.recycle();
                }
                if (parcel != null) {
                    parcel.recycle();
                }
                throw th;
            }
        }
    }

    public static void c(int i, int i2) {
        Parcel obtain;
        RemoteException e;
        Throwable th;
        Parcel parcel = null;
        aw.i("PushLog2841", "ctrlSocket cmd is " + i + ", param is " + i2);
        try {
            IBinder service = ServiceManager.getService("connectivity");
            if (service == null) {
                aw.w("PushLog2841", "get connectivity service failed ");
                if (parcel != null) {
                    parcel.recycle();
                }
                if (parcel != null) {
                    parcel.recycle();
                    return;
                }
                return;
            }
            obtain = Parcel.obtain();
            try {
                obtain.writeInt(Process.myPid());
                obtain.writeInt(i);
                obtain.writeInt(i2);
                parcel = Parcel.obtain();
                service.transact(1003, obtain, parcel, 0);
                aw.i("PushLog2841", "ctrlSocket success");
                if (obtain != null) {
                    obtain.recycle();
                }
                if (parcel != null) {
                    parcel.recycle();
                }
            } catch (RemoteException e2) {
                e = e2;
                try {
                    aw.e("PushLog2841", "ctrlSocket error:" + e.getMessage());
                    if (obtain != null) {
                        obtain.recycle();
                    }
                    if (parcel != null) {
                        parcel.recycle();
                    }
                } catch (Throwable th2) {
                    th = th2;
                    if (obtain != null) {
                        obtain.recycle();
                    }
                    if (parcel != null) {
                        parcel.recycle();
                    }
                    throw th;
                }
            } catch (Exception e3) {
                th = e3;
                aw.d("PushLog2841", "ctrlSocket error:", th);
                if (obtain != null) {
                    obtain.recycle();
                }
                if (parcel != null) {
                    parcel.recycle();
                }
            }
        } catch (RemoteException e4) {
            e = e4;
            obtain = parcel;
            aw.e("PushLog2841", "ctrlSocket error:" + e.getMessage());
            if (obtain != null) {
                obtain.recycle();
            }
            if (parcel != null) {
                parcel.recycle();
            }
        } catch (Exception e5) {
            th = e5;
            obtain = parcel;
            aw.d("PushLog2841", "ctrlSocket error:", th);
            if (obtain != null) {
                obtain.recycle();
            }
            if (parcel != null) {
                parcel.recycle();
            }
        } catch (Throwable th3) {
            th = th3;
            obtain = parcel;
            if (obtain != null) {
                obtain.recycle();
            }
            if (parcel != null) {
                parcel.recycle();
            }
            throw th;
        }
    }

    public static String[] cq() {
        Parcel obtain;
        RemoteException e;
        Throwable th;
        Throwable e2;
        Parcel parcel = null;
        String[] strArr = new String[0];
        try {
            IBinder service = ServiceManager.getService("connectivity");
            if (service == null) {
                aw.w("PushLog2841", "get connectivity service failed ");
                if (parcel != null) {
                    parcel.recycle();
                }
                if (parcel != null) {
                    parcel.recycle();
                }
            } else {
                obtain = Parcel.obtain();
                try {
                    parcel = Parcel.obtain();
                    service.transact(1004, obtain, parcel, 0);
                    Object readString = parcel.readString();
                    aw.i("PushLog2841", "ctrlSocket whitepackages is:" + readString);
                    if (!TextUtils.isEmpty(readString)) {
                        strArr = readString.split("\t");
                    }
                    if (obtain != null) {
                        obtain.recycle();
                    }
                    if (parcel != null) {
                        parcel.recycle();
                    }
                } catch (RemoteException e3) {
                    e = e3;
                    try {
                        aw.e("PushLog2841", "ctrlSocket error:" + e.getMessage());
                        if (obtain != null) {
                            obtain.recycle();
                        }
                        if (parcel != null) {
                            parcel.recycle();
                        }
                        return strArr;
                    } catch (Throwable th2) {
                        th = th2;
                        if (obtain != null) {
                            obtain.recycle();
                        }
                        if (parcel != null) {
                            parcel.recycle();
                        }
                        throw th;
                    }
                } catch (Exception e4) {
                    e2 = e4;
                    aw.d("PushLog2841", "ctrlSocket error:", e2);
                    if (obtain != null) {
                        obtain.recycle();
                    }
                    if (parcel != null) {
                        parcel.recycle();
                    }
                    return strArr;
                }
            }
        } catch (RemoteException e5) {
            e = e5;
            obtain = parcel;
            aw.e("PushLog2841", "ctrlSocket error:" + e.getMessage());
            if (obtain != null) {
                obtain.recycle();
            }
            if (parcel != null) {
                parcel.recycle();
            }
            return strArr;
        } catch (Exception e6) {
            e2 = e6;
            obtain = parcel;
            aw.d("PushLog2841", "ctrlSocket error:", e2);
            if (obtain != null) {
                obtain.recycle();
            }
            if (parcel != null) {
                parcel.recycle();
            }
            return strArr;
        } catch (Throwable th3) {
            th = th3;
            obtain = parcel;
            if (obtain != null) {
                obtain.recycle();
            }
            if (parcel != null) {
                parcel.recycle();
            }
            throw th;
        }
        return strArr;
    }

    public static int cr() {
        Parcel obtain;
        RemoteException e;
        Throwable th;
        Throwable e2;
        Parcel parcel = null;
        int i = -1;
        try {
            IBinder service = ServiceManager.getService("connectivity");
            if (service == null) {
                aw.w("PushLog2841", "get connectivity service failed ");
                if (parcel != null) {
                    parcel.recycle();
                }
                if (parcel != null) {
                    parcel.recycle();
                }
                return i;
            }
            obtain = Parcel.obtain();
            try {
                parcel = Parcel.obtain();
                service.transact(1005, obtain, parcel, 0);
                i = parcel.readInt();
                if (obtain != null) {
                    obtain.recycle();
                }
                if (parcel != null) {
                    parcel.recycle();
                }
            } catch (RemoteException e3) {
                e = e3;
                try {
                    aw.e("PushLog2841", "getCtrlSocketModel error:" + e.getMessage());
                    if (obtain != null) {
                        obtain.recycle();
                    }
                    if (parcel != null) {
                        parcel.recycle();
                    }
                    aw.i("PushLog2841", "ctrlSocket level is:" + i);
                    return i;
                } catch (Throwable th2) {
                    th = th2;
                    if (obtain != null) {
                        obtain.recycle();
                    }
                    if (parcel != null) {
                        parcel.recycle();
                    }
                    throw th;
                }
            } catch (Exception e4) {
                e2 = e4;
                aw.d("PushLog2841", "getCtrlSocketModel error:", e2);
                if (obtain != null) {
                    obtain.recycle();
                }
                if (parcel != null) {
                    parcel.recycle();
                }
                aw.i("PushLog2841", "ctrlSocket level is:" + i);
                return i;
            }
            aw.i("PushLog2841", "ctrlSocket level is:" + i);
            return i;
        } catch (RemoteException e5) {
            e = e5;
            obtain = parcel;
            aw.e("PushLog2841", "getCtrlSocketModel error:" + e.getMessage());
            if (obtain != null) {
                obtain.recycle();
            }
            if (parcel != null) {
                parcel.recycle();
            }
            aw.i("PushLog2841", "ctrlSocket level is:" + i);
            return i;
        } catch (Exception e6) {
            e2 = e6;
            obtain = parcel;
            aw.d("PushLog2841", "getCtrlSocketModel error:", e2);
            if (obtain != null) {
                obtain.recycle();
            }
            if (parcel != null) {
                parcel.recycle();
            }
            aw.i("PushLog2841", "ctrlSocket level is:" + i);
            return i;
        } catch (Throwable th3) {
            th = th3;
            obtain = parcel;
            if (obtain != null) {
                obtain.recycle();
            }
            if (parcel != null) {
                parcel.recycle();
            }
            throw th;
        }
    }

    private static String cs() {
        RemoteException e;
        Throwable th;
        Throwable e2;
        Parcel parcel = null;
        String str = "";
        Parcel obtain;
        try {
            IBinder service = ServiceManager.getService("connectivity");
            if (service == null) {
                aw.w("PushLog2841", "get connectivity service failed ");
                if (parcel != null) {
                    parcel.recycle();
                }
                if (parcel != null) {
                    parcel.recycle();
                }
                return str;
            }
            obtain = Parcel.obtain();
            try {
                parcel = Parcel.obtain();
                service.transact(1006, obtain, parcel, 0);
                str = parcel.readString();
                if (obtain != null) {
                    obtain.recycle();
                }
                if (parcel != null) {
                    parcel.recycle();
                }
            } catch (RemoteException e3) {
                e = e3;
                try {
                    aw.e("PushLog2841", "getCtrlSocketVersion error:" + e.getMessage());
                    if (obtain != null) {
                        obtain.recycle();
                    }
                    if (parcel != null) {
                        parcel.recycle();
                    }
                    aw.i("PushLog2841", "ctrlSocket version is:" + str);
                    return str;
                } catch (Throwable th2) {
                    th = th2;
                    if (obtain != null) {
                        obtain.recycle();
                    }
                    if (parcel != null) {
                        parcel.recycle();
                    }
                    throw th;
                }
            } catch (Exception e4) {
                e2 = e4;
                aw.d("PushLog2841", "getCtrlSocketVersion error:", e2);
                if (obtain != null) {
                    obtain.recycle();
                }
                if (parcel != null) {
                    parcel.recycle();
                }
                aw.i("PushLog2841", "ctrlSocket version is:" + str);
                return str;
            }
            aw.i("PushLog2841", "ctrlSocket version is:" + str);
            return str;
        } catch (RemoteException e5) {
            e = e5;
            obtain = parcel;
            aw.e("PushLog2841", "getCtrlSocketVersion error:" + e.getMessage());
            if (obtain != null) {
                obtain.recycle();
            }
            if (parcel != null) {
                parcel.recycle();
            }
            aw.i("PushLog2841", "ctrlSocket version is:" + str);
            return str;
        } catch (Exception e6) {
            e2 = e6;
            obtain = parcel;
            aw.d("PushLog2841", "getCtrlSocketVersion error:", e2);
            if (obtain != null) {
                obtain.recycle();
            }
            if (parcel != null) {
                parcel.recycle();
            }
            aw.i("PushLog2841", "ctrlSocket version is:" + str);
            return str;
        } catch (Throwable th3) {
            th = th3;
            obtain = parcel;
            if (obtain != null) {
                obtain.recycle();
            }
            if (parcel != null) {
                parcel.recycle();
            }
            throw th;
        }
    }

    public static boolean ct() {
        String str = "v2";
        aw.i("PushLog2841", "enter isSupportCtrlSocketV2, mHasCheckCtrlSocketVersion:" + co + ",mIsSupportCtrlSokceV2:" + cp);
        if (!co) {
            co = true;
            cp = str.equals(bv.cs());
            aw.i("PushLog2841", "mIsSupportCtrlSokceV2:" + cp);
        }
        return cp;
    }
}
