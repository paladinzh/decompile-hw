package com.google.common.hash;

import com.google.common.annotations.Beta;

@Beta
public interface HashFunction {
    Hasher newHasher();

    Hasher newHasher(int i);
}
