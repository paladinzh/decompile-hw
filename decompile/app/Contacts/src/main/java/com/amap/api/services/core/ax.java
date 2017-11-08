package com.amap.api.services.core;

import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/* compiled from: ANRLogWriter */
class ax extends bg {
    private String[] a = new String[10];
    private int b = 0;
    private boolean c = false;
    private int d = 0;
    private a e;

    /* compiled from: ANRLogWriter */
    private class a implements cc {
        final /* synthetic */ ax a;
        private bq b;

        private a(ax axVar, bq bqVar) {
            this.a = axVar;
            this.b = bqVar;
        }

        public void a(String str) {
            try {
                this.b.b(str, this.a.a());
            } catch (Throwable th) {
                th.printStackTrace();
            }
        }
    }

    ax() {
    }

    protected int a() {
        return 2;
    }

    protected String b() {
        return bd.d;
    }

    protected String a(String str) {
        return ap.b(str);
    }

    protected cc a(bq bqVar) {
        try {
            if (this.e == null) {
                this.e = new a(bqVar);
            }
        } catch (Throwable th) {
            av.a(th, "ANRWriter", "getListener");
            th.printStackTrace();
        }
        return this.e;
    }

    protected String a(List<ar> list) {
        InputStream fileInputStream;
        cd cdVar;
        cd cdVar2;
        InputStream inputStream;
        Throwable e;
        try {
            File file = new File("/data/anr/traces.txt");
            if (!file.exists()) {
                return null;
            }
            fileInputStream = new FileInputStream(file);
            try {
                cdVar = new cd(fileInputStream, ce.a);
                Object obj = null;
                while (true) {
                    try {
                        String str;
                        Object obj2;
                        String a = cdVar.a();
                        if (a.contains("pid")) {
                            while (!a.contains("\"main\"")) {
                                a = cdVar.a();
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
                                for (ar arVar : list) {
                                    this.c = a(arVar.f(), str);
                                    if (this.c) {
                                        a(arVar);
                                    }
                                }
                            }
                        }
                    } catch (EOFException e2) {
                    } catch (FileNotFoundException e3) {
                        cdVar2 = cdVar;
                        inputStream = fileInputStream;
                    } catch (IOException e4) {
                        e = e4;
                    }
                }
                if (cdVar != null) {
                    try {
                        cdVar.close();
                    } catch (Throwable e5) {
                        av.a(e5, "ANRWriter", "initLog1");
                        e5.printStackTrace();
                    } catch (Throwable e52) {
                        av.a(e52, "ANRWriter", "initLog2");
                        e52.printStackTrace();
                    }
                }
                if (fileInputStream != null) {
                    try {
                        fileInputStream.close();
                    } catch (Throwable e522) {
                        av.a(e522, "ANRWriter", "initLog3");
                        e522.printStackTrace();
                    } catch (Throwable e5222) {
                        av.a(e5222, "ANRWriter", "initLog4");
                        e5222.printStackTrace();
                    }
                }
            } catch (FileNotFoundException e6) {
                cdVar2 = null;
                inputStream = fileInputStream;
                if (cdVar2 != null) {
                    try {
                        cdVar2.close();
                    } catch (Throwable e52222) {
                        av.a(e52222, "ANRWriter", "initLog1");
                        e52222.printStackTrace();
                    } catch (Throwable e522222) {
                        av.a(e522222, "ANRWriter", "initLog2");
                        e522222.printStackTrace();
                    }
                }
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (Throwable e5222222) {
                        av.a(e5222222, "ANRWriter", "initLog3");
                        e5222222.printStackTrace();
                    } catch (Throwable e52222222) {
                        av.a(e52222222, "ANRWriter", "initLog4");
                        e52222222.printStackTrace();
                    }
                }
                if (this.c) {
                    return null;
                }
                return c();
            } catch (IOException e7) {
                e52222222 = e7;
                cdVar = null;
                try {
                    av.a(e52222222, "ANRWriter", "initLog");
                    e52222222.printStackTrace();
                    if (cdVar != null) {
                        try {
                            cdVar.close();
                        } catch (Throwable e522222222) {
                            av.a(e522222222, "ANRWriter", "initLog1");
                            e522222222.printStackTrace();
                        } catch (Throwable e5222222222) {
                            av.a(e5222222222, "ANRWriter", "initLog2");
                            e5222222222.printStackTrace();
                        }
                    }
                    if (fileInputStream != null) {
                        try {
                            fileInputStream.close();
                        } catch (Throwable e52222222222) {
                            av.a(e52222222222, "ANRWriter", "initLog3");
                            e52222222222.printStackTrace();
                        } catch (Throwable e522222222222) {
                            av.a(e522222222222, "ANRWriter", "initLog4");
                            e522222222222.printStackTrace();
                        }
                    }
                    if (this.c) {
                        return null;
                    }
                    return c();
                } catch (Throwable th) {
                    e522222222222 = th;
                    if (cdVar != null) {
                        try {
                            cdVar.close();
                        } catch (Throwable e8) {
                            av.a(e8, "ANRWriter", "initLog1");
                            e8.printStackTrace();
                        } catch (Throwable e82) {
                            av.a(e82, "ANRWriter", "initLog2");
                            e82.printStackTrace();
                        }
                    }
                    if (fileInputStream != null) {
                        try {
                            fileInputStream.close();
                        } catch (Throwable e822) {
                            av.a(e822, "ANRWriter", "initLog3");
                            e822.printStackTrace();
                        } catch (Throwable e8222) {
                            av.a(e8222, "ANRWriter", "initLog4");
                            e8222.printStackTrace();
                        }
                    }
                    throw e522222222222;
                }
            } catch (Throwable th2) {
                e522222222222 = th2;
                cdVar = null;
                if (cdVar != null) {
                    cdVar.close();
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
            cdVar2 = null;
            inputStream = null;
            if (cdVar2 != null) {
                cdVar2.close();
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
            cdVar = null;
            fileInputStream = null;
            av.a(e522222222222, "ANRWriter", "initLog");
            e522222222222.printStackTrace();
            if (cdVar != null) {
                cdVar.close();
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
            cdVar = null;
            fileInputStream = null;
            if (cdVar != null) {
                cdVar.close();
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
            av.a(th, "ANRWriter", "getLogInfo");
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
            av.a(th, "ANRWriter", "addData");
            th.printStackTrace();
        }
    }
}
