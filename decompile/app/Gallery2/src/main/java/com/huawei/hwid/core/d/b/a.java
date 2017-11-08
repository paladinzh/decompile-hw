package com.huawei.hwid.core.d.b;

import android.util.Log;
import com.android.gallery3d.gadget.XmlUtils;
import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class a implements com.huawei.hwid.core.d.b.c.a, Runnable {
    private final BlockingQueue<String> a = new LinkedBlockingQueue();
    private Thread b;
    private volatile boolean c = true;
    private File d;

    public void a(String str) {
        if (str != null && this.d != null && !this.a.offer(str)) {
            Log.w("FileLogger", "write offer failed");
        }
    }

    public void a(File file) {
        if (file != null) {
            File parentFile = file.getParentFile();
            if (parentFile != null) {
                if (!parentFile.mkdirs()) {
                    Log.w("FileLogger", "Failed to create the log dir or has created.");
                }
                if (parentFile.isDirectory()) {
                    this.d = file;
                    this.b = new Thread(this, "hwid-log-thread");
                    this.b.start();
                    return;
                }
                Log.w("FileLogger", "Failed to create the log dir.");
                return;
            }
            Log.w("FileLogger", "logDir is null");
            return;
        }
        Log.w("FileLogger", "Invalid argument.");
    }

    public void run() {
        this.c = true;
        if (this.d != null) {
            while (this.c) {
                try {
                    String str = (String) this.a.poll(1, TimeUnit.SECONDS);
                    if (str != null) {
                        b(str);
                        c(str);
                    }
                } catch (InterruptedException e) {
                }
            }
        }
        Log.i("FileLogger", "The log logger is closed.");
    }

    private void b(String str) {
        Object obj = null;
        if (this.d.length() + ((long) str.length()) <= 3145728) {
            obj = 1;
        }
        if (obj == null) {
            if (!this.d.renameTo(new File(this.d.getPath() + ".bak"))) {
                Log.w("FileLogger", "Failed to backup the log file.");
            }
        }
    }

    private void c(String str) {
        Closeable fileOutputStream;
        Closeable bufferedOutputStream;
        Closeable outputStreamWriter;
        Throwable th;
        Throwable th2;
        Closeable closeable = null;
        try {
            fileOutputStream = new FileOutputStream(this.d, true);
            try {
                bufferedOutputStream = new BufferedOutputStream(fileOutputStream);
                try {
                    outputStreamWriter = new OutputStreamWriter(bufferedOutputStream, XmlUtils.INPUT_ENCODING);
                    try {
                        outputStreamWriter.write(str);
                        outputStreamWriter.flush();
                        a(outputStreamWriter);
                        a(bufferedOutputStream);
                        a(fileOutputStream);
                    } catch (FileNotFoundException e) {
                        closeable = bufferedOutputStream;
                        bufferedOutputStream = fileOutputStream;
                        try {
                            Log.d("FileLogger", "Exception when writing the log file.");
                            a(outputStreamWriter);
                            a(closeable);
                            a(bufferedOutputStream);
                        } catch (Throwable th3) {
                            th = th3;
                            fileOutputStream = bufferedOutputStream;
                            bufferedOutputStream = closeable;
                            closeable = outputStreamWriter;
                            th2 = th;
                            a(closeable);
                            a(bufferedOutputStream);
                            a(fileOutputStream);
                            throw th2;
                        }
                    } catch (IOException e2) {
                        closeable = outputStreamWriter;
                        try {
                            Log.d("FileLogger", "Exception when writing the log file.");
                            a(closeable);
                            a(bufferedOutputStream);
                            a(fileOutputStream);
                        } catch (Throwable th4) {
                            th2 = th4;
                            a(closeable);
                            a(bufferedOutputStream);
                            a(fileOutputStream);
                            throw th2;
                        }
                    } catch (Throwable th5) {
                        th = th5;
                        closeable = outputStreamWriter;
                        th2 = th;
                        a(closeable);
                        a(bufferedOutputStream);
                        a(fileOutputStream);
                        throw th2;
                    }
                } catch (FileNotFoundException e3) {
                    outputStreamWriter = null;
                    closeable = bufferedOutputStream;
                    bufferedOutputStream = fileOutputStream;
                    Log.d("FileLogger", "Exception when writing the log file.");
                    a(outputStreamWriter);
                    a(closeable);
                    a(bufferedOutputStream);
                } catch (IOException e4) {
                    Log.d("FileLogger", "Exception when writing the log file.");
                    a(closeable);
                    a(bufferedOutputStream);
                    a(fileOutputStream);
                }
            } catch (FileNotFoundException e5) {
                outputStreamWriter = null;
                bufferedOutputStream = fileOutputStream;
                Log.d("FileLogger", "Exception when writing the log file.");
                a(outputStreamWriter);
                a(closeable);
                a(bufferedOutputStream);
            } catch (IOException e6) {
                bufferedOutputStream = null;
                Log.d("FileLogger", "Exception when writing the log file.");
                a(closeable);
                a(bufferedOutputStream);
                a(fileOutputStream);
            } catch (Throwable th6) {
                th2 = th6;
                bufferedOutputStream = null;
                a(closeable);
                a(bufferedOutputStream);
                a(fileOutputStream);
                throw th2;
            }
        } catch (FileNotFoundException e7) {
            outputStreamWriter = null;
            bufferedOutputStream = null;
            Log.d("FileLogger", "Exception when writing the log file.");
            a(outputStreamWriter);
            a(closeable);
            a(bufferedOutputStream);
        } catch (IOException e8) {
            bufferedOutputStream = null;
            fileOutputStream = null;
            Log.d("FileLogger", "Exception when writing the log file.");
            a(closeable);
            a(bufferedOutputStream);
            a(fileOutputStream);
        } catch (Throwable th7) {
            th2 = th7;
            bufferedOutputStream = null;
            fileOutputStream = null;
            a(closeable);
            a(bufferedOutputStream);
            a(fileOutputStream);
            throw th2;
        }
    }

    private static void a(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException e) {
                Log.d("FileLogger", "Exception when closing the closeable.");
            }
        }
    }
}
