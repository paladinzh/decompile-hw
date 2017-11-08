package com.android.server.pfw.autostartup.xmlparser;

class PreciseProviderParser extends AbsPreciseParser {
    PreciseProviderParser() {
    }

    protected int getPreciseType() {
        return 0;
    }

    protected String getXmlSubElementKey() {
        return "caller";
    }
}
