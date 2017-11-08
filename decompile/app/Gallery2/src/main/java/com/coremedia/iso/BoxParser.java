package com.coremedia.iso;

import com.coremedia.iso.boxes.Box;
import com.coremedia.iso.boxes.ContainerBox;
import java.io.IOException;
import java.nio.channels.ReadableByteChannel;

public interface BoxParser {
    Box parseBox(ReadableByteChannel readableByteChannel, ContainerBox containerBox) throws IOException;
}
