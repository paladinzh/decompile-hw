package com.avast.android.shepherd.obfuscated;

import android.content.Context;
import com.avast.android.shepherd.obfuscated.bc.q;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/* compiled from: Unknown */
public class aa {
    public static q a(Context context) {
        FileInputStream fileInputStream;
        Throwable e;
        if (context == null) {
            return null;
        }
        File b = b(context);
        if (!b.exists()) {
            return null;
        }
        try {
            fileInputStream = new FileInputStream(b);
            try {
                q a = q.a((InputStream) fileInputStream);
                if (fileInputStream != null) {
                    try {
                        fileInputStream.close();
                    } catch (IOException e2) {
                    }
                }
                return a;
            } catch (FileNotFoundException e3) {
                e = e3;
                try {
                    x.b(e.getMessage(), e);
                    if (fileInputStream != null) {
                        try {
                            fileInputStream.close();
                        } catch (IOException e4) {
                        }
                    }
                    return null;
                } catch (Throwable th) {
                    e = th;
                    if (fileInputStream != null) {
                        try {
                            fileInputStream.close();
                        } catch (IOException e5) {
                        }
                    }
                    throw e;
                }
            } catch (IOException e6) {
                e = e6;
                x.b(e.getMessage(), e);
                if (fileInputStream != null) {
                    try {
                        fileInputStream.close();
                    } catch (IOException e7) {
                    }
                }
                return null;
            }
        } catch (FileNotFoundException e8) {
            e = e8;
            fileInputStream = null;
            x.b(e.getMessage(), e);
            if (fileInputStream != null) {
                fileInputStream.close();
            }
            return null;
        } catch (IOException e9) {
            e = e9;
            fileInputStream = null;
            x.b(e.getMessage(), e);
            if (fileInputStream != null) {
                fileInputStream.close();
            }
            return null;
        } catch (Throwable th2) {
            e = th2;
            fileInputStream = null;
            if (fileInputStream != null) {
                fileInputStream.close();
            }
            throw e;
        }
    }

    public static void a(Context context, byte[] bArr) {
        FileOutputStream fileOutputStream;
        Throwable e;
        Throwable th;
        FileOutputStream fileOutputStream2 = null;
        if (context != null && bArr != null && bArr.length != 0) {
            File b = b(context);
            if (b.exists() && !b.delete()) {
                x.c("Couldn't delete old Shepherd config file");
            }
            try {
                b.createNewFile();
                try {
                    fileOutputStream = new FileOutputStream(b);
                    try {
                        fileOutputStream.write(bArr);
                        fileOutputStream.flush();
                        if (fileOutputStream != null) {
                            try {
                                fileOutputStream.close();
                            } catch (Throwable e2) {
                                x.a(e2.getMessage(), e2);
                            }
                        }
                    } catch (FileNotFoundException e3) {
                        if (fileOutputStream != null) {
                            try {
                                fileOutputStream.close();
                            } catch (Throwable e22) {
                                x.a(e22.getMessage(), e22);
                            }
                        }
                    } catch (Throwable e4) {
                        th = e4;
                        fileOutputStream2 = fileOutputStream;
                        e22 = th;
                        try {
                            x.b(e22.getMessage(), e22);
                            if (fileOutputStream2 != null) {
                                try {
                                    fileOutputStream2.close();
                                } catch (Throwable e222) {
                                    x.a(e222.getMessage(), e222);
                                }
                            }
                            if (!b.delete()) {
                                x.c("Couldn't delete invalid Shepherd config file");
                            }
                        } catch (Throwable th2) {
                            e222 = th2;
                            if (fileOutputStream2 != null) {
                                try {
                                    fileOutputStream2.close();
                                } catch (Throwable e42) {
                                    x.a(e42.getMessage(), e42);
                                }
                            }
                            throw e222;
                        }
                    } catch (Throwable e422) {
                        th = e422;
                        fileOutputStream2 = fileOutputStream;
                        e222 = th;
                        if (fileOutputStream2 != null) {
                            fileOutputStream2.close();
                        }
                        throw e222;
                    }
                } catch (FileNotFoundException e5) {
                    fileOutputStream = null;
                    if (fileOutputStream != null) {
                        fileOutputStream.close();
                    }
                } catch (IOException e6) {
                    e222 = e6;
                    x.b(e222.getMessage(), e222);
                    if (fileOutputStream2 != null) {
                        fileOutputStream2.close();
                    }
                    if (b.delete()) {
                        x.c("Couldn't delete invalid Shepherd config file");
                    }
                }
            } catch (Throwable e2222) {
                x.b(e2222.getMessage(), e2222);
            }
        }
    }

    private static File b(Context context) {
        return new File(context.getDir("shepherd", 0), "shepherd.config");
    }
}
