package com.coremedia.iso.boxes;

import com.coremedia.iso.IsoFile;
import java.util.List;

public interface ContainerBox extends Box {
    List<Box> getBoxes();

    <T extends Box> List<T> getBoxes(Class<T> cls);

    <T extends Box> List<T> getBoxes(Class<T> cls, boolean z);

    IsoFile getIsoFile();
}
