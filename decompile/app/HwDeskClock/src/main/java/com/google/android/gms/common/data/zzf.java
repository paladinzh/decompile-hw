package com.google.android.gms.common.data;

import java.util.ArrayList;

/* compiled from: Unknown */
public abstract class zzf<T> extends AbstractDataBuffer<T> {
    private ArrayList<Integer> zzabA;
    private boolean zzabz = false;

    protected zzf(DataHolder dataHolder) {
        super(dataHolder);
    }

    private void zznY() {
        synchronized (this) {
            if (!this.zzabz) {
                int count = this.zzYX.getCount();
                this.zzabA = new ArrayList();
                if (count > 0) {
                    this.zzabA.add(Integer.valueOf(0));
                    String zznX = zznX();
                    int i = 1;
                    Object zzd = this.zzYX.zzd(zznX, 0, this.zzYX.zzbo(0));
                    while (i < count) {
                        int zzbo = this.zzYX.zzbo(i);
                        String zzd2 = this.zzYX.zzd(zznX, i, zzbo);
                        if (zzd2 != null) {
                            if (!zzd2.equals(zzd)) {
                                this.zzabA.add(Integer.valueOf(i));
                                String str = zzd2;
                            }
                            i++;
                        } else {
                            throw new NullPointerException("Missing value for markerColumn: " + zznX + ", at row: " + i + ", for window: " + zzbo);
                        }
                    }
                }
                this.zzabz = true;
            }
        }
    }

    public final T get(int position) {
        zznY();
        return zzk(zzbr(position), zzbs(position));
    }

    public int getCount() {
        zznY();
        return this.zzabA.size();
    }

    int zzbr(int i) {
        if (i >= 0 && i < this.zzabA.size()) {
            return ((Integer) this.zzabA.get(i)).intValue();
        }
        throw new IllegalArgumentException("Position " + i + " is out of bounds for this buffer");
    }

    protected int zzbs(int i) {
        if (i < 0 || i == this.zzabA.size()) {
            return 0;
        }
        int intValue;
        int intValue2;
        if (i != this.zzabA.size() - 1) {
            intValue = ((Integer) this.zzabA.get(i + 1)).intValue();
            intValue2 = ((Integer) this.zzabA.get(i)).intValue();
        } else {
            intValue = this.zzYX.getCount();
            intValue2 = ((Integer) this.zzabA.get(i)).intValue();
        }
        intValue2 = intValue - intValue2;
        if (intValue2 == 1) {
            intValue = zzbr(i);
            int zzbo = this.zzYX.zzbo(intValue);
            String zznZ = zznZ();
            return (zznZ == null || this.zzYX.zzd(zznZ, intValue, zzbo) != null) ? intValue2 : 0;
        }
    }

    protected abstract T zzk(int i, int i2);

    protected abstract String zznX();

    protected String zznZ() {
        return null;
    }
}
