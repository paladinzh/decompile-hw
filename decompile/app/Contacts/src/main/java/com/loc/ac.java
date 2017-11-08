package com.loc;

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
public class ac extends ag {
    private static boolean a = true;
    private String[] b = new String[10];
    private int c = 0;
    private boolean d = false;
    private int e = 0;

    protected ac(int i) {
        super(i);
    }

    private void b(String str) {
        try {
            if (this.c > 9) {
                this.c = 0;
            }
            this.b[this.c] = str;
            this.c++;
        } catch (Throwable th) {
            aa.a(th, "ANRWriter", "addData");
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
            aa.a(th, "ANRWriter", "getLogInfo");
        }
        return stringBuilder.toString();
    }

    protected String a(List<v> list) {
        InputStream fileInputStream;
        bm bmVar;
        InputStream inputStream;
        Throwable e;
        bm bmVar2;
        try {
            File file = new File("/data/anr/traces.txt");
            if (!file.exists()) {
                return null;
            }
            fileInputStream = new FileInputStream(file);
            try {
                bmVar2 = new bm(fileInputStream, bn.a);
                Object obj = null;
                while (true) {
                    try {
                        String str;
                        Object obj2;
                        String a = bmVar2.a();
                        if (a.contains("pid")) {
                            while (!a.contains("\"main\"")) {
                                a = bmVar2.a();
                            }
                            str = a;
                            int i = 1;
                        } else {
                            str = a;
                            obj2 = obj;
                        }
                        obj = !str.equals("") ? obj2 : null;
                        if (obj != null) {
                            b(str);
                            if (this.e == 5) {
                                break;
                            } else if (this.d) {
                                this.e++;
                            } else {
                                for (v vVar : list) {
                                    this.d = a(vVar.f(), str);
                                    if (this.d) {
                                        a(vVar);
                                    }
                                }
                            }
                        }
                    } catch (EOFException e2) {
                    } catch (FileNotFoundException e3) {
                        bmVar = bmVar2;
                        inputStream = fileInputStream;
                    } catch (IOException e4) {
                        e = e4;
                    }
                }
                if (bmVar2 != null) {
                    try {
                        bmVar2.close();
                    } catch (Throwable e5) {
                        aa.a(e5, "ANRWriter", "initLog1");
                    } catch (Throwable e52) {
                        aa.a(e52, "ANRWriter", "initLog2");
                    }
                }
                if (fileInputStream != null) {
                    try {
                        fileInputStream.close();
                    } catch (Throwable e522) {
                        aa.a(e522, "ANRWriter", "initLog3");
                    } catch (Throwable e5222) {
                        aa.a(e5222, "ANRWriter", "initLog4");
                    }
                }
            } catch (FileNotFoundException e6) {
                bmVar = null;
                inputStream = fileInputStream;
                if (bmVar != null) {
                    try {
                        bmVar.close();
                    } catch (Throwable e52222) {
                        aa.a(e52222, "ANRWriter", "initLog1");
                    } catch (Throwable e522222) {
                        aa.a(e522222, "ANRWriter", "initLog2");
                    }
                }
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (Throwable e5222222) {
                        aa.a(e5222222, "ANRWriter", "initLog3");
                    } catch (Throwable e52222222) {
                        aa.a(e52222222, "ANRWriter", "initLog4");
                    }
                }
                return this.d ? d() : null;
            } catch (IOException e7) {
                e52222222 = e7;
                bmVar2 = null;
                try {
                    aa.a(e52222222, "ANRWriter", "initLog");
                    if (bmVar2 != null) {
                        try {
                            bmVar2.close();
                        } catch (Throwable e522222222) {
                            aa.a(e522222222, "ANRWriter", "initLog1");
                        } catch (Throwable e5222222222) {
                            aa.a(e5222222222, "ANRWriter", "initLog2");
                        }
                    }
                    if (fileInputStream != null) {
                        try {
                            fileInputStream.close();
                        } catch (Throwable e52222222222) {
                            aa.a(e52222222222, "ANRWriter", "initLog3");
                        } catch (Throwable e522222222222) {
                            aa.a(e522222222222, "ANRWriter", "initLog4");
                        }
                    }
                    if (this.d) {
                    }
                } catch (Throwable th) {
                    e522222222222 = th;
                    if (bmVar2 != null) {
                        try {
                            bmVar2.close();
                        } catch (Throwable e8) {
                            aa.a(e8, "ANRWriter", "initLog1");
                        } catch (Throwable e82) {
                            aa.a(e82, "ANRWriter", "initLog2");
                        }
                    }
                    if (fileInputStream != null) {
                        try {
                            fileInputStream.close();
                        } catch (Throwable e822) {
                            aa.a(e822, "ANRWriter", "initLog3");
                        } catch (Throwable e8222) {
                            aa.a(e8222, "ANRWriter", "initLog4");
                        }
                    }
                    throw e522222222222;
                }
            } catch (Throwable th2) {
                e522222222222 = th2;
                bmVar2 = null;
                if (bmVar2 != null) {
                    bmVar2.close();
                }
                if (fileInputStream != null) {
                    fileInputStream.close();
                }
                throw e522222222222;
            }
            if (this.d) {
            }
        } catch (FileNotFoundException e9) {
            bmVar = null;
            inputStream = null;
            if (bmVar != null) {
                bmVar.close();
            }
            if (inputStream != null) {
                inputStream.close();
            }
            if (this.d) {
            }
        } catch (IOException e10) {
            e522222222222 = e10;
            bmVar2 = null;
            fileInputStream = null;
            aa.a(e522222222222, "ANRWriter", "initLog");
            if (bmVar2 != null) {
                bmVar2.close();
            }
            if (fileInputStream != null) {
                fileInputStream.close();
            }
            if (this.d) {
            }
        } catch (Throwable th3) {
            e522222222222 = th3;
            bmVar2 = null;
            fileInputStream = null;
            if (bmVar2 != null) {
                bmVar2.close();
            }
            if (fileInputStream != null) {
                fileInputStream.close();
            }
            throw e522222222222;
        }
    }

    protected boolean a(Context context) {
        if (q.m(context) != 1 || !a) {
            return false;
        }
        a = false;
        synchronized (Looper.getMainLooper()) {
            as asVar = new as(context);
            au a = asVar.a();
            if (a == null) {
                return true;
            } else if (a.c()) {
                a.c(false);
                asVar.a(a);
                return true;
            } else {
                return false;
            }
        }
    }
}
