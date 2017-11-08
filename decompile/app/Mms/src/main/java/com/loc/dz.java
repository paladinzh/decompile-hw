package com.loc;

import android.location.Location;
import android.net.wifi.ScanResult;
import android.telephony.CellLocation;
import java.util.List;

/* compiled from: Unknown */
public final class dz {
    private static int c = 10;
    private static int d = 100;
    private static float f = 0.5f;
    protected ed a = new ed(this);
    protected ea b = new ea(this);
    private dl e;

    protected dz(dl dlVar) {
        this.e = dlVar;
    }

    protected static void a() {
    }

    protected static void a(int i) {
        c = i;
    }

    protected static void b(int i) {
        d = i;
    }

    protected final boolean a(Location location) {
        eb ebVar = null;
        boolean z = false;
        if (this.e == null) {
            return false;
        }
        List j = this.e.j();
        if (j == null || location == null) {
            return false;
        }
        "cell.list.size: " + j.size();
        if (j.size() >= 2) {
            eb ebVar2 = new eb((CellLocation) j.get(1));
            if (this.b.b != null) {
                boolean z2 = location.distanceTo(this.b.b) > ((float) d);
                if (!z2) {
                    ebVar = this.b.a;
                    z2 = ebVar2.e == ebVar.e && ebVar2.d == ebVar.d && ebVar2.c == ebVar.c && ebVar2.b == ebVar.b && ebVar2.a == ebVar.a;
                    z2 = !z2;
                }
                "collect cell?: " + z2;
                z = z2;
                ebVar = ebVar2;
            } else {
                ebVar = ebVar2;
                z = true;
            }
        }
        if (z) {
            this.b.a = ebVar;
        }
        return z;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    protected final boolean b(Location location) {
        int i = 0;
        if (this.e == null) {
            return false;
        }
        boolean z;
        List a = this.e.a(false);
        if (a.size() < 2) {
            a = null;
            z = false;
        } else {
            boolean z2;
            List list = (List) a.get(1);
            if (this.a.b != null) {
                if (list != null && list.size() > 0) {
                    z2 = location.distanceTo(this.a.b) > ((float) c);
                    if (!z2) {
                        int i2;
                        List list2 = this.a.a;
                        float f = f;
                        if (!(list == null || list2 == null || list == null || list2 == null)) {
                            int size = list.size();
                            int size2 = list2.size();
                            float f2 = (float) (size + size2);
                            if (size == 0) {
                                if (size2 != 0) {
                                }
                                i2 = 1;
                                if (i2 != 0) {
                                    z2 = false;
                                }
                            }
                            if (!(size == 0 || size2 == 0)) {
                                int i3 = 0;
                                int i4 = 0;
                                while (i3 < size) {
                                    String str = ((ScanResult) list.get(i3)).BSSID;
                                    if (str != null) {
                                        for (int i5 = 0; i5 < size2; i5++) {
                                            if (str.equals(((ec) list2.get(i5)).a)) {
                                                i2 = i4 + 1;
                                                break;
                                            }
                                        }
                                    }
                                    i2 = i4;
                                    i3++;
                                    i4 = i2;
                                }
                            }
                        }
                        i2 = 0;
                        if (i2 != 0) {
                            z2 = false;
                        }
                    }
                    z = z2;
                    a = list;
                } else {
                    a = list;
                    z = false;
                }
            }
            z2 = true;
            z = z2;
            a = list;
        }
        if (z) {
            this.a.a.clear();
            int size3 = a.size();
            while (i < size3) {
                this.a.a.add(new ec(((ScanResult) a.get(i)).BSSID));
                i++;
            }
        }
        return z;
    }
}
