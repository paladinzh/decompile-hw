package com.avast.android.shepherd.obfuscated;

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
public class an implements s {
    private static an a = null;
    private final AtomicReference<n> b = new AtomicReference();
    private final Context c;

    private an(Context context) {
        this.c = context;
    }

    public static synchronized an a(Context context) {
        an anVar;
        synchronized (an.class) {
            if (a == null) {
                a = new an(context);
            }
            anVar = a;
        }
        return anVar;
    }

    private n b() {
        StreamCorruptedException e;
        Throwable th;
        IOException e2;
        n nVar = null;
        File file = new File(this.c.getDir("streamback", 0) + File.separator + "sb.key");
        FileInputStream fileInputStream;
        ObjectInputStream objectInputStream;
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
                    objectInputStream = nVar;
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
                        n nVar2 = new n(ByteString.copyFrom(bArr), ByteString.copyFrom(bArr2), objectInputStream.readLong());
                        if (objectInputStream != null) {
                            objectInputStream.close();
                        }
                        if (fileInputStream != null) {
                            fileInputStream.close();
                        }
                        return nVar2;
                    }
                    throw new IOException("Not enough bytes in " + file.getAbsolutePath() + " to read " + "object");
                }
                throw new IOException("Not enough bytes in " + file.getAbsolutePath() + " to read " + "object");
            } catch (StreamCorruptedException e6) {
                e = e6;
                nVar = objectInputStream;
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

    private void b(n nVar) {
        FileNotFoundException e;
        Throwable th;
        IOException e2;
        ObjectOutputStream objectOutputStream = null;
        if (nVar != null) {
            File file = new File(this.c.getDir("streamback", 0) + File.separator + "sb.key");
            if (file.exists() && file.delete()) {
            }
            FileOutputStream fileOutputStream;
            ObjectOutputStream objectOutputStream2;
            try {
                fileOutputStream = new FileOutputStream(file);
                try {
                    objectOutputStream2 = new ObjectOutputStream(fileOutputStream);
                } catch (FileNotFoundException e3) {
                    e = e3;
                    try {
                        throw new IOException(e.getMessage());
                    } catch (Throwable th2) {
                        th = th2;
                        objectOutputStream2 = objectOutputStream;
                        if (objectOutputStream2 != null) {
                            objectOutputStream2.flush();
                            objectOutputStream2.close();
                        }
                        if (fileOutputStream != null) {
                            fileOutputStream.close();
                        }
                        throw th;
                    }
                } catch (IOException e4) {
                    e2 = e4;
                    objectOutputStream2 = null;
                    try {
                        throw e2;
                    } catch (Throwable th3) {
                        th = th3;
                    }
                } catch (Throwable th4) {
                    th = th4;
                    objectOutputStream2 = null;
                    if (objectOutputStream2 != null) {
                        objectOutputStream2.flush();
                        objectOutputStream2.close();
                    }
                    if (fileOutputStream != null) {
                        fileOutputStream.close();
                    }
                    throw th;
                }
                try {
                    byte[] toByteArray = nVar.a().toByteArray();
                    if (toByteArray != null) {
                        if (toByteArray.length != 0) {
                            objectOutputStream2.writeInt(toByteArray.length);
                            objectOutputStream2.write(toByteArray);
                            toByteArray = nVar.b().toByteArray();
                            if (toByteArray != null) {
                                if (toByteArray.length != 0) {
                                    objectOutputStream2.writeInt(toByteArray.length);
                                    objectOutputStream2.write(toByteArray);
                                    objectOutputStream2.writeLong(nVar.c());
                                    if (objectOutputStream2 != null) {
                                        objectOutputStream2.flush();
                                        objectOutputStream2.close();
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
                    objectOutputStream = objectOutputStream2;
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
                objectOutputStream2 = null;
                throw e2;
            } catch (Throwable th5) {
                th = th5;
                fileOutputStream = null;
                objectOutputStream2 = null;
                if (objectOutputStream2 != null) {
                    objectOutputStream2.flush();
                    objectOutputStream2.close();
                }
                if (fileOutputStream != null) {
                    fileOutputStream.close();
                }
                throw th;
            }
        }
        throw new IOException("Key is null");
    }

    public n a() {
        n nVar;
        Object obj;
        n nVar2 = (n) this.b.get();
        if (nVar2 != null) {
            nVar = nVar2;
            obj = null;
        } else {
            nVar = b();
            obj = 1;
        }
        if (nVar == null || nVar.d()) {
            return nVar == null ? null : null;
        } else {
            if (obj != null) {
                this.b.set(nVar);
            }
            return nVar;
        }
    }

    public void a(n nVar) {
        b(nVar);
        this.b.set(nVar);
    }
}
