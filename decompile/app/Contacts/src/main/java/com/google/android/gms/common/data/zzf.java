package com.google.android.gms.common.data;

import java.util.ArrayList;

/* compiled from: Unknown */
public abstract class zzf<T> extends AbstractDataBuffer<T> {
    private boolean zzajw = false;
    private ArrayList<Integer> zzajx;

    protected zzf(DataHolder dataHolder) {
        super(dataHolder);
    }

    private void zzqh() {
        synchronized (this) {
            if (!this.zzajw) {
                int count = this.zzahi.getCount();
                this.zzajx = new ArrayList();
                if (count > 0) {
                    this.zzajx.add(Integer.valueOf(0));
                    String zzqg = zzqg();
                    int i = 1;
                    Object zzd = this.zzahi.zzd(zzqg, 0, this.zzahi.zzbH(0));
                    while (i < count) {
                        int zzbH = this.zzahi.zzbH(i);
                        String zzd2 = this.zzahi.zzd(zzqg, i, zzbH);
                        if (zzd2 != null) {
                            if (!zzd2.equals(zzd)) {
                                this.zzajx.add(Integer.valueOf(i));
                                String str = zzd2;
                            }
                            i++;
                        } else {
                            throw new NullPointerException("Missing value for markerColumn: " + zzqg + ", at row: " + i + ", for window: " + zzbH);
                        }
                    }
                }
                this.zzajw = true;
            }
        }
    }

    public final T get(int position) {
        zzqh();
        return zzk(zzbK(position), zzbL(position));
    }

    public int getCount() {
        zzqh();
        return this.zzajx.size();
    }

    int zzbK(int i) {
        if (i >= 0 && i < this.zzajx.size()) {
            return ((Integer) this.zzajx.get(i)).intValue();
        }
        throw new IllegalArgumentException("Position " + i + " is out of bounds for this buffer");
    }

    protected int zzbL(int i) {
        if (i < 0 || i == this.zzajx.size()) {
            return 0;
        }
        int intValue;
        int intValue2;
        if (i != this.zzajx.size() - 1) {
            intValue = ((Integer) this.zzajx.get(i + 1)).intValue();
            intValue2 = ((Integer) this.zzajx.get(i)).intValue();
        } else {
            intValue = this.zzahi.getCount();
            intValue2 = ((Integer) this.zzajx.get(i)).intValue();
        }
        intValue2 = intValue - intValue2;
        if (intValue2 == 1) {
            intValue = zzbK(i);
            int zzbH = this.zzahi.zzbH(intValue);
            String zzqi = zzqi();
            return (zzqi == null || this.zzahi.zzd(zzqi, intValue, zzbH) != null) ? intValue2 : 0;
        }
    }

    protected abstract T zzk(int i, int i2);

    protected abstract String zzqg();

    protected String zzqi() {
        return null;
    }
}
