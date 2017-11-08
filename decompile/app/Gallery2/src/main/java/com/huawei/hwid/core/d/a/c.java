package com.huawei.hwid.core.d.a;

import android.content.Context;
import android.text.TextUtils;
import com.huawei.hwid.core.d.b;
import com.huawei.hwid.core.d.b.e;
import com.huawei.hwid.core.d.f;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class c implements b {
    public void a(Context context, int i, int i2) {
        if (i < i2) {
            e.b("PropertiesGrade", "update settings.properties when version update");
            a(context);
            b(context);
            return;
        }
        e.d("PropertiesGrade", "newVersion is less then oldVersion, onUpgrade error");
    }

    private void a(Context context) {
        e.b("PropertiesGrade", "begin update bindFingetUserId in settings.properties");
        Object a = a(context, "bindFingetUserId");
        f.a(context, new String[]{"bindFingetUserId"});
        if (TextUtils.isEmpty(a)) {
            e.b("PropertiesGrade", "bindFingetUserId is null in settings.properties");
            return;
        }
        String a2 = com.huawei.hwid.core.encrypt.e.a(context, a);
        if (TextUtils.isEmpty(a2)) {
            e.b("PropertiesGrade", "bindFingetUserId ecb decrypt error");
            return;
        }
        e.b("PropertiesGrade", "update bindFingetUserId in settings.properties");
        f.a(context, "bindFingetUserId", a2);
    }

    private void b(Context context) {
        e.b("PropertiesGrade", "begin update curName in settings.properties");
        String a = a(context, "curName");
        f.a(context, new String[]{"curName"});
        if (TextUtils.isEmpty(a)) {
            e.b("PropertiesGrade", "curName is null in settings.properties");
            return;
        }
        if (b.a(context, "isSDKAccountDataEncrypted", false)) {
            f.a(context, new String[]{"isSDKAccountDataEncrypted"});
            a = com.huawei.hwid.core.encrypt.e.a(context, a);
        }
        if (TextUtils.isEmpty(a)) {
            e.b("PropertiesGrade", "curName ecb decrypt error");
            return;
        }
        e.b("PropertiesGrade", "update curName in settings.properties");
        f.a(context, "curName", a);
    }

    public static synchronized String a(Context context, String str) {
        FileOutputStream fileOutputStream;
        Throwable e;
        FileOutputStream fileOutputStream2;
        FileInputStream fileInputStream;
        String str2;
        InputStream inputStream;
        Object obj;
        FileOutputStream fileOutputStream3 = null;
        synchronized (c.class) {
            try {
                Properties properties = new Properties();
                if (new File(context.getFilesDir().getPath() + "/" + "settings.properties").exists()) {
                    fileOutputStream = fileOutputStream3;
                } else {
                    fileOutputStream = context.openFileOutput("settings.properties", 0);
                }
                try {
                    InputStream openFileInput = context.openFileInput("settings.properties");
                    if (openFileInput == null) {
                        try {
                            e.b("PropertiesGrade", "inStream is null");
                        } catch (FileNotFoundException e2) {
                            e = e2;
                            fileOutputStream2 = fileOutputStream;
                            fileInputStream = openFileInput;
                            fileOutputStream3 = fileOutputStream2;
                            try {
                                e.d("PropertiesGrade", "Can not find the file settings.properties", e);
                                if (fileOutputStream3 != null) {
                                    try {
                                        fileOutputStream3.close();
                                    } catch (Throwable e3) {
                                        e.d("PropertiesGrade", "IOException / " + e3.getMessage(), e3);
                                    }
                                }
                                if (fileInputStream != null) {
                                    try {
                                        fileInputStream.close();
                                    } catch (Throwable e32) {
                                        e.d("PropertiesGrade", "IOException / " + e32.getMessage(), e32);
                                    }
                                }
                                str2 = "";
                                return str2;
                            } catch (Throwable th) {
                                e32 = th;
                                if (fileOutputStream3 != null) {
                                    try {
                                        fileOutputStream3.close();
                                    } catch (Throwable e4) {
                                        e.d("PropertiesGrade", "IOException / " + e4.getMessage(), e4);
                                    }
                                }
                                if (fileInputStream != null) {
                                    try {
                                        fileInputStream.close();
                                    } catch (Throwable e42) {
                                        e.d("PropertiesGrade", "IOException / " + e42.getMessage(), e42);
                                    }
                                }
                                throw e32;
                            }
                        } catch (IOException e5) {
                            e32 = e5;
                            fileOutputStream2 = fileOutputStream;
                            inputStream = openFileInput;
                            fileOutputStream3 = fileOutputStream2;
                            e.d("PropertiesGrade", "IOException / " + e32.getMessage(), e32);
                            if (fileOutputStream3 != null) {
                                try {
                                    fileOutputStream3.close();
                                } catch (Throwable e322) {
                                    e.d("PropertiesGrade", "IOException / " + e322.getMessage(), e322);
                                }
                            }
                            if (fileInputStream != null) {
                                try {
                                    fileInputStream.close();
                                } catch (Throwable e3222) {
                                    e.d("PropertiesGrade", "IOException / " + e3222.getMessage(), e3222);
                                }
                            }
                            str2 = "";
                            return str2;
                        } catch (NullPointerException e6) {
                            e3222 = e6;
                            fileOutputStream2 = fileOutputStream;
                            inputStream = openFileInput;
                            fileOutputStream3 = fileOutputStream2;
                            e.d("PropertiesGrade", "NullPointerException / " + e3222.getMessage(), e3222);
                            if (fileOutputStream3 != null) {
                                try {
                                    fileOutputStream3.close();
                                } catch (Throwable e32222) {
                                    e.d("PropertiesGrade", "IOException / " + e32222.getMessage(), e32222);
                                }
                            }
                            if (fileInputStream != null) {
                                try {
                                    fileInputStream.close();
                                } catch (Throwable e322222) {
                                    e.d("PropertiesGrade", "IOException / " + e322222.getMessage(), e322222);
                                }
                            }
                            str2 = "";
                            return str2;
                        } catch (Throwable th2) {
                            e322222 = th2;
                            fileOutputStream2 = fileOutputStream;
                            inputStream = openFileInput;
                            fileOutputStream3 = fileOutputStream2;
                            if (fileOutputStream3 != null) {
                                fileOutputStream3.close();
                            }
                            if (fileInputStream != null) {
                                fileInputStream.close();
                            }
                            throw e322222;
                        }
                    }
                    properties.load(openFileInput);
                    str2 = properties.getProperty(str);
                    if (fileOutputStream != null) {
                        try {
                            fileOutputStream.close();
                        } catch (Throwable e7) {
                            e.d("PropertiesGrade", "IOException / " + e7.getMessage(), e7);
                        }
                    }
                    if (openFileInput != null) {
                        try {
                            openFileInput.close();
                        } catch (Throwable e422) {
                            e.d("PropertiesGrade", "IOException / " + e422.getMessage(), e422);
                        }
                    }
                } catch (FileNotFoundException e8) {
                    e322222 = e8;
                    fileOutputStream2 = fileOutputStream;
                    obj = fileOutputStream3;
                    fileOutputStream3 = fileOutputStream2;
                    e.d("PropertiesGrade", "Can not find the file settings.properties", e322222);
                    if (fileOutputStream3 != null) {
                        fileOutputStream3.close();
                    }
                    if (fileInputStream != null) {
                        fileInputStream.close();
                    }
                    str2 = "";
                    return str2;
                } catch (IOException e9) {
                    e322222 = e9;
                    fileOutputStream2 = fileOutputStream;
                    obj = fileOutputStream3;
                    fileOutputStream3 = fileOutputStream2;
                    e.d("PropertiesGrade", "IOException / " + e322222.getMessage(), e322222);
                    if (fileOutputStream3 != null) {
                        fileOutputStream3.close();
                    }
                    if (fileInputStream != null) {
                        fileInputStream.close();
                    }
                    str2 = "";
                    return str2;
                } catch (NullPointerException e10) {
                    e322222 = e10;
                    fileOutputStream2 = fileOutputStream;
                    obj = fileOutputStream3;
                    fileOutputStream3 = fileOutputStream2;
                    e.d("PropertiesGrade", "NullPointerException / " + e322222.getMessage(), e322222);
                    if (fileOutputStream3 != null) {
                        fileOutputStream3.close();
                    }
                    if (fileInputStream != null) {
                        fileInputStream.close();
                    }
                    str2 = "";
                    return str2;
                } catch (Throwable th3) {
                    e322222 = th3;
                    fileOutputStream2 = fileOutputStream;
                    obj = fileOutputStream3;
                    fileOutputStream3 = fileOutputStream2;
                    if (fileOutputStream3 != null) {
                        fileOutputStream3.close();
                    }
                    if (fileInputStream != null) {
                        fileInputStream.close();
                    }
                    throw e322222;
                }
            } catch (FileNotFoundException e11) {
                e322222 = e11;
                obj = fileOutputStream3;
                e.d("PropertiesGrade", "Can not find the file settings.properties", e322222);
                if (fileOutputStream3 != null) {
                    fileOutputStream3.close();
                }
                if (fileInputStream != null) {
                    fileInputStream.close();
                }
                str2 = "";
                return str2;
            } catch (IOException e12) {
                e322222 = e12;
                fileInputStream = fileOutputStream3;
                e.d("PropertiesGrade", "IOException / " + e322222.getMessage(), e322222);
                if (fileOutputStream3 != null) {
                    fileOutputStream3.close();
                }
                if (fileInputStream != null) {
                    fileInputStream.close();
                }
                str2 = "";
                return str2;
            } catch (NullPointerException e13) {
                e322222 = e13;
                fileInputStream = fileOutputStream3;
                e.d("PropertiesGrade", "NullPointerException / " + e322222.getMessage(), e322222);
                if (fileOutputStream3 != null) {
                    fileOutputStream3.close();
                }
                if (fileInputStream != null) {
                    fileInputStream.close();
                }
                str2 = "";
                return str2;
            } catch (Throwable th4) {
                e322222 = th4;
                fileInputStream = fileOutputStream3;
                if (fileOutputStream3 != null) {
                    fileOutputStream3.close();
                }
                if (fileInputStream != null) {
                    fileInputStream.close();
                }
                throw e322222;
            }
        }
        return str2;
    }
}
