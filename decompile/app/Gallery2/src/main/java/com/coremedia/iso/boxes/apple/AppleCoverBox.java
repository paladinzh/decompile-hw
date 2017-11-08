package com.coremedia.iso.boxes.apple;

import java.util.logging.Logger;

public final class AppleCoverBox extends AbstractAppleMetaDataBox {
    private static Logger LOG = Logger.getLogger(AppleCoverBox.class.getName());

    public AppleCoverBox() {
        super("covr");
    }

    public String getValue() {
        return "---";
    }
}
