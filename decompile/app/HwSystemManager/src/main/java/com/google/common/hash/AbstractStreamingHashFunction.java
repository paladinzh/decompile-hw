package com.google.common.hash;

import com.google.common.base.Preconditions;

abstract class AbstractStreamingHashFunction implements HashFunction {
    AbstractStreamingHashFunction() {
    }

    public Hasher newHasher(int expectedInputSize) {
        boolean z = false;
        if (expectedInputSize >= 0) {
            z = true;
        }
        Preconditions.checkArgument(z);
        return newHasher();
    }
}
