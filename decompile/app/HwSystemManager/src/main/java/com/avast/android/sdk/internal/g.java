package com.avast.android.sdk.internal;

import android.os.Environment;
import com.avast.android.sdk.engine.obfuscated.ao;
import com.huawei.harassmentinterception.common.ConstValues;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.Stack;
import java.util.UUID;

/* compiled from: Unknown */
public class g {
    private boolean a = false;

    private List<String> a(File file) {
        List<String> arrayList = new ArrayList();
        Stack stack = new Stack();
        stack.push(file);
        while (!stack.isEmpty()) {
            File file2 = (File) stack.pop();
            File[] listFiles = file2.listFiles();
            if (listFiles != null) {
                Object obj = null;
                for (File file3 : listFiles) {
                    if (file3.isDirectory() && b(file3)) {
                        obj = 1;
                    }
                }
                if (obj == null) {
                    for (File file4 : listFiles) {
                        if (file4.isDirectory()) {
                            stack.push(file4);
                        }
                    }
                } else {
                    try {
                        arrayList.add(file2.getCanonicalPath());
                    } catch (Throwable e) {
                        ao.a("Can't resolve symlinks to external storage dir.", e);
                        arrayList.add(file2.getAbsolutePath());
                    }
                }
            }
        }
        return arrayList;
    }

    private void a(List<String> list) {
        for (int i = 0; i < list.size() && !this.a; i++) {
            String str = (String) list.get(i);
            int i2 = i + 1;
            while (i2 < list.size()) {
                String str2 = (String) list.get(i2);
                boolean startsWith = str2.startsWith(str + "/");
                boolean equals = str2.equals(str);
                if (startsWith || equals) {
                    ao.a("Found path duplicate: " + str2 + " = " + str);
                    int i3 = i2 - 1;
                    list.remove(i2);
                    i2 = i3;
                }
                i2++;
            }
        }
    }

    private void b(List<String> list) {
        Throwable e;
        int i;
        int i2 = 0;
        while (i2 < list.size() && !this.a) {
            File file;
            String uuid;
            String str = (String) list.get(i2);
            do {
                try {
                    uuid = UUID.randomUUID().toString();
                    file = new File(str, uuid);
                } catch (Exception e2) {
                    e = e2;
                }
            } while (file.isFile());
            file.createNewFile();
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            fileOutputStream.write(uuid.getBytes());
            fileOutputStream.flush();
            fileOutputStream.close();
            if (file.isFile()) {
                int i3 = i2 + 1;
                while (i3 < list.size()) {
                    String str2 = (String) list.get(i3);
                    File file2 = new File(str2, uuid);
                    Object obj = (file2.isFile() && new BufferedReader(new FileReader(file2)).readLine().equals(uuid)) ? 1 : null;
                    if (obj == null) {
                        i = i3;
                    } else {
                        ao.a("Found mount duplicate: " + str2 + " = " + str);
                        i = i3 - 1;
                        list.remove(i3);
                    }
                    i3 = i + 1;
                }
                file.delete();
            } else {
                ao.a("Can't write test file to external storage: " + str);
                i = i2 - 1;
                try {
                    list.remove(i2);
                    i2 = i;
                } catch (Throwable e3) {
                    Throwable th = e3;
                    i2 = i;
                    e = th;
                    ao.a("Can't access external storage: " + str, e);
                    int i4 = i2 - 1;
                    list.remove(i2);
                    i2 = i4;
                    i2++;
                }
            }
            i2++;
        }
    }

    private boolean b(File file) {
        return "LOST.DIR".equals(file.getName());
    }

    private List<String> c() {
        Scanner scanner;
        FileNotFoundException e;
        Throwable th;
        List<String> arrayList = new ArrayList();
        String str = "";
        try {
            scanner = new Scanner(new FileInputStream("/proc/mounts"), "UTF-8");
            while (scanner.hasNextLine() && !this.a) {
                int length;
                int i;
                String[] split = scanner.nextLine().split(" ");
                if (split.length - 3 != 3) {
                    length = split.length;
                    while (true) {
                        length--;
                        if (length <= -1) {
                            break;
                        }
                        try {
                            Integer.parseInt(split[length]);
                        } catch (NumberFormatException e2) {
                            i = length;
                        }
                    }
                }
                i = 3;
                String[] split2 = split[i].split(ConstValues.SEPARATOR_KEYWORDS_EN);
                for (length = 0; length < split2.length && !this.a; length++) {
                    String toLowerCase = split2[length].toLowerCase();
                    if ("dirsync".equals(toLowerCase)) {
                        arrayList.addAll(a(new File(split[i - 2])));
                    } else if ("allow_other".equals(toLowerCase)) {
                        try {
                            arrayList.add(split[i - 2]);
                        } catch (FileNotFoundException e3) {
                            e = e3;
                        }
                    } else {
                        continue;
                    }
                }
            }
            if (scanner != null) {
                scanner.close();
            }
        } catch (FileNotFoundException e4) {
            e = e4;
            scanner = null;
            try {
                e.printStackTrace();
                if (scanner != null) {
                    scanner.close();
                }
                return arrayList;
            } catch (Throwable th2) {
                th = th2;
                if (scanner != null) {
                    scanner.close();
                }
                throw th;
            }
        } catch (Throwable th3) {
            th = th3;
            scanner = null;
            if (scanner != null) {
                scanner.close();
            }
            throw th;
        }
        return arrayList;
    }

    public void a() {
        this.a = false;
    }

    public List<String> b() {
        ao.a("Finding SD cards...");
        List c = c();
        try {
            c.add(Environment.getExternalStorageDirectory().getCanonicalPath());
        } catch (Throwable e) {
            ao.a("Can't resolve symlinks to external storage dir.", e);
            c.add(Environment.getExternalStorageDirectory().getAbsolutePath());
        }
        ao.a("SD cards list before check for dups: " + Arrays.toString(c.toArray(new String[c.size()])));
        a(c);
        b(c);
        if (c.isEmpty() && "mounted".equals(Environment.getExternalStorageState())) {
            c.add(Environment.getExternalStorageDirectory().getAbsolutePath());
        }
        ao.a("SD cards list: " + Arrays.toString(c.toArray(new String[c.size()])));
        return c;
    }
}
