package com.google.common.hash;

import com.google.common.annotations.VisibleForTesting;
import javax.annotation.Nullable;

@VisibleForTesting
final class Hashing$ConcatenatedHashFunction extends AbstractCompositeHashFunction {
    private final int bits;

    public boolean equals(@Nullable Object object) {
        if (!(object instanceof Hashing$ConcatenatedHashFunction)) {
            return false;
        }
        Hashing$ConcatenatedHashFunction other = (Hashing$ConcatenatedHashFunction) object;
        if (this.bits != other.bits || this.functions.length != other.functions.length) {
            return false;
        }
        for (int i = 0; i < this.functions.length; i++) {
            if (!this.functions[i].equals(other.functions[i])) {
                return false;
            }
        }
        return true;
    }

    public int hashCode() {
        int hash = this.bits;
        for (HashFunction function : this.functions) {
            hash ^= function.hashCode();
        }
        return hash;
    }
}
