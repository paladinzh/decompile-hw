package com.google.android.gms.internal;

import com.google.android.gms.wearable.Asset;
import com.google.android.gms.wearable.DataMap;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

/* compiled from: Unknown */
public final class zzrl {

    /* compiled from: Unknown */
    public static class zza {
        public final zzrm zzbbs;
        public final List<Asset> zzbbt;

        public zza(zzrm zzrm, List<Asset> list) {
            this.zzbbs = zzrm;
            this.zzbbt = list;
        }
    }

    private static int zza(String str, com.google.android.gms.internal.zzrm.zza.zza[] zzaArr) {
        int i = 14;
        for (com.google.android.gms.internal.zzrm.zza.zza zza : zzaArr) {
            if (i != 14) {
                if (zza.type != i) {
                    throw new IllegalArgumentException("The ArrayList elements should all be the same type, but ArrayList with key " + str + " contains items of type " + i + " and " + zza.type);
                }
            } else if (zza.type == 9 || zza.type == 2 || zza.type == 6) {
                i = zza.type;
            } else if (zza.type != 14) {
                throw new IllegalArgumentException("Unexpected TypedValue type: " + zza.type + " for key " + str);
            }
        }
        return i;
    }

    static int zza(List<Asset> list, Asset asset) {
        list.add(asset);
        return list.size() - 1;
    }

    public static zza zza(DataMap dataMap) {
        zzrm zzrm = new zzrm();
        List arrayList = new ArrayList();
        zzrm.zzbbu = zza(dataMap, arrayList);
        return new zza(zzrm, arrayList);
    }

    private static com.google.android.gms.internal.zzrm.zza.zza zza(List<Asset> list, Object obj) {
        com.google.android.gms.internal.zzrm.zza.zza zza = new com.google.android.gms.internal.zzrm.zza.zza();
        if (obj != null) {
            zza.zzbby = new com.google.android.gms.internal.zzrm.zza.zza.zza();
            if (obj instanceof String) {
                zza.type = 2;
                zza.zzbby.zzbbA = (String) obj;
            } else if (obj instanceof Integer) {
                zza.type = 6;
                zza.zzbby.zzbbE = ((Integer) obj).intValue();
            } else if (obj instanceof Long) {
                zza.type = 5;
                zza.zzbby.zzbbD = ((Long) obj).longValue();
            } else if (obj instanceof Double) {
                zza.type = 3;
                zza.zzbby.zzbbB = ((Double) obj).doubleValue();
            } else if (obj instanceof Float) {
                zza.type = 4;
                zza.zzbby.zzbbC = ((Float) obj).floatValue();
            } else if (obj instanceof Boolean) {
                zza.type = 8;
                zza.zzbby.zzbbG = ((Boolean) obj).booleanValue();
            } else if (obj instanceof Byte) {
                zza.type = 7;
                zza.zzbby.zzbbF = ((Byte) obj).byteValue();
            } else if (obj instanceof byte[]) {
                zza.type = 1;
                zza.zzbby.zzbbz = (byte[]) obj;
            } else if (obj instanceof String[]) {
                zza.type = 11;
                zza.zzbby.zzbbJ = (String[]) obj;
            } else if (obj instanceof long[]) {
                zza.type = 12;
                zza.zzbby.zzbbK = (long[]) obj;
            } else if (obj instanceof float[]) {
                zza.type = 15;
                zza.zzbby.zzbbL = (float[]) obj;
            } else if (obj instanceof Asset) {
                zza.type = 13;
                zza.zzbby.zzbbM = (long) zza((List) list, (Asset) obj);
            } else if (obj instanceof DataMap) {
                zza.type = 9;
                DataMap dataMap = (DataMap) obj;
                TreeSet treeSet = new TreeSet(dataMap.keySet());
                com.google.android.gms.internal.zzrm.zza[] zzaArr = new com.google.android.gms.internal.zzrm.zza[treeSet.size()];
                Iterator it = treeSet.iterator();
                r1 = 0;
                while (it.hasNext()) {
                    String str = (String) it.next();
                    zzaArr[r1] = new com.google.android.gms.internal.zzrm.zza();
                    zzaArr[r1].name = str;
                    zzaArr[r1].zzbbw = zza((List) list, dataMap.get(str));
                    r1++;
                }
                zza.zzbby.zzbbH = zzaArr;
            } else if (obj instanceof ArrayList) {
                zza.type = 10;
                ArrayList arrayList = (ArrayList) obj;
                com.google.android.gms.internal.zzrm.zza.zza[] zzaArr2 = new com.google.android.gms.internal.zzrm.zza.zza[arrayList.size()];
                Object obj2 = null;
                int size = arrayList.size();
                int i = 0;
                int i2 = 14;
                while (i < size) {
                    Object obj3 = arrayList.get(i);
                    com.google.android.gms.internal.zzrm.zza.zza zza2 = zza((List) list, obj3);
                    if (zza2.type == 14 || zza2.type == 2 || zza2.type == 6 || zza2.type == 9) {
                        if (i2 == 14 && zza2.type != 14) {
                            r1 = zza2.type;
                        } else if (zza2.type == i2) {
                            obj3 = obj2;
                            r1 = i2;
                        } else {
                            throw new IllegalArgumentException("ArrayList elements must all be of the sameclass, but this one contains a " + obj2.getClass() + " and a " + obj3.getClass());
                        }
                        zzaArr2[i] = zza2;
                        i++;
                        i2 = r1;
                        obj2 = obj3;
                    } else {
                        throw new IllegalArgumentException("The only ArrayList element types supported by DataBundleUtil are String, Integer, Bundle, and null, but this ArrayList contains a " + obj3.getClass());
                    }
                }
                zza.zzbby.zzbbI = zzaArr2;
            } else {
                throw new RuntimeException("newFieldValueFromValue: unexpected value " + obj.getClass().getSimpleName());
            }
            return zza;
        }
        zza.type = 14;
        return zza;
    }

    public static DataMap zza(zza zza) {
        DataMap dataMap = new DataMap();
        for (com.google.android.gms.internal.zzrm.zza zza2 : zza.zzbbs.zzbbu) {
            zza(zza.zzbbt, dataMap, zza2.name, zza2.zzbbw);
        }
        return dataMap;
    }

    private static ArrayList zza(List<Asset> list, com.google.android.gms.internal.zzrm.zza.zza.zza zza, int i) {
        ArrayList arrayList = new ArrayList(zza.zzbbI.length);
        for (com.google.android.gms.internal.zzrm.zza.zza zza2 : zza.zzbbI) {
            if (zza2.type == 14) {
                arrayList.add(null);
            } else if (i == 9) {
                DataMap dataMap = new DataMap();
                for (com.google.android.gms.internal.zzrm.zza zza3 : zza2.zzbby.zzbbH) {
                    zza(list, dataMap, zza3.name, zza3.zzbbw);
                }
                arrayList.add(dataMap);
            } else if (i == 2) {
                arrayList.add(zza2.zzbby.zzbbA);
            } else if (i != 6) {
                throw new IllegalArgumentException("Unexpected typeOfArrayList: " + i);
            } else {
                arrayList.add(Integer.valueOf(zza2.zzbby.zzbbE));
            }
        }
        return arrayList;
    }

    private static void zza(List<Asset> list, DataMap dataMap, String str, com.google.android.gms.internal.zzrm.zza.zza zza) {
        int i = zza.type;
        if (i != 14) {
            com.google.android.gms.internal.zzrm.zza.zza.zza zza2 = zza.zzbby;
            if (i == 1) {
                dataMap.putByteArray(str, zza2.zzbbz);
            } else if (i == 11) {
                dataMap.putStringArray(str, zza2.zzbbJ);
            } else if (i == 12) {
                dataMap.putLongArray(str, zza2.zzbbK);
            } else if (i == 15) {
                dataMap.putFloatArray(str, zza2.zzbbL);
            } else if (i == 2) {
                dataMap.putString(str, zza2.zzbbA);
            } else if (i == 3) {
                dataMap.putDouble(str, zza2.zzbbB);
            } else if (i == 4) {
                dataMap.putFloat(str, zza2.zzbbC);
            } else if (i == 5) {
                dataMap.putLong(str, zza2.zzbbD);
            } else if (i == 6) {
                dataMap.putInt(str, zza2.zzbbE);
            } else if (i == 7) {
                dataMap.putByte(str, (byte) zza2.zzbbF);
            } else if (i == 8) {
                dataMap.putBoolean(str, zza2.zzbbG);
            } else if (i != 13) {
                if (i == 9) {
                    DataMap dataMap2 = new DataMap();
                    for (com.google.android.gms.internal.zzrm.zza zza3 : zza2.zzbbH) {
                        zza(list, dataMap2, zza3.name, zza3.zzbbw);
                    }
                    dataMap.putDataMap(str, dataMap2);
                } else if (i != 10) {
                    throw new RuntimeException("populateBundle: unexpected type " + i);
                } else {
                    i = zza(str, zza2.zzbbI);
                    ArrayList zza4 = zza(list, zza2, i);
                    if (i == 14) {
                        dataMap.putStringArrayList(str, zza4);
                    } else if (i == 9) {
                        dataMap.putDataMapArrayList(str, zza4);
                    } else if (i == 2) {
                        dataMap.putStringArrayList(str, zza4);
                    } else if (i != 6) {
                        throw new IllegalStateException("Unexpected typeOfArrayList: " + i);
                    } else {
                        dataMap.putIntegerArrayList(str, zza4);
                    }
                }
            } else if (list != null) {
                dataMap.putAsset(str, (Asset) list.get((int) zza2.zzbbM));
            } else {
                throw new RuntimeException("populateBundle: unexpected type for: " + str);
            }
            return;
        }
        dataMap.putString(str, null);
    }

    private static com.google.android.gms.internal.zzrm.zza[] zza(DataMap dataMap, List<Asset> list) {
        TreeSet treeSet = new TreeSet(dataMap.keySet());
        com.google.android.gms.internal.zzrm.zza[] zzaArr = new com.google.android.gms.internal.zzrm.zza[treeSet.size()];
        Iterator it = treeSet.iterator();
        int i = 0;
        while (it.hasNext()) {
            String str = (String) it.next();
            Object obj = dataMap.get(str);
            zzaArr[i] = new com.google.android.gms.internal.zzrm.zza();
            zzaArr[i].name = str;
            zzaArr[i].zzbbw = zza((List) list, obj);
            i++;
        }
        return zzaArr;
    }
}
