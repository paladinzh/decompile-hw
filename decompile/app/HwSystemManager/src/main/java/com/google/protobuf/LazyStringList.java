package com.google.protobuf;

import java.util.List;

public interface LazyStringList extends List<String> {
    void add(ByteString byteString);

    ByteString getByteString(int i);

    List<?> getUnderlyingElements();
}
