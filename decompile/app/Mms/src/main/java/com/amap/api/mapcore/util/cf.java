package com.amap.api.mapcore.util;

import android.content.Context;
import android.os.Looper;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/* compiled from: AnrLogProcessor */
public class cf extends ci {
    private static boolean a = true;
    private String[] b = new String[10];
    private int c = 0;
    private boolean d = false;
    private int e = 0;

    protected cf(int i) {
        super(i);
    }

    protected boolean a(Context context) {
        if (bq.m(context) != 1 || !a) {
            return false;
        }
        a = false;
        synchronized (Looper.getMainLooper()) {
            cv cvVar = new cv(context);
            cw a = cvVar.a();
            if (a == null) {
                return true;
            } else if (a.c()) {
                a.c(false);
                cvVar.a(a);
                return true;
            } else {
                return false;
            }
        }
    }

    protected String a(List<bv> list) {
        InputStream fileInputStream;
        db dbVar;
        db dbVar2;
        InputStream inputStream;
        Throwable e;
        try {
            File file = new File("/data/anr/traces.txt");
            if (!file.exists()) {
                return null;
            }
            fileInputStream = new FileInputStream(file);
            try {
                dbVar = new db(fileInputStream, dc.a);
                Object obj = null;
                while (true) {
                    try {
                        String str;
                        Object obj2;
                        String a = dbVar.a();
                        if (a.contains("pid")) {
                            while (!a.contains("\"main\"")) {
                                a = dbVar.a();
                            }
                            str = a;
                            int i = 1;
                        } else {
                            str = a;
                            obj2 = obj;
                        }
                        if (str.equals("")) {
                            obj = null;
                        } else {
                            obj = obj2;
                        }
                        if (obj != null) {
                            b(str);
                            if (this.e == 5) {
                                break;
                            } else if (this.d) {
                                this.e++;
                            } else {
                                for (bv bvVar : list) {
                                    this.d = a(bvVar.e(), str);
                                    if (this.d) {
                                        a(bvVar);
                                    }
                                }
                            }
                        }
                    } catch (EOFException e2) {
                    } catch (FileNotFoundException e3) {
                        dbVar2 = dbVar;
                        inputStream = fileInputStream;
                    } catch (IOException e4) {
                        e = e4;
                    }
                }
                if (dbVar != null) {
                    try {
                        dbVar.close();
                    } catch (Throwable e5) {
                        cb.a(e5, "ANRWriter", "initLog1");
                    } catch (Throwable e52) {
                        cb.a(e52, "ANRWriter", "initLog2");
                    }
                }
                if (fileInputStream != null) {
                    try {
                        fileInputStream.close();
                    } catch (Throwable e522) {
                        cb.a(e522, "ANRWriter", "initLog3");
                    } catch (Throwable e5222) {
                        cb.a(e5222, "ANRWriter", "initLog4");
                    }
                }
            } catch (FileNotFoundException e6) {
                dbVar2 = null;
                inputStream = fileInputStream;
                if (dbVar2 != null) {
                    try {
                        dbVar2.close();
                    } catch (Throwable e52222) {
                        cb.a(e52222, "ANRWriter", "initLog1");
                    } catch (Throwable e522222) {
                        cb.a(e522222, "ANRWriter", "initLog2");
                    }
                }
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (Throwable e5222222) {
                        cb.a(e5222222, "ANRWriter", "initLog3");
                    } catch (Throwable e52222222) {
                        cb.a(e52222222, "ANRWriter", "initLog4");
                    }
                }
                if (this.d) {
                    return null;
                }
                return d();
            } catch (IOException e7) {
                e52222222 = e7;
                dbVar = null;
                try {
                    cb.a(e52222222, "ANRWriter", "initLog");
                    if (dbVar != null) {
                        try {
                            dbVar.close();
                        } catch (Throwable e522222222) {
                            cb.a(e522222222, "ANRWriter", "initLog1");
                        } catch (Throwable e5222222222) {
                            cb.a(e5222222222, "ANRWriter", "initLog2");
                        }
                    }
                    if (fileInputStream != null) {
                        try {
                            fileInputStream.close();
                        } catch (Throwable e52222222222) {
                            cb.a(e52222222222, "ANRWriter", "initLog3");
                        } catch (Throwable e522222222222) {
                            cb.a(e522222222222, "ANRWriter", "initLog4");
                        }
                    }
                    if (this.d) {
                        return null;
                    }
                    return d();
                } catch (Throwable th) {
                    e522222222222 = th;
                    if (dbVar != null) {
                        try {
                            dbVar.close();
                        } catch (Throwable e8) {
                            cb.a(e8, "ANRWriter", "initLog1");
                        } catch (Throwable e82) {
                            cb.a(e82, "ANRWriter", "initLog2");
                        }
                    }
                    if (fileInputStream != null) {
                        try {
                            fileInputStream.close();
                        } catch (Throwable e822) {
                            cb.a(e822, "ANRWriter", "initLog3");
                        } catch (Throwable e8222) {
                            cb.a(e8222, "ANRWriter", "initLog4");
                        }
                    }
                    throw e522222222222;
                }
            } catch (Throwable th2) {
                e522222222222 = th2;
                dbVar = null;
                if (dbVar != null) {
                    dbVar.close();
                }
                if (fileInputStream != null) {
                    fileInputStream.close();
                }
                throw e522222222222;
            }
            if (this.d) {
                return null;
            }
            return d();
        } catch (FileNotFoundException e9) {
            dbVar2 = null;
            inputStream = null;
            if (dbVar2 != null) {
                dbVar2.close();
            }
            if (inputStream != null) {
                inputStream.close();
            }
            if (this.d) {
                return d();
            }
            return null;
        } catch (IOException e10) {
            e522222222222 = e10;
            dbVar = null;
            fileInputStream = null;
            cb.a(e522222222222, "ANRWriter", "initLog");
            if (dbVar != null) {
                dbVar.close();
            }
            if (fileInputStream != null) {
                fileInputStream.close();
            }
            if (this.d) {
                return null;
            }
            return d();
        } catch (Throwable th3) {
            e522222222222 = th3;
            dbVar = null;
            fileInputStream = null;
            if (dbVar != null) {
                dbVar.close();
            }
            if (fileInputStream != null) {
                fileInputStream.close();
            }
            throw e522222222222;
        }
    }

    private String d() {
        StringBuilder stringBuilder = new StringBuilder();
        try {
            int i = this.c;
            while (i < 10 && i <= 9) {
                stringBuilder.append(this.b[i]);
                i++;
            }
            for (i = 0; i < this.c; i++) {
                stringBuilder.append(this.b[i]);
            }
        } catch (Throwable th) {
            cb.a(th, "ANRWriter", "getLogInfo");
        }
        return stringBuilder.toString();
    }

    private void b(String str) {
        try {
            if (this.c > 9) {
                this.c = 0;
            }
            this.b[this.c] = str;
            this.c++;
        } catch (Throwable th) {
            cb.a(th, "ANRWriter", "addData");
        }
    }
}
