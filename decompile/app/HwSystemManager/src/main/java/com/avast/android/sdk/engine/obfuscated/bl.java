package com.avast.android.sdk.engine.obfuscated;

import android.content.Context;
import com.google.protobuf.ByteString;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;
import java.util.concurrent.atomic.AtomicReference;

/* compiled from: Unknown */
public class bl implements t {
    private static bl a = null;
    private final AtomicReference<o> b = new AtomicReference();
    private final Context c;

    private bl(Context context) {
        this.c = context;
    }

    public static synchronized bl a(Context context) {
        bl blVar;
        synchronized (bl.class) {
            if (a == null) {
                a = new bl(context);
            }
            blVar = a;
        }
        return blVar;
    }

    private o b() throws IOException {
        FileInputStream fileInputStream;
        ObjectInputStream objectInputStream;
        StreamCorruptedException e;
        Throwable th;
        IOException e2;
        o oVar = null;
        File file = new File(this.c.getDir("streamback", 0) + File.separator + "sb.key");
        try {
            fileInputStream = new FileInputStream(file);
            try {
                objectInputStream = new ObjectInputStream(fileInputStream);
            } catch (StreamCorruptedException e3) {
                e = e3;
                try {
                    throw new IOException(e.getMessage());
                } catch (Throwable th2) {
                    th = th2;
                    objectInputStream = oVar;
                    if (objectInputStream != null) {
                        objectInputStream.close();
                    }
                    if (fileInputStream != null) {
                        fileInputStream.close();
                    }
                    throw th;
                }
            } catch (FileNotFoundException e4) {
                objectInputStream = null;
                if (objectInputStream != null) {
                    objectInputStream.close();
                }
                if (fileInputStream != null) {
                    fileInputStream.close();
                }
                return null;
            } catch (IOException e5) {
                e2 = e5;
                objectInputStream = null;
                try {
                    throw e2;
                } catch (Throwable th3) {
                    th = th3;
                }
            } catch (Throwable th4) {
                th = th4;
                objectInputStream = null;
                if (objectInputStream != null) {
                    objectInputStream.close();
                }
                if (fileInputStream != null) {
                    fileInputStream.close();
                }
                throw th;
            }
            try {
                int readInt = objectInputStream.readInt();
                byte[] bArr = new byte[readInt];
                if (objectInputStream.read(bArr) == readInt) {
                    readInt = objectInputStream.readInt();
                    byte[] bArr2 = new byte[readInt];
                    if (objectInputStream.read(bArr2) == readInt) {
                        o oVar2 = new o(ByteString.copyFrom(bArr), ByteString.copyFrom(bArr2), objectInputStream.readLong());
                        if (objectInputStream != null) {
                            objectInputStream.close();
                        }
                        if (fileInputStream != null) {
                            fileInputStream.close();
                        }
                        return oVar2;
                    }
                    throw new IOException("Not enough bytes in " + file.getAbsolutePath() + " to read " + "object");
                }
                throw new IOException("Not enough bytes in " + file.getAbsolutePath() + " to read " + "object");
            } catch (StreamCorruptedException e6) {
                e = e6;
                oVar = objectInputStream;
                throw new IOException(e.getMessage());
            } catch (FileNotFoundException e7) {
                if (objectInputStream != null) {
                    objectInputStream.close();
                }
                if (fileInputStream != null) {
                    fileInputStream.close();
                }
                return null;
            } catch (IOException e8) {
                e2 = e8;
                throw e2;
            }
        } catch (StreamCorruptedException e9) {
            e = e9;
            fileInputStream = null;
            throw new IOException(e.getMessage());
        } catch (FileNotFoundException e10) {
            fileInputStream = null;
            objectInputStream = null;
            if (objectInputStream != null) {
                objectInputStream.close();
            }
            if (fileInputStream != null) {
                fileInputStream.close();
            }
            return null;
        } catch (IOException e11) {
            e2 = e11;
            fileInputStream = null;
            objectInputStream = null;
            throw e2;
        } catch (Throwable th5) {
            th = th5;
            fileInputStream = null;
            objectInputStream = null;
            if (objectInputStream != null) {
                objectInputStream.close();
            }
            if (fileInputStream != null) {
                fileInputStream.close();
            }
            throw th;
        }
    }

    private void b(o oVar) throws IOException {
        FileOutputStream fileOutputStream;
        ObjectOutputStream objectOutputStream;
        FileNotFoundException e;
        Throwable th;
        IOException e2;
        ObjectOutputStream objectOutputStream2 = null;
        if (oVar != null) {
            File file = new File(this.c.getDir("streamback", 0) + File.separator + "sb.key");
            if (file.exists() && file.delete()) {
            }
            try {
                fileOutputStream = new FileOutputStream(file);
                try {
                    objectOutputStream = new ObjectOutputStream(fileOutputStream);
                } catch (FileNotFoundException e3) {
                    e = e3;
                    try {
                        throw new IOException(e.getMessage());
                    } catch (Throwable th2) {
                        th = th2;
                        objectOutputStream = objectOutputStream2;
                        if (objectOutputStream != null) {
                            objectOutputStream.flush();
                            objectOutputStream.close();
                        }
                        if (fileOutputStream != null) {
                            fileOutputStream.close();
                        }
                        throw th;
                    }
                } catch (IOException e4) {
                    e2 = e4;
                    objectOutputStream = null;
                    try {
                        throw e2;
                    } catch (Throwable th3) {
                        th = th3;
                    }
                } catch (Throwable th4) {
                    th = th4;
                    objectOutputStream = null;
                    if (objectOutputStream != null) {
                        objectOutputStream.flush();
                        objectOutputStream.close();
                    }
                    if (fileOutputStream != null) {
                        fileOutputStream.close();
                    }
                    throw th;
                }
                try {
                    byte[] toByteArray = oVar.a().toByteArray();
                    if (toByteArray != null) {
                        if (toByteArray.length != 0) {
                            objectOutputStream.writeInt(toByteArray.length);
                            objectOutputStream.write(toByteArray);
                            toByteArray = oVar.b().toByteArray();
                            if (toByteArray != null) {
                                if (toByteArray.length != 0) {
                                    objectOutputStream.writeInt(toByteArray.length);
                                    objectOutputStream.write(toByteArray);
                                    objectOutputStream.writeLong(oVar.c());
                                    if (objectOutputStream != null) {
                                        objectOutputStream.flush();
                                        objectOutputStream.close();
                                    }
                                    if (fileOutputStream != null) {
                                        fileOutputStream.close();
                                        return;
                                    }
                                    return;
                                }
                            }
                            throw new IOException("Invalid key to write");
                        }
                    }
                    throw new IOException("Invalid id to write");
                } catch (FileNotFoundException e5) {
                    e = e5;
                    objectOutputStream2 = objectOutputStream;
                    throw new IOException(e.getMessage());
                } catch (IOException e6) {
                    e2 = e6;
                    throw e2;
                }
            } catch (FileNotFoundException e7) {
                e = e7;
                fileOutputStream = null;
                throw new IOException(e.getMessage());
            } catch (IOException e8) {
                e2 = e8;
                fileOutputStream = null;
                objectOutputStream = null;
                throw e2;
            } catch (Throwable th5) {
                th = th5;
                fileOutputStream = null;
                objectOutputStream = null;
                if (objectOutputStream != null) {
                    objectOutputStream.flush();
                    objectOutputStream.close();
                }
                if (fileOutputStream != null) {
                    fileOutputStream.close();
                }
                throw th;
            }
        }
        throw new IOException("Key is null");
    }

    public o a() throws IOException {
        o oVar;
        Object obj;
        o oVar2 = (o) this.b.get();
        if (oVar2 != null) {
            oVar = oVar2;
            obj = null;
        } else {
            oVar = b();
            obj = 1;
        }
        if (oVar == null || oVar.d()) {
            return oVar == null ? null : null;
        } else {
            if (obj != null) {
                this.b.set(oVar);
            }
            return oVar;
        }
    }

    public void a(o oVar) throws IOException {
        b(oVar);
        this.b.set(oVar);
    }
}
