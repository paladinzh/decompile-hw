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
public class fp extends fs {
    private static boolean a = true;
    private String[] b = new String[10];
    private int c = 0;
    private boolean d = false;
    private int e = 0;

    protected fp(int i) {
        super(i);
    }

    protected boolean a(Context context) {
        if (fc.m(context) != 1 || !a) {
            return false;
        }
        a = false;
        synchronized (Looper.getMainLooper()) {
            gf gfVar = new gf(context);
            gg a = gfVar.a();
            if (a == null) {
                return true;
            } else if (a.c()) {
                a.c(false);
                gfVar.a(a);
                return true;
            } else {
                return false;
            }
        }
    }

    protected String a(List<fh> list) {
        InputStream fileInputStream;
        gv gvVar;
        gv gvVar2;
        InputStream inputStream;
        Throwable e;
        try {
            File file = new File("/data/anr/traces.txt");
            if (!file.exists()) {
                return null;
            }
            fileInputStream = new FileInputStream(file);
            try {
                gvVar = new gv(fileInputStream, gw.a);
                Object obj = null;
                while (true) {
                    try {
                        String str;
                        Object obj2;
                        String a = gvVar.a();
                        if (a.contains("pid")) {
                            while (!a.contains("\"main\"")) {
                                a = gvVar.a();
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
                                for (fh fhVar : list) {
                                    this.d = fs.b(fhVar.f(), str);
                                    if (this.d) {
                                        a(fhVar);
                                    }
                                }
                            }
                        }
                    } catch (EOFException e2) {
                    } catch (FileNotFoundException e3) {
                        gvVar2 = gvVar;
                        inputStream = fileInputStream;
                    } catch (IOException e4) {
                        e = e4;
                    }
                }
                if (gvVar != null) {
                    try {
                        gvVar.close();
                    } catch (Throwable e5) {
                        fl.a(e5, "ANRWriter", "initLog1");
                    } catch (Throwable e52) {
                        fl.a(e52, "ANRWriter", "initLog2");
                    }
                }
                if (fileInputStream != null) {
                    try {
                        fileInputStream.close();
                    } catch (Throwable e522) {
                        fl.a(e522, "ANRWriter", "initLog3");
                    } catch (Throwable e5222) {
                        fl.a(e5222, "ANRWriter", "initLog4");
                    }
                }
            } catch (FileNotFoundException e6) {
                gvVar2 = null;
                inputStream = fileInputStream;
                if (gvVar2 != null) {
                    try {
                        gvVar2.close();
                    } catch (Throwable e52222) {
                        fl.a(e52222, "ANRWriter", "initLog1");
                    } catch (Throwable e522222) {
                        fl.a(e522222, "ANRWriter", "initLog2");
                    }
                }
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (Throwable e5222222) {
                        fl.a(e5222222, "ANRWriter", "initLog3");
                    } catch (Throwable e52222222) {
                        fl.a(e52222222, "ANRWriter", "initLog4");
                    }
                }
                if (this.d) {
                    return null;
                }
                return d();
            } catch (IOException e7) {
                e52222222 = e7;
                gvVar = null;
                try {
                    fl.a(e52222222, "ANRWriter", "initLog");
                    if (gvVar != null) {
                        try {
                            gvVar.close();
                        } catch (Throwable e522222222) {
                            fl.a(e522222222, "ANRWriter", "initLog1");
                        } catch (Throwable e5222222222) {
                            fl.a(e5222222222, "ANRWriter", "initLog2");
                        }
                    }
                    if (fileInputStream != null) {
                        try {
                            fileInputStream.close();
                        } catch (Throwable e52222222222) {
                            fl.a(e52222222222, "ANRWriter", "initLog3");
                        } catch (Throwable e522222222222) {
                            fl.a(e522222222222, "ANRWriter", "initLog4");
                        }
                    }
                    if (this.d) {
                        return null;
                    }
                    return d();
                } catch (Throwable th) {
                    e522222222222 = th;
                    if (gvVar != null) {
                        try {
                            gvVar.close();
                        } catch (Throwable e8) {
                            fl.a(e8, "ANRWriter", "initLog1");
                        } catch (Throwable e82) {
                            fl.a(e82, "ANRWriter", "initLog2");
                        }
                    }
                    if (fileInputStream != null) {
                        try {
                            fileInputStream.close();
                        } catch (Throwable e822) {
                            fl.a(e822, "ANRWriter", "initLog3");
                        } catch (Throwable e8222) {
                            fl.a(e8222, "ANRWriter", "initLog4");
                        }
                    }
                    throw e522222222222;
                }
            } catch (Throwable th2) {
                e522222222222 = th2;
                gvVar = null;
                if (gvVar != null) {
                    gvVar.close();
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
            gvVar2 = null;
            inputStream = null;
            if (gvVar2 != null) {
                gvVar2.close();
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
            gvVar = null;
            fileInputStream = null;
            fl.a(e522222222222, "ANRWriter", "initLog");
            if (gvVar != null) {
                gvVar.close();
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
            gvVar = null;
            fileInputStream = null;
            if (gvVar != null) {
                gvVar.close();
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
            fl.a(th, "ANRWriter", "getLogInfo");
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
            fl.a(th, "ANRWriter", "addData");
        }
    }
}
