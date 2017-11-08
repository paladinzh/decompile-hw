package com.common.imageloader.cache.disc.naming;

public class HashCodeFileNameGenerator implements FileNameGenerator {
    public String generate(String imageUri) {
        return String.valueOf(imageUri.hashCode());
    }
}
