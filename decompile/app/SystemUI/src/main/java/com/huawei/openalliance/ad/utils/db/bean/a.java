package com.huawei.openalliance.ad.utils.db.bean;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.database.Cursor;
import com.huawei.openalliance.ad.utils.b.d;
import com.huawei.openalliance.ad.utils.f;
import fyusion.vislib.BuildConfig;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

@SuppressLint({"NewApi"})
/* compiled from: Unknown */
public class a {
    private static Map<String, String> DBTypeMap = new HashMap(4);
    private boolean isFiledCutUnderline = true;

    static {
        DBTypeMap.put("String", "TEXT");
        DBTypeMap.put("long", "INTEGER");
        DBTypeMap.put("int", "INTEGER");
        DBTypeMap.put("float", "REAL");
    }

    private String a(String str) {
        return (this.isFiledCutUnderline && str.endsWith("_")) ? str.substring(0, str.length() - 1) : str;
    }

    public void a(Cursor cursor) {
        Field[] a = f.a(getClass());
        for (int i = 0; i < a.length; i++) {
            try {
                a[i].setAccessible(true);
                String name = a[i].getName();
                if ("_id".equals(name)) {
                    a[i].set(this, cursor.getString(cursor.getColumnIndex(name)));
                } else if (name.endsWith("_")) {
                    if (!name.contains("$")) {
                        String simpleName = a[i].getType().getSimpleName();
                        int columnIndex = cursor.getColumnIndex(a(name));
                        if (columnIndex != -1) {
                            if ("String".equals(simpleName)) {
                                a[i].set(this, cursor.getString(columnIndex));
                            } else if ("int".equals(simpleName)) {
                                a[i].set(this, Integer.valueOf(cursor.getInt(columnIndex)));
                            } else if ("long".equals(simpleName)) {
                                a[i].set(this, Long.valueOf(cursor.getLong(columnIndex)));
                            } else if ("float".equals(simpleName)) {
                                a[i].set(this, Float.valueOf(cursor.getFloat(columnIndex)));
                            } else {
                                d.c("RecordBean", "unsupport field type:", simpleName, " ", a[i].getName());
                            }
                        }
                    }
                }
            } catch (Throwable e) {
                d.a("RecordBean", "IllegalAccessException", e);
            }
        }
    }

    public String m(String str) {
        Field[] a = f.a(getClass());
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("create table ");
        stringBuilder.append(str);
        stringBuilder.append(" ( ");
        stringBuilder.append("_id INTEGER primary key autoincrement ");
        for (int i = 0; i < a.length; i++) {
            a[i].setAccessible(true);
            String name = a[i].getName();
            if (name.endsWith("_") && !name.contains("$")) {
                String str2 = (String) DBTypeMap.get(a[i].getType().getSimpleName());
                if (str2 != null) {
                    name = a(name);
                    stringBuilder.append(" , ");
                    stringBuilder.append(name);
                    stringBuilder.append(" ");
                    stringBuilder.append(str2);
                    if (n() != null && n().equals(name)) {
                        stringBuilder.append(" unique");
                    }
                }
            }
        }
        stringBuilder.append(" ) ");
        return stringBuilder.toString();
    }

    public String n() {
        return BuildConfig.FLAVOR;
    }

    public ContentValues q() {
        Field[] a = f.a(getClass());
        ContentValues contentValues = new ContentValues();
        for (int i = 0; i < a.length; i++) {
            try {
                a[i].setAccessible(true);
                String name = a[i].getName();
                if (name.endsWith("_") && !name.contains("$")) {
                    Object obj = a[i].get(this);
                    if (obj != null) {
                        name = a(name);
                        if (obj instanceof String) {
                            contentValues.put(name, (String) obj);
                        } else if (obj instanceof Integer) {
                            contentValues.put(name, (Integer) obj);
                        } else if (obj instanceof Long) {
                            contentValues.put(name, (Long) obj);
                        } else {
                            d.c("RecordBean", "unsupport type, name:", a[i].getName());
                        }
                    }
                }
            } catch (IllegalAccessException e) {
            }
        }
        return contentValues;
    }

    public String r() {
        return m(s());
    }

    public String s() {
        return getClass().getSimpleName();
    }
}
