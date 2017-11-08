package com.avast.android.sdk.engine.internal.vps;

import com.avast.android.sdk.engine.obfuscated.ao;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/* compiled from: Unknown */
public class ObjectStorage {
    private static final List<HashMap<String, byte[]>> a = new ArrayList();
    private static final List<HashMap<String, Object>> b = new ArrayList();

    public static synchronized void clearStorage(int i) {
        synchronized (ObjectStorage.class) {
            if (i >= 0) {
                if (i < a.size()) {
                    HashMap hashMap = (HashMap) a.get(i);
                    if (hashMap != null) {
                        hashMap.clear();
                    }
                    if (i >= 0 && i < b.size()) {
                        hashMap = (HashMap) b.get(i);
                        if (hashMap != null) {
                            hashMap.clear();
                        }
                    } else {
                        throw new IndexOutOfBoundsException();
                    }
                }
            }
            throw new IndexOutOfBoundsException();
        }
    }

    public static synchronized byte[] getByteArray(int i, String str) {
        synchronized (ObjectStorage.class) {
            if (i < 0) {
                throw new IllegalArgumentException();
            } else if (str == null) {
                throw new NullPointerException();
            } else if (i < a.size()) {
                HashMap hashMap = (HashMap) a.get(i);
                if (hashMap == null) {
                    return null;
                }
                byte[] bArr = (byte[]) hashMap.get(str);
                return bArr;
            } else {
                return null;
            }
        }
    }

    public static synchronized Object getObject(int i, String str) {
        synchronized (ObjectStorage.class) {
            if (i < 0) {
                throw new IllegalArgumentException();
            } else if (str == null) {
                throw new NullPointerException();
            } else if (i < b.size()) {
                HashMap hashMap = (HashMap) b.get(i);
                if (hashMap == null) {
                    return null;
                }
                Object obj = hashMap.get(str);
                return obj;
            } else {
                return null;
            }
        }
    }

    public static synchronized void initializeStorage(int i) {
        synchronized (ObjectStorage.class) {
            if (i >= 0) {
                int size;
                ao.a("ObjectStorage: initiating for contextId = " + i);
                if (a.size() <= i) {
                    for (size = a.size(); size <= i; size++) {
                        a.add(new HashMap());
                    }
                }
                if (a.get(i) == null) {
                    a.set(i, new HashMap());
                }
                if (b.size() <= i) {
                    for (size = b.size(); size <= i; size++) {
                        b.add(new HashMap());
                    }
                }
                if (b.get(i) == null) {
                    b.set(i, new HashMap());
                }
            } else {
                throw new IllegalArgumentException();
            }
        }
    }

    public static synchronized void setByteArray(int i, String str, byte[] bArr) {
        synchronized (ObjectStorage.class) {
            if (i < 0) {
                throw new IllegalArgumentException();
            } else if (str == null) {
                throw new NullPointerException();
            } else if (a.size() > i) {
                HashMap hashMap = (HashMap) a.get(i);
                if (hashMap != null) {
                    hashMap.put(str, bArr);
                }
            } else {
                throw new IllegalStateException();
            }
        }
    }

    public static synchronized void setObject(int i, String str, Object obj) {
        synchronized (ObjectStorage.class) {
            if (i < 0) {
                throw new IllegalArgumentException();
            } else if (str == null) {
                throw new NullPointerException();
            } else if (b.size() > i) {
                HashMap hashMap = (HashMap) b.get(i);
                if (hashMap != null) {
                    hashMap.put(str, obj);
                }
            } else {
                throw new IllegalStateException();
            }
        }
    }
}
