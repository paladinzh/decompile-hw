package com.amap.api.services.core;

import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/* compiled from: ANRLogWriter */
class ba extends bi {
    private String[] a = new String[10];
    private int b = 0;
    private boolean c = false;
    private int d = 0;
    private a e;

    /* compiled from: ANRLogWriter */
    private class a implements bn {
        final /* synthetic */ ba a;
        private ak b;

        private a(ba baVar, ak akVar) {
            this.a = baVar;
            this.b = akVar;
        }

        public void a(String str) {
            try {
                this.b.b(str, this.a.a());
            } catch (Throwable th) {
                th.printStackTrace();
            }
        }
    }

    ba() {
    }

    protected int a() {
        return 2;
    }

    protected String b() {
        return bf.d;
    }

    protected String a(String str) {
        return ab.b(str);
    }

    protected bn a(ak akVar) {
        try {
            if (this.e == null) {
                this.e = new a(akVar);
            }
        } catch (Throwable th) {
            ay.a(th, "ANRWriter", "getListener");
            th.printStackTrace();
        }
        return this.e;
    }

    protected String a(List<ad> list) {
        bo boVar;
        bo boVar2;
        InputStream inputStream;
        Throwable e;
        InputStream fileInputStream;
        try {
            File file = new File("/data/anr/traces.txt");
            if (!file.exists()) {
                return null;
            }
            fileInputStream = new FileInputStream(file);
            try {
                boVar = new bo(fileInputStream, bp.a);
                Object obj = null;
                while (true) {
                    try {
                        String str;
                        Object obj2;
                        String a = boVar.a();
                        if (a.contains("pid")) {
                            while (!a.contains("\"main\"")) {
                                a = boVar.a();
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
                            if (this.d == 5) {
                                break;
                            } else if (this.c) {
                                this.d++;
                            } else {
                                for (ad adVar : list) {
                                    this.c = a(adVar.f(), str);
                                    if (this.c) {
                                        a(adVar);
                                    }
                                }
                            }
                        }
                    } catch (EOFException e2) {
                    } catch (FileNotFoundException e3) {
                        boVar2 = boVar;
                        inputStream = fileInputStream;
                    } catch (IOException e4) {
                        e = e4;
                    }
                }
                if (boVar != null) {
                    try {
                        boVar.close();
                    } catch (Throwable e5) {
                        ay.a(e5, "ANRWriter", "initLog1");
                        e5.printStackTrace();
                    } catch (Throwable e52) {
                        ay.a(e52, "ANRWriter", "initLog2");
                        e52.printStackTrace();
                    }
                }
                if (fileInputStream != null) {
                    try {
                        fileInputStream.close();
                    } catch (Throwable e522) {
                        ay.a(e522, "ANRWriter", "initLog3");
                        e522.printStackTrace();
                    } catch (Throwable e5222) {
                        ay.a(e5222, "ANRWriter", "initLog4");
                        e5222.printStackTrace();
                    }
                }
            } catch (FileNotFoundException e6) {
                boVar2 = null;
                inputStream = fileInputStream;
                if (boVar2 != null) {
                    try {
                        boVar2.close();
                    } catch (Throwable e52222) {
                        ay.a(e52222, "ANRWriter", "initLog1");
                        e52222.printStackTrace();
                    } catch (Throwable e522222) {
                        ay.a(e522222, "ANRWriter", "initLog2");
                        e522222.printStackTrace();
                    }
                }
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (Throwable e5222222) {
                        ay.a(e5222222, "ANRWriter", "initLog3");
                        e5222222.printStackTrace();
                    } catch (Throwable e52222222) {
                        ay.a(e52222222, "ANRWriter", "initLog4");
                        e52222222.printStackTrace();
                    }
                }
                if (this.c) {
                    return null;
                }
                return c();
            } catch (IOException e7) {
                e52222222 = e7;
                boVar = null;
                try {
                    ay.a(e52222222, "ANRWriter", "initLog");
                    e52222222.printStackTrace();
                    if (boVar != null) {
                        try {
                            boVar.close();
                        } catch (Throwable e522222222) {
                            ay.a(e522222222, "ANRWriter", "initLog1");
                            e522222222.printStackTrace();
                        } catch (Throwable e5222222222) {
                            ay.a(e5222222222, "ANRWriter", "initLog2");
                            e5222222222.printStackTrace();
                        }
                    }
                    if (fileInputStream != null) {
                        try {
                            fileInputStream.close();
                        } catch (Throwable e52222222222) {
                            ay.a(e52222222222, "ANRWriter", "initLog3");
                            e52222222222.printStackTrace();
                        } catch (Throwable e522222222222) {
                            ay.a(e522222222222, "ANRWriter", "initLog4");
                            e522222222222.printStackTrace();
                        }
                    }
                    if (this.c) {
                        return null;
                    }
                    return c();
                } catch (Throwable th) {
                    e522222222222 = th;
                    if (boVar != null) {
                        try {
                            boVar.close();
                        } catch (Throwable e8) {
                            ay.a(e8, "ANRWriter", "initLog1");
                            e8.printStackTrace();
                        } catch (Throwable e82) {
                            ay.a(e82, "ANRWriter", "initLog2");
                            e82.printStackTrace();
                        }
                    }
                    if (fileInputStream != null) {
                        try {
                            fileInputStream.close();
                        } catch (Throwable e822) {
                            ay.a(e822, "ANRWriter", "initLog3");
                            e822.printStackTrace();
                        } catch (Throwable e8222) {
                            ay.a(e8222, "ANRWriter", "initLog4");
                            e8222.printStackTrace();
                        }
                    }
                    throw e522222222222;
                }
            } catch (Throwable th2) {
                e522222222222 = th2;
                boVar = null;
                if (boVar != null) {
                    boVar.close();
                }
                if (fileInputStream != null) {
                    fileInputStream.close();
                }
                throw e522222222222;
            }
            if (this.c) {
                return null;
            }
            return c();
        } catch (FileNotFoundException e9) {
            boVar2 = null;
            inputStream = null;
            if (boVar2 != null) {
                boVar2.close();
            }
            if (inputStream != null) {
                inputStream.close();
            }
            if (this.c) {
                return c();
            }
            return null;
        } catch (IOException e10) {
            e522222222222 = e10;
            boVar = null;
            fileInputStream = null;
            ay.a(e522222222222, "ANRWriter", "initLog");
            e522222222222.printStackTrace();
            if (boVar != null) {
                boVar.close();
            }
            if (fileInputStream != null) {
                fileInputStream.close();
            }
            if (this.c) {
                return null;
            }
            return c();
        } catch (Throwable th3) {
            e522222222222 = th3;
            boVar = null;
            fileInputStream = null;
            if (boVar != null) {
                boVar.close();
            }
            if (fileInputStream != null) {
                fileInputStream.close();
            }
            throw e522222222222;
        }
    }

    private String c() {
        StringBuilder stringBuilder = new StringBuilder();
        try {
            int i = this.b;
            while (i < 10 && i <= 9) {
                stringBuilder.append(this.a[i]);
                i++;
            }
            for (i = 0; i < this.b; i++) {
                stringBuilder.append(this.a[i]);
            }
        } catch (Throwable th) {
            ay.a(th, "ANRWriter", "getLogInfo");
            th.printStackTrace();
        }
        return stringBuilder.toString();
    }

    private void b(String str) {
        try {
            if (this.b > 9) {
                this.b = 0;
            }
            this.a[this.b] = str;
            this.b++;
        } catch (Throwable th) {
            ay.a(th, "ANRWriter", "addData");
            th.printStackTrace();
        }
    }
}
