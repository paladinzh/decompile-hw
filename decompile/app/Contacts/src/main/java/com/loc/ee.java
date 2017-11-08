package com.loc;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.location.places.Place;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.zip.GZIPInputStream;

/* compiled from: Unknown */
public final class ee {
    private RandomAccessFile a;
    private di b;
    private File c = null;

    protected ee(di diVar) {
        this.b = diVar;
    }

    private static byte a(byte[] bArr) {
        byte[] bArr2 = null;
        try {
            InputStream byteArrayInputStream = new ByteArrayInputStream(bArr);
            GZIPInputStream gZIPInputStream = new GZIPInputStream(byteArrayInputStream);
            byte[] bArr3 = new byte[Place.TYPE_SUBLOCALITY_LEVEL_2];
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            while (true) {
                int read = gZIPInputStream.read(bArr3, 0, bArr3.length);
                if (read == -1) {
                    break;
                }
                byteArrayOutputStream.write(bArr3, 0, read);
            }
            bArr2 = byteArrayOutputStream.toByteArray();
            byteArrayOutputStream.flush();
            byteArrayOutputStream.close();
            gZIPInputStream.close();
            byteArrayInputStream.close();
        } catch (Exception e) {
        }
        return bArr2[0];
    }

    private static int a(int i, int i2, int i3) {
        int i4 = ((i3 - 1) * ConnectionResult.DRIVE_EXTERNAL_STORAGE_REQUIRED) + i;
        while (i4 >= i2) {
            i4 -= 1500;
        }
        return i4;
    }

    private int a(BitSet bitSet) {
        for (int i = 0; i < bitSet.length(); i++) {
            if (bitSet.get(i)) {
                return this.b.a() + ((i * ConnectionResult.DRIVE_EXTERNAL_STORAGE_REQUIRED) + 4);
            }
        }
        return 0;
    }

    private ArrayList a(int i, int i2) {
        ArrayList arrayList = new ArrayList();
        while (i <= i2) {
            try {
                this.a.seek((long) i);
                int readInt = this.a.readInt();
                this.a.readLong();
                if (readInt <= 0 || readInt > ConnectionResult.DRIVE_EXTERNAL_STORAGE_REQUIRED) {
                    return null;
                }
                byte[] bArr = new byte[readInt];
                this.a.read(bArr);
                byte a = a(bArr);
                if (a != (byte) 3 && a != (byte) 4 && a != (byte) 41) {
                    return null;
                }
                arrayList.add(bArr);
                i += ConnectionResult.DRIVE_EXTERNAL_STORAGE_REQUIRED;
            } catch (IOException e) {
            }
        }
        return arrayList;
    }

    private BitSet b() {
        BitSet bitSet = null;
        byte[] bArr = new byte[this.b.a()];
        try {
            this.a.read(bArr);
            bitSet = di.b(bArr);
        } catch (IOException e) {
        }
        return bitSet;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    protected final int a() {
        int i = 0;
        synchronized (this) {
            this.c = this.b.b();
            try {
                if (this.c != null) {
                    this.a = new RandomAccessFile(this.b.b(), "rw");
                    byte[] bArr = new byte[this.b.a()];
                    if (dl.c && this.a != null) {
                        this.a.close();
                        if (this.a != null) {
                            try {
                                this.a.close();
                            } catch (IOException e) {
                            }
                        }
                    }
                    this.a.read(bArr);
                    BitSet b = di.b(bArr);
                    for (int i2 = 0; i2 < b.size(); i2++) {
                        if (b.get(i2)) {
                            i++;
                        }
                    }
                }
                if (this.a != null) {
                    try {
                        this.a.close();
                    } catch (IOException e2) {
                    }
                }
            } catch (FileNotFoundException e3) {
                if (this.a != null) {
                    try {
                        this.a.close();
                    } catch (IOException e4) {
                    }
                }
            } catch (IOException e5) {
                if (this.a != null) {
                    try {
                        this.a.close();
                    } catch (IOException e6) {
                    }
                }
            } catch (NullPointerException e7) {
                if (this.a != null) {
                    try {
                        this.a.close();
                    } catch (IOException e8) {
                    }
                }
            } catch (Throwable th) {
                if (this.a != null) {
                    try {
                        this.a.close();
                    } catch (IOException e9) {
                    }
                }
            }
            this.c = null;
            return i;
        }
        return 0;
    }

    protected final synchronized dh a(int i) {
        dh dhVar;
        if (this.b == null) {
            return null;
        }
        synchronized (this) {
            this.c = this.b.b();
            if (this.c != null) {
                try {
                    this.a = new RandomAccessFile(this.c, "rw");
                    if (dl.c) {
                        if (this.a != null) {
                            try {
                                this.a.close();
                                if (this.a != null) {
                                    try {
                                        this.a.close();
                                    } catch (Exception e) {
                                    }
                                }
                            } catch (IOException e2) {
                            }
                        }
                    }
                    BitSet b = b();
                    if (b != null) {
                        int a = a(b);
                        ArrayList a2 = a(a, a(a, (int) this.c.length(), i));
                        if (a2 != null) {
                            dhVar = new dh(this.c, a2, new int[]{((a - this.b.a()) - 4) / ConnectionResult.DRIVE_EXTERNAL_STORAGE_REQUIRED, ((r2 - this.b.a()) - 4) / ConnectionResult.DRIVE_EXTERNAL_STORAGE_REQUIRED});
                            if (this.a != null) {
                                try {
                                    this.a.close();
                                } catch (Exception e3) {
                                }
                            }
                            if (dhVar != null) {
                                if (dhVar.c() > 100 && dhVar.c() < 5242880) {
                                    return dhVar;
                                }
                            }
                            this.c.delete();
                            this.c = null;
                            return null;
                        }
                        this.c.delete();
                        if (this.a != null) {
                            try {
                                this.a.close();
                            } catch (Exception e4) {
                            }
                        }
                    } else {
                        this.c.delete();
                        if (this.a != null) {
                            try {
                                this.a.close();
                            } catch (Exception e5) {
                            }
                        }
                    }
                } catch (FileNotFoundException e6) {
                    if (this.a != null) {
                        try {
                            this.a.close();
                        } catch (Exception e7) {
                        }
                    }
                    dhVar = null;
                    if (dhVar != null) {
                        return dhVar;
                    }
                    this.c.delete();
                    this.c = null;
                    return null;
                } catch (Exception e8) {
                    if (this.a != null) {
                        try {
                            this.a.close();
                        } catch (Exception e9) {
                        }
                    }
                    dhVar = null;
                    if (dhVar != null) {
                        return dhVar;
                    }
                    this.c.delete();
                    this.c = null;
                    return null;
                } catch (Throwable th) {
                    if (this.a != null) {
                        try {
                            this.a.close();
                        } catch (Exception e10) {
                        }
                    }
                }
            } else {
                return null;
            }
        }
        return null;
        return null;
        return null;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    protected final synchronized void a(dh dhVar) {
        BitSet bitSet = null;
        synchronized (this) {
            synchronized (this) {
                this.c = dhVar.a;
                if (this.c != null) {
                    byte[] bArr;
                    try {
                        this.a = new RandomAccessFile(this.c, "rw");
                        bArr = new byte[this.b.a()];
                        if (dl.c) {
                            if (this.a != null) {
                                this.a.close();
                                if (this.a != null) {
                                    try {
                                        this.a.close();
                                    } catch (IOException e) {
                                    }
                                }
                            }
                        }
                    } catch (IOException e2) {
                    } catch (FileNotFoundException e3) {
                        if (this.a != null) {
                            try {
                                this.a.close();
                            } catch (IOException e4) {
                            }
                        }
                    } catch (Throwable th) {
                        if (this.a != null) {
                            try {
                                this.a.close();
                            } catch (IOException e5) {
                            }
                        }
                    }
                    this.a.read(bArr);
                    bitSet = di.b(bArr);
                    if (dhVar.b()) {
                        for (int i = dhVar.b[0]; i <= dhVar.b[1]; i++) {
                            bitSet.set(i, false);
                        }
                        this.a.seek(0);
                        this.a.write(di.a(bitSet));
                    }
                    if (this.a != null) {
                        try {
                            this.a.close();
                        } catch (IOException e6) {
                        }
                    }
                    if (bitSet.isEmpty()) {
                        this.c.delete();
                    }
                    this.c = null;
                    return;
                }
                return;
            }
        }
    }
}
